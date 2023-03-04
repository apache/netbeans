/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.nativeexecution.api.util;

import com.jcraft.jsch.Channel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.jsch.MeasurableSocketFactory;
import org.netbeans.modules.nativeexecution.jsch.MeasurableSocketFactory.IOListener;
import org.openide.modules.OnStop;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;

@OnStop
public final class RemoteStatistics implements Callable<Boolean> {

    public static final boolean COLLECT_STATISTICS = Boolean.parseBoolean(System.getProperty("jsch.statistics", "false")); // NOI18N
    public static final boolean COLLECT_TRAFFIC = Boolean.parseBoolean(System.getProperty("jsch.statistics.traffic", "true")); // NOI18N
    public static final boolean COLLECT_STACKS = COLLECT_STATISTICS && Boolean.parseBoolean(System.getProperty("jsch.statistics.stacks", "false")); // NOI18N
    private static final String BREAK_UPLOADS_FLAG_FILE = System.getProperty("break.uploads"); // NOI18N
    private static final TrafficCounters trafficCounters = new TrafficCounters();
    private static final RemoteMeasurementsRef unnamed = new RemoteMeasurementsRef("uncategorized", new RemoteMeasurements("uncategorized"), null, 0); // NOI18N
    private static final AtomicReference<RemoteMeasurementsRef> currentStatRef = new AtomicReference<>(unnamed);
    private static final BlockingQueue<Task> queue = new ArrayBlockingQueue<>(1);
    private static final RemoteIOListener listener = new RemoteIOListener();
    private static final AtomicBoolean trafficDetected = new AtomicBoolean();

    static {
        if (COLLECT_STATISTICS && COLLECT_TRAFFIC) {
            MeasurableSocketFactory.getInstance().addIOListener(listener);
        }
    }
    
    public abstract static class ActivityID {
        /*package*/ ActivityID() {}
    }


    public RemoteStatistics() {
    }

    /**
     * Start a new measurement.
     *
     * It is assumed that this method is called before an action that results in
     * network activity.
     *
     * When this method is called it:
     *
     * <ul> <li>blocks thread waiting that all current network activity is done
     * (quietPrePeriodMillis without any traffic).</li>
     *
     * <li>associates all further traffic with a named (see description)
     * 'counters'</li>
     *
     * <li>once quietPostPeriodMillis passed without any traffic it dumps named
     * (see description) 'counters' into appropriate file.</li> </ul>
     *
     * Note that there could not be several measurements done in parallel.
     * Subsequent call to startTest() will block called thread until after
     * previous measurement is done.
     *
     * @TheadSafe
     *
     * @param description - description to be used in output file name
     * @param continuation - runnable to invoke once measurements are done
     * @param quietPrePeriodMillis - quiet period (in milliseconds) of network
     * inactivity before measurements start.
     * @param quietPostPeriodMillis - quiet period (in milliseconds) of network
     * inactivity before consider that measurements are done.
     */
    public static void startTest(final String description, final Runnable continuation, final int quietPrePeriodMillis, final int quietPostPeriodMillis) {
        if (!COLLECT_STATISTICS) {
            if (continuation != null) {
                continuation.run();
            }
            return;
        }

        final Task newTask = RequestProcessor.getDefault().create(new Runnable() {

            @Override
            public void run() {
                try {
                    stopAction();
                    queue.poll();
                } finally {
                    if (continuation != null) {
                        continuation.run();
                    }
                }
            }
        });

        try {
            queue.put(newTask);
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }

        // At this point only a single thread runs.

        while (true) {
            synchronized (trafficDetected) {
                try {
                    if (quietPrePeriodMillis > 0) {
                        trafficDetected.wait(quietPrePeriodMillis);
                    }
                    if (!trafficDetected.getAndSet(false)) {
                        break;
                    }
                } catch (InterruptedException ex) {
                    break;
                }
            }
        }

        newTask.schedule(quietPostPeriodMillis);
        currentStatRef.set(new RemoteMeasurementsRef(description, new RemoteMeasurements(description), newTask, quietPostPeriodMillis));
    }

    static boolean stopAction() {
        RemoteMeasurementsRef stopped = currentStatRef.getAndSet(unnamed);
        PrintStream output = getOutput(stopped.name);
        try {
            stopped.stat.dump(output);
        } finally {
            if (!output.equals(System.out)) {
                output.close();
            }
        }
        return unnamed.equals(stopped);
    }

    public static ActivityID startChannelActivity(CharSequence category, CharSequence... args) {
        if (!COLLECT_STATISTICS) {
            return null;
        }
        return currentStatRef.get().stat.startChannelActivity(category, args);
    }

