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
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;

/**
 * Ability to find class usages.
 * @since 1.17
 */
public interface ClassUsageQuery {

    
    /**
     * Finds all usages of a given class.
     * @param className the (binary) FQN of a class that might be used as an API
     * @param repos repositories of this type to search in
     * @return result object with a list of usages
     */
    ResultImplementation<RepositoryQueries.ClassUsage> findClassUsages(String className, List<RepositoryInfo> repos);

}
