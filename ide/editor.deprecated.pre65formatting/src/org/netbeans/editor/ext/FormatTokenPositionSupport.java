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

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import javax.swing.text.Position;
import org.netbeans.editor.TokenItem;

/**
* Support class for mapping the token-positions
* to the tokens and providing additional operations.
*
* @author Miloslav Metelka
* @version 1.00
*/

class FormatTokenPositionSupport {

    private final FormatWriter formatWriter;

    /** First save set in the chain */
    private SaveSet firstSet;

    /** Last save set in the chain */
    private SaveSet lastSet;

    /** Map holding the [token, token-position-list] pairs. */
    private final HashMap<TokenItem, List<ExtTokenPosition>> tokens2positionLists = new HashMap<>();

    FormatTokenPositionSupport(FormatWriter formatWriter) {
        this.formatWriter = formatWriter;
    }

    private List<ExtTokenPosition> getPosList(TokenItem token) {
        List<ExtTokenPosition> ret = tokens2positionLists.get(token);
        if (ret == null) {
            ret = new ArrayList<>(3);
            tokens2positionLists.put(token, ret);
        }
        return ret;
    }

    /** Get the token-position for the given token and offset.
     * @param token token for which the token-position is being created.
     * @param offset offset inside the token at which the position is being
     *   created.
     */
    synchronized ExtTokenPosition getTokenPosition(TokenItem token, int offset,
    Position.Bias bias) {
        // Check offset correctness
        if (token == null) {
            if (offset != 0) {
                throw new IllegalArgumentException(
                    "Ending token position has non-zero offset=" + offset); // NOI18N
            }

        } else if (offset >= token.getImage().length()) {
            throw new IllegalArgumentException("Offset=" + offset // NOI18N
                    + " >= tokenLength=" + token.getImage().length()); // NOI18N
        }

        List<ExtTokenPosition> posList = getPosList(token);
        int cnt = posList.size();
        ExtTokenPosition etp;
        for (int i = 0; i < cnt; i++) {
            etp = posList.get(i);
            if (etp.getOffset() == offset && etp.getBias() == bias) {
                return etp;
            }
        }

        etp = new ExtTokenPosition(token, offset, bias);
        posList.add(etp);
        return etp;
    }

    /** Notify that the previous token was created with
     * the appropriate text taken from the start of this token.
     * It's now necessary to split the marks according
     * @param token token that was split
     * @param startLength initial length of the token-text
     *  that was cut and inserted into the previous token
     *  in the chain.
     */
    synchronized void splitStartTokenPositions(TokenItem token, int startLength) {
        TokenItem prevToken = token.getPrevious();
        if (prevToken != null) {
            prevToken = formatWriter.findNonEmptyToken(prevToken, true);
        }
        List<ExtTokenPosition> posList = getPosList(token);
        int len = posList.size();
        List<ExtTokenPosition> prevPosList = getPosList(prevToken);
        for (int i = 0; i < len; i++) {
            ExtTokenPosition etp = posList.get(i);
            if (etp.offset < startLength) { // move to prevToken
                etp.token = prevToken;
                posList.remove(i);
                prevPosList.add(etp);
                i--;
                len--;
            }
        }
    }

    /** Notify that the previous token was created with
     * the appropriate text taken from the start of this token.
     * It's now necessary to split the marks according
     * @param token token that was split
     * @param endLength initial length of the token-text
     *  that was cut and inserted into the previous token
     *  in the chain.
     */
    synchronized void splitEndTokenPositions(TokenItem token, int endLength) {
        TokenItem nextToken = token.getNext();
        if (nextToken != null) {
            nextToken = formatWriter.findNonEmptyToken(nextToken, false);
        }
        List<ExtTokenPosition> nextPosList = getPosList(nextToken);

        List<ExtTokenPosition> posList = getPosList(token);
        int len = posList.size();
        int offset = token.getImage().length() - endLength;
        for (int i = 0; i < len; i++) {
            ExtTokenPosition etp = posList.get(i);
            if (etp.offset >= offset) { // move to nextToken
                etp.token = nextToken;
                etp.offset -= offset;
                posList.remove(i);
                nextPosList.add(etp);
                i--;
                len--;
            }
        }
    }

