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

package org.netbeans.modules.bugzilla.api;

import java.io.File;
import java.net.URL;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.Util;
import org.netbeans.modules.bugzilla.Bugzilla;
import org.netbeans.modules.bugzilla.issue.BugzillaIssue;
import org.netbeans.modules.bugzilla.repository.BugzillaRepository;
import org.netbeans.modules.bugzilla.repository.NBRepositorySupport;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class NBBugzillaUtils {

    /**
     * Opens in the IDE the given issue from the netbeans repository
     *
     * @param issueID issue identifier
     */
    public static void openIssue(String issueID) {
        Repository nbRepo = NBRepositorySupport.getInstance().getNBRepository(false);
        assert nbRepo != null;
        if(nbRepo == null) {
            Bugzilla.LOG.warning("No bugzilla repository available for netbeans.org"); // NOI18N
            return;
        }
        if(issueID != null) {
            Util.openIssue(nbRepo, issueID);
        } else {
            Util.createNewIssue(nbRepo);
        }
    }

    public static void reportAnIssue() {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                Repository nbRepo = NBRepositorySupport.getInstance().getNBRepository(true);
                if(nbRepo != null) {
                    Util.createNewIssue(nbRepo);
                }
            }
        });
    }
        
    /**
     * Returns the netbeans.org username
     * Shouldn't be called in awt
     *
     * @return username
     */
    public static String getNBUsername() {
        return org.netbeans.modules.bugtracking.commons.NBBugzillaUtils.getNBUsername();
    }

    /**
     * Returns the netbeans.org password
     * Shouldn't be called in awt
     *
     * @return password
     */
    public static char[] getNBPassword() {
        return org.netbeans.modules.bugtracking.commons.NBBugzillaUtils.getNBPassword();
    }

    /**
     * Save the given username as a netbeans.org username.
     * Shouldn't be called in awt
     */
    public static void saveNBUsername(String username) {
        org.netbeans.modules.bugtracking.commons.NBBugzillaUtils.saveNBUsername(username);
    }

    /**
     * Saves the given value as a netbeans.org password
     * Shouldn't be called in awt
     */
    public static void saveNBPassword(char[] password) {
        org.netbeans.modules.bugtracking.commons.NBBugzillaUtils.saveNBPassword(password);
    }

    /**
     * Determines wheter the given url is a netbeans.org url or not
     *
     * @return true if the given url is netbeans.org url, otherwise false
     */
    public static boolean isNbRepository(URL url) {
        assert url != null;
        return org.netbeans.modules.bugtracking.commons.NBBugzillaUtils.isNbRepository(url.toString());
    }

    public static Repository findNBRepository() {
        return NBRepositorySupport.getInstance().getNBRepository(false);
    }

    /**
     * Attaches files to the issue with the given id.
     * 
     * @param id issue id
     * @param comment comment to be added to the issue
     * @param desc attachment description per file
     * @param contentType content type per file
     * @param files files to be attached
     */
    public static void attachFiles(String id, String comment, String[] desc, String[] contentType, File[] files) {
        assert id != null;
        assert desc != null;
        assert files != null;
        assert contentType != null;
        assert desc.length == files.length;
        assert contentType.length == files.length;
        
        BugzillaRepository nbRepo = NBRepositorySupport.getInstance().getNBBugzillaRepository(false);
        BugzillaIssue issue = nbRepo.getIssue(id);
        if(issue == null) {
            return;
        }
        for (int i = 0; i < files.length; i++) {
            issue.addAttachment(files[i], comment, desc[i], contentType[i], false);
        }
        BugzillaUtil.openIssue(issue);
    }
    
}
