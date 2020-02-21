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

package org.netbeans.modules.cnd.editor.fortran.reformat;

import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;

/**
 *
 */
class FortranStackEntry {

    private final FortranTokenId kind;
    private int indent;
    private int selfIndent;
    private int label = -1;

    FortranStackEntry(Token<FortranTokenId> token, FortranExtendedTokenSequence ts) {
        kind = token.id();
        init(ts);
    }

    FortranStackEntry(FortranTokenId id) {
        kind = id;
        init(null);
    }

    private void init(FortranExtendedTokenSequence ts) {
        switch (kind) {
            case KW_DO:
                if (ts != null) {
                    Token<FortranTokenId> next = ts.lookNextLineImportantAfter(kind);
                    if (next != null && next.id() == NUM_LITERAL_INT) {
                        label = Integer.parseInt(next.text().toString());
                    }
                }
                break;
            case KW_INTERFACE:
            case KW_STRUCTURE:
            case KW_UNION:
            case KW_ENUM:
            case KW_TYPE:
            case KW_BLOCKDATA:

            case KW_MODULE:
            case KW_PROGRAM:
            case KW_PROCEDURE:
            case KW_SUBROUTINE:
            case KW_FUNCTION:

            case KW_MAP:

            case KW_BLOCK:
            case KW_IF:
            case KW_ELSE:
            case KW_ELSEIF:
            case KW_ELSEWHERE:
            case KW_WHERE:
            case KW_WHILE:
            case KW_FORALL:
            case KW_SELECT:
            case KW_SELECTCASE:
            case KW_SELECTTYPE:
                break;
            default:
                assert (false);
        }
    }

    public int getIndent(){
        return indent;
    }

    public void setIndent(int indent){
        this.indent = indent;
    }

    public int getSelfIndent(){
        return selfIndent;
    }

    public void setSelfIndent(int selfIndent){
        this.selfIndent = selfIndent;
    }
    
    public FortranTokenId getKind() {
        return kind;
    }

    public int getLabel() {
        return label;
    }

    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder(kind.name());
        return buf.toString();
    }
}
