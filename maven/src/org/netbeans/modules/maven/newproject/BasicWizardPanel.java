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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.newproject;

import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.api.archetype.Archetype;
import static org.netbeans.modules.maven.newproject.Bundle.*;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle.Messages;

public class BasicWizardPanel implements WizardDescriptor.FinishablePanel<WizardDescriptor> {
    
    private WizardDescriptor wizardDescriptor;
    private BasicPanelVisual component;

    private final boolean isFinish;
    private final boolean additional;
    private final ValidationGroup validationGroup;
    private final Archetype arch;
    
    public BasicWizardPanel(ValidationGroup vg, @NullAllowed Archetype arch, boolean isFinish, boolean additional) {
        this.isFinish = isFinish;
        this.additional = additional;
        this.validationGroup = vg;
        this.arch = arch;
    }

    ValidationGroup getValidationGroup() {
        return validationGroup;
    }
    
    @Messages("LBL_CreateProjectStep2=Name and Location")
    public @Override BasicPanelVisual getComponent() {
        if (component == null) {
            component = new BasicPanelVisual(this, arch);
            component.setName(LBL_CreateProjectStep2());
        }
        return component;
    }

    boolean areAdditional() {
        return additional;
    }

    public @Override HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.maven.newproject.BasicWizardPanel");
    }
    
    
    private final ChangeSupport cs = new ChangeSupport(this);
    public @Override void addChangeListener(ChangeListener l) {
        cs.addChangeListener(l);
    }
    public @Override void removeChangeListener(ChangeListener l) {
        cs.removeChangeListener(l);
    }
    protected final void fireChangeEvent() {
        cs.fireChange();
    }
    
    public @Override void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        getComponent().read(wizardDescriptor);
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
//        Object substitute = getComponent().getClientProperty ("NewProjectWizard_Title"); // NOI18N
//        if (substitute != null) {
//            wizardDescriptor.putProperty ("NewProjectWizard_Title", "XXX"); // NOI18N
//        }        
    }
    
    public @Override void storeSettings(WizardDescriptor settings) {
        getComponent().store(settings);
    }
    
    public @Override boolean isFinishPanel() {
        return isFinish;
    }
    
    public @Override boolean isValid() {
        return validationGroup.performValidation() == null;
    }
    
}
