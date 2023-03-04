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

/**
 * Identification of a git user.
 * User is specified by a name and e-mail address.
 * @author Jan Becicka
 * @author Tomas Stupka
 */
public final class GitUser {

    private final String name;
    private final String email;

    /**
     * 
     * @param name user's human readable name
     * @param email user's address
     */
    public GitUser (String name, String email) {
        this.name = name;
        this.email = email;
    }

    /**
     * Returns user's name
     * @return user's name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns user's email address
     * @return user's email address
     */
    public String getEmailAddress() {
        return email;
    }
        
    @Override 
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof GitUser)) {
            return false;
        }
        final GitUser other = (GitUser) obj;
        if ((this.getName() == null) ? (other.getName() != null) : !this.getName().equals(other.getName())) {
            return false;
        }
        if ((this.getEmailAddress() == null) ? (other.getEmailAddress() != null) : !this.getEmailAddress().equals(other.getEmailAddress())) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.getName() != null ? this.getName().hashCode() : 0);
        hash = 37 * hash + (this.getEmailAddress() != null ? this.getEmailAddress().hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        String name = getName();
        String mail = getEmailAddress();
        return name + (mail != null && !mail.isEmpty() ? " <" + mail + ">" : ""); //NOI18N
    }
}
