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

import java.io.IOException;
import java.io.Writer;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.editor.BaseKit;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;
import org.netbeans.editor.TokenContextPath;
import org.netbeans.editor.TokenItem;
import org.netbeans.editor.Utilities;
import org.netbeans.editor.EditorDebug;

/**
* Formatting writter accepts the input-text, formats it
* and writes the output to the underlying writer.
* The data written to the writer are immediately splitted
* into token-items and the chain of token-items is created
* from them. Then the writer then waits for the flush() or close()
* method call which then makes the real formatting.
* The formatting is done through going through the format-layers
* registered in <tt>ExtFormatter</tt> and asking them for formatting.
* These layers go through the chain and possibly add or remove
* the tokens as necessary. The good thing is that the layers
* can ask the tokens before those written to the writer.
* In that case they will get the tokens from the document at point
* the formatting should be done. The advantage is that the chain
* is compact so the border between the tokens written to the writer
* and those that come from the document is invisible.
*
* @author Miloslav Metelka
* @version 1.00
*/

public final class FormatWriter extends Writer {

    /** Whether debug messages should be displayed */
    public static final boolean debug
        = Boolean.getBoolean("netbeans.debug.editor.format"); // NOI18N

    /** Whether debug messages should be displayed */
    public static final boolean debugModify
        = Boolean.getBoolean("netbeans.debug.editor.format.modify"); // NOI18N

    private static final char[] EMPTY_BUFFER = new char[0];

    /** Formatter related to this format-writer */
    private ExtFormatter formatter;

    /** Document being formatted */
    private Document doc;

    /** Offset at which the formatting occurs */
    private int offset;

    /** Underlying writer */
    private Writer underWriter;

    /** Syntax scanning the characters passed to the writer. For non-BaseDocuments
    * it also scans the characters preceding the format offset. The goal here
    * is to maintain the formatted tokens consistent with the scanning context 
    * at the offset in the document. This is achieved differently
    * depending on whether the document is an instance of the BaseDocument
    * or not.
    * If the document is an instance of the <tt>BaseDocument</tt>,
    * the syntax is used solely for the scanning of the formatted tokens
    * and it is first prepared to scan right after the offset.
    * For non-BaseDocuments the syntax first scans the whole are
    * from the begining of the document till the offset and then continues
    * on with the formatted tokens.
    */
    private Syntax syntax;

    /** Whether the purpose is to find an indentation
    *  instead of formatting the tokens. In this mode only the '\n' is written
    *  to the format-writer, and the layers should insert the appropriate
    *  white-space tokens before the '\n' that will form
    *  the indentation of the line.
    */
    private boolean indentOnly;

    /** Buffer being scanned */
    private char[] buffer;

    /** Number of the valid chars in the buffer */
    private int bufferSize;

    /** Support for creating the positions. */
    private FormatTokenPositionSupport ftps;

    /** Prescan at the offset position */
    private int offsetPreScan;

    /** Whether the first flush() is being done.
     * it must respect the offsetPreScan.
     */
    private boolean firstFlush;

    /** Last token-item in the chain */
    private ExtTokenItem lastToken;

    /** Position where the formatting should start. */
    private FormatTokenPosition formatStartPosition;

    /** The first position that doesn't belong to the document. */
    private FormatTokenPosition textStartPosition;

    /** This flag is set automatically if the new removal or insertion
    * into chain occurs. The formatter can use this flag to detect whether
    * a particular format-layer changed the chain.
    */
    private boolean chainModified;

    /** Whether the format should be restarted. */
    private boolean restartFormat;

    /** Flag that helps to avoid unnecessary formatting when
    * calling flush() periodically without calling write()
    */
    private boolean lastFlush;

    /** Shift resulting indentation position to which the caret is moved.
     * By default the caret goes to the first non-whitespace character
     * on the formatted line. If the line is empty then to the end of the
     * indentation whitespace. This variable enables to move the resulting
     * position either left or right.
     */
    private int indentShift;

    /** Whether this format writer runs in the simple mode.
     * In simple mode the input is directly written to output.
     */
    private boolean simple;
    
    /** Added to fix #5620 */
    private boolean reformatting;
    /** Added to fix #5620 */
    void setReformatting(boolean reformatting) {
        this.reformatting = reformatting;
    }

