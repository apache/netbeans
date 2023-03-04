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

import java.util.HashMap;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Analyzer;

/**
* Format support presents a set of operations over the format-writer
* that is specific for the given set of formatting layers.
* It presents the way how to extend the low level methods
* offered by the format-writer.
* In general there can be more format-layers that use one type
* of the format-support. 
*
* @author Miloslav Metelka
* @version 1.00
*/

public class FormatSupport {

    /** Format-writer over which this support is constructed. */
    private FormatWriter formatWriter;

    public FormatSupport(FormatWriter formatWriter) {
        this.formatWriter = formatWriter;
    }

    /** Getter for the format-writer associated with this format-support. */
    public FormatWriter getFormatWriter() {
        return formatWriter;
    }

    public int getTabSize() {
        Document doc = formatWriter.getDocument();
        return (doc instanceof BaseDocument)
            ? ((BaseDocument)doc).getTabSize()
            : formatWriter.getFormatter().getTabSize();
    }

    public int getShiftWidth() {
        Document doc = formatWriter.getDocument();
        return (doc instanceof BaseDocument)
            ? ((BaseDocument)doc).getShiftWidth()
            : formatWriter.getFormatter().getShiftWidth();
    }

    public boolean expandTabs() {
        ExtFormatter.pushFormattingContextDocument(formatWriter.getDocument());
        try {
            return formatWriter.getFormatter().expandTabs();
        } finally {
            ExtFormatter.popFormattingContextDocument(formatWriter.getDocument());
        }
    }

    public int getSpacesPerTab() {
        ExtFormatter.pushFormattingContextDocument(formatWriter.getDocument());
        try {
            return formatWriter.getFormatter().getSpacesPerTab();
        } finally {
            ExtFormatter.popFormattingContextDocument(formatWriter.getDocument());
        }
    }

    public Object getSettingValue(String settingName) {
        return formatWriter.getFormatter().getSettingValue(settingName);
    }

    public Object getSettingValue(String settingName, Object defaultValue) {
        Object value = getSettingValue(settingName);
        return (value != null) ? value : defaultValue;
    }

    public boolean getSettingBoolean(String settingName, Boolean defaultValue) {
        return ((Boolean)getSettingValue(settingName, defaultValue)).booleanValue();
    }

    public boolean getSettingBoolean(String settingName, boolean defaultValue) {
        return ((Boolean)getSettingValue(settingName,
             (defaultValue ? Boolean.TRUE : Boolean.FALSE))).booleanValue();
    }

    public int getSettingInteger(String settingName, Integer defaultValue) {
        return ((Integer)getSettingValue(settingName, defaultValue)).intValue();
    }

    public int getSettingInteger(String settingName, int defaultValue) {
        Object value = getSettingValue(settingName);
        return (value instanceof Integer) ? ((Integer)value).intValue() : defaultValue;
    }

    /** Delegation to the same method in format-writer. */
    public final boolean isIndentOnly() {
        return formatWriter.isIndentOnly();
    }

    /** Delegation to the same method in format-writer. */
    public FormatTokenPosition getFormatStartPosition() {
        return formatWriter.getFormatStartPosition();
    }

    public FormatTokenPosition getTextStartPosition() {
        return formatWriter.getTextStartPosition();
    }

    /** Get the first token in chain. */
    public TokenItem findFirstToken(TokenItem token) {
        return formatWriter.findFirstToken(token);
    }

    /** Delegation to the same method in format-writer. */
    public TokenItem getLastToken() {
        return formatWriter.getLastToken();
    }

    public FormatTokenPosition getLastPosition() {
        TokenItem lt = findNonEmptyToken(getLastToken(), true);
        return (lt == null) ? null : getPosition(lt, lt.getImage().length() - 1);
    }

    /** Delegation to the same method in format-writer. */
    public boolean canInsertToken(TokenItem beforeToken) {
        return formatWriter.canInsertToken(beforeToken);
    }

    /** Delegation to the same method in format-writer. */
    public TokenItem insertToken(TokenItem beforeToken,
    TokenID tokenID, TokenContextPath tokenContextPath, String tokenImage) {
        return formatWriter.insertToken(beforeToken,
                tokenID, tokenContextPath, tokenImage);
    }

    public void insertSpaces(TokenItem beforeToken, int spaceCount) {
        TokenID whitespaceTokenID = getWhitespaceTokenID();
        if (whitespaceTokenID == null) {
            throw new IllegalStateException("Valid whitespace token-id required."); // NOI18N
        }

        insertToken(beforeToken, whitespaceTokenID, null, 
                    new String(Analyzer.getSpacesBuffer(spaceCount), 0, spaceCount));
    }

