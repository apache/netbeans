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
package org.netbeans.modules.java.source.tasklist;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.openide.util.NbPreferences;

/**
 *
 * @author Jan Lahoda
 */
public class TasklistSettings {

    private static final String KEY_DEPENDENCY_TRACKING = "dependency-tracking"; //NOI18N
    private static final String DEFAULT_DEPENDENCY_TRACKING = DependencyTracking.ENABLED.name();

    static {
        getPreferencesNode().addPreferenceChangeListener(new PreferenceChangeListener() {

            private DependencyTracking curr = getDependencyTracking();

            public void preferenceChange(PreferenceChangeEvent evt) {
                if (KEY_DEPENDENCY_TRACKING.equals(evt.getKey())) {
                    final DependencyTracking dt = getDependencyTracking();
                    if (curr != dt) {
                        if (dt.ordinal() > curr.ordinal()) {
                            IndexingManager.getDefault().refreshAllIndices(JavaIndex.NAME);
                        }
                        curr = dt;
                    }
                }
            }
        });
    }
    
    private TasklistSettings() {
    }
    
    public static boolean isBadgesEnabled() {
        return getDependencyTracking() != DependencyTracking.DISABLED;
    }

    public static DependencyTracking getDependencyTracking() {
        String s = getPreferencesNode().get(KEY_DEPENDENCY_TRACKING, DEFAULT_DEPENDENCY_TRACKING);
        try {
            return DependencyTracking.valueOf(s);
        } catch (IllegalArgumentException e) {
            return DependencyTracking.valueOf(DEFAULT_DEPENDENCY_TRACKING);
        }
    }
    
    private static Preferences getPreferencesNode() {
        return NbPreferences.forModule(TasklistSettings.class).node("tasklist");
    }

    public static enum DependencyTracking {
        DISABLED,
        ENABLED_WITHIN_ROOT,
        ENABLED_WITHIN_PROJECT,
        ENABLED
    }
}
