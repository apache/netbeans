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

package org.netbeans.modules.editor.structure.formatting;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.ext.ExtFormatter;
import org.netbeans.editor.ext.ExtSyntaxSupport;
import org.openide.ErrorManager;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
@Deprecated // use TagBasedLexerFormatter instead
public abstract class TagBasedFormatter extends ExtFormatter  {
    
    /** Creates a new instance of TagBases */
    public TagBasedFormatter(Class kitClass) {
        super(kitClass);
        ErrorManager.getDefault().log(ErrorManager.WARNING, 
                "Class " + getClass().getName() + " is deprecated, use *IndentTask");
    }
    
    protected abstract ExtSyntaxSupport getSyntaxSupport(BaseDocument doc);
    protected abstract boolean isClosingTag(TokenItem token);
    protected abstract boolean isUnformattableToken(TokenItem token);
    protected abstract boolean isUnformattableTag(String tag);
    protected abstract boolean isOpeningTag(TokenItem token);
    protected abstract String extractTagName(TokenItem tknTag);
    protected abstract boolean areTagNamesEqual(String tagName1, String tagName2);
    protected abstract boolean isClosingTagRequired(BaseDocument doc, String tagName);
    protected abstract int getOpeningSymbolOffset(TokenItem tknTag);
    protected abstract TokenItem getTagTokenEndingAtPosition(BaseDocument doc, int position) throws BadLocationException;
    protected abstract int getTagEndOffset(TokenItem token);
    
    protected Writer extFormatterReformat(final BaseDocument doc, final int startOffset, final int endOffset,
            final boolean indentOnly) throws BadLocationException, IOException {
        return super.reformat(doc, startOffset, endOffset, indentOnly);
    }
    
    protected boolean isWSTag(TokenItem tag){
        char chars[] = tag.getImage().toCharArray();
        
        for (char c : chars){
            if (!Character.isWhitespace(c)){
                return false;
            }
        }
        
        return true;
    }
    
    protected int getIndentForTagParameter(BaseDocument doc, TokenItem tag) throws BadLocationException{
        int tagStartLine = LineDocumentUtils.getLineIndex(doc, tag.getOffset());
        TokenItem currentToken = tag.getNext();
        
        /*
         * Find the offset of the first attribute if it is specified on the same line as the opening of the tag
         * e.g. <tag   |attr=
         * 
         */
        while (currentToken != null && isWSTag(currentToken) && tagStartLine == LineDocumentUtils.getLineIndex(doc, currentToken.getOffset())){
            currentToken = currentToken.getNext();
        }
        
        if (tag != null && !isWSTag(currentToken) && tagStartLine == LineDocumentUtils.getLineIndex(doc, currentToken.getOffset())){
            return currentToken.getOffset() - Utilities.getRowIndent(doc, currentToken.getOffset()) - LineDocumentUtils.getLineStartOffset(doc, currentToken.getOffset());
        }
        
        return getShiftWidth(); // default;
    }
    
