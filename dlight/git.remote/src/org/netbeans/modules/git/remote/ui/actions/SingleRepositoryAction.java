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

package org.netbeans.modules.git.remote.ui.actions;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.remote.utils.GitUtils;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;

/**
 *
 */
public abstract class SingleRepositoryAction extends GitAction {

    private static final Logger LOG = Logger.getLogger(SingleRepositoryAction.class.getName());

    protected SingleRepositoryAction () {
        this(null);
    }

    protected SingleRepositoryAction (String iconResource) {
        super(iconResource);
    }
    
    @Override
    protected final void performContextAction (final Node[] nodes) {
        Utils.postParallel(new Runnable () {
            @Override
            public void run() {
                VCSContext context = getCurrentContext(nodes);
                performAction(context);
            }
        }, 0);
    }

    public final void performAction (VCSContext context) {
        Map.Entry<VCSFileProxy, VCSFileProxy[]> actionRoots = getActionRoots(context);
        if (actionRoots != null) {
            GitUtils.logRemoteRepositoryAccess(actionRoots.getKey());
            performAction(actionRoots.getKey(), actionRoots.getValue(), context);
        }
    }
    
    protected abstract void performAction (VCSFileProxy repository, VCSFileProxy[] roots, VCSContext context);

    protected static Entry<VCSFileProxy, VCSFileProxy[]> getActionRoots (VCSContext context) {
        Set<VCSFileProxy> repositories = GitUtils.getRepositoryRoots(context);
        if (repositories.isEmpty()) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.log(Level.FINE, "No repository in the given context: {0}", context.getRootFiles()); //NOI18N
            }
            return null;
        }
        SimpleImmutableEntry<VCSFileProxy, VCSFileProxy[]> actionRoots = GitUtils.getActionRoots(context);
        if (actionRoots != null) {
            VCSFileProxy repository = actionRoots.getKey();
            if (repositories.size() > 1) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "Multiple repositories in the given context: {0}, selected {1}", new Object[] { context.getRootFiles(), repository }); //NOI18N
                }
            }
        }
        return actionRoots;
    }
}
