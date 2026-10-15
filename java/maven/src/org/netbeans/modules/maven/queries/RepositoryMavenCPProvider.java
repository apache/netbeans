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
package org.netbeans.modules.maven.queries;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.modules.maven.NbMavenProjectFactory;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.embedder.MavenEmbedder;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author mkleint
 */
@ServiceProvider (service=ClassPathProvider.class, position=11)
public class RepositoryMavenCPProvider implements ClassPathProvider {
    private static final Logger LOG = Logger.getLogger(RepositoryMavenCPProvider.class.getName());
    
    private static final int CACHE_MAX_SIZE = Integer.getInteger("RepositoryMavenCPProvider.cacheMaxSize", 10); // NOI18N
    private final Map<FileObject, SoftReference<MavenProject>> cache = new LinkedHashMap<FileObject, SoftReference<MavenProject>>() {
        @Override
        protected boolean removeEldestEntry(Entry<FileObject, SoftReference<MavenProject>> eldest) {
            return size() > CACHE_MAX_SIZE;
        }
    };
    
    @Override
    public ClassPath findClassPath(FileObject file, String type) {
        //#223841 at least one project opened is a stronger condition, embedder gets sometimes reset.
        //once we have the project loaded, not loaded embedder doesn't matter anymore, we have to process.
        // sometimes the embedder is loaded even though a maven project is not yet loaded, it doesn't hurt to proceed then.
        if (!NbMavenProjectFactory.isAtLeastOneMavenProjectAround() && !EmbedderFactory.isProjectEmbedderLoaded()) { 
            return null;
        }
        
        FileObject archive = FileUtil.getArchiveFile(file);
        if (archive != null && archive.getNameExt().endsWith("-sources.jar")) { //first simple check
            File sourceFile = FileUtil.toFile(archive);
            if (sourceFile != null) {
//                String name = jarFile.getName();
                File parent = sourceFile.getParentFile();
                if (parent != null) {
                    File parentParent = parent.getParentFile();
                    if (parentParent != null) {
                        // each repository artifact should have this structure
                        String artifact = parentParent.getName();
                        String version = parent.getName();
                        if (archive.getNameExt().startsWith(artifact + "-" + version)) { //#211158 another heuristic check to avoid calling EmbedderFactory for non- local maven repository artifacts
                            
                            //TODO is there a need for generified extension lookup or is just .jar files ok?
                              //TODO can the .jar extension be hardwired? on CP..
                            File bin = new File(parent, artifact + "-" + version + ".jar"); //NOI18N
                            File pom = new File(parent, artifact + "-" + version + ".pom"); //NOI18N
                            URI localRepo = Utilities.toURI(EmbedderFactory.getProjectEmbedder().getLocalRepositoryFile());
                            URI rel = localRepo.relativize(Utilities.toURI(parentParent.getParentFile()));
                            if (!rel.isAbsolute()) {
                                String groupId = rel.getPath();
                                if (groupId != null && !groupId.equals("")) {
                                    groupId = groupId.replace("/", ".");
                                    if (groupId.endsWith(".")) {
                                        groupId = groupId.substring(0, groupId.length() - 1);
                                    }
                                    if (ClassPath.SOURCE.equals(type)) {
                                        return ClassPathFactory.createClassPath(createSourceCPI(sourceFile));
                                    }
                                    if (ClassPath.BOOT.equals(type)) {
                                        return JavaPlatform.getDefault().getBootstrapLibraries();
                                    }
                                    if (ClassPath.COMPILE.equals(type)) {
                                        // Gradle generated POM files does not contain `compileOnly(...)` dependencies supported by gradle
                                        // and therefore cannot be used to provide complete compile classpath for the given file.
                                        // Better to fallback to DefaultClassPathProvider in such case.
                                        if (!fromGradleMetadata(pom)) {
                                            MavenProject mp = getMavenProject(archive, pom, groupId, artifact, version);
                                            return ClassPathFactory.createClassPath(createCompileCPI(mp, bin));
                                        }
                                    }
                                    if (ClassPath.EXECUTE.equals(type)) {
                                        MavenProject mp = getMavenProject(archive, pom, groupId, artifact, version);
                                        return ClassPathFactory.createClassPath(createExecuteCPI(mp, bin));
                                    }
                                } else {
                                    //some sort of weird groupId?
                                }
                            }
                        }
                            
                    }
                }
            }
            
            
        }
        return null;
    }

