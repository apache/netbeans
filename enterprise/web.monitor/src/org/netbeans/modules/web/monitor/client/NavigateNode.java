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
 * NavigateNode.java
 *
 *
 * Created: Fri May 19 17:02:05 2000
 *
 * @author Ana von Klopp
 * @version
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;

public class NavigateNode extends AbstractNode {

    public NavigateNode(Children ch) {
	super(ch);
	setIconBaseWithExtension("org/netbeans/modules/web/monitor/client/icons/folder.gif"); //NOI18N
	setName(NbBundle.getBundle(NavigateNode.class).getString("MON_All_transactions_2"));
	//initialize();
    }

    /* Getter for set of actions that should be present in the
     * popup menu of this node. This set is used in construction of
     * menu returned from getContextMenu and specially when a menu for
     * more nodes is constructed.
     *
     * @return array of system actions that should be in popup menu
     */

    protected SystemAction[] createActions () {
	return new SystemAction[] {
            SystemAction.get(ReloadAction.class),
	    SystemAction.get(DeleteAllAction.class),
            null,
            SystemAction.get(SortAction.class),
            null,
            SystemAction.get(ShowTimestampAction.class),
	};
    }
    
} // NavigateNode
