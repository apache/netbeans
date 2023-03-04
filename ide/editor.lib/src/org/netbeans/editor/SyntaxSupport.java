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

package org.netbeans.editor;

import java.util.HashMap;
import javax.swing.text.BadLocationException;
import javax.swing.text.Segment;

/**
* Support methods for syntax analyzes
*
* @author Miloslav Metelka
* @version 1.00
*/

public class SyntaxSupport {

    private static final int[] EMPTY_INT_ARRAY = new int[0];

    private static final int MATCH_ARRAY_CACHE_SIZE = 3;

    private HashMap supMap;

    /** Document to work with */
    private BaseDocument doc;

    /** Whether all the token-ids this class deals with have valid
    * numeric-ids. It's not necessary to set this flag, however
    * it presents an optimization in testing whether a token
    * belongs to some group of tokens or not. The testing whether
    * the particular token belongs to some group is improved
    * by creating a boolean array in which the numeric-ids serve
    * as the array indexes.
    */
    protected boolean tokenNumericIDsValid;

    private int[] tokenBlocks = EMPTY_INT_ARRAY;

    private TokenID[][] lastTokenIDArrays = new TokenID[MATCH_ARRAY_CACHE_SIZE][];

    private boolean[][] lastMatchArrays = new boolean[MATCH_ARRAY_CACHE_SIZE][];

    public SyntaxSupport(BaseDocument doc) {
        this.doc = doc;

    }

    /** Getter for the document that this support is associated to. */
    public final BaseDocument getDocument() {
        return doc;
    }

    /** Get the support that fits the requested support class
    * in the best way. The value returned will be either instance
    * of the requested class or its descendant or it will be null.
    *
    * @param syntaxSupportClass returned value will be instance of this
    *  class (or its descendant) or it will be null
    * @return instance of syntaxSupportClass (or its descendant) or null
    *  if there's no fitting support.
    */
    public synchronized SyntaxSupport get(Class syntaxSupportClass) {
        if (supMap == null) {
            supMap = new HashMap(11);
        }

        SyntaxSupport sup = (SyntaxSupport)supMap.get(syntaxSupportClass);
        if (sup == null) {
            sup = createSyntaxSupport(syntaxSupportClass);
            supMap.put(syntaxSupportClass, sup);
        }

        return sup;
    }

    protected SyntaxSupport createSyntaxSupport(Class syntaxSupportClass) {
        if (syntaxSupportClass.isInstance(this)) {
            return this;
        }
        return null;
    }


    /** Get the array of booleans with trues at indexes retrieved
    * as numeric-ids from the token-id array.
    */
    private boolean[] getMatchArray(TokenID[] tokenIDArray) {
        boolean[] matchArray = null;
        int ind;
        for (ind = 0; ind < MATCH_ARRAY_CACHE_SIZE; ind++) {
            // Test only on array equality, not Arrays.equals(Ob1[], Ob2[])
            // Supposing they will be static
            if (tokenIDArray == lastTokenIDArrays[ind]) {
                matchArray = lastMatchArrays[ind];
                break;
            }
        }

        if (matchArray == null) { // not found in cache
            int maxTokenNumericID = -1;
            if (tokenIDArray != null) {
                for (int i = 0; i < tokenIDArray.length; i++) {
                    if (tokenIDArray[i].getNumericID() > maxTokenNumericID) {
                        maxTokenNumericID = tokenIDArray[i].getNumericID();
                    }
                }
            }

            matchArray = new boolean[maxTokenNumericID + 1];
            for (int i = 0; i < tokenIDArray.length; i++) {
                matchArray[tokenIDArray[i].getNumericID()] = true;
            }
        }

        if (ind > 0) {
            ind = Math.min(ind, MATCH_ARRAY_CACHE_SIZE - 1);
            System.arraycopy(lastTokenIDArrays, 0, lastTokenIDArrays, 1, ind);
            System.arraycopy(lastMatchArrays, 0, lastMatchArrays, 1, ind);
            lastTokenIDArrays[0] = tokenIDArray;
            lastMatchArrays[0] = matchArray;
        }

        return matchArray;
    }

