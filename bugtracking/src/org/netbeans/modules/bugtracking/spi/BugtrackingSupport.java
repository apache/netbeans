/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.spi;

import java.awt.Image;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bugtracking.*;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.commons.NBBugzillaUtils;
import org.netbeans.modules.bugtracking.tasks.DashboardTopComponent;
import org.netbeans.modules.bugtracking.tasks.DashboardUtils;
import org.netbeans.modules.bugtracking.util.BugtrackingUtil;

/**
 * 
 * Collection of utility methods for bugtracking systems implementors. 
 * 
 * @author Tomas Stupka
 * 
 * @param <R> the implementation specific repository type
 * @param <Q> the implementation specific query type
 * @param <I> the implementation specific issue type
 * @since 1.85
 */
public final class BugtrackingSupport<R, Q, I> {
    private final RepositoryProvider<R, Q, I> repositoryProvider;
    private final QueryProvider<Q, I> queryProvider;
    private final IssueProvider<I> issueProvider;

    /**
     * Creates a new BugtrackingSupport preconfigured with mandatory provider implementations.
     * 
     * @param repositoryProvider a {@link RepositoryProvider} to access the implementation specific repository.<br/> 
     *                           Is mandatory and cannot be null.
     * @param queryProvider a {@link QueryProvider} to access queries from the given repository.<br/>
     *                      Is mandatory and cannot be null.
     * @param issueProvider an {@link IssueProvider} to access issues from the given repository.<br/>
     *                      Is mandatory and cannot be null.
     * @since 1.85
     */
    public BugtrackingSupport(RepositoryProvider<R, Q, I> repositoryProvider, QueryProvider<Q, I> queryProvider, IssueProvider<I> issueProvider) {
        this.repositoryProvider = repositoryProvider;
        this.queryProvider = queryProvider;
        this.issueProvider = issueProvider;
    }
    
    /**
     * Factory method to create a {@link Repository} instance configured with optional providers.
     * 
     * @param r a implementation specific repository instance
     * @param issueStatusProvider an {@link IssueStatusProvider} to provide status information 
     *                            of an implementation specific issue.<br/> 
     *                            Might be null.
     * @param issueSchedulingProvider an {@link IssueScheduleProvider} to provide scheduling information 
     *                                of an implementation specific issue.<br/> 
     *                                Might be null.
     * @param issuePriorityProvider an {@link IssuePriorityProvider} to provide priority information 
     *                              of an implementation specific issue.<br/> 
     *                              Might be null.
     * @param issueFinder an {@link IssueFinder} to find issue references in text..<br/> 
     *                    Might be null.
     * 
     * @return a {@link Repository} instance
     * @since 1.85
     */
    public Repository createRepository(R r,
            IssueStatusProvider<R, I> issueStatusProvider,
            IssueScheduleProvider<I> issueSchedulingProvider,
            IssuePriorityProvider<I> issuePriorityProvider,
            IssueFinder issueFinder)
    {
        RepositoryRegistry registry = RepositoryRegistry.getInstance();
        Repository repo = registry.isInitializing() ? null : getRepository(r);
        if(repo != null) {
            return repo;
        }
        RepositoryImpl<R, Q, I> impl = new RepositoryImpl<R, Q, I>(r, repositoryProvider, queryProvider, issueProvider, issueStatusProvider, issueSchedulingProvider, issuePriorityProvider, issueFinder);
        RepositoryInfo info = impl.getInfo();
        if(info != null) {            
            // might be we just automatically generated a nb repository,
            // in such a case it also has to be added to the registry 
            // as otherwise it happens only on manual repositoy creation
            // XXX this realy isn't the cleanest way how to do it, if possible - change 
            // currently there are 2 impls taking atvantage of this hack:
            // - hardcoded repository created for the netbeans bugzilla 
            // - hardcoded repository for the inhouse bugdb tracker
            if(!registry.isInitializing() && getRepositoryImpl(r, false) == null) {
                registry.addRepository(impl);
            }
        }
        return impl.getRepository();
    }
    
    /**
     * Opens in the editor area a TopComponent with the given Queries UI in edit mode.
     * 
     * @param r a implementation specific Repository instance
     * @param q a implementation specific Query instance
     * 
     * @see QueryController
     * @since 1.85
     */
    public void editQuery(R r, Q q) {
        QueryImpl query = getQueryImpl(r, q);
        if(query != null) {
            query.open(QueryController.QueryMode.EDIT);
        }
    }

