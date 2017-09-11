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

import java.awt.Dialog;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.projectimport.eclipse.core.EclipseProject;
import org.openide.DialogDisplayer;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

/**
 * Wizard for importing Eclipse project.
 *
 * @author mkrauskopf
 */
public final class ProjectImporterWizard {
    
    private List<EclipseProject> projects;
    private String destination;
    private boolean recursively;
    private boolean cancelled;
    private int numberOfImportedProjects;
    private List<WizardDescriptor.Panel<WizardDescriptor>> extraPanels;
    
    /** Starts Eclipse importer wizard. */
    public void start() {
        final EclipseWizardIterator iterator = new EclipseWizardIterator();
        final WizardDescriptor wizardDescriptor = new WizardDescriptor(iterator);
        iterator.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                        iterator.getErrorMessage());
            }
        });
        wizardDescriptor.setTitleFormat(new java.text.MessageFormat("{1}")); // NOI18N
        wizardDescriptor.setTitle(
                ProjectImporterWizard.getMessage("CTL_WizardTitle")); // NOI18N
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialog.setVisible(true);
        dialog.toFront();
        cancelled = wizardDescriptor.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            projects = iterator.getProjects();
            //showAdditionalInfo(projects);
            destination = iterator.getDestination();
            recursively = iterator.getRecursively();
            numberOfImportedProjects = iterator.getNumberOfImportedProject();
            extraPanels = iterator.getExtraPanels();
        }
    }
    
//    private void showAdditionalInfo(Set projects) {
//        StringBuffer messages = null;
//        for (Iterator it = projects.iterator(); it.hasNext(); ) {
//            EclipseProject prj = (EclipseProject) it.next();
//            Set natures = prj.getNatures();
//            if (natures != null && !natures.isEmpty()) {
//                if (messages == null) {
//                    messages = new StringBuffer(
//                            getMessage("MSG_CreatedByPlugin") + "\n\n"); // NOI18N
//                }
//                messages.append(" - " + prj.getName()); // NOI18N
//            }
//        }
//        if (messages != null) {
//            NotifyDescriptor d = new DialogDescriptor.Message(
//                    messages.toString(), NotifyDescriptor.INFORMATION_MESSAGE);
//            DialogDisplayer.getDefault().notify(d);
//        }
//    }
    
    /** Returns project selected by user with the help of the wizard. */
    public List<EclipseProject> getProjects() {
        return projects;
    }
    
    /**
     * Returns number of projects which will be imported (including both
     * required and selected projects)
     */
    public int getNumberOfImportedProject() {
        return numberOfImportedProjects;
    }
    
    /**
     * Returns destination directory where new NetBeans projects will be stored.
     */
    public String getDestination() {
        return destination;
    }
    
    public boolean getRecursively() {
        return recursively;
    }
    
    /**
     * Returns whether user canceled the wizard.
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /** Gets message from properties bundle for this package. */
    static String getMessage(String key) {
        return NbBundle.getMessage(ProjectImporterWizard.class, key);
    }
    
    /** Gets message from properties bundle for this package. */
    static String getMessage(String key, Object param1) {
        return NbBundle.getMessage(ProjectImporterWizard.class, key, param1);
    }
    
    public List<WizardDescriptor.Panel<WizardDescriptor>> getExtraPanels() {
        return extraPanels;
    }
    
}
