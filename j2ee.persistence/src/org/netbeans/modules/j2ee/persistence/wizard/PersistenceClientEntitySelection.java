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

package org.netbeans.modules.j2ee.persistence.wizard;

import java.awt.Component;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;

/**
 * @author Pavel Buzek
 */
public final class PersistenceClientEntitySelection implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {
    public final static String DISABLENOIDSELECTION = "disableNoIdSelection";//NOI18N, used to control if entities without id can be selected
    
    private WizardDescriptor wizardDescriptor;
    private String panelName;
    private HelpCtx helpCtx;
    private PersistenceClientEntitySelectionVisual component;
    private boolean disableNoIdSelection = false;
    
    
    /** Create the wizard panel descriptor. */
    public PersistenceClientEntitySelection(String panelName, HelpCtx helpCtx, WizardDescriptor wizardDescriptor) {
        this.panelName = panelName;
        this.helpCtx = helpCtx;
        this.wizardDescriptor = wizardDescriptor;
    }
    
    @Override
    public boolean isFinishPanel() {
        return false;
    }
    
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new PersistenceClientEntitySelectionVisual(panelName, wizardDescriptor);
            component.addChangeListener(this);
        }
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        return helpCtx;
    }
    
    @Override
    public boolean isValid() {
        getComponent();
        return component.valid(wizardDescriptor);
    }
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    
    @Override
    public final void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    @Override
    public final void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }
    protected final void fireChangeEvent(ChangeEvent ev) {
        changeSupport.fireChange();
    }
    
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        component.read(wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null){
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
        }
    }
    
    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);
        d.putProperty(WizardProperties.CREATE_PERSISTENCE_UNIT, component.getCreatePersistenceUnit());
        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }
    
    @Override
    public void stateChanged(ChangeEvent e) {
        fireChangeEvent(e);
    }
    
}
