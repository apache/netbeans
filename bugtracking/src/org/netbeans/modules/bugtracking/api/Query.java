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

import java.beans.PropertyChangeListener;
import java.util.Collection;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.spi.QueryProvider;

/**
 * Represents a bugtracking Query.
 * 
 * @author Tomas Stupka
 * @since 1.85
 */
public final class Query {
    
    /**
     * Fired after the Query was refreshed. 
     * @since 1.85
     */
    public final static String EVENT_QUERY_REFRESHED = "bugtracking.query.finished"; // NOI18N
    
    private final QueryImpl impl;

    /**
     * C'tor
     * @param impl 
     */
    Query(QueryImpl impl) {
        this.impl = impl;
    }

    /**
     * Returns the tooltip text describing this Query.
     * 
     * @return the tooltip
     * @since 1.85
     */
    public String getTooltip() {
        return impl.getTooltip();
    }

    /**
     * Returns the display name of this Query. 
     * 
     * @return display name
     * @since 1.85
     */
    public String getDisplayName() {
        return impl.getDisplayName();
    }
    
    /**
     * The Issues returned by this Query.
     * @return issues from this query
     * @since 1.85
     */
    public Collection<Issue> getIssues() {
        return Util.toIssues(impl.getIssues());
    }

    /**
     * Refreshes this query.
     * 
     * <p>
     * Please <b>note</b> that this method might block for a longer time. Do not 
     * execute in AWT. 
     * <p>
     * 
     * @since 1.85
     */
    public void refresh() {
        impl.refresh();
    }

    /**
     * Returns the Repository this Query belongs to.
     * 
     * @return repository
     * @since 1.85
     */
    public Repository getRepository() {
        return impl.getRepositoryImpl().getRepository();
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
    
    QueryImpl getImpl() {
        return impl;
    }
    
}
