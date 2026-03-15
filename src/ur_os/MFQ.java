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
                if (os.isCPUEmpty()) {
                    getNext(true);
                }
            }
        }
    }

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

        // Preemption estricta: solo nivel estrictamente menor
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

    @Override
    public void getNext(boolean cpuEmpty) {
        if (schedulers.isEmpty()) return;

        if (currentScheduler == -1 || schedulers.get(currentScheduler).isEmpty()) {
            defineCurrentScheduler();
        }

        if (currentScheduler == -1) return;

        schedulers.get(currentScheduler).getNext(cpuEmpty);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    }

    private void defineCurrentScheduler() {
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).isEmpty()) {
                // Siempre resetear al seleccionar una cola para cargar:
                // cubre tanto cambio de cola como reactivación de la misma cola
                // tras haber estado vacía (y con cont acumulado de antes)
                resetSchedulerCounter(i);
                if (i != currentScheduler) {
                    currentScheduler = i;
                }
                return;
            }
        }
        currentScheduler = -1;
    }

    private void resetSchedulerCounter(int index) {
        if (index >= 0 && index < schedulers.size()) {
            Scheduler s = schedulers.get(index);
            if (s instanceof RoundRobin) ((RoundRobin) s).resetCounter();
        }
    }

}