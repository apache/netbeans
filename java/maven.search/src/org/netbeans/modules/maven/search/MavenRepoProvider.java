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
package org.netbeans.modules.maven.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.lucene.search.BooleanQuery;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries.Result;
import org.netbeans.modules.maven.indexer.spi.ui.ArtifactNodeSelector;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class MavenRepoProvider implements SearchProvider {

    private static final RequestProcessor RP = new RequestProcessor(MavenRepoProvider.class.getName(), 10);

    /**
     * Method is called by infrastructure when search operation was requested.
     * Implementors should evaluate given request and fill response object with
     * appropriate results
     *
     * @param request Search request object that contains information what to search for
     * @param response Search response object that stores search results. Note that it's important to react to return value of SearchResponse.addResult(...) method and stop computation if false value is returned.
     */
    @Override
    public void evaluate(final SearchRequest request, final SearchResponse response) {
        final ArtifactNodeSelector s = Lookup.getDefault().lookup(ArtifactNodeSelector.class);
        if (s == null) {
            return;
        }
        final String q = request.getText();
        if (q == null || q.trim().isEmpty()) {
            //#205552
            return;
        }
        
        final List<RepositoryInfo> loadedRepos = RepositoryQueries.getLoadedContexts();
        if (loadedRepos.isEmpty()) {
            return;
        }

        List<NBVersionInfo> infos = new ArrayList<NBVersionInfo>();
        final List<NBVersionInfo> tempInfos = new ArrayList<NBVersionInfo>();

        final RequestProcessor.Task searchTask = RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    Result<NBVersionInfo> result = RepositoryQueries.findResult(getQuery(q), loadedRepos);
                    synchronized (tempInfos) {
                        tempInfos.addAll(result.getResults());
                    }
                } catch (BooleanQuery.TooManyClauses exc) {
                    // query too general, just ignore it
                    synchronized (tempInfos) {
                        tempInfos.clear();
                    }
                } catch (OutOfMemoryError oome) {
                    // running into OOME may still happen in Lucene despite the fact that
                    // we are trying hard to prevent it in NexusRepositoryIndexerImpl
                    // (see #190265)
                    // in the bad circumstances theoretically any thread may encounter OOME
                    // but most probably this thread will be it
                    synchronized (tempInfos) {
                        tempInfos.clear();
                    }
                }
            }
        });
        try {
            // wait maximum 5 seconds for the repository search to complete
            // after the timeout tempInfos should contain at least partial results
            // we are not waiting longer, repository index download may be running on the background
            // because NexusRepositoryIndexerImpl.getLoaded() might have returned also repos
            // which are not available for the search yet
            searchTask.waitFinished(5000);
        } catch (InterruptedException ex) {
        }
        synchronized (tempInfos) {
            infos.addAll(tempInfos);
        }
        searchTask.cancel();

        Collections.sort(infos);
        Set<String> artifacts = new HashSet<String>();
        String ql = q.toLowerCase(Locale.ENGLISH);
        for (final NBVersionInfo art : infos) {
            String label = art.getGroupId() + " : " + art.getArtifactId();
            if (!artifacts.add(label)) {
                continue; // ignore older versions
            }
            if (!label.toLowerCase(Locale.ENGLISH).contains(ql)) {
                String projectName = art.getProjectName();
                String projectDescription = art.getProjectDescription();
                if (projectName != null && projectName.toLowerCase(Locale.ENGLISH).contains(ql)) {
                    label += " (" + projectName + ")";
                } else if (projectDescription != null && projectDescription.toLowerCase(Locale.ENGLISH).contains(ql)) {
                    label += " \"" + projectDescription + "\"";
                }
            }
            if (!response.addResult(new Runnable() {
                @Override public void run() {
                    s.select(art);
                }
            }, label)) {
                return;
            }
        }
    }

    List<QueryField> getQuery(String q) {
        List<QueryField> fq = new ArrayList<QueryField>();
        String[] splits = q.split(" "); //NOI18N
        List<String> fields = new ArrayList<String>();
        fields.add(QueryField.FIELD_GROUPID);
        fields.add(QueryField.FIELD_ARTIFACTID);
//        fields.add(QueryField.FIELD_VERSION);
        fields.add(QueryField.FIELD_NAME);
        fields.add(QueryField.FIELD_DESCRIPTION);
//        fields.add(QueryField.FIELD_CLASSES);

        for (String one : splits) {
            if (one.trim().isEmpty()) {
                //#205552
                continue;
            }
            for (String fld : fields) {
                QueryField f = new QueryField();
                f.setField(fld);
                f.setValue(one);
                fq.add(f);
            }
        }
        return fq;
    }

}
