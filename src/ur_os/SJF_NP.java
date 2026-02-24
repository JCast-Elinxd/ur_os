/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package ur_os;

public class SJF_NP extends Scheduler {

    public SJF_NP(OS os) {
        super(os);
    }

    @Override
    public void getNext(boolean cpuEmpty) {

        if (!processes.isEmpty() && cpuEmpty) {
            
            Process shortest = processes.get(0);
            
            for (Process p : processes) {
                int tiempoP = p.getPbl().getRemainingTimeInCurrentBurst();
                int tiempoShortest = shortest.getPbl().getRemainingTimeInCurrentBurst();
                
                if (tiempoP < tiempoShortest) {
                    shortest = p;
                } 

                else if (tiempoP == tiempoShortest) {
                    shortest = tieBreaker(shortest, p);
                }
            }
            
            processes.remove(shortest);
            
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, shortest);
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {

    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    }
}
