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

package org.netbeans.modules.apisupport.project.ui.wizard;

import java.awt.Component;
import java.io.File;
import java.util.regex.Pattern;
import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;

/**
 * First panel of <code>NewNbModuleWizardIterator</code>. Allows user to enter
 * basic module information:
 *
 * <ul>
 *  <li>Project name</li>
 *  <li>Project Location</li>
 *  <li>Project Folder</li>
 *  <li>If should be set as a Main Project</li>
 *  <li>NetBeans Platform (for standalone modules)</li>
 *  <li>Module Suite (for suite modules)</li>
 * </ul>
 *
 * @author Martin Krauskopf
 */
final class BasicInfoWizardPanel extends NewTemplatePanel implements WizardDescriptor.ValidatingPanel<WizardDescriptor> {
    
    /** Representing visual component for this step. */
    private BasicInfoVisualPanel visualPanel;
    
    /** Creates a new instance of BasicInfoWizardPanel */
    public BasicInfoWizardPanel(final NewModuleProjectData data) {
        super(data);
    }
    
    public void reloadData() {
        getVisualPanel().refreshData();
    }
    
    public void storeData() {
        getVisualPanel().storeData();
    }
    
    private BasicInfoVisualPanel getVisualPanel() {
        return (BasicInfoVisualPanel) getComponent();
    }
    
    public Component getComponent() {
        if (visualPanel == null) {
            visualPanel = new BasicInfoVisualPanel(getData());
            visualPanel.addPropertyChangeListener(WeakListeners.propertyChange(this, visualPanel));
            visualPanel.setName(NbBundle.getMessage(BasicInfoWizardPanel.class, "LBL_BasicInfoPanel_Title"));
            visualPanel.updateAndCheck();
        }
        return visualPanel;
    }
    
    public @Override HelpCtx getHelp() {
        return new HelpCtx(BasicInfoWizardPanel.class.getName() + "_" + getWizardTypeString());
    }
    
    public void validate() throws WizardValidationException {
        // XXX this is little strange. Since this method is called first time the panel appears.
        // So we have to do this null check (data are uninitialized)
        String prjFolder = getData().getProjectFolder();
        if (prjFolder != null) {
            File prjFolderF = new File(prjFolder);
            String name = getData().getProjectName();

            String pattern;
            String forbiddenChars;
            if (Utilities.isWindows()) {
                pattern = ".*[\\/:*?\"<>|].*";    // NOI18N
                forbiddenChars = "\\ / : * ? \" < > |";    // NOI18N
            } else {
                pattern = ".*[\\/].*";    // NOI18N
                forbiddenChars = "\\ /";    // NOI18N
            }
            // #145574: check for forbidden characters in FolderObject
            if (Pattern.matches(pattern, name)) {
                String message = NbBundle.getMessage(BasicInfoWizardPanel.class, "MSG_ProjectFolderInvalidCharacters");
                message = String.format(message, forbiddenChars);
                throw new WizardValidationException(getVisualPanel().nameValue, message, message);
            }
                    if (prjFolderF.mkdir()) {
                prjFolderF.delete();
            } else {
                String message = NbBundle.getMessage(BasicInfoWizardPanel.class, "MSG_UnableToCreateProjectFolder");
                throw new WizardValidationException(getVisualPanel().nameValue, message, message);
            }
        }
    }
    
}
