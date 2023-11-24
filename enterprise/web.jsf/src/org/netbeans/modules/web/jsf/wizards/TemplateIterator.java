/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.jsf.JSFConfigUtilities;
import org.netbeans.modules.web.jsf.JSFFrameworkProvider;
import org.netbeans.modules.web.jsf.JSFUtils;
import org.netbeans.modules.web.jsf.api.facesmodel.JsfVersionUtils;
import org.netbeans.modules.web.jsf.palette.JSFPaletteUtilities;
import org.netbeans.modules.web.jsfapi.api.JsfVersion;
import org.netbeans.modules.web.jsfapi.api.NamespaceUtils;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.MapFormat;
import org.openide.util.NbBundle;

/**
 *
 * @author Petr Pisl
 */
public class TemplateIterator implements TemplateWizard.Iterator {

    private static final long serialVersionUID = 458897855L;
    private int index;
    private transient WizardDescriptor.Panel[] panels;
    private TemplatePanel templatePanel;
    /*package*/ static final String CSS_FOLDER = "css"; //NOI18N
    private static final String CSS_FOLDER2 = "resources/css"; //NOI18N
    private static final String CSS_EXT = "css"; //NOI18N
    private static final String XHTML_EXT = "xhtml";    //NOI18N
    private static final String ENCODING = "UTF-8"; //NOI18N
    private static String TEMPLATE_XHTML = "template.xhtml"; //NOI18N
    private static String TEMPLATE_XHTML2 = "template-jsf2.template"; //NOI18N
    private static String TEMPLATE_XHTML22 = "template-jsf22.template"; //NOI18N
    private static String FL_RESOURCE_FOLDER = "org/netbeans/modules/web/jsf/facelets/resources/templates/"; //NOI18N

    /**
     * Creates a new instance of TemplateIterator
     */
    public TemplateIterator() {
    }

