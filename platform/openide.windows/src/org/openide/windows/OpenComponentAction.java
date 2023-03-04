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

package org.openide.windows;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

/** Opens a top component.
 *
 * @author Jaroslav Tulach
 */
final class OpenComponentAction implements ActionListener {
    private TopComponent component;
    private final Map<?,?> map;

    OpenComponentAction(TopComponent component) {
        assert component != null; //to diagnose #185355
        this.component = component;
        map = null;
    }
    
    OpenComponentAction(Map<?,?> map) {
        this.map = map;
    }
    
    private TopComponent getTopComponent() {
        assert EventQueue.isDispatchThread();
        if (component != null) {
            return component;
        }
        TopComponent c = null;
        Object id = map.get("preferredID"); // NOI18N
        if (id instanceof String) {
            c = WindowManager.getDefault().findTopComponent((String)id);
        }
        if (c == null) {
            c = (TopComponent)map.get("component");
        }
        if (id != null) {
            component = c;
        }
        return c;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent win = getTopComponent();
        if( null == win ) {
            throw new IllegalStateException( "Cannot find TopComponent with preferredID " 
                    + map.get("preferredID") + ", see IDE log for more details." ); //NOI18N
        }
        win.open();
        win.requestActive();
    }
}
