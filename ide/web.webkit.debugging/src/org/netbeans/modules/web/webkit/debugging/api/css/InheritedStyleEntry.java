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
package org.netbeans.modules.web.webkit.debugging.api.css;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Collection of CSS rules matching an ancestor node in the style inheritance chain.
 *
 * @author Jan Stola
 */
public class InheritedStyleEntry {
    /** Ancestor node's inline style, if any. */
    private Style inlineStyle;
    /** Rules matching the ancestor node in the style inheritance chain. */
    private final List<Rule> matchedRules;

    /**
     * Creates a new {@code InheritedStyleEntry} that corresponds to the given JSONObject.
     *
     * @param entry JSONObject describing the inherited style.
     */
    InheritedStyleEntry(JSONObject entry) {
        if (entry.containsKey("inlineStyle")) { // NOI18N
            inlineStyle = new Style((JSONObject)entry.get("inlineStyle")); // NOI18N
        }
        JSONArray rules = (JSONArray)entry.get("matchedCSSRules"); // NOI18N
        matchedRules = new ArrayList<Rule>(rules.size());
        for (Object o : rules) {
            JSONObject rule  = (JSONObject)o;
            matchedRules.add(new Rule(rule));
        }
        Collections.reverse(matchedRules);
    }

    /**
     * Returns ancestor node's inline style.
     * 
     * @return ancestor node's inline style or {@code null} if there
     * is no inline style specified for the ancestor node.
     */
    public Style getInlineStyle() {
        return inlineStyle;
    }

    /**
     * Returns the rules matching the ancestor node.
     * 
     * @return rules matching the ancestor node.
     */
    public List<Rule> getMatchedRules() {
        return Collections.unmodifiableList(matchedRules);
    }

}
