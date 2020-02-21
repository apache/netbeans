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

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.cnd.api.lexer.CppTokenId;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_ELIF;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_ELSE;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_ENDIF;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_IF;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_IFDEF;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_IFNDEF;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_START;
import static org.netbeans.cnd.api.lexer.CppTokenId.PREPROCESSOR_START_ALT;
import static org.netbeans.cnd.api.lexer.CppTokenId.WHITESPACE;
import org.netbeans.cnd.api.lexer.FortranTokenId;
import org.netbeans.modules.cnd.editor.fortran.options.FortranCodeStyle;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranDiffLinkedList.DiffResult;
import org.netbeans.modules.cnd.editor.fortran.reformat.FortranReformatter.Diff;

/**
 *
 */
public class FortranPreprocessorFormatter {
    private final FortranReformatterImpl context;
    private final FortranExtendedTokenSequence ts;
    private final FortranCodeStyle codeStyle;
    private final FortranDiffLinkedList diffs;
    private int prepocessorDepth = 0;
    private final Stack<PreprocessorStateStack> stateStack = new Stack<PreprocessorStateStack>();
    private final FortranBracesStack braces;

    
    /*package local*/ FortranPreprocessorFormatter(FortranReformatterImpl context){
        this.context = context;
        this.ts = context.ts;
        this.codeStyle = context.codeStyle;
        this.diffs = context.diffs;
        this.braces = context.braces;
    }
    
    /*package local*/ void indentPreprocessor(Token<FortranTokenId> previous) {
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
                    if (prep.token().id() == PREPROCESSOR_START || prep.token().id() == PREPROCESSOR_START_ALT) {
                        atSharp = true;
                        break;
                    }
                }
            }
        }
        if (atSharp) {
            selectPreprocessorIndent(previous, prep);
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
    
    private void selectPreprocessorIndent(Token<FortranTokenId> previous, TokenSequence<CppTokenId> prep) {
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

    private void noIndent(Token<FortranTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        indentBefore(previous, 0, false); // NOI18N
        indentAfter(prep, next, 0); // NOI18N
    }

    private void indentBefore(Token<FortranTokenId> previous, int spaces, boolean isIndent) {
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
        if (previous != null && previous.id() == FortranTokenId.WHITESPACE) {
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

    private void indentByCode(Token<FortranTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
        if (codeStyle.sharpAtStartLine()) {
            indentBefore(previous, 0, false); // NOI18N
            indentAfter(prep, next, context.getIndent());
        } else {
            indentBefore(previous, context.getIndent(), true);
            indentAfter(prep, next, 0); // NOI18N
        }
    }

    private void indentByPreprocessor(Token<FortranTokenId> previous, TokenSequence<CppTokenId> prep, Token<CppTokenId> next) {
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
            return shift * codeStyle.indentSize();
        } else {
            return 0;
        }
    }
    private static class PreprocessorStateStack {
        private FortranBracesStack inputStack;
        private List<FortranBracesStack> outputStack = new ArrayList<FortranBracesStack>();
        private PreprocessorStateStack(FortranBracesStack inputStack){
            this.inputStack = inputStack;
        }
        private FortranBracesStack getBestOutputStack(){
            if (outputStack.size()>0){
                FortranBracesStack min =null;
                int minLen = Integer.MAX_VALUE;
                FortranBracesStack max =null;
                int maxLen = Integer.MIN_VALUE;
                int inLen = inputStack.getLength();
                for(FortranBracesStack out : outputStack){
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
