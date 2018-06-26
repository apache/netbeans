/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.groovy.qaf;

import java.awt.event.ActionEvent;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import javax.swing.SwingUtilities;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.FilesTabOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.groovy.grails.settings.GrailsSettings;
import org.netbeans.modules.groovy.grailsproject.actions.GotoDomainClassAction;
import org.openide.windows.TopComponent;

/**
 * Tests for actions available on Grails projects
 *
 * @author lukas
 */
public class GrailsActionsTest extends GrailsTestCase {

    private static final String APP_PORT = "9998"; //NOI18N
    private static final String DEFAULT_PORT = "8080"; //NOI18N
    private static final String PLUGIN_NAME = "testing"; //NOI18N
    private static boolean isPortSet = false;
    private OpenAction oa = new OpenAction();

    public GrailsActionsTest(String name) {
        super(name);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        if (!isPortSet) {
            setApplicationPort(APP_PORT);
            assertEquals(APP_PORT, GrailsSettings.getInstance().getPortForProject(getProject()));
            isPortSet = true;
        }
    }

    @Override
    protected String getProjectName() {
        return "GrailsActions"; //NOI18N
    }

    /**
     * Test Generate all action on the domain class node
     *
     */
    public void testGenerateAll() {
        //Generate All
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_GenerateAllAction");
        getDomainClassNode("Book").performPopupAction(label); //NOI18N
        waitFor("generate-all", "Finished generation for domain class"); //NOI18N
    }

