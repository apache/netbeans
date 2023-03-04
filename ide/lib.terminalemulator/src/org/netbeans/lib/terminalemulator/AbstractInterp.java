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
