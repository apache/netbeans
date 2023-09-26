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

package org.netbeans.modules.profiler;

import java.io.IOException;
import java.lang.management.LockInfo;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.lib.profiler.common.ProfilingSettingsPresets;
import org.netbeans.lib.profiler.results.CCTNode;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot;
import org.netbeans.lib.profiler.results.cpu.CPUResultsSnapshot.NoDataAvailableException;
import org.netbeans.lib.profiler.results.cpu.PrestimeCPUCCTNode;
import org.netbeans.lib.profiler.results.cpu.StackTraceSnapshotBuilder;
import org.netbeans.modules.profiler.LoadedSnapshot.SamplesInputStream;
import org.netbeans.modules.profiler.LoadedSnapshot.ThreadsSample;
import org.netbeans.modules.profiler.api.GoToSource;
import org.openide.filesystems.FileObject;

/** SampledCPUSnapshot provides access to NPSS file
 *
 * @author Tomas Hurka
 */
public final class SampledCPUSnapshot {
    public static final String OPEN_THREADS_URL = "file:/stackframe/";     // NOI18N

    private FileObject npssFile;
    private SamplesInputStream samplesStream;
    private long lastTimestamp;
    private int samples;
    private int currentIndex;
    private ThreadsSample sample;
    private StackTraceSnapshotBuilder builder;
    private long startTime;

    public SampledCPUSnapshot(FileObject file) throws IOException {
        samplesStream = new SamplesInputStream(file.getInputStream());
        npssFile = file;
        samples = samplesStream.getSamples();
        lastTimestamp = samplesStream.getLastTimestamp();
        if (samples == 0) {
            initSamples();
        }
        currentIndex = -1;
    }

    public int getSamplesCount() {
        return samples;
    }

    /**
     * Returns start time
     * @return start time in nanoseconds
     */
    public long getStartTime() {
        return startTime;
    }
    
    public long getTimestamp(int sampleIndex) throws IOException {
        long timestamp;

        if (sampleIndex == getSamplesCount()-1) {
            return lastTimestamp;
        }
        getSample(sampleIndex);
        timestamp = sample.getTime();
        if (startTime == 0) {
            startTime = timestamp;
            builder = new StackTraceSnapshotBuilder();
        }
        builder.addStacktrace(sample.getTinfos(),timestamp);
        return timestamp;
    }

    public long getValue(int sampleIndex, int valIndex) throws IOException {
        getSample(sampleIndex);
        long ret = 0;
        for (ThreadInfo info : sample.getTinfos()) {
            if (info.getThreadState() == Thread.State.RUNNABLE) {
                ret += info.getStackTrace().length;
            }
        }
        return ret;
    }

    public List<Integer> getIntervals(int startIndex, int endIndex,PrestimeCPUCCTNode node) throws IOException {
        List<Integer> intervals = new ArrayList<>();
        SamplesInputStream stream = seek(startIndex);
        CCTNode n = node;
        List<String[]> stack = new ArrayList<>();
        final String NATIVE_ID = "[native]"; // NOI18N 
        boolean match = false;
        do {
            if (n instanceof PrestimeCPUCCTNode) {
                PrestimeCPUCCTNode cctNode = (PrestimeCPUCCTNode) n;
                if (isRegular(cctNode)) {
                    String[] mid = cctNode.getMethodClassNameAndSig();
                    
                    if (mid[1].endsWith(NATIVE_ID)) {
                        mid[1] = mid[1].substring(0,mid[1].length()-NATIVE_ID.length());
                    }
                    stack.add(0,mid);
                }
            }
            n = n.getParent();
        } while (n != null);

        for (int i = startIndex; i <= endIndex; i++) {
            LoadedSnapshot.ThreadsSample _sample = stream.readSample();
            ThreadInfo[] threads = _sample.getTinfos();
            if (findStack(stack, threads)) {// match found
                if (!match) {
                    intervals.add(i);
                }
                match = true;
            } else {
                if (match) {
                    intervals.add(i-1);
                }
                match = false;
            }
        }
        if (match) {
            intervals.add(endIndex);
        }
        stream.close();
        stream = null;
        return intervals;
    }

