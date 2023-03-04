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
package org.netbeans.modules.diff.builtin.visualizer;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;


/**
 * Re-sources action to given source.
 *
 * @author Petr Kuzel
 */
public class SourceTranslatorAction implements Action, PropertyChangeListener {

    final Action scrollAction;
    final Object source;
    final PropertyChangeSupport support;

    public SourceTranslatorAction(Action action, Object source) {
        scrollAction = action;
        this.source = source;
        support = new PropertyChangeSupport(action);
    }

    public Object getValue(String key) {
        return scrollAction.getValue(key);
    }

    public void putValue(String key, Object value) {
        scrollAction.putValue(key, value);
    }

    public void setEnabled(boolean b) {
        scrollAction.setEnabled(b);
    }

    public boolean isEnabled() {
        return scrollAction.isEnabled();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (support.hasListeners(null) == false) {
            scrollAction.addPropertyChangeListener(this);
        }
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
        if (support.hasListeners(null) == false) {
            scrollAction.removePropertyChangeListener(this);
        }
    }

    public void actionPerformed(ActionEvent e) {
        ActionEvent event = new ActionEvent(source, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers());
        scrollAction.actionPerformed(event);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        support.firePropertyChange(evt);
    }
}
