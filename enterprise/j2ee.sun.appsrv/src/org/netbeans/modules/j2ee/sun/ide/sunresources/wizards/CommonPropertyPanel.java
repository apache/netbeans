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
 * CommonPropertyPanel.java
 *
 * Created on November 19, 2003, 12:33 PM
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.util.Vector;
import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.ide.editors.NameValuePair;

import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroup;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.FieldGroupHelper;

/**
 *
 * @author  nityad
 */
public class CommonPropertyPanel extends ResourceWizardPanel {
    
    private CommonPropertyVisualPanel component;
    private ResourceConfigHelper helper;
    private Wizard wiz;
    boolean firstTime = false;
    /** Creates a new instance of CommonPropertyPanel */
    public CommonPropertyPanel(ResourceConfigHelper helper, Wizard wiz) {
        this.helper = helper;
        this.wiz = wiz;
    }
    
     // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    public Component getComponent() {
        if (component == null) {
            component = new CommonPropertyVisualPanel(this);
        }
        return component;
    }
    
    public void setInitialFocus(){
        if(component != null) {
            component.refreshFields();
            component.setInitialFocus();
        }
    }
    
    public FieldGroup getFieldGroup(String groupName) {
        return FieldGroupHelper.getFieldGroup(wiz, groupName);
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public HelpCtx getHelp() {
        if (wiz.getName().equals(__JdbcResource)) {
            return new HelpCtx("AS_Wiz_DataSource_props"); //NOI18N
        }else if (wiz.getName().equals(__PersistenceManagerFactoryResource)) {
            return new HelpCtx("AS_Wiz_PMF_props"); //NOI18N
        }else {
            return HelpCtx.DEFAULT_HELP;
        }
    }
    
    public boolean isValid() {
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
    
    
}