    /** Whether the token-item can be removed. It can be removed
    * only in case it doesn't come from the document's text
    * and it wasn't yet written to the underlying writer.
    */
    public boolean canRemoveToken(TokenItem token) {
        return formatWriter.canRemoveToken(token);
    }

    /** Remove the token-item from the chain. It can be removed
    * only in case it doesn't come from the document's text
    * and it wasn't yet written to the underlying writer.

    */
    public void removeToken(TokenItem token) {
        formatWriter.removeToken(token);
    }

    /** Remove all the tokens between start and end token inclusive. */
    public void removeTokenChain(TokenItem startToken, TokenItem endToken) {
        while (startToken != null && startToken != endToken) {
            TokenItem t = startToken.getNext();
            removeToken(startToken);
            startToken = t;
        }
    }

    public TokenItem splitStart(TokenItem token, int startLength,
    TokenID newTokenID, TokenContextPath newTokenContextPath) {
        return formatWriter.splitStart(token, startLength, newTokenID, newTokenContextPath);
    }

    public TokenItem splitEnd(TokenItem token, int endLength,
    TokenID newTokenID, TokenContextPath newTokenContextPath) {
        return formatWriter.splitEnd(token, endLength, newTokenID, newTokenContextPath);
    }

    public void insertString(TokenItem token, int offset, String text) {
        formatWriter.insertString(token, offset, text);
    }

    public void insertString(FormatTokenPosition pos, String text) {
        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        if (token == null) { // ending position
            token = getLastToken();
            if (token == null) {
                throw new IllegalStateException("Cannot insert string. No tokens."); // NOI18N
            }
            offset = token.getImage().length();
        }

        insertString(token, offset, text);
    }

    public void remove(TokenItem token, int offset, int length) {
        formatWriter.remove(token, offset, length);
    }

    public void remove(FormatTokenPosition pos, int length) {
        remove(pos.getToken(), pos.getOffset(), length);
    }

    /** Check whether the given token has empty text and if so
    * start searching for token with non-empty text in the given
    * direction. If there's no non-empty token in the given direction
    * the method returns null.
    */
    public TokenItem findNonEmptyToken(TokenItem token, boolean backward) {
        return formatWriter.findNonEmptyToken(token, backward);
    }

    /** Get the token position that corresponds to the given token and offset.
     * @param token token for which the token-position is being created.
     * @param offset offset inside the token.
     */
    public FormatTokenPosition getPosition(TokenItem token, int offset) {
        return getPosition(token, offset, Position.Bias.Forward);
    }

    public FormatTokenPosition getPosition(TokenItem token, int offset, Position.Bias bias) {
        return formatWriter.getPosition(token, offset, bias);
    }

    /** Get the next position of the position given by parameters.
     * It can be either just offset increasing but it can be movement
     * to the next token for the token boundary.
     * @return next token-position or null for the EOT position
     */
    public FormatTokenPosition getNextPosition(TokenItem token, int offset,
    Position.Bias bias) {
        if (token == null) { // end of chain
            return null;

        } else { // regular token
            offset++;

            if (offset >= token.getImage().length()) {
                token = token.getNext();
                offset = 0;
            }

            return getPosition(token, offset, bias);
        }
    }

    /** Get the previous position of the position given by parameters.
     * It can be either just offset decreasing but it can be movement
     * to the previous token for the token boundary.
     * @return next token-position or null for the first position in the chain
     */
    public FormatTokenPosition getPreviousPosition(TokenItem token, int offset,
    Position.Bias bias) {
        FormatTokenPosition ret = null;
        if (token == null) { // end of chain
            TokenItem lastToken = findNonEmptyToken(getLastToken(), true);
            if (lastToken != null) { // regular last token
                ret = getPosition(lastToken, lastToken.getImage().length() - 1,
                        Position.Bias.Forward);
            }

        } else { // regular token
            offset--;

            if (offset < 0) {
                token = token.getPrevious();
                if (token != null) { // was first pos in first token
                    ret = getPosition(token, token.getImage().length() - 1,
                            Position.Bias.Forward);
                }

            } else { // still inside token
                ret = getPosition(token, offset,
                        Position.Bias.Forward);
            }
        }

        return ret;
    }

