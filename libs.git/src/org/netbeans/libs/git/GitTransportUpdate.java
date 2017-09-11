/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.libs.git;

import java.util.Map;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.transport.RemoteRefUpdate;
import org.eclipse.jgit.transport.TrackingRefUpdate;
import org.eclipse.jgit.transport.URIish;

/**
 * Represents a result of transport and update of a git reference between a local and remote
 * repository.
 * Instance of this class is usually returned by inter-repository commands (as fetch or push).
 * 
 * @author Ondra Vrabec
 */
public final class GitTransportUpdate {

    private final String localName;
    private final String remoteName;
    private final String oldObjectId;
    private final String newObjectId;
    private final GitRefUpdateResult result;
    private final String uri;
    private final Type type;

    /**
     * Git object kind.
     */
    public enum Type {
        /**
         * a branch
         */
        BRANCH,
        /**
         * a tag
         */
        TAG,
        /**
         * a general reference
         */
        REFERENCE
    }

    GitTransportUpdate (URIish uri, TrackingRefUpdate update) {
        this.localName = stripRefs(update.getLocalName());
        this.remoteName = stripRefs(update.getRemoteName());
        this.oldObjectId = update.getOldObjectId() == null || ObjectId.zeroId().equals(update.getOldObjectId()) ? null : update.getOldObjectId().getName();
        this.newObjectId = update.getNewObjectId() == null || ObjectId.zeroId().equals(update.getNewObjectId()) ? null : update.getNewObjectId().getName();
        this.result = GitRefUpdateResult.valueOf((update.getResult() == null 
                ? RefUpdate.Result.NOT_ATTEMPTED 
                : update.getResult()).name());
        this.uri = uri.toString();
        this.type = getType(update.getLocalName());
    }

    GitTransportUpdate (URIish uri, RemoteRefUpdate update, Map<String, GitBranch> remoteBranches) {
        this.localName = stripRefs(update.getSrcRef());
        this.remoteName = stripRefs(update.getRemoteName());
        this.oldObjectId = getOldRevisionId(remoteBranches.get(remoteName));
        this.newObjectId = update.getNewObjectId() == null || ObjectId.zeroId().equals(update.getNewObjectId()) ? null : update.getNewObjectId().getName();
        this.result = GitRefUpdateResult.valueOf(update.getStatus().name());
        this.uri = uri.toString();
        this.type = getType(update.getRemoteName());
    }
    
    /**
     * @return URI of a remote repository
     */
    public String getRemoteUri () {
        return uri;
    }

    /**
     * Returns the name of the reference in a local repository without the prefix.
     * Instead of <code>refs/heads/master</code> <code>master</code> is returned.
     * @return name of the reference in a local repository without the reference prefix.
     */
    public String getLocalName () {
        return localName;
    }

    /**
     * Returns the name of the reference in a remote repository without the prefix.
     * Instead of <code>refs/heads/master</code> <code>master</code> is returned.
     * @return name of the reference in a remote repository without the reference prefix.
     */
    public String getRemoteName () {
        return remoteName;
    }

    /**
     * @return object id the reference pointed to before it was updated.
     */
    public String getOldObjectId () {
        return oldObjectId;
    }

    /**
     * @return object id the reference points to now, after the update.
     */
    public String getNewObjectId () {
        return newObjectId;
    }

    /**
     * Returns information about the result of the local/remote reference update.
     * @return result of the reference update
     */
    public GitRefUpdateResult getResult () {
        return result;
    }

    /**
     * @return kind of a git object this update refers to.
     */
    public Type getType () {
        return type;
    }

    private static String stripRefs (String refName) {
        if (refName == null) {
            
        } else if (refName.startsWith(Constants.R_HEADS)) {
            refName = refName.substring(Constants.R_HEADS.length());
        } else if (refName.startsWith(Constants.R_TAGS)) {
            refName = refName.substring(Constants.R_TAGS.length());
        } else if (refName.startsWith(Constants.R_REMOTES)) {
            refName = refName.substring(Constants.R_REMOTES.length());
        } else if (refName.startsWith(Constants.R_REFS)) {
            refName = refName.substring(Constants.R_REFS.length());
        } else {
            throw new IllegalArgumentException("Unknown refName: " + refName);
        }
        return refName;
    }

    private Type getType (String refName) {
        Type retval;
        if (refName.startsWith(Constants.R_TAGS)) {
            retval = Type.TAG;
        } else if (refName.startsWith(Constants.R_REMOTES)) {
            retval = Type.BRANCH;
        } else if (refName.startsWith(Constants.R_HEADS)) {
            retval = Type.BRANCH;
        } else if (refName.startsWith(Constants.R_REFS)) {
            retval = Type.REFERENCE;
        } else {
            throw new IllegalArgumentException("Unknown type for: " + refName);
        }
        return retval;
    }

    private String getOldRevisionId (GitBranch branch) {
        return branch == null ? null : branch.getId();
    }
}
