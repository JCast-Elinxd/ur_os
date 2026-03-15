package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

public class MFQ extends Scheduler {

    private final ArrayList<Scheduler> schedulers;
    private int currentScheduler;

    MFQ(OS os) {
        super(os);
        schedulers       = new ArrayList<>();
        currentScheduler = -1;
    }

    MFQ(OS os, Scheduler... s) {
        this(os);
        if (s != null) schedulers.addAll(Arrays.asList(s));
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Override
    public void update() {
        if (os.isCPUEmpty()) {
            getNext(true);
        } else {
            if (currentScheduler != -1 && !schedulers.isEmpty()) {
                Process inCPU = os.getProcessInCPU();
                if (inCPU == null || inCPU.getCurrentScheduler() == currentScheduler) {
                    schedulers.get(currentScheduler).getNext(false);
                }
                // Después del tick, si la CPU quedó vacía (RR expulsó al proceso
                // en modo multiqueue), cargamos el siguiente inmediatamente
                // para no perder el ciclo.
                if (os.isCPUEmpty()) {
                    getNext(true);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // addProcess
    // -------------------------------------------------------------------------

    @Override
    public void addProcess(Process p) {
        if (p == null || schedulers.isEmpty()) return;

        int level = p.getCurrentScheduler();

        switch (p.getState()) {
            case NEW:
                level = 0;
                break;

            case IO:
                level = Math.max(0, level - 1);
                break;

            case CPU:
                level = Math.min(schedulers.size() - 1, level + 1);
                break;

            case READY:
                // RoundRobin multiqueue devuelve el proceso en READY sin cambiar
                // el estado. Si viene de la cola activa = fue expulsado por quantum,
                // debe bajar un nivel.
                if (level == currentScheduler) {
                    level = Math.min(schedulers.size() - 1, level + 1);
                } else if (level < 0 || level >= schedulers.size()) {
                    level = 0;
                }
                break;

            default:
                if (level < 0 || level >= schedulers.size()) level = 0;
                break;
        }

        p.setCurrentScheduler(level);
        p.setState(ProcessState.READY);
        schedulers.get(level).addProcess(p);

        // Preemption estricta: proceso nuevo con mayor prioridad expulsa al de CPU
        if (!os.isCPUEmpty()) {
            Process inCPU = os.getProcessInCPU();
            if (inCPU != null) {
                int cpuLevel = inCPU.getCurrentScheduler();
                if (level < cpuLevel) {
                    inCPU.setState(ProcessState.CPU);
                    os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // getNext
    // -------------------------------------------------------------------------

    @Override
    public void getNext(boolean cpuEmpty) {
        if (schedulers.isEmpty()) return;

        if (currentScheduler == -1 || schedulers.get(currentScheduler).isEmpty()) {
            defineCurrentScheduler();
        }

        if (currentScheduler == -1) return;

        schedulers.get(currentScheduler).getNext(cpuEmpty);
    }

    // -------------------------------------------------------------------------
    // newProcess / IOReturningProcess
    // -------------------------------------------------------------------------

    @Override
    public void newProcess(boolean cpuEmpty) {
        Process p = os.getNewProcessFromQueue();
        if (p != null) {
            p.setState(ProcessState.NEW);
            addProcess(p);
            // Si la CPU estaba vacía, cargar inmediatamente sin esperar al
            // siguiente ciclo — esto elimina el Response Time de 1.
            if (os.isCPUEmpty()) {
                getNext(true);
            }
        }
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        Process p = os.getReturningProcess();
        if (p != null) {
            p.setState(ProcessState.IO);
            addProcess(p);
            if (cpuEmpty && os.isCPUEmpty()) {
                getNext(true);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void defineCurrentScheduler() {
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).isEmpty()) {
                if (i != currentScheduler) {
                    resetSchedulerCounter(i);
                    currentScheduler = i;
                    log("Scheduler activo -> cola " + currentScheduler);
                }
                return;
            }
        }
        currentScheduler = -1;
        log("Scheduler activo -> ninguna cola tiene procesos (idle)");
    }

    private void resetSchedulerCounter(int index) {
        if (index >= 0 && index < schedulers.size()) {
            Scheduler s = schedulers.get(index);
            if (s instanceof RoundRobin) ((RoundRobin) s).resetCounter();
        }
    }

    private void log(String msg) {
        System.out.println("[MFQ] " + msg);
    }
}