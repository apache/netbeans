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
package org.netbeans.modules.cnd.modelimpl.trace;

import java.text.NumberFormat;
import org.netbeans.modules.cnd.apt.support.api.StartEntry;
import org.netbeans.modules.cnd.modelimpl.debug.Diagnostic;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CPPParserEx;
import org.netbeans.modules.cnd.apt.support.APTBuilder;
import org.netbeans.modules.cnd.apt.support.APTTokenStreamBuilder;
import org.netbeans.modules.cnd.apt.support.APTSystemStorage;
import org.netbeans.modules.cnd.apt.support.APTDriver;
import org.netbeans.modules.cnd.apt.utils.APTCommentsFilter;
import org.netbeans.modules.cnd.apt.utils.APTTraceUtils;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.*;
import java.util.List;

import org.netbeans.modules.cnd.antlr.*;
import org.netbeans.modules.cnd.antlr.collections.*;

import java.util.concurrent.ConcurrentHashMap;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.*;
import org.netbeans.modules.cnd.api.project.IncludePath;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.apt.support.APTMacroExpandedStream;
import org.netbeans.modules.cnd.apt.structure.APT;
import org.netbeans.modules.cnd.apt.structure.APTFile;
import org.netbeans.modules.cnd.apt.support.APTFileCacheManager;
import org.netbeans.modules.cnd.apt.support.api.PPIncludeHandler;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.apt.support.api.PPMacroMap;
import org.netbeans.modules.cnd.apt.support.api.PreprocHandler;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTIncludePathStorage;
import org.netbeans.modules.cnd.apt.support.APTMacroCallback;
import org.netbeans.modules.cnd.apt.support.APTToken;
import org.netbeans.modules.cnd.apt.support.APTTokenTypes;
import org.netbeans.modules.cnd.apt.support.ClankDriver;
import org.netbeans.modules.cnd.apt.support.IncludeDirEntry;
import org.netbeans.modules.cnd.apt.support.ResolvedPath;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.accessors.CsmCorePackageAccessor;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.platform.ModelSupport;
import org.netbeans.modules.cnd.repository.support.RepositoryTestUtils;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.util.CharSequences;

/**
 * Tracer for model
 */
public class TraceModel extends TraceModelBase {

    public static final class TestResult {

        private long time;
        private long lineCount;

        private TestResult() {
            this(0);
        }

        private TestResult(long time) {
            this.setTime(time);
        }

        private TestResult(long time, long lineCount) {
            this.setTime(time);
            this.setLineCount(lineCount);
        }

        public String getLPS() {
            if (time == 0 || lineCount <= 0) {
                return "N/A";  // NOI18N
            } else {
                return "" + (lineCount * 1000 / time);
            }
        }

        public long getTime() {
            return time;
        }

        private void setTime(long time) {
            this.time = time;
        }

        public long getLineCount() {
            return (lineCount < 0) ? 0 : lineCount;
        }

        public boolean isLineCountValid() {
            return lineCount >= 0;
        }

        private void setLineCount(long lineCount) {
            this.lineCount = lineCount;
        }

        private void accumulate(TestResult toAdd) {
            time += toAdd.time;
            if (isLineCountValid()) {
                if (toAdd.isLineCountValid()) {
                    lineCount += toAdd.getLineCount();
                } else {
                    //		    lineCount = -1;
                }
            }
        }
    }

    public interface ParsingTimeResultListener {
        void notifyParsingTime(TestResult parsingTime);
    }

    void addParsingTimeResultListener(ParsingTimeResultListener listener) {
        parsingTimeResultListener = listener;
    }

    private static final int APT_REPEAT_TEST = Integer.getInteger("apt.repeat.test", 3).intValue(); // NOI18N

    public static void main(String[] args) {
        new TraceModel(true).test(args);
        APTDriver.close();
        ClankDriver.close();
        APTFileCacheManager.close();
    //System.out.println("" + org.netbeans.modules.cnd.apt.utils.APTIncludeUtils.getHitRate());
    }
    private static CsmTracer tracer = new CsmTracer(false);
    private boolean showAstWindow = false;
    private boolean dumpAst = false;
    private boolean dumpModel = false;
    private boolean dumpLib = false;
    private boolean dumpFileOnly = false;
    private boolean showTime = false;
    //private boolean showErrorCount = false;
    private boolean testLibProject = false;
    private boolean deep = true;
    private boolean showMemoryUsage = false;
    private boolean testUniqueName = false;
    private int     testAPTIterations = APT_REPEAT_TEST;
    private boolean testAPT = false;
    private boolean testAPTLexer = false;
    private boolean testAPTDriver = false;
    private boolean testAPTWalkerVisit = false;
    private boolean testAPTWalkerGetStream = false;
    private boolean testAPTWalkerGetExpandedStream = false;
    private boolean testAPTWalkerGetFilteredStream = false;
    private boolean testAPTParser = false;
    private boolean breakAfterAPT = false;
    private boolean stopBeforeAll = false;
    private boolean stopAfterAll = false;
    private boolean printTokens = false;
    private boolean dumpModelAfterCleaningCache = false; // --clean4dump
    private boolean dumpTemplateParameters = false; // --tparm
    private int repeatCount = 1; // --repeat
    private boolean dumpStatistics = false;
    private static final int DEFAULT_TRACEMODEL_STATISTICS_LEVEL = 1;
    private String dumpFile = null;
    private String dumpDir = null;
    private static final String statPostfix = ".stat"; // NOI18N

    // Callback options
    private boolean dumpPPState = false;

    public void setDumpModel(boolean dumpModel) {
        this.dumpModel = dumpModel;
    }

    /**
     * Note that if you switch dumping ON - this must be done BEFORE parsing starts
     * since this flag now affects gathering handlers as well.
     */
    public void setDumpPPState(boolean dumpPPState) {
        this.dumpPPState = dumpPPState;
    }

    public boolean isDumpingPPState() {
        return this.dumpPPState;
    }
    
    private boolean listFilesAtEnd = false;
    private boolean testRawPerformance = false;
    private boolean printUserFileList = false;
    private boolean quiet = false;
    private boolean memBySize = false;
    private boolean doCleanRepository = Boolean.getBoolean("cnd.clean.repository");
//    private boolean testFolding = false;
    private Map<String, Long> cacheTimes = new HashMap<>();
    private int lap = 0;
    private final Map<CsmFile, PreprocHandler> states = new ConcurrentHashMap<>();
    public interface TestHook {

        void parsingFinished(CsmFile file, PreprocHandler preprocHandler);
    }