    private boolean fromGradleMetadata(File pom) {
        try {
            String content = Files.readString(pom.toPath());
            return content.contains("<!-- do_not_remove: published-with-gradle-metadata -->"); //NOI18N
        } catch (IOException ex) {
            LOG.log(Level.FINER, "Failed to read POM file {0}", new Object[] {pom});
        }
        return false;
    }

    private MavenProject getMavenProject(FileObject archive, File pom, String groupId, String artifact, String version) {
        SoftReference<MavenProject> ref = cache.get(archive);
        MavenProject mp = ref != null ? ref.get() : null;
        if(mp == null) {
            mp = loadMavenProject(pom, groupId, artifact, version);
            cache.put(archive, new SoftReference<>(mp));
        } 
        return mp;
    }
    
    private MavenProject loadMavenProject(File pom, String groupId, String artifactId, String version) {
        MavenEmbedder embedder = EmbedderFactory.getProjectEmbedder();
        Artifact projectArtifact = embedder.createArtifact(groupId, artifactId, version,  "jar");
        try {
            ProjectBuildingRequest dpbr = embedder.createMavenExecutionRequest().getProjectBuildingRequest();
            dpbr.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            
            dpbr.setProcessPlugins(false);
            dpbr.setResolveDependencies(true);
            ArrayList<ArtifactRepository> remoteRepos = new ArrayList<ArtifactRepository>();
//for project embedder doens't matter            
//            remoteRepos = RepositoryPreferences.getInstance().remoteRepositories();
            dpbr.setRemoteRepositories(remoteRepos);
            
            ProjectBuildingResult res = embedder.buildProject(projectArtifact, dpbr);
            if (res.getProject() != null) {
                return res.getProject();
            } else {
                LOG.log(Level.INFO, "No project model from repository for {0}: {1}", new Object[] {projectArtifact, res.getProblems()});
            }
        } catch (ProjectBuildingException ex) {
            LOG.log(Level.FINER, "Failed to load project model from repository for {0}: {1}", new Object[] {projectArtifact, ex});
        } catch (Exception exception) {
            LOG.log(Level.FINER, "Failed to load project model from repository for " + projectArtifact, exception);
        }
        return null;
    }
    
    
    
    private ClassPathImplementation createCompileCPI(MavenProject project, File binary) {
        List<PathResourceImplementation> items = new ArrayList<PathResourceImplementation>();
        //according to jglick this could be posisble to leave out on compilation CP..
        items.add(ClassPathSupport.createResource(FileUtil.urlForArchiveOrDir(binary)));
        if (project != null) {
            for (Artifact s : project.getCompileArtifacts()) {
                File file = s.getFile(); // TODO perhaps needs patching
                if (file == null) continue;
                URL u = FileUtil.urlForArchiveOrDir(file);
                if(u != null) {
                    items.add(ClassPathSupport.createResource(u));
                } else {
                    LOG.log(Level.FINE, "Could not retrieve URL for artifact file {0}", new Object[] {file}); // NOI18N
                }
            }
        }
        return ClassPathSupport.createClassPathImplementation(items);
    } 
    
    private ClassPathImplementation createExecuteCPI(MavenProject project, File binary) {
        List<PathResourceImplementation> items = new ArrayList<PathResourceImplementation>(); 
        items.add(ClassPathSupport.createResource(FileUtil.urlForArchiveOrDir(binary)));
        if (project != null) {
            for (Artifact s : project.getRuntimeArtifacts()) {
                if (s.getFile() == null) continue;
                items.add(ClassPathSupport.createResource(FileUtil.urlForArchiveOrDir(s.getFile())));
            }
        }
        return ClassPathSupport.createClassPathImplementation(items);
    }
    
    private ClassPathImplementation createSourceCPI(File sourceFile) {
        return ClassPathSupport.createClassPathImplementation(Collections.singletonList(ClassPathSupport.createResource(FileUtil.urlForArchiveOrDir(sourceFile))));
    }
}
