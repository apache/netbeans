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
package org.netbeans.lib.terminalemulator;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import org.netbeans.lib.terminalemulator.AbstractInterp.Actor;

/**
 * Stuff common to InterpDtTerm and InterpXTerm but not InterpANSI.
 * @author ivan
 */
class InterpProtoANSIX extends InterpProtoANSI {

    protected static class InterpTypeProtoANSIX extends InterpTypeProtoANSI {
	protected final State st_wait = new State("wait");	// NOI18N

	protected final State st_esc_rb = new State("esc_rb");	// NOI18N
	protected final State st_esc_rb_N = new State("esc_rb_N");// NOI18N
	protected final State st_esc_lb_q = new State("esc_lb_q");// NOI18N
	protected final State st_esc_lb_b = new State("esc_lb_b");// NOI18N

	protected final Actor act_ind = new ACT_IND();

	protected final Actor act_start_collect = new ACT_START_COLLECT();
	protected final Actor act_collect = new ACT_COLLECT();
	protected final Actor act_done_collect_bel = new ACT_DONE_COLLECT_BEL();
	protected final Actor act_DEC_private = new ACT_DEC_PRIVATE();

	protected InterpTypeProtoANSIX() {
            st_esc.setAction('7', st_base, new ACT_SC());
            st_esc.setAction('8', st_base, new ACT_RC());
            st_esc.setAction('>', st_base, new ACT_PNM());
            st_esc.setAction('=', st_base, new ACT_PAM());

	    st_esc.setAction('D', st_base, act_ind);

            // \ESC]%d;%s\BEL
	    st_esc.setAction(']', st_esc_rb, act_start_collect);
	    for (char c = '0'; c <= '9'; c++)
		st_esc_rb.setAction(c, st_esc_rb_N, act_collect);
	    for (char c = 0; c < 128; c++)
		st_esc_rb_N.setAction(c, st_esc_rb_N, act_collect);
            st_esc_rb_N.setRegular(st_esc_rb_N, act_collect);
	    st_esc_rb_N.setAction((char) 7, st_base, act_done_collect_bel);// BEL

            // \ESC[?%dh
            // \ESC[?%dl
            // \ESC[?%dr
            // \ESC[?%ds
	    st_esc_lb.setAction('?', st_esc_lb_q, act_reset_number);
	    for (char c = '0'; c <= '9'; c++)
		st_esc_lb_q.setAction(c, st_esc_lb_q, act_remember_digit);
	    st_esc_lb_q.setAction(';', st_esc_lb_q, act_push_number);
	    st_esc_lb_q.setAction('h', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('l', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('r', st_base, act_DEC_private);
	    st_esc_lb_q.setAction('s', st_base, act_DEC_private);

            // \ESC[!p
	    st_esc_lb.setAction('!', st_esc_lb_b, act_reset_number);
	    st_esc_lb_b.setAction('p', st_base, new ACT_DEC_STR());
        }

	private static final class ACT_SC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_sc();
		return null;
	    }
	}

