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
import java.text.MessageFormat;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.jgit.Utils;
import org.netbeans.libs.git.progress.FileListener;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class RenameCommand extends MoveTreeCommand {

    final File source;
    final File target;
    final boolean after;

    public RenameCommand (Repository repository, GitClassFactory gitFactory, File source, File target, boolean after, ProgressMonitor monitor, FileListener listener){
        super(repository, gitFactory, source, target, after, false, monitor, listener);
        this.source = source;
        this.target = target;
        this.after = after;
    }

    @Override
    protected boolean prepareCommand() throws GitException {
        boolean retval = super.prepareCommand();
        if (retval) {
            if (source.equals(getRepository().getWorkTree())) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_CannotMoveWT"), source.getAbsolutePath())); //NOI18N
            }
            if (!source.exists() && !after) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_SourceDoesNotExist"), source.getAbsolutePath())); //NOI18N
            }
            if (target.exists()) {
                if (!after) {
                    throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_TargetExists"), target.getAbsolutePath())); //NOI18N
                }
            } else if (after) {
                throw new GitException(MessageFormat.format(Utils.getBundle(RenameCommand.class).getString("MSG_Exception_TargetDoesNotExist"), target.getAbsolutePath())); //NOI18N
            }
        }
        return retval;
    }

    @Override
    protected String getCommandDescription() {
        return new StringBuilder("git mv ").append(source).append(" ").append(target).toString(); //NOI18N
    }
}
