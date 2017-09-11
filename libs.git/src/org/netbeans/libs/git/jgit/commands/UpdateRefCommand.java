/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
    protected void run () throws GitException {
        Repository repository = getRepository();
        try {
            
            Ref ref = repository.getRef(refName);
            if (ref == null || ref.isSymbolic()) {
                // currently unable to update symbolic references
                result = GitRefUpdateResult.valueOf(RefUpdate.Result.NOT_ATTEMPTED.name());
                return;
            }
            
            Ref newRef = repository.getRef(revision);
            String name;
            if (newRef == null) {
                ObjectId id = repository.resolve(revision);
                newRef = new ObjectIdRef.Unpeeled(Ref.Storage.LOOSE, id.name(),id.copy());
                name = newRef.getName();
            } else {
                name = revision;
            }
            
            RefUpdate u = repository.updateRef(ref.getName());
            newRef = repository.peel(newRef);
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
    protected String getCommandDescription () {
        return new StringBuilder("git update-ref ").append(refName).append(" ").append(revision).toString(); //NOI18N
    }

    public GitRefUpdateResult getResult () {
        return result;
    }
    
}
