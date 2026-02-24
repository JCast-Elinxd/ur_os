package ur_os;

public class SJF_P extends Scheduler {

    SJF_P(OS os) {
        super(os);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
        checkPreemption(cpuEmpty);
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
        checkPreemption(cpuEmpty);
    }

    private void checkPreemption(boolean cpuEmpty) {

        if (cpuEmpty || processes.isEmpty()) {
            return;
        }

        Process current = os.getProcessInCPU();
        Process best = getBestProcess();

        if (best == null) return;

        if (best.getRemainingTimeInCurrentBurst() <
            current.getRemainingTimeInCurrentBurst()) {

            processes.remove(best);
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, best);
        }
        else if (best.getRemainingTimeInCurrentBurst() ==
                 current.getRemainingTimeInCurrentBurst()) {

            Process winner = tieBreaker(best, current);

            if (winner == best) {
                processes.remove(best);
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, best);
            }
        }
    }

    @Override
    public void getNext(boolean cpuEmpty) {

        if (processes.isEmpty()) return;

        Process current = os.getProcessInCPU();
        Process best = getBestProcess();

        if (cpuEmpty) {
            processes.remove(best);
            os.interrupt(InterruptType.SCHEDULER_RQ_TO_CPU, best);
            return;
        }

        if (best == current) return;

        if (best.getRemainingTimeInCurrentBurst() <
            current.getRemainingTimeInCurrentBurst()) {

            processes.remove(best);
            os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, best);
        }
        else if (best.getRemainingTimeInCurrentBurst() ==
                 current.getRemainingTimeInCurrentBurst()) {

            Process winner = tieBreaker(best, current);

            if (winner == best) {
                processes.remove(best);
                os.interrupt(InterruptType.SCHEDULER_CPU_TO_RQ, best);
            }
        }
    }

    private Process getBestProcess() {

        Process best = processes.getFirst();

        for (Process p : processes) {

            if (p.getRemainingTimeInCurrentBurst() <
                best.getRemainingTimeInCurrentBurst()) {

                best = p;

            } else if (p.getRemainingTimeInCurrentBurst() ==
                       best.getRemainingTimeInCurrentBurst()) {

                best = tieBreaker(p, best);
            }
        }

        return best;
    }
}