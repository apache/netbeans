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

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.api.jaxws.project.config.Service;
import org.netbeans.modules.websvc.api.support.LogUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author rico
 */
public class ConvertToRestAction extends CookieAction {

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if(activatedNodes.length == 1){
            Node node = activatedNodes[0];
            Service service = node.getLookup().lookup(Service.class);
            FileObject implClass = node.getLookup().lookup(FileObject.class);
            if (implClass == null) {
                return false;
            }
            Project project = FileOwnerQuery.getOwner(implClass);
            if (project == null || project.getLookup().lookup(RestSupport.class) == null) {
                return false;
            }
            if(service != null){
                return !service.isUseProvider();
            }
        }
        return super.enable(activatedNodes);
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{ConvertToRestCookie.class};
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        if(activatedNodes.length == 1){
            Node node = activatedNodes[0];
            ConvertToRestCookie cookie = node.getCookie(ConvertToRestCookie.class);
            if(cookie != null){
                cookie.convertToRest();
                // logging usage of action
                Object[] params = new Object[2];
                params[0] = LogUtils.WS_STACK_JAXWS;
                params[1] = "CONVERT TO REST"; // NOI18N
                LogUtils.logWsAction(params);
            }
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(JaxWsRefreshAction.class, "LBL_ConvertToRestAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

}
