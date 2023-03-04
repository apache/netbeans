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
 * Preferences class for externalizing the hardwired plugin versions to
 * allow changes by advanced users?
 * @author mkleint
 */
public final class MavenVersionSettings {
    private static final MavenVersionSettings INSTANCE = new MavenVersionSettings();
    
    public static final String VERSION_COMPILER = "maven-compiler-plugin"; //NOI18N
    public static final String VERSION_RESOURCES = "maven-resources-plugin"; //NOI18N
    
    public static MavenVersionSettings getDefault() {
        return INSTANCE;
    }
    
    protected final Preferences getPreferences() {
        return NbPreferences.root().node("org/netbeans/modules/maven/pluginVersions"); //NOI18N
    }
    
    protected final String putProperty(String key, String value) {
        String retval = getProperty(key);
        if (value != null) {
            getPreferences().put(key, value);
        } else {
            getPreferences().remove(key);
        }
        return retval;
    }

    protected final String getProperty(String key) {
        return getPreferences().get(key, null);
    }    
    
    private MavenVersionSettings() {
    }
    
    public String getVersion(String plugin) {
        String toRet = getProperty(plugin);
        if (toRet == null) {
            // XXX these should rather read the most recent version from the repository index
            if (VERSION_RESOURCES.equals(plugin)) {
                toRet = "2.4.3"; //NOI18N
            }
            else if (VERSION_COMPILER.equals(plugin)) {
                toRet = "2.3.2"; //NOI18N
            }
        }
        if (toRet == null) {
            toRet = "RELEASE"; // this is wrong for 2.1
        }
        return toRet;
    }
    
}
