package ur_os;

public class RoundRobin extends Scheduler {

    int q; 
    int cont; 
    boolean multiqueue;

    // 1. Constructor por defecto con multiqueue en falso según instrucción del profe
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
            // Si la CPU está vacía, intentamos cargar un proceso de esta cola
            if (!processes.isEmpty()) {
                Process p = processes.poll(); 
                // Registramos tiempo de respuesta si es la primera vez
                if (p.getFirstExecutionTime() == -1) {
                    p.setFirstExecutionTime(os.system.getTime());
                }
                os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, p);
                resetCounter();
            }
        } else {
            // Si la CPU NO está vacía, el proceso actual consume un tick de quantum
            cont++; 
            
            if (cont >= q) {
                // SE ACABÓ EL QUANTUM
                if (!multiqueue) {
                    // MODO SIMPLE: Carga automática (Interrumpe y mete el siguiente)
                    if (!processes.isEmpty()) {
                        Process next = processes.poll();
                        os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, next);
                        resetCounter();
                    } else {
                        // Si no hay nadie más, el proceso actual sigue (opcional según política)
                        resetCounter();
                    }
                } else {
                    // MODO MULTIQUEUE (Priority/MFQ): 
                    // No hacemos interrupción automática hacia la ReadyQueue general.
                    // Sacamos al proceso de la CPU para que quede vacía (cpuEmpty = true)
                    // y el PriorityQueue en su siguiente paso lo reubique.
                    Process temp = os.getProcessInCPU();
                    if (temp != null) {
                        os.removeProcessFromCPU();
                        // Devolvemos el proceso a la ReadyQueue del sistema, 
                        // la cual llamará al addProcess del PriorityQueue
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
        // En RR puro no suele haber preemption inmediata aquí, 
        // se espera al getNext.
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        // En RR puro no suele haber preemption inmediata aquí.
    }
}