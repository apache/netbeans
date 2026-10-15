/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.web.clientproject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.web.clientproject.env.CommonProjectHelper;
import org.openide.util.EditableProperties;

public class ProjectUpgrader {

    private final ClientSideProject project;


    public ProjectUpgrader(ClientSideProject project) {
        assert project != null;
        this.project = project;
    }

    public void upgrade() {
        ProjectManager.mutex().writeAccess(new Runnable() {
            @Override
            public void run() {
                upgradeProjectProperties();
            }
        });
    }

    void upgradeProjectProperties() {
        EditableProperties properties = project.getProjectHelper().getProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH);
        // specific upgrades
        upgradeGrunt(properties);
        project.getProjectHelper().putProperties(CommonProjectHelper.PROJECT_PROPERTIES_PATH, properties);
    }

    //~ Grunt

    private static final String LEGACY_GRUNT_ACTION_PREFIX = "grunt.action."; // NOI18N
    private static final String GRUNT_ACTION_PREFIX = "auxiliary.org-netbeans-modules-javascript-grunt.action_2e_"; // NOI18N


    private void upgradeGrunt(EditableProperties properties) {
        Set<String> toRemove = null;
        Map<String, String> toAdd = null;
        for (Map.Entry<String, String> entry : properties.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(LEGACY_GRUNT_ACTION_PREFIX)) {
                if (toRemove == null) {
                    toRemove = new HashSet<>();
                }
                toRemove.add(key);
                if (toAdd == null) {
                    toAdd = new HashMap<>();
                }
                String newKey = GRUNT_ACTION_PREFIX + key.replace(LEGACY_GRUNT_ACTION_PREFIX, ""); // NOI18N
                toAdd.put(newKey, entry.getValue());
            }
        }
        if (toRemove != null) {
            properties.keySet().removeAll(toRemove);
        }
        if (toAdd != null) {
            properties.putAll(toAdd);
        }
    }

}
