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


package org.netbeans.modules.i18n.wizard;

import java.awt.Dialog;
import java.lang.ref.WeakReference;
import java.text.MessageFormat;
import java.util.ArrayList;

import org.netbeans.modules.i18n.I18nUtil;

import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.WizardDescriptor;
import org.openide.DialogDisplayer;
import org.netbeans.api.project.Project;

/**
 * Action which runs i18n test wizard.
 *
 * @author  Peter Zavadsky
 * @author  Petr Kuzel
 */
public class I18nTestWizardAction extends NodeAction {

    public I18nTestWizardAction() {
        putValue("noIconInMenu", Boolean.TRUE);                         //NOI18N
    }
    
    /** Generated serial version UID. */
    static final long serialVersionUID = -3265587506739081248L;

    /** Weak reference to dialog. */
    private static WeakReference dialogWRef = new WeakReference(null);
    
    
    /** 
     * We create non-modal but not rentrant dialog. Wait until
     * previous one is closed.
     */
    protected boolean enable(Node[] activatedNodes) {

        if (Util.wizardEnabled(activatedNodes) == false) {
            return false;
        }
        
        Dialog previous = (Dialog) dialogWRef.get();
        if (previous == null) return true;
        return previous.isVisible() == false;
    }
    
    /** 
     * Popup non modal wizard.
     */
    protected void performAction(Node[] activatedNodes) {
        Dialog dialog = (Dialog)dialogWRef.get();
        
        if(dialog != null) {
            dialog.setVisible(false);
            dialog.dispose();
        }

        Project project = org.netbeans.modules.i18n.Util.getProjectFor(activatedNodes);
	if (project == null) return;

        WizardDescriptor wizardDescriptor = I18nWizardDescriptor.createI18nWizardDescriptor(
            getWizardIterator(),
            new I18nWizardDescriptor.
	    Settings(Util.createWizardSourceMap(activatedNodes),
		     project)

        );

        initWizard(wizardDescriptor);
        
        dialog = DialogDisplayer.getDefault().createDialog(wizardDescriptor);
        dialogWRef = new WeakReference(dialog);
        dialog.setVisible(true);
    }

    /** Gets wizard iterator thru panels used in wizard invoked by this action, 
     * i.e I18N wizard. */
    private WizardDescriptor.Iterator getWizardIterator() {
        WizardDescriptor.Panel[] panels = new WizardDescriptor.Panel[3];
        
        panels[0] = new SourceWizardPanel.Panel(true);
        panels[1] = new ResourceWizardPanel.Panel(true);
        panels[2] = new TestStringWizardPanel.Panel();
        
        return new WizardDescriptor.ArrayIterator(panels);
            
    }
    
    /** Initializes wizard descriptor. */
    private void initWizard(WizardDescriptor wizardDesc) {
        // Init properties.
        wizardDesc.putProperty(WizardDescriptor.PROP_AUTO_WIZARD_STYLE, Boolean.TRUE);
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_DISPLAYED, Boolean.TRUE);
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_NUMBERED, Boolean.TRUE);

        ArrayList contents = new ArrayList(3);
        contents.add(Util.getString("TXT_SelectTestSources"));          //NOI18N
        contents.add(Util.getString("TXT_SelectTestResources"));        //NOI18N
        contents.add(Util.getString("TXT_FoundMissingResources"));      //NOI18N
        
        wizardDesc.putProperty(WizardDescriptor.PROP_CONTENT_DATA, (String[])contents.toArray(new String[0]));
        
        wizardDesc.setTitle(Util.getString("LBL_TestWizardTitle"));     //NOI18N
        wizardDesc.setTitleFormat(new MessageFormat("{0} ({1})"));      //NOI18N

        wizardDesc.setModal(false);
    }
    
    /** Gets localized name of action. Overrides superclass method. */
    public String getName() {
        return Util.getString("LBL_TestWizardActionName");              //NOI18N
    }
    
    /** Gets the action's help context. Implemenst superclass abstract method. */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(I18nUtil.HELP_ID_TESTING);
    }

    protected boolean asynchronous() {
      return false;
    }
    

}
