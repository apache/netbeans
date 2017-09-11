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

package org.netbeans.modules.apisupport.project.ui.wizard.common;

import org.netbeans.modules.apisupport.project.api.BasicWizardPanel;
import org.netbeans.modules.apisupport.project.api.BasicVisualPanel;
import java.awt.Component;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.event.ChangeListener;
import javax.swing.text.BadLocationException;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.editor.indent.api.Reformat;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import static org.netbeans.modules.apisupport.project.ui.wizard.common.Bundle.*;
import org.openide.util.NbBundle.Messages;

/**
 * Convenient class for implementing {@link org.openide.WizardDescriptor.InstantiatingIterator}.
 *
 * @author Radek Matous
 */
public abstract class BasicWizardIterator implements WizardDescriptor.AsynchronousInstantiatingIterator<WizardDescriptor> {
    
    private int position = 0;
    private BasicWizardIterator.PrivateWizardPanel[] wizardPanels;
    
    /** Create a new wizard iterator. */
    protected BasicWizardIterator() {}
    
    /** @return all panels provided by this {@link org.openide.WizardDescriptor.InstantiatingIterator} */
    protected abstract BasicWizardIterator.Panel[] createPanels(WizardDescriptor wiz);
    
    /** Basic visual panel.*/
    public abstract static class Panel extends BasicVisualPanel {
        
        protected Panel(WizardDescriptor wiz) {
            super(wiz);
        }
        
        /**
         * Returned name is used by a wizard. e.g. on its left side in the
         * <em>step list</em>.
         * @return name of panel
         */
        protected abstract String getPanelName();
        
        /**
         * Gives a chance to store an instance of {@link
         * BasicWizardIterator.BasicDataModel}. It is called when a panel is
         * going to be <em>hidden</em> (e.g. when switching to next/previous
         * panel).
         */
        protected abstract void storeToDataModel();
        
        /**
         * Gives a chance to refresh a panel (usually by reading a state of an
         * instance of {@link BasicWizardIterator.BasicDataModel}. It is called
         * when a panel is going to be <em>displayed</em> (e.g. when switching
         * from next/previous panel).
         */
        protected abstract void readFromDataModel();
        
        protected abstract HelpCtx getHelp();
        
    }
    
    /** DataModel that is passed through individual panels.*/
    public static class BasicDataModel {
        
        private Project project;
        private NbModuleProvider module;
        private SourceGroup sourceRootGroup;
        private String packageName;
        
        /** Creates a new instance of NewFileDescriptorData */
        public BasicDataModel(WizardDescriptor wiz) {
            Project tmpProject = Templates.getProject(wiz);
            
            if (tmpProject == null) {
                throw new IllegalArgumentException();
            }
            module = tmpProject.getLookup().lookup(NbModuleProvider.class);
            if (module == null) {
                throw new IllegalArgumentException(tmpProject.getClass().toString());
            }
            
            project = tmpProject;
            // #66339 need to prefetch the packagename and populate data with it..
            FileObject fo = Templates.getTargetFolder(wiz);
            if (fo != null) {
                Sources srcs = ProjectUtils.getSources(project); // #63247: don't use lookup directly
                SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int i = 0; i < grps.length; i++) {
                    if (FileUtil.isParentOf(grps[i].getRootFolder(), fo)) {
                        String relPath = FileUtil.getRelativePath(grps[i].getRootFolder(), fo);
                        relPath = relPath.replace('/', '.');
                        setPackageName(relPath);
                        break;
                    }
                }
            }
        }
        
        public Project getProject() {
            return project;
        }
        
        public NbModuleProvider getModuleInfo() {
            return module;
        }
        
