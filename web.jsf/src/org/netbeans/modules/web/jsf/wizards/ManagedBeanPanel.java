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

package org.netbeans.modules.web.jsf.wizards;

import java.awt.Component;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.common.ProjectUtil;
import org.netbeans.modules.j2ee.common.ServerUtil;

import org.openide.WizardDescriptor;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Panel asking for web frameworks to use.
 * @author Radko Najman
 */
final class ManagedBeanPanel implements WizardDescriptor.Panel, WizardDescriptor.FinishablePanel, ChangeListener {

    private TemplateWizard wizard;
    private ManagedBeanPanelVisual component;
    private String managedBeanClass;
    
    private Project project;
    /** Create the wizard panel descriptor. */
    public ManagedBeanPanel(Project project, TemplateWizard wizard) {
        this.project = project;
        this.wizard = wizard;
    }

    @Override
    public boolean isFinishPanel() {
        return true;
    }

    @Override
    public Component getComponent() {
        if (component == null) {
            ManagedBeanPanelVisual gui = new ManagedBeanPanelVisual(project);
            gui.addChangeListener(this);
            component = gui;
        }

        return component;
    }

    public void updateManagedBeanName(WizardDescriptor.Panel panel) {
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

        if ((targetName == null) || targetName.trim().equals("")) {
            return;
        }

        if (managedBeanClass!=null && targetName.equals(managedBeanClass.substring(0, 1).toUpperCase()+ managedBeanClass.substring(1))) {
            return;
        } else {
            managedBeanClass = targetName.substring(0, 1).toLowerCase()+targetName.substring(1);
        }

        getComponent();
        String name = component.getManagedBeanName();
        if ((name == null) || !name.equals(managedBeanClass)) {
            component.setManagedBeanName(managedBeanClass);
        }
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx("org.netbeans.modules.web.jsf.wizards.ManagedBeanPanel");
    }

    @Override
    public boolean isValid() {
        getComponent();
        if (component.valid(wizard)) {
            // check that this project has a valid target server
            if (!ServerUtil.isValidServerInstance(project)) {
                wizard.putProperty(WizardDescriptor.PROP_WARNING_MESSAGE,
                        NbBundle.getMessage(ManagedBeanPanel.class, "WARN_MissingTargetServer"));
            }
            return true;
        }
        return false;
    }

    public boolean isAddBeanToConfig() {
        if (component == null) {
            return false;
        }
        return component.isAddBeanToConfig();
    }
    private final Set/*<ChangeListener>*/ listeners = new HashSet(1);

    @Override
    public final void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    @Override
    public final void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }
    protected final void fireChangeEvent() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }

    @Override
    public void readSettings(Object settings) {
        wizard = (TemplateWizard) settings;
        component.read(wizard);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizard.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }

    @Override
    public void storeSettings(Object settings) {
        WizardDescriptor d = (WizardDescriptor) settings;
        component.store(d);

        ((WizardDescriptor) d).putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    @Override
    public void stateChanged(ChangeEvent arg0) {
        isValid();
    }
}
