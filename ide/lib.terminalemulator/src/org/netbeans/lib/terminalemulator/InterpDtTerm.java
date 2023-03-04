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
 * "InterpDtTerm.java"
 * InterpDtTerm.java 1.2 01/07/23
 * Input stream interpreter
 * Decodes incoming characters into cursor motion etc.
 * 
 * See
 * http://h30097.www3.hp.com/docs/base_doc/DOCUMENTATION/V51_HTML/MAN/MAN5/0200____.HTM
 */

package org.netbeans.lib.terminalemulator;


class InterpDtTerm extends InterpProtoANSIX {

    protected static class InterpTypeDtTerm extends InterpTypeProtoANSIX {

	protected final State st_esc_rb_L = new State("esc_rb_N");// NOI18N

	protected final Actor act_done_collect3 = new ACT_DONE_COLLECT3();

	protected InterpTypeDtTerm() {
	    st_esc_lb.setAction('t', st_base, new ACT_GLYPH());
            st_esc_rb.setAction('l', st_esc_rb_L, act_collect);
            // LATER st_esc_rb.setAction('I', st_esc_rb_L, act_collect);
            st_esc_rb.setAction('L', st_esc_rb_L, act_collect);
	    for (char c = 0; c < 128; c++)
		st_esc_rb_L.setAction(c, st_esc_rb_L, act_collect);

	    st_esc_rb_L.setAction((char) 27, st_wait, act_nop);         // ESC
	    st_wait.setAction('\\', st_base, act_done_collect3);
	}

	private static final class ACT_GLYPH implements Actor {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		if (ai.noNumber()) {
		    return "ACT GLYPH: missing number";	// NOI18N
		} else {
		    int p1 = ai.numberAt(0);
		    int p2 = ai.numberAt(1);
		    int p3 = ai.numberAt(2);
		    if (p1 == 22) {
			ai.ops.op_glyph(p2, p3);
		    } else {
			return "ACT GLYPH: op othger than 22 not supported";	// NOI18N
		    } 
		} 
		return null;
	    }
	}

	private static final class ACT_DONE_COLLECT3 implements Actor {
            @Override
	    public String action(AbstractInterp ai, char c) {
		InterpProtoANSIX i = (InterpProtoANSIX) ai;
                String s = i.text.substring(1);
                switch (i.text.charAt(0)) {
                    case 'l':
                        ai.ops.op_win_title(s);
                        break;
                    case 'I':
                        // LATER ai.ops.op_icon_imagefile(s);
                        break;
                    case 'L':
                        ai.ops.op_icon_name(s);
                        break;
                }
		/* DEBUG
		System.out.println("DtTerm emulation: got '" + text + "'");	// NOI18N
		*/
		return null;
	    }
	}

    }

    private final InterpTypeDtTerm type;

    private static final InterpTypeDtTerm type_singleton = new InterpTypeDtTerm();

    public InterpDtTerm(Ops ops) {
	super(ops, type_singleton);
	this.type = type_singleton;
	setup();
    } 

    protected InterpDtTerm(Ops ops, InterpTypeDtTerm type) {
	super(ops, type);
	this.type = type;
	setup();
    } 

    @Override
    public String name() {
	return "dtterm";	// NOI18N
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
            case 2:
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
	state = type.st_base;
    }
}
