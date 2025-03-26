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
package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.target.iterator.api.TargetChooserPanel;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.api.webmodule.WebProjectConstants;
import org.netbeans.modules.web.core.Util;
import org.netbeans.modules.web.taglib.TLDDataObject;
import org.netbeans.modules.web.taglib.TaglibCatalog;
import org.netbeans.modules.web.taglib.model.TagFileType;
import org.netbeans.modules.web.taglib.model.Taglib;
import org.netbeans.modules.web.wizards.targetpanel.providers.TagLibTargetPanelProvider;
import org.netbeans.modules.web.wizards.targetpanel.providers.TagTargetPanelProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;

/** A template wizard iterator (sequence of panels).
 * Used to fill in the second and subsequent panels in the New wizard.
 * Associate this to a template inside a layer using the
 * Sequence of Panels extra property.
 * Create one or more panels from template as needed too.
 *
 * @author  Milan Kuchtiak
 */
public class PageIterator implements TemplateWizard.Iterator {

    private static final Logger LOG = Logger.getLogger(PageIterator.class.getName());
    private static final long serialVersionUID = -7586964579556513549L;
    private transient FileType fileType;
    private WizardDescriptor.Panel folderPanel;
    private transient SourceGroup[] sourceGroups;

    public static PageIterator createJspIterator() {
        return new PageIterator(FileType.JSP);
    }

    public static PageIterator createJsfIterator() {
        return new PageIterator(FileType.JSF);
    }

    public static PageIterator createTagIterator() {
        return new PageIterator(FileType.TAG);
    }

    public static PageIterator createTagLibraryIterator() {
        return new PageIterator(FileType.TAGLIBRARY);
    }

    public static PageIterator createHtmlIterator() {
        return new PageIterator(FileType.HTML);
    }
    
    public static PageIterator createJSIterator() {
        return new PageIterator(FileType.JS);
    }

    public static PageIterator createXHtmlIterator() {
        return new PageIterator(FileType.XHTML);
    }

    public static PageIterator createXCssIterator() {
        return new PageIterator(FileType.CSS);
    }

    protected PageIterator(FileType fileType) {
        this.fileType = fileType;
    }

