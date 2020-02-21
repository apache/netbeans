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
package org.netbeans.modules.cnd.modelui.trace;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;
import javax.swing.Action;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProgressAdapter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.trace.TraceXRef;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Cancellable;
import org.openide.util.CharSequences;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.SharedClassObject;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 *
 */
public class TestProjectReferencesAction extends TestProjectActionBase {

    private static boolean running = false;
    private final boolean allReferences;
    private final boolean analyzeStatistics;
    private final Boolean reportUnresolved;
    private final boolean reportIndex;
    private int numThreads = 1;
    // < 0 if use; == 0 if do not use; > 0 if collect
    private long timeThreshold = 0;

    public static Action getSmartCompletionAnalyzerAction() {
        return SharedClassObject.findObject(SmartCompletionAnalyzerAction.class, true);
    }
    
    public static Action getTestReparseAction() {
        return SharedClassObject.findObject(TestReparseAction.class, true);
    }
    
    public static Action getDirectUsageReferencesAction() {
        return SharedClassObject.findObject(DirectUsageAction.class, true);
    }

    public static Action getIndexReferencesAction() {
        return SharedClassObject.findObject(IndexUsageAction.class, true);
    }
    
    public static Action getAllReferencesAction() {
        return SharedClassObject.findObject(AllUsagesAction.class, true);
    }
    
    public static Action getAllReferencesPerformanceAction() {
        return SharedClassObject.findObject(AllUsagesPerformanceAction.class, true);
    }

    public static Action getFileContainerAction() {
        return SharedClassObject.findObject(TestFileContainerAction.class, true);
    }

    public static Action getGraphContainerAction() {
        return SharedClassObject.findObject(TestGraphContainerAction.class, true);
    }

    public static Action getTestRepositoryStatisticsAction() {
        return SharedClassObject.findObject(TestRepositoryStatisticsAction.class, true);
    }

    private static final Map<CharSequence, Map<CharSequence, Long>> times = new HashMap<CharSequence, Map<CharSequence, Long>>();
    private Map<CharSequence, Long> getProjectMap(CsmProject p) {
        CharSequence name = p.getName();
        Map<CharSequence, Long> out = times.get(name);
        if (out == null) {
            out = new ConcurrentHashMap<CharSequence, Long>();
            loadXRefTimes(p, out);
            times.put(name, out);
        }
        return out;
    }
    
    private void loadXRefTimes(CsmProject p, Map<CharSequence, Long> out) throws NumberFormatException {
        // try to load
        Preferences props = getProjectPrefs(p);
        if (props != null) {
            String storedTimes = props.get("xRefTimes", ""); // NOI18N
            String[] split = storedTimes.split("\n");// NOI18N
            for (String fileTime : split) {
                int delim = fileTime.lastIndexOf("|"); // NOI18N
                if (delim > 0) {
                    try {
                        out.put(CharSequences.create(fileTime.substring(0, delim)), Long.parseLong(fileTime.substring(delim+1)));
                    } catch (NumberFormatException e) {
                        Exceptions.printStackTrace(e);
                        // continue
                    }
                }
            }
        }
    }

    private void saveXRefTimes(CsmProject p, Map<CharSequence, Long> out) throws NumberFormatException {
        // try to save
        Preferences props = getProjectPrefs(p);
        if (props != null) {
            StringBuilder storedTimes = new StringBuilder();
            for (Map.Entry<CharSequence, Long> entry : out.entrySet()) {
                storedTimes.append(entry.getKey()).append("|").append(entry.getValue().toString()).append("\n"); // NOI18N
            }
            props.put("xRefTimes", storedTimes.toString()); // NOI18N
        }
    }
    
    static final class SmartCompletionAnalyzerAction extends TestProjectReferencesAction {

        SmartCompletionAnalyzerAction() {
            super(false, true, null, false);
        }
    }
    
    static final class DirectUsageAction extends TestProjectReferencesAction {
        DirectUsageAction() {
            super(false, false, null, false);
        }
    }

    static final class IndexUsageAction extends TestProjectReferencesAction {
        IndexUsageAction() {
            super(false, false, null, true);
        }
    }
    
    static final class AllUsagesAction extends TestProjectReferencesAction {

        AllUsagesAction() {
            super(true, false, Boolean.TRUE, false);
        }
    }

    static final class AllUsagesPerformanceAction extends TestProjectReferencesAction {

        AllUsagesPerformanceAction() {
            super(true, false, Boolean.FALSE, false);
        }
    }
    
    protected TestProjectReferencesAction(boolean allReferences, boolean analyzeStatistics, Boolean reportUnresolved, boolean reportIndex) {
        this.allReferences = allReferences;
        this.analyzeStatistics = analyzeStatistics;
        this.reportUnresolved = reportUnresolved;
        this.reportIndex = reportIndex;
        this.numThreads = (reportUnresolved == Boolean.FALSE) ? Runtime.getRuntime().availableProcessors() : 1;
    }

