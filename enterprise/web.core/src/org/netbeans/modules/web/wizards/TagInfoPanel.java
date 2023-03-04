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
import org.netbeans.api.java.project.JavaProjectConstants;
import org.openide.WizardDescriptor;
import org.openide.util.HelpCtx;
import org.openide.loaders.TemplateWizard;
import org.openide.filesystems.FileObject;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author  Milan Kuchtiak
 */
public class TagInfoPanel implements WizardDescriptor.Panel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private TagHandlerPanelGUI component;
    private transient TemplateWizard wizard;
    private transient Project proj;
    private transient SourceGroup[] sourceGroups;
    private String className;
    
    /** Create the wizard panel descriptor. */
    public TagInfoPanel(TemplateWizard wizard, Project proj, SourceGroup[] sourceGroups) {
        this.wizard=wizard;
        this.proj=proj;
        this.sourceGroups=sourceGroups;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new TagHandlerPanelGUI(wizard,this,proj,sourceGroups);
        }
        return component;
    }
    
    public HelpCtx getHelp() {
        //return new HelpCtx(TagInfoPanel.class); //NOI18N
        return HelpCtx.DEFAULT_HELP;
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        if (writeToTLD() && getTLDFile()==null) {
            wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "MSG_noTldSelected")); // NOI18N
            return false;
        } else if (isTagNameEmpty(getTagName())) {
            wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "TXT_missingTagName")); // NOI18N
            return false;        
        } else if (!isValidTagName(getTagName())) {
            wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "TXT_wrongTagName",getTagName())); // NOI18N
            return false;        
        } else if (tagNameExists(getTagName())) {
            wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, org.openide.util.NbBundle.getMessage(TagInfoPanel.class, "TXT_tagNameExists",getTagName())); // NOI18N
            return false;        
        } else {
            wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, ""); // NOI18N
            return true;
        }
        
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
            it = new HashSet<ChangeListener>(listeners).iterator ();
        }
        ChangeEvent ev = new ChangeEvent (this);
        while (it.hasNext ()) {
            it.next().stateChanged(ev);
        }
    }
    
    // You can use a settings object to keep track of state.
    // Normally the settings object will be the WizardDescriptor,
    // so you can use WizardDescriptor.getProperty & putProperty
    // to store information entered by the user.
    public void readSettings(Object settings) {
        TemplateWizard w = (TemplateWizard)settings;
        //Project project = Templates.getProject(w);
        String targetName = w.getTargetName();
        org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(w);
        Project project = Templates.getProject( w );
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
        }
        if (packageName == null)
            packageName = ""; //NOI18N
        packageName = packageName.replace('/','.');
        
        if (targetName!=null) {
            if (packageName.length()>0)
                className=packageName+"."+targetName;//NOI18N
            else
                className=targetName;
            component.setClassName(className);
            if (component.getTagName().length()==0)
                component.setTagName(targetName);
        }
        Boolean bodySupport = (Boolean)w.getProperty("BODY_SUPPORT");//NOI18N
        if (bodySupport!=null && bodySupport) 
            component.setBodySupport(true);
        else component.setBodySupport(false);
    }
    public void storeSettings(Object settings) {
    }
    
    public String getClassName() {
        return className;
    }
    public String getTagName() {
        return component.getTagName();
    }
    public FileObject getTLDFile() {
        return component.getTLDFile();
    }
    public boolean isEmpty() {
        return component.isEmpty();
    }
    public boolean isScriptless() {
        return component.isScriptless();
    }
    public boolean isTegdependent() {
        return component.isTegdependent();
    }
    public boolean writeToTLD() {
        return component.writeToTLD();
    }
    public Object[][] getAttributes() {
        return component.getAttributes();
    }
    
    static boolean isTagNameEmpty(String name) {
        if (name == null) {
            return true;
        }
        return "".equals(name); // NOI18N
    }
    
    static boolean isValidTagName(String name) {
        if (name==null) return false;
        return org.apache.xerces.util.XMLChar.isValidNCName(name);
    }
    
    private boolean tagNameExists(String name) {
        java.util.Set tagValues = component.getTagValues();
        if (tagValues!=null && tagValues.contains(name)) return true; 
        else return false;
    }
}
