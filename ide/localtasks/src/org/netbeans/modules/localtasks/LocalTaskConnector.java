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

package org.netbeans.modules.localtasks;

import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;

/**
 *
 * @author Ondrej Vrabec
 */
@BugtrackingConnector.Registration(
        displayName = "#LBL_ConnectorName",
        id = LocalTaskConnector.CONNECTOR_NAME,
        tooltip = "#LBL_ConnectorTooltip",
        providesRepositoryManagement = false
)
public class LocalTaskConnector implements BugtrackingConnector {
    public static final String CONNECTOR_NAME = "NB_LOCAL_TASKS";

    @Override
    public Repository createRepository (RepositoryInfo info) {
        return createRepository();
    }

    @Override
    public Repository createRepository () {
        return LocalRepository.getInstance().getRepository();
    }
}
