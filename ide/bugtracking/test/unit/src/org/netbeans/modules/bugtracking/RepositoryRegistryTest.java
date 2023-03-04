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

import java.awt.Image;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.spi.*;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class RepositoryRegistryTest extends NbTestCase {
    

    public RepositoryRegistryTest(String arg0) {
        super(arg0);
    }

    @Override
    protected Level logLevel() {
        return Level.ALL;
    }   
    
    @Override
    protected void setUp() throws Exception {    
        MockLookup.setLayersAndInstances();
        RepositoryRegistry.getInstance().loadRepositories();
    }

    @Override
    protected void tearDown() throws Exception {   
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getRepositories();
        for (RepositoryImpl repository : repos) {
            RepositoryRegistry.getInstance().removeRepository(repository);
        }
        RepositoryRegistry.getInstance().flushRepositories();
    }

    public void testEmpty() {
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(0, repos.size());
        repos = RepositoryRegistry.getInstance().getRepositories("fake", true);
        assertEquals(0, repos.size());
    }
    
    public void testAddGetRemove() {
        RepositoryImpl repo = getRepository(new MyRepository("1"));

        // add
        RepositoryRegistry.getInstance().addRepository(repo);
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(1, repos.size());
        
        // remove
        RepositoryRegistry.getInstance().removeRepository(repo);
        repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(0, repos.size());
    }
    
    public void testWrongConnector() {
        RepositoryImpl repo = getRepository(new MyRepository("1"));

        // add
        RepositoryRegistry.getInstance().addRepository(repo);
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(1, repos.size());
    
        repos = RepositoryRegistry.getInstance().getRepositories("fake", true);
        assertEquals(0, repos.size());
    }
    
    public void testDifferentConnectors() {
        RepositoryImpl repo1c1 = getRepository(new MyRepository("r1", ID_CONNECTOR1));
        RepositoryImpl repo2c1 = getRepository(new MyRepository("r2", ID_CONNECTOR1));
        RepositoryImpl repo1c2 = getRepository(new MyRepository("r1", ID_CONNECTOR2));
        RepositoryImpl repo2c2 = getRepository(new MyRepository("r2", ID_CONNECTOR2));

        // add
        RepositoryRegistry.getInstance().addRepository(repo1c1);
        RepositoryRegistry.getInstance().addRepository(repo2c1);
        RepositoryRegistry.getInstance().addRepository(repo1c2);
        RepositoryRegistry.getInstance().addRepository(repo2c2);
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(4, repos.size());
        repos = RepositoryRegistry.getInstance().getRepositories(ID_CONNECTOR1, true);
        assertEquals(2, repos.size());
        assertTrue(repos.contains(repo1c1));
        assertTrue(repos.contains(repo2c1));
        
        // remove
        RepositoryRegistry.getInstance().removeRepository(repo1c1);
        repos = RepositoryRegistry.getInstance().getRepositories(ID_CONNECTOR1, true);
        assertEquals(1, repos.size());
        assertTrue(repos.contains(repo2c1));
        RepositoryRegistry.getInstance().removeRepository(repo2c1);
        repos = RepositoryRegistry.getInstance().getRepositories(ID_CONNECTOR1, true);
        assertEquals(0, repos.size());
        
        repos = RepositoryRegistry.getInstance().getRepositories();
        assertEquals(2, repos.size());
        assertTrue(repos.contains(repo1c2));
        assertTrue(repos.contains(repo2c2));
    }

    public void testListener() {
        RepositoryImpl repo1 = getRepository(new MyRepository("1"));
        RepositoryImpl repo2 = getRepository(new MyRepository("2"));
        class L implements PropertyChangeListener {
            private Collection<RepositoryImpl> addedRepos;
            private Collection<RepositoryImpl> removedRepos;
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                removedRepos = (Collection<RepositoryImpl>) evt.getOldValue();
                addedRepos = (Collection<RepositoryImpl>) evt.getNewValue();
            }
        };
        L l = new L();
        
        RepositoryRegistry.getInstance().addPropertyChangeListener(l);
        
        // add 1.
        RepositoryRegistry.getInstance().addRepository(repo1);
        assertNull(l.removedRepos);
        assertEquals(1, l.addedRepos.size());
        assertTrue(l.addedRepos.contains(repo1));
        
        // add 2.
        RepositoryRegistry.getInstance().addRepository(repo2);
        assertNull(l.removedRepos);
        assertEquals(1, l.addedRepos.size());
        assertFalse(l.addedRepos.contains(repo1));
        assertTrue(l.addedRepos.contains(repo2));
        
        // remove 1.
        RepositoryRegistry.getInstance().removeRepository(repo1);
        assertEquals(1, l.removedRepos.size());
        assertNull(l.addedRepos);
        assertTrue(l.removedRepos.contains(repo1));
        assertFalse(l.removedRepos.contains(repo2));
        
        // remove 1.
        RepositoryRegistry.getInstance().removeRepository(repo2);
        assertEquals(1, l.removedRepos.size());
        assertNull(l.addedRepos);
        assertTrue(l.removedRepos.contains(repo2));
        
        // remove listner
        RepositoryRegistry.getInstance().removePropertyChangeListener(l);
        l.addedRepos = null;
        l.removedRepos = null;
        RepositoryRegistry.getInstance().addRepository(repo2);
        assertNull(l.addedRepos);
        assertNull(l.removedRepos);
    }
    
    public void testStoredRepository() {
        RepositoryInfo info = new RepositoryInfo("repoid", ID_CONNECTOR1, "http://url", null, null, null, null, null, null);
        RepositoryRegistry.getInstance().putRepository(getRepository(new MyRepository(info)));
        RepositoryRegistry.getInstance().loadRepositories();
        
        Collection<RepositoryImpl> repos = RepositoryRegistry.getInstance().getRepositories(ID_CONNECTOR1, true);
        assertEquals(1, repos.size());
        RepositoryImpl repo = repos.iterator().next();
        assertEquals("repoid", repo.getId());
        assertEquals(ID_CONNECTOR1, repo.getConnectorId());
    }

    private RepositoryImpl getRepository(MyRepository myRepository) {
        return TestKit.getRepository(myRepository);
    }
    
    private static class MyRepository extends TestRepository {
        private RepositoryInfo info;

        public MyRepository(RepositoryInfo info) {
            this.info = info;
        }

        public MyRepository(String id) {
            this(id, ID_CONNECTOR1);
        }
        
        public MyRepository(String id, String cid) {
            this.info = new RepositoryInfo(id, cid, "http://test", null, null, null, null, null, null);
        }
        
        @Override
        public RepositoryInfo getInfo() {
            return info;
        }
    }
    
    private static final String ID_CONNECTOR1 = "RepositoryRegistryTestConector1";
    @BugtrackingConnector.Registration (id=ID_CONNECTOR1,displayName=ID_CONNECTOR1,tooltip=ID_CONNECTOR1)    
    public static class MyConnector1 implements BugtrackingConnector {
        public MyConnector1() {
        }

        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

        @Override
        public Repository createRepository(RepositoryInfo info) {
            return TestKit.getRepository(new MyRepository(info)).getRepository();
        }

        @Override
        public Repository createRepository() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }    
    
    private static final String ID_CONNECTOR2 = "RepositoryRegistryTestConector2";
    @BugtrackingConnector.Registration (id=ID_CONNECTOR2,displayName=ID_CONNECTOR2,tooltip=ID_CONNECTOR2)    
    public static class MyConnector2 extends MyConnector1 {} 
    
    private DelegatingConnector findConnector(String id) {
        DelegatingConnector[] conns = BugtrackingManager.getInstance().getConnectors();
        for (DelegatingConnector dc : conns) {
            if(id.equals(dc.getID())) {
                return dc;
            }
        }
        return null;
    }
}
