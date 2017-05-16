/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
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
    private static final String KEY_SORTING_PRJ = "preffer-projects";   //NOI18N
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
        return getSortingNode().getBoolean(KEY_SORTING_PRJ, true);
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
