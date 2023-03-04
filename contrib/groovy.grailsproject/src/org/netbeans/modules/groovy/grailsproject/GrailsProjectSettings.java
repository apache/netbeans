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

package org.netbeans.modules.groovy.grailsproject;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;
import java.io.File;

/**
 *
 * @author schmidtm
 */
public class GrailsProjectSettings {
    
    private static final GrailsProjectSettings INSTANCE = new GrailsProjectSettings();
    
    private static final String LAST_USED_ARTIFACT_FOLDER = "lastUsedArtifactFolder"; //NOI18N
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N
    
    public static GrailsProjectSettings getDefault () {
        return INSTANCE;
    }
    
    private static Preferences getPreferences() {
        return NbPreferences.forModule(GrailsProjectSettings.class);
    }

    public int getNewProjectCount () {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public void setNewProjectCount (int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }    
    
    public File getLastUsedArtifactFolder () {
        return new File (getPreferences().get(LAST_USED_ARTIFACT_FOLDER, System.getProperty("user.home")));
    }

    public void setLastUsedArtifactFolder (File folder) {
        assert folder != null : "Folder can not be null";
        String path = folder.getAbsolutePath();
        getPreferences().put(LAST_USED_ARTIFACT_FOLDER, path);
    }   
    
}
