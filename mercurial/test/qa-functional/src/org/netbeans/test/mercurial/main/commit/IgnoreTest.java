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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.ide.ProjectSupport;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author peter pis
 */
public class IgnoreTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaAppIgnUnign";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    static Logger log;
    
    /** Creates a new instance of IgnoreTest */
    public IgnoreTest(String name) {
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
                NbModuleSuite.createConfiguration(IgnoreTest.class).addTest("testIgnoreUnignoreFile" /*, "testFinalRemove" */).enableModules(".*").clusters(".*"));
    }
        
    public void testIgnoreUnignoreFile() throws Exception {


            NbDialogOperator ndo = null;
            
        try {
            MessageHandler mh = new MessageHandler("Ignoring");

            log.addHandler(mh);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            
            TestKit.showStatusLabels();
            TestKit.prepareProject(TestKit.PROJECT_CATEGORY, TestKit.PROJECT_TYPE, PROJECT_NAME);
            ProjectSupport.waitScanFinished();
            new EventTool().waitNoEvent(1000);
            new ProjectsTabOperator().getProjectRootNode(TestKit.PROJECT_NAME).performPopupActionNoBlock("Versioning|Initialize Mercurial Project");

            ndo = new NbDialogOperator("Repository root path");
            JButtonOperator buttOperator = new JButtonOperator(ndo,"OK");
            buttOperator.push();


            new EventTool().waitNoEvent(5000);

            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));



            TestKit.createNewElement(PROJECT_NAME, "javaappignunign", "NewClassIgnUnign");

           

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
          
            node.performPopupAction("Mercurial|Toggle Ignore");
//            String outputTabName=TestKit.getProjectAbsolutePath(PROJECT_NAME);

            TestKit.waitText(mh);
           
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            
            org.openide.nodes.Node nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
       

            String color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            String status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be ignored!!!", TestKit.IGNORED_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be ignored!!!", TestKit.IGNORED_STATUS, status);
            
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();

            //unignore file
            mh = new MessageHandler("Ignoring");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            node.performPopupAction("Mercurial|Toggle Ignore");
            TestKit.waitText(mh);
            new EventTool().waitNoEvent(1000);
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");
            node.select();
            nodeIDE = (org.openide.nodes.Node) node.getOpenideNode();
            new EventTool().waitNoEvent(5000);
            color = TestKit.getColor(nodeIDE.getHtmlDisplayName());
            status = TestKit.getStatus(nodeIDE.getHtmlDisplayName());
            assertEquals("Wrong color of node - file color should be new!!!", TestKit.NEW_COLOR, color);
            assertEquals("Wrong annotation of node - file status should be new!!!", TestKit.NEW_STATUS, status);
            
            //verify content of Versioning view
            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaappignunign|NewClassIgnUnign");

            mh = new MessageHandler("Refreshing");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node.select();
            node.performPopupAction("Mercurial|Status");
            TestKit.waitText(mh);
            new EventTool().waitNoEvent(1000);
            VersioningOperator vo = VersioningOperator.invoke();
            TableModel model = vo.tabFiles().getModel();
            assertEquals("Versioning view should be empty", 1, model.getRowCount());
            assertEquals("File should be listed in Versioning view", "NewClassIgnUnign.java", model.getValueAt(0, 0).toString());
            
            stream.flush();
            stream.close();
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        }
    }
    
    public void testFinalRemove() throws Exception {
        TestKit.finalRemove();
    }
}
