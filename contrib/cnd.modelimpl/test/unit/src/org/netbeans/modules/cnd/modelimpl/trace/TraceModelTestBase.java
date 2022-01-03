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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.apt.debug.APTTraceFlags;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.test.ModelImplBaseTestCase;
import org.netbeans.modules.cnd.test.CndCoreTestUtils;
import org.netbeans.modules.cnd.utils.cache.CndFileUtils;

/**
 *
 */
public class TraceModelTestBase extends ModelImplBaseTestCase {

    private TestModelHelper helper;
    private char forcedPathSeparator = '\0';
    protected boolean cleanCache = true;

    public TraceModelTestBase(String testName) {
        super(testName);
    }

    protected boolean isDumpingPPState() {
        return getTraceModel().isDumpingPPState();
    }

    protected TraceModel getTraceModel() {
        assert helper != null;
        return helper.getTraceModel();
    }

    protected TestModelHelper getTestModelHelper(){
        return helper;
    }

    protected void parsingTime(TraceModel.TestResult time) {
    }
    
    protected void performTestWithForcedPathSeparator(String source, char separator) throws Exception {
        char oldValue = this.forcedPathSeparator;
        this.forcedPathSeparator = separator;
        performTest(source);
        this.forcedPathSeparator = oldValue;
    }

    protected void performTest(String source) throws Exception {
        String goldenDataFileName = null;
        String goldenErrFileName = null;
        if (APTTraceFlags.USE_CLANK) {
            if (getGoldenFile(source + ".clank.dat").exists()) { // NOI18N
                goldenDataFileName = source + ".clank.dat"; // NOI18N
            }
            if (getGoldenFile(source + ".clank.err").exists()) { // NOI18N
                goldenErrFileName = source + ".clank.err"; // NOI18N
            }
        }
        if (goldenDataFileName == null) {
            goldenDataFileName = source + ".dat"; // NOI18N
        }
        if (goldenErrFileName == null) {
            goldenErrFileName = source + ".err"; // NOI18N
        }
        performTest(source, goldenDataFileName, goldenErrFileName); // NOI18N
    }

    protected final ProjectBase getProject() {
        return helper.getProject();
    }

    protected final CsmProject getCsmProject() {
        return helper.getProject();
    }

    protected final void resetProject() {
        helper.resetProject();
    }

    protected final CsmModel getModel() {
        return helper.getModel();
    }

    protected void preSetUp() throws Exception {
        // init flags needed for file model tests before creating TraceModel
    }

    protected void postSetUp() throws Exception {
        // init flags needed for file model tests
    }

    protected final void initParsedProject() throws Exception {
        File projectDir = getTestCaseDataDir();
        helper.initParsedProject(projectDir.getAbsolutePath());
    }

    protected final FileImpl getFileImpl(File file) {
        return helper.getProject().getFile(CndFileUtils.normalizeFile(file).getAbsolutePath(), true);
    }

    protected final void reparseFile(CsmFile file) {
        if (file instanceof FileImpl) {
            ((FileImpl) file).markReparseNeeded(true);
            try {
                file.scheduleParsing(true);
            } catch (InterruptedException ex) {
                ex.printStackTrace(System.err);
            }
        }
    }
    
    protected final FileImpl findFile(String name) throws Exception{
        ProjectBase project = this.getProject();
        if (project != null) {
            String toCompare = File.separator + name;
            for (FileImpl file : project.getAllFileImpls()) {
                if (file.getAbsolutePath().toString().endsWith(toCompare)) {
                    return file;
                }
            }
        }
        assertTrue("CsmFile not found for " + name, false);
        return null;
    }
    
    @Override
    protected void setUp() throws Exception {
        preSetUp();
        super.setUp();
        super.clearWorkDir();
        helper = new TestModelHelper(cleanCache, getProjectFileFilter());
        helper.addParsingTimeResultListener(new TraceModel.ParsingTimeResultListener() {
            @Override
            public void notifyParsingTime(TraceModel.TestResult parsingTime) {
                parsingTime(parsingTime);
            }
        });
        assertNotNull("Model must be valid", getTraceModel().getModel()); // NOI18N
        postSetUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        helper.shutdown(true);
    }

    protected TraceModelFileFilter getProjectFileFilter() {
        return null;
    }

    protected final void performModelTest(File testFile, PrintStream streamOut, PrintStream streamErr) throws Exception {
        performModelTest(new String[]{testFile.getAbsolutePath()}, streamOut, streamErr);
    }

