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

package org.netbeans.modules.javascript.jstestdriver.ui.customizer;

import java.awt.EventQueue;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.jstestdriver.preferences.JsTestDriverPreferences;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.util.NbBundle;

public final class CustomizerPanel implements CustomizerPanelImplementation {

    private final Project project;

    // creation @GuardedBy("this")
    private volatile CustomizerJsTestDriver customizerJsTestDriver;


    public CustomizerPanel(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public String getIdentifier() {
        return "JsTestDriver"; // NOI18N
    }

    @NbBundle.Messages("CustomizerPanel.displayName=JS Test Driver")
    @Override
    public String getDisplayName() {
        return Bundle.CustomizerPanel_displayName();
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        getComponent().addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        getComponent().removeChangeListener(listener);
    }

    @Override
    public synchronized CustomizerJsTestDriver getComponent() {
        if (customizerJsTestDriver == null) {
            customizerJsTestDriver = new CustomizerJsTestDriver(project);
        }
        return customizerJsTestDriver;
    }

    @Override
    public boolean isValid() {
        return getErrorMessage() == null;
    }

    @Override
    public String getErrorMessage() {
        return getComponent().getErrorMessage();
    }

    @Override
    public String getWarningMessage() {
        return getComponent().getWarningMessage();
    }

    @Override
    public void save() {
        assert !EventQueue.isDispatchThread();
        assert customizerJsTestDriver != null;
        JsTestDriverPreferences.setConfig(project, customizerJsTestDriver.getConfig());
    }

}
