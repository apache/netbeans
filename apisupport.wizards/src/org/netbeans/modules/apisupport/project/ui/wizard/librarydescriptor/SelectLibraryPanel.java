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

package org.netbeans.modules.apisupport.project.ui.wizard.librarydescriptor;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.modules.apisupport.project.ui.wizard.common.BasicWizardIterator;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Represents <em>Libraries</em> panel in J2SE Library Descriptor Wizard.
 */
final class SelectLibraryPanel extends BasicWizardIterator.Panel {
    
    private NewLibraryDescriptor.DataModel data;
    private final LibraryChooser.Panel panel;
    
    public SelectLibraryPanel(WizardDescriptor setting, NewLibraryDescriptor.DataModel data) {
        super(setting);
        this.data = data;
        getAccessibleContext().setAccessibleDescription(getMessage("ACS_SelectLibraryPanel"));
        putClientProperty("NewFileWizard_Title", getMessage("LBL_LibraryWizardTitle"));
        panel = LibraryChooser.createPanel(null, null);
        panel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (LibraryChooser.Panel.PROP_SELECTED_LIBRARIES.equals(evt.getPropertyName())) {
                    checkValidity();
                }
            }
        });
        setLayout(new BorderLayout());
        add(panel.getVisualComponent(), BorderLayout.CENTER);
    }
    
    private void checkValidity() {
        if (getSelectedLibrary() != null) {
            markValid();
        } else {
            markInvalid();
        }
    }
    
    private Library getSelectedLibrary() {
        Set<Library> selection = panel.getSelectedLibraries();
        return selection.size() == 1 ? selection.iterator().next() : null;
    }
    
    protected void storeToDataModel() {
        data.setLibrary(getSelectedLibrary());
    }
    
    protected void readFromDataModel() {
        checkValidity();
    }
    
    protected String getPanelName() {
        return getMessage("LBL_SelectLibraryPanel_Title");
    }
    
    protected HelpCtx getHelp() {
        return new HelpCtx(SelectLibraryPanel.class);
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(SelectLibraryPanel.class, key);
    }
    
}
