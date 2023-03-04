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

package org.netbeans.modules.bugtracking.util;

import java.io.File;
import java.util.MissingResourceException;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.ui.selectors.RepositorySelector;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka, Jan Stola
 * @author Marian Petras
 */
public class BugtrackingUtil {

    public static RepositoryImpl createRepository() {
        return createRepository(true);
    }
    
    public static RepositoryImpl createRepository(boolean selectNode) {
        RepositorySelector rs = new RepositorySelector();
        RepositoryImpl repo = rs.create(selectNode);
        return repo;
    }

    public static boolean editRepository(RepositoryImpl repository, String errorMessage) {
        RepositorySelector rs = new RepositorySelector();
        return rs.edit(repository, errorMessage);
    }

    public static boolean editRepository(Repository repository) {
        return editRepository(APIAccessor.IMPL.getImpl(repository), null);
    }

    public static BugtrackingConnector[] getBugtrackingConnectors() {
        DelegatingConnector[] dcs = BugtrackingManager.getInstance().getConnectors();
        BugtrackingConnector[] cons = new BugtrackingConnector[dcs.length];
        for (int i = 0; i < cons.length; i++) {
            cons[i] = dcs[i].getDelegate();
        }
        return cons;
    }

    public static void savePassword(char[] password, String prefix, String user, String url) throws MissingResourceException {
        if (password != null && password.length != 0) {                  
            Keyring.save(getPasswordKey(prefix, user, url), password, NbBundle.getMessage(BugtrackingUtil.class, "password_keyring_description", url)); // NOI18N
        } else {
            Keyring.delete(getPasswordKey(prefix, user, url));
        }
    }

    /**
     *
     * @param scrambledPassword
     * @param keyPrefix
     * @param url
     * @param user
     * @return
     */
    public static char[] readPassword(String scrambledPassword, String keyPrefix, String user, String url) {
        char[] password = Keyring.read(getPasswordKey(keyPrefix, user, url));
        return password != null ? password : new char[0];
    }

    private static String getPasswordKey(String prefix, String user, String url) {
        return (prefix != null ? prefix + "-" : "") + user + "@" + url;         // NOI18N
    }

    public static File getLargerSelection() {
        FileObject[] fos = BugtrackingUtil.getCurrentSelection();
        if(fos == null) {
            return null;
        }
        for (FileObject fo : fos) {
            FileObject ownerDirectory = getFileOwnerDirectory(fo);
            if (ownerDirectory != null) {
                fo = ownerDirectory;
        }
            File file = FileUtil.toFile(fo);
            if(file != null) {
                return file;
        }
        }        
        return null;
    }
    
    public static FileObject getFileOwnerDirectory(FileObject fileObject) {
        ProjectServices projectServices = BugtrackingManager.getInstance().getProjectServices();
        return projectServices != null ? projectServices.getFileOwnerDirectory(fileObject): null;
    }
    
    public static FileObject[] getCurrentSelection() {
        ProjectServices projectServices = BugtrackingManager.getInstance().getProjectServices();
        return projectServices != null ? projectServices.getCurrentSelection() : null;
    }
    
    /**
     * Determines if the jira plugin is instaled or not
     *
     * @return true if jira plugin is installed, otherwise false
     */
    public static boolean isJiraInstalled() {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector c : connectors) {
            // XXX hack
            if(c.getDelegate() != null && 
               c.getDelegate().getClass().getName().startsWith("org.netbeans.modules.jira")) // NOI18N
            {    
                return true;
            }
        }
        return false;
    }

    public static RepositoryImpl findNBRepository() {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector c : connectors) {
            BugtrackingConnector bugtrackingConnector = c.getDelegate();
            if ((bugtrackingConnector instanceof TeamBugtrackingConnector)) {
                TeamBugtrackingConnector teamConnector = (TeamBugtrackingConnector) bugtrackingConnector;
                if(teamConnector.getType() == TeamBugtrackingConnector.BugtrackingType.BUGZILLA) {
                    String id = teamConnector.findNBRepository(); // ensure repository exists
                    return RepositoryRegistry.getInstance().getRepository(c.getID(), id, true);
                }
            }
        }
        return null;
    }        
}
