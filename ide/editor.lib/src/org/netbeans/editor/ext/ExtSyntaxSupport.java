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

package org.netbeans.editor.ext;

import java.util.Map;
import java.util.HashMap;
import javax.swing.event.DocumentListener;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.SyntaxSupport;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.TokenProcessor;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.FinderFactory;
import org.netbeans.editor.TextBatchProcessor;
import org.netbeans.editor.Analyzer;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtSyntaxSupport extends SyntaxSupport {

    // used in ExtKit
    
    /** Schedule content update making completion visible. */
    public static final int COMPLETION_POPUP = 0;
    /** Cancel request without changing completion visibility. */
    public static final int COMPLETION_CANCEL = 1;
    /** Update content immediatelly if it's currently visible. */
    public static final int COMPLETION_REFRESH = 2;
    /** Schedule content update if it's currently visible. */
    public static final int COMPLETION_POST_REFRESH = 3;
    /** Hide completion. */
    public static final int COMPLETION_HIDE = 4;

    private static final TokenID[] EMPTY_TOKEN_ID_ARRAY = new TokenID[0];

    /** Listens for the changes on the document. Children can override
    * the documentModified() method to perform some processing.
    */
    private DocumentListener docL;

    /** Map holding the [position, local-variable-map] pairs */
    private HashMap localVarMaps = new HashMap();

    /** Map holding the [position, global-variable-map] pairs */
    private HashMap globalVarMaps = new HashMap();

    public ExtSyntaxSupport(BaseDocument doc) {
        super(doc);

        // Create listener to listen on document changes
        docL = new DocumentListener() {
                   public void insertUpdate(DocumentEvent evt) {
                       documentModified(evt);
                   }

                   public void removeUpdate(DocumentEvent evt) {
                       documentModified(evt);
                   }

                   public void changedUpdate(DocumentEvent evt) {
                   }
               };
        getDocument().addDocumentListener(docL);
    }

    /** Get the chain of the tokens for the given block of text.
    * The returned chain of token-items reflects the tokens
    * as they occur in the text and therefore the first token
    * can start at the slightly lower position than the requested one.
    * The chain itself can be extended automatically when
    * reaching the first chain item and calling <code>getPrevious()</code>
    * on it. Another chunk of the tokens will be parsed and
    * the head of the chain will be extended. However this happens
    * only in case there was no modification performed to the document
    * between the creation of the chain and this moment. Otherwise
    * this call throws <code>IllegalStateException</code>.
    * 
    * @param startOffset starting position of the block
    * @param endOffset ending position of the block
    * @return the first item of the token-item chain or null if there are
    *  no tokens in the given area or the area is so small that it lays
    *  inside one token. To prevent this provide the area that spans a new-line.
    */
    public TokenItem getTokenChain(int startOffset, int endOffset)
    throws BadLocationException {

        if (startOffset < 0) {
	    throw new IllegalArgumentException("startOffset=" + startOffset + " < 0"); // NOI18N
	}
	if (startOffset > endOffset) {
	    throw new IllegalArgumentException("startOffset=" + startOffset // NOI18N
		    + " > endOffset=" + endOffset); // NOI18N
	}
        TokenItem chain = null;
        BaseDocument doc = getDocument();
        doc.readLock();
        try {
            int docLen = doc.getLength();
            endOffset = Math.min(endOffset, docLen);
            if( startOffset < docLen ) {
                TokenItemTP tp = new TokenItemTP();            
                tp.targetOffset = endOffset;
                tokenizeText(tp, startOffset, endOffset, false);
                chain = tp.getTokenChain();
            }
        } finally {
            doc.readUnlock();
        }

        return chain;
    }

    /** Called when the document was modified by either the insert or removal.
    * @param evt event received with the modification notification. getType()
    *   can be used to obtain the type of the event.
    */
    protected void documentModified(DocumentEvent evt) {
        // Invalidate variable maps
        if (localVarMaps.size() > 0)
            localVarMaps.clear();
        if (globalVarMaps.size() > 0)
            globalVarMaps.clear();
    }

    /** Get the bracket finder that will search for the matching bracket
     * or null if the bracket character doesn't belong to bracket
     * characters.
     */
    protected BracketFinder getMatchingBracketFinder(char bracketChar) {
        BracketFinder bf = new BracketFinder(bracketChar);
        if (bf.moveCount == 0) { // not valid bracket char
            bf = null;
        }

        return bf;
    }

    /** Find matching bracket or more generally block
     * that matches with the current position.
     * @param offset position of the starting bracket
     * @param simpleSearch whether the search should skip comment and possibly other areas.
     *   This can be useful when the speed is critical, because the simple
     *   search is faster.
     * @return array of integers containing starting and ending position
     *   of the block in the document. Null is returned if there's
     *   no matching block.
     */
    public int[] findMatchingBlock(int offset, boolean simpleSearch)
    throws BadLocationException {
        char bracketChar = getDocument().getChars(offset, 1)[0];
        int foundPos = -1;

        final BracketFinder bf = getMatchingBracketFinder(bracketChar);

        if (bf != null) { // valid finder
            if (!simpleSearch) {
                TokenID tokenID = getTokenID(offset);
                TokenID[] bst = getBracketSkipTokens();
                for (int i = bst.length - 1; i >= 0; i--) {
                    if (tokenID == bst[i]) {
                        simpleSearch = true; // turn to simple search
                        break;
                    }
                }
            }

            if (simpleSearch) { // don't exclude comments etc.
                if (bf.isForward()) {
                    foundPos = getDocument().find(bf, offset, -1);
                } else {
                    foundPos = getDocument().find(bf, offset + 1, 0);
                }

            } else { // exclude comments etc. from the search
                TextBatchProcessor tbp = new TextBatchProcessor() {
                     public int processTextBatch(BaseDocument doc, int startPos, int endPos,
                                                 boolean lastBatch) {
                         try {
                             int[] blks = getTokenBlocks(startPos, endPos, getBracketSkipTokens());
                             return findOutsideBlocks(bf, startPos, endPos, blks);
                         } catch (BadLocationException e) {
                             return -1;
                         }
                     }
                };

                if (bf.isForward()) {
                    foundPos = getDocument().processText(tbp, offset, -1);
                } else {
                    foundPos = getDocument().processText(tbp, offset + 1, 0);
                }
            }
        }

        return (foundPos != -1) ? new int[] { foundPos, foundPos + 1 } : null;
    }

    /** Get the array of token IDs that should be skipped when
    * searching for matching bracket. It usually includes comments
    * and character and string constants. Returns empty array by default.
    */
    protected TokenID[] getBracketSkipTokens() {
        return EMPTY_TOKEN_ID_ARRAY;
    }

    /** Gets the token-id of the token at the given position.
    * @param offset position at which the token should be returned
    * @return token-id of the token at the requested position. If there's no more
    *   tokens in the text, the <code>Syntax.INVALID</code> is returned.
    */
    public TokenID getTokenID(int offset) throws BadLocationException {
        FirstTokenTP fttp = new FirstTokenTP();
        tokenizeText(fttp, offset, getDocument().getLength(), true);
        return fttp.getTokenID();
    }

    /** Is the identifier at the position a function call?
    * It first checks whether there is a identifier under
    * the cursor and then it searches for the function call
    * character - usually '('. Note: Java 1.5 annotations are not
    * taken as function calls.
    * @param identifierBlock int[2] block delimiting the identifier
    * @return int[2] block or null if there's no function call
    */
    public int[] getFunctionBlock(int[] identifierBlock) throws BadLocationException {
        if (identifierBlock != null) {
            int nwPos = Utilities.getFirstNonWhiteFwd(getDocument(), identifierBlock[1]);
            if ((nwPos >= 0) && (getDocument().getChars(nwPos, 1)[0] == '(')) {
                return new int[] { identifierBlock[0], nwPos + 1 };
            }
        }
        return null;
    }

    public int[] getFunctionBlock(int offset) throws BadLocationException {
        return getFunctionBlock(Utilities.getIdentifierBlock(getDocument(), offset));
    }

    public boolean isWhitespaceToken(TokenID tokenID,
                                     char[] buffer, int offset, int tokenLength) {
        return Analyzer.isWhitespace(buffer, offset, tokenLength);
    }

    public boolean isCommentOrWhitespace(int startPos, int endPos)
    throws BadLocationException {
        CommentOrWhitespaceTP tp= new CommentOrWhitespaceTP(getCommentTokens());
        tokenizeText(tp, startPos, endPos, true);
        return !tp.nonEmpty;
    }

    /** Gets the last non-blank and non-comment character on the given line.
    */
    public int getRowLastValidChar(int offset)
    throws BadLocationException {
        return Utilities.getRowLastNonWhite(getDocument(), offset);
    }

    /** Does the line contain some valid code besides of possible white space
    * and comments?
    */
    public boolean isRowValid(int offset)
    throws BadLocationException {
        return LineDocumentUtils.isLineWhitespace(getDocument(), offset);
    }

    /** Get the array of token IDs that denote the comments.
    * Returns empty array by default.
    */
    public TokenID[] getCommentTokens() {
        return EMPTY_TOKEN_ID_ARRAY;
    }

    /** Get the blocks consisting of comments in a specified document area.
    * @param startPos starting position of the searched document area
    * @param endPos ending position of the searched document area
    */
    public int[] getCommentBlocks(int startPos, int endPos)
    throws BadLocationException {
        return getTokenBlocks(startPos, endPos, getCommentTokens());
    }

    /** Find the type of the variable. The default behavior is to first
    * search for the local variable declaration and then possibly for
    * the global declaration and if the declaration position is found
    * to get the first word on that position.
    * @return it returns Object to enable the custom implementations
    *   to return the appropriate instances.
    */
    public Object findType(String varName, int varPos) {
        Object type = null;
        Map varMap = getLocalVariableMap(varPos); // first try local vars
        if (varMap != null) {
            type = varMap.get(varName);
        }

        if (type == null) {
            varMap = getGlobalVariableMap(varPos); // try global vars
            if (varMap != null) {
                type = varMap.get(varName);
            }
        }

        return type;
    }

    public Map getLocalVariableMap(int offset) {
        Integer posI = Integer.valueOf(offset);
        Map varMap = (Map)localVarMaps.get(posI);
        if (varMap == null) {
            varMap = buildLocalVariableMap(offset);
            localVarMaps.put(posI, varMap);
        }
        return varMap;
    }

    protected Map buildLocalVariableMap(int offset) {
        int methodStartPos = getMethodStartPosition(offset);
        if (methodStartPos >= 0 && methodStartPos < offset) {
            VariableMapTokenProcessor vmtp = createVariableMapTokenProcessor(methodStartPos, offset);
            try {
                tokenizeText(vmtp, methodStartPos, offset, true);
                return vmtp.getVariableMap();
            } catch (BadLocationException e) {
                // will default null
            }
        }
        return null;
    }

    public Map getGlobalVariableMap(int offset) {
        Integer posI = Integer.valueOf(offset);
        Map varMap = (Map)globalVarMaps.get(posI);
        if (varMap == null) {
            varMap = buildGlobalVariableMap(offset);
            globalVarMaps.put(posI, varMap);
        }
        return varMap;
    }

    protected Map buildGlobalVariableMap(int offset) {
        int docLen = getDocument().getLength();
        VariableMapTokenProcessor vmtp = createVariableMapTokenProcessor(0, docLen);
        if (vmtp != null) {
            try {
                tokenizeText(vmtp, 0, docLen, true);
                return vmtp.getVariableMap();
            } catch (BadLocationException e) {
                // will default null
            }
        }
        return null;
    }

    /** Get the start position of the method or the area
    * where the declaration can start.
    */
    protected int getMethodStartPosition(int offset) {
        return 0; // return begining of the document by default
    }

    /** Find either the local or global declaration position. First
    * try the local declaration and if it doesn't succeed, then
    * try the global declaration.
    */
    public int findDeclarationPosition(String varName, int varPos) {
        int offset = findLocalDeclarationPosition(varName, varPos);
        if (offset < 0) {
            offset = findGlobalDeclarationPosition(varName, varPos);
        }
        return offset;
    }

    public int findLocalDeclarationPosition(String varName, int varPos) {
        int methodStartPos = getMethodStartPosition(varPos);
        if (methodStartPos >= 0 && methodStartPos < varPos) {
            return findDeclarationPositionImpl(varName, methodStartPos, varPos);
        }
        return -1;
    }

    /** Get the position of the global declaration of a given variable.
    * By default it's implemented to use the same token processor as for the local
    * variables but the whole file is searched.
    */
    public int findGlobalDeclarationPosition(String varName, int varPos) {
        return findDeclarationPositionImpl(varName, 0, getDocument().getLength());
    }

    private int findDeclarationPositionImpl(String varName, int startPos, int endPos) {
        DeclarationTokenProcessor dtp = createDeclarationTokenProcessor(varName, startPos, endPos);
        if (dtp != null) {
            try {
                tokenizeText(dtp, startPos, endPos, true);
                return dtp.getDeclarationPosition();
            } catch (BadLocationException e) {
                // will default to -1
            }
        }
        return -1;
    }

    protected DeclarationTokenProcessor createDeclarationTokenProcessor(
        String varName, int startPos, int endPos) {
        return null;
    }

    protected VariableMapTokenProcessor createVariableMapTokenProcessor(
        int startPos, int endPos) {
        return null;
    }

    
    /** Check and possibly popup, hide or refresh the completion */
    public int checkCompletion(JTextComponent target, String typedText, boolean visible ) {
        return visible ? COMPLETION_HIDE : COMPLETION_CANCEL;
    }

    /** Token processor extended to get declaration position
    * of the given variable.
    */
    public interface DeclarationTokenProcessor extends TokenProcessor {

        /** Get the declaration position. */
        public int getDeclarationPosition();

    }

    public interface VariableMapTokenProcessor extends TokenProcessor {

        /** Get the map that contains the pairs [variable-name, variable-type]. */
        public Map getVariableMap();

    }

    /** Finder for the matching bracket. It gets the original bracket char
    * and searches for the appropriate matching bracket character.
    */
    public class BracketFinder extends FinderFactory.GenericFinder {

        /** Original bracket char */
        protected char bracketChar;

        /** Matching bracket char */
        protected char matchChar;

        /** Depth of original brackets */
        private int depth;

        /** Will it be a forward finder +1 or backward finder -1 or 0 when
        * the given character is not bracket character.
        */
        protected int moveCount;

        /**
        * @param bracketChar bracket char
        */
        public BracketFinder(char bracketChar) {
            this.bracketChar = bracketChar;

            updateStatus();

            forward = (moveCount > 0);
        }

        /** Check whether the bracketChar really contains
         * the bracket character. If so assign the matchChar
         * and moveCount variables.
         */
        protected boolean updateStatus() {
            boolean valid = true;
            switch (bracketChar) {
                case '(':
                    matchChar = ')';
                    moveCount = +1;
                    break;
                case ')':
                    matchChar = '(';
                    moveCount = -1;
                    break;
                case '{':
                    matchChar = '}';
                    moveCount = +1;
                    break;
                case '}':
                    matchChar = '{';
                    moveCount = -1;
                    break;
                case '[':
                    matchChar = ']';
                    moveCount = +1;
                    break;
                case ']':
                    matchChar = '[';
                    moveCount = -1;
                    break;
                case '<':
                    matchChar = '>';
                    moveCount = +1;
                    break;
                case '>':
                    matchChar = '<';
                    moveCount = -1;
                    break;
                default:
                    valid = false;
            }
            return valid;
        }

        protected int scan(char ch, boolean lastChar) {
            if (ch == bracketChar) {
                depth++;
            } else if (ch == matchChar) {
                if (--depth == 0) {
                    found = true;
                    return 0;
                }
            }
            return moveCount;
        }

    }

    /** Create token-items */
    final class TokenItemTP implements TokenProcessor {

        private Item firstItem;

        private Item lastItem;

        private int fwdBatchLineCnt;
        private int bwdBatchLineCnt;

        private char[] buffer;

        private int bufferStartPos;
        
        /** Target position corresponding to the begining of the token
         * that is already chained if searching for backward tokens,
         * or, the last token that should be scanned if searching
         * in forward direction.
         */
        int targetOffset;


        TokenItemTP() {
            fwdBatchLineCnt = bwdBatchLineCnt = ((Integer)getDocument().getProperty(BaseDocument.LINE_BATCH_SIZE)).intValue();
        }

        public TokenItem getTokenChain() {
            return firstItem;
        }

        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
        int tokenBufferOffset, int tokenLength) {
            if (bufferStartPos + tokenBufferOffset >= targetOffset) { // stop scanning
                return false;
            }

            lastItem = new Item(tokenID, tokenContextPath,
                    bufferStartPos + tokenBufferOffset,
                    new String(buffer, tokenBufferOffset, tokenLength), lastItem
            );
            
            if (firstItem == null) { // not yet assigned
                firstItem = lastItem;
            }

            return true;
        }

        public int eot(int offset) {
            return ((Integer)getDocument().getProperty("mark-distance")).intValue(); //NOI18N, same constant as in the BaseDocument
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
            bufferStartPos = startPos - offset;
        }

        Item getNextChunk(Item i) {
            BaseDocument doc = getDocument();
            int itemEndPos = i.getOffset() + i.getImage().length();
            int docLen = doc.getLength();
            if (itemEndPos == docLen) {
                return null;
            }

            int endPos;
            try {
                endPos = Utilities.getRowStart(doc, itemEndPos, fwdBatchLineCnt);
            } catch (BadLocationException e) {
                return null;
            }

            if (endPos == -1) { // past end of doc
                endPos = docLen;
            }
            fwdBatchLineCnt *= 2; // larger batch in next call


            Item nextChunkHead = null;
            Item fit = firstItem;
            Item lit = lastItem;
            try {
                // Simulate initial conditions
                firstItem = null;
                lastItem = null;
                targetOffset = endPos;

                tokenizeText(this, itemEndPos, endPos, false);
                nextChunkHead = firstItem;

            } catch (BadLocationException e) {
            } finally {
                // Link previous last with the current first
                if (firstItem != null) {
                    lit.next = firstItem;
                    firstItem.previous = lit;
                }

                firstItem = fit;
                if (lastItem == null) { // restore in case of no token or crash
                    lastItem = lit;
                }
            }

            return nextChunkHead;
        }

        Item getPreviousChunk(Item i) {
            BaseDocument doc = getDocument();
            int itemStartPos = i.getOffset();
            if (itemStartPos == 0) {
                return null;
            }

            int startPos;
            try {
                startPos = Utilities.getRowStart(doc, itemStartPos, -bwdBatchLineCnt);
            } catch (BadLocationException e) {
                return null;
            }

            if (startPos == -1) { // before begining of doc
                startPos = 0;
            }
            bwdBatchLineCnt *= 2;

            Item previousChunkLast = null;
            Item fit = firstItem;
            Item lit = lastItem;
            try {
                // Simulate initial conditions
                firstItem = null;
                lastItem = null;
                targetOffset = itemStartPos;

                tokenizeText(this, startPos, itemStartPos, false);
                previousChunkLast = lastItem;

            } catch (BadLocationException e) {
            } finally {
                // Link previous last
                if (lastItem != null) {
                    fit.previous = lastItem;
                    lastItem.next = fit;
                }

                lastItem = lit;
                if (firstItem == null) { // restore in case of no token or crash
                    firstItem = fit;
                }
            }

            return previousChunkLast;
        }

        final class Item extends TokenItem.AbstractItem {

            Item previous;

            TokenItem next;

            Item(TokenID tokenID, TokenContextPath tokenContextPath,
            int offset, String image, Item previous) {
                super(tokenID, tokenContextPath, offset, image);
                if (previous != null) {
                    this.previous = previous;
                    previous.next = this;
                }
            }

            public TokenItem getNext() {
                if (next == null) {
                    next = getNextChunk(this);
                }
                return next;
            }

            public TokenItem getPrevious() {
                if (previous == null) {
                    previous = getPreviousChunk(this);
                }
                return previous;
            }

        }

    }

    /** Token processor that matches either the comments or whitespace */
    class CommentOrWhitespaceTP implements TokenProcessor {

        private char[] buffer;

        private TokenID[] commentTokens;

        boolean nonEmpty;

        CommentOrWhitespaceTP(TokenID[] commentTokens) {
            this.commentTokens = commentTokens;
        }

        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
        int offset, int tokenLength) {
            for (int i = 0; i < commentTokens.length; i++) {
                if (tokenID == commentTokens[i]) {
                    return true; // comment token found
                }
            }
            boolean nonWS = isWhitespaceToken(tokenID, buffer, offset, tokenLength);
            if (nonWS) {
                nonEmpty = true;
            }
            return nonWS;
        }

        public int eot(int offset) {
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
            this.buffer = buffer;
        }

    }

    static class FirstTokenTP implements TokenProcessor {

        private TokenID tokenID;

        public TokenID getTokenID() {
            return tokenID;
        }

        public boolean token(TokenID tokenID, TokenContextPath tokenContextPath,
        int offset, int tokenLen) {
            this.tokenID = tokenID;
            return false; // no more tokens
        }

        public int eot(int offset) {
            return 0;
        }

        public void nextBuffer(char[] buffer, int offset, int len,
                               int startPos, int preScan, boolean lastBuffer) {
        }

    }

}
