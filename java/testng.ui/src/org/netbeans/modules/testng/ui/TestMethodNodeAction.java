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

package org.netbeans.modules.testng.ui;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.Action;
import org.netbeans.spi.project.ActionProvider;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * Action for execution of an arbitrary command using a project's
 * {@code ActionProvider}.
 *
 * @author  Marian Petras
 */
final class TestMethodNodeAction implements Action {

    private final ActionProvider actionProvider;
    private final Lookup context;
    private final String command;
    private final String bundleKey;

    public TestMethodNodeAction(ActionProvider actionProvider,
                                Lookup context,
                                String command,
                                String nameBundleKey) {
        this.actionProvider = actionProvider;
        this.context = context;
        this.command = command;
        this.bundleKey = nameBundleKey;
    }

    public void actionPerformed(ActionEvent ev) {
        actionProvider.invokeAction(command, context);
    }

    public Object getValue(String key) {
        if (key == null) {
            return null;
        }

        if (key.equals(Action.NAME)) {
            return NbBundle.getMessage(TestMethodNodeAction.class, bundleKey);
        } else if (key.equals(Action.ACTION_COMMAND_KEY)) {
            return command;
        } else {
            return null;
        }
    }

    public void putValue(String key, Object value) {
        throw new UnsupportedOperationException(
                "This should not be called.");                          //NOI18N
    }

    public boolean isEnabled() {
        return true;
    }

    public void setEnabled(boolean b) {
        throw new UnsupportedOperationException(
                "This should not be called.");                          //NOI18N
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //no property changes - no listeners
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //no property changes - no listeners
    }

}
