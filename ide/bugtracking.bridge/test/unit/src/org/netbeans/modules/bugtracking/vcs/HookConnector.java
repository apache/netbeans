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
package org.netbeans.modules.bugtracking.vcs;

import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.Lookup;

/**
 *
 * @author tomas
 */
@BugtrackingConnector.Registration (
    id=HookConnector.ID,
    displayName=HookConnector.ID,
    tooltip=HookConnector.ID
)    
public class HookConnector implements BugtrackingConnector {
    public static final String ID = "HookTestConnector";
    
    private static HookConnector instance;
    public HookConnector() {
    }

    @Override
    public Repository createRepository() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public Repository createRepository(RepositoryInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    static HookConnector getInstance() {
        if(instance == null) {
            DelegatingConnector[] conns = BugtrackingManager.getInstance().getConnectors();
            for (DelegatingConnector dc : conns) {
                if(HookConnector.ID.equals(dc.getID())) {
                    instance = (HookConnector) dc.getDelegate();
                }
            }
        }
        return instance;
    }
}
