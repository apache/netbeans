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

package org.netbeans.modules.maven.indexer.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.netbeans.api.annotations.common.NonNull;

/**
 * Observable query request to be passed to
 * <code>RepositoryQueries.find(queryRequest)</code>.
 *
 * @author anebuzelsky
 */
public final class QueryRequest extends Observable {
    
    private List<QueryField> queryFields;
    private String className;
    private List<RepositoryInfo> repositories;
    private boolean queryFinished;
    private final List<NBVersionInfo> results = new ArrayList<NBVersionInfo>();

    /**
     * Constructor of a new query request. The request is created by a requester
     * who gets attached here as the observer. The observer gets notified when
     * the running query adds more results to this object. The requester should
     * remove itself from the list of observers of this object when it loses
     * interest in additional results of the query.
     * 
     * @param fields
     * @param repos
     * @param observer
     */
    public QueryRequest(List<QueryField> fields, @NonNull List<RepositoryInfo> repos, Observer observer) {
        queryFields = fields;
        repositories = repos;
        addObserver(observer);
        queryFinished = false;
    }
    
    /**
     * Constructor of a new query request. The request is created by a requester
     * who gets attached here as the observer. The observer gets notified when
     * the running query adds more results to this object. The requester should
     * remove itself from the list of observers of this object when it loses
     * interest in additional results of the query.
     * 
     * @param classname
     * @param repos
     * @param observer
     */
    public QueryRequest(String classname, @NonNull List<RepositoryInfo> repos, Observer observer) {
        className = classname;
        repositories = repos;
        addObserver(observer);
        queryFinished = false;
    }
    
    /**
     * Change the search parameters of this object, to be able to reuse it for
     * the query restart.
     * 
     * @param newFields
     */
    public void changeFields(List<QueryField> newFields) {
        synchronized (results) {
            queryFields = newFields;
            queryFinished = false;
            results.clear();
        }
    }
    
    /**
     * Is the query finished or should more results be expected?
     * 
     * @return
     */
    public boolean isFinished() {
        synchronized (results) {
            return queryFinished;
        }
    }
    
    /**
     * Returns current set of available results.
     * 
     * @return
     */
    public List<NBVersionInfo> getResults() {
        synchronized (results) {
            return results;
        }
    }
    
    /**
     * Called by the query to add more results to the object and to notify
     * observers.
     * 
     * @param newResults
     * @param queryFinished
     */
    public void addResults(List<NBVersionInfo> newResults, boolean queryFinished) {
        synchronized (results) {
            if (null!=newResults) results.addAll(newResults);
            if (queryFinished) {
                this.queryFinished = true;
                Collections.sort(results);
            }
            setChanged();
            notifyObservers();
        }
    }

    public List<QueryField> getQueryFields() {
        assert queryFields != null;
        return queryFields;
    }
    
    public String getClassName() {
        assert className != null;
        return className;
    }
    
    public @NonNull List<RepositoryInfo> getRepositories() {
        return Collections.unmodifiableList(repositories);
    }
}
