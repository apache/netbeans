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

package org.netbeans.modules.maven.centralsearch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author mbien
 */
public class MavenCentralClassesQuery implements ClassesQuery {

    private static final Logger LOG = Logger.getLogger(MavenCentralClassesQuery.class.getName());

    private static final String REPO_NAME = "central";
    private static final MavenCentralSearch search = new MavenCentralSearch();


    public boolean handlesRepository(RepositoryInfo repo) {
        return repo.isLocal() == false && REPO_NAME.equals(repo.getName());
    }

    private boolean checkIfCentralRepo(List<RepositoryInfo> repos) {
        return repos.size() == 1 && handlesRepository(repos.get(0));
    }

    @Override
    public ResultImplementation<NBVersionInfo> findVersionsByClass(String className, List<RepositoryInfo> repos) {
        if (!checkIfCentralRepo(repos)) {
            throw new IllegalArgumentException("single, 'central' repository expected, but got: "+repos);
        }
        return new SearchResultImpl(() -> search.findArtifactsByClass(className));
    }

    @FunctionalInterface
    private interface IOSupplier<T> {
        T get() throws IOException;
    }

    private static final class SearchResultImpl implements ResultImplementation<NBVersionInfo> {

        private static final RequestProcessor RP = new RequestProcessor("maven-central-search", 5);

        private JSONSearchResponse response;
        private List<NBVersionInfo> list = Collections.emptyList();

        private final Future<JSONSearchResponse> request;

        public SearchResultImpl(IOSupplier<JSONSearchResponse> finder) {
            request = RP.submit(() -> {
                try {
                    return finder.get();
                } catch (IOException ex) {
                    throw new ExecutionException(ex);
                }
            });
        }

        @Override
        public List<NBVersionInfo> getResults() {
            if (response == null) {
                try {
                    response = request.get(10, TimeUnit.SECONDS);
                    list = response.getDocs().stream()
                            .map((d) -> new NBVersionInfo(REPO_NAME, d.getGroup(), d.getArtifact(), d.getVersion(), d.getPackage(), null, null, null, null))
                            .collect(Collectors.toList());
                    LOG.log(Level.INFO, "{0}", response);
                } catch (TimeoutException ex) {
                    LOG.log(Level.WARNING, "search request timed out", ex);
                } catch (ExecutionException ex) {
                    LOG.log(Level.WARNING, "search request failed", ex);
                } catch (InterruptedException ex) {
                    Thread.interrupted();
                    Exceptions.printStackTrace(ex);
                }
            }
            return list;
        }

        @Override
        public boolean isPartial() {
            return false;
        }

        @Override
        public void waitForSkipped() {

        }

        @Override
        public int getTotalResultCount() {
            return response.getNumFound();
        }

        @Override
        public int getReturnedResultCount() {
            return list.size();
        }
    }

}
