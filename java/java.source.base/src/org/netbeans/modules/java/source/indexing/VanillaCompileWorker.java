/**
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

package org.netbeans.modules.java.source.indexing;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DeconstructionPatternTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.TreeScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.DeferredCompletionFailureHandler;
import com.sun.tools.javac.code.DeferredCompletionFailureHandler.Handler;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds.Kind;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Source.Feature;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.RecordComponent;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.SymbolMetadata;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSwitchExpression;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.tree.TreeMaker;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.lib.nbjavac.services.NBJavaCompiler;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.java.source.util.AbortChecker;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.SuspendStatus;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 * @author Dusan Balek
 */
final class VanillaCompileWorker extends CompileWorker {

    @Override
    @SuppressWarnings("UseSpecificCatch")
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

        final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
        final LinkedList<CompilationUnitTree> trees = new LinkedList<>();
        Map<CompilationUnitTree, CompileTuple> units = new IdentityHashMap<>();
        JavacTaskImpl jt = null;

        boolean nop = true;
        final SuspendStatus suspendStatus = context.getSuspendStatus();
        final SourcePrefetcher sourcePrefetcher = SourcePrefetcher.create(files, suspendStatus);
        Map<JavaFileObject, CompileTuple> fileObjects = new IdentityHashMap<>();
        try {
            while (sourcePrefetcher.hasNext())  {
                final CompileTuple tuple = sourcePrefetcher.next();
                try {
                    if (tuple != null) {
                        nop = false;
                        if (context.isCancelled()) {
                            return null;
                        }
                        fileObjects.put(tuple.jfo, tuple);
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
        try {
            jt = JavacParser.createJavacTask(
                javaContext.getClasspathInfo(),
                dc,
                javaContext.getSourceLevel(),
                javaContext.getProfile(),
                javaContext.getFQNs(),
                new CancelService() {
                    public @Override boolean isCanceled() {
                        return context.isCancelled() || isLowMemory(new boolean[] {true});
                    }
                },
                fileObjects.values().iterator().next().aptGenerated ? null : APTUtils.get(context.getRoot()),
                CompilerOptionsQuery.getOptions(context.getRoot()),
                fileObjects.keySet());
            for (CompilationUnitTree cut : jt.parse()) {
                trees.add(cut);
                CompileTuple tuple = fileObjects.get(cut.getSourceFile());
                units.put(cut, tuple);
                computeFQNs(file2FQNs, cut, tuple);
            }
        } catch (Throwable t) {
            if (AbortChecker.isCancelAbort(t)) {
                if (context.isCancelled() && JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    JavaIndex.LOG.log(Level.FINEST, "VanillaCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), t);  //NOI18N
                }
            } else {
                if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                    final ClassPath bootPath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                    final ClassPath classPath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                    final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                    final String message = String.format("VanillaCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            fileObjects.values().iterator().next().indexable.getURL(),
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null ? null : bootPath.toString(),
                            classPath == null ? null : classPath.toString(),
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
        if (jt == null || units == null || JavaCustomIndexer.NO_ONE_PASS_COMPILE_WORKER) {
            return ParsingOutput.failure(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }
        if (context.isCancelled()) {
            return null;
        }
        if (isLowMemory(new boolean[] {true})) {
            fallbackCopyExistingClassFiles(context, javaContext, files);
            return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }
        boolean aptEnabled = true;
        Log log = Log.instance(jt.getContext());
        JavaCompiler compiler = JavaCompiler.instance(jt.getContext());
        try {
            final Iterable<? extends Element> types = jt.enter(trees);
            if (context.isCancelled()) {
                return null;
            }
            if (isLowMemory(new boolean[] {true})) {
                fallbackCopyExistingClassFiles(context, javaContext, files);
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            }
            final Map<Element, CompileTuple> clazz2Tuple = new IdentityHashMap<>();
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
            if (isLowMemory(new boolean[] {true})) {
                fallbackCopyExistingClassFiles(context, javaContext, files);
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
                dropMethodsAndErrors(jt.getContext(), unit.getKey(), dc);
                JavaCustomIndexer.setErrors(context, active, dc);
            }
            if (context.isCancelled()) {
                return null;
            }
            if (isLowMemory(new boolean[] {true})) {
                fallbackCopyExistingClassFiles(context, javaContext, files);
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            }
            final JavacTaskImpl jtFin = jt;
            ((NBJavaCompiler) NBJavaCompiler.instance(jt.getContext())).setDesugarCallback(env -> {
                if (env == null) {
                    return;
                }
                dropMethodsAndErrors(jtFin.getContext(), env.toplevel, dc);
                log.nerrors = 0;
            });
            final Future<Void> done = FileManagerTransaction.runConcurrent(() -> {
                Modules modules = Modules.instance(jtFin.getContext());
                compiler.shouldStopPolicyIfError = CompileState.FLOW;
                for (Element type : types) {
                    if (isErroneousClass(type)) {
                        //likely a duplicate of another class, don't touch:
                        continue;
                    }
                    TreePath tp = Trees.instance(jtFin).getPath(type);
                    assert tp != null;
                    log.nerrors = 0;
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
                        module = modules.getDefaultModule();
                    }
                    moduleName.name = module == null || module.isUnnamed() ?
                            null :
                            module.getQualifiedName().toString();
                    moduleName.assigned = true;
                }
            });
            for (Entry<CompilationUnitTree, CompileTuple> unit : units.entrySet()) {
                finished.add(unit.getValue().indexable);
            }
            done.get();
            return ParsingOutput.success(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        } catch (OutputFileManager.InvalidSourcePath isp) {
            //Deleted project - log & ignore
            if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                final String message = String.format("VanillaCompileWorker caused an exception\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            FileUtil.getFileDisplayName(context.getRoot()),
                            bootPath == null   ? null : bootPath.toString(),
                            classPath == null  ? null : classPath.toString(),
                            sourcePath == null ? null : sourcePath.toString()
                            );
                JavaIndex.LOG.log(Level.FINEST, message, isp);
            }
        } catch (Throwable t) {
            if (AbortChecker.isCancelAbort(t)) {
                if (isLowMemory(new boolean[]{true})) {
                    fallbackCopyExistingClassFiles(context, javaContext, files);
                    return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
                } else if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                    JavaIndex.LOG.log(Level.FINEST, "VanillaCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), t);  //NOI18N
                }
            } else {
                Exceptions.printStackTrace(t);
                if (t instanceof ThreadDeath) {
                    throw (ThreadDeath) t;
                } else {
                    Level level = t instanceof FatalError ? Level.FINEST : Level.WARNING;
                    if (JavaIndex.LOG.isLoggable(level)) {
                        final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                        final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                        final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                        final String message = String.format("VanillaCompileWorker caused an exception\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                                    FileUtil.getFileDisplayName(context.getRoot()),
                                    bootPath == null   ? null : bootPath.toString(),
                                    classPath == null  ? null : classPath.toString(),
                                    sourcePath == null ? null : sourcePath.toString()
                                    );
                        JavaIndex.LOG.log(level, message, t);  //NOI18N
                    }
                }
            }
        }
        fallbackCopyExistingClassFiles(context, javaContext, files);
        return ParsingOutput.success(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
    }

    private static void fallbackCopyExistingClassFiles(final Context context,
                                                       final JavaParsingContext javaContext,
                                                       final Collection<? extends CompileTuple> files) {
        //fallback: copy output classes to caches, so that editing is not extremely slow/broken:
        BinaryForSourceQuery.Result res2 = BinaryForSourceQuery.findBinaryRoots(context.getRootURI());
        Set<String> filter;
        if (!context.isAllFilesIndexing()) {
            filter = new HashSet<>();
            for (CompileTuple toIndex : files) {
                String path = toIndex.indexable.getRelativePath();
                filter.add(path.substring(0, path.lastIndexOf(".")));
            }
        } else {
            filter = null;
        }
        try {
            final Future<Void> done = FileManagerTransaction.runConcurrent(() -> {
                File cache = JavaIndex.getClassFolder(context.getRootURI(), false, false);
                for (URL u : res2.getRoots()) {
                    FileObject binaryFO = URLMapper.findFileObject(u);
                    if (binaryFO == null)
                        continue;
                    FileManagerTransaction fmtx = TransactionContext.get().get(FileManagerTransaction.class);
                    List<File> copied = new ArrayList<>();
                    copyRecursively(binaryFO, cache, cache, filter, fmtx, copied);
                    final ClassIndexImpl cii = javaContext.getClassIndexImpl();
                    if (cii != null) {
                        cii.getBinaryAnalyser().analyse(context, cache, copied);
                    }
                }
            });
            done.get();
        } catch (IOException | InterruptedException | ExecutionException ex) {
            Exceptions.printStackTrace(ex);
        }
        }
    private static void copyRecursively(FileObject source, File targetRoot, File target, Set<String> filter, FileManagerTransaction fmtx, List<File> copied) throws IOException {
        if (source.isFolder()) {
            if (target.exists() && !target.isDirectory()) {
                throw new IOException("Cannot create folder: " + target.getAbsolutePath() + ", already exists as a file.");
            }

            FileObject[] listed = source.getChildren();

            for (FileObject f : listed) {
                String name = f.getNameExt();
                if (name.endsWith(".class")) {
                    name = name.substring(0, name.length() - FileObjects.CLASS.length()) + FileObjects.SIG;
                    copyRecursively(f, targetRoot, new File(target, name), filter, fmtx, copied);
                }
            }
        } else {
            if (target.isDirectory()) {
                throw new IOException("Cannot create file: " + target.getAbsolutePath() + ", already exists as a folder.");
            }

            boolean copy;

            if (filter != null) {
                String path = FileObjects.getRelativePath(targetRoot, target);
                int dot = path.lastIndexOf('.');
                if (dot != (-1)) path = path.substring(0, dot);
                int dollar = path.indexOf('$');
                if (dollar != (-1)) path = path.substring(0, dollar);
                copy = filter.contains(path);
            } else {
                copy = true;
            }

            if (copy) {
                copyFile(source, fmtx.createFileObject(StandardLocation.CLASS_OUTPUT, target, targetRoot, null, null));
                copied.add(target);
            }
        }
    }

    private static void copyFile(FileObject updatedFile, JavaFileObject target) throws IOException {
        try (InputStream ins = updatedFile.getInputStream();
             OutputStream out = target.openOutputStream()) {
            FileUtil.copy(ins, out);
        }
    }

    private static class HandledUnits {
        public final List<CompilationUnitTree> handled = new ArrayList<>();
    }

    @SuppressWarnings("PackageVisibleField") // Unittests
    static BiConsumer<JavaFileObject, CompilationUnitTree> fixedListener = (file, cut) -> {};

    private void dropMethodsAndErrors(com.sun.tools.javac.util.Context ctx, CompilationUnitTree cut, DiagnosticListenerImpl dc) {
        HandledUnits hu = ctx.get(HandledUnits.class);
        if (hu == null) {
            hu = new HandledUnits();
            ctx.put(HandledUnits.class, hu);
        }
        if (hu.handled.contains(cut)) {
            //already seen
            return ;
        }
        hu.handled.add(cut);
        Names names = Names.instance(ctx);
        Symtab syms = Symtab.instance(ctx);
        Trees trees = Trees.instance(BasicJavacTask.instance(ctx));
        Types types = Types.instance(ctx);
        TreeMaker make = TreeMaker.instance(ctx);
        Elements el = JavacElements.instance(ctx);
        Source source = Source.instance(ctx);
        boolean hasMatchException = el.getTypeElement("java.lang.MatchException") != null;
        DeferredCompletionFailureHandler dcfh = DeferredCompletionFailureHandler.instance(ctx);
        //TODO: should preserve error types!!!
        new TreePathScanner<Void, Void>() {
            private Set<JCNewClass> anonymousClasses = Collections.newSetFromMap(new LinkedHashMap<>());
            private ClassSymbol currentClass = null;
            private TreeMap<Long, List<Diagnostic<? extends JavaFileObject>>> diags;

            @Override
            public Void visitCompilationUnit(CompilationUnitTree node, Void p) {
                diags = dc.peekDiagnostics(cut.getSourceFile())
                          .stream()
                          .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                          .collect(Collectors.toMap(d -> d.getPosition(),
                                                    d -> Collections.singletonList(d),
                                                    (dl1, dl2) -> Stream.of(dl1, dl2)
                                                                        .flatMap(dl -> dl.stream())
                                                                        .collect(Collectors.toList()),
                                                    () -> new TreeMap<>()));
                super.visitCompilationUnit(node, p);
                //TODO: if diagnostics are remaining, make all classes non-usable
                return null;
            }
            
            @Override
            public Void visitVariable(VariableTree node, Void p) {
                JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) node;
                scan(node.getModifiers(), null);
                scan(node.getType(), null);
                scan(node.getNameExpression(), null);
                if (TreeUtilities.CLASS_TREE_KINDS.contains(getCurrentPath().getParentPath().getLeaf().getKind())) {
                    boolean prevErrorFound = errorFound;
                    errorFound = false;
                    scan(node.getInitializer(), null);
                    SortedMap<Long, List<Diagnostic<? extends JavaFileObject>>> blockDiags = treeDiags(node.getInitializer());
                    if (errorFound || !blockDiags.isEmpty()) {
                        JCClassDecl clazz = (JCClassDecl) getCurrentPath().getParentPath().getLeaf();
                        if ((decl.mods.flags & Flags.ENUM) == 0) {
                            decl.init = null;
                        } else {
                            Symbol sym = clazz.sym.members().findFirst(clazz.sym.name.table.names.init);
                            JCNewClass nct = make.NewClass(null, com.sun.tools.javac.util.List.nil(), make.Ident(sym), sym.asType().getParameterTypes().map(this::nullExpression), null);
                            nct.constructor = sym;
                            nct.constructorType = sym.type;
                            decl.init = nct.setType(clazz.type);
                        }
                        clazz.defs = clazz.defs.prepend(make.Block(node.getModifiers().getFlags().contains(Modifier.STATIC) ? Flags.STATIC : 0, com.sun.tools.javac.util.List.of(throwTree(blockDiags))));
                        blockDiags.clear();
                    }
                    errorFound = prevErrorFound;
                } else {
                    scan(node.getInitializer(), null);
                }
                decl.type = error2Object(decl.type);
                decl.sym.type = error2Object(decl.sym.type);
                clearAnnotations(decl.sym.getMetadata());
                return null;
            }

            private JCExpression nullExpression(Type type) {
                if (type.isPrimitive()) {
                    return make.Literal(type.getTag(), 0).setType(syms.booleanType);
                } else {
                    return make.Literal(TypeTag.BOT, null).setType(syms.botType);
                }
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                JCTree.JCMethodDecl decl = (JCTree.JCMethodDecl) node;
                Symbol.MethodSymbol msym = decl.sym;
                super.visitMethod(node, p);
                //TODO: fix up modifiers:
//                if (Collections.disjoint(msym.getModifiers(), EnumSet.of(Modifier.NATIVE, Modifier.ABSTRACT))) {
//                    JCTree.JCNewClass nct =
//                            make.NewClass(null,
//                                          com.sun.tools.javac.util.List.nil(),
//                                          make.QualIdent(syms.runtimeExceptionType.tsym),
//                                          com.sun.tools.javac.util.List.of(make.Literal("")),
//                                          null);
//                    nct.type = syms.runtimeExceptionType;
//                    nct.constructor = syms.runtimeExceptionType.tsym.members().getSymbols(
//                            s -> s.getKind() == ElementKind.CONSTRUCTOR && s.type.getParameterTypes().size() == 1 && s.type.getParameterTypes().head.tsym == syms.stringType.tsym
//                    ).iterator().next();
//                    decl.body = make.Block(0, com.sun.tools.javac.util.List.of(make.Throw(nct)));
//                } else {
//                    decl.body = null;
//                }
                Type.MethodType mt;
                if (msym.type.hasTag(TypeTag.FORALL)) {
                    ForAll fa = (ForAll) msym.type;
                    fa.tvars = error2Object(fa.tvars);
                    mt = fa.asMethodType();
                } else {
                    mt = (Type.MethodType) msym.type;
                }
                clearMethodType(mt);
                if (msym.erasure_field != null && msym.erasure_field.hasTag(TypeTag.METHOD))
                    clearMethodType((Type.MethodType) msym.erasure_field);
                clearAnnotations(decl.sym.getMetadata());
                if (decl.sym.defaultValue != null && isAnnotationErroneous(decl.sym.defaultValue)) {
                    decl.sym.defaultValue = null;
                }
                return null;
            }

            private void clearMethodType(Type.MethodType mt) {
                mt.restype = error2Object(mt.restype);
                mt.argtypes = error2Object(mt.argtypes);
                mt.thrown = error2Object(mt.thrown);
            }

            @Override
            public Void visitBlock(BlockTree node, Void p) {
                boolean prevErrorFound = errorFound;
                errorFound = false;
                super.visitBlock(node, p);
                SortedMap<Long, List<Diagnostic<? extends JavaFileObject>>> blockDiags = treeDiags(node);
                if (errorFound || !blockDiags.isEmpty()) {
                    ((JCBlock) node).stats = com.sun.tools.javac.util.List.of(throwTree(blockDiags));
                    blockDiags.clear();
                }
                errorFound = prevErrorFound;
                return null;
            }

            private JCStatement throwTree(SortedMap<Long, List<Diagnostic<? extends JavaFileObject>>> diags) {
                String message = diags.isEmpty() ? null
                                                 : DIAGNOSTIC_TO_TEXT.apply(diags.values().iterator().next().get(0));
                return throwTree(message);
            }

            private JCStatement throwTree(String message) {
                message = message == null ? "Uncompilable code"
                                          : "Uncompilable code - " + message;
                JCNewClass nct =
                        make.NewClass(null,
                                      com.sun.tools.javac.util.List.nil(),
                                      make.QualIdent(syms.runtimeExceptionType.tsym),
                                      com.sun.tools.javac.util.List.of(make.Literal(message)),
                                      null);
                nct.type = syms.runtimeExceptionType;
                //find the constructor for RuntimeException(String):
                for (Element el : ElementFilter.constructorsIn(syms.runtimeExceptionType.tsym.getEnclosedElements())) {
                    Symbol s = (Symbol) el;

                    if (s.getKind() == ElementKind.CONSTRUCTOR && s.type.getParameterTypes().size() == 1 && s.type.getParameterTypes().head.tsym == syms.stringType.tsym) {
                        nct.constructor = s;
                        break;
                    }
                }
                return make.Throw(nct);
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                Set<JCNewClass> oldAnonymousClasses = anonymousClasses;
                ClassSymbol prevCurrentClass = currentClass;
                boolean prevErrorFound = errorFound;
                errorFound = false;
                try {
                    anonymousClasses = Collections.newSetFromMap(new LinkedHashMap<>());
                JCClassDecl clazz = (JCTree.JCClassDecl) node;
                Symbol.ClassSymbol csym = clazz.sym;
                if (isErroneousClass(csym)) {
                    //likely a duplicate of another class, don't touch:
                    return null;
                }
                if (isOtherClass(csym)) {
                    // Something went somewhere the csym.type is Type.Unknown,
                    // do not go any further
                    return null;
                }
                currentClass = csym;
                Type.ClassType ct = (Type.ClassType) csym.type;
                if (csym == syms.objectType.tsym) {
                    ct.all_interfaces_field = com.sun.tools.javac.util.List.nil();
                    ct.allparams_field = com.sun.tools.javac.util.List.nil();
                    ct.interfaces_field = com.sun.tools.javac.util.List.nil();
                    ct.typarams_field = com.sun.tools.javac.util.List.nil();
                    ct.supertype_field = Type.noType;
                } else {
                    ct.all_interfaces_field = error2Object(ct.all_interfaces_field);
                    ct.allparams_field = error2Object(ct.allparams_field);
                    ct.interfaces_field = error2Object(ct.interfaces_field);
                    ct.typarams_field = error2Object(ct.typarams_field);
                    ct.supertype_field = error2Object(ct.supertype_field);
                }
                clearAnnotations(clazz.sym.getMetadata());
                for (RecordComponent rc : clazz.sym.getRecordComponents()) {
                    rc.type = error2Object(rc.type);
                    scan(rc.accessorMeth, p);
                    if (rc.accessor == null) {
                        //the accessor is not created when the component type matches
                        //a non-arg j.l.Object method (which is a compile-time error)
                        //but the missing accessor will break Lower.
                        //initialize the field:
                        rc.accessor = new MethodSymbol(0, names.empty, new MethodType(com.sun.tools.javac.util.List.nil(), syms.errType, com.sun.tools.javac.util.List.nil(), syms.methodClass), clazz.sym);
                    }
                }
                for (JCTree def : clazz.defs) {
                    boolean errorClass = isErroneousClass(def);
                    if (errorClass) {
                        ClassSymbol member = ((JCClassDecl) def).sym;
                        if (member != null) {
                            csym.members_field.remove(member);
                        }
                    }
                    if (errorClass || def.hasTag(JCTree.Tag.ERRONEOUS)) {
                        clazz.defs = com.sun.tools.javac.util.List.filter(clazz.defs, def);
                    }
                }
                fixRecordMethods(clazz);
                super.visitClass(node, p);
                //remove anonymous classes that remained in the tree from anonymousClasses:
                new TreeScanner<Void, Void>() {
                        @Override
                        public Void visitNewClass(NewClassTree node, Void p) {
                            anonymousClasses.remove(node);
                            return super.visitNewClass(node, p);
                        }

                        @Override
                        public Void visitClass(ClassTree nestedNode, Void p) {
                            if (nestedNode == node) {
                                return super.visitClass(nestedNode, p);
                            }
                            return null;
                        }
                }.scan(node, null);
                if (!anonymousClasses.isEmpty()) {
                    anonymousClasses.forEach(a -> addAnonymousClass(clazz, a));
                }
                SortedMap<Long, List<Diagnostic<? extends JavaFileObject>>> classDiags = treeDiags(node);
                if (errorFound || !classDiags.isEmpty()) {
                    clazz.defs = clazz.defs.prepend(make.Block(Flags.STATIC, com.sun.tools.javac.util.List.of(throwTree(classDiags))));
                    classDiags.clear();
                }
                } finally {
                    errorFound = prevErrorFound;
                    currentClass = prevCurrentClass;
                    anonymousClasses = oldAnonymousClasses;
                }
                return null;
            }

            private final Set<String> RECORD_METHODS = new HashSet<>(Arrays.asList("toString", "hashCode", "equals"));

            private void fixRecordMethods(JCClassDecl clazz) {
                if ((clazz.sym.flags() & Flags.RECORD) == 0) {
                    return ;
                }
                Handler prevHandler = dcfh.setHandler(dcfh.speculativeCodeHandler);
                try {
                    try {
                        syms.objectMethodsType.tsym.flags();
                    } catch (CompletionFailure cf) {
                        //ignore
                    }
                    if (!syms.objectMethodsType.tsym.type.isErroneous()) {
                        //ObjectMethods exist:
                        return ;
                    }
                } finally {
                    dcfh.setHandler(prevHandler);
                }
                for (Symbol s : clazz.sym.members().getSymbols(s -> (s.flags() & Flags.RECORD) != 0 && s.kind == Kind.MTH && RECORD_METHODS.contains(s.name.toString()))) {
                    clazz.defs = clazz.defs.prepend(make.MethodDef((MethodSymbol) s, make.Block(0, com.sun.tools.javac.util.List.of(throwTree("java.lang.runtime.ObjectMethods does not exist!")))));
                    s.flags_field &= ~Flags.RECORD;
                }
            }

            private JCStatement clearAndWrapAnonymous(JCNewClass nc) {
                new TreeScanner<Void, Void>() {
                    @Override
                    public Void visitClass(ClassTree node, Void p) {
                        //no nested classes
                        return null;
                    }
                    @Override
                    public Void visitNewClass(NewClassTree node, Void p) {
                        if (node.getClassBody() != null) {
                            addAnonymousClass(nc.def, (JCNewClass) node);
                        }
                        return super.visitNewClass(node, p);
                    }
                    @Override
                    public Void visitMethod(MethodTree node, Void p) {
                        if (!node.getName().contentEquals(ANONYMOUS_CLASSES_METHOD)) {
                            return super.visitMethod(node, p);
                        }
                        return null;
                    }
                }.scan(nc.def.defs, null);
                for (JCTree t : nc.def.defs) {
                    switch (t.getTag()) {
                        case METHODDEF:
                            JCMethodDecl m = (JCMethodDecl) t;
                            if (!m.name.contentEquals(ANONYMOUS_CLASSES_METHOD)) {
                                m.body = make.Block(0, com.sun.tools.javac.util.List.of(throwTree(Collections.emptySortedMap())));
                            }
                            break;
                        case VARDEF:
                            ((JCVariableDecl) t).init = nullExpression(((JCVariableDecl) t).type);
                            break;
                    }
                }
                nc.type = nc.def.type;
                MethodSymbol constructor = (MethodSymbol) nc.constructor;
                ListBuffer<JCExpression> args = new ListBuffer<>();
                int startIdx = 0;
                if (nc.getEnclosingExpression() != null) {
                    startIdx = 1;
                }
                if (nc.clazz.type.tsym.isEnum()) {
                    args.add(nullExpression(syms.stringType));
                    args.add(nullExpression(syms.intType));
                }
                //XXX: should not touch constructor.params, as it can be null - but there is not test for that:
                com.sun.tools.javac.util.List<Type> paramTypes = constructor.type.getParameterTypes();
                for (Type paramType : paramTypes.subList(startIdx, paramTypes.size())) {
                    args.add(nullExpression(paramType));
                }
                nc.args = args.toList();
                return make.Exec(nc);
            }

            private static final String ANONYMOUS_CLASSES_METHOD = "$$anonymousClasses";

            private void addAnonymousClass(JCClassDecl current, JCNewClass anonymous) {
                Optional<JCMethodDecl> anonymousClassesMethodCandidate = current.defs.stream().filter(d -> d.hasTag(Tag.METHODDEF)).map(d -> (JCMethodDecl) d).filter(m -> m.name.contentEquals(ANONYMOUS_CLASSES_METHOD)).findAny();
                JCMethodDecl anonymousClassesMethodTree;
                if (!anonymousClassesMethodCandidate.isPresent()) {
                    MethodSymbol anonymousClassesMethod = new MethodSymbol(0, current.name.table.fromString(ANONYMOUS_CLASSES_METHOD), new MethodType(com.sun.tools.javac.util.List.nil(), syms.voidType, com.sun.tools.javac.util.List.nil(), syms.methodClass), current.sym);
                    current.sym.members_field.enter(anonymousClassesMethod);
                    anonymousClassesMethodTree = make.MethodDef(anonymousClassesMethod, make.Block(0, com.sun.tools.javac.util.List.nil()));
                    current.defs = current.defs.prepend(anonymousClassesMethodTree);
                } else {
                    anonymousClassesMethodTree = anonymousClassesMethodCandidate.get();
                }
                anonymousClassesMethodTree.body.stats = anonymousClassesMethodTree.body.stats.prepend(clearAndWrapAnonymous(anonymous));
            }

            private SortedMap<Long, List<Diagnostic<? extends JavaFileObject>>> treeDiags(Tree node) {
                long start = trees.getSourcePositions().getStartPosition(cut, node);
                long end = trees.getSourcePositions().getEndPosition(cut, node);
                SortedMap<Long, List<Diagnostic<? extends JavaFileObject>>> classDiags = start < end ? diags.subMap(start, end) : new TreeMap<>();
                return classDiags;
            }

            @Override
            public Void visitNewClass(NewClassTree node, Void p) {
                //TODO: fix constructors:
                JCNewClass nc = (JCNewClass) node;
                if (node.getClassBody() != null && !nc.clazz.type.hasTag(TypeTag.ERROR)) {
                    if (nc.constructor.kind == Kind.MTH) {
                        //make sure this class is generated even if the code is erroneous:
                        anonymousClasses.add(nc);
                    } else {
                        //TODO: if there is no constructor, skip for now - is there a better solution? See testNewClass
                        errorFound = true;
                    }
                }
//                    nct.constructor = constructor;
//                    nct.constructorType = constructor.type;
//                    nct.def = null;
                errorFound |= isErroneous(trees.getTypeMirror(getCurrentPath())); //isErroneous - enough?
                return super.visitNewClass(node, p);
            }

            @Override
            public Void visitIdentifier(IdentifierTree node, Void p) {
                errorFound |= isErroneous(trees.getTypeMirror(getCurrentPath())); //isErroneous - enough?
                return super.visitIdentifier(node, p);
            }

            @Override
            public Void visitMemberSelect(MemberSelectTree node, Void p) {
                errorFound |= isErroneous(trees.getTypeMirror(getCurrentPath())); //isErroneous - enough?
                if (node.getExpression().getKind() == Tree.Kind.IDENTIFIER &&
                    ((IdentifierTree) node.getExpression()).getName().contentEquals("super")) {
                    TreePath selected = new TreePath(getCurrentPath(), node.getExpression());
                    Element selectedEl = trees.getElement(selected);
                    if (selectedEl != null && !Objects.equals(selectedEl.getEnclosingElement(), currentClass)) {
                        errorFound = true;
                    }
                }
                return super.visitMemberSelect(node, p);
            }

            @Override
            public Void visitMethodInvocation(MethodInvocationTree node, Void p) {
                Type varArgs = ((JCMethodInvocation) node).varargsElement;
                if (varArgs != null) {
                    errorFound |= isErroneous(varArgs);
                }
                return super.visitMethodInvocation(node, p);
            }

            @Override
            public Void visitMemberReference(MemberReferenceTree node, Void p) {
                JCMemberReference ref = (JCMemberReference) node;
                ref.target = error2Object(ref.target);
                return super.visitMemberReference(node, p);
            }

            @Override
            public Void visitBindingPattern(BindingPatternTree node, Void p) {
                return super.visitBindingPattern(node, p);
            }

            @Override
            public Void visitDeconstructionPattern(DeconstructionPatternTree node, Void p) {
                errorFound |= !hasMatchException;
                return super.visitDeconstructionPattern(node, p);
            }

            @Override
            public Void visitSwitch(SwitchTree node, Void p) {
                JCSwitch swt = (JCSwitch) node;
                handleSwitch(swt.patternSwitch);
                return super.visitSwitch(node, p);
            }

            @Override
            public Void visitSwitchExpression(SwitchExpressionTree node, Void p) {
                JCSwitchExpression swt = (JCSwitchExpression) node;
                handleSwitch(swt.patternSwitch);
                return super.visitSwitchExpression(node, p);
            }

            private void handleSwitch(boolean patternSwitch) {
                if (patternSwitch && !Feature.PATTERN_SWITCH.allowedInSource(source)) {
                    errorFound = true;
                }
            }

            @Override
            public Void scan(Tree tree, Void p) {
                if (tree != null && ExpressionTree.class.isAssignableFrom(tree.getClass())) {
                    errorFound |= isErroneous(trees.getTypeMirror(new TreePath(getCurrentPath(), tree))); //isErroneous - enough?
                }
                return super.scan(tree, p);
            }

            private void clearAnnotations(SymbolMetadata metadata) {
                if (metadata == null)
                    return;

                //TODO: type annotations, etc.
                com.sun.tools.javac.util.List<Attribute.Compound> annotations = metadata.getDeclarationAttributes();
                com.sun.tools.javac.util.List<Attribute.Compound> prev = null;
                while (annotations.nonEmpty()) {
                    if (isAnnotationErroneous(annotations.head)) {
                        if (prev == null) {
                            metadata.reset();
                            metadata.setDeclarationAttributes(annotations.tail);
                        } else {
                            prev.tail = annotations.tail;
                        }
                    } else {
                        prev = annotations;
                    }
                    annotations = annotations.tail;
                }
            }

            private boolean isAnnotationErroneous(Attribute annotation) {
                if (isErroneous(annotation.type)) {
                    return true;
                } else if (annotation instanceof Attribute.Array) {
                    for (Attribute nested : ((Attribute.Array) annotation).values) {
                        if (isAnnotationErroneous(nested)) {
                            return true;
                        }
                    }
                    return false;
                } else if (annotation instanceof Attribute.Class) {
                    return isErroneous(((Attribute.Class) annotation).classType);
                } else if (annotation instanceof Attribute.Compound) {
                    for (Pair<MethodSymbol, Attribute> p : ((Attribute.Compound) annotation).values) {
                        if (isAnnotationErroneous(p.snd)) {
                            return true;
                        }
                    }
                    return false;
                } else if (annotation instanceof Attribute.Constant) {
                    return false;
                } else if (annotation instanceof Attribute.Enum) {
                    return false;
                } else if (annotation instanceof Attribute.Error) {
                    return true;
                } else {
                    //let's skip all unknown attributes, as we cannot check if they are fine or not
                    return true;
                }
            }

            private boolean isErroneous(TypeMirror type) {
                return type == null || type.getKind() == TypeKind.ERROR || type.getKind() == TypeKind.NONE || type.getKind() == TypeKind.OTHER || (type.getKind() == TypeKind.ARRAY && isErroneous(((ArrayType) type).getComponentType()));
            }

            private boolean errorFound;
            private final Map<Type, Boolean> seen = new IdentityHashMap<>();

            private Type error2Object(Type t) {
                if (t == null)
                    return null;

                if (isErroneous(t)) {
                    errorFound = true;
                    return syms.objectType;
                }

                Boolean err = seen.get(t);
                if (err != null) {
                    errorFound |= err;
                    return t;
                }

                seen.put(t, false);
                boolean prevErrorFound = errorFound;
                errorFound = false;
                switch (t.getKind()) {
                    case DECLARED: {
                        resolveErrors((ClassType) t);
                        break;
                    }
                    case WILDCARD: {
                        Type.WildcardType wt = ((Type.WildcardType) t);
                        wt.type = error2Object(wt.type);
                        TypeVar tv = wt.bound;
                        if (tv != null) {
                            clearTypeVar(tv);
                        }
                        break;
                    }
                    case TYPEVAR: {
                        clearTypeVar((Type.TypeVar) t);
                        break;
                    }
                    case ARRAY: {
                        Type.ArrayType at = (Type.ArrayType) t;
                        Type component = error2Object(at.elemtype);
                        if (component != at.elemtype) {
                            at.elemtype = types.makeArrayType(component);
                        }
                        break;
                    }
                }
                seen.put(t, errorFound);
                errorFound |= prevErrorFound;

                return t;
            }

            private void clearTypeVar(Type.TypeVar tv) {
                String[] boundNames = {"bound", "_bound"};
                for (String boundName : boundNames) {
                    try {
                        Field bound = tv.getClass().getDeclaredField(boundName);
                        bound.setAccessible(true);
                        bound.set(tv, error2Object((Type) bound.get(tv)));
                    } catch (IllegalAccessException | NoSuchFieldException | SecurityException ex) {
                        JavaIndex.LOG.log(Level.FINEST, null, ex);
                    }
                }
                tv.lower = error2Object(tv.lower);
            }

            private com.sun.tools.javac.util.List<Type> error2Object(com.sun.tools.javac.util.List<Type> types) {
                if (types == null)
                    return null;

                ListBuffer<Type> lb = new ListBuffer<>();
                boolean changed = false;
                for (Type t : types) {
                    Type nue = error2Object(t);
                    changed |= nue != t;
                    lb.append(nue);
                }
                return changed ? lb.toList() : types;
            }

            private void resolveErrors(ClassType ct) {
                if (ct.tsym == syms.objectType.tsym) return ;
                ct.all_interfaces_field = error2Object(ct.all_interfaces_field);
                ct.allparams_field = error2Object(ct.allparams_field); //TODO: should replace with bounds
                ct.interfaces_field = error2Object(ct.interfaces_field);
                ct.typarams_field = error2Object(ct.typarams_field);
                ct.supertype_field = error2Object(ct.supertype_field);
            }
        }.scan(cut, null);
        fixedListener.accept(((JCCompilationUnit) cut).sourcefile, cut);
    }

    /**
     * Check if a class is a duplicate, has cyclic dependencies,
     * or has another critical issue.
     */
    private boolean isErroneousClass(JCTree tree) {
        if (!tree.hasTag(Tag.CLASSDEF)) {
            return false;
        }
        return isErroneousClass(((JCClassDecl) tree).sym);
    }

    private boolean isErroneousClass(Element el) {
        return el instanceof ClassSymbol && (((ClassSymbol) el).asType() == null || ((ClassSymbol) el).asType().getKind() == TypeKind.ERROR);
    }

    private boolean isOtherClass(Element el) {
        return el instanceof ClassSymbol && (((ClassSymbol) el).asType() == null || ((ClassSymbol) el).asType().getKind() == TypeKind.OTHER);
    }

    @SuppressWarnings("PackageVisibleField") // Unittests
    static Function<Diagnostic<?>, String> DIAGNOSTIC_TO_TEXT = d -> d.getMessage(null);
}
