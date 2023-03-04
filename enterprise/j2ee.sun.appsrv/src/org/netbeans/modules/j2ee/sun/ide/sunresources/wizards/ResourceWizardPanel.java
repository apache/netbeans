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

package org.netbeans.modules.j2ee.sun.ide.sunresources.wizards;

import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.j2ee.sun.sunresources.beans.Wizard;
import org.netbeans.modules.j2ee.sun.sunresources.beans.WizardConstants;
import org.openide.ErrorManager;

import org.openide.util.HelpCtx;
import org.openide.WizardDescriptor;
import org.openide.util.NbBundle;

public abstract class ResourceWizardPanel implements WizardDescriptor.FinishablePanel, ChangeListener, WizardConstants {

    private final List listeners = new ArrayList();

    /** Default preferred width of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_WIDTH = 600;
    /** Default preferred height of the panel - should be the same for all panels within one wizard */
    private static final int DEFAULT_HEIGHT = 390;
   
    public WizardDescriptor wizDescriptor;
    public ResourceBundle bundle = NbBundle.getBundle("org.netbeans.modules.j2ee.sun.ide.sunresources.wizards.Bundle"); //NOI18N
    
    public ResourceWizardPanel() { }

    /** @return preferred size of the wizard panel - it should be the same for all panels within one Wizard
    * so that the wizard dialog does not change its size when switching between panels */
    
    public java.awt.Dimension getPreferredSize () {
        return new java.awt.Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    public HelpCtx getHelp() {
        return null; // HelpCtx.DEFAULT_HELP;
    }

    public void stateChanged(ChangeEvent event) {
        fireChange(event);
    }
    
    public void fireChange (Object source) {
        ArrayList lst;

        synchronized (listeners) {
            lst = new ArrayList(listeners);
        }

        ChangeEvent event = new ChangeEvent(source);
        for (int i=0; i< lst.size(); i++){
            ChangeListener listener = (ChangeListener) lst.get(i);
            listener.stateChanged(event);
        }
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
    
    public boolean isFinishPanel() {
        return false;
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
    
    public void readSettings(Object settings) {
        this.wizDescriptor = (WizardDescriptor)settings;
    }
    
    public void storeSettings(Object settings) {
    }
    
    public Wizard getWizardInfo(String dataFile){
        Wizard wizardInfo = null;
        try{
            InputStream in = Wizard.class.getClassLoader().getResourceAsStream(dataFile);
            wizardInfo = Wizard.createGraph(in);
        }catch(Exception ex){
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
        }
        return wizardInfo;
    }
}
