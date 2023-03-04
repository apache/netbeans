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
package org.netbeans.modules.javascript.grunt.ui.customizer;

import java.awt.EventQueue;
import javax.swing.JComponent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.javascript.grunt.preferences.GruntPreferences;
import org.netbeans.modules.web.clientproject.spi.build.CustomizerPanelImplementation;

public class GruntCustomizerPanelImpl implements CustomizerPanelImplementation {

    private final GruntPreferences preferences;

    // @GuardedBy("this")
    private GruntCustomizerPanel panel;


    GruntCustomizerPanelImpl(GruntPreferences preferences) {
        assert preferences != null;
        this.preferences = preferences;
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getPanel().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getPanel().removeChangeListener(listener);
    }

    @Override
    public JComponent getComponent() {
        return getPanel();
    }

    @Override
    public boolean isValid() {
        return getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        return getPanel().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getPanel().getWarningMessage();
    }

    @Override
    public void save() {
        assert !EventQueue.isDispatchThread();
        preferences.setTasks(getPanel().getTaks());
    }

    private synchronized GruntCustomizerPanel getPanel() {
        if (panel == null) {
            panel = new GruntCustomizerPanel(preferences.getProject(), preferences.getTasks());
        }
        return panel;
    }

}
