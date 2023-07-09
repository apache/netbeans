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
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.bugtracking.APIAccessor;
import org.netbeans.modules.bugtracking.ui.query.QueryTopComponent;
import org.openide.util.test.MockLookup;

/**
 *
 * @author tomas
 */
public class QueryTest extends NbTestCase {

    public QueryTest(String arg0) {
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
    protected void tearDown() throws Exception { }

    public void testGetAttributes() {
        APITestQuery apiQuery = getAPIQuery();
        Query query = getQuery();
        
        assertEquals(apiQuery.getDisplayName(), query.getDisplayName());
        assertEquals(apiQuery.getTooltip(), query.getTooltip());
    }
    
    public void testGetRepository() {
        assertEquals(getRepo(), getQuery().getRepository());
    }    
        
    public void testRefresh() {
        APITestQuery apiQuery = getAPIQuery();
        Query query = getQuery();
        
        apiQuery.wasRefreshed = false;
        query.refresh();
        assertTrue(apiQuery.wasRefreshed);
    }
        
    public void testGetIssues() {
        APITestQuery apiQuery = getAPIQuery();
        Query query = getQuery();
        
        assertEquals(APIAccessor.IMPL.getImpl(query).getIssues().size(), query.getIssues().size());
    }
    
    public void testPCL() {
        APITestQuery apiQuery = getAPIQuery();
        Query query = getQuery();
        
        final boolean refreshed[] = new boolean[] {false};
        PropertyChangeListener l = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                refreshed[0] = true;
            }
        };
        
        query.addPropertyChangeListener(l);
        apiQuery.wasRefreshed = false;
        query.refresh();
        assertTrue(apiQuery.wasRefreshed);
        assertTrue(refreshed[0]);
        
        refreshed[0] = false;
        query.removePropertyChangeListener(l);
        assertFalse(refreshed[0]);
    }

    private APITestRepository getApiRepo() {
        return APITestKit.getAPIRepo(APITestRepository.ID);
    }

    private Repository getRepo() {
        return APITestKit.getRepo(APITestRepository.ID);
    }

    private APITestQuery getAPIQuery() {
        return getApiRepo().getQueries().iterator().next();
    }
    
    private Query getQuery() {
        return getRepo().getQueries().iterator().next();
    }

    private void assertOpened(APITestQuery apiQuery) throws InterruptedException {
        long t = System.currentTimeMillis();
        while(!apiQuery.wasOpened) {
            Thread.currentThread().sleep(200);
            if(System.currentTimeMillis() - t > 5000) {
                // timeout
                fail("issue wasn't opened");
            }
        }
    }

}
