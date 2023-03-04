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

/**
 * @author Ana von Klopp
 */

package org.netbeans.modules.web.monitor.client;

import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;

public class SavedNode extends AbstractNode {

    public SavedNode(Children ch) {
	super(ch);
	setIconBaseWithExtension("org/netbeans/modules/web/monitor/client/icons/folder.gif"); //NOI18N
	setName(NbBundle.getBundle(SavedNode.class).getString("MON_Saved_Transactions_22"));
    }
    protected SystemAction[] createActions () {
	return new SystemAction[] {
	    SystemAction.get(DeleteSavedAction.class),
	};
    }
} // SavedNode
