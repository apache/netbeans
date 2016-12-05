/** *****************************************************************************
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ****************************************************************************** */
package org.jetbrains.kotlin.projectsextensions;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jetbrains.kotlin.diagnostics.netbeans.parser.KotlinParser;
import org.jetbrains.kotlin.model.KotlinEnvironment;
import org.jetbrains.kotlin.projectsextensions.j2se.classpath.J2SEExtendedClassPathProvider;
import org.jetbrains.kotlin.project.KotlinSources;
import org.jetbrains.kotlin.projectsextensions.maven.classpath.MavenExtendedClassPath;
import org.jetbrains.kotlin.projectsextensions.maven.classpath.MavenClassPathProviderImpl;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.lang.java.JavaEnvironment;
import org.jetbrains.kotlin.utils.ProjectUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Alexander.Baratynski
 */
public class KotlinProjectHelper {

    public static KotlinProjectHelper INSTANCE = new KotlinProjectHelper();

    private KotlinProjectHelper() {
    }

    private final Map<Project, KotlinSources> kotlinSources = new HashMap<>();
    private final Map<Project, FileObject> lightClassesDirs = new HashMap<>();
    private final Map<Project, ClassPathExtender> extendedClassPaths = new HashMap<>();
    private final Map<Project, ClassPath> fullClasspaths = new HashMap<>();
    private final RequestProcessor environmentLoader
            = new RequestProcessor("Kotlin Environment loader");
    private final Map<Project, Boolean> isScanning = new HashMap<>();
    
    public boolean isScanning(Project project) {
        if (isScanning.get(project) == null) return false;
        
        return isScanning.get(project);
    }
    
    public void postTask(Runnable run) {
        environmentLoader.post(run);
    }

    public void doInitialScan(final Project project) {
        JavaEnvironment.INSTANCE.checkJavaSource(project);
        try {
            JavaEnvironment.INSTANCE.getJAVA_SOURCE().get(project).runWhenScanFinished(
                    new Task<CompilationController>() {
                Runnable run = new Runnable() {
                    @Override
                    public void run() {
                        final ProgressHandle progressBar
                                = ProgressHandleFactory.createHandle("Kotlin files analysis...");
                        progressBar.start();
                        isScanning.put(project, true);
                        
                        for (KtFile ktFile : ProjectUtils.getSourceFiles(project)) {
                            KotlinParser.getAnalysisResult(ktFile, project);
                        }
                        for (FileObject ktFile : new KotlinSources(project).getAllKtFiles()) {
                            IndexingManager.getDefault().refreshAllIndices(ktFile);
                        }

                        isScanning.put(project, false);
                        progressBar.finish();
                    }
                };

                @Override
                public void run(CompilationController parameter) {
                    KotlinProjectHelper.INSTANCE.postTask(run);
                }
            }, true);
        } catch (IOException ex) {
        }
    }

    public boolean checkProject(Project project) {
        String className = project.getClass().getName();
        return className.equals("org.netbeans.modules.java.j2seproject.J2SEProject")
                || className.equals("org.netbeans.modules.maven.NbMavenProjectImpl");
    }

    public boolean isMavenProject(Project project) {
        return project.getClass().getName().equals("org.netbeans.modules.maven.NbMavenProjectImpl");
    }

    public void removeProjectCache(Project project) {
        kotlinSources.remove(project);
        lightClassesDirs.remove(project);
        extendedClassPaths.remove(project);
        fullClasspaths.remove(project);
    }

    public KotlinSources getKotlinSources(Project project) {
        Project p = project;

        if (!(checkProject(p))) {
            return null;
        }

        if (!kotlinSources.containsKey(p)) {
            kotlinSources.put(p, new KotlinSources(p));
        }

        return kotlinSources.get(p);
    }

    public FileObject getLightClassesDirectory(Project project) {
        Project p = project;

        if (!(checkProject(p))) {
            return null;
        }

        if (!(lightClassesDirs.containsKey(p))) {
            lightClassesDirs.put(p, setLightClassesDir(p));
        }

        return lightClassesDirs.get(p);
    }

    private FileObject setLightClassesDir(Project project) {
        if (Places.getUserDirectory() == null) {
            return project.getProjectDirectory();
        }
        FileObject userDirectory = FileUtil.toFileObject(Places.getUserDirectory());
        String projectName = project.getProjectDirectory().getName();
        if (userDirectory.getFileObject(projectName) == null) {
            try {
                userDirectory.createFolder(projectName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return userDirectory.getFileObject(projectName);
    }

    public ClassPathExtender getExtendedClassPath(Project project) {
        Project p = project;

        if (!(checkProject(p))) {
            return null;
        }

        if (!extendedClassPaths.containsKey(p)) {
            if (project.getClass().getName().
                    equals("org.netbeans.modules.java.j2seproject.J2SEProject")) {
                extendedClassPaths.put(p, new J2SEExtendedClassPathProvider(p));
            }
            if (project.getClass().getName().
                    equals("org.netbeans.modules.maven.NbMavenProjectImpl")) {
                extendedClassPaths.put(p, new MavenExtendedClassPath(p));

            }
        }

        return extendedClassPaths.get(p);
    }

    public ClassPath getFullClassPath(Project project) {
        if (!fullClasspaths.containsKey(project)) {
            ClassPathExtender classpath = getExtendedClassPath(project);

            ClassPath boot = classpath.getProjectSourcesClassPath(ClassPath.BOOT);
            ClassPath compile = classpath.getProjectSourcesClassPath(ClassPath.COMPILE);
            ClassPath source = classpath.getProjectSourcesClassPath(ClassPath.SOURCE);

            ClassPath proxy = ClassPathSupport.createProxyClassPath(boot, compile, source);
            fullClasspaths.put(project, proxy);
        }

        return fullClasspaths.get(project);
    }

    private void updateFullClassPath(Project project) {
        ClassPathExtender classpath = getExtendedClassPath(project);

        ClassPath boot = classpath.getProjectSourcesClassPath(ClassPath.BOOT);
        ClassPath compile = classpath.getProjectSourcesClassPath(ClassPath.COMPILE);
        ClassPath source = classpath.getProjectSourcesClassPath(ClassPath.SOURCE);

        ClassPath proxy = ClassPathSupport.createProxyClassPath(boot, compile, source);
        fullClasspaths.put(project, proxy);
    }

    public void updateExtendedClassPath(Project project) {
        Project p = project;
        if (project.getClass().getName().
                equals("org.netbeans.modules.java.j2seproject.J2SEProject")) {
            extendedClassPaths.put(p, new J2SEExtendedClassPathProvider(p));
        }
        if (project.getClass().getName().
                equals("org.netbeans.modules.maven.NbMavenProjectImpl")) {
            MavenClassPathProviderImpl impl = p.getLookup().lookup(MavenClassPathProviderImpl.class);
            if (impl != null) {
                impl.updateClassPath();
            }
            extendedClassPaths.put(p, new MavenExtendedClassPath(p));
        }
        updateFullClassPath(project);
        JavaEnvironment.INSTANCE.updateClasspathInfo(p);
        KotlinEnvironment.Companion.updateKotlinEnvironment(project);
    }

}
