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

package org.netbeans.modules.bugzilla.repository;

import java.util.Collection;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugzilla.BugzillaConnector;
import org.netbeans.modules.bugzilla.commands.ValidateCommand;
import org.netbeans.modules.bugzilla.util.BugzillaUtil;
import org.netbeans.modules.team.spi.TeamAccessor;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class NBRepositorySupport extends BugzillaRepository {    

    private static final String NB_BUGZILLA_HOST = "netbeans.org";           // NOI18N
    public static final String NB_BUGZILLA_URL = "https://" + NB_BUGZILLA_HOST + "/bugzilla";           // NOI18N
    public static final String URL_NB_ORG_SIGNUP = "https://" + NB_BUGZILLA_HOST + "/people/new";       // NOI18N

    private static NBRepositorySupport instance;
    private BugzillaRepository bugzillaRepository;
    private Repository nbRepository;
    private boolean isTeam;

    private NBRepositorySupport() {}
    
    public static synchronized NBRepositorySupport getInstance() {
        if(instance == null) {
            instance = new NBRepositorySupport();
        }
        return instance;
    }
    
    /**
     * Goes through all known bugzilla repositories and returns either the first 
     * which is registered for a netbeans.org url or creates a new with the netbeans
     * bugzilla url and registeres it under services/issue tracking. 
     * In case there is a non kenai and a kenai repository available, the non kenai will
     * be returned. User credentials will be reused in case that netbeans.org was
     * already accessed via exception reporter in the past and a new repository
     * was created.
     *
     * @return a BugzillaRepository
     */
    public Repository getNBRepository(boolean checkLogin) {
        Collection<Repository> repos;
        boolean registered = false;
        if(nbRepository != null) {
            // check if repository wasn't removed since the last time it was used
            if(!isTeam) {
                repos = RepositoryManager.getInstance().getRepositories(BugzillaConnector.ID);
                for (Repository repo : repos) {
                    if(NBBugzillaUtils.isNbRepository(repo.getUrl())) {
                        registered = true;
                        break;
                    }
                }
                if(!registered) {
                    nbRepository = null; // create a new one
                }
            }
            if(registered) {
                return nbRepository;
            }
        }
        repos = RepositoryManager.getInstance().getRepositories(BugzillaConnector.ID);
        for (Repository repo : repos) {
            if(NBBugzillaUtils.isNbRepository(repo.getUrl())) {
                nbRepository = repo;
                return repo;
            }
        }

        if(isNBTeamServerRegistered()) {
            isTeam = true;
            // there is a nb team server registered in the ide
            // create a new repo but _do not register_ in services
            nbRepository = createRepositoryIntern(checkLogin); // XXX for now we keep a repository for each
                                                     //     nb team project. there will be no need
                                                     //     to create a new instance as soon as we will
                                                     //     have only one repository instance for all
                                                     //     team projects. see also issue #177578
        }

        if(nbRepository == null) {                              // no nb repo yet ...
            nbRepository = createRepositoryIntern(checkLogin);            // ... create ...
        } 

        return nbRepository;
    }

    public void setNBBugzillaRepository(BugzillaRepository repo) {
        bugzillaRepository = repo;
    }

    public BugzillaRepository getNBBugzillaRepository(boolean checkLogin) {
        if(bugzillaRepository == null) {
            getNBRepository(checkLogin); // invoke creation
        }
        return bugzillaRepository;
    }
            
    private Repository createRepositoryIntern(boolean checkLogin) {
        char[] password = NBBugzillaUtils.getNBPassword();
        final String username = NBBugzillaUtils.getNBUsername();
        String name = NbBundle.getMessage(NBRepositorySupport.class, "LBL_NBRepository"); // NOI18N
        RepositoryInfo info = new RepositoryInfo(
                "NetbeansRepository" + System.currentTimeMillis(), // NOI18N
                BugzillaConnector.ID, 
                NB_BUGZILLA_URL, 
                NbBundle.getMessage(NBRepositorySupport.class, "LBL_NBRepository"), // NOI18N
                NbBundle.getMessage(NBRepositorySupport.class, "LBL_RepositoryTooltip", new Object[] {name, username, NB_BUGZILLA_URL}), // NOI18N
                username, 
                null, 
                password, 
                null);
        BugzillaRepository repo = new BugzillaRepository(info); 
        if(checkLogin && !checkLogin(repo)) {
            return null;
        }
        bugzillaRepository = repo;
        return BugzillaUtil.getRepository(bugzillaRepository);
    }
    
    private static boolean isNBTeamServerRegistered() {
        for (TeamAccessor ka : TeamAccessorUtils.getTeamAccessors()) {
            if (ka.isNBTeamServerRegistered()) {
                return true;
            }
        }
        return false;
    }    
    
    private static boolean checkLogin(final BugzillaRepository repo) {
        if(repo.getUsername() != null && !repo.getUsername().equals("")) { // NOI18N
            return true;
        }

        String errorMsg = NbBundle.getMessage(NBLoginPanel.class, "MSG_MISSING_USERNAME_PASSWORD");  // NOI18N
        while(NBLoginPanel.show(repo, errorMsg)) {

            ValidateCommand cmd = new ValidateCommand(repo.getTaskRepository());
            ProgressHandle handle = ProgressHandleFactory.createHandle(NbBundle.getMessage(NBLoginPanel.class, "MSG_CONNECTING_2_NBORG")); // NOI18N
            handle.start();
            try {
                repo.getExecutor().execute(cmd, false, false, false);
            } finally {
                handle.finish();
            }
            if(cmd.hasFailed()) {
                errorMsg = cmd.getErrorMessage();
                continue;
            }
            return true;
        }
        repo.setCredentials(null, null, null, null); // reset
        return false;
    }
}
