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

package org.netbeans.modules.cnd.modelimpl.debug;

import org.netbeans.modules.cnd.antlr.RecognitionException;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPParser;
import org.openide.util.Exceptions;

/**
 * Debugging output, collect file statistics
 *
 * Usecase from test harness:
 * if (Diagnostic.getStatisticsLevel() == 0) {
 *   // need to set the default level
 *   Diagnostic.setStatisticsLevel(DEFAULT_TRACEMODEL_STATISTICS_LEVEL);
 * }
 * Diagnostic.initFileStatistics(file.getAbsolutePath());
 * ...
 * Diagnostic.dumpFileStatistics(dumpFile);
 *
 */
public class Diagnostic {

    private static int STATISTICS_LEVEL=Integer.getInteger("cnd.modelimpl.stat.level", 0).intValue(); // NOI18N

    private static DiagnosticUnresolved diagnosticUnresolved = null;

    private Diagnostic() {
    }

    public static class ProjectStat {
        private static final int SLOW_FILE_NUMBER = Math.max(1, Integer.getInteger("cnd.modelimpl.slow.file.number", 5)); // NOI18N
        private final ConcurrentMap<CsmUID<CsmProject>, SlowFilesCollection> projectStats = new ConcurrentHashMap<>();
        public void addParseFileStatistics(ProjectBase project, FileImpl file, long parseTime) {
            if (project != null && !project.isArtificial()) {
                CsmUID<CsmProject> uID = project.getUID();
                SlowFilesCollection data = projectStats.get(uID);
                if (data == null && uID != null) {
                    data = new SlowFilesCollection();
                    SlowFilesCollection old = projectStats.putIfAbsent(uID, data);
                    if (old != null) {
                        data = old;
                    }
                }
                if (data != null) {
                    data.put(file, parseTime);
                }
            }
        }

        public void traceProjectData(ProjectBase project) {
            if (project != null && !project.isArtificial()) {
                SlowFilesCollection data = projectStats.get(project.getUID());
                if (data != null) {
                    System.err.printf("Slowest Files for %s are:%n%s", project.getName(), data.asString());
                    System.err.println();
                    System.err.flush();
                } else {
                    System.err.printf("No Slowest Files info for " +  project.getName());
                    System.err.println();
                    System.err.flush();
                }
            }
        }

        public void clear() {
            projectStats.clear();
        }

        private final static class SlowFilesCollection {
            private final LinkedList<Entry> times = new LinkedList<>();

            private void put(FileImpl file, long parseTime) {
                synchronized (this) {
                    // the first is the slowest
                    ListIterator<Entry> iterator = times.listIterator(times.size());
                    boolean add = !iterator.hasPrevious();
                    while (iterator.hasPrevious()) {
                        Entry elem = iterator.previous();
                        if (elem.time < parseTime) {
                            add = true;
                        } else {
                            if (add) {
                                iterator.add(new Entry(parseTime, file.getBuffer().getUrl()));
                                add = false;
                                break;
                            }
                        }
                    }
                    if (add) {
                        times.addFirst(new Entry(parseTime, file.getBuffer().getUrl()));
                    }
                    if (times.size() > SLOW_FILE_NUMBER) {
                        times.removeLast();
                    }
                }
            }

            private String asString() {
                StringBuilder out = new StringBuilder();
                synchronized (this) {
                    for (Entry entry : times) {
                        out.append(entry).append('\n'); // NOI18N
                    }
                }
                return out.toString();
            }

            private final static class Entry {
                final long time;
                final CharSequence file;

                public Entry(long time, CharSequence file) {
                    this.time = time;
                    this.file = file;
                }

                @Override
                public String toString() {
                    return " file=" + file + " " + time + " ms"; // NOI18N
                }
            }
        }
    }

    public static class StopWatch {

        private long time;
        private long lastStart;
        private boolean running;

        public StopWatch() {
            this(true);
        }

