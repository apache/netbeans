/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * CSS style matching a node.
 *
 * @author Jan Stola
 */
public class MatchedStyles {
    /** CSS rules matching the node, from all applicable stylesheets. */
    private final List<Rule> matchedRules;
    /** Pseudo style rules for the node. */
    // TODO: introduce when needed: private List<PseudoIdRules> pseudoRules;
    /** A chain of inherited styles (from the immediate node parent up to the DOM tree root). */
    private List<InheritedStyleEntry> inheritedRules;

    /**
     * Creates a new {@code MatchedStyles} that corresponds to the given JSONObject.
     *
     * @param styles JSONObject describing the styles.
     */
    MatchedStyles(JSONObject styles) {
        if (styles.containsKey("matchedCSSRules")) { // NOI18N
            JSONArray rules = (JSONArray)styles.get("matchedCSSRules"); // NOI18N
            matchedRules = new ArrayList<Rule>(rules.size());
            for (Object o : rules) {
                Rule rule = new Rule((JSONObject)o);
                matchedRules.add(rule);
            }
            Collections.reverse(matchedRules);
        } else {
            matchedRules = Collections.EMPTY_LIST;
        }
        /* TODO: After it's needed, follow the most recent spec.
        if (styles.containsKey("pseudoElements")) { // NOI18N
            JSONArray rules = (JSONArray)styles.get("pseudoElements"); // NOI18N
            pseudoRules = new ArrayList<PseudoIdRules>(rules.size());
            for (Object o : rules) {
                PseudoIdRules rule = new PseudoIdRules((JSONObject)o);
                pseudoRules.add(rule);
            }
        }*/
        if (styles.containsKey("inherited")) { // NOI18N
            JSONArray rules = (JSONArray)styles.get("inherited"); // NOI18N
            inheritedRules = new ArrayList<InheritedStyleEntry>(rules.size());
            for (Object o : rules) {
                InheritedStyleEntry entry = new InheritedStyleEntry((JSONObject)o);
                inheritedRules.add(entry);
            }
        }
    }

    /**
     * Returns the rules matching the node.
     *
     * @return rules matchinf the node.
     */
    public List<Rule> getMatchedRules() {
        return Collections.unmodifiableList(matchedRules);
    }

    /**
     * Returns pseudo style rules for the node.
     *
     * @return pseudo style rules for the node.
     * TODO: Provide after it's needed, with the updated data structure.
    public List<PseudoIdRules> getPseudoRules() {
        return Collections.unmodifiableList(pseudoRules);
    }*/

    /**
     * Returns the chain of inherited styles.
     *
     * @return chain of inherited styles (from the immediate node parent up to the DOM tree root).
     */
    public List<InheritedStyleEntry> getInheritedRules() {
        return Collections.unmodifiableList(inheritedRules);
    }

}
