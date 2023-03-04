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
package org.netbeans.modules.bugtracking.api;

import java.util.Collection;

/**
 *
 * @author tomas
 */
public class APITestKit {
    
    public static APITestRepository getAPIRepo(String id) {
        return APITestConnector.getAPIRepo(id);

    }
    public static Repository getRepo(String id) {
        Collection<Repository> repos = RepositoryManager.getInstance().getRepositories(APITestConnector.ID_CONNECTOR);
        for (Repository repo : repos) {
            if(repo.getId().equals(id)) {
                return repo;
            }
        }
        return null;
    }

}
