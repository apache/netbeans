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

package org.netbeans.modules.java.freeform.ui;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * @author  David Konecny
 */
public class ClasspathWizardPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private ClasspathPanel component;
    private WizardDescriptor wizardDescriptor;

    public ClasspathWizardPanel() {
        getComponent().setName(NbBundle.getMessage (NewJ2SEFreeformProjectWizardIterator.class, "TXT_NewJ2SEFreeformProjectWizardIterator_Classpath"));
    }
    
    public Component getComponent() {
        if (component == null) {
            component = new ClasspathPanel();
            ((JComponent)component).getAccessibleContext ().setAccessibleDescription (NbBundle.getMessage(ClasspathWizardPanel.class, "ACSD_ClasspathWizardPanel")); // NOI18N
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx( ClasspathWizardPanel.class );
    }
    
    public boolean isValid() {
        getComponent();
        return true;
    }
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public final void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public final void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        ProjectModel pm = (ProjectModel) wizardDescriptor.getProperty(NewJ2SEFreeformProjectWizardIterator.PROP_PROJECT_MODEL);
        component.setModel(pm);
        wizardDescriptor.putProperty("NewProjectWizard_Title", component.getClientProperty("NewProjectWizard_Title")); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor)settings;
        wizardDescriptor.putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isFinishPanel() {
        return true;
    }
    
}