    /** Get position pairs covering the blocks that include only the tokens
    * from the given token array. Although the startPos can be greater than
    * endPos, the blocks are always returned in the natural order.
    * @param doc document to work with
    * @param startPos starting position of the requested document area.
    * @param endPos ending position of the requested document area
    * @param tokenIDArray the array of the token IDs that should be in the blocks.
    */
    public int[] getTokenBlocks(int startPos, int endPos,
            TokenID[] tokenIDArray) throws BadLocationException {
        doc.readLock();
        try {
            synchronized (this) {
                boolean matchArray[] = tokenNumericIDsValid ? getMatchArray(tokenIDArray) : null;
                int blkInd = 0;
                if (startPos > endPos) { // swap
                    int tmp = startPos;
                    startPos = endPos;
                    endPos = tmp;
                }

                SyntaxSeg.Slot slot = SyntaxSeg.getFreeSlot();
                Syntax syntax = doc.getFreeSyntax();
                try {
                    doc.prepareSyntax(slot, syntax, startPos, endPos - startPos, true, false);

                    int preScan = syntax.getPreScan();
                    int pos = startPos - preScan;
                    int blkStart = -1;

                    boolean cont = true;
                    while (cont) {
                        TokenID tokenID = syntax.nextToken();
                        if (tokenID == null) {
                            cont = false;
                        } else {
                            // Test whether token-id belongs to the token-array
                            boolean matches = (tokenID != null)
                                && !(pos + syntax.getTokenLength() <= startPos);
                            if (matches) {
                                if (matchArray != null) {
                                    int numID = tokenID.getNumericID();
                                    matches = (numID < matchArray.length && matchArray[numID]);
                                } else { // doesn't support numeric-ids
                                    matches = false;
                                    for (int i = 0; i < tokenIDArray.length; i++) {
                                        if (tokenID == tokenIDArray[i]) {
                                            matches = true;
                                            break;
                                        }
                                    }
                                }
                            }

                            if (matches) {
                                if (blkStart >= 0) {
                                    // still in token block
                                } else {
                                    blkStart = Math.max(pos, startPos);
                                }
                            } else { // not searched token
                                if (blkStart >= 0) {
                                    tokenBlocks = addTokenBlock(tokenBlocks, blkInd, blkStart, pos);
                                    blkInd += 2;
                                    blkStart = -1;
                                } else {
                                    // still not in block
                                }
                            }
                            pos += syntax.getTokenLength();
                        }
                    }

                    if (blkStart >= 0) { // was in comment
                        tokenBlocks = addTokenBlock(tokenBlocks, blkInd, blkStart, endPos);
                        blkInd += 2;
                    }

                } finally {
                    doc.releaseSyntax(syntax);
                    SyntaxSeg.releaseSlot(slot);
                }

                int[] ret = new int[blkInd];
                System.arraycopy(tokenBlocks, 0, ret, 0, blkInd);
                return ret;
            }
        } finally {
            doc.readUnlock();
        }
    }

    private int[] addTokenBlock(int[] blks, int blkInd, int blkStartPos, int blkEndPos) {
        if (blks.length < blkInd + 2) {
            int[] tmp = new int[Math.max(2, blks.length * 2)];
            System.arraycopy(blks, 0, tmp, 0, blkInd);
            blks = tmp;
        }

        blks[blkInd++] = blkStartPos;
        blks[blkInd] = blkEndPos;
        return blks;
    }

    public int findInsideBlocks(Finder finder,
                                int startPos, int endPos, int[] blocks) throws BadLocationException {
        boolean fwd = (startPos <= endPos);

        if (fwd) {
            for (int i = 0; i < blocks.length; i += 2) {
                int pos = doc.find(finder, blocks[i], blocks[i + 1]);
                if (pos >= 0) {
                    return pos;
                }
            }
        } else { // find backward
            for (int i = blocks.length - 2; i >= 0; i -= 2) {
                int pos = doc.find(finder, blocks[i + 1], blocks[i]);
                if (pos >= 0) {
                    return pos;
                }
            }
        }
        return -1;
    }

    public int findOutsideBlocks(Finder finder,
                                 int startPos, int endPos, int[] blocks) throws BadLocationException {
        boolean fwd = (startPos <= endPos);

        if (fwd) {
            int pos = doc.find(finder, startPos, (blocks.length > 0) ? blocks[0] : endPos);
            if (pos >= 0) {
                return pos;
            }

            int ind = 2;
            while (ind <= blocks.length) {
                pos = doc.find(finder, blocks[ind - 1], (ind >= blocks.length) ? endPos : blocks[ind]);
                if (pos >= 0) {
                    return pos;
                }
                ind += 2;
            }
        } else { // find backward
            int pos = doc.find(finder, startPos, (blocks.length > 0) ? blocks[blocks.length - 1] : endPos);
            if (pos >= 0) {
                return pos;
            }

            int ind = blocks.length - 2;
            while (ind >= 0) {
                pos = doc.find(finder, blocks[ind], (ind == 0) ? endPos : blocks[ind - 1]);
                if (pos >= 0) {
                    return pos;
                }
                ind -= 2;
            }
        }
        return -1;
    }

    /** Initialize the syntax so it's ready to scan the given area.
    * @param syntax lexical analyzer to prepare
    * @param startPos starting position of the scanning
    * @param endPos ending position of the scanning
    * @param forceLastBuffer force the syntax to think that the scanned area is the last
    *  in the document. This is useful for forcing the syntax to process all the characters
    *  in the given area.
    * @param forceNotLastBuffer force the syntax to think that the scanned area is NOT
    *  the last buffer in the document. This is useful when the syntax will continue
    *  scanning on another buffer.
    */
    public void initSyntax(Syntax syntax, int startPos, int endPos,
    boolean forceLastBuffer, boolean forceNotLastBuffer)
    throws BadLocationException {
        doc.readLock();
        try {
            Segment text = new Segment();
            int docLen = doc.getLength();
            doc.prepareSyntax(text, syntax, startPos, 0, forceLastBuffer, forceNotLastBuffer);
            int preScan = syntax.getPreScan();
            char[] buffer = doc.getChars(startPos - preScan, endPos - startPos + preScan);
            boolean lastBuffer = forceNotLastBuffer ? false
                : (forceLastBuffer || (endPos == docLen));
            syntax.relocate(buffer, preScan, endPos - startPos, lastBuffer, endPos);
        } finally {
            doc.readUnlock();
        }
    }

