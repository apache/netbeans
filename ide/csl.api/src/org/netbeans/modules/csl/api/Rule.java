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

import java.util.Set;
import java.util.prefs.Preferences;
import javax.swing.JComponent;

/** Represents a rule to be run on the source.
 * Only contains the basic identification and UI properties of the rule. 
 * Instances of the rules can be placed into the system filesystem.
 * 
 * (Copied from java/hints)
 *
 * @author Petr Hrebejk
 */
public interface Rule {

    /**
     * Return true iff this hint applies to the given file
     */
    public boolean appliesTo(RuleContext context);

    /** Get's UI usable name of the rule
     */
    public String getDisplayName();

    /**
     * Whether this task should be shown in the tasklist
     */
    public boolean showInTasklist();

    /** Gets current severiry of the hint.
     * @return Hints severity in current profile.
     */
    public HintSeverity getDefaultSeverity();

    /**
     * A rule which is configurable (enabled, properties, etc) by the user.
     *
     * @author Tor Norbye
     */
    public interface UserConfigurableRule extends Rule {
        /** Gets unique ID of the rule
         */
        public String getId();

        /** Gets longer description of the rule
         */
        public String getDescription();

        /** Finds out whether the rule is currently enabled.
         * @return true if enabled false otherwise.
         */
        public boolean getDefaultEnabled();

        /** Gets the UI description for this rule. It is fine to return null
         * to get the default behavior. Notice that the Preferences node is a copy
         * of the node returned from {link:getPreferences()}. This is in oder to permit 
         * canceling changes done in the options dialog.<BR>
         * Default implementation return null, which results in no customizer.
         * It is fine to return null (as default implementation does)
         *
         * <p>
         * Be sure to set the default values for the options controlled by the
         * customizer into the provided {@link Preferences}. This should be done
         * before returning the customizer. If you do not, the infrastructure
         * will not be able to correctly enable/disable the Apply button in
         * options window.
         * @param node Preferences node the customizer should work on.
         * @return Component which will be shown in the options dialog.
         */
        public JComponent getCustomizer(Preferences node);
    }

    /**
     * Represents a rule to be run on the source file, passing in some
     * compilation context to aid the rule. (Similar to TreeRule for java/hints).
     *
     * @author Tor Norbye
     */
    public interface AstRule extends UserConfigurableRule {

        /** 
         * Get the types of ast nodes that this rule applies to.
         */
        public Set<?> getKinds();
    }

    /** 
     * Represents a rule to be run on the java source in case the compiler 
     * issued an error or a warning.
     *
     * (Copied from java/hints)
     * 
     *
     * @author Petr Hrebejk, Jan Lahoda
     */
    public interface ErrorRule extends Rule {//XXX: should ErrorRule extend UserConfigurableRule?

        /** Get the diagnostic codes this rule should run on
         */
        public Set<?> getCodes();
    }

    /**
     * Represents a rule to be run on text selection
     *
     * @author Tor Norbye
     */
    public interface SelectionRule extends Rule {
    }
}