    protected final void performModelTest(String[] args, PrintStream streamOut, PrintStream streamErr) throws Exception {
        getTraceModel().test(args, streamOut, streamErr);
    }

    protected void doTest(String[] args, PrintStream streamOut, PrintStream streamErr, Object... params) throws Exception {
        PrintStream oldOut = System.out;
        PrintStream oldErr = System.err;
        try {
            // redirect output and err
            System.setOut(streamOut);
            System.setErr(streamErr);
            performModelTest(args, streamOut, streamErr);
            postTest(args, params);
        } finally {
            // restore err and out
            System.setOut(oldOut);
            System.setErr(oldErr);
        }
    }

    /*
     * Used to filter out messages that may differ on different machines
     */
    protected static class FilteredPrintStream extends PrintStream {
        public FilteredPrintStream(File file) throws FileNotFoundException {
            super(file);
        }

        public FilteredPrintStream(OutputStream stream) {
            super(stream);
        }

        @Override
        public void println(String s) {
            if (s==null ||
                    !s.startsWith("Java Accessibility Bridge for GNOME loaded.") ||
                    !s.startsWith("WARNING: FileUtil.normalizeFile") ||
                    !s.contains("org.openide.filesystems.FileUtil normalizeFile")
                    ) {
                super.println(s);
            }
        }
    }
    
    protected void postTest(String[] args, Object... params) throws Exception {
        
    }

    protected void performPreprocessorTest(String source) throws Exception {
        performPreprocessorTest(source, source + ".dat", source + ".err");
    }

    protected void performPreprocessorTest(String source, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        String flags = "-oG"; // NOI18N
        File testFile = getDataFile(source);
        performTest(new String[]{flags, testFile.getAbsolutePath()}, goldenDataFileName, goldenErrFileName, params);
    }

    protected void performTest(String[] source, String goldenNameBase, Object... params) throws Exception {
        String[] absFiles = new String[source.length];
        for (int i = 0; i < source.length; i++) {
            absFiles[i] = getDataFile(source[i]).getAbsolutePath();            
        }
        String goldenDataFileName = null;
        String goldenErrFileName = null;
        if (APTTraceFlags.USE_CLANK) {
            if (getGoldenFile(goldenNameBase + ".clank.dat").exists()) { // NOI18N
                goldenDataFileName = goldenNameBase + ".clank.dat"; // NOI18N
            }
            if (getGoldenFile(goldenNameBase + ".clank.err").exists()) { // NOI18N
                goldenErrFileName = goldenNameBase + ".clank.err"; // NOI18N
            }
        }
        if (goldenDataFileName == null) {
            goldenDataFileName = goldenNameBase + ".dat"; // NOI18N
        }
        if (goldenErrFileName == null) {
            goldenErrFileName = goldenNameBase + ".err"; // NOI18N
        }
        performTest(absFiles, goldenDataFileName, goldenErrFileName, params);
    }

    protected void performTest(String source, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        CsmCacheManager.enter();
        try {
            File testFile = getDataFile(source);
            assertTrue("no test file " + testFile.getAbsolutePath(), testFile.exists());
            performTest(new String[]{testFile.getAbsolutePath()}, goldenDataFileName, goldenErrFileName, params);
        } finally {
            CsmCacheManager.leave();
        }
    }

