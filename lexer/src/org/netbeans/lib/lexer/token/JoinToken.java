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

import java.util.List;
import org.netbeans.api.lexer.PartType;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.WrapTokenId;
import org.netbeans.spi.lexer.TokenPropertyProvider;

/**
 * Token consisting of multiple parts.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class JoinToken<T extends TokenId> extends PropertyToken<T> {

    private List<PartToken<T>> joinedParts; // 32 bytes (28-super + 4)
    
    private int completeLength; // 36 bytes
    
    /**
     * Number of ETLs spanned including empty ETLs except a first part.
     */
    private int extraTokenListSpanCount; // 40 bytes

    public JoinToken(WrapTokenId<T> wid, int length, TokenPropertyProvider<T> propertyProvider, PartType partType) {
        super(wid, length, propertyProvider, partType);
    }

    @Override
    public List<PartToken<T>> joinedParts() {
        return joinedParts;
    }
    
    public void setJoinedParts(List<PartToken<T>> joinedParts, int extraTokenListSpanCount) {
        assert (joinedParts != null) : "joinedParts expected to be non-null";
        this.joinedParts = joinedParts;
        for (PartToken partToken : joinedParts) {
            completeLength += partToken.length();
        }
        this.extraTokenListSpanCount = extraTokenListSpanCount;
    }

    public PartToken<T> lastPart() {
        return joinedParts.get(joinedParts.size() - 1);
    }

    public int extraTokenListSpanCount() {
        return extraTokenListSpanCount;
    }

    @Override
    public int offset(TokenHierarchy<?> tokenHierarchy) {
        return joinedParts.get(0).offset(tokenHierarchy);
    }
    
    public int endOffset() {
        PartToken<T> partToken = joinedParts.get(joinedParts.size() - 1);
        return partToken.offset(null) + partToken.length();
    }

    @Override
    public int length() {
        return completeLength;
    }

    @Override
    public CharSequence text() {
        return new JoinTokenText<T>(joinedParts, completeLength);
    }

    @Override
    public boolean isRemoved() {
        // Check whether last part of token is removed - this needs to be improved
        // for the case when token is just partially recreated.
        return lastPart().isRemoved();
    }

    @Override
    public StringBuilder dumpInfo(StringBuilder sb, TokenHierarchy<?> tokenHierarchy,
            boolean dumpTokenText, boolean dumpRealOffset, int indent
    ) {
        sb = super.dumpInfo(sb, tokenHierarchy, dumpTokenText, dumpRealOffset, indent);
        sb.append(", ").append(joinedParts.size()).append(" parts");
        int digitCount = String.valueOf(joinedParts.size() - 1).length();
        for (int i = 0; i < joinedParts.size(); i++) {
            sb.append('\n');
            ArrayUtilities.appendSpaces(sb, indent + 2);
            ArrayUtilities.appendBracketedIndex(sb, i, digitCount);
            joinedParts.get(i).dumpInfo(sb, tokenHierarchy, dumpTokenText, dumpRealOffset, indent + 4);
        }
        return sb;
    }

    public StringBuilder dumpText(StringBuilder sb, CharSequence inputSourceText) {
        for (int i = 0; i < joinedParts.size(); i++) {
            joinedParts.get(i).dumpText(sb, inputSourceText);
        }
        return sb;
    }

    @Override
    protected String dumpInfoTokenType() {
        return "JoiT"; // NOI18N
    }

}
