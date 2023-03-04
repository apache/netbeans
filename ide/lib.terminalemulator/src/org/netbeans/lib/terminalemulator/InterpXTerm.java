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


class InterpXTerm extends InterpProtoANSIX {

    protected static class InterpTypeXTerm extends InterpTypeProtoANSIX {

	protected final State st_esc_lb_gt = new State("esc_lb_gt");	// NOI18N
	protected final Actor act_done_collect_escbs = new ACT_DONE_COLLECT_ESCBS();

	protected InterpTypeXTerm() {
	    st_esc_lb.setAction('>', st_esc_lb_gt, act_reset_number);
	    for (char c = '0'; c <= '9'; c++)
		st_esc_lb_gt.setAction(c, st_esc_lb_gt, act_remember_digit);
	    st_esc_lb_gt.setAction(';', st_esc_lb_gt, act_push_number);
	    st_esc_lb_gt.setAction('T', st_base, new ACT_XTERM_CAPITAL_T());
	    st_esc_lb_gt.setAction('c', st_base, new ACT_XTERM_c());
	    st_esc_lb_gt.setAction('m', st_base, new ACT_XTERM_m());
	    st_esc_lb_gt.setAction('n', st_base, new ACT_XTERM_n());
	    st_esc_lb_gt.setAction('p', st_base, new ACT_XTERM_p());
	    st_esc_lb_gt.setAction('t', st_base, new ACT_XTERM_t());

	    st_esc_rb_N.setAction((char) 27, st_wait, act_nop);         // ESC
	    st_wait.setAction('\\', st_base, act_done_collect_escbs);
	}

	private static final class ACT_XTERM_CAPITAL_T implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                return "ACT_XTERM_T: UNIMPLEMENTED";  // NOI18N
            }
        }

	private static final class ACT_XTERM_c implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                // first number: 0 for vt100, 1 for vt220
                // second number: firmware version / patch#
                // third number: always 0
                ai.ops.op_send_chars("\033[>0;0;0c");   // NOI18N
                return null;
            }
        }

	private static final class ACT_XTERM_m implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                return "ACT_XTERM_m: UNIMPLEMENTED";  // NOI18N
            }
        }

	private static final class ACT_XTERM_n implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                return "ACT_XTERM_n: UNIMPLEMENTED";  // NOI18N
            }
        }

	private static final class ACT_XTERM_p implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                return "ACT_XTERM_p: UNIMPLEMENTED";  // NOI18N
            }
        }

	private static final class ACT_XTERM_t implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
                return "ACT_XTERM_t: UNIMPLEMENTED";  // NOI18N
            }
        }

	private static final class ACT_DONE_COLLECT_ESCBS implements Actor {
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
                        /* LATER
                        cwd is a dttermism. For xterm we're supposed to set X properties
                        ai.ops.op_cwd(p2);
                        */
                        break;

                    case 10: {
                        // This is specific to nbterm!
                        int semix2 = p2.indexOf(';');
                        if (semix2 == -1)
                            return null;
                        String p3 = p2.substring(semix2+1);
                        p2 = p2.substring(0, semix2);
                        ai.ops.op_hyperlink(p2, p3);
                    }
                }
		return null;
	    }
	}

    }

    private final InterpTypeXTerm type;

    private static final InterpTypeXTerm type_singleton = new InterpTypeXTerm();

    public InterpXTerm(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    }

    protected InterpXTerm(Ops ops, InterpTypeXTerm type) {
	super(ops, type);
	this.type = type;
	setup();
    }

    @Override
    public String name() {
	return "xterm-16color";	// NOI18N
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
            case 0:
            case 1:
            case 4:
            case 5:
            case 7:
            case 8:

            case 22:
            case 24:
            case 25:
            case 27:
            case 28:

            case 30:
            case 31:
            case 32:
            case 33:
            case 34:
            case 35:
            case 36:
            case 37:

            case 39:

            case 40:
            case 41:
            case 42:
            case 43:
            case 44:
            case 45:
            case 46:
            case 47:

            case 49:

	    // bright fg
	    case 90:
	    case 91:
	    case 92:
	    case 93:
	    case 94:
	    case 95:
	    case 96:
	    case 97:

	    // bright bg
	    case 100:
	    case 101:
	    case 102:
	    case 103:
	    case 104:
	    case 105:
	    case 106:
	    case 107:
                ai.ops.op_attr(n);
                return true;
            default:
                return false;
        }
    }

    private void setup() {
    }
}
