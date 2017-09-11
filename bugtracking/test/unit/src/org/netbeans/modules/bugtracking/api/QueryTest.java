/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.bugtracking.api;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;
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