    /**
     * Opens in the editor area a TopComponent with the given Issues UI in edit mode.
     * 
     * @param r a implementation specific Repository instance
     * @param i a implementation specific Issue instance
     * @since 1.85
     */
    public void openIssue(R r, I i) {
        IssueImpl issue = getIssueImpl(r, i);
        if (issue != null) {
            issue.open();
        }
    }
    
    /**
     * Opens a UI to select an Tasks Dashboard Category to add the given Issue into.
     * 
     * @param r a implementation specific Repository instance
     * @param i a implementation specific Issue instance
     * @since 1.85
     */
    public void addToCategory(final R r, final I i) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                IssueImpl issue = getIssueImpl(r, i);
                if (issue != null) {
                    DashboardTopComponent.findInstance().addTask(issue);
                }
            }
        });
    }
    
    /**
     * Determines default auto-refresh for a given query.
     * 
     * @param r a implementation specific Repository instance
     * @param q a implementation specific Query instance
     * @param autoRefresh determines the auto-refresh setting for a given query
     * @since 1.107
     */
    public void setQueryAutoRefresh(R r, Q q, boolean autoRefresh) {
        QueryImpl queryImpl = getQueryImpl(r, q);
        DashboardUtils.setQueryAutoRefresh(queryImpl, autoRefresh);
    }
    
    /**
     * Opens a UI to edit a repository. 
     * 
     * @param r a implementation specific Repository instance
     * @param errorMessage an message to be show in the repository edit dialog
     * @return <code>true</code> in case the repository was changed, otherwise <code>false</code>.
     * @since 1.85
     */
    public boolean editRepository(R r, String errorMessage) {
        RepositoryImpl impl = getRepositoryImpl(r, false);
        return impl != null ? BugtrackingUtil.editRepository(impl, errorMessage) : false;
    }

    /**
     * Priority icons used by default in the Tasks Dashboard sorted from the highest priority. 
     * <br/>
     * Use them in case you want your bugtracking plugin implementation to use 
     * the same icons as are used by default in the Tasks Dashboard, or provide a 
     * {@link IssuePriorityProvider} implementation via {@link #createRepository(java.lang.Object, org.netbeans.modules.bugtracking.spi.IssueStatusProvider, org.netbeans.modules.bugtracking.spi.IssueScheduleProvider, org.netbeans.modules.bugtracking.spi.IssuePriorityProvider, org.netbeans.modules.bugtracking.spi.IssueFinder) }
     * 
     * @return  priority icons
     * @since 1.85
     */
    public Image[] getPriorityIcons() {
        return IssuePrioritySupport.getIcons();
    }
    
    private QueryImpl getQueryImpl(R r, Q q) {
        RepositoryImpl<R, Q, I> impl = getRepositoryImpl(r, true);
        return impl != null ? impl.getQuery(q) : null;
    }
    
    private IssueImpl getIssueImpl(R r, I i) {
        RepositoryImpl<R, Q, I> impl = getRepositoryImpl(r, true);
        return impl != null ? impl.getIssue(i) : null;
    }

    private Repository getRepository(String connectorId, String repositoryId) {
        RepositoryImpl impl = getRepositoryImpl(connectorId, repositoryId, false);
        return impl != null ? impl.getRepository() : null;
    }    
    
    private RepositoryImpl getRepositoryImpl(R r, boolean allKnown) {
       RepositoryInfo info = repositoryProvider.getInfo(r);
       return info != null ? getRepositoryImpl(info.getConnectorId(), info.getID(), allKnown) : null;
    }
    
    private RepositoryImpl getRepositoryImpl(String connectorId, String repositoryId, boolean allKnown) {
        RepositoryRegistry registry = RepositoryRegistry.getInstance();
        return !registry.isInitializing() ? registry.getRepository(connectorId, repositoryId, allKnown) : null;
    }    
    
    private Repository getRepository(R r) {
        RepositoryInfo info = repositoryProvider.getInfo(r);
        if (info != null) {
            String repositoryId = info.getID();
            String connectorId = repositoryProvider.getInfo(r).getConnectorId();
            Repository repo = getRepository(connectorId, repositoryId);
            return repo;
        }
        return null;
    }

}
