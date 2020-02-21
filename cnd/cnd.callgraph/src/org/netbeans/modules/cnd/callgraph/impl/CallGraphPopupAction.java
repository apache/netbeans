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

package org.netbeans.modules.cnd.callgraph.impl;

import org.netbeans.modules.cnd.callgraph.api.CallModel;
import org.netbeans.modules.cnd.callgraph.api.ui.CallGraphModelFactory;
import org.openide.awt.DynamicMenuContent;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 */
public final class CallGraphPopupAction extends CookieAction {
    
    public CallGraphPopupAction() {
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, Boolean.TRUE);
    }

    protected void performAction(Node[] activatedNodes) {
        CallModel model = CallGraphModelFactory.getDefault().getModel(activatedNodes);
        if (model != null){
            CallGraphTopComponent view = CallGraphTopComponent.findInstance();
            view.setModel(model, CallGraphModelFactory.getDefault().getUI(model)); 
            view.open();
            view.requestActive();
        }
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return CallGraphModelFactory.getDefault().isCallGraphAvailiable(activatedNodes);
    }

    protected int mode() {
        return CookieAction.MODE_EXACTLY_ONE;
    }

    public String getName() {
        return NbBundle.getMessage(CallGraphPopupAction.class, "CTL_CallGraphPopupAction"); // NOI18N
    }

    protected Class[] cookieClasses() {
        return new Class[]{EditorCookie.class};
    }

    @Override
    protected void initialize() {
        super.initialize();
        // see org.openide.util.actions.SystemAction.iconResource() Javadoc for more details
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
}