        public StopWatch(boolean start) {
            time = 0;
            if( start ) {
                start();
            }
        }

        public void start() {
            running = true;
            lastStart = System.currentTimeMillis();
        }

        public long stop() {
            running = false;
            time += System.currentTimeMillis() - lastStart;
            return time;
        }

        public long stopAndReport(String text) {
            long out = stop();
            report(text);
            return out;
        }

        public long report(String text) {
            System.err.println(' ' + text + ' ' + time + " ms");
            return time;
        }

        public boolean isRunning() {
            return running;
        }

        public long getTime() {
            return time;
        }

    }

    public static int getStatisticsLevel() {
        return STATISTICS_LEVEL;
    }

    public static void setStatisticsLevel(int level) {
        Diagnostic.STATISTICS_LEVEL = level;
    }

    public static boolean needStatistics() {
        return STATISTICS_LEVEL > 0;
    }

    private static final int step = 4;

    private static StringBuilder indentBuffer = new StringBuilder();

    public static void indent() {
        setupIndentBuffer(indentBuffer.length() + step);
    }

    public static void unindent() {
        setupIndentBuffer(indentBuffer.length() - step);
    }

    private static void setupIndentBuffer(int len) {
        if( len <= 0 ) {
            indentBuffer.setLength(0);
        } else {
            indentBuffer.setLength(len);
            for( int i = 0; i < len; i++ ) {
                indentBuffer.setCharAt(i,  ' ');
            }
        }
    }

    public static void trace(PrintStream out, Object arg) {
        if( TraceFlags.DEBUG || needStatistics()) {
            out.println(indentBuffer.toString() + arg);
        }
    }

    public static void trace(Object arg) {
        trace(System.err, arg);
    }

    public static void traceStack(String message) {
        if( TraceFlags.DEBUG ) {
            trace(message);
            StringWriter wr = new StringWriter();
            new Exception(message).printStackTrace(new PrintWriter(wr));
            //StringReader sr = new StringReader(wr.getBuffer().toString());
            BufferedReader br = new  BufferedReader(new StringReader(wr.getBuffer().toString()));
            try {
                br.readLine(); br.readLine();
                for( String s = br.readLine(); s != null; s = br.readLine() ) {
                    trace(s);
                }
            } catch( IOException e ) {
                e.printStackTrace(System.err);
            }
        }
    }

    public static void printlnStack(String message, int depth) {
        StringBuilder buf = new StringBuilder(message);
        buf.append('\n');
        StringWriter wr = new StringWriter();
        new Exception(message).printStackTrace(new PrintWriter(wr));
        BufferedReader br = new  BufferedReader(new StringReader(wr.getBuffer().toString()));
        try {
            br.readLine(); br.readLine();
            int i = 0;
            for( String s = br.readLine(); s != null; s = br.readLine() ) {
                if (i == 0){
                    buf.append("  in thread "+Thread.currentThread().getName()); // NOI18N
                    buf.append('\n');
                }
                buf.append(s);
                buf.append('\n');
                if (i > depth -1){
                    break;
                }
                i++;
            }
        } catch( IOException e ) {
            e.printStackTrace(System.err);
        }
        System.out.println(buf.toString());
    }

