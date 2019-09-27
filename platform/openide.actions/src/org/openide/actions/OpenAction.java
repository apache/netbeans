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

import org.openide.cookies.OpenCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.*;


/** Opens a node (for example, in a web browser, or in the Editor).
* @see OpenCookie
*
* @author   Petr Hamernik
*/
public class OpenAction extends CookieAction {
    protected Class[] cookieClasses() {
        return new Class[] { OpenCookie.class };
    }

    protected boolean surviveFocusChange() {
        return false;
    }

    protected int mode() {
        return MODE_ANY;
    }

    public String getName() {
        return NbBundle.getMessage(OpenAction.class, "Open");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(OpenAction.class);
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            OpenCookie oc = activatedNodes[i].getCookie(OpenCookie.class);

            if (oc != null) {
                oc.open();
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