    protected void performTest(String[] args, String goldenDataFileName, String goldenErrFileName, Object... params) throws Exception {
        File workDir = getWorkDir();

        File output = new File(workDir, goldenDataFileName);
        PrintStream streamOut = new PrintStream(new BufferedOutputStream(new FileOutputStream(output))) {

            @Override
            public void print(String s) {
                // fake override to easy debug model tests
                super.print(s);
            }

            @Override
            public void println(String s) {
                super.println(s);
            }
        };
        File error = goldenErrFileName == null ? null : new File(workDir, goldenErrFileName);
        PrintStream streamErr = goldenErrFileName == null ? System.err : new FilteredPrintStream(new BufferedOutputStream(new FileOutputStream(error))) {

            @Override
            public void print(String s) {
                // fake override to easy debug model tests
                super.print(s);
            }

            @Override
            public void println(String s) {
                super.println(s);
            }

            @Override
            public PrintStream printf(String format, Object... args) {
                return super.printf(format, args); //To change body of generated methods, choose Tools | Templates.
            }
        };
        try {
            doTest(args, streamOut, streamErr, params);
        } finally {
            // restore err and out
            streamOut.close();
            if (streamErr != null) {
                streamErr.close();
            }
        }
        //System.out.println("finished testing " + testFile);
        boolean errTheSame = true;
        File goldenErrFile = null;
        File goldenErrFileCopy = null;
        File diffErrorFile = null;
        // first of all check err, because if not failed (often) => dat diff will be created
        if (goldenErrFileName != null && !Boolean.getBoolean("cnd.skip.err.check")) {
            goldenErrFile = getGoldenFile(goldenErrFileName);
            if (goldenErrFile.exists()) {
                goldenErrFileCopy = copyGoldenErrFile(workDir, goldenErrFileName, goldenErrFile);
                if (diffErrorFiles(error, goldenErrFileCopy, null)) {
                    errTheSame = false;
                    diffErrorFile = new File(workDir, goldenErrFileName + ".diff");
                    diffErrorFiles(error, goldenErrFileCopy, diffErrorFile);
                }
            } else {
                // golden err.file doesn't exist => err.file should be empty
                errTheSame = (error.length() == 0);
            }
        }

        boolean outTheSame = true;
        File goldenDataFile = getGoldenFile(goldenDataFileName);
        File goldenDataFileCopy = null;
        File diffOutputFile = null;
        if (diffGoldenFiles(isDumpingPPState(), output, goldenDataFile, null)) {
            outTheSame = false;
            // copy golden
            goldenDataFileCopy = new File(workDir, goldenDataFileName + ".golden");
            CndCoreTestUtils.copyToWorkDir(goldenDataFile, goldenDataFileCopy); // NOI18N
            diffOutputFile = new File(workDir, goldenDataFileName + ".diff");
            diffGoldenFiles(isDumpingPPState(), output, goldenDataFile, diffOutputFile);
        }
        if (outTheSame) {
            if (!errTheSame) {
                if (goldenErrFile.exists()) {
                    StringBuilder buf = new StringBuilder("ERR Difference - check: diff " + error + " " + goldenErrFileCopy);
                    showDiff(diffErrorFile, buf);
                    assertTrue(buf.toString(), false); // NOI18N
                } else {
                    assertTrue("ERR Difference - error should be emty: " + error, false); // NOI18N
                }
            }
        } else if (errTheSame) {
            StringBuilder buf = new StringBuilder("OUTPUT Difference - check: diff " + output + " " + goldenDataFileCopy);
            showDiff(diffOutputFile, buf);
            assertTrue(buf.toString(), outTheSame); // NOI18N
        } else {
            StringBuilder buf = new StringBuilder("ERR and OUTPUT are different, see content of folder " + workDir);
            showDiff(diffErrorFile, buf);
            showDiff(diffOutputFile, buf);
            assertTrue(buf.toString(), false); // NOI18N
        }
        assertNoExceptions();
    }

    private File copyGoldenErrFile(File workDir, String goldenErrFileName, File goldenErrFile) throws IOException {
        // copy golden
        String golden = "goldenfiles";
        String macro = "${origin}";
        String origin = goldenErrFile.getAbsolutePath();
        int i = origin.lastIndexOf(golden);
        if (i < 1) { // this happens, for example, when running ReopenBrokenRepositoryValidationTest
            return goldenErrFile;
        }
        char separator = origin.charAt(i-1);
        if (forcedPathSeparator != '\0' && separator != forcedPathSeparator) {
            origin = origin.replace(separator, forcedPathSeparator);
            separator = forcedPathSeparator;
        }
        origin = origin.substring(0,i-1)+origin.substring(i+golden.length());
        i = origin.lastIndexOf(separator);
        origin = origin.substring(0, i);
        
        File goldenErrFileCopy = new File(workDir, goldenErrFileName + ".golden");
        final Charset charset = Charset.forName("UTF-8");
        BufferedReader br = Files.newBufferedReader(goldenErrFile.toPath(), charset);
        BufferedWriter wr = Files.newBufferedWriter(goldenErrFileCopy.toPath(), charset);
        for (String line = br.readLine(); line != null; line = br.readLine()) {
            i = line.indexOf(macro);
            if (i >= 0) {
                // fixing tests on Windows
                char currentSeparator = line.charAt(i+macro.length());
                if (separator != currentSeparator) {
                     //line = line.replace(currentSeparator, separator);
                     line = line.replace(macro + currentSeparator, origin + separator);
                }
                line = line.replace(macro, origin);
            }
            wr.write(line);
            wr.write('\n');
        }
        br.close();
        wr.close();
        return goldenErrFileCopy;
    }
}
