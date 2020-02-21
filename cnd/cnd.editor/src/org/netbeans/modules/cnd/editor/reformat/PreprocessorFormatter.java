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

package org.netbeans.modules.cnd.editor.reformat;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.*;
import org.netbeans.modules.cnd.editor.api.CodeStyle;
import org.netbeans.modules.cnd.editor.api.CodeStyle.BracePlacement;
import org.netbeans.modules.cnd.editor.reformat.DiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.reformat.Reformatter.Diff;

/**
 *
 */
public class PreprocessorFormatter {
    private final ReformatterImpl context;
    private final ExtendedTokenSequence ts;
    private final CodeStyle codeStyle;
    private final DiffLinkedList diffs;
    private int prepocessorDepth = 0;
    private final Stack<PreprocessorStateStack> stateStack = new Stack<PreprocessorStateStack>();
    private final BracesStack braces;

    
    /*package local*/ PreprocessorFormatter(ReformatterImpl context){
        this.context = context;
        this.ts = context.ts;
        this.codeStyle = context.codeStyle;
        this.diffs = context.diffs;
        this.braces = context.braces;
    }
    
    /*package local*/ void indentPreprocessor(Token<CppTokenId> previous) {
        TokenSequence<CppTokenId> prep = ts.embedded(CppTokenId.languagePreproc());
        if (prep == null){
            return;
        }
        prep.moveStart();
        while (prep.moveNext()) {
            if (!(prep.token().id() == WHITESPACE ||
                    prep.token().id() == PREPROCESSOR_START ||
                    prep.token().id() == PREPROCESSOR_START_ALT)) {
                break;
            }
        }
        Token<CppTokenId> directive = null;
        boolean atSharp = false;
        if (prep.token() != null) {
            directive = prep.token();
        }
        PreprocessorStateStack ps = null;
        if (directive != null) {
            switch (directive.id()) {
                case PREPROCESSOR_ELSE: //("else", "preprocessor-keyword-directive"),
                case PREPROCESSOR_ELIF: //("elif", "preprocessor-keyword-directive"),
                    prepocessorDepth--;
                    if (!stateStack.empty()){
                        ps = stateStack.pop();
                        ps.outputStack.add(braces.clone());
                        braces.reset(ps.inputStack);
                    }
                    break;
                case PREPROCESSOR_ENDIF: //("endif", "preprocessor-keyword-directive"),
                    prepocessorDepth--;
                    if (!stateStack.empty()){
                        ps = stateStack.pop();
                        ps.outputStack.add(braces.clone());
                        braces.reset(ps.getBestOutputStack());
                    }
                    break;
            }
            if (context.doFormat()) {
                while(prep.movePrevious()) {
                    if (prep.token().id() == PREPROCESSOR_START || prep.token().id() == CppTokenId.PREPROCESSOR_START_ALT) {
                        atSharp = true;
                        break;
                    }
                }
            }
        }
        if (atSharp) {
            selectPreprocessorIndent(previous, prep);
            if (prep.moveNext()) {
                Token<CppTokenId> prev = prep.token();
                while(prep.moveNext()) {
                    Token<CppTokenId> current = prep.token();
                    if (current.id() == WHITESPACE) {
                        replaceCurrentImbeded(prep, current, prev);
                    } else if (current.id() == ESCAPED_WHITESPACE) {
                        replaceCurrentImbeded(prep, current, prev);
                    }
                    prev = current;
                }
            }
        }
        if (directive != null) {
            switch (directive.id()) {
                case PREPROCESSOR_IF: //("if", "preprocessor-keyword-directive"),
                case PREPROCESSOR_IFDEF: //("ifdef", "preprocessor-keyword-directive"),
                case PREPROCESSOR_IFNDEF: //("ifndef", "preprocessor-keyword-directive"),
                    prepocessorDepth++;
                    stateStack.push(new PreprocessorStateStack(braces.clone()));
                    break;
                case PREPROCESSOR_ELSE: //("else", "preprocessor-keyword-directive"),
                case PREPROCESSOR_ELIF: //("elif", "preprocessor-keyword-directive"),
                    prepocessorDepth++;
                    if (ps != null) {
                        stateStack.push(ps);
                    } else {
                        stateStack.push(new PreprocessorStateStack(braces.clone()));
                    }
                    break;
            }
        }
    }
    
    private void selectPreprocessorIndent(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep) {
        Token<CppTokenId> next = null;
        if (prep.moveNext()) {
            next = prep.token();
            prep.movePrevious();
        }
        switch(codeStyle.indentPreprocessorDirectives()) {
            case CODE_INDENT:
                 indentByCode(previous, prep, next);
                 break;
            case START_LINE:
                 noIndent(previous, prep, next);
                 break;
            case PREPROCESSOR_INDENT:
                 indentByPreprocessor(previous, prep, next);
                 break;
        }
    }

    private void noIndent(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        indentBefore(previous, 0, false); // NOI18N
        indentAfter(prep, next, 0); // NOI18N
    }

    private int textLength(String text, boolean startLine) {
        if (startLine) {
            int l = 0;
            for(int i = 0; i < text.length(); i++) {
                if (text.charAt(i) == '\t') {
                    if (context.tabSize > 1) {
                        l += context.tabSize;
                    } else {
                        l++;
                    }
                } else {
                    l++;
                }
            }
            return l;
        } else {
            return text.length();
        }
    }