    /** The format writers should not be extended to enable
    * operating of the layers on all the writers even for different
    * languages.
    * @param underWriter underlying writer
    */
    FormatWriter(ExtFormatter formatter, Document doc, int offset,
                 Writer underWriter, boolean indentOnly) {
        this.formatter = formatter;
        this.doc = doc;
        this.offset = offset;
        this.underWriter = underWriter;
        this.setIndentOnly(indentOnly);

        if (debug) {
            System.err.println("FormatWriter() created, formatter=" + formatter // NOI18N
                + ", document=" + doc.getClass() + ", expandTabs=" + formatter.expandTabs() // NOI18N
                + ", spacesPerTab=" + formatter.getSpacesPerTab() // NOI18N
                + ", tabSize=" + ((doc instanceof BaseDocument) // NOI18N
                    ? ((BaseDocument)doc).getTabSize() : formatter.getTabSize())
                + ", shiftWidth=" + ((doc instanceof BaseDocument) // NOI18N
                    ? ((BaseDocument)doc).getShiftWidth() : formatter.getShiftWidth())
            );
        }

        // Return now for simple formatter
        if (formatter.isSimple()) {
            simple = true;
            return;
        }

        buffer = EMPTY_BUFFER;
        firstFlush = true;

        // Hack for getting the right kit and then syntax
        Class kitClass = (doc instanceof BaseDocument)
            ? ((BaseDocument)doc).getKitClass()
            : formatter.getKitClass();

        if (kitClass != null && BaseKit.class.isAssignableFrom(kitClass)) {
            syntax = BaseKit.getKit(kitClass).createFormatSyntax(doc);
        } else {
            simple = true;
            return;
        }

        if (!formatter.acceptSyntax(syntax)) {
            simple = true; // turn to simple format writer
            return;
        }

        ftps = new FormatTokenPositionSupport(this);

        if (doc instanceof BaseDocument) {
            try {
                BaseDocument bdoc = (BaseDocument)doc;

                /* Init syntax right at the formatting offset so it will
                 * contain the prescan characters only. The non-last-buffer
                 * is inforced (even when at the document end) because the 
                 * text will follow the current document text.
                 */
                bdoc.getSyntaxSupport().initSyntax(syntax, offset, offset, false, true);
                offsetPreScan = syntax.getPreScan();

                if (debug) {
                    System.err.println("FormatWriter: preScan=" + offsetPreScan + " at offset=" + offset);
                }

                if (offset > 0) { // only if not formatting from the start of the document
                    ExtSyntaxSupport sup = (ExtSyntaxSupport)bdoc.getSyntaxSupport();
                    Integer lines = (Integer)bdoc.getProperty(BaseDocument.LINE_BATCH_SIZE);

                    int startOffset = Utilities.getRowStart(bdoc,
                            Math.max(offset - offsetPreScan, 0),
                            -Math.max(lines.intValue(), 1)
                    );

                    if (startOffset < 0) { // invalid line
                        startOffset = 0;
                    }

                    // Parse tokens till the offset
                    TokenItem ti = sup.getTokenChain(startOffset, offset);

                    if (ti != null && ti.getOffset() < offset - offsetPreScan) {
                        lastToken = new FilterDocumentItem(ti, null, false);

                        if (debug) {
                            System.err.println("FormatWriter: first doc token=" + lastToken); // NOI18N
                        }

                        // Iterate through the chain till the last item
                        while (lastToken.getNext() != null
                                && lastToken.getNext().getOffset() < offset - offsetPreScan
                        ) {
                            lastToken = (ExtTokenItem)lastToken.getNext();

                            if (debug) {
                                System.err.println("FormatWriter: doc token=" + lastToken); // NOI18N
                            }
                        }

                        // Terminate the end of chain so it doesn't try
                        // to append the next token from the document
                        ((FilterDocumentItem)lastToken).terminate();

                    }
                }

            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            }

        } else { // non-BaseDocument
            try {
                String text = doc.getText(0, offset);
                char[] charBuffer = text.toCharArray();

                // Force non-last buffer
                syntax.load(null, charBuffer, 0, charBuffer.length, false, 0);

                TokenID tokenID = syntax.nextToken();
                while (tokenID != null) {
                    int tokenOffset = syntax.getTokenOffset();
                    lastToken = new FormatTokenItem(tokenID,
                            syntax.getTokenContextPath(),
                            tokenOffset,
                            text.substring(tokenOffset, tokenOffset + syntax.getTokenLength()),
                            lastToken
                    );

                    if (debug) {
                        System.err.println("FormatWriter: non-bd token=" + lastToken);
                    }

                    ((FormatTokenItem)lastToken).markWritten();
                    tokenID = syntax.nextToken(); 
                }

                // Assign the preScan
                offsetPreScan = syntax.getPreScan();

            } catch (BadLocationException e) {
                Utilities.annotateLoggable(e);
            }
        }

        // Write the preScan characters
        char[] buf = syntax.getBuffer();
        int bufOffset = syntax.getOffset();

        if (debug) {
            System.err.println("FormatWriter: writing preScan chars='" // NOI18N
                    + EditorDebug.debugChars(buf, bufOffset - offsetPreScan,
                        offsetPreScan) + "'" // NOI18N
                    + ", length=" + offsetPreScan // NOI18N
            );
        }

        // Write the preScan chars to the buffer
        addToBuffer(buf, bufOffset - offsetPreScan, offsetPreScan);
    }

    public final ExtFormatter getFormatter() {
        return formatter;
    }

    /** Get the document being formatted */
    public final Document getDocument() {
        return doc;
    }

    /** Get the starting offset of the formatting */
    public final int getOffset() {
        return offset;
    }

