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
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeScanner;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.CouplingAbort;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.MissingPlatformError;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.annotation.processing.Processor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
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
import org.netbeans.modules.java.source.nbjavac.parsing.TreeLoader;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Dusan Balek
 */
final class MultiPassCompileWorker extends CompileWorker {

    private static final int MEMORY_LOW = 1;
    private static final int ERR = 2;
    private boolean checkForMemLow = true;

    @Override
    protected ParsingOutput compile(
            final ParsingOutput previous,
            final Context context,
            final JavaParsingContext javaContext,
            final Collection<? extends CompileTuple> files) {
        final LinkedList<CompileTuple> toProcess = new LinkedList<>();
        final HashMap<JavaFileObject, CompileTuple> jfo2tuples = new HashMap<>();
        final ModuleName moduleName = new ModuleName(javaContext.getModuleName());
        for (CompileTuple i : files) {
            if (!previous.finishedFiles.contains(i.indexable)) {
                toProcess.add(i);
                jfo2tuples.put(i.jfo, i);
            }
        }
        if (toProcess.isEmpty()) {
            return ParsingOutput.success(
                    moduleName.name,
                    previous.file2FQNs,
                    previous.addedTypes,
                    previous.addedModules,
                    previous.createdFiles,
                    previous.finishedFiles,
                    previous.modifiedTypes,
                    previous.aptGenerated);
        }
        final ClassNamesForFileOraculumImpl cnffOraculum = new ClassNamesForFileOraculumImpl(previous.file2FQNs);
        final DiagnosticListenerImpl diagnosticListener = new DiagnosticListenerImpl();
        final LinkedList<CompileTuple> bigFiles = new LinkedList<CompileTuple>();
        JavacTaskImpl jt = null;
        CompileTuple active = null;
        boolean aptEnabled = false;
        int state = 0;
        boolean isBigFile = false;
        boolean[] flm = null;        
        while (!toProcess.isEmpty() || !bigFiles.isEmpty() || active != null) {
            if (context.isCancelled()) {
                return null;
            }
            try {
                context.getSuspendStatus().parkWhileSuspended();
            } catch (InterruptedException ex) {
                //NOP - safe to ignore
            }
            try {
                try {
                    if (isLowMemory(flm)) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        if ((state & MEMORY_LOW) != 0) {
                            break;
                        } else {
                            state |= MEMORY_LOW;
                        }
                        freeMemory(true);
                        continue;
                    }
                    if (active == null) {
                        if (!toProcess.isEmpty()) {
                            active = toProcess.removeFirst();
                            if (active == null || previous.finishedFiles.contains(active.indexable))
                                continue;
                            isBigFile = false;
                        } else {
                            active = bigFiles.removeFirst();
                            isBigFile = true;
                            if (flm == null) {
                                flm = new boolean[] {true};
                            }
                        }
                    }
                    if (jt == null) {
                        jt = JavacParser.createJavacTask(
                                javaContext.getClasspathInfo(),
                                diagnosticListener,
                                javaContext.getSourceLevel(),
                                javaContext.getProfile(),
                                javaContext.getFQNs(),
                                new CancelService() {
                                    public @Override boolean isCanceled() {
                                        return context.isCancelled() || (checkForMemLow && isLowMemory(null));
                                    }
                                },
                                active.aptGenerated ? null : APTUtils.get(context.getRoot()),
                                CompilerOptionsQuery.getOptions(context.getRoot()),
                                Collections.emptyList());
                        jt.getContext().put(ClassNamesForFileOraculum.class, cnffOraculum);
                        Iterable<? extends Processor> processors = jt.getProcessors();
                        aptEnabled = processors != null && processors.iterator().hasNext();
                        if (JavaIndex.LOG.isLoggable(Level.FINER)) {
                            JavaIndex.LOG.finer("Created new JavacTask for: " + FileUtil.getFileDisplayName(context.getRoot()) + " " + javaContext.getClasspathInfo().toString()); //NOI18N
                        }
                    }
                    Iterable<? extends CompilationUnitTree> trees = jt.parse(new JavaFileObject[]{active.jfo});
                    if (isLowMemory(flm)) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        trees = null;
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
                        }
                        freeMemory(true);
                        continue;
                    }
                    Iterable<? extends Element> types;
                    types = jt.enterTrees(trees);
                    if (jfo2tuples.remove(active.jfo) != null) {
                        final Types ts = Types.instance(jt.getContext());
                        final Indexable activeIndexable = active.indexable;
                        class ScanNested extends TreeScanner {
                            Set<CompileTuple> dependencies = new LinkedHashSet<CompileTuple>();
                            @Override
                            public void visitClassDef(JCClassDecl node) {
                                if (node.sym != null) {
                                    Type st = ts.supertype(node.sym.type);
                                    boolean envForSuperTypeFound = false;
                                    while (!envForSuperTypeFound && st != null && st.hasTag(TypeTag.CLASS)) {
                                        ClassSymbol c = st.tsym.outermostClass();
                                        CompileTuple u = jfo2tuples.get(c.sourcefile);
                                        if (u != null && !previous.finishedFiles.contains(u.indexable) && !u.indexable.equals(activeIndexable)) {
                                            dependencies.add(u);
                                            envForSuperTypeFound = true;
                                        }
                                        st = ts.supertype(st);
                                    }
                                }
                                super.visitClassDef(node);
                            }
                        }
                        ScanNested scanner = new ScanNested();
                        for (CompilationUnitTree cut : trees) {
                            scanner.scan((JCCompilationUnit)cut);
                        }
                        if (!scanner.dependencies.isEmpty()) {
                            toProcess.addFirst(active);
                            for (CompileTuple tuple : scanner.dependencies) {
                                toProcess.addFirst(tuple);
                            }
                            active = null;
                            continue;
                        }
                    }
                    if (isLowMemory(flm)) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        trees = null;
                        types = null;
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
                        }
                        freeMemory(true);
                        continue;
                    }
                    jt.analyze(types);
                    if (aptEnabled) {
                        JavaCustomIndexer.addAptGenerated(context, javaContext, active, previous.aptGenerated);
                    }
                    if (isLowMemory(flm)) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        trees = null;
                        types = null;
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
                        }
                        freeMemory(true);
                        continue;
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
                        javaContext.analyze(trees, jt, active, previous.addedTypes, previous.addedModules, main);
                    } else {
                        final Set<ElementHandle<TypeElement>> aTypes = new HashSet<>();
                        javaContext.analyze(trees, jt, active, aTypes, previous.addedModules, main);
                        previous.addedTypes.addAll(aTypes);
                        previous.modifiedTypes.addAll(aTypes);
                    }
                    ExecutableFilesIndex.DEFAULT.setMainClass(context.getRoot().toURL(), active.indexable.getURL(), main[0]);
                    JavaCustomIndexer.setErrors(context, active, diagnosticListener);
                    Iterable<? extends JavaFileObject> generatedFiles = jt.generate(types);
                    if (!active.virtual) {
                        for (JavaFileObject generated : generatedFiles) {
                            if (generated instanceof FileObjects.FileBase) {
                                previous.createdFiles.add(((FileObjects.FileBase) generated).getFile());
                            } else {
                                // presumably should not happen
                            }
                        }
                    }
                    if (!moduleName.assigned) {
                        ModuleElement module = trees.iterator().hasNext() ?
                            ((JCTree.JCCompilationUnit)trees.iterator().next()).modle :
                            null;
                        if (module == null) {
                            module = module = Modules.instance(jt.getContext()).getDefaultModule();
                        }
                        moduleName.name = module == null || module.isUnnamed() ?
                                null :
                                module.getQualifiedName().toString();
                        moduleName.assigned = true;
                    }
                    Log.instance(jt.getContext()).nerrors = 0;
                    previous.finishedFiles.add(active.indexable);
                    active = null;
                    state  = 0;
                } catch (CancelAbort ca) {
                    if (isLowMemory(flm)) {
                        dumpSymFiles(jt, previous.createdFiles, context);
                        jt = null;
                        diagnosticListener.cleanDiagnostics();
                        if ((state & MEMORY_LOW) != 0) {
                            if (isBigFile) {
                                break;
                            } else {
                                bigFiles.add(active);
                                active = null;
                                state &= ~MEMORY_LOW;
                            }
                        } else {
                            state |= MEMORY_LOW;
                        }
                        freeMemory(true);
                    } else if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                        JavaIndex.LOG.log(Level.FINEST, "OnePassCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
                    }
                }
            } catch (CouplingAbort ca) {
                //Coupling error
                TreeLoader.dumpCouplingAbort(ca, null);
                jt = null;
                diagnosticListener.cleanDiagnostics();
                if ((state & ERR) != 0) {
                    //When a javac failed with the Exception mark a file
                    //causing this exceptin as compiled
                    if (active != null)
                        previous.finishedFiles.add(active.indexable);
                    active = null;
                    state = 0;
                } else {
                    state |= ERR;
                }
            } catch (OutputFileManager.InvalidSourcePath isp) {
                //Deleted project - log & ignore
                if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.FINEST, message, isp);
                }
                return ParsingOutput.failure(moduleName.name, previous.file2FQNs,
                        previous.addedTypes, previous.addedModules, previous.createdFiles, previous.finishedFiles,
                        previous.modifiedTypes, previous.aptGenerated);
            } catch (MissingPlatformError mpe) {
                //No platform - log & mark files as errornous
                if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                active.jfo.toUri().toString(),
                                FileUtil.getFileDisplayName(context.getRoot()),
                                bootPath == null   ? null : bootPath.toString(),
                                classPath == null  ? null : classPath.toString(),
                                sourcePath == null ? null : sourcePath.toString()
                                );
                    JavaIndex.LOG.log(Level.FINEST, message, mpe);
                }
                JavaCustomIndexer.brokenPlatform(context, files, mpe.getDiagnostic());
                return ParsingOutput.failure(moduleName.name, previous.file2FQNs,
                        previous.addedTypes, previous.addedModules, previous.createdFiles, previous.finishedFiles,
                        previous.modifiedTypes, previous.aptGenerated);
            } catch (Throwable t) {
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                }
                else {
                    Level level = t instanceof FatalError ? Level.FINEST : Level.WARNING;
                    if (JavaIndex.LOG.isLoggable(level)) {
                        final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                        final String message = String.format("MultiPassCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                    active == null ? null : active.jfo.toUri().toString(),
                                    FileUtil.getFileDisplayName(context.getRoot()),
                                    bootPath == null ? null : bootPath.toString(),
                                    classPath == null ? null : classPath.toString(),
                                    sourcePath == null ? null : sourcePath.toString()
                                    );
                        JavaIndex.LOG.log(level, message, t);  //NOI18N
                    }
                    jt = null;
                    diagnosticListener.cleanDiagnostics();
                    if ((state & ERR) != 0) {
                        //When a javac failed with the Exception mark a file
                        //causing this exceptin as compiled
                        if (active != null)
                            previous.finishedFiles.add(active.indexable);
                        active = null;
                        state = 0;
                    } else {
                        state |= ERR;
                    }
                }
            }
        }
        return (state & MEMORY_LOW) == 0?
            ParsingOutput.success(moduleName.name, previous.file2FQNs,
                    previous.addedTypes, previous.addedModules, previous.createdFiles, previous.finishedFiles,
                    previous.modifiedTypes, previous.aptGenerated):
            ParsingOutput.lowMemory(moduleName.name, previous.file2FQNs,
                    previous.addedTypes, previous.addedModules, previous.createdFiles, previous.finishedFiles,
                    previous.modifiedTypes, previous.aptGenerated);
    }

    private void dumpSymFiles(
            final JavacTaskImpl jti,
            final Set<File> alreadyCreated,
            final Context ctx) throws IOException {
        if (jti != null) {
            final JavaFileManager jfm = jti.getContext().get(JavaFileManager.class);
            checkForMemLow = false;
            try {
                final Types types = Types.instance(jti.getContext());
                final Enter enter = Enter.instance(jti.getContext());
                final Symtab syms = Symtab.instance(jti.getContext());
                final HashMap<ClassSymbol, JCClassDecl> syms2trees = new HashMap<>();
                class ScanNested extends TreeScanner {
                    private Env<AttrContext> env;
                    private Set<Env<AttrContext>> checked = new HashSet<Env<AttrContext>>();
                    private List<Env<AttrContext>> dependencies = new LinkedList<Env<AttrContext>>();
                    public ScanNested(Env<AttrContext> env) {
                        this.env = env;
                    }
                    @Override
                    public void visitClassDef(JCClassDecl node) {
                        if (node.sym != null) {
                            Type st = types.supertype(node.sym.type);
                            boolean envForSuperTypeFound = false;
                            while (!envForSuperTypeFound && st != null && st.hasTag(TypeTag.CLASS)) {
                                ClassSymbol c = st.tsym.outermostClass();
                                Env<AttrContext> stEnv = enter.getEnv(c);
                                if (stEnv != null && env != stEnv) {
                                    if (checked.add(stEnv)) {
                                        scan(stEnv.tree);
                                        if (TreeLoader.pruneTree(stEnv.tree, syms, syms2trees))
                                            dependencies.add(stEnv);
                                    }
                                    envForSuperTypeFound = true;
                                }
                                st = types.supertype(st);
                            }
                        }
                        super.visitClassDef(node);
                    }
                }
                final Set<Env<AttrContext>> processedEnvs = new HashSet<Env<AttrContext>>();
                File classes = JavaIndex.getClassFolder(ctx);
                for (Env<AttrContext> env : jti.getTodo()) {
                    if (processedEnvs.add(env)) {
                        ScanNested scanner = new ScanNested(env);
                        scanner.scan(env.tree);
                        for (Env<AttrContext> dep: scanner.dependencies) {
                            if (processedEnvs.add(dep)) {
                                dumpSymFile(jfm, jti, dep.enclClass.sym, alreadyCreated, classes, syms2trees);
                            }
                        }
                        if (TreeLoader.pruneTree(env.tree, syms, syms2trees))
                            dumpSymFile(jfm, jti, env.enclClass.sym, alreadyCreated, classes, syms2trees);
                    }
                }
            } finally {
                checkForMemLow = true;
            }
        }
    }
    
    private void dumpSymFile(
            @NonNull final JavaFileManager jfm,
            @NonNull final JavacTaskImpl jti,
            @NullAllowed final ClassSymbol cs,
            @NonNull final Set<File> alreadyCreated,
            @NonNull final File classes,
            @NonNull final HashMap<ClassSymbol, JCClassDecl> syms2trees) throws IOException {
        if (cs == null) {
            //ClassDecl has no symbol because compilation was cancelled
            //by low memory before ENTER done.
            return;
        }        
        JavaFileObject file = jfm.getJavaFileForOutput(StandardLocation.CLASS_OUTPUT,
                cs.flatname.toString(), JavaFileObject.Kind.CLASS, cs.sourcefile);
        if (file instanceof FileObjects.FileBase && !alreadyCreated.contains(((FileObjects.FileBase)file).getFile())) {
            TreeLoader.dumpSymFile(jfm, jti, cs, classes, syms2trees);
        }
    }
}
