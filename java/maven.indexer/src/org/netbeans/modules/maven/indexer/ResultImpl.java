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
package org.netbeans.modules.maven.indexer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.netbeans.modules.maven.indexer.spi.impl.Redo;
import org.netbeans.modules.project.spi.intern.ProjectIDEServices;

/**
 *
 * @author Tomas Stupka
 */
public class ResultImpl<T> implements ResultImplementation<T> {
    private final List<RepositoryInfo> skipped = new ArrayList<>();
    private final List<T> results = new ArrayList<>();
    private final Redo<T> redoAction;

    private int totalResults = 0;
    private int returnedResults = 0;

    /**
     * used internally by the repository indexing/searching engine(s)
     */
    public ResultImpl(Redo<T> redo) {
        redoAction = redo;
    }

    /**
     * returns true is one or more indexes were skipped, eg because the indexing was taking place.
     * @return 
     */
    @Override
    public synchronized boolean isPartial() {
        return !skipped.isEmpty();
    }

    /**
     * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
     */
    synchronized void addSkipped(RepositoryInfo info) {
        skipped.add(info);
    }

    /**
     * waits for currently unaccessible indexes to finish, not to be called in AWT thread.
     */
    @Override
    public void waitForSkipped() {
        assert !ProjectIDEServices.isEventDispatchThread();
        redoAction.run(this);
        synchronized (this) {
            skipped.clear();
        }
    }

    synchronized void setResults(Collection<T> newResults) {
        results.clear();
        results.addAll(newResults);
    }

    @Override
    public synchronized List<T> getResults() {
        return Collections.unmodifiableList(results);
    }


    /**
     * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
     */
    synchronized void addSkipped(Collection<RepositoryInfo> infos) {
        skipped.addAll(infos);
    }

    /**
     * used internally by the repository indexing/searching engine(s) to mark the result as partially skipped
     */
    synchronized List<RepositoryInfo> getSkipped() {
        return Collections.unmodifiableList(skipped);
    }
    
    /**
     * total number of hits
     * @return
     * @since 2.20
     */
    @Override
    public int getTotalResultCount() {
        return totalResults;
    }

    void addTotalResultCount(int moreTotalResults) {
        totalResults += moreTotalResults;
    }
    /**
     * in some cases not entirely accurate number of processed and returned hits, typically should be less or equals to totalResultCount
     * @return 
     * @since 2.20
     */
    @Override
    public int getReturnedResultCount() {
        return returnedResults;
    }

    void addReturnedResultCount(int moreReturnedResults) {
        returnedResults = moreReturnedResults + returnedResults;
    }
    
}
