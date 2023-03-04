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
     * @return lower bounds date
     */
    public Date getFrom () {
        return from;
    }

    /**
     * Sets the limit date all commits must satisfy.
     * @param date lower bounds date
     */
    public void setFrom (Date date) {
        this.from = date;
    }

    /**
     * No commit that was created later than the date returned by this method
     * does not satisfy the criteria.
     * @return upper bound date
     */
    public Date getTo () {
        return to;
    }

    /**
     * Sets the limit date all commits must satisfy.
     * @param date upper dounds date
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
     * @param flag true to include merges
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
     * @param username substring
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
     * @param message substring
     */
    public void setMessage (String message) {
        this.message = message;
    }

    /**
     * Returns <code>true</code> if a command that iterates through a collection
     * of commits should follow path renames and do not stop on a commit where 
     * an interesting file (given by the <code>getFiles</code> method) is 
     * renamed to another.
     * @return true if follow renames
     */
    public boolean isFollow () {
        return follow;
    }

    /**
     * Set the flag indicating the commits where a renamed file is modified 
     * satisfy the given criteria.
     * @param flag follow renames
     */
    public void setFollowRenames (boolean flag) {
        this.follow = flag;
    }
}