    /** Get the token-position preceeding the given one. Use the same
     * bias like the given position has.
     */
    public FormatTokenPosition getPreviousPosition(FormatTokenPosition pos) {
        return getPreviousPosition(pos.getToken(),
                pos.getOffset(), pos.getBias());
    }

    /** Get the token-position preceeding the given one.
     * @param bias bias that the returned position will have.
     */
    public FormatTokenPosition getPreviousPosition(FormatTokenPosition pos, Position.Bias bias) {
        return getPreviousPosition(pos.getToken(), pos.getOffset(), bias);
    }

    public FormatTokenPosition getPreviousPosition(TokenItem token, int offset) {
        return getPreviousPosition(token, offset, Position.Bias.Forward);
    }

    /** Get the next successive token-position after the given one.
     * Use the same bias like the given position has.
     */
    public FormatTokenPosition getNextPosition(FormatTokenPosition pos) {
        return getNextPosition(pos.getToken(),
                pos.getOffset(), pos.getBias());
    }

    /** Get the token-position preceeding the given one.
     * @param bias bias that the returned position will have.
     */
    public FormatTokenPosition getNextPosition(FormatTokenPosition pos, Position.Bias bias) {
        return getNextPosition(pos.getToken(), pos.getOffset(), bias);
    }

    public FormatTokenPosition getNextPosition(TokenItem token, int offset) {
        return getNextPosition(token, offset, Position.Bias.Forward);
    }

    public boolean isAfter(TokenItem testedToken, TokenItem afterToken) {
        return formatWriter.isAfter(testedToken, afterToken);
    }

    public boolean isAfter(FormatTokenPosition testedPosition,
    FormatTokenPosition afterPosition) {
        return formatWriter.isAfter(testedPosition, afterPosition);
    }

    public boolean isChainStartPosition(FormatTokenPosition pos) {
        return formatWriter.isChainStartPosition(pos);
    }

    /** Whether the given token can be replaced or not. It's
    * identical to whether the token can be removed.
    */
    public boolean canReplaceToken(TokenItem token) {
        return canRemoveToken(token);
    }

    /** Replace the given token with the new token.
    * @param originalToken original token to be replaced.
    * @param tokenID token-id of the new token-item
    * @param tokenContextPath token-context-path of the new token-item
    * @param tokenImage token-text of the new token-item
    */
    public void replaceToken(TokenItem originalToken, 
    TokenID tokenID, TokenContextPath tokenContextPath, String tokenImage) {
        if (!canReplaceToken(originalToken)) {
            throw new IllegalStateException("Cannot insert token into chain"); // NOI18N
        }

        TokenItem next = originalToken.getNext();
        removeToken(originalToken);
        insertToken(next, tokenID, tokenContextPath, tokenImage);
    }

    /** Delegation to the same method in format-writer. */
    public boolean isRestartFormat() {
        return formatWriter.isRestartFormat();
    }

    /** Delegation to the same method in format-writer. */
    public void setRestartFormat(boolean restartFormat) {
        formatWriter.setRestartFormat(restartFormat);
    }

    /** Delegation to the same method in format-writer. */
    public int getIndentShift() {
        return formatWriter.getIndentShift();
    }

    /** Delegation to the same method in format-writer. */
    public void setIndentShift(int indentShift) {
        formatWriter.setIndentShift(indentShift);
    }

    /** Compare token-id of the compare-token with the given token-id.
    * Token text and token-context-path are ignored in comparison.
    * @param compareToken token to compare
    * @param withTokenID token-id with which the token's token-id is compared
    * @return true if the token-ids match, false otherwise
    */
    public boolean tokenEquals(TokenItem compareToken, TokenID withTokenID) {
        return tokenEquals(compareToken, withTokenID, null, null);
    }

    /** Compare token-id of the compare-token with the given token-id and
    * token-context-path. Token text is ignored in comparison.
    * @param compareToken token to compare
    * @param withTokenID token-id with which the token's token-id is compared.
    * @param withTokenContextPath token-context-path to which
    *  the token's token-context-path is compared.
    * @return true if the token-ids match, false otherwise
    */
    public boolean tokenEquals(TokenItem compareToken, TokenID withTokenID,
    TokenContextPath withTokenContextPath) {
        return tokenEquals(compareToken, withTokenID, withTokenContextPath, null);
    }

