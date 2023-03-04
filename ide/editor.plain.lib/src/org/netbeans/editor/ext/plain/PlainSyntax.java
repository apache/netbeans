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

package org.netbeans.editor.ext.plain;

import org.netbeans.editor.Syntax;
import org.netbeans.editor.TokenID;

/**
 * Lexical analyzer for the plain text.
 *
 * @author Miloslav Metelka
 * @deprecated If you need this class you are doing something wrong, 
 *   please ask on nbdev@netbeans.org.
 */
@Deprecated
public class PlainSyntax extends Syntax {

    /* Internal states used internally by analyzer. There
    * can be any number of them declared by the analyzer.
    * They are usually numbered starting from zero but they don't
    * have to. The only reserved value is -1 which is reserved
    * for the INIT state - the initial internal state of the analyzer.
    */
    private static final int ISI_TEXT = 0;

    public PlainSyntax() {
        tokenContextPath = PlainTokenContext.contextPath;
    }

    /** 
     * This is core function of analyzer and it returns one of following numbers:
     * 
     * a) token number of next token from scanned text
     * b) EOL when end of line was found in scanned buffer
     * c) EOT when there is no more chars available in scanned buffer.
     *
     * The function scans the active character and does one or more
     * of the following actions:
     * 1. change internal analyzer state (state = new-state)
     * 2. return token ID (return token-ID)
     * 3. adjust current position to signal different end of token;
     *    the character that offset points to is not included in the token
     * 
     * @return See above.
     */
    protected TokenID parseToken() {
        // The main loop that reads characters one by one follows
        while (offset < stopOffset) {
            char ch = buffer[offset]; // get the current character

            switch (state) { // switch by the current internal state
            case INIT:
                switch (ch) {
                case '\n':
                    offset++;
                    return PlainTokenContext.EOL;
                default:
                    state = ISI_TEXT;
                    break;
                }
                break;

            case ISI_TEXT:
                switch (ch) {
                case '\n':
                    state = INIT;
                    return PlainTokenContext.TEXT;
                }
                break;

            } // end of switch(state)

            offset++; // move to the next char
        }

        /* At this state there's no more text in the scanned buffer.
        * The caller will decide either to stop scanning at all
        * or to relocate scanning and provide next buffer with characters.
        * The lastBuffer variable indicates whether the scanning will
        * stop (true) or the caller will provide another buffer
        * to continue on (false) and call relocate() to continue on the given buffer.
        * If this is the last buffer, the analyzer must ensure
        * that for all internal states there will be some token ID returned.
        * The easiest way how to ensure that all the internal states will
        * be covered is to copy all the internal state constants and
        * put them after the switch() and provide the code that will return
        * appropriate token ID.
        *
        * When there are no more characters available in the buffer
        * and the buffer is not the last one the analyzer can still
        * decide to return the token ID even if it doesn't know whether
        * the token is complete or not. This is possible in this simple
        * implementation for example because it doesn't matter whether
        * it returns the text all together or broken into several pieces.
        * The advantage of such aproach is that the preScan value
        * is minimized which avoids the additional increasing of the buffer
        * by preScan characters, but on the other hand it can become
        * problematic if the token should be forwarded for some further
        * processing. For example it could seem handy to return incomplete
        * token for java block comments but it could become difficult
        * if we would want to analyzer these comment tokens additionally
        * by the HTML analyzer for example.
        */

        // Normally the following block would be done only for lastBuffer == true
        // but in this case it can always be done
        switch (state) {
        case ISI_TEXT:
            state = INIT;
            return PlainTokenContext.TEXT;
        }

        // need to continue on another buffer
        return null;
    }

}
