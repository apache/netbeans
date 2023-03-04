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

package org.netbeans.modules.hudson.ui.impl;

import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.hudson.ui.spi.ProjectHudsonProvider;
import org.openide.util.lookup.ServiceProvider;

/**
 * Tracks job to project association based on NetBeans-specific metadata.
 */
@ServiceProvider(service=ProjectHudsonProvider.class, position=1000)
public class MetadataProjectHudsonProvider extends ProjectHudsonProvider {

    private static final String KEY = "builder"; // NOI18N

    public Association findAssociation(Project project) {
        // XXX is using shared metadata appropriate? server may or may not be public...
        Preferences prefs = ProjectUtils.getPreferences(project, MetadataProjectHudsonProvider.class, true);
        String url = prefs.get(KEY, null);
        if (url != null) {
            return Association.fromString(url);
        } else {
            return null;
        }
    }

    public boolean recordAssociation(Project project, Association assoc) {
        Preferences prefs = ProjectUtils.getPreferences(project, MetadataProjectHudsonProvider.class, true);
        if (assoc != null) {
            prefs.put(KEY, assoc.toString());
        } else {
            prefs.remove(KEY);
        }
        return true;
    }

}
