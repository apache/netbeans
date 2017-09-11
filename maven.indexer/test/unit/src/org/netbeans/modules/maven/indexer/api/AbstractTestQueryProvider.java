/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.indexer.api;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.apache.maven.artifact.Artifact;
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

    abstract protected String getID();

    @Override
    public ArchetypeQueries getArchetypeQueries() {
        return new ArchetypeQueries() {
            @Override
            public ResultImplementation<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
                return new ResultImplementation<NBVersionInfo>() {
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
