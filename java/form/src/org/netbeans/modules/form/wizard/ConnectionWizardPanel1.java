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

import javax.swing.event.*;
import org.netbeans.modules.form.*;

/**
 * The first panel of connection wizard - for selecting the activation event
 * on the source component and handler for the event (where the connection code
 * will be generated).
 *
 * @author Tomas Pavek
 */

class ConnectionWizardPanel1 implements org.openide.WizardDescriptor.Panel {

    private Event selectedEvent;

    private RADComponent sourceComponent;

    private EventListenerList listenerList;

    private ConnectionPanel1 uiPanel;

    // -------

    ConnectionWizardPanel1(RADComponent source) {
        sourceComponent = source;
    }

    RADComponent getSourceComponent() {
        return sourceComponent;
    }

    Event getSelectedEvent() {
        return selectedEvent;
    }
    String getEventName() {
        return uiPanel != null ? uiPanel.getEventName() : null;
    }

    void setSelectedEvent(Event event) {
        selectedEvent = event;
        fireStateChanged();
    }

    boolean handlerAlreadyExists() {
        if (uiPanel == null)
            return false;

        return selectedEvent != null
               && selectedEvent.hasEventHandler(uiPanel.getEventName());
    }

    // ----------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (uiPanel == null)
            uiPanel = new ConnectionPanel1(this);
        return uiPanel;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("gui.connecting.source"); // NOI18N
    }

    @Override
    public boolean isValid() {
        String eventName = uiPanel != null ? uiPanel.getEventName() : null;
        return selectedEvent != null
               && eventName != null && !"".equals(eventName) 
               && org.openide.util.Utilities.isJavaIdentifier(eventName);
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

    // -----

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
