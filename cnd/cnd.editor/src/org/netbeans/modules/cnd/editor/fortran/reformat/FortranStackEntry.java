/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