    private boolean findStack(final List<String[]> stack, final ThreadInfo[] threads) {      
        for (ThreadInfo t : threads) {
            StackTraceElement[] els = t.getStackTrace();
            
            if (els == null || els.length < stack.size()) {
                continue;
            }
            int j=0;
            for (; j<stack.size(); j++) {
                StackTraceElement el = els[els.length - j - 1];
                String[] method = stack.get(j);
                
                if (!el.getClassName().equals(method[0]) || !el.getMethodName().equals(method[1])) {
                    break;  // try next thread
                }
            }
            if (j == stack.size()) { // match
                return true;
            }
        }
        return false;
    }
    
    public String getThreadDump(int sampleIndex) throws IOException {
        StringBuilder sb = new StringBuilder(4096);
        SamplesInputStream stream = seek(sampleIndex);
        ThreadsSample _sample = stream.readSample();
        ThreadInfo[] threads = _sample.getTinfos();

        stream.close();
        stream = null;
        printThreads(sb, threads);
        return sb.toString();
    }

    public LoadedSnapshot getCPUSnapshot(int startIndex, int endIndex) throws IOException {
        LoadedSnapshot snapshot;

        if (builder != null && samplesStream == null &&
            startIndex == 0 && endIndex == getSamplesCount()-1) { // full snapshot prepared in advance
            snapshot = createSnapshot(startTime/1000000,builder);
            builder = null;
        } else {
            SamplesInputStream stream = seek(startIndex);
            StackTraceSnapshotBuilder _builder = new StackTraceSnapshotBuilder();
            long _startTime = 0;  // in milliseconds

            for (int i = startIndex; i <= endIndex; i++) {
                LoadedSnapshot.ThreadsSample _sample = stream.readSample();
                if (_startTime == 0) {
                    _startTime = _sample.getTime() / 1000000;
                }
                _builder.addStacktrace(_sample.getTinfos(),_sample.getTime());
            }
            stream.close();
            stream = null;
            snapshot = createSnapshot(_startTime, _builder);
        }
        return snapshot;
    }

    /**
     * 
     * @param startTime start time in milliseconds
     * @param builder StackTraceSnapshotBuilder
     * @return snapshot
     * @throws IOException 
     */
    private LoadedSnapshot createSnapshot(final long startTime, final StackTraceSnapshotBuilder builder) throws IOException {
        CPUResultsSnapshot snapshot;
        try {
            snapshot = builder.createSnapshot(startTime);
        } catch (NoDataAvailableException ex) {
            throw new IOException(ex);
        }
        LoadedSnapshot ls = new LoadedSnapshot(snapshot, ProfilingSettingsPresets.createCPUPreset(), null, null);
        ls.setSaved(true);
        return ls;
    }

    private SamplesInputStream seek(final int sampleIndex) throws IOException {
        SamplesInputStream stream = new SamplesInputStream(npssFile.getInputStream());
//        ThreadsSample sample;

        for (int i = 0; i < sampleIndex; i++) {
            stream.readSample();
        }
        return stream;
    }

    private void getSample(final int sampleIndex) throws IllegalArgumentException, IOException {
        if (currentIndex > sampleIndex || currentIndex+1 < sampleIndex ) {
            throw new IllegalArgumentException("current sample "+currentIndex+" requested sample "+sampleIndex); // NOI18N
        }
        if (currentIndex+1 == sampleIndex) {
            currentIndex++;
            sample = samplesStream.readSample();
            if (sampleIndex == getSamplesCount()-1) {
                samplesStream.close();
                samplesStream = null;
            }
        }
    }

    private void printThreads(final StringBuilder sb, ThreadInfo[] threads) {
        boolean goToSourceAvailable = GoToSource.isAvailable();
        sb.append("<pre>"); // NOI18N
        for (ThreadInfo thread : threads) {
            if (thread != null) {
                print16Thread(sb, thread, goToSourceAvailable);
            }
        }
        sb.append("</pre>"); // NOI18N
    }

