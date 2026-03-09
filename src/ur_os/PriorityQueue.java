/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *
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
        // El proceso se añade al scheduler que le corresponde según su prioridad
        if (priority >= 0 && priority < schedulers.size()) {
            schedulers.get(priority).addProcess(p);
        } else {
            schedulers.get(schedulers.size() - 1).addProcess(p);
        }
    }

    void defineCurrentScheduler() {
        for (int i = 0; i < schedulers.size(); i++) {
            if (!schedulers.get(i).processes.isEmpty()) {
                currentScheduler = i;
                return;
            }
        }
        currentScheduler = -1;
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        defineCurrentScheduler();

        if (currentScheduler == -1) return;

        if (cpuEmpty) {
            schedulers.get(currentScheduler).getNext(true);
        } else {
            Process currentInCpu = os.getProcessInCPU();
            if (currentInCpu != null) {
                // EXPLICACIÓN: Si el índice del scheduler con procesos listos 
                // es menor que la prioridad del que está en CPU, hay que interrumpir.
                if (currentScheduler < currentInCpu.getPriority()) {
                    
                    // 1. Sacamos el proceso de la CPU
                    os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, null);
                    addContextSwitch();
                    
                    // 2. IMPORTANTE: Re-calculamos el scheduler actual 
                    // por si el proceso que salió cambió algo
                    defineCurrentScheduler();
                    
                    // 3. Despachamos el de mayor prioridad
                    schedulers.get(currentScheduler).getNext(true);
                }
            }
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        // Obligamos a revisar la prioridad apenas entra un proceso nuevo
        getNext(cpuEmpty);
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        // Obligamos a revisar la prioridad cuando un proceso vuelve de IO
        getNext(cpuEmpty);
    }
}