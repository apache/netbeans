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
package org.netbeans.modules.jumpto.settings;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author Tomas Zezula
 */
public final class GoToSettings {

    public enum HighlightingMode {
        NONE("none", NbBundle.getMessage(GoToSettings.class, "NAME_NONE")),
        ACTIVE("active", NbBundle.getMessage(GoToSettings.class, "NAME_ACTIVE")),
        ALL("all", NbBundle.getMessage(GoToSettings.class, "NAME_ALL"));

        private static final Map<String, HighlightingMode> modesBySystemName;
        static {
            final Map<String,HighlightingMode> map = new HashMap<>();
            for (HighlightingMode m : HighlightingMode.values()) {
                map.put(m.getSystemName(), m);
            }
            modesBySystemName = Collections.unmodifiableMap(map);
        }

        private final String systemName;
        private final String displayName;

        private HighlightingMode(@NonNull String systemName, @NonNull String displayName) {
            assert systemName != null;
            assert displayName != null;
            this.systemName = systemName;
            this.displayName = displayName;
        }

        @NonNull
        public String getDisplayName() {
            return this.displayName;
        }

        @NonNull
        String getSystemName() {
            return this.systemName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @NonNull
        static HighlightingMode forSystemName(@NullAllowed final String sysName) {
            HighlightingMode m = modesBySystemName.get(sysName);
            if (m == null) {
                m = getDefault();
            }
            return m;
        }

        @NonNull
        private static HighlightingMode getDefault() {
            return ACTIVE;
        }
    }

    public enum HighlightingType {
        BOLD("bold", NbBundle.getMessage(GoToSettings.class, "NAME_BOLD")),
        BACKGROUND("background", NbBundle.getMessage(GoToSettings.class, "NAME_BACKGROUND"));

        private static final Map<String,HighlightingType> typesBySystemName;
        static {
            Map<String,HighlightingType> map = new HashMap<>();
            for (HighlightingType t : HighlightingType.values()) {
                map.put(t.getSystemName(), t);
            }
            typesBySystemName = Collections.unmodifiableMap(map);
        }

        private final String systemName;
        private final String displayName;

        private HighlightingType(@NonNull String systemName, @NonNull String displayName) {
            assert systemName != null;
            assert displayName != null;
            this.systemName = systemName;
            this.displayName = displayName;
        }

        @NonNull
        public String getDisplayName() {
            return this.displayName;
        }

        @NonNull
        String getSystemName() {
            return this.systemName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @NonNull
        static HighlightingType forSystemName(@NullAllowed final String systemName) {
            HighlightingType type = typesBySystemName.get(systemName);
            if (type == null) {
                type = getDefault();
            }
            return type;
        }

        @NonNull
        private static HighlightingType getDefault() {
            return BACKGROUND;
        }
    }

    public enum SortingType {
        LEXICOGRAPHIC("lexicographic", NbBundle.getMessage(GoToSettings.class, "NAME_LEXICOGRAPHIC")),  //NOI18N
        LEVENSHTEIN("levenshtein", NbBundle.getMessage(GoToSettings.class, "NAME_LEVENSHTEIN"));        //NOI18N

        private static final Map<String,SortingType> typesBySystemName;
        static {
            Map<String,SortingType> map = new HashMap<>();
            for (SortingType t : SortingType.values()) {
                map.put(t.getSystemName(), t);
            }
            typesBySystemName = Collections.unmodifiableMap(map);
        }

        private final String systemName;
        private final String displayName;

        private SortingType(@NonNull String systemName, @NonNull String displayName) {
            assert systemName != null;
            assert displayName != null;
            this.systemName = systemName;
            this.displayName = displayName;
        }

        @NonNull
        public String getDisplayName() {
            return this.displayName;
        }

        @NonNull
        String getSystemName() {
            return this.systemName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }

        @NonNull
        static SortingType forSystemName(@NullAllowed final String systemName) {
            SortingType type = typesBySystemName.get(systemName);
            if (type == null) {
                type = getDefault();
            }
            return type;
        }

        @NonNull
        private static SortingType getDefault() {
            return LEXICOGRAPHIC;
        }
    }

    private static final String NODE_HIGHLIGHTING = "highlighting";     //NOI18N
    private static final String NODE_SORTING = "sorting";               //NOI18N
    private static final String KEY_HIGHLIGHTING_MODE = "mode";         //NOI18N
    private static final String KEY_HIGHLIGHTING_TYPE = "type";         //NOI18N
    private static final String KEY_SORTING_TYPE = "type";              //NOI18N
    private static final String KEY_SORTING_PRJ = "prefer-projects";   //NOI18N
    private static final GoToSettings INSTANCE = new GoToSettings();

    private GoToSettings() {
    }

    @NonNull
    public HighlightingMode getHighlightingMode() {
        return HighlightingMode.forSystemName(getHighlightingNode().get(KEY_HIGHLIGHTING_MODE, null));
    }

    void setHighlightingMode(@NonNull final HighlightingMode mode) {
        assert mode != null;
        getHighlightingNode().put(KEY_HIGHLIGHTING_MODE, mode.getSystemName());
    }

    @NonNull
    public HighlightingType getHighlightingType() {
        return HighlightingType.forSystemName(getHighlightingNode().get(KEY_HIGHLIGHTING_TYPE, null));
    }

    void setHighlightingType(@NonNull final HighlightingType type) {
        assert type != null;
        getHighlightingNode().put(KEY_HIGHLIGHTING_TYPE, type.getSystemName());
    }

    @NonNull
    public SortingType getSortingType() {
        return SortingType.forSystemName(getSortingNode().get(KEY_SORTING_TYPE, null));
    }

    void setSortingType(@NonNull final SortingType type) {
        assert type != null;
        getSortingNode().put(KEY_SORTING_TYPE, type.getSystemName());
    }

    public boolean isSortingPreferOpenProjects() {
        return getSortingNode().getBoolean(KEY_SORTING_PRJ, false);
    }

    public void setSortingPreferOpenProjects(final boolean value) {
        getSortingNode().putBoolean(KEY_SORTING_PRJ, value);
    }

    @NonNull
    private Preferences getHighlightingNode() {
        final Preferences prefs = NbPreferences.forModule(GoToSettings.class);
        return prefs.node(NODE_HIGHLIGHTING);
    }

    @NonNull
    private Preferences getSortingNode() {
        final Preferences prefs = NbPreferences.forModule(GoToSettings.class);
        return prefs.node(NODE_SORTING);
    }

    @NonNull
    public static GoToSettings getDefault() {
        return INSTANCE;
    }
}
