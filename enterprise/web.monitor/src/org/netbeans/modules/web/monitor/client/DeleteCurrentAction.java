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

/*
 * DeleteCurrentAction.java
 *
 *
 * Created: Wed Mar  1 16:59:21 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

public class DeleteCurrentAction extends NodeAction {

    public DeleteCurrentAction() {}
    /**
     * Sets the name of the action
     */
    public String getName() {
	return NbBundle.getBundle(DeleteCurrentAction.class).getString("MON_Delete_current_10");
    }

    /**
     * Not implemented
     */
    public HelpCtx getHelpCtx() {
	return HelpCtx.DEFAULT_HELP;
    }

    public void performAction(Node[] node) { 
	// PENDING - this string... 
	MonitorAction.getController().deleteDirectory(Controller.currDirStr); 
    }

    public boolean enable(Node[] nodes) {
	return true;
    }

    public boolean asynchronous() { 
	return false; 
    } 
} // DeleteCurrentAction
