/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */

package org.netbeans.test.mercurial.main.commit;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.operators.CommitOperator;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author peter
 */
public class CommitDataTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    static Logger log;
    
    /** Creates a new instance of CommitDataTest */
    public CommitDataTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        System.out.println("### "+getName()+" ###");
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
                NbModuleSuite.createConfiguration(CommitDataTest.class).addTest("testCommitFile", "testRecognizeMimeType").enableModules(".*").clusters(".*"));
    }
        
    public void testCommitFile() throws Exception {
        long timeout = JemmyProperties.getCurrentTimeout("ComponentOperator.WaitComponentTimeout");
//        try {
//            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
//        } finally {
//            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", timeout);
//        }
//
//        timeout = JemmyProperties.getCurrentTimeout("DialogWaiter.WaitDialogTimeout");
//        try {
//            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
//        } finally {
//            JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", timeout);
//        }
        
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            TestKit.showStatusLabels();
            
            org.openide.nodes.Node nodeIDE;
            long start;
            long end;
            String color;
            JTableOperator table;
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());

            MessageHandler mh = new MessageHandler("Refreshing");
            log.addHandler(mh);
            TestKit.createNewElement(PROJECT_NAME, "javaapp", "NewClass");

            Node nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeFile.performPopupAction("Mercurial|Status");
            TestKit.waitText(mh);

            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            VersioningOperator vo = VersioningOperator.invoke();
            table = vo.tabFiles();
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            
            //invoke commit action but exlude the file from commit
            start = System.currentTimeMillis();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            mh = new MessageHandler("Preparing Commit");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            CommitOperator cmo = CommitOperator.invoke(nodeFile);
            TestKit.waitText(mh);

            end = System.currentTimeMillis();
            //System.out.println("Duration of invoking Commit dialog: " + (end - start));
            //print message to log file.
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            new EventTool().waitNoEvent(2000);
            cmo.selectCommitAction("NewClass.java", "Exclude from Commit");
            TimeoutExpiredException tee = null;
            assertFalse(cmo.btCommit().isEnabled());
            new EventTool().waitNoEvent(1000);
            cmo.cancel();
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            table = vo.tabFiles();
            new EventTool().waitNoEvent(1000);
            assertEquals("Wrong row count of table.", 1, table.getRowCount());
            assertEquals("Expected file is missing.", "NewClass.java", table.getModel().getValueAt(0, 0).toString());
            assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);

            mh = new MessageHandler("Preparing Commit");
            log.addHandler(mh);

            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            cmo = CommitOperator.invoke(nodeFile);
            TestKit.waitText(mh);
            new EventTool().waitNoEvent(2000);
            cmo.selectCommitAction("NewClass.java", "Commit");
            start = System.currentTimeMillis();

            mh = new MessageHandler("Committing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            cmo.commit();

            TestKit.waitText(mh);
            
            end = System.currentTimeMillis();
            
            nodeFile = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp" + "|NewClass.java");
            nodeIDE = (org.openide.nodes.Node) nodeFile.getOpenideNode();
            TestKit.printLogStream(stream, "Duration of invoking Commit dialog: " + (end - start));
            //color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            new EventTool().waitNoEvent(1000);
            vo = VersioningOperator.invoke();
            //        TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
    
    public void testRecognizeMimeType() throws Exception {
        //JemmyProperties.setCurrentTimeout("ComponentOperator.WaitComponentTimeout", 30000);
        //JemmyProperties.setCurrentTimeout("DialogWaiter.WaitDialogTimeout", 30000);
        try {
            TestKit.showStatusLabels();
            org.openide.nodes.Node nodeIDE;
            JTableOperator table;
            String color;
            String status;
            String[] expected = {"pp.bmp", "pp.dib", "pp.GIF", "pp.JFIF", "pp.JPE", "pp.JPEG", "pp.JPG", "pp.PNG", "pp.TIF", "pp.TIFF", "pp.zip", "text.txt", "test.jar"};
            
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            File work = TestKit.loadOpenProject(PROJECT_NAME, getDataDir());
            //create various types of files
            String src = getDataDir().getCanonicalPath() + File.separator + "files" + File.separator;
            String dest = work.getCanonicalPath() + File.separator + PROJECT_NAME + File.separator + "src" + File.separator + "javaapp" + File.separator;
            
            for (int i = 0; i < expected.length; i++) {
                TestKit.copyTo(src + expected[i], dest + expected[i]);
            }
            
            Node nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            nodeSrc.performPopupAction("Mercurial|Status");
            new EventTool().waitNoEvent(5000);
            
            Node nodeTest;
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
                color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
                //System.out.println(expected[i] + " #1 : " + nodeIDE.getHtmlDisplayName());
                assertEquals("Wrong status of node!!!", TestKit.NEW_STATUS, status);
                assertEquals("Wrong color of node!!!", TestKit.NEW_COLOR, color);
            }
            
            VersioningOperator vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            String[] actual = new String[model.getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
            }
            int result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            
            nodeSrc = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            CommitOperator cmo = CommitOperator.invoke(nodeSrc);
            new EventTool().waitNoEvent(1500);
            table = cmo.tabFiles();
            model = table.getModel();
            actual = new String[model.getRowCount()];
            for (int i = 0; i < actual.length; i++) {
                actual[i] = model.getValueAt(i, 0).toString();
                if (actual[i].endsWith(".txt")) {
                    assertEquals("Expected text file.", "Commit", model.getValueAt(i, 2).toString());
                    //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=77046!!!");
                } else {
                    assertEquals("Expected text file.", "Commit", model.getValueAt(i, 2).toString());
                }
            }

            MessageHandler mh = new MessageHandler("Committing");
            log.addHandler(mh);

            result = TestKit.compareThem(expected, actual, false);
            assertEquals("Not All files listed in Commit dialog", expected.length, result);
            cmo.commit();
//            OutputTabOperator oto = new OutputTabOperator(TestKit.getProjectAbsolutePath(PROJECT_NAME));
//            oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 30000);
//            for (int i = 0; i < expected.length; i++) {
//                oto.waitText("hg add " + expected[i]);
//            }
//            oto.waitText("INFO: End of Commit");
            //System.out.println("Issue should be fixed: http://www.netbeans.org/issues/show_bug.cgi?id=77060!!!");

            TestKit.waitText(mh);

            //files have been committed,
            //verify explorer node
            for (int i = 0; i < expected.length; i++) {
                nodeTest = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|" + expected[i]);
                nodeIDE = (org.openide.nodes.Node) nodeTest.getOpenideNode();
                stream.print(expected[i] + ": " + nodeIDE.getHtmlDisplayName());
                //System.out.println(expected[i] + " #2: " + nodeIDE.getHtmlDisplayName());
                assertNull("Wrong status or color of node!!!", TestKit.getColor(nodeIDE.getHtmlDisplayName()));
            }
            //verify versioning view
            vo = VersioningOperator.invoke();
            TimeoutExpiredException tee = null;
            try {
                vo.tabFiles();
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
            assertNotNull("There shouldn't be any table in Versioning view", tee);
            //TestKit.removeAllData(PROJECT_NAME);
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
            
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
}
