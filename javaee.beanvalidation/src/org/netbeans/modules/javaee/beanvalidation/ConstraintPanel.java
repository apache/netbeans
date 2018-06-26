/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javaee.beanvalidation;

import java.awt.Component;
import java.lang.reflect.Method;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.openide.WizardDescriptor;
import org.openide.WizardDescriptor.Panel;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;

/**
 *
 * @author alexeybutenko
 */
public class ConstraintPanel implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, ChangeListener{

    private ConstraintPanelVisual component;
    private TemplateWizard wizard;

    public ConstraintPanel(TemplateWizard wizard) {
        this.wizard = wizard;
    }


    @Override
    public Component getComponent() {
        if (component == null) {
            component = new ConstraintPanelVisual(wizard);
        }
        return component;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(ConstraintPanel.class);
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        wizard = (TemplateWizard) settings;
    }

    @Override
    public void storeSettings(WizardDescriptor d) {
        component.store((TemplateWizard)d);
    }

    @Override
    public boolean isValid() {
        return component.validateTemplate(wizard);
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        getComponent();
        component.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        getComponent();
        component.removeChangeListener(l);
    }


    @Override
    public boolean isFinishPanel() {
        return true;
    }

    private String oldTargetName;

    @Override
    public void stateChanged(ChangeEvent e) {
        WizardDescriptor.Panel panel = (Panel) e.getSource();
        String targetName = null;
        Component gui = panel.getComponent();
        try {
            // XXX JavaTargetChooserPanel should introduce new API to get current contents
            // of its component JavaTargetChooserPanelGUI (see Issue#154655)
            Method getTargetName = gui.getClass().getMethod("getTargetName", (Class[]) null); // NOI18N
            targetName = (String) getTargetName.invoke(gui, (Object[]) null);
        } catch (Exception ex) {
            return;
        }

        if ((targetName == null) || targetName.trim().equals("") || targetName.trim().equals(oldTargetName)) {
            return;
        }
        oldTargetName = targetName;
        ((ConstraintPanelVisual)getComponent()).updateValidatorClassName(targetName);
    }

}
