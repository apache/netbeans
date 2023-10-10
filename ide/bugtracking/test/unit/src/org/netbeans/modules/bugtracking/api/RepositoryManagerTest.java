/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.BugtrackingManager;
import org.netbeans.modules.bugtracking.DelegatingConnector;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import static org.netbeans.modules.bugtracking.api.APITestConnector.ID_CONNECTOR;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class RepositoryManagerTest extends NbTestCase {

    public RepositoryManagerTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        MockLookup.setLayersAndInstances();
        APITestConnector.init();
    }

    @Override
    protected void tearDown() throws Exception {   
    }

    public void testGetRepositories() {
        Collection<Repository> repos = RepositoryManager.getInstance().getRepositories();
        assertNotNull(repos);
        
        for (Repository repo : repos) {
            if(repo.getId().equals(APITestRepository.ID)) {
                return;
            }
        }
        fail("test repository not found");
    }
    
    public void testGetRepositoriesByConnector() {
        Collection<Repository> repos = RepositoryManager.getInstance().getRepositories(APITestConnector.ID_CONNECTOR);
        assertNotNull(repos);
        
        for (Repository repo : repos) {
            if(repo.getId().equals(APITestRepository.ID)) {
                return;
            }
        }
        fail("test repository not found");
    }
    
    public void testPCL() {
        final List<Repository> added = new LinkedList<Repository>();
        final List<Repository> removed = new LinkedList<Repository>();
        PropertyChangeListener l = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if(RepositoryManager.EVENT_REPOSITORIES_CHANGED.equals(pce.getPropertyName())) {
                    Collection<Repository> oldRepos = (Collection<Repository>) pce.getOldValue();
                    Collection<Repository> newRepos = (Collection<Repository>) pce.getNewValue();
                    
                    if(newRepos != null) {
                        for (Repository nr : newRepos) {
                            boolean found = false;
                            if(oldRepos != null) {
                                for (Repository or : oldRepos) {
                                    if(or.getId().equals(nr.getId())) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if(!found) {
                                added.add(nr);
                            }
                        }
                    }
                    if(oldRepos != null) {
                        for (Repository or : oldRepos) {
                            boolean found = false;
                            if(newRepos != null) {
                                for (Repository nr : newRepos) {
                                    if(or.getId().equals(nr.getId())) {
                                        found = true;
                                        break;
                                    }
                                }
                            }
                            if(!found) {
                                removed.add(or);
                            }
                        }
                    }
                }
            }
        };
        assertTrue(added.isEmpty());
        RepositoryManager.getInstance().addPropertChangeListener(l);
        addAnotherRepository();
        assertEquals(1, added.size());
        
        assertTrue(removed.isEmpty());
        RepositoryRegistry.getInstance().removeRepository(added.iterator().next().getImpl());
        assertEquals(1, removed.size());
        
        RepositoryManager.getInstance().removePropertChangeListener(l);
        added.clear();
        addAnotherRepository();
        assertTrue(added.isEmpty());
    }
    
    public static void addAnotherRepository() {
        DelegatingConnector[] cons = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector dc : cons) {
            if(ID_CONNECTOR.equals(dc.getID())) {
                // init repos
                RepositoryRegistry.getInstance().addRepository(dc.createRepository(getInfo()).getImpl());
            }
        }
    }
    
    private static RepositoryInfo getInfo() {
        return new RepositoryInfo(
            APITestRepository.ID, 
            "AnotherTestRepository", 
            "http://anothertestrepo/url", 
            "AnotherTestRepository Name", 
            "AnotherTestRepository Tooltip");
    }
}
