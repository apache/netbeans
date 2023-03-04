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

package org.netbeans.modules.php.phpdoc.ui.options;

import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.phpdoc.PhpDocScript;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Tomas Mysik
 */
public final class PhpDocOptions {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "phpdoc"; // NOI18N

    private static final PhpDocOptions INSTANCE = new PhpDocOptions();

    // phpdoc script
    private static final String PHPDOC = "phpdoc"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private volatile boolean phpDocSearched = false;

    private PhpDocOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static PhpDocOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public synchronized String getPhpDoc() {
        String phpDoc = getPreferences().get(PHPDOC, null);
        if (phpDoc == null && !phpDocSearched) {
            phpDocSearched = true;
            List<String> scripts = FileUtils.findFileOnUsersPath(PhpDocScript.SCRIPT_NAME, PhpDocScript.SCRIPT_NAME_LONG, PhpDocScript.SCRIPT_NAME_PHAR);
            if (!scripts.isEmpty()) {
                phpDoc = scripts.get(0);
                setPhpDoc(phpDoc);
            }
        }
        return phpDoc;
    }

    public void setPhpDoc(String phpDoc) {
        getPreferences().put(PHPDOC, phpDoc);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(PhpDocOptions.class).node(PREFERENCES_PATH);
    }
}