    TestHook hook = new TestHook() {

        @Override
        public void parsingFinished(CsmFile file, PreprocHandler preprocHandler) {
            if (dumpPPState) {
                states.put(file, preprocHandler);
            }
        }
    };
    private ParsingTimeResultListener parsingTimeResultListener;

    private void notifyPrseTime(TestResult total) {
        if (parsingTimeResultListener != null) {
            parsingTimeResultListener.notifyParsingTime(total);
        }
    }

    @Override
    public void shutdown(boolean clearCache) {
        super.shutdown(clearCache); 
        states.clear();
    }

    public interface ErrorListener {
        void error(String text, int line, int column);
    }


    public TraceModel(boolean cleanCache) {
        this(cleanCache, null);
    }

    public TraceModel(boolean cleanCache, TraceModelFileFilter filter) {
        super(cleanCache, filter);
        CsmCorePackageAccessor.get().setFileImplTestHook(hook);
    }

    @Override
    protected ProcessFlagResult processFlag(char flag, String argRest) {
        ProcessFlagResult result = super.processFlag(flag, argRest);
        if (result != ProcessFlagResult.NONE_PROCESSED) {
            return result;
        }
        // it's easier to set the most "popular" return value here and NONE_... in default case
        result = ProcessFlagResult.CHAR_PROCESSED;
        switch (flag) {
            case 'n':
                deep = false;
                break;
            case 'E':
                testAPTIterations = 0;
                testAPTWalkerGetFilteredStream = true;
                printTokens = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'e':
                System.setErr(System.out);
                break;
            case 'w':
                showAstWindow = true;
                break;
            case 'a':
                dumpAst = true;
                break;
            case 'm':
                dumpModel = true;
                dumpFileOnly = false;
                break; // -m overrides -f
            case 'M':
                showMemoryUsage = true;
                break;
            case 'u':
                testUniqueName = true;
                break;
            case 'f':
                if (!dumpModel) { // do not ovverride -m
                    dumpModel = true;
                    dumpFileOnly = true;
                }
                break;
            case 't':
                showTime = true;
                break;
            //            case 'L':   testLexer = true; break;
            //case 'c':   showErrorCount = true; break;
            case 'l':
                testLibProject = true;
                break;
            case 'p':
                dumpPPState = true;
                break;
            // "-SDir" defines dump directory for per file statistics
            case 'S':
                dumpStatistics = true;
                if (argRest.length() > 0) {
                    // dump directory for per file statistics
                    File perFileDumpDir = new File(argRest);
                    perFileDumpDir.mkdirs();
                    if (!perFileDumpDir.isDirectory()) {
                        print("Parameter -S" + argRest + " does not specify valid directory"); // NOI18N
                    } else {
                        this.dumpDir = perFileDumpDir.getAbsolutePath();
                    }
                    result = ProcessFlagResult.ALL_PROCESSED;
                }
                break;
            // "-sFileName" defines global statistics dump file
            case 's':
                dumpStatistics = true;
                if (argRest.length() > 0) {
                    File globalDumpFile = new File(argRest);
                    if (globalDumpFile.exists()) {
                        globalDumpFile.delete();
                    }
                    try {
                        if (globalDumpFile.getParentFile() != null) {
                            globalDumpFile.getParentFile().mkdirs();
                            globalDumpFile.createNewFile();
                            this.dumpFile = globalDumpFile.getAbsolutePath();
                            result = ProcessFlagResult.ALL_PROCESSED;
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace(System.err);
                    }
                }
                break;
            case 'A':
                testAPT = true;
                testAPTWalkerVisit = true;
                testAPTWalkerGetStream = true;
                testAPTWalkerGetExpandedStream = true;
                testAPTWalkerGetFilteredStream = true;
                testAPTLexer = true;
                breakAfterAPT = true;
                testAPTDriver = true;
                break;
            //            case 'b':   testAPTPlainLexer = true; testAPT = true; breakAfterAPT = true; break;
            case 'B':
                testAPTLexer = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'o':
                printTokens = true;
                break;
            case 'v':
                testAPTWalkerVisit = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'g':
                testAPTWalkerGetStream = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'G':
                testAPTWalkerGetExpandedStream = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'F':
                testAPTWalkerGetFilteredStream = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'd':
                testAPTDriver = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'h':
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'H':
                testAPTParser = true;
                testAPT = true;
                breakAfterAPT = true;
                break;
            case 'O':
                stopBeforeAll = true;
                stopAfterAll = true;
                break;
            case 'q':
                quiet = true;
                break;
            default:
                result = ProcessFlagResult.NONE_PROCESSED;
        }
        return result;
    }

    @Override
    protected boolean processFlag(String flag) {
        if (super.processFlag(flag)) {
            return true;
        } else if ("dumplib".equals(flag)) { // NOI18N
            dumpLib = true;
        } else if ("listfiles".equals(flag)) { // NOI18N
            listFilesAtEnd = true;
        } else if ("raw".equals(flag)) { // NOI18N
            testRawPerformance = true;
        //TraceFlags.DO_NOT_RENDER = true;
        } else if ("listfiles".equals(flag)) { // NOI18N
            printUserFileList = true;
        } else if ("mbs".equals(flag)) { // NOI18N
            memBySize = true;
        } else if ("cleanrepository".equals(flag)) { // NOI18N
            doCleanRepository = true;
//        } else if ("folding".equals(flag)) { // NOI18N
//            testFolding = true;
        } else if ("clean4dump".equals(flag)) { // NOI18N
            dumpModelAfterCleaningCache = true;
        } else if ("tparm".equals(flag)) { // NOI18N
            dumpTemplateParameters = true;
        } else if ("repeat".equals(flag) || flag.startsWith("repeat:")) { // NOI18N
            int len = "repeat".length(); // NOI18N
            if (flag.length() == len) {
                repeatCount = 2;
            } else {
                repeatCount = Integer.parseInt(flag.substring(len + 1));
            }
        } else {
            return false;
        }
        return true;
    }

    private void test(String[] args) {
        try {
            processArguments(args);
            doTest();
        } catch (Error thr) {
            System.err.printf("%n");
            DiagnosticExceptoins.register(thr);
            return;
        } finally {
            getModel().shutdown();
        }
    }

    /*package*/
    void doTest() {
        if (repeatCount > 1) {
            for (int i = 0; i < repeatCount; i++) {
                print("\n\n==================== Pass " + i + "====================\n"); // NOI18N
                doTest2();
                resetProject();
            //sleep(2000, "Waiting for repository to shutdown");
            }
        } else {
            doTest2();
        }
    }

    /*package*/
    void doTest2() {
        if (stopBeforeAll) {
            waitAnyKey();
        }
        if (dumpStatistics) {
            if (dumpFile == null && dumpDir == null) {
                print("Turning OFF statistics as neither global file nor directory is specified"); // NOI18N
                dumpStatistics = false;
            } else {
                print("Dumping Statistics is ON"); // NOI18N
                if (Diagnostic.getStatisticsLevel() == 0) {
                    // need to set the default level
                    Diagnostic.setStatisticsLevel(DEFAULT_TRACEMODEL_STATISTICS_LEVEL);
                }
                if (dumpFile != null) {
                    print("Global Dump file is " + dumpFile); // NOI18N
                }
                if (dumpDir != null) {
                    print("Dump directory for per file statistics is " + dumpDir); // NOI18N
                }
            }
        }

        if (testLibProject) {
            testLibProject();
        }

        if (printUserFileList) {
            print("Processing files:\n"); // NOI18N
            for (NativeFileItem file : getFileItems()) {
                print(file.getAbsolutePath() + ' ', false);
            }
            print("");
        }

        long memUsed = 0;
        if (showMemoryUsage) {
            memUsed = usedMemory();
        }

        long t = System.currentTimeMillis();
        TestResult total = test();
        total.time = System.currentTimeMillis() - t;
        notifyPrseTime(total);

        if (testRawPerformance) {
            print("Take one finished."); // NOI18N
            print("Total parsing time " + total.time + " ms"); // NOI18N
            calculateAverageLPS(total, true);
            print("Lines count " + total.lineCount); // NOI18N
            print("Average LPS " + total.getLPS()); // NOI18N
            if (showMemoryUsage) {
                showMemoryUsage(memUsed);
            }

            //	    for (int i = 0; i < 100; i++) {
            //		initProject();
            //		test();
            //		showMemoryUsage(memUsed);
            //	    }
            print("\nTesting raw performance: parsing project, take two\n"); // NOI18N
            resetProject();
            if (stopBeforeAll) {
                waitAnyKey();
            }
            t = System.currentTimeMillis();
            total = test();
            total.time = System.currentTimeMillis() - t;
        }

        /* this unnecessary since we call waitProjectParsed() for each file
        if( showTime ) {
        print("Waiting for the rest of the parser queue to be parsed");
        }
        waitProjectParsed();
         */

        if (dumpLib) {
            CsmCacheManager.enter();
            try {
                for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
                    CsmProject lib = (CsmProject) it.next();
                    tracer.dumpModel(lib);
                }
            } finally {
                CsmCacheManager.leave();
            }
        }

        if (isShowTime()) {

            int maxLen = 0;
            for (int i = 0; i < CPPParserEx.MAX_GUESS_IDX; i++) {
                if (CPPParserEx.guessingNames[i] != null) {
                    int len = CPPParserEx.guessingNames[i].length();
                    if (len > maxLen) {
                        maxLen = len;
                    }
                }
            }

            boolean printGuessStat = false;
            // check if we had the statistics
            for (int i = 0; i < CPPParserEx.MAX_GUESS_IDX; i++) {
                if (CPPParserEx.guessingCount[i] != 0) {
                    printGuessStat = true;
                    break;
                }
            }
            if (listFilesAtEnd) {
                print("\n========== User project files =========="); // NOI18N
                List<CharSequence> l = new ArrayList<>(getProject().getAllFiles().size());
                for (Iterator it = getProject().getAllFiles().iterator(); it.hasNext();) {
                    CsmFile file = (CsmFile) it.next();
                    l.add(file.getAbsolutePath());
                }
                Collections.sort(l, CharSequences.comparator());
                for (Iterator it = l.iterator(); it.hasNext();) {
                    print((String) it.next());
                }
                print("\n========== Library files =========="); // NOI18N
                l = new ArrayList<>();
                for (Iterator it1 = getProject().getLibraries().iterator(); it1.hasNext();) {
                    ProjectBase lib = (ProjectBase) it1.next();
                    for (Iterator it2 = lib.getAllFiles().iterator(); it2.hasNext();) {
                        CsmFile file = (CsmFile) it2.next();
                        l.add(file.getAbsolutePath());
                    }
                }
                Collections.sort(l, CharSequences.comparator());
                for (Iterator it = l.iterator(); it.hasNext();) {
                    print((String) it.next());
                }
            }
            if (printGuessStat) {
                print("\nGuessing statistics:"); // NOI18N
                print(
                        "Id" // NOI18N
                        + "\t" + padR("Rule:Line", maxLen) // NOI18N
                        + "\tTime" // NOI18N
                        + "\tCount" // NOI18N
                        + "\tFail" // NOI18N
                        //+ "\tTime in failures"
                        + "\tSuccess, %"); // NOI18N
                long guessingTime = 0;
                for (int i = 0; i < CPPParserEx.MAX_GUESS_IDX; i++) {
                    guessingTime += CPPParserEx.guessingTimes[i];
                    //double sps = (CPPParserEx.guessingTimes[i] !=0) ? ((double)CPPParserEx.guessingCount[i])/CPPParserEx.guessingTimes[i] : 0;
                    double usa = 0;
                    if (CPPParserEx.guessingCount[i] != 0) {
                        usa = (1 - ((double) CPPParserEx.guessingFailures[i]) / CPPParserEx.guessingCount[i]) * 100;
                    }
                    print("" + i + "\t" + padR(CPPParserEx.guessingNames[i], maxLen) // NOI18N
                            + "\t" + CPPParserEx.guessingTimes[i] // NOI18N
                            + "\t" + CPPParserEx.guessingCount[i] // NOI18N
                            + "\t" + CPPParserEx.guessingFailures[i] // NOI18N
                            //+ "\t" + (int)sps
                            + "\t" + (int) usa); // NOI18N
                }

                print("\nTotal guessing time: " + guessingTime + "ms " + "(" + ((total.getTime() != 0) ? guessingTime * 100 / total.getTime() : -1) + "% of total parse time)"); // NOI18N
            }
        }
        if (isShowTime() || testRawPerformance) {
            print("Total parsing time: " + total.getTime() + "ms"); // NOI18N
            //print("Average LPS: " + total.getLPS());
            calculateAverageLPS(total, !testRawPerformance);
            print("Lines count " + total.lineCount); // NOI18N
            String text = testRawPerformance ? "Raw performance (average LPS): " : "Average LPS: "; // NOI18N
            print(text + total.getLPS());
            int userFiles = countUserFiles();
            int systemHeaders = countSystemHeaders();
            print("" + userFiles + " user files"); // NOI18N
            print("" + systemHeaders + " system headers"); // NOI18N
        }
        if (showMemoryUsage) {
            showMemoryUsage(memUsed);
        }
        if (isShowTime() || showMemoryUsage || dumpModel || dumpFileOnly || dumpPPState) {
            print("\n"); // NOI18N
        }
        if (dumpStatistics) {
            if (this.dumpFile != null) {
                try {
                    Diagnostic.dumpUnresolvedStatistics(this.dumpFile, true);
                } catch (FileNotFoundException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
        }

        if (TraceFlags.CLEAN_MACROS_AFTER_PARSE) {
            List restoredFiles = ProjectBase.testGetRestoredFiles();
            if (restoredFiles != null) {
                System.err.println("the number of restored files " + restoredFiles.size());
                for (int i = 0; i < restoredFiles.size(); i++) {
                    System.err.println("#" + i + ":" + restoredFiles.get(i));
                }
            }
        }

        if (dumpModelAfterCleaningCache) {
            anyKey("Press any key to clean repository:"); // NOI18N
            RepositoryTestUtils.deleteDefaultCacheLocation();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            System.gc();
            anyKey("Press any key to dump model:"); // NOI18N
            if (!dumpFileOnly) {
                CsmCacheManager.enter();
                try {
                    tracer.dumpModel(getProject());
                } finally {
                    CsmCacheManager.leave();
                }
            }
        }
        if (stopAfterAll) {
            System.gc();
            anyKey("Press any key to finish:"); // NOI18N
        }
    }

    private void anyKey(String message) {
        System.err.println(message);
        try {
            System.in.read();
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
    }

    private void showMemoryUsage(long memUsed) {
        long newMemUsed = usedMemory();
        long memDelta = newMemUsed - memUsed;
        NumberFormat nf = NumberFormat.getIntegerInstance();
        nf.setGroupingUsed(true);
        nf.setMinimumIntegerDigits(6);
        print("Amount of memory used" + getLap() + ": " + nf.format((memDelta) / 1024) + " Kb"); // NOI18N
        if (memBySize) {
            TestResult rInc = new TestResult();
            TestResult rExc = new TestResult();
            calculateAverageLPS(rInc, true);
            calculateAverageLPS(rExc, false);
            print("User code lines:  " + rExc.lineCount); // NOI18N
            print("Total lines (including all headers):  " + rInc.lineCount); // NOI18N
            print("Memory usage per (user) line " + getLap() + '\t' + nf.format(memDelta / rExc.lineCount) + " bytes per line"); // NOI18N
            print("Memory usage per (total) line" + getLap() + '\t' + nf.format(memDelta / rInc.lineCount) + " bytes per line"); // NOI18N
        }
    }

    private void waitAnyKey() {
        System.out.println("Press any key to continue:"); // NOI18N
        try {
            System.in.read();
        } catch (IOException ex) {
            DiagnosticExceptoins.register(ex);
        }
    }

    private TestResult test() {
        lap++;
        TestResult total = new TestResult();
        //for (int i = 0; i < fileList.size(); i++) {
        for (NativeFileItem item : getFileItems()) {
            try {
                TestResult res = test(item);
                total.accumulate(res);
            } catch (Exception e) {
                DiagnosticExceptoins.register(e);
            }
        }

        return total;
    }

    private String getLap() {
        return " (lap " + lap + ") "; // NOI18N
    }

    private String padR(String s, int len) {
        if (s == null) {
            s = "";
        }
        if (s.length() >= len) {
            return s;
        } else {
            StringBuilder sb = new StringBuilder(s);
            sb.setLength(len);
            for (int i = s.length(); i < len; i++) {
                sb.setCharAt(i, ' ');
            }
            return sb.toString();
        }
    }

//    private void sleep(int timeout, String message) {
//	System.err.printf("Sleeping: %s\n", message);
//	sleep(timeout);
//	System.err.printf("Awoke (%s)\n", message);
//    }
    private void sleep(int timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException ex) {
            //ex.printStackTrace();
        }
    }
    private final APTSystemStorage sysAPTData = APTSystemStorage.getInstance();
    private final APTIncludePathStorage userPathStorage = new APTIncludePathStorage();
    
    private static List<IncludePath> toIncludePaths(FileSystem fileSystem, Collection<String> paths) {
        if (paths != null && paths.size() > 0) {
            List<IncludePath> result = new ArrayList<IncludePath>(paths.size());
            for (String path : paths) {
                result.add(new IncludePath(fileSystem, path, false));
            }
            return result;
        }
        return Collections.<IncludePath>emptyList();
    }
    
    private PPIncludeHandler getIncludeHandler(FileObject fo) {
        FileSystem localFS = CndFileUtils.getLocalFileSystem();
        List<IncludePath> systemIncludes = toIncludePaths(localFS, getSystemIncludes());
        List<IncludeDirEntry> sysIncludes = sysAPTData.getIncludes(systemIncludes.toString(), systemIncludes); // NOI18N
        List<String> qInc = getQuoteIncludePaths();
        if (isPathsRelCurFile()) {
            qInc = new ArrayList<>(getQuoteIncludePaths().size());
            for (Iterator<String> it = getQuoteIncludePaths().iterator(); it.hasNext();) {
                String path = it.next();
                if (CndPathUtilities.isPathAbsolute(path)) {
                    qInc.add(path);
                } else {
                    FileObject dirFO = fo.getParent();
                    FileObject pathFile = dirFO.getFileObject(path);
                    if (pathFile != null && pathFile.isValid()) {
                        path = pathFile.getPath();
                        qInc.add(path);
                    }
                }
            }
        }
        StartEntry startEntry = new StartEntry(localFS, fo.getPath(), getProject().getUIDKey());
        List<IncludeDirEntry> userIncludes = userPathStorage.get(qInc.toString(), toIncludePaths(localFS, qInc));
        return APTHandlersSupport.createIncludeHandler(startEntry, sysIncludes, userIncludes, Collections.<FSPath>emptyList(), null);
    }

    private PPMacroMap getMacroMap(FileObject fo) {
        //print("SystemIncludePaths: " + systemIncludePaths.toString() + "\n");
        //print("QuoteIncludePaths: " + quoteIncludePaths.toString() + "\n");
        PPMacroMap map = APTHandlersSupport.createMacroMap(getSysMap(fo), getMacros());
        return map;
    }

//    private PreprocHandler getPreprocHandler(File file) {
//	PreprocHandler preprocHandler = APTHandlersSupport.createPreprocHandler(getMacroMap(file), getIncludeHandler(file), !file.getPath().endsWith(".h")); // NOI18N
//	return preprocHandler;
//    }
    private PPMacroMap getSysMap(FileObject fo) {
        PPMacroMap map = sysAPTData.getMacroMap("TraceModelSysMacros", getSysMacros()); // NOI18N
        return map;
    }

//
//    @Override
//    protected List<String> getSystemIncludes() {
//	List<String> result = super.getSystemIncludes();
//	if( result.isEmpty() && ! dumpPPState ) {
//	    // NB: want any fake value but not for suite.sh which is run with dumpPPState
//	    result.add("/usr/non-exists"); // NOI18N
//	}
//	return result;
//    }
//
//    @Override
//    protected List<String> getSysMacros() {
//	List<String> result = super.getSysMacros();
//	if( result.isEmpty() && ! dumpPPState ) {
//	    // NB: want any fake value but not for suite.sh which is run with dumpPPState
//	    result.add("NO_DEFAULT_DEFINED_SYSTEM_MACROS"); // NOI18N
//	}
//	return result;
//    }
    private long testAPTLexer(FileObject fo, boolean printTokens) throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        print("Testing APT lexer:"); // NOI18N
        long time = System.currentTimeMillis();
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = new BufferedInputStream(fo.getInputStream(), TraceFlags.BUF_SIZE);
            reader = new InputStreamReader(stream, FileEncodingQuery.getDefaultEncoding());
            APTFile.Kind langKind = APTDriver.langFlavorToAPTFileKind(getFileLanguage(fo));
            TokenStream ts = APTTokenStreamBuilder.buildTokenStream(fo.getPath(), reader, langKind);
            for (Token t = ts.nextToken(); !APTUtils.isEOF(t); t = ts.nextToken()) {
                if (printTokens) {
                    print("" + t);
                }
            }
            time = System.currentTimeMillis() - time;
            if (isShowTime()) {
                print("APT Lexing " + fo.getNameExt() + " took " + time + " ms"); // NOI18N
            }
            return time;
        } finally {
            if (reader != null) {
                reader.close();
            }
            if (stream != null) {
                stream.close();
            }
        }
    }

