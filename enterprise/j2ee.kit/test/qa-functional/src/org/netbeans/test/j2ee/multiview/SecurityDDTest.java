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
