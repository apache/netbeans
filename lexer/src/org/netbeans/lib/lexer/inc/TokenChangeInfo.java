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

package org.netbeans.lib.lexer.inc;

import org.netbeans.api.lexer.TokenChange;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.LexerApiPackageAccessor;
import org.netbeans.lib.lexer.TokenList;

/**
 * Description of the change in a token list.
 * <br/>
 * The change is expressed as a list of removed tokens
 * plus the current list and index and number of the tokens
 * added to the current list.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class TokenChangeInfo<T extends TokenId> {
    
    private static final TokenChange<?>[] EMPTY_EMBEDDED_CHANGES
            = (TokenChange<?>[])new TokenChange[0];

    private TokenChange<?>[] embeddedChanges = EMPTY_EMBEDDED_CHANGES;
    
    private final TokenList<T> currentTokenList;
    
    private RemovedTokenList<T> removedTokenList;
    
    private int addedTokenCount;

    private int index;

    private int offset;
    
    private boolean boundsChange;


    public TokenChangeInfo(TokenList<T> currentTokenList) {
        this.currentTokenList = currentTokenList;
    }

    public TokenChange<?>[] embeddedChanges() {
        return embeddedChanges;
    }
    
    public void addEmbeddedChange(TokenChangeInfo<?> change) {
        TokenChange<?>[] tmp = (TokenChange<?>[])
                new TokenChange[embeddedChanges.length + 1];
        System.arraycopy(embeddedChanges, 0, tmp, 0, embeddedChanges.length);
        tmp[embeddedChanges.length] = LexerApiPackageAccessor.get().createTokenChange(change);
        embeddedChanges = tmp;
    }
    
    public int index() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
    public int offset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public RemovedTokenList<T> removedTokenList() {
        return removedTokenList;
    }
    
    public void setRemovedTokenList(RemovedTokenList<T> removedTokenList) {
        this.removedTokenList = removedTokenList;
    }
    
    public int addedTokenCount() {
        return addedTokenCount;
    }

    public void setAddedTokenCount(int addedTokenCount) {
        this.addedTokenCount = addedTokenCount;
    }
    
    public void updateAddedTokenCount(int diff) {
        addedTokenCount += diff;
    }

    public TokenList<T> currentTokenList() {
        return currentTokenList;
    }
    
    public boolean isBoundsChange() {
        return boundsChange;
    }
    
    public void markBoundsChange() {
        this.boundsChange = true;
    }
    
}