    private void print16Thread(final StringBuilder sb, final ThreadInfo thread, boolean goToSourceAvailable) {
        MonitorInfo[] monitors = thread.getLockedMonitors();
        sb.append("&nbsp;<b>");   // NOI18N
        sb.append("\"").append(thread.getThreadName()).append("\" - Thread t@").append(thread.getThreadId()).append("<br>");    // NOI18N
        sb.append("    java.lang.Thread.State: ").append(thread.getThreadState()); // NOI18N
        sb.append("</b><br>");   // NOI18N
        int index = 0;
        for (StackTraceElement st : thread.getStackTrace()) {
            LockInfo lock = thread.getLockInfo();
            String stackElementText = htmlize(st.toString());
            String lockOwner = thread.getLockOwnerName();
            String className = st.getClassName();
            String method = st.getMethodName();
            int lineNo = st.getLineNumber();
            
            String stackEl = stackElementText;
            if (goToSourceAvailable) {
                String stackUrl = OPEN_THREADS_URL+className+"|"+method+"|"+lineNo; // NOI18N
                stackEl = "<a href=\""+stackUrl+"\">"+stackElementText+"</a>";    // NOI18N
            }

            sb.append("\tat ").append(stackEl).append("<br>");    // NOI18N
            if (index == 0) {
                if ("java.lang.Object".equals(st.getClassName()) &&     // NOI18N
                        "wait".equals(st.getMethodName())) {                // NOI18N
                    if (lock != null) {
                        sb.append("\t- waiting on ");    // NOI18N
                        printLock(sb,lock);
                        sb.append("<br>");    // NOI18N
                    }
                } else if (lock != null) {
                    if (lockOwner == null) {
                        sb.append("\t- parking to wait for ");      // NOI18N
                        printLock(sb,lock);
                        sb.append("<br>");            // NOI18N
                    } else {
                        sb.append("\t- waiting to lock ");      // NOI18N
                        printLock(sb,lock);
                        sb.append(" owned by \"").append(lockOwner).append("\" t@").append(thread.getLockOwnerId()).append("<br>");   // NOI18N
                    }
                }
            }
            printMonitors(sb, monitors, index);
            index++;
        }
        StringBuilder jnisb = new StringBuilder();
        printMonitors(jnisb, monitors, -1);
        if (jnisb.length() > 0) {
            sb.append("   JNI locked monitors:<br>");
            sb.append(jnisb);
        }
        LockInfo[] synchronizers = thread.getLockedSynchronizers();
        if (synchronizers != null) {
            sb.append("<br>   Locked ownable synchronizers:");    // NOI18N
            if (synchronizers.length == 0) {
                sb.append("<br>\t- None\n");  // NOI18N
            } else {
                for (LockInfo li : synchronizers) {
                    sb.append("<br>\t- locked ");         // NOI18N
                    printLock(sb,li);
                    sb.append("<br>");  // NOI18N
                }
            }
        }
        sb.append("<br>");
    }

    private void printMonitors(final StringBuilder sb, final MonitorInfo[] monitors, final int index) {
        if (monitors != null) {
            for (MonitorInfo mi : monitors) {
                if (mi.getLockedStackDepth() == index) {
                    sb.append("\t- locked ");   // NOI18N
                    printLock(sb,mi);
                    sb.append("<br>");    // NOI18N
                }
            }
        }
    }

    private void printLock(StringBuilder sb,LockInfo lock) {
        String id = Integer.toHexString(lock.getIdentityHashCode());
        String className = lock.getClassName();

        sb.append("&lt;").append(id).append("&gt; (a ").append(className).append(")");       // NOI18N
    }
    
    private static String htmlize(String value) {
            return value.replace(">", "&gt;").replace("<", "&lt;");     // NOI18N
    }

    private boolean isRegular(PrestimeCPUCCTNode n) {
        return !n.isThreadNode() && !n.isFiltered();
    }

    private void initSamples() throws IOException {
        SamplesInputStream stream = new SamplesInputStream(npssFile.getInputStream());
        int samplesGuess = (int)(npssFile.getSize()/130);
        ProgressHandle ph = ProgressHandle.createSystemHandle("Computing snapshot samples", null);
        ph.start(samplesGuess);
        
        for(ThreadsSample s = stream.readSample(); s != null; s = stream.readSample()) {
            samples++;
            lastTimestamp = s.getTime();
            if (samples < samplesGuess) {
                ph.progress(samples);
            }
        }
        ph.finish();
    }
}
