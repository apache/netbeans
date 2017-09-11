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

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import org.netbeans.modules.projectimport.eclipse.core.EclipseUtils;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;

/**
 * Selection wizard panel for Eclipse Wizard importer.
 *
 * @author mkrauskopf
 */
final class SelectionWizardPanel extends ImporterWizardPanel implements
        PropertyChangeListener, WizardDescriptor.ValidatingPanel<WizardDescriptor> {
    
    private SelectionPanel panel;
    
    /** Creates a new instance of WorkspaceWizardPanel */
    SelectionWizardPanel() {
        panel = new SelectionPanel();
        panel.addPropertyChangeListener(this);
        initPanel(panel, 0);
    }
    
    public Component getComponent() {
        return panel;
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        String propName = evt.getPropertyName();
        if ("errorMessage".equals(propName)) { //NOI18N
            setErrorMessage((String) evt.getNewValue());
        } else if ("workspaceChoosen".equals(propName)) { // NOI18N
            String[] steps;
            if (((Boolean) evt.getNewValue()).booleanValue()) {
                steps = new String[] { WORKSPACE_LOCATION_STEP, PROJECTS_SELECTION_STEP };
            } else {
                steps = new String[] { PROJECT_SELECTION_STEP };
            }
            panel.putClientProperty(WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
            // force Next and Finish buttons state refresh:
            setValid(isValid(), true);
        }
    }
    
    // ==== delegate methods ==== //

    boolean isWorkspaceChosen() {
        return panel.isWorkspaceChosen();
    }
    
    /** Returns project directory of single-selected project. */
    String getProjectDir() {
        return panel.getProjectDir();
    }
    
    /** Returns destination directory for single-selected project. */
    public String getProjectDestinationDir() {
        return panel.getProjectDestinationDir();
    }
    
    /** Returns workspace directory choosed by user. */
    public File getWorkspaceDir() {
        return panel.getWorkspaceDir();
    }
    
    public void validate() throws WizardValidationException {
        if (!panel.isWorkspaceChosen()) {
            String dest = getProjectDestinationDir();

            String message = null;
            if (dest != null && ((!new File(dest).isAbsolute()) || !EclipseUtils.isWritable(dest))) {
                message = ProjectImporterWizard.getMessage(
                        "MSG_CannotCreateProjectInFolder", dest); // NOI18N
            } else if (!EclipseUtils.isRegularProject(getProjectDir())) {
                message = ProjectImporterWizard.getMessage(
                        "MSG_CannotImportNonJavaProject"); // NOI18N
            }
            if (message != null) {
                setErrorMessage(message);
                throw new WizardValidationException(panel, message, null);
            }
        }
    }
}
