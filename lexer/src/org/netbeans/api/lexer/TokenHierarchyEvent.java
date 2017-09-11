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
