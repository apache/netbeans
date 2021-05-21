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
import com.sun.tools.javac.api.ClassNamesForFileOraculum;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
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
import org.openide.util.Pair;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
final class OnePassCompileWorker extends CompileWorker {

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
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(file2FQNs);

        final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
        final HashMap<JavaFileObject, Pair<CompilationUnitTree, CompileTuple>> jfo2units = new HashMap<JavaFileObject, Pair<CompilationUnitTree, CompileTuple>>();
        LinkedList<Pair<CompilationUnitTree, CompileTuple>> units = new LinkedList<Pair<CompilationUnitTree, CompileTuple>>();
        JavacTaskImpl jt = null;

        boolean nop = true;
        final SuspendStatus suspendStatus = context.getSuspendStatus();
        final SourcePrefetcher sourcePrefetcher = SourcePrefetcher.create(files, suspendStatus);
        try {
            while (sourcePrefetcher.hasNext())  {
                final CompileTuple tuple = sourcePrefetcher.next();
                try {
                    if (tuple != null) {
                        nop = false;
                        if (context.isCancelled()) {
                            return null;
                        }
                        try {
                            if (isLowMemory(null)) {
                                jt = null;
                                units = null;
                                jfo2units.clear();
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
                                jt.getContext().put(ClassNamesForFileOraculum.class, cnffOraculum);
                            }
                            for (CompilationUnitTree cut : jt.parse(tuple.jfo)) { //TODO: should be exactly one
                                if (units != null) {
                                    Pair<CompilationUnitTree, CompileTuple> unit = Pair.<CompilationUnitTree, CompileTuple>of(cut, tuple);
                                    units.add(unit);
                                    jfo2units.put(tuple.jfo, unit);
                                }
                                computeFQNs(file2FQNs, cut, tuple);
                            }
                            Log.instance(jt.getContext()).nerrors = 0;
                        } catch (CancelAbort ca) {
                            if (context.isCancelled() && JavaIndex.LOG.isLoggable(Level.FINEST)) {
                                JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
                            }
                        } catch (Throwable t) {
                            if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                                final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
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
                } finally {
                    sourcePrefetcher.remove();
                }
            }
        } finally {
            try {
                sourcePrefetcher.close();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        final ModuleName moduleName = new ModuleName(javaContext.getModuleName());
        if (nop) {
            return ParsingOutput.success(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }

        if (units == null || JavaCustomIndexer.NO_ONE_PASS_COMPILE_WORKER) {
            return ParsingOutput.failure(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }

        CompileTuple active = null;
        final boolean aptEnabled = Optional.ofNullable(jt)
                .map((jtask) -> jtask.getProcessors())
                .map((it) -> it.iterator().hasNext())
                .orElse(Boolean.FALSE);
        final boolean[] flm = {true};
        try {
            final Queue<Future<Void>> barriers = new ArrayDeque<>();
            while(!units.isEmpty()) {
                if (context.isCancelled()) {
                    return null;
                }
                final Pair<CompilationUnitTree, CompileTuple> unit = units.removeFirst();
                active = unit.second();
                if (finished.contains(active.indexable)) {
                    continue;
                }
                if (isLowMemory(flm)) {
                    return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
                }
                final Iterable<? extends Element> types = jt.enterTrees(Collections.singletonList(unit.first()));
                if (jfo2units.remove(active.jfo) != null) {
                    final Types ts = Types.instance(jt.getContext());
                    final Indexable activeIndexable = active.indexable;
                    class ScanNested extends TreeScanner {
                        Set<Pair<CompilationUnitTree, CompileTuple>> dependencies = new LinkedHashSet<Pair<CompilationUnitTree, CompileTuple>>();
                        @Override
                        public void visitClassDef(JCClassDecl node) {
                            if (node.sym != null) {
                                Type st = ts.supertype(node.sym.type);
                                boolean envForSuperTypeFound = false;
                                while (!envForSuperTypeFound && st != null && st.hasTag(TypeTag.CLASS)) {
                                    ClassSymbol c = st.tsym.outermostClass();
                                    Pair<CompilationUnitTree, CompileTuple> u = jfo2units.remove(c.sourcefile);
                                    if (u != null && !finished.contains(u.second().indexable) && !u.second().indexable.equals(activeIndexable)) {
                                        if (dependencies.add(u)) {
                                            scan((JCCompilationUnit)u.first());
                                        }
                                        envForSuperTypeFound = true;
                                    }
                                    st = ts.supertype(st);
                                }
                            }
                            super.visitClassDef(node);
                        }
                    }
                    ScanNested scanner = new ScanNested();
                    scanner.scan((JCCompilationUnit)unit.first());
                    if (!scanner.dependencies.isEmpty()) {
                        units.addFirst(unit);
                        for (Pair<CompilationUnitTree, CompileTuple> pair : scanner.dependencies) {
                            units.addFirst(pair);
                        }
                        continue;
                    }
                }
                if (isLowMemory(flm)) {
                    return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
                }
                jt.analyze(types);
                if (aptEnabled) {
                    JavaCustomIndexer.addAptGenerated(context, javaContext, active, aptGenerated);
                }
                if (isLowMemory(flm)) {
                    return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
                }
                javaContext.getFQNs().set(types, active.indexable.getURL());
                boolean[] main = new boolean[1];
                if (javaContext.getCheckSums().checkAndSet(
                        active.indexable.getURL(),
                        StreamSupport.stream(types.spliterator(), false)
                                .filter((e) -> e.getKind().isClass() || e.getKind().isInterface())
                                .map ((e) -> (TypeElement)e)
                                .collect(Collectors.toList()),
                        jt.getElements()) || context.isSupplementaryFilesIndexing()) {
                    javaContext.analyze(Collections.singleton(unit.first()), jt, unit.second(), addedTypes, addedModules, main);
                } else {
                    final Set<ElementHandle<TypeElement>> aTypes = new HashSet<>();
                    javaContext.analyze(Collections.singleton(unit.first()), jt, unit.second(), aTypes, addedModules, main);
                    addedTypes.addAll(aTypes);
                    modifiedTypes.addAll(aTypes);
                }
                ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().toURL(), active.indexable.getURL(), main[0]);
                JavaCustomIndexer.setErrors(context, active, dc);
                final boolean virtual = active.virtual;
                final JavacTaskImpl jtFin = jt;
                barriers.offer(FileManagerTransaction.runConcurrent(new FileSystem.AtomicAction(){
                    @Override
                    public void run() throws IOException {
                        Iterable<? extends JavaFileObject> generatedFiles = jtFin.generate(types);
                        if (!virtual) {
                            for (JavaFileObject generated : generatedFiles) {
                                if (generated instanceof FileObjects.FileBase) {
                                    createdFiles.add(((FileObjects.FileBase) generated).getFile());
                                } else {
                                    // presumably should not happen
                                }
                            }
                        }
                        if (!moduleName.assigned) {
                            ModuleElement module = ((JCTree.JCCompilationUnit)unit.first()).modle;
                            if (module == null) {
                                module = Modules.instance(jtFin.getContext()).getDefaultModule();
                            }
                            moduleName.name = module == null || module.isUnnamed() ?
                                null :
                                module.getQualifiedName().toString();
                            moduleName.assigned = true;
                        }
                    }
                }));
                Log.instance(jt.getContext()).nerrors = 0;
                finished.add(active.indexable);
            }
            for (Future<Void> barrier : barriers) {
                barrier.get();
            }
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
                final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            active.jfo.toUri().toString(),
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
                final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            active.jfo.toUri().toString(),
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null   ? null : bootPath.toString(),
                            classPath == null  ? null : classPath.toString(),
                            sourcePath == null ? null : sourcePath.toString()
                            );
                JavaIndex.LOG.log(Level.FINEST, message, mpe);
            }
            JavaCustomIndexer.brokenPlatform(context, files, mpe.getDiagnostic());
        } catch (CancelAbort ca) {
            if (isLowMemory(flm)) {
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            } else if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
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
                    final String message = String.format("OnePassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
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
