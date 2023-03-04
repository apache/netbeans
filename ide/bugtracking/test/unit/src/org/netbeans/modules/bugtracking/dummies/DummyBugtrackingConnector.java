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

package org.netbeans.modules.bugtracking.dummies;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.TestKit;
import org.netbeans.modules.bugtracking.spi.BugtrackingConnector;
import org.netbeans.modules.bugtracking.spi.RepositoryInfo;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.api.Repository;
import org.netbeans.modules.bugtracking.ui.repository.RepositoryComboSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Marian Petras
 */
@BugtrackingConnector.Registration (
    id=DummyBugtrackingConnector.ID,
    displayName=DummyBugtrackingConnector.DISPLAY_NAME,
    tooltip=DummyBugtrackingConnector.TOOLTIP
)    
public class DummyBugtrackingConnector implements BugtrackingConnector {
    public static final String ID = "DummyBugtrackingConnector";
    public static final String DISPLAY_NAME = "Dummy bugtracking connector";
    public static final String TOOLTIP = "bugtracking connector created for testing purposes";
    
    private char newRepositoryName = 'A';
    private int newRepositoryNumber = 0;
    private List<RepositoryImpl> repositories;
    public static DummyBugtrackingConnector instance;

    public DummyBugtrackingConnector() {
        instance = this;
    }
    
    @Override
    public Repository createRepository() {
        return createRepository(generateNewRepositoryName());
    }

    public Repository createRepository(String repositoryName) {
        return createRepository(repositoryName, true);
    }
    
    public Repository createRepository(String repositoryName, boolean canAttach) {
        RepositoryImpl newRepository = TestKit.getRepository(new DummyRepository(this, repositoryName, canAttach));
        storeRepository(newRepository);
        return newRepository.getRepository();
    }

    private String generateNewRepositoryName() {
        if (newRepositoryName != 'X') {
            return String.valueOf(newRepositoryName++);
        } else {
            return 'X' + String.valueOf(++newRepositoryNumber);
        }
    }

    private void storeRepository(RepositoryImpl repository) {
        if (repositories == null) {
            repositories = new ArrayList<RepositoryImpl>();
        }
        repositories.add(repository);
        RepositoryRegistry.getInstance().addRepository(repository);
    }

    void removeRepository(RepositoryImpl repository) {
        if (repositories == null) {
            return;
        }

        repositories.remove(repository);
        RepositoryRegistry.getInstance().removeRepository(repository);
    }

    public void reset() {
        if(repositories != null) {
            for (RepositoryImpl repository : repositories) {
                RepositoryRegistry.getInstance().removeRepository(repository);
            }
            repositories = null;
        }
    }

    public Lookup getLookup() {
        return Lookup.EMPTY;
    }

    @Override
    public Repository createRepository(RepositoryInfo info) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
