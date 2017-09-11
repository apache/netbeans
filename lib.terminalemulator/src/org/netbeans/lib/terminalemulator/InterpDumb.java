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
 *
 */
package org.netbeans.lib.terminalemulator;

import java.awt.event.KeyEvent;
import java.util.Stack;

/**
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 */
public class InterpDumb extends AbstractInterp {

    protected static class InterpTypeDumb {

        public final State st_base = new State("base");	// NOI18N
        protected final Actor act_nop = new ACT_NOP();
        protected final Actor act_pause = new ACT_PAUSE();
        protected final Actor act_err = new ACT_ERR();
        protected final Actor act_regular = new ACT_REGULAR();
        protected final Actor act_cr = new ACT_CR();
        protected final Actor act_lf = new ACT_LF();
        protected final Actor act_bs = new ACT_BS();
        protected final Actor act_tab = new ACT_TAB();
        protected final Actor act_beL = new ACT_BEL();

        protected InterpTypeDumb() {
            st_base.setRegular(st_base, act_regular);

            for (char c = 0; c < 128; c++) {
                st_base.setAction(c, st_base, act_regular);
            }

            st_base.setAction((char) 0, st_base, act_pause);
            st_base.setAction('\r', st_base, act_cr);
            st_base.setAction('\n', st_base, act_lf);
            st_base.setAction('\b', st_base, act_bs);
            st_base.setAction('\t', st_base, act_tab);
            st_base.setAction((char) 7, st_base, act_beL);
        }
        
        private static final class ACT_NOP implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                return null;
            }
        };

        private static final class ACT_PAUSE implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_pause();
                return null;
            }
        };

        private static final class ACT_ERR implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                return "ACT ERROR";	// NOI18N
            }
        };

        private static final class ACT_REGULAR implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_char(c);
                return null;
            }
        };

        private static final class ACT_CR implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_carriage_return();
                return null;
            }
        };

        private static final class ACT_LF implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_line_feed();
                return null;
            }
        };

        private static final class ACT_BS implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_back_space();
                return null;
            }
        };

        private static final class ACT_TAB implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_tab();
                return null;
            }
        };

        private static final class ACT_BEL implements Actor {

            @Override
            public String action(AbstractInterp ai, char c) {
                ai.ops.op_bel();
                return null;
            }
        }
    }

    /*
     * A stack for State
     */
    private final Stack<State> stack = new Stack<>();

    protected void push_state(State s) {
        stack.push(s);
    }

    protected State pop_state() {
        return stack.pop();
    }

    protected void pop_all_states() {
        while (!stack.empty()) {
            stack.pop();
        }
    }
    private StringBuilder ctlSequence;

    private final InterpTypeDumb type;
    private static final InterpTypeDumb type_singleton = new InterpTypeDumb();

    public InterpDumb(Ops ops) {
        super(ops);
        this.type = type_singleton;
        setup();
        ctlSequence = new StringBuilder();
    }

    protected InterpDumb(Ops ops, InterpTypeDumb type) {
        super(ops);
        this.type = type;
        setup();
        ctlSequence = new StringBuilder();
    }

    @Override
    public String name() {
        return "dumb";	// NOI18N
    }

    @Override
    public void reset() {
        super.reset();
        pop_all_states();
        state = type.st_base;
        ctlSequence = new StringBuilder();
    }

    private void setup() {
        state = type.st_base;
    }

    private void reset_state_bad() {
        reset();
    }

    @Override
    public void processChar(char c) {

        ctlSequence.append(c);

        try {
            State.Action a = state.getAction(c);
            /* DEBUG
            if (a == null) {
            System.out.println("null action in state " + state.name() +	// NOI18N
            " for char " + c + " = " + (int) c);	// NOI18N
            }
            if (a.actor == null) {
            System.out.println("null a.actor in state " + state.name() +	// NOI18N
            " for char " + c + " = " + (int) c);	// NOI18N
            }
             */
            String err_str = a.actor.action(this, c);
            /* DEBUG
//            String newName = (a.new_state) == null ? "null" : a.new_state.name(); //NOI18N
//            System.out.println(c + " " +(int)c + " " + a.actor.getClass().getSimpleName() + " " + state.name() + " " + newName); //NOI18N
             */
            if (err_str != null) {
                ops.logUnrecognizedSequence(ctlSequence.toString());
                reset_state_bad();
                return;
            }

            if (a.new_state != null) {
                state = a.new_state;
            } else {
                // must be set by action, usually using pop_state()
            }

        } finally {
            if (state == type.st_base) {
                ops.logCompletedSequence(ctlSequence.toString());
                ctlSequence = new StringBuilder();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyPressed(KeyEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char mapACS(char inChar) {
        return '\0';
    }

    @Override
    public void softReset() {
    }
}