    public static void stopChannelActivity(RemoteStatistics.ActivityID activityID) {
        stopChannelActivity(activityID, 0);
    }

    public static void stopChannelActivity(RemoteStatistics.ActivityID activityID, long supposedTraffic) {
        if (!COLLECT_STATISTICS) {
            return;
        }
        if (activityID == null) {
            return;
        }
        currentStatRef.get().stat.stopChannelActivity(activityID, supposedTraffic);
    }

    private static RemoteMeasurementsRef reschedule() {
        RemoteMeasurementsRef ref = currentStatRef.get();
        if (ref.task != null) {
            ref.task.schedule(ref.quietPostPeriodMillis);
        }
        return ref;
    }

    private static PrintStream getOutput(String name) {
        String OUTPUT = COLLECT_STATISTICS ? System.getProperty("jsch.statistics.output", null) : null; // NOI18N
        if (OUTPUT == null) {
            return System.out;
        }
        try {
            File dir = new File(OUTPUT);
            if (!dir.isDirectory()) {
                throw new IOException(OUTPUT + " is not a directory!"); // NOI18N
            }
            if (!dir.canWrite()) {
                throw new IOException(OUTPUT + " is not writable!"); // NOI18N
            }
            return new PrintStream(new File(dir, name));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return System.out;
        }
    }

    @Override
    public Boolean call() throws Exception {
        if (COLLECT_STATISTICS) {
            while (!stopAction()) {
            }

            PrintStream output = getOutput("totalTraffic"); // NOI18N
            try {
                trafficCounters.dump(output); // NOI18N
            } finally {
                if (!output.equals(System.out)) {
                    output.close();
                }
            }
        }

        return true;
    }

    private static class TrafficCounters {

        private final AtomicLong up = new AtomicLong();
        private final AtomicLong down = new AtomicLong();

        public TrafficCounters() {
            if (!COLLECT_STATISTICS) {
                return;
            }
            MeasurableSocketFactory.getInstance().addIOListener(new IOListener() {

                @Override
                public void bytesUploaded(int bytes) {
                    up.addAndGet(bytes);
                    synchronized (trafficDetected) {
                        trafficDetected.set(true);
                        trafficDetected.notifyAll();
                    }
                    reschedule();
                }

                @Override
                public void bytesDownloaded(int bytes) {
                    down.addAndGet(bytes);
                    synchronized (trafficDetected) {
                        trafficDetected.set(true);
                        trafficDetected.notifyAll();
                    }
                    reschedule();
                }
            });
        }

        private void dump(PrintStream out) {
            out.println("Total upload traffic [bytes]: " + up.get()); // NOI18N
            out.println("Total download traffic [bytes]: " + down.get()); // NOI18N
        }
    }

    private static class RemoteIOListener implements IOListener {

        @Override
        public void bytesUploaded(int bytes) {
            RemoteMeasurementsRef stat = reschedule();
            stat.stat.bytesUploaded(bytes);
            checkBreakUploads();
        }

        /** Allows broken upload testing  */
        private void checkBreakUploads() {
            if (BREAK_UPLOADS_FLAG_FILE != null && new File(BREAK_UPLOADS_FLAG_FILE).exists()) {
                boolean isOpenW = false;
                for (StackTraceElement el : Thread.currentThread().getStackTrace()) {
                    if (el.getClassName().endsWith(".ChannelSftp")) { // NOI18N
                        if (el.getMethodName().equals("sendOPENW")) { // NOI18N
                            isOpenW = true;
                            break;
                        }
                    }
                }
                if (isOpenW) {
                    List<ExecutionEnvironment> recentConnections = ConnectionManager.getInstance().getRecentConnections();
                    for (ExecutionEnvironment env : recentConnections) {
                        ConnectionManager.getInstance().disconnect(env);
                    }
                }
            }
        }

        @Override
        public void bytesDownloaded(int bytes) {
            RemoteMeasurementsRef stat = reschedule();
            stat.stat.bytesDownloaded(bytes);
        }
    }

    private static class RemoteMeasurementsRef {

        private final String name;
        private final int quietPostPeriodMillis;
        private final Task task;
        private final RemoteMeasurements stat;

        public RemoteMeasurementsRef(String name, RemoteMeasurements stat, Task task, int quietPostPeriodMillis) {
            this.name = name + "_" + System.currentTimeMillis(); // NOI18N
            this.task = task;
            this.stat = stat;
            this.quietPostPeriodMillis = quietPostPeriodMillis;
        }
    }
}
