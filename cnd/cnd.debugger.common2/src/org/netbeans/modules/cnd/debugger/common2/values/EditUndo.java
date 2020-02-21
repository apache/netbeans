/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.debugger.common2.values;

/*
 * HACK
 * Utility for managing asynchronous property validation.
 *
 * NOTE: In NB 5.x ...
 * - we aquired a bonafide panel-style breakpoint editor
 * - debuggercore provides us with aninterface which doesn' directly involve
 *   Nodes, properties or property sheets.
 * However, AsyncEditor and this undo facility is still relevant for in-place
 * editing of breakpoint properties in the table cells.
 * 
 *
 * NB properties have an "editor", a gui component, that allows the user 
 * to specify the new value. The editor, through the PropertyEditorSupport
 * "interface", ultimately calls the property setter on a Node. Normally
 * the setter is supposed to validate the new value and either accept it
 * or throw an IllegalArgumentException in order to reject the new value. 
 * 
 * Unfortunately in our case the setter usually has to forward a message to
 * the engine for validation so ...
 * ... it cannot block because the engine interacts with us asynchronously.
 * ... it cannot go to sleep on something to happen because it's in the AWT EQ.
 * ... it cannot't throw an IllegalArgumentException because that's too "noisy".
 * So it just quietly returns as if the property is validated.
 *
 * Eventually the engine will get around to doing a specific ACK for that
 * edit or it will send a generic error message. In the specific ACK case
 * the Node property gets truly changed. In the case of errors the property
 * value of the _Node_ is OK, but the _editor_ has the value that the user
 * had entered and that one needs to be reset, (this happens by firing
 * the property change event causing the editor to re-pull the original value)
 * This is where EditUndo comes in.
 *
 * Becauese the error message from the engine may be completely generic an
 * instance of EditUndo is retained to connect the error message with a
 * specific node and property. Here's how it works:
 * - When the property setter gets called (the one that is supposed to fwd
 *   a msg to the engine) it creates a EditUndo with the proper back-pointing
 *   data. This gets recorded in 'EditUndo.current'.
 * - At some point Dbx.sendCommand() is called. It moves the EditUndo from
 *   'EditUndo.current' to 'EditUndo.pending'. This is done via 'advance()'.
 *  'current' is nulled.
 * - If an error message comes before the next sendCommand() goes out the
 *   instance in 'pending' is retrieved and run. It typically gets the
 *   node to invalidate the value of the relevant property. This happens
 *   through the EditUndoable interface.
 * - If no error message comes back or even if one does, on the next 
 *   sendCommand() 'pending' is replaced by a 'null' from 'current' or
 *   a new EditUndo. This way the likelihood of an unrelated message
 *   "undoing" values in property editors is diminished although this
 *   is NOT PERFECT and this solution is ultimately a HACK.
 */

public class EditUndo implements Runnable {
    private static EditUndo current;
    private static EditUndo pending;

    private EditUndoable target;
    private String property;

    public EditUndo() {
	if (current != null)
	    System.out.println("EditUndo multiple instances not supported"); // NOI18N
	current = this;
    }

    public static void advance() {
	pending = current;
	current = null;
    } 

    public static void undo() {
	if (pending != null)
	    pending.run();
    }



    public EditUndo(EditUndoable target, String property) {
	this();
	this.target = target;
	this.property = property;
    }

    @Override
    public void run() {
	target.undo(property);
    }
}
