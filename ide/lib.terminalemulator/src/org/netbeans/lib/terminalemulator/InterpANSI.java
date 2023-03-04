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

/*
 * "InterpANSI.java"
 * InterpANSI.java 1.6 01/07/30
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 */

package org.netbeans.lib.terminalemulator;

import java.awt.event.KeyEvent;

public class InterpANSI extends InterpProtoANSI {

    protected static class InterpTypeANSI extends InterpTypeProtoANSI {

	protected InterpTypeANSI() {
	}
    }

    private final InterpTypeANSI type;

    private static final InterpTypeANSI type_singleton = new InterpTypeANSI();

    public InterpANSI(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    } 

    protected InterpANSI(Ops ops, InterpTypeANSI type) {
	super(ops, type);
	this.type = type;
	setup();
    } 

    @Override
    public String name() {
	return "ansi";	// NOI18N
    } 

    @Override
    public void reset() {
	super.reset();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean dispatchAttr(AbstractInterp ai, int n) {
        switch (n) {
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                ai.ops.op_setG(0, n - 10);
                return true;

            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:

            case 20:
            case 21:
            case 22:
            case 23:
            case 24:
            case 25:
            // case 26:
            case 27:
            case 28:
            case 29:

            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:
            // case 38:
            case 39:

            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:
            // case 48:
            case 49:
                ai.ops.op_attr(n);
                return true;
            default:
                return false;
        }
    }

    private void setup() {
	state = type.st_base;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char mapACS(char inChar) {
        switch (inChar) {
            default: return '\0';
            case 0020: return '+'; // arrow pointing right            ACS_RARROW
            case 0021: return ','; // arrow pointing left             ACS_LARROW
            case 0030: return '-'; // arrow pointing up               ACS_UARROW
            case 0031: return '.'; // (^Y) arrow pointing down        ACS_DARROW
            case 0333: return '0'; // solid square block              ACS_BLOCK

            case 0004: return '`'; // diamond                        ACS_DIAMOND
            case 0261: return 'a'; // checker board (stipple)         ACS_CKBOARD
            // no-char return 'b'; // HT
            // no-char return 'c'; // FF
            // no-char return 'd'; // CR
            // no-char return 'e'; // LF
            case 0370: return 'f'; // degree symbol                   ACS_DEGREE
            case 0361: return 'g'; // plus/minus                      ACS_PLMINUS
            case 0260: return 'h'; // board of squares                ACS_BOARD  or NL
            // no-char return 'i'; // lantern symbol                  ACS_LANTERN or VT
            case 0331: return 'j'; // lower right corner              ACS_LRCORNER
            case 0277: return 'k'; // upper right corner              ACS_URCORNER
            case 0332: return 'l'; // upper left corner               ACS_ULCORNER
            case 0300: return 'm'; // lower left corner               ACS_LLCORNER
            case 0305: return 'n'; // large plus or crossover         ACS_PLUS
            case 0176: return 'o'; // (~) scan line 1                 ACS_S1
            // case 0304: return 'p'; // scan line 3                  ACS_S3
            case 0304: return 'q'; // horizontal line                 ACS_HLINE
            // case 0304: return 'r'; // scan line 7                  ACS_S7
            case 0137: return 's'; // (_) scan line 9                 ACS_S9
            case 0303: return 't'; // tee pointing right              ACS_LTEE
            case 0264: return 'u'; // tee pointing left               ACS_RTEE
            case 0301: return 'v'; // tee pointing up                 ACS_BTEE
            case 0302: return 'w'; // tee pointing down               ACS_TTEE
            case 0263: return 'x'; // vertical line                   ACS_VLINE
            case 0363: return 'y'; // less-than-or-equal-to           ACS_LEQUAL
            case 0362: return 'z'; // greater-than-or-equal-to        ACS_GEQUAL
            case 0343: return '{'; // greek pi                        ACS_PI
            case 0330: return '|'; // not-equal                       ACS_NEQUAL
            case 0234: return '}'; // UK pound sign                   ACS_STERLING
            case 0376: return '~'; // bullet                          ACS_BULLET
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {
            //
            // 6 key editing group
            //
            case KeyEvent.VK_INSERT:
                sendChars(e, "\033[L");         // NOI18N
                break;
            case KeyEvent.VK_HOME:
                sendChars(e, "\033[H");         // NOI18N
                break;

            //
            // Arrow keys
            //
            case KeyEvent.VK_UP:
                sendChars(e, "\033[A");         // NOI18N
                break;
            case KeyEvent.VK_DOWN:
                sendChars(e, "\033[B");         // NOI18N
                break;
            case KeyEvent.VK_RIGHT:
                sendChars(e, "\033[C");         // NOI18N
                break;
            case KeyEvent.VK_LEFT:
                sendChars(e, "\033[D");         // NOI18N
                break;

        }
    }
}
