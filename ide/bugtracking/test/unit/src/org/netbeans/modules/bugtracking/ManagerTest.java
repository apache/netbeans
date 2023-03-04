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

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class ManagerTest extends NbTestCase {
    

    public ManagerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        MockLookup.setLayersAndInstances();
    }

    @Override
    protected void tearDown() throws Exception {        
    }

    public void testGetRepositories() {
        DelegatingConnector[] connectors = BugtrackingManager.getInstance().getConnectors();
        assertNotNull(connectors);
        assertTrue(connectors.length > 1);
        Set<String> repos = new HashSet<String>();
        for (DelegatingConnector c : connectors) {
            repos.add(c.getDisplayName());
        }
        assertTrue(repos.contains("ManagerTestConector"));
    }

    @BugtrackingConnector.Registration (
        id="ManagerTestConnector",
        displayName="ManagerTestConector",
        tooltip="ManagerTestConector"
    )    
    public static class MyConnector implements BugtrackingConnector {
        public MyConnector() {
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
    }

}
