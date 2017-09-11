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
