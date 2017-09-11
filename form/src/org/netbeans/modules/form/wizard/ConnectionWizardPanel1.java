/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
