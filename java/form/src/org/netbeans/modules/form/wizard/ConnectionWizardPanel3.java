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
import java.lang.reflect.Method;
import org.netbeans.modules.form.*;

/**
 * The third panel of connection wizard - for entering parameters for method
 * which will be called on the target component (specified in step 2).
 *
 * @author Tomas Pavek
 */

class ConnectionWizardPanel3 implements org.openide.WizardDescriptor.Panel {

    private FormModel formModel;
    private Method method;

    private EventListenerList listenerList = null;

    private ConnectionPanel3 uiPanel;

    // --------

    ConnectionWizardPanel3(FormModel model) {
        formModel = model;
    }

    FormModel getFormModel() {
        return formModel;
    }

    void setMethod(Method m) {
        method = m;
        if (uiPanel != null)
            uiPanel.setMethod(m);
    }

    String getParametersText() {
        return uiPanel != null ? uiPanel.getParametersText() : null;
    }

    Object[] getParameters() {
        return uiPanel != null ? uiPanel.getParameters() : null;
    }

    // ---------
    // WizardDescriptor.Panel implementation

    @Override
    public java.awt.Component getComponent() {
        if (uiPanel == null) {
            uiPanel = new ConnectionPanel3(this);
            if (method != null)
                uiPanel.setMethod(method);
        }
        return uiPanel;
    }

    @Override
    public org.openide.util.HelpCtx getHelp() {
        return new org.openide.util.HelpCtx("gui.connecting.param"); // NOI18N
    }

    @Override
    public boolean isValid() {
        return uiPanel != null ? uiPanel.isFilled() : false;
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

    // ------

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
