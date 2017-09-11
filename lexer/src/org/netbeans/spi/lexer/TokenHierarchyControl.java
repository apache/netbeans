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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.spi.lexer;

import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenHierarchyOperation;

/**
 * Control class for managing token hierarchy of a mutable text input.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyControl<I> {

    private TokenHierarchyOperation<I,?> operation;

    TokenHierarchyControl(MutableTextInput<I> input) {
        this.operation = new TokenHierarchyOperation<I,TokenId>(input);
    }

    /**
     * Get token hierarchy managed by this control object.
     * 
     * @return non-null token hierarchy.
     */
    public TokenHierarchy<I> tokenHierarchy() {
        return operation.tokenHierarchy();
    }
    
    /**
     * Notify that the text of the mutable text input was modified.
     * <p>
     * This method should only be invoked under modification lock (write-lock)
     * over the mutable input source.
     * </p>
     * 
     *
     * @param offset &gt;=0 offset where the modification occurred.
     * @param removedLength &gt;=0 number of characters removed from the input.
     * @param removedText text removed from the input. If it's not available
     *  to determine the removed text then this parameter may be null.
     *  <br>
     *  Providing of the removed text allows the incremental
     *  algorithm to use an efficient token validation if possible.
     * @param insertedLength &gt;=0 number of characters inserted at the offset
     *  after the removal.
     */
    public void textModified(int offset,
    int removedLength, CharSequence removedText,
    int insertedLength) {
        operation.textModified(offset, removedLength, removedText, insertedLength);
    }

    /**
     * Making the token hierarchy inactive will release all the tokens in the hierarchy
     * so that there will be no tokens. The hierarchy can be made active again
     * later.
     * <br/>
     * Making the hierarchy inactive will free memory occupied by tokens. It can be done
     * e.g. once a document is not edited for a long time (and is not showing on screen).
     * 
     * <p>
     * This method should only be invoked under modification lock (write-lock)
     * over the mutable input source.
     * </p>
     * 
     * @param active whether the hierarchy should become active or inactive.
     */
    public void setActive(boolean active) {
        operation.setActive(active);
    }
    
    /**
     * Check whether the hierarchy is currently active or not. Inactive hierarchy
     * does not hold any tokens and its {@link TokenHierarchy#tokenSequence()}
     * returns null.
     * 
     * <p>
     * This method should only be invoked under read/write lock over the mutable input source.
     * </p>
     * 
     * @return true if the hierarchy is active or false when inactive.
     */
    public boolean isActive() {
        return operation.isActive();
    }

    /**
     * Rebuild token hierarchy completely.
     * <br/>
     * This may be necessary if lexing depends on some input properties
     * that get changed.
     * <br/>
     * This method will drop all present tokens and let them to be lazily recreated.
     * <br/>
     * This method should only be invoked under modification lock over the mutable
     * input source (e.g. a document's write-lock).
     * Otherwise all the active token sequences would fail with 
     * <code>ConcurrentModificationException</code>.
     */
    public void rebuild() {
        operation.rebuild();
    }

}
