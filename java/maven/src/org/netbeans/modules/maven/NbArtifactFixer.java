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

package org.netbeans.modules.maven;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.ArtifactFixer;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.PlaceholderCapture;
import org.netbeans.modules.maven.queries.MavenFileOwnerQueryImpl;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.eclipse.aether.artifact.Artifact;

/**
 * #189442: tries to associate (usually snapshot) artifacts with their owners.
 */
@ServiceProvider(service=ArtifactFixer.class)
public class NbArtifactFixer implements ArtifactFixer {

    private static final Logger LOG = Logger.getLogger(NbArtifactFixer.class.getName());

    private final ThreadLocal<Set<String>> gav = new ThreadLocal<Set<String>>();        //#234586

    @Override
    public File resolve(Artifact artifact) {
        return resolve(artifact, null);
    }

    @Override
    public File resolve(Artifact artifact, PlaceholderCapture capture) {
        if (!artifact.getExtension().equals(NbMavenProject.TYPE_POM)) {
            return null;
        }
        if (!artifact.getClassifier().isEmpty()) {
            return null;
        }
        ArtifactRepository local = EmbedderFactory.getProjectEmbedder().getLocalRepository();
        if (local.getLayout() != null) { // #189807: for unknown reasons, there is no layout when running inside MavenCommandLineExecutor.run
            
            //the special snapshot handling is important in case of SNAPSHOT or x-SNAPSHOT versions, for some reason aether has slightly different
            //handling of baseversion compared to maven artifact. we need to manually set the baseversion here..
            boolean isSnapshot = artifact.isSnapshot();
            DefaultArtifact art = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), null, artifact.getExtension(), artifact.getClassifier(), new DefaultArtifactHandler(artifact.getExtension()));
            if (isSnapshot) {
                art.setBaseVersion(artifact.getBaseVersion());
            }
            String path = local.pathOf(art);
            if (new File(local.getBasedir(), path).exists()) {
                return null; // for now, we prefer the repository version when available
            }
        }
        //#234586
        Set<String> gavSet = gav.get();
        if (gavSet == null) {
            gavSet = new HashSet<String>();
            gav.set(gavSet);
        }
        String id = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" + artifact.getVersion();

        if (!gavSet.contains(id)) {
            try {
                gavSet.add(id); //#234586
                File pom = MavenFileOwnerQueryImpl.getInstance().getOwnerPOM(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
                if (pom != null) {
                    return pom;
                }
            } finally {
                gavSet.remove(id); //#234586
                if (gavSet.isEmpty()) {
                    gav.remove();
                }
            }
        } else {
            LOG.log(Level.INFO, "Cycle in NbArtifactFixer resolution (issue #234586): {0}", Arrays.toString(gavSet.toArray()));
        }
        // NOTE: this catches metadata request for all artifacts not locally cached, but all will be repored as POMs.
        try {
            File f = createFallbackPOM(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion());
            if (capture != null) {
                String c = artifact.getProperty("nbResolvingArtifact.classifier", null);
                String e = artifact.getProperty("nbResolvingArtifact.extension", null);

                if ("".equals(c)) {
                    c = null;
                }
                // If it really resolves a POM dependency, add to missing artifacts.
                if ((c == null && e == null) ||
                    (Objects.equals(c, artifact.getClassifier()) && Objects.equals(e, artifact.getExtension()))) {
                    capture.add(artifact);
                } else {
                    if (local.getLayout() != null) { // #189807: for unknown reasons, there is no layout when running inside MavenCommandLineExecutor.run

                        //the special snapshot handling is important in case of SNAPSHOT or x-SNAPSHOT versions, for some reason aether has slightly different
                        //handling of baseversion compared to maven artifact. we need to manually set the baseversion here..
                        boolean isSnapshot = artifact.isSnapshot();
                        DefaultArtifact art = new DefaultArtifact(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), null, e, c, new DefaultArtifactHandler(e));
                        if (isSnapshot) {
                            art.setBaseVersion(artifact.getBaseVersion());
                        }
                        String path = local.pathOf(art);
                        File af = new File(local.getBasedir(), path);
                        art.setFile(af);
                        org.eclipse.aether.artifact.DefaultArtifact da = new org.eclipse.aether.artifact.DefaultArtifact(
                                art.getGroupId(), art.getArtifactId(), art.getClassifier(), art.getType(), 
                                art.getVersion(), artifact.getProperties(), af);
                        capture.add(da);
                    }
                }
            }
            return f;
        } catch (IOException x) {
            Exceptions.printStackTrace(x);
            return null;
        }
    }

    public static final String FALLBACK_NAME = "F@LLB@CK";
    
    /**
     * method attempting to tell if the given artifact's File is a File coming from this class
     * @param file
     * @return 
     */
    public static boolean isFallbackFile(File file) {
        return file.getName().startsWith("fallback") && file.getName().endsWith("netbeans.pom"); 
    }
    
    private static Map<String,File> fallbackPOMs = new HashMap<String,File>();
    private static synchronized File createFallbackPOM(String groupId, String artifactId, String version) throws IOException {
        String k = groupId + ':' + artifactId + ':' + version;
        File fallbackPOM = fallbackPOMs.get(k);
        if (fallbackPOM == null) {
            fallbackPOM = Files.createTempFile("fallback", ".netbeans.pom").toFile();
            fallbackPOM.deleteOnExit();
            PrintWriter w = new PrintWriter(fallbackPOM);
            try {
                w.println("<project>");
                w.println("<modelVersion>4.0.0</modelVersion>");
                w.println("<groupId>" + groupId + "</groupId>");
                w.println("<artifactId>" + artifactId + "</artifactId>");
                w.println("<packaging>pom</packaging>");
                w.println("<version>" + version + "</version>");
                w.println("<name>" + FALLBACK_NAME + "</name>");
                w.println("</project>");
                w.flush();
            } finally {
                w.close();
            }
            fallbackPOMs.put(k, fallbackPOM);
        }
        return fallbackPOM;
    }

    public interface ExceptionCallable<T, E extends Throwable> {
        public T call() throws E;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void sneakyThrow(Throwable exception) throws T {
        throw (T) exception;
    }        
    
    /**
     * Performs an operation and collects the placeholder (forged) artifacts created during that
     * operation. The invocation can be nested; each invocation gets only the artifacts recorded
     * into its own {@link PlaceholderCapture}. The function passes on all runtime exceptions and
     * checked exception thrown by the operation. The capture must be wired into the operation,
     * see e.g. {@link MavenEmbedder#readProjectWithDependencies(MavenExecutionRequest, boolean, PlaceholderCapture)}.
     *
     * @param <T> value produced by the executed operation
     * @param <E> exception thrown from the operation
     * @param capture the sink the operation records placeholder artifacts into
     * @param code the operation to call and monitor
     * @param collector callback that will get collected artifacts.
     * @return
     * @throws E
     */
    public static <T, E extends Throwable> T collectPlaceholderArtifacts(PlaceholderCapture capture, ExceptionCallable<T, E> code, Consumer<Set<Artifact>> collector) throws E {
        try {
            return code.call();
        } catch (Error | RuntimeException r) {
            throw r;
        } catch (Exception ex) {
            sneakyThrow(ex);
            // unreachable
            throw new Error();
        } finally {
            if (collector != null) {
                collector.accept(capture.getArtifacts());
            }
        }
    }
}
