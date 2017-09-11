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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
