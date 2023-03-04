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
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.j2ee.dd.api.common.InitParam;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Filter;
import org.netbeans.modules.j2ee.dd.api.web.FilterMapping;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.SessionConfig;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.ddloaders.web.DDDataObject;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.ContextParamsTablePanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.DDBeanTableModel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.FilterMappingsTablePanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.FilterParamsPanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.InitParamsPanel;
import org.netbeans.modules.j2ee.ddloaders.web.multiview.ListenersTablePanel;
import org.netbeans.modules.web.project.WebProject;
import org.netbeans.test.j2ee.lib.J2eeProjectSupport;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;

/**
 *
 * @author jp159440
 */
public class WebProjectDDTest extends J2eeTestCase {

    /** Creates a new instance of WebProjectDDTest */
    public WebProjectDDTest(String testName) {
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
    public static String[] webprojectddtests = {
        "testOpenProject", "testValuesInOverview", "testModificationByApi", "testModificationInDesign",
        "testExistingContextParameters", "testAddContextParameters", "testModifyContextParameter",
        "testDelContextParameter", "testExistingListeners", "testAddListener", "testModifyListener",
        "testDelListener", "testExistingServlets", "testModifyServlet", "testAddServletParam",
        "testModifyServletParam", "testDelServletParam", "testExistingFilters", "testModifyFilter",
        "testAddFilterParam", "testModifyFilterParam", "testDelFilterParam", "testExistingFilterMappings",
        "testAddFilterMapping", "testFilterNameChangePropagation", "testServletNameChangePropagation",
        "testModifyFilterMapping", "testDelFilterMapping"};
    public static String[] pagesandreferencesddtests = {
        "testOpenProject", "testExistingWelcomePages", "testAddWelcomePage", "testDelWelcomePage",
        "testExistingErrorPages", "testAddErrorPage", "testModifyErrorPage", "testDelErrorPage",
        "testExistingPropertyGroups", "testModifyPropertyGroup", "testDelPropertyGroup",
        "testExistingEnvEntries", "testAddEnvEntry", "testModifyEnvEntry", "testDelEnvEntry",
        "testExistingResReferences", "testAddResReference", "testModifyResReference",
        "testDelResReference", "testExistingResEnvReferences", "testAddResEnvReference",
        "testModifyResEnvReference", "testDelResEnvReference", "testExistingEJBReferences",
        "testAddLocalEJBReference", "testModifyLocalEJBReference", "testDelLocalEJBReference",
        "testAddRemoteEJBReference", "testModifyRemoteEJBReference", "testDelRemoteEJBReference",
        "testExistingMsgDstReferences", "testAddMsgDstReference", "testModifyMsgDstReference",
        "testDelMsgDstReference"};
    public static String[] securityddtests = {
        "testOpenProject", "testExistingLoginConfiguration", "testExistingSecurityRoles",
        "testAddSecurityRole", "testEditSecurityRole", "testDelSecurityRole"};

    public static Test suite() {

        NbModuleSuite.Configuration conf = emptyConfiguration();
        conf = addServerTests(Server.GLASSFISH, conf, WebProjectDDTest.class, webprojectddtests);
        conf = addServerTests(Server.ANY, conf, PagesAndReferencesDDTest.class, pagesandreferencesddtests);
        conf = addServerTests(Server.ANY, conf, SecurityDDTest.class, securityddtests);
        return conf.suite();
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
        ddObj.openView(2); // open General view
        utils = new DDTestUtils(ddObj, this);
        Utils.waitForAWTDispatchThread();
    }

    public void testValuesInOverview() throws Exception {
        //test general panel
        assertEquals("Default display name does not match", "DisplayName", webapp.getDefaultDisplayName());
        assertEquals("Default description does not match", "Description", webapp.getDefaultDescription());
        assertEquals("Distributable not set", true, webapp.isDistributable());
        utils.checkInDDXML(".*<distributable/>.*");
        utils.checkInDDXML(".*<session-timeout>\\s*30\\s*</session-timeout>.*");
    }

    public void testModificationByApi() throws Exception {
        //test modify general panel
        webapp.setDisplayName("ModifiedDisplayName");
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        utils.checkInDDXML(".*<display-name>ModifiedDisplayName</display-name>.*");

        webapp.setDescription("Modified Description");
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        utils.checkInDDXML(".*<description>Modified Description</description>.*");

        webapp.setDistributable(false);
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        utils.checkNotInDDXML(".*<distributable/>.*");

        SessionConfig sessionConf = webapp.getSingleSessionConfig();
        sessionConf.setSessionTimeout(new java.math.BigInteger("25"));
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        utils.checkInDDXML(".*<session-timeout>\\s*25\\s*</session-timeout>.*");
    }

    public void testModificationInDesign() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("overview");
        Component[] comp = panel.getComponents();
        panel.requestFocus();
        DDTestUtils.waitForDispatchThread();
        utils.setText(((JTextField) comp[3]), "dispname");
        ((JTextField) comp[8]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<display-name>dispname</display-name>.*");
            }

            @Override
            public void finalCheck() {
                assertEquals("Display name not set", "dispname", webapp.getDefaultDisplayName());
                utils.checkInDDXML(".*<display-name>dispname</display-name>.*");
            }
        };
        utils.setText((JTextArea) comp[5], "descript");
        ((JCheckBox) comp[6]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<description>descript</description>.*");
            }

