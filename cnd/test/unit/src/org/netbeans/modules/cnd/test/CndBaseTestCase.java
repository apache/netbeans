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

package org.netbeans.modules.cnd.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.EditorKit;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.junit.Manager;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.cnd.editor.cplusplus.CCKit;
import org.netbeans.modules.cnd.editor.cplusplus.CKit;
import org.netbeans.modules.cnd.editor.cplusplus.HKit;
import org.netbeans.modules.cnd.editor.fortran.FKit;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranReformatter;
import org.netbeans.modules.cnd.editor.reformat.Reformatter;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.editor.NbEditorKit;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * IMPORTANT NOTE:
 * If This class is not compiled with the notification about not resolved
 * NbTestCase class => NB JUnit module is absent in target platform
 *
 * To solve this problem NB JUnit must be installed
 * For instance from Netbeans Update Center Beta:
 * - start target(!) platform as IDE from command line (/opt/NBDEV/bin/netbeans)
 * - in opened IDE go into Tools->Update Center
 * - select "Netbeans Update Center Beta"
 * -- if absent => configure it using the following url as example
 *    http://www.netbeans.org/updates/beta/55_{$netbeans.autoupdate.version}_{$netbeans.autoupdate.regnum}.xml?{$netbeans.hash.code}
 * - press Next
 * - in Libraries subfolder found NB JUnit module
 * - Add it and install
 * - close target IDE and reload development IDE to update the information of
 *         available modules in target's platform
 *
 * if NBDEV is NB-5.5 based => INSANE module must be installed the same way in target platform
 *
 * On Windows cnd must be in the path without spaces for correct resolving golden and data files by junit harness
 */

/**
 * base class to isolate using of NbJUnit library
 * ${xtest.data} value is usually ${module}/test/unit/data folder
 */
public abstract class CndBaseTestCase extends NativeExecutionBaseTestCase {

    private static final boolean TRACE_START_STOP = Boolean.getBoolean("cnd.test.trace.start.stop");
    private static final int PAUSE_ON_FIRST_RUN = Integer.getInteger("cnd.test.pause.on.first.run", 0);

    private static final AtomicBoolean first = (PAUSE_ON_FIRST_RUN > 0) ? new AtomicBoolean(true) : null;

    private MimePath mimePath1;
    private MimePath mimePath2;
    private MimePath mimePath3;
    private MimePath mimePath4;
    private MimePath mimePath5;
    private Logger logger1;
    private Logger logger2;
    private Logger logger3;
    private Logger logger4;
    private Logger logger5;
    private Logger logger6;
    
    /** Creates a new instance of BaseTestCase */
    public CndBaseTestCase(String testName) {
        super(testName);
    }

