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
package org.netbeans.modules.php.nette2.options;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Nette2Options {
    private static final String PREFERENCES_PATH = "nette2"; //NOI18N
    private static final Nette2Options INSTANCE = new Nette2Options();
    private static final String SANDBOX = "sandbox"; // NOI18N
    private static final String NETTE_DIRECTORY = "nette-directory"; // NOI18N
    private final ChangeSupport changeSupport = new ChangeSupport(this);

    public static Nette2Options getInstance() {
        return INSTANCE;
    }

    private Nette2Options() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(Nette2Options.class).node(PREFERENCES_PATH);
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getSandbox() {
        return getPreferences().get(SANDBOX, null);
    }

    public void setSandbox(String sandbox) {
        getPreferences().put(SANDBOX, sandbox);
    }

    public String getNetteDirectory() {
        return getPreferences().get(NETTE_DIRECTORY, null);
    }

    public void setNetteDirectory(String netteDirectory) {
        getPreferences().put(NETTE_DIRECTORY, netteDirectory);
    }

}
