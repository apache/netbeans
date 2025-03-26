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
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectIdRef;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitRefUpdateResult;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class UpdateRefCommand extends GitCommand {
    private final String revision;
    private final String refName;
    private GitRefUpdateResult result;

    public UpdateRefCommand (Repository repository, GitClassFactory gitFactory, String refName, String revision, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.refName = refName;
        this.revision = revision;
    }

    @Override
    protected void run() throws GitException {
        Repository repository = getRepository();
        try {
            
            Ref ref = repository.findRef(refName);
            if (ref == null || ref.isSymbolic()) {
                // currently unable to update symbolic references
                result = GitRefUpdateResult.valueOf(RefUpdate.Result.NOT_ATTEMPTED.name());
                return;
            }
            
            Ref newRef = repository.findRef(revision);
            String name;
            if (newRef == null) {
                ObjectId id = repository.resolve(revision);
                newRef = new ObjectIdRef.Unpeeled(Ref.Storage.LOOSE, id.name(),id.copy());
                name = newRef.getName();
            } else {
                name = revision;
            }
            
            RefUpdate u = repository.updateRef(ref.getName());
            newRef = repository.getRefDatabase().peel(newRef);
            ObjectId srcObjectId = newRef.getPeeledObjectId();
            if (srcObjectId == null) {
                srcObjectId = newRef.getObjectId();
            }
            u.setNewObjectId(srcObjectId);
            u.setRefLogMessage("merge " + name + ": Fast-forward", false); //NOI18N
            u.update();
            result = GitRefUpdateResult.valueOf((u.getResult() == null 
                ? RefUpdate.Result.NOT_ATTEMPTED
                : u.getResult()).name());
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription() {
        return "git update-ref "+ refName + " " + revision; //NOI18N
    }

    public GitRefUpdateResult getResult() {
        return result;
    }
    
}
