/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author prestamour
 */
public class SJF_P extends Scheduler{

    
    SJF_P(OS os){
        super(os);

    }

    @Override
    public void newProcess(boolean cpuEmpty) {

        if (!processes.isEmpty()) {

            processes.sort(
                Comparator
                    .comparingInt(Process::getRemainingTimeInCurrentBurst)
                    .thenComparingInt(Process::getPid)
            );

            Process shortest = processes.get(0);

            if (!cpuEmpty) {
                Process current = os.getProcessInCPU();

                int cmp = Comparator
                    .comparingInt(Process::getRemainingTimeInCurrentBurst)
                    .thenComparingInt(Process::getPid)
                    .compare(shortest, current);

                if (cmp < 0) {  // estrictamente mejor
                    processes.remove(0);
                    os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, shortest);
                    return;
                }
            }
        }

        getNext(cpuEmpty);
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        newProcess(cpuEmpty);
    }
    
   
    @Override
    public void getNext(boolean cpuEmpty) {
        if (!processes.isEmpty() && cpuEmpty) {
            processes.sort(
                Comparator
                    .comparingInt(Process::getRemainingTimeInCurrentBurst)
                    .thenComparingInt(Process::getPid)
            );
            Process p = processes.get(0);
            processes.remove(0);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, p);
        }
    }
 
}
