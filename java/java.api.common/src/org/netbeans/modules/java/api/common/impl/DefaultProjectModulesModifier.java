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
package org.netbeans.modules.java.api.common.impl;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.RequiresTree;
import org.netbeans.api.java.source.support.ErrorAwareTreeScanner;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.netbeans.api.actions.Savable;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.project.classpath.ProjectModulesModifier;
import org.openide.filesystems.FileObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author sdedic
 */
@ServiceProvider(service = ProjectModulesModifier.class)
public class DefaultProjectModulesModifier implements ProjectModulesModifier {
    @Override
    public String provideModularClasspath(FileObject projectArtifact, String classPathType) {
        assert projectArtifact != null;
        assert classPathType != null;
        
        Project p = FileOwnerQuery.getOwner(projectArtifact);
        if (p == null) {
            return null;
        }
        ProjectModulesModifier delegate = p.getLookup().lookup(ProjectModulesModifier.class);
        if (delegate != null) {
            return delegate.provideModularClasspath(projectArtifact, classPathType);
        }
        if (findModuleInfo(projectArtifact) == null) {
            return null;
        }
        switch (classPathType) {
            case ClassPath.COMPILE:
                return JavaClassPathConstants.MODULE_COMPILE_PATH;
            case ClassPath.EXECUTE:
                return JavaClassPathConstants.MODULE_EXECUTE_PATH;
                
            default:
                return null;
        }
    }

    @Override
    public boolean addRequiredModules(String originalPathType, FileObject projectArtifact, Collection<URL> moduleNames) throws IOException {
        assert projectArtifact != null;
        assert originalPathType != null;
        
        Project p = FileOwnerQuery.getOwner(projectArtifact);
        if (p == null) {
            return false;
        }
        FileObject modInfo = findModuleInfo(projectArtifact);
        if (modInfo == null) {
            return false;
        }
        ProjectModulesModifier delegate = p.getLookup().lookup(ProjectModulesModifier.class);
        if (delegate != null) {
            return delegate.addRequiredModules(originalPathType, projectArtifact, moduleNames);
        }

        switch (originalPathType) {
            case ClassPath.COMPILE:
            case ClassPath.EXECUTE:
            case JavaClassPathConstants.MODULE_COMPILE_PATH:
            case JavaClassPathConstants.MODULE_EXECUTE_PATH:
                return extendModuleInfo(modInfo, moduleNames);
                
            default:
                return false;
        }
    }

    @Override
    public boolean removeRequiredModules(String originalPathType, FileObject projectArtifact, Collection<URL> moduleNames) throws IOException {
        assert projectArtifact != null;
        assert originalPathType != null;
        
        Project p = FileOwnerQuery.getOwner(projectArtifact);
        if (p == null) {
            return false;
        }
        FileObject modInfo = findModuleInfo(projectArtifact);
        if (modInfo == null) {
            return false;
        }
        ProjectModulesModifier delegate = p.getLookup().lookup(ProjectModulesModifier.class);
        if (delegate != null) {
            return delegate.removeRequiredModules(originalPathType, projectArtifact, moduleNames);
        }
        switch (originalPathType) {
            case ClassPath.COMPILE:
            case ClassPath.EXECUTE:
            case JavaClassPathConstants.MODULE_COMPILE_PATH:
            case JavaClassPathConstants.MODULE_EXECUTE_PATH:
                return removeRequiredModuleArtifacts(modInfo, moduleNames);
                
            default:
                return false;
        }
    }
    
    private static final SpecificationVersion JDK9_SPEC = new SpecificationVersion("9");

    @CheckForNull
    private static FileObject findModuleInfo(@NonNull final FileObject artifact) {
        if (JDK9_SPEC.compareTo(new SpecificationVersion(SourceLevelQuery.getSourceLevel(artifact))) < 0) {
            return null;
        }
        ClassPath cp = ClassPath.getClassPath(artifact, ClassPath.SOURCE);
        return cp == null ? null : cp.findResource("module-info.java");   //NOI18N
    }
    
    public static boolean extendModuleInfo(
            @NonNull final FileObject info,
            @NonNull final  Collection<URL> modules) throws IOException {
        if (info == null || modules.isEmpty()) {
            return false;
        }
        final Collection<String> moduleNames = modules.stream()
                .map((url) -> SourceUtils.getModuleName(url, true))
                .filter((name) -> name != null)
                .collect(Collectors.toList());
        return addRequiredModules(info, moduleNames);
    }
    
    public static boolean addRequiredModules(
            @NonNull final FileObject info,
            @NonNull final  Collection<String> moduleNames) throws IOException {
        if (moduleNames.isEmpty()) {
            return false;
        }
        final JavaSource js = JavaSource.forFileObject(info);
        if (js == null) {
            return false;
        }
        boolean[] modified = new boolean[1];
        js.runModificationTask((wc) -> {
            wc.toPhase(JavaSource.Phase.RESOLVED);
            final CompilationUnitTree cu = wc.getCompilationUnit();
            final Set<String> knownModules = new HashSet<>();
            final ModuleTree[] module = new ModuleTree[1];
            final RequiresTree[] lastRequires = new RequiresTree[1];
            cu.accept(new ErrorAwareTreeScanner<Void, Void>() {
                        @Override
                        public Void visitModule(ModuleTree m, Void p) {
                            module[0] = m;
                            return super.visitModule(m, p);
                        }
                        @Override
                        public Void visitRequires(RequiresTree r, Void p) {
                            lastRequires[0] = r;
                            knownModules.add(r.getModuleName().toString());
                            return super.visitRequires(r, p);
                        }
                    },
                    null);
            if (module[0] != null) {
                moduleNames.removeAll(knownModules);
                modified[0] = !moduleNames.isEmpty();
                final TreeMaker tm = wc.getTreeMaker();
                final List<RequiresTree> newRequires = moduleNames.stream()
                        .map((name) -> tm.Requires(false, false, tm.QualIdent(name)))
                        .collect(Collectors.toList());

                final List<DirectiveTree> newDirectives = new ArrayList<>(
                        module[0].getDirectives().size() + newRequires.size());
                if (lastRequires[0] == null) {
                    newDirectives.addAll(newRequires);
                }
                for (DirectiveTree dt : module[0].getDirectives()) {
                    newDirectives.add(dt);
                    if (dt == lastRequires[0]) {
                        newDirectives.addAll(newRequires);
                    }
                }
                final ModuleTree newModule = tm.Module(
                        tm.Modifiers(0, module[0].getAnnotations()),
                        module[0].getModuleType(),
                        module[0].getName(),
                        newDirectives);
                wc.rewrite(module[0], newModule);
            }                    
        })
        .commit();
        save(info);
        return modified[0];
    }
    
