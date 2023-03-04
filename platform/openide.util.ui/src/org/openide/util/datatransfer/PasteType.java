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
package org.openide.util.datatransfer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.awt.datatransfer.*;

import java.io.IOException;


/** Clipboard operation providing one kind of paste action. Used by
* <a href="@org-openide-nodes/org/openide/nodes.Node#getPasteTypes">Node.getPasteTypes</a>.
*
* @author Petr Hamernik
*/
public abstract class PasteType extends Object implements HelpCtx.Provider {
    /** Display name for the paste action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(PasteType.class).getString("Paste");
    }

    /** Help content for the action.
    * @return the help context
    */
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Perform the paste action.
    * @return transferable which should be inserted into the clipboard after the
    *         paste action. It can be <code>null</code>, meaning that the clipboard content
    *         is not affected. Use e.g. {@link ExTransferable#EMPTY} to clear it.
    * @throws IOException if something fails
    */
    public abstract Transferable paste() throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Perform the paste action at an index.
    * @see NewType#createAt(int)
    * @param indx index to insert into, can be ignored if not supported
    * @return new transferable to be inserted into the clipboard
    *  public Transferable pasteAt (int indx) throws IOException {
      return paste ();
    }
    */
}
