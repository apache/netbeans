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
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import static junit.framework.Assert.assertTrue;
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
