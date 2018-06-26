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

package org.netbeans.modules.web.project.ui.wizards;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.web.api.webmodule.ExtenderController;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * Panel asking for web frameworks to use.
 * @author Radko Najman
 */
final class PanelSupportedFrameworks implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel {

    private ExtenderController controller = ExtenderController.create();
    private WizardDescriptor wizardDescriptor;
    private PanelSupportedFrameworksVisual component;
    private NewWebProjectWizardIterator iterator = null;
    
    /** Create the wizard panel descriptor. */
    public PanelSupportedFrameworks(NewWebProjectWizardIterator iterator) {
        this.iterator = iterator;
    }
    
    public boolean isFinishPanel() {
        return true;
    }

    public Component getComponent() {
        if (component == null)
            component = new PanelSupportedFrameworksVisual(this, controller, null, PanelSupportedFrameworksVisual.ALL_FRAMEWORKS, null);

        return component;
    }
    
    public HelpCtx getHelp() {
        HelpCtx helpCtx = null;
        if (component != null && (helpCtx = component.getHelpCtx())!=null)
            return helpCtx;
        return new HelpCtx(PanelSupportedFrameworks.class);
    }
    
    public boolean isValid() {

        if(iterator.isIstantiating == true)
            return false;

        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected void fireChangeEvent() {
        changeSupport.fireChange();
    }
    
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }
    
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
}