    /** Whether the purpose of this writer is to find the proper indentation
     * instead of formatting the tokens. It allows to have a modified
     * formatting behavior for the cases when user presses Enter or a key
     * that causes immediate reformatting of the line.
     */
    public final boolean isIndentOnly() {
        return indentOnly;
    }
    
    /** Sets whether the purpose of this writer is to find the proper indentation
     * instead of formatting the tokens.
     * @see isIndentOnly()
     */
    public void setIndentOnly(boolean indentOnly) {
        this.indentOnly = indentOnly;
    }

    /** Get the first token that should be formatted.
    * This can change as the format-layers continue to change the token-chain.
    * If the caller calls flush(), this method will return null. After
    * additional writing to the writer, new tokens will be added and
    * the first one of them will become the first token to be formatted.
    * @return the first token that should be formatted. It can be null
    *  in case some layer removes all the tokens that should be formatted.
    *  Most of the layers probably do nothing in case this value is null.
    */
    public FormatTokenPosition getFormatStartPosition() {
        return formatStartPosition;
    }

    /** Get the first position that doesn't belong to the document.
     * Initially it's the same as the <tt>getFormatStartPosition()</tt> but
     * if there are multiple flushes performed on the writer they will differ.
     */
    public FormatTokenPosition getTextStartPosition() {
        return textStartPosition;
    }

    /** Get the last token in the chain. It can be null
    * if there are no tokens in the chain.
    */
    public TokenItem getLastToken() {
        return lastToken;
    }

    /** Find the first token in the chain. It should be used only when necessary
     * and possibly in situations when the start of the chain
     * was already reached by other methods, because this method
     * will extend the chain till the begining of the document.
     * @param token token from which the search for previous tokens will
     *  start. It can be null in which case the last document token or last
     *  token are attempted instead.
     */
    public TokenItem findFirstToken(TokenItem token) {
        if (token == null) {
            // Try textStartPosition first
            token = (textStartPosition != null)
                ? textStartPosition.getToken() : null;

            if (token == null) {
                // Try starting of the formatting position next
                token = formatStartPosition.getToken();
                if (token == null) {
                    token = lastToken;
                    if (token == null) {
                        return null;
                    }
                }
            }
        }

        while (token.getPrevious() != null) {
            token = token.getPrevious();
        }
        return token;
    }

    /** It checks whether the tested token is after some other token in the chain.
     * @param testedToken token to test (whether it's after afterToken or not)
     * @param afterToken token to be compared to the testedToken
     * @return true whether the testedToken is after afterToken or not.
     *   Returns false if the token == afterToken
     *   or not or if token is before the afterToken or not.
     */
    public boolean isAfter(TokenItem testedToken, TokenItem afterToken) {
        while (afterToken != null) {
            afterToken = afterToken.getNext();

            if (afterToken == testedToken) {
                return true;
            }
        }

        return false;
    }

    /** Checks whether the tested position is after some other position. */ 
    public boolean isAfter(FormatTokenPosition testedPosition,
    FormatTokenPosition afterPosition) {
        if (testedPosition.getToken() == afterPosition.getToken()) {
            return (testedPosition.getOffset() > afterPosition.getOffset());

        } else { // different tokens
            return isAfter(testedPosition.getToken(), afterPosition.getToken());
        }
    }

    /** Check whether the given token has empty text and if so
    * start searching for token with non-empty text in the given
    * direction. If there's no non-empty token in the given direction
    * the method returns null.
    * @param token token to start to search from. If it has zero
    *  length, the search for non-empty token is performed in the given
    *  direction.
    */
    public TokenItem findNonEmptyToken(TokenItem token, boolean backward) {
        while (token != null && token.getImage().length() == 0) {
            token = backward ? token.getPrevious() : token.getNext();
        }

        return token;
    }

    /** Check whether a new token can be inserted into the chain
    * before the given token-item. The token
    * can be inserted only into the tokens that come
    * from the text that was written to the format-writer
    * but was not yet written to the underlying writer.
    * @param beforeToken token-item before which
    *  the new token-item is about to be inserted. It can
    *  be null to append the new token to the end of the chain.
    */
    public boolean canInsertToken(TokenItem beforeToken) {
        return beforeToken== null // appending to the end
               || !((ExtTokenItem)beforeToken).isWritten();
    }

