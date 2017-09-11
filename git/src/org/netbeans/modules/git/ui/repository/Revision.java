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