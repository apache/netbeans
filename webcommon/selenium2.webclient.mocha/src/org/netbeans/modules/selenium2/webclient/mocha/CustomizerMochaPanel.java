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
package org.netbeans.modules.selenium2.webclient.mocha;

import java.awt.EventQueue;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaJSPreferences;
import org.netbeans.modules.selenium2.webclient.mocha.preferences.MochaSeleniumPreferences;
import org.netbeans.modules.web.clientproject.spi.CustomizerPanelImplementation;
import org.openide.util.NbBundle;

/**
 *
 * @author Theofanis Oikonomou
 */
@NbBundle.Messages("CustomizerMochaPanel.displayName=Mocha")
public class CustomizerMochaPanel implements CustomizerPanelImplementation {
    
    public static final String IDENTIFIER = "Mocha"; // NOI18N

    private final Project project;
    private final boolean isSelenium;

    // creation @GuardedBy("this")
    private volatile CustomizerMocha customizerMocha;


    public CustomizerMochaPanel(Project project, boolean isSelenium) {
        assert project != null;
        this.project = project;
        this.isSelenium = isSelenium;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public String getDisplayName() {
        return Bundle.CustomizerMochaPanel_displayName();
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
    public synchronized CustomizerMocha getComponent() {
        if (customizerMocha == null) {
            customizerMocha = new CustomizerMocha(project, isSelenium);
        }
        return customizerMocha;
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
        assert customizerMocha != null;
        if(isSelenium) {
            MochaSeleniumPreferences.setMochaDir(project, customizerMocha.getMochaInstallFolder());
            MochaSeleniumPreferences.setTimeout(project, customizerMocha.getTimeout());
        } else {
            MochaJSPreferences.setMochaDir(project, customizerMocha.getMochaInstallFolder());
            MochaJSPreferences.setTimeout(project, customizerMocha.getTimeout());
            MochaJSPreferences.setAutoWatch(project, customizerMocha.getAutoWatch());
        }
    }

}
