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
