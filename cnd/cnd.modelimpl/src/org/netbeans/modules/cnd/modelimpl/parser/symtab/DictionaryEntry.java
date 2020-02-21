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

/**
 * based on DictionaryEntry removed from cppparser.g
 *
 */
public class DictionaryEntry {

    static final ObjectType UNSPECIFIED_TYPE = new ObjectType() {

        @Override
        public String toString() {
            return "UNSPECIFIED_TYPE"; // NOI18N
        }
    };

    void setHashCode(int h) {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }

    void setKey(CharSequence strdup) {
        throw new UnsupportedOperationException("Not yet implemented"); // NOI18N
    }

    interface ObjectType {
    }
    private final CharSequence key;
    private final int hashCode;
    private DictionaryEntry next;		// next element in the bucket
    private DictionaryEntry scope;		// next element in the scope
    int this_scope;			// scope index

    DictionaryEntry(CharSequence k, int bucketIndex, int scopeIndex) {
        key = k;
        hashCode = bucketIndex;
        next = scope = null;
        this_scope = scopeIndex;
    }

    CharSequence getKey() {
        return key;
    }

    int getHashCode() {
        return hashCode;
    }

    void setNext(DictionaryEntry n) {
        next = n;
    }

    DictionaryEntry getNext() {
        return next;
    }

    void setScope(DictionaryEntry s) {
        scope = s;
    }

    DictionaryEntry getNextInScope() {
        return scope;
    }

    ObjectType getType() {
        return UNSPECIFIED_TYPE;
    }

    boolean isTypeOf(ObjectType type) {
        return false;
    }
}
