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
package org.openide.actions;

import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;


/** Reorder items in a list with a dialog.
* @see Index
*
* @author   Petr Hamernik, Dafe Simonek
*/
public class ReorderAction extends CookieAction {
    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(ReorderAction.class).getString("Reorder");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ReorderAction.class);
    }

    protected Class[] cookieClasses() {
        return new Class[] { Index.class };
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    protected void performAction(Node[] activatedNodes) {
        Node n = activatedNodes[0]; // we supposed that one node is activated
        Index order = n.getCookie(Index.class);
        order.reorder();
    }

    protected boolean asynchronous() {
        return false;
    }
}
