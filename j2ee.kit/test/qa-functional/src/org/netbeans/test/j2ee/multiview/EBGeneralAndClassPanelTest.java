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
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbImplementationAndInterfacesNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbImplementationAndInterfacesPanel;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.netbeans.modules.j2ee.ddloaders.multiview.EntityOverviewNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.EntityOverviewPanel;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.netbeans.test.j2ee.lib.ContentComparator;

/**
 *
 * @author blaha
 */
public class EBGeneralAndClassPanelTest extends J2eeTestCase {

    private static Project project;
    private static EjbJarMultiViewDataObject ddObj;
    private static FileObject ddFo;
    private static Entity bean;

    public EBGeneralAndClassPanelTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.ANY, EBGeneralAndClassPanelTest.class,
                "testOpenProject",
                "testEntityNodeName",
                "testEntityName",
                "testEntityPersistanceType",
                "testEntityAbstractName",
                "testEntityPKField",
                "testEntityPKClass",
                "testChangeReentrant",
                "testBeanClassName",
                "testLocalHomeIName",
                "testLocalIName",
                // need to be fixed
                //"testRemoteIName",
                //"testChangePKMultiple",
                //"testChangePK",
                //"testLocalInterfaceCheckBox",
                //"testRemoteInterfaceCheckBox",
                //"testEnableRemoteI",
                //"testDisableRemoteI",
                "testRemoteHomeIName");
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("############ " + getName() + " ############");
    }

    @Override
    protected void tearDown() throws Exception {
    }

    /*
     * Method open project
     *
     */
    public void testOpenProject() throws Exception {
        File projectDir = new File(getDataDir(), "projects/" + Utils.EJB_PROJECT_NAME);
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

        // select CustomerBean
        EnterpriseBeans beans = DDProvider.getDefault().getDDRoot(ddFo).getEnterpriseBeans();
        bean = (Entity) beans.findBeanByName(EnterpriseBeans.ENTITY,
                Ejb.EJB_NAME, "CustomerBean");

        ddObj.showElement(bean); //open visual editor
        Utils.waitForAWTDispatchThread();
    }

    // General panel
    public void testEntityNodeName() {
        assertEquals("Entity node doesn't name CustomerEB", "CustomerEB", Utils.getEntityNode(ddObj).getDisplayName());
    }

    public void testEntityName() {
        assertEquals(bean.getEjbName(), getEntityGeneralPanel().getEjbNameTextField().getText());
    }

    public void testEntityPersistanceType() {
        assertEquals("Container (CMP)", getEntityGeneralPanel().getPersistenceTypeTextField().getText());
    }

    public void testEntityAbstractName() {
        assertEquals(bean.getAbstractSchemaName(), getEntityGeneralPanel().getAbstractSchemaNameTextField().getText());
    }

    public void testEntityPKField() {
        String pkField = (String) getEntityGeneralPanel().getPrimaryKeyFieldComboBox().getSelectedItem();
        assertEquals(bean.getPrimkeyField(), pkField);
    }

    public void testEntityPKClass() {
        String className = (String) getEntityGeneralPanel().getPrimaryKeyClassComboBox().getSelectedItem();
        assertEquals(bean.getPrimKeyClass(), className);
    }

    public void testChangeReentrant() throws Exception {
        assertEquals(false, getEntityGeneralPanel().getReentrantCheckBox().isSelected());
        getEntityGeneralPanel().getReentrantCheckBox().doClick(); // change it
        assertEquals(true, bean.isReentrant());
        Thread.sleep(1000);
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<reentrant>true</reentrant>");
        utils.save(ddObj);
        if (ContentComparator.equalsXML(FileUtil.toFile(ddFo), getGoldenFile("testChangeReentrant_ejb-jar.xml")) == false) {
            assertFile(FileUtil.toFile(ddFo), getGoldenFile("testChangeReentrant_ejb-jar.xml"), new File(getWorkDir(), "testChangeReentrant.diff")); //check file on disc
        }
    }

    public void testChangePKMultiple() throws Exception {
        assertNotNull("DDObject not found", ddObj);
        getEntityGeneralPanel().getPrimaryKeyFieldComboBox().setSelectedIndex(0); //select multiply PK
        getEntityGeneralPanel().getPrimaryKeyClassComboBox().setSelectedItem("cmp.CompoundClassTest");
        assertEquals("cmp.CompoundClassTest", bean.getPrimKeyClass());
        assertNull(bean.getPrimkeyField());
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<prim-key-class>cmp.CompoundClassTest</prim-key-class>");
        utils.save(ddObj);
        utils.checkFiles("testChangePKMultiple", new String[]{"ejb-jar.xml"}, new String[]{"CustomerBean.java", "CustomerLocalHome.java"});
    }

    public void testChangePK() throws Exception {
        assertNotNull("DDObject not found", ddObj);
        getEntityGeneralPanel().getPrimaryKeyFieldComboBox().setSelectedItem("lastName");
        assertEquals("java.lang.String", bean.getPrimKeyClass());
        assertEquals("lastName", bean.getPrimkeyField());
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<prim-key-class>java.lang.String</prim-key-class>");
        utils.checkInXML(ddObj, "<primkey-field>lastName</primkey-field>");
        utils.save(ddObj);
        utils.checkFiles("testChangePK", new String[]{"ejb-jar.xml"}, new String[]{"CustomerBean.java", "CustomerLocalHome.java"});
    }

    // Interfaces and implementation classes panel
    public void testBeanClassName() {
        assertEquals("cmp.CustomerBean", bean.getEjbClass());
        //assertEquals("cmp.CustomerBean", getClassAndInterfacePanel().getBeanClassTextField().getText());
    }

    public void testLocalHomeIName() {
        assertEquals("cmp.CustomerLocalHome", bean.getLocalHome());
        //assertEquals("cmp.CustomerLocalHome", getClassAndInterfacePanel().getLocalHomeTextField().getText());
    }

    public void testLocalIName() {
        assertEquals("cmp.CustomerLocal", bean.getLocal());
        //assertEquals("cmp.CustomerLocal", getClassAndInterfacePanel().getLocalComponentTextField().getText());
    }

    // Need to be fixed
    public void testLocalInterfaceCheckBox() {
        //assertEquals(true, getClassAndInterfacePanel().getLocalInterfaceCheckBox().isSelected());
    }

    // bean doesn't have remote interface
    public void testRemoteHomeIName() {
        assertNull(bean.getHome());
        assertEquals("", getClassAndInterfacePanel().getRemoteHomeTextField().getText());
    }

    public void testRemoteIName() {
        assertNull(bean.getRemote());
        assertEquals("", getClassAndInterfacePanel().getRemoteComponentTextField().getText());
    }

    // Need to be fixed
    public void testRemoteInterfaceCheckBox() {
        //assertEquals(false, getClassAndInterfacePanel().getRemoteInterfaceCheckBox().isSelected());
    }

    // Need to be fixed
    public void testEnableRemoteI() throws Exception {
        ddObj.getEntityHelper(bean).addInterfaces(false); // add remote interface
        // check DD API
        Thread.sleep(1000);
        assertEquals("cmp.CustomerRemote", bean.getRemote());
        assertEquals("cmp.CustomerRemote", getClassAndInterfacePanel().getRemoteComponentTextField().getText());
        assertEquals("cmp.CustomerRemoteHome", bean.getHome());
        assertEquals("cmp.CustomerRemoteHome", getClassAndInterfacePanel().getRemoteHomeTextField().getText());
        //assertEquals(true, getClassAndInterfacePanel().getRemoteInterfaceCheckBox().isSelected());
        // check XML view
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<remote>cmp.CustomerRemote</remote>");
        utils.checkInXML(ddObj, "<home>cmp.CustomerRemoteHome</home>");
        // check files
        utils.save(ddObj);
        utils.checkFiles("testEnableRemoteI", new String[]{"ejb-jar.xml"},
                new String[]{"CustomerBean.java", "CustomerRemoteHome.java", "CustomerRemote.java"});
    }

    // Need to be fixed
    public void testDisableRemoteI() throws Exception {
        ddObj.getEntityHelper(bean).removeInterfaces(false);
        Thread.sleep(1000);
        assertNull(bean.getRemote());
        assertEquals("", getClassAndInterfacePanel().getRemoteComponentTextField().getText().trim());
        assertNull(bean.getHome());
        assertEquals("", getClassAndInterfacePanel().getRemoteHomeTextField().getText().trim());
        //assertEquals(false, getClassAndInterfacePanel().getRemoteInterfaceCheckBox().isSelected());
        // check XML view
        Utils utils = new Utils(this);
        try {
            utils.checkInXML(ddObj, "<remote>cmp.CustomerRemote</remote>");
            utils.checkInXML(ddObj, "<home>cmp.CustomerRemoteHome</home>");
            fail("Remote elements are still displayd in text view.");
        } catch (AssertionFailedError ex) {
            //it's OK
        }
        // check files
        utils.save(ddObj);
        utils.checkFiles("testDisableRemoteI", new String[]{"ejb-jar.xml"},
                new String[]{"CustomerBean.java"});
        // check tha interface files are deleted
        checkDeletedFiles("CustomerRemoteHome.java");
        checkDeletedFiles("CustomerRemote.java");
        checkDeletedFiles("CustomerRemoteBusiness.java");
    }

    private void checkDeletedFiles(String fileName) {
        if (new File(getDataDir(), "projects/" + Utils.EJB_PROJECT_NAME + "src/java/cmp/" + fileName).exists()) {
            fail(fileName + " isn't deleted.");
        }
    }

    private EntityOverviewPanel getEntityGeneralPanel() {
        Node[] nnChild = Utils.getChildrenNodes(Utils.getEntityNode(ddObj));
        for (int k = 0; k < nnChild.length; k++) {
            if (nnChild[k] instanceof EntityOverviewNode) {
                SectionNodePanel overviewPanel = ((EntityOverviewNode) nnChild[k]).getSectionNodePanel();
                overviewPanel.open();
                EntityOverviewPanel entityPanel = (EntityOverviewPanel) overviewPanel.getInnerPanel();
                return entityPanel;
            }
        }
        return null;
    }

    private EjbImplementationAndInterfacesPanel getClassAndInterfacePanel() {
        assertNotNull(ddObj);
        Node[] nnChild = Utils.getChildrenNodes(Utils.getEntityNode(ddObj));
        for (int k = 0; k < nnChild.length; k++) {
            if (nnChild[k] instanceof EjbImplementationAndInterfacesNode) {
                SectionNodePanel overviewPanel = ((EjbImplementationAndInterfacesNode) nnChild[k]).getSectionNodePanel();
                overviewPanel.open();
                return (EjbImplementationAndInterfacesPanel) overviewPanel.getInnerPanel();
            }
        }
        return null;
    }
}
