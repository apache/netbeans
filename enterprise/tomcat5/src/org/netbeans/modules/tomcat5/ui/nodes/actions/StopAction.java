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

package org.netbeans.modules.tomcat5.ui.nodes.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author  Petr Pisl
 */
public class StopAction extends NodeAction {

    /** Creates a new instance of Undeploy */
    public StopAction() {
    }

    @Override
    protected boolean enable(org.openide.nodes.Node[] nodes) {
        TomcatWebModuleCookie cookie;
        for (int i=0; i<nodes.length; i++) {
            cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);            
            if (cookie == null || !cookie.isRunning()) {
                return false;
            }
        }
         
        return true;
    }
    
    @Override
    public String getName() {
        return NbBundle.getMessage(StopAction.class, "LBL_StopAction"); //NOI18N
    }
    
    @Override
    protected void performAction(org.openide.nodes.Node[] nodes) {
        
        for (int i=0; i<nodes.length; i++) {
            TomcatWebModuleCookie cookie = (TomcatWebModuleCookie)nodes[i].getCookie(TomcatWebModuleCookie.class);            
            if (cookie != null) {
                cookie.stop();
            }
        }
    }
    
   
    
    @Override
    protected boolean asynchronous() { return false; }
    
    @Override
    public org.openide.util.HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
    
}
