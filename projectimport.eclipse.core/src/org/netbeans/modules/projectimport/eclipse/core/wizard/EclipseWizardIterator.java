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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
        return names.toArray(new String[names.size()]);
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
