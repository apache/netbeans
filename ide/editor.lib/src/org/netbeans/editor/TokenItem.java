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

/**
* Token-item presents a token as a piece information
* without dependence on a character buffer and it enables
* to chain the token-items in both directions.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface TokenItem {

    /** Get the token-id of this token-item */
    public TokenID getTokenID();

    /** Get the token-id of this token-item */
    public TokenContextPath getTokenContextPath();

    /** Get the position of the token in the document */
    public int getOffset();

    /** Get the image of this token. */
    public String getImage();

    /** Get next token-item in the text. It returns null
    * if there's no more next tokens in the text. It can throw
    * <tt>IllegalStateException</tt> in case the document
    * was changed so the token-item chain becomes invalid.
    */
    public TokenItem getNext();

    /** Get previous token-item in the text. It returns null
    * if there's no more previous tokens in the text. It can throw
    * <tt>IllegalStateException</tt> in case the document
    * was changed so the token-item chain becomes invalid.
    */
    public TokenItem getPrevious();

    /** Abstract implementation that doesn't contain chaining methods. */
    public abstract static class AbstractItem implements TokenItem {

        private TokenID tokenID;

        private TokenContextPath tokenContextPath;

        private String image;

        private int offset;

        public AbstractItem(TokenID tokenID, TokenContextPath tokenContextPath,
        int offset, String image) {
            this.tokenID = tokenID;
            this.tokenContextPath = tokenContextPath;
            this.offset = offset;
            this.image = image;
        }

        public TokenID getTokenID() {
            return tokenID;
        }

        public TokenContextPath getTokenContextPath() {
            return tokenContextPath;
        }

        public int getOffset() {
            return offset;
        }

        public String getImage() {
            return image;
        }

        public String toString() {
            return "'" + org.netbeans.editor.EditorDebug.debugString(getImage()) // NOI18N
                   + "', tokenID=" + getTokenID() + ", tcp=" + getTokenContextPath() // NOI18N
                   + ", offset=" + getOffset(); // NOI18N
        }

    }

    /** Implementation useful for delegation. */
    public static class FilterItem implements TokenItem {

        protected TokenItem delegate;

        public FilterItem(TokenItem delegate) {
            this.delegate = delegate;
        }

        public TokenID getTokenID() {
            return delegate.getTokenID();
        }

        public TokenContextPath getTokenContextPath() {
            return delegate.getTokenContextPath();
        }

        public int getOffset() {
            return delegate.getOffset();
        }

        public String getImage() {
            return delegate.getImage();
        }

        public TokenItem getNext() {
            return delegate.getNext();
        }

        public TokenItem getPrevious() {
            return delegate.getPrevious();
        }

        public String toString() {
            return delegate.toString();
        }

    }

}
