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
package org.netbeans.modules.maven.options;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 * Preferences class 
 * @author mkleint
 */
public final class DontShowAgainSettings {
    private static final DontShowAgainSettings INSTANCE = new DontShowAgainSettings();
    
    public static DontShowAgainSettings getDefault() {
        return INSTANCE;
    }

    private DontShowAgainSettings() {
    }
    
    protected final Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/showQuestions"); //NOI18N
    }
    
    public boolean showWarningAboutBuildWithDependencies() {
        return getPreferences().getBoolean("showBuildWithDependenciesWarning", true);//NOI18N
    }

    public void dontShowWarningAboutBuildWithDependenciesAnymore() {
        getPreferences().putBoolean("showBuildWithDependenciesWarning", false);//NOI18N
    }

    public boolean showWarningAboutApplicationCoS() {
        return getPreferences().getBoolean("showApplicationCompileOnSave", true);//NOI18N
    }

    public void dontshowWarningAboutApplicationCoSAnymore() {
        getPreferences().putBoolean("showApplicationCompileOnSave", false);//NOI18N
    }
}
