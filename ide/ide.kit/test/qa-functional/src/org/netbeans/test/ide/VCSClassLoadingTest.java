/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.ide;

/**
 *
 * @author petr.cyhelsky@oracle.com
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestSuite;

public class VCSClassLoadingTest extends JellyTestCase {
    
    private static int stage = 1;
    private static String VERSIONING = "Team";
    private static final String HG = "Mercurial";
    private static final String CVS = "CVS";
    private static final String SVN = "Subversion";
    private static final String GIT = "Git";
    private static final String SEPARATOR = "|";
    private static final String FILE = "vcswhitelist_";
    private static final String TXT = ".txt";
    private static final String TXT_GZ = ".txt.gz";
    private static final String BIGLIST = "biglist";
    private static final String[] LIST = {HG.toLowerCase(),CVS.toLowerCase(),SVN.toLowerCase(),GIT.toLowerCase()};
    private static final String FS = File.separator;
    private static final String PATH = new VCSClassLoadingTest("Dummy").getDataDir()+FS;
    private static final String TMP = System.getProperty("java.io.tmpdir");

    public VCSClassLoadingTest(String testName)
    {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();        
    }

    
    public static NbTestSuite suite() {
        StringTokenizer tok = new StringTokenizer(PATH, FS, true);
        StringBuilder ignore = new StringBuilder();
        String currentToken;
        for (int i = 0; i < tok.countTokens(); i++) {
            currentToken = tok.nextToken();
            if (currentToken.equalsIgnoreCase("build")) {
                break;
            } else {
                ignore.append(currentToken);
            }
        }
        System.setProperty("versioning.unversionedFolders", ignore.toString());
        unzipBigList();
        initBlacklistedClassesHandler();
        NbTestSuite suite = new NbTestSuite();
        suite.addTest(NbModuleSuite.create(NbModuleSuite.createConfiguration(VCSClassLoadingTest.class)
                .addTest("testOpenIDE")                
                .addTest("testCreateNewSampleProject")
                .addTest("testTraverseCVSMenu")
                .addTest("testTraverseSVNMenu")
                .addTest("testTraverseHGMenu")
                .addTest("testTraverseGitMenu")
                .enableModules(".*").clusters(".*")));
        return suite;
    }

    private static boolean initBlacklistedClassesHandler()
    {
        String whitelistFN = PATH+FILE+stage+TXT;
        System.out.println("================  "+whitelistFN);
        BlacklistedClassesHandler bcHandler = BlacklistedClassesHandlerSingleton.getInstance();
        System.out.println("BlacklistedClassesHandler will be initialized with "+whitelistFN);
        bcHandler.resetInitiated();
        if(bcHandler.initSingleton(null, whitelistFN, false)) {            
            bcHandler.register();
            System.out.println("BlacklistedClassesHandler handler added");
            System.setProperty("netbeans.warmup.skip", "true");
            System.out.println("Warmup disabled");
            return true;
        } else
        {
            return false;
        }
    }

    private void testWhitelist() throws Exception {
        BlacklistedClassesHandler bcHandler;
        bcHandler = BlacklistedClassesHandlerSingleton.getBlacklistedClassesHandler();
        assertNotNull("BlacklistedClassesHandler should be available", bcHandler);
        try {
            bcHandler.filterViolators(LIST);
            System.out.println("========= Using "+FILE+stage+TXT+" ===============");
            bcHandler.listViolations(getLog(FILE + stage + TXT), false);            
            bcHandler.listViolations(getLog("report_" + stage + TXT), false, true);
            int allowed = Integer.getInteger("allowed.violations", 0);
            int number = bcHandler.getNumberOfViolations();
            String txt = null;
            if (number > 0) {
                txt = bcHandler.reportViolations(getLog("violations_" + stage + ".xml"));
                bcHandler.writeViolationsSnapshot(new File(getWorkDir(),"violations_" + stage + ".npss"));
            }
            if (number > allowed) {
                fail("Too many violations. Allowed only "+allowed+" but was: "+number+":\n"+txt);
            }
        } finally {
            bcHandler.unregister();
        }
    }
    
    public void testOpenIDE() throws Exception {
        stage = 1;
        Thread.sleep(1000);
        testWhitelist();
    }

    public void testCreateNewSampleProject() throws Exception {
        stage = 2;
        initBlacklistedClassesHandler();
        Thread.sleep(1000);
        NewProjectWizardOperator wizard = NewProjectWizardOperator.invoke();
        wizard.selectCategory("samples"+SEPARATOR+"Java");
        wizard.selectProject("Anagram Game");
        wizard.next();
        NbDialogOperator next = new NbDialogOperator("New Anagram Game");
        new JTextFieldOperator(next,1).enterText(TMP);
        wizard.finish();
        waitScanFinished();
        closeAllModal();
        testWhitelist();
    }
    
    public void testTraverseHGMenu() throws Exception {
        stage = 5;        
        initBlacklistedClassesHandler();
        MainWindowOperator.getDefault().menuBar().pushMenu(VERSIONING+SEPARATOR+HG);
        MainWindowOperator.getDefault().menuBar().closeSubmenus();        
        testWhitelist();
    }
    
    public void testTraverseCVSMenu() throws Exception {
        stage = 3;        
        initBlacklistedClassesHandler();
        MainWindowOperator.getDefault().menuBar().pushMenu(VERSIONING+SEPARATOR+CVS);
        MainWindowOperator.getDefault().menuBar().closeSubmenus();
        testWhitelist();
    }
    
    public void testTraverseSVNMenu() throws Exception {
        stage = 4;        
        initBlacklistedClassesHandler();
        MainWindowOperator.getDefault().menuBar().pushMenu(VERSIONING+SEPARATOR+SVN);
        MainWindowOperator.getDefault().menuBar().closeSubmenus();
        testWhitelist();
    }
    
    public void testTraverseGitMenu() throws Exception {
        stage = 6;        
        initBlacklistedClassesHandler();
        MainWindowOperator.getDefault().menuBar().pushMenu(VERSIONING+SEPARATOR+GIT);
        MainWindowOperator.getDefault().menuBar().closeSubmenus();
        testWhitelist();
    }
    
    @SuppressWarnings("NestedAssignment")
    private static void unzipBigList() {
        try (FileInputStream inputStream = new FileInputStream(PATH+BIGLIST+TXT_GZ);
            GZIPInputStream gzipInput = new GZIPInputStream(inputStream);
            FileOutputStream outputStream = new FileOutputStream(PATH+BIGLIST+TXT)) {
            int ch;
            while ((ch = gzipInput.read()) != -1) {
                outputStream.write(ch);
            }
        }
        catch (Exception err)
        {
             System.out.println("An error occurred - while unzipping "+PATH+BIGLIST+TXT_GZ+" :\n" + err.getMessage());
        }
    }
}
