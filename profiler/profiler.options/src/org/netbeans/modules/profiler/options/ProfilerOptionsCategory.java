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

package org.netbeans.modules.profiler.options;

import java.beans.PropertyChangeListener;
import org.netbeans.modules.options.java.api.JavaOptions;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsContainer;
import org.netbeans.modules.profiler.options.ui.v2.ProfilerOptionsPanel;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
    location=JavaOptions.JAVA,
    displayName="#ProfilerOptionsCategory_Title",
    keywords="#KW_ProfilerOptions",
    keywordsCategory=JavaOptions.JAVA + "/Profiler")
public class ProfilerOptionsCategory extends OptionsPanelController {
    
    private static final HelpCtx HELP_CTX = new HelpCtx("ProfilerOptions.Help"); // NOI18N
    
    private static ProfilerOptionsPanel settingsPanel = null;

    
    public ProfilerOptionsPanel getComponent(Lookup lookup) {
        if (settingsPanel == null) settingsPanel = new ProfilerOptionsContainer();
        return settingsPanel;
    }

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }
    
    public boolean isChanged() {
        if (settingsPanel == null) return false;
        return !settingsPanel.equalsTo(ProfilerIDESettings.getInstance());
    }

    public boolean isValid() {
        return true;
    }

    public void applyChanges() {
        if (settingsPanel == null) return;
        settingsPanel.storeTo(ProfilerIDESettings.getInstance());
    }

    public void cancel() {
    }

    public void update() {
        if (settingsPanel == null) return;
        settingsPanel.loadFrom(ProfilerIDESettings.getInstance());
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
    }
    
    public void removePropertyChangeListener(PropertyChangeListener l) {
    }

}
