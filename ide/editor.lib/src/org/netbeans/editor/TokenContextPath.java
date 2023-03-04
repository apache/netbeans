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

package org.netbeans.editor;

import java.util.HashMap;

/**
* Immutable and 'interned' wrapper holding
* an array of the contexts starting
* with the original context in which the token is defined
* and ending with the target context from which the token
* is being returned.
* It is final and has no public constructor.
* The only entrypoint is through the <tt>get()</tt> method.
* It's guaranteed that the two context-paths containing
* the same contexts in the same order are the same objects
* and the equal-operator can be used instead of calling <tt>equals()</tt>.
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class TokenContextPath {

    /** Cache containing [ArrayMatcher, TokenContextPath] pairs. */
    private static final HashMap registry = new HashMap(199);

    /** Contexts contained in this context-path. */
    private final TokenContext[] contexts;

    /** Path for context-array without the last member. */
    private TokenContextPath parent;

    /** Path corresponding to the first context in the context-array. */
    private TokenContextPath base;

    /** Full name-prefix consisting of the prefixes
    * of all the contexts in the path. 
    */
    private String namePrefix;

    /** Map holding [token-name, prefixed-token-name] pairs. */
    private final HashMap tokenNameCache = new HashMap();

    /** Map holding [start-path-replacement, replaced-path] */
    private final HashMap replaceStartCache = new HashMap();

    /** Get the context-path for non-empty array of the contexts. */
    static synchronized TokenContextPath get(TokenContext[] contexts) {
        if (contexts == null || contexts.length == 0) {
            throw new IllegalArgumentException("Contexts must be valid and non-empty."); // NOI18N
        }

        ArrayMatcher am = new ArrayMatcher(contexts);
        TokenContextPath path = (TokenContextPath)registry.get(am);
        if (path == null) {
            path = new TokenContextPath(contexts);
            registry.put(am, path);
        }

        return path;
    }

    /** Construction from outside prohibited. */
    private TokenContextPath(TokenContext[] contexts) {
        this.contexts  = contexts;

        if (contexts.length == 1) {
            base = this; // it's base for itself
        }
    }

    /** Retrieve the contexts of this context-path. The contexts
    * of the context-array must NOT be modified in any way.
    */
    public TokenContext[] getContexts() {
        return contexts;
    }

    /** Get the length of the path returning the length of the contexts array. */
    public int length() {
        return contexts.length;
    }

    /** Get parent context-path that consists of all the contexts
    * of this path except the last one.
    */
    public TokenContextPath getParent() {
        if (parent == null && contexts.length > 1) {
            TokenContext[] parentContexts = new TokenContext[contexts.length - 1];
            System.arraycopy(contexts, 0, parentContexts, 0, contexts.length - 1);

            parent = get(parentContexts);
        }

        return parent;
    }

    /** Get the base path which corresponds to only the first context
    * in the context-array. The base path can be used for fast checking
    * of the origin path of the token.
    */
    public TokenContextPath getBase() {
        if (base == null) {
            base = getParent().getBase();
        }

        return base;
    }

    /** Does this path contain the given path. It corresponds
    * to the situation when the contexts of the given path
    * are at the begining of this path.
    */
    public boolean contains(TokenContextPath tcp) {
        if (tcp == this) {
            return true;

        } else if (contexts.length > 1) {
            return getParent().contains(tcp);

        } else {
            return false;
        }
    }

    /** Get the path which has the initial part of the path
     * (usually only the base path) replaced by the given path. The length
     * of the replaced part of the path is the same as the length
     * of the path that will replace it.
     * For better performance the method caches the [byPath, result-path]
     * in hashmap.
     * @param byPath path that will replace the initial portion
     *  of this path. The length of the portion is the same as the length
     *  of this parameter.
     * @return the path with the initial part being replaced.
     */
    public TokenContextPath replaceStart(TokenContextPath byPath) { 
        // Check whether byPath isn't longer than this path
        if (contexts.length < byPath.contexts.length) {
            throw new IllegalArgumentException("byPath=" + byPath + " is too long."); // NOI18N
        }

        synchronized (replaceStartCache) {
            TokenContextPath ret = (TokenContextPath)replaceStartCache.get(byPath);
            if (ret == null) {
                TokenContext[] targetContexts = (TokenContext[])contexts.clone();
                for (int i = byPath.contexts.length - 1; i >= 0; i--) {
                    targetContexts[i] = byPath.contexts[i];
                }
                ret = get(targetContexts);
                replaceStartCache.put(byPath, ret);
            }

            return ret;
        }
    }

    /** Get the prefix that this context adds to the name of its tokens. */
    public String getNamePrefix() {
        if (namePrefix == null) {
            if (contexts.length == 1) {
                namePrefix = contexts[0].getNamePrefix();

            } else { // path has more contexts
                namePrefix = (contexts[contexts.length - 1].getNamePrefix()
                              + getParent().getNamePrefix()).intern();
            }
        }

        return namePrefix;
    }

    /** Get the token-name with the name-prefix of this context-path.
    * It merges the token-name with the name-prefix of this context-path
    * but it does it without creating a new object.
    */
    public String getFullTokenName(TokenCategory tokenIDOrCategory) {
        String tokenName = tokenIDOrCategory.getName();
        String fullName;
        synchronized (tokenNameCache) {
            fullName = (String)tokenNameCache.get(tokenName);
            if (fullName == null) {
                fullName = (getNamePrefix() + tokenName).intern();
                tokenNameCache.put(tokenName, fullName);
            }
        }

        return fullName;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("|"); // NOI18N
        for (int i = 0; i < contexts.length; i++) {
            String shortName = contexts[i].getClass().getName();
            shortName = shortName.substring(shortName.lastIndexOf('.') + 1);

            sb.append('<');
            sb.append(shortName);
            sb.append('>');
        }
        sb.append('|');

        return sb.toString();
    }

    private static final class ArrayMatcher {

        /** Cached hash-code */
        private int hash;

        private TokenContext[] contexts;

        ArrayMatcher(TokenContext[] contexts) {
            this.contexts = contexts;
        }

        public int hashCode() {
            int h = hash;
            if (h == 0) {
                for (int i = contexts.length - 1; i >= 0; i--) {
                    h = h * 31 + contexts[i].hashCode(); // !!!
                }
                hash = h;
            }

            return h;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o instanceof ArrayMatcher) {
                ArrayMatcher am = (ArrayMatcher)o;
                if (contexts.length == am.contexts.length) {
                    for (int i = contexts.length - 1; i >= 0; i--) {
                        if (!contexts[i].equals(am.contexts[i])) {
                            return false;
                        }
                    }
                    return true;
                }
            }

            return false;
        }

    }

}