    /** Check whether the given word is identifier or not. */
    public boolean isIdentifier(String word) {
        if (word == null || word.length() == 0) {
            return false; // not qualified as word
        }

        for (int i = 0; i < word.length(); i++) {
            if (!doc.isIdentifierPart(word.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /** Parse the text and pass the resulting tokens to the token processor.
    * @param tp token processor that will be informed about the found tokens.
    * @param startOffset starting position in the text
    * @param endOffset ending position in the text
    * @param forceLastBuffer force the syntax scanner to think that the requested
    *   area is the last in the document.
    */
    public void tokenizeText(TokenProcessor tp, int startOffset, int endOffset,
    boolean forceLastBuffer) throws BadLocationException {
        Syntax syntax = null;
        doc.readLock();
        try {
            Segment text = new Segment();
            syntax = doc.getFreeSyntax();
            int docLen = doc.getLength();
            doc.prepareSyntax(text, syntax, startOffset,
                endOffset - startOffset, forceLastBuffer, false);
            int preScan = syntax.getPreScan();
            tp.nextBuffer(text.array, syntax.getOffset(), endOffset - startOffset,
                          startOffset, preScan, syntax.lastBuffer);

            int bufferStartOffset = startOffset - syntax.getOffset();

            boolean cont = true;
            while (cont) {
                TokenID tokenID = syntax.nextToken();
                TokenContextPath tcp = syntax.getTokenContextPath();
                if (tokenID == null) { //EOT
                    int nextLen = tp.eot(syntax.tokenOffset);
                    nextLen = Math.min(nextLen, docLen - endOffset);
                    if (nextLen == 0) {
                        cont = false;
                    } else { // continue
                        preScan = syntax.getPreScan();
                        doc.getText(endOffset - preScan, preScan + nextLen, text);

                        boolean lastBuffer = forceLastBuffer || (endOffset + nextLen >= docLen);
                        syntax.relocate(text.array, text.offset + preScan, nextLen,
                                        lastBuffer, endOffset + nextLen);
                        tp.nextBuffer(text.array, syntax.getOffset(), nextLen,
                                      endOffset, preScan, lastBuffer);
                        bufferStartOffset = endOffset - syntax.getOffset();
                        endOffset += nextLen;
                    }

                } else { // not EOT
                    int tokenLen = syntax.getTokenLength();
                    int tokenOffset = syntax.getTokenOffset();

                    // Check whether the token isn't too left
                    if (bufferStartOffset +  tokenOffset + tokenLen > startOffset ) {
                        if (!tp.token(tokenID, tcp, tokenOffset, tokenLen)) {
                            cont = false;
                        }
                    }
                }
            }
        } finally {
            if (syntax != null) {
                doc.releaseSyntax(syntax);
            }
            doc.readUnlock();
        }
    }

    /** Parse the text and pass the resulting tokens to the token processor.
    * @param tp token processor that will be informed about the found tokens.
    * @param text text to parse
    */
    public void tokenizeText(TokenProcessor tp, String text) {
        Syntax syntax = null;
        try {
            syntax = doc.getFreeSyntax();
            char[] buf = text.toCharArray();
            syntax.load(null, buf, 0, buf.length, true, -1);

            boolean cont = true;
            while (cont) {
                TokenID tokenID = syntax.nextToken();
                TokenContextPath tcp = syntax.getTokenContextPath();
                if (tokenID == null) {
                    tp.eot(syntax.tokenOffset);
                    cont = false;

                } else {
                    if (!tp.token(tokenID, tcp, syntax.getTokenOffset(), syntax.getTokenLength())) {
                        cont = false;
                    }
                }
            }

        } finally {
            if (syntax != null) {
                doc.releaseSyntax(syntax);
            }
        }
    }

    
    /** Get the member of the chain of the tokens for the given document position.
     * @param offset position in the document for which the chain
     *  is being retrieved.
     * @return token-item around the offset or right at the offset. Null
     *  is returned if offset is equal to document length.
     */
    public TokenItem getTokenChain(int offset) throws BadLocationException {
        // null for end of document
        if (doc.getLength() <= offset) {
            return null;
        }

        return null;
    }

    /**
     * Check whether the abbreviation expansion should be disabled
     * at the given offset.
     *
     * @param offset offset at which the situation should be checked.
     * @return true if the abbreviation expansion should be disabled
     *  at the given offset or false otherwise.
     */
    protected boolean isAbbrevDisabled(int offset) {
        return false;
    }

}
