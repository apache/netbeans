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

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.text.Collator;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

/**
 * Implements filtering for Add Module Dependency panel.
 * @author Jesse Glick
 */
final class AddModuleFilter {

    private final Set<ModuleDependency> universe;
    private final String dependingModuleCNB;

    /**
     * Construct a filter given a list of possible dependencies.
     */
    public AddModuleFilter(Set<ModuleDependency> universe, String dependingModuleCNB) {
        this.universe = universe;
        this.dependingModuleCNB = dependingModuleCNB;
        // Prime the cache:
        for (ModuleDependency dep : universe) {
            if (Thread.interrupted()) {
                break;
            }
            dep.getFilterTokens(dependingModuleCNB);
        }
        // To test "Please wait" use:
        //try{Thread.sleep(2000);}catch(InterruptedException e){}
    }
    
    /**
     * Find matches for a search string.
     */
    public Set<ModuleDependency> getMatches(Set<ModuleDependency> dependencies, String text, Boolean matchCase) {
        Set<ModuleDependency> dependenciesToFilter;
        if(dependencies != null) {
            dependenciesToFilter = dependencies;
        } else {
            dependenciesToFilter = universe;
        }
        String textLC = matchCase?text:text.toLowerCase(Locale.ENGLISH);
        List<Set<ModuleDependency>> matches = new ArrayList<Set<ModuleDependency>>(3);
        for (int i = 0; i < 3; i++) {
            // Within groups, just sort by module display name:
            matches.add(new TreeSet<ModuleDependency>(ModuleDependency.LOCALIZED_NAME_COMPARATOR));
        }
        for (ModuleDependency dep : dependenciesToFilter) {
            if (Thread.interrupted()) {
                break;
            }
            int matchLevel = 3;
            for (String tok : dep.getFilterTokens(dependingModuleCNB)) {
                String token = matchCase?tok:tok.toLowerCase(Locale.ENGLISH);
                // Presort by relevance (#71995):
                if (token.equals(textLC) || token.endsWith("." + textLC)) { // NOI18N
                    // Exact match (possibly after dot).
                    matchLevel = Math.min(0, matchLevel);
                } else if (token.indexOf("." + textLC) != -1) { // NOI18N
                    // Starts with match (after dot).
                    matchLevel = Math.min(1, matchLevel);
                } else if (token.indexOf(textLC) != -1) {
                    // Substring match.
                    matchLevel = Math.min(2, matchLevel);
                }
            }
            if (matchLevel < 3) {
                matches.get(matchLevel).add(dep);
            }
        }
        Set<ModuleDependency> result = new LinkedHashSet<ModuleDependency>();
        for (Set<ModuleDependency> deps : matches) {
            result.addAll(deps);
        }
        return result;
    }
    
    /**
     * Find which tokens actually matched a given dependency.
     */
    public Set<String> getMatchesFor(String text, ModuleDependency dep) {
        String textLC = text.toLowerCase(Locale.US);
        Set<String> tokens = new TreeSet<String>(Collator.getInstance());
        for (String token : dep.getFilterTokens(dependingModuleCNB)) {
            if (token.toLowerCase(Locale.US).indexOf(textLC) != -1) {
                tokens.add(token);
            }
        }
        return tokens;
    }
    
}