    /** Text in the token will be inserted. */
    synchronized void tokenTextInsert(TokenItem token, int offset, int length) {
        List<ExtTokenPosition> posList = getPosList(token);
        int len = posList.size();
        // Add length to all positions after insertion point
        for (int i = 0; i < len; i++) {
            ExtTokenPosition etp = posList.get(i);
            if ((etp.bias == Position.Bias.Backward)
                    ? (etp.offset > offset) : (etp.offset >= offset)) {
                etp.offset += length;
            }
        }

        // Move bwd-bias marks from the next token if insert at end
        if (token.getImage().length() == offset) {
            TokenItem nextToken = token.getNext();
            if (nextToken != null) {
                nextToken = formatWriter.findNonEmptyToken(nextToken, false);
            }
            posList = getPosList(nextToken);
            len = posList.size();
            for (int i = 0; i < len; i++) {
                ExtTokenPosition etp = posList.get(i);
                if (etp.bias == Position.Bias.Backward && etp.offset == 0) {
                    etp.token = token;
                    etp.offset = offset;
                }
            }
        }


    }

    /** Text in the token will be removed. */
    synchronized void tokenTextRemove(TokenItem token, int offset, int length) {
        List<ExtTokenPosition> posList = getPosList(token);
        int len = posList.size();
        int newLen = token.getImage().length() - length;
        List<ExtTokenPosition> nextList = getPosList(token.getNext());
        for (int i = 0; i < len; i++) {
            ExtTokenPosition etp = posList.get(i);
            if (etp.offset >= offset + length) { // move to nextToken
                etp.offset -= length;

            } else if (etp.offset >= offset) {
                etp.offset = offset;
            }

            // Check if pos right at the end of token and therefore invalid
            if (etp.offset >= newLen) { // need to move to begining of next token
                etp.token = token.getNext();
                etp.offset = 0;
                posList.remove(i);
                nextList.add(etp);
                i--;
                len--;
            }
        }
    }

    /** Whole token being removed. */
    synchronized void tokenRemove(TokenItem token) {
        TokenItem nextToken = token.getNext();
        if (nextToken != null) {
            nextToken = formatWriter.findNonEmptyToken(nextToken, false);
        }
        List<ExtTokenPosition> nextPosList = getPosList(nextToken);

        List<ExtTokenPosition> posList = getPosList(token);
        int len = posList.size();
        for (int i = 0; i < len; i++) {
            ExtTokenPosition etp = posList.get(i);
            etp.token = nextToken;
            etp.offset = 0;
            nextPosList.add(etp);
        }
        posList.clear();

        // Remove the token from registry
        tokens2positionLists.remove(token);
    }

    /** Given token was inserted into the chain */
    synchronized void tokenInsert(TokenItem token) {
        if (token.getImage().length() > 0) { // only for non-zero size
            List<ExtTokenPosition> posList = getPosList(token);

            TokenItem nextToken = token.getNext();
            if (nextToken != null) {
                nextToken = formatWriter.findNonEmptyToken(nextToken, false);
            }
            List<ExtTokenPosition> nextPosList = getPosList(nextToken);

            int nextLen = nextPosList.size();
            for (int i = 0; i < nextLen; i++) {
                ExtTokenPosition etp = nextPosList.get(i);
                if (etp.offset == 0 && etp.getBias() == Position.Bias.Backward) {
                    etp.token = token; // offset will stay equal to zero
                    nextPosList.remove(i);
                    i--;
                    nextLen--;
                    posList.add(etp);
                }
            }
        }
    }

    /** Clear all the save-sets. */
    synchronized void clearSaveSets() {
        firstSet = null;
        lastSet = null;
    }

    /** Add the save-set to the registry and perform the checking
     * whether the offsets are OK.
     */
    synchronized void addSaveSet(int baseOffset, int writtenLen,
    int[] offsets, Position.Bias[] biases) {
        // Check whether the offsets are OK
        for (int i = 0; i < offsets.length; i++) {
            if (offsets[i] < 0 || offsets[i] > writtenLen) {
                throw new IllegalArgumentException(
                    "Invalid save-offset=" + offsets[i] + " at index=" + i // NOI18N
                    + ". Written length is " + writtenLen); // NOI18N
            }
        }

        SaveSet newSet = new SaveSet(baseOffset, offsets, biases);

        if (firstSet != null) {
            lastSet.next = newSet;
            lastSet = newSet;

        } else { // first set
            firstSet = lastSet = newSet;
        }
    }