    @Override
    public String getName() {
        String nameKey;
        if (analyzeStatistics) {
            nameKey = "CTL_TestProjectSmartCCDirectUsageReferencesAction"; // NOI18N
        } else if (reportUnresolved != null) {
            nameKey = (reportUnresolved ? "CTL_TestProjectReferencesAction" : "CTL_TestProjectReferencesPerformanceAction"); // NOI18N
        } else if (reportIndex) {
            nameKey = "CTL_TestProjectIndexUsageReferencesAction"; // NOI18N
        } else {
            nameKey = "CTL_TestProjectDirectUsageReferencesAction"; // NOI18N
        }
        return NbBundle.getMessage(getClass(), nameKey); // NOI18N
    }

    @Override
    protected void performAction(Collection<CsmProject> projects) {
        if (reportUnresolved == Boolean.FALSE) {
            boolean hasSlowInfo = false;
            if (projects != null) {
                for (CsmProject p : projects) {
                    hasSlowInfo |= !getProjectMap(p).isEmpty();
                }
            }
            TestReferencePanel panel = new TestReferencePanel(numThreads, timeThreshold, hasSlowInfo);
            final NotifyDescriptor input = new NotifyDescriptor(panel, "Test References", NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.QUESTION_MESSAGE, // NOI18N
                    new Object[] {NotifyDescriptor.OK_OPTION, NotifyDescriptor.CANCEL_OPTION}, NotifyDescriptor.OK_OPTION); // NOI18N
            Object option = DialogDisplayer.getDefault().notify(input); //NOI18N
            if (option == NotifyDescriptor.CANCEL_OPTION) {
                return;
            } else {
                numThreads = panel.getThreadsNumber();
                timeThreshold = panel.getThreshold();
                if (!panel.isCollecting()) {
                    timeThreshold = -timeThreshold;
                }
            }
        }
        if (projects != null) {
            for (CsmProject p : projects) {
                testProject(p);
            }
        }
    }

    
    private void testProject(CsmProject p) {
        String task = (this.reportIndex ? "Indexed " : (this.allReferences ? "All " : "Direct usage ")) + "xRef - " + p.getName() + (this.analyzeStatistics ? " Statistics" : ""); // NOI18N
        InputOutput io = IOProvider.getDefault().getIO(task, false);
        io.select();
        final AtomicBoolean canceled = new AtomicBoolean(false);        
        final ProgressHandle handle = ProgressHandleFactory.createHandle(task, new Cancellable() {
            @Override
            public boolean cancel() {
                canceled.set(true);
                return true;
            }
        });
        handle.start();
        final OutputWriter out = io.getOut();
        final OutputWriter err = io.getErr();
        final long[] time = new long[2];
        time[0] = System.currentTimeMillis();
        Set<CsmReferenceKind> interestedElems = this.allReferences ? CsmReferenceKind.ANY_REFERENCE_IN_ACTIVE_CODE : EnumSet.<CsmReferenceKind>of(CsmReferenceKind.DIRECT_USAGE);
        Map<CharSequence, Long> fileTimes = getProjectMap(p);
        Map<CharSequence, Long> filesMap;
        if (timeThreshold > 0) {
            fileTimes.clear();
        }
        long passedThreshold = timeThreshold;
        if (passedThreshold < 0) {
            passedThreshold = 0;
            // do not overwrite what was collected before
            filesMap = new ConcurrentHashMap<CharSequence, Long>(fileTimes.size());
            for (Map.Entry<CharSequence, Long> entry : fileTimes.entrySet()) {
                if (entry.getValue() > (-timeThreshold)) {
                    filesMap.put(entry.getKey(), entry.getValue());
                }
            }
            err.println("analyze " + filesMap.size() + " remembered slow files only");// NOI18N
        } else {
            filesMap = fileTimes;
            // collect mode
            if (passedThreshold > 0) {
                err.println("collect files slower than " + passedThreshold + "ms");// NOI18N
                filesMap.clear();
            }
        }
        TraceXRef.traceProjectRefsStatistics(p, filesMap, new TraceXRef.StatisticsParameters(interestedElems, analyzeStatistics,
                (reportUnresolved == null) ? true : reportUnresolved.booleanValue(), reportIndex, numThreads, passedThreshold), out, err, new CsmProgressAdapter() {
            private volatile int handled = 0;
            @Override
            public void projectFilesCounted(CsmProject project, int filesCount) {
                err.flush();
                out.println("Project " + project.getName() + " has " + filesCount + " files"); // NOI18N
                out.flush();
                handle.switchToDeterminate(filesCount);
            }

            @Override
            public synchronized void fileParsingStarted(CsmFile file) {
                handle.progress("Analyzing " + file.getName(), ++handled); // NOI18N
            }

            @Override
            public void projectParsingFinished(CsmProject project) {
                time[1] = System.currentTimeMillis();
            }
        }, canceled);
        handle.finish();
        out.println("Analyzing " + p.getName() + " took " + (time[1]-time[0]) + "ms"); // NOI18N
        if (timeThreshold > 0) {
            saveXRefTimes(p, fileTimes);
            err.println(fileTimes.size() + " files which were analyzed longer than " + timeThreshold + "ms are remembered"); // NOI18N
        }
        err.flush();
        out.flush();
        out.close();
        err.close();
    }
}
