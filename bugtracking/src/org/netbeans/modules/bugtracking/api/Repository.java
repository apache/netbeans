/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.bugtracking.api;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.spi.RepositoryProvider;

/**
 * Represents a bugtracking Repository.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class Repository {
    
    /**
     * A query from this repository was saved or removed.
     * @since 1.85
     */
    public final static String EVENT_QUERY_LIST_CHANGED = RepositoryProvider.EVENT_QUERY_LIST_CHANGED;

    /**
     * RepositoryProvider's attributes have changed, e.g. name, url, etc.
     * @since 1.85
     */
    public final static String EVENT_ATTRIBUTES_CHANGED = RepositoryImpl.EVENT_ATTRIBUTES_CHANGED;

    private final static Logger LOG = Logger.getLogger("org.netbeans.modules.bugtracking.Repository"); // NOI18N
    
    static {
        APIAccessorImpl.createAccesor();
    }
    
    private final RepositoryImpl<?, ?, ?> impl;

    <R, Q, I> Repository(RepositoryImpl<R, Q, I> impl) {
        this.impl = impl;
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, "created repository {0} - repository: {1} - impl: {2}", new Object[]{getDisplayName(), this, impl}); // NOI18N
        }
    }

    /**
     * Returns the icon for this repository.
     * 
     * @return the icon
     * @since 1.85
     */
    public Image getIcon() {
        return impl.getIcon();
    }

    /**
     * Returns the display name for this repository.
     * 
     * @return the display name
     * @since 1.85
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }

    /**
     * Returns the tooltip describing this repository.
     * 
     * @return the tooltip
     * @since 1.85
     */
    public String getTooltip() {
        return impl.getTooltip();
    }

    /**
     * Returns a unique id associated with this repository.
     * 
     * @return id
     * @since 1.85
     */
    public String getId() {
        return impl.getId();
    }

    /**
     * Returns the remote url of this repository.
     * 
     * @return url
     * @since 1.85
     */
    public String getUrl() {
        return impl.getUrl();
    }

    /**
     * Returns a list of all saved queries for this repository.
     * 
     * <p>
     * Please <b>note</b> that in some cases this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @return queries
     * @since 1.85
     */
    public Collection<Query> getQueries() {
        Collection<QueryImpl> c = impl.getQueries();
        Collection<Query> ret = new ArrayList<Query>();
        for (QueryImpl q : c) {
            ret.add(q.getQuery());
        }
        return ret;
    }

    /**
     * Determines if this repository can be deleted or changed by the user.
     * 
     * @return <code>true</code> if this repository can be deleted or changed by 
     *         the user. Otherwise <code>false</code>.
     * @since 1.85
     */
    public boolean isMutable() {
        return impl.isMutable();
    }
    
    /**
     * Determines whether it is possible to attach files to Issues from this repository.
     * 
     * @return <code>true</code> in case it is possible to attach files, otherwise <code>false</code>
     * @since 1.85
     */
    public boolean canAttachFiles() {
        return impl.canAttachFiles();
    }
    
    /**
     * Removes this repository.
     * 
     * @since 1.85
     */
    public void remove() {
        impl.remove();
    }
    
    /**
     * Registers a PropertyChangeListener.
     * 
     * @param listener 
     * @since 1.85
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        impl.addPropertyChangeListener(listener);
    }
    
    /**
     * Unregisters a PropertyChangeListener. 
     * 
     * @param listener 
     * @since 1.85
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        impl.removePropertyChangeListener(listener);
    }

    /**
     * Returns the issues with the given id-s or an empty array in case none found.
     * 
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @param ids
     * @return issues
     * @since 1.85
     */
    public Issue[] getIssues(String... ids) {
        Collection<IssueImpl> impls = impl.getIssueImpls(ids);
        List<Issue> ret = new ArrayList<Issue>(impls.size());
        for (IssueImpl issueImpl : impls) {
            ret.add(issueImpl.getIssue());
        }
        return ret.toArray(new Issue[ret.size()]);
    }
    
    <R, Q, I> RepositoryImpl<R, Q, I> getImpl() {
        return (RepositoryImpl<R, Q, I>) impl;
    }
}
