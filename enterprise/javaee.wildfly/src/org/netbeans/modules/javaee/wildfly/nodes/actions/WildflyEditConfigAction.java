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

package org.netbeans.modules.javaee.wildfly.nodes.actions;

import org.openide.cookies.EditCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author Emmanuel Hugonnet (ehsavoie) <ehsavoie@netbeans.org>
 */
public class WildflyEditConfigAction extends CookieAction {

    @Override
    protected boolean surviveFocusChange() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(WildflyEditConfigAction.class, "LBL_EditConfigAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.openide.actions.EditAction");
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class[] cookieClasses() {
        return new Class[]{EditCookie.class};
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        for (int i = 0; i < activatedNodes.length; i++) {
            EditCookie es = (EditCookie) activatedNodes[i].getLookup().lookup(EditCookie.class);
            if (es != null) {
                es.edit();
            }
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
