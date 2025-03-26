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
package org.netbeans.libs.git.jgit.commands;

import java.io.IOException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class DeleteTagCommand extends GitCommand {
    private final String tagName;

    public DeleteTagCommand(Repository repository, GitClassFactory gitFactory, String tagName, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.tagName = tagName;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            Ref currentRef = repository.exactRef(Constants.R_TAGS + tagName);
            if (currentRef == null) {
                throw new GitException.MissingObjectException(tagName, GitObjectType.TAG);
            }
            RefUpdate update = repository.updateRef(currentRef.getName());
            update.setRefLogMessage("tag deleted", false);
            update.setForceUpdate(true);
            Result deleteResult = update.delete();

            switch (deleteResult) {
                case IO_FAILURE:
                case LOCK_FAILURE:
                case REJECTED:
                    throw new GitException.RefUpdateException("Cannot delete tag " + tagName, GitRefUpdateResult.valueOf(deleteResult.name()));
            }
        } catch (IOException ex) {
            throw new GitException(ex);
        }
        
    }

    @Override
    protected String getCommandDescription() {
        return "git tag -d " + tagName;
    }
}
