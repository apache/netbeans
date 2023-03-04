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
