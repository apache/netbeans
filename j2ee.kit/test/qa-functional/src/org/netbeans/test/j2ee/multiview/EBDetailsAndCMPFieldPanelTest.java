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
