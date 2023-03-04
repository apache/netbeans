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
package org.netbeans.test.html5;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.*;
import org.netbeans.modules.web.browser.api.WebBrowser;
import org.netbeans.modules.web.clientproject.spi.SiteTemplateImplementation;
import org.netbeans.modules.web.inspect.PageInspectorImpl;
import org.netbeans.modules.web.inspect.PageModel;

/**
 *
 * @author Vladimir Riha
 */
public class GeneralHTMLProject extends JellyTestCase {

    /**
     * Timeout for retrieving inspected elements
     */
    private static final int WAIT_INSPECTION_TIME = 20000;
    protected EventTool evt;
    public static final String PROJECT_CATEGORY_NAME = "HTML5";
    public static final String PROJECT_NAME = "HTML5 Application";
    public static final String SAMPLES = "Samples";
    public static final String SAMPLES_CATEGORY = "HTML5";
    public static int RUN_WAIT_TIMEOUT = 1000;
    public static String current_project = "";
    public static boolean inEmbeddedBrowser = false;
    public static String currentBrowser = "Chrome";
    public static final Logger LOGGER = Logger.getLogger(GeneralHTMLProject.class.getName());

    public GeneralHTMLProject(String arg0) {
        super(arg0);
        this.evt = new EventTool();
    }

    /**
     * Sets time to wait when run file action is performed based on browser
     *
     * @param browserName
     */
    public static void setRunTimeout(String browserName) {
        if (browserName.startsWith("Chrome") || browserName.startsWith("Chromium")) {
            GeneralHTMLProject.RUN_WAIT_TIMEOUT = 5000;
        } else if (browserName.startsWith("Embedded")) {
            GeneralHTMLProject.RUN_WAIT_TIMEOUT = 1000;
        } else {
            GeneralHTMLProject.RUN_WAIT_TIMEOUT = 2000;
        }
    }

