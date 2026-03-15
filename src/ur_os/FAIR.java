package ur_os;

import java.util.Random;
import java.util.LinkedList;

public class FAIR extends Scheduler {
    private Random random;

    public FAIR(OS os) {
        super(os);
        this.random = new Random(); 
        this.processes = new LinkedList<Process>(); 
    }

  @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty && !processes.isEmpty()) {
            
            int totalTickets = 0;
            
            System.out.println("\n========================================");
            System.out.println("[LOTTERY] --- Iniciando nuevo sorteo ---");
            
            for (Process p : processes) {
                int ticketsDelProceso = (11 - p.getPriority());
                totalTickets += ticketsDelProceso;
                
                System.out.println("[TICKETS] PID: " + p.getPid() + 
                                   " (Prioridad: " + p.getPriority() + 
                                   ") -> Boletos: " + ticketsDelProceso);
            }
            
            int winnerTicket = random.nextInt(totalTickets);
            System.out.println("[SORTEO] Boleto ganador: #" + winnerTicket + " de un total de " + totalTickets);

            int currentSum = 0;
            Process winner = null;
            for (Process p : processes) {
                currentSum += (11 - p.getPriority());
                if (currentSum > winnerTicket) {
                    winner = p;
                    break;
                }
            }

            if (winner != null) {
                System.out.println("[GANADOR] El proceso elegido es el PID: " + winner.getPid());
                System.out.println("========================================\n");
                
                processes.remove(winner);
                if (winner.getFirstExecutionTime() == -1) {
                    winner.setFirstExecutionTime(os.system.getTime());
                }
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, winner);
            }
        }
    }

    @Override
    public void addProcess(Process p) {
        if (this.processes == null) {
            this.processes = new LinkedList<Process>();
        }
        this.processes.add(p);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    }
}