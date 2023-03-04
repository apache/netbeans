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

package org.netbeans.editor.ext.java;

import javax.swing.text.JTextComponent;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.ext.AbstractFormatLayer;
import org.netbeans.editor.ext.FormatTokenPosition;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.FormatSupport;
import org.netbeans.editor.ext.FormatWriter;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.editor.util.swing.DocumentUtilities;

/**
* Java indentation services are located here
*
* @author Miloslav Metelka
* @version 1.00
*/

public class JavaFormatter extends ExtFormatter {

    public JavaFormatter(Class kitClass) {
        super(kitClass);
    }

    protected boolean acceptSyntax(Syntax syntax) {
        return (syntax instanceof JavaSyntax);
    }

    public int[] getReformatBlock(JTextComponent target, String typedText) {
        int[] ret = null;
        BaseDocument doc = Utilities.getDocument(target);
        int dotPos = target.getCaret().getDot();
        if (doc != null) {
            /* Check whether the user has written the ending 'e'
             * of the first 'else' on the line.
             */
            if ("e".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 4 == dotPos
                        && CharSequenceUtilities.textEquals("else", DocumentUtilities.getText(doc, fnw, 4)) // NOI18N
                    ) {
                        ret = new int[] { fnw, fnw + 4 };
                    }
                } catch (BadLocationException e) {
                }

            } else if (":".equals(typedText)) { // NOI18N
                try {
                    int fnw = Utilities.getRowFirstNonWhite(doc, dotPos);
                    if (fnw >= 0 && fnw + 4 <= doc.getLength()
                        && CharSequenceUtilities.textEquals("case", DocumentUtilities.getText(doc, fnw, 4)) // NOI18N
                    ) {
                        ret = new int[] { fnw, fnw + 4 };
                    } else {
                        if (fnw >= 0 & fnw + 7 <= doc.getLength()
                            && CharSequenceUtilities.textEquals("default", DocumentUtilities.getText(doc, fnw, 7)) // NOI18N
                        ) {
                            ret = new int[] {fnw, fnw + 7 };
                        }
                    }
                } catch (BadLocationException e) {
                }
            
            } else {
                ret = super.getReformatBlock(target, typedText);
            }
        }
        
        return ret;
    }

    protected void initFormatLayers() {
        addFormatLayer(new StripEndWhitespaceLayer());
        addFormatLayer(new JavaLayer());
    }

    public FormatSupport createFormatSupport(FormatWriter fw) {
        return new JavaFormatSupport(fw);
    }

    public class StripEndWhitespaceLayer extends AbstractFormatLayer {

        public StripEndWhitespaceLayer() {
            super("java-strip-whitespace-at-line-end"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new JavaFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            JavaFormatSupport jfs = (JavaFormatSupport)createFormatSupport(fw);

            FormatTokenPosition pos = jfs.getFormatStartPosition();
            if (jfs.isIndentOnly()) { // don't do anything

            } else { // remove end-line whitespace
                while (pos.getToken() != null) {
                    FormatTokenPosition startPos = pos;
                    pos = jfs.removeLineEndWhitespace(pos);
                    if (pos.getToken() != null) {
                        pos = jfs.getNextPosition(pos);
                    }
                    // fix for issue 14725
                    // this is more hack than correct fix. It happens that 
                    // jfs.removeLineEndWhitespace() does not move to next
                    // position. The reason is that token from which the 
                    // endline whitespaces must be removed is not 'modifiable' - 
                    // FormatWritter.canModifyToken() returns false in
                    // FormatWritter.remove. I don't dare to fix this problem 
                    // in ExtFormatSupport and so I'm patching this
                    // loop to check whether we are still on the same position
                    // and if we are, let's do break. If similar problem reappear
                    // we will have to find better fix. Hopefully, with the planned
                    // conversion of indentation engines to new lexel module
                    // all this code will be replaced in next verison.
                    if (startPos.equals(pos)) {
                        break;
                    }
                }
            }
        }

    }

    public class JavaLayer extends AbstractFormatLayer {

        public JavaLayer() {
            super("java-layer"); // NOI18N
        }

        protected FormatSupport createFormatSupport(FormatWriter fw) {
            return new JavaFormatSupport(fw);
        }

        public void format(FormatWriter fw) {
            try {
                JavaFormatSupport jfs = (JavaFormatSupport)createFormatSupport(fw);

                FormatTokenPosition pos = jfs.getFormatStartPosition();

                if (jfs.isIndentOnly()) {  // create indentation only
                    jfs.indentLine(pos);

                } else { // regular formatting

                    while (pos != null) {

                        // Indent the current line
                        jfs.indentLine(pos);

                        // Format the line by additional rules
                        formatLine(jfs, pos);

                        // Goto next line
                        FormatTokenPosition pos2 = jfs.findLineEnd(pos);
                        if (pos2 == null || pos2.getToken() == null)
                            break; // the last line was processed
                        
                        pos = jfs.getNextPosition(pos2, javax.swing.text.Position.Bias.Forward);
                        if (pos == pos2)
                            break; // in case there is no next position
                        if (pos == null || pos.getToken() == null)
                            break; // there is nothing after the end of line
                        
                        FormatTokenPosition fnw = jfs.findLineFirstNonWhitespace(pos);
                        if (fnw != null) {
                          pos = fnw;
                        } else { // no non-whitespace char on the line
                          pos = jfs.findLineStart(pos);
                        }
                    }
                }
            } catch (IllegalStateException e) {
            }
        }


        private void removeLineBeforeToken(TokenItem token, JavaFormatSupport jfs, boolean checkRBraceBefore){
            FormatTokenPosition tokenPos = jfs.getPosition(token, 0);
            // Check that nothing exists before token
            if (jfs.findNonWhitespace(tokenPos, null, true, true) != null){
                return;
            }

            // Check that the backward nonWhite is }
            if (checkRBraceBefore){
                FormatTokenPosition ftpos = jfs.findNonWhitespace(tokenPos, null, false, true);
                if (ftpos == null || ftpos.getToken().getTokenID().getNumericID() != JavaTokenContext.RBRACE_ID){
                    return;
                }
            } 
            
            // Check that nothing exists after token, but ignore comments
            if (jfs.getNextPosition(tokenPos) != null){
                FormatTokenPosition ftp = jfs.findImportant(jfs.getNextPosition(tokenPos), null, true, false);
                if (ftp != null){
                    insertNewLineBeforeToken(ftp.getToken(), jfs);
                }
            }

            // check that on previous line is some stmt
            FormatTokenPosition ftp = jfs.findLineStart(tokenPos); // find start of current line
            FormatTokenPosition endOfPreviousLine = jfs.getPreviousPosition(ftp); // go one position back - means previous line
            if (endOfPreviousLine == null || endOfPreviousLine.getToken().getTokenID() != JavaTokenContext.WHITESPACE){
                return;
            }
            ftp = jfs.findLineStart(endOfPreviousLine); // find start of the previous line - now we have limit position
            ftp = jfs.findImportant(tokenPos, ftp, false, true); // find something important till the limit
            if (ftp == null){
                return;
            }

            // check that previous line does not end with "{" or line comment
            ftp = jfs.findNonWhitespace(endOfPreviousLine, null, true, true);
            if (ftp.getToken().getTokenID() == JavaTokenContext.LINE_COMMENT ||
                ftp.getToken().getTokenID() == JavaTokenContext.LBRACE){
                return;
            }

            // now move the token to the end of previous line
            boolean remove = true;
            while (remove)
            {
                if (token.getPrevious() == endOfPreviousLine.getToken()){
                    remove = false;
                }
                if (jfs.canRemoveToken(token.getPrevious())){
                    jfs.removeToken(token.getPrevious());
                }else{
                    return;  // should never get here!
                }
            }
            // insert one space before token
            if (jfs.canInsertToken(token)){
                jfs.insertSpaces(token, 1);
            }
        
        }
        
        /** insertNewLineBeforeKeyword such as else, catch, finally
         *  if getFormatNewlineBeforeBrace is true
         */
        private void insertNewLineBeforeToken(TokenItem token, JavaFormatSupport jfs){
            FormatTokenPosition elsePos = jfs.getPosition(token, 0);
            FormatTokenPosition imp = jfs.findImportant(elsePos,
                    null, true, true); // stop on line start
            if (imp != null && imp.getToken().getTokenContextPath()
                                    == jfs.getTokenContextPath()
            ) {
                // Insert new-line
                if (jfs.canInsertToken(token)) {
                    jfs.insertToken(token, jfs.getValidWhitespaceTokenID(),
                        jfs.getValidWhitespaceTokenContextPath(), "\n"); // NOI18N
                    jfs.removeLineEndWhitespace(imp);
                    // reindent newly created line
                    jfs.indentLine(elsePos);
                }
            }
        }
        
        protected void formatLine(JavaFormatSupport jfs, FormatTokenPosition pos) {
            TokenItem token = jfs.findLineStart(pos).getToken();
            while (token != null) {
/*                if (jfs.findLineEnd(jfs.getPosition(token, 0)).getToken() == token) {
                    break; // at line end
                }
 */

                if (token.getTokenContextPath() == jfs.getTokenContextPath()) {
                    switch (token.getTokenID().getNumericID()) {
                        case JavaTokenContext.ELSE_ID: //else
                            if (jfs.getFormatNewlineBeforeBrace()) {
                                // add a new line before else, if getFormatNewlineBeforeBrace
                                insertNewLineBeforeToken(token, jfs);
                            } else {
                                removeLineBeforeToken(token, jfs, true);
                            }
                            break;
                        case JavaTokenContext.CATCH_ID: //catch
                            if (jfs.getFormatNewlineBeforeBrace()) {
                                // add a new line before catch, if getFormatNewlineBeforeBrace
                                insertNewLineBeforeToken(token, jfs);
                            } else {
                                removeLineBeforeToken(token, jfs, true);
                            }
                            break;
                        case JavaTokenContext.FINALLY_ID: //finally
                            if (jfs.getFormatNewlineBeforeBrace()) {
                                // add a new line before finally, if getFormatNewlineBeforeBrace
                                insertNewLineBeforeToken(token, jfs);
                            } else {
                                removeLineBeforeToken(token, jfs, true);
                            }
                            break;
                        case JavaTokenContext.LBRACE_ID: // '{'
                            if (!jfs.isIndentOnly()) {
                            if (jfs.getFormatNewlineBeforeBrace()) {
                                FormatTokenPosition lbracePos = jfs.getPosition(token, 0);
                                // Look for first important token in backward direction
                                FormatTokenPosition imp = jfs.findImportant(lbracePos,
                                        null, true, true); // stop on line start
                                if (imp != null && imp.getToken().getTokenContextPath()
                                                        == jfs.getTokenContextPath()
                                ) {
                                    switch (imp.getToken().getTokenID().getNumericID()) {
                                        case JavaTokenContext.BLOCK_COMMENT_ID:
                                        case JavaTokenContext.LINE_COMMENT_ID:
                                            break; // comments are ignored

                                        case JavaTokenContext.RBRACKET_ID:
                                            break; // array initializtion "ttt [] {...}"
                                            
                                        case JavaTokenContext.COMMA_ID:
                                        case JavaTokenContext.EQ_ID:
                                        case JavaTokenContext.LBRACE_ID:
                                            // multi array initialization
                                            //        static int[][] CONVERT_TABLE= { {3,5},
                                            //            {1,2}, {2,3}, ...
                                            break;
                                            

                                        default:
                                            // Check whether it isn't a "{ }" case
                                            FormatTokenPosition next = jfs.findImportant(
                                                    lbracePos, null, true, false);
                                            if (next == null || next.getToken() == null
                                                || next.getToken().getTokenID() != JavaTokenContext.RBRACE
                                            ) {
                                                // Insert new-line
                                                if (jfs.canInsertToken(token)) {
                                                    jfs.insertToken(token, jfs.getValidWhitespaceTokenID(),
                                                        jfs.getValidWhitespaceTokenContextPath(), "\n"); // NOI18N
                                                    jfs.removeLineEndWhitespace(imp);
                                                    // bug fix: 10225 - reindent newly created line
                                                    jfs.indentLine(lbracePos);
                                                }

                                                //token = imp.getToken();
                                            }
                                            break;
                                    }
                                }

                            } else {
                                //remove line before token if applicable
                                FormatTokenPosition tokenPos = jfs.getPosition(token, 0);
                                FormatTokenPosition ftpos = jfs.findNonWhitespace(tokenPos, null, false, true);
                                if (ftpos != null){
                                    switch (ftpos.getToken().getTokenID().getNumericID()) {
                                        case JavaTokenContext.RPAREN_ID: // ) {
                                        case JavaTokenContext.IDENTIFIER_ID: // public class Hello {
                                        case JavaTokenContext.ELSE_ID:
                                        case JavaTokenContext.FINALLY_ID:
                                        case JavaTokenContext.TRY_ID:
                                            removeLineBeforeToken(token, jfs, false);
                                            break;
                                    }
                                }
                            }
                            } // !jfs.isIndentOnly()
                            break;

                        case JavaTokenContext.LPAREN_ID:
                            if (jfs.getFormatSpaceBeforeParenthesis()) {
                                TokenItem prevToken = token.getPrevious();
                                if (prevToken != null && 
                                    (prevToken.getTokenID() == JavaTokenContext.IDENTIFIER ||
                                    prevToken.getTokenID() == JavaTokenContext.THIS ||
                                    prevToken.getTokenID() == JavaTokenContext.SUPER) ) {
                                    if (jfs.canInsertToken(token)) {
                                        jfs.insertToken(token, jfs.getWhitespaceTokenID(),
                                            jfs.getWhitespaceTokenContextPath(), " "); // NOI18N
                                    }
                                }
                            } else {
                                // bugfix 9813: remove space before left parenthesis
                                TokenItem prevToken = token.getPrevious();
                                if (prevToken != null && prevToken.getTokenID() == JavaTokenContext.WHITESPACE &&
                                        prevToken.getImage().length() == 1) {
                                    TokenItem prevprevToken = prevToken.getPrevious();
                                    if (prevprevToken != null && 
                                        (prevprevToken.getTokenID() == JavaTokenContext.IDENTIFIER ||
                                        prevprevToken.getTokenID() == JavaTokenContext.THIS ||
                                        prevprevToken.getTokenID() == JavaTokenContext.SUPER) )
                                    {
                                        if (jfs.canRemoveToken(prevToken)) {
                                            jfs.removeToken(prevToken);
                                        }
                                    }
                                }
                                
                            }
                            break;
                    }
                }

                token = token.getNext();
            }
        }

    }

}
