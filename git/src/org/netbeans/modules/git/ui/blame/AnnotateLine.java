/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
