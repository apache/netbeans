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
/*
 * CPPropertiesPanelPanel.java
 *
 * Created on October 8, 2003
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.Vector;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;

/** A single panel descriptor for a wizard.
 * You probably want to make a wizard iterator to hold it.
 *
 * @author nityad
 */
public class CPPropertiesPanelPanel extends ResourceWizardPanel {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CPPropertiesPanelVisualPanel component;
    private ResourceConfigHelper helper;
    private Wizard wizard;
    
    /** Create the wizard panel descriptor. */
    public CPPropertiesPanelPanel(ResourceConfigHelper helper, Wizard wizard) {
        this.helper = helper;
        this.wizard = wizard;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CPPropertiesPanelVisualPanel(this, this.helper, this.wizard);
        }
        return component;
    }
    
    public void refreshFields(){
        if(component != null){
            component.refreshFields();
            component.setInitialFocus();
        }    
    }
     
    public HelpCtx getHelp() {
        return  new HelpCtx("AS_Wiz_ConnPool_props"); //NOI18N
    }
    
    public boolean isValid() {
        // If it is always OK to press Next or Finish, then:
        setErrorMsg(bundle.getString("Empty_String"));
        ResourceConfigData data = helper.getData();
        Vector vec = data.getProperties(); 
        for (int i = 0; i < vec.size(); i++) {
            NameValuePair pair = (NameValuePair)vec.elementAt(i);
            if (pair.getParamName() == null || pair.getParamValue() == null ||
                    pair.getParamName().length() == 0 || pair.getParamValue().length() == 0){
                setErrorMsg(bundle.getString("Err_InvalidNameValue"));
                return false;
            }  
        }
        return true;
    }
    
    protected final void fireChangeEvent (Object source) {
       super.fireChange(this);
    }
    
    public boolean isFinishPanel() {
       return isValid();
    }
}
