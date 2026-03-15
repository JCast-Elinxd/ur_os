package ur_os;

public class RoundRobin extends Scheduler {

    int q; 
    int cont; 
    boolean multiqueue;

    RoundRobin(OS os) {
        super(os);
        q = 5; 
        cont = 0;
        this.multiqueue = false;
    }

    RoundRobin(OS os, int q) {
        this(os);
        this.q = q;
    }

    RoundRobin(OS os, int q, boolean multiqueue) {
        this(os);
        this.q = q;
        this.multiqueue = multiqueue;
    }

    void resetCounter() {
        cont = 0;
    }

    @Override
    public void getNext(boolean cpuEmpty) {
        if (cpuEmpty) {
            if (!processes.isEmpty()) {
                Process p = processes.poll(); 
                if (p.getFirstExecutionTime() == -1) {
                    p.setFirstExecutionTime(os.system.getTime());
                }
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, p);
                resetCounter();
            }
        } else {
            cont++; 
            
            if (cont >= q) {
                if (!multiqueue) {
                    if (!processes.isEmpty()) {
                        Process next = processes.poll();
                        os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, next);
                        resetCounter();
                    } else {
                        resetCounter();
                    }
                } else {

                    Process temp = os.getProcessInCPU();
                    if (temp != null) {
                        os.removeProcessFromCPU();
                        os.rq.addProcess(temp); 
                        resetCounter();
                    }
                }
            }
        }
    }

    @Override
    public void addProcess(Process p) {
        processes.add(p);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {

    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {

    }
}