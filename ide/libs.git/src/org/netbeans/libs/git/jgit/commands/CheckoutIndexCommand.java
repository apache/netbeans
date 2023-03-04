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
package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.dircache.DirCache;
import org.eclipse.jgit.dircache.DirCacheBuilder;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.utils.CheckoutIndex;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class CheckoutIndexCommand extends GitCommand {

    private final File[] roots;
    private final FileListener listener;
    private final ProgressMonitor monitor;
    private final boolean recursively;

    public CheckoutIndexCommand (Repository repository, GitClassFactory gitFactory, File[] roots, boolean recursively, ProgressMonitor monitor, FileListener listener) {
        super(repository, gitFactory, monitor);
        this.roots = roots;
        this.listener = listener;
        this.monitor = monitor;
        this.recursively = recursively;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        DirCache cache = null;
        try {
            // cache must be locked because checkout index may modify its entries
            cache = repository.lockDirCache();
            DirCacheBuilder builder = cache.builder();
            if (cache.getEntryCount() > 0) {
                builder.keep(0, cache.getEntryCount());
            }
            builder.finish();
            new CheckoutIndex(repository, cache, roots, recursively, listener, monitor, true).checkout();
            // cache must be saved to disk because checkout index may modify its entries
            builder.commit();
        } catch (IOException ex) {
            throw new GitException(ex);
        } finally {
            if (cache != null) {
                cache.unlock();
            }
        }
    }

    @Override
    protected String getCommandDescription () {
        StringBuilder sb = new StringBuilder("git checkout -- "); //NOI18N
        for (File root : roots) {
            sb.append(" ").append(root); //NOI18N
        }
        return sb.toString();
    }
}
