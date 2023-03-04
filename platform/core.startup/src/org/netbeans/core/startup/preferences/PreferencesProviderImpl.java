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

package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;
import org.netbeans.Util;
import org.openide.util.lookup.ServiceProvider;

/**
 * @author Radek Matous
 */
@ServiceProvider(service=org.openide.util.NbPreferences.Provider.class)
public class PreferencesProviderImpl implements org.openide.util.NbPreferences.Provider {
    public Preferences preferencesForModule(Class cls) {
        String absolutePath = null;
        // Could use Modules.getDefault().ownerOf(cls) but this would initialize
        // module system which may be undesirable in general. Fix might be to
        // register Modules global impl from o.n.bootstrap using Util.ModuleProvider,
        // though this needs to be overridden by ModuleManager impl since that has
        // specific behavior in case of JNLP.
        ClassLoader cl = cls.getClassLoader();
        if (cl instanceof Util.ModuleProvider) {
            absolutePath = ((Util.ModuleProvider) cl).getModule().getCodeNameBase();
        } else {
            absolutePath = cls.getName().replaceFirst("(^|\\.)[^.]+$", "");//NOI18N
        }
        assert absolutePath != null;        
        return preferencesRoot().node(absolutePath.replace('.','/'));//NOI18N
    }
    
    public Preferences preferencesRoot() {
        return NbPreferences.userRootImpl();
    }
}
