package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

public class PriorityQueue extends Scheduler {

    int currentScheduler;
    private ArrayList<Scheduler> schedulers;

    PriorityQueue(OS os) {
        super(os);
        currentScheduler = -1;
        schedulers = new ArrayList<Scheduler>();
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

        if (!cpuEmpty && currentInCpu != null) {
            int prio = currentInCpu.getPriority();
            if (prio >= 0 && prio < schedulers.size()) {
                schedulers.get(prio).getNext(false);
            } else {
                schedulers.get(schedulers.size() - 1).getNext(false);
            }
        }

        defineCurrentScheduler();
        if (currentScheduler == -1) return; 

        if (os.isCPUEmpty()) {
            schedulers.get(currentScheduler).getNext(true);
        }
    }

    @Override
    public void newProcess(boolean cpuEmpty) {}

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {}

    @Override
    public boolean isEmpty() {
        for (Scheduler s : schedulers) {
            if (!s.isEmpty()) return false;
        }
        return true;
    }
}