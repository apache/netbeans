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

package org.netbeans.modules.websvc.core.jaxws.actions;

import java.io.IOException;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.openide.ErrorManager;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author rico
 */
public class JaxWsGenWSDLAction extends CookieAction{

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{JaxWsGenWSDLCookie.class};
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        JaxWsGenWSDLCookie cookie = activatedNodes[0].getCookie(JaxWsGenWSDLCookie.class);
        if(cookie != null){
            try {
                cookie.generateWSDL();
                
                // logging usage of action
                Object[] params = new Object[2];
                params[0] = LogUtils.WS_STACK_JAXWS;
                params[1] = "GENERATE WSDL"; // NOI18N
                LogUtils.logWsAction(params);

            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(JaxWsGenWSDLAction.class, "LBL_Generate_WSDL");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
