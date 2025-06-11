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

/*
 * CheckoutUITest.java
 *
 * Created on 19 April 2006, 13:34
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package org.netbeans.test.subversion.main.checkout;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CheckoutUITest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;

    /** Creates a new instance of CheckoutUITest */
    public CheckoutUITest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");        
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(CheckoutUITest.class).addTest(
                   "testInvokeClose",
                   "testChangeAccessTypes",
                   "testIncorrentUrl",
                   "testAvailableFields",
                   "testRepositoryFolder"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeClose() throws Exception {
        TestKit.showStatusLabels();
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        new EventTool().waitNoEvent(2000);
        co.btCancel().pushNoBlock();
    }
    
    public void testChangeAccessTypes() throws Exception {
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().waitNoEvent(2000);
        TestKit.showStatusLabels();
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        new EventTool().waitNoEvent(2000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_SVN);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_SVN);
        new EventTool().waitNoEvent(2000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_SVNSSH);
        new EventTool().waitNoEvent(2000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_HTTP);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_HTTP);
        new EventTool().waitNoEvent(2000);
        //
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso.txtUser().setText(RepositoryStepOperator.ITEM_HTTPS);
        rso.txtPassword().setText(RepositoryStepOperator.ITEM_HTTPS);
        new EventTool().waitNoEvent(2000);
        co.btCancel().pushNoBlock();
    }
    
    public void testIncorrentUrl() throws Exception {
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().waitNoEvent(2000);
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        new EventTool().waitNoEvent(2000);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        new EventTool().waitNoEvent(2000);
        //wrong file
        rso.setRepositoryURL("dfile:///");
        new EventTool().waitNoEvent(2000);
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;dfile:///\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        //wrong svn
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL("dsvn://");
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;dsvn://\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        //space in file
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL("file :///");
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;file&#160;:///\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        //space in svn
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL("svn ://");
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;svn&#160;://\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        //space in http
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL("http ://");
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;http&#160;://\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        //space in https
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL("https ://");
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;https&#160;://\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        //space in svn+ssh
        new EventTool().waitNoEvent(2000);
        rso.setRepositoryURL("svn+ssh ://");
        assertEquals("This should be wrong url string!!!", "<html>\n  <head>\n    \n  </head>\n  <body>\n    Invalid&#160;svn&#160;url:&#160;svn+ssh&#160;://\n  </body>\n</html>\n", rso.txtPaneWarning().getText());
        new EventTool().waitNoEvent(2000);
        co.btCancel().pushNoBlock();
    }
    
    public void testAvailableFields() throws Exception {
        long timeoutCO = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 3000);
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeoutCO);
        }

        long timeoutDW = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitComponentTimeout", 3000);
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitComponentTimeout", timeoutDW);
        }

        long timeoutRSO = JemmyProperties.getCurrentTimeout("RepositoryStepOperator.WaitComponentTimeout");
        try {
            JemmyProperties.setCurrentTimeout("RepositoryStepOperator.WaitComponentTimeout", 3000);
        } catch (Exception e) {
            JemmyProperties.setCurrentTimeout("RepositoryStepOperator.WaitComponentTimeout", timeoutRSO);
        }

        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        //file
        rso.selectRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        TimeoutExpiredException tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //http
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTP);
        rso.lblUser();
        rso.lblPassword();
        rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //https
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_HTTPS);
        rso.lblUser();
        rso.lblPassword();
        rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVN);
        rso.lblUser();
        rso.lblPassword();
        rso.btProxyConfiguration();
        
        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        
        //svn+ssh
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_SVNSSH);
        //rso.lblUseExternal();
        //rso.lblTunnelCommand();
        //JTextFieldOperator txt = rso.txtTunnelCommand();
        //txt.typeText("plink");
        new EventTool().waitNoEvent(2000);

        //file
        rso = new RepositoryStepOperator();
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE);
        tee = null;
        try {
            rso.lblUser();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("User name should not be accessible for file:///!!!" ,tee);
        
        tee = null;
        try {
            rso.lblPassword();
        } catch (Exception e) {
            tee = (TimeoutExpiredException) e;
        }
        assertNotNull("Password should not be accessible for file:///!!!" ,tee);
        JemmyProperties.setCurrentTimeout("DialogWaiter.WaitComponentTimeout", timeoutDW);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeoutCO);
        JemmyProperties.setCurrentTimeout("RepositoryStepOperator.WaitDialogTimeout", timeoutRSO);
        co.btCancel().pushNoBlock();
    }
    
    public void testRepositoryFolder() throws Exception {
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().waitNoEvent(2000);
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        
        //create repository...
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        //next step
        rso.next();
        new EventTool().waitNoEvent(2000);
        
        WorkDirStepOperator wdso = new WorkDirStepOperator();
        wdso.verify();
        RepositoryBrowserOperator rbo = wdso.browseRepository();
        rbo.verify();
        //Try to select folders
        new EventTool().waitNoEvent(2000);
        rbo.table().selectCell(2, 2);
        new EventTool().waitNoEvent(2000);
//        rbo.selectFolder("branches");
//        rbo.selectFolder("tags");
//        rbo.selectFolder("trunk");
//        rbo.selectFolder("trunk|JavaApp|src|javaapp");
        rbo.ok();
        
        assertEquals("Wrong folder selection!!!", "tags", wdso.getRepositoryFolder());
        rbo = wdso.browseRepository();
        new EventTool().waitNoEvent(2000);
        rbo.table().selectCell(1, 2);
        new EventTool().waitNoEvent(2000);
//        rbo.selectFolder("trunk|JavaApp");
        rbo.ok();
        assertEquals("Wrong folder selection!!!", "branches", wdso.getRepositoryFolder());
        //wdso.setLocalFolder("/tmp");
        //JFileChooserOperator jfc = wdso.browseLocalFolder();
        //assertEquals("Directory set in wizard not propagated to file chooser:", true, jfc.getCurrentDirectory().getAbsolutePath().endsWith("tmp"));
        //jfc.cancel();
        //wdso.setRepositoryRevision("10");
        //wdso.checkCheckoutContentOnly(true);
        co.btCancel().pushNoBlock();
    }
    
    public void testStopProcess() throws Exception {
        TestKit.closeProject(PROJECT_NAME);
        new EventTool().waitNoEvent(2000);
        comOperator = new Operator.DefaultStringComparator(true, true);
        oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
        Operator.setDefaultStringComparator(comOperator);
        CheckoutWizardOperator co = CheckoutWizardOperator.invoke();
        Operator.setDefaultStringComparator(oldOperator);
        RepositoryStepOperator rso = new RepositoryStepOperator();
        
        //create repository...
        new File(TMP_PATH).mkdirs();
        RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
        RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
        RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
        rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
        
        //next step
        rso.next();
        rso.btStop().push();
        assertEquals("Warning message - process was cancelled by user", "Action canceled by user", rso.txtPaneWarning().getText());
        co.btCancel().pushNoBlock();
    }
}
