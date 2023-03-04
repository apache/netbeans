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

package org.netbeans.modules.git.ui.actions;

import java.io.File;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;

/**
 *
 * @author ondra
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
        Map.Entry<File, File[]> actionRoots = getActionRoots(context);
        if (actionRoots != null) {
            GitUtils.logRemoteRepositoryAccess(actionRoots.getKey());
            performAction(actionRoots.getKey(), actionRoots.getValue(), context);
        }
    }
    
    protected abstract void performAction (File repository, File[] roots, VCSContext context);

    protected static Entry<File, File[]> getActionRoots (VCSContext context) {
        Set<File> repositories = GitUtils.getRepositoryRoots(context);
        if (repositories.isEmpty()) {
            LOG.log(Level.FINE, "No repository in the given context: {0}", context.getRootFiles()); //NOI18N
            return null;
        }
        SimpleImmutableEntry<File, File[]> actionRoots = GitUtils.getActionRoots(context);
        if (actionRoots != null) {
            File repository = actionRoots.getKey();
            if (repositories.size() > 1) {
                LOG.log(Level.FINE, "Multiple repositories in the given context: {0}, selected {1}", new Object[] { context.getRootFiles(), repository }); //NOI18N
            }
        }
        return actionRoots;
    }
}
