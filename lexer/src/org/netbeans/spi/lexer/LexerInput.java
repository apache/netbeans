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

package org.netbeans.spi.lexer;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.editor.util.AbstractCharSequence;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.lib.lexer.LexerInputOperation;
import org.netbeans.lib.lexer.LexerUtilsConstants;

/**
 * Provides characters to feed the {@link Lexer}.
 * It logically corresponds to <CODE>java.io.Reader</CODE> but its {@link #read()} method
 * does not throw any checked exception.
 * <br>
 * It allows to backup one or more characters that were already read
 * by {@link #read()} so that they can be re-read again later.
 * <br>
 * It supports viewing of the previously read characters as <CODE>java.lang.CharSequence</CODE>
 * by {@link #readText(int, int)}.
 *
 * <p>
 * The <code>LexerInput</code> can only be used safely by a single thread.
 *
 * <p>The following picture shows an example of java identifier recognition:
 *
 * <p><IMG src="doc-files/lexer-input.gif">.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public final class LexerInput {
    
    /**
     * Integer constant -1 returned by {@link #read()} to signal
     * that there are no more characters available on input.
     * <br/>
     * It cannot be a part of any token's text but it is counted
     * as a single character in {@link #backup(int)} operations.
     * <br/>
     * Translates to <code>0xFFFF</code> when casted to <code>char</code>.
     */
    public static final int EOF = -1;
    
    /**
     * LexerInputOperation on which this lexer input delegates.
     */
    private LexerInputOperation<?> operation;
    
    /**
     * Character sequence that corresponds
     * to the text that was read after past the end
     * of the last returned token.
     */
    private ReadText readText;

    /**
     * 1 if after EOF was just read or 0 otherwise.
     */
    private int eof;
    
    static final Logger LOG = Logger.getLogger(LexerInput.class.getName());
    private static boolean loggable;

    /**
     * Construct instance of the lexer input.
     *
     * @param operation non-null character provider for this lexer input.
     */
    LexerInput(LexerInputOperation operation) {
        this.operation = operation;
        // Refresh cached loggable value
        loggable = LOG.isLoggable(Level.FINE);
    }
    
    /**
     * Read a single character from input or return {@link #EOF}.
     *
     * @return valid character from input
     *   or {@link #EOF} when there are no more characters available
     *   on input. It's allowed to repeat the reads once EOF was returned
     *   - all of them will return EOF.
     */
    public int read() {
        int c = operation.read();
        if (c == EOF) {
            eof = 1;
        }
        if (loggable) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("+LexerInput.read(");
            if (c == EOF) {
                sb.append("EOF");
            } else {
                sb.append('\'');
                CharSequenceUtilities.debugChar(sb, (char)c);
                sb.append('\'');
            }
            sb.append(")\n");
            LOG.fine(sb.toString());
        }
        return c;
    }

    /**
     * Undo last <code>count</code> of {@link #read()} operations.
     * <br>
     * The operation moves back read-offset (from which {@link #read()}
     * reads characters) so that subsequent read operations
     * will re-read the characters that were backed up.
     * <br/>
     * If {@link LexerInput#EOF} was returned by {@link #read()} then
     * it will count as a single character in the backup operation
     * (even if returned multiple times)
     * i.e backup(1) will undo reading of (previously read) EOF.
     *
     * <p/>
     * <i>Example:</i><pre>
     *   // backup last character that was read - either regular char or EOF
     *   lexerInput.backup(1);
     *
     *   // Backup all characters read during recognition of current token
     *   lexerInput.backup(readLengthEOF());
     * </pre>
     *
     * @param count >=0 amount of characters to return back to the input.
     * @throws IndexOutOfBoundsException in case
     *  the <code>count > readLengthEOF()</code>.
     */
    public void backup(int count) {
        if (count < 0) {
            throw new IndexOutOfBoundsException("count=" + count + " <0"); // NOI18N
        }
        // count >= 0
        LexerUtilsConstants.checkValidBackup(count, readLengthEOF());
        if (eof != 0) {
            eof = 0; // backup EOF
            count--;
            if (loggable) {
                LOG.fine("-LexerInput.backup(EOF)\n");
            }
        }
        if (loggable && count > 0) {
            StringBuilder sb = new StringBuilder(100);
            sb.append("-LexerInput.backup(").append(count);
            sb.append(")\n");
            LOG.fine(sb.toString());
        }
        operation.backup(count);
    }
    
    /**
     * Get distance between the current reading point and the begining of a token
     * being currently recognized (excluding possibly read EOF).
     *
     * @return &gt;=0 number of characters obtained from the input
     *   by subsequent {@link #read()} operations since
     *   the last token was returned. The {@link #backup(int)}
     *   operations with positive argument decrease that value
     *   while those with negative argument increase it.
     *   <p>
     *   Once a token gets created by
     *   {@link TokenFactory#createToken(TokenId)}
     *   the value returned by <CODE>readLength()</CODE> becomes zero.
     *   <br>
     *   If {@link LexerInput#EOF} was read then it is not counted into read length.
     */
    public int readLength() {
        return operation.readLength();
    }
    
    /**
     * Read length that includes EOF as a single character
     * if it was just read from this input.
     */
    public int readLengthEOF() {
        return readLength() + eof;
    }
    
    /**
     * Get character sequence that corresponds to characters
     * that were read by previous {@link #read()} operations in the current token.
     * <br><i>Example:</i><pre>
     *
     *   private static final Map kwdStr2id = new HashMap();
     *
     *   static {
     *       String[] keywords = new String[] { "private", "protected", ... };
     *       TokenId[] ids = new TokenId[] { JavaLanguage.PRIVATE, JavaLanguage.PROTECTED, ... };
     *       for (int i = keywords.length - 1; i >= 0; i--) {
     *           kwdStr2id.put(keywords[i], ids[i]);
     *       }
     *   }
     *   
     *   public Token nextToken() {
     *       ... read characters of identifier/keyword by lexerInput.read() ...
     *
     *       // Now decide between keyword or identifier
     *       CharSequence text = lexerInput.readText(0, lexerInput.readLength());
     *       TokenId id = (TokenId)kwdStr2id.get(text);
     *       return (id != null) ? id : JavaLanguage.IDENTIFIER;
     *   }
     *
     * </pre>
     *
     * <p>
     * If {@link LexerInput#EOF} was previously returned by {@link #read()}
     * then it will not be a part of the returned charcter sequence
     * (it also does not count into {@link #readLength()}.
     *
     * <p>
     * Subsequent invocations of this method are cheap as the returned
     * CharSequence instance is reused and just reinitialized.
     *
     * @param start &gt;=0 and =&lt;{@link #readLength()}
     *  is the starting index of the character sequence in the previously read characters.
     * @param end &gt;=start and =&lt;{@link #readLength()}
     *  is the starting index of the character sequence in the previously read characters.
     * @return character sequence corresponding to read characters.
     *   <P>The returned character sequence is only valid
     *   until any of <CODE>read()</CODE>, <CODE>backup()</CODE>,
     *   <CODE>createToken()</CODE> or another <CODE>readText()</CODE> is called.
     *   <P>The <CODE>length()</CODE> of the returned
     *   character sequence will be equal
     *   to the <CODE>end - start</CODE>.
     *   <BR>The <CODE>hashCode()</CODE> method of the returned
     *   character sequence works in the same way like
     *   {@link String#hashCode()}.
     *   <BR>The <CODE>equals()</CODE> method
     *   attempts to cast the compared object to {@link CharSequence}
     *   and compare the lengths and if they match
     *   then compare every character of the given
     *   character sequence i.e. the same way like <CODE>String.equals()</CODE> works.
     * @throws IndexOutOfBoundsException in case the parameters are not in the
     *   required bounds.
     */
    public CharSequence readText(int start, int end) {
        assert (start >= 0 && end >= start && end <= readLength())
            : "start=" + start + ", end=" + end + ", readLength()=" + readLength(); // NOI18N

        if (readText == null) {
            readText = new ReadText();
        }
        readText.reinit(start, end);
        return readText;
    }

    /**
     * Return the read text for all the characters consumed from the input
     * for the current token recognition.
     */
    public CharSequence readText() {
        return readText(0, readLength());
    }
    
    /**
     * Read the next character and check whether it's '\n'
     * and if not backup it (otherwise leave it consumed).
     *
     * <p>
     * This method is useful in the following scenario:
     * <pre>
     *  switch (ch) {
     *      case 'x':
     *          ...
     *          break;
     *      case 'y':
     *          ...
     *          break;
     *      case '\r': input.consumeNewline();
     *      case '\n':
     *          // Line separator recognized
     *  }
     * </pre>
     *
     * @return true if newline was consumed or false otherwise.
     */
    public boolean consumeNewline() {
        if (read() == '\n') {
            return true;
        } else {
            backup(1);
            return false;
        }
    }
    
//    /**
//     * Lexer may call this method to get cached <code>java.lang.Integer</code> instance.
//     * <br/>
//     * The caching is only guaranteed if the given int value is below or equal to certain value
//     * - the present implementation uses 127.
//     * <br/>
//     * If the value is above this constant a new value will be constructed
//     * during each call. In such case the clients could possibly
//     * implement their own caching.
//     */
//    public static Integer integerState(int state) {
//        return IntegerCache.integer(state);
//    }
    
    /**
     * Helper character sequence being returned from <code>readText()</code>.
     */
    private final class ReadText extends AbstractCharSequence.StringLike {
        
        private int start;
        
        private int length;
        
        private void reinit(int start, int end) {
            this.start = start;
            this.length = (end - start);
        }
        
        public int length() {
            return length;
        }

        public char charAt(int index) {
            if (index < 0 || index >= length) {
                throw new IndexOutOfBoundsException("index=" + index + ", length=" + length); // NOI18N
            }
            return operation.readExistingAtIndex(start + index);
        }
        
    }
    
}
