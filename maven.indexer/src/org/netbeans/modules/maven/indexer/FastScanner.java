/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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
 *
 * @author Tim Boudreau
 */
public class FastScanner
        implements Scanner {

    private final ArtifactContextProducer artifactContextProducer;
    private static final Logger LOG = Logger.getLogger(ClassDependencyIndexCreator.class.getName());

    @Inject
    public FastScanner(ArtifactContextProducer artifactContextProducer) {
        this.artifactContextProducer = artifactContextProducer;
    }

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
            private Set<Path> poms = new HashSet<>();
            private Set<Path> artifacts = new HashSet<>();

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
                } else if (nm.endsWith(".jar") || nm.endsWith(".nbm") || nm.endsWith(".txt") || nm.endsWith(".sha1") || nm.endsWith(".xml")) {
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
                // Ensure JARs are proessed before POMs - see comments on
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
