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
package org.netbeans.modules.bugtracking;

import org.netbeans.modules.bugtracking.api.Issue;
import org.netbeans.modules.bugtracking.api.Query;
import org.netbeans.modules.bugtracking.api.Repository;

/**
 *
 * @author Tomas Stupka
 */
public abstract class APIAccessor {
    
    public static APIAccessor IMPL;
    
    static {
        // invokes static initializer of Repository.class
        // that will assign value to the IMPL field above
        Class c = Repository.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }  

    /**
     * WARNING! To be called only from RepositoryImpl
     * 
     * @param impl
     * @return 
     */
    public abstract Repository createRepository(RepositoryImpl impl);
    
    public abstract Query createQuery(QueryImpl impl);
    
    public abstract Issue createIssue(IssueImpl impl);
    
    public abstract RepositoryImpl getImpl(Repository repository);
    
    public abstract QueryImpl getImpl(Query query);
    
    public abstract IssueImpl getImpl(Issue issue);
}
