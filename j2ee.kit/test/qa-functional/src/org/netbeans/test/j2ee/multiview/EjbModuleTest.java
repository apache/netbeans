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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.test.j2ee.multiview;

import java.io.File;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarDetailsNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarDetailsPanel;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.XmlMultiViewEditorSupport;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author blaha
 */
public class EjbModuleTest extends J2eeTestCase {

    private static Project project;
    private static EjbJarMultiViewDataObject ddObj;
    private static FileObject ddFo;

    public EjbModuleTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("############ " + getName() + " ############");
    }

    @Override
    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.ANY, EjbModuleTest.class,
                "testOpenProject",
                "testRenameDisplayName",
                "testChangeDescription",
                "testAddSmallIcon",
                "testAddLargeIcon");
    }

    /*
     * Method open project
     *
     */
    public void testOpenProject() throws Exception {
        File projectDir = new File(getDataDir(), "projects/TestCMP");
        project = (Project) J2eeProjectSupport.openProject(projectDir);
        assertNotNull("Project is null.", project);
        Thread.sleep(1000);

        EjbJarProject ejbJarProject = (EjbJarProject) project;
        ddFo = ejbJarProject.getAPIEjbJar().getDeploymentDescriptor();  // deployment descriptor
        assertNotNull("ejb-jar.xml FileObject is null.", ddFo);

        ddObj = (EjbJarMultiViewDataObject) DataObject.find(ddFo); //MultiView Editor
        assertNotNull("MultiViewDO is null.", ddObj);

        EditCookie edit = (EditCookie) ddObj.getCookie(EditCookie.class);
        edit.edit();
        Thread.sleep(1000);
    }

    /*
     * Rename Display name from UI, test DD, text in editor and then check file
     */
    public void testRenameDisplayName() throws Exception {
        String displayName = "testDisplayName";
        assertNotNull("DDObject not found", ddObj);

        ddObj.showElement(ddObj.getEjbJar()); //open visual editor
        Utils.waitForAWTDispatchThread();
        getDetailPanel().getDisplayNameTextField().setText(displayName);
        // check DD beans
        assertEquals("DD bean isn't updated", displayName, DDProvider.getDefault().getDDRoot(ddFo).getDisplayName(null));
        Thread.sleep(4000);
        checkinXML("<display-name>" + displayName + "</display-name>");

        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
    }

    public void testChangeDescription() throws Exception {
        String descriptionName = "test New description";
        getDetailPanel().getDescriptionTextArea().setText(descriptionName);
        // check DD beans
        assertEquals("DD bean isn't updated", descriptionName, DDProvider.getDefault().getDDRoot(ddFo).getDescription(null));

        checkinXML("<description>" + descriptionName + "</description>");

        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
    }

    public void testAddSmallIcon() throws Exception {
        String smallIcon = "/tmp/test/small";
        getDetailPanel().getSmallIconTextField().setText(smallIcon);
        // check DD beans
        assertEquals("DD bean isn't updated", smallIcon, DDProvider.getDefault().getDDRoot(ddFo).getSmallIcon());

        checkinXML("<small-icon>" + smallIcon + "</small-icon>");

        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
    }

    public void testAddLargeIcon() throws Exception {
        String largeIcon = "/tmp/test/large";
        getDetailPanel().getLargeIconTextField().setText(largeIcon);
        // check DD beans
        assertEquals("DD bean isn't updated", largeIcon, DDProvider.getDefault().getDDRoot(ddFo).getLargeIcon());

        checkinXML("<large-icon>" + largeIcon + "</large-icon>");

        //save ejb-jar.xml editor
        SaveCookie saveCookie = (SaveCookie) ddObj.getCookie(SaveCookie.class);
        assertNotNull("Save cookie is null, Data object isn't changed!", saveCookie);
        if (saveCookie != null) {
            saveCookie.save();
        }
    }

    private void checkinXML(String findText) throws Exception {
        Thread.sleep(3000);
        //check editor in text node
        XmlMultiViewEditorSupport editor = (XmlMultiViewEditorSupport) ddObj.getCookie(EditorCookie.class);
        javax.swing.text.Document document = editor.getDocument();
        document.addDocumentListener(new TestDocumentListener(findText));
    }

    private EjbJarDetailsPanel getDetailPanel() {
        ToolBarMultiViewElement toolBar = ddObj.getActiveMVElement();
        assertNotNull("ToolBarMultiViewElement is null", toolBar);
        SectionNodeView sectionView = (SectionNodeView) toolBar.getSectionView();

        Children nodes = sectionView.getRootNode().getChildren();
        Node[] n = nodes.getNodes();
        if (n[0] instanceof EjbJarDetailsNode) {
            EjbJarDetailsNode detailNode = (EjbJarDetailsNode) n[0];
            return (EjbJarDetailsPanel) detailNode.getSectionNodePanel().getInnerPanel();
        }
        return null;
    }
}
