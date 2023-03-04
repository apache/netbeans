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
package org.netbeans.modules.java.hints.spi;

import java.util.prefs.Preferences;
import javax.swing.JComponent;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager.APIAccessor;
import org.netbeans.modules.java.hints.legacy.spi.RulesManager.LegacyHintConfiguration;
import org.netbeans.modules.java.hints.providers.spi.HintMetadata;
import org.netbeans.modules.java.hints.spiimpl.options.HintsSettings;
import org.netbeans.spi.editor.hints.Severity;
import org.netbeans.spi.java.hints.Hint;

/** Class to be extended by all the Java hints.
 *
 * @author Petr Hrebejk
 * @deprecated Use {@link Hint} instead.
 */
@Deprecated
public abstract class AbstractHint implements TreeRule {
    
    private boolean enableDefault;
    private boolean showInTaskListDefault;
    private HintSeverity severityDefault;
    private String suppressBy[];        
    
    static {
        RulesManager.ACCESSOR = new HintAccessorImpl();
    }
    
    public AbstractHint(  boolean enableDefault, boolean showInTaskListDefault,HintSeverity severityDefault, String... suppressBy) {
        this.enableDefault = enableDefault;
        this.showInTaskListDefault = showInTaskListDefault;
        this.severityDefault = severityDefault != null ? severityDefault : HintSeverity.WARNING;
        this.suppressBy = suppressBy;
    }
    
    
    /** Gets preferences node, which stores the options for given hint. It is not
     * necessary to override this method unless you want to create some special
     * behavior. The default implementation will create the the preferences node
     * by calling <code>NbPreferences.forModule(this.getClass()).node(profile).node(getId());</code>
     * @profile Profile to get the node for. May be null for current profile
     * @return Preferences node for given hint.
     */
    public Preferences getPreferences( String profile ) {
        LegacyHintConfiguration conf = RulesManager.currentHintPreferences.get();
        
        if (conf != null) return conf.preferences;
        
        //TODO: better fallback?
        return HintsSettings.getGlobalSettings().getHintPreferences(HintMetadata.Builder.create(getId()).build());
    }
        
    /** Gets the UI description for this rule. It is fine to return null
     * to get the default behavior. Notice that the Preferences node is a copy
     * of the node returned frok {link:getPreferences()}. This is in oder to permit 
     * canceling changes done in the options dialog.<BR>
     * Default implementation return null, which results in no customizer.
     * It is fine to return null (as default implementation does)
     * 
     * <p>Be sure to set the default values for the options controlled by the customizer
     * into the provided {@link Preferences}. This should be done before returning the customizer. 
     * If you do not, the infrastructure will not be able to correctly enable/disable the Apply button in options window.
     * @param node Preferences node the customizer should work on.
     * @return Component which will be shown in the options dialog.
     */    
    public JComponent getCustomizer( Preferences node ) {
        return null;
    }
    
    public abstract String getDescription();
    
    /** Finds out whether the rule is currently enabled.
     * @return true if enabled false otherwise.
     */
    public final boolean isEnabled() {
        LegacyHintConfiguration conf = RulesManager.currentHintPreferences.get();
        
        if (conf != null) return conf.enabled;
        
        return enableDefault;
    }
    
    /** Gets current severity of the hint.
     * @return Hints severity in current profile.
     */
    public final HintSeverity getSeverity() {
        LegacyHintConfiguration conf = RulesManager.currentHintPreferences.get();
        
        if (conf != null && conf.severity != null) return HintSeverity.fromOfficialHintSeverity(conf.severity, severityDefault);
        
        return severityDefault;
    }
    
    /** Severity of hint
     *  <li><code>ERROR</code>  - will show up as error
     *  <li><code>WARNING</code>  - will show up as warrnig
     *  <li><code>CURRENT_LINE_WARNING</code>  - will only show up when the caret is placed in the errorneous element
     */
    public static enum HintSeverity {
        ERROR(Severity.ERROR),
        WARNING(Severity.VERIFIER),
        CURRENT_LINE_WARNING(Severity.HINT);

        private final Severity editorSeverity;

        private HintSeverity(Severity editorSeverity) {
            this.editorSeverity = editorSeverity;
        }

        public Severity toEditorSeverity() {
            return editorSeverity;
        }

        private static HintSeverity fromOfficialHintSeverity(Severity official, HintSeverity def) {
            if (official == null) return def;

            switch (official) {
                case ERROR: return HintSeverity.ERROR;
                case HINT: return HintSeverity.CURRENT_LINE_WARNING;
                default:
                case VERIFIER: return HintSeverity.WARNING;
            }
        }

        public Severity toOfficialSeverity() {
            return editorSeverity;
        }
    }

    // Private section ---------------------------------------------------------
    
    private static class HintAccessorImpl implements APIAccessor {

        public boolean isEnabledDefault(AbstractHint hint) {
            return hint.enableDefault;
        }

        public boolean isShowInTaskListDefault(AbstractHint hint) {            
            return hint.showInTaskListDefault;
        }

        public HintSeverity severiryDefault(AbstractHint hint) {
            return hint.severityDefault;
        }
        
        public String[] getSuppressBy(AbstractHint hint) {
            return hint.suppressBy;
        }
        
    }
}
