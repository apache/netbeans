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

public abstract class AbstractInterp implements Interp {

    protected interface Actor {
	public String action(AbstractInterp interp, char c);
    }

    protected static class State {

	// some generic actors
	Actor act_error = new Actor() {
	    @Override
	    public String action(AbstractInterp ai, char c) {
		return "generic error";	// NOI18N
	    } 
	};

	public String name() {
	    return name;
	} 
	private final String name;


	class Action {
	    public State new_state = null;
	    public Actor actor = act_error;
	};

	private final Action action[] = new Action[128];
	private final Action action_regular = new Action();

	public State(String name) {
	    this.name = name;
	    for (int i = 0; i < action.length; i++)
		action[i] = new Action();
	    action_regular.actor = null;
	    action_regular.new_state = null;
	}

	/*
	 * Specify the state action_regular will transition to.
	 */
	public void setRegular(State new_state, Actor actor) {
	    action_regular.actor = actor;
	    action_regular.new_state = new_state;
	} 

	public void setAction(char c, State new_state, Actor actor) {
	    if ((int) c > 127)
		return;
	    action[c].actor = actor;
	    action[c].new_state = new_state;
	}

	Action getAction(char c) {
	    if ((int) c > 127)
		return action_regular;
	    return action[c];
	} 
    };

    // Why make these be public and not protected?
    // Someone might inherit from us in a package other than org.netbeans
    // and while the inherited Interp will see these if they are protected, 
    // the corresponding InterpType won't.

    /*
    */
    public Ops ops;
    public State state;	// current state

    /*
    protected Ops ops;
    protected State state;	// current state
    */

    protected AbstractInterp(Ops ops) {
	this.ops = ops;
    } 

    public void reset() {
    } 

    protected final void sendChars(KeyEvent e, String s) {
        e.consume();
        ops.op_send_chars(s);
    }

    /*
     * Management of number parsing
     */

    private static final int MAX_NUMBERS = 5;
    private int numberx = 0;
    private final String number[] = new String[MAX_NUMBERS];

    protected void resetNumber() {
	for (int x = 0; x < MAX_NUMBERS; x++) {
	    number[x] = "";
	}
	numberx = 0;
    }
    protected void remember_digit(char c) {
	number[numberx] += c;
    }
    protected boolean pushNumber() {
	numberx++;
	return (numberx < MAX_NUMBERS);
    }
    protected boolean noNumber() {
	return number[0].equals("");	// NOI18N
    }
    protected int numberAt(int position) {
	// SHOULD pass in a fallback number instead of returning 0 or 1.
	if (position > numberx)
	    return 1;

	try {
	    return Integer.parseInt(number[position]);
	} catch (NumberFormatException x) {
	    return 0;
	}
    }
    protected int nNumbers() {
	return numberx;
    }
} 
