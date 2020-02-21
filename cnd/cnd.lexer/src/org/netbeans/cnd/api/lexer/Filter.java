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
