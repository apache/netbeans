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

package org.netbeans.modules.java.examples;

import java.util.prefs.Preferences;
import org.netbeans.modules.java.examples.FoldersListSettings;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;


public class FoldersListSettings  {
    private static final FoldersListSettings INSTANCE = new FoldersListSettings();
    private static final String LAST_EXTERNAL_SOURCE_ROOT = "srcRoot";  //NOI18N
    private static final String NEW_PROJECT_COUNT = "newProjectCount"; //NOI18N


    public String displayName() {
        return NbBundle.getMessage (FoldersListSettings.class, "TXT_J2SEProjectFolderList"); //NOI18N
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(FoldersListSettings.class);
    }
    
    public String getLastExternalSourceRoot () {        
        return getPreferences().get(LAST_EXTERNAL_SOURCE_ROOT, null);
    }

    public void setLastExternalSourceRoot (String path) {
        getPreferences().put(LAST_EXTERNAL_SOURCE_ROOT, path);
    }

    public int getNewProjectCount () {
        return getPreferences().getInt(NEW_PROJECT_COUNT, 0);
    }

    public void setNewProjectCount (int count) {
        getPreferences().putInt(NEW_PROJECT_COUNT, count);
    }
    

    public static FoldersListSettings getDefault () {
        return INSTANCE;
    }
}
