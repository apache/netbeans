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

package org.netbeans.modules.cnd.debugger.common2.debugger.test;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.netbeans.junit.Manager;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;

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
 * - in Libraries subfoler found NB JUnit module
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
 * ${xtest.data} vallue is usually ${module}/test/unit/data folder
 */
public abstract class CndBaseTestCase extends NbTestCase {

    private Logger logger5;
    private Logger logger4;
    static {
        // Setting netbeans.dirs makes installedFileLocator work properly
        File[] clusters = findClusters();
        StringBuilder sb = new StringBuilder();
        for (File cluster : clusters) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(cluster.getPath());
        }
        System.setProperty("netbeans.dirs", sb.toString());
    }

    // it's like what org.netbeans.junit.NbModuleSuite does,
    // but reusing NbModuleSuite will cause too massive changes in existing CND tests
    private static File[] findClusters() {
        File netbeans = findNetbeans();
        assert netbeans != null;
        File[] clusters = netbeans.listFiles(new FileFilter() {
            @Override
            public boolean accept(File dir) {
                if (dir.isDirectory()) {
                    File m = new File(new File(dir, "config"), "Modules");
                    return m.exists();
                }
                return false;
            }
        });
        return clusters;
    }

    // it's like what org.netbeans.junit.NbModuleSuite does,
    // but reusing NbModuleSuite will cause too massive changes in existing CND tests
    private static File findNetbeans() {
        try {
            Class<?> lookup = Class.forName("org.openide.util.Lookup"); // NOI18N
            File util = new File(lookup.getProtectionDomain().getCodeSource().getLocation().toURI());
            Assert.assertTrue("Util exists: " + util, util.exists());
            return util.getParentFile().getParentFile().getParentFile();
        } catch (Exception ex) {
            try {
                File nbjunit = new File(NbModuleSuite.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                File harness = nbjunit.getParentFile().getParentFile();
                Assert.assertEquals("NbJUnit is in harness", "harness", harness.getName());
                TreeSet<File> sorted = new TreeSet<File>();
                File[] listFiles = harness.getParentFile().listFiles();
                if (listFiles != null) {
                    for (File p : listFiles) {
                        if (p.getName().startsWith("platform")) {
                            sorted.add(p);
                        }
                    }
                }
                Assert.assertFalse("Platform shall be found in " + harness.getParent(), sorted.isEmpty());
                return sorted.last();
            } catch (Exception ex2) {
                Assert.fail("Cannot find utilities JAR: " + ex + " and: " + ex2);
            }
            return null;
        }
    }
    
    /** Creates a new instance of BaseTestCase */
    public CndBaseTestCase(String testName) {
        super(testName);
        setupUserDir();
    }

    private void setupUserDir() {
        File dataDir = getDataDir();
        File dataDirParent = dataDir.getParentFile();
        File userDir = new File(dataDirParent, "userdir");
        userDir.mkdirs();
        System.setProperty("netbeans.user", userDir.getAbsolutePath());
    }

    @Override
    protected int timeOut() {
        return 500000;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        Logger.getLogger("org.netbeans.modules.editor.settings.storage.Utils").setLevel(Level.SEVERE);
        System.setProperty("cnd.mode.unittest", "true");
        System.setProperty("SUNW_NO_UPDATE_NOTIFY", "true");
        logger4 = Logger.getLogger("org.netbeans.modules.settings.RecognizeInstanceObjects");
        logger4.setLevel(Level.SEVERE);
        logger5 = Logger.getLogger("org.netbeans.ui.indexing");
        logger5.setLevel(Level.SEVERE);
        System.setProperty("RepositoryUpdate.increasedLogLevel", "SEVERE");
        List<Class> list = new ArrayList<Class>();
//        list.add(MockMimeLookup.class);
        for(Class cls : getServises()){
            list.add(cls);
        }
        MockServices.setServices(list.toArray(new Class[list.size()]));
//        setUpMime();
    }

//    protected void setUpMime() {
//        MimePath mimePath = MimePath.parse(MIMENames.CPLUSPLUS_MIME_TYPE);
//        MockMimeLookup.setInstances(mimePath, new CCKit());
//        mimePath = MimePath.parse(MIMENames.HEADER_MIME_TYPE);
//        MockMimeLookup.setInstances(mimePath, new HKit());
//        mimePath = MimePath.parse(MIMENames.C_MIME_TYPE);
//        MockMimeLookup.setInstances(mimePath, new CKit());
//        mimePath = MimePath.parse(MIMENames.FORTRAN_MIME_TYPE);
//        MockMimeLookup.setInstances(mimePath, new FKit());
//    }

    protected List<Class> getServises(){
        return Collections.<Class>emptyList();
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
    protected Class getTestCaseGoldenDataClass() {
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
            
            if (CndCoreTestUtils.diff(testFile, goldenFile, null)) {
                // copy golden
                File goldenDataFileCopy = new File(getWorkDir(), goldenFilename + ".golden"); // NOI18N
                CndCoreTestUtils.copyToWorkDir(goldenFile, goldenDataFileCopy); 
                fail("Files differ; diff " +testFile.getAbsolutePath()+ " "+ goldenDataFileCopy); // NOI18N
            }             
        } catch (IOException ioe) {
            fail("Error comparing files: " + ioe); // NOI18N
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
}
