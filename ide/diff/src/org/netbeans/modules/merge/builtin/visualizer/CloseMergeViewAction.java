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

package org.netbeans.modules.merge.builtin.visualizer;

import org.openide.util.actions.CookieAction;
import org.openide.cookies.CloseCookie;

/**
 *
 * @author  Martin Entlicher
 */
public class CloseMergeViewAction extends CookieAction {

    private static final long serialVersionUID = 2746214508313015932L;

    protected Class[] cookieClasses() {
        return new Class[] { CloseCookie.class };
    }

    public org.openide.util.HelpCtx getHelpCtx() {
        return new org.openide.util.HelpCtx (CloseMergeViewAction.class);
    }

    public String getName() {
        return org.openide.util.NbBundle.getMessage(CloseMergeViewAction.class, "CloseAction");
    }
    
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected void performAction(org.openide.nodes.Node[] node) {
        if (node.length == 0) return;
        CloseCookie cc = (CloseCookie) node[0].getCookie (CloseCookie.class);
        if (cc != null) {
            cc.close();
        }
    }
    
}
