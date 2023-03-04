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

package org.netbeans.editor.ext;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.ImageTokenID;
import org.netbeans.editor.TokenContextPath;

/**
* Extended format-support offers comment-token support,
* token-and-text operations and other support.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class ExtFormatSupport extends FormatSupport {

    public ExtFormatSupport(FormatWriter formatWriter) {
        super(formatWriter);
    }

    /** Find how many EOLs is between two token-position.
     * @param fromPosition the position from which to start counting.
     *  If it's EOL, it's counted.
     * @param toPosition the ending position. If it points at EOL,
     *  it's ignored from the total count.
     *  It is necessary for the second position to follow
     *  the first one.
     */
    public int findLineDistance(FormatTokenPosition fromPosition,
    FormatTokenPosition toPosition) {
        int lineCounter = 0;

        TokenItem token = fromPosition.getToken();
        int offset = fromPosition.getOffset();
        TokenItem targetToken = toPosition.getToken();
        int targetOffset = toPosition.getOffset();

        // Solve special case if both positions are ending
        if (token == null && targetToken == null) {
            return 0;
        }

        while (token != null) {
            String text = token.getImage();
            int textLen = text.length();
            while (offset < textLen) {
                if (token == targetToken && offset == targetOffset) {
                    return lineCounter;
                }

                if (text.charAt(offset) == '\n') {
                    lineCounter++;
                }

                offset++;
            }

            token = token.getNext();
            offset = 0;
        }

        throw new IllegalStateException("Tokens don't follow in chain."); // NOI18N
    }

    /** Is the given token a comment token? By default it returns
    * false but it can be redefined in descendants.
    */
    public boolean isComment(TokenItem token, int offset) {
        return false;
    }

    public boolean isComment(FormatTokenPosition pos) {
        return isComment(pos.getToken(), pos.getOffset());
    }

    /** Whether the given position is not a whitespace or comment. */
    public boolean isImportant(TokenItem token, int offset) {
        return !isComment(token, offset) && !isWhitespace(token, offset);
    }

    public boolean isImportant(FormatTokenPosition pos) {
        return isImportant(pos.getToken(), pos.getOffset());
    }

    /** Get the first position that is not whitespace and that is not comment.
    * @param startPosition position from which the search starts.
    *  For the backward search the character right at startPosition
    *  is not considered as part of the search.
    * @param limitPosition position where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    * @param stopOnEOL whether stop and return EOL token or continue search if
    *  EOL token is found.
    * @param backward whether search in backward direction.
    * @return first non-whitespace token or EOL or null if all the tokens
    *  till the begining of the chain are whitespaces.
    */
    public FormatTokenPosition findImportant(FormatTokenPosition startPosition,
    FormatTokenPosition limitPosition, boolean stopOnEOL, boolean backward) {
        // Return immediately for equal positions
        if (startPosition.equals(limitPosition)) {
            return null;
        }

        if (backward) {
            TokenItem limitToken;
            int limitOffset;

            if (limitPosition == null) {
                limitToken = null;
                limitOffset = 0;

            } else { // valid limit position
                limitPosition = getPreviousPosition(limitPosition);
                if (limitPosition == null) {
                    limitToken = null;
                    limitOffset = 0;

                } else { // valid limit position
                    limitToken = limitPosition.getToken();
                    limitOffset = limitPosition.getOffset();
                }
            }

            startPosition = getPreviousPosition(startPosition);
            if (startPosition == null) {
                return null;
            }

            TokenItem token = startPosition.getToken();
            int offset = startPosition.getOffset();

            while (true) {
                String text = token.getImage();
                while (offset >= 0) {
                    if (stopOnEOL && text.charAt(offset) == '\n') {
                        return null;
                    }

                    if (isImportant(token, offset)) {
                        return getPosition(token, offset);
                    }

                    if (token == limitToken && offset == limitOffset) {
                        return null;
                    }

                    offset--;
                }

                token = token.getPrevious();
                if (token == null) {
                    return null;
                }
                offset = token.getImage().length() - 1;
            }

        } else { // forward direction
            TokenItem limitToken;
            int limitOffset;

            if (limitPosition == null) {
                limitToken = null;
                limitOffset = 0;

            } else { // valid limit position
                limitToken = limitPosition.getToken();
                limitOffset = limitPosition.getOffset();
            }

            TokenItem token = startPosition.getToken();
            int offset = startPosition.getOffset();

            if (token == null)
                return null;
            
            while (true) {
                String text = token.getImage();
                int textLen = text.length();
                while (offset < textLen) {
                    if (token == limitToken && offset == limitOffset) {
                        return null;
                    }

                    if (stopOnEOL && text.charAt(offset) == '\n') {
                        return null;
                    }

                    if (isImportant(token, offset)) {
                        return getPosition(token, offset);
                    }

                    offset++;
                }

                token = token.getNext();
                if (token == null) {
                    return null;
                }
                offset = 0;
            }
        }
    }

    /** Get the first non-whitespace and non-comment token or null.
     * @param pos any position on the line.
     */
    public FormatTokenPosition findLineFirstImportant(FormatTokenPosition pos) {
        pos = findLineStart(pos);
        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        if (token == null) { // no line start, no WS
            return null;
        }

        while (true) {
            String text = token.getImage();
            int textLen = text.length();
            while (offset < textLen) {
                if (text.charAt(offset) == '\n') {
                    return null;
                }

                if (isImportant(token, offset)) {
                    return getPosition(token, offset);
                }

                offset++;
            }

            if (token.getNext() == null) {
                return null;
            }

            token = token.getNext();
            offset = 0;
        }
    }

    /** Get the start of the area of line where there is only
     * whitespace or comment till the end of the line.
     * @param pos any position on the line.
     * Return null if there's no such area.
     */
    public FormatTokenPosition findLineEndNonImportant(FormatTokenPosition pos) {
        pos = findLineEnd(pos);
        if (isChainStartPosition(pos)) { // empty first line
            return pos;

        } else {
            pos = getPreviousPosition(pos);
        }


        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        while (true) {
            String text = token.getImage();
            int textLen = text.length();
            while (offset >= 0) {
                if (offset < textLen
                    && ((text.charAt(offset) == '\n')
                        || isImportant(token, offset))
                        
                ) {
                    return getNextPosition(token, offset);
                }


                offset--;
            }

            if (token.getPrevious() == null) {
                // This is the first token in chain, return position 0
                return getPosition(token, 0);
            }

            token = token.getPrevious();
            offset = token.getImage().length() - 1;
        }
    }

    /** Insert the token that has token-id containing image, so additional
    * text is not necessary.
    */
    public TokenItem insertImageToken(TokenItem beforeToken,
    ImageTokenID tokenID, TokenContextPath tokenContextPath) {
        return super.insertToken(beforeToken, tokenID, tokenContextPath,
                tokenID.getImage());
    }

    /** Find the token either by token-id or token-text or both.
    * @param startToken token from which to start searching. For backward
    *  search this token is excluded from the search.
    * @param limitToken the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search this token is not considered to be part of search,
    *  but for backward search it is.
    * @param tokenID token-id to be searched. If null the token-id
    *  of the tokens inspected will be ignored.
    * @param tokenImage text of the token to find. If null the text
    *  of the tokens inspected will be ignored.
    * @param backward true for searching in backward direction or false
    *  to serach in forward direction.
    * @return return the matching token or null if nothing was found
    */
    public TokenItem findToken(TokenItem startToken, TokenItem limitToken,
    TokenID tokenID, TokenContextPath tokenContextPath,
    String tokenImage, boolean backward) {

        if (backward) { // go to the previous token for the backward search
            if (startToken != null && startToken == limitToken) { // empty search
                return null;
            }

            startToken = getPreviousToken(startToken);

            if (limitToken != null) {
                limitToken = limitToken.getPrevious();
            }
        }

        while (startToken != null && startToken != limitToken) {
            if (tokenEquals(startToken, tokenID, tokenContextPath, tokenImage)) {
                return startToken;
            }

            startToken = backward ? startToken.getPrevious() : startToken.getNext();
        }

        return null;
    }

    /** Find the first non-whitespace and non-comment token in the given
     * direction. This is similair to <tt>findImportant()</tt>
     * but it operates over the tokens.
     * @param startToken token from which to start searching. For backward
     *  search this token is excluded from the search.
     * @param limitToken the token where the search will be broken
     *  reporting that nothing was found. It can be null to search
     *  till the end or begining of the chain (depending on direction).
     *  For forward search this token is not considered to be part of search,
     *  but for backward search it is.
     * @param backward true for searching in backward direction or false
     *  to serach in forward direction.
     * @return return the matching token or null if nothing was found
     */
    public TokenItem findImportantToken(TokenItem startToken, TokenItem limitToken,
    boolean backward) {

        if (backward) { // go to the previous token for the backward search
            if (startToken != null && startToken == limitToken) { // empty search
                return null;
            }

            startToken = getPreviousToken(startToken);

            if (limitToken != null) {
                limitToken = limitToken.getPrevious();
            }
        }

        while (startToken != null && startToken != limitToken) {
            if (isImportant(startToken, 0)) {
                return startToken;
            }

            startToken = backward ? startToken.getPrevious() : startToken.getNext();
        }

        return null;
    }

    /** This method can be used to find a matching brace token. Both
    * the token-id and token-text are used for comparison of the starting token.
    * @param startToken token from which to start. It cannot be null.
    *  For backward search this token is ignored and the previous one is used.
    * @param limitToken the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search this token is not considered to be part of search,
    *  but for backward search it is.
    * @param matchTokenID matching token-id for the start token.
    * @param matchTokenImage matching token-text for the start token.
    * @param backward true for searching in backward direction or false
    *  to serach in forward direction.
    */
    public TokenItem findMatchingToken(TokenItem startToken, TokenItem limitToken,
    TokenID matchTokenID, String matchTokenImage, boolean backward) {

        int depth = 0;
        TokenID startTokenID = startToken.getTokenID();
        TokenContextPath startTokenContextPath = startToken.getTokenContextPath();
        String startText = startToken.getImage();

        // Start to search from the adjacent item
        TokenItem token = backward ? startToken.getPrevious() : startToken.getNext();

        while (token != null && token != limitToken) {
            if (tokenEquals(token, matchTokenID, startTokenContextPath,
                matchTokenImage)
            ) {
                if (depth-- == 0) {
                    return token;
                }

            } else if (tokenEquals(token, startTokenID, startTokenContextPath,
                startText)
            ) {
                depth++;
            }

            token = backward ? token.getPrevious() : token.getNext();
        }

        return null;
    }

    public TokenItem findMatchingToken(TokenItem startToken, TokenItem limitToken,
    ImageTokenID matchTokenID, boolean backward) {
        return findMatchingToken(startToken, limitToken, matchTokenID,
            matchTokenID.getImage(), backward);
    }
        
    /** Search for any of the image tokens from the given array
    * and return if the token matches any item from the array.
    * The index of the item from the array that matched
    * can be found by calling <tt>getIndex()</tt> method.
    * It is suitable mainly for the image-token-ids.
    *
    * @param startToken token from which to start. For backward search
    *  this token is excluded from the search.
    * @param limitToken the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search this token is not considered to be part of search,
    *  but for backward search it is.
    * @param tokenIDArray array of the token-ids for which to search.
    * @param tokenContextPath context path that the found token must have.
    *  It can be null.
    * @param backward true for searching in backward direction or false
    *  to serach in forward direction.
    */
    public TokenItem findAnyToken(TokenItem startToken, TokenItem limitToken,
    TokenID[] tokenIDArray, TokenContextPath tokenContextPath, boolean backward) {

        if (backward) { // go to the previous token for the backward search
            if (startToken != null && startToken == limitToken) { // empty search
                return null;
            }

            startToken = getPreviousToken(startToken);

            if (limitToken != null) {
                limitToken = limitToken.getPrevious();
            }
        }

        while (startToken != null && startToken != limitToken) {
            for (int i = 0; i < tokenIDArray.length; i++) {
                if (tokenEquals(startToken, tokenIDArray[i], tokenContextPath)) {
                    return startToken;
                }
            }

            startToken = backward ? startToken.getPrevious() : startToken.getNext();
        }

        return null;
    }

    /** Get the index of the token in the given token-id-and-text array or -1
    * if the token is not in the array.
    */
    public int getIndex(TokenItem token, TokenID[] tokenIDArray) {
        for (int i = 0; i < tokenIDArray.length; i++) {
            if (tokenEquals(token, tokenIDArray[i])) {
                return i;
            }
        }
        return -1; // not found
    }

    /** Remove the ending whitespace from the line.
     * @param pos position on the line to be checked.
     * @return position of the EOL on the line or end of chain position
     */
    public FormatTokenPosition removeLineEndWhitespace(FormatTokenPosition pos) {
        FormatTokenPosition endWS = findLineEndWhitespace(pos);
        if (endWS == null || endWS.getToken() == null) { // no WS on line
            return findLineEnd(pos);

        } else { // some WS on line
            int removeInd;
            TokenItem token = endWS.getToken();
            int offset = endWS.getOffset();

            while (true) {
                String text = token.getImage();
                int textLen = text.length();
                removeInd = offset;
                while (offset < textLen) {
                    if (text.charAt(offset) == '\n') {
                        remove(token, removeInd, offset - removeInd);
                        return getPosition(token, removeInd);
                    }

                    offset++;
                }

                TokenItem nextToken = token.getNext();
                if (removeInd == 0) {
                    removeToken(token);

                } else { // only ending part removed
                    remove(token, removeInd, textLen - removeInd);
                }

                token = nextToken;
                if (token == null) {
                    return getPosition(null, 0);
                }
                offset = 0;
            }
        }
    }

    /** Get the character at the given position. The caller must care
     * about not to pass the end-of-chain position to this method.
     */
    public char getChar(FormatTokenPosition pos) {
        return pos.getToken().getImage().charAt(pos.getOffset());
    }

    /** Whether the given position is at the begining of the line. */
    public boolean isLineStart(FormatTokenPosition pos) {
        return isChainStartPosition(pos) || getChar(getPreviousPosition(pos)) == '\n';
    }

    public boolean isNewLine(FormatTokenPosition pos) {
        return (pos.getToken() != null) && getChar(pos) == '\n';
    }

}
