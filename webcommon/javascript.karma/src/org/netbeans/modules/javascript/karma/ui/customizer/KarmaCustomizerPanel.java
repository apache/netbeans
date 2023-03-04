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

package org.netbeans.modules.javascript.karma.ui.customizer;

import java.awt.EventQueue;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.karma.preferences.KarmaPreferences;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.util.NbBundle;

public final class KarmaCustomizerPanel implements CustomizerPanelImplementation {

    private final Project project;

    // creation @GuardedBy("this")
    private volatile CustomizerKarma customizerKarma;


    public KarmaCustomizerPanel(Project project) {
        assert project != null;
        this.project = project;
    }

    @Override
    public String getIdentifier() {
        return "Karma"; // NOI18N
    }

    @NbBundle.Messages("KarmaCustomizerPanel.displayName=Karma")
    @Override
    public String getDisplayName() {
        return Bundle.KarmaCustomizerPanel_displayName();
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
    public synchronized CustomizerKarma getComponent() {
        if (customizerKarma == null) {
            customizerKarma = new CustomizerKarma(project);
        }
        return customizerKarma;
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
        assert customizerKarma != null;
        KarmaPreferences.setConfig(project, customizerKarma.getConfig());
        KarmaPreferences.setAutowatch(project, customizerKarma.isAutowatch());
        KarmaPreferences.setFailOnBrowserError(project, customizerKarma.isFailOnBrowserError());
        KarmaPreferences.setDebug(project, customizerKarma.isDebug());
        KarmaPreferences.setDebugBrowserId(project, customizerKarma.getSelectedBrowserId());
    }

}
