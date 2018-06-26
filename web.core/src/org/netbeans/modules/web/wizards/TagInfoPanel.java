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
        if (bodySupport!=null && bodySupport.booleanValue()) 
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