    public static synchronized void printToFile(String fileName, String format, Object... args) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName, true);
            PrintStream ps = new PrintStream(fos);
            ps.printf(format, args);
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public static void traceThreads(String message) {
        if( TraceFlags.DEBUG ) {
            trace(message);
            trace("Threads are:"); // NOI18N
            int cnt = Thread.activeCount();
            Thread[] threads = new Thread[cnt];
            Thread.enumerate(threads);
            for (int i = 0; i < cnt; i++) {
                String s = threads[i].getName() + " " + threads[i].getPriority(); // NOI18N
                if (threads[i] == Thread.currentThread()) {
                    s += " (current)"; // NOI18N
                }
                trace(s);
            }
            trace("");
        }
    }

    /// handle problems

    private static FileStatistics curFileHandler = new FileStatistics(null);

    public static void initFileStatistics(String file) {
        // TODO: we can store old data in map, if we are interested in them
        // but for now just delete old data
        curFileHandler.dispose();
        curFileHandler = null;
        curFileHandler = new FileStatistics(file);
    }

    public static void dumpFileStatistics(String dumpFile) throws FileNotFoundException {
        dumpFileStatistics(dumpFile, false);
    }

    public static void dumpFileStatistics(String dumpFile, boolean append) throws FileNotFoundException {
        PrintStream dump = new PrintStream(new FileOutputStream(dumpFile, append), true);
        try {
            curFileHandler.dump(dump);
        } finally {
            dump.close();
        }
    }

    public static void onUnresolvedError(CharSequence[] nameTokens, CsmFile file, int offset) {
        if( STATISTICS_LEVEL > 0 ) {
            getDiagnosticUnresolved().onUnresolved(nameTokens, file, offset);
        }
    }

    public static void dumpUnresolvedStatistics(String dumpFile, boolean append) throws FileNotFoundException {
        getDiagnosticUnresolved().dumpStatictics(dumpFile, append);
    }

    private static synchronized DiagnosticUnresolved getDiagnosticUnresolved() {
        if( diagnosticUnresolved == null ) {
            diagnosticUnresolved = new DiagnosticUnresolved(STATISTICS_LEVEL);
        }
        return diagnosticUnresolved;
    }

    public static void onLexerError(RecognitionException e) {
        curFileHandler.handleLexerError(e);
    }

    public static void onParserError(RecognitionException e) {
        curFileHandler.handleParserError(e);
    }

    /**
     * Used when type of error is not important, but there is error to report about.
     */
    public static void onError(Exception e, String source) {
        curFileHandler.handleOtherError(e, source);
    }

    public static void onInclude(String include, String absBaseFilePath, String resolvedIncludePath) {
        curFileHandler.handleInclude(include, absBaseFilePath, resolvedIncludePath, false);
    }

    public static void onRecurseInclude(String resolvedIncludePath, String absBaseFilePath) {
        curFileHandler.handleInclude(null, absBaseFilePath, resolvedIncludePath, true);
    }

    /**
     * Collection of file statistics
     *
     */
    private static class FileStatistics {
        private final Map<ExceptionWrapper, ExceptionWrapper> lexerProblems = new HashMap<>();
        private final Map<ExceptionWrapper, ExceptionWrapper> parserProblems = new HashMap<>();
        private final Map<ExceptionWrapper, ExceptionWrapper> otherProblems = new HashMap<>();
        private final Map<String, IncludeInfo> includes = new HashMap<>();

        /** first and last errors could be interesting */
        private ExceptionWrapper lastError = null;
        private ExceptionWrapper firstError = null;
        private String                lastErrorMsg = null;

        private String handledFile;

        public FileStatistics(String file) {
            this.handledFile = file;
        }

        public void handleLexerError(RecognitionException e) {
            handleError(lexerProblems, new LexerExceptionWrapper(e), true);
        }

        public void handleParserError(RecognitionException e) {
            handleError(parserProblems, new ParserExceptionWrapper(e), true);
        }

        public void handleOtherError(Exception e, String source) {
            handleError(otherProblems, new ExceptionWrapper(e, source), false);
        }

        public void handleInclude(String include, String absBaseFilePath,
                String resolvedIncludePath, boolean recursion) {
            // if resolvedIncludePath is valid => include path resolving was OK
            String key = (resolvedIncludePath == null) ? include : resolvedIncludePath;
            assert (key != null) : "at least 'include' or 'resolvedIncludePath' must be specified";
            IncludeInfo info = includes.get(key);
            if (info == null) {
                info = new IncludeInfo(key, resolvedIncludePath == null);
                includes.put(key, info);
            }
            info.add(absBaseFilePath, recursion);
        }

        public void dispose() {
            this.handledFile = null;
            this.lastError = null;
            this.firstError = null;
            this.lastErrorMsg = null;
            this.lexerProblems.clear();
            this.parserProblems.clear();
            this.otherProblems.clear();
            this.includes.clear();
        }

//        private boolean hasStatistics() {
//            if (Diagnostic.getStatisticsLevel() == 1) {
//                // for the first level need to inform only about real problems
//                return hasLexerProblems() ||
//                        hasParserProblems() ||
//                        hasOtherProblems() ||
//                        hasIncludeProblems();
//
//            }
//            // for other levels need detailed statistics
//            return true;
//        }

        private boolean hasLexerProblems() {
            return lexerProblems.size() > 0;
        }

        private boolean hasParserProblems() {
            return parserProblems.size() > 0;
        }

        private boolean hasOtherProblems() {
            return otherProblems.size() > 0;
        }

        private boolean hasIncludeProblems() {
            for (Iterator<IncludeInfo> it = includes.values().iterator(); it.hasNext();) {
                IncludeInfo elem = it.next();
                if (elem.hasErrors()) {
                    return true;
                }
            }
            return false;
        }

        public void dump(PrintStream dumpFile) {
            if (lexerProblems.isEmpty() && parserProblems.isEmpty() &&
                    includes.isEmpty()) {
                trace(dumpFile, "*** No errors found in file " + handledFile); // NOI18N
            } else {
                trace(dumpFile, "*** Statistics of file " + handledFile); // NOI18N
                if (lastError != null) {
                    trace(dumpFile, "****** First and last lexer/parser errors ******"); // NOI18N
                    indent();
                    trace(dumpFile, "[FIRST ERROR MSG]: (" + firstError.getSourceName() + ")"); // NOI18N
                    trace(dumpFile,  firstError.e.toString());
                    trace(dumpFile, "[LAST ERROR MSG]: (" + lastError.getSourceName() + ")"); // NOI18N
                    trace(dumpFile,  lastErrorMsg);
                    if (Diagnostic.getStatisticsLevel() > 1) {
                        trace(dumpFile, "+++ More details +++ "); // NOI18N
                        if (lastError != firstError) {
                            trace(dumpFile, "[FIRST ERROR] "+firstError); // NOI18N
                            trace(dumpFile, "[LAST ERROR] "+lastError); // NOI18N
                        } else {
                            trace(dumpFile, "[ERROR] " + lastError); // NOI18N
                        }
                    }
                    unindent();
                }
                if (hasOtherProblems()) {
                    trace(dumpFile, "****** All unclassified errors ******"); // NOI18N
                    indent();
                    dumpExceptions(dumpFile, otherProblems);
                    unindent();
                }
                if (hasLexerProblems()) {
                    trace(dumpFile, "****** All Lexer errors ******"); // NOI18N
                    indent();
                    dumpExceptions(dumpFile, lexerProblems);
                    unindent();
                }
                if (hasParserProblems()) {
                    trace(dumpFile, "****** All Parser errors ******"); // NOI18N
                    indent();
                    dumpExceptions(dumpFile, parserProblems);
                    unindent();
                }
                if ((Diagnostic.getStatisticsLevel() > 1) || hasIncludeProblems()) {
                    trace(dumpFile, "****** Inclusions ******"); // NOI18N
                    indent();
                    dumpIncludes(dumpFile, includes);
                    unindent();
                }
                trace(dumpFile, "*** End of statistics for " + handledFile + '\n'); // NOI18N
            }
        }

        private void handleError(Map<ExceptionWrapper, ExceptionWrapper> errors,
                ExceptionWrapper error, boolean updateFirstLastError) {
            assert (error != null);
            assert (error.getException() != null);
            ExceptionWrapper wrap = errors.get(error);
            if (wrap == null) {
                wrap = error;
                errors.put(wrap, wrap);
            }
            wrap.add(error.getException());
            if (updateFirstLastError) {
                lastError = wrap;
                lastErrorMsg = error.e.toString();
                if (firstError == null) {
                    firstError = lastError;
                }
            }
        }

        private void dumpExceptions(PrintStream dumpFile,
                Map<ExceptionWrapper, ExceptionWrapper> errors) {
            // sort errors
            List<ExceptionWrapper> values = new ArrayList<>(errors.keySet());
            Collections.sort(values, ExceptionWrapper.COMPARATOR);
            for (Iterator<ExceptionWrapper> it = values.iterator(); it.hasNext();) {
                ExceptionWrapper elem = it.next();
                trace(dumpFile, elem);
            }
        }

        private void dumpIncludes(PrintStream dumpFile, Map<String, IncludeInfo> includes) {
            List<IncludeInfo> values = new ArrayList<>(includes.values());
            // sort to have failed first
            Collections.sort(values, IncludeInfo.COMPARATOR);
            for (Iterator<IncludeInfo> it = values.iterator(); it.hasNext();) {
                IncludeInfo elem = it.next();
                if ((Diagnostic.getStatisticsLevel() == 1) && !elem.hasErrors()) {
                    // includes are sorted to have failed in the head of list
                    // don't need to trace not failed inclusions
                    // for the first level of statistics
                    break;
                }
                trace(dumpFile, elem);
            }
        }

        private static class IncludeInfo {
            /**
             * absolute path of included file, if include string was correctly resolved
             * otherwise the inclusion string ("myIncl.h" or <sys/types.h>)
             */
            private String include;

            /** success of inclusion */
            private boolean failedInclusion;
            /** amount of includes from all places*/
            private int     counter = 0;
            /** set of files from which was this include */
            private final Map<String, Integer> includedFrom = new HashMap<>();
            /** set of files from which was recursion include */
            private Set<String> recursionFrom = new HashSet<>();

            /** comparator */
            static final Comparator<IncludeInfo> COMPARATOR = new Comparator<IncludeInfo>() {
                @Override
                public int compare(IncludeInfo i1, IncludeInfo i2) {
                    if (i1 == i2) {
                        return 0;
                    }
                    // failed inclusion has priority
                    if (i1.failedInclusion != i2.failedInclusion) {
                        return i1.failedInclusion ? -1 : 1;
                    }
                    // then recurse inclusion has priority
                    if (i1.recursionFrom.size() != i2.recursionFrom.size()) {
                        return (i1.recursionFrom.size() > i2.recursionFrom.size()) ? -1 : 1;
                    }
                    // then counter has priority
                    if (i1.counter != i2.counter) {
                        return (i1.counter > i2.counter) ? -1 : 1;
                    }
                    // then name of include
                    return i1.include.compareTo(i2.include);
                }
            };

            IncludeInfo(String include, boolean failedInclusion) {
                this.include = include;
                this.failedInclusion = failedInclusion;
            }

            void add(String absBaseFilePath, boolean recursion) {
                counter++;
                Integer fileCounter = includedFrom.containsKey(absBaseFilePath) ?
                    includedFrom.get(absBaseFilePath) : Integer.valueOf(0);
                includedFrom.put(absBaseFilePath, Integer.valueOf(fileCounter + 1));
                if (recursion) {
                    recursionFrom.add(absBaseFilePath);
                }
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof IncludeInfo)) {
                    return false;
                }
                // info is equal for the same include files
                IncludeInfo other = (IncludeInfo)obj;
                boolean retValue = (this.isFailedInclude() == other.isFailedInclude());
                retValue &= this.include.equals(other.include);
                return retValue;
            }

            @Override
            public int hashCode() {
                // hash code by code of include file
                int retValue = include.hashCode() + 17*(isFailedInclude()?0:1);
                return retValue;
            }

            @Override
            public String toString() {
                StringBuilder retValue = new StringBuilder();

                retValue.append("===> ").append(include); // NOI18N
                if (this.isFailedInclude()) {
                    retValue.append(" (FAILED)"); // NOI18N
                } else if (this.hasRecursionInclude()) {
                    retValue.append(" (HAS RECURSION)"); // NOI18N
                }
                retValue.append(" included "); // NOI18N
                retValue.append(counter).append(" time(s)"); // NOI18N
                if (Diagnostic.getStatisticsLevel() == 1) {
                    // only first include is interested for failed or recursive inclusion
                    // and no any info except above counter for good inclusions
                    if (this.isFailedInclude() || this.hasRecursionInclude()) {
                        retValue.append("\n").append(indentBuffer.toString()).append(indentBuffer.toString()); // NOI18N
                        String from;
                        if (hasRecursionInclude()) {
                            assert (recursionFrom.size() > 0);
                            assert (recursionFrom.iterator().hasNext());
                            from = recursionFrom.iterator().next();
                            retValue.append(" [RECURSION] "); // NOI18N
                        } else {
                            assert (includedFrom.size() > 0);
                            assert (includedFrom.keySet().iterator().hasNext());
                            from = includedFrom.keySet().iterator().next();
                            retValue.append(" where [").append(includedFrom.get(from)).append("] time(s) "); // NOI18N
                        }
                        retValue.append("from ").append(from); // NOI18N
                    }
                } else {
                    // sort "from" files
                    List<String> files = new ArrayList<>(this.includedFrom.keySet());
                    Collections.sort(files);
                    for (Iterator<String> it = files.iterator(); it.hasNext();) {
                        String from = it.next();
                        retValue.append("\n").append(indentBuffer.toString()); // NOI18N
                        retValue.append(indentBuffer.toString());
                        if (recursionFrom.contains(from)) {
                            retValue.append(" [RECURSION] "); // NOI18N
                        } else {
                            retValue.append(" [").append(includedFrom.get(from)).append("] "); // NOI18N
                        }
                        retValue.append("from ").append(from); // NOI18N
                    }
                }
                return retValue.toString();
            }

            public boolean isFailedInclude() {
                return failedInclusion;
            }

            public boolean hasRecursionInclude() {
                return !recursionFrom.isEmpty();
            }

            public boolean hasErrors() {
                return isFailedInclude() || hasRecursionInclude();
            }
        }

        private static class ExceptionWrapper {

            private static final int CKHECKED_STACK_DEPTH = 15;
            // the first recognition exception of the same types
            private Exception e;
            // collection of error messages
            private final Set<String> errorMessages = new HashSet<>();
            private int counter = 0;
            private final String source;

            /** comparator */
            static final Comparator<ExceptionWrapper> COMPARATOR = new Comparator<ExceptionWrapper>() {

                @Override
                public int compare(ExceptionWrapper w1, ExceptionWrapper w2) {
                    if (w1 == w2) {
                        return 0;
                    }
                    // then order by number of errors
                    if (w1.counter > w2.counter) {
                        return -1;
                    } else if (w1.counter < w2.counter) {
                        return 1;
                    }
                    String msg1 = w1.e.toString();
                    String msg2 = w2.e.toString();
                    return msg1.compareTo(msg2);
                }

            };

            ExceptionWrapper(Exception e, String source) {
                this.e = e;
                this.source = source;
            }

            public Exception getException() {
                return e;
            }

            @Override
            public boolean equals(Object obj) {
                if (this == obj) {
                    return true;
                }
                if (!(obj instanceof ExceptionWrapper)) {
                    return false;
                }
                ExceptionWrapper check = (ExceptionWrapper) obj;
                // check that the same exceptions classes
                if (!check.e.getClass().equals(e.getClass())) {
                    return false;
                }
                // check stack traces with CKHECKED_STACK_DEPTH depth elements, not more
                StackTraceElement[] stack = e.getStackTrace();
                StackTraceElement[] checkStack = check.e.getStackTrace();
                int length = Math.min(CKHECKED_STACK_DEPTH, Math.min(stack.length, checkStack.length));
                for (int i = 0; i < length; i++) {
                    StackTraceElement curElem = stack[i];
                    StackTraceElement checkedElem = checkStack[i];
                    // check if we already can stop checking
                    if (isStopElement(curElem) || check.isStopElement(checkedElem)) {
                        // all before was the same, one of current stack elements
                        // is out of interested stack => equal wrappers
                        return true;
                    } else if (!equals(curElem, checkedElem)) {
                        return false;
                    }
                }
                return true;
            }

            protected String getSourceName() {
                return this.source;
            }

            @Override
            public String toString() {
                StringBuilder retValue = new StringBuilder();
                retValue.append("===> [").append(counter).append("] similar "); // NOI18N
                retValue.append(getSourceName()).append(" error(s) with the first :\n"); // NOI18N
                retValue.append(indentBuffer.toString()).append(e.toString());
                if (getStatisticsLevel() > 2) {
                    String indent = indentBuffer.toString() + indentBuffer.toString() + indentBuffer.toString();
                    StackTraceElement[] stack = e.getStackTrace();
                    for (int i = 0; i < stack.length; i++) {
                        retValue.append("\n").append(indent); // NOI18N
                        retValue.append("at ").append(stack[i]); // NOI18N
                    }
                }
                if (getStatisticsLevel() > 1) {
                    // trace all error messages if more than one
                    if (errorMessages.size() > 1) {
                        String indent = indentBuffer.toString() + indentBuffer.toString();
                        retValue.append("\n").append(indent); // NOI18N
                        retValue.append("+++ all error messages:"); // NOI18N
                        List<String> values = new ArrayList<>(errorMessages);
                        Collections.sort(values);
                        for (Iterator<String> it = values.iterator(); it.hasNext();) {
                            String elem = it.next();
                            retValue.append('\n').append(indent).append(elem);
                        }
                    }
                }
                return retValue.toString();
            }

            @Override
            public int hashCode() {
                int retValue;
                StackTraceElement[] stack = e.getStackTrace();
                // as hash code try to use the first stack trace element
                retValue = (stack.length > 0)? stack[0].hashCode() : e.hashCode();
                return retValue;
            }

            protected boolean isStopElement(StackTraceElement curElem) {
                return false;
            }

            private boolean equals(StackTraceElement elem1, StackTraceElement elem2) {
                assert (elem1 != null);
                assert (elem2 != null);
                return elem1.equals(elem2);
            }

            public void add(Exception e) {
                counter++;
                errorMessages.add(e.toString());
            }
        }

        private static class LexerExceptionWrapper extends ExceptionWrapper {
            LexerExceptionWrapper(RecognitionException e) {
                super(e, "Lexer"); //NOI18N
            }

            @Override
            protected boolean isStopElement(StackTraceElement curElem) {
                // stop if not in lexer's method
                // we are not interested in stack before lexer call
                if (!curElem.getClassName().equals("org.netbeans.modules.cnd.apt.impl.support.generated.APTLexer")) { // NOI18N
                    return true;
                } else if (curElem.getMethodName().equals("nextToken")) { // NOI18N
                    // also we stop on nextToken()
                    return true;
                }
                return false;
            }
        }

        private static class ParserExceptionWrapper extends ExceptionWrapper {
            ParserExceptionWrapper(RecognitionException e) {
                super(e, "Parser");//NOI18N
            }

            @Override
            protected boolean isStopElement(StackTraceElement curElem) {
                // stop if not in parser's method
                // we are not interested in stack before parser call
                if (!curElem.getClassName().equals(CPPParser.class.getName())) {
                    return true;
                } else if (curElem.getMethodName().equals("translation_unit")) { // NOI18N
                    // aslo we stop on translation_unit()
                    return true;
                }
                return false;
            }
        }
    }

}
