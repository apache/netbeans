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
package org.netbeans.modules.csl.api;

import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.settings.SimpleValueNames;

/**
 * Manage a set of options configurable by the user in the IDE.
 * Language plugins can and should register their own options panels,
 * but some editor options (such as tab settings) are managed by the IDE,
 * in and these can be accessed via this class.
 * 
 * @author Tor Norbye
 */
public final class EditorOptions {
    
    @CheckForNull
    public static EditorOptions get (
        String                  mimeType
    ) {
        return new EditorOptions (mimeType);
    }
    
    private final String        mimeType;
    private final Preferences   preferences;

    private EditorOptions (
        String                  mimeType
    ) {
        this.mimeType = mimeType;
        this.preferences = MimeLookup.getLookup (mimeType).lookup (Preferences.class);
    }

    public int getTabSize () {
        return preferences.getInt (SimpleValueNames.TAB_SIZE, 8);
    }

    public boolean getExpandTabs () {
        return preferences.getBoolean (SimpleValueNames.EXPAND_TABS, true);
    }

    public int getSpacesPerTab () {
        return preferences.getInt (SimpleValueNames.SPACES_PER_TAB, 2);
    }

    public boolean getMatchBrackets () {
        return preferences.getBoolean (SimpleValueNames.COMPLETION_PAIR_CHARACTERS, true);
    }

    public int getRightMargin () {
        return preferences.getInt (SimpleValueNames.TEXT_LIMIT_WIDTH, 80);
    }
}
