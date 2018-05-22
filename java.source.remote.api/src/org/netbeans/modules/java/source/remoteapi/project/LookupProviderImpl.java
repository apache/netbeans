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
package org.netbeans.modules.java.source.remoteapi.project;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.modules.java.source.remoteapi.DefaultRemotePlatform;
import org.netbeans.spi.project.LookupProvider;
import org.netbeans.spi.project.LookupProvider.Registration;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author lahvac
 */
@Registration(projectType="org-netbeans-modules-java-j2seproject")
public class LookupProviderImpl implements LookupProvider {

    @Override
    public Lookup createAdditionalLookup(Lookup baseContext) {
        Project prj = baseContext.lookup(Project.class);

        if (prj == null)
            return Lookup.EMPTY;

        Preferences p = ProjectCustomizerImpl.getPreferences(prj);

        return new AdditionalLookup(p);
    }

    private static final class AdditionalLookup extends ProxyLookup implements PreferenceChangeListener {

        private final Preferences settings;
        private final Lookup withPlatform;

        public AdditionalLookup(Preferences settings) {
            this.settings = settings;
            settings.addPreferenceChangeListener(this);
            withPlatform = Lookups.singleton(new DefaultRemotePlatform.ProviderImpl(true));
            preferenceChange(null);
        }

        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
            if (ProjectCustomizerImpl.isRemotingEnabled(settings)) {
                if (getLookups().length == 0) {
                    setLookups(withPlatform);
                }
            } else {
                if (getLookups().length != 0) {
                    setLookups();
                }
            }
        }

    }
}
