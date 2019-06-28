// <editor-fold defaultstate="collapsed" desc=" License Header ">
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
//</editor-fold>

package org.netbeans.modules.payara.common.nodes.actions;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public final class EditDetailsAction extends NodeAction {

    protected void performAction(Node[] nodes) {
        if((nodes == null) || (nodes.length < 1)) {
            return;
        }

        for(Node node : nodes) {
            EditDetailsCookie uCookie = node.getCookie(EditDetailsCookie.class);

            if(uCookie != null) {
                uCookie.openCustomizer();
            }
        }
    }

    public String getName() {
        return NbBundle.getMessage(EditDetailsAction.class, "CTL_EditDetailsAction");
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE);
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] nodes) {
        for(Node node : nodes) {
            EditDetailsCookie cookie = node.getCookie(EditDetailsCookie.class);
            if(cookie == null) { // || cookie.isRunning()) {
                return false;
            }
        }

        return true;
    }
}