    public static boolean removeRequiredModuleArtifacts(FileObject moduleInfo, Collection<URL> modules) {
        return removeRequiredModules(moduleInfo, 
            modules.stream()
                .map((url) -> SourceUtils.getModuleName(url, true))
                .filter((name) -> name != null)
                .collect(Collectors.toSet()
        ));
    }
    
    public static boolean removeRequiredModules(FileObject moduleInfo, Collection<String> modules) {
        if (moduleInfo == null || modules.isEmpty()) {
            return false;
        }
        final boolean[] modified = new boolean[1];
        try {
            final JavaSource js = JavaSource.forFileObject(moduleInfo);
            if (js != null) {
                js.runModificationTask((wc) -> {
                    wc.toPhase(JavaSource.Phase.PARSED);
                    final CompilationUnitTree cu = wc.getCompilationUnit();
                    final Set<DirectiveTree> toRemove = new HashSet<>();
                    final ModuleTree[] module = new ModuleTree[1];
                    cu.accept(
                            new ErrorAwareTreeScanner<Void, Set<DirectiveTree>>() {
                                @Override
                                public Void visitModule(final ModuleTree node, Set<DirectiveTree> param) {
                                    module[0] = node;
                                    return super.visitModule(node, param);
                                }
                                @Override
                                public Void visitRequires(final RequiresTree node, final Set<DirectiveTree> param) {
                                    final String fqn = node.getModuleName().toString();
                                    if (modules.contains(fqn)) {
                                        param.add(node);
                                    }
                                    return super.visitRequires(node, param);
                                }
                            },
                            toRemove);
                    if (!toRemove.isEmpty()) {
                        modified[0] = true;
                        final List<DirectiveTree> newDirectives = new ArrayList<>(module[0].getDirectives().size());
                        for (DirectiveTree dt : module[0].getDirectives()) {
                            if (!toRemove.contains(dt)) {
                                newDirectives.add(dt);
                            }
                        }
                        final ModuleTree newModule = wc.getTreeMaker().Module(
                                wc.getTreeMaker().Modifiers(0, module[0].getAnnotations()),
                                module[0].getModuleType(),
                                module[0].getName(),
                                newDirectives);
                        wc.rewrite(module[0], newModule);
                    }
                }).commit();
                save(moduleInfo);
            }
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return modified[0];
    }
    
    @Override
    public Map<URL, Collection<ClassPath>> findModuleUsages(FileObject projectArtifact, Collection<URL> locations) {
        return doFindModuleUsages(projectArtifact, locations);
    }
    
    public static Map<URL, Collection<ClassPath>> doFindModuleUsages(FileObject projectArtifact, Collection<URL> locations) {
        Project p = FileOwnerQuery.getOwner(projectArtifact);
        if (p == null) {
            return Collections.emptyMap();
        }
        Map<String, URL> modLocations = new HashMap<>();
        for (URL u : locations) {
            String n = SourceUtils.getModuleName(u, true);
            if (n != null) {
                modLocations.put(n, u);
            }
        }
        Map<URL, Collection<ClassPath>> resultMap = new HashMap<>();
        Set seenCP = new HashSet<>();
        for (SourceGroup g : ProjectUtils.getSources(p).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            FileObject r = g.getRootFolder();
            ClassPath src = ClassPath.getClassPath(r, ClassPath.SOURCE);
            if (!seenCP.add(Arrays.asList(src.getRoots()))) {
                continue;
            }
            if (src.findResource("module-info.java") == null) {
                continue;
            }
            JavaSource js = JavaSource.forFileObject(r);
            if (js != null) {
                try {
                    js.runUserActionTask(new S(src, modLocations, resultMap), true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return resultMap;
    }
    
    private static class S extends ErrorAwareTreeScanner implements Task<CompilationController> {
        final Map<URL, Collection<ClassPath>> resultMap;
        final ClassPath g;
        final Map<String, URL> modLocations;

        public S(ClassPath g, Map<String, URL> modLocations, Map<URL, Collection<ClassPath>> resultMap) {
            this.g = g;
            this.modLocations = modLocations;
            this.resultMap = resultMap;
        }
         
        @Override
        public Object visitRequires(RequiresTree node, Object p) {
            String s = node.getModuleName().toString();
            URL u = modLocations.get(s);
            if (u != null) {
                resultMap.computeIfAbsent(u, (x) -> new ArrayList<>()).add(g);
            }
            return null;
        }
        
        @Override
        public void run(CompilationController parameter) throws Exception {
            parameter.toPhase(JavaSource.Phase.PARSED);
            scan(parameter.getCompilationUnit(), null);
        }
    }
    
    private static void save(@NonNull final FileObject file) throws IOException {
        final Savable save = file.getLookup().lookup(Savable.class);
        if (save != null) {
            save.save();
        }
    }
}
