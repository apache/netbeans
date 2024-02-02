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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;
import org.apache.maven.search.api.SearchRequest;
import org.apache.maven.search.backend.smo.SmoSearchBackend;
import org.apache.maven.search.backend.smo.SmoSearchBackendFactory;
import org.apache.maven.artifact.versioning.ComparableVersion;

import static java.util.FormatProcessor.FMT;
import static org.apache.maven.search.api.MAVEN.ARTIFACT_ID;
import static org.apache.maven.search.api.MAVEN.CLASSIFIER;
import static org.apache.maven.search.api.MAVEN.GROUP_ID;
import static org.apache.maven.search.api.MAVEN.VERSION;
import static org.apache.maven.search.api.request.BooleanQuery.and;
import static org.apache.maven.search.api.request.FieldQuery.fieldQuery;

/**
 * Scans for binaries-list files and checks if newer versions of the declared dependencies exist.
 * 
 * <pre>org.apache.maven.indexer:search-backend-smo</pre> must be in classpath.
 * 
 * @author mbien
 */
public class BinariesListUpdates {

    private static final LongAdder updates = new LongAdder();
    private static final LongAdder checks = new LongAdder();
    private static final LongAdder skips = new LongAdder();

    // java --enable-preview --source 22 --class-path "lib/*" BinariesListUpdates.java /path/to/netbeans/project
    public static void main(String[] args) throws IOException, InterruptedException {

        if (args.length != 1 || Files.notExists(Path.of(args[0]).resolve("README.md"))) {
            throw new IllegalArgumentException("path to netbeans folder expected");
        }

        Path path = Path.of(args[0]);
        try (Stream<Path> dependencyFiles = Files.find(path, 10, (p, a) -> p.getFileName().toString().equals("binaries-list"));
             SmoSearchBackend backend = SmoSearchBackendFactory.createDefault()) {
            dependencyFiles.sorted().forEach(p -> {
                try {
                    checkDependencies(p, backend);
                } catch (IOException | InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            });
        }

        System.out.println(STR."checked \{checks.sum()} dependencies, found \{updates.sum()} updates, skipped \{skips.sum()}." );
    }

    private static void checkDependencies(Path path, SmoSearchBackend backend) throws IOException, InterruptedException {
        System.out.println(path);
        try (Stream<String> lines = Files.lines(path).parallel()) {

            // 321C614F85F1DEA6BB08C1817C60D53B7F3552FD org.fusesource.jansi:jansi:2.4.0
            lines.filter(l -> !l.startsWith("#"))
                 .filter(l -> l.length() > 40 && l.charAt(40) == ' ')
                 .map(l -> l.substring(40+1))
                 .forEach(l -> {

                String[] comp = l.split("\\:");
                if (comp.length == 3 || comp.length == 4) {
                    String gid = comp[0].strip();
                    String aid = comp[1].strip();
                    String version = comp[2].strip();
                    String classifier = comp.length == 4 ? comp[3].strip() : null;
                    try {
                        String gac;
                        String latest;
                        if (classifier == null) {
                            latest = queryLatestVersion(backend, gid, aid);
                            gac = String.join(":", gid, aid);
                        } else {
                            latest = queryLatestVersion(backend, gid, aid, classifier.split("@")[0]);
                            gac = String.join(":", gid, aid, classifier);
                        }
                        if (latest != null && !version.equals(latest)) {
                            System.out.println(FMT."    %-50s\{gac} \{version} -> \{latest}");
                            updates.increment();
                        }
                    } catch (IOException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    System.out.println("    skip: '"+l+"'");
                    skips.increment();
                }
                checks.increment();
            });
        }
        System.out.println();
    }

    private static String queryLatestVersion(SmoSearchBackend backend, String gid, String aid) throws IOException, InterruptedException {
        return queryLatestVersion(backend, new SearchRequest(and(fieldQuery(GROUP_ID, gid), fieldQuery(ARTIFACT_ID, aid))));
    }

    private static String queryLatestVersion(SmoSearchBackend backend, String gid, String aid, String classifier) throws IOException, InterruptedException {
        return queryLatestVersion(backend, new SearchRequest(and(fieldQuery(GROUP_ID, gid), fieldQuery(ARTIFACT_ID, aid), fieldQuery(CLASSIFIER, classifier))));
    }

    // reduce concurrency level if needed
    private final static Semaphore requests = new Semaphore(4);

    private static String queryLatestVersion(SmoSearchBackend backend, SearchRequest request) throws IOException, InterruptedException {
        requests.acquire();
        try {
            return backend.search(request).getPage().stream()
                    .map(r -> r.getValue(VERSION))
                    .filter(v -> !v.contains("alpha") && !v.contains("beta"))
                    .filter(v -> !v.contains("M") && !v.contains("m") && !v.contains("B") && !v.contains("b") && !v.contains("ea"))
                    .limit(5)
                    .max((v1, v2) -> new ComparableVersion(v1).compareTo(new ComparableVersion(v2)))
                    .orElse(null);
        } finally {
            requests.release();
        }
    }

}
