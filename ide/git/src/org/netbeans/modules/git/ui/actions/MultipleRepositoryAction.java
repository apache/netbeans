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
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.git.utils.GitUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor.Task;

/**
 *
 * @author ondra
 */
public abstract class MultipleRepositoryAction extends GitAction {

    private static final Logger LOG = Logger.getLogger(MultipleRepositoryAction.class.getName());

    protected MultipleRepositoryAction () {
        this(null);
    }

    protected MultipleRepositoryAction (String iconResource) {
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
        Set<File> repositories = GitUtils.getRepositoryRoots(context);
        if (repositories.isEmpty()) {
            LOG.log(Level.FINE, "No repository in the current context: {0}", context.getRootFiles()); //NOI18N
            return;
        }
        for (File repository : repositories) {
            GitUtils.logRemoteRepositoryAccess(repository);
            Task runningTask = performAction(repository, GitUtils.filterForRepository(context, repository), context);
            if (runningTask != null) {
                runningTask.waitFinished();
            }
        }
    }

    protected abstract Task performAction (File repository, File[] roots, VCSContext context);
}
