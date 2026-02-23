/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ur_os;

import java.util.ArrayList;
import java.util.Arrays;
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

            processes.sort((p1, p2) ->
                Integer.compare(
                    p1.getRemainingTimeInCurrentBurst(),
                    p2.getRemainingTimeInCurrentBurst()
                )
            );

            Process shortest = processes.get(0);

            if (!cpuEmpty) {

                Process current = os.getProcessInCPU();

                if (shortest.getRemainingTimeInCurrentBurst() <
                    current.getRemainingTimeInCurrentBurst()) {

                    processes.remove(0); // remover antes

                    os.interrupt(
                        InterruptType.SCHEDULER_CPU_TO_RQ,
                        shortest
                    );

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

            processes.sort((p1, p2) ->
                Integer.compare(p1.getRemainingTimeInCurrentBurst(), p2.getRemainingTimeInCurrentBurst())
            );

            Process p = processes.get(0);
            processes.remove(0);

            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, p);
        }
    }
 
}
