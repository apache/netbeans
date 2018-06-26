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
package org.netbeans.modules.glassfish.javaee.templates;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;


/*
 * Adapted from SunDDWizardPanel
 * @author Vince Kraemer
 */
public class WebLogicDDWizardPanel implements WizardDescriptor.Panel {
    
    /**
     * The visual component that displays this panel. If you need to access the
     * component from this class, just use getComponent().
     */
//    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);
    private final Set listeners = new HashSet(1);
    private WebLogicDDVisualPanel component = new WebLogicDDVisualPanel();
    private WizardDescriptor wizardDescriptor;
    private Project project;
    
    public WebLogicDDWizardPanel() {
        component.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                fireChangeEvent();
            }
        });
    }
    
//    FileObject getSelectedLocation() {
    File getSelectedLocation() {
        return component.getSelectedLocation();
    }
    
    Project getProject() {
        return project;
    }
    
    String getFileName() {
        return component.getFileName();
    }
    
    @Override
    public Component getComponent() {
        return component;
    }
    
    @Override
    public HelpCtx getHelp() {
        // Show no Help button for this panel:
        return HelpCtx.DEFAULT_HELP;
        // If you have context help:
        // return new HelpCtx(SampleWizardPanel1.class);
    }
    
    @Override
    public boolean isValid() {

        String sunDDFileName = component.getFileName();
        if(sunDDFileName == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,  // NOI18N
                    NbBundle.getMessage(WebLogicDDWizardPanel.class,"ERR_NoJavaEEModuleType")); //NOI18N
            return false;
        }
        
        File location = component.getSelectedLocation();
        if(location == null) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,  // NOI18N
                    NbBundle.getMessage(WebLogicDDWizardPanel.class,"ERR_NoValidLocation", sunDDFileName)); //NOI18N
            return false;
        }

        File sunDDFile = component.getFile();
        if(sunDDFile.exists()) {
            wizardDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE,  // NOI18N
                    NbBundle.getMessage(WebLogicDDWizardPanel.class,"ERR_FileExists", sunDDFileName)); //NOI18N
            return false;
        }
        
        return true;
    }
    
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
//        Iterator<ChangeListener> it;
        Iterator it;
        synchronized (listeners) {
//            it = new HashSet<ChangeListener>(listeners).iterator();
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
//            it.next().stateChanged(ev);
            ((ChangeListener)it.next()).stateChanged(ev);
        }
    }
    
    // You can use a settings object to keep track of state. Normally the
    // settings object will be the WizardDescriptor, so you can use
    // WizardDescriptor.getProperty & putProperty to store information entered
    // by the user.
    @Override
    public void readSettings(Object settings) {
        wizardDescriptor = (WizardDescriptor) settings;
        if (project == null) {
            project = Templates.getProject(wizardDescriptor);
            component.setProject(project);
        }
    }
    
    @Override
    public void storeSettings(Object settings) {
    }
    
}

