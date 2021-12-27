/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.web.struts.wizards;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.web.struts.StrutsConfigDataObject;
import org.netbeans.modules.web.struts.config.model.*;
import org.netbeans.modules.web.struts.editor.StrutsEditorUtilities;
import org.netbeans.spi.java.project.support.ui.templates.JavaTemplates;
import org.openide.WizardDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.util.NbBundle;
import org.openide.filesystems.FileObject;

import org.netbeans.api.project.Project;

import org.netbeans.spi.project.ui.templates.support.Templates;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.web.struts.StrutsUtilities;
import org.openide.cookies.OpenCookie;

/** A template wizard iterator for new struts action
 *
 * @author Petr Pisl
 * 
 */

public class ActionIterator implements TemplateWizard.Iterator {
    
    private int index;
    
    private transient WizardDescriptor.Panel[] panels;
    
    private transient boolean debug = false;
    
    public void initialize (TemplateWizard wizard) {
        if (debug) log ("initialize");  //NOI18N
        index = 0;
        // obtaining target folder
        Project project = Templates.getProject( wizard );
        DataFolder targetFolder=null;
        try {
            targetFolder = wizard.getTargetFolder();
        } catch (IOException ex) {
            targetFolder = DataFolder.findFolder(project.getProjectDirectory());
        }
        
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                                    JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (debug) {
            log ("\tproject: " + project);                          //NOI18N
            log ("\ttargetFolder: " + targetFolder);                //NOI18N
            log ("\tsourceGroups.length: " + sourceGroups.length);  //NOI18N
        }
        
        WizardDescriptor.Panel secondPanel = new ActionPanel(project, wizard);
        WizardDescriptor.Panel thirdPanel = new ActionPanel1(project);
        
        WizardDescriptor.Panel javaPanel;
        if (sourceGroups.length == 0)
            javaPanel = new FinishableProxyWizardPanel(Templates.createSimpleTargetChooser(project, sourceGroups, secondPanel));
        else
            javaPanel = new FinishableProxyWizardPanel(JavaTemplates.createPackageChooser(project, sourceGroups, secondPanel));
        
        panels = new WizardDescriptor.Panel[] { javaPanel, thirdPanel };
        
        // Creating steps.
        Object prop = wizard.getProperty (WizardDescriptor.PROP_CONTENT_DATA); // NOI18N
        String[] beforeSteps = null;
        if (prop != null && prop instanceof String[]) {
            beforeSteps = (String[])prop;
        }
        String[] steps = createSteps (beforeSteps, panels);
        
        for (int i = 0; i < panels.length; i++) { 
            JComponent jc = (JComponent)panels[i].getComponent ();
            if (steps[i] == null) {
                steps[i] = jc.getName ();
            }
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, i); // NOI18N 
	    jc.putClientProperty (WizardDescriptor.PROP_CONTENT_DATA, steps); // NOI18N
	}
    }
    
    public void uninitialize (TemplateWizard wizard) {
        panels = null;
    }
    
    public Set instantiate(TemplateWizard wizard) throws IOException {
        if (debug)
            log("instantiate");                                 //NOI18N
        
        FileObject dir = Templates.getTargetFolder( wizard );
        DataFolder df = DataFolder.findFolder( dir );
        FileObject template = Templates.getTemplate( wizard );
        
        String superclass=(String)wizard.getProperty(WizardProperties.ACTION_SUPERCLASS);
        if (debug)
            log("superclass="+superclass);   //NOI18N
        boolean replaceSuperClass = false;
        if (ActionPanelVisual.DEFAULT_ACTION.equals(superclass)){
            superclass = "Action";
            replaceSuperClass = true;
        } else if (ActionPanelVisual.DISPATCH_ACTION.equals(superclass)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("DispatchAction","java"); //NOI18N
        } else if (ActionPanelVisual.MAPPING_DISPATCH_ACTION.equals(superclass)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("MappingDispatchAction","java"); //NOI18N
        } else if (ActionPanelVisual.LOOKUP_DISPATCH_ACTION.equals(superclass)) {
            FileObject templateParent = template.getParent();
            template = templateParent.getFileObject("LookupDispatchAction","java"); //NOI18N
        }
        else {
            replaceSuperClass = true;
        }
        
        
        String targetName = Templates.getTargetName(wizard);
        DataObject dTemplate = DataObject.find( template );
        Map<String, Object> attributes = new HashMap<String,Object>();
        attributes.put("superclass", wizard.getProperty("action_superclass"));
        DataObject dobj = dTemplate.createFromTemplate(df, targetName, attributes);


        Project project = Templates.getProject(wizard);
        WebModule wm = WebModule.getWebModule(project.getProjectDirectory());
        if (wm != null && !StrutsUtilities.isInWebModule(wm)) {
            StrutsUtilities.enableStruts(wm, null);
        }

        String configFile = (String) wizard.getProperty(WizardProperties.ACTION_CONFIG_FILE);
        if (wm != null && configFile != null && !"".equals(configFile)) { //NOI18N
            // the file is created outside a wm -> we don't need to write the declaration.
            dir = wm.getDocumentBase();
            
            FileObject fo = dir.getFileObject(configFile); 
            StrutsConfigDataObject configDO = (StrutsConfigDataObject)DataObject.find(fo);
            StrutsConfig config= configDO.getStrutsConfig();
            Action action = new Action();

            Sources sources = ProjectUtils.getSources(project);
            SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
            String packageName = null;
            org.openide.filesystems.FileObject targetFolder = Templates.getTargetFolder(wizard);
            for (int i = 0; i < groups.length && packageName == null; i++) {
                packageName = org.openide.filesystems.FileUtil.getRelativePath (groups [i].getRootFolder (), targetFolder);
                if (packageName!=null) break;
            }
            if (packageName!=null) packageName = packageName.replace('/','.');
            else packageName="";                        //NOI18N
            String className=null;
            if (packageName.length()>0)
                className=packageName+"."+targetName;//NOI18N
            else
                className=targetName;
            action.setAttributeValue("type", className);                    //NOI18N

            String path = (String) wizard.getProperty(WizardProperties.ACTION_PATH);
            action.setAttributeValue("path", path.startsWith("/") ? path : "/" + path);     //NOI18N

            String formName = (String) wizard.getProperty(WizardProperties.ACTION_FORM_NAME);
            if (formName!=null) {
                action.setAttributeValue("name", formName);         //NOI18N
                action.setAttributeValue("scope",(String) wizard.getProperty(WizardProperties.ACTION_SCOPE));   //NOI18N
                action.setAttributeValue("input",(String) wizard.getProperty(WizardProperties.ACTION_INPUT));   //NOI18N
                action.setAttributeValue("attribute",(String) wizard.getProperty(WizardProperties.ACTION_ATTRIBUTE));   //NOI18N
                Boolean validate = (Boolean) wizard.getProperty(WizardProperties.ACTION_VALIDATE);
                if (Boolean.FALSE.equals(validate)) action.setAttributeValue("validate","false"); //NOI18N
                action.setAttributeValue("attribute",(String) wizard.getProperty(WizardProperties.ACTION_ATTRIBUTE));   //NOI18N
            }
            action.setAttributeValue("parameter",(String) wizard.getProperty(WizardProperties.ACTION_PARAMETER));       //NOI18N

            if (config != null) {
                if (config.getActionMappings() == null) {
                    config.setActionMappings(new ActionMappings());
                }
                config.getActionMappings().addAction(action);
            }
            BaseDocument doc = (BaseDocument)configDO.getEditorSupport().getDocument();
            if (doc == null){
                ((OpenCookie)configDO.getCookie(OpenCookie.class)).open();
                doc = (BaseDocument)configDO.getEditorSupport().getDocument();
            }
            StrutsEditorUtilities.writeBean(doc, action, "action", "action-mappings");                                  //NOI18N
            configDO.getEditorSupport().saveDocument();        
        }
        return Collections.singleton(dobj);
    }
    
    public void previousPanel () {
        if (! hasPrevious ()) throw new NoSuchElementException ();
        index--;
    }
    
    public void nextPanel () {
        if (! hasNext ()) throw new NoSuchElementException ();
        index++;
    }
    
    public boolean hasPrevious () {
        return index > 0;
    }
    
    public boolean hasNext () {
        return index < panels.length - 1;
    }
    
    public String name () {
        return NbBundle.getMessage (ActionIterator.class, "TITLE_x_of_y", //NOI18N
                index + 1, panels.length);
    }
    
    public WizardDescriptor.Panel current () {
        return panels[index];
    }
    // If nothing unusual changes in the middle of the wizard, simply:
    public final void addChangeListener (ChangeListener l) {}
    public final void removeChangeListener (ChangeListener l) {}
    
    
    private void log (String message){
        System.out.println("ActionIterator:: \t" + message);    //NOI18N
    }
    
    private String[] createSteps(String[] before, WizardDescriptor.Panel[] panels) {
        int diff = 0;
        if (before == null) {
            before = new String[0];
        } else if (before.length > 0) {
            diff = ("...".equals (before[before.length - 1])) ? 1 : 0; // NOI18N
        }
        String[] res = new String[ (before.length - diff) + panels.length];
        for (int i = 0; i < res.length; i++) {
            if (i < (before.length - diff)) {
                res[i] = before[i];
            } else {
                res[i] = panels[i - before.length + diff].getComponent ().getName ();
            }
        }
        return res;
    }
    
    private void replaceInDocument(javax.swing.text.Document document, String replaceFrom, String replaceTo) {
        javax.swing.text.AbstractDocument doc = (javax.swing.text.AbstractDocument)document;
        int len = replaceFrom.length();
        try {
            String content = doc.getText(0,doc.getLength());
            int index = content.lastIndexOf(replaceFrom);
            while (index>=0) {
                doc.replace(index,len,replaceTo,null);
                content=content.substring(0,index);
                index = content.lastIndexOf(replaceFrom);
            }
        } catch (javax.swing.text.BadLocationException ex){}
    }
    
}
