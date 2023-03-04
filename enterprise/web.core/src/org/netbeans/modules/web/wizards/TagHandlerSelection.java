/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.wizards;

import java.awt.Component;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.ProjectUtils;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.modules.web.api.webmodule.WebModule;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  Milan Kuchtiak
 */
public class TagHandlerSelection implements WizardDescriptor.Panel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private transient TagHandlerPanel component;
    private transient TemplateWizard wizard;
    private transient Profile j2eeVersion;
    
    /** Create the wizard panel descriptor. */
    public TagHandlerSelection(TemplateWizard wizard) {
        this.wizard=wizard;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        Project project = Templates.getProject( wizard );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        WebModule wm=null;
        j2eeVersion = Profile.J2EE_14;
        if (groups!=null && groups.length>0) {
            wm = WebModule.getWebModule(groups[0].getRootFolder());
        }
        if (wm!=null) {
            j2eeVersion=wm.getJ2eeProfile();
        }
        if (component == null) {
            component = new TagHandlerPanel(this,j2eeVersion);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx(TagHandlerSelection.class); //NOI18N
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        if (!isBodyTagSupport() && Profile.J2EE_13.equals(j2eeVersion)) {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, // NOI18N
                org.openide.util.NbBundle.getMessage(TagHandlerSelection.class, "NOTE_simpleTag"));
        } else {
            wizard.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
        }
        return true;
        // If it depends on some condition (form filled out...), then:
        // return someCondition ();
        // and when this condition changes (last form field filled in...) then:
        // fireChangeEvent ();
        // and uncomment the complicated stuff below.
    }
 
    // FIXME: use org.openide.util.ChangeSupport for ChangeListeners
    private final Set<ChangeListener> listeners = new HashSet<ChangeListener>(1);

    public final void addChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.add (l);
        }
    }
    public final void removeChangeListener (ChangeListener l) {
        synchronized (listeners) {
            listeners.remove (l);
        }
    }
    protected final void fireChangeEvent () {
        Iterator<ChangeListener> it;
        synchronized (listeners) {
            it = new HashSet<ChangeListener>(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext()) {
            it.next().stateChanged(ev);
        }
    }

    
    // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
    }
    public void storeSettings(Object settings) {
        WizardDescriptor w = (WizardDescriptor)settings;
        if (isBodyTagSupport())
            w.putProperty("BODY_SUPPORT",Boolean.TRUE);//NOI18N
        else 
            w.putProperty("BODY_SUPPORT",Boolean.FALSE);//NOI18N
    }
    
    boolean isBodyTagSupport() {
        return component.isBodyTagSupport();
    }
    
}
