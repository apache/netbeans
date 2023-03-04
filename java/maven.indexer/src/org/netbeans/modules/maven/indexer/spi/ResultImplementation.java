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
package org.netbeans.modules.maven.indexer.spi;

import java.util.List;

/**
 * Implement to provide a maven index query result.
 * 
 * @author Tomas Stupka
 * @param <T>
 * @since 2.38
 * @see org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result
 */
public interface ResultImplementation<T> {
    /**
     * Returns true is one or more indexes were skipped, e.g. because the indexing was taking place.
     * 
     * @return <code>true</code> if the result is partial, otherwise <code>false</code>
     * @since 2.38
     */
    public boolean isPartial();
    
    /**
     * Waits for currently unaccessible indexes to finish, not to be called in AWT thread.
     * 
     * @since 2.38
     */
    public void waitForSkipped();
    
    /**
     * Returns the results.
     * 
     * @return a list of results
     * @since 2.38
     */
    public List<T> getResults();
    
    /**
     * Total number of hits.
     * 
     * @return the total number of hits
     * @since 2.38
     */
    public int getTotalResultCount();
    
    /**
     * In some cases not entirely accurate number of processed and returned hits, 
     * typically should be less or equal to {@link #getReturnedResultCount()}.
     * 
     * @return the returned result count
     * @since 2.38
     */
    public int getReturnedResultCount();
}
