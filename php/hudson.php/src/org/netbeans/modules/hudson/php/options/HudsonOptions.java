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
package org.netbeans.modules.hudson.php.options;

import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * Hudson PHP options.
 */
public final class HudsonOptions {

    private static final Logger LOGGER = Logger.getLogger(HudsonOptions.class.getName());

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "hudson"; // NOI18N

    private static final HudsonOptions INSTANCE = new HudsonOptions();

    // properties
    private static final String BUILD_XML = "build.xml.path"; // NOI18N
    private static final String JOB_CONFIG = "job.config.path"; // NOI18N
    private static final String PHP_UNIT_CONFIG = "phpunit.config.path"; // NOI18N

    final ChangeSupport changeSupport = new ChangeSupport(this);


    private HudsonOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static HudsonOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @CheckForNull
    public String getBuildXml() {
        return getPreferences().get(BUILD_XML, null);
    }

    public void setBuildXml(String buildXml) {
        getPreferences().put(BUILD_XML, buildXml);
    }

    @CheckForNull
    public String getJobConfig() {
        return getPreferences().get(JOB_CONFIG, null);
    }

    public void setJobConfig(String jobConfig) {
        getPreferences().put(JOB_CONFIG, jobConfig);
    }

    @CheckForNull
    public String getPhpUnitConfig() {
        return getPreferences().get(PHP_UNIT_CONFIG, null);
    }

    public void setPhpUnitConfig(String phpUnitConfig) {
        getPreferences().put(PHP_UNIT_CONFIG, phpUnitConfig);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(HudsonOptions.class).node(PREFERENCES_PATH);
    }

}
