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

import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.cnd.api.lexer.CndLexerUtilities.FortranFormat;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import static org.netbeans.cnd.api.lexer.FortranTokenId.*;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;

/**
 *
 */
class FortranBracesStack implements Cloneable {
    private static final boolean TRACE_STACK = false;
    private static final int FIXED_FORMAT_SHIFT = 6;
    
    private final Stack<FortranStackEntry> stack = new Stack<FortranStackEntry>();
    private final FortranCodeStyle codeStyle;
    int parenDepth = 0;

    FortranBracesStack(FortranCodeStyle codeStyle) {
        this.codeStyle = codeStyle;
    }

    @Override
    public FortranBracesStack clone(){
        FortranBracesStack clone = new FortranBracesStack(codeStyle);
        for(int i = 0; i < stack.size(); i++){
            clone.stack.add(stack.get(i));
        }
        clone.parenDepth = parenDepth;
        return clone;
    }
    
    public void reset(FortranBracesStack clone){
        stack.clear();
        for(int i = 0; i < clone.stack.size(); i++){
            stack.add(clone.stack.get(i));
        }
        parenDepth = clone.parenDepth;
    }

    public void push(Token<FortranTokenId> token, FortranExtendedTokenSequence ts) {
        FortranStackEntry newEntry = new FortranStackEntry(token, ts);
        pushImpl(newEntry);
    }

    public void push(FortranTokenId id) {
        FortranStackEntry newEntry = new FortranStackEntry(id);
        pushImpl(newEntry);
    }

    private void pushImpl(FortranStackEntry newEntry) {
        FortranStackEntry prevEntry = peek();
        int prevIndent = 0;
        int prevSelfIndent = 0;
        int statementIndent = codeStyle.indentSize();
        int switchIndent = codeStyle.indentSize();
        if (prevEntry != null){
            prevIndent = prevEntry.getIndent();
            prevSelfIndent = prevEntry.getSelfIndent();
        }
        switch (newEntry.getKind()) {
            case KW_ELSEIF:
            case KW_ELSE:
                if (prevEntry != null && 
                   (prevEntry.getKind() == KW_IF || prevEntry.getKind() == KW_ELSE || prevEntry.getKind() == KW_ELSEIF ||
                    prevEntry.getKind() == KW_WHERE || prevEntry.getKind() == KW_ELSE || prevEntry.getKind() == KW_ELSEWHERE)) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_IF:
                if (prevEntry != null && prevEntry.getKind() == KW_ELSE) {
                    newEntry.setIndent(prevIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                    break;
                }
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_WHERE:
            case KW_DO:
            case KW_FORALL:
            case KW_WHILE:
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_SELECT:
            case KW_SELECTCASE:
            case KW_SELECTTYPE:
                if (codeStyle.indentCasesFromSwitch()) {
                    newEntry.setIndent(prevSelfIndent + codeStyle.indentSize() + switchIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                } else {
                    newEntry.setIndent(prevSelfIndent + switchIndent);
                    newEntry.setSelfIndent(prevSelfIndent);
                }
                break;
            case KW_MODULE:
            case KW_PROGRAM:
            case KW_PROCEDURE:
            case KW_SUBROUTINE:
            case KW_FUNCTION:
            case KW_BLOCK:
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            case KW_INTERFACE:
            case KW_STRUCTURE:
            case KW_UNION:
            case KW_ENUM:
            case KW_TYPE:
            case KW_BLOCKDATA:
            case KW_MAP:
                newEntry.setIndent(prevIndent + statementIndent);
                newEntry.setSelfIndent(prevIndent);
                break;
            default:
                assert(false);
        }
        push(newEntry);
    }
        
    public int getIndent(){
        int shift = 0;
        if (codeStyle.getFormatFortran() == FortranFormat.FIXED){
            shift = FIXED_FORMAT_SHIFT;
        }
        FortranStackEntry top = peek();
        if (top != null) {
            return shift + top.getIndent();
        }
        return shift;
    }

    public int getSelfIndent(){
        int shift = 0;
        if (codeStyle.getFormatFortran() == FortranFormat.FIXED){
            shift = FIXED_FORMAT_SHIFT;
        }
        FortranStackEntry top = peek();
        if (top != null) {
            return shift + top.getSelfIndent();
        }
        return shift;
    }

    private void push(FortranStackEntry entry) {
        if (entry.getKind() == KW_ELSE || entry.getKind() == KW_ELSEIF || entry.getKind() == KW_ELSEWHERE){
            if (stack.size() > 0 && 
                (stack.peek().getKind() == KW_IF || stack.peek().getKind() == KW_ELSE || stack.peek().getKind() == KW_ELSEIF ||
                 stack.peek().getKind() == KW_WHERE ||stack.peek().getKind() == KW_ELSEWHERE)) {
                stack.pop();
            }
        }
        stack.push(entry);
        if (TRACE_STACK) {System.out.println("push: "+toString());} // NOI18N
    }

    public void pop(FortranExtendedTokenSequence ts) {
        popImpl(ts);
        if (TRACE_STACK) {System.out.println("pop "+ts.token().id().name()+": "+toString());} // NOI18N
    }

    public void popImpl(FortranExtendedTokenSequence ts) {
        if (stack.empty()) {
            return;
        }
        stack.pop();
    }
    
    public FortranStackEntry peek() {
        if (stack.empty()) {
            return null;
        }
        return stack.peek();
    }

    public int getLength() {
        return stack.size();
    }
    
    @Override
    public String toString(){
        StringBuilder buf = new StringBuilder();
        for(int i = 0; i < stack.size(); i++){
            FortranStackEntry entry = stack.get(i);
            if (i > 0) {
                buf.append(", "); // NOI18N
            }
            buf.append(entry.toString());
        }
        buf.append("+").append(getIndent()).append("-").append(getSelfIndent()); // NOI18N
        return buf.toString();
    }

}
