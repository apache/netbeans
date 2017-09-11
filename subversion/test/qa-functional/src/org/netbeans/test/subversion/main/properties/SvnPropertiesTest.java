/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.test.subversion.main.properties;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.SourcePackagesNode;
import org.netbeans.test.subversion.operators.CheckoutWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.operators.SvnPropertiesOperator;
import org.netbeans.test.subversion.operators.VersioningOperator;
import org.netbeans.test.subversion.operators.WorkDirStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author novakm
 */
public class SvnPropertiesTest extends JellyTestCase {

    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    static Logger log;

    public SvnPropertiesTest(String name) {
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
                 NbModuleSuite.createConfiguration(SvnPropertiesTest.class).addTest(
                    "propTest"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }

    public void propTest() throws Exception {
        try {
            MessageHandler mh = new MessageHandler("Checking out");
            log.addHandler(mh);
            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            //VersioningOperator.invoke();
            TestKit.showStatusLabels();
            CheckoutWizardOperator.invoke();
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

            //open project
            TestKit.waitText(mh);

            NbDialogOperator nbdialog = new NbDialogOperator("Checkout Completed");
            JButtonOperator open = new JButtonOperator(nbdialog, "Open Project");
            open.push();
            TestKit.waitForScanFinishedSimple();

            // set svnProperty for file
            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");

            Thread.sleep(3000);
            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            SvnPropertiesOperator spo = SvnPropertiesOperator.invoke(node);

            TestKit.waitText(mh);

            spo.typePropertyName("fileName");
            spo.typePropertyValue("fileValue");

            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            spo.add();

            TestKit.waitText(mh);

            Thread.sleep(1000);
            assertEquals("1. Wrong row count of table.", 1, spo.propertiesTable().getRowCount());
//            assertFalse("Recursively checkbox should be disabled on file! ", spo.cbRecursively().isEnabled());
            Thread.sleep(1000);
            spo.cancel();
            Thread.sleep(1000);
            //  set svnProperty for folder - one recursive and one nonrecursive
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");

            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            spo = SvnPropertiesOperator.invoke(node);

            TestKit.waitText(mh);

            assertTrue("Recursively checkbox should be enabled on package! ", spo.cbRecursively().isEnabled());
            spo.checkRecursively(false);
            spo.typePropertyName("nonrecursiveName");
            spo.typePropertyValue("nonrecursiveValue");
            spo.add();
            Thread.sleep(1000);
            spo.checkRecursively(true);
            spo.typePropertyName("recursiveName");
            spo.typePropertyValue("recursiveValue");

            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            spo.add();
            spo.refresh();

            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            Thread.sleep(1000);
            assertEquals("2. Wrong row count of table.", 2, spo.propertiesTable().getRowCount());
            spo.cancel();

            //  verify whether the recursive property is present on file
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");

            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            spo = SvnPropertiesOperator.invoke(node);

            TestKit.waitText(mh);

            Thread.sleep(1000);
            assertEquals("3. Wrong row count of table.", 2, spo.propertiesTable().getRowCount());
            assertEquals("Expected file is missing.", "recursiveName", spo.propertiesTable().getModel().getValueAt(1, 0).toString());
            spo.propertiesTable().selectCell(1, 0);

            mh = new MessageHandler("Scanning svn properties");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            spo.remove();
            spo.refresh();

            TestKit.waitText(mh);

            Thread.sleep(5000);
            assertEquals("4. Wrong row count of table.", 1, spo.propertiesTable().getRowCount());
            spo.cancel();
        } catch (Exception e) {
            throw new Exception("Test failed: " + e);
        } finally {
            TestKit.closeProject(PROJECT_NAME);
        }
    }
}
