/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
