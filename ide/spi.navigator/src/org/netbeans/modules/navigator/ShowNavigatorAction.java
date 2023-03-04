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
package org.netbeans.modules.navigator;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CallableSystemAction;

/**
 * Action that opens the Navigator window
 *
 * @author Tim Boudreau, Dafe Simonek
 */
public class ShowNavigatorAction extends CallableSystemAction {

    public void performAction () {
        NavigatorTC navTC = NavigatorTC.getInstance();
        navTC.open();
        navTC.requestActive();
    }

    public String getName () {
        return NbBundle.getMessage(ShowNavigatorAction.class, "LBL_Action"); //NOI18N
    }

    protected String iconResource () {
        return "org/netbeans/modules/navigator/resources/navigator.png"; //NOI18N
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    public boolean asynchronous () {
        return false;
    }
}

