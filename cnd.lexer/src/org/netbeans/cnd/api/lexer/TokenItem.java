/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
