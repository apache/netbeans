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
package org.netbeans.modules.maven.indexer.api;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.openide.util.Exceptions;
import org.netbeans.modules.maven.indexer.spi.RepositoryIndexQueryProvider;

/**
 *
 * @author tomas
 */
public abstract class AbstractTestQueryProvider implements RepositoryIndexQueryProvider {

    private static final String TESTREPO = "testrepo";
    
    public static class TestIndexer1 extends AbstractTestQueryProvider {
        public static final String ID = TESTREPO + ".1";
        public static RepositoryInfo REPO;
        static {
            try {REPO = new RepositoryInfo(ID, ID, null, "http://test1"); } catch (URISyntaxException ex) { Exceptions.printStackTrace(ex); }
        }
        public TestIndexer1() {
            this.repos = new RepositoryInfo[] {REPO};
        }
        @Override
        protected String getID() {
            return ID;
        }
    }
    public static class TestIndexer2 extends AbstractTestQueryProvider {
        public static final String ID = TESTREPO + ".2";
        public static RepositoryInfo REPO;
        static {
            try {REPO = new RepositoryInfo(ID, ID, null, "http://test1"); } catch (URISyntaxException ex) { Exceptions.printStackTrace(ex); }
        }
        public TestIndexer2() {
            this.repos = new RepositoryInfo[] {REPO};
        }
        
        @Override
        protected String getID() {
            return ID;
        }
    }
    
    protected RepositoryInfo[] repos;
    public AbstractTestQueryProvider() {
    }

    @Override
    public boolean handlesRepository(RepositoryInfo repo) {
        for (RepositoryInfo r : repos) {
            if(repo.getId().equals(r.getId())) {
                return true;
            }
        }    
        return false;
    }

    protected abstract String getID();

    @Override
    public ArchetypeQueries getArchetypeQueries() {
        return (List<RepositoryInfo> repos1) -> new ResultImplementation<NBVersionInfo>() {
            @Override
            public boolean isPartial() {
                return false;
            }

            @Override
            public void waitForSkipped() { }

            @Override
            public List<NBVersionInfo> getResults() {
                return Arrays.asList(new NBVersionInfo(getID(), getID(), getID(), "1.0", "jar", "jar", "test", "test", null));
            }

            @Override
            public int getTotalResultCount() {
                return 1;
            }

            @Override
            public int getReturnedResultCount() {
                return 1;
            }
        };
    }
    
    @Override
    public BaseQueries getBaseQueries() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ChecksumQueries getChecksumQueries() {
        return null;
    }

    @Override
    public ClassUsageQuery getClassUsageQuery() {
        return null;
    }

    @Override
    public ClassesQuery getClassesQuery() {
        return null;
    }

    @Override
    public ContextLoadedQuery getContextLoadedQuery() {
        return null;
    }

    @Override
    public DependencyInfoQueries getDependencyInfoQueries() {
        return null;
    }

    @Override
    public GenericFindQuery getGenericFindQuery() {
        return null;
    }

}