    // You should define what panels you want to use here:
    protected WizardDescriptor.Panel[] createPanels(Project project) {
        Sources sources = (Sources) project.getLookup().lookup(org.netbeans.api.project.Sources.class);
        if (fileType.equals(FileType.JSP)||fileType.equals(FileType.JSF)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            //folderPanel = new TargetChooserPanel(project, sourceGroups, fileType);
            folderPanel = new PageIteratorValidation.JsfJspValidatorPanel(
                    new TargetChooserPanel<FileType>(project, sourceGroups, fileType));

            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        } else if (fileType.equals(FileType.HTML) || fileType.equals(FileType.XHTML) 
                || fileType.equals(FileType.CSS) || fileType.equals(FileType.JS) ) 
        {
            SourceGroup[] docRoot = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            SourceGroup[] srcRoots = Util.getJavaSourceGroups(project);
            if (docRoot != null && srcRoots != null) {
                sourceGroups = new SourceGroup[docRoot.length + srcRoots.length];
                System.arraycopy(docRoot, 0, sourceGroups, 0, docRoot.length);
                System.arraycopy(srcRoots, 0, sourceGroups, docRoot.length, srcRoots.length);
            }
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel<FileType>(project, sourceGroups, 
                    fileType);
            
            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        } else if (fileType.equals(FileType.TAG)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_WEB_INF);
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            }
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            }
            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel<FileType>(project, sourceGroups, fileType );
            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        } else if (fileType.equals(FileType.TAGLIBRARY)) {
            sourceGroups = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            SourceGroup[] docRoot = sources.getSourceGroups(WebProjectConstants.TYPE_DOC_ROOT);
            SourceGroup[] webInfGroups = sources.getSourceGroups(WebProjectConstants.TYPE_WEB_INF);
            if (docRoot == null || docRoot.length == 0) {
                docRoot = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            }

            if (docRoot != null && webInfGroups != null) {
                sourceGroups = new SourceGroup[docRoot.length + webInfGroups.length];
                System.arraycopy(webInfGroups, 0, sourceGroups, 0, webInfGroups.length);
                System.arraycopy(docRoot, 0, sourceGroups, webInfGroups.length, docRoot.length);
            }

            if (sourceGroups == null || sourceGroups.length == 0) {
                sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
            }
            folderPanel = new TargetChooserPanel<FileType>(project, sourceGroups, 
                    fileType);
            return new WizardDescriptor.Panel[]{
                        folderPanel
                    };
        }
        return new WizardDescriptor.Panel[]{
                    Templates.createSimpleTargetChooser(project, sourceGroups)
                };
    }

    private static boolean isJSF20(WebModule wm) {
        ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        return classpath != null && classpath.findResource("javax/faces/application/ProjectStage.class") != null; //NOI18N
    }

    private static boolean isJSF22(WebModule wm) {
        ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        return classpath != null && classpath.findResource("javax/faces/flow/Flow.class") != null; //NOI18N
    }
    
    private static boolean isJSF30(WebModule wm) {
        ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        return classpath != null && classpath.findResource("jakarta/faces/flow/Flow.class") != null; //NOI18N
    }

    private static boolean isJSF40(WebModule wm) {
        ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        return classpath != null && classpath.findResource("jakarta/faces/lifecycle/ClientWindowScoped.class") != null; //NOI18N
    }
    
    private static boolean isJSF41(WebModule wm) {
        ClassPath classpath = ClassPath.getClassPath(wm.getDocumentBase(), ClassPath.COMPILE);
        return classpath != null && classpath.findResource("jakarta/faces/convert/UUIDConverter.class") != null; //NOI18N
    }

    public Set<DataObject> instantiate(TemplateWizard wiz) throws IOException {
        // Here is the default plain behavior. Simply takes the selected
        // template (you need to have included the standard second panel
        // in createPanels(), or at least set the properties targetName and
        // targetFolder correctly), instantiates it in the provided
        // position, and returns the result.
        // More advanced wizards can create multiple objects from template
        // (return them all in the result of this method), populate file
        // contents on the fly, etc.

        org.openide.filesystems.FileObject dir = Templates.getTargetFolder(wiz);
        DataFolder df = DataFolder.findFolder(dir);
        FileObject template = Templates.getTemplate(wiz);
        FileObject templateParent = template.getParent();
        
        Map<String, Object> wizardProps = new HashMap<>();
        String defaultNamespace = null;

        if (FileType.JSP.equals(fileType) || FileType.JSF.equals(fileType)) {
            if (isSegment(wiz)) {
                if (isXml(wiz)) {
                    template = templateParent.getFileObject("JSPFX", "jspf"); //NOI18N
                } else {
                    template = templateParent.getFileObject("JSPF", "jspf"); //NOI18N
                }
            } else {
                if (isXml(wiz)) {
                    template = templateParent.getFileObject("JSPX", "jspx"); //NOI18N
                }
                if (isFacelets(wiz)) {
                    template = templateParent.getFileObject("JSP", "xhtml"); //NOI18N
                    WebModule wm = WebModule.getWebModule(df.getPrimaryFile());
                    if (wm != null) {
                        if (isJSF41(wm)) {
                            wizardProps.put("isJSF41", Boolean.TRUE);
                        } else if (isJSF40(wm)) {
                            wizardProps.put("isJSF40", Boolean.TRUE);
                        } else if (isJSF30(wm)) {
                            wizardProps.put("isJSF30", Boolean.TRUE);
                        } else if (isJSF22(wm)) {
                            wizardProps.put("isJSF22", Boolean.TRUE);
                        } else if (isJSF20(wm)) {
                            wizardProps.put("isJSF20", Boolean.TRUE);
                        }
                    }
                }
            }
        } else if (FileType.TAG.equals(fileType)) {
            if (isSegment(wiz)) {
                if (isXml(wiz)) {
                    template = templateParent.getFileObject("TagFileFX", "tagf"); //NOI18N
                } else {
                    template = templateParent.getFileObject("TagFileF", "tagf"); //NOI18N
                }
            } else {
                if (isXml(wiz)) {
                    template = templateParent.getFileObject("TagFileX", "tagx"); //NOI18N
                }
            }
        } else if (FileType.TAGLIBRARY.equals(fileType)) {
            WebModule wm = WebModule.getWebModule(dir);
            if (wm != null) {
                Profile j2eeVersion = wm.getJ2eeProfile();
                if (Profile.J2EE_13.equals(j2eeVersion)) {
                    template = templateParent.getFileObject("TagLibrary_1_2", "tld"); //NOI18N
                    defaultNamespace = TaglibCatalog.J2EE_NS;
                } else if (Profile.J2EE_14.equals(j2eeVersion)) {
                    template = templateParent.getFileObject("TagLibrary_2_0", "tld"); //NOI18N
                    defaultNamespace = TaglibCatalog.J2EE_NS;
                }
            }
        }
        DataObject dTemplate = DataObject.find(template);
        DataObject dobj = dTemplate.createFromTemplate(df, Templates.getTargetName(wiz), wizardProps);
        if (dobj != null) {
            if (FileType.TAGLIBRARY.equals(fileType)) { //TLD file 
                TLDDataObject tldDO = (TLDDataObject) dobj;
                Taglib taglib = tldDO.getTaglib();
                if (defaultNamespace != null) {
                    taglib.setDefaultNamespace(defaultNamespace);
                }
                taglib.setUri(wiz.getProperty(TagLibTargetPanelProvider.URI).toString());
                taglib.setShortName(wiz.getProperty(
                        TagLibTargetPanelProvider.PREFIX).toString());
                tldDO.write(taglib);
            } else if (FileType.TAG.equals(fileType) && 
                    (Boolean)wiz.getProperty(TagTargetPanelProvider.IS_TLD_SELECTED)) 
            { //Write Tag File to TLD 
                FileObject tldFo = (FileObject)wiz.getProperty(
                        TagTargetPanelProvider.TLD_FILE_OBJECT);
                if (tldFo != null) {
                    if (!tldFo.canWrite()) {
                        String mes = java.text.MessageFormat.format(
                                NbBundle.getMessage(PageIterator.class, "MSG_tldRO"),
                                new Object[]{tldFo.getNameExt()});
                        org.openide.NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(mes,
                                org.openide.NotifyDescriptor.Message.ERROR_MESSAGE);
                        org.openide.DialogDisplayer.getDefault().notify(desc);
                    } else {
                        TLDDataObject tldDO = (TLDDataObject) DataObject.find(tldFo);
                        Taglib taglib = null;
                        try {
                            taglib = tldDO.getTaglib();
                        } catch (IOException ex) {
                            String mes = java.text.MessageFormat.format(
                                    NbBundle.getMessage(PageIterator.class, "MSG_tldCorrupted"),
                                    new Object[]{tldFo.getNameExt()});
                            org.openide.NotifyDescriptor desc = new org.openide.NotifyDescriptor.Message(mes,
                                    org.openide.NotifyDescriptor.Message.ERROR_MESSAGE);
                            org.openide.DialogDisplayer.getDefault().notify(desc);
                        }
                        if (taglib != null) {
                            TagFileType tag = new TagFileType();
                            tag.setName(wiz.getProperty( 
                                    TagTargetPanelProvider.TAG_NAME).toString());
                            String packageName = null;
                            for (int i = 0; i < sourceGroups.length && packageName == null; i++) {
                                FileObject rootFolder = sourceGroups[i].getRootFolder();
                                packageName = rootFolder.getName()+"/"+org.openide.filesystems.FileUtil.getRelativePath(rootFolder, dobj.getPrimaryFile());
                            }
                            tag.setPath("/" +packageName); //NOI18N
                            taglib.addTagFile(tag);
                            SaveCookie save = (SaveCookie) tldDO.getCookie(SaveCookie.class);
                            if (save != null) {
                                save.save();
                            }
                            try {
                                tldDO.write(taglib);
                            } catch (IOException ex) {
                                LOG.log(Level.WARNING, null, ex);
                            }
                        }
                    }
                }
            }
        }
        return Collections.singleton(dobj);
    }
    
    private boolean isXml(TemplateWizard wiz ){
        return (Boolean)wiz.getProperty(FileType.IS_XML);
    }
    
    private boolean isSegment(TemplateWizard wiz ){
        return (Boolean)wiz.getProperty(FileType.IS_SEGMENT);
    }
    
    private boolean isFacelets(TemplateWizard wiz ){
        return (Boolean)wiz.getProperty(FileType.IS_FACELETS);
    }
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient TemplateWizard wiz;

    // You can keep a reference to the TemplateWizard which can
    // provide various kinds of useful information such as
    // the currently selected target name.
    // Also the panels will receive wiz as their "settings" object.
    public void initialize(TemplateWizard wiz) {
        this.wiz = wiz;
        index = 0;
        Project project = Templates.getProject(wiz);
        panels = createPanels(project);

        // Creating steps.
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop instanceof String[]) {
            beforeSteps = (String[]) prop;
        }
        String[] steps = Utilities.createSteps(beforeSteps, panels);

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

    public void uninitialize(TemplateWizard wiz) {
        this.wiz = null;
        panels = null;
    }

    // --- WizardDescriptor.Iterator METHODS: ---
    // Note that this is very similar to WizardDescriptor.Iterator, but with a
    // few more options for customization. If you e.g. want to make panels appear
    // or disappear dynamically, go ahead.
    public String name() {
        return NbBundle.getMessage(PageIterator.class, "TITLE_x_of_y",
                index + 1, panels.length);
    }

    public boolean hasNext() {
        return index < panels.length - 1;
    }

    public boolean hasPrevious() {
        return index > 0;
    }

    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    public WizardDescriptor.Panel current() {
        return panels[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {
    }

    public final void removeChangeListener(ChangeListener l) {
    }
    // If something changes dynamically (besides moving between panels),
    // e.g. the number of panels changes in response to user input, then
    // uncomment the following and call when needed:
    // fireChangeEvent ();
}
