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
package org.netbeans.modules.java.api.common.project.ui;

import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author Tomas Zezula
 */
final class EditRootAction extends NodeAction {

    static interface Editable {
        public boolean canEdit();
        public void edit();
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        final Editable editable = activatedNodes[0].getLookup().lookup(Editable.class);
        assert editable != null;
        editable.edit();
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        final Editable editable =  activatedNodes[0].getLookup().lookup(Editable.class);
        if (editable == null) {
            return false;
        }
        return editable.canEdit();
    }

    @Override
    @NbBundle.Messages({"TXT_EditPlatform=Edit..."})
    public String getName() {
        return Bundle.TXT_EditPlatform();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(PlatformNode.class);
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

}


