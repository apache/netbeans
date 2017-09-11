/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
import java.util.zip.GZIPOutputStream;
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
    private static final String ZIP = ".zip";
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
    
    private static void unzipBigList() {
        try {
            FileInputStream inputStream = new FileInputStream(PATH+BIGLIST+ZIP);
            GZIPInputStream gzipInput = new GZIPInputStream(inputStream);
            FileOutputStream outputStream = new FileOutputStream(PATH+BIGLIST+TXT);
            int ch;
            while ((ch = gzipInput.read()) != -1) {
                outputStream.write(ch);
            }
            inputStream.close();
            gzipInput.close(); 
            outputStream.close();
        }
        catch (Exception err)
        {
             System.out.println("An error occurred - while unzipping "+PATH+BIGLIST+ZIP+" :\n" + err.getMessage());
        }
    }
    
    private static void zipBigList(String pathToBigList) {
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(PATH+BIGLIST+ZIP); 
            GZIPOutputStream zipOutputStream = new GZIPOutputStream(fileOutputStream);
            FileInputStream fileInputStream = new FileInputStream(PATH+BIGLIST+TXT);
            int ch;
            while ((ch = fileInputStream.read()) != -1) {
                zipOutputStream.write(ch);
            }
            fileInputStream.close();
            zipOutputStream.close();
        }
        catch (Exception err) {
            System.out.println("An error occurred - while zipping "+PATH+BIGLIST+TXT+" :\n" + err.getMessage());
        }
    }
}
