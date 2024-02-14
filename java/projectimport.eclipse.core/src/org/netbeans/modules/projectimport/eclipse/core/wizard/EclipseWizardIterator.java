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

package org.netbeans.modules.projectimport.eclipse.core.wizard;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.netbeans.modules.projectimport.eclipse.core.ProjectFactory;
import org.netbeans.modules.projectimport.eclipse.core.ProjectImporterException;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

/**
 * Iterates on the sequence of Eclipse wizard panels.
 *
 * @author mkrauskopf
 */
final class EclipseWizardIterator implements
        WizardDescriptor.Iterator<WizardDescriptor>, ChangeListener {
    
    private String errorMessage;
    private SelectionWizardPanel workspacePanel;
    private ProjectWizardPanel projectPanel;
    private List<WizardDescriptor.Panel<WizardDescriptor>> extraPanels = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
    private List<String> currentPanelProviders = new ArrayList<String>();
    
    int numberOfPanels = 2;
    int currentPanel = 0;
    
    private final ChangeSupport cs = new ChangeSupport(this);
    
    /** Initialize and create an instance. */
    EclipseWizardIterator() {
        workspacePanel = new SelectionWizardPanel();
        workspacePanel.addChangeListener(this);
        projectPanel = new ProjectWizardPanel();
        projectPanel.addChangeListener(this);
    }

    List<Panel<WizardDescriptor>> getExtraPanels() {
        return extraPanels;
    }
    
    /** Returns projects selected by selection panel */
    List<EclipseProject> getProjects() {
        if (workspacePanel.isWorkspaceChosen()) {
            return projectPanel.getProjects();
        } else {
            try {
                File projectDirF = FileUtil.normalizeFile(new File(workspacePanel.getProjectDir()));
                return Collections.<EclipseProject>singletonList(ProjectFactory.getInstance().load(projectDirF));
            } catch (ProjectImporterException e) {
                ErrorManager.getDefault().log(ErrorManager.ERROR,
                        "ProjectImporterException catched: " + e); // NOI18N
                e.printStackTrace();
                return Collections.<EclipseProject>emptyList();
            }
        }
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    int getNumberOfImportedProject() {
        return (workspacePanel.isWorkspaceChosen() ?
            projectPanel.getNumberOfImportedProject() : 1);
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     */
    String getDestination() {
        return (workspacePanel.isWorkspaceChosen() ?
            projectPanel.getDestination() :
            workspacePanel.getProjectDestinationDir());
    }
    
    /**
     * Returns whether selected projects should be imported recursively or not.
     */
    boolean getRecursively() {
        return workspacePanel.isWorkspaceChosen();
    }
    
    public void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    
    public void previousPanel() {
        currentPanel--;
        updateErrorMessage();
    }
    
    public void nextPanel() {
        if (getCurrent() == workspacePanel) {
            projectPanel.loadProjects(workspacePanel.getWorkspaceDir());
        }
        currentPanel++;
        updateErrorMessage();
    }
    
    public String name() {
        if (getCurrent() == workspacePanel) {
            return ImporterWizardPanel.WORKSPACE_LOCATION_STEP;
        } else if (getCurrent() == projectPanel) {
            return ImporterWizardPanel.PROJECT_SELECTION_STEP;
        } else {
            return getCurrent().getComponent().getName();
        }
    }
    
    public boolean hasPrevious() {
        return currentPanel > 0;
    }
    
    public boolean hasNext() {
        return currentPanel < numberOfPanels-1;
    }
    
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return getCurrent();
    }
    
    public void stateChanged(javax.swing.event.ChangeEvent e) {
        updateExtraWizardPanels();
        updateErrorMessage();
    }
    
    private void updateExtraWizardPanels() {
        List<WizardDescriptor.Panel<WizardDescriptor>> l = new ArrayList<WizardDescriptor.Panel<WizardDescriptor>>();
        if (getCurrent() == workspacePanel) {
            numberOfPanels = workspacePanel.isWorkspaceChosen() ? 2 : 1;
        }
        if (getCurrent() != projectPanel) {
            return;
        }
        Set<String> alreadyIncluded = new HashSet<String>();
        List<String> panelProviders = new ArrayList<String>();
        for (EclipseProject ep : getProjects()) {
            if (!ep.isImportSupported()) {
                continue;
            }
            if (alreadyIncluded.contains(ep.getProjectTypeFactory().getClass().getName())) {
                continue;
            } else {
                alreadyIncluded.add(ep.getProjectTypeFactory().getClass().getName());
            }
            l.addAll(ep.getProjectTypeFactory().getAdditionalImportWizardPanels());
            panelProviders.add(ep.getProjectTypeFactory().getClass().getName());
        }
        if (panelProviders.equals(currentPanelProviders)) {
            return;
        } else {
            currentPanelProviders = panelProviders;
        }
        extraPanels = l;
        numberOfPanels = 2 + l.size();
        int index = 2;
        for (WizardDescriptor.Panel p : l) {
            JComponent comp = (JComponent)p.getComponent();
            comp.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX,  // NOI18N
                    new Integer(index));
            index++;
            comp.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getWizardPanelName(l));
        }
        ((JComponent)projectPanel.getComponent()).putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, getWizardPanelName(l));
    }
    
    private String[] getWizardPanelName(List<WizardDescriptor.Panel<WizardDescriptor>> l) {
        List<String> names = new ArrayList<String>();
        names.add(ImporterWizardPanel.WORKSPACE_LOCATION_STEP);
        names.add(ImporterWizardPanel.PROJECTS_SELECTION_STEP);
        if (l != null) {
            for (WizardDescriptor.Panel p : l) {
                JComponent comp = (JComponent)p.getComponent();
                names.add(comp.getName());
            }
        }
        return names.toArray(new String[0]);
    }
    
    void updateErrorMessage() {
        if (getCurrent() == workspacePanel) {
            errorMessage = workspacePanel.getErrorMessage();
        } else if (getCurrent() == projectPanel) {
            errorMessage = projectPanel.getErrorMessage();
        } else {
            errorMessage = null;
        }
        cs.fireChange();
    }
    
    String getErrorMessage() {
        return errorMessage;
    }

    WizardDescriptor.Panel<WizardDescriptor> getCurrent() {
        if (currentPanel == 0) {
            return workspacePanel;
        } else if (currentPanel == 1) {
            return projectPanel;
        } else {
            return extraPanels.get(currentPanel-2);
        }
    }
    
}