    private long testAPTWalkerVisit(APTFile apt, FileBuffer buffer) throws TokenStreamException, IOException {
        FileObject fo = buffer.getFileObject();
        boolean cleanAPT = apt == null;
        long time = System.currentTimeMillis();
        PreprocHandler ppHandler = APTHandlersSupport.createPreprocHandler(getMacroMap(fo), getIncludeHandler(fo), true, CharSequences.empty(), CharSequences.empty());
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();
            apt = APTDriver.findAPTLight(buffer, APTHandlersSupport.getAPTFileKind(ppHandler));
        }
        APTWalkerTest walker = new APTWalkerTest(apt, ppHandler);
        walker.visit();
        time = System.currentTimeMillis() - time;

        if (isShowTime()) {
            print("Visiting APT " + (cleanAPT ? "with cleaning APT in driver" : "") + " took " + time + " ms"); // NOI18N
            print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms"); // NOI18N
        }

        //        time = System.currentTimeMillis();
        //        if (cleanAPT) {
        //            invalidateAPT(file);
        //            time = System.currentTimeMillis();
        //            apt = APTDriver.getInstance().findAPT(file);
        //        }
        //        walker = new APTWalkerTest(apt, getMacroMap(file), getIncludeHandler(file));
        //        walker.nonRecurseVisit();
        //        time = System.currentTimeMillis() - time;
        //
        //        if( showTime ) {
        //            print("Non recursive visiting APT "+ (cleanAPT ? "with cleaning APT in driver":"") + " took " + time + " ms");
        //        }
        return time;
    }

    private long testAPTWalkerGetStream(APTFile apt, FileBuffer buffer, boolean expand, boolean filter, boolean printTokens) throws TokenStreamException, IOException {
        FileObject fo = buffer.getFileObject();
        boolean cleanAPT = apt == null;
        long time = System.currentTimeMillis();
        PPMacroMap macroMap = getMacroMap(fo);
        PreprocHandler ppHandler = APTHandlersSupport.createPreprocHandler(macroMap, getIncludeHandler(fo), true, getFileLanguage(fo), CharSequences.empty());
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();
            apt = APTDriver.findAPT(buffer, APTHandlersSupport.getAPTFileKind(ppHandler));
        }
        APTWalkerTest walker = new APTWalkerTest(apt, ppHandler);
        TokenStream ts = walker.getTokenStream();
        if (expand) {
            ts = new APTMacroExpandedStream(ts, (APTMacroCallback)macroMap, false);
        }
        if (filter) {
            ts = APTLanguageSupport.getInstance().getFilter(APTLanguageSupport.GNU_CPP).getFilteredStream(new APTCommentsFilter(ts));
        }
        int lastLine = -1;
        boolean forceNewLine = false;
        for (Token t = ts.nextToken(); !APTUtils.isEOF(t); t = ts.nextToken()) {
            if (printTokens) {
                String text = " " + t.getText(); // NOI18N
                boolean newLine = forceNewLine || (t.getLine() != lastLine);
                forceNewLine = false;
                if (isIncludeToken(t.getType())) {
                    APTToken aptToken = (APTToken)t;
                    ResolvedPath path = (ResolvedPath) aptToken.getProperty(ResolvedPath.class);
                    if (path != null) {
                        assert aptToken.getProperty(Boolean.class) != null;
                        String prefix = ((Boolean)aptToken.getProperty(Boolean.class)) ? "#=> " : "#<= "; // NOI18N
                        text = prefix + "\"" + path.getPath() + "\" [" + path.getFolder() + "]"; // NOI18N
                    } else {
                        text = "#include " + t.toString(); // NOI18N
                    }
                    newLine = true;
                    forceNewLine = true;
                }
                print(text, newLine);
            }
            lastLine = t.getLine();
        }
        if (printTokens && lastLine >= 0) {
            print("", true);
        }
        time = System.currentTimeMillis() - time;

        if (isShowTime()) {
            print("Getting" + (expand ? " expanded" : "") + (filter ? " filtered" : "") + " APT token stream " + (cleanAPT ? "with cleaning APT in driver" : "") + " took " + time + " ms"); // NOI18N
            print(" resolving include paths took " + walker.getIncludeResolvingTime() + " ms"); // NOI18N
        }
        return time;
    }

    private static boolean isIncludeToken(int kind) {
        return kind == APTTokenTypes.INCLUDE || kind == APTTokenTypes.INCLUDE_NEXT;
    }

    private long testAPTParser(NativeFileItem item, boolean cleanAPT) throws IOException, RecognitionException, TokenStreamException {
        FileBuffer buffer = ModelSupport.createFileBuffer(item.getFileObject());
        print("Testing APT Parser"); // NOI18N
        long time = System.currentTimeMillis();
        if (cleanAPT) {
            invalidateAPT(buffer);
            time = System.currentTimeMillis();
        }
        FileImpl fileImpl = (FileImpl) getProject().testAPTParseFile(item);
        getProject().waitParse();
        time = System.currentTimeMillis() - time;

        if (isShowTime()) {
            print("Parsing" + (cleanAPT ? " with cleaning APT in driver" : "") + " took " + time + " ms"); // NOI18N
        }
        return time;
    }

    private void testAPT(NativeFileItem item) throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {
        FileObject fo = item.getFileObject();
        FileBuffer buffer = ModelSupport.createFileBuffer(item.getFileObject());
        print("Testing APT: " + fo.getNameExt()); // NOI18N
        long minLexer = Long.MAX_VALUE;
        long maxLexer = Long.MIN_VALUE;
        long minAPTLexer = Long.MAX_VALUE;
        long maxAPTLexer = Long.MIN_VALUE;
        if (testAPTLexer) {
            for (int i = -1; i < testAPTIterations; i++) {
                long val = testAPTLexer(fo, i == -1 ? printTokens : false);
                minAPTLexer = Math.min(minAPTLexer, val);
                maxAPTLexer = Math.max(maxAPTLexer, val);
            }
        }
        APTFile apt = null;
        minDriver = Long.MAX_VALUE;
        maxDriver = Long.MIN_VALUE;
        if (testAPTDriver) {
            for (int i = -1; i < testAPTIterations; i++) {
                invalidateAPT(buffer);
                apt = testAPTDriver(buffer, i == -1 ? true : false);
            }
        }
        boolean cleanAPT = minDriver == Long.MAX_VALUE;

        long minVisit = Long.MAX_VALUE;
        long maxVisit = Long.MIN_VALUE;
        if (testAPTWalkerVisit) {
            for (int i = -1; i < testAPTIterations; i++) {
                long val = testAPTWalkerVisit(apt, buffer);
                minVisit = Math.min(minVisit, val);
                maxVisit = Math.max(maxVisit, val);
            }
        }
        long minGetTS = Long.MAX_VALUE;
        long maxGetTS = Long.MIN_VALUE;
        if (testAPTWalkerGetStream) {
            for (int i = -1; i < testAPTIterations; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, false, false, i == -1 ? printTokens : false);
                minGetTS = Math.min(minGetTS, val);
                maxGetTS = Math.max(maxGetTS, val);
            }
        }
        long minGetExpandedTS = Long.MAX_VALUE;
        long maxGetExpandedTS = Long.MIN_VALUE;
        if (testAPTWalkerGetExpandedStream) {
            for (int i = -1; i < testAPTIterations; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, true, false, i == -1 ? printTokens : false);
                minGetExpandedTS = Math.min(minGetExpandedTS, val);
                maxGetExpandedTS = Math.max(maxGetExpandedTS, val);
            }
        }
        long minGetFilteredTS = Long.MAX_VALUE;
        long maxGetFilteredTS = Long.MIN_VALUE;
        if (testAPTWalkerGetFilteredStream) {
            for (int i = -1; i < testAPTIterations; i++) {
                long val = testAPTWalkerGetStream(apt, buffer, true, true, i == -1 ? printTokens : false);
                minGetFilteredTS = Math.min(minGetFilteredTS, val);
                maxGetFilteredTS = Math.max(maxGetFilteredTS, val);
            }
        }
        long minParsing = Long.MAX_VALUE;
        long maxParsing = Long.MIN_VALUE;
        long minAPTParsing = Long.MAX_VALUE;
        long maxAPTParsing = Long.MIN_VALUE;
        if (testAPTParser) {
            for (int i = -1; i < testAPTIterations; i++) {
                long val = testAPTParser(item, cleanAPT);
                minAPTParsing = Math.min(minAPTParsing, val);
                maxAPTParsing = Math.max(maxAPTParsing, val);
            }
        }
        if (isShowTime()) {
            print("APT BEST/WORST results for " + fo.getPath()); // NOI18N
            if (minLexer != Long.MAX_VALUE) {
                print(minLexer + " ms BEST Plain lexer"); // NOI18N
                print(maxLexer + " ms WORST Plain lexer"); // NOI18N
            }
            if (minAPTLexer != Long.MAX_VALUE) {
                print(minAPTLexer + " ms BEST APT lexer"); // NOI18N
                print(maxAPTLexer + " ms WORST APT lexer"); // NOI18N
            }
            if (minDriver != Long.MAX_VALUE) {
                print(minDriver + " ms BEST Building APT:"); // NOI18N
                print(maxDriver + " ms WORST Building APT:"); // NOI18N
            }

            if (minVisit != Long.MAX_VALUE) {
                print(minVisit + " ms BEST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxVisit + " ms WORST Visiting APT" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minGetTS != Long.MAX_VALUE) {
                print(minGetTS + " ms BEST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxGetTS + " ms WORST Getting APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minGetExpandedTS != Long.MAX_VALUE) {
                print(minGetExpandedTS + " ms BEST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxGetExpandedTS + " ms WORST Getting Expanded APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minGetFilteredTS != Long.MAX_VALUE) {
                print(minGetFilteredTS + " ms BEST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxGetFilteredTS + " ms WORST Getting Expanded Filtered APT token stream" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
            if (minParsing != Long.MAX_VALUE) {
                print(minParsing + " ms BEST Plaing Parsing"); // NOI18N
                print(maxParsing + " ms WORST Plaing Parsing"); // NOI18N
            }
            if (minAPTParsing != Long.MAX_VALUE) {
                print(minAPTParsing + " ms BEST APT parsing" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
                print(maxAPTParsing + " ms WORST APT parsing" + (cleanAPT ? " with cleaning APT in driver:" : ":")); // NOI18N
            }
        }
    }
    private static String firstFile = null;

    private void invalidateAPT(final FileBuffer buffer) {
        String absPath = buffer.getAbsolutePath().toString();
        if (firstFile == null || firstFile.equalsIgnoreCase(absPath)) {
            firstFile = absPath;
            APTDriver.invalidateAll();
            ClankDriver.invalidateAll();
            APTFileCacheManager.invalidateAll();
            getProject().debugInvalidateFiles();
        } else {
            APTDriver.invalidateAPT(buffer);
            ClankDriver.invalidate(buffer);
            APTFileCacheManager.getInstance(buffer.getFileSystem()).invalidate(buffer.getAbsolutePath());
        }
    }
    long minDriver = Long.MAX_VALUE;
    long maxDriver = Long.MIN_VALUE;

    private APTFile testAPTDriver(final FileBuffer buffer, boolean buildXML) throws IOException, FileNotFoundException {
        FileObject fo = buffer.getFileObject();
        long oldMem = usedMemory();
        long time = System.currentTimeMillis();
        APTFile apt = APTDriver.findAPT(buffer, APTDriver.langFlavorToAPTFileKind(getFileLanguage(fo)));
        time = System.currentTimeMillis() - time;
        long newMem = usedMemory();
        if (isShowTime()) {
            minDriver = Math.min(minDriver, time);
            maxDriver = Math.max(maxDriver, time);
            print("Building APT for " + fo.getNameExt() + "\n SIZE OF FILE:" + fo.getSize() / 1024 + "Kb\n TIME: took " + time + " ms\n MEMORY: changed from " + (oldMem) / (1024) + " to " + newMem / (1024) + "[" + (newMem - oldMem) / 1024 + "]Kb"); // NOI18N
        }

        //        System.out.println("apt tree: \n" + APTTraceUtils.toStringList(apt));
        if (buildXML) {
            File outDir = new File("/tmp/aptout/"); // NOI18N
            outDir.mkdirs();
            File outFile = new File(outDir, fo.getNameExt() + ".xml"); // NOI18N
            if (outFile.exists()) {
                outFile.delete();
            }
            outFile.createNewFile();
            Writer out = Files.newBufferedWriter(outFile.toPath(), Charset.forName("UTF-8")); //NOI18N 
            APTTraceUtils.xmlSerialize(apt, out);
            out.flush();
            APT light = APTBuilder.buildAPTLight(apt);
            File outFileLW = new File(outDir, fo.getNameExt() + "_lw.xml"); // NOI18N
            if (outFileLW.exists()) {
                outFileLW.delete();
            }
            outFileLW.createNewFile();
            Writer outLW =  Files.newBufferedWriter(outFileLW.toPath(), Charset.forName("UTF-8")); //NOI18N
            APTTraceUtils.xmlSerialize(light, outLW);
            outLW.flush();
        }
        return apt;
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("DM_GC")
    private long usedMemory() {
        System.gc();
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    /*package*/
    void test(String[] args, PrintStream out, PrintStream err) throws Exception {
        tracer.setPrintStream(out);
        processArguments(args);
        doTest();
        initDataObjects();
    }

    private TestResult test(NativeFileItem item)
            throws FileNotFoundException, RecognitionException, TokenStreamException, IOException, ClassNotFoundException {

        TestResult result = new TestResult();

        if (testAPT) {
            testAPT(item);
            if (breakAfterAPT) {
                return new TestResult();
            }
        }

        AST ast = null;

        if (dumpStatistics) {
            Diagnostic.initFileStatistics(item.getAbsolutePath());
        }

        long time = System.currentTimeMillis();

        AST tree = null;
        int errCount = 0;

        FileImpl fileImpl = (FileImpl) getProject().testAPTParseFile(item);
        waitProjectParsed(getProject(), false);
        if (dumpAst || showAstWindow) {
            tree = fileImpl.debugParse();
        }
        errCount = CsmCorePackageAccessor.get().getErrorCount(fileImpl);
        if (dumpPPState) {
            int antiLoop = 0;
            while (antiLoop++ < 100 && !states.containsKey(fileImpl)) {
                sleep(100); // so that we don't run ahead of fileParsingFinished event
            }
            PreprocHandler preprocHandler = states.get(fileImpl);
            assert preprocHandler != null : "no handler was kept for " + fileImpl;
            dumpMacroMap(preprocHandler.getMacroMap());
        }
        time = System.currentTimeMillis() - time;
        if (isShowTime()) {
            result.setTime(time);
            result.setLineCount(countLines(fileImpl));
            if (!quiet) {
                print("Processing " + item.getName() + " took " + time + " ms; LPS=" + result.getLPS() + "; error count: " + errCount); // NOI18N
            }
        }

        if (dumpStatistics) {
            if (this.dumpDir != null) {
                String postfix = statPostfix;
                if (Diagnostic.getStatisticsLevel() > 1) {
                    postfix += "." + Diagnostic.getStatisticsLevel(); // NOI18N
                }
                String name = item.getName() + postfix;
                String theDumpFile = new File(this.dumpDir, name).getAbsolutePath();
                Diagnostic.dumpFileStatistics(theDumpFile);
            }
            if (this.dumpFile != null) {
                Diagnostic.dumpFileStatistics(this.dumpFile, true);
            }
        }

        if (dumpAst) {
            System.out.println("AST DUMP for file " + item.getName()); // NOI18N
            dumpAst(tree);
        }

        if (doCleanRepository) {
            CsmProject prj = fileImpl.getProject();
            CharSequence absPath = fileImpl.getAbsolutePath();
            fileImpl = null;
            ParserThreadManager.instance().waitEmptyProjectQueue((ProjectBase) prj);
            waitProjectParsed(getProject(), false);
            RepositoryTestUtils.deleteDefaultCacheLocation();
            fileImpl = (FileImpl) prj.findFile(absPath, true, false);
        }

        if (dumpModel) {
            if (fileImpl != null) {
                CsmCacheManager.enter();
                try {
                    tracer.setDeep(deep);
                    tracer.setDumpTemplateParameters(dumpTemplateParameters);
                    tracer.setTestUniqueName(testUniqueName);
                    tracer.dumpModel(fileImpl);
                    if (!dumpFileOnly) {
                        tracer.dumpModel(getProject());
                    }
                } finally {
                    CsmCacheManager.leave();
                }
            } else {
                print("FileImpl is null - not possible to dump File Model"); // NOI18N
            }
        }

        if (showAstWindow) {
            test(tree, item.getName());
        }

        return result;
    }

    private boolean hasNonEmptyIncludes(CsmFile fileImpl) {
        for (Iterator<CsmInclude> it = fileImpl.getIncludes().iterator(); it.hasNext();) {
            CsmInclude inc = it.next();
            if (inc.getIncludeFile() != null) {
                return true;
            }
        }
        return false;
    }

    private long countLines(CsmFile fileImpl) {
        return countLines(fileImpl, false);
    }

    private long countLines(CsmFile fileImpl, boolean allowResolvedIncludes) {
        if (fileImpl == null) {
            return -1;
        }
        if (!allowResolvedIncludes && hasNonEmptyIncludes(fileImpl)) {
            //! fileImpl.getIncludes().isEmpty() ) {
            return -1;
        }
        CharSequence text = fileImpl.getText();
        long cnt = 0;
        for (int pos = 0; pos < text.length(); pos++) {
            if (text.charAt(pos) == '\n') {
                cnt++;
            }
        }
        return cnt;
    }

    private void test(AST tree, String label) {

        //	    System.out.println("LIST:");
        //	    System.out.println(tree.toStringList());
        //
        //	    System.out.println("DUMP:");
        //	    DumpASTVisitor visitor = new DumpASTVisitor();
        //	    visitor.visit(tree);
        ASTFrameEx frame = new ASTFrameEx(label, tree);
        frame.setVisible(true);
    }

    public static void getFileErrors(CsmFile file, ErrorListener errorListener) {
        CsmCorePackageAccessor.get().testFileImplErrors((FileImpl)file, errorListener);
    }

//    private boolean isDummyUnresolved(CsmDeclaration decl) {
//	return decl == null || decl instanceof Unresolved.UnresolvedClass;
//    }
    public static void dumpAst(AST ast) {
        ASTVisitor visitor = new ASTVisitor() {

            @Override
            public void visit(AST node) {
                for (AST node2 = node; node2 != null; node2 = node2.getNextSibling()) {
                    String ofStr = (node2 instanceof CsmAST) ? (" offset=" + ((CsmAST) node2).getOffset() + " file = " + ((CsmAST) node2).getFilename()) : ""; // NOI18N
                    print("" + node2.getText() + " [" + node2.getType() + "] " + node2.getLine() + ':' + node2.getColumn() + ofStr); // NOI18N
                    if (node2.getFirstChild() != null) {
                        indent();
                        visit(node2.getFirstChild());
                        unindent();
                    }
                }
            }
        };
        visitor.visit(ast);
    }

    private void dumpMacroMap(PPMacroMap macroMap) {
        tracer.print("State of macro map:"); // NOI18N
        tracer.print(macroMap == null ? "empty macro map" : macroMap.toString()); // NOI18N
    }

    private void testLibProject() {
        LibProjectImpl libProject = LibProjectImpl.createInstance(getModel(), CndFileUtils.getLocalFileSystem(), "/usr/include", -1); // NOI18N
        getModel().testAddProject(libProject);
        CsmCacheManager.enter();
        try {
            tracer.dumpModel(libProject);
        } finally {
            CsmCacheManager.leave();
        }
    }

    private static void print(String s) {
        tracer.print(s);
    }

    private void print(String s, boolean newLine) {
        tracer.print(s, newLine);
    }

    private static void indent() {
        tracer.indent();
    }

    private static void unindent() {
        tracer.unindent();
    }

    private int countUserFiles() {
        return getProject().getAllFiles().size();
    }

    private int countSystemHeaders() {
        int cnt = 0;
        Set processedProjects = new HashSet();
        for (Iterator it = getProject().getLibraries().iterator(); it.hasNext();) {
            cnt += countFiles((ProjectBase) it.next(), processedProjects);
        }
        return cnt;
    }

    private int countFiles(ProjectBase prj, Collection processedProjects) {
        if (processedProjects.contains(prj)) {
            return 0; // already counted
        }
        int cnt = prj.getAllFiles().size();
        for (Iterator it = prj.getLibraries().iterator(); it.hasNext();) {
            cnt += countFiles((ProjectBase) it.next(), processedProjects);
        }
        return cnt;
    }

    private void calculateAverageLPS(TestResult total, boolean includeLibs) {
        total.lineCount = 0;
        for (Iterator it = getProject().getAllFiles().iterator(); it.hasNext();) {
            CsmFile file = (CsmFile) it.next();
            total.lineCount += countLines(file, true);
        }
        if (includeLibs) {
            for (Iterator it1 = getProject().getLibraries().iterator(); it1.hasNext();) {
                ProjectBase lib = (ProjectBase) it1.next();
                for (Iterator it2 = lib.getAllFiles().iterator(); it2.hasNext();) {
                    CsmFile file = (CsmFile) it2.next();
                    total.lineCount += countLines(file, true);
                }
            }
        }
    }

//    private void testFolding(File file) {
//        InputStream is;
//        try {
//            is = new FileInputStream(file);
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//            return;
//        }
//        if (is == null) {
//            return;
//        }
//        Reader reader = new InputStreamReader(is);
//        reader = new BufferedReader(reader);
//        FoldingParser p = Lookup.getDefault().lookup(FoldingParser.class);
//        if (p != null) {
//            List<CppFoldRecord> folds = p.parse(file.getAbsolutePath(), reader);
//            try {
//                reader.close();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            }
//            printFolds(file.getAbsolutePath(), folds);
//        } else {
//            System.out.println("No Folding Provider"); // NOI18N
//        }
//    }
//
//    private void printFolds(String file, List<CppFoldRecord> folds) {
//        Collections.sort(folds, FOLD_COMPARATOR);
//        System.out.println("Foldings of the file " + file); // NOI18N
//        for (Iterator it = folds.iterator(); it.hasNext();) {
//            CppFoldRecord fold = (CppFoldRecord) it.next();
//            System.out.println(fold);
//        }
//    }
//    private static Comparator<CppFoldRecord> FOLD_COMPARATOR = new Comparator<CppFoldRecord>() {
//
//        public int compare(CppFoldRecord o1, CppFoldRecord o2) {
//            int start1 = o1.getStartLine();
//            int start2 = o2.getStartLine();
//            if (start1 == start2) {
//                return o1.getStartOffset() - o2.getStartOffset();
//            } else {
//                return start1 - start2;
//            }
//        }
//    };

    boolean isShowTime() {
        return showTime;
    }

    private List<NativeFileItem> getFileItems() {
        List<NativeFileItem> result = new ArrayList<>();

        Object platformProject = getProject().getPlatformProject();
        if (platformProject instanceof NativeProject) {
            NativeProject nativeProject = (NativeProject) platformProject;
            result.addAll(nativeProject.getAllFiles());
// these are all files specified in command line; it does not make sense to filter them
//            for(NativeFileItem item : nativeProject.getAllFiles()){
//                if (!item.isExcluded()) {
//                    switch(item.getLanguage()){
//                        case C:
//                        case CPP:
//                        case C_HEADER:
//                            result.add(item);
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            }

        }
        return result;
    }

    private static String getFileLanguage(FileObject fo) {
        String lang = APTLanguageSupport.GNU_CPP;
        String ext = fo.getExt();
        if (ext.equals("c")) { // NOI18N
            lang = APTLanguageSupport.GNU_C;
        }
        if (ext.equals("f")) { // NOI18N
            lang = APTLanguageSupport.FORTRAN;
        }
        return lang;
    }
}

