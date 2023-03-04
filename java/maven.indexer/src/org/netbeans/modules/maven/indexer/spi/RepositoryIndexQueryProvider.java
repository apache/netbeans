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

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;

/**
 * Provides repository queries.<br> 
 * Register via <code>@ServiceProvider(service=RepositoryIndexQueryProvider.class)</code>
 * 
 * @author Tomas Stupka
 * @since 2.38
 */
public interface RepositoryIndexQueryProvider {
    
    /** 
     * Determines if queries for the given repository are handled by this provider or not.
     * 
     * @param repo the repository
     * @return <code>true</code> if the given repository is handled by this provider, otherwise <code>false</code>
     * @since 2.38
     */
    boolean handlesRepository(RepositoryInfo repo);
    
    /**
     * Returns a {@link ArchetypeQueries} implementation.
     * 
     * @return ArchetypeQueries or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ArchetypeQueries getArchetypeQueries();
    
    /**
     * Returns a {@link BaseQueries} implementation.
     * 
     * @return BaseQueries
     * @since 2.38
     */
    @NonNull BaseQueries getBaseQueries();
    
    /**
     * Returns a {@link ChecksumQueries} implementation.
     * 
     * @return ChecksumQueries or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ChecksumQueries getChecksumQueries();
    
    /**
     * Returns a {@link ClassUsageQuery} implementation.
     * 
     * @return ClassUsageQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ClassUsageQuery getClassUsageQuery();
    
    /**
     * Returns a {@link ClassesQuery} implementation.
     * 
     * @return ClassesQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ClassesQuery getClassesQuery();
    
    /**
     * Returns a {@link ContextLoadedQuery} implementation.
     * 
     * @return ContextLoadedQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull ContextLoadedQuery getContextLoadedQuery();
    
    /**
     * Returns a {@link DependencyInfoQueries} implementation.
     * 
     * @return DependencyInfoQueries or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull DependencyInfoQueries getDependencyInfoQueries();
    
    /**
     * Returns a {@link GenericFindQuery} implementation.
     * 
     * @return GenericFindQuery or <code>null</code> if not provided
     * @since 2.38
     */
    @CheckForNull GenericFindQuery getGenericFindQuery();
}
