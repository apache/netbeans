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
package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.impl.SchemaPanel;
import java.util.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.HelpCtx;
import javax.swing.event.ChangeListener;


/**
 * Base class of wizardable panels. <code>updateModel</code>
 * and <code>initView</code> methods need to be implemented. They are called as user goes
 * over wizard steps and it must (re)store current state.
 * <p>
 * For proper functionality it must be wrapped by {@link WizardStep}.
 *
 * @author  Petr Kuzel
 * @version
 */
public abstract class AbstractPanel extends JPanel implements Customizer {

    /** Serial Version UID */
    private static final long serialVersionUID =508989667995691L;
    
    /**
     * After a setObject() call contains current model driving wizard.
     */
    protected DocumentModel model;

    // associated wizard step wrapper (initialized by step.    
    private WizardStep step;
            
    /**
     * User just leaved the panel, update model
     */
    protected abstract void updateModel();
    
    /**
     * User just entered the panel, init view by model values
     */
    protected abstract void initView();
    
    /**
     * User just reentered the panel.
     */
    protected abstract void updateView();
    
    
    
    // customizer impl ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public void setObject(Object model) {
        if ( not(model instanceof DocumentModel) ) {
            throw new IllegalArgumentException("DocumentModel class expected.");  // NOI18N
        }        
        
        this.model = (DocumentModel) model;
        initView();
    }    
        
    private Vector listeners = new Vector();
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
    
    protected void fireChange() {
        Iterator it;
        synchronized (listeners) {
            it = new HashSet(listeners).iterator();
        }
        ChangeEvent ev = new ChangeEvent(this);
        while (it.hasNext()) {
            ChangeListener next = (ChangeListener)it.next();
            next.stateChanged(ev);
        }
    }

    protected static boolean not(boolean expr) {
        return ! expr;
    }

    /**
     * Gives access to WizardStep wrapper.
     * It is supported only for AbstractPanel with associated WizardStep.
     * @return step or <code>IllegalStateException</code> exception.
     */
    protected final WizardStep getStep() {
        if (step == null) throw new IllegalStateException("new WizardStep(this) have not been called!");
        return step;
    }
    
    /**
     * WizardDescriptor.Panel adapter for AbstractPanel.
     * It solved isValid() clash between Component and WizardDescriptor.Panel.
     */
    public static class WizardStep implements WizardDescriptor.Panel, ChangeListener  {
        
        private AbstractPanel peer;
        private Vector listeners = new Vector();
        private ChangeEvent EVENT = new ChangeEvent(this);
        private boolean valid = true;
        
        public WizardStep(AbstractPanel peer) {
            if (peer == null) throw new NullPointerException();
            this.peer = peer;
            peer.step = this;
            peer.addChangeListener(this);
        }
    
        public java.awt.Component getComponent() {            
            return peer;
        }

        public void readSettings(Object settings) {
            peer.updateView();
        }

        /**
         * Cunstruct help ctx from WizardPanel_helpURL property.
         */
        public final HelpCtx getHelp() {
    //        URL url = (URL) getClientProperty(WizardDescriptor.PROP_HELP_URL);
    //        if (url != null) {
    //            return new HelpCtx(peer.getClass());  // warning getClass(0 returns a subclass
    //        }
            return HelpCtx.DEFAULT_HELP;
        }
           
        public void storeSettings(Object settings) {
            peer.updateModel();
        }

        public boolean isValid() {
            if(peer instanceof XMLContentPanel)
              return ((XMLContentPanel)peer).isPanelValid();
            if(peer instanceof SchemaPanel){
              return ((SchemaPanel)peer).isPanelValid();              
                //if(num > 0 && ((SchemaPanel)peer).isPrimarySchemaSelected())
                //    return true;
               //0 e//lse 
                   // return false;
            }
            return valid;
        }

        protected final void setValid(boolean valid) {
            if (this.valid == valid) return;
            this.valid = valid;
        }

        public final void addChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.add(l);
            }
        }
        public final void removeChangeListener(ChangeListener l) {
            synchronized (listeners) {
                listeners.remove(l);
            }
        }
        protected final void fireChangeEvent() {
            Iterator it;
            synchronized (listeners) {
                it = new HashSet(listeners).iterator();
            }
            ChangeEvent ev = new ChangeEvent(this);
            while (it.hasNext()) {
                ChangeListener next = (ChangeListener)it.next();
                next.stateChanged(ev);
            }
        }
        
        public void stateChanged(ChangeEvent e) {
            fireChangeEvent();
        }
        
    }
    
}
