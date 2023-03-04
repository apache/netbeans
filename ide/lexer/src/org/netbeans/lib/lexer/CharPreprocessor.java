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

/**
 * Character preprocessor allows to translate a sequence
 * of characters to a single character so it may be used
 * for example for Unicode sequences translation.
 * <br/>
 * If there are any preprocessed characters for a particular token
 * then a special token instance get created that provides
 * the preprocessed chars.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public abstract class CharPreprocessor {
    
    /**
     * Create instance of character preprocessor for Unicode escape sequences.
     */
    public static CharPreprocessor createUnicodeEscapesPreprocessor() {
        return new UnicodeEscapesPreprocessor();
    }

    private CharPreprocessorOperation operation;
    

    /**
     * Preprocess at least one character of the input.
     * <br/>
     * Preprocessor must always preprocess at least one input character
     * per invocation but only a minimum necessary number of characters
     * should be preprocessed by each invocation of this method.
     * 
     * <p>
     * Example:
     * <pre>
     *   public void preprocessChar() {
     *     switch (ch = inputRead()) {
     *       case '\\': // possible start of sequence
     *         switch (ch = inputRead()) {
     *           case 'u': // start of escape sequence
     *             ... // read the whole sequence
     *             outputPreprocessed(prepCh, extraInputLength);
     *             break;
     *           default:
     *             outputOriginal('\\');
     *             outputOriginal(ch);
     *             break;
     *         }
     *         break;
     *       default:
     *         outputOriginal(ch);
     *     }
     *   }
     * </pre>
     * </p>
     * 
     * <p>
     * The processor is only designed to do several-chars-to-one translation.
     * <br/>
     * It is not designed to return more than one character for a single input char.
     * <br/>
     * Also if the character is really preprocessed it must be composed
     * from at least two input characters (see extraInputLength parameter
     * of {@link #outputPreprocessed(char, int)}.
     * </p>
     *
     * <p>
     * The preprocessor must be able to process all the characters
     * given to it on input.
     * However it should not preprocess EOF in any way
     * - the EOF is just information that there is an end of the input
     * and any possibly unfinished escape sequence needs to be translated
     * in a reasonable way.
     * <br/>
     * Once all the characters prior EOF were preprocessed the EOF
     * should be returned by {@link #outputOriginal(int)}.
     * </p>
     * 
     */
    protected abstract void preprocessChar();
    
    /**
     * Check whether the given character may be part of the sequences preprocessed
     * by this preprocessor.
     * <br/>
     * The infrastructure may use this method to test whether it can start
     * relexing starting at a particular position.
     */
    protected abstract boolean isSensitiveChar(char ch);
    
    /**
     * Return maximum number of extra characters (not being part of the recognized
     * sequence) that this preprocessor
     * may look ahead in order to recognize the preprocessed character sequence.
     * <br/>
     * For example for unicode escape sequences the returned number is 1
     * (see UnicodeEscapesPreprocessor implementation for details).
     */
    protected abstract int maxLookahead();
    
    /**
     * Read a single character for preprocessing from the underlying input.
     *
     * @return valid character or {@link LexerInput#EOF} if there are no more
     *  characters available on the input.
     */
    protected final int inputRead() {
        return operation.inputRead();
    }
    
    /**
     * Backup a given number of input characters.
     *
     * @param count >=0 number of chars to backup.
     */
    protected final void inputBackup(int count) {
        operation.inputBackup(count);
    }
    
    /**
     * Output the character as it was read from the input.
     * <br/>
     * By using this method the infrastructure knows that the character
     * is the same like the original character read by {@link #inputRead()}.
     */
    protected final void outputOriginal(int ch) {
        operation.outputOriginal(ch);
    }

    /**
     * Output preprocessed character. There is usually more than one input character
     * forming a single preprocessed character.
     *
     * @param ch preprocessed character.
     * @param extraInputLength >0 number of extra input characters
     *  (besides a single character) that form the preprocessed character.
     * <br/>
     * For example for unicode escape sequence "\u0020" it's 6-1=5.
     * <br/>
     * The number is expected to be greater than zero
     * (otherwise the present implementation would not work correctly).
     * which should be fine for the known implementations 
     * (if not please request an API change).
     */
    protected final void outputPreprocessed(char ch, int extraInputLength) {
        assert (extraInputLength > 0) : "extraInputLength > 0 expected.";
        operation.outputPreprocessed(ch, extraInputLength);
    }
    
    /**
     * Notify the error that occurred during preprocessing of the current character.
     *
     * @param errorMessage non-null error description.
     */
    protected final void notifyError(String errorMessage) {
        operation.notifyError(errorMessage);
    }

    void init(CharPreprocessorOperation operation) {
        this.operation = operation;
    }

}
