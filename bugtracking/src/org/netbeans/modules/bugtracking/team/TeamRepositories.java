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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.team;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import org.netbeans.api.queries.VersioningQuery;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.api.RepositoryManager;
import org.netbeans.modules.team.ide.spi.ProjectServices;
import org.netbeans.modules.team.spi.TeamAccessor;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector;
import org.netbeans.modules.team.spi.TeamBugtrackingConnector.BugtrackingType;
import org.netbeans.modules.team.spi.TeamProject;
import org.netbeans.modules.team.spi.OwnerInfo;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.team.spi.TeamAccessorUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Stupka
 * @author Marian Petras
 */
public abstract class TeamRepositories implements PropertyChangeListener {

    private static TeamRepositories instance;

    private final Map<String, Object> teamLocks = new HashMap<String, Object>(1);
    private final Map<RepositoryImpl, TeamProject> repoToTeam = new WeakHashMap<RepositoryImpl, TeamProject>(5);

    /**
     * Holds already created team repositories
     */
    private final Map<String, RepositoryImpl> repositoriesCache = Collections.synchronizedMap(new HashMap<String, RepositoryImpl>());

    protected TeamRepositories() { }

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    
    public synchronized static TeamRepositories getInstance() {
        if(instance == null) {
            instance = Lookup.getDefault().lookup(TeamRepositories.class);
            if (instance == null) {
                instance = new DefaultImpl();
            }
        }
        return instance;
    }

    //--------------------------------------------------------------------------