        public String getPackageName() {
            return packageName;
        }
        
        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }
        
        public SourceGroup getSourceRootGroup() {
            if (sourceRootGroup == null) {
                FileObject tempSrcRoot = getModuleInfo().getSourceDirectory();
                assert tempSrcRoot != null;
                
                Sources sources = ProjectUtils.getSources(project);
                SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                for (int i = 0; sourceRootGroup == null && i < groups.length; i++) {
                    if (groups[i].getRootFolder().equals(tempSrcRoot)) {
                        sourceRootGroup = groups[i];
                    }
                }
            }
            return sourceRootGroup;
        }
        
        public String getDefaultPackagePath(String fileName, boolean resource) {
            return getDefaultPackagePath(fileName, resource, false);
        }

        public String getDefaultPackagePath(String fileName, boolean resource, boolean inTests) {
            if (inTests) {
                String path;
                if (resource) {
                    path = getModuleInfo().getResourceDirectoryPath(true);
                } else {
                    path = getModuleInfo().getTestSourceDirectoryPath();
                }
                return path + '/'
                        + getPackageName().replace('.', '/') + '/' + fileName;
            }
            
            return (resource ? getModuleInfo().getResourceDirectoryPath(inTests) : getModuleInfo().getSourceDirectoryPath()) + '/' +
                    getPackageName().replace('.','/') + '/' + fileName;
        }
        
        /**
         * Conditionally adds an operation to the given {@link
         * CreatedModifiedFiles}. Result of the operation, after given
         * CreatedModifiedFiles are run, is copied (into the package) icon
         * representing given <code>origIconPath</code>. If the origIconPath is
         * already inside the project's source directory nothing happens.
         *
         * @return path of the icon relative to the project's source directory
         */
        public String addCreateIconOperation(CreatedModifiedFiles cmf, @NonNull FileObject originalIcon) {
            String relativeIconPath = null;
            if (!FileUtil.isParentOf(Util.getResourceDirectory(getProject()), originalIcon)) {
                String iconPath = getDefaultPackagePath(originalIcon.getNameExt(), true);
                cmf.add(cmf.createFile(iconPath, originalIcon));
                relativeIconPath = getPackageName().replace('.', '/') + '/' + originalIcon.getNameExt();
            } else {
                relativeIconPath = FileUtil.getRelativePath(Util.getResourceDirectory(getProject()), originalIcon);
            }
            return relativeIconPath;
        }
        
    }
    
    public static class InvalidProjectPanel extends Panel {

        @Messages("LBL_InvalidProjectMsg=Unable to finish selected wizard. Project is either not initialized or of wrong type.")
        public InvalidProjectPanel(WizardDescriptor wiz) {
            super(wiz);
            setError(LBL_InvalidProjectMsg());
        }

        @Messages("LBL_InvalidProject=Invalid Project")
        protected @Override String getPanelName() {
            return LBL_InvalidProject();
        }

        @Override
        protected HelpCtx getHelp() {
            return new HelpCtx(InvalidProjectPanel.class);
        }

        @Override
        protected void readFromDataModel() {
            // nothing
        }

        @Override
        protected void storeToDataModel() {
            // nothing
        }
        
    }

    public void initialize(WizardDescriptor wiz) {
        // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
        String[] beforeSteps = null;
        Object prop = wiz.getProperty(WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        position = 0;

        // #176828: do not continue with wizard if called on non-NBM project;
        // can be e.g. LazyProject or project without preferred templates
        Project tmpProject = Templates.getProject(wiz);
        boolean isValidPrj = (tmpProject != null && tmpProject.getLookup().lookup(NbModuleProvider.class) != null);
        Panel[] panels = isValidPrj ? createPanels(wiz)
                : new Panel[] { new InvalidProjectPanel(wiz) };
        String[] steps = BasicWizardIterator.createSteps(beforeSteps, panels);
        wizardPanels = new BasicWizardIterator.PrivateWizardPanel[panels.length];
        
        for (int i = 0; i < panels.length; i++) {
            wizardPanels[i] = new BasicWizardIterator.PrivateWizardPanel(panels[i], steps, i);
        }
    }
    
    // mkleint: copied from the NewJavaFileWizardIterator.. there must be something painfully wrong..
    private static String[] createSteps(String[] before, BasicWizardIterator.Panel[] panels) {
        assert panels != null;
        // hack to use the steps set before this panel processed
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals(before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getPanelName();
            }
        }
        return res;
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        wizardPanels = null;
    }
    
    public String name() {
        return ((BasicWizardIterator.PrivateWizardPanel)
        current()).getPanel().getPanelName();
    }
    
    public boolean hasNext() {
        return position < (wizardPanels.length - 1);
    }
    
    public boolean hasPrevious() {
        return position > 0;
    }
    
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        position++;
    }
    
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        position--;
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return wizardPanels[position];
    }
    
    public final void addChangeListener(ChangeListener  l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
    public static Set<FileObject> getCreatedFiles(final CreatedModifiedFiles cmf, final Project project) throws IOException {
        Collection<DataObject> toBeShown = new HashSet<DataObject>();
        Set<FileObject> set = new HashSet<FileObject>();
        for (String path : cmf.getCreatedPaths()) {
            FileObject fo = project.getProjectDirectory().getFileObject(path);
            assert fo != null : path;
            formatFile(fo);
            DataObject dObj = DataObject.find(fo);
            if (dObj != null && toBeShown.size() < 10 && toBeShown.add(dObj)) {
                set.add(fo);
            }
        }
        return set;
    }
    
    private static BaseDocument getDocument(final FileObject fo) throws DataObjectNotFoundException, IOException {
        BaseDocument doc = null;
        DataObject dObj = DataObject.find(fo);
        if (dObj != null) {
            EditorCookie editor = dObj.getLookup().lookup(EditorCookie.class);
            if (editor != null) {
                doc = (BaseDocument) editor.openDocument();
            }
        }
        return doc;
    }
    
    // copy-pasted-adjusted from org.netbeans.editor.ActionFactory.FormatAction
    private static void formatFile(final FileObject fo) {
        assert fo != null;
        final BaseDocument doc;
        try {
            doc = BasicWizardIterator.getDocument(fo);
            if (doc == null) {
                return;
            }
            final Reformat reformat = Reformat.get(doc);
            reformat.lock();
            try {
                doc.runAtomic(new Runnable() {
                    public void run() {
                        try {
                            reformat.reformat(0, doc.getLength());
                        } catch (BadLocationException x) {
                            throw new RuntimeException(x);
                        }
                    }
                });
            } finally {
                reformat.unlock();
            }
            try {
                DataObject.find(fo).getLookup().lookup(EditorCookie.class).saveDocument();
            } catch (IOException e) {
                Util.err.notify(ErrorManager.INFORMATIONAL, e);
            }
        } catch (Exception ex) {
            // no disaster
            ErrorManager.getDefault().log(ErrorManager.WARNING, "Cannot reformat the file: " + fo.getPath()); // NOI18N
        }
    }
    
    private static final class PrivateWizardPanel extends BasicWizardPanel {
        
        private BasicWizardIterator.Panel panel;
        
        PrivateWizardPanel(BasicWizardIterator.Panel panel, String[] allSteps, int stepIndex) {
            super(panel.getSettings());
            panel.addPropertyChangeListener(this);
            panel.setName(panel.getPanelName()); // NOI18N
            this.panel = panel;
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(stepIndex)); // NOI18N
            // names of currently used steps
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, allSteps); // NOI18N
        }
        
        private BasicWizardIterator.Panel getPanel() {
            return panel;
        }
        
        public Component getComponent() {
            return getPanel();
        }
        
        public @Override void storeSettings(WizardDescriptor wiz) {
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) ||
                    WizardDescriptor.FINISH_OPTION.equals(wiz.getValue())) {
                panel.storeToDataModel();
            }
            //XXX hack
            wiz.putProperty("NewFileWizard_Title", null); // NOI18N
        }
        
        public @Override void readSettings(WizardDescriptor wiz) {
            // mkleint - copied from someplace.. is definitely weird..
            // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
            // this name is used in NewProjectWizard to modify the title
            Object substitute = getPanel().getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wiz.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
            
            if (WizardDescriptor.NEXT_OPTION.equals(wiz.getValue()) || wiz.getValue() == null) {
                panel.readFromDataModel();
            }
        }
        
        public @Override HelpCtx getHelp() {
            return getPanel().getHelp();
        }
        
    }
    
}

