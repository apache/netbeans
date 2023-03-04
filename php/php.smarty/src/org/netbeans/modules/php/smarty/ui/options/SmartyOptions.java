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

package org.netbeans.modules.php.smarty.ui.options;

import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.php.smarty.SmartyFramework;
import org.openide.util.ChangeSupport;
import org.openide.util.NbPreferences;

/**
 * @author Martin Fousek
 */
public final class SmartyOptions {
    // Do not change arbitrary - consult with layer's folder OptionsExport
    // Path to Preferences node for storing these preferences
    protected static final String PREFERENCES_PATH = "smarty"; // NOI18N

    private static final SmartyOptions INSTANCE = new SmartyOptions();

    // default values for Smarty properties
    public static final int DEFAULT_TPL_SCANNING_DEPTH = 1;

    // preferences properties names
    private static final String OPEN_DELIMITER = "{"; // NOI18N
    private static final String CLOSE_DELIMITER = "}"; // NOI18N
    protected static final String PROP_TPL_VERSION = "tpl-version";
    protected static final String PROP_TPL_TOGGLE_COMMENT = "tpl-toggle-comment";

    final ChangeSupport changeSupport = new ChangeSupport(this);

    private SmartyOptions() {
        getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
            @Override
            public void preferenceChange(PreferenceChangeEvent evt) {
                changeSupport.fireChange();
            }
        });
    }

    public static SmartyOptions getInstance() {
        return INSTANCE;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    public String getDefaultOpenDelimiter() {
        return getPreferences().get(OPEN_DELIMITER, SmartyFramework.OPEN_DELIMITER);
    }

    public void setDefaultOpenDelimiter(String delimiter) {
        getPreferences().put(OPEN_DELIMITER, delimiter);
        SmartyFramework.setDelimiterDefaultOpen(delimiter);
    }

    public String getDefaultCloseDelimiter() {
        return getPreferences().get(CLOSE_DELIMITER, SmartyFramework.CLOSE_DELIMITER);
    }

    public void setDefaultCloseDelimiter(String delimiter) {
        getPreferences().put(CLOSE_DELIMITER, delimiter);
        SmartyFramework.setDelimiterDefaultClose(delimiter);
    }

    public SmartyFramework.Version getSmartyVersion() {
        String version = getPreferences().get(PROP_TPL_VERSION, "SMARTY3"); // NOI18N
        return SmartyFramework.Version.valueOf(version);
    }

    public void setSmartyVersion(SmartyFramework.Version version) {
        getPreferences().put(PROP_TPL_VERSION, version.name());
        SmartyFramework.setSmartyVersion(version);
    }

    public SmartyFramework.ToggleCommentOption getToggleCommentOption() {
        String commentOption = getPreferences().get(PROP_TPL_TOGGLE_COMMENT,
                SmartyFramework.ToggleCommentOption.SMARTY.name());
        return SmartyFramework.ToggleCommentOption.valueOf(commentOption);
    }

    public void setToggleCommentOption(SmartyFramework.ToggleCommentOption commentOption) {
        getPreferences().put(PROP_TPL_TOGGLE_COMMENT, commentOption.name());
        SmartyFramework.setToggleCommentOption(commentOption);
    }

    private static Preferences getPreferences() {
        return NbPreferences.forModule(SmartyOptions.class).node(PREFERENCES_PATH);
    }

}
