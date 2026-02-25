package ur_os;

import static ur_os.InterruptType.SCHEDULER_CPU_TO_RQ;

public class OS {

    ReadyQueue rq;
    IOQueue ioq;
    private static int process_count = 0;
    SystemOS system;
    CPU cpu;

    private SchedulerType currentSchedulerType;

    public final TieBreakerType SCHEDULER_TIEBREAKER_TYPE = TieBreakerType.LARGEST_PID;

    

    public OS(SystemOS system, CPU cpu, IOQueue ioq, SchedulerType schedulerType) {
        this.system = system;
        this.cpu = cpu;
        this.ioq = ioq;
        this.currentSchedulerType = schedulerType;
        this.rq = new ReadyQueue(this, currentSchedulerType);
    }

    public void update() {
        rq.update();
    }

    public int getTime() {
        return system.getTime();
    }

    public boolean isCPUEmpty() {
        return cpu.isEmpty();
    }

    public Process getProcessInCPU() {
        return cpu.getProcess();
    }

    public void interrupt(InterruptType t, Process p) {
        switch (t) {
            case CPU:
                if (p.isFinished()) {
                    p.setState(ProcessState.FINISHED);
                    p.setTime_finished(system.getTime());
                    System.out.println("Process " + p.getPid() + " finished!");
                } else {
                    ioq.addProcess(p);
                }
                break;

            case IO:
                rq.addProcess(p);
                break;

            case SCHEDULER_CPU_TO_RQ:
                Process temp = cpu.extractProcess();
                // mark context switch for the process leaving the CPU
                if (temp != null) temp.increaseContextSwitches();
                rq.addProcess(temp);
                if (p != null) {
                    // load the new process into CPU and mark its context switch
                    cpu.addProcess(p);
                    p.increaseContextSwitches();
                    // increment scheduler-level context switch counter
                    if (rq != null && rq.s != null) rq.s.addContextSwitch();
                    System.out.println("Process " + p.getPid() + " was loaded!");
                }
                break;

            case SCHEDULER_RQ_TO_CPU:
                cpu.addProcess(p);
                // mark context switch for the process being dispatched
                if (p.getFirstExecutionTime() == -1) {
                    p.setFirstExecutionTime(system.getTime()); // <-- aquí
                }
                if (p != null) p.increaseContextSwitches();
                if (rq != null && rq.s != null) rq.s.addContextSwitch();
                System.out.println("Process " + p.getPid() + " was loaded!");
                break;
        }
    }

    public void removeProcessFromCPU() {
        cpu.removeProcess();
    }

    public void create_process() {
        rq.addProcess(new Process(process_count++, system.getTime()));
    }

    public void create_process(Process p) {
        p.setPid(process_count++);
        rq.addProcess(p);
    }

    public void showProcesses() {
        System.out.println("Process list:");
        System.out.println(rq.toString());
    }
}
