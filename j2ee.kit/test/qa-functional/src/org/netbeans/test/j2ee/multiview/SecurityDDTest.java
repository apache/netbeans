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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
 *
 */
package org.netbeans.test.j2ee.multiview;

import java.awt.Component;
import java.io.File;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.web.*;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.SecurityRoleTableModel;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.modules.xml.multiview.ui.DefaultTablePanel;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Called from WebProjectDDTest.
 * 
 * @author kolard
 */
public class SecurityDDTest extends J2eeTestCase {

    private static DDTestUtils utils;

    /** Creates a new instance of SecurityDDTest */
    public SecurityDDTest(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    private static FileObject ddFo;
    private static WebApp webapp;
    private static DDDataObject ddObj;

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(SecurityDDTest.class);
        conf = addServerTests(conf,
                "testOpenProject",
                "testExistingLoginConfiguration",
                "testExistingSecurityRoles",
                "testAddSecurityRole",
                "testEditSecurityRole",
                "testDelSecurityRole");
        conf = conf.enableModules(".*").clusters(".*");
        return NbModuleSuite.create(conf);
    }

    public void testOpenProject() throws Exception {
        File projectDir = new File(getDataDir(), "projects/TestWebApp");
        Project project = (Project) J2eeProjectSupport.openProject(projectDir);
        assertNotNull("Project is null.", project);
        WebProject webproj = (WebProject) project;
        assertNotNull("Project is not webproject", webproj);
        ddFo = webproj.getAPIWebModule().getDeploymentDescriptor();
        assertNotNull("Can't get deploy descriptor file object", ddFo);
        webapp = DDProvider.getDefault().getDDRoot(ddFo);
        ddObj = (DDDataObject) DataObject.find(ddFo);
        assertNotNull("Multiview is null", ddObj);
        ddObj.openView(12); // open Security view
        utils = new DDTestUtils(ddObj, this);
        Utils.waitForAWTDispatchThread();
    }

    public void testExistingLoginConfiguration() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("login_config");
        Component[] comp = panel.getComponents();
        /*
        for(int i=0;i<comp.length;i++)
        {
        System.err.println("comp:" + (comp[i]));
        }
         */
        assertEquals("Login authentication isn't set to form", "Form", ((JRadioButton) comp[4]).getText());
        assertEquals("Undefined login page", "/login.jsp", webapp.getSingleLoginConfig().getFormLoginConfig().getFormLoginPage());
        assertEquals("Undefined error page", "/loginError.jsp", webapp.getSingleLoginConfig().getFormLoginConfig().getFormErrorPage());

    }

    public void testExistingSecurityRoles() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        assertEquals("Wrong number of roles", 2, model.getRowCount());
        assertEquals("Wrong role name", "admin", model.getValueAt(0, 0));
        assertEquals("Wrong role description", "administrator", model.getValueAt(0, 1));
        assertEquals("Wrong role name", "user", model.getValueAt(1, 0));
        assertEquals("Wrong role description", "testuser", model.getValueAt(1, 1));

    }

    public void testAddSecurityRole() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        model.addRow(new Object[]{"user1", "user1desc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        assertEquals("Role not added", 3, model.getRowCount());
        ((Component) comp[0]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<security-role>\\s*<description>user1desc</description>\\s*<role-name>user1</role-name>\\s*</security-role>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<security-role>\\s*<description>user1desc</description>\\s*<role-name>user1</role-name>\\s*</security-role>.*");
            }
        };
    }

    public void testEditSecurityRole() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        model.editRow(2, new Object[]{"user2", "user2desc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        assertEquals("Role not changed", "user2", model.getValueAt(2, 0));
        assertEquals("Role description not changed", "user2desc", model.getValueAt(2, 1));
        ((Component) comp[0]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<security-role>\\s*<description>user2desc</description>\\s*<role-name>user2</role-name>\\s*</security-role>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<security-role>\\s*<description>user2desc</description>\\s*<role-name>user2</role-name>\\s*</security-role>.*");
            }
        };
    }

    public void testDelSecurityRole() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("security_roles");
        Component[] comp = panel.getComponents();
        SecurityRoleTableModel model = (SecurityRoleTableModel) ((DefaultTablePanel) comp[0]).getModel();
        model.removeRow(2);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        assertEquals("Role not deleted", 2, model.getRowCount());
        utils.checkNotInDDXML(".*<security-role>\\s*<description>user2desc</description>\\s*<role-name>user2</role-name>\\s*</security-role>.*");

    }
}
