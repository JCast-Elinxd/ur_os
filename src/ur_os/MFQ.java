package ur_os;

import java.util.ArrayList;
import java.util.Arrays;

public class MFQ extends Scheduler {

    private ArrayList<Scheduler> schedulers;
    private int currentScheduler;

    MFQ(OS os) {
        super(os);
        schedulers = new ArrayList<>();
        currentScheduler = -1;
    }

    MFQ(OS os, Scheduler... s) {
        this(os);
        schedulers.addAll(Arrays.asList(s));

        if (!schedulers.isEmpty())
            currentScheduler = 0;
    }

    @Override
    public void addProcess(Process p) {

        p.setCurrentScheduler(0);

        schedulers.get(0).addProcess(p);
    }

    private void defineCurrentScheduler() {

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

        if (cpuEmpty) {

            defineCurrentScheduler();

            if (currentScheduler == -1)
                return;
        }

        Scheduler s = schedulers.get(currentScheduler);

        s.getNext(cpuEmpty);
    }

    @Override
    public void newProcess(boolean cpuEmpty) {
    }

    @Override
    public void IOReturningProcess(boolean cpuEmpty) {
    }
}