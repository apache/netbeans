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

import org.openide.cookies.ViewCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;


/**
* View an object (but do not edit it).
* @see ViewCookie
*
* @author Jan Jancura, Dafe Simonek
*/
public class ViewAction extends CookieAction {
    @Override
    protected boolean surviveFocusChange() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getBundle(ViewAction.class).getString("View");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ViewAction.class);
    }

    @Override
    protected int mode() {
        return MODE_ALL;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[] { ViewCookie.class };
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        if (activatedNodes == null) {
            return;
        }
        for (int i = 0; i < activatedNodes.length; i++) {
            ViewCookie es = activatedNodes[i].getCookie(ViewCookie.class);
            if (es != null) {
                es.view();
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
