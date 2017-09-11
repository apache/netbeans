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
        allTags = new LinkedHashMap<String, GitTag>(tags.size());
        RevWalk walk = new RevWalk(repository);
        try {
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
        } finally {
            walk.release();
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