    @Override public Writer reformat(BaseDocument doc, int startOffset, int endOffset,
            boolean indentOnly) throws BadLocationException {
        
        if (!hasValidSyntaxSupport(doc)){
            return null;
        }
        
        LinkedList<TagIndentationData>unprocessedOpeningTags = new LinkedList<TagIndentationData>();
        List<TagIndentationData>matchedOpeningTags = new ArrayList<TagIndentationData>();
        doc.atomicLock();
        
        try{
            int lastLine = LineDocumentUtils.getLineIndex(doc, doc.getLength());
            int firstRefBlockLine = LineDocumentUtils.getLineIndex(doc, startOffset);
            int lastRefBlockLine = LineDocumentUtils.getLineIndex(doc, endOffset);
            int firstUnformattableLine = -1;
            
            boolean unformattableLines[] = new boolean[lastLine + 1];
            int indentsWithinTags[] = new int[lastLine + 1];
            
            ExtSyntaxSupport sup = getSyntaxSupport(doc);
            TokenItem token = sup.getTokenChain(0, doc.getLength() - 1);
            
            if (token != null){
                // calc line indents - pass 1
                do{
                    boolean isOpenTag = isOpeningTag(token);
                    boolean isCloseTag = isClosingTag(token);
                    
                    if (isOpenTag || isCloseTag){
                        
                        String tagName = extractTagName(token);
                        int tagEndOffset = getTagEndOffset(token);
                        
                        if (tagEndOffset == -1){
                            break; // incomplete closing tag
                        }
                        
                        int lastTagLine = LineDocumentUtils.getLineIndex(doc, tagEndOffset);
                        
                        if (isOpenTag){
                            
                            TagIndentationData tagData = new TagIndentationData(tagName, lastTagLine);
                            unprocessedOpeningTags.add(tagData);
                            
                            // format lines within tag
                            int firstTagLine = LineDocumentUtils.getLineIndex(doc, token.getOffset());
                            
                            if (firstTagLine < lastTagLine){ // performance!
                                int indentWithinTag = getIndentForTagParameter(doc, token);
                                
                                for (int i = firstTagLine + 1; i <= lastTagLine; i ++){
                                    indentsWithinTags[i] = indentWithinTag;
                                }
                                
                                // if there is only the closing symbol on the last line of tag do not indent it
                                TokenItem currentToken = token.getNext();
                                while (LineDocumentUtils.getLineIndex(doc, currentToken.getOffset()) < lastTagLine
                                        || isWSTag(currentToken)){
                                    
                                    currentToken = currentToken.getNext();
                                }
                                
                                if (currentToken.getOffset() == tagEndOffset){
                                    indentsWithinTags[lastTagLine] = 0;
                                }
                            }
                        } else {
                            // isCloseTag - find matching opening tag record
                            LinkedList<TagIndentationData>tagsToBeRemoved = new LinkedList<TagIndentationData>();
                            
                            while (!unprocessedOpeningTags.isEmpty()){
                                TagIndentationData processedTD = unprocessedOpeningTags.removeLast();
                                
                                if (areTagNamesEqual(tagName, processedTD.getTagName())){
                                    processedTD.setClosedOnLine(lastTagLine);
                                    matchedOpeningTags.add(processedTD);
                                    
                                    // mark all the stuff between unformattable tag as unformattable
                                    if (isUnformattableTag(tagName)){
                                        for (int i = lastTagLine - 1; i > processedTD.getLine(); i --){
                                            unformattableLines[i] = true;
                                        }
                                    }
                                    
                                    // forgetting preceding tags permanently
                                    tagsToBeRemoved.clear();
                                    break;
                                } else{
                                    tagsToBeRemoved.add(processedTD);
                                }
                            }
                            
                            // if matching opening tag was not found on the stack put all the tags back
                            unprocessedOpeningTags.addAll(tagsToBeRemoved);
                        }
                    }
                    
                    boolean wasPreviousTokenUnformattable = isUnformattableToken(token);
                    
                    if (wasPreviousTokenUnformattable && firstUnformattableLine == -1){
                        firstUnformattableLine = LineDocumentUtils.getLineIndex(doc, token.getOffset());
                    }
                    
                    token = token.getNext();
                    
                    // detect an end of unformattable block; mark it
                    if (firstUnformattableLine > -1
                            && (!wasPreviousTokenUnformattable || token == null)){
                        
                        int lastUnformattableLine = token == null ? lastLine :
                            LineDocumentUtils.getLineIndex(doc, token.getOffset() - 1);
                        
                        for (int i = firstUnformattableLine + 1; i < lastUnformattableLine; i ++){
                            unformattableLines[i] = true;
                        }
                        
                        firstUnformattableLine = -1;
                    }
                }
                while (token != null);
            }
            
            // calc line indents - pass 2
            // TODO: optimize it
            int indentLevels[] = new int[lastLine + 1];
            Arrays.fill(indentLevels, 0);
            
            for (TagIndentationData td : matchedOpeningTags){
                // increase indent from one line after the opening tag
                // up to one line before the closing tag
                
                for (int i = td.getLine() + 1; i <= td.getClosedOnLine() - 1; i ++){
                    indentLevels[i] ++;
                }
            }
            
            // when reformatting only a part of file
            // we need to take into account the local bias
            InitialIndentData initialIndentData = new InitialIndentData(doc, indentLevels,
                    indentsWithinTags, firstRefBlockLine, lastRefBlockLine);
            
            // apply line indents
            for (int line = firstRefBlockLine; line <= lastRefBlockLine; line ++){
                int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
                
                if (!unformattableLines[line] && initialIndentData.isEligibleToIndent(line)){
                    changeRowIndent(doc, lineStart, initialIndentData.getIndent(line));
                }
            }
        } finally{
            doc.atomicUnlock();
        }
        
        return null;
    }
    
    protected void enterPressed(JTextComponent txtComponent, int dotPos) throws BadLocationException {
        BaseDocument doc = Utilities.getDocument(txtComponent);
        int lineNumber = LineDocumentUtils.getLineIndex(doc, dotPos);
        int initialIndent = getInitialIndentFromPreviousLine(doc, lineNumber);
        int endOfPreviousLine = LineDocumentUtils.getPreviousNonWhitespace(doc, dotPos);
        endOfPreviousLine = endOfPreviousLine == -1 ? 0 : endOfPreviousLine;
        
        // workaround for \n passed from code completion to reformatter
        if (lineNumber == LineDocumentUtils.getLineIndex(doc, endOfPreviousLine)){
            return;
        }
        
        TokenItem tknOpeningTag = getTagTokenEndingAtPosition(doc, endOfPreviousLine);
        
        if (isOpeningTag(tknOpeningTag)){
            TokenItem tknClosingTag = getNextClosingTag(doc, dotPos + 1);
            
            if (tknClosingTag != null){
                TokenItem tknMatchingOpeningTag = getMatchingOpeningTag(tknClosingTag);
                
                if (tknMatchingOpeningTag != null
                        && tknMatchingOpeningTag.getOffset() == tknOpeningTag.getOffset()){
                    
                    int openingTagLine = LineDocumentUtils.getLineIndex(doc, tknOpeningTag.getOffset());
                    int closingTagLine = LineDocumentUtils.getLineIndex(doc, tknClosingTag.getOffset());
                    
                    if (closingTagLine == LineDocumentUtils.getLineIndex(doc, dotPos)){
                        
                        if (openingTagLine == closingTagLine - 1){
                            /* "smart enter"
                             * <t>|optional text</t>
                             */
                            Position closingTagPos = doc.createPosition(getOpeningSymbolOffset(tknClosingTag));
                            changeRowIndent(doc, dotPos, initialIndent + doc.getShiftWidth());
                            doc.insertString(closingTagPos.getOffset(), "\n", null); //NOI18N
                            int newCaretPos = closingTagPos.getOffset() - 1;
                            changeRowIndent(doc, closingTagPos.getOffset() + 1, initialIndent);
                            newCaretPos = LineDocumentUtils.getLineEndOffset(doc, newCaretPos);
                            txtComponent.setCaretPosition(newCaretPos);
                        } else{
                            /*  <t>
                             *
                             *  |</t>
                             */
                            changeRowIndent(doc, dotPos, initialIndent);
                        }
                    }
                }
                
                int indent = initialIndent;
                
                if (isClosingTagRequired(doc, extractTagName(tknOpeningTag))){
                    indent += doc.getShiftWidth();
                }
                
                changeRowIndent(doc, dotPos, indent);
            }
        } else{
            int indent = initialIndent;
            
            if (isJustBeforeClosingTag(doc, dotPos)){
                indent -= doc.getShiftWidth();
                indent = indent < 0 ? 0 : indent;
            }
            
            // preceeding token is not opening tag, keep same indentation
            changeRowIndent(doc, dotPos, indent);
        }
    }
    
    @Override public int[] getReformatBlock(JTextComponent target, String typedText) {
        BaseDocument doc = Utilities.getDocument(target);
        
        if (!hasValidSyntaxSupport(doc)){
            return null;
        }
        
        char lastChar = typedText.charAt(typedText.length() - 1);
        
        try{
            int dotPos = target.getCaret().getDot();
            
            if (lastChar == '>') {
                TokenItem tknPrecedingToken = getTagTokenEndingAtPosition(doc, dotPos - 1);
                
                if (isClosingTag(tknPrecedingToken)){
                    // the user has just entered a closing tag
                    // - reformat it unless matching opening tag is on the same line 
                    
                    TokenItem tknOpeningTag = getMatchingOpeningTag(tknPrecedingToken);
                    
                    if (tknOpeningTag != null){
                        int openingTagLine = LineDocumentUtils.getLineIndex(doc, tknOpeningTag.getOffset());
                        int closingTagSymbolLine = LineDocumentUtils.getLineIndex(doc, dotPos);
                        
                        if(openingTagLine != closingTagSymbolLine){
                            return new int[]{tknPrecedingToken.getOffset(), dotPos};
                        }
                    }
                }
            }
            
            else if(lastChar == '\n') {
                // just pressed enter
                enterPressed(target, dotPos);
            }
            
        } catch (Exception e){
            ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
        }
        
        return null;
    }
    
    protected TokenItem getMatchingOpeningTag(TokenItem tknClosingTag){
        String searchedTagName = extractTagName(tknClosingTag);
        TokenItem token = tknClosingTag.getPrevious();
        int balance = 0;
        
        while (token != null){
            if (areTagNamesEqual(searchedTagName, extractTagName(token))){
                if (isOpeningTag(token)){
                    if (balance == 0){
                        return token;
                    }
                    
                    balance --;
                } else if (isClosingTag(token)){
                    balance ++;
                }
            }
            
            token = token.getPrevious();
        }
        
        return null;
    }
    
    protected int getInitialIndentFromPreviousLine(final BaseDocument doc, final int line) throws BadLocationException {
        
        // get initial indent from the previous line
        int initialIndent = 0;
        
        if (line > 0){
            int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
            int previousNonWhiteLineEnd = LineDocumentUtils.getPreviousNonWhitespace(doc, lineStart);
            
            if (previousNonWhiteLineEnd > 0){
                initialIndent = Utilities.getRowIndent(doc, previousNonWhiteLineEnd);
            }
        }
        
        return initialIndent;
    }
    
