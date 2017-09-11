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