    public CndBaseTestCase(String name, ExecutionEnvironment testExecutionEnvironment) {
        super(name, testExecutionEnvironment);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        if (TRACE_START_STOP) {
            System.err.println("End   "+getName()+" at "+Calendar.getInstance().getTime());
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        String name = getName()+" at "+Calendar.getInstance().getTime();
        Thread.currentThread().setName("Test "+name);
        super.setUp();
        if (TRACE_START_STOP) {
            System.err.println("Start " + name);
        }
        if (PAUSE_ON_FIRST_RUN > 0) {
            if( first.compareAndSet(true, false) ) {
                System.out.println("Pausiong for " + PAUSE_ON_FIRST_RUN + " seconds on first run");
                Thread.sleep(PAUSE_ON_FIRST_RUN * 1000);
            }
        }
        
        logger1 = Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils");
        logger1.setLevel(Level.SEVERE);
        logger2 = Logger.getLogger("org.netbeans.modules.masterfs.filebasedfs.utils.FileChangedManager");
        logger2.setLevel(Level.SEVERE);
        logger3 = Logger.getLogger("org.openide.filesystems.FileUtil");
        logger3.setLevel(Level.OFF);
        logger4 = Logger.getLogger("org.netbeans.modules.settings.RecognizeInstanceObjects");
        logger4.setLevel(Level.SEVERE);
        logger5 = Logger.getLogger("org.netbeans.ui.indexing");
        logger5.setLevel(Level.SEVERE);
        logger6 = Logger.getLogger("org.netbeans.modules.masterfs.watcher.Watcher");
        logger6.setLevel(Level.SEVERE);
        System.setProperty("RepositoryUpdate.increasedLogLevel", "SEVERE");
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("SUNW_NO_UPDATE_NOTIFY", "true");
        List<Class<?>> list = new ArrayList<Class<?>>();
        list.add(MockMimeLookup.class);
        list.add(FileEncodingQueryImplementationImpl.class);
        for(Class<?> cls : getServices()){
            list.add(cls);
        }
        setUpMime();
        MockServices.setServices(list.toArray(new Class<?>[list.size()]));

        Lookup lookup = MimeLookup.getLookup(MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE));
        assertNotNull(lookup);
        if (addEditorSupport()) {
            EditorKit kit = lookup.lookup(EditorKit.class);
            assertTrue(kit instanceof  CCKit);
        }

        lookup = MimeLookup.getLookup(MimePath.parse(MIMENames.HEADER_MIME_TYPE));
        assertNotNull(lookup);
        if (addEditorSupport()) {
            EditorKit kit = lookup.lookup(EditorKit.class);
            assertTrue(kit instanceof  HKit);
        }

        lookup = MimeLookup.getLookup(MimePath.parse(MIMENames.C_MIME_TYPE));
        assertNotNull(lookup);
        if (addEditorSupport()) {
            EditorKit kit = lookup.lookup(EditorKit.class);
            assertTrue(kit instanceof  CKit);
        }

        lookup = MimeLookup.getLookup(MimePath.parse(MIMENames.FORTRAN_MIME_TYPE));
        assertNotNull(lookup);
        if (addEditorSupport()) {
            EditorKit kit = lookup.lookup(EditorKit.class);
            assertTrue(kit instanceof  FKit);
        }

        lookup = MimeLookup.getLookup(MimePath.parse(MIMENames.ASM_MIME_TYPE));
        assertNotNull(lookup);
        if (addEditorSupport()) {
            EditorKit kit = lookup.lookup(EditorKit.class);
            //assertTrue(kit instanceof AsmEditorKit);
        }
    }

    protected boolean addEditorSupport() {
        return true;
    }
    
