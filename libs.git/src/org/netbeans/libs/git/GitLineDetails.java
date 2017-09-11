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

import java.io.File;

/**
 * Contains information needed to construct a file's line annotation.
 * 
 * @author Ondra Vrabec
 */
public final class GitLineDetails {
    private final GitRevisionInfo revision;
    private final GitUser author;
    private final GitUser committer;
    private final File sourceFile;
    private final int sourceLine;
    private final String content;

    GitLineDetails (String content, GitRevisionInfo revision, GitUser author, GitUser committer, File sourceFile, int sourceLine) {
        this.revision = revision;
        this.author = author;
        this.committer = committer;
        this.sourceFile = sourceFile;
        this.sourceLine = sourceLine;
        this.content = content;
    }
    
    /**
     * @return line's last modification's author
     */
    public GitUser getAuthor () {
        return author;
    }
    
    /**
     * @return line's last modification's committer
     */
    public GitUser getCommitter () {
        return committer;
    }

    /**
     * @return information about the commit that modified the line
     */
    public GitRevisionInfo getRevisionInfo () {
        return revision;
    }

    /**
     * @return the file that provided the line of the result.
     *         Can be different from the current file if the file was renamed.
     */
    public File getSourceFile () {
        return sourceFile;
    }

    /**
     * @return the original line number in the original file returned by <code>getSourceFile</code>
     *         in the revision returned by <code>getRevisionInfo</code>.
     */
    public int getSourceLine () {
        return sourceLine;
    }

    /**
     * @return current content of the file's line
     */
    public String getContent () {
        return content;
    }
}
