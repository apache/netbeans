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

package org.netbeans.core.actions;

import org.openide.LifecycleManager;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Shut down the system.
 * @author Ian Formanek, Jesse Glick, et al.
 */
public class SystemExit extends CallableSystemAction implements Runnable {
    
    private static final RequestProcessor RP = new RequestProcessor(SystemExit.class.getName(), 3);

    /** generated Serialized Version UID */
    private static final long serialVersionUID = 5198683109749927396L;

    /** Human presentable name of the action. This should be
     * presented as an item in a menu.
     * @return the name of the action
     */
    @Override
    public String getName() {
        return NbBundle.getMessage(SystemExit.class, "Exit");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.core.actions.SystemExit");
    }
    
    @Override
    protected boolean asynchronous() {
        // Not managed alongside other actions.
        return false;
    }

    @Override
    public void performAction() {
        // Do not run in AWT.
        RP.post(this);
    }

    /* Performs the exit (by calling LifecycleManager).*/
    @Override
    public void run() {
        LifecycleManager.getDefault().exit();
    }

    @Override
    protected void initialize() {
        super.initialize();
        putValue("noIconInMenu", Boolean.TRUE); // NOI18N
    }
    
}
