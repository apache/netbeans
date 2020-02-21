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
package org.netbeans.modules.cnd.modelimpl.parser.symtab;

import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.openide.util.Exceptions;

/**
 * based on Dictionary removed from cppparser.g
 *
 */
public class Dictionary {

    protected Dictionary(int nrBuckets, int nrScopes, int nrCharacters) {

        init(nrBuckets, nrScopes);
    }

    int hash(CharSequence s) {
        return s.hashCode();
    }

    int getBucketIndex(CharSequence key) {
        return hash(key) % nbuckets;
    }

    DictionaryEntry lookup(CharSequence key) {
        return lookup(key, null);
    }

    /*
     * Return ptr to 1st entry found in table under key (return null if none
     * found).
     */
    DictionaryEntry lookup(CharSequence key, DictionaryEntry.ObjectType tp) {
        //printf("Dictionary.cpp lookup entered with %s for type %d\n",key,tp);
        // tp is type to be looked for
        // Default, 0, is to look for any symbol irrespective of type
        // Each new DictionaryEntry is stored at start of its list so most
        // recent additions are reached first
        //int scope = getCurrentScopeIndex();
        //if(sc != -1)
        //		scope = sc;

        int h = getBucketIndex(key);

        // This searches symbol bucket for CPPSymbol entries
        for (DictionaryEntry q = bucket[h]; q != null; q = q.getNext()) {
            //printf("Dictionary.cpp lookup symbol %s in scope %d current scope %d\n",
            //	q.getKey(),q.this_scope,getCurrentScopeIndex());

            if (h != q.getHashCode()) {
                System.err.printf("dictionary.cpp lookup, h not equal to q.getHashCode() for %s%n", key);
            }

            if (h == q.getHashCode() && strcmp(key, q.getKey())) {
                // Search for first matching symbol
                if (tp == DictionaryEntry.UNSPECIFIED_TYPE) {
                    return q;
                } else {
                    // Search for first type name
                    if (q.isTypeOf(tp)) {
                        return q;
                    } else {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    void define(CharSequence key, DictionaryEntry value) {
        defineInScope(key, value, currentScope);
    }

    void defineInScope(CharSequence key, DictionaryEntry entry, int sc) {
        int h = getBucketIndex(key);
        //printf("Dictionary.cpp defineInScope key %s hash(key) %d bucket %d scope %d\n",
        //	key,hash(key),h,sc);
        entry.this_scope = sc;
        entry.setKey(strdup(key));	// Make a local copy of key
        entry.setHashCode(h);
        entry.setNext(bucket[h]);	// Set next pointer to current entry in bucket
        bucket[h] = entry;			// Replace current entry in bucket
        if (endScope[sc] == null) {
            scope[sc] = endScope[sc] = entry;
        } else {
            endScope[sc].setScope(entry);
            endScope[sc] = entry;
        }
    }

    void saveScope() {
        // Advance scope number (for included scope)
        currentScope++;
        if (currentScope >= nscopes) {
            panic("saveScope: overflow"); // NOI18N
        }
        //printf("Dictionary saveScope entered. Scope now %d\n",currentScope);
    }

    void restoreScope() {
        // Reduce scope number for next highest scope
        if (currentScope == 0) {
            panic("restoreScope: underflow"); // NOI18N
        }
        currentScope--;
        //printf("Dictionary restoreScope entered. Scope now %d\n",currentScope);
    }

    DictionaryEntry getCurrentScope() {
        if (currentScope < 0 || currentScope > nscopes) {
            panic("getCurrentScope: no scope"); // NOI18N
        }
        return scope[currentScope];
    }

    int getCurrentScopeIndex() {
        return currentScope;
    }

    DictionaryEntry removeScope() {
        return removeScope(-1);
    }

    /*
     * This unlinks all entries from the Dictionary that are members of the
     * current scope. The scope level is not restored to the previous scope
     * however. This requires use of restoreScope() above
     */
    DictionaryEntry removeScope(int sc) {
        DictionaryEntry de, r;
        if (sc == -1) // removeScope() without parameter value defaults sc to -1
        {
            sc = currentScope;
        }
        for (de = scope[sc]; de != null; de = de.getNextInScope()) {
            remove(de);
        }
        r = scope[sc];
        scope[sc] = endScope[sc] = null;
        return r;
    }

    /*
     * Lookup the object referred to by 'key' and then physically remove it from
     * the Dictionary. Return the object referred to by the key. If more than
     * one definition is found for 'key', then only the first one is removed.
     * Return null if not found.
     */
    DictionaryEntry remove(CharSequence key) {
        DictionaryEntry q, prev;

        int h = getBucketIndex(key);
        for (prev = null, q = bucket[h]; q != null; prev = q, q = q.getNext()) {
            if (h == q.getHashCode() && strcmp(key, q.getKey())) {
                if (prev == null) {
                    bucket[h] = q.getNext();
                } else {
                    prev.setNext(q.getNext());
                }
                q.setNext(null);
                return q;
            }
        }
        assert false;
        return null;      // should never get here, but make compiler happy
    }

    //	Remove this dictEntry from its bucket by unlinking it
    DictionaryEntry remove(DictionaryEntry de) {
        DictionaryEntry prev, curr;
        if (de == null) {
            panic("Dictionary remove: null ptr"); // NOI18N
        }
        int h = getBucketIndex(de.getKey());	// Find pointer to bucket
        for (prev = null, curr = bucket[h]; curr != null; prev = curr, curr = curr.getNext()) {
            if (de == curr) {
                if (prev == null) {
                    bucket[h] = de.getNext();
                } else {
                    prev.setNext(de.getNext());
                }
                de.setNext(null);
                return de;
            }
        }
        return null;	// should never get here...
    }

    private boolean strcmp(CharSequence s1, CharSequence s2) {
        return s1.equals(s2);
    }

    private CharSequence strdup(CharSequence key) {
        return NameCache.getManager().getString(key);
    }

    void dumpScope(Appendable stream, int sc) {
        DictionaryEntry s;

        if (sc == -1) // dumpScope() without parameter value defaults sc to -1
        {
            sc = currentScope;
        }
        for (s = scope[sc]; s != null; s = s.getNextInScope()) {
            dumpSymbol(stream, s);
        }
    }

    // Diagnostic function
    // Contents of first 10 scopes printed
    void dumpScopes() {
        DictionaryEntry dictEntry;
        int i;

        printf("Scopes"); // NOI18N
        for (i = 0; i < 10; i++) {
            printf("     %d     ", i); // NOI18N
        }
        printf("\n"); // NOI18N
        printf(" first"); // NOI18N
        for (i = 0; i < 10; i++) {
            if (scope[i] != null) {
                dictEntry = scope[i];
                printf("%10s ", dictEntry.getKey()); // NOI18N
            } else {
                printf("           "); // NOI18N
            }
        }
        printf("\n"); // NOI18N
        printf(" last "); // NOI18N
        for (i = 0; i < 10; i++) {
            if (endScope[i] != null) {
                dictEntry = endScope[i];
                printf("%10s ", dictEntry.getKey()); // NOI18N
            } else {
                printf("           "); // NOI18N
            }
        }
        printf("\n"); // NOI18N
    }

    void dumpSymbol(Appendable stream, DictionaryEntry e) {
        try {
            stream.append(e.getKey()).append("\n"); // NOI18N
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    void printf(CharSequence msg, Object... args) {
        System.err.printf(msg.toString(), args);
    }

    final void panic(CharSequence msg) {
        throw new IllegalStateException(msg == null ? "" : msg.toString());
    }

    private void init(int nrBuckets, int nrScopes) {
        int i;

        // allocate buckets for names
        bucket = new DictionaryEntry[nrBuckets];
        if (bucket == null) {
            panic("can't alloc buckets"); // NOI18N
        }

        // Initialize buckets for names
        nbuckets = nrBuckets;
        for (i = 0; i < nrBuckets; i++) {
            bucket[i] = null;
        }

        // allocate a scope list for the start of each scope list
        scope = new DictionaryEntry[nrScopes];
        if (scope == null) {
            panic("can't alloc scopes"); // NOI18N
        }

        // allocate an endScope list for the end of each scope list
        endScope = new DictionaryEntry[nrScopes];
        if (endScope == null) {
            panic("can't alloc endScope"); // NOI18N
        }

        // Initialize scopes and endscopes
        nscopes = nrScopes;
        for (i = 0; i < nrScopes; i++) {
            scope[i] = null;
            endScope[i] = null;
        }

        currentScope = 0;
//
//	strsize = nrCharacters;
//	strings = new char[nrCharacters];
//	strp = strings;
    }

    private DictionaryEntry[] scope, endScope;
    private int nscopes, currentScope;
    private DictionaryEntry[] bucket;
    private int nbuckets;
//	private static unsigned char randomNumbers[];
//	private static CharSequence strings;
//	private static CharSequence strp;
//	private static unsigned strsize;
}
