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
package org.netbeans.modules.debugger.jpda.projectsui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.editor.ext.ToolTipSupport;

/**
 *
 * @author martin
 */
final class DebuggerStateChangeListener implements PropertyChangeListener, Runnable {

    private final ToolTipSupport tts;

    private DebuggerStateChangeListener(ToolTipSupport tts) {
        this.tts = tts;
    }

    static void attach(JPDADebugger d, ToolTipSupport tts) {
        DebuggerStateChangeListener dscl = new DebuggerStateChangeListener(tts);
        d.addPropertyChangeListener(JPDADebugger.PROP_STATE, dscl);
        tts.addPropertyChangeListener(propListener -> {
            if (ToolTipSupport.PROP_STATUS.equals(propListener.getPropertyName()) &&
                    !tts.isToolTipVisible()) {
                d.removePropertyChangeListener(JPDADebugger.PROP_STATE, dscl);
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        int state = ((Integer) evt.getNewValue());
        if (JPDADebugger.STATE_DISCONNECTED == state ||
            JPDADebugger.STATE_RUNNING == state) {
            SwingUtilities.invokeLater(this);
        }
    }

    @Override
    public void run() {
        tts.setToolTipVisible(false);
    }
}
