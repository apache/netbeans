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
package org.netbeans.modules.cloud.amazon.ui;

import org.netbeans.api.server.CommonServerUIs;
import org.netbeans.modules.cloud.amazon.AmazonInstance;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 *
 */
public class PropertiesAction extends NodeAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        AmazonInstance ai = activatedNodes[0].getLookup().lookup(AmazonInstance.class);
        CommonServerUIs.showCloudCustomizer(ai.getServerInstance());
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        return activatedNodes.length > 0 && activatedNodes[0].getLookup().lookup(AmazonInstance.class) != null;
    }

    @Override
    public String getName() {
        return "Properties";
    }

    
    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }
    
}
