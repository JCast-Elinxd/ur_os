package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Priority Queue Scheduler
 * @author prestamour
 */
public class PriorityQueue extends Scheduler {

    int currentScheduler;
    private ArrayList<Scheduler> schedulers;

    PriorityQueue(OS os) {
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList();
    }

    PriorityQueue(OS os, Scheduler... s) {
        this(os);
        schedulers.addAll(Arrays.asList(s));
        if (s.length > 0) {
            currentScheduler = 0;
        }
    }

    @Override
    public void addProcess(Process p) {
        int priority = p.getPriority();
        
        // Asignamos el proceso a su cola correspondiente
        if (priority >= 0 && priority < schedulers.size()) {
            schedulers.get(priority).addProcess(p);
        } else {
            schedulers.get(schedulers.size() - 1).addProcess(p);
        }
    }

    void defineCurrentScheduler() {
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).isEmpty()) {
                currentScheduler = i;
                return;
            }
        }
        currentScheduler = -1;
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        Process currentInCpu = os.getProcessInCPU();

        // 1. AVANZAR EL SUB-SCHEDULER ACTUAL
        // Esto permite que el Round Robin interno descuente su quantum
        if (!cpuEmpty && currentInCpu != null) {
            int prio = currentInCpu.getPriority();
            if (prio >= 0 && prio < schedulers.size()) {
                schedulers.get(prio).getNext(false);
            } else {
                schedulers.get(schedulers.size() - 1).getNext(false);
            }
        }

        // 2. RE-EVALUAR EL ESTADO DESPUÉS DE AVANZAR
        defineCurrentScheduler();
        if (currentScheduler == -1) return; // No hay procesos pendientes

        if (os.isCPUEmpty()) {
            // Si el sub-scheduler vació la CPU (ej. se acabó el quantum de RR), 
            // le damos paso al proceso de mayor prioridad disponible.
            schedulers.get(currentScheduler).getNext(true);
        } else {
            // Si la CPU sigue ocupada, validamos si alguien de mayor prioridad quiere entrar
            currentInCpu = os.getProcessInCPU(); // Actualizamos la variable por si hubo cambios
            
            if (currentInCpu != null && currentScheduler < currentInCpu.getPriority()) {
                // Expulsión directa (Preemption)
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                schedulers.get(currentScheduler).getNext(true);
            }
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {} //Non-preemtive in this event

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {} //Non-preemtive in this event
}