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
package org.netbeans.modules.xml.tools.java.generator;

import org.netbeans.modules.xml.tools.generator.*;
import java.util.*;
import java.beans.*;

import javax.swing.*;
import javax.swing.event.*;

import org.openide.*;
import org.openide.util.HelpCtx;
import java.net.URL;

/**
 * Base class of wizardable customizer panels. <code>updateModel</code>
 * and <code>initView</code> methods need to be implemented. They are called as user goes
 * over wizard steps and it must (re)store current state.
 *
 * @author  Petr Kuzel
 * @version
 */
public abstract class SAXGeneratorAbstractPanel extends JPanel implements Customizer {

    /** Serial Version UID */
    private static final long serialVersionUID =5089896677680825691L;
    
    // associated wizard step or null
    private WizardStep step;

    /**
     * After a setObject() call contains current model driving wizard.
     */
    protected SAXGeneratorModel model;
    
    /** Creates new SAXGeneratorAbstractPanel */
    public SAXGeneratorAbstractPanel() {
    }

    public static final class WizardStep implements WizardDescriptor.Panel {

        private SAXGeneratorAbstractPanel peer;
        private Class peerClass;
        private Object bean;
        private Integer index;
        
        private Vector<ChangeListener> listeners = new Vector<>(); 
        private final ChangeEvent EVENT = new ChangeEvent(this);
        private boolean valid = true;
        
        /**
         * Create wizard step that uses instance of passed class as its component.
         */
        public WizardStep(Class peerClass) {
            if (SAXGeneratorAbstractPanel.class.isAssignableFrom(peerClass) == false) {
                throw new IllegalArgumentException("SAXGeneratorAbstractPanel required. Got " + peerClass);
            }
            this.peerClass = peerClass;
        }
        
        public java.awt.Component getComponent() {
            return getPeer();
        }
        
        private SAXGeneratorAbstractPanel getPeer() {
            if (peer == null) {
                try {
                    // unfortunately constructor does not initialize this
                    // object properly, client need to call setIndex and setBean
                    if (bean == null) throw new IllegalStateException();
                    if (index == null) throw new IllegalStateException();
                    peer = (SAXGeneratorAbstractPanel) peerClass.getDeclaredConstructor().newInstance();
                    peer.step = this;
                    peer.setObject(bean);
                    peer.putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, index);  // NOI18N
                } catch (ReflectiveOperationException ex) {
                    throw new IllegalStateException();
                }
            }
            return peer;
        }

        void setBean(Object bean) {
            this.bean = bean;
        }
        
        void setIndex(int index) {
            this.index = index;
        }
        
        public void readSettings(java.lang.Object p1) {
            getPeer().updateView();
        }
        
        /**
         * Cunstruct help ctx from WizardPanel_helpURL property.
         */
        public HelpCtx getHelp() {
            //return new HelpCtx(getPeer().getClass());
            return null;
        }

        public void addChangeListener(javax.swing.event.ChangeListener l) {
            listeners.add(l);
        }

        public void storeSettings(java.lang.Object p1) {
            getPeer().updateModel();
        }

        public boolean isValid() {
            return valid;
        }

        void setValid(boolean valid) {

            if (this.valid == valid) return;

            this.valid = valid;

            synchronized (listeners) {
                Iterator<ChangeListener> it = listeners.iterator();
                while (it.hasNext()) {
                    ChangeListener next = it.next();
                    next.stateChanged(EVENT);
                }
            }
        }

        public void removeChangeListener(javax.swing.event.ChangeListener l) {
            listeners.remove(l);
        }
    }
        

    /**
     * Update validity of associted wizard step or void.
     */
    protected final void setValid(boolean valid) {
        if (step != null) step.setValid(valid);
    }

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
    
    
    public void setObject(java.lang.Object peer) {
        if ( not(peer instanceof SAXGeneratorModel) ) {
            throw new IllegalArgumentException("SAXGeneratorModel class expected.");  // NOI18N
        }        
        
        model = (SAXGeneratorModel) peer;
        initView();
    }    
        
    public void addPropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }
    
    public void removePropertyChangeListener(java.beans.PropertyChangeListener p1) {
    }

    protected final boolean not (boolean expr) {
        return ! expr;
    }
    
}
