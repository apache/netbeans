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

import org.openide.cookies.PrintCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;


/** Print the selected object.
* @see PrintCookie
*
* @author Ales Novak
*/
public class PrintAction extends CookieAction {
    public PrintAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[] { PrintCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            PrintCookie pc = activatedNodes[i].getCookie(PrintCookie.class);

            if (pc != null) {
                pc.print();
            }
        }
    }

    protected boolean asynchronous() {
        return true;
    }

    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(PrintAction.class, "Print");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(PrintAction.class);
    }

    protected String iconResource() {
        return "org/openide/resources/actions/print.png"; // NOI18N
    }
}
