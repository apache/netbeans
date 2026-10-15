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

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.Operator;
import org.netbeans.jemmy.operators.Operator.DefaultStringComparator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.MergeOneRepoOperator;
import org.netbeans.test.subversion.operators.MergeOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class MergeUiTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    Operator.DefaultStringComparator comOperator;
    Operator.DefaultStringComparator oldOperator;
    static Logger log;

    /** Creates a new instance of MergeUiTest */
    public MergeUiTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.out.println("### " + getName() + " ###");
        if (log == null) {
            log = Logger.getLogger(TestKit.LOGGER_NAME);
            log.setLevel(Level.ALL);
            TestKit.removeHandlers(log);
        } else {
            TestKit.removeHandlers(log);
        }
    }
    
    public static Test suite() {
         return NbModuleSuite.create(
                 NbModuleSuite.createConfiguration(MergeUiTest.class).addTest(
                    "testInvokeCloseMerge"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void testInvokeCloseMerge() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Checking out");
            log.addHandler(mh);
            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            comOperator = new Operator.DefaultStringComparator(true, true);
            oldOperator = (DefaultStringComparator) Operator.getDefaultStringComparator();
            Operator.setDefaultStringComparator(comOperator);
            CheckoutWizardOperator.invoke();
            Operator.setDefaultStringComparator(oldOperator);
            RepositoryStepOperator rso = new RepositoryStepOperator();

            //create repository...
            File work = new File(TMP_PATH + File.separator + WORK_PATH + File.separator + "w" + System.currentTimeMillis());
            new File(TMP_PATH).mkdirs();
            work.mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));

            rso.next();
            WorkDirStepOperator wdso = new WorkDirStepOperator();
            wdso.setRepositoryFolder("trunk/" + PROJECT_NAME);
            wdso.setLocalFolder(work.getCanonicalPath());
            wdso.checkCheckoutContentOnly(false);

            wdso.finish();
            TestKit.waitText(mh);

            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            TestKit.waitForScanFinishedSimple();

            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            MergeOperator mo = MergeOperator.invoke(projNode);

            Thread.sleep(3000);
            //0. one repository operator
            mo.cboMergeFrom().selectItem(0);
            MergeOneRepoOperator moro = new MergeOneRepoOperator();
            moro.setRepositoryFolder("branches");
            assertEquals("Setting repository folder problem", "branches", moro.getRepositoryFolder());
//            moro.verify();
            moro.close();
//            RepositoryBrowserOperator rbo = moro.browseRepository();
            //moro.selectRepositoryFolder("ok");
            
            
// //            assertEquals("Wrong folder selection!!!", "tags", moro.getRepositoryFolder());
//            moro.setRepositoryFolder("");
//            //1. two repository operator
//            moro.cboMergeFrom().selectItem(2);
//            MergeTwoRepoOperator mtro = new MergeTwoRepoOperator();
//            //mtro.verify();
//            rbo = mtro.browseRepositoryFolder1();
// //            rbo.selectFolder("trunk");
// //            rbo.selectFolder("branches");
// //            rbo.selectFolder("tags");
//            rbo.ok();
//            assertEquals("Wrong folder selection!!!", "tags", mtro.getRepositoryFolder1());
//            mtro.setRepositoryFolder1("");
//            rbo = mtro.browseRepositoryFolder2();
// //            rbo.selectFolder("tags");
// //            rbo.selectFolder("branches");
// //            rbo.selectFolder("trunk");
//            rbo.ok();
//            assertEquals("Wrong folder selection!!!", "trunk", mtro.getRepositoryFolder2());
//            mtro.setRepositoryFolder2("");
//
//            //2. two repository operator
//            moro.cboMergeFrom().selectItem(1);
//            MergeOriginOperator moo = new MergeOriginOperator();
//            //moo.verify();
//            rbo = moo.browseRepositoryFolder();
// //            rbo.selectFolder("trunk");
// //            rbo.selectFolder("branches");
// //            rbo.selectFolder("tags");
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
