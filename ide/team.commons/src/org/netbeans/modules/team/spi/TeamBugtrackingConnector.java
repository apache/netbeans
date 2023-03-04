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
package org.netbeans.modules.team.spi;

/**
 * Provides Team specific functionality to a {@link BugtrackingConnector}
 * 
 * @author tomas
 */
public interface TeamBugtrackingConnector {
    public static String TEAM_PROJECT_NAME = "team.project.name"; // NOI18N
    
    public enum BugtrackingType {
        BUGZILLA,
        JIRA,
        ODCS
    }
        
    /**
     * Creates a {@link RepositoryProvider} for the given {@link }
     *
     * @return
     */
    public String findNBRepository();
    
    /**
     * Determines the bugtracking type
     *
     * @return
     */
    public abstract BugtrackingType getType();
}
