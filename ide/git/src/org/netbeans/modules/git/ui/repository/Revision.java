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

package org.netbeans.modules.git.ui.repository;

import org.netbeans.libs.git.GitBranch;
import org.netbeans.modules.git.utils.GitUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
public class Revision {
    private final String revision;
    private final String name;
    private final String shortMessage;
    private final String fullMessage;

    public Revision (String revision, String name) {
        this(revision, name, null, null);
    }
    
    public Revision (String revision, String name, String shortMessage, String fullMessage) {
        this.revision = revision;
        this.name = name;
        this.shortMessage = shortMessage;
        this.fullMessage = fullMessage;
    }
    
    @NbBundle.Messages("LBL_Revision.LOCAL.name=Local Changes")
    public static final Revision LOCAL = new Revision(Bundle.LBL_Revision_LOCAL_name(), Bundle.LBL_Revision_LOCAL_name()) {

        @Override
        public String toString (boolean shorten) {
            return getRevision();
        }
    };
    @NbBundle.Messages("LBL_Revision.HEAD.name=HEAD")
    public static final Revision HEAD = new Revision(GitUtils.HEAD, Bundle.LBL_Revision_HEAD_name()) {

        @Override
        public String toString (boolean shorten) {
            return getRevision();
        }
    };

    public String getCommitId () {
        return revision;
    }

    public String getRevision () {
        return name;
    }

    @Override
    public String toString () {
        return toString(false);
    }

    public String toString (boolean shorten) {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.equals(revision)) {
            sb.append(name).append(" (").append(shorten //NOI18N
                    ? revision.substring(0, 7)
                    : revision).append(")"); //NOI18N
        } else {
            if (shorten && revision.length() > 7) {
                sb.append(revision.substring(0, 7));
            } else {
                sb.append(revision);
            }
        }
        if (shortMessage != null && !shortMessage.isEmpty()) {
            sb.append(" - ").append(shortMessage); //NOI18N
        }
        return sb.toString();
    }

    @Override
    public final boolean equals (Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Revision)) {
            return false;
        }
        Revision other = (Revision) obj;
        return name.equals(other.name) && revision.equals(other.revision);
    }

    @Override
    public final int hashCode () {
        int hash = 7;
        hash = 97 * hash + (this.revision != null ? this.revision.hashCode() : 0);
        hash = 97 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

    public String getShortMessage () {
        return shortMessage;
    }

    public String getFullMessage () {
        return fullMessage;
    }
    
    public static final class BranchReference extends Revision {
        private final String branchName;
        private final String commitId;

        public BranchReference (GitBranch branch) {
            this(branch.getName(), branch.getId());
        }

        public BranchReference (String branchName, String commitId) {
            super(commitId, branchName);
            this.branchName = branchName;
            this.commitId = commitId;
        }

        @Override
        public String toString (boolean shorten) {
            if (shorten) {
                return branchName;
            } else {
                return new StringBuilder(branchName).append(" (") //NOI18N
                        .append(commitId.substring(0, 7)).append(")").toString(); //NOI18N
            }
        }
    }
}