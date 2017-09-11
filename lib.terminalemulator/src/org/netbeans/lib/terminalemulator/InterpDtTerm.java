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
 *			"Portions Copyrighted [year] [name of copyright owner]"
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
