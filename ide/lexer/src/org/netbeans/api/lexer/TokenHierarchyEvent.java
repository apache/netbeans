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

package org.netbeans.api.lexer;

import org.netbeans.lib.lexer.inc.TokenHierarchyEventInfo;

/**
 * Description of the changes made in a token hierarchy.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenHierarchyEvent extends java.util.EventObject {

    private final TokenHierarchyEventInfo info;

    TokenHierarchyEvent(TokenHierarchyEventInfo info) {
        super(info.tokenHierarchyOperation().tokenHierarchy());
        this.info = info;
    }

    /**
     * Get source of this event as a token hierarchy instance.
     */
    public TokenHierarchy<?> tokenHierarchy() {
        return (TokenHierarchy<?>)getSource();
    }
    
    /**
     * Get reason why a token hierarchy event was fired.
     */
    public TokenHierarchyEventType type() {
        return info.type();
    }

    /**
     * Get the token change that occurred in the tokens
     * at the top-level of the token hierarchy.
     */
    public TokenChange<?> tokenChange() {
        return info.tokenChange();
    }

    /**
     * Get the token change if the top level of the token hierarchy
     * contains tokens of the given language.
     *
     * @param language non-null language.
     * @return non-null token change if the language at the top level
     *  of the token hierarchy equals to the given language.
     *  Returns null otherwise.
     */
    public <T extends TokenId> TokenChange<T> tokenChange(Language<T> language) {
        TokenChange<?> tc = tokenChange();
        @SuppressWarnings("unchecked")
        TokenChange<T> tcl = (tc != null && tc.language() == language) ? (TokenChange<T>)tc : null;
        return tcl;
    }
    
    /**
     * Get start offset of the area that was affected by the attached
     * token change(s).
     */
    public int affectedStartOffset() {
        return info.affectedStartOffset();
    }
    
    /**
     * Get end offset of the area that was affected by the attached
     * token change(s).
     * <br/>
     * If there was a text modification the offsets are related
     * to the state after the modification.
     */
    public int affectedEndOffset() {
        return info.affectedEndOffset();
    }

    /**
     * Get offset in the input source where the modification occurred.
     *
     * @return modification offset or -1
     *  if this event's type is not {@link TokenHierarchyEventType#MODIFICATION}.
     */
    public int modificationOffset() {
        return info.modOffset();
    }
    
    /**
     * Get number of characters inserted by the text modification
     * that caused this token change.
     *
     * @return number of inserted characters by the modification.
     *  <br/>
     *  Returns 0 
     *  if this event's type is not {@link TokenHierarchyEventType#MODIFICATION}.
     */
    public int insertedLength() {
        return info.insertedLength();
    }
    
    /**
     * Get number of characters removed by the text modification
     * that caused this token change.
     *
     * @return number of inserted characters by the modification.
     *  <br/>
     *  Returns 0 
     *  if this event's type is not {@link TokenHierarchyEventType#MODIFICATION}.
     */
    public int removedLength() {
        return info.removedLength();
    }
    
    public String toString() {
        return "THEvent@" + Integer.toHexString(System.identityHashCode(this)) + "; " + info;
    }

}
