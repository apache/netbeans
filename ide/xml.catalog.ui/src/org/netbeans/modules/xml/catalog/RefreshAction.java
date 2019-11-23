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
package org.netbeans.modules.xml.catalog;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 * Action sensitive to the node selection that refreshs children.
 *
 * @author  Petr Kuzel
 */
final class RefreshAction extends CookieAction {

    /** Serial Version UID */
    private static final long serialVersionUID =4798470042774935554L;

    protected void performAction (Node[] nodes) {
        if (nodes == null) return;
        try {
            for (int i = 0; i<nodes.length; i++) {
                String msg = NbBundle.getMessage(RefreshAction.class, "MSG_refreshing", nodes[i].getDisplayName());
                StatusDisplayer.getDefault().setStatusText(msg);
                Refreshable cake = nodes[i].getCookie(Refreshable.class);
                cake.refresh();
            }
        } finally {
            String msg = NbBundle.getMessage(RefreshAction.class, "MSG_refreshed");
            StatusDisplayer.getDefault().setStatusText(msg);
        }
    }

    public String getName () {
        return NbBundle.getMessage(RefreshAction.class, "LBL_Action");
    }

    protected String iconResource () {
        return null;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(getClass());
    }

    protected Class[] cookieClasses() {
        return new Class[] {Refreshable.class};
    }
    
    protected int mode() {
        return CookieAction.MODE_ALL;
    }
    
    protected boolean asynchronous() {
        return false;
    }

}
