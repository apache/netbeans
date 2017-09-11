/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009-2011 Sun Microsystems, Inc.
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

        public void start(int totalWork) {
            delegate.start(totalWork);
        }

        public void progress(int currentWorkDone) {
            delegate.progress(currentWorkDone);
        }

        public void progress(String message) {
            delegate.progress(message);
        }

        public void finish() {
            delegate.finish();
        }
    }

    private static final class AnalysisContextBasedProgressHandleAbstraction implements ProgressHandleAbstraction {
        private final Context delegate;
        AnalysisContextBasedProgressHandleAbstraction(Context delegate) {
            this.delegate = delegate;
        }

        public void start(int totalWork) {
            delegate.start(totalWork);
        }

        public void progress(int currentWorkDone) {
            delegate.progress(currentWorkDone);
        }

        public void progress(String message) {
            delegate.progress(message);
        }

        public void finish() {
            delegate.finish();
        }
    }

    private static final class ProgressHandleWrapperBasedProgressHandleAbstraction implements ProgressHandleAbstraction {
        private final ProgressHandleWrapper delegate;
        public ProgressHandleWrapperBasedProgressHandleAbstraction(ProgressHandleWrapper delegate) {
            this.delegate = delegate;
        }

        public void start(int totalWork) {
            delegate.startNextPart(totalWork);
        }

        public void progress(int currentWorkDone) {
            delegate.setCurrentPartWorkDone(currentWorkDone);
        }

        public void progress(String message) {
            delegate.setMessage(message);
        }

        public void finish() {}
    }

}
