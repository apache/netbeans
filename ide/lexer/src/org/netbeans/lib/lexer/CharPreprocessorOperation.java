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

package org.netbeans.lib.lexer;

import java.util.logging.Level;
import org.netbeans.lib.editor.util.ArrayUtilities;
import org.netbeans.lib.lexer.token.AbstractToken;
import org.netbeans.lib.lexer.CharPreprocessor;
import org.netbeans.spi.lexer.LexerInput;


/**
 * Character preprocessor delegates all its operation
 * to this class.
 * <br/>
 * Each preprocessor operation has its parent character provider
 * which is LexerInputOperation if this is there's just one char preprocessor.
 * <br/>
 * There can be more preprocessors chained above the LexerInputOperation.
 * <br/>
 * The LexerInput operates on top of the top char provider (preprocessor).
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class CharPreprocessorOperation implements CharProvider {
    
    /**
     * Parent char provider from which the characters are read.
     */
    private CharProvider parent;
    
    /** The actual preprocessor that this operation wraps. */
    private CharPreprocessor preprocessor;
    
    /**
     * Current reading index among the characters offered by this operation
     * relative to the current token's begining.
     */
    private int readIndex;
    
    /**
     * Max read index that either the client was requesting or which resulted
     * by calling more than one {@link #outputOriginal(char)}
     * in the {@link #preprocessChar()}.
     */
    private int lookaheadIndex;
    
    /**
     * Read index corresponding to the first char that really had to be preprocessed
     * for the current token (and is the first one in the prepChars array).
     */
    private int prepStartIndex;
    
    /**
     * Read index corresponding to the first non-preprocessed character
     * after the preprocessed characters.
     * <br/>
     * This variable is only valid if (prepStartIndex != lookaheadIndex).
     */
    private int prepEndIndex;
    
    /**
     * Characters translated by this preprocessor.
     * The token's initial characters that do not need any translation
     * are not stored in this array. Once there is a char resulting
     * from translation then all the subsequent chars will be stored
     * in this array even if they did not require translation.
     * <br/>
     * Once the token gets created or skipped the indexes are cleared
     * but the allocated array is retained.
     */
    private char[] prepChars = ArrayUtilities.emptyCharArray();

    /**
     * Extra read lengths of the input characters corresponding to each preprocessed char
     * on the output.
     * <br/>
     * The length shifts are related to the parent but at the end of the processing
     * of a particular token they need to retrieve the raw lengths against
     * the original input text and the extraRawLengthShifts gets populated
     * if necessary.
     */
    private int[] rawLengthShifts;
    
    /**
     * Last character passed to outputOriginal() or outputPreprocessed().
     */
    private int lastOutputChar;
    
    /** Computed and cached token length. */
    private int tokenLength;
    
    private LexerInputOperation lexerInputOperation;
    
    private int tokenEndRawLengthShift;

    /**
     * Construct new char preprocessor operation.
     * @param lexerInputOperation may be null then error notification won't work.
     */
    CharPreprocessorOperation(CharProvider parent, CharPreprocessor preprocessor,
    LexerInputOperation lexerInputOperation) {
        this.parent = parent;
        this.preprocessor = preprocessor;
        this.lexerInputOperation = lexerInputOperation;
        // Init the preprocessor to use this operation
//        LexerSpiPackageAccessor.get().init(preprocessor, this);
    }
    
    /**
     * Init the given token if necessary before {@link #tokenApproved()}
     * gets called.
     * <br/>
     * This method is only called on the preprocessor's operation.
     */
    public void initApprovedToken(AbstractToken token) {
        
    }

    /**
     * Read a single character for preprocessing from the underlying input.
     * <br/>
     * The character is obtained either from the real input source
     * or from another (parent) char preprocessor.
     *
     * @return valid character or {@link LexerInput#EOF} if there are no more
     *  characters available on the input.
     */
    public int inputRead() {
        return parent.read();
    }
    
    public void inputBackup(int count) {
        parent.backup(count);
    }
    
    public void outputOriginal(int ch) {
        lastOutputChar = ch;
        if (ch != LexerInput.EOF) {
            if (prepStartIndex == lookaheadIndex) { // collecting non-preprocessed
                prepStartIndex++;
            } else { // adding to existing prepChars
                // leave prepEndIndex as it is now
            }
            lookaheadIndex++;
        }
    }
    
    public void outputPreprocessed(char ch, int extraInputLength) {
        lastOutputChar = ch;
        if (prepStartIndex == lookaheadIndex) { // no prepChars yet
            prepEndIndex = prepStartIndex;
        } else if (prepEndIndex < lookaheadIndex) {
            // Add missing output chars
            do {
                addPrepChar(parent.readExisting(prepEndIndex), 0);
            } while (prepEndIndex < lookaheadIndex);
        } // else adding to the end of prepChars (last char was prep char)
        addPrepChar(ch, extraInputLength);
        lookaheadIndex++; // State that a prep char was added
    }
    
    public int deepRawLength(int length) {
        return parent.deepRawLength(parentLength(length));
    }
    
    public int deepRawLengthShift(int index) {
        return rawLengthShift(index) + parent.deepRawLengthShift(index);
    }
    
    private int rawLengthShift(int index) {
        if (index < prepStartIndex) {
            return index;
        } else if (index < prepEndIndex) {
            return rawLengthShifts[index - prepStartIndex];
        } else {
            return totalRawLengthShift();
        }
    }

    /**
     * Given length here translated into the length in parent.
     */
    private int parentLength(int length) {
        if (length > prepStartIndex) {
            if (length <= prepEndIndex) {
                length += rawLengthShifts[length - 1 - prepStartIndex];
            } else {
                length += totalRawLengthShift();
            }
        }
        return length;
    }

    private int totalRawLengthShift() {
        return rawLengthShifts[prepEndIndex - 1 - prepStartIndex];
    }
    
    public void notifyError(String errorMessage) {
        if (lexerInputOperation != null) {
            int parentIndex = parent.readIndex(); // Get the 
//            lexerInputOperation.notifyPreprocessorError(
//                new CharPreprocessorError(errorMessage, parent.deepRawLength(parentIndex)));
        }
    }

    public int read() {
        // Check whether any characters need to be preprocessed first
        if (readIndex == lookaheadIndex) {
            // Most typical situation - preprocess char
//            LexerSpiPackageAccessor.get().preprocessChar(preprocessor);
            readIndex++;
            // Expect only a single char to be put into lastOutputChar
            if (readIndex == lookaheadIndex) {
                return lastOutputChar;
            } else { // possibly more chars processed or EOF found etc.
                readIndex--;
                // Check whether EOF was processed (returned)
                if (readIndex == lookaheadIndex && lastOutputChar == LexerInput.EOF) {
                    return LexerInput.EOF;
                }
            }
            
        }
        
        return readExisting(readIndex++);
    }
    
    public char readExisting(int index) {
        return (index < prepStartIndex)// below the first preprocessed character
            ? parent.readExisting(index)
            : (index < prepEndIndex) // inside prepChars
                ? prepChars[index - prepStartIndex]
                : parent.readExisting(index + totalRawLengthShift());
    }
    
    public int readIndex() {
        return readIndex;
    }
    
    public void backup(int count) {
        readIndex -= count;
    }
    
    public int tokenLength() {
        return tokenLength;
    }
    
    public void assignTokenLength(int tokenLength, boolean skipToken) {
        this.tokenLength = tokenLength;
        // Modify tokenLength for preprocessed characters
        parent.assignTokenLength(parentLength(tokenLength), skipToken);
    }
    
    public PreprocessedTextStorage createPreprocessedTextStorage(CharSequence rawText,
    CharProvider.ExtraPreprocessedChars epc) {
        int pStartIndex;
        int pEndIndex;
        int topEndIndex;
        if (prepStartIndex >= tokenLength) {
            if (prepEndIndex > tokenLength) {
                updateTokenEndRawLengthShift();
                pEndIndex = tokenLength - 1;
                // Optimize the case when there are lookahead chars
                // for the present token and the ending chars could possibly
                // be non-preprocessed (prepEndIndex > tokenLength)
                while (--pEndIndex >= prepStartIndex && rawLengthShifts[pEndIndex] == tokenEndRawLengthShift) { // not preprocessed
                }
                pEndIndex += 2;
            } else // prepEndIndex <= tokenLength
                pEndIndex = prepEndIndex;
            topEndIndex = parentLength(pEndIndex);

            // Get deep raw lengths
            for (int i = prepStartIndex; i < pEndIndex; i++) {
                rawLengthShifts[i - prepStartIndex] = deepRawLength(i + 1) - (i + 1);
            }
            pStartIndex = prepStartIndex;

        } else { // No preprocessed chars inside token
            pStartIndex = tokenLength;
            pEndIndex = tokenLength;
            topEndIndex = tokenLength;
        }

        PreprocessedTextStorage prepStorage;
        if (epc != null) {
            parent.collectExtraPreprocessedChars(epc, pStartIndex, pEndIndex, topEndIndex);
            prepStorage = PreprocessedTextStorage.create(rawText,
                prepChars, pEndIndex - pStartIndex, pStartIndex, rawLengthShifts,
                epc.extraPrepChars(), epc.extraRawLengthShifts(),
                epc.preStartIndex(), epc.postEndIndex());
            epc.clear();
            
        } else { // no extra preprocessed chars
            prepStorage = PreprocessedTextStorage.create(rawText,
                prepChars, pEndIndex - pStartIndex, pStartIndex, rawLengthShifts);
        }
        return prepStorage;
    }
    
    private void updateTokenEndRawLengthShift() {
        tokenEndRawLengthShift = rawLengthShifts[tokenLength - 1 - prepStartIndex];
    }

    public void collectExtraPreprocessedChars(ExtraPreprocessedChars epc,
    int prepStartIndex, int prepEndIndex, int topPrepEndIndex) {
        if (prepStartIndex < tokenLength) { // Some preprocessed characters
            // Check for any pre-prepChars
            int preCount = Math.max(prepStartIndex - this.prepStartIndex, 0);
            // Check for post-prepChars
            int postCount;
            if (this.prepEndIndex > tokenLength) {
                updateTokenEndRawLengthShift();
                postCount = tokenLength - prepEndIndex;
                if (postCount > 0) {
                    int i = tokenLength - 2;
                    // Optimize the case when there are lookahead chars
                    // for the present token and the ending chars could possibly
                    // be non-preprocessed (prepEndIndex > tokenLength)
                    while (--i >= prepStartIndex && postCount > 0 && rawLengthShifts[i] == tokenEndRawLengthShift) { // not preprocessed
                        postCount--;
                    }
                } else // postCount <= 0
                    postCount = 0;

            } else { // this.prepEndIndex <= tokenLength
                postCount = this.prepEndIndex - prepEndIndex;
            }

            assert (preCount >= 0 && postCount >= 0);
            epc.ensureExtraLength(preCount + postCount);
            while (--preCount >= 0) {
                epc.insert(readExisting(prepStartIndex - 1), deepRawLength(prepStartIndex) - prepStartIndex);
                prepStartIndex--;
            }
            while (--postCount >= 0) {
                epc.append(readExisting(prepEndIndex), deepRawLength(prepEndIndex) - topPrepEndIndex);
                prepEndIndex++;
                topPrepEndIndex++;
            }
        }
        
        parent.collectExtraPreprocessedChars(epc, prepStartIndex, prepEndIndex, topPrepEndIndex);
    }
    
    /**
     * This method is called after the token has been recognized
     * to clear internal data related to processing of token's characters.
     */
    public void consumeTokenLength() {
        if (prepStartIndex != lookaheadIndex) { // some prep chars (may be after token length)
            if (prepStartIndex < tokenLength) { // prep chars before token end
                if (prepEndIndex <= tokenLength) { // no preprocessed chars past token end
                    prepStartIndex = lookaheadIndex; // signal no preprocessed chars
                } else { // prepEndIndex > tokenLength => initial prep chars in the next token
                    // updateTokenLengthParentShift() was already called in this case
                    for (int i = tokenLength; i < prepEndIndex; i++) {
                        rawLengthShifts[i] -= tokenEndRawLengthShift;
                    }
                    System.arraycopy(prepChars, prepStartIndex, prepChars, 0,
                            prepEndIndex - prepStartIndex);
                    System.arraycopy(rawLengthShifts, prepStartIndex, rawLengthShifts, 0,
                            prepEndIndex - prepStartIndex);
                    prepStartIndex = 0;
                    prepEndIndex -= tokenLength;
                }

            } else { // prepStartIndex >= tokenLength
                prepStartIndex -= tokenLength;
                prepEndIndex -= tokenLength;
            }
        } else
            prepStartIndex -= tokenLength;

        readIndex -= tokenLength;
        lookaheadIndex -= tokenLength;
        parent.consumeTokenLength();

        if (TokenList.LOG.isLoggable(Level.FINE)) {
            consistencyCheck();
        }
    }
    
    /**
     * Add preprocessed or passed char to prepChars
     */
    private void addPrepChar(char ch, int extraInputLength) {
        int prepCharsLength = prepEndIndex - prepStartIndex;
        if (prepCharsLength == prepChars.length) { // reallocate
            prepChars = ArrayUtilities.charArray(prepChars);
            rawLengthShifts = ArrayUtilities.intArray(rawLengthShifts);
        }
        prepChars[prepCharsLength] = ch;
        int prevRawLengthShift = (prepCharsLength > 0)
                ? rawLengthShifts[prepCharsLength -1]
                : 0;
        rawLengthShifts[prepCharsLength] = prevRawLengthShift + extraInputLength;
        prepEndIndex++;
    }
    
    private void consistencyCheck() {
        if (readIndex > lookaheadIndex) {
            throw new IllegalStateException("readIndex > lookaheadIndex: " + this);
        }
        if (prepStartIndex > lookaheadIndex) {
            throw new IllegalStateException("prepStartIndex > lookaheadIndex: " + this);
        }
        if (prepStartIndex != lookaheadIndex && prepStartIndex >= prepEndIndex) {
            throw new IllegalStateException("prepStartIndex >= prepEndIndex: " + this);
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("readIndex=");
        sb.append(readIndex);
        sb.append(", lookaheadIndex=");
        sb.append(lookaheadIndex);
        sb.append(", prepStartIndex=");
        sb.append(prepStartIndex);
        sb.append(", prepEndIndex=");
        sb.append(prepEndIndex);
        return sb.toString();
    }

}
