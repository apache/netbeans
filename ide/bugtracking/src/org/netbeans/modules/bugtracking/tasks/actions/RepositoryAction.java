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
package org.netbeans.modules.bugtracking.tasks.actions;

import javax.swing.AbstractAction;
import org.netbeans.modules.bugtracking.tasks.dashboard.RepositoryNode;

/**
 *
 * @author jpeska
 */
public abstract class RepositoryAction extends AbstractAction {

    private RepositoryNode[] repositoryNodes;

    public RepositoryAction(String name, RepositoryNode... repositoryNodes) {
        super(name);
        this.repositoryNodes = repositoryNodes;
    }

    @Override
    public boolean isEnabled() {
        for (RepositoryNode repositoryNode : repositoryNodes) {
            if (!repositoryNode.isOpened()) {
                return false;
            }
        }
        return true;
    }

    public RepositoryNode[] getRepositoryNodes() {
        return repositoryNodes;
    }
}
