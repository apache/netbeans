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
