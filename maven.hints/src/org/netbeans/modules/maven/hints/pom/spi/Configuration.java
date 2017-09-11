/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.maven.hints.pom.spi;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.NbPreferences;

/**
 *
 * @author mkleint
 */
public final class Configuration {
    private final String id;
    private final String description;
    private final boolean defaultEnabled;
    public static final String ENABLED_KEY = "enabled";         // NOI18N
    public static final String SEVERITY_KEY = "severity";       // NOI18N
    static final String IN_TASK_LIST_KEY = "inTaskList"; // NOI18N
    private final HintSeverity defaultSeverity;
    private final String displayName;
    private final Map<String, Object> id2Saved = new HashMap<String, Object>();

    public Configuration(String id, String displayName, String description, boolean defaultEnabled, HintSeverity defaultSeverity) {
        this.id = id;
        this.description = description;
        this.defaultEnabled = defaultEnabled;
        this.defaultSeverity = defaultSeverity;
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @return Preferences node for given hint.
     */

    public String getId() {
        return id;
    }

    public Preferences getPreferences() {
//        Map<String, Preferences> override = HintsSettings.getPreferencesOverride();
//        if (override != null) {
//            Preferences p = override.get(getId());
//            if (p != null) {
//                return p;
//            }
//        }
        return NbPreferences.forModule(this.getClass()).node(getId()); //NOI18N
    }


    public String getDescription() {
        return description;
    }

    /** Finds out whether the rule is currently enabled.
     * @return true if enabled false otherwise.
     */
    public final boolean isEnabled(Preferences p) {
        boolean enabled = p.getBoolean(ENABLED_KEY, defaultEnabled);
        if(!id2Saved.containsKey(ENABLED_KEY)) {
            id2Saved.put(ENABLED_KEY, enabled);
        }
        return enabled;
    }

    public void setEnabled( Preferences p, boolean value ) {
        p.putBoolean(ENABLED_KEY, value);
    }


    /** Gets current severiry of the hint.
     * @return Hints severity in current profile.
     */
    public final HintSeverity getSeverity(Preferences p) {
        String s = p.get(SEVERITY_KEY, null );
        HintSeverity severity = s == null ? defaultSeverity : fromPreferenceString(s);
        if(!id2Saved.containsKey(SEVERITY_KEY)) {
            id2Saved.put(SEVERITY_KEY, severity);
        }
        return severity;
    }


    public void setSeverity( Preferences p, Configuration.HintSeverity severity ) {
        p.put(SEVERITY_KEY, severity.toPreferenceString());
    }
    
    public Object getSavedValue(String key) {
        return id2Saved.get(key);
    }


    /** Severity of hint
     *  <li><code>ERROR</code>  - will show up as error
     *  <li><code>WARNING</code>  - will show up as warrnig
     */
    public static enum HintSeverity {
        ERROR,
        WARNING;

        public Severity toEditorSeverity() {
            switch ( this ) {
                case ERROR:
                    return Severity.ERROR;
                case WARNING:
                    return Severity.VERIFIER;
                default:
                    return null;
            }
        }

        public String toPreferenceString() {
            switch ( this ) {
                case ERROR:
                    return "error";
                case WARNING:
                    return "warning";
                default:
                    return null;
            }
        }

    }

    public static HintSeverity fromPreferenceString(String sev) {
        if (sev.equals("error")) {
            return HintSeverity.ERROR;
        }
        if (sev.equals("warning")) {
            return HintSeverity.WARNING;
        }
        throw new IllegalStateException(sev);

    }

}