            @Override
            public void finalCheck() {
                assertEquals("Description not set", "descript", webapp.getDefaultDescription());
                utils.checkInDDXML(".*<description>descript</description>.*");
            }
        };
        ((JCheckBox) comp[6]).setSelected(true);
        ddObj.modelUpdatedFromUI();
        ((JTextField) comp[8]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<distributable/>.*");
            }

            @Override
            public void finalCheck() {
                assertTrue("Distributable not set to true", webapp.isDistributable());
                utils.checkInDDXML(".*<distributable/>.*");
            }
        };
        utils.setText((JTextField) comp[8], "26");
        ((JTextField) comp[3]).requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<session-timeout>\\s*26\\s*</session-timeout>.*");
            }

            @Override
            public void finalCheck() {
                assertEquals("Session timeout not set", new java.math.BigInteger("26"), webapp.getSingleSessionConfig().getSessionTimeout());
                utils.checkInDDXML(".*<session-timeout>\\s*26\\s*</session-timeout>.*");
            }
        };
        utils.save();
    }

    public void testExistingContextParameters() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("context_params");
        InitParam[] params = webapp.getContextParam();
        assertEquals("Wrong parameters count in model", 1, params.length);
        InitParam param = params[0];
        utils.testProperties(param, new String[]{"ParamName", "ParamValue"}, new Object[]{"contextparamname", "contextparamvalue"});
        assertEquals("Unexpected parameter description", "contextparamdesc", param.getDefaultDescription());
        //table model
        Component[] comp = panel.getComponents();
        ContextParamsTablePanel contextPanel = (ContextParamsTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) contextPanel.getModel();
        assertEquals("Wrong parameters count in table model", 1, model.getRowCount());
        utils.testTableRow(model, 0, new String[]{"contextparamname", "contextparamvalue", "contextparamdesc"});
        //test XML
        utils.checkInDDXML(".*<context-param>\\s*<description>contextparamdesc</description>\\s*<param-name>contextparamname</param-name>\\s*<param-value>contextparamvalue</param-value>\\s*</context-param>.*");
    }

    public void testAddContextParameters() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("context_params");
        Component[] comp = panel.getComponents();
        ContextParamsTablePanel contextPanel = (ContextParamsTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) contextPanel.getModel();
        model.addRow(new String[]{"newname", "newval", "newdes"});
        ddObj.modelUpdatedFromUI();
        //table model
        int i = 0;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newname")) {
                break;
            }
        }
        assertFalse("Parameter not added to table model", i == model.getRowCount());
        assertEquals("Unexpected parameter value", "newval", model.getValueAt(i, 1));
        assertEquals("Unexpected parameter description", "newdes", model.getValueAt(i, 2));
        //xml
        Thread.sleep(2000);
        utils.checkInDDXML(".*<context-param>\\s*<description>newdes</description>\\s*<param-name>newname</param-name>\\s*<param-value>newval</param-value>\\s*</context-param>.*");
        utils.save();
    }

    public void testModifyContextParameter() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("context_params");
        Component[] comp = panel.getComponents();
        ContextParamsTablePanel contextPanel = (ContextParamsTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) contextPanel.getModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newname")) {
                break;
            }
        }
        assertFalse("Parameter not found in table model", i == model.getRowCount());
        model.setValueAt("newparamname", i, 0);
        ddObj.modelUpdatedFromUI();
        assertEquals("Parameter name not updated in table model", "newparamname", model.getValueAt(i, 0));

        model.setValueAt("newparamvalue", i, 1);
        ddObj.modelUpdatedFromUI();
        assertEquals("Parameter value not updated in table model", "newparamvalue", model.getValueAt(i, 1));

        model.setValueAt("newparamdesc", i, 2);
        ddObj.modelUpdatedFromUI();
        assertEquals("Parameter description not updated in table model", "newparamdesc", model.getValueAt(i, 2));
        Thread.sleep(2000);
        utils.checkInDDXML(".*<context-param>\\s*<description>newparamdesc</description>\\s*<param-name>newparamname</param-name>\\s*<param-value>newparamvalue</param-value>\\s*</context-param>.*");
        utils.save();
    }

    public void testDelContextParameter() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("context_params");
        Component[] comp = panel.getComponents();
        ContextParamsTablePanel contextPanel = (ContextParamsTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) contextPanel.getModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparamname")) {
                break;
            }
        }
        assertFalse("Parameter not found in table model", i == model.getRowCount());
        model.removeRow(i);
        ddObj.modelUpdatedFromUI();
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparamname")) {
                break;
            }
        }
        assertTrue("Parameter not deleted from in table model", i == model.getRowCount());
        Thread.sleep(2000);
        utils.checkNotInDDXML(".*<context-param>.*<param-name>newparamname</param-name>.*</context-param>.*");
        utils.save();
    }

    public void testExistingListeners() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("listeners");
        Listener[] list = webapp.getListener();
        assertEquals("Wrong listeners count in model", 1, list.length);
        Listener listener = list[0];
        assertEquals("Unexpected listener class", "listenerclass", listener.getListenerClass());
        assertEquals("Unexpected listener description", "listenerdescription", listener.getDefaultDescription());
        //table model
        Component[] comp = panel.getComponents();
        ListenersTablePanel listPanel = (ListenersTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) listPanel.getModel();
        assertEquals("Wrong listeners count in table model", 1, model.getRowCount());
        utils.testTableRow(model, 0, new String[]{"listenerclass", "listenerdescription"});
        //test XML
        utils.checkInDDXML(".* <listener>\\s*<description>listenerdescription</description>\\s*<listener-class>listenerclass</listener-class>\\s*</listener>.*");
    }

    public void testAddListener() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("listeners");
        Component[] comp = panel.getComponents();
        ListenersTablePanel listPanel = (ListenersTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) listPanel.getModel();
        model.addRow(new String[]{"newlist", "newlistdesc"});
        ddObj.modelUpdatedFromUI();
        Listener[] list = webapp.getListener();
        int i;
        for (i = 0; i < list.length; i++) {
            if (list[i].getListenerClass().equals("newlist")) {
                break;
            }
        }
        assertFalse("New listener not added", i == list.length);
        assertEquals("Listener class does not match", "newlist", list[i].getListenerClass());
        assertEquals("Listener description does not match", "newlistdesc", list[i].getDefaultDescription());
        Thread.sleep(2000);
        utils.checkInDDXML(".*<listener>\\s*<description>newlistdesc</description>\\s*<listener-class>newlist</listener-class>\\s*</listener>.*");
    }

    public void testModifyListener() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("listeners");
        Component[] comp = panel.getComponents();
        ListenersTablePanel listPanel = (ListenersTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) listPanel.getModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newlist")) {
                break;
            }
        }
        assertFalse("Listener not found", i == model.getRowCount());
        model.setValueAt("newlistmod", i, 0);
        model.setValueAt("newlistdescmod", i, 1);
        ddObj.modelUpdatedFromUI();
        Listener[] list = webapp.getListener();
        for (i = 0; i < list.length; i++) {
            if (list[i].getListenerClass().equals("newlistmod")) {
                break;
            }
        }
        assertFalse("Listener not modified", i == list.length);
        assertEquals("Description not changed", "newlistdescmod", list[i].getDefaultDescription());
        Thread.sleep(2000);
        utils.checkInDDXML(".*<listener>\\s*<description>newlistdescmod</description>\\s*<listener-class>newlistmod</listener-class>\\s*</listener>.*");
    }

    public void testDelListener() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("listeners");
        Component[] comp = panel.getComponents();
        ListenersTablePanel listPanel = (ListenersTablePanel) comp[1];
        DDBeanTableModel model = (DDBeanTableModel) listPanel.getModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newlistmod")) {
                break;
            }
        }
        assertFalse("Listener not found", i == model.getRowCount());
        model.removeRow(i);
        ddObj.modelUpdatedFromUI();
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newlistmod")) {
                break;
            }
        }
        assertTrue("Listener not deleted from in table model", i == model.getRowCount());
        Thread.sleep(2000);
        utils.checkNotInDDXML(".* <listener>\\s*<description>newlistdescmod</description>\\s*<listener-class>mewlistmod</listener-class>\\s*</listener>.*");
        utils.save();
    }

    public void testExistingServlets() throws Exception {
        ddObj.openView(4); // open Servlets view
        Utils.waitForAWTDispatchThread();
        Servlet[] servlets = webapp.getServlet();
        assertEquals("Wrong count of servlets", 1, servlets.length);
        JPanel panel = utils.getInnerSectionPanel(servlets[0]);
        Component[] comp = panel.getComponents();
        assertEquals("Wrong servlet name", "ServletName", ((JTextField) comp[1]).getText());
        assertEquals("Wrong startup order", "", ((JTextField) comp[3]).getText());
        assertEquals("Wrong servlet description", "ServletDescription", ((JTextArea) comp[5]).getText());
        assertEquals("Wrong servlet source type", true, ((JRadioButton) comp[6]).isSelected());
        assertEquals("Wrong servlet class", "ServletClass", ((JTextField) comp[7]).getText());
        assertEquals("Wrong servlet source type", false, ((JRadioButton) comp[9]).isSelected());
        assertEquals("Wrong servlet pattern", "*", ((JTextField) comp[13]).getText());
        InitParamsPanel tablePanel = ((InitParamsPanel) comp[17]);
        DDBeanTableModel model = (DDBeanTableModel) tablePanel.getTable().getModel();
        assertEquals("Wrong count of init parameters", 1, model.getRowCount());
        utils.testTableRow(model, 0, new String[]{"ServletParam", "ServletParamVal", "ServletParamDesc"});

        Servlet servlet = servlets[0];
        utils.testProperties(servlet, new String[]{"ServletName", "LoadOnStartup", "ServletClass"}, new Object[]{"ServletName", null, "ServletClass"});
        ServletMapping[] mappings = webapp.getServletMapping();
        assertEquals("Wrong servlet mapping", 1, mappings.length);
        utils.testProperties(mappings[0], new String[]{"ServletName", "UrlPattern"}, new Object[]{"ServletName", "*"});
        assertEquals("Description does not match", "ServletDescription", servlet.getDefaultDescription());
        InitParam[] params = servlet.getInitParam();
        assertEquals("Wrong number of init params", 1, params.length);
        utils.testProperties(params[0], new String[]{"ParamName", "ParamValue"}, new Object[]{"ServletParam", "ServletParamVal"});
        assertEquals("Description does not match", "ServletParamDesc", params[0].getDefaultDescription());
    }

    public void testModifyServlet() throws Exception {
        Servlet[] servlets = webapp.getServlet();
        assertEquals("Wrong count of servlets", 1, servlets.length);
        JPanel panel = utils.getInnerSectionPanel(servlets[0]);
        Component[] comp = panel.getComponents();
        utils.setText((JTextField) comp[1], "ServletNameMod");
        comp[3].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<servlet>.*<servlet-name>ServletNameMod</servlet-name>.*</servlet>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<servlet>.*<servlet-name>ServletNameMod</servlet-name>.*</servlet>.*");
            }
        };
        utils.setText((JTextField) comp[3], "1");
        comp[5].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<servlet>.*<load-on-startup>1</load-on-startup>.*</servlet>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<servlet>.*<load-on-startup>1</load-on-startup>.*</servlet>.*");
            }
        };
        utils.setText((JTextArea) comp[5], "ServletDescriptionMod");
        comp[9].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<servlet>\\s*<description>ServletDescriptionMod</description>.*</servlet>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<servlet>\\s*<description>ServletDescriptionMod</description>.*</servlet>.*");
            }
        };
        ((JRadioButton) comp[9]).setSelected(true);
        comp[10].requestFocus();
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        utils.setText((JTextField) comp[10], "JSPFile.jsp");
        comp[13].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<servlet>.*<jsp-file>JSPFile.jsp</jsp-file>.*</servlet>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<servlet>.*<jsp-file>JSPFile.jsp</jsp-file>.*</servlet>.*");
            }
        };
        utils.setText((JTextField) comp[13], "/*");
        comp[1].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<servlet-mapping>\\s*<servlet-name>ServletNameMod</servlet-name>\\s*<url-pattern>/\\*</url-pattern>\\s*</servlet-mapping>.*");
            }

            @Override
            public void finalCheck() {
                utils.checkInDDXML(".*<servlet-mapping>\\s*<servlet-name>ServletNameMod</servlet-name>\\s*<url-pattern>/\\*</url-pattern>\\s*</servlet-mapping>.*");
            }
        };
        Servlet servlet = servlets[0];
        utils.testProperties(servlet, new String[]{"ServletName", "LoadOnStartup", "JspFile"}, new Object[]{"ServletNameMod", new java.math.BigInteger("1"), "JSPFile.jsp"});
        ServletMapping[] mappings = webapp.getServletMapping();
        assertEquals("Wrong servlet mapping", 1, mappings.length);
        utils.testProperties(mappings[0], new String[]{"ServletName", "UrlPattern"}, new Object[]{"ServletNameMod", "/*"});
        utils.checkInDDXML(".*<servlet>\\s*<description>ServletDescriptionMod</description>\\s*<servlet-name>ServletNameMod</servlet-name>\\s*<jsp-file>JSPFile.jsp</jsp-file>.*<load-on-startup>1</load-on-startup>\\s*</servlet>.*");
        utils.checkInDDXML(".*<servlet-mapping>\\s*<servlet-name>ServletNameMod</servlet-name>\\s*<url-pattern>/\\*</url-pattern>\\s*</servlet-mapping>.*");
        utils.save();
    }

    public void testAddServletParam() throws Exception {
        Servlet[] servlets = webapp.getServlet();
        DDBeanTableModel model = utils.getServletInitParamsTableModel();
        model.addRow(new String[]{"newparam", "newval", "newdesc"});
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        InitParam[] params = servlets[0].getInitParam();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparam")) {
                break;
            }
        }
        assertFalse("New parameter is not added", i == model.getRowCount());
        utils.testTableRow(model, i, new String[]{"newparam", "newval", "newdesc"});
        for (i = 0; i < params.length; i++) {
            if (params[i].getParamName().equals("newparam")) {
                break;
            }
        }
        assertFalse("New parameter is not added", i == params.length);
        utils.testProperties(params[i], new String[]{"ParamName", "ParamValue"}, new Object[]{"newparam", "newval"});
        assertEquals("Description does not match", "newdesc", params[i].getDefaultDescription());
        utils.checkInDDXML(".*<servlet>.*<init-param>\\s*<description>newdesc</description>\\s*<param-name>newparam</param-name>\\s*<param-value>newval</param-value>\\s*</init-param>.*</servlet>.*");
        utils.save();
    }

    public void testModifyServletParam() throws Exception {
        Servlet[] servlets = webapp.getServlet();
        DDBeanTableModel model = utils.getServletInitParamsTableModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparam")) {
                break;
            }
        }
        assertFalse("Parameter is not found", i == model.getRowCount());
        model.setValueAt("newparammod", i, 0);
        model.setValueAt("newvalmod", i, 1);
        model.setValueAt("newdescmod", i, 2);
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparammod")) {
                break;
            }
        }
        assertFalse("Parameter is not modified", i == model.getRowCount());
        utils.testTableRow(model, i, new String[]{"newparammod", "newvalmod", "newdescmod"});
        InitParam[] params = servlets[0].getInitParam();
        for (i = 0; i < params.length; i++) {
            if (params[i].getParamName().equals("newparammod")) {
                break;
            }
        }
        assertFalse("Parameter is not modified", i == params.length);
        utils.testProperties(params[i], new String[]{"ParamName", "ParamValue"}, new Object[]{"newparammod", "newvalmod"});
        assertEquals("Description does not match", "newdescmod", params[i].getDefaultDescription());
        utils.checkInDDXML(".*<servlet>.*<init-param>\\s*<description>newdescmod</description>\\s*<param-name>newparammod</param-name>\\s*<param-value>newvalmod</param-value>\\s*</init-param>.*</servlet>.*");
        utils.save();
    }

    public void testDelServletParam() throws Exception {
        Servlet[] servlets = webapp.getServlet();
        DDBeanTableModel model = utils.getServletInitParamsTableModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparammod")) {
                break;
            }
        }
        assertFalse("Parameter is not found", i == model.getRowCount());
        model.removeRow(i);
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newparammod")) {
                break;
            }
        }
        assertTrue("Parameter is not deleted", i == model.getRowCount());
        InitParam[] params = servlets[0].getInitParam();
        for (i = 0; i < params.length; i++) {
            if (params[i].getParamName().equals("newparammod")) {
                break;
            }
        }
        assertTrue("Parameter is not deleted", i == params.length);
        utils.checkNotInDDXML(".*<servlet>.*<init-param>\\s*<description>newdescmod</description>\\s*<param-name>newparammod</param-name>\\s*<param-value>newvalmod</param-value>\\s*</init-param>.*</servlet>.*");
        utils.save();
    }

    public void testExistingFilters() throws Exception {
        ddObj.openView(6); // open Filters view
        Utils.waitForAWTDispatchThread();
        Filter[] filters = webapp.getFilter();
        assertEquals("Unexpected filter count", 1, filters.length);
        Filter filter = filters[0];
        ddObj.showElement(filter);
        JPanel panel = utils.getInnerSectionPanel(filter);
        Component[] comp = panel.getComponents();
        assertEquals("Filter name does not match", "FilterName", ((JTextField) comp[1]).getText());
        assertEquals("Filter description does not match", "FilterDescription", ((JTextArea) comp[3]).getText());
        assertEquals("Filter class does not match", "FilterClass", ((JTextField) comp[5]).getText());
        DDBeanTableModel model = (DDBeanTableModel) ((FilterParamsPanel) comp[9]).getModel();
        assertEquals("Unexpected filter params count", 1, model.getRowCount());
        utils.testTableRow(model, 0, new String[]{"FilterParamName", "FilterParamValue", "FilterParamDescription"});
        utils.testProperties(filter, new String[]{"FilterName", "FilterClass"}, new Object[]{"FilterName", "FilterClass"});
        assertEquals("Description does not match", "FilterDescription", filter.getDefaultDescription());
        assertEquals("Unexpected conut of filter init params", 1, filter.getInitParam().length);
        utils.testProperties(filter.getInitParam(0), new String[]{"ParamName", "ParamValue"}, new Object[]{"FilterParamName", "FilterParamValue"});
        assertEquals("Filter param description does not match.", "FilterParamDescription", filter.getInitParam(0).getDefaultDescription());
    }

    public void testModifyFilter() throws Exception {
        Filter[] filters = webapp.getFilter();
        assertEquals("Unexpected filter count", 1, filters.length);
        Filter filter = filters[0];
        ddObj.showElement(filter);
        JPanel panel = utils.getInnerSectionPanel(filter);
        Component[] comp = panel.getComponents();
        utils.setText((JTextField) comp[1], "FilterNameMod");
        comp[3].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<filter-name>FilterNameMod</filter-name>.*");
            }
        };
        utils.setText((JTextArea) comp[3], "FilterDescriptionMod");
        comp[5].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<description>FilterDescriptionMod</description>.*");
            }
        };
        utils.setText((JTextField) comp[5], "FilterClassMod");
        comp[1].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<filter-class>FilterClassMod</filter-class>.*");
            }
        };
        filter = webapp.getFilter(0);
        utils.testProperties(filter, new String[]{"FilterName", "FilterClass"}, new Object[]{"FilterNameMod", "FilterClassMod"});
        assertEquals("Description not updated.", "FilterDescriptionMod", filter.getDefaultDescription());
        utils.checkInDDXML(".*<description>FilterDescriptionMod</description>\\s*<filter-name>FilterNameMod</filter-name>\\s*<filter-class>FilterClassMod</filter-class>.*");
        utils.save();
    }

    public void testAddFilterParam() throws Exception {
        Filter[] filters = webapp.getFilter();
        DDBeanTableModel model = utils.getFilterInitParamsTableModel();
        model.addRow(new String[]{"newfilterparam", "newfilterparamval", "newfilterparamdesc"});
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        InitParam[] params = filters[0].getInitParam();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newfilterparam")) {
                break;
            }
        }
        assertFalse("New parameter is not added", i == model.getRowCount());
        utils.testTableRow(model, i, new String[]{"newfilterparam", "newfilterparamval", "newfilterparamdesc"});
        for (i = 0; i < params.length; i++) {
            if (params[i].getParamName().equals("newfilterparam")) {
                break;
            }
        }
        assertFalse("New parameter is not added", i == params.length);
        utils.testProperties(params[i], new String[]{"ParamName", "ParamValue"}, new Object[]{"newfilterparam", "newfilterparamval"});
        assertEquals("Description does not match", "newfilterparamdesc", params[i].getDefaultDescription());
        utils.checkInDDXML(".*<filter>.*<init-param>\\s*<description>newfilterparamdesc</description>\\s*<param-name>newfilterparam</param-name>\\s*<param-value>newfilterparamval</param-value>\\s*</init-param>.*</filter>.*");
        utils.save();
    }

    public void testModifyFilterParam() throws Exception {
        Filter[] filters = webapp.getFilter();
        DDBeanTableModel model = utils.getFilterInitParamsTableModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newfilterparam")) {
                break;
            }
        }
        assertFalse("Parameter not found", i == model.getRowCount());
        utils.setTableRow(model, i, new Object[]{"newfilterparammod", "newfilterparamvalmod", "newfilterparamdescmod"});
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        InitParam[] params = filters[0].getInitParam();
        for (i = 0; i < params.length; i++) {
            if (params[i].getParamName().equals("newfilterparammod")) {
                break;
            }
        }
        assertFalse("Parameter not modified", i == params.length);
        utils.testProperties(params[i], new String[]{"ParamName", "ParamValue"}, new Object[]{"newfilterparammod", "newfilterparamvalmod"});
        assertEquals("Filter param description not modified", "newfilterparamdescmod", params[i].getDefaultDescription());
        utils.checkInDDXML(".*<filter>.*<init-param>\\s*<description>newfilterparamdescmod</description>\\s*<param-name>newfilterparammod</param-name>\\s*<param-value>newfilterparamvalmod</param-value>\\s*</init-param>.*</filter>.*");
        utils.save();
    }

    public void testDelFilterParam() throws Exception {
        Filter[] filters = webapp.getFilter();
        DDBeanTableModel model = utils.getFilterInitParamsTableModel();
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals("newfilterparammod")) {
                break;
            }
        }
        assertFalse("Parameter not found", i == model.getRowCount());
        model.removeRow(i);
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        InitParam[] params = filters[0].getInitParam();
        for (i = 0; i < params.length; i++) {
            if (params[i].getParamName().equals("newfilterparammod")) {
                break;
            }
        }
        assertTrue("Parameter not deleted", i == params.length);
        utils.checkNotInDDXML(".*<filter>.*<init-param>\\s*<description>newfilterparamdescmod</description>\\s*<param-name>newfilterparammod</param-name>\\s*<param-value>newfilterparamvalmod</param-value>\\s*</init-param>.*</filter>.*");
        utils.save();
    }

    public void testExistingFilterMappings() throws Exception {
        FilterMapping[] mappings = webapp.getFilterMapping();
        assertTrue("Wrong number of filter mappings", mappings.length == 1);
        JPanel panel = utils.getInnerSectionPanel("filter_mappings");
        utils.testProperties(mappings[0], new String[]{"FilterName", "UrlPattern", "ServletName"}, new Object[]{"MappedFilterName", "/*", null});
        String[] exDisp = {"REQUEST", "FORWARD", "INCLUDE", "ERROR"};
        String[] actDisp = mappings[0].getDispatcher();
        assertEquals("Wrong count of dispatchers types", exDisp.length, actDisp.length);
        for (int i = 0; i < exDisp.length; i++) {
            assertEquals("Wrong dispatcher type.", exDisp[i], actDisp[i]);
        }
        DDBeanTableModel model = (DDBeanTableModel) ((FilterMappingsTablePanel) panel.getComponent(1)).getTable().getModel();
        assertEquals("Wrong filter mapping count.", 1, model.getRowCount());
        utils.testTableRow(model, 0, new String[]{"MappedFilterName", "/* (URL)", "REQUEST, FORWARD, INCLUDE, ERROR"});
    }

    public void testAddFilterMapping() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("filter_mappings");
        DDBeanTableModel model = (DDBeanTableModel) ((FilterMappingsTablePanel) panel.getComponent(1)).getTable().getModel();
        String filterName = webapp.getFilter(0).getFilterName();
        String servletName = webapp.getServlet(0).getServletName();
        model.addRow(new Object[]{filterName, null, new String[]{servletName}, new String[]{"REQUEST", "ERROR"}});
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        //check table model
        int i;
        assertEquals("Mapping was not added", 2, model.getRowCount());
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(filterName)) {
                break;
            }
        }
        assertTrue("Mapping was not found", i < model.getRowCount());
        utils.testTableRow(model, i, new String[]{filterName, servletName + " (Servlet)", "REQUEST, ERROR"});
        //check model
        FilterMapping[] mappings = webapp.getFilterMapping();
        for (i = 0; i < mappings.length; i++) {
            if (mappings[i].getFilterName().equals(filterName)) {
                break;
            }
        }
        assertTrue("Mapping was not found", i < mappings.length);
        utils.testProperties(mappings[i], new String[]{"FilterName", "ServletName", "UrlPattern"}, new Object[]{filterName, servletName, null});
        String[] exDisp = {"REQUEST", "ERROR"};
        String[] actDisp = mappings[i].getDispatcher();
        assertEquals("Wrong count of dispatchers types", exDisp.length, actDisp.length);
        for (i = 0; i < exDisp.length; i++) {
            assertEquals("Wrong dispatcher type.", exDisp[i], actDisp[i]);
        }
        //check XML
        utils.checkInDDXML(".*<filter-mapping>\\s*<filter-name>" + filterName + "</filter-name>\\s*<servlet-name>" + servletName + "</servlet-name>\\s*<dispatcher>REQUEST</dispatcher>\\s*<dispatcher>ERROR</dispatcher>\\s*</filter-mapping>.*");
        utils.save();
    }

    public void testFilterNameChangePropagation() throws Exception {
        final String filterName = "changedfilter";
        String servletName = webapp.getServlet(0).getServletName();
        Filter[] filters = webapp.getFilter();
        Filter filter = filters[0];
        ddObj.showElement(filter);
        JPanel panel = utils.getInnerSectionPanel(filter);
        Component[] comp = panel.getComponents();
        comp[1].requestFocus();
        utils.setText((JTextField) comp[1], filterName);
        comp[3].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<filter-name>" + filterName + "</filter-name>.*");
            }
        };
        //testing propagation of the changes
        panel = utils.getInnerSectionPanel("filter_mappings");
        panel.requestFocus();
        ddObj.showElement(webapp.getFilterMapping(0));
        DDBeanTableModel model = (DDBeanTableModel) ((FilterMappingsTablePanel) panel.getComponent(1)).getTable().getModel();
        //check table model
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (model.getValueAt(i, 0).equals(filterName)) {
                break;
            }
        }
        assertTrue("Mapping was not changed", i < model.getRowCount());
        utils.testTableRow(model, i, new String[]{filterName, servletName + " (Servlet)", "REQUEST, ERROR"});
        //check model
        FilterMapping[] mappings = webapp.getFilterMapping();
        for (i = 0; i < mappings.length; i++) {
            if (mappings[i].getFilterName().equals(filterName)) {
                break;
            }
        }
        assertTrue("Mapping was not found", i < mappings.length);
        utils.testProperties(mappings[i], new String[]{"FilterName", "ServletName", "UrlPattern"}, new Object[]{filterName, servletName, null});
        String[] exDisp = {"REQUEST", "ERROR"};
        String[] actDisp = mappings[i].getDispatcher();
        assertEquals("Wrong count of dispatchers types", exDisp.length, actDisp.length);
        for (i = 0; i < exDisp.length; i++) {
            assertEquals("Wrong dispatcher type.", exDisp[i], actDisp[i]);
        }
        //check XML
        utils.checkInDDXML(".*<filter-mapping>\\s*<filter-name>" + filterName + "</filter-name>\\s*<servlet-name>" + servletName + "</servlet-name>\\s*<dispatcher>REQUEST</dispatcher>\\s*<dispatcher>ERROR</dispatcher>\\s*</filter-mapping>.*");
        utils.save();
    }

    public void testServletNameChangePropagation() throws Exception {
        final String filterName = webapp.getFilter(0).getFilterName();
        final String servletName = "changedS";
        Servlet[] servlets = webapp.getServlet();
        Servlet servlet = servlets[0];
        ddObj.openView(4); // open Servlets view
        Utils.waitForAWTDispatchThread();
        ddObj.showElement(servlet);
        JPanel panel = utils.getInnerSectionPanel(servlet);
        Component[] comp = panel.getComponents();
        comp[1].requestFocus();
        utils.setText((JTextField) comp[1], servletName);
        comp[3].requestFocus();
        new StepIterator() {

            @Override
            public boolean step() throws Exception {
                return utils.contains(".*<servlet>.*<servlet-name>" + servletName + "</servlet-name>.*</servlet>.*");
            }
        };
        ddObj.openView(6); // open Filters view
        Utils.waitForAWTDispatchThread();
        panel = utils.getInnerSectionPanel("filter_mappings");
        panel.requestFocus();
        DDBeanTableModel model = (DDBeanTableModel) ((FilterMappingsTablePanel) panel.getComponent(1)).getTable().getModel();
        //check table model
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            if (((String) model.getValueAt(i, 1)).startsWith(servletName)) {
                break;
            }
        }
        assertTrue("Mapping was not changed", i < model.getRowCount());
        utils.testTableRow(model, i, new String[]{filterName, servletName + " (Servlet)", "REQUEST, ERROR"});
        //check model
        FilterMapping[] mappings = webapp.getFilterMapping();
        for (i = 0; i < mappings.length; i++) {
            if (mappings[i].getFilterName().equals(filterName)) {
                break;
            }
        }
        assertTrue("Mapping was not found", i < mappings.length);
        utils.testProperties(mappings[i], new String[]{"FilterName", "ServletName", "UrlPattern"}, new Object[]{filterName, servletName, null});
        String[] exDisp = {"REQUEST", "ERROR"};
        String[] actDisp = mappings[i].getDispatcher();
        assertEquals("Wrong count of dispatchers types", exDisp.length, actDisp.length);
        for (i = 0; i < exDisp.length; i++) {
            assertEquals("Wrong dispatcher type.", exDisp[i], actDisp[i]);
        }
        //check XML
        utils.checkInDDXML(".*<filter-mapping>\\s*<filter-name>" + filterName + "</filter-name>\\s*<servlet-name>" + servletName + "</servlet-name>\\s*<dispatcher>REQUEST</dispatcher>\\s*<dispatcher>ERROR</dispatcher>\\s*</filter-mapping>.*");
        utils.save();
    }

    public void testModifyFilterMapping() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("filter_mappings");
        DDBeanTableModel model = (DDBeanTableModel) ((FilterMappingsTablePanel) panel.getComponent(1)).getTable().getModel();
        model.editRow(0, new Object[]{"FilterNameMod", "*", null, new String[]{"FORWARD", "INCLUDE"}});
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        //check table model
        utils.testTableRow(model, 0, new String[]{"FilterNameMod", "* (URL)", "FORWARD, INCLUDE"});
        //check model
        FilterMapping[] mappings = webapp.getFilterMapping();
        utils.testProperties(mappings[0], new String[]{"FilterName", "ServletName", "UrlPattern"}, new Object[]{"FilterNameMod", null, "*"});
        String[] exDisp = {"FORWARD", "INCLUDE"};
        String[] actDisp = mappings[0].getDispatcher();
        assertEquals("Wrong count of dispatchers types", exDisp.length, actDisp.length);
        for (int i = 0; i < exDisp.length; i++) {
            assertEquals("Wrong dispatcher type.", exDisp[i], actDisp[i]);
        }
        //check XML
        utils.checkInDDXML(".*<filter-mapping>\\s*<filter-name>FilterNameMod</filter-name>\\s*<url-pattern>\\*</url-pattern>\\s*<dispatcher>FORWARD</dispatcher>\\s*<dispatcher>INCLUDE</dispatcher>\\s*</filter-mapping>.*");
    }

    public void testDelFilterMapping() throws Exception {
        JPanel panel = utils.getInnerSectionPanel("filter_mappings");
        DDBeanTableModel model = (DDBeanTableModel) ((FilterMappingsTablePanel) panel.getComponent(1)).getTable().getModel();
        final String mappingName = (String) model.getValueAt(0, 0);
        model.removeRow(0);
        ddObj.modelUpdatedFromUI();
        Thread.sleep(2000);
        int i;
        for (i = 0; i < model.getRowCount(); i++) {
            assertFalse("Mapping was not deleted.", model.getValueAt(i, 0).equals(mappingName));
        }
        FilterMapping[] mappings = webapp.getFilterMapping();
        for (i = 0; i < mappings.length; i++) {
            assertFalse("Mapping was not deleted.", mappings[i].getFilterName().equals(mappingName));
        }
        utils.checkNotInDDXML(".*<filter-mapping>\\s*<filter-name>" + mappingName + "</filter-name>.*</filter-mapping>.*");
        utils.save();
    }
    // TODO tests for pages
    // TODO tests for references
}