	private static final class ACT_PAM implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
                ((InterpProtoANSIX)ai).DECPAM = true;
		return null;
	    }
	}

	private static final class ACT_PNM implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
                ((InterpProtoANSIX)ai).DECPAM = false;
		return null;
	    }
	}

	private static final class ACT_RC implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_rc();
		return null;
	    }
	}

	private static final class ACT_IND implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                // scroll
		ai.ops.op_ind(1);
		return null;
	    }
	};

	private static final class ACT_START_COLLECT implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
		i.text = "";	// NOI18N
		return null;
	    }
	}

	private static final class ACT_COLLECT implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		// java bug 4318526 text += c;
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
		i.text = i.text + c;
		return null;
	    }
	}

	private static final class ACT_DONE_COLLECT_BEL implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
                int semix = i.text.indexOf(';');
                if (semix == -1)
                    return null;
                String p1 = i.text.substring(0, semix);
                String p2 = i.text.substring(semix+1);
                int code = Integer.parseInt(p1);
                switch (code) {
                    case 0:
                        ai.ops.op_icon_name(p2);
                        ai.ops.op_win_title(p2);
                        break;
                    case 1:
                        ai.ops.op_icon_name(p2);
                        break;
                    case 2:
                        ai.ops.op_win_title(p2);
                        break;
                    case 3:
                        // cwd is a dttermism.
                        // This will be inherited by InterpXTerm but it's really
                        // not supported by xterm.
                        ai.ops.op_cwd(p2);
                        break;

                    case 10: {
                        // This is specific to nbterm!
                        int semix2 = p2.indexOf(';');
                        if (semix2 == -1)
                            return null;
                        String p3 = p2.substring(semix2+1);
                        p2 = p2.substring(0, semix2);
                        if (p3.isEmpty() && p2.startsWith(Term.ExternalCommandsConstants.COMMAND_PREFIX)) { //NOI18N
                            ai.ops.op_ext(p2);
                        } else {
                            ai.ops.op_hyperlink(p2, p3);
                        }
                    }
                }
		return null;
	    }
	}

	private static final class ACT_DEC_PRIVATE implements Actor {

            // xterm Sequences to turn mouse reporting on and off are to be
            // implemeted here.
            // See http://www.xfree86.org/current/ctlseqs.html#Mouse%20Tracking

            private static String decPrivateSet(AbstractInterp ai, char c, int n) {
                switch (n) {
                    case 1:
                        ((InterpProtoANSIX)ai).DECCKM = true;
                        break;
                    case 5:
                        ai.ops.op_reverse(true);
                        break;
                    case 12:
                        // blinking cursor
                        break;
                    case 25:
                        ai.ops.op_cursor_visible(true);
                        break;
                    default:
                        return "act_DEC_private: unrecognized code " + n;	// NOI18N
                }
                return null;
            }

            private static String decPrivateReset(AbstractInterp ai, char c, int n) {
                switch (n) {
                    case 1:
                        ((InterpProtoANSIX)ai).DECCKM = false;
                        break;
                    case 5:
                        ai.ops.op_reverse(false);
                        break;
                    case 12:
                        // blinking cursor
                        break;
                    case 25:
                        ai.ops.op_cursor_visible(false);
                        break;
                    default:
                        return "act_DEC_private: unrecognized code " + n;	// NOI18N
                }
                return null;
            }

            private static String decPrivateSave(AbstractInterp ai, char c, int n) {
                return "act_DEC_private: unrecognized code " + n;	// NOI18N
            }


            private static String decPrivateRestore(AbstractInterp ai, char c, int n) {
                return "act_DEC_private: unrecognized code " + n;	// NOI18N
            }

            @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber())
		    return "act_DEC_private: no number";	// NOI18N
                for (int nx = 0; nx <= ai.nNumbers(); nx++) {
                    int n = ai.numberAt(nx);
                    switch(c) {
                        case 'h': return decPrivateSet(ai, c, n);
                        case 'l': return decPrivateReset(ai, c, n);
                        case 'r': return decPrivateRestore(ai, c, n);
                        case 's': return decPrivateSave(ai, c, n);
                        default:  return "act_DEC_private: unrecognized cmd " + c;	// NOI18N
                    }
                }
                return null;
	    }
	}

	protected static final class ACT_DEC_STR implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		ai.ops.op_soft_reset();
		return null;
	    }
	}
    }

    protected String text = null;

    private final InterpTypeProtoANSIX type;

    private static final InterpTypeProtoANSIX type_singleton = new InterpTypeProtoANSIX();

    private boolean DECCKM;             // CursorKeyboardMode
    private boolean DECPAM;             // PadApplicationMode
                                        // opposite of DECPNM (PadNormalMode)

    public InterpProtoANSIX(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    }

    protected InterpProtoANSIX(Ops ops, InterpTypeProtoANSIX type) {
	super(ops, type);
	this.type = type;
	setup();
    }

    @Override
    public String name() {
	return "proto-ansi-x";	// NOI18N
    }

    @Override
    public void reset() {
	super.reset();
	text = null;
    }

    private void setup() {
    }

    private static boolean numLock() {
        // getLockingKeyState() seems somewhat unreliable.
        // Doesn't work:
        //      Linux w/ Java 6
        // Works:
        //          Linux w/ Java 7
        // All other platforms unknown.
        //
        // We return false in cases where it doesn't work because NumLock is
        // off by default.

        final Toolkit toolkit = Toolkit.getDefaultToolkit();
        try {
            return toolkit.getLockingKeyState(KeyEvent.VK_NUM_LOCK);
        } catch (UnsupportedOperationException x) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyPressed(KeyEvent e) {

        final boolean pam = DECPAM && ! numLock();

        switch (e.getKeyCode()) {
            //
            // 6 key editing group
            //
            case KeyEvent.VK_INSERT:
                sendChars(e, "\033[2~");                        // NOI18N
                break;
            case KeyEvent.VK_DELETE:
                sendChars(e, "\033[3~");                       // NOI18N
                break;
            case KeyEvent.VK_PAGE_UP:
                sendChars(e, "\033[5~");                       // NOI18N
                break;
            case KeyEvent.VK_PAGE_DOWN:
                sendChars(e, "\033[6~");                       // NOI18N
                break;
            case KeyEvent.VK_HOME:
                sendChars(e, DECCKM? "\033OH": "\033[H");      // NOI18N
                break;
            case KeyEvent.VK_END:
                sendChars(e, DECCKM? "\033OF": "\033[F");      // NOI18N
                break;

            //
            // Arrow keys
            //
            case KeyEvent.VK_UP:
                sendChars(e, DECCKM? "\033OA": "\033[A");      // NOI18N
                break;
            case KeyEvent.VK_DOWN:
                sendChars(e, DECCKM? "\033OB": "\033[B");      // NOI18N
                break;
            case KeyEvent.VK_RIGHT:
                sendChars(e, DECCKM? "\033OC": "\033[C");      // NOI18N
                break;
            case KeyEvent.VK_LEFT:
                sendChars(e, DECCKM? "\033OD": "\033[D");      // NOI18N
                break;

            //
            // Function keys
            //
            case KeyEvent.VK_F1:
                sendChars(e, "\033OP");                // NOI18N
                break;
            case KeyEvent.VK_F2:
                sendChars(e, "\033OQ");                // NOI18N
                break;
            case KeyEvent.VK_F3:
                sendChars(e, "\033OR");                // NOI18N
                break;
            case KeyEvent.VK_F4:
                sendChars(e, "\033OS");                // NOI18N
                break;

            case KeyEvent.VK_F5:
                sendChars(e, "\033[15~");                      // NOI18N
                break;
            case KeyEvent.VK_F6:
                sendChars(e, "\033[17~");                      // NOI18N
                break;
            case KeyEvent.VK_F7:
                sendChars(e, "\033[18~");                      // NOI18N
                break;
            case KeyEvent.VK_F8:
                sendChars(e, "\033[19~");                      // NOI18N
                break;

            case KeyEvent.VK_F9:
                sendChars(e, "\033[20~");                      // NOI18N
                break;
            case KeyEvent.VK_F10:
                sendChars(e, "\033[21~");                      // NOI18N
                break;
            case KeyEvent.VK_F11:
                sendChars(e, "\033[23~");                      // NOI18N
                break;
            case KeyEvent.VK_F12:
                sendChars(e, "\033[24~");                      // NOI18N
                break;

            //
            // Keypad
            //
            case KeyEvent.VK_DIVIDE:
                sendChars(e, pam? "\033Oo": "/");                      // NOI18N
                break;
            case KeyEvent.VK_MULTIPLY:
                sendChars(e, pam? "\033Oj": "*");                      // NOI18N
                break;
            case KeyEvent.VK_SUBTRACT:
                sendChars(e, pam? "\033Om": "-");                      // NOI18N
                break;
            case KeyEvent.VK_ADD:
                sendChars(e, pam? "\033Ok": "+");                      // NOI18N
                break;
            /* Java doesn't provide a way to distinguish between the regular
             * Enter/Return key and the keypad Enter key.
            case KeyEvent.VK_ENTER:
                if (pam)
                    sendChars(e, "\033OM");
                break;
            */

            case KeyEvent.VK_NUMPAD0:
                sendChars(e, "0");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD1:
                sendChars(e, "1");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD2:
                sendChars(e, "2");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD3:
                sendChars(e, "3");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD4:
                sendChars(e, "4");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD5:
                sendChars(e, "5");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD6:
                sendChars(e, "6");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD7:
                sendChars(e, "7");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD8:
                sendChars(e, "8");                     // NOI18N
                break;
            case KeyEvent.VK_NUMPAD9:
                sendChars(e, "9");                     // NOI18N
                break;
            case KeyEvent.VK_DECIMAL:
                sendChars(e, ".");                     // NOI18N
                break;

            case KeyEvent.VK_BEGIN:
                sendChars(e, "\033[E");                // NOI18N
                break;
            case KeyEvent.VK_KP_UP:
                sendChars(e, "\033[A");                // NOI18N
                break;
            case KeyEvent.VK_KP_DOWN:
                sendChars(e, "\033[B");                // NOI18N
                break;
            case KeyEvent.VK_KP_LEFT:
                sendChars(e, "\033[D");                // NOI18N
                break;
            case KeyEvent.VK_KP_RIGHT:
                sendChars(e, "\033[C");                // NOI18N
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char mapACS(final char inChar) {
        switch (inChar) {
            default: return '\0';
            case '+': return inChar; // arrow pointing right            ACS_RARROW
            case ',': return inChar; // arrow pointing left             ACS_LARROW
            case '-': return inChar; // arrow pointing up               ACS_UARROW
            case '.': return inChar; // (^Y) arrow pointing down        ACS_DARROW
            case '0': return inChar; // solid square block              ACS_BLOCK

            case '`': return inChar; // diamond                        ACS_DIAMOND
            case 'a': return inChar; // checker board (stipple)         ACS_CKBOARD
            // no-char return 'b'; // HT
            // no-char return 'c'; // FF
            // no-char return 'd'; // CR
            // no-char return 'e'; // LF
            case 'f': return inChar; // degree symbol                   ACS_DEGREE
            case 'g': return inChar; // plus/minus                      ACS_PLMINUS
            case 'h': return inChar; // board of squares                ACS_BOARD  or NL
            // no-char return 'i'; // lantern symbol                  ACS_LANTERN or VT
            case 'j': return inChar; // lower right corner              ACS_LRCORNER
            case 'k': return inChar; // upper right corner              ACS_URCORNER
            case 'l': return inChar; // upper left corner               ACS_ULCORNER
            case 'm': return inChar; // lower left corner               ACS_LLCORNER
            case 'n': return inChar; // large plus or crossover         ACS_PLUS
            case 'o': return inChar; // (~) scan line 1                 ACS_S1
            // case 0304: return 'p'; // scan line 3                  ACS_S3
            case 'q': return inChar; // horizontal line                 ACS_HLINE
            // case 0304: return 'r'; // scan line 7                  ACS_S7
            case 's': return inChar; // (_) scan line 9                 ACS_S9
            case 't': return inChar; // tee pointing right              ACS_LTEE
            case 'u': return inChar; // tee pointing left               ACS_RTEE
            case 'v': return inChar; // tee pointing up                 ACS_BTEE
            case 'w': return inChar; // tee pointing down               ACS_TTEE
            case 'x': return inChar; // vertical line                   ACS_VLINE
            case 'y': return inChar; // less-than-or-equal-to           ACS_LEQUAL
            case 'z': return inChar; // greater-than-or-equal-to        ACS_GEQUAL
            case '{': return inChar; // greek pi                        ACS_PI
            case '|': return inChar; // not-equal                       ACS_NEQUAL
            case '}': return inChar; // UK pound sign                   ACS_STERLING
            case '~': return inChar; // bullet                          ACS_BULLET
        }
    }

    @Override
    public void softReset() {
        DECCKM = false;
        DECPAM = false;
    }
}
