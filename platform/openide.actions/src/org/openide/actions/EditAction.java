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

import org.openide.cookies.EditCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;


/**
* Edit an object.
* @see EditCookie
*
* @author Jaroslav Tulach
*/
public class EditAction extends CookieAction {
    protected boolean surviveFocusChange() {
        return false;
    }

    public String getName() {
        return NbBundle.getBundle(EditAction.class).getString("Edit");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(EditAction.class);
    }

    protected int mode() {
        return MODE_ALL;
    }

    protected Class[] cookieClasses() {
        return new Class[] { EditCookie.class };
    }

    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            EditCookie es = activatedNodes[i].getCookie(EditCookie.class);

            if (es != null) {
                es.edit();
            }
        }
    }

    protected boolean asynchronous() {
        return false;
    }
}
