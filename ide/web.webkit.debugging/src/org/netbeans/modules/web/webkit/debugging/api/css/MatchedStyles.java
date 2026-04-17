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
    /* Pseudo style rules for the node. */
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
            matchedRules = Collections.emptyList();
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

    /*
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