    /** Create the token-positions for all the save sets */
    synchronized void createPositions(FormatTokenPosition formatStartPosition) {
        updateSaveOffsets(formatStartPosition);

        SaveSet curSet = firstSet;
        FormatWriter.FormatTokenItem token 
                = (FormatWriter.FormatTokenItem)formatStartPosition.getToken();
        boolean noText = (token == null);

        while (curSet != null) {
            int len = curSet.offsets.length;
            for (int i = 0; i < len; i++) {
                if (noText) {
                    curSet.positions[i] = getTokenPosition(null, 0, curSet.biases[i]);

                } else { // there's some text to be formatted

                    // Find the covering token and create the position
                    int offset = curSet.offsets[i];
                    while (token != null) {
                        if (offset < token.getSaveOffset()) {
                            token = (FormatWriter.FormatTokenItem)token.getPrevious();

                        } else if ((offset > token.getSaveOffset() + token.getImage().length())
                            || token.getImage().length() == 0
                        ) {
                            token = (FormatWriter.FormatTokenItem)token.getNext();

                        } else { // the right token
                            curSet.positions[i] = getTokenPosition(token,
                                    offset - token.getSaveOffset(), curSet.biases[i]);
                            break; // break the loop
                        }
                    }

                    if (token == null) { // It is right at the end
                        curSet.positions[i] = getTokenPosition(null, 0, curSet.biases[i]);
                        token = (FormatWriter.FormatTokenItem)formatWriter.getLastToken();
                    }
                }
            }

            curSet = curSet.next;
        }
    }

    synchronized void updateSaveSets(FormatTokenPosition formatStartPosition) {
        updateSaveOffsets(formatStartPosition);

        SaveSet curSet = firstSet;
        int endOffset = 0; // offset of the null token
        if (formatStartPosition.getToken() != null) {
            endOffset = ((FormatWriter.FormatTokenItem)formatWriter.getLastToken()).getSaveOffset()
                + formatWriter.getLastToken().getImage().length();
        }

        while (curSet != null) {
            int len = curSet.offsets.length;
            for (int i = 0; i < len; i++) {
                FormatWriter.FormatTokenItem token
                    = (FormatWriter.FormatTokenItem)curSet.positions[i].getToken();
                if (token == null) {
                    curSet.offsets[i] = endOffset;

                } else { // non-null token
                    curSet.offsets[i] = token.getSaveOffset()
                        + curSet.positions[i].getOffset();
                }
            }
        }
    }

    /** Number the tokens so that they are OK for finding out the
     * offsets.
     */
    private void updateSaveOffsets(FormatTokenPosition formatStartPosition) {
        if (firstSet != null) { // it has only sense if there are any save-sets
            FormatWriter.FormatTokenItem ti
                = (FormatWriter.FormatTokenItem)formatStartPosition.getToken();
            int offset = -formatStartPosition.getOffset();

            while (ti != null) {
                ti.setSaveOffset(offset);
                offset += ti.getImage().length();

                ti = (FormatWriter.FormatTokenItem)ti.getNext();
            }
        }
    }

    /** Implementation of the extended-token-position that allows
     * modification of its token and offset fields.
     */
    class ExtTokenPosition implements FormatTokenPosition {

        TokenItem token;

        int offset;

        /** Whether the position should stay the same if inserted right at it. */
        Position.Bias bias;

        ExtTokenPosition(TokenItem token, int offset) {
            this(token, offset, Position.Bias.Forward);
        }

        ExtTokenPosition(TokenItem token, int offset, Position.Bias bias) {
            this.token = token;
            this.offset = offset;
            this.bias = bias;
        }

        public TokenItem getToken() {
            return token;
        }

        public int getOffset() {
            return (token != null) ? offset : 0;
        }

        public Position.Bias getBias() {
            return bias;
        }

        public boolean equals(Object o) {
            return equals(o, true); // ignore bias in comparison
        }

        public boolean equals(Object o, boolean ignoreBias) {
            if (o instanceof FormatTokenPosition) {
                FormatTokenPosition tp = (FormatTokenPosition)o;

                return token == tp.getToken() && offset == tp.getOffset()
                    && (ignoreBias || bias == tp.getBias());
            }

            return false;
        }

        public String toString() {
            return "<" + getToken() + ", " + getOffset() + ", " + getBias() + ">"; // NOI18N
        }

    }

    /** Class holding the info about the set of the offsets to save
     * during the formatting.
     */
    static class SaveSet {

        /** Next set in the chain. */
        SaveSet next;

        /** Base offset of the buffer corresponding to the offsets */
        int baseOffset;

        /** Offsets to save */
        int[] offsets;

        /** Biases for the positions */
        Position.Bias[] biases;

        /** Token positions corresponding to the offsets */
        FormatTokenPosition[] positions;

        SaveSet(int baseOffset, int[] offsets, Position.Bias[] biases) {
            this.baseOffset = baseOffset;
            this.offsets = offsets;
            this.biases = biases;
        }

    }

}
