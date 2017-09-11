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
package org.netbeans.modules.mercurial.ui.annotate;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One line of annotation, this is copied from CVS so that other support classes stay the same.
 *
 * @author Maros Sandor
 */
public class AnnotateLine {

    private String  author;
    private String  revision;
    private String  id;
    private String  file;
    private Date    date;
    private String  content;
    private int     lineNum = -1; // default is unknown
    private int prevLineNum;

    private String commitMessage;
    private static final Pattern validUserFormat = Pattern.compile("(.+)\\<(.*)\\>.*"); //NOI18N
    
    /**
     * The default is true to enable rollback even if we were unable to determine the correct value.  
     */ 
    private boolean canBeRolledBack = true;
    private String username;

    public String getCommitMessage() {
        return commitMessage;
    }

    public void setCommitMessage(String commitMessage) {
        this.commitMessage = commitMessage;
    }

    /**
     * Returns the author of this line.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author of this line.
     */
    public void setAuthor(String author) {
        this.author = author;
        this.username = parseUsername(author);
    }

    /**
     * Returns the file of this line.
     */
    public String getFileName() {
        return file;
    }

    /**
    /**
     * Sets the file of this line.
     */
    public void setFileName(String file) {
        this.file = file;
    }

    /**
     * Returns the revision of this line.
     */
    public String getRevision() {
        return revision;
    }

    /**
     * Sets the revision of this line.
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    /**
     * Returns the changset id of this line.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the changeset id of this line.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the date of this line.
     */
    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
    
    /**
     * Return the line's content.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the line's content.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Returns the line's number. It's 1 based.
     */
    public int getLineNum() {
        return lineNum;
    }

    /**
     * Returns the line's number.
     */
    public Integer getLineNumInteger() {
        return new Integer(lineNum);
    }

    /**
     * Sets the line's number.
     */
    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
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

    public int getPreviousLineNumber () {
        return prevLineNum;
    }

    /**
     * Sets the previous line's number.
     */
    public void setPrevLineNum (int lineNum) {
        this.prevLineNum = lineNum;
    }
    
    public String getUsername () {
        if (username == null) {
            return "";
        } else {
            String shortened = username;
            if (shortened.length() > 15) {
                shortened = shortened.substring(0, 12) + "..."; //NOI18N
            }
            return shortened;
        }
    }
    
    private String parseUsername (String author) {
        if (author == null) {
            return null;
        } else {
            String shortened = null;
            String email = parseEmail(author);
            int pos;
            if (email != null && !email.isEmpty()) { 
                if ((pos = email.indexOf('@')) > -1) {
                    shortened = email.substring(0, pos);
                } else {
                    shortened = email;
                }
            }
            if (shortened == null) {
                shortened = author;
            }
            return shortened;
        }
    }

    private String parseEmail (String author) {
        String mail = author;
        Matcher m = validUserFormat.matcher(author.trim());
        if (m.matches()) {
            mail = m.groupCount() > 1 ? (m.group(2) != null ? m.group(2) : "") : ""; //NOI18N
            mail = mail.trim();
        }
        return mail;
    }
}
