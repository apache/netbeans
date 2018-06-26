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

package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;

import org.openide.WizardDescriptor;
import org.openide.WizardValidationException;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;

/**
 *
 * @author radko
 */
public class WebServiceFromWSDL implements WizardDescriptor.Panel<WizardDescriptor>, WizardDescriptor.FinishablePanel<WizardDescriptor>, WizardDescriptor.ValidatingPanel<WizardDescriptor>, ChangeListener {

    private WebServiceFromWSDLPanel component;
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    /** Creates a new instance of WebServiceType */
    public WebServiceFromWSDL(WizardDescriptor wizardDescriptor, Project project) {
        this.wizardDescriptor = wizardDescriptor;
        this.project = project;
    }

    public Component getComponent() {
        if (component == null) {
            component = new WebServiceFromWSDLPanel(project);
            component.addChangeListener(this);
        }
        
        return component;
    }

    public HelpCtx getHelp() {
        HelpCtx helpCtx = null;
        if (getComponent() != null && (helpCtx = component.getHelpCtx()) != null)
            return helpCtx;
        
        return new HelpCtx(WebServiceFromWSDL.class);
    }

    public void readSettings(WizardDescriptor settings) {
        wizardDescriptor = settings;
        component.read (wizardDescriptor);
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewProjectWizard to modify the title
        Object substitute = ((JComponent) component).getClientProperty("NewProjectWizard_Title"); // NOI18N
        if (substitute != null)
            wizardDescriptor.putProperty("NewProjectWizard_Title", substitute); // NOI18N
    }

    public void storeSettings(WizardDescriptor settings) {
        component.store(settings);
        settings.putProperty("NewProjectWizard_Title", null); // NOI18N
    }

    public boolean isValid() {
        getComponent();
        return component.isValid(wizardDescriptor);
    }

    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    
    public void addChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.add(l);
        }
    }

    public void removeChangeListener(ChangeListener l) {
        synchronized (listeners) {
            listeners.remove(l);
        }
    }

    public boolean isFinishPanel() {
        return isValid();
    }

    public void validate() throws WizardValidationException {
        component.validate(wizardDescriptor);
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    private void fireChange() {
        Mutex.EVENT.readAccess(new Runnable() {
            
            @Override
            public void run() {
                ChangeEvent e = new ChangeEvent(this);
                synchronized (WebServiceFromWSDL.this) {
                    Iterator<ChangeListener> it = listeners.iterator();
                    while (it.hasNext()) {
                        it.next().stateChanged(e);
                    }                    
                }
            }
        });
    }

}
