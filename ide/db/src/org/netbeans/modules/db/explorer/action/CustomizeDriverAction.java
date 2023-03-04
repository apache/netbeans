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

package org.netbeans.modules.db.explorer.action;

import org.netbeans.modules.db.explorer.dlg.AddDriverDialog;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

public class CustomizeDriverAction extends BaseAction {
    
    @Override
    public String getName() {
        return NbBundle.getMessage (CustomizeDriverAction.class, "Customize"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CustomizeDriverAction.class);
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes.length == 1;
    }
    
    public void performAction(Node[] activatedNodes) {
        Lookup lookup = activatedNodes[0].getLookup();
        final DriverNode node = lookup.lookup(DriverNode.class);
        if (node != null) {
            AddDriverDialog.showDialog(node);
        }
    }
}
