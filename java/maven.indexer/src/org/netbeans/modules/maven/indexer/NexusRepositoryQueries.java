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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.maven.index.ArtifactAvailability;
import org.apache.maven.index.ArtifactInfo;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.Field;
import org.apache.maven.index.IteratorResultSet;
import org.apache.maven.index.IteratorSearchRequest;
import org.apache.maven.index.IteratorSearchResponse;
import org.apache.maven.index.MAVEN;
import org.apache.maven.index.context.IndexingContext;
import org.apache.maven.index.expr.StringSearchExpression;
import org.apache.maven.search.api.SearchRequest;
import org.apache.maven.search.api.request.FieldQuery;
import org.apache.maven.search.api.request.Paging;
import org.apache.maven.search.api.transport.Java11HttpClientTransport;
import org.apache.maven.search.backend.smo.SmoSearchBackend;
import org.apache.maven.search.backend.smo.SmoSearchBackendFactory;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.maven.indexer.api.NBArtifactInfo;
import org.netbeans.modules.maven.indexer.api.NBGroupInfo;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.QueryField;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.indexer.spi.ArchetypeQueries;
import org.netbeans.modules.maven.indexer.spi.BaseQueries;
import org.netbeans.modules.maven.indexer.spi.ChecksumQueries;
import org.netbeans.modules.maven.indexer.spi.ClassUsageQuery;
import org.netbeans.modules.maven.indexer.spi.ClassesQuery;
import org.netbeans.modules.maven.indexer.spi.ContextLoadedQuery;
import org.netbeans.modules.maven.indexer.spi.DependencyInfoQueries;
import org.netbeans.modules.maven.indexer.spi.GenericFindQuery;
import org.netbeans.modules.maven.indexer.spi.ResultImplementation;
import org.openide.util.Mutex;

/**
 * Implements all maven queries.
 */