    public synchronized void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
        TeamAccessor[] teamAccessors = TeamAccessorUtils.getTeamAccessors();
        for (TeamAccessor teamAccessor : teamAccessors) {
            teamAccessor.addPropertyChangeListener(this);
        }        
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
        TeamAccessor[] teamAccessors = TeamAccessorUtils.getTeamAccessors();
        for (TeamAccessor teamAccessor : teamAccessors) {
            teamAccessor.removePropertyChangeListener(this);
        }        
    }

    private void fireProjectsChanged(Collection<RepositoryImpl> removed, Collection<RepositoryImpl> added) {
        support.firePropertyChange(RepositoryManager.EVENT_REPOSITORIES_CHANGED, removed, added);
    }

    public RepositoryImpl getRepository(String url, String projectName) {
        TeamProject p = TeamAccessorUtils.getTeamProject(url, projectName);
        if(p == null) {
            return null;
        }
        return getRepository(p);
    }

    /**
     * Returns a {@link Repository} representing the given {@link TeamProject}
     *
     * @param kp
     * @return
     */
    public RepositoryImpl getRepository(TeamProject kp) {
        return getRepository(kp, true);
    }

    /**
     * Returns a {@link Repository} representing the given {@link TeamProject}.
     *
     * @param kp TeamProject
     * @param forceCreate determines if a Repository instance should be created if it doesn't already exist
     * @return
     */
    public RepositoryImpl getRepository(TeamProject kp, boolean forceCreate) {

        String repositoryKey = kp.getWebLocation().toString();
        BugtrackingManager.LOG.log(Level.FINER, "requesting repository for {0}", repositoryKey);  // NOI18N

        Object lock = getTeamLock(kp);
        synchronized(lock) { // synchronize for a team instance and bugtracking type
            RepositoryImpl repository = repositoriesCache.get(repositoryKey);
            if(repository == null && forceCreate) {
                repository = createRepository(kp);
                if(repository != null) {
                    // XXX what if more repos?!
                    repositoriesCache.put(repositoryKey, repository);
                } else {
                    BugtrackingManager.LOG.log(Level.FINER, "no repository available for {0}", repositoryKey);  // NOI18N
                    return null;
                }
            }
            BugtrackingManager.LOG.log(
                    Level.FINER,
                    "returning repository {0}:{1} for {2}", // NOI18N
                    new Object[]{repository != null ? repository.getDisplayName() : "null", repository != null ? repository.getUrl() : "", repositoryKey});  // NOI18N
            return repository;
        }
    }
    
    public TeamProject getTeamProject(RepositoryImpl repoImpl) {
        return repoToTeam.get(repoImpl);
    }
    
    /**
     * Creates a {@link Repository} for the given {@link TeamProject}
     *
     * @param project
     * @return
     */
    private RepositoryImpl createRepository(TeamProject project) {
        BugtrackingConnector[] connectors = BugtrackingUtil.getBugtrackingConnectors();
        for (BugtrackingConnector c : connectors) {
            if (isType(c, project.getType())) {
                BugtrackingManager.LOG.log(Level.FINER, "found suport for {0}", project.getWebLocation().toString()); // NOI18N
                RepositoryInfo info = new RepositoryInfo(project.getName(), null, project.getHost(), project.getDisplayName(), project.getDisplayName());
                info.putValue(TeamBugtrackingConnector.TEAM_PROJECT_NAME, project.getName());
                Repository repo = (c).createRepository(info);
                if(repo != null) { 
                    RepositoryImpl repoImpl = APIAccessor.IMPL.getImpl(repo);
                    repoToTeam.put(repoImpl, project);
                    return repoImpl;
                }
            }
        }
        return null;
    }    


    private static boolean isSupported(TeamProject project) {
        BugtrackingConnector[] connectors = BugtrackingUtil.getBugtrackingConnectors();
        for (BugtrackingConnector c : connectors) {
            if (isType(c, project.getType())) {
                BugtrackingManager.LOG.log(Level.FINER, "found suport for {0}", project.getWebLocation().toString()); // NOI18N
                return true;
            }
        }
        return false;
    }
    
    private static boolean isType(BugtrackingConnector connector, BugtrackingType type) {
        return connector instanceof TeamBugtrackingConnector && ((TeamBugtrackingConnector) connector).getType() == type;
    }
    
    private Object getTeamLock(TeamProject kp) {
        BugtrackingType type = kp.getType();
        synchronized(teamLocks) {
            final String key = kp.getWebLocation().getHost() + ":" + type;  // NOI18N
            BugtrackingManager.LOG.log(Level.FINER, "requesting lock for {0}", key); // NOI18N
            Object lock = teamLocks.get(key);
            if(lock == null) {
                lock = new Object();
                teamLocks.put(key, lock);
            }
            BugtrackingManager.LOG.log(Level.FINER, "returning lock {0} for {1}", new Object[]{lock, key}); // NOI18N
            return lock;
        }
    }   

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if(evt.getPropertyName().equals(TeamAccessor.PROP_PROJETCS_CHANGED) || 
           evt.getPropertyName().equals(TeamAccessor.PROP_LOGIN)) 
        {
            fireProjectsChanged(null, null);
        }
    }

    /**
     * Returns bugtracking repositories of all Team projects.
     *
     * @param  includeIDEProjects  if {@code false}, search only Team projects
     *                          that are currently open in the Team dashboard;
     *                          if {@code true}, search also all Team projects
     *                          currently opened in the IDE
     * @param onlyDashboardOpenProjects if {@code true}, return only projects from dashboard which
     *                                  are opened, otherwise return all projects from dashboard - 
     *                                  opened and member projects
     * @return  array of repositories collected from the projects
     *          (never {@code null})
     */    
    public abstract Collection<RepositoryImpl> getRepositories(boolean includeIDEProjects, boolean onlyDashboardOpenProjects);

    //--------------------------------------------------------------------------

    /**
     * The default implementation of {@code TeamRepositories}.
     * This implementation is used if no other implementation is found
     * in the default lookup.
     */
    private static class DefaultImpl extends TeamRepositories {
        
        @Override
        public Collection<RepositoryImpl> getRepositories(boolean includeIDEProjects, boolean onlyDashboardOpenProjects) {
            if("true".equals(System.getProperty("netbeans.bugtracking.noOpenProjects", "false"))) {
                includeIDEProjects = false; 
            }
            TeamProject[] teamProjects = includeIDEProjects
                                           ? union(getDashboardProjects(onlyDashboardOpenProjects),
                                                   getProjectsViewProjects())
                                           : getDashboardProjects(onlyDashboardOpenProjects);

            List<RepositoryImpl> result = new ArrayList<RepositoryImpl>(teamProjects.length);

            EnumSet<BugtrackingType> reluctantSupports = EnumSet.noneOf(BugtrackingType.class);
            for (TeamProject kp : teamProjects) {
                if(onlyDashboardOpenProjects && !TeamAccessorUtils.isLoggedIn(kp.getWebLocation())) {
                    continue;
                }
                if(kp.getType() == null) {
                    // no bugtracking feature
                    continue;
                }
                if(!reluctantSupports.contains(kp.getType())) {
                    RepositoryImpl repo = getRepository(kp);
                    if (repo != null) {
                        result.add(repo);
                    } else {
                        if(isSupported(kp) && kp.getFeatureLocation() != null) {
                            BugtrackingManager.LOG.log(
                                    Level.WARNING,
                                    "could not get repository for project {0} with {1} bugtracking type ",
                                    new Object[]{kp.getWebLocation(), kp.getType()});
                            // there is a support available for the projects bugtracking type, yet
                            // we weren't able to create a repository for the project.
                            // lets assume there is something with the bugracker or that the user canceled
                            // the authorisation (see also issue #182946) and skip all other projects with the same
                            // support in this one call.
                            reluctantSupports.add(kp.getType());
                        }
                    }
                } else {
                    BugtrackingManager.LOG.log(
                                    Level.WARNING,
                                    "skipping getRepository for project {0} with {1} bugtracking type ",
                                    new Object[]{kp.getWebLocation(), kp.getType()});
                }
            }
            return result;
        }

        private TeamProject[] getDashboardProjects(boolean onlyOpenProjects) {
            return TeamAccessorUtils.getDashboardProjects(onlyOpenProjects);
        }

        private TeamProject[] getProjectsViewProjects() {
            ProjectServices projectServices = BugtrackingManager.getInstance().getProjectServices();
            FileObject[] openProjectFiles = projectServices != null ? projectServices.getOpenProjectsDirectories() : null;
            
            if (openProjectFiles == null || openProjectFiles.length == 0) {
                return new TeamProject[0];
            }

            int count = 0;
            TeamProject[] teamProjects = new TeamProject[openProjectFiles.length];
            for (FileObject rootDir : openProjectFiles) {
                TeamProject teamProject = getTeamProject(rootDir);
                if (teamProject != null) {
                    teamProjects[count++] = teamProject;
                }
            }

            return stripTrailingNulls(teamProjects);
        }

        private static TeamProject getTeamProject(FileObject rootDir) {
            String url = null;
            try {
                url = VersioningQuery.getRemoteLocation(rootDir.toURL().toURI());
            } catch (URISyntaxException ex) {
                BugtrackingManager.LOG.log(Level.WARNING, rootDir.getPath(), ex);
            }
            if (url == null) {
                return null;
            }

            TeamProject teamProject = null;
            try {
                if(NBBugzillaUtils.isNbRepository(url)) {
                    OwnerInfo owner = TeamAccessorUtils.getOwnerInfo(FileUtil.toFile(rootDir));
                    if(owner != null) {
                        teamProject = TeamAccessorUtils.getTeamProject(url, owner.getOwner());
                    } else {
                        // might be deactivated
                        BugtrackingManager.LOG.fine("team accessor not available");
                    }
                } else {
                    teamProject = TeamAccessorUtils.getTeamProjectForRepository(url);
                }

            } catch (IOException ex) {
                teamProject = null;
                BugtrackingManager.LOG.log(Level.WARNING,
                        "No Team project is available for bugtracking repository " //NOI18N
                        + " [" + url + "]"); //NOI18N
                BugtrackingManager.LOG.log(Level.FINE, null, ex);
            }
            return teamProject;
        }

        private static TeamProject[] union(TeamProject[]... projectArrays) {
            Map<String, TeamProject> union = new HashMap<String, TeamProject>();
            for (TeamProject[] projectArray : projectArrays) {
                for (TeamProject p : projectArray) {
                    String name = p.getName();
                    if (!union.keySet().contains(name)) {
                        union.put(name, p);
                    }
                }
            }
            return union.values().toArray(new TeamProject[union.values().size()]);
        }

        private static <T> T[] stripTrailingNulls(T[] array) {

            /* count trailing nulls -> compute size of the resulting array */
            int resultSize = array.length;
            while ((resultSize > 0) && (array[resultSize - 1] == null)) {
                resultSize--;
            }

            if (resultSize == array.length) {       //no trailing nulls
                return array;
            }

            T[] result = (T[]) java.lang.reflect.Array.newInstance(
                                                array.getClass().getComponentType(),
                                                resultSize);
            if (resultSize != 0) {
                System.arraycopy(array, 0, result, 0, resultSize);
            }
            return result;
        }

    }

}
