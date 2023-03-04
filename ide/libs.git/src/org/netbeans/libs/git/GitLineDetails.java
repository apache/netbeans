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
