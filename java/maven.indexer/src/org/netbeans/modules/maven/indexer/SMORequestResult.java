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
package org.netbeans.modules.maven.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.maven.search.api.MAVEN;
import org.apache.maven.search.api.SearchRequest;
import org.apache.maven.search.backend.smo.SmoSearchBackend;
import org.apache.maven.search.backend.smo.SmoSearchResponse;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;

/**
 * Wraps search-maven-org query results and handles pagination.
 * 
 * @author mbien
 */
final class SMORequestResult implements ResultImplementation<NBVersionInfo> {

    private static final Logger LOG = Logger.getLogger(SMORequestResult.class.getName());

    // if we paginate too much we will get throttled
    private static final int MAX_PAGES = 10;
    private static final long TIMEOUT = 20_000;

    private final SmoSearchBackend smo;
    private SmoSearchResponse response;

    private List<NBVersionInfo> list;
    
    public SMORequestResult(SmoSearchBackend smo, SearchRequest request) {
        this.smo = smo;
        try {
            this.response = search(request);
            this.list = null;
        } catch (IOException ex) {
            LOG.log(Level.INFO, "SMO "+request+" failed", ex);
            this.response = null;
            this.list = Collections.emptyList();
        }
    }

    @Override
    public List<NBVersionInfo> getResults() {
        if (list == null) {
            list = response.getPage().stream()
                .map(rec -> new NBVersionInfo(
                        smo.getRepositoryId(),
                        rec.getValue(MAVEN.GROUP_ID),
                        rec.getValue(MAVEN.ARTIFACT_ID),
                        rec.getValue(MAVEN.VERSION),
                        rec.getValue(MAVEN.PACKAGING), // todo, type is used in the UI as packaging??
                        rec.getValue(MAVEN.PACKAGING),
                        null,
                        null,
                        rec.getValue(MAVEN.CLASSIFIER)))
                .collect(Collectors.toList());
        }
        return list;
    }

    @Override
    public void waitForSkipped() {
        if (!isPartial()) {
            return;
        }
        List<NBVersionInfo> fullList = new ArrayList<>(getResults());
        long start = System.currentTimeMillis();
        int page = 0;
        while (isPartial() && page++ < MAX_PAGES && (System.currentTimeMillis()-start) < TIMEOUT) {
            list = null;
            try {
                response = search(response.getSearchRequest().nextPage());
                fullList.addAll(getResults());
            } catch (IOException ex) {
                LOG.log(Level.INFO, "SMO request failed during pagination", ex);
                break;
            }
        }
        list = Collections.unmodifiableList(fullList);
    }

    private SmoSearchResponse search(SearchRequest request) throws IOException {
        long delta = System.currentTimeMillis();
        SmoSearchResponse resp = smo.search(request);
        LOG.log(Level.INFO, "SMO {0} finished in {1} ms", new Object[] {request, System.currentTimeMillis()-delta});
        return resp;
    }

    @Override
    public boolean isPartial() {
        return response != null && response.getTotalHits() > response.getCurrentHits();
    }

    @Override
    public int getTotalResultCount() {
        return response == null ? 0 : response.getTotalHits();
    }

    @Override
    public int getReturnedResultCount() {
        return response == null ? 0 : response.getCurrentHits();
    }
    
}