    static FileObject createTemplate(Project project, FileObject targetDir, boolean addJSFFrameworkIfNecessary) throws IOException {
        FileObject result = null;
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null) {
            FileObject dir = wm.getDocumentBase();
            if (dir.getFileObject(TEMPLATE_XHTML) != null) {
                return null;
            }
            if (addJSFFrameworkIfNecessary && !JSFConfigUtilities.hasJsfFramework(dir)) {
                JSFConfigUtilities.extendJsfFramework(dir, false);
            }

            JsfVersion version = JsfVersionUtils.forWebModule(wm);
            String templateFile = TEMPLATE_XHTML2;
            if (version != null && version.isAtLeast(JsfVersion.JSF_2_2)) {
                templateFile = TEMPLATE_XHTML22;
            }
            String content = JSFFrameworkProvider.readResource(Thread.currentThread().getContextClassLoader().getResourceAsStream(FL_RESOURCE_FOLDER + templateFile), ENCODING);
            result = FileUtil.createData(targetDir, TEMPLATE_XHTML); //NOI18N
            JSFFrameworkProvider.createFile(result, content, ENCODING); //NOI18N
            DataObject dob = DataObject.find(result);
            if (dob != null) {
                JSFPaletteUtilities.reformat(dob);
            }
        }
        return result;
    }

    @Override
    public Set instantiate(TemplateWizard wiz) throws IOException {
        final org.openide.filesystems.FileObject dir = Templates.getTargetFolder(wiz);
        final String targetName = Templates.getTargetName(wiz);
        final DataFolder df = DataFolder.findFolder(dir);
        if (df != null) {
            WebModule wm = WebModule.getWebModule(df.getPrimaryFile());
            if (wm != null) {
                final FileObject docBase = wm.getDocumentBase();
                if (!JSFConfigUtilities.hasJsfFramework(docBase)) {
                    JSFConfigUtilities.extendJsfFramework(dir, false);
                }
                final JsfVersion jsfVersion = JsfVersionUtils.forWebModule(wm) != null ? JsfVersionUtils.forWebModule(wm) : JsfVersion.JSF_2_2;
                FileObject cssFolder = handleCssFolderCreation(jsfVersion, docBase);
                return createTemplate(wiz, df, targetName, cssFolder, jsfVersion);
            } else {
                // get the JSF version
                Project project = Templates.getProject(wiz);
                JsfVersion jsfVersion = JsfVersionUtils.forProject(project);
                jsfVersion = jsfVersion == null ? JsfVersion.JSF_2_2 : jsfVersion;

                String folderName = (jsfVersion == null || jsfVersion.isAtLeast(JsfVersion.JSF_2_0)) ? CSS_FOLDER2 : CSS_FOLDER;
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] sourceGroups = sources.getSourceGroups("java"); //NOII18N
                if (sourceGroups.length > 0) {
                    // create META-INF folder
                    FileObject root = sourceGroups[0].getRootFolder();
                    FileObject metaInf = root.getFileObject("META-INF");
                    if (metaInf == null) {
                        metaInf = FileUtil.createFolder(root, "META-INF");
                    }

                    // css folder
                    FileObject cssFolder = handleCssFolderCreation(jsfVersion, metaInf);

                    // template creation
                    return createTemplate(wiz, df, targetName, cssFolder, jsfVersion);
                }
            }
        }
        return Collections.emptySet();
    }

    @Override
    public void initialize(TemplateWizard wiz) {
        //this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject(wiz);
        panels = createPanels(project, wiz);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA);
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = createSteps(beforeSteps, panels);

        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            if (steps[i] == null) {
                // Default step name to component name of panel.
                // Mainly useful for getting the name of the target
                // chooser to appear in the list of steps.
                steps[i] = c.getName();
            }
            if (c instanceof JComponent) { // assume Swing components
                JComponent jc = (JComponent) c;
                // Step #.
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i);
                // Step name (actually the whole list for reference).
                jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            }
        }
    }

    @Override
    public void uninitialize(TemplateWizard wiz) {
        panels = null;
    }

    @Override
    public WizardDescriptor.Panel current() {
        return panels[index];
    }

    @Override
    public String name() {
        return NbBundle.getMessage(TemplateIterator.class, "TITLE_x_of_y", index + 1, panels.length);
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    protected WizardDescriptor.Panel[] createPanels(Project project, TemplateWizard wiz) {
        Sources sources = (Sources) ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups1 = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
        SourceGroup[] sourceGroups;
        if (sourceGroups1.length == 0) {
            sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        } else if (sourceGroups1.length == 1) {
            sourceGroups = new SourceGroup[]{sourceGroups1[0], sourceGroups1[0]};
        } else {
            sourceGroups = sourceGroups1;
        }

        templatePanel = new TemplatePanel(wiz);
        // creates simple wizard panel with bottom panel
        WizardDescriptor.Panel firstPanel = new JSFValidationPanel(
                Templates.buildSimpleTargetChooser(project, sourceGroups).bottomPanel(templatePanel).create());
        JComponent c = (JComponent) firstPanel.getComponent();
        Dimension d = c.getPreferredSize();
        d.setSize(d.getWidth(), d.getHeight() + 65);
        c.setPreferredSize(d);
        return new WizardDescriptor.Panel[]{
            firstPanel
        };
    }

    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[(before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent().getName();
            }
        }
        return res;
    }

    private FileObject handleCssFolderCreation(JsfVersion jsfVersion, FileObject rootDir) throws IOException {
        String folderName = (jsfVersion == null || jsfVersion.isAtLeast(JsfVersion.JSF_2_0)) ? CSS_FOLDER2 : CSS_FOLDER;
        FileObject cssFolder = rootDir.getFileObject(folderName);
        if (cssFolder == null) {
            cssFolder = FileUtil.createFolder(rootDir, folderName);
        }
        return cssFolder;
    }

    private Set<DataObject> createTemplate(TemplateWizard wiz, DataFolder df, String targetName, FileObject cssFolder, JsfVersion jsfVersion) throws IOException {
        CreateTemplateAction createTemplateAction = new CreateTemplateAction(
                templatePanel.getComponent(),
                Templates.getTargetName(wiz),
                Templates.getTargetFolder(wiz),
                cssFolder,
                jsfVersion);
        df.getPrimaryFile().getFileSystem().runAtomicAction(createTemplateAction);

        FileObject target = df.getPrimaryFile().getFileObject(targetName, XHTML_EXT);
        DataObject dob = DataObject.find(target);
        JSFPaletteUtilities.reformat(dob);
        return Collections.singleton(dob);
    }

    /*package*/ static class CreateTemplateAction implements FileSystem.AtomicAction {

        private final TemplatePanelVisual templatePanel;
        private final String templateName;
        private final FileObject targetFolder;
        private final FileObject cssTargetFolder;
        private final JsfVersion jsfVersion;
        private FileObject result;

        public CreateTemplateAction(TemplatePanelVisual templatePanel, String templateName, FileObject targetFolder,
                FileObject cssTargetFolder, JsfVersion jsfVersion) {
            this.templatePanel = templatePanel;
            this.templateName = templateName;
            this.targetFolder = targetFolder;
            this.cssTargetFolder = cssTargetFolder;
            this.jsfVersion = jsfVersion;
        }

        @Override
        public void run() throws IOException {
            InputStream is;
            FileObject target = targetFolder.createData(templateName, XHTML_EXT); //NOI18N

            // name of the layout file
            String layoutName = templatePanel.getLayoutFileName();
            FileObject cssFile = cssTargetFolder.getFileObject(layoutName, CSS_EXT); //NOI18N
            if (cssFile == null) {
                cssFile = cssTargetFolder.createData(layoutName, CSS_EXT);
                is = templatePanel.getLayoutCSS();
                JSFFrameworkProvider.createFile(cssFile, JSFFrameworkProvider.readResource(is, ENCODING), ENCODING);
            }
            String layoutPath = getResourceRelativePath(target, cssFile);
            cssFile = cssTargetFolder.getFileObject("default", CSS_EXT);  //NOI18N
            if (cssFile == null) {
                cssFile = cssTargetFolder.createData("default", CSS_EXT); //NOI18N
                is = templatePanel.getDefaultCSS();
                JSFFrameworkProvider.createFile(cssFile, JSFFrameworkProvider.readResource(is, ENCODING), ENCODING);
            }
            String defaultPath = getResourceRelativePath(target, cssFile);

            is = templatePanel.getTemplate();
            String content = JSFFrameworkProvider.readResource(is, ENCODING);
            if (!jsfVersion.isAtLeast(JsfVersion.JSF_2_0)) {
                content = content.replace("h:head", "head").replace("h:body", "body"); //NOI18N
            }
            String namespaceLocation = jsfVersion.isAtLeast(JsfVersion.JSF_2_2) ? NamespaceUtils.JCP_ORG_LOCATION : NamespaceUtils.SUN_COM_LOCATION;

            HashMap args = new HashMap();
            args.put("LAYOUT_CSS_PATH", layoutPath);    //NOI18N
            args.put("DEFAULT_CSS_PATH", defaultPath);  //NOI18N
            args.put("NS_LOCATION", namespaceLocation); //NOI18N
            MapFormat formater = new MapFormat(args);
            formater.setLeftBrace("__");    //NOI18N
            formater.setRightBrace("__");   //NOI18N
            formater.setExactMatch(false);
            content = formater.format(content);

            JSFFrameworkProvider.createFile(target, content, ENCODING);
            result = target;
        }

        public FileObject getResult() {
            return result;
        }

        private static String getResourceRelativePath(FileObject fromFO, FileObject toFO) {
            String relativePath = JSFUtils.getRelativePath(fromFO, toFO);
            if (relativePath.contains("resources")) {   //NOI18N
                // web resource in the resources folder (common Facelet Template)
                return "./css/" + toFO.getNameExt();    //NOI18N
            } else {
                // web resource in the subdir of the Resource Library Contract
                return relativePath;
            }
        }
    }
}
