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
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevWalk;
import org.netbeans.libs.git.GitException;
import org.netbeans.libs.git.GitObjectType;
import org.netbeans.libs.git.GitTag;
import org.netbeans.libs.git.jgit.GitClassFactory;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ListTagCommand extends GitCommand {
    private Map<String, GitTag> allTags;
    private final boolean all;

    public ListTagCommand (Repository repository, GitClassFactory gitFactory, boolean all, ProgressMonitor monitor) {
        super(repository, gitFactory, monitor);
        this.all = all;
    }

    @Override
    protected void run () throws GitException {
        Repository repository = getRepository();
        Map<String, Ref> tags = repository.getTags();
        allTags = new LinkedHashMap<>(tags.size());
        try (RevWalk walk = new RevWalk(repository);) {
            for (Map.Entry<String, Ref> e : tags.entrySet()) {
                GitTag tag;
                try {
                    tag = getClassFactory().createTag(walk.parseTag(e.getValue().getLeaf().getObjectId()));
                } catch (IncorrectObjectTypeException ex) {
                    tag = getClassFactory().createTag(e.getKey(),
                            getClassFactory().createRevisionInfo(walk.parseCommit(e.getValue().getLeaf().getObjectId()), repository));
                }
                if (all || tag.getTaggedObjectType() == GitObjectType.COMMIT) {
                    allTags.put(tag.getTagName(), tag);
                }
            }
        } catch (MissingObjectException ex) {
            throw new GitException.MissingObjectException(ex.getObjectId().getName(), GitObjectType.TAG);
        } catch (IOException ex) {
            throw new GitException(ex);
        }
    }

    @Override
    protected String getCommandDescription () {
        return "git tag -l"; //NOI18N
    }

    public Map<String, GitTag> getTags () {
        return allTags;
    }

}
