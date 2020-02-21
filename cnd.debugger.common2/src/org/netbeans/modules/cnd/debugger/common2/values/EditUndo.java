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