    private void replaceCurrentImbeded(TokenSequence<CppTokenId> prep, Token<CppTokenId> current, Token<CppTokenId> previous){
        String old = current.text().toString();
        if (current.id() == WHITESPACE) {
            int l = textLength(old, previous.id() == ESCAPED_LINE);
            if (!Diff.equals(old, 0, l, false, context.expandTabToSpaces, context.tabSize)){
                diffs.addFirst(prep.offset(), prep.offset() + current.length(), 0, l, previous.id() == ESCAPED_LINE);
            }
        } else if (current.id() == ESCAPED_WHITESPACE) {
            int beg = -1;
            for(int i = 0; i < old.length(); i++) {
                if (old.charAt(i) == '\\') {
                    beg = i;
                    break;
                }
            }
            if (beg > 0) {
                String first = old.substring(0, beg);
                int l = textLength(first, previous.id() == ESCAPED_LINE);
                if (!Diff.equals(first, 0, l, false, context.expandTabToSpaces, context.tabSize)){
                    diffs.addFirst(prep.offset(), prep.offset() + first.length(), 0, l, false);
                }
                String rest = old.substring(beg+2);
                l = textLength(rest, true);
                if (!Diff.equals(rest, 0, l, false, context.expandTabToSpaces, context.tabSize)){
                    diffs.addFirst(prep.offset()+beg+2, prep.offset() + old.length(), 0, l, true);
                }
            }
        }
    }

    private void indentBefore(Token<CppTokenId> previous, int spaces, boolean isIndent) {
        DiffResult diff = diffs.getDiffs(ts, -1);
        if (diff != null) {
            if (diff.after != null) {
                diff.after.replaceSpaces(spaces, isIndent); // NOI18N
                if (diff.replace != null && !diff.after.hasNewLine()){
                    diff.replace.replaceSpaces(0, false); // NOI18N
                }
                return;
            } else if (diff.replace != null) {
                diff.replace.replaceSpaces(spaces, isIndent); // NOI18N
                return;
            }
        }
        if (previous != null && previous.id() == WHITESPACE) {
            if (!Diff.equals(previous.text().toString(), 0, spaces, isIndent, context.expandTabToSpaces, context.tabSize)){
                ts.replacePrevious(previous, 0, spaces, isIndent);
            }
        } else {
            if (spaces > 0){
                ts.addBeforeCurrent(0, spaces, isIndent);
            }
        }
    }

    private void indentAfter(TokenSequence<CppTokenId> prep, Token<CppTokenId> next, int spaces) {
        if (next.id() == WHITESPACE) {
            if (!Diff.equals(next.text().toString(), 0, spaces, false, context.expandTabToSpaces, context.tabSize)){
                diffs.addFirst(prep.offset() + prep.token().length(),
                               prep.offset() + prep.token().length() + next.length(), 0, spaces, false);
            }
        } else {
            if (spaces > 0) {
                diffs.addFirst(prep.offset()+ prep.token().length(),
                               prep.offset()+ prep.token().length(), 0, spaces, false);
            }
        }
    }

    private void indentByCode(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        if (codeStyle.sharpAtStartLine()) {
            indentBefore(previous, 0, false); // NOI18N
            indentAfter(prep, next, context.getIndent());
        } else {
            indentBefore(previous, context.getIndent(), true);
            indentAfter(prep, next, 0); // NOI18N
        }
    }

    private void indentByPreprocessor(Token<CppTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        if (codeStyle.sharpAtStartLine()) {
            indentBefore(previous, 0, false); // NOI18N
            indentAfter(prep, next, getPreprocessorIndent(prepocessorDepth));
        } else {
            indentBefore(previous, getPreprocessorIndent(prepocessorDepth), true);
            indentAfter(prep, next, 0);
        }
    }

    private int getPreprocessorIndent(int shift) {
        if (shift > 0) {
            if (codeStyle.getFormatNewlineBeforeBrace() == BracePlacement.NEW_LINE_HALF_INDENTED) {
                return shift * (codeStyle.indentSize()/2);
            } else {
                return shift * codeStyle.indentSize();
            }
        } else {
            return 0;
        }
    }
    private static class PreprocessorStateStack {
        private BracesStack inputStack;
        private List<BracesStack> outputStack = new ArrayList<BracesStack>();
        private PreprocessorStateStack(BracesStack inputStack){
            this.inputStack = inputStack;
        }
        private BracesStack getBestOutputStack(){
            if (outputStack.size()>0){
                BracesStack min =null;
                int minLen = Integer.MAX_VALUE;
                BracesStack max =null;
                int maxLen = Integer.MIN_VALUE;
                int inLen = inputStack.getLength();
                for(BracesStack out : outputStack){
                    int currentLen = out.getLength();
                    if (currentLen < inLen){
                        if (currentLen <= minLen) {
                            min = out;
                            minLen = currentLen;
                        }
                    } else if (currentLen > inLen) {
                        if (currentLen >= maxLen) {
                            max = out;
                            maxLen = currentLen;
                        }
                    }
                }
                if (min != null && max == null) {
                    return min;
                }
                if (max != null) {
                    return max;
                }
                return outputStack.get(outputStack.size()-1);
            }
            return inputStack;
        }
        
    }
}
