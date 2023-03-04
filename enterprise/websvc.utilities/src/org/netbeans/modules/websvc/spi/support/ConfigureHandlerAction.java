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

package org.netbeans.modules.websvc.spi.support;

import org.netbeans.modules.websvc.api.support.ConfigureHandlerCookie;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * @author  rico
 */
public class ConfigureHandlerAction extends CookieAction{

    /** Creates a new instance of ConfigureHandlerAction */
    public ConfigureHandlerAction() {
    }

    public String getName() {
        return NbBundle.getMessage(ConfigureHandlerAction.class, "LBL_ConfigureHandlerAction");
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected Class[] cookieClasses() {
        return new Class[] {};
    }
    
    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1;
    }
    
    
    protected void performAction(Node[] activatedNodes) {
        final ConfigureHandlerCookie cookie =
            activatedNodes[0].getCookie(ConfigureHandlerCookie.class);
        if (cookie != null) {
            cookie.configureHandler();

            // logging usage of action
            Object[] params = new Object[2];
            String cookieClassName = cookie.getClass().getName();
            params[0] = cookieClassName.contains("jaxrpc") ? LogUtils.WS_STACK_JAXRPC : LogUtils.WS_STACK_JAXWS; //NOI18N
            params[1] = "CONFIGURE HANDLERS"; // NOI18N
            LogUtils.logWsAction(params);
        }
        
    }
    
}
