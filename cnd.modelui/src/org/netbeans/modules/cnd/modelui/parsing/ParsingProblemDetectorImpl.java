/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.modelui.parsing;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.spi.model.services.CodeModelProblemResolver.ParsingProblemDetector;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.NbBundle;

/**
 * Detector of parsing problems. Class watches used memory while project parsing.
 * If algorithm detects slowdown parsing speed class shows alert message with advice to increase java heap size.
 * 
 */
public class ParsingProblemDetectorImpl implements ParsingProblemDetector {

    private static final Logger LOG = Logger.getLogger("cnd.parsing.problem.detector"); // NOI18N
    public static final boolean TIMING = /*TraceFlags.TIMING*/Boolean.getBoolean("cnd.modelimpl.timing"); // NOI18N
    private static final int Mb = 1024 * 1024;
    private static final int timeThreshold = 1000*60;
    private final Runtime runtime;
    public final int maxMemory;
    private final int startMemory;
    private int lineCount;
    private long startTime;
    private AverageSpeed averageSpeed;
    private final List<Measure> measures;
    private final int memoryThreshold;
    private final CsmProject project;
    private long remainingTime = 0;
    private static boolean isDialogShown = false;

    /**
     * Constructs progress information for project
     */
    public ParsingProblemDetectorImpl(CsmProject project) {
        runtime = Runtime.getRuntime();
        maxMemory = (int) (runtime.maxMemory() / Mb);
        memoryThreshold = Math.max(maxMemory/10, 10);
        startMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / Mb);
        measures = new ArrayList<Measure>();
        this.project = project;
    }

    @Override
    public void start() {
    }

    @Override
    public void finish() {
        if (measures.size() > 1) {
            int lines = measures.get(measures.size()-1).lines;
            if (lines > 0) {
                int parsingTime = measures.get(measures.size()-1).time;
                if (parsingTime > 0) {
                    int parsingMemory = 0;
                    for(Measure m : measures) {
                        parsingMemory = Math.max(parsingMemory, m.memory);
                    }
                    StringBuilder buf = new StringBuilder();
                    buf.append("Parsing statistic of ").append(project.getDisplayName()).append(":\n");// NOI18N
                    buf.append("Parsed ").append(lines/1000).append(" KLines, Time ").append(parsingTime/1000).append(" seconds, Speed ").append(lines/parsingTime).append(" KLines/second, Max Memory ").append(parsingMemory).append(" Mb\n"); // NOI18N
                    int currentPercent = 1;
                    int curentTime = 0;
                    int curentLines = 0;
                    buf.append("Work, %\t\tSpeed, KLines/second\tMemory, Mb\n"); // NOI18N
                    for(Measure m : measures) {
                        int p = m.lines*100/lines;
                        if (p - currentPercent*5 >= 0) {
                            int l = m.lines - curentLines;
                            curentLines = m.lines;
                            int t = m.time - curentTime;
                            curentTime = m.time;
                            currentPercent++;
                            if (t != 0) {
                                buf.append("\t").append(p).append("\t\t").append(l / t).append("\t\t").append(m.memory).append("\n"); // NOI18N
                            }
                        }
                    }
                    LOG.log(Level.INFO, buf.toString());
                }
            }
        }
    }

    public List<Measure> getData() {
        List<Measure> res = new ArrayList<Measure>();
        synchronized(measures) {
            res.addAll(measures);
        }
        return res;
    }
    
    private void showWarning() {
        if (isDialogShown) {
            return;
        }
        if (CndUtils.isStandalone() || CndUtils.isUnitTestMode()) {
            return;
        }
        if (remainingTime < timeThreshold) {
            return;
        }
        int usedMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / Mb);
        if (maxMemory - usedMemory < memoryThreshold) {
            isDialogShown = true;
            LOG.log(Level.INFO, "Lack of Memory, Heap Size={0}Mb, Used Memory={1}Mb", new Object[]{maxMemory, usedMemory}); //NOI18N
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    ParsingProblemResolver.showParsingProblemResolver(ParsingProblemDetectorImpl.this);
                }
            });
        }
    }
    
    /**
     * inform about starting handling next file item
     */
    @Override
    public String nextCsmFile(CsmFile file, int fileLineCount, int current, int allWork) {
        String msg = "";
        int usedMemory = (int) ((runtime.totalMemory() - runtime.freeMemory()) / Mb);
        final long currentTimeMillis = System.currentTimeMillis();
        final long delta = currentTimeMillis - startTime;
        if (TIMING) {
            lineCount += fileLineCount;
            synchronized(measures) {
                measures.add(new Measure(lineCount, (int)delta, usedMemory));
            }
        }
        if (averageSpeed != null) {
            averageSpeed.add(currentTimeMillis);
            remainingTime = averageSpeed.getEstimation(startTime, delta, current, allWork);
        }
        if (maxMemory - usedMemory < memoryThreshold) {
            msg = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "MSG_LowMemory"); // NOI18N
        }
        showWarning();
        return msg;
    }

    @Override
    public String getRemainingTime() {
        if (remainingTime == 0) {
            return ""; // NOI18N
        }
        String esimation;
        if (remainingTime < 1000) {
            esimation = ""; // NOI18N
        } else if (remainingTime < 1000*60) {
            int s = (int) (remainingTime/1000);
            esimation = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Remaining_seconds", ""+s); // NOI18N
        } else if (remainingTime < 1000*60*60) {
            int s = (int) (remainingTime/1000/60);
            esimation = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Remaining_minutes", ""+s); // NOI18N
        } else {
            int s = (int) (remainingTime/1000/60/60);
            esimation = NbBundle.getMessage(ParsingProblemDetectorImpl.class, "Remaining_hours", ""+s); // NOI18N
        }
        return esimation;
    }

    @Override
    public void switchToDeterminate(int maxWorkUnits) {
        startTime = System.currentTimeMillis();
        averageSpeed = new AverageSpeed(startTime);
    }
    
    public static final class Measure {
        public final int lines;
        public final int time;
        public final int memory;
        Measure(int lines, int time, int memory) {
            this.lines = lines;
            this.time = time;
            this.memory = memory;
        }
    }
    
    private static final class AverageSpeed {
        private static final int MOVING_AVERAGE = 60;
        private final int[] last = new int[MOVING_AVERAGE];
        private long movingStartTime;
        private AverageSpeed(long startTime) {
            movingStartTime = startTime;
        }
        private void add(long currentTime) {
            int delta = (int) ((currentTime - movingStartTime) / 1000);
            if (delta < 0) {
                return;
            }
            if (delta >= MOVING_AVERAGE) {
                int shift = 1 + delta - MOVING_AVERAGE;
                for(int i = 0; i < MOVING_AVERAGE; i++) {
                    if (i+shift < MOVING_AVERAGE) {
                        last[i] = last[i+shift];
                    } else {
                        last[i] = 0;
                    }
                }
                movingStartTime += shift * 1000;
                delta -= shift;
            }
            last[delta] += 1;
        }
        
        private long getEstimation(long startTime, long delta, int current, int allWork) {
            if (current < 10) {
                return 0;
            } else {
                final long interval = startTime + delta - movingStartTime;   
                if (interval/1000 >  MOVING_AVERAGE/2) {   
                    int work = 0;
                    for(int i = 0; i < MOVING_AVERAGE; i++) {
                        work += last[i];
                    }
                    if (work > 0) {
                        return interval*(allWork-current)/work;
                    }
                }
                return delta*(allWork-current)/current;
            }
        }
    }
}
