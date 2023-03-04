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

package org.netbeans.modules.localtasks;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author Ondrej Vrabec
 */
public class LocalRepositoryConfig {
    private static LocalRepositoryConfig instance;
    private static final String PREF_SECTION_COLLAPSED = "collapsedSection"; //NOI18N
    private static final String PREF_TASK = "task."; //NOI18N
    
    public static synchronized LocalRepositoryConfig getInstance () {
        if (instance == null) {
            instance = new LocalRepositoryConfig();
        }
        return instance;
    }
    
    private LocalRepositoryConfig () {
        
    }

    public void setEditorSectionCollapsed (String taskId, String sectionName, boolean collapsed) {
        String key = getTaskKey(taskId) + PREF_SECTION_COLLAPSED + sectionName;
        getPreferences().putBoolean(key, collapsed);
    }

    public void deleteTaskPreferences (String taskId) {
        Preferences prefs = getPreferences();
        try {
            String taskKey = getTaskKey(taskId);
            for (String key : prefs.keys()) {
                if (key.startsWith(taskKey)) {
                    prefs.remove(key);
                }
            }
        } catch (BackingStoreException ex) {
        }
    }

    public boolean isEditorSectionCollapsed (String taskId, String sectionName, boolean defaultValue) {
        String key = getTaskKey(taskId) + PREF_SECTION_COLLAPSED + sectionName;
        return getPreferences().getBoolean(key, defaultValue);
    }
    
    private Preferences getPreferences () {
        return NbPreferences.forModule(LocalRepositoryConfig.class);
    }

    private String getTaskKey (String taskId) {
        return PREF_TASK + taskId + ".";
    }

}
