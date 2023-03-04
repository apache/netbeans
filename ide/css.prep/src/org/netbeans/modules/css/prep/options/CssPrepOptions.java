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
package org.netbeans.modules.css.prep.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.modules.css.prep.less.LessExecutable;
import org.netbeans.modules.css.prep.sass.SassCli;
import org.netbeans.modules.css.prep.util.FileUtils;
import org.openide.util.NbPreferences;

public final class CssPrepOptions {

    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    private static final String PREFERENCES_PATH = "css-prep"; // NOI18N

    public static final String SASS_PATH_PROPERTY = "SASS_PATH_PROPERTY"; // NOI18N
    public static final String LESS_PATH_PROPERTY = "LESS_PATH_PROPERTY"; // NOI18N

    private static final CssPrepOptions INSTANCE = new CssPrepOptions();

    // sass
    private static final String SASS_PATH = "sass.path"; // NOI18N
    private static final String SASS_OUTPUT_ON_ERROR = "sass.outputOnError"; // NOI18N
    private static final String SASS_DEBUG = "sass.debug"; // NOI18N
    // less
    private static final String LESS_PATH = "less.path"; // NOI18N
    private static final String LESS_OUTPUT_ON_ERROR = "less.outputOnError"; // NOI18N
    private static final String LESS_DEBUG = "less.debug"; // NOI18N

    final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);

    private volatile boolean sassSearched = false;
    private volatile boolean lessSearched = false;


    private CssPrepOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                String key = evt.getKey();
                String newValue = evt.getNewValue();
                if (SASS_PATH.equals(key)) {
                    propertyChangeSupport.firePropertyChange(SASS_PATH_PROPERTY, null, newValue);
                } else if (LESS_PATH.equals(key)) {
                    propertyChangeSupport.firePropertyChange(LESS_PATH_PROPERTY, null, newValue);
                }
            }
        });
    }

    public static CssPrepOptions getInstance() {
        return INSTANCE;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }

    @CheckForNull
    public String getSassPath() {
        String path = getPreferences().get(SASS_PATH, null);
        if (path == null && !sassSearched) {
            sassSearched = true;
            List<String> paths = FileUtils.findFileOnUsersPath(SassCli.getExecutableNames());
            if (!paths.isEmpty()) {
                path = paths.get(0);
                setSassPath(path);
            }
        }
        return path;
    }

    public void setSassPath(String sassPath) {
        getPreferences().put(SASS_PATH, sassPath);
    }

    public boolean getSassOutputOnError() {
        return getPreferences().getBoolean(SASS_OUTPUT_ON_ERROR, true);
    }

    public void setSassOutpuOnError(boolean outputOnError) {
        getPreferences().putBoolean(SASS_OUTPUT_ON_ERROR, outputOnError);
    }

    public boolean getSassDebug() {
        return getPreferences().getBoolean(SASS_DEBUG, true);
    }

    public void setSassDebug(boolean debug) {
        getPreferences().putBoolean(SASS_DEBUG, debug);
    }

    @CheckForNull
    public String getLessPath() {
        String path = getPreferences().get(LESS_PATH, null);
        if (path == null && !lessSearched) {
            lessSearched = true;
            List<String> paths = FileUtils.findFileOnUsersPath(LessExecutable.EXECUTABLE_LONG_NAME, LessExecutable.EXECUTABLE_NAME);
            if (!paths.isEmpty()) {
                path = paths.get(0);
                setLessPath(path);
            }
        }
        return path;
    }

    public void setLessPath(String lessPath) {
        getPreferences().put(LESS_PATH, lessPath);
    }

    public boolean getLessOutputOnError() {
        return getPreferences().getBoolean(LESS_OUTPUT_ON_ERROR, true);
    }

    public void setLessOutpuOnError(boolean outputOnError) {
        getPreferences().putBoolean(LESS_OUTPUT_ON_ERROR, outputOnError);
    }

    public boolean getLessDebug() {
        return getPreferences().getBoolean(LESS_DEBUG, true);
    }

    public void setLessDebug(boolean debug) {
        getPreferences().putBoolean(LESS_DEBUG, debug);
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(CssPrepOptions.class).node(PREFERENCES_PATH);
    }

}
