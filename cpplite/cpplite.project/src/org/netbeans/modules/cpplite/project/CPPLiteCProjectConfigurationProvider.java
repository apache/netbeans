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
package org.netbeans.modules.cpplite.project;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.cpplite.editor.spi.CProjectConfigurationProvider;
import org.openide.util.ChangeSupport;

/**
 *
 * @author lahvac
 */
public class CPPLiteCProjectConfigurationProvider implements CProjectConfigurationProvider {

    private final ChangeSupport cs = new ChangeSupport(this);
    private final Preferences mainPrefs;

    public CPPLiteCProjectConfigurationProvider(Preferences mainPrefs) {
        this.mainPrefs = mainPrefs;
        this.mainPrefs.addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                if (evt.getKey() == null ||
                    CPPLiteProject.KEY_COMPILE_COMMANDS.equals(evt.getKey()) ||
                    CPPLiteProject.KEY_COMPILE_COMMANDS_EXECUTABLE.equals(evt.getKey())) {
                    cs.fireChange();
                }
            }
        });
    }

    @Override
    public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override
    public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public ProjectConfiguration getProjectConfiguration() {
        String path = mainPrefs.get(CPPLiteProject.KEY_COMPILE_COMMANDS, null);
        if (path != null) {
            return new ProjectConfiguration(path);
        }
        String command = mainPrefs.get(CPPLiteProject.KEY_COMPILE_COMMANDS_EXECUTABLE, null);
        if (command != null) {
            return new ProjectConfiguration(Utils.decode(command).get(0));
        }
        return null;
    }
    
}