    /** Compare token-id of the compare-token with the given token-id and
    * given token-text.
    * @param compareToken token to compare
    * @param withTokenID token-id with which the token's token-id is compared.
    *  It can be null in which case the token-id is ignored from comparison.
    * @param withTokenContextPath token-context-path to which
    *  the token's token-context-path is compared.
    *  It can be null in which case the token-context-path is ignored from comparison.
    * @param withTokenImage token-text with which the token's token-text is compared.
    *  It can be null in which case the token-text is ignored from comparison.
    * @return true if the token-ids and token-texts match, false otherwise
    */
    public boolean tokenEquals(TokenItem compareToken, TokenID withTokenID,
    TokenContextPath withTokenContextPath, String withTokenImage) {
        return (withTokenID == null || compareToken.getTokenID() == withTokenID)
            && (withTokenContextPath == null
                    || compareToken.getTokenContextPath() == withTokenContextPath)
            && (withTokenImage == null || compareToken.getImage().equals(withTokenImage));
    }

    /** Decide whether the character at the given offset in the given token
     * is whitespace.
     */
    public boolean isWhitespace(TokenItem token, int offset) {
        return Character.isWhitespace(token.getImage().charAt(offset));
    }

    public boolean isWhitespace(FormatTokenPosition pos) {
        return isWhitespace(pos.getToken(), pos.getOffset());
    }

