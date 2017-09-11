/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008-2009 Sun Microsystems, Inc.
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