    /** Create a new token-item and insert it before
    * the token-item given as parameter.
    * The <tt>canInsertToken()</tt> should be called
    * first to determine whether the given token can
    * be inserted into the chain or not. The token
    * can be inserted only into the tokens that come
    * from the text that was written to the format-writer
    * but was not yet written to the underlying writer.
    * @param beforeToken token-item before which
    *  the new token-item is about to be inserted. It can
    *  be null to append the new token to the end of the chain.
    * @param tokenID token-id of the new token-item
    * @param tokenContextPath token-context-path of the new token-item
    * @param tokenImage image of the new token-item
    */
    public TokenItem insertToken(TokenItem beforeToken,
    TokenID tokenID, TokenContextPath tokenContextPath, String tokenImage) {
        if (debugModify) {
            System.err.println("FormatWriter.insertToken(): beforeToken=" + beforeToken // NOI18N
                + ", tokenID=" + tokenID + ", contextPath=" + tokenContextPath // NOI18N
                + ", tokenImage='" + tokenImage + "'" // NOI18N
            );
        }

        if (!canInsertToken(beforeToken)) {
            throw new IllegalStateException("Can't insert token into chain"); // NOI18N
        }

        // #5620
        if (reformatting) {
            try {
                doc.insertString(getDocOffset(beforeToken), tokenImage, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
        
        FormatTokenItem fti;
        if (beforeToken != null) {
            fti = ((FormatTokenItem)beforeToken).insertToken(tokenID,
                    tokenContextPath, -1, tokenImage);

        } else { // beforeToken is null
            fti = new FormatTokenItem(tokenID, tokenContextPath, -1, tokenImage, lastToken);
            lastToken = fti;
        }

        // Update token-positions
        ftps.tokenInsert(fti);

        chainModified = true;

        return fti;
    }

    /** Added to fix #5620 */
    private int getDocOffset(TokenItem token) {
        int len = 0;
        if (token != null) {
            token = token.getPrevious();
            
        } else { // after last token
            token = lastToken;
        }

        while (token != null) {
            len += token.getImage().length();
            if (token instanceof FilterDocumentItem) {
                 return len + token.getOffset();
            }
            token = token.getPrevious();
        }
        
        return len;
    }
    
    /** Whether the token-item can be removed. It can be removed
    * only in case it doesn't come from the document's text
    * and it wasn't yet written to the underlying writer.
    */
    public boolean canRemoveToken(TokenItem token) {
        return !((ExtTokenItem)token).isWritten();
    }

    /** Remove the token-item from the chain. It can be removed
    * only in case it doesn't come from the document's text
    * and it wasn't yet written to the underlying writer.
    */
    public void removeToken(TokenItem token) {
        if (debugModify) {
            System.err.println("FormatWriter.removeToken(): token=" + token); // NOI18N
        }

        if (!canRemoveToken(token)) {
            if (true) { // !!!
                return;
            }
            throw new IllegalStateException("Can't remove token from chain"); // NOI18N
        }

        // #5620
        if (reformatting) {
            try {
                doc.remove(getDocOffset(token), token.getImage().length());
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        // Update token-positions
        ftps.tokenRemove(token);

        if (lastToken == token) {
            lastToken = (ExtTokenItem)token.getPrevious();
        }
        ((FormatTokenItem)token).remove(); // remove self from chain

        chainModified = true;
    }

    public boolean canSplitStart(TokenItem token, int startLength) {
        return !((ExtTokenItem)token).isWritten();
    }

    /** Create the additional token from the text at the start
     * of the given token.
     * @param token token being split.
     * @param startLength length of the text at the begining of the token
     *   for which the additional token will be created.
     * @param tokenID token-id that will be assigned to the new token
     * @param tokenContextPath token-context-path that will be assigned
     *   to the new token
     */
    public TokenItem splitStart(TokenItem token, int startLength,
    TokenID newTokenID, TokenContextPath newTokenContextPath) {
        if (!canSplitStart(token, startLength)) {
            throw new IllegalStateException("Can't split the token=" + token); // NOI18N
        }

        String text = token.getImage();
        if (startLength > text.length()) {
            throw new IllegalArgumentException("startLength=" + startLength // NOI18N
                + " is greater than token length=" + text.length()); // NOI18N
        }

        String newText = text.substring(0, startLength);
        ExtTokenItem newToken = (ExtTokenItem)insertToken(token,
                newTokenID, newTokenContextPath, newText);

        // Update token-positions
        ftps.splitStartTokenPositions(token, startLength);

        remove(token, 0, startLength);
        return newToken;
    }

    public boolean canSplitEnd(TokenItem token, int endLength) {
        int splitOffset = token.getImage().length() - endLength;
        return (((ExtTokenItem)token).getWrittenLength() <= splitOffset);
    }

    /** Create the additional token from the text at the end
     * of the given token.
     * @param token token being split.
     * @param endLength length of the text at the end of the token
     *   for which the additional token will be created.
     * @param tokenID token-id that will be assigned to the new token
     * @param tokenContextPath token-context-path that will be assigned
     *   to the new token
     */
    public TokenItem splitEnd(TokenItem token, int endLength,
    TokenID newTokenID, TokenContextPath newTokenContextPath) {
        if (!canSplitEnd(token, endLength)) {
            throw new IllegalStateException("Can't split the token=" + token); // NOI18N
        }

        String text = token.getImage();
        if (endLength > text.length()) {
            throw new IllegalArgumentException("endLength=" + endLength // NOI18N
                + " is greater than token length=" + text.length()); // NOI18N
        }

        String newText = text.substring(0, endLength);
        ExtTokenItem newToken = (ExtTokenItem)insertToken(token.getNext(),
                newTokenID, newTokenContextPath, newText);

        // Update token-positions
        ftps.splitEndTokenPositions(token, endLength);

        remove(token, text.length() - endLength, endLength);
        return newToken;
    }

    /** Whether the token can be modified either by insertion or removal
     * at the given offset.
     */
    public boolean canModifyToken(TokenItem token, int offset) {
        int wrLen = ((ExtTokenItem)token).getWrittenLength();
        return (offset >= 0 && wrLen <= offset);
    }

    /** Insert the text at the offset inside the given token.
     * All the token-positions at and after the offset will 
     * be increased by <tt>text.length()</tt>.
     * <tt>IllegalArgumentException</tt> is thrown if offset
     * is wrong.
     * @param token token in which the text is inserted.
     * @param offset offset at which the text will be inserted.
     * @param text text that will be inserted at the offset.
     */
    public void insertString(TokenItem token, int offset, String text) {
        // Check debugging
        if (debugModify) {
            System.err.println("FormatWriter.insertString(): token=" + token // NOI18N
                + ", offset=" + offset + ", text='" + text + "'"); // NOI18N
        }

        // Check empty insert
        if (text.length() == 0) {
            return;
        }

        // Check whether modification is allowed
        if (!canModifyToken(token, offset)) {
            if (true) { // !!!
                return;
            }
            throw new IllegalStateException("Can't insert into token=" + token // NOI18N
                    + ", at offset=" + offset + ", text='" + text + "'"); // NOI18N
        }
        
        // #5620
        if (reformatting) {
            try {
                doc.insertString(getDocOffset(token) + offset, text, null);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }


        // Update token-positions
        ftps.tokenTextInsert(token, offset, text.length());

        String image = token.getImage();
        ((ExtTokenItem)token).setImage(image.substring(0, offset) + text
                  + image.substring(offset));
    }

    /** Remove the length of the characters at the given
     * offset inside the given token.
     * <tt>IllegalArgumentException</tt> is thrown if offset
     * or length are wrong.
     * @param token token in which the text is removed.
     * @param offset offset at which the text will be removed.
     * @param length length of the removed text.
     */
    public void remove(TokenItem token, int offset, int length) {
        // Check debugging
        if (debugModify) {
            String removedText;
            if (offset >= 0 && length >= 0
                && offset + length <= token.getImage().length()
            ) {
                removedText = token.getImage().substring(offset, offset + length);

            } else {
                removedText = "<INVALID>"; // NOI18N
            }

            System.err.println("FormatWriter.remove(): token=" + token // NOI18N
                + ", offset=" + offset + ", length=" + length // NOI18N
                + "removing text='" + removedText + "'"); // NOI18N
        }

        // Check empty remove
        if (length == 0) {
            return;
        }

        // Check whether modification is allowed
        if (!canModifyToken(token, offset)) {
            if (true) { // !!!
                return;
            }
            throw new IllegalStateException("Can't remove from token=" + token // NOI18N
                    + ", at offset=" + offset + ", length=" + length); // NOI18N
        }

        // #5620
        if (reformatting) {
            try {
                doc.remove(getDocOffset(token) + offset, length);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        // Update token-positions
        ftps.tokenTextRemove(token, offset, length);

        String text = token.getImage();
        ((ExtTokenItem)token).setImage(text.substring(0, offset)
                    + text.substring(offset + length));
    }

    /** Get the token-position that corresponds to the given
     * offset inside the given token. The returned position is persistent
     * and if the token is removed from chain the position
     * is assigned to the end of the previous token or to the begining
     * of the next token if there's no previous token.
     * @param token token in which the position is created.
     * @param offset inside the token at which the position
     *   will be created.
     * @param bias forward or backward bias
     */
    public FormatTokenPosition getPosition(TokenItem token, int offset, Position.Bias bias) {
        return ftps.getTokenPosition(token, offset, bias);
    }

    /** Check whether this is the first position in the chain of tokens. */
    public boolean isChainStartPosition(FormatTokenPosition pos) {
        TokenItem token = pos.getToken();
        return (pos.getOffset() == 0)
            && ((token == null && getLastToken() == null) // no tokens
            || (token != null && token.getPrevious() == null));
    }

    /** Add the given chars to the current buffer of chars to format. */
    private void addToBuffer(char[] buf, int off, int len) {
        // If necessary increase the buffer size
        if (len > buffer.length - bufferSize) {
            char[] tmp = new char[len + 2 * buffer.length];
            System.arraycopy(buffer, 0, tmp, 0, bufferSize);
            buffer = tmp;
        }

        // Copy the characters
        System.arraycopy(buf, off, buffer, bufferSize, len);
        bufferSize += len;
    }

    public void write(char[] cbuf, int off, int len) throws IOException {
        if (simple) {
            underWriter.write(cbuf, off, len);
            return;
        }

        write(cbuf, off, len, null, null);
    }

    public synchronized void write(char[] cbuf, int off, int len,
    int[] saveOffsets, Position.Bias[] saveBiases) throws IOException {
        if (simple) {
            underWriter.write(cbuf, off, len);
            return;
        }

        if (saveOffsets != null) {
            ftps.addSaveSet(bufferSize, len, saveOffsets, saveBiases);
        }

        lastFlush = false; // signal write() was the last so flush() can be done

        if (debug) {
            System.err.println("FormatWriter.write(): '" // NOI18N
                    + org.netbeans.editor.EditorDebug.debugChars(cbuf, off, len)
                    + "', length=" + len + ", bufferSize=" + bufferSize); // NOI18N
        }

        // Add the chars to the buffer for formatting
        addToBuffer(cbuf, off, len);
    }

    /** Return the flag that is set automatically if the new removal or insertion
    * into chain occurs. The formatter can use this flag to detect whether
    * a particular format-layer changed the chain or not.
    */
    public boolean isChainModified() {
        return chainModified;
    }

    public void setChainModified(boolean chainModified) {
        this.chainModified = chainModified;
    }

    /** Return whether the layer requested to restart the format. The formatter
    * can use this flag to restart the formatting from the first layer.
    */
    public boolean isRestartFormat() {
        return restartFormat;
    }

    public void setRestartFormat(boolean restartFormat) {
        this.restartFormat = restartFormat;
    }

    public int getIndentShift() {
        return indentShift;
    }

    public void setIndentShift(int indentShift) {
        this.indentShift = indentShift;
    }

    public void flush() throws IOException {
        if (debug) {
            System.err.println("FormatWriter.flush() called"); // NOI18N
        }

        if (simple) {
            underWriter.flush();
            return;
        }

        if (lastFlush) { // flush already done
            return;
        }
        lastFlush = true; // flush is being done

        int startOffset = 0; // offset where syntax will start scanning
        if (firstFlush) { // must respect the offsetPreScan
            startOffset = offsetPreScan;
        }

        syntax.relocate(buffer, startOffset, bufferSize - startOffset, true, -1);

        // Reset formatStartPosition so that it will get filled with new value
        formatStartPosition = null;

        TokenID tokenID = syntax.nextToken();
        if (firstFlush) { // doing first flush
            if (startOffset > 0) { // check whether there's a preScan
                while(true)
                {
                String text = new String(buffer, syntax.getTokenOffset(),
                                              syntax.getTokenLength());
                // add a new token-item to the chain
                lastToken = new FormatTokenItem(tokenID,
                        syntax.getTokenContextPath(), -1, text, lastToken);

                if (debug) {
                    System.err.println("FormatWriter.flush(): doc&format token=" // NOI18N
                            + lastToken);
                }

                // Set that it was only partially written
                lastToken.setWrittenLength(startOffset);

                // If the start position is inside this token, assign it
                if (text.length() > startOffset) {
                    formatStartPosition = getPosition(lastToken, startOffset,
                            Position.Bias.Backward);
                }

                tokenID = syntax.nextToken(); // get next token
                
                // When force last buffer is true, the XML token chain can be split
                // into more than one token. This does not happen for Java tokens.
                // Because of this split must all tokens which are part of preScan
                // (means which have end position smaller than startOffset), be changed to
                // "unmodifiable" and only the last one token will be used.
                // see issue 12701
                if (text.length() >= startOffset)
                    break;
                else
                {
                    lastToken.setWrittenLength(Integer.MAX_VALUE);
                    startOffset -= text.length();
                }
                }

            }
        }

        while (tokenID != null) {
            String text = new String(buffer, syntax.getTokenOffset(),
                                          syntax.getTokenLength());
            // add a new token-item to the chain
            lastToken = new FormatTokenItem(tokenID,
                    syntax.getTokenContextPath(), -1, text, lastToken);

            if (formatStartPosition == null) {
                formatStartPosition = getPosition(lastToken, 0,
                        Position.Bias.Backward);
            }

            if (debug) {
                System.err.println("FormatWriter.flush(): format token=" + lastToken);
            }

            tokenID = syntax.nextToken();
        }
        
        // Assign formatStartPosition even if there are no tokens
        if (formatStartPosition == null) {
            formatStartPosition = getPosition(null, 0, Position.Bias.Backward);
        }

        // Assign textStartPosition if this is the first flush
        if (firstFlush) {
            textStartPosition = formatStartPosition;
        }

        bufferSize = 0; // reset the current buffer size

        if (debug) {
            System.err.println("FormatWriter.flush(): formatting ...");
        }

        // Format the tokens
        formatter.format(this);

        // Write the output tokens to the underlying writer marking them as written
        StringBuffer sb = new StringBuffer();
        ExtTokenItem token = (ExtTokenItem)formatStartPosition.getToken();
        ExtTokenItem prevToken = null;
        if (token != null) {
            // Process the first token
            switch (token.getWrittenLength()) {
                case -1: // write whole token
                    sb.append(token.getImage());
                    break;

                case Integer.MAX_VALUE:
                    throw new IllegalStateException("Wrong formatStartPosition"); // NOI18N

                default:
                    sb.append(token.getImage().substring(formatStartPosition.getOffset()));
                    break;
            }

            token.markWritten();
            prevToken = token;
            token = (ExtTokenItem)token.getNext();

            // Process additional tokens
            while (token != null) {
                // First mark the previous token that it can't be extended
                prevToken.setWrittenLength(Integer.MAX_VALUE);

                // Write current token and mark it as written
                sb.append(token.getImage());
                token.markWritten();

                // Goto next token
                prevToken = token;
                token = (ExtTokenItem)token.getNext();
            }
        }

        // Write to the underlying writer
        if (sb.length() > 0) {
            char[] outBuf = new char[sb.length()];
            sb.getChars(0, outBuf.length, outBuf, 0);

            if (debug) {
                System.err.println("FormatWriter.flush(): chars to underlying writer='" // NOI18N
                        + EditorDebug.debugChars(outBuf, 0, outBuf.length)
                        + "'"); // NOI18N
            }

            underWriter.write(outBuf, 0, outBuf.length);
        }

        underWriter.flush();

        firstFlush = false; // no more first flush
    }

    public void close() throws IOException {
        if (debug) {
            System.err.println("FormatWriter: close() called (-> flush())"); // NOI18N
        }

        flush();
        underWriter.close();
    }

    /** Check the chain whether it's OK. */
    public void checkChain() {
        // Check whether the lastToken is really last
        TokenItem lt = getLastToken();
        if (lt.getNext() != null) {
            throw new IllegalStateException("Successor of last token exists."); // NOI18N
        }

        // Check whether formatStartPosition is non-null
        FormatTokenPosition fsp = getFormatStartPosition();
        if (fsp == null) {
            throw new IllegalStateException("getFormatStartPosition() returns null."); // NOI18N
        }

        // Check whether formatStartPosition follows textStartPosition
        checkFSPFollowsTSP();

        // !!! implement checks:
        // Check whether all the document tokens have written flag true
        // Check whether all formatted tokens are writable
    }

    /** Check whether formatStartPosition follows the textStartPosition */
    private void checkFSPFollowsTSP() {
        if (!(formatStartPosition.equals(textStartPosition)
                || isAfter(formatStartPosition, textStartPosition)
        )) {
            throw new IllegalStateException(
                    "formatStartPosition doesn't follow textStartPosition"); // NOI18N
        }
    }

    public String chainToString(TokenItem token) {
        return chainToString(token, 5);
    }

    /** Debug the current state of the chain.
    * @param token mark this token as current one. It can be null.
    * @param maxDocumentTokens how many document tokens should be shown.
    */
    public String chainToString(TokenItem token, int maxDocumentTokens) {
        // First check the chain whether it's correct
        checkChain();

        StringBuffer sb = new StringBuffer();
        sb.append("D - document tokens, W - written tokens, F - tokens being formatted\n"); // NOI18N

        // Check whether format-start position follows textStartPosition
        checkFSPFollowsTSP();

        TokenItem tst = getTextStartPosition().getToken();
        TokenItem fst = getFormatStartPosition().getToken();

        // Goto maxDocumentTokens back from the tst
        TokenItem t = tst;
        if (t == null) {
            t = getLastToken();
        }

        // Go back through document tokens
        while (t != null && t.getPrevious() != null && --maxDocumentTokens > 0) {
            t = t.getPrevious();
        }

        // Display the document tokens
        while (t != tst) {
            sb.append((t == token) ? '>' : ' '); // NOI18N
            sb.append("D  "); // NOI18N
            sb.append(t.toString());
            sb.append('\n'); // NOI18N

            t = t.getNext();
        }

        while (t != fst) {
            sb.append((t == token) ? '>' : ' ');
            if (t == tst) { // found last document token
                sb.append("D(" + getTextStartPosition().getOffset() + ')'); // NOI18N
            }
            sb.append("W "); // NOI18N
            sb.append(t.toString());
            sb.append('\n'); // NOI18N

            t = t.getNext();
        }

        sb.append((t == token) ? '>' : ' ');
        if (getFormatStartPosition().getOffset() > 0) {
            if (fst == tst) {
                sb.append('D');

            } else { // means something was already formatted
                sb.append('W'); // NOI18N
            }
        }
        sb.append("F "); // NOI18N
        sb.append((t != null) ? t.toString() : "NULL"); // NOI18N
        sb.append('\n'); // NOI18N

        if (t != null) {
            t = t.getNext();
        }

        while (t != null) {
            sb.append((t == token) ? '>' : ' '); // NOI18N
            sb.append("F "); // NOI18N
            sb.append(t.toString());
            sb.append('\n'); // NOI18N

            t = t.getNext();
        }

        return sb.toString();
    }

    /** Token-item created for the tokens that come from the text
    * written to the writer.
    */
    static class FormatTokenItem extends TokenItem.AbstractItem
    implements ExtTokenItem {

        /** How big part of the token was already written
         * to the underlying writer. -1 means nothing was written yet,
         * Integer.MAX_VALUE means everything was written and the token
         * cannot be extended and some other value means how big part
         * was already written.
         */
        int writtenLength = -1;

        /** Next token in chain */
        TokenItem next;

        /** Previous token in chain */
        TokenItem previous;

        /** Image of the token. It's changeable. */
        String image;

        /** Save offset used to give the relative position
         * to the start of the current formatting. It's used
         * by the FormatTokenPositionSupport.
         */
        int saveOffset;

        FormatTokenItem(TokenID tokenID, TokenContextPath tokenContextPath,
        int offset, String image, TokenItem previous) {
            super(tokenID, tokenContextPath, offset, image);
            this.image = image;
            this.previous = previous;
            if (previous instanceof ExtTokenItem) {
                ((ExtTokenItem)previous).setNext(this);
            }
        }

        public TokenItem getNext() {
            return next;
        }

        public TokenItem getPrevious() {
            return previous;
        }

        public void setNext(TokenItem next) {
            this.next = next;
        }

        public void setPrevious(TokenItem previous) {
            this.previous = previous;
        }

        public boolean isWritten() {
            return (writtenLength >= 0);
        }

        public void markWritten() {
            if (writtenLength == Integer.MAX_VALUE) {
                throw new IllegalStateException("Already marked unextendable."); // NOI18N
            }

            writtenLength = getImage().length();
        }

        public int getWrittenLength() {
            return writtenLength;
        }

        public void setWrittenLength(int writtenLength) {
            if (writtenLength <= this.writtenLength) {
                throw new IllegalArgumentException(
                        "this.writtenLength=" + this.writtenLength // NOI18N
                        + " < writtenLength=" + writtenLength); // NOI18N
            }

            this.writtenLength = writtenLength;
        }

        public @Override String getImage() {
            return image;
        }

        public void setImage(String image) {
            this.image = image;
        }

        FormatTokenItem insertToken(TokenID tokenID, TokenContextPath tokenContextPath,
        int offset, String image) {
            FormatTokenItem fti = new FormatTokenItem(tokenID, tokenContextPath,
                    offset, image, previous);
            fti.next = this;
            this.previous = fti;
            return fti;
        }

        void remove() {
            if (previous instanceof ExtTokenItem) {
                ((ExtTokenItem)this.previous).setNext(next);
            }
            if (next instanceof ExtTokenItem) {
                ((ExtTokenItem)this.next).setPrevious(previous);
            }
        }

        int getSaveOffset() {
            return saveOffset;
        }

        void setSaveOffset(int saveOffset) {
            this.saveOffset = saveOffset;
        }

    }

    /** This token item wraps every token-item that comes
    * from the syntax-support.
    */
    static class FilterDocumentItem extends TokenItem.FilterItem
    implements ExtTokenItem {

        private static final FilterDocumentItem NULL_ITEM
            = new FilterDocumentItem(null, null, false);

        private TokenItem previous;

        private TokenItem next;

        FilterDocumentItem(TokenItem delegate, FilterDocumentItem neighbour, boolean isNeighbourPrevious) {
            super(delegate);
            if (neighbour != null) {
                if (isNeighbourPrevious) {
                    previous = neighbour;

                } else { // neighbour is next
                    next = neighbour;
                }
            }
        }

        public @Override TokenItem getNext() {
            if (next == null) {
                TokenItem ti = super.getNext();
                if (ti != null) {
                    next = new FilterDocumentItem(ti, this, true);
                }
            }

            return (next != NULL_ITEM) ? next : null;
        }

        public void setNext(TokenItem next) {
            this.next = next;
        }

        /** Change the next item to the NULL one. */
        public void terminate() {
            setNext(NULL_ITEM);
        }

        public void setPrevious(TokenItem previous) {
            this.previous = previous;
        }

        public boolean isWritten() {
            return true;
        }

        public void markWritten() {
        }

        public int getWrittenLength() {
            return Integer.MAX_VALUE;
        }

        public void setWrittenLength(int writtenLength) {
            if (writtenLength != Integer.MAX_VALUE) {
                throw new IllegalArgumentException("Wrong writtenLength=" // NOI18N
                        + writtenLength);
            }
        }

        public void setImage(String image) {
            throw new IllegalStateException("Cannot set image of the document-token."); // NOI18N
        }

        public @Override TokenItem getPrevious() {
            if (previous == null) {
                TokenItem ti = super.getPrevious();
                if (ti != null) {
                    previous = new FilterDocumentItem(ti, this, false);
                }
            }

            return previous;
        }

    }

    interface ExtTokenItem extends TokenItem {

        /** Set the next item */
        public void setNext(TokenItem next);

        /** Set the previous item */
        public void setPrevious(TokenItem previous);

        /** Was this item already flushed to the underlying writer
        * or does it belong to the document?
        */
        public boolean isWritten();

        /** Set the flag marking the item as written to the underlying writer. */
        public void markWritten();

        /** Get the length that was written to the output writer. It can be used
         * when determining whether there can be insert of the text made
         * at the particular offset.
         * @return -1 to signal that token wasn't written at all.
         *   Or Integer.MAX_VALUE to signal that the token was written and cannot
         *   be extended.
         *   Or something else that means how long part of the token
         *   was already written to the file.
         */
        public int getWrittenLength();

        /** Set the length of the written part of the token. */
        public void setWrittenLength(int writtenLength);

        /** Set the image of the token to the specified string. */
        public void setImage(String image);

    }


}
