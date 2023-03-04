/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.j2ee.multiview;

import java.io.File;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.ejb.DDProvider;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.j2ee.dd.api.ejb.Entity;
import org.netbeans.modules.j2ee.ddloaders.multiview.BeanDetailNode;
import org.netbeans.modules.j2ee.ddloaders.multiview.BeanDetailsPanel;
import org.netbeans.modules.j2ee.ddloaders.multiview.EjbJarMultiViewDataObject;
import org.netbeans.modules.j2ee.ejbjarproject.EjbJarProject;
import org.netbeans.modules.xml.multiview.ui.SectionNodePanel;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.cookies.EditCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

/**
 *
 * @author blaha
 */
public class EBDetailsAndCMPFieldPanelTest extends J2eeTestCase {

    private static Project project;
    private static EjbJarMultiViewDataObject ddObj;
    private static FileObject ddFo;
    private static Entity bean;
    private static EjbJar ejbJar;

    public EBDetailsAndCMPFieldPanelTest(String testName) {
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
        return createAllModulesServerSuite(Server.ANY, EBDetailsAndCMPFieldPanelTest.class,
                "testOpenProject",
                "testEBName",
                "testDescription",
                "testSmallIcon",
                "testLargeIcon",
                "testRevertChanges");
    }

    public void testOpenProject() throws Exception {
        File projectDir = new File(getDataDir(), "projects/TestCMP");
        project = (Project) J2eeProjectSupport.openProject(projectDir);
        assertNotNull("Project is null.", project);
        Thread.sleep(1000);

        EjbJarProject ejbJarProject = (EjbJarProject) project;
        ddFo = ejbJarProject.getAPIEjbJar().getDeploymentDescriptor();  // deployment descriptor
        assertNotNull("ejb-jar.xml FileObject is null.", ddFo);

        ejbJar = DDProvider.getDefault().getDDRoot(ddFo);

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

    public void testEBName() throws Exception {
        assertEquals("CustomerEB", getBeanDetailPanel().getDisplayNameTextField().getText());
        getBeanDetailPanel().getDisplayNameTextField().setText("testBeanName");
        assertEquals("testBeanName", bean.getDisplayName(null));
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<display-name>testBeanName</display-name>");
        utils.save(ddObj);
        utils.checkFiles("testEBName", new String[]{"ejb-jar.xml"}, null);
    }

    public void testDescription() throws Exception {
        assertEquals("jdbc:mysql://localhost:3306/users [blaha on Default schema]", getBeanDetailPanel().getDescriptionTextArea().getText());
        getBeanDetailPanel().getDescriptionTextArea().setText("testDescription");
        assertEquals("testDescription", bean.getDescription(null));
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<description>testDescription</description>");
        utils.save(ddObj);
        utils.checkFiles("testDescription", new String[]{"ejb-jar.xml"}, null);
    }

    public void testSmallIcon() throws Exception {
        assertEquals("", getBeanDetailPanel().getSmallIconTextField().getText().trim());
        getBeanDetailPanel().getSmallIconTextField().setText("testEntitySmallIcon");
        assertEquals("testEntitySmallIcon", bean.getSmallIcon(null));
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<small-icon>testEntitySmallIcon</small-icon>");
        utils.save(ddObj);
        utils.checkFiles("testSmallIcon", new String[]{"ejb-jar.xml"}, null);
    }

    public void testLargeIcon() throws Exception {
        assertEquals("", getBeanDetailPanel().getLargeIconTextField().getText().trim());
        getBeanDetailPanel().getLargeIconTextField().setText("testEntityLargeIcon");
        assertEquals("testEntityLargeIcon", bean.getLargeIcon(null));
        Utils utils = new Utils(this);
        utils.checkInXML(ddObj, "<large-icon>testEntityLargeIcon</large-icon>");
        utils.save(ddObj);
        utils.checkFiles("testLargeIcon", new String[]{"ejb-jar.xml"}, null);
    }

    public void testRevertChanges() throws Exception {
        bean.setDisplayName("CustomerEB"); // back changes
        bean.setDescription("jdbc:mysql://localhost:3306/users [blaha on Default schema]"); // back changes
        bean.setSmallIcon(null); // back changes
        bean.setLargeIcon(null); // back changes
        ejbJar.write(ddFo);
    }

    private BeanDetailsPanel getBeanDetailPanel() {
        Node[] nnChild = Utils.getChildrenNodes(Utils.getEntityNode(ddObj));
        for (int k = 0; k < nnChild.length; k++) {
            if (nnChild[k] instanceof BeanDetailNode) {
                SectionNodePanel overviewPanel = ((BeanDetailNode) nnChild[k]).getSectionNodePanel();
                overviewPanel.open();
                return (BeanDetailsPanel) overviewPanel.getInnerPanel();
            }
        }
        return null;
    }
}