final class NexusRepositoryQueries implements
        BaseQueries, ChecksumQueries, ArchetypeQueries, DependencyInfoQueries,
        ClassesQuery, ClassUsageQuery, GenericFindQuery, ContextLoadedQuery {

    private static final Logger LOGGER = Logger.getLogger(NexusRepositoryQueries.class.getName());

    private SmoSearchBackend smo;
    private final NexusRepositoryIndexManager indexer;

    NexusRepositoryQueries(NexusRepositoryIndexManager indexer) {
        this.indexer = indexer;
    }

    @Override
    public ResultImplementation<String> getGroups(List<RepositoryInfo> repos) {
        return filterGroupIds("", repos);
    }

    private ResultImplementation<String> filterGroupIds(String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>(
            (ResultImpl<String> res) -> filterGroupIds(prefix, res, res.getSkipped(), false)
        );
        return filterGroupIds(prefix, result, repos, true);
    }

    private ResultImplementation<String> filterGroupIds(String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final Set<String> groups = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);

        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            Set<String> all= context.getAllGroups();
            if (!all.isEmpty()) {
                if (prefix.isEmpty()) {
                    groups.addAll(all);
                } else {
                    for (String gr : all) {
                        if (gr.startsWith(prefix)) {
                            groups.add(gr);
                        }
                    }
                }
            }
        }, skipAction, skipUnIndexed);

        result.setResults(groups);
        return result;
    }

    @Override
    public ResultImplementation<String> getGAVsForPackaging(String packaging, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            getGAVsForPackaging(packaging, result1, result1.getSkipped(), false);
        });
        return getGAVsForPackaging(packaging,result, repos, true);
    }

    private ResultImplementation<String> getGAVsForPackaging(String packaging, ResultImpl<String> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final List<String> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, packaging)), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.NO_CAP_RESULT_COUNT);
            if (response != null) {
               try {
                    for (ArtifactInfo ai : response) {
                        String gav = ai.getGroupId() + ":" + ai.getArtifactId() + ":" + ai.getVersion();
                        if (!infos.contains(gav)) {
                            infos.add(gav);
                        }
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> getRecords(String groupId, String artifactId, String version, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            getRecords(groupId, artifactId, version, result1, result1.getSkipped(), false);
        });
        return getRecords(groupId, artifactId, version, result, repos, true);
    }

    private ResultImplementation<NBVersionInfo> getRecords(String groupId, String artifactId, String version, ResultImpl<NBVersionInfo> result,
                                             List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS + version + ArtifactInfo.FS;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(new PrefixQuery(new Term(ArtifactInfo.UINFO, id)), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<String> getArtifacts(String groupId, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            getArtifacts(groupId, result1, result1.getSkipped(), false);
        });
        return getArtifacts(groupId, result, repos, true);
    }

    private  ResultImplementation<String> getArtifacts(String groupId, ResultImpl<String> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getArtifactId());
                    }
                } finally {
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> getVersions(String groupId, String artifactId, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            getVersions(groupId, artifactId, result1, result1.getSkipped(), false);
        });
        return getVersions(groupId, artifactId, result, repos, true);
    }

    private ResultImplementation<NBVersionInfo> getVersions(String groupId, String artifactId, ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS + artifactId + ArtifactInfo.FS;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            // Some projects generated quite a lot of artifacts by now.
            // Since this query is sometimes used by code which wants to find the top x most recent artifacts,
            // we have to use a relatively high results limit - this doesn't seem to be a performance problem (other queries set no limit)
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, 10_000);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findVersionsByClass(String className, List<RepositoryInfo> repos) {

        Optional<RepositoryInfo> central = repos.stream()
                .filter(repo -> repo.getId().equals(getSMO().getRepositoryId()))
                .findFirst();

        // remote index contains no class data -> use web service
        if (central.isPresent()) {
            List<RepositoryInfo> otherRepos = new ArrayList<>(repos);
            otherRepos.remove(central.get());

            SearchRequest request = new SearchRequest(new Paging(128),
                    FieldQuery.fieldQuery(className.contains(".") ?
                            org.apache.maven.search.api.MAVEN.FQ_CLASS_NAME
                          : org.apache.maven.search.api.MAVEN.CLASS_NAME, className));

            return new CompositeResult<>(findVersionsByClass(className, otherRepos), new SMORequestResult(getSMO(), request));
        } else {
            ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
                findVersionsByClass(className, result1, result1.getSkipped(), false);
            });
            return findVersionsByClass(className, result, repos, true);
        }
    }

    private ResultImplementation<NBVersionInfo> findVersionsByClass(String className, ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            String clsname = className.replace(".", "/");
            while (!clsname.isEmpty() && (clsname.startsWith("*") || clsname.startsWith("?"))) {
                //#238740
                clsname = clsname.substring(1);
            }
            if (clsname.isEmpty()) {
                return;
            }

            Query q = setBooleanRewrite(constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH)));
            IteratorSearchResponse response = repeatedPagedSearch(q, context, NexusRepositoryIndexManager.MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    infos.addAll(postProcessClasses(response.getResults(), clsname));
                } finally {
                    //?? really count in this case?
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    private Collection<NBVersionInfo> postProcessClasses(IteratorResultSet artifactInfos, String classname) {
        List<NBVersionInfo> toRet = new ArrayList<>();
        int patter = Pattern.DOTALL + Pattern.MULTILINE;
        boolean isPath = classname.contains("/");
        if (isPath) {
            for (ArtifactInfo i : artifactInfos) {
                toRet.add(convertToNBVersionInfo(i));
            }
            return toRet;
        }
        //if I got it right, we need an exact match of class name, which the query doesn't provide? why?
        String pattStr = ".*/" + classname + "$.*";
        Pattern patt = Pattern.compile(pattStr, patter);
        //#217932 for some reason IteratorResultSet implementation decided
        //not to implemenent Iterator.remove().
        //we need to copy to our own list instead of removing from original.
        ArrayList<ArtifactInfo> altArtifactInfos = new ArrayList<>();
        for (ArtifactInfo ai : artifactInfos) {
            Matcher m = patt.matcher(ai.getClassNames());
            if (m.matches()) {
                altArtifactInfos.add(ai);
            }
        }
        for (ArtifactInfo i : altArtifactInfos) {
            toRet.add(convertToNBVersionInfo(i));
        }
        return toRet;
    }

    @Override
    public ResultImplementation<RepositoryQueries.ClassUsage> findClassUsages(String className, @NullAllowed List<RepositoryInfo> repos) {
        ResultImpl<RepositoryQueries.ClassUsage> result = new ResultImpl<>((ResultImpl<RepositoryQueries.ClassUsage> result1) -> {
            findClassUsages(className, result1, result1.getSkipped(), false);
        });
        return findClassUsages(className, result, repos, true);
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private ResultImplementation<RepositoryQueries.ClassUsage> findClassUsages(String className, ResultImpl<RepositoryQueries.ClassUsage> result, @NullAllowed List<RepositoryInfo> repos, boolean skipUnIndexed) {
        List<RepositoryInfo> localRepos = new ArrayList<>();
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (RepositoryInfo repo : repos) {
            if (repo.isLocal()) {
                localRepos.add(repo);
            }
        }
        final List<RepositoryQueries.ClassUsage> results = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(localRepos, (RepositoryInfo repo, IndexingContext context) -> {
            ClassDependencyIndexCreator.search(className, indexer.getIndexer(), List.of(context), results);
        }, skipAction, skipUnIndexed);
        results.sort((RepositoryQueries.ClassUsage r1, RepositoryQueries.ClassUsage r2) -> r1.getArtifact().compareTo(r2.getArtifact()));
        result.setResults(results);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findDependencyUsage(String groupId, String artifactId, String version, @NullAllowed List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            findDependencyUsage(groupId, artifactId, version, result1, result1.getSkipped(), false);
        });
        return findDependencyUsage(groupId, artifactId, version, result, repos, true);
    }

    private ResultImplementation<NBVersionInfo> findDependencyUsage(String groupId, String artifactId, String version, ResultImpl<NBVersionInfo> result, @NullAllowed List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final Query q = ArtifactDependencyIndexCreator.query(groupId, artifactId, version);
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            IteratorSearchResponse response = repeatedPagedSearch(q, context, NexusRepositoryIndexManager.MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBGroupInfo> findDependencyUsageGroups(String groupId, String artifactId, String version, List<RepositoryInfo> repos) {
        ResultImpl<NBGroupInfo> result = new ResultImpl<>((ResultImpl<NBGroupInfo> result1) -> {
            findDependencyUsageGroups(groupId, artifactId, version, result1, result1.getSkipped(), false);
        });
        return findDependencyUsageGroups(groupId, artifactId, version, result, repos, true);
    }

    private ResultImplementation<NBGroupInfo> findDependencyUsageGroups(String groupId, String artifactId, String version, ResultImpl<NBGroupInfo> result, List<RepositoryInfo> repos, final boolean skipUnIndexed) {
        //tempmaps
        Map<String, NBGroupInfo> groupMap = new HashMap<>();
        Map<String, NBArtifactInfo> artifactMap = new HashMap<>();
        List<NBGroupInfo> groupInfos = new ArrayList<>(result.getResults());
        ResultImpl<NBVersionInfo> res = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            //noop will not be called
        });
        findDependencyUsage(groupId, artifactId, version, res, repos, skipUnIndexed);
        convertToNBGroupInfo(res.getResults(), groupMap, artifactMap, groupInfos);
        if (res.isPartial()) {
            result.addSkipped(res.getSkipped());
        }
        result.setResults(groupInfos);
        return result;
    }

    private static void convertToNBGroupInfo(Collection<NBVersionInfo> artifactInfos,
                                      Map<String, NBGroupInfo> groupMap,
                                      Map<String, NBArtifactInfo> artifactMap,
                                      List<NBGroupInfo> groupInfos) {
        for (NBVersionInfo ai : artifactInfos) {
            String groupId = ai.getGroupId();
            String artId = ai.getArtifactId();

            NBGroupInfo ug = groupMap.get(groupId);
            if (ug == null) {
                ug = new NBGroupInfo(groupId);
                groupInfos.add(ug);
                groupMap.put(groupId, ug);
            }
            NBArtifactInfo ua = artifactMap.get(artId);
            if (ua == null) {
                ua = new NBArtifactInfo(artId);
                ug.addArtifactInfo(ua);
                artifactMap.put(artId, ua);
            }
            ua.addVersionInfo(ai);
        }
    }

    @Override
    public ResultImplementation<NBVersionInfo> findBySHA1(String sha1, List<RepositoryInfo> repos) {

        Optional<RepositoryInfo> central = repos.stream()
            .filter(repo -> repo.getId().equals(getSMO().getRepositoryId()))
            .findFirst();

        // remote index contains no sh1 data -> use web service
        if (central.isPresent()) {
            List<RepositoryInfo> otherRepos = new ArrayList<>(repos);
            otherRepos.remove(central.get());

            SearchRequest request = new SearchRequest(new Paging(8),
                    FieldQuery.fieldQuery(org.apache.maven.search.api.MAVEN.SHA1, sha1));

            return new CompositeResult<>(findBySHA1(sha1, otherRepos), new SMORequestResult(getSMO(), request));
        } else {
            ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
                findBySHA1(sha1, result1, result1.getSkipped(), false);
            });
            return findBySHA1(sha1, result, repos, true);
        }
    }

    private ResultImplementation<NBVersionInfo> findBySHA1(String sha1, ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            BooleanQuery bq = new BooleanQuery.Builder()
                    .add(new BooleanClause((setBooleanRewrite(constructQuery(MAVEN.SHA1, sha1))), BooleanClause.Occur.SHOULD))
                    .build();
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.MAX_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> findArchetypes(List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            findArchetypes(result1, result1.getSkipped(), false);
        });
        return findArchetypes( result, repos, true);
    }

    private ResultImplementation<NBVersionInfo> findArchetypes(ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        BooleanQuery bq = new BooleanQuery.Builder()
                // XXX also consider using NexusArchetypeDataSource
                .add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-archetype")), BooleanClause.Occur.MUST)) //NOI18N
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            /* There are >512 archetypes in Central, and we want them all in ChooseArchetypePanel
            FlatSearchRequest fsr = new FlatSearchRequest(bq, ArtifactInfo.VERSION_COMPARATOR);
            fsr.setCount(NexusRepositoryIndexerImpl.MAX_RESULT_COUNT);
            */
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo ai : response) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(response.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(response.getTotalHitsCount());
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    @Override
    public ResultImplementation<String> filterPluginArtifactIds(String groupId, String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            filterPluginArtifactIds(groupId, prefix, result1, result1.getSkipped(), false);
        });
        return filterPluginArtifactIds(groupId, prefix, result, repos, true);
    }

    private ResultImplementation<String> filterPluginArtifactIds(String groupId, String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        String id = groupId + ArtifactInfo.FS + prefix;
        BooleanQuery bq = new BooleanQuery.Builder()
                .add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST))
                .add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.UINFO, id))), BooleanClause.Occur.MUST))
                .build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getArtifactId());
                    }
                } finally {
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<String> filterPluginGroupIds(String prefix, List<RepositoryInfo> repos) {
        ResultImpl<String> result = new ResultImpl<>((ResultImpl<String> result1) -> {
            filterPluginGroupIds(prefix, result1, result1.getSkipped(), false);
        });
        return filterPluginGroupIds( prefix, result, repos, true);
    }

    private ResultImplementation<String> filterPluginGroupIds(String prefix, ResultImpl<String> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final Set<String> artifacts = new TreeSet<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        BooleanQuery.Builder builder = new BooleanQuery.Builder();
        builder.add(new BooleanClause(new TermQuery(new Term(ArtifactInfo.PACKAGING, "maven-plugin")), BooleanClause.Occur.MUST));
        if (!prefix.isEmpty()) { //heap out of memory otherwise
            builder.add(new BooleanClause(setBooleanRewrite(new PrefixQuery(new Term(ArtifactInfo.GROUP_ID, prefix))), BooleanClause.Occur.MUST));
        }
        BooleanQuery bq = builder.build();
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            //mkleint: this is not capped, because only a string is collected (and collapsed), the rest gets CGed fast
            IteratorSearchResponse response = repeatedPagedSearch(bq, context, NexusRepositoryIndexManager.NO_CAP_RESULT_COUNT);
            if (response != null) {
                try {
                    for (ArtifactInfo artifactInfo : response.getResults()) {
                        artifacts.add(artifactInfo.getGroupId());
                    }
                } finally {
                    response.close();
                }
            }
        }, skipAction, skipUnIndexed);
        result.setResults(artifacts);
        return result;
    }

    @Override
    public ResultImplementation<NBVersionInfo> find(final List<QueryField> fields, List<RepositoryInfo> repos) {
        ResultImpl<NBVersionInfo> result = new ResultImpl<>((ResultImpl<NBVersionInfo> result1) -> {
            find(fields, result1, result1.getSkipped(), false);
        });
        return find(fields,  result, repos, true);
    }

    private ResultImplementation<NBVersionInfo> find(List<QueryField> fields, ResultImpl<NBVersionInfo> result, List<RepositoryInfo> repos, boolean skipUnIndexed) {
        final List<NBVersionInfo> infos = new ArrayList<>(result.getResults());
        final SkippedAction skipAction = new SkippedAction(result);
        iterate(repos, (RepositoryInfo repo, IndexingContext context) -> {
            BooleanQuery.Builder bq = new BooleanQuery.Builder();
            for (QueryField field : fields) {
                BooleanClause.Occur occur = field.getOccur() == QueryField.OCCUR_SHOULD ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
                String fieldName = toNexusField(field.getField());
                String one = field.getValue();
                while (!one.isEmpty() && (one.startsWith("*") || one.startsWith("?"))) {
                    //#196046
                    one = one.substring(1);
                }
                if (one.isEmpty()) {
                    continue;
                }

                if (fieldName != null) {
                    Query q;
                    if (ArtifactInfo.NAMES.equals(fieldName)) {
                        try {
                            String clsname = one.replace(".", "/"); //NOI18N
                            q = constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH));
                        } catch (IllegalArgumentException iae) {
                            //#204651 only escape when problems occur
                            String clsname = QueryParser.escape(one.replace(".", "/")); //NOI18N
                            try {
                                q = constructQuery(MAVEN.CLASSNAMES, clsname.toLowerCase(Locale.ENGLISH));
                            } catch (IllegalArgumentException iae2) {
                                //#224088
                                continue;
                            }
                        }
                    } else if (ArtifactInfo.ARTIFACT_ID.equals(fieldName)) {
                        String aid = one.replace("-", "?").replace(".", "?");
                        try {
                            q = constructQuery(MAVEN.ARTIFACT_ID, aid);
                        } catch (IllegalArgumentException iae) {
                            //#204651 only escape when problems occur
                            try {
                                q = constructQuery(MAVEN.ARTIFACT_ID, QueryParser.escape(aid));
                            } catch (IllegalArgumentException iae2) {
                                //#224088
                                continue;
                            }
                        }
                    } else if (ArtifactInfo.GROUP_ID.equals(fieldName)) {
                        String gid = one.replace("-", "?").replace(".", "?");
                        try {
                            q = constructQuery(MAVEN.GROUP_ID, gid);
                        } catch (IllegalArgumentException iae) {
                            //#204651 only escape when problems occur
                            try {
                                q = constructQuery(MAVEN.GROUP_ID, QueryParser.escape(gid));
                            } catch (IllegalArgumentException iae2) {
                                //#224088
                                continue;
                            }
                        }
                    } else {
                        if (field.getMatch() == QueryField.MATCH_EXACT) {
                            q = new TermQuery(new Term(fieldName, one));
                        } else {
                            q = new PrefixQuery(new Term(fieldName, one));
                        }
                    }
                    bq.add(new BooleanClause(setBooleanRewrite(q), occur));
                } else {
                    //TODO when all fields, we need to create separate
                    //queries for each field.
                }
            }
            IteratorSearchResponse resp = repeatedPagedSearch(bq.build(), context, NexusRepositoryIndexManager.MAX_RESULT_COUNT);
            if (resp != null) {
                try {
                    for (ArtifactInfo ai : resp) {
                        infos.add(convertToNBVersionInfo(ai));
                    }
                } finally {
                    result.addReturnedResultCount(resp.getTotalProcessedArtifactInfoCount());
                    result.addTotalResultCount(resp.getTotalHitsCount());
                    resp.close();
                }
            }
        }, skipAction, skipUnIndexed);
        doSortIssue226100(infos);
        result.setResults(infos);
        return result;
    }

    private void doSortIssue226100(List<NBVersionInfo> infos) {
        try {
            Collections.sort(infos);
        } catch (IllegalStateException | IllegalArgumentException ex) {
//            doLogError226100(infos, ex);
        }
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void iterate(List<RepositoryInfo> repos, RepoAction action, RepoAction actionSkip, boolean skipUnIndexed) {
        if (repos == null) {
            repos = RepositoryPreferences.getInstance().getRepositoryInfos();
        }
        for (final RepositoryInfo repo : repos) {
            Mutex mutex = NexusRepositoryIndexManager.getRepoMutex(repo);
            if (skipUnIndexed && NexusRepositoryIndexManager.isIndexing(mutex)) {
                try {
                    actionSkip.run(repo, null);
                } catch (IOException ex) {
                    LOGGER.log(Level.FINER, "could not skip " + repo.getId(), ex);
                }
            } else {
                mutex.writeAccess((Mutex.Action<Void>) () -> {
                    try {
                        boolean index = indexer.loadIndexingContext(repo);
                        if (skipUnIndexed && index) {
                            if (!RepositoryPreferences.isIndexRepositories()) {
                                return null;
                            }
                            boolean spawned = indexer.spawnIndexLoadedRepo(repo);
                            if (spawned) {
                                actionSkip.run(repo, null);
                            }
                            return null;
                        }
                        IndexingContext context = indexer.getIndexingContexts().get(repo.getId());
                        if (context == null) {
                            if (skipUnIndexed) {
                                actionSkip.run(repo, null);
                            }
                            return null;
                        }
                        action.run(repo, context);
                    } catch (IOException x) {
                        LOGGER.log(Level.INFO, "could not process " + repo.getId(), x);
                    }
                    return null;
                });
            }
        }
    }

    @CheckForNull IteratorSearchResponse repeatedPagedSearch(Query q, IndexingContext context, int count) throws IOException {
        return repeatedPagedSearch(q, List.of(context), count);
    }

    @CheckForNull IteratorSearchResponse repeatedPagedSearch(Query q, List<IndexingContext> contexts, int count) throws IOException {
        IteratorSearchRequest isr = new IteratorSearchRequest(q, contexts, new NoJavadocSourceFilter());
        if (count > 0) {
            isr.setCount(count);
        }

        int MAX_MAX_CLAUSE = 1<<11;  // conservative maximum for too general queries, like "c:*class*"

        if (q instanceof BooleanQuery booleanQuery) {
            List<BooleanClause> list = booleanQuery.clauses();
            if (list.size() == 1) {
                Query q1 = list.get(0).getQuery();
                if (q1 instanceof PrefixQuery pq && "u".equals(pq.getPrefix().field())) {
                    // increase for queries like "+u:org.netbeans.modules|*" to succeed
                    MAX_MAX_CLAUSE = 1<<16;
                } else if (q1 instanceof TermQuery tq && "p".equals(tq.getTerm().field())) {
                    // +p:nbm also produces several thousand hits
                    MAX_MAX_CLAUSE = 1<<16;
                }
            }
        }

        int oldMax = IndexSearcher.getMaxClauseCount();
        try {
            int max = oldMax;
            while (true) {
                IteratorSearchResponse response;
                try {
                    IndexSearcher.setMaxClauseCount(max);
                    response = indexer.getSearcher().searchIteratorPaged(isr, contexts);
                    LOGGER.log(Level.FINE, "passed on {0} clauses processing {1} with {2} hits", new Object[] {max, q, response.getTotalHitsCount()});
                    return response;
                } catch (IndexSearcher.TooManyClauses exc) {
                    LOGGER.log(Level.FINE, "TooManyClauses on {0} clauses processing {1}", new Object[] {max, q});
                    max *= 2;
                    if (max > MAX_MAX_CLAUSE) {
                        LOGGER.log(Level.WARNING, "Encountered more than {0} clauses processing {1}", new Object[] {MAX_MAX_CLAUSE, q});
                        return null;
                    }
                }
            }
        } finally {
            IndexSearcher.setMaxClauseCount(oldMax);
        }
    }

    private Query constructQuery(Field f, String qs) {
        return indexer.getIndexer().constructQuery(f, new StringSearchExpression(qs));
    }

    private static Query setBooleanRewrite(final Query q) {
        if (q instanceof MultiTermQuery multiTermQuery) {
            multiTermQuery.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_REWRITE);
        } else if (q instanceof BooleanQuery booleanQuery) {
            for (BooleanClause c : booleanQuery.clauses()) {
                setBooleanRewrite(c.getQuery());
            }
        }
        return q;
    }

    private static String toNexusField(String field) {
        if (field != null) switch (field) {
            case QueryField.FIELD_ARTIFACTID: return ArtifactInfo.ARTIFACT_ID;
            case QueryField.FIELD_GROUPID: return ArtifactInfo.GROUP_ID;
            case QueryField.FIELD_VERSION: return ArtifactInfo.VERSION;
            case QueryField.FIELD_CLASSES: return ArtifactInfo.NAMES;
            case QueryField.FIELD_NAME: return ArtifactInfo.NAME;
            case QueryField.FIELD_DESCRIPTION: return ArtifactInfo.DESCRIPTION;
            case QueryField.FIELD_PACKAGING: return ArtifactInfo.PACKAGING;
        }
        return field;
    }

    static List<NBVersionInfo> convertToNBVersionInfo(Collection<ArtifactInfo> artifactInfos) {
        List<NBVersionInfo> bVersionInfos = new ArrayList<>();
        for (ArtifactInfo ai : artifactInfos) {
            NBVersionInfo nbvi = convertToNBVersionInfo(ai);
            if (nbvi != null) {
              bVersionInfos.add(nbvi);
            }
        }
        return bVersionInfos;
    }

    static NBVersionInfo convertToNBVersionInfo(ArtifactInfo ai) {
        if ("javadoc".equals(ai.getClassifier()) || "sources".equals(ai.getClassifier())) { //NOI18N
            // we don't want javadoc and sources shown anywhere, we use the getJavadocExists(), getSourceExists() methods.
            return null;
        }
        // fextension != packaging - e.g a pom could be packaging "bundle" but from type/extension "jar"
        NBVersionInfo nbvi = new NBVersionInfo(ai.getRepository(), ai.getGroupId(), ai.getArtifactId(),
                ai.getVersion(), ai.getFileExtension(), ai.getPackaging(), ai.getName(), ai.getDescription(), ai.getClassifier());
        /*Javadoc & Sources*/
        nbvi.setJavadocExists(ai.getJavadocExists() == ArtifactAvailability.PRESENT);
        nbvi.setSourcesExists(ai.getSourcesExists() == ArtifactAvailability.PRESENT);
        nbvi.setSignatureExists(ai.getSignatureExists() == ArtifactAvailability.PRESENT);
//        nbvi.setSha(ai.sha1);
        nbvi.setLastModified(ai.getLastModified());
        nbvi.setSize(ai.getSize());
        nbvi.setLuceneScore(ai.getLuceneScore());
        return nbvi;
    }

    @Override
    public List<RepositoryInfo> getLoaded(final List<RepositoryInfo> repos) {
        final List<RepositoryInfo> toRet = new ArrayList<>(repos.size());
        for (final RepositoryInfo repo : repos) {
            Path loc = NexusRepositoryIndexManager.getIndexDirectory(repo); // index folder
            if (NexusRepositoryIndexManager.indexExists(loc)) {
                toRet.add(repo);
            }
        }
        return toRet;
    }

    synchronized SmoSearchBackend getSMO() {
        if (smo == null) {
            smo = SmoSearchBackendFactory.create(
                    SmoSearchBackendFactory.DEFAULT_BACKEND_ID,
                    SmoSearchBackendFactory.DEFAULT_REPOSITORY_ID,
                    SmoSearchBackendFactory.DEFAULT_SMO_URI,
                    new Java11HttpClientTransport(SMORequestResult.REQUEST_TIMEOUT)
            );
        }
        return smo;
    }

    private interface RepoAction {
        void run(RepositoryInfo repo, IndexingContext context) throws IOException;
    }

    private record SkippedAction(ResultImpl<?> result) implements RepoAction {
        @Override
        public void run(RepositoryInfo repo, IndexingContext context) throws IOException {
            //indexing context is always null here..
            result.addSkipped(repo);
        }
    }

    private static class NoJavadocSourceFilter implements ArtifactInfoFilter {
        @Override
        public boolean accepts(IndexingContext ctx, ArtifactInfo ai) {
            return !("javadoc".equals(ai.getClassifier()) || "sources".equals(ai.getClassifier()));
        }
    }

}
