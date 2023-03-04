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
package org.netbeans.modules.maven.indexer;

import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.index.ArtifactContext;
import org.apache.maven.index.ArtifactContextProducer;
import org.apache.maven.index.Scanner;
import org.apache.maven.index.ScanningRequest;
import org.apache.maven.index.ScanningResult;
import org.apache.maven.index.context.IndexingContext;

/**
 * Alternative to Maven's DefaultScanner which ignores files NetBeans will not
 * be interested in indexing; and publishes scanning requests incrementally,
 * per-directory, rather than first collecting a tree's worth of artifacts.
 *
 * @author Tim Boudreau
 */
public class FastScanner
        implements Scanner {

    private final ArtifactContextProducer artifactContextProducer;
    private static final Logger LOG = Logger.getLogger(FastScanner.class.getName());

    @Inject
    public FastScanner(ArtifactContextProducer artifactContextProducer) {
        this.artifactContextProducer = artifactContextProducer;
    }

    @Override
    public ScanningResult scan(ScanningRequest request) {
        request.getArtifactScanningListener().scanningStarted(request.getIndexingContext());
        ScanningResult result = new ScanningResult(request);
        try {
            scanDirectory(request.getStartingDirectory().toPath(), request);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Scanning failed", ex);
        } finally {
            request.getArtifactScanningListener().scanningFinished(request.getIndexingContext(), result);
        }

        return result;
    }

    private void scanDirectory(Path dir, ScanningRequest request) throws IOException {
        if (dir == null) {
            return;
        }
        Files.walkFileTree(dir, new FileVisitor<Path>() {
            private final Set<Path> poms = new HashSet<>();
            private final Set<Path> artifacts = new HashSet<>();

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                poms.clear();
                artifacts.clear();
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String nm = file.getFileName().toString();
                if (nm.endsWith(".pom")) {
                    poms.add(file);
                    // txt and sha1 are needed for tests to pass, but not likely useful
                    // in NetBeans, and will impact performance
                } else if (nm.endsWith(".jar") || nm.endsWith(".nbm") || nm.endsWith(".txt") || nm.endsWith(".xml")) {
                    artifacts.add(file);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                LOG.log(Level.INFO, "Visit failed: " + file, exc);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                // Ensure JARs are procssed before POMs - see comments on
                // DefaultScanner's nested comparator class for why
                try {
                    if (!artifacts.isEmpty()) {
                        for (Path jar : artifacts) {
                            processFile(jar.toFile(), request);
                        }
                    }
                    if (!poms.isEmpty()) {
                        for (Path pom : poms) {
                            processFile(pom.toFile(), request);
                        }
                    }
                } catch (Exception e) {
                    LOG.log(Level.INFO, "Exception indexing " + artifacts + ", " + poms, e);
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void processFile(File file, ScanningRequest request) {
        IndexingContext context = request.getIndexingContext();
        ArtifactContext ac = artifactContextProducer.getArtifactContext(context, file);

        if (ac != null) {
            request.getArtifactScanningListener().artifactDiscovered(ac);
        }
    }
}