    /**
     * Test for Grails Plugins action
     *  -install plugin
     */
    public void testManagePlugins() {
        //open plugin manager and install a plugin
        //Grails Plugins...
        String actionLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_ManagePluginsAction");
        getProjectRootNode().performPopupActionNoBlock(actionLabel);
        //Grails Plugins
        String title = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_PluginTitle");
        NbDialogOperator ndo = new NbDialogOperator(title);
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(ndo);
        //New Plugins
        String tabTitle = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.Bundle", "GrailsPluginPanel.newPlugins");
        jtpo.selectPage(tabTitle);
        JListOperator jlo = new JListOperator(jtpo);
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 500000); //NOI18N
        jlo.waitItem("activemq", 1);
        jlo.selectItem(PLUGIN_NAME);
        //Install
        String btnLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.Bundle", "GrailsPluginPanel.installButton.text");
        new JButtonOperator(jtpo, btnLabel).push();
        //Installed
        tabTitle = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.Bundle", "GrailsPluginPanel.installed");
        JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 240000); //NOI18N
        jtpo.selectPage(tabTitle);
        jlo = new JListOperator(jtpo);
        jlo.waitItem(PLUGIN_NAME, 0);
        assertEquals(1, jlo.getModel().getSize());
        ndo.closeByButton();
    }

    /**
     * Test Create view action on the domain class node
     *
     */
    public void testGenerateViews() {
        //Generate Views
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_GenerateViewsAction");
        getDomainClassNode("Author").performPopupAction(label); //NOI18N
        waitFor("generate-views", "Finished generation for domain class"); //NOI18N
    }

    /**
     * Test Go to Controller action
     *
     */
    public void testGotoController() {
        //Go to Grails Controller
        Action a = getGrailsNavigateAction("CTL_GotoControllerAction"); //NOI18N
        //from a domain class
        oa.perform(getDomainClassNode("Book")); //NOI18N
        EditorOperator eo = new EditorOperator("Book.groovy"); //NOI18N
        assertNotNull(eo);
        try {
            //XXX - first call to popup can fail (win, solaris)
            a.performPopup(eo);
        } catch (TimeoutExpiredException tee) {
            //try it once again
            a.performPopup(eo);
        }
        assertTrue(getActiveTC().endsWith("controllers" + File.separator + "BookController.groovy")); //NOI18N
        //from a view
        oa.perform(getViewNode("book|edit")); //NOI18N
        eo = new EditorOperator("edit.gsp"); //NOI18N
        assertNotNull(eo);
        a.performPopup(eo); //NOI18N
        assertTrue(getActiveTC().endsWith("controllers" + File.separator + "BookController.groovy")); //NOI18N
    }

    /**
     * Test Go to View action
     *
     */
    public void testGotoView() {
        //Go to Grails View
        Action a = getGrailsNavigateAction("CTL_GotoViewAction"); //NOI18N
        //from a domain class
        oa.perform(getDomainClassNode("Book")); //NOI18N
        EditorOperator eo = new EditorOperator("Book.groovy"); //NOI18N
        assertNotNull(eo);
        a.performPopup(eo);
        assertTrue(getActiveTC().endsWith("views" + File.separator + "book" + File.separator + "show.gsp")); //NOI18N
        //from a controller
        oa.perform(getControllerNode("BookController")); //NOI18N
        eo = new EditorOperator("BookController.groovy"); //NOI18N
        assertNotNull(eo);
        a.performPopup(eo);
        assertTrue(getActiveTC().endsWith("views" + File.separator + "book" + File.separator + "show.gsp")); //NOI18N
    }

    /**
     * Test Go to Domain class action
     *
     */
    public void testGotoDomainClass() throws InterruptedException, InvocationTargetException {
        //XXX - no direct UI entry to this action
        // see: http://www.netbeans.org/issues/show_bug.cgi?id=154768
        final GotoDomainClassAction a = new GotoDomainClassAction();
        //from a view
        oa.perform(getViewNode("book|list")); //NOI18N
        final EditorOperator eo = new EditorOperator("list.gsp"); //NOI18N
        assertNotNull(eo);
        assertTrue(a.isEnabled());
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                a.actionPerformed(new ActionEvent(eo.txtEditorPane().getSource(), -1, null));
            }
        });
        assertTrue(getActiveTC().endsWith("domain" + File.separator + "Book.groovy")); //NOI18N
        //from a controller
        oa.perform(getControllerNode("BookController")); //NOI18N
        final EditorOperator eo2 = new EditorOperator("BookController.groovy"); //NOI18N
        assertNotNull(eo2);
        assertTrue(a.isEnabled());
        SwingUtilities.invokeAndWait(new Runnable() {

            public void run() {
                a.actionPerformed(new ActionEvent(eo2.txtEditorPane().getSource(), -1, null));
            }
        });
        assertTrue(getActiveTC().endsWith("domain" + File.separator + "Book.groovy")); //NOI18N
    }

    /**
     * Test grails project Run and Stop actions
     *
     */
    public void testStopApp() {
        //XXX - better to have ability to not open browser during run
        //      (remove TestURLDisplayer, can be changed after
        //       http://www.netbeans.org/issues/show_bug.cgi?id=154920)
        runGrailsApp();
        stopGrailsApp();
    }

    /**
     * Test for Grails Plugins action
     *  -uninstall plugin
     */
    public void testUninstallPlugin() {
        //open the dialog again and uninstall previously installed plugin
        //Grails Plugins...
        String actionLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_ManagePluginsAction");
        getProjectRootNode().performPopupActionNoBlock(actionLabel);
        //Grails Plugins
        String title = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "CTL_PluginTitle");
        NbDialogOperator ndo = new NbDialogOperator(title);
        JTabbedPaneOperator jtpo = new JTabbedPaneOperator(ndo);
        JListOperator jlo = new JListOperator(jtpo);
        jlo.selectItem(PLUGIN_NAME);
        //Uninstall
        String btnLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.ui.Bundle", "GrailsPluginPanel.uninstallButton.text");
        new JButtonOperator(jtpo, btnLabel).push();
        //XXX - need in case project is versioned by hg (bug?)
        long end = System.currentTimeMillis() + 240000;
        while (jlo.getModel().getSize() > 0 && System.currentTimeMillis() < end) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ie) {}
        }
        assertEquals(0, jlo.getModel().getSize());
        ndo.closeByButton();
    }

    /**
     * Test Create war project action
     *
     */
    public void testCreateWar() {
        String label = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", "LBL_CreateWarFile");
        getProjectRootNode().performPopupAction(label);
        waitFor("war", "Done creating WAR"); //NOI18N
        FilesTabOperator fto = FilesTabOperator.invoke();
        Node n = new Node(fto.getProjectNode(getProjectName()), getProjectName() + "-0.1.war"); //NOI18N
        assertNotNull(n);
    }

    private void setApplicationPort(String port) {
        getProjectRootNode().properties();
        NbDialogOperator ndo = new NbDialogOperator(getProjectName());
        JTextFieldOperator jtfo = new JTextFieldOperator(ndo, DEFAULT_PORT);
        jtfo.clearText();
        jtfo.typeText(port);
        ndo.btOK().push();
        //wait till project data are really saved
        //Saving Project data
        String saveDlgTitle = Bundle.getStringTrimmed("org.netbeans.modules.project.uiapi.Bundle", "LBL_Saving_Project_data");
        waitDialogClosed(saveDlgTitle);
    }

    private Node getDomainClassNode(String domainClass) {
        //Domain Classes
        String label = getNodeLabel("LBL_grails-app_domain"); //NOI18N
        Node n = new Node(getProjectRootNode(), label);
        return new Node(n, domainClass + ".groovy"); //NOI18N
    }

    private Node getControllerNode(String controller) {
        //Controllers
        String label = getNodeLabel("LBL_grails-app_controllers"); //NOI18N
        Node n = new Node(getProjectRootNode(), label);
        return new Node(n, controller + ".groovy"); //NOI18N
    }

    private Node getViewNode(String view) {
        //Views and Layouts
        String label = getNodeLabel("LBL_grails-app_views"); //NOI18N
        Node n = new Node(getProjectRootNode(), label);
        return new Node(n, view + ".gsp"); //NOI18N
    }

    private String getNodeLabel(String key) {
        return Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.Bundle", key);
    }

    private Action getGrailsNavigateAction(String key) {
        String groupLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.Bundle", "Editors/text/x-gsp/Popup/goto");
        String actionLabel = Bundle.getStringTrimmed("org.netbeans.modules.groovy.grailsproject.actions.Bundle", key);
        return new Action(null, groupLabel + "|" + actionLabel); //NOI18N
    }

    private String getActiveTC() {
        return TopComponent.getRegistry().getActivated().getToolTipText();
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(GrailsActionsTest.class)
                .enableModules(".*").clusters(".*")); //NOI18N
    }

}
