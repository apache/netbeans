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
package org.openide.util.datatransfer;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import java.io.IOException;


/** Describes a type that can be created anew. Used by <a href="@org-openide-nodes@/org/openide/nodes/Node.html#getNewTypes()">Node.getNewTypes</a>.
*
* @author Jaroslav Tulach
*/
public abstract class NewType extends Object implements HelpCtx.Provider {
    /** Display name for the creation action. This should be
    * presented as an item in a menu.
    *
    * @return the name of the action
    */
    public String getName() {
        return NbBundle.getBundle(NewType.class).getString("Create");
    }

    /** Help context for the creation action.
    * @return the help context
    */
    public HelpCtx getHelpCtx() {
        return org.openide.util.HelpCtx.DEFAULT_HELP;
    }

    /** Create the object.
    * @exception IOException if something fails
    */
    public abstract void create() throws IOException;

    /* JST: Originally designed for dnd and it now uses getDropType () of a node.
    *
    * Create the object at a specific position.
    * The default implementation simply calls {@link #create()}.
    * Subclasses may
    * allow pastes to a specific index in their
    * children list (if the object has children indexed by integer).
    *
    * @param indx index to insert into, can be ignored if not supported
    * @throws IOException if something fails
    *
    public void createAt (int indx) throws IOException {
      create ();
    }
    */
}
