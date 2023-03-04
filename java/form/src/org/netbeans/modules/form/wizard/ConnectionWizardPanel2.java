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

package org.netbeans.modules.form.wizard;

import java.beans.*;
import javax.swing.event.*;
import org.netbeans.modules.form.*;

/**
 * The second panel of connection wizard - for selecting what to perform on
 * the target component (set a property, call a method or execute some user code).
 *
 * @author Tomas Pavek
 */

class ConnectionWizardPanel2 implements org.openide.WizardDescriptor.Panel {

    static final int METHOD_TYPE = 0;
    static final int PROPERTY_TYPE = 1;
    static final int CODE_TYPE = 2;

    private RADComponent targetComponent;

    private EventListenerList listenerList;

    private ConnectionPanel2 uiPanel;

    // -------

    ConnectionWizardPanel2(RADComponent target) {
        targetComponent = target;
    }

    RADComponent getTargetComponent() {
        return targetComponent;
    }

    int getActionType() {
        return uiPanel != null ? uiPanel.getActionType() : -1 ;
    }

    MethodDescriptor getSelectedMethod() {
        return uiPanel != null ? uiPanel.getSelectedMethod() : null;
    }

    PropertyDescriptor getSelectedProperty() {
        return uiPanel != null ? uiPanel.getSelectedProperty() : null;
    }

    // ----------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (uiPanel == null)
            uiPanel = new ConnectionPanel2(this);
        return uiPanel;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("gui.connecting.target"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return getActionType() == CODE_TYPE
               || getSelectedMethod() != null
               || getSelectedProperty() != null;
    }

    @Override
    public void readSettings(java.lang.Object settings) {
    }

    @Override
    public void storeSettings(java.lang.Object settings) {
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        if (listenerList == null)
            listenerList = new EventListenerList();
        listenerList.add(ChangeListener.class, listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        if (listenerList != null)
            listenerList.remove(ChangeListener.class, listener);
    }

    // --------

    void fireStateChanged() {
        if (listenerList == null)
            return;

        ChangeEvent e = null;
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length-2; i>=0; i-=2) {
            if (listeners[i] == ChangeListener.class) {
                if (e == null)
                    e = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(e);
            }
        }
    }
}
