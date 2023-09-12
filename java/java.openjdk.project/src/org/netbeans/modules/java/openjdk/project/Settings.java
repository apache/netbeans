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
package org.netbeans.modules.java.openjdk.project;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author lahvac
 */
public class Settings {

    private static final String KEY_BUILD_BEFORE_TESTS = "build-before-test";
    private static final String KEY_JTREG_LOCATION = "jtreg-location";
    private static final String KEY_USE_ANT_BUILD = "use-langtools-ant-build";
    private static final String KEY_ANT_BUILD_LOCATION = "langtools-ant-build-location";

    private static final String DEF_ANT_BUILD_LOCATION = "make/ide/netbeans/langtools/build.xml";

    private final Project prj;

    public Settings(Project prj) {
        this.prj = prj;
    }

    private Preferences getPrivatePreferences() {
        return ProjectUtils.getPreferences(prj, Settings.class, false);
    }

    public RunBuild getRunBuildSetting() {
        try {
            return RunBuild.valueOf(getPrivatePreferences().get(KEY_BUILD_BEFORE_TESTS, RunBuild.ALWAYS.name()));
        } catch (IllegalArgumentException ex) {
            return RunBuild.ALWAYS;
        }
    }

    public void setRunBuildSetting(RunBuild value) {
        getPrivatePreferences().put(KEY_BUILD_BEFORE_TESTS, value.name());
    }

    public String getJTregLocation() {
        return getPrivatePreferences().get(KEY_JTREG_LOCATION, null);
    }

    public void setJTregLocation(String jtregLocation) {
        getPrivatePreferences().put(KEY_JTREG_LOCATION, jtregLocation);
    }

    public boolean isUseAntBuild() {
        return getPrivatePreferences().getBoolean(KEY_USE_ANT_BUILD, true);
    }

    public void setUseAntBuild(boolean useAntBuild) {
        getPrivatePreferences().putBoolean(KEY_USE_ANT_BUILD, useAntBuild);
    }

    public String getAntBuildLocation() {
        return getPrivatePreferences().get(KEY_ANT_BUILD_LOCATION, DEF_ANT_BUILD_LOCATION);
    }

    public void setAntBuildLocation(String antBuildLocation) {
        getPrivatePreferences().put(KEY_ANT_BUILD_LOCATION, antBuildLocation);
    }

    public void flush() {
        try {
            getPrivatePreferences().flush();
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public enum RunBuild {
        ALWAYS,
//        SMART,
        NEVER;
    }

}