    /** Get the starting position of the line. It searches for the new-line
     * character in backward direction and returns the position
     * of the character following
     * the new-line character or the first character of the first token
     * in the chain.
     * @param pos any token-position on the line.
     */
    public FormatTokenPosition findLineStart(FormatTokenPosition pos) {
        if (isChainStartPosition(pos)) { // begining of the chain
            return pos;
        }

        // Go to the previous char
        pos = getPreviousPosition(pos);

        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        while (true) {
            String text = token.getImage();
            while (offset >= 0) {
                if (text.charAt(offset) == '\n') {
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

    /** Get the ending position of the line. It searches for the new-line
     * character and returns the position of it
     * or the position after the last character of the last token
     * in the chain.
     * @param pos any token-position on the line.
     */
    public FormatTokenPosition findLineEnd(FormatTokenPosition pos) {
        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        if (token == null) { // end of whole chain is EOL too
            return pos;
        }

        while (true) {
            String text = token.getImage();
            int textLen = text.length();
            while (offset < textLen) {
                if (text.charAt(offset) == '\n') {
                    return getPosition(token, offset);
                }

                offset++;
            }

            if (token.getNext() == null) {
                // This is the first token in chain, return end position
                return getPosition(null, 0);
            }

            token = token.getNext();
            offset = 0;
        }
    }

    /** Return the first non-whitespace character on the line
     * or null if there is no non-WS char on the line.
     */
    public FormatTokenPosition findLineFirstNonWhitespace(FormatTokenPosition pos) {
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

                if (!isWhitespace(token, offset)) {
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

    /** Return the ending whitespace on the line or null
    * if there's no such token on the given line.
    */
    public FormatTokenPosition findLineEndWhitespace(FormatTokenPosition pos) {
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
                    && ((text.charAt(offset) == '\n') || !isWhitespace(token, offset))
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

    /** Get the first EOL in backward direction. The current position
     * is ignored by the search.
     * @return first EOL in backward direction or null if there
     *  is no such token.
     */
    public FormatTokenPosition findPreviousEOL(FormatTokenPosition pos) {
        pos = getPreviousPosition(pos);
        if (pos == null) { // was the start position
            return null;
        }

        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        while (true) {
            String text = token.getImage();
            while (offset >= 0) {
                if (text.charAt(offset) == '\n') {
                    return getPosition(token, offset);
                }

                offset--;
            }

            if (token.getPrevious() == null) {
                return null;
            }

            token = token.getPrevious();
            offset = token.getImage().length() - 1;
        }
    }

    /** Get the first EOL in forward direction.
    * @param pos starting token-position that is ignored by the search
    *  so it can be even EOL.
    * @return first EOL token-position in the forward direction or null if there
    *  is no such token.
    */
    public FormatTokenPosition findNextEOL(FormatTokenPosition pos) {
        pos = getNextPosition(pos);
        if (pos == null) {
            return null;
        }

        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        if (token == null) { // right at the end
            return null;
        }

        while (true) {
            String text = token.getImage();
            int textLen = text.length();
            while (offset < textLen) {
                if (text.charAt(offset) == '\n') {
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

    /** Check whether there are no tokens except the ending EOL
    * on the given line.
    * @param pos any position on the line
    */
    public boolean isLineEmpty(FormatTokenPosition pos) {
        return findLineStart(pos).equals(findLineEnd(pos));
    }

    /** Check whether there are only the whitespace tokens
    * on the given line.
    * @param token any token on the line. It doesn't have to be the first one.
    */
    public boolean isLineWhite(FormatTokenPosition pos) {
        FormatTokenPosition lineStart = findLineStart(pos);
        return findLineEndWhitespace(pos).equals(lineStart);
    }


    /** Get the column-offset of the tokenItem on its line. The tabs
    * are expanded according to the tab-size.
    */
    public int getVisualColumnOffset(FormatTokenPosition pos) {
        TokenItem targetToken = pos.getToken();
        int targetOffset = pos.getOffset();

        FormatTokenPosition lineStart = findLineStart(pos);
        TokenItem token = lineStart.getToken();
        int offset = lineStart.getOffset();

        int col = 0;
        int tabSize = getTabSize();

        while (token != null) {
            String text = token.getImage();
            int textLen = text.length();
            while (offset < textLen) {
                if (token == targetToken && offset == targetOffset) {
                    return col;
                }

                switch (text.charAt(offset)) {
                    case '\t':
                        col = (col + tabSize) / tabSize * tabSize;
                        break;
                    default:
                        col++;
                }

                offset++;
            }

            token = token.getNext();
            offset = 0;
        }

        return col;
    }

    /** Get the first non-whitespace position in the given direction.
    * @param startPosition position at which the search starts.
    *  For the backward search the character right at startPosition
    *  is not considered as part of the search.
    * @param limitPosition the token where the search will be broken
    *  reporting that nothing was found. It can be null to search
    *  till the end or begining of the chain (depending on direction).
    *  For forward search the char at the limitPosition
    *  is not considered to be part of search,
    *  but for backward search it is.
    * @param stopOnEOL whether stop and return EOL position
    *  or continue search if EOL token is found.
    * @param backward whether search in backward direction.
    * @return first non-whitespace position or EOL or null if all the tokens
    *  till the begining of the chain are whitespaces.
    */
    public FormatTokenPosition findNonWhitespace(FormatTokenPosition startPosition,
    FormatTokenPosition limitPosition, boolean stopOnEOL, boolean backward) {

        // Return immediately for equal positions
        if (startPosition.equals(limitPosition)) {
            return null;
        }

        if (backward) { // Backward search
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

                    if (!isWhitespace(token, offset)) {
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

        } else { // Forward direction
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

                    if (!isWhitespace(token, offset)) {
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

    /** Get the previous token or last token if the argument is null. */
    public TokenItem getPreviousToken(TokenItem token) {
        return (token == null) ? getLastToken() : token.getPrevious();
    }

    /** Get the token-id that should be assigned to the token
     * that consists of the indentation whitespace only. This method should
     * be overriden in the descendants.
     */
    public TokenID getWhitespaceTokenID() {
        return null;
    }

    /** Get the valid whitespace token-id by calling <tt>getWhitespaceTokenID()</tt>.
     * Throw <tt>IllegalStateException</tt> if the whitespace-token-id is null.
     */
    public TokenID getValidWhitespaceTokenID() {
        TokenID wsID = getWhitespaceTokenID();
        if (wsID == null) {
            throw new IllegalStateException("Null whitespace token-id"); // NOI18N
        }
        return wsID;
    }

    /** Get the token-context-path that should be assigned to the token
     * that consists of the indentation whitespace only. This method should
     * be overriden in the descendants.
     */
    public TokenContextPath getWhitespaceTokenContextPath() {
        return null;
    }

    /** Get the valid whitespace token-context-path
     * by calling <tt>getWhitespaceTokenContextPath()</tt>.
     * Throw <tt>IllegalStateException</tt> if the whitespace-token-id is null.
     */
    public TokenContextPath getValidWhitespaceTokenContextPath() {
        TokenContextPath wsTCP = getWhitespaceTokenContextPath();
        if (wsTCP == null) {
            throw new IllegalStateException("Null whitespace token-context-path"); // NOI18N
        }
        return wsTCP;
    }

    /** Check whether the given token enables modifying
     * of a whitespace in it. This method should be overriden
     * in the descendants.
     */
    public boolean canModifyWhitespace(TokenItem inToken) {
        return false;
    }

    /** This delegates to the same method in formatter. */
    public String getIndentString(int indent) {
        return formatWriter.getFormatter().getIndentString(indent);
    }

    /** Get the indentation of the line.
    * @param formatTokenPosition any position on the line.
    *   It doesn't have to be the first one.
    * @param zeroForWSLine If set to true the method will return zero
    *   in case the line consist of whitespace only. If false
    *   the method will return the indentation even for whitespace
    *   lines.
    */
    public int getLineIndent(FormatTokenPosition pos, boolean zeroForWSLine) {
        FormatTokenPosition firstNWS = findLineFirstNonWhitespace(pos);
        if (firstNWS == null) { // no non-WS char on the line
            if (zeroForWSLine) {
                return 0;

            } else { // return indent even for WS lines
                firstNWS = findLineEnd(pos);
            }
        }

        return getVisualColumnOffset(firstNWS);
    }

    /** Change the indentation of the line. This method should
     * be always called for all the lines because it ensures
     * that the indentation will contain exactly the characters from
     * the indentation string.
     * @param pos any position on the line being checked.
     * @param indent the indentation for the line.
     * @return some position on the line
     */
    public FormatTokenPosition changeLineIndent(FormatTokenPosition pos, int indent) {
        pos = findLineStart(pos); // go to line begining
        String indentString = getIndentString(indent);
        int indentStringLen = indentString.length();
        int indentStringInd = 0; // current index in the indentString
        TokenItem token = pos.getToken();
        int offset = pos.getOffset();

        if (token == null) { // last line is empty, append the indent string
            if (indentString.length() > 0) {
                token = insertToken(null, getValidWhitespaceTokenID(),
                        getValidWhitespaceTokenContextPath(), indentString);
            }
            return pos; // return original end-of-chain position
        }


        while (true) {

            String text = token.getImage();
            int textLen = text.length();

            while (indentStringInd < indentStringLen && offset < textLen) {
                if (indentString.charAt(indentStringInd) != text.charAt(offset)) {
                    if (canModifyWhitespace(token)) {
                        // modify token text to insert the whitespace
                        insertString(token, offset, indentString.substring(indentStringInd));
                        offset += indentStringLen - indentStringInd; // skip WS
                        indentStringInd = indentStringLen;

                    } else { // cannot modify the whitespace of this token
                        if (isWhitespace(token, offset) || offset > 0) {
                            throw new IllegalStateException(
                                "Cannot modify token=" + token); // NOI18N

                        } else { // nonWS token at begining, will insert WS
                            insertToken(token, getValidWhitespaceTokenID(),
                                getValidWhitespaceTokenContextPath(),
                                indentString.substring(indentStringInd)
                            );
                            return getPosition(token, 0);
                        }
                    }
                                
                } else { // current char matches indentString
                    indentStringInd++; // advance inside indentString
                    offset++;
                }
            }

            if (indentStringInd < indentStringLen) { // move to next token
                token = token.getNext();
                if (token == null) { // was last token, insert WS token
                    token = insertToken(null, getValidWhitespaceTokenID(),
                        getValidWhitespaceTokenContextPath(),
                        indentString.substring(indentStringInd)
                    );
                    return getPosition(token, 0);

                } else { // non-null token
                    offset = 0;
                }

            } else { // indent already done, need to remove all the resting WS

                while (true) {
                    text = token.getImage();
                    textLen = text.length();
                    int removeInd = -1;

                    while (offset < textLen) {
                        if (!isWhitespace(token, offset) || text.charAt(offset) == '\n') {
                            if (removeInd >= 0) {
                                remove(token, removeInd, offset - removeInd);
                                offset = removeInd;
                            }

                            return getPosition(token, offset);

                        } else { // whitespace char found
                            if (removeInd < 0) {
                                removeInd = offset;
                            }
                        }
                        offset++;
                    }

                    if (removeInd == -1) { // nothing to remove
                        token = token.getNext(); // was right at the end

                    } else if (removeInd == 0) { // remove whole token
                        TokenItem nextToken = token.getNext();
                        removeToken(token);
                        token = nextToken;

                    } else { // remove just end part of token
                        remove(token, removeInd, textLen - removeInd);
                        token = token.getNext();
                    }
                    offset = 0;

                    if (token == null) {
                        return getPosition(null, 0);
                    }
                }
            }
        }
    }

    /** Debug the current state of the chain.
    * @param token mark this token as current one. It can be null.
    */
    public String chainToString(TokenItem token) {
        return formatWriter.chainToString(token);
    }

    public String chainToString(TokenItem token, int maxDocumentTokens) {
        return formatWriter.chainToString(token, maxDocumentTokens);
    }

}