    private int getInitialIndentFromNextLine(final BaseDocument doc, final int line) throws BadLocationException {
        
        // get initial indent from the next line
        int initialIndent = 0;
        
        int lineStart = Utilities.getRowStartFromLineOffset(doc, line);
        int lineEnd = LineDocumentUtils.getLineEndOffset(doc, lineStart);
        int nextNonWhiteLineStart = LineDocumentUtils.getNextNonWhitespace(doc, lineEnd);
        
        if (nextNonWhiteLineStart > 0){
            initialIndent = Utilities.getRowIndent(doc, nextNonWhiteLineStart, true);
        }
        
        return initialIndent;
    }
    
    private boolean hasValidSyntaxSupport(BaseDocument doc){
        ExtSyntaxSupport sup = getSyntaxSupport(doc);
        
        if (sup == null){
            ErrorManager.getDefault().log(ErrorManager.WARNING,
                    "TagBasedFormatter: failed to retrieve SyntaxSupport for document;" + //NOI18N
                    " probably attempt to use incompatible indentation engine"); //NOI18N
            
            return false;
        }
        
        return true;
    }

    protected static int getNumberOfLines(BaseDocument doc) throws BadLocationException{
        return LineDocumentUtils.getLineIndex(doc, doc.getLength() - 1) + 1;
    }
    
    protected TokenItem getNextClosingTag(BaseDocument doc, int offset) throws BadLocationException{
        ExtSyntaxSupport sup = getSyntaxSupport(doc);
        TokenItem token = sup.getTokenChain(offset, offset + 1);
        
        while (token != null){
            if (isClosingTag(token)){
                return token;
            }
            
            token = token.getNext();
        }
        
        return null;
    }

    protected boolean isJustBeforeClosingTag(BaseDocument doc, int pos) throws BadLocationException {
        ExtSyntaxSupport sup = getSyntaxSupport(doc);
        TokenItem tknTag = sup.getTokenChain(pos, pos + 1);
        
        if (isClosingTag(tknTag)){
            return true;
        }
        
        return false;
    }
    
    protected class InitialIndentData{
        private final int indentLevelBias;
        private final int indentBias;
        private final int indentLevels[];
        private final int indentsWithinTags[];
        private BaseDocument doc;
        
        public InitialIndentData(BaseDocument doc, int indentLevels[], int indentsWithinTags[],
                int firstRefBlockLine, int lastRefBlockLine) throws BadLocationException{
            
            int initialIndent = getInitialIndentFromPreviousLine(doc, firstRefBlockLine);
            int indentLevelBiasFromTheTop = initialIndent / doc.getShiftWidth() - (firstRefBlockLine > 0 ? indentLevels[firstRefBlockLine - 1] : 0);
            
            int initialIndentFromTheBottom = getInitialIndentFromNextLine(doc, lastRefBlockLine);
            int indentLevelBiasFromTheBottom = initialIndentFromTheBottom / doc.getShiftWidth() - (lastRefBlockLine < getNumberOfLines(doc) - 1 ? indentLevels[lastRefBlockLine + 1] : 0);
            
            if (indentLevelBiasFromTheBottom > indentLevelBiasFromTheTop){
                indentLevelBias = indentLevelBiasFromTheBottom;
                initialIndent = initialIndentFromTheBottom;
            }
            else{
                indentLevelBias = indentLevelBiasFromTheTop;
            }
            
            indentBias = initialIndent % doc.getShiftWidth();
            this.indentLevels = indentLevels;
            this.indentsWithinTags = indentsWithinTags;
            this.doc = doc;
        }
        
        public boolean isEligibleToIndent(int line){
            return getActualIndentLevel(line) >= 0;
        }
        
        public int getIndent(int line){
            return indentBias + indentsWithinTags[line] + getActualIndentLevel(line) * doc.getShiftWidth();
        }
        
        private int getActualIndentLevel(int line){
            return indentLevels[line] + indentLevelBias;
        }
    }
    
    protected static class TagIndentationData{
        private final String tagName;
        private final int line;
        private int closedOnLine;
        
        public TagIndentationData(String tagName, int line){
            this.tagName = tagName;
            this.line = line;
        }
        
        public String getTagName() {
            return tagName;
        }
        
        public int getLine() {
            return line;
        }
        
        public int getClosedOnLine() {
            return closedOnLine;
        }
        
        public void setClosedOnLine(int closedOnLine) {
            this.closedOnLine = closedOnLine;
        }
    }
}
