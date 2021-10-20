/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rocks.imsofa.n2fproxy;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author lendle
 */
public class N2fExecutorImpl implements N2fExecutor {

    private boolean transactionStarted = false;
    private File homeDirectory = null;
    private File resultDirectory = null;
    private File n2fDirectoryInResult = null;
    private File statusFile = null;
    private File lockFile = null;
    private File dataDirectory = null;
    private List<N2fExecutorListener> listeners = new ArrayList<>();
    private StatusMonitorThread statusMonitorThread = new StatusMonitorThread();

    public N2fExecutorImpl(File homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    @Override
    public synchronized void init(File dataDirectory) {
        this.dataDirectory = dataDirectory;
        lockFile = new File(dataDirectory, ".n2fLock");
        if (lockFile.exists()) {
            throw new RuntimeException("cannot lock directory, unclean transactions may exist!");
        }
        try {
            FileUtils.touch(lockFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public synchronized void execute(long numSubspaces, long frequency, double theta, double phi) {
        try {
            statusMonitorThread.start();
            resultDirectory = new File(homeDirectory, "results_" + System.currentTimeMillis());
            FileUtils.forceMkdir(resultDirectory);
            n2fDirectoryInResult = new File(resultDirectory, ".n2f");
            FileUtils.forceMkdir(n2fDirectoryInResult);
            statusFile = new File(n2fDirectoryInResult, "status");
            FileUtils.write(statusFile, "initializing...", "utf-8", true);
            ProcessBuilder pb = new ProcessBuilder();
            pb = pb.directory(homeDirectory);
            pb = pb.redirectErrorStream(true);
            pb = pb.redirectOutput(ProcessBuilder.Redirect.to(new File("cpscript.log")));
            pb = pb.command("./cpscript", "" + numSubspaces, "" + frequency, dataDirectory.getAbsolutePath(), resultDirectory.getAbsolutePath());
            Process process = pb.start();
            process.waitFor();
            FileUtils.copyDirectory(new File(dataDirectory, "ap"), resultDirectory);
            FileUtils.write(statusFile, "RCS: Executing RCS_code.m...", "utf-8", true);

            pb = new ProcessBuilder();
            pb = pb.directory(homeDirectory);
            pb = pb.redirectErrorStream(true);
            pb = pb.redirectOutput(ProcessBuilder.Redirect.to(new File("RCS_code.log")));
            pb = pb.command("octave-cli", "RCS_code.m", resultDirectory.getAbsolutePath() + "/", "" + numSubspaces, "" + theta, "" + phi);
            process = pb.start();
            process.waitFor();
            statusMonitorThread.shutdown();
            statusMonitorThread = null;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public double[] getResults() {
        try {
            File resultFile = new File(resultDirectory, "RCS_total.csv");
            List<String> lines = FileUtils.readLines(resultFile, "utf-8");
            double[] results = new double[lines.size()];
            for (int i = 0; i < lines.size(); i++) {
                results[i] = Double.valueOf(lines.get(i));
            }
            return results;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void close() {
        try {
            FileUtils.deleteQuietly(lockFile);
            FileUtils.deleteDirectory(resultDirectory);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void addN2fExecutorListener(N2fExecutorListener l) {
        this.listeners.add(l);
    }

    class StatusMonitorThread extends Thread {

        private boolean running = true;
        private String lastString = null;

        public StatusMonitorThread() {
            this.setDaemon(true);
        }

        public void shutdown() {
            this.running = false;
        }

        public void run() {
            while (running) {
                try {
                    if (statusFile.exists()) {
                        String str = FileUtils.readFileToString(statusFile, "utf-8");
                        N2fExecutorEvent event = new N2fExecutorEvent();
                        if (str != null && str.trim().length() > 0) {
                            if (str.equals(lastString) == false) {
                                lastString = str;
                                if (str.startsWith("fast_n2f:")) {
                                    event.setStage("fast_n2f");
                                } else if (str.startsWith("RCS:")) {
                                    event.setStage("RCS");
                                } else {
                                    event.setStage("other");
                                }
                                event.setMessage(str);
                                for (N2fExecutorListener l : listeners) {
                                    l.statusUpdated(event);
                                }
                            }
                        }
                    }
                    Thread.sleep(250);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

}
