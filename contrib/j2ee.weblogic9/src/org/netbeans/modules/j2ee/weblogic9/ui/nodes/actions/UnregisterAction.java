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
package org.netbeans.modules.j2ee.weblogic9.ui.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;


/**
 * @author ads
 *
 */
public class UnregisterAction extends NodeAction {

    /* (non-Javadoc)
     * @see org.openide.util.actions.NodeAction#enable(org.openide.nodes.Node[])
     */
    @Override
    protected boolean enable( Node[] nodes ) {
        for (int i = 0; i < nodes.length; i++) {
            UnregisterCookie cookie = nodes[i].getCookie(UnregisterCookie.class);
            if (cookie == null) {
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.NodeAction#performAction(org.openide.nodes.Node[])
     */
    @Override
    protected void performAction( Node[] nodes ) {
        for (int i = 0; i < nodes.length; i++) {
            UnregisterCookie cookie = nodes[i].getCookie(UnregisterCookie.class);
            if (cookie != null) {
                cookie.unregister();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.SystemAction#getHelpCtx()
     */
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    /* (non-Javadoc)
     * @see org.openide.util.actions.SystemAction#getName()
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(RefreshModulesAction.class, "LBL_UnregisterAction"); // NOI18N
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }

}
