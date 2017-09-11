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

package org.netbeans.test.subversion.main.branches;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.subversion.operators.CommitStepOperator;
import org.netbeans.test.subversion.operators.CopyToOperator;
import org.netbeans.test.subversion.operators.CreateNewFolderOperator;
import org.netbeans.test.subversion.operators.FolderToImportStepOperator;
import org.netbeans.test.subversion.operators.ImportWizardOperator;
import org.netbeans.test.subversion.operators.RepositoryBrowserImpOperator;
import org.netbeans.test.subversion.operators.RepositoryStepOperator;
import org.netbeans.test.subversion.utils.MessageHandler;
import org.netbeans.test.subversion.utils.RepositoryMaintenance;
import org.netbeans.test.subversion.utils.TestKit;

/**
 *
 * @author peter
 */
public class CopyUiTest extends JellyTestCase{
    
    public static final String TMP_PATH = "/tmp";
    public static final String REPO_PATH = "repo";
    public static final String WORK_PATH = "work";
    public static final String PROJECT_NAME = "JavaApp";
    public File projectPath;
    static Logger log;
    
    /** Creates a new instance of CopyUiTest */
    public CopyUiTest(String name) {
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
                 NbModuleSuite.createConfiguration(CopyUiTest.class).addTest(
                    "testInvokeCloseCopy"
                 )
                 .enableModules(".*")
                 .clusters(".*")
        );
     }
    
    public void testInvokeCloseCopy() throws Exception{
       // try {
            MessageHandler mh = new MessageHandler("Committing");
            log.addHandler(mh);
            TestKit.closeProject(PROJECT_NAME);
            if (TestKit.getOsName().indexOf("Mac") > -1)
                new NewProjectWizardOperator().invoke().close();
            

            new File(TMP_PATH).mkdirs();
            RepositoryMaintenance.deleteFolder(new File(TMP_PATH + File.separator + REPO_PATH));
            RepositoryMaintenance.createRepository(TMP_PATH + File.separator + REPO_PATH);
            RepositoryMaintenance.loadRepositoryFromFile(TMP_PATH + File.separator + REPO_PATH, getDataDir().getCanonicalPath() + File.separator + "repo_dump");      
            projectPath = TestKit.prepareProject("Java", "Java Application", PROJECT_NAME);

            ImportWizardOperator.invoke(ProjectsTabOperator.invoke().getProjectRootNode(PROJECT_NAME));
            new EventTool().waitNoEvent(2000);
            RepositoryStepOperator rso = new RepositoryStepOperator();
            rso.setRepositoryURL(RepositoryStepOperator.ITEM_FILE + RepositoryMaintenance.changeFileSeparator(TMP_PATH + File.separator + REPO_PATH, false));
            rso.next();
            new EventTool().waitNoEvent(2000);

            FolderToImportStepOperator ftiso = new FolderToImportStepOperator();
            ftiso.setRepositoryFolder("trunk/Import" + PROJECT_NAME);
            ftiso.setImportMessage("initial import");
            ftiso.next();
            new EventTool().waitNoEvent(2000);
            CommitStepOperator cso = new CommitStepOperator();
            cso.finish();
            
            TestKit.waitText(mh);
            
            Node projNode = new Node(new ProjectsTabOperator().tree(), PROJECT_NAME);
            CopyToOperator cto = CopyToOperator.invoke(projNode);
            cto.verify();
            //only required nodes are expended - want to see all in browser
            cto.setRepositoryFolder("");
            // needs to be fixed, repo browser is outline view
            RepositoryBrowserImpOperator rbio = cto.browseRepository();
            //rbio.verify();
            //rbio.selectFolder("tags");
           // rbio.selectFolder("trunk");
            //rbio.selectFolder("branches");
            CreateNewFolderOperator cnfo = rbio.createNewFolder();
            cnfo.setFolderName("release01-" + PROJECT_NAME);
            cnfo.cancel();
            //Creation of new folder was canceled - no new folder can't be created
            TimeoutExpiredException tee = null;
            try {
               // rbio.selectFolder("branches|release01-" + PROJECT_NAME);
            } catch (Exception e) {
                tee = (TimeoutExpiredException) e;
            }
           // assertNotNull(tee);

            //rbio.selectFolder("branches");
            cnfo = rbio.createNewFolder();
            cnfo.setFolderName("release01-" + PROJECT_NAME);
            cnfo.ok();
            //rbio.selectFolder("branches|release01-" + PROJECT_NAME);
            rbio.ok();
            //assertEquals("New folder for copy purpose wasn't created", "branches/release01-" + PROJECT_NAME, cto.getRepositoryFolder());
            cto.cancel();
        //} catch (Exception e) {
          //  throw new Exception("Test failed: " + e);
        //} finally {
            TestKit.closeProject(PROJECT_NAME); 
       // }    
    }
}
