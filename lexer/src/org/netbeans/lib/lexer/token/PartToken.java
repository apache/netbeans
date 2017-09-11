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

package org.netbeans.lib.lexer.token;

import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.lexer.TokenOrEmbedding;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Part of a {@link JoinToken}.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class PartToken<T extends TokenId> extends PropertyToken<T> {

    private TokenOrEmbedding<T> joinTokenOrEmbedding; // 32 bytes (28-super + 4)
    
    private final int partTokenIndex; // Index of this part inside 
    
    private final int partTextOffset; // Offset of this part's text among all parts that comprise the complete token

    public PartToken(WrapTokenId<T> wid, int length, TokenPropertyProvider<T> propertyProvider, PartType partType,
            TokenOrEmbedding<T> joinToken, int partTokenIndex, int partTextOffset
    ) {
        super(wid, length, propertyProvider, partType);
        setJoinTokenOrEmbedding(joinToken);
        this.partTokenIndex = partTokenIndex;
        this.partTextOffset = partTextOffset;
    }

    @Override
    public JoinToken<T> joinToken() {
        return (JoinToken<T>)joinTokenOrEmbedding.token();
    }
    
    public boolean isLastPart() {
        return (joinToken().lastPart() == this);
    }
    
    public TokenOrEmbedding<T> joinTokenOrEmbedding() {
        return joinTokenOrEmbedding;
    }
    
    public void setJoinTokenOrEmbedding(TokenOrEmbedding<T> joinTokenOrEmbedding) {
        this.joinTokenOrEmbedding = joinTokenOrEmbedding;
    }

    public int partTokenIndex() {
        return partTokenIndex;
    }

    public int partTextOffset() {
        return partTextOffset;
    }

    public int partTextEndOffset() {
        return partTextOffset + length();
    }

    @Override
    protected String dumpInfoTokenType() {
        return "ParT[" + (partTokenIndex+1) + "/" + joinToken().joinedParts().size() + "]"; // NOI18N
    }

}
