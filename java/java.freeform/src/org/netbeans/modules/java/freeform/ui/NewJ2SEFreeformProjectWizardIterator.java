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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.ant.freeform.spi.TargetDescriptor;
import org.netbeans.modules.ant.freeform.spi.support.NewFreeformProjectSupport;
import org.netbeans.modules.java.freeform.spi.support.NewJavaFreeformProjectSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class NewJ2SEFreeformProjectWizardIterator implements WizardDescriptor.ProgressInstantiatingIterator {

    public static final String PROP_PROJECT_MODEL = "projectModel"; // <List> NOI18N
    
    private static final long serialVersionUID = 1L;
    
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;
    private transient WizardDescriptor wiz;
    private final AtomicBoolean hasNextCalled = new AtomicBoolean();
    
    public NewJ2SEFreeformProjectWizardIterator() {
    }
    
    private WizardDescriptor.Panel[] createPanels () {
        return new WizardDescriptor.Panel[] {
                NewFreeformProjectSupport.createBasicProjectInfoWizardPanel(),
                NewFreeformProjectSupport.createTargetMappingWizardPanel(new ArrayList<TargetDescriptor>()), // NOI18N
                new SourceFoldersWizardPanel(),
                new ClasspathWizardPanel(),
                new OutputWizardPanel(),
            };
    }
    
    public Set<?/*FileObject*/> instantiate() throws IOException {
        throw new AssertionError();
    }

    public Set<?/*FileObject*/> instantiate(final ProgressHandle handle) throws IOException {
        handle.start(6);
        final WizardDescriptor wiz = this.wiz;
        final IOException[] ioe = new IOException[1];
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                try {
                    AntProjectHelper helper = NewFreeformProjectSupport.instantiateBasicProjectInfoWizardPanel(wiz);
                    handle.progress(1);
                    NewFreeformProjectSupport.instantiateTargetMappingWizardPanel(helper, wiz);
                    handle.progress(2);
                    ProjectModel pm = (ProjectModel)wiz.getProperty(PROP_PROJECT_MODEL);
                    ProjectModel.instantiateJavaProject(helper, pm);
                    handle.progress(3);
                    Project p = ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                    handle.progress(4);
                    ProjectManager.getDefault().saveProject(p);
                    handle.progress(5);
                } catch (IOException e) {
                    ioe[0] = e;
                    return;
                }
            }});
        if (ioe[0] != null) {
            throw ioe[0];
        }
        ProjectModel pm = (ProjectModel)wiz.getProperty(PROP_PROJECT_MODEL);
        File nbProjectFolder = pm.getNBProjectFolder();
        Set<FileObject> resultSet = new HashSet<FileObject>();
        resultSet.add(FileUtil.toFileObject(nbProjectFolder));
        Project p = ProjectManager.getDefault().findProject(FileUtil.toFileObject(nbProjectFolder));
        if (p != null) {
            Sources srcs = ProjectUtils.getSources(p);
            if (srcs != null) {
                SourceGroup[] grps = srcs.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                if (grps != null && grps.length > 0) {
                    resultSet.add(grps[0].getRootFolder());
                }
            }
        }
        
        File f = nbProjectFolder.getParentFile();
        if (f != null) {
            ProjectChooser.setProjectsFolder(f);
        }
        handle.finish();
        return resultSet;
    }
    
        
    public void initialize(WizardDescriptor wiz) {
        this.wiz = wiz;
        index = 0;
        panels = createPanels();
        
        List<String> l = new ArrayList<String>();
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            l.add(jc.getName());
        }
        String[] steps = l.toArray(new String[0]);
        
        for (int i = 0; i < panels.length; i++) {
            Component c = panels[i].getComponent();
            assert c instanceof JComponent;
            JComponent jc = (JComponent)c;
            // Step #.
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(i));
            // Step name (actually the whole list for reference).
            jc.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps);
            // set title
            jc.putClientProperty ("NewProjectWizard_Title", NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_NewProjectWizardTitle")); // NOI18N
        }
    }
    
    public void uninitialize(WizardDescriptor wiz) {
        NewFreeformProjectSupport.uninitializeBasicProjectInfoWizardPanel(wiz);
        NewFreeformProjectSupport.uninitializeTargetMappingWizardPanel(wiz);
        NewJavaFreeformProjectSupport.uninitializeJavaPanels(wiz);
        this.wiz = null;
        panels = null;
    }
    
    public String name() {
        return NbBundle.getMessage(NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_TitleFormat", index + 1, panels.length);
    }
    
    public boolean hasNext() {
        hasNextCalled.set(true);
        return hasNextImpl();
    }
    //where
    private boolean hasNextImpl() {
        if (current() instanceof SourceFoldersWizardPanel) {
            assert current().getComponent() instanceof SourceFoldersPanel;
            SourceFoldersPanel sfp = (SourceFoldersPanel)current().getComponent();
            if (!sfp.hasSomeSourceFolder()) {
                return false;
            }
        }
        return index < panels.length - 1;
    }
    
    public boolean hasPrevious() {
        return index > 0;
    }
    public void nextPanel() {
        final boolean hnc = hasNextCalled.getAndSet(false);
        if (!hasNextImpl()) {
            throw new NoSuchElementException(
               MessageFormat.format(
                "index: {0}, panels: {1}, called has next: {2}",
                index,
                panels.length,
                hnc
                ));
        }
        index++;
    }
    public void previousPanel() {
        if (!hasPrevious()) throw new NoSuchElementException();
        index--;
    }
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener(ChangeListener l) {}
    public final void removeChangeListener(ChangeListener l) {}
    
}
