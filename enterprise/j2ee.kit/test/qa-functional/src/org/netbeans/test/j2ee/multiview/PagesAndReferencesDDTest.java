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

import javax.swing.text.JTextComponent;
import java.awt.Component;
import java.io.File;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.common.EjbLocalRef;
import org.netbeans.modules.j2ee.dd.api.common.EjbRef;
import org.netbeans.modules.j2ee.dd.api.common.EnvEntry;
import org.netbeans.modules.j2ee.dd.api.common.MessageDestinationRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceEnvRef;
import org.netbeans.modules.j2ee.dd.api.common.ResourceRef;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.ErrorPage;
import org.netbeans.modules.j2ee.dd.api.web.JspConfig;
import org.netbeans.modules.j2ee.dd.api.web.JspPropertyGroup;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.ErrorPagesTablePanel;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 * Called from WebProjectDDTest.
 * @author jp159440
 */
public class PagesAndReferencesDDTest extends J2eeTestCase {

    /** Creates a new instance of PagesAndReferencesDDTest */
    public PagesAndReferencesDDTest(String testName) {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.out.println("############ " + getName() + " ############");
    }
    private static FileObject ddFo;
    private static WebApp webapp;
    private static DDDataObject ddObj;
    private static DDTestUtils utils;

    public static Test suite() {
        NbModuleSuite.Configuration conf = NbModuleSuite.createConfiguration(PagesAndReferencesDDTest.class);
        conf = addServerTests(conf, "testOpenProject", "testExistingWelcomePages", "testAddWelcomePage",
                "testDelWelcomePage", "testExistingErrorPages", "testAddErrorPage", "testModifyErrorPage",
                "testDelErrorPage", "testExistingPropertyGroups", "testModifyPropertyGroup",
                "testDelPropertyGroup", "testExistingEnvEntries", "testAddEnvEntry", "testModifyEnvEntry",
                "testDelEnvEntry", "testExistingResReferences", "testAddResReference", "testModifyResReference",
                "testDelResReference", "testExistingResEnvReferences", "testAddResEnvReference",
                "testModifyResEnvReference", "testDelResEnvReference", "testExistingEJBReferences",
                "testAddLocalEJBReference", "testModifyLocalEJBReference", "testDelLocalEJBReference",
                "testAddRemoteEJBReference", "testModifyRemoteEJBReference", "testDelRemoteEJBReference",
                "testExistingMsgDstReferences", "testAddMsgDstReference", "testModifyMsgDstReference",
                "testDelMsgDstReference");
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
        ddObj.openView(8); // open Pages view
        utils = new DDTestUtils(ddObj, this);
        Utils.waitForAWTDispatchThread();
    }

