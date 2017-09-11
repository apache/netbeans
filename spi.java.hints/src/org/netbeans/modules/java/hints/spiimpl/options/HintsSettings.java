/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.java.hints.spiimpl.options;

import java.util.prefs.Preferences;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences;
import org.netbeans.spi.editor.hints.settings.FileHintPreferences.GlobalHintPreferencesProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.NbPreferences;
import org.openide.util.Parameters;

/**
 *
 * @author Petr Hrebejk
 * @author Jan Lahoda
 */
public abstract class HintsSettings {

    private static final String ENABLED_KEY = "enabled";         // NOI18N
    private static final String OLD_SEVERITY_KEY = "severity";       // NOI18N
    private static final String NEW_SEVERITY_KEY = "hintSeverity";       // NOI18N
//    protected static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N

    public abstract boolean isEnabled(HintMetadata hint);
    public abstract void setEnabled(HintMetadata hint, boolean value);
    public abstract Preferences getHintPreferences(HintMetadata hint);
    public abstract Severity getSeverity(HintMetadata hint);
    public abstract void setSeverity(HintMetadata hint, Severity severity);
//    public abstract Iterable<? extends HintDescription> getEnabledHints();
    
    private static final class PreferencesBasedHintsSettings extends HintsSettings {

        private final Preferences preferences;
        private final boolean useDefaultEnabled;
        private final Severity overrideSeverity;

        public PreferencesBasedHintsSettings(Preferences preferences, boolean useDefaultEnabled, Severity overrideSeverity) {
            Parameters.notNull("preferences", preferences);
            this.preferences = preferences;
            this.useDefaultEnabled = useDefaultEnabled;
            this.overrideSeverity = overrideSeverity;
        }

        @Override
        public boolean isEnabled(HintMetadata hint) {
            return getHintPreferences(hint).getBoolean(ENABLED_KEY, useDefaultEnabled && hint.enabled);
        }

        @Override
        public void setEnabled(HintMetadata hint, boolean value) {
            getHintPreferences(hint).putBoolean(ENABLED_KEY, value);
        }

        @Override
        public Preferences getHintPreferences(HintMetadata hint) {
            return preferences.node(hint.id);
        }

        @Override
        public Severity getSeverity(HintMetadata hint) {
            Preferences prefs = getHintPreferences(hint);
            String s = prefs.get(NEW_SEVERITY_KEY, null);
            if (s != null) return Severity.valueOf(s);

            s = prefs.get(OLD_SEVERITY_KEY, null);

            if (s == null) return overrideSeverity != null ? overrideSeverity : hint != null ? hint.severity : null;

            if ("ERROR".equals(s)) return Severity.ERROR;
            else if ("WARNING".equals(s)) return Severity.VERIFIER;
            else if ("CURRENT_LINE_WARNING".equals(s)) return Severity.HINT;

            return overrideSeverity != null ? overrideSeverity : hint != null ? hint.severity : null;
        }
        
        @Override
        public void setSeverity(HintMetadata hint, Severity severity) {
            getHintPreferences(hint).put(NEW_SEVERITY_KEY, severity.name());
        }
    }
    
    public static HintsSettings createPreferencesBasedHintsSettings(Preferences preferences, boolean useDefaultEnabled, Severity overrideSeverity) {
        return new PreferencesBasedHintsSettings(preferences, useDefaultEnabled, overrideSeverity);
    }
    
    public static HintsSettings getSettingsFor(FileObject file) {
        return createPreferencesBasedHintsSettings(FileHintPreferences.getFilePreferences(file, "text/x-java"), true, null);
    }
    
    public static HintsSettings getGlobalSettings() {
        return GLOBAL_SETTINGS;
    }
    
    private static final String DEFAULT_PROFILE = "default"; // NOI18N
    private static final String PREFERENCES_LOCATION = "org/netbeans/modules/java/hints";
    private static final HintsSettings GLOBAL_SETTINGS = createPreferencesBasedHintsSettings(NbPreferences.root().node(PREFERENCES_LOCATION).node(DEFAULT_PROFILE), true, null);
    
    @MimeRegistration(mimeType="text/x-java", service=GlobalHintPreferencesProvider.class)
    public static class GlobalSettingsProvider implements GlobalHintPreferencesProvider {

        @Override
        public Preferences getGlobalPreferences() {
            return NbPreferences.root().node(PREFERENCES_LOCATION).node(DEFAULT_PROFILE);
        }
        
    }
}
