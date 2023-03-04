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
package org.netbeans.modules.java.hints.spiimpl.batch;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.java.hints.spiimpl.Utilities;

public final class ProgressHandleWrapper {

    private static final int TOTAL = 1000;
    private final ProgressHandleAbstraction handle;
    private final int[] parts;
    private int currentPart = -1;
    private int currentPartTotalWork;
    private int currentPartWorkDone;
    private long currentPartStartTime;
    private int currentOffset;
    private final long[] spentTime;
    private boolean debug;

    public ProgressHandleWrapper(int... parts) {
        this((ProgressHandleAbstraction) null, parts);
    }

    public ProgressHandleWrapper(ProgressHandle handle, int... parts) {
        this(new ProgressHandleBasedProgressHandleAbstraction(handle), parts);
    }

    public ProgressHandleWrapper(Context handle, int... parts) {
        this(new AnalysisContextBasedProgressHandleAbstraction(handle), parts);
    }

    public ProgressHandleWrapper(ProgressHandleAbstraction handle, int... parts) {
        this.handle = handle;
        if (handle == null) {
            this.parts = null;
            this.spentTime = null;
        } else {
            int total = 0;
            for (int i : parts) {
                total += i;
            }
            this.parts = new int[parts.length];
            for (int cntr = 0; cntr < parts.length; cntr++) {
                this.parts[cntr] = (TOTAL * parts[cntr]) / total;
            }
            this.spentTime = new long[parts.length];
        }
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void startNextPart(int totalWork) {
        if (handle == null) {
            return;
        }
        if (currentPart == (-1)) {
            handle.start(TOTAL);
        } else {
            currentOffset += parts[currentPart];
            spentTime[currentPart] = System.currentTimeMillis() - currentPartStartTime;
        }
        currentPart++;
        currentPartTotalWork = totalWork;
        currentPartWorkDone = 0;
        currentPartStartTime = System.currentTimeMillis();
        currentPartWorkDoneUpdated();
    }

    public ProgressHandleWrapper startNextPartWithEmbedding(int... embeddedParts) {
//        startNextPart(TOTAL);
        return new ProgressHandleWrapper(new ProgressHandleWrapperBasedProgressHandleAbstraction(this), embeddedParts);
    }

    public void tick() {
        if (handle == null) {
            return;
        }
        currentPartWorkDone++;
        currentPartWorkDoneUpdated();
    }

    private void setCurrentPartWorkDone(int done) {
        if (handle == null) {
            return;
        }
        currentPartWorkDone = done;
        currentPartWorkDoneUpdated();
    }

    private void currentPartWorkDoneUpdated() {
        if (currentPartTotalWork > 0) {
            int parentProgress = currentOffset + (parts[currentPart] * currentPartWorkDone) / currentPartTotalWork;
            if (debug) {
                System.err.println("currentOffset=" + currentOffset);
                System.err.println("currentPart=" + currentPart);
                System.err.println("parts[currentPart]= " +parts[currentPart]);
                System.err.println("currentPartWorkDone=" + currentPartWorkDone);
                System.err.println("currentPartTotalWork= " +currentPartTotalWork);
                System.err.println("parentProgress=" + parentProgress);
            }
            handle.progress(parentProgress);
        } else {
            handle.progress(currentOffset + parts[currentPart]);
        }
        setAutomatedMessage();
    }

    public void setMessage(String message) {
        if (handle == null) {
            return;
        }
        handle.progress(message);
    }

    private void setAutomatedMessage() {
        if (handle == null || currentPart == (-1)) {
            return;
        }
        long spentTime = System.currentTimeMillis() - currentPartStartTime;
        double timePerUnit = ((double) spentTime) / currentPartWorkDone;
        String timeString;
        if (spentTime > 0) {
            double totalTime = currentPartTotalWork * timePerUnit;
            timeString = Utilities.toHumanReadableTime(spentTime) + "/" + Utilities.toHumanReadableTime(totalTime);
        } else {
            timeString = "No estimate";
        }
        handle.progress("Part " + (currentPart + 1) + "/" + parts.length + ", " + currentPartWorkDone + "/" + currentPartTotalWork + ", " + timeString);
    }

    public void finish() {
        if (handle == null) {
            return ;
        }

        handle.finish();

        if (currentPart < 0) return ;
        
        spentTime[currentPart] = System.currentTimeMillis() - currentPartStartTime;

        double total = 0.0;

        for (long t : spentTime) {
            total += t;
        }

        double[] actualSplit = new double[spentTime.length];
        int i = 0;

        for (long t : spentTime) {
            actualSplit[i++] = TOTAL * (t / total);
        }

        Logger.getLogger(ProgressHandleWrapper.class.getName()).log(Level.FINE, "Progress handle with split: {0}, actual times: {1}, actual split: {2}", new Object[] {Arrays.toString(parts), Arrays.toString(spentTime), Arrays.toString(actualSplit)});
    }

    public static int[] prepareParts(int count) {
        int[] result = new int[count];

        for (int cntr = 0; cntr < count; cntr++) {
            result[cntr] = 1;
        }

        return result;
    }

    public static interface ProgressHandleAbstraction {

        public void start(int totalWork);

        public void progress(int currentWorkDone);

        public void progress(String message);

        public void finish();

    }

    private static final class ProgressHandleBasedProgressHandleAbstraction implements ProgressHandleAbstraction {
        private final ProgressHandle delegate;
        public ProgressHandleBasedProgressHandleAbstraction(ProgressHandle delegate) {
            this.delegate = delegate;
        }

        @Override
        public void start(int totalWork) {
            delegate.start(totalWork);
        }

        @Override
        public void progress(int currentWorkDone) {
            delegate.progress(currentWorkDone);
        }

        @Override
        public void progress(String message) {
            delegate.progress(message);
        }

        @Override
        public void finish() {
            delegate.finish();
        }
    }

    private static final class AnalysisContextBasedProgressHandleAbstraction implements ProgressHandleAbstraction {
        private final Context delegate;
        AnalysisContextBasedProgressHandleAbstraction(Context delegate) {
            this.delegate = delegate;
        }

        @Override
        public void start(int totalWork) {
            delegate.start(totalWork);
        }

        @Override
        public void progress(int currentWorkDone) {
            delegate.progress(currentWorkDone);
        }

        @Override
        public void progress(String message) {
            delegate.progress(message);
        }

        @Override
        public void finish() {
            delegate.finish();
        }
    }

    private static final class ProgressHandleWrapperBasedProgressHandleAbstraction implements ProgressHandleAbstraction {
        private final ProgressHandleWrapper delegate;
        public ProgressHandleWrapperBasedProgressHandleAbstraction(ProgressHandleWrapper delegate) {
            this.delegate = delegate;
        }

        @Override
        public void start(int totalWork) {
            delegate.startNextPart(totalWork);
        }

        @Override
        public void progress(int currentWorkDone) {
            delegate.setCurrentPartWorkDone(currentWorkDone);
        }

        @Override
        public void progress(String message) {
            delegate.setMessage(message);
        }

        @Override
        public void finish() {}
    }

}