    public void testExistingWelcomePages() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("welcome_files");
        Component[] comp = panel.getComponents();
        assertEquals("Welcome pages doesn't match", "index.jsp", ((JTextField) comp[1]).getText());
        String[] files = webapp.getSingleWelcomeFileList().getWelcomeFile();
        assertEquals("Wrong number of welcome files", 1, files.length);
        assertEquals("Wrong welcome file", "index.jsp", files[0].trim());
    }

    public void testAddWelcomePage() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("welcome_files");
        Component[] comp = panel.getComponents();
        utils.setText((JTextField) comp[1], "index2.jsp, index3.jsp");
        ((Component) comp[2]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<welcome-file-list>\\s*<welcome-file>index2.jsp</welcome-file>\\s*<welcome-file>index3.jsp</welcome-file>\\s*</welcome-file-list>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<welcome-file-list>\\s*<welcome-file>index2.jsp</welcome-file>\\s*<welcome-file>index3.jsp</welcome-file>\\s*</welcome-file-list>.*");
                String[] files = webapp.getSingleWelcomeFileList().getWelcomeFile();
                assertEquals("Wrong count of welcome pages", 2, files.length);
                assertEquals("Welcome file name doesn't match.", "index2.jsp", files[0]);
                assertEquals("Welcome file name doesn't match.", "index3.jsp", files[1]);
            }
        };
        utils.save();
    }

    public void testDelWelcomePage() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("welcome_files");
        Component[] comp = panel.getComponents();
        utils.setText((JTextField) comp[1], "index.jsp");
        ((Component) comp[2]).requestFocus();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<welcome-file-list>\\s*<welcome-file>\\s*index.jsp\\s*</welcome-file>\\s*</welcome-file-list>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<welcome-file-list>\\s*<welcome-file>\\s*index.jsp\\s*</welcome-file>\\s*</welcome-file-list>.*");
                String[] files = webapp.getSingleWelcomeFileList().getWelcomeFile();
                assertEquals("Wrong count of welcome pages", 1, files.length);
                assertEquals("Welcome file name doesn't match.", "index.jsp", files[0].trim());
            }
        };
        utils.save();
    }

    public void testExistingErrorPages() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("error_pages");
        Component[] comp = panel.getComponents();
        DDBeanTableModel model = (DDBeanTableModel) ((ErrorPagesTablePanel) comp[1]).getTable().getModel();
        assertEquals("Wrong count of error pages", 1, model.getRowCount());
        assertEquals("Error wrong error page parameter.", "/index.jsp", model.getValueAt(0, 0));
        assertEquals("Error wrong error page parameter.", 404, model.getValueAt(0, 1));
        assertEquals("Error wrong error page parameter.", null, model.getValueAt(0, 2));
        ErrorPage[] errorPage = webapp.getErrorPage();
        utils.testProperties(errorPage[0], new String[]{"Location", "ErrorCode", "ExceptionType"}, new Object[]{"/index.jsp", 404, null});
    }

    public void testAddErrorPage() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("error_pages");
        Component[] comp = panel.getComponents();
        DDBeanTableModel model = (DDBeanTableModel) ((ErrorPagesTablePanel) comp[1]).getTable().getModel();
        model.addRow(new Object[]{"/index2.jsp", null, "java.lang.NullPointerException"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        assertEquals("Error page not added", 2, model.getRowCount());
        int i = 0;
        ErrorPage[] errorPages = webapp.getErrorPage();
        for (i = 0; i < errorPages.length; i++) {
            if (errorPages[i].getLocation().equals("/index2.jsp")) {
                break;
            }
        }
        assertTrue("New error page not found", i < errorPages.length);
        utils.testProperties(errorPages[i], new String[]{"Location", "ErrorCode", "ExceptionType"}, new Object[]{"/index2.jsp", null, "java.lang.NullPointerException"});
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("/index2.jsp")) {
                break;
            }
        }
        assertTrue("New error page not found", i < model.getRowCount());
        utils.testTableRow(model, i, new String[]{"/index2.jsp", null, "java.lang.NullPointerException"});
        utils.checkInDDXML(".*<error-page>\\s*<exception-type>java.lang.NullPointerException</exception-type>\\s*<location>/index2.jsp</location>\\s*</error-page>.*");
    }

    public void testModifyErrorPage() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("error_pages");
        Component[] comp = panel.getComponents();
        DDBeanTableModel model = (DDBeanTableModel) ((ErrorPagesTablePanel) comp[1]).getTable().getModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("/index.jsp")) {
                break;
            }
        }
        assertTrue("Error page not found", i < model.getRowCount());
        model.setValueAt("/index3.jsp", i, 0);
        ddObj.modelUpdatedFromUI();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<location>/index3.jsp</location>\\s*</error-page>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<location>/index3.jsp</location>\\s*</error-page>.*");
            }
        };
        model.setValueAt(null, i, 1);
        model.setValueAt("java.lang.IndexOutOfBoundsException", i, 2);
        ddObj.modelUpdatedFromUI();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<exception-type>java.lang.IndexOutOfBoundsException</exception-type>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<exception-type>java.lang.IndexOutOfBoundsException</exception-type>.*");
            }
        };
        utils.save();
        for (i = 0; i < webapp.getErrorPage().length; i++) {
            if (webapp.getErrorPage(i).getLocation().equals("/index3.jsp")) {
                break;
            }
        }
        assertTrue("Error page not found.", i < webapp.getErrorPage().length);
        utils.testProperties(webapp.getErrorPage(i), new String[]{"Location", "ErrorCode", "ExceptionType"}, new Object[]{"/index3.jsp", null, "java.lang.IndexOutOfBoundsException"});
        utils.checkInDDXML(".*<error-page>\\s*<exception-type>java.lang.IndexOutOfBoundsException</exception-type>\\s*<location>/index3.jsp</location>\\s*</error-page>.*");
    }

    public void testDelErrorPage() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("error_pages");
        Component[] comp = panel.getComponents();
        DDBeanTableModel model = (DDBeanTableModel) ((ErrorPagesTablePanel) comp[1]).getTable().getModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("/index3.jsp")) {
                break;
            }
        }
        assertTrue("Error page not found", i < model.getRowCount());
        model.removeRow(i);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        for (i = 0; i < webapp.getErrorPage().length; i++) {
            assertFalse("Error Page not removed", webapp.getErrorPage(i).getLocation().equals("index3.jsp"));
        }
        utils.checkNotInDDXML(".*<error-page>.*<location>/index3.jsp</location>\\s*</error-page>.*");
    }

    public void testExistingPropertyGroups() throws Exception {
        JspConfig jspConfig = webapp.getSingleJspConfig();
        assertNotNull("JspConfig is null", jspConfig);
        JspPropertyGroup[] propertyGroups = jspConfig.getJspPropertyGroup();
        assertEquals("Wrong number of JspPropertyGroups", 1, propertyGroups.length);
        JspPropertyGroup propertyGrp = propertyGroups[0];
        utils.checkPropertyGroup(propertyGrp, "PropGrpName", "PropGrpDesc", "ASCII", new String[]{"head.jsp", "head2.jsp"}, new String[]{"foot.jsp", "foot2.jsp"}, new String[]{"/*"}, new boolean[]{true, true, true});
    }

    public void testModifyPropertyGroup() throws Exception {
        JspPropertyGroup propertyGrp = webapp.getSingleJspConfig().getJspPropertyGroup(0);
        JPanel panel = utils.getInnerSectionPanel(propertyGrp);
        Component[] comp = panel.getComponents();
        panel.requestFocus();
        DDTestUtils.waitForDispatchThread();
        utils.setText((JTextComponent) comp[1], "newname");
        comp[3].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<display-name>newname</display-name>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.setText((JTextComponent) comp[3], "newdesc");
        comp[5].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<description>newdesc</description>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.setText((JTextComponent) comp[5], "*");
        comp[9].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<url-pattern>\\*</url-pattern>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.setText((JTextComponent) comp[9], "ISO-8859-2");
        comp[10].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<page-encoding>ISO-8859-2</page-encoding>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        ((JCheckBox) comp[10]).setSelected(false);
        ddObj.modelUpdatedFromUI();
        comp[14].requestFocus();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<el-ignored>false</el-ignored>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        ((JCheckBox) comp[11]).setSelected(false);
        comp[14].requestFocus();
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<scripting-invalid>false</scripting-invalid>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        ((JCheckBox) comp[12]).setSelected(false);
        ddObj.modelUpdatedFromUI();
        comp[14].requestFocus();
        ddObj.modelUpdatedFromUI();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<is-xml>false</is-xml>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.setText((JTextComponent) comp[14], "prelude.jsp");
        comp[17].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<include-prelude>prelude.jsp</include-prelude>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.setText((JTextComponent) comp[17], "coda.jsp");
        comp[1].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<include-coda>coda.jsp</include-coda>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.save();
        utils.checkPropertyGroup(propertyGrp, "newname", "newdesc", "ISO-8859-2", new String[]{"prelude.jsp"}, new String[]{"coda.jsp"}, new String[]{"*"}, new boolean[]{false, false, false});
        String xml = ".*<jsp-config>\\s*"
                + "<jsp-property-group>\\s*"
                + "<description>newdesc</description>\\s*"
                + "<display-name>newname</display-name>\\s*"
                + "<url-pattern>\\*</url-pattern>\\s*"
                + "<el-ignored>false</el-ignored>\\s*"
                + "<page-encoding>ISO-8859-2</page-encoding>\\s*"
                + "<scripting-invalid>false</scripting-invalid>\\s*"
                + "<is-xml>false</is-xml>\\s*"
                + "<include-prelude>prelude.jsp</include-prelude>\\s*"
                + "<include-coda>coda.jsp</include-coda>\\s*"
                + "</jsp-property-group>\\s*"
                + "</jsp-config>.*";
        utils.checkInDDXML(xml);
    }

    public void testDelPropertyGroup() throws Exception {
        JspPropertyGroup propertyGrp = webapp.getSingleJspConfig().getJspPropertyGroup(0);
        final String name = propertyGrp.getDefaultDisplayName();
        webapp.getSingleJspConfig().removeJspPropertyGroup(propertyGrp);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return !utils.contains(".*<jsp-config>\\s*<jsp-property-group>.*<display-name>" + name + "</display-name>.*</jsp-property-group>\\s*</jsp-config>.*");
            }
        };
        utils.save();
        JspPropertyGroup[] groups = webapp.getSingleJspConfig().getJspPropertyGroup();
        for (int i = 0; i < groups.length; i++) {
            assertNotSame("Property group not deleted", name, groups[i].getDefaultDisplayName());
        }
    }

    public void testExistingEnvEntries() throws Exception {
        ddObj.openView(10); // open References view
        DDTestUtils.waitForDispatchThread();
        DDBeanTableModel model = utils.getModelByBean("env_entries");
        utils.testTable(model, new String[][]{{"EnvName", "java.lang.Character", "EnvValue", "EnvDesc"}});
        EnvEntry[] entries = webapp.getEnvEntry();
        assertEquals("Wrong number of env entries", 1, entries.length);
        utils.testProperties(entries[0], new String[]{"EnvEntryName", "EnvEntryType", "EnvEntryValue"}, new Object[]{"EnvName", "java.lang.Character", "EnvValue"});
        assertEquals("EnvDesc", entries[0].getDefaultDescription());
    }

    public void testAddEnvEntry() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("env_entries");
        model.addRow(new Object[]{"newEnvName", "java.lang.Integer", "newEnvValue", "newEnvDesc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        EnvEntry envEntry = (EnvEntry) utils.getBeanByProp(webapp.getEnvEntry(), "EnvEntryName", "newEnvName");
        assertNotNull("New Env Entry is not added.", envEntry);
        utils.testProperties(envEntry, new String[]{"EnvEntryName", "EnvEntryType", "EnvEntryValue"}, new Object[]{"newEnvName", "java.lang.Integer", "newEnvValue"});
        assertEquals("newEnvDesc", envEntry.getDefaultDescription());
        utils.checkInDDXML(".*<env-entry>\\s*<description>newEnvDesc</description>\\s*<env-entry-name>newEnvName</env-entry-name>\\s*<env-entry-type>java.lang.Integer</env-entry-type>\\s*<env-entry-value>newEnvValue</env-entry-value>\\s*</env-entry>.*");
    }

    public void testModifyEnvEntry() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("env_entries");
        int row = utils.getRowIndexByProp(model, 0, "EnvName");
        assertTrue("Env entry not found in table model", row >= 0);
        utils.setTableRow(model, row, new Object[]{"EnvNameMod", "java.lang.Double", "EnvValueMod", "EnvDescMod"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<env-entry>\\s*<description>EnvDescMod</description>\\s*<env-entry-name>EnvNameMod</env-entry-name>\\s*<env-entry-type>java.lang.Double</env-entry-type>\\s*<env-entry-value>EnvValueMod</env-entry-value>\\s*</env-entry>.*");
            }
        };
        EnvEntry envEntry = (EnvEntry) utils.getBeanByProp(webapp.getEnvEntry(), "EnvEntryName", "EnvNameMod");
        assertNotNull("Env Entry not modified", envEntry);
        utils.testProperties(envEntry, new String[]{"EnvEntryName", "EnvEntryType", "EnvEntryValue"}, new Object[]{"EnvNameMod", "java.lang.Double", "EnvValueMod"});
        assertEquals("EnvDescMod", envEntry.getDefaultDescription());
        utils.checkNotInDDXML(".*<env-entry>.*<env-entry-name>EnvName</env-entry-name>.*</env-entry>.*");
        utils.checkInDDXML(".*<env-entry>\\s*<description>EnvDescMod</description>\\s*<env-entry-name>EnvNameMod</env-entry-name>\\s*<env-entry-type>java.lang.Double</env-entry-type>\\s*<env-entry-value>EnvValueMod</env-entry-value>\\s*</env-entry>.*");
    }

    public void testDelEnvEntry() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("env_entries");
        String entryName = (String) model.getValueAt(0, 0);
        model.removeRow(0);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        Object envEntry = utils.getBeanByProp(webapp.getEnvEntry(), "EnvEntryName", entryName);
        assertNull("Env Entry was not deleted", envEntry);
        utils.checkNotInDDXML(".*<env-entry>.*<env-entry-name>" + entryName + "</env-entry-name>.*</env-entry>.*");
    }

    public void testExistingResReferences() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_refs");
        utils.testTable(model, new String[][]{{"ResName", "javax.mail.Session", "Application", "Unshareable", "ResDesc"}});
        ResourceRef[] refs = webapp.getResourceRef();
        assertEquals("Wrong number of resource references", 1, refs.length);
        utils.testProperties(refs[0], new String[]{"ResRefName", "ResType", "ResAuth", "ResSharingScope"}, new Object[]{"ResName", "javax.mail.Session", "Application", "Unshareable"});
        assertEquals("ResDesc", refs[0].getDefaultDescription());
    }

    public void testAddResReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_refs");
        model.addRow(new Object[]{"newResName", "java.net.URL", "Container", "Shareable", "newResDesc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<resource-ref>\\s*<description>newResDesc</description>\\s*<res-ref-name>newResName</res-ref-name>\\s*<res-type>java.net.URL</res-type>\\s*<res-auth>Container</res-auth>\\s*<res-sharing-scope>Shareable</res-sharing-scope>\\s*</resource-ref>.*");
            }
        };
        ResourceRef ref = (ResourceRef) utils.getBeanByProp(webapp.getResourceRef(), "ResRefName", "newResName");
        assertNotNull("New Resource Ref is not added.", ref);
        utils.testProperties(ref, new String[]{"ResRefName", "ResType", "ResAuth", "ResSharingScope"}, new Object[]{"newResName", "java.net.URL", "Container", "Shareable"});
        assertEquals("newResDesc", ref.getDefaultDescription());
        utils.checkInDDXML(".*<resource-ref>\\s*<description>newResDesc</description>\\s*<res-ref-name>newResName</res-ref-name>\\s*<res-type>java.net.URL</res-type>\\s*<res-auth>Container</res-auth>\\s*<res-sharing-scope>Shareable</res-sharing-scope>\\s*</resource-ref>.*");
    }

    public void testModifyResReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_refs");
        int row = utils.getRowIndexByProp(model, 0, "ResName");
        assertTrue("Resource ref not found in table model", row >= 0);
        utils.setTableRow(model, row, new Object[]{"ResNameMod", "javax.sql.DataSource", "Container", "Shareable", "ResDescMod"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<resource-ref>\\s*<description>ResDescMod</description>\\s*<res-ref-name>ResNameMod</res-ref-name>\\s*<res-type>javax.sql.DataSource</res-type>\\s*<res-auth>Container</res-auth>\\s*<res-sharing-scope>Shareable</res-sharing-scope>\\s*</resource-ref>.*");
            }
        };
        utils.save();
        ResourceRef ref = (ResourceRef) utils.getBeanByProp(webapp.getResourceRef(), "ResRefName", "ResNameMod");
        assertNotNull("Resource ref not modified", ref);
        utils.testProperties(ref, new String[]{"ResRefName", "ResType", "ResAuth", "ResSharingScope"}, new Object[]{"ResNameMod", "javax.sql.DataSource", "Container", "Shareable"});
        assertEquals("ResDescMod", ref.getDefaultDescription());
        utils.checkNotInDDXML(".*<resource-ref>.*<res-ref-name>ResName</res-ref-name>.*</resource-ref>.*");
        utils.checkInDDXML(".*<resource-ref>\\s*<description>ResDescMod</description>\\s*<res-ref-name>ResNameMod</res-ref-name>\\s*<res-type>javax.sql.DataSource</res-type>\\s*<res-auth>Container</res-auth>\\s*<res-sharing-scope>Shareable</res-sharing-scope>\\s*</resource-ref>.*");
    }

    public void testDelResReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_refs");
        String resRefName = (String) model.getValueAt(0, 0);
        model.removeRow(0);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        Object resRef = utils.getBeanByProp(webapp.getResourceRef(), "ResRefName", resRefName);
        assertNull("Resource ref was not deleted", resRef);
        utils.checkNotInDDXML(".*<resource-ref>.*<res-ref-name>" + resRefName + "</res-ref-name>.*</resource-ref>.*");
    }

    public void testExistingResEnvReferences() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_env_refs");
        utils.testTable(model, new String[][]{{"ResEnvName", "javax.jms.Topic", "ResEnvdesc"}});
        ResourceEnvRef[] refs = webapp.getResourceEnvRef();
        assertEquals("Wrong number of resource references", 1, refs.length);
        utils.testProperties(refs[0], new String[]{"ResourceEnvRefName", "ResourceEnvRefType"}, new Object[]{"ResEnvName", "javax.jms.Topic"});
        assertEquals("ResEnvdesc", refs[0].getDefaultDescription());
    }

    public void testAddResEnvReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_env_refs");
        model.addRow(new Object[]{"newResEnvName", "javax.jms.Queue", "newResEnvDesc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<resource-env-ref>\\s*<description>newResEnvDesc</description>\\s*<resource-env-ref-name>newResEnvName</resource-env-ref-name>\\s*<resource-env-ref-type>javax.jms.Queue</resource-env-ref-type>\\s*</resource-env-ref>.*");
            }
        };
        ResourceEnvRef ref = (ResourceEnvRef) utils.getBeanByProp(webapp.getResourceEnvRef(), "ResourceEnvRefName", "newResEnvName");
        assertNotNull("New Env Resource Ref is not added.", ref);
        utils.testProperties(ref, new String[]{"ResourceEnvRefName", "ResourceEnvRefType"}, new Object[]{"newResEnvName", "javax.jms.Queue"});
        assertEquals("newResEnvDesc", ref.getDefaultDescription());
        utils.checkInDDXML(".*<resource-env-ref>\\s*<description>newResEnvDesc</description>\\s*<resource-env-ref-name>newResEnvName</resource-env-ref-name>\\s*<resource-env-ref-type>javax.jms.Queue</resource-env-ref-type>\\s*</resource-env-ref>.*");
    }

    public void testModifyResEnvReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_env_refs");
        int row = utils.getRowIndexByProp(model, 0, "ResEnvName");
        assertTrue("Resource env ref not found in table model", row >= 0);
        utils.setTableRow(model, row, new Object[]{"ResEnvNameMod", "javax.jms.Queue", "ResEnvDescMod"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<resource-env-ref>\\s*<description>ResEnvDescMod</description>\\s*<resource-env-ref-name>ResEnvNameMod</resource-env-ref-name>\\s*<resource-env-ref-type>javax.jms.Queue</resource-env-ref-type>\\s*</resource-env-ref>.*");
            }
        };
        utils.save();
        ResourceEnvRef ref = (ResourceEnvRef) utils.getBeanByProp(webapp.getResourceEnvRef(), "ResourceEnvRefName", "ResEnvNameMod");
        assertNotNull("Resource env ref not modified", ref);
        utils.testProperties(ref, new String[]{"ResourceEnvRefName", "ResourceEnvRefType"}, new Object[]{"ResEnvNameMod", "javax.jms.Queue"});
        assertEquals("ResEnvDescMod", ref.getDefaultDescription());
        utils.checkNotInDDXML(".*<resource-env-ref>.*<resource-env-ref-name>ResEnvName</resource-env-ref-name>.*</resource-env-ref>.*");
        utils.checkInDDXML(".*<resource-env-ref>\\s*<description>ResEnvDescMod</description>\\s*<resource-env-ref-name>ResEnvNameMod</resource-env-ref-name>\\s*<resource-env-ref-type>javax.jms.Queue</resource-env-ref-type>\\s*</resource-env-ref>.*");
    }

    public void testDelResEnvReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("res_env_refs");
        String resEnvRefName = (String) model.getValueAt(0, 0);
        model.removeRow(0);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        Object resRef = utils.getBeanByProp(webapp.getResourceEnvRef(), "ResourceEnvRefName", resEnvRefName);
        assertNull("Env. resource ref was not deleted", resRef);
        utils.checkNotInDDXML(".*<resource-env-ref>.*<resource-env-ref-name>" + resEnvRefName + "</resource-env-ref-name>.*</resource-env-ref>.*");
    }

    public void testExistingEJBReferences() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        String[][] vals = {{"EJBName2", "Session", "Remote", "EJBHome2", "EJBRemote", "LinkedEJB2", "EJBDesc2"},
            {"EJBName", "Entity", "Local", "EJBHome", "EJBLocal", "LinkedEJB", "EJBDesc"}};
        utils.testTable(model, vals);
        EjbLocalRef[] refs = webapp.getEjbLocalRef();
        assertEquals("Wrong number of EJB local references", 1, refs.length);
        utils.testProperties(refs[0], new String[]{"EjbRefName", "EjbRefType", "LocalHome", "Local", "EjbLink"}, new Object[]{"EJBName", "Entity", "EJBHome", "EJBLocal", "LinkedEJB"});
        assertEquals("EJBDesc", refs[0].getDefaultDescription());
        EjbRef[] refs2 = webapp.getEjbRef();
        assertEquals("Wrong number of EJB remote references", 1, refs.length);
        utils.testProperties(refs2[0], new String[]{"EjbRefName", "EjbRefType", "Home", "Remote", "EjbLink"}, new Object[]{"EJBName2", "Session", "EJBHome2", "EJBRemote", "LinkedEJB2"});
        assertEquals("EJBDesc2", refs2[0].getDefaultDescription());
    }

    public void testAddLocalEJBReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        model.addRow(new Object[]{"newEJBName", "Session", "Local", "newHome", "newLocal", "newLinked", "newDesc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<ejb-local-ref>\\s*<description>newDesc</description>\\s*<ejb-ref-name>newEJBName</ejb-ref-name>\\s*<ejb-ref-type>Session</ejb-ref-type>\\s*<local-home>newHome</local-home>\\s*<local>newLocal</local>\\s*<ejb-link>newLinked</ejb-link>\\s*</ejb-local-ref>.*");
            }
        };
        EjbLocalRef ref = (EjbLocalRef) utils.getBeanByProp(webapp.getEjbLocalRef(), "EjbRefName", "newEJBName");
        assertNotNull("New Local EJB Ref is not added.", ref);
        utils.testProperties(ref, new String[]{"EjbRefName", "EjbRefType", "LocalHome", "Local", "EjbLink"}, new Object[]{"newEJBName", "Session", "newHome", "newLocal", "newLinked"});
        assertEquals("newDesc", ref.getDefaultDescription());
        utils.checkInDDXML(".*<ejb-local-ref>\\s*<description>newDesc</description>\\s*<ejb-ref-name>newEJBName</ejb-ref-name>\\s*<ejb-ref-type>Session</ejb-ref-type>\\s*<local-home>newHome</local-home>\\s*<local>newLocal</local>\\s*<ejb-link>newLinked</ejb-link>\\s*</ejb-local-ref>.*");
    }

    public void testModifyLocalEJBReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        int row = utils.getRowIndexByProp(model, 0, "EJBName");
        assertTrue("Local EJB ref not found in table model", row >= 0);
        utils.setTableRow(model, row, new Object[]{"EJBNameMod", "Session", "", "EJBHomeMod", "EJBLocalMod", "LinkedEJBMod", "EJBDescMod"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<ejb-local-ref>\\s*<description>EJBDescMod</description>\\s*<ejb-ref-name>EJBNameMod</ejb-ref-name>\\s*<ejb-ref-type>Session</ejb-ref-type>\\s*<local-home>EJBHomeMod</local-home>\\s*<local>EJBLocalMod</local>\\s*<ejb-link>LinkedEJBMod</ejb-link>\\s*</ejb-local-ref>.*");
            }
        };
        utils.save();
        EjbLocalRef ref = (EjbLocalRef) utils.getBeanByProp(webapp.getEjbLocalRef(), "EjbRefName", "EJBNameMod");
        assertNotNull("Local EJB ref not modified", ref);
        utils.testProperties(ref, new String[]{"EjbRefName", "EjbRefType", "LocalHome", "Local", "EjbLink"}, new Object[]{"EJBNameMod", "Session", "EJBHomeMod", "EJBLocalMod", "LinkedEJBMod"});
        assertEquals("EJBDescMod", ref.getDefaultDescription());
        utils.checkNotInDDXML(".*<ejb-local-ref>.*<ejb-ref-name>EJBName</ejb-ref-name>.*</ejb-local-ref>.*");
        utils.checkInDDXML(".*<ejb-local-ref>\\s*<description>EJBDescMod</description>\\s*<ejb-ref-name>EJBNameMod</ejb-ref-name>\\s*<ejb-ref-type>Session</ejb-ref-type>\\s*<local-home>EJBHomeMod</local-home>\\s*<local>EJBLocalMod</local>\\s*<ejb-link>LinkedEJBMod</ejb-link>\\s*</ejb-local-ref>.*");
    }

    public void testDelLocalEJBReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        int row = utils.getRowIndexByProp(model, 2, "Local");
        assertTrue("No local EJB ref found in table model", row >= 0);
        String refName = (String) model.getValueAt(row, 0);
        model.removeRow(row);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        Object resRef = utils.getBeanByProp(webapp.getEjbLocalRef(), "EjbRefName", refName);
        assertNull("Local EJB ref was not deleted", resRef);
        utils.checkNotInDDXML(".*<ejb-local-ref>.*<ejb-ref-name>" + refName + "</ejb-ref-name>.*</ejb-local-ref>.*");
    }

    public void testAddRemoteEJBReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        model.addRow(new Object[]{"newEJBName2", "Session", "Remote", "newHome2", "newRemote", "newLinked2", "newDesc2"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<ejb-ref>\\s*<description>newDesc2</description>\\s*<ejb-ref-name>newEJBName2</ejb-ref-name>\\s*<ejb-ref-type>Session</ejb-ref-type>\\s*<home>newHome2</home>\\s*<remote>newRemote</remote>\\s*<ejb-link>newLinked2</ejb-link>\\s*</ejb-ref>.*");
            }
        };
        EjbRef ref = (EjbRef) utils.getBeanByProp(webapp.getEjbRef(), "EjbRefName", "newEJBName2");
        assertNotNull("New EJB Ref is not added.", ref);
        utils.testProperties(ref, new String[]{"EjbRefName", "EjbRefType", "Home", "Remote", "EjbLink"}, new Object[]{"newEJBName2", "Session", "newHome2", "newRemote", "newLinked2"});
        assertEquals("newDesc2", ref.getDefaultDescription());
        utils.checkInDDXML(".*<ejb-ref>\\s*<description>newDesc2</description>\\s*<ejb-ref-name>newEJBName2</ejb-ref-name>\\s*<ejb-ref-type>Session</ejb-ref-type>\\s*<home>newHome2</home>\\s*<remote>newRemote</remote>\\s*<ejb-link>newLinked2</ejb-link>\\s*</ejb-ref>.*");
    }

    public void testModifyRemoteEJBReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        int row = utils.getRowIndexByProp(model, 0, "EJBName2");
        assertTrue("EJB ref not found in table model", row >= 0);
        utils.setTableRow(model, row, new Object[]{"EJBName2Mod", "Entity", "", "EJBHome2Mod", "EJBRemoteMod", "LinkedEJB2Mod", "EJBDesc2Mod"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<ejb-ref>\\s*<description>EJBDesc2Mod</description>\\s*<ejb-ref-name>EJBName2Mod</ejb-ref-name>\\s*<ejb-ref-type>Entity</ejb-ref-type>\\s*<home>EJBHome2Mod</home>\\s*<remote>EJBRemoteMod</remote>\\s*<ejb-link>LinkedEJB2Mod</ejb-link>\\s*</ejb-ref>.*");
            }
        };
        utils.save();
        EjbRef ref = (EjbRef) utils.getBeanByProp(webapp.getEjbRef(), "EjbRefName", "EJBName2Mod");
        assertNotNull("EJB ref not modified", ref);
        utils.testProperties(ref, new String[]{"EjbRefName", "EjbRefType", "Home", "Remote", "EjbLink"}, new Object[]{"EJBName2Mod", "Entity", "EJBHome2Mod", "EJBRemoteMod", "LinkedEJB2Mod"});
        assertEquals("EJBDesc2Mod", ref.getDefaultDescription());
        utils.checkNotInDDXML(".*<ejb-ref>.*<ejb-ref-name>EJBName2</ejb-ref-name>.*</ejb-ref>.*");
        utils.checkInDDXML(".*<ejb-ref>\\s*<description>EJBDesc2Mod</description>\\s*<ejb-ref-name>EJBName2Mod</ejb-ref-name>\\s*<ejb-ref-type>Entity</ejb-ref-type>\\s*<home>EJBHome2Mod</home>\\s*<remote>EJBRemoteMod</remote>\\s*<ejb-link>LinkedEJB2Mod</ejb-link>\\s*</ejb-ref>.*");
    }

    public void testDelRemoteEJBReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("ejb_refs");
        int row = utils.getRowIndexByProp(model, 2, "Remote");
        assertTrue("No remote EJB ref found in table model", row >= 0);
        String refName = (String) model.getValueAt(row, 0);
        model.removeRow(row);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        Object resRef = utils.getBeanByProp(webapp.getEjbRef(), "EjbRefName", refName);
        assertNull("Remote EJB ref was not deleted", resRef);
        utils.checkNotInDDXML(".*<ejb-ref>.*<ejb-ref-name>" + refName + "</ejb-ref-name>.*</ejb-ref>.*");
    }

    public void testExistingMsgDstReferences() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("message_dest_refs");
        utils.testTable(model, new String[][]{{"MsgName", "javax.jms.Topic", "Produces", "MsgLink", "MsgDesc"}});
        MessageDestinationRef[] refs = webapp.getMessageDestinationRef();
        assertEquals("Wrong number of message dst references", 1, refs.length);
        utils.testProperties(refs[0], new String[]{"MessageDestinationRefName", "MessageDestinationType", "MessageDestinationUsage", "MessageDestinationLink"}, new Object[]{"MsgName", "javax.jms.Topic", "Produces", "MsgLink"});
        assertEquals("MsgDesc", refs[0].getDefaultDescription());
    }

    public void testAddMsgDstReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("message_dest_refs");
        model.addRow(new Object[]{"newMsgName", "javax.jms.Queue", "Consumes", "newMsgLink", "newMsgDesc"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<message-destination-ref>\\s*<description>newMsgDesc</description>\\s*<message-destination-ref-name>newMsgName</message-destination-ref-name>\\s*<message-destination-type>javax.jms.Queue</message-destination-type>\\s*<message-destination-usage>Consumes</message-destination-usage>\\s*<message-destination-link>newMsgLink</message-destination-link>\\s*</message-destination-ref>.*");
            }
        };
        MessageDestinationRef ref = (MessageDestinationRef) utils.getBeanByProp(webapp.getMessageDestinationRef(), "MessageDestinationRefName", "newMsgName");
        assertNotNull("Message dst ref is not added.", ref);
        utils.testProperties(ref, new String[]{"MessageDestinationRefName", "MessageDestinationType", "MessageDestinationUsage", "MessageDestinationLink"}, new Object[]{"newMsgName", "javax.jms.Queue", "Consumes", "newMsgLink"});
        assertEquals("newMsgDesc", ref.getDefaultDescription());
        utils.checkInDDXML(".*<message-destination-ref>\\s*<description>newMsgDesc</description>\\s*<message-destination-ref-name>newMsgName</message-destination-ref-name>\\s*<message-destination-type>javax\\.jms\\.Queue</message-destination-type>\\s*<message-destination-usage>Consumes</message-destination-usage>\\s*<message-destination-link>newMsgLink</message-destination-link>\\s*</message-destination-ref>.*");
    }

    public void testModifyMsgDstReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("message_dest_refs");
        int row = utils.getRowIndexByProp(model, 0, "MsgName");
        assertTrue("Message dst ref not found in table model", row >= 0);
        utils.setTableRow(model, row, new Object[]{"MsgNameMod", "javax.jms.Queue", "ConsumesProduces", "MsgLinkMod", "MsgDescMod"});
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<message-destination-ref>\\s*<description>MsgDescMod</description>\\s*<message-destination-ref-name>MsgNameMod</message-destination-ref-name>\\s*<message-destination-type>javax.jms.Queue</message-destination-type>\\s*<message-destination-usage>ConsumesProduces</message-destination-usage>\\s*<message-destination-link>MsgLinkMod</message-destination-link>\\s*</message-destination-ref>.*");
            }
        };
        utils.save();
        MessageDestinationRef ref = (MessageDestinationRef) utils.getBeanByProp(webapp.getMessageDestinationRef(), "MessageDestinationRefName", "MsgNameMod");
        assertNotNull("EJB ref not modified", ref);
        utils.testProperties(ref, new String[]{"MessageDestinationRefName", "MessageDestinationType", "MessageDestinationUsage", "MessageDestinationLink"}, new Object[]{"MsgNameMod", "javax.jms.Queue", "ConsumesProduces", "MsgLinkMod"});
        assertEquals("MsgDescMod", ref.getDefaultDescription());
        utils.checkNotInDDXML(".*<message-destination-ref>.*<message-destination-ref-name>MsgName</message-destination-ref-name>.*</message-destination-ref>.*");
        utils.checkInDDXML(".*<message-destination-ref>\\s*<description>MsgDescMod</description>\\s*<message-destination-ref-name>MsgNameMod</message-destination-ref-name>\\s*<message-destination-type>javax.jms.Queue</message-destination-type>\\s*<message-destination-usage>ConsumesProduces</message-destination-usage>\\s*<message-destination-link>MsgLinkMod</message-destination-link>\\s*</message-destination-ref>.*");
    }

    public void testDelMsgDstReference() throws Exception {
        DDBeanTableModel model = utils.getModelByBean("message_dest_refs");
        String refName = (String) model.getValueAt(0, 0);
        model.removeRow(0);
        ddObj.modelUpdatedFromUI();
        DDTestUtils.waitForDispatchThread();
        utils.save();
        Object resRef = utils.getBeanByProp(webapp.getMessageDestinationRef(), "MessageDestinationRefName", refName);
        assertNull("Message dst ref was not deleted", resRef);
        utils.checkNotInDDXML(".*<message-destination-ref>.*<message-destination-ref-name>" + refName + "</message-destination-ref-name>.*</message-destination-ref>.*");
    }
}