    public void createDefaultProject(String projectName) {
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PROJECT_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PROJECT_NAME);
        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);
        jtName.setText(projectName);

        evt.waitNoEvent(1000);

        opNewProjectWizard.finish();
        waitScanFinished();
    }

    /**
     * Creates new project from online template
     *
     * @param projectName project name
     * @param templateName online template name
     */
    public void createtProjectOnlineTemplate(String projectName, String templateName) {
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();
        opNewProjectWizard.selectCategory(PROJECT_CATEGORY_NAME);
        opNewProjectWizard.selectProject(PROJECT_NAME);
        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, 0);
        jtName.setText(projectName);
        evt.waitNoEvent(1000);
        opNewProjectWizard.next();

        (new JRadioButtonOperator(jdNew, "Download Online Template:")).setSelected(true);
        JListOperator templates = new JListOperator(jdNew, 1);
        SiteTemplateImplementation site;
        boolean templateExists = false;
        for (int i = 0; i < templates.getModel().getSize(); i++) {
            try {
                site = (SiteTemplateImplementation) templates.getModel().getElementAt(i);
                if (site.getName().startsWith(templateName)) {
                    templates.selectItem(i);
                    templateExists = true;
                    break;
                }
            } catch (java.lang.ClassCastException ex) {
                LOGGER.log(Level.INFO, "cannot cast site template to SiteTemplateImplementation", ex);

            }
        }

        if (!templateExists) {
            fail("Online template \"" + templateName + "\" not found");
        }

        opNewProjectWizard.finish();
        waitScanFinished();
    }

    /**
     * Creates new sample project
     *
     * @param sampleName name of sample
     * @param projectName target project name
     */
    public void createSampleProject(String sampleName, String projectName) {
        setProxy();
        NewProjectWizardOperator opNewProjectWizard = NewProjectWizardOperator.invoke();

        opNewProjectWizard.selectCategory(SAMPLES + "|" + SAMPLES_CATEGORY);
        opNewProjectWizard.selectProject(sampleName);
        opNewProjectWizard.next();

        JDialogOperator jdNew = new JDialogOperator("New HTML5 Sample Project");
        JTextComponentOperator jtName = new JTextComponentOperator(jdNew, sampleName.substring(0, 5));
        jtName.setText(projectName);
        opNewProjectWizard.finish();
        waitScanFinished();

    }

    protected void setProxy() {
        OptionsOperator optionsOper = OptionsOperator.invoke();
        optionsOper.selectGeneral();
        // "Manual Proxy Setting"
        String hTTPProxyLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Use_HTTP_Proxy");
        new JRadioButtonOperator(optionsOper, hTTPProxyLabel).push();
        // "HTTP Proxy:"
        String proxyHostLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Proxy_Host");
        JLabelOperator jloHost = new JLabelOperator(optionsOper, proxyHostLabel);
        new JTextFieldOperator((JTextField) jloHost.getLabelFor()).typeText("emea-proxy.uk.oracle.com"); // NOI18N
        // "Port:"
        String proxyPortLabel = Bundle.getStringTrimmed(
                "org.netbeans.core.ui.options.general.Bundle", "CTL_Proxy_Port");
        JLabelOperator jloPort = new JLabelOperator(optionsOper, proxyPortLabel);
        new JTextFieldOperator((JTextField) jloPort.getLabelFor()).setText("80"); // NOI18N
        optionsOper.ok();
    }

    /**
     * Runs project via context menu on project node in Projects window
     *
     * @param projectName name of the project
     */
    public void runProject(String projectName) {
        ProjectsTabOperator.invoke().getProjectRootNode(projectName).performPopupAction("Run");
    }

    /**
     * Runs selected file via its context menu action "Run File". Before running
     * the file, it is opened in editor window
     *
     * @param projectName
     * @param pathAndFileName relative path to Site Root, e.g. for Site
     * Root/web/index.html it would be "web|index.html" (| is path separator)
     * @throws IllegalStateException
     */
    public void runFile(String projectName, String pathAndFileName) throws IllegalStateException {
        openFile(pathAndFileName, projectName);
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralHTMLProject.class.getName()).log(Level.INFO, "Opening file {0}", pathAndFileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Site Root|" + pathAndFileName);
        node.select();
        node.performPopupAction("Run File");
    }

    /**
     * Sets project's run configuration via Project Properties dialog
     *
     * @param browserName
     * @param autoRefresh
     * @param syncHover
     */
    public void setRunConfiguration(String browserName, boolean autoRefresh, boolean syncHover) {
        ProjectsTabOperator.invoke().getProjectRootNode(GeneralHTMLProject.current_project).properties();
        NbDialogOperator propertiesDialogOper = new NbDialogOperator("Project Properties");
        new Node(new JTreeOperator(propertiesDialogOper), "Run").select();
        JComboBoxOperator browsers = new JComboBoxOperator(propertiesDialogOper, "Browser");
        WebBrowser browser;
        GeneralHTMLProject.setRunTimeout(browserName);
        GeneralHTMLProject.currentBrowser = browserName;
        for (int i = 0; i < browsers.getModel().getSize(); i++) {
            browser = (WebBrowser) browsers.getModel().getElementAt(i);

            if (browser.getName().equals(browserName)) {
                browsers.setSelectedIndex(i);
                if (browser.hasNetBeansIntegration()) {
                    (new JCheckBoxOperator(propertiesDialogOper, "Auto-refresh")).setSelected(autoRefresh);
                    (new JCheckBoxOperator(propertiesDialogOper, "Synchronize")).setSelected(syncHover);
                }
                propertiesDialogOper.ok();
                waitScanFinished();
                GeneralHTMLProject.inEmbeddedBrowser = browserName.equalsIgnoreCase("Embedded WebKit Browser");

                return;
            }
        }

        fail("Browser \"" + browserName + "\" not found");
    }

    public void openProject(String projectName) throws IOException {
        JemmyProperties.setCurrentTimeout("ActionProducer.MaxActionTime", 180000);
        openDataProjects(projectName);
        waitScanFinished();
    }

    /**
     * Waits for Remote Files node to appear in Projects tab
     *
     * @param projectName
     */
    public void waitForRemoteFiles(String projectName) {
        final String project = projectName;
        try {
            Waiter waiter = new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    Node rootNode = new ProjectsTabOperator().getProjectRootNode(project);
                    return rootNode.isChildPresent("Remote Files") ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait for Remote Files node to appear");
                }
            });

            waiter.waitAction(null);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Opens file in editor
     *
     * @param pathAndFileName relative path to Site Root in Projects window,
     * e.g. for Site Root/web/index.html it would be "web|index.html" (| is path
     * separator)
     * @param projectName project name
     */
    public void openFile(String pathAndFileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralHTMLProject.class.getName()).log(Level.INFO, "Opening file {0}", pathAndFileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Site Root|" + pathAndFileName);
        evt.waitNoEvent(1000);

        if (node.isLeaf()) {
            node.select();
            node.performPopupAction("Open");
        }
    }

    /**
     * Opens remote file in editor
     *
     * @param fileName remote file name
     * @param projectName project name
     */
    public void openRemoteFile(String fileName, String projectName) {
        if (projectName == null) {
            throw new IllegalStateException("YOU MUST OPEN PROJECT FIRST");
        }
        Logger.getLogger(GeneralHTMLProject.class.getName()).log(Level.INFO, "Opening file {0}", fileName);
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Remote Files|" + fileName);
        evt.waitNoEvent(1000);
        node.performPopupAction("Open");
    }

    /**
     * Returns array of all elements that are selected in browser. List of given
     * elements is retrieved from model, not browser as such
     *
     * @return selected elements
     */
    public HTMLElement[] getSelectedElements() {
        PageModel page = PageInspectorImpl.getDefault().getPage();
        List<? extends org.openide.nodes.Node> nodes = page.getSelectedNodes();
        return getElements(nodes);
    }

    /**
     * Returns array of all elements that are highlighted in browser. List of
     * given elements is retrieved from model, not browser as such
     *
     * @return highlighted elements
     */
    public HTMLElement[] getHighlightedElements() {
        PageModel page = PageInspectorImpl.getDefault().getPage();
        List<? extends org.openide.nodes.Node> nodes = page.getHighlightedNodes();
        return getElements(nodes);
    }

    /**
     * Returns array of all elements that matches selected CSS rule and are
     * outlined in browser. List of given elements is retrieved from model, not
     * browser as such
     *
     * @return matching elements
     */
    public HTMLElement[] getMatchingElements() {
        PageModel page = PageInspectorImpl.getDefault().getPage();
        List<? extends org.openide.nodes.Node> nodes = page.getNodesMatchingSelectedRule();
        return getElements(nodes);
    }

    /**
     * Opens given file and types space at the end of first line (on slow system this seems to help with parsing Remote files)
     * @param pathAndFileName
     * @param projectName
     * @param fileName
     */
    public void dummyEdit(String pathAndFileName, String projectName, String fileName){

        Node rootNode = new ProjectsTabOperator().getProjectRootNode(projectName);
        Node node = new Node(rootNode, "Site Root|" + pathAndFileName);
        evt.waitNoEvent(500);

        if (node.isLeaf()) {
            node.select();
            node.performPopupAction("Open");
        }

        EditorOperator ep = new EditorOperator(fileName);
        ep.setCaretPositionToEndOfLine(1);
        type(ep, " ");
        ep.save();
        ep.close();
    }

    private HTMLElement[] getElements(List<? extends org.openide.nodes.Node> nodes) {
        ArrayList<String> parents;
        ArrayList<String> parentsPlain;
        int nodesSize = nodes.size();
        HTMLElement[] elements = new HTMLElement[nodesSize];
        org.openide.nodes.Node node;

        for (int i = 0; i < nodesSize; i++) {
            node = nodes.get(i);
            parents = new ArrayList<String>();
            parentsPlain = new ArrayList<String>();
            elements[i] = new HTMLElement(node.getName().toLowerCase(), node.getDisplayName());
            while (node.getParentNode() != null) {
                node = node.getParentNode();
                if (!node.getDisplayName().equals("#document")) {
                    parents.add(node.getDisplayName());
                    parentsPlain.add(node.getName().toLowerCase());
                }
            }

            Collections.reverse(parents);
            Collections.reverse(parentsPlain);
            elements[i].parents = parents;
            elements[i].parentsPlain = parentsPlain;

        }
        return elements;
    }

    /**
     * Sets selected nodes via PageModel, browser should reflect this change
     *
     * @param nodes nodes (elements) to be selected
     */
    public void setSelectedElementsModel(List<? extends org.openide.nodes.Node> nodes) {
        PageModel page = PageInspectorImpl.getDefault().getPage();
        page.setSelectedNodes(nodes);
    }

    public void type(EditorOperator edit, String code) {
        int iLimit = code.length();
        for (int i = 0; i < iLimit; i++) {
            edit.typeKey(code.charAt(i));
        }
        evt.waitNoEvent(100);
    }

    /**
     * Waits until given number of selected elements (in
     * {@link PageModel#getSelectedNodes()}) is equal to given parameter.
     * Timeout is {@link GeneralHTMLProject#WAIT_INSPECTION_TIME}
     *
     * @param expectedElements number of elements to be selected
     * @param jsLimit initial waiting time - since attributes in html could be
     * changed using setTimeout(), elements are modified to look as selected
     * after this timeout. So first one must wait this timeout and then start to
     * wait for actual selected element(s)
     */
    public void waitElementsSelected(final int expectedElements, long jsLimit) {
        evt.waitNoEvent(jsLimit);
        try {
            Waiter waiter = new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    return (PageInspectorImpl.getDefault().getPage()).getSelectedNodes().size() == expectedElements ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait for number of elements to be " + expectedElements);
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", WAIT_INSPECTION_TIME);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Waits until given number of highlighted elements (in
     * {@link PageModel#getHighlightedNodes()}) is equal to given parameter.
     * Timeout is {@link GeneralHTMLProject#WAIT_INSPECTION_TIME}
     *
     * @param expectedElements number of elements to be highlighted
     * @param jsLimit initial waiting time - since attributes in html could be
     * changed using setTimeout(), elements are modified to look as selected
     * after this timeout. So first one must wait this timeout and then start to
     * wait for actual selected element(s)
     */
    public void waitElementsHighlighted(final int expectedElements, long jsLimit) {
        evt.waitNoEvent(jsLimit);
        try {
            Waiter waiter = new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    return (PageInspectorImpl.getDefault().getPage()).getHighlightedNodes().size() == expectedElements ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait for number of elements to be " + expectedElements);
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", WAIT_INSPECTION_TIME);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Waits until given number of matched elements (in
     * {@link PageModel#getNodesMatchingSelectedRule()}) is equal to given
     * parameter. Timeout is {@link GeneralHTMLProject#WAIT_INSPECTION_TIME}
     *
     * @param expectedElements number of elements to be outlined
     * @param jsLimit initial waiting time - since attributes in html could be
     * changed using setTimeout(), elements are modified to look as selected
     * after this timeout. So first one must wait this timeout and then start to
     * wait for actual selected element(s)
     */
    public void waitMatchedElements(final int expectedElements, long jsLimit) {
        evt.waitNoEvent(jsLimit);
        try {
            Waiter waiter = new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    return (PageInspectorImpl.getDefault().getPage()).getNodesMatchingSelectedRule().size() == expectedElements ? Boolean.TRUE : null;
                }

                @Override
                public String getDescription() {
                    return ("Wait for number of elements to be " + expectedElements);
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", WAIT_INSPECTION_TIME);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
        }
    }
}

class HTMLElement {

    public String namePlain;
    public String name;
    /**
     * Array of strings, each string contain HTML element name and ID and class
     * attribute
     */
    public ArrayList<String> parents;
    /**
     * Array of strings, each string contains only HTML element name
     */
    public ArrayList<String> parentsPlain;

    public HTMLElement(String namePlain, String name) {
        this.namePlain = namePlain;
        this.name = name;
    }

    /**
     * Returns string that is same as returned by {@link DomOperator#getFocusedElement()
     * }
     *
     * @return sample output {@code [root, html, body]body#foo.bar}
     */
    public String getNavigatorString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (String parent : this.parents) {
            sb.append(parent).append(", ");
        }
        sb.append(this.name).append("]");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.name).append(":\n   ");
        for (String parent : this.parents) {
            sb.append(parent).append(" ");
        }
        sb.append("\n   ");
        for (String parent : this.parentsPlain) {
            sb.append(parent).append(" ");
        }
        return sb.toString();
    }
}
