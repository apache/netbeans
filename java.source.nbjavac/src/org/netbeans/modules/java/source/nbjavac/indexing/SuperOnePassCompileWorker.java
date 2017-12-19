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

package org.netbeans.modules.java.source.nbjavac.indexing;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.indexing.APTUtils;
import org.netbeans.modules.java.source.indexing.CompileWorker;
import org.netbeans.modules.java.source.indexing.DiagnosticListenerImpl;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.indexing.JavaParsingContext;
import org.netbeans.modules.java.source.indexing.SourcePrefetcher;
import org.netbeans.modules.java.source.nbjavac.parsing.TreeLoader;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
//import org.netbeans.modules.java.source.usages.ClassNamesForFileOraculumImpl;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
final class SuperOnePassCompileWorker extends CompileWorker {

    @Override
    protected ParsingOutput compile(
            final ParsingOutput previous,
            final Context context,
            final JavaParsingContext javaContext,
            final Collection<? extends CompileTuple> files) {
        final Map<JavaFileObject, List<String>> file2FQNs = previous != null ? previous.file2FQNs : new HashMap<>();
        final Set<ElementHandle<TypeElement>> addedTypes = previous != null ? previous.addedTypes : new HashSet<>();
        final Set<ElementHandle<ModuleElement>> addedModules = previous != null ? previous.addedModules : new HashSet<>();
        final Set<File> createdFiles = previous != null ? previous.createdFiles : new HashSet<>();
        final Set<Indexable> finished = previous != null ? previous.finishedFiles : new HashSet<>();
        final Set<ElementHandle<TypeElement>> modifiedTypes = previous != null ? previous.modifiedTypes : new HashSet<>();
        final Set<javax.tools.FileObject> aptGenerated = previous != null ? previous.aptGenerated : new HashSet<>();
//        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(file2FQNs);

        final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
        final LinkedList<CompilationUnitTree> trees = new LinkedList<CompilationUnitTree>();
        Map<CompilationUnitTree, CompileTuple> units = new IdentityHashMap<CompilationUnitTree, CompileTuple>();
        JavacTaskImpl jt = null;

        boolean nop = true;
        final SuspendStatus suspendStatus = context.getSuspendStatus();
        final SourcePrefetcher sourcePrefetcher = SourcePrefetcher.create(files, suspendStatus);
        try {
            final boolean flm[] = {true};
            while (sourcePrefetcher.hasNext())  {
                final CompileTuple tuple = sourcePrefetcher.next();
                try {
                    if (tuple != null) {
                        nop = false;
                        if (context.isCancelled()) {
                            return null;
                        }
                        try {
                            if (isLowMemory(flm)) {
                                jt = null;
                                units = null;
                                trees.clear();
                                dc.cleanDiagnostics();
                                freeMemory(false);
                            }
                            if (jt == null) {
                                jt = JavacParser.createJavacTask(
                                    javaContext.getClasspathInfo(),
                                    dc,
                                    javaContext.getSourceLevel(),
                                    javaContext.getProfile(),
                                    javaContext.getFQNs(),
                                    new CancelService() {
                                        public @Override boolean isCanceled() {
                                            return context.isCancelled() || isLowMemory(null);
                                        }
                                    },
                                    tuple.aptGenerated ? null : APTUtils.get(context.getRoot()),
                                    CompilerOptionsQuery.getOptions(context.getRoot()),
                                    Collections.emptyList());
                            }
                            for (CompilationUnitTree cut : jt.parse(tuple.jfo)) { //TODO: should be exactly one
                                if (units != null) {
                                    trees.add(cut);
                                    units.put(cut, tuple);
                                }
                                computeFQNs(file2FQNs, cut, tuple);
                            }
                            Log.instance(jt.getContext()).nerrors = 0;
                        } catch (CancelAbort ca) {
                            if (context.isCancelled() && JavaIndex.LOG.isLoggable(Level.FINEST)) {
                                JavaIndex.LOG.log(Level.FINEST, "SuperOnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
                            }
                        } catch (Throwable t) {
                            if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                                final String message = String.format("SuperOnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                            tuple.indexable.getURL().toString(),
                                            FileUtil.getFileDisplayName(context.getRoot()),
                                            bootPath == null   ? null : bootPath.toString(),
                                            classPath == null  ? null : classPath.toString(),
                                            sourcePath == null ? null : sourcePath.toString()
                                            );
                                JavaIndex.LOG.log(Level.WARNING, message, t);  //NOI18N
                            }
                            if (t instanceof ThreadDeath) {
                                throw (ThreadDeath) t;
                            } else {
                                jt = null;
                                units = null;
                                dc.cleanDiagnostics();
                                freeMemory(false);
                            }
                        }
                    }
                }  finally {
                    sourcePrefetcher.remove();
                }
            }
        } finally {
            try {
                sourcePrefetcher.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        ModuleName moduleName = new ModuleName(javaContext.getModuleName());
        if (nop) {
            return ParsingOutput.success(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }
        if (jt == null || units == null || JavaCustomIndexer.NO_ONE_PASS_COMPILE_WORKER) {
            return ParsingOutput.failure(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }
        if (context.isCancelled()) {
            return null;
        }
        if (isLowMemory(null)) {
            return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }
        Iterable<? extends Processor> processors = jt.getProcessors();
        boolean aptEnabled = processors != null && processors.iterator().hasNext();
        try {
            final Iterable<? extends Element> types = jt.enter(trees);
            if (context.isCancelled()) {
                return null;
            }
            if (isLowMemory(null)) {
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            }
            final Map<Element, CompileTuple> clazz2Tuple = new IdentityHashMap<Element, CompileTuple>();
            Enter enter = Enter.instance(jt.getContext());
            for (Element type : types) {
                if (type.getKind().isClass() || type.getKind().isInterface() || type.getKind() == ElementKind.MODULE) {
                    Env<AttrContext> typeEnv = enter.getEnv((TypeSymbol) type);
                    if (typeEnv == null) {
                        JavaIndex.LOG.log(Level.FINE, "No Env for: {0}", ((TypeSymbol) type).getQualifiedName());
                        continue;
                    }
                    clazz2Tuple.put(type, units.get(typeEnv.toplevel));
                }
            }
            jt.analyze(types);
            if (context.isCancelled()) {
                return null;
            }
            if (isLowMemory(null)) {
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            }
            for (Entry<CompilationUnitTree, CompileTuple> unit : units.entrySet()) {
                CompileTuple active = unit.getValue();
                if (aptEnabled) {
                    JavaCustomIndexer.addAptGenerated(context, javaContext, active, aptGenerated);
                }
                List<Element> activeTypes = new ArrayList<>();
                if (unit.getValue().jfo.isNameCompatible("package-info", JavaFileObject.Kind.SOURCE)) {
                    final PackageTree pt = unit.getKey().getPackage();
                    if (pt instanceof JCPackageDecl) {                        
                        final Element sym = ((JCPackageDecl)pt).packge;
                        if (sym != null)
                            activeTypes.add(sym);
                    }
                } else {
                    for (Tree tree : unit.getKey().getTypeDecls()) {
                        if (tree instanceof JCTree) {
                            final JCTree jct = (JCTree)tree;
                            if (jct.getTag() == JCTree.Tag.CLASSDEF) {
                                final Element sym = ((JCClassDecl)tree).sym;
                                if (sym != null)
                                    activeTypes.add(sym);
                            } else if (jct.getTag() == JCTree.Tag.MODULEDEF) {
                                final Element sym = ((JCModuleDecl)tree).sym;
                                if (sym != null)
                                    activeTypes.add(sym);
                            }
                        }
                    }
                }
                javaContext.getFQNs().set(activeTypes, active.indexable.getURL());
                boolean[] main = new boolean[1];
                if (javaContext.getCheckSums().checkAndSet(
                        active.indexable.getURL(),
                        activeTypes.stream()                                                
                                .filter((e) -> e.getKind().isClass() || e.getKind().isInterface())
                                .map ((e) -> (TypeElement)e)
                                .collect(Collectors.toList()),
                        jt.getElements()) || context.isSupplementaryFilesIndexing()) {
                    javaContext.analyze(Collections.singleton(unit.getKey()), jt, active, addedTypes, addedModules, main);
                } else {
                    final Set<ElementHandle<TypeElement>> aTypes = new HashSet<>();
                    javaContext.analyze(Collections.singleton(unit.getKey()), jt, active, aTypes, addedModules, main);
                    addedTypes.addAll(aTypes);
                    modifiedTypes.addAll(aTypes);
                }
                ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().toURL(), active.indexable.getURL(), main[0]);
                JavaCustomIndexer.setErrors(context, active, dc);
            }
            if (context.isCancelled()) {
                return null;
            }
            if (isLowMemory(null)) {
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            }
            final JavacTaskImpl jtFin = jt;
            final Future<Void> done = FileManagerTransaction.runConcurrent(new FileSystem.AtomicAction() {
                @Override
                public void run() throws IOException {
                    for (Element type : types) {
                        Iterable<? extends JavaFileObject> generatedFiles = jtFin.generate(Collections.singletonList(type));
                        CompileTuple unit = clazz2Tuple.get(type);
                        if (unit == null || !unit.virtual) {
                            for (JavaFileObject generated : generatedFiles) {
                                if (generated instanceof FileObjects.FileBase) {
                                    createdFiles.add(((FileObjects.FileBase) generated).getFile());
                                } else {
                                    // presumably should not happen
                                }
                            }
                        }
                    }
                    if (!moduleName.assigned) {
                        ModuleElement module = !trees.isEmpty() ?
                            ((JCTree.JCCompilationUnit)trees.getFirst()).modle :
                            null;
                        if (module == null) {
                            module = Modules.instance(jtFin.getContext()).getDefaultModule();
                        }
                        moduleName.name = module == null || module.isUnnamed() ?
                            null :
                            module.getQualifiedName().toString();
                        moduleName.assigned = true;
                    }
                }
            });
            for (Entry<CompilationUnitTree, CompileTuple> unit : units.entrySet()) {
                finished.add(unit.getValue().indexable);
            }
            done.get();
            return ParsingOutput.success(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        } catch (CouplingAbort ca) {
            //Coupling error
            TreeLoader.dumpCouplingAbort(ca, null);
        } catch (OutputFileManager.InvalidSourcePath isp) {
            //Deleted project - log & ignore
            if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                final String message = String.format("SuperOnePassCompileWorker caused an exception\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null   ? null : bootPath.toString(),
                            classPath == null  ? null : classPath.toString(),
                            sourcePath == null ? null : sourcePath.toString()
                            );
                JavaIndex.LOG.log(Level.FINEST, message, isp);
            }
        } catch (MissingPlatformError mpe) {
            //No platform - log & mark files as errornous
            if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                final String message = String.format("SuperOnePassCompileWorker caused an exception\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null   ? null : bootPath.toString(),
                            classPath == null  ? null : classPath.toString(),
                            sourcePath == null ? null : sourcePath.toString()
                            );
                JavaIndex.LOG.log(Level.FINEST, message, mpe);
            }
            JavaCustomIndexer.brokenPlatform(context, files, mpe.getDiagnostic());
        } catch (CancelAbort ca) {
            if (isLowMemory(null)) {
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            } else if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                JavaIndex.LOG.log(Level.FINEST, "SuperOnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
            }
        } catch (Throwable t) {
            if (t instanceof ThreadDeath) {
                throw (ThreadDeath) t;
            } else {
                Level level = t instanceof FatalError ? Level.FINEST : Level.WARNING;
                if (JavaIndex.LOG.isLoggable(level)) {
                    final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("SuperOnePassCompileWorker caused an exception\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(level, message, t);  //NOI18N
                }
            }
        }
        return ParsingOutput.failure(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
    }
}
