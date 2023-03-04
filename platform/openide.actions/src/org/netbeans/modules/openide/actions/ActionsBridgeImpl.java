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

package org.netbeans.modules.openide.actions;

import org.openide.util.lookup.ServiceProvider;
import org.openide.util.actions.ActionInvoker;


/** Implements the delegation to ActionManager that is called from
 * openide/util.
 */
@ServiceProvider(service=ActionInvoker.class)
public class ActionsBridgeImpl extends ActionInvoker {
    /** Invokes an action.
     */
    @SuppressWarnings("deprecation")
    protected void invokeAction (javax.swing.Action action, java.awt.event.ActionEvent ev) {
        org.openide.actions.ActionManager.getDefault().invokeAction(action, ev);
    }
}
