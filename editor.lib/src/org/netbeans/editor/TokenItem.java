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
    public static abstract class AbstractItem implements TokenItem {

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