    protected void setUpMime() {
        mimePath1 = MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE);
        mimePath2 = MimePath.parse(MIMENames.HEADER_MIME_TYPE);
        mimePath3 = MimePath.parse(MIMENames.C_MIME_TYPE);
        mimePath4 = MimePath.parse(MIMENames.FORTRAN_MIME_TYPE);
        mimePath5 = MimePath.parse(MIMENames.ASM_MIME_TYPE);
        if (addEditorSupport()) {
            MockMimeLookup.setInstances(mimePath1, new CCKit(), new Reformatter.Factory());
            MockMimeLookup.setInstances(mimePath2, new HKit(), new Reformatter.Factory());
            MockMimeLookup.setInstances(mimePath3, new CKit(), new Reformatter.Factory());
            MockMimeLookup.setInstances(mimePath4, new FKit(), new FortranReformatter.Factory());
            // TODO: add needed dependency in all dependant test cases to use real asm editor kit
            //MockMimeLookup.setInstances(mimePath5, new AsmEditorKit());
            MockMimeLookup.setInstances(mimePath5, new AsmStub());
        } else {
            MockMimeLookup.setInstances(mimePath1);
            MockMimeLookup.setInstances(mimePath2);
            MockMimeLookup.setInstances(mimePath3);
            MockMimeLookup.setInstances(mimePath4);
            MockMimeLookup.setInstances(mimePath5);
        }
        //Main.getModuleSystem();
    }

    private static final class AsmStub extends NbEditorKit {
        private AsmStub(){
        }
    }

    protected final void cleanUserDir()  {
        File userDir = getUserDir();
        if (userDir.exists()) {
            if (!removeDirectoryContent(userDir)) {
                assertTrue("Can not remove the content of " +  userDir.getAbsolutePath(), false);
            }
        }
    }

    protected List<Class<?>> getServices(){
        return Collections.<Class<?>>emptyList();
    }

    /**
     * Get the test method specific data file; 
     * usually it is ${xtest.data}/${classname}/filename
     * @see getTestCaseDataClass
     * @see getTestCaseDataDir
     */
    protected File getDataFile(String filename) {
        return new File(getTestCaseDataDir(), filename);
    }

    /** Get the test method specific golden file as ${xtest.data}/goldenfiles/${classname}/filename
     * @param filename filename to get from golden files directory
     * @return golden file
     * @see getTestCaseGoldenDataClass
     */
    @Override
    public File getGoldenFile(String filename) {
        String fullClassName = getTestCaseGoldenDataClass().getName();
        String goldenFileName = fullClassName.replace('.', File.separatorChar) + File.separator + filename;
        File goldenFile = new File(getDataDir() + "/goldenfiles/" + goldenFileName); // NOI18N
        return goldenFile;
    }

    /**
     * this method is responsible for construction of part
     * ${classname}
     * in path ${xtest.data}/goldenfiles/${classname}/filename
     * @see getGoldenFile
     */
    protected Class<?> getTestCaseGoldenDataClass() {
        return getTestCaseDataClass();
    }

    /**
     * Get the test method specific data dir
     * usually it is ${xtest.data}/${classname}
     * @see getTestCaseDataClass
     */
    protected File getTestCaseDataDir() {
        File dataDir = super.getDataDir();
        String fullClassName = getTestCaseDataClass().getName();
        String filePath = fullClassName.replace('.', File.separatorChar);
        return Manager.normalizeFile(new File(dataDir, filePath));
    }

    /**
     * this method is responsible for construction of part
     * ${classname}
     * in path ${xtest.data}/${classname}
     * @see getGoldenFile
     */    
    protected Class<?> getTestCaseDataClass() {
        return this.getClass();
    }
    
    /** Compares golden file and reference log. If both files are the
     * same, test passes. If files differ, test fails and diff file is
     * created (diff is created only when using native diff, for details
     * see JUnit module documentation)
     * @param testFilename reference log file name
     * @param goldenFilename golden file name
     */
    public void compareReferenceFiles(String testFilename, String goldenFilename) {
        try {
            File goldenFile = getGoldenFile(goldenFilename);
            File testFile = new File(getWorkDir(),testFilename);
            
            if(goldenFile.exists()) {
                if (CndCoreTestUtils.diff(testFile, goldenFile, null)) {
                    // copy golden
                    File goldenDataFileCopy = new File(getWorkDir(), goldenFilename + ".golden"); // NOI18N
                    CndCoreTestUtils.copyToWorkDir(goldenFile, goldenDataFileCopy); 

                    StringBuilder buf = new StringBuilder("Files differ; diff " +testFile.getAbsolutePath()+ " "+ goldenDataFileCopy);
                    File diffErrorFile = new File(testFile.getAbsolutePath() + ".diff");
                    CndCoreTestUtils.diff(testFile, goldenFile, diffErrorFile);
                    showDiff(diffErrorFile, buf);
                    fail(buf.toString());
                }            
            } else {
                if (testFile.length() != 0) {
                    StringBuilder buf = new StringBuilder("Files differ; " +testFile.getAbsolutePath()+ " and no golden file");
                    showDiff(testFile, buf);
                    fail(buf.toString());
                }                
            }
        } catch (IOException ioe) {
            fail("Error comparing files: " + ioe); // NOI18N
        }
    }    

    protected void showDiff(File diffOutputFile, StringBuilder buf) {
        if (diffOutputFile != null && diffOutputFile.exists()) {
            int i = 0;
            try {
                BufferedReader in = Files.newBufferedReader(diffOutputFile.toPath(), Charset.forName("UTF-8"));
                while (true) {
                    String line = in.readLine();
                    if (line == null) {
                        break;
                    }
                    if (i > 50) {
                        break;
                    }
                    if (i == 0) {
                        buf.append("\nBeginning of diff:");
                    }
                    buf.append("\n\t").append(line);
                    i++;
                }
                in.close();
            } catch (IOException ex) {
                //
            }
        }
    }
    
    /** Compares default golden file and default reference log. If both files are the
     * same, test passes. If files differ, test fails and default diff (${methodname}.diff)
     * file is created (diff is created only when using native diff, for details
     * see JUnit module documentation)
     */
    @Override
    public void compareReferenceFiles() {
        compareReferenceFiles(this.getName()+".ref",this.getName()+".ref"); // NOI18N
    }

    public static final class FileEncodingQueryImplementationImpl extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject file) {
            return Charset.forName("UTF-8");
        }
    }
}
