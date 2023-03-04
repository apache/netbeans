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
package org.netbeans.modules.maven.jaxws.actions;

import org.netbeans.modules.websvc.api.support.RefreshCookie;
import org.openide.util.actions.CookieAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;

public class JaxWsRefreshAction extends CookieAction {
    @Override
    public String getName() {
        return NbBundle.getMessage(JaxWsRefreshAction.class, "LBL_RefreshAction");
    }
    
    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    @Override
    protected Class[] cookieClasses() {
        return new Class[] {RefreshCookie.class};
    }
    
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        RefreshCookie cookie = 
           activatedNodes[0].getCookie(RefreshCookie.class);
        cookie.refreshService(true);
    }
  
}
