/**
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
public final class HighlightingSettings {

    public enum Mode {
        NONE("none", NbBundle.getMessage(HighlightingSettings.class, "NAME_NONE")),
        ACTIVE("active", NbBundle.getMessage(HighlightingSettings.class, "NAME_ACTIVE")),
        ALL("all", NbBundle.getMessage(HighlightingSettings.class, "NAME_ALL"));

        private static final Map<String, Mode> modesBySystemName;
        static {
            final Map<String,Mode> map = new HashMap<>();
            for (Mode m : Mode.values()) {
                map.put(m.getSystemName(), m);
            }
            modesBySystemName = Collections.unmodifiableMap(map);
        }

        private final String systemName;
        private final String displayName;

        private Mode(@NonNull String systemName, @NonNull String displayName) {
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
        static Mode forSystemName(@NullAllowed final String sysName) {
            Mode m = modesBySystemName.get(sysName);
            if (m == null) {
                m = getDefault();
            }
            return m;
        }

        @NonNull
        private static Mode getDefault() {
            return ACTIVE;
        }
    }

    public enum Type {
        BOLD("bold", NbBundle.getMessage(HighlightingSettings.class, "NAME_BOLD")),
        BACKGROUND("background", NbBundle.getMessage(HighlightingSettings.class, "NAME_BACKGROUND"));

        private static final Map<String,Type> typesBySystemName;
        static {
            Map<String,Type> map = new HashMap<>();
            for (Type t : Type.values()) {
                map.put(t.getSystemName(), t);
            }
            typesBySystemName = Collections.unmodifiableMap(map);
        }

        private final String systemName;
        private final String displayName;

        private Type(@NonNull String systemName, @NonNull String displayName) {
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
        static Type forSystemName(@NullAllowed final String systemName) {
            Type type = typesBySystemName.get(systemName);
            if (type == null) {
                type = getDefault();
            }
            return type;
        }

        @NonNull
        private static Type getDefault() {
            return BACKGROUND;
        }
    }

    private static final String NODE_NAME = "highlighting";  //NOI18N
    private static final String KEY_MODE = "mode";      //NOI18N
    private static final String KEY_TYPE = "type";      //NOI18N
    private static final HighlightingSettings INSTANCE = new HighlightingSettings();

    private HighlightingSettings() {
    }

    @NonNull
    public Mode getMode() {
        return Mode.forSystemName(getNode().get(KEY_MODE, null));
    }

    void setMode(@NonNull final Mode mode) {
        assert mode != null;
        getNode().put(KEY_MODE, mode.getSystemName());
    }

    @NonNull
    public Type getType() {
        return Type.forSystemName(getNode().get(KEY_TYPE, null));
    }

    void setType(@NonNull final Type type) {
        assert type != null;
        getNode().put(KEY_TYPE, type.getSystemName());
    }

    @NonNull
    private Preferences getNode() {
        final Preferences prefs = NbPreferences.forModule(HighlightingSettings.class);
        return prefs.node(NODE_NAME);
    }

    @NonNull
    public static HighlightingSettings getDefault() {
        return INSTANCE;
    }
}
