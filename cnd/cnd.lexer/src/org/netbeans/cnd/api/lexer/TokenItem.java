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

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.openide.util.CharSequences;

/**
 * Token-item presents a token as a piece information
 * without dependence on a character buffer and it enables
 * to chain the token-items in both directions.
 * 
 */
public interface TokenItem<T extends TokenId> {
    /** Get the token-id of this token-item */
    public T id();

    /** Get the position of the token in the document */
    public int offset();

    /** Get the image of this token. */
    public CharSequence text();

    /** Get the index in token stream */
    public int index();

    /** Get the lenfth of token*/
    public int length();

    public PartType partType();

//    /** Get next token-item in the text. It returns null
//     * if there's no more next tokens in the text. It can throw
//     * <tt>IllegalStateException</tt> in case the document
//     * was changed so the token-item chain becomes invalid.
//     */
//    public TokenItem getNext();
//
//    /** Get previous token-item in the text. It returns null
//     * if there's no more previous tokens in the text. It can throw
//     * <tt>IllegalStateException</tt> in case the document
//     * was changed so the token-item chain becomes invalid.
//     */
//    public TokenItem getPrevious();

    /** Abstract implementation that doesn't contain chaining methods. */
    public static abstract class AbstractItem<T extends TokenId> implements TokenItem<T> {

        private final T id;
        private final CharSequence text;
        private final int offset;
        private final PartType partType;
        
        public AbstractItem(T tokenID, PartType partType, int offset, CharSequence image) {
            this.id = tokenID;
            this.offset = offset;
            this.text = CharSequences.create(image);
            this.partType = partType;
        }

        @Override
        public String toString() {
            return "'" + text // NOI18N
                    + "', id=" + id() // NOI18N
                    + "', index=" + index() // NOI18N
                    + ", offset=" + offset(); // NOI18N
        }

        public T id() {
            return id;
        }

        public int offset() {
            return offset;
        }

        public CharSequence text() {
            return text;
        }

        public int index() {
            return -1;
        }

        public int length() {
            return text.length();
        }

        public PartType partType() {
            return partType;
        }

    }
}
