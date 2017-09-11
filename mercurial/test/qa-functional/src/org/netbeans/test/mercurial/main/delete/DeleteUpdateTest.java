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

package org.netbeans.test.mercurial.main.delete;

import java.io.File;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.mercurial.operators.VersioningOperator;
import org.netbeans.test.mercurial.utils.MessageHandler;
import org.netbeans.test.mercurial.utils.TestKit;

/**
 *
 * @author pvcs
 */
public class DeleteUpdateTest extends JellyTestCase {
    
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    public PrintStream stream;
    String os_name;
    static Logger log;
    
    /** Creates a new instance of DeleteUpdateTest */
    public DeleteUpdateTest(String name) {
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
                NbModuleSuite.createConfiguration(DeleteUpdateTest.class).addTest("testDeleteUpdate").enableModules(".*").clusters(".*"));
    }
    
    public void testDeleteUpdate() throws Exception {
        try {
            if (TestKit.getOsName().indexOf("Mac") > -1)
                NewProjectWizardOperator.invoke().close();
            stream = new PrintStream(new File(getWorkDir(), getName() + ".log"));
            TestKit.loadOpenProject(PROJECT_NAME, getDataDir());

            MessageHandler mh = new MessageHandler("Refreshing");
            log.addHandler(mh);

            Node node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            node.performPopupAction("Mercurial|Status");
            TestKit.waitText(mh);

            new EventTool().waitNoEvent(10000);

            node.performPopupActionNoBlock("Delete");
            NbDialogOperator dialog = new NbDialogOperator("Delete");
//            NbDialogOperator dialog = new NbDialogOperator("Safe Delete");
            JButtonOperator btn1 = new JButtonOperator(dialog, "OK");
            
            btn1.push();
            
            new EventTool().waitNoEvent(1000);
            VersioningOperator vo = VersioningOperator.invoke();
            JTableOperator table;
            Exception e = null;
            try {
                table = vo.tabFiles();
                assertEquals("Files should have been [Locally Removed]", "Locally Removed", table.getValueAt(0, 1).toString());
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("Unexpected behavior - file should appear in Versioning view!!!", e);
            
            e = null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNotNull("TimeoutExpiredException should have been thrown. Deleted file can't be visible!!!", e);
            
            System.out.println("DEBUG: testDeleteUpdate - 1");
            //update so the deleted file appears again
            String tabName=TestKit.getProjectAbsolutePath(PROJECT_NAME);
            new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp").select();
            new EventTool().waitNoEvent(1000);
            System.out.println("DEBUG: testDeleteUpdate - 2");
            //node.performPopupActionNoBlock("Mercurial|Update..."); // the popup menu was removed...

            mh = new MessageHandler("Updating");
            TestKit.removeHandlers(log);
            log.addHandler(mh);

            node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp");
            node.select();
            node.performMenuActionNoBlock("Team|Mercurial|Update...");
            NbDialogOperator dlgUpdate = new NbDialogOperator("Update Repository");
            JButtonOperator btnUpdate = new JButtonOperator(dlgUpdate, "Update");
            btnUpdate.push();

            TestKit.waitText(mh);

            new EventTool().waitNoEvent(3000);
            
            e=null;
            try {
                node = new Node(new SourcePackagesNode(PROJECT_NAME), "javaapp|Main.java");
            } catch (Exception ex) {
                e = ex;
            }
            assertNull("TimeoutExpiredException should not have been thrown. Updating deleted file should make it visible!!!", e);
            TestKit.closeProject(PROJECT_NAME);
        } catch (Exception e) {
            TestKit.closeProject(PROJECT_NAME);
            throw new Exception("Test failed: " + e);
        } 
    }
}
