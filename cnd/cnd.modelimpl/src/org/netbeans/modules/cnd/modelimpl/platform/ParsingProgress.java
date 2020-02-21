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

package org.netbeans.modules.cnd.modelimpl.platform;

import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.spi.model.services.CodeModelProblemResolver;
import org.netbeans.modules.cnd.spi.model.services.CodeModelProblemResolver.ParsingProblemDetector;
import org.openide.util.Cancellable;
import org.openide.util.NbBundle;


/**
 * provides progress bar in status bar
 */
final class ParsingProgress implements Cancellable {

    private final ProgressHandle handle;
    private int curWorkedUnits = 0;
    private int maxWorkUnits = 0;
    private int addedAfterStartParsing = 0;
    private int allWork = 0; // in procent points.
    private static final double ALL_WORK_DOUBLE = 10000.0;
    private static final int ALL_WORK_INT = 10000;
    private boolean started = false;
    private boolean cancelled = false;
    private boolean determinate = false;
    private final ParsingProblemDetector problemDetector;
    private final Cancellable cancelDelegate;
    
    /**  
     * Delay amount of milliseconds
     * that shall pass before the progress appears in status bar
     */
    private static final int INITIAL_DELAY;
    static {
        int value = 1000;
        try {
            value = Integer.parseInt(NbBundle.getMessage(ParsingProgress.class, "CSM_PARSING_PROGRESS_INITIAL_DELAY")); // NOI18N
            if (value < 0) {
                value = Integer.MAX_VALUE;
            }
        } catch (NumberFormatException e) {
            
        }
        INITIAL_DELAY = value;
    }
    
    /**
     * Constructs progress information for project
     */
    public ParsingProgress(CsmProject project, Cancellable cancel) {
        String msg=NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgress", project.getName());
        problemDetector = CodeModelProblemResolver.getParsingProblemDetector(project);
        this.cancelDelegate = cancel;
        if (cancel == null) {
            handle = ProgressHandleFactory.createHandle(msg);
        } else {
            handle = ProgressHandleFactory.createHandle(msg, ParsingProgress.this);
        }
    }
    
    /**
     * Start the progress indication for indeterminate task.
     * it will be visualized by a progress bar in indeterminate mode.
     */
    public void start() {
        synchronized (handle) {
            if (cancelled) {
                return;
            }
            if(!started) {
                started = true;
                handle.setInitialDelay(INITIAL_DELAY);
                if (problemDetector != null) {
                    problemDetector.start();
                }
                handle.start();
            }
        }
    }
    
    /**
     * finish the task, remove the task's component from the progress bar UI.
     */        
    public void finish() {
        synchronized (handle) {
            if( started ) {
                if (problemDetector != null) {
                    problemDetector.finish();
                }
                handle.finish();
                started = false;
            }
        }
    }

    /**
     * inform about adding header to reparse
     */
    public void addedToParse(CsmFile file) {
        addedAfterStartParsing++;
    }

    /**
     * inform about starting handling next file item
     */
    public void nextCsmFile(CsmFile file) {
        if( ! started || !determinate || cancelled) {
            return;
        }
        // extract expensive line counting out of sync block;
        // also we know it is used only when TIMING is true
        int lineCount = 0;
        if (problemDetector != null && TraceFlags.TIMING) {
            lineCount = CsmFileInfoQuery.getDefault().getLineCount(file);
        }
        synchronized (handle) {
            if( ! started || !determinate || cancelled) {
                return;
            }
            if( curWorkedUnits < maxWorkUnits + addedAfterStartParsing) {
                curWorkedUnits++;
                double ratio = 1.0;
                if (maxWorkUnits + addedAfterStartParsing > 0) {
                    ratio = ALL_WORK_DOUBLE / (maxWorkUnits + addedAfterStartParsing);
                }
                int work = (int)(ratio * curWorkedUnits);
                if (allWork <= work && work < ALL_WORK_INT) {
                    allWork = work;
                }
            } 
            try {
                String problem = ""; // NOI18N
                String elapsedTime = ""; // NOI18N
                if (problemDetector != null) {
                    problem = problemDetector.nextCsmFile(file, lineCount, curWorkedUnits, maxWorkUnits + addedAfterStartParsing);
                    elapsedTime = problemDetector.getRemainingTime();
                }
                String msg = NbBundle.getMessage(ModelSupport.class, "MSG_ParsingProgressFull", ""+curWorkedUnits, ""+(maxWorkUnits + addedAfterStartParsing), file.getName().toString(), elapsedTime, problem); // NOI18N
                handle.progress(msg, allWork);
                //assert(curWorkedUnits <= maxWorkUnits);
            } catch (NullPointerException ex) {
                // very strange... but do not interrupt process
                DiagnosticExceptoins.register(ex);
            }
        }
    }

    /**
     * Currently indeterminate task can be switched to show percentage completed.
     * A common use case is to calculate the amount of work in the beginning showing
     * in indeterminate mode and later switch to the progress with known steps
     */
    public void switchToDeterminate(int maxWorkUnits) {
        synchronized (handle) {
            if( ! started || cancelled) {
                return;
            }
            if (!determinate) {
                this.maxWorkUnits = maxWorkUnits;
                addedAfterStartParsing = 0;
                if (problemDetector != null) {
                    problemDetector.switchToDeterminate(maxWorkUnits);
                }
                handle.switchToDeterminate(ALL_WORK_INT);
                determinate = true;
            }
        }
    }

    @Override
    public boolean cancel() {
        synchronized (handle) {
            cancelled = true;
            if (cancelDelegate == null) {
                return true;
            } else {
                return cancelDelegate.cancel();
            }
        }
    }
}   
