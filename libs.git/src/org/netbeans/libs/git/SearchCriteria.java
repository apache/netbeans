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
import java.util.Date;

/**
 * Describes required search criteria for git commands that iterate through git
 * commits.
 */
public final class SearchCriteria {

    private int limit;
    private String revisionFrom;
    private String revisionTo;
    private Date from;
    private Date to;
    private File[] files;
    private boolean includeMerges = true;
    private String username;
    private String message;
    private boolean follow;

    /**
     * Default constructor.
     * Sets search criteria that every commit in the repository satisfies.
     */
    public SearchCriteria () {
        this.limit = -1;
        this.files = new File[0];
    }
    
    /**
     * Returns an array of files that an examined commit should touch.
     * A commit satisfies this criteria if:
     * <ul>
     * <li>the return value is an empty array</li>
     * <li>the commit's diff contains a record about any of the files in the array</li>
     * </ul>
     * @return non null array of files
     */
    public File[] getFiles () {
        return files;
    }

    /**
     * Sets files that a commit has to modify to satisfy the criteria.
     * @param files non null array of files
     * @throws IllegalArgumentException if files is null
     */
    public void setFiles (File[] files) throws IllegalArgumentException {
        if (files == null) {
            throw new IllegalArgumentException("Parameter files cannot be null"); //NOI18N
        }
        this.files = files;
    }

    /**
     * A maximum number of commits to list in a command result.
     * @return number of commits to return in a result or <code>-1</code> for 
     * all commits.
     */
    public int getLimit () {
        return limit;
    }

    /**
     * Sets the number of commits to list in a command's result.
     * @param limit number of commits, <code>-1</code> for all commits.
     */
    public void setLimit (int limit) {
        this.limit = limit;
    }

    /**
     * A commit is in compliance with this criteria if:
     * <ul>
     * <li>its id is equal to the return value</li>
     * <li>it is a descendant of the commit identified by the return value</li>
     * </ul>
     * That means no ancestors commit of the one identified by the value satisfy
     * the criteria.
     * @return commit id
     */
    public String getRevisionFrom () {
        return revisionFrom;
    }

    /**
     * Sets the id of an ancestor commit for all commits that should satisfy
     * the criteria.
     * @param revisionFrom ancestor commit id
     */
    public void setRevisionFrom (String revisionFrom) {
        this.revisionFrom = revisionFrom;
    }

    /**
     * A commit is in compliance with this criteria if:
     * <ul>
     * <li>its id is equal to the return value</li>
     * <li>it is an ancestor of the commit identified by the return value</li>
     * </ul>
     * That means no descendant commits of the one identified by the value satisfy
     * the criteria.
     * @return commit id of the latest satisfiable commit
     */
    public String getRevisionTo () {
        return revisionTo;
    }

    /**
     * Sets the id of the latest commit that satisfies the criteria
     * @param revisionTo commit id of the latest satisfiable commit
     */
    public void setRevisionTo (String revisionTo) {
        this.revisionTo = revisionTo;
    }

    /**
     * No commit that was created before than the date returned by this method
     * does not satisfy the criteria.
     */
    public Date getFrom () {
        return from;
    }

    /**
     * Sets the limit date all commits must satisfy.
     */
    public void setFrom (Date date) {
        this.from = date;
    }

    /**
     * No commit that was created later than the date returned by this method
     * does not satisfy the criteria.
     */
    public Date getTo () {
        return to;
    }

    /**
     * Sets the limit date all commits must satisfy.
     */
    public void setTo (Date date) {
        this.to = date;
    }

    /**
     * If the return value is <code>false</code> then no merge commits may 
     * satisfy the criteria. Default value is <code>true</code>.
     * @return <code>true</code> if merge commits are in compliance with 
     * the criteria, <code>false</code> if they should be omitted.
     */
    public boolean isIncludeMerges () {
        return includeMerges;
    }

    /**
     * If the given value is <code>false</code> then no merge commits will 
     * satisfy the criteria and be processed.
     */
    public void setIncludeMerges (boolean flag) {
        this.includeMerges = flag;
    }

    /**
     * Returns the string that must be part of a commit's author or committer
     * string for the commit to pass the criteria.
     * @return username substring
     */
    public String getUsername () {
        return username;
    }

    /**
     * Sets the string that must be part of a commit's author or committer
     * string for the commit to pass the criteria.
     */
    public void setUsername (String username) {
        this.username = username;
    }

    /**
     * Returns the string that must be part of a commit's message string 
     * for the commit to pass the criteria.
     * @return message substring
     */
    public String getMessage () {
        return message;
    }

    /**
     * Sets the string that must be part of a commit's message string 
     * for the commit to pass the criteria.
     */
    public void setMessage (String message) {
        this.message = message;
    }

    /**
     * Returns <code>true</code> if a command that iterates through a collection
     * of commits should follow path renames and do not stop on a commit where 
     * an interesting file (given by the <code>getFiles</code> method) is 
     * renamed to another.
     */
    public boolean isFollow () {
        return follow;
    }

    /**
     * Set the flag indicating the commits where a renamed file is modified 
     * satisfy the given criteria.
     */
    public void setFollowRenames (boolean flag) {
        this.follow = flag;
    }
}
