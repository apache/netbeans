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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.cnd.api.lexer;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.lexer.TokenId;

/**
 * help class to match known text with existing token ids.
 * Can be used to match identifiers which are keywords
 * @param <T> token ID type
 */
public final class Filter<T extends TokenId> {

    private final Map<CharSequence, T> filter = new HashMap<CharSequence, T>();
    private final Map<CharSequence, T> prefixFilter = new HashMap<CharSequence, T>();
    private final String name;
    /*package*/ Filter(String name) {
        this.name = name;
    }
    
    /**
     * process specific text and return correspondent token-id if match. 
     * Provided text doesn't have escaped LFs, so text can be matched using maps
     * @param text text without escaped LFs
     * @return TokenID or null if text does not match to any known id
     */
    public final T check(CharSequence text) {
        T out = filter.get(text);
        if (out == null && !prefixFilter.isEmpty()) {
            int bestPrefixLen = 0;
            for (Map.Entry<CharSequence, T> entry : prefixFilter.entrySet()) {
                CharSequence prefix = entry.getKey();
                int length = prefix.length();
                if (bestPrefixLen < length && length <= text.length()) {
                    bestPrefixLen = length;
                    for (int i = 0; i < length; i++) {
                        if (prefix.charAt(i) != text.charAt(i)) {
                            return null;
                        }
                    }
                    out = entry.getValue();
                }
            }
        }
        return out;
    }
    
    /**
     * allow text starting with prefix to be treated as token 
     */
    /*package*/ final void addPrefixedMatch(CharSequence prefix, T id) {
        assert prefix.length() > 0;
        prefixFilter.put(prefix, id);
    }
    
    /**
     * add text to be filtered as id
     */  
    /*package*/ final void addMatch(CharSequence text, T id) {
        filter.put(text, id);
    }    

    @Override
    public String toString() {
        return name + " with " + filter.size() + " keywords" + (prefixFilter.isEmpty() ? "" : (" and matching " + prefixFilter.size() + " prefixes")); // NOI18N
    }
    
    public static <T extends TokenId> Filter<T> create(String filterName, Map<CharSequence, T> matchTable) {
        Filter<T> out = new Filter<T>(filterName);
        for (Map.Entry<CharSequence, T> entry : matchTable.entrySet()) {
            out.addMatch(entry.getKey(), entry.getValue());
        }
        return out;
    }
}
