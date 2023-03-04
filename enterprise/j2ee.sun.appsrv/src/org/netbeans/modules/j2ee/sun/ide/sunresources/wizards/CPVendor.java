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
 * CPVendor.java
 *
 * Created on February 11, 2009
 */

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.awt.Component;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.util.HelpCtx;

import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.openide.WizardDescriptor;

/** 
 * 
 *
 * @author  Nitya Doraisamy
 */
public class CPVendor implements WizardDescriptor.Panel, ChangeListener {
    
    /** The visual component that displays this panel.
     * If you need to access the component from this class,
     * just use getComponent().
     */
    private CPVendorPanel component;
    private ResourceConfigHelper helper;    
    private Wizard wizardInfo;
    private final List listeners = new ArrayList();
    private WizardDescriptor wizDescriptor;
    
    /** Create the wizard panel descriptor. */
    public CPVendor(ResourceConfigHelper helper, Wizard wizardInfo) {
        this.helper = helper;
        this.wizardInfo = wizardInfo;
    }
    
    // Get the visual component for the panel. In this template, the component
    // is kept separate. This can be more efficient: if the wizard is created
    // but never displayed, or not all panels are displayed, it is better to
    // create only those which really need to be visible.
    @Override
    public Component getComponent() {
        if (component == null) {
            component = new CPVendorPanel(this, this.helper, this.wizardInfo);
            component.addChangeListener(this);
        }
        return component;
    }

    private CPVendorPanel getVisual() {
        return (CPVendorPanel) getComponent();
    }

    public String getResourceName() {
        return this.wizardInfo.getName();
    }
    
    public HelpCtx getHelp() {
         return new HelpCtx("AS_Wiz_ConnPool_chooseDB"); //NOI18N
    }
    
    public ResourceConfigHelper getHelper() {
        return helper;
    }
    
    public Wizard getWizard() {
        return wizardInfo;
    }
    
    /**
     * Checks if the JNDI Name in the wizard is duplicate name in the
     * Unregistered resource list for JDBC Data Sources, Persistenc Managers, 
     * and Java Mail Sessions.
     *
     * @return boolean true if there is a duplicate name.
     * false if not.
     */
    public boolean isValid() {
        boolean value = getVisual().hasValidData();
        return value;
    }
    
    public void readSettings(Object settings) {
        wizDescriptor = (WizardDescriptor) settings;
        getVisual().read(settings);
    }

    public void storeSettings(Object settings) {
        //getVisual().store((AddServerInstanceWizard)settings);
    }
    
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

    public void stateChanged(ChangeEvent event) {
        fireChange(event);
    }

    private void fireChange(ChangeEvent event) {
        ArrayList tempList;

        synchronized (listeners) {
            tempList = new ArrayList(listeners);
        }

        Iterator iter = tempList.iterator();
        while (iter.hasNext()) {
            ((ChangeListener)iter.next()).stateChanged(event);
        }
    }

    public void setErrorMsg(String message) {
        if (this.wizDescriptor != null) {
            this.wizDescriptor.putProperty(WizardDescriptor.PROP_ERROR_MESSAGE, message);    //NOI18N
        }
    }

    public void setErrorMessage(String msg, String value){
        String message = MessageFormat.format(msg, new Object[] {value});
        setErrorMsg(message);
    }

    public void setInitialFocus(){
        getVisual().setInitialFocus();
    }
}

