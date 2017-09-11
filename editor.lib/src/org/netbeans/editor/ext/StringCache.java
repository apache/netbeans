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

package org.netbeans.editor.ext;

import org.netbeans.editor.StringMap;

/**
* Cache holding the most commonly used strings.
* The unused strings are discarded when they reach the end of chain.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class StringCache {

    private static final int DEFAULT_MAX_SIZE = 300;

    private static final int DEFAULT_INITIAL_CAPACITY = 701;

    int maxSize;

    int size;

    StringMap strMap;

    /** First chain member */
    private Entry chain;

    /** Last chain member */
    private Entry endChain;

    /** Last entry that was made free */
    private Entry freeEntry;

    public int statQueries; // count of queries
    public int statHits; // count of cache hits

    public StringCache() {
        this(DEFAULT_MAX_SIZE, DEFAULT_INITIAL_CAPACITY);
    }

    public StringCache(int maxSize) {
        this(maxSize, 2 * maxSize);
    }

    public StringCache(int maxSize, int initialMapCapacity) {
        this.maxSize = maxSize;
        strMap = new StringMap(initialMapCapacity);
    }

    private void toStart(Entry e) {
        if (e != chain) {
            // chain removal
            Entry ep = e.prev; // ep surely not null
            Entry en = e.next;
            if (en != null) {
                en.prev = ep;
            } else { // last chain member
                endChain = ep;
            }
            ep.next = en;

            // insert to chain start
            if (chain != null) {
                e.next = chain;
                chain.prev = e;
            }
            chain = e;
        }
    }

    public String getString(char[] chars, int offset, int len) {
        statQueries++;
        Object o = strMap.get(chars, offset, len);
        String ret;
        if (o instanceof Entry) {
            Entry e = (Entry)o;
            toStart(e);
            statHits++;
            ret = e.str;
        } else if (o instanceof String) {
            statHits++;
            ret = (String)o;
        } else { // string not found in cache
            ret = new String(chars, offset, len);
            storeString(ret);
        }
        return ret;
    }

    /** Remove string that can be in the cache */
    private void removeString(String s) {
        Object o = strMap.remove(s);
        if (o instanceof Entry) {
            Entry e = (Entry)o;
            Entry ep = e.prev;
            Entry en = e.next;

            if (e == chain) {
                chain = en;
                if (e == endChain) {
                    endChain = null;
                }
            } else { // not begining of chain
                if (en != null) {
                    en.prev = ep;
                } else {
                    endChain = ep;
                }
            }

            freeEntry = e; // free - can be reused for addition
            size--;
        }
        /* In other cases the removed object was either
        * the string which should be fine here
        * or it was null.
        */
    }

    /** Store string that's not yet in the cache */
    private void storeString(String s) {
        Entry e;
        if (size >= maxSize) {
            // take last one and move to begining and replace value
            e = endChain;
            toStart(e);
            strMap.remove(e.str);
            e.str = s;
        } else { // count of entries less than max
            if (freeEntry != null) {
                e = freeEntry;
                freeEntry = null;
                e.str = s;
                e.next = chain;
            } else {
                e = new Entry(s, chain);
            }

            if (chain != null) {
                chain.prev = e;
            } else { // nothing inserted yet
                endChain = e;
            }
            chain = e;
            size++;
        }
        strMap.put(s, e);
    }

    /** Put a string into cache that will survive there
    * so that it will be never removed.
    */
    public void putSurviveString(String s) {
        removeString(s);
        strMap.put(s, s);
    }

    static class Entry {

        Entry(String str, Entry next) { // prev always null
            this.str = str;
            this.next = next;
        }

        String str;

        Entry next;

        Entry prev;

    }

    public String toString() {
        String ret = "size=" + size + ", maxSize=" + maxSize // NOI18N
                     + ", statHits=" + statHits + ", statQueries=" + statQueries; // NOI18N
        if (statQueries > 0) {
            ret += ", hit ratio=" + (statHits * 100 / statQueries) + "%"; // NOI18N
        }
        return ret;
    }

}
