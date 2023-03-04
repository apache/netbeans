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
    private final List<NBVersionInfo> results = new ArrayList<>();

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
