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
package org.netbeans.modules.git.ui.blame;

import java.io.File;
import java.util.logging.Level;
import org.netbeans.libs.git.GitLineDetails;
import org.netbeans.libs.git.GitRevisionInfo;
import org.netbeans.libs.git.GitUser;
import org.openide.util.NbBundle;

/**
 * One line of annotation, this is copied from CVS so that other support classes stay the same.
 *
 * @author Maros Sandor
 */
public class AnnotateLine {

    private final GitUser  author;
    private final String   authorShort;
    private final GitUser  committer;
    private final GitRevisionInfo  revision;
    private final File    file;
    private final String  content;
    private final int     lineNum;
    private final int sourceLine;

    
    /**
     * The default is true to enable rollback even if we were unable to determine the correct value.  
     */ 
    private boolean canBeRolledBack = true;
    private static final String fakeItem = NbBundle.getMessage(AnnotateLine.class, "MSG_AnnotateAction.lineDetail.unknown"); //NOI18N

    AnnotateLine (GitLineDetails lineDetails, int lineNumber) {
        if (lineDetails == null) {
            revision = null;
            author = null;
            authorShort = fakeItem;
            committer = null;
            content = fakeItem;
            file = null;
            sourceLine = -1;
        } else {
            revision = lineDetails.getRevisionInfo();
            author = lineDetails.getAuthor() == null ? lineDetails.getCommitter() : lineDetails.getAuthor();
            authorShort = getAuthorShort(author);
            committer = lineDetails.getCommitter();
            String cont = lineDetails.getContent().replace("\r", "").replace("\n", ""); //NOI18N
            if (cont.length() != lineDetails.getContent().length()) {
                AnnotationBar.LOG.log(Level.FINE, "AnnotateLine: line content contains '\\r' or '\\n': {0}:{1}", new Object[] { lineDetails.getSourceFile(), lineNumber }); //NOI18N
            }
            content = cont;
            file = lineDetails.getSourceFile();
            sourceLine = lineDetails.getSourceLine();
        }
        lineNum = lineNumber;
    }

    /**
     * Returns the author of this line.
     */
    public GitUser getAuthor() {
        return author;
    }

    /**
     * Returns the file of this line.
     */
    public File getFile () {
        return file;
    }

    /**
     * Returns the revision of this line.
     */
    public GitRevisionInfo getRevisionInfo() {
        return revision;
    }
    
    /**
     * Return the line's content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Returns the line's number. It's 1 based.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Returns the line's number in the previous source file. It's 0 based.
     */
    public int getSourceLineNum () {
        return sourceLine;
    }

    /**
     * @return false if the file was added to repository (created) in this revision, true otherwise
     */ 
    public boolean canBeRolledBack() {
        return this.canBeRolledBack;
    }

    public void setCanBeRolledBack(boolean canBeRolledBack) {
        this.canBeRolledBack = canBeRolledBack;
    }

    public String getAuthorShort () {
        return authorShort;
    }

    private String getAuthorShort (GitUser author) {
        if (author == null) {
            return fakeItem;
        } else {
            String shortened = null;
            String email = author.getEmailAddress();
            int pos;
            if (email != null && (pos = email.indexOf("@")) > -1) {
                shortened = email.substring(0, pos);
            }
            if (shortened == null) {
                shortened = author.toString();
                if (shortened.length() > 10) {
                    pos = shortened.indexOf(' ', 7);
                    if (pos > 0 && pos <= 10) {
                        shortened = shortened.substring(0, pos);
                    }
                }
            }
            if (shortened.length() > 10) {
                shortened = shortened.substring(0, 7) + "..."; //NOI18N
            }
            return shortened;
        }
    }
}
