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
package org.netbeans.modules.maven;

import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.lang.model.element.ModuleElement;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.TreeMaker;
import org.netbeans.api.java.source.WorkingCopy;
import static org.netbeans.modules.maven.classpath.ClassPathProviderImpl.MODULE_INFO_JAVA;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tomas Stupka
 */
public class ModuleInfoSupport {

    private static final Logger LOG = Logger.getLogger(ModuleInfoSupport.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(ModuleInfoSupport.class.getName());
        
    private final DependencyType type;
    private final NbMavenProjectImpl project;
    private FileObject moduleInfo;
    
    private final Set<String> declaredModules = new HashSet<>();
    
    private final FileChangeAdapter moduleInfoListener = new FileChangeAdapter() {        
        @Override public void fileDataCreated(FileEvent fe) {
            if(MODULE_INFO_JAVA.equals(fe.getFile().getNameExt())) {                    
                moduleInfoChange();
            }
        }
        @Override public void fileChanged(FileEvent fe) {
            if(MODULE_INFO_JAVA.equals(fe.getFile().getNameExt())) {
                moduleInfoChange();
            }
        }
        @Override public void fileDeleted(FileEvent fe) {
            if(MODULE_INFO_JAVA.equals(fe.getFile().getNameExt())) {
                moduleInfoChange();
            }
        }
        @Override public void fileRenamed(FileRenameEvent fe) {
            if(MODULE_INFO_JAVA.equals(fe.getFile().getNameExt()) || MODULE_INFO_JAVA.equals(fe.getName() + "." + fe.getExt())) {
                moduleInfoChange();
            }
        }
    };
    
    public ModuleInfoSupport(NbMavenProjectImpl project, DependencyType type) {
        this.project = project;
        this.type = type;        
        
        Collection<String> roots = getRoots(project.getOriginalMavenProject(), type);
        for (String root : roots) {
            FileUtil.addFileChangeListener(moduleInfoListener, new File(root));
        }        
        moduleInfo = getModuleInfo(roots);
        if(moduleInfo != null) {
            populateDeclaredModules(moduleInfo);
        }
    }

    private static Collection<String> getRoots(MavenProject mp, DependencyType type) {
        final Collection<String> roots = type == DependencyType.TEST ? mp.getTestCompileSourceRoots() : mp.getCompileSourceRoots();
        return roots;
    }

    private synchronized void moduleInfoChange() {
        moduleInfo = getModuleInfo();
        populateDeclaredModules(moduleInfo);
    }
    
    private synchronized void populateDeclaredModules(final FileObject moduleInfo) {        
        declaredModules.clear();
        if(moduleInfo != null) {
            Set<String> dm = getDeclaredModules(moduleInfo);
            if(dm != null) {
                declaredModules.addAll(dm);
            }
        } 
    }

    public synchronized boolean canAddToModuleInfo(String name) {
        if(moduleInfo == null || !moduleInfo.isValid()) {
            return false;
        }
        return declaredModules != null ? !declaredModules.contains(name) : true;
    }
    
    private FileObject getModuleInfo() {
        MavenProject mp = project.getOriginalMavenProject();
        Collection<String> roots = type == DependencyType.TEST ? mp.getTestCompileSourceRoots() : mp.getCompileSourceRoots();
        return getModuleInfo(roots);
    }
    
    static FileObject getModuleInfo(Collection<String> sourceRoots) {
        // XXX cash me ?
        FileObject moduleInfo = null;
        for (String sourceRoot : sourceRoots) {
            File file = new File(sourceRoot, MODULE_INFO_JAVA);
            moduleInfo = FileUtil.toFileObject(file);
            if(moduleInfo != null) {
                break;
            }
        }
        return moduleInfo;
    }
    
    static Set<String> getDeclaredModules(NbMavenProjectImpl project) {
        FileObject moduleInfo = getModuleInfo(project.getOriginalMavenProject().getCompileSourceRoots());
        return moduleInfo != null ? getDeclaredModules(moduleInfo) : null;
    }
    
    static Set<String> getDeclaredModules(FileObject moduleInfo) {
        JavaSource src = moduleInfo != null ? JavaSource.forFileObject(moduleInfo) : null;
        if (src == null) {
            return null;
        }    
        return getDeclaredModules(src);
    }
    
    private static Set<String> getDeclaredModules(final JavaSource src) {
        Set<String> declaredModuleNames = new HashSet<>();
        try {
            src.runUserActionTask((cc)-> {
                cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                final List<? extends Tree> decls = cc.getCompilationUnit().getTypeDecls();
                final ModuleElement me =  !decls.isEmpty() && decls.get(0).getKind() == Tree.Kind.MODULE ?
                        (ModuleElement) cc.getTrees().getElement(TreePath.getPath(cc.getCompilationUnit(), decls.get(0))) :
                        null;
                if (me != null) {
                    for (ModuleElement.Directive d : me.getDirectives()) {
                        if (d.getKind() == ModuleElement.DirectiveKind.REQUIRES) {
                            final ModuleElement.RequiresDirective reqD = (ModuleElement.RequiresDirective) d;
                            final String name = reqD.getDependency().getQualifiedName().toString();
                            declaredModuleNames.add(name);
                        }
                    }
                }
            }, true);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
        }
        log("Declared modules:", declaredModuleNames); // NOI18N
        return declaredModuleNames;
    }
        
    public static void addRequires(MavenProject mp, Collection<? extends Artifact> artifacts) {
        artifacts.stream().collect(Collectors.groupingBy(DependencyType::forArtifact))
                          .entrySet().forEach((e) -> addRequires(getModuleInfo(getRoots(mp, e.getKey())), e.getValue()));
    }
    
    private static void addRequires(FileObject moduleInfo, Collection<? extends Artifact> artifacts) {
        RP.post(() -> {
            if(moduleInfo != null) {
                Set<String> declaredModules = getDeclaredModules(moduleInfo);
                List<String> newModules = new LinkedList<>();
                for (Artifact a : artifacts) {
                    URL url = FileUtil.urlForArchiveOrDir(a.getFile());
                    String name = url != null ? SourceUtils.getModuleName(url) : null;
                    LOG.log(Level.FINE, "Artifact {0} has modules name ''{1}''", new Object[]{url, name}); // NOI18N
                    if(name != null) {
                        if(!declaredModules.contains(name)) {
                            newModules.add(name);
                        }
                    } else {
                        LOG.log(Level.WARNING, "Could not determine module name for artifact {0}", new Object[]{url}); // NOI18N
                    }
                }
                if(!newModules.isEmpty()) {
                    ModuleInfoSupport.addRequires(moduleInfo, newModules);
                }
            }                
        });
    }
    
    private static void addRequires(FileObject moduleInfo, List<String> newModules) {
        final JavaSource src = JavaSource.forFileObject(moduleInfo);
        if (src == null) {
            return;
        }

        Set<String> declaredModuleNames = getDeclaredModules(src);
        Set<String> requiredModuleNames = new LinkedHashSet<>();
        for (String  name : newModules) {
            if (name != null && !declaredModuleNames.contains(name)) {
                requiredModuleNames.add(name);
            }
        }

        log("To be addded modules:", requiredModuleNames); // NOI18N
        if (!requiredModuleNames.isEmpty()) {
            final Set<String> mNames = requiredModuleNames;
            try {
                src.runModificationTask((WorkingCopy copy) -> {
                    copy.toPhase(JavaSource.Phase.RESOLVED);
                    TreeMaker tm = copy.getTreeMaker();
                    ModuleTree modle = (ModuleTree) copy.getCompilationUnit().getTypeDecls().get(0);
                    ModuleTree newModle = modle;
                    for (String mName : mNames) {
                        newModle = tm.addModuleDirective(newModle, tm.Requires(false, false, tm.QualIdent(mName)));
                    }
                    copy.rewrite(modle, newModle);
                }).commit();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
        }
    }

    static void log(String prefix, Collection cls) {                        
        if(LOG.isLoggable(Level.FINE)) {
            LOG.log(Level.FINE, prefix);  
            if(cls.isEmpty()) {
                LOG.log(Level.FINE, " EMPTY"); // NOI18N
            } else {
                for (Object o : cls) {
                    LOG.log(Level.FINE, " {0}", o.toString()); // NOI18N
                }                                
            }
        }
    }    
}
