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

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.TreePathScanner;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.SymbolMetadata;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.ForAll;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.CompileStates.CompileState;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Modules;
import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCModuleDecl;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.TreeMaker;
import org.netbeans.lib.nbjavac.services.CancelAbort;
import org.netbeans.lib.nbjavac.services.CancelService;
import com.sun.tools.javac.util.FatalError;
import com.sun.tools.javac.util.ListBuffer;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.CompilerOptionsQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer.CompileTuple;
import org.netbeans.modules.java.source.parsing.FileManagerTransaction;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.parsing.OutputFileManager;
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
final class VanillaCompileWorker extends CompileWorker {

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

        final DiagnosticListenerImpl dc = new DiagnosticListenerImpl();
        final LinkedList<CompilationUnitTree> trees = new LinkedList<CompilationUnitTree>();
        Map<CompilationUnitTree, CompileTuple> units = new IdentityHashMap<CompilationUnitTree, CompileTuple>();
        JavacTaskImpl jt = null;

        boolean nop = true;
        final SuspendStatus suspendStatus = context.getSuspendStatus();
        final SourcePrefetcher sourcePrefetcher = SourcePrefetcher.create(files, suspendStatus);
        Map<JavaFileObject, CompileTuple> fileObjects = new IdentityHashMap<>();
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
                        return context.isCancelled() || isLowMemory(null);
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
            Log.instance(jt.getContext()).nerrors = 0;
        } catch (CancelAbort ca) {
            if (context.isCancelled() && JavaIndex.LOG.isLoggable(Level.FINEST)) {
                JavaIndex.LOG.log(Level.FINEST, "VanillaCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
            }
        } catch (Throwable t) {
            if (JavaIndex.LOG.isLoggable(Level.WARNING)) {
                final ClassPath bootPath   = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.BOOT);
                final ClassPath classPath  = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.COMPILE);
                final ClassPath sourcePath = javaContext.getClasspathInfo().getClassPath(ClasspathInfo.PathKind.SOURCE);
                final String message = String.format("VanillaCompileWorker caused an exception\nFile: %s\nRoot: %s\nBootpath: %s\nClasspath: %s\nSourcepath: %s", //NOI18N
                            fileObjects.values().iterator().next().indexable.getURL().toString(),
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
        if (jt == null || units == null || JavaCustomIndexer.NO_ONE_PASS_COMPILE_WORKER) {
            return ParsingOutput.failure(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
        }
        if (context.isCancelled()) {
            return null;
        }
        if (isLowMemory(null)) {
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
                dropMethodsAndErrors(jt.getContext(), unit.getKey());
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
                    Modules modules = Modules.instance(jtFin.getContext());
                    compiler.shouldStopPolicyIfError = CompileState.FLOW; 
                    for (Element type : types) {
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
        } catch (CancelAbort ca) {
            if (isLowMemory(null)) {
                return ParsingOutput.lowMemory(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
            } else if (JavaIndex.LOG.isLoggable(Level.FINEST)) {
                JavaIndex.LOG.log(Level.FINEST, "VanillaCompileWorker was canceled in root: " + FileUtil.getFileDisplayName(context.getRoot()), ca);  //NOI18N
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
        return ParsingOutput.failure(moduleName.name, file2FQNs, addedTypes, addedModules, createdFiles, finished, modifiedTypes, aptGenerated);
    }

    private void dropMethodsAndErrors(com.sun.tools.javac.util.Context ctx, CompilationUnitTree cut) {
        Symtab syms = Symtab.instance(ctx);
        Names names = Names.instance(ctx);
        TreeMaker make = TreeMaker.instance(ctx);
        //TODO: should preserve error types!!!
        new TreePathScanner<Void, Void>() {
            @Override
            public Void visitVariable(VariableTree node, Void p) {
                JCTree.JCVariableDecl decl = (JCTree.JCVariableDecl) node;
                if ((decl.mods.flags & Flags.ENUM) == 0) {
                    decl.init = null;
                }
                decl.sym.type = decl.type = error2Object(decl.type);
                clearAnnotations(decl.sym.getMetadata());
                return super.visitVariable(node, p);
            }

            @Override
            public Void visitMethod(MethodTree node, Void p) {
                JCTree.JCMethodDecl decl = (JCTree.JCMethodDecl) node;
                Symbol.MethodSymbol msym = decl.sym;
                if (Collections.disjoint(msym.getModifiers(), EnumSet.of(Modifier.NATIVE, Modifier.ABSTRACT))) {
                    JCTree.JCNewClass nct =
                            make.NewClass(null,
                                          com.sun.tools.javac.util.List.nil(),
                                          make.QualIdent(syms.runtimeExceptionType.tsym),
                                          com.sun.tools.javac.util.List.of(make.Literal("")),
                                          null);
                    nct.type = syms.runtimeExceptionType;
                    nct.constructor = syms.runtimeExceptionType.tsym.members().getSymbols(
                            s -> s.getKind() == ElementKind.CONSTRUCTOR && s.type.getParameterTypes().size() == 1 && s.type.getParameterTypes().head.tsym == syms.stringType.tsym
                    ).iterator().next();
                    decl.body = make.Block(0, com.sun.tools.javac.util.List.of(make.Throw(nct)));
                }
                Type.MethodType mt;
                if (msym.type.hasTag(TypeTag.FORALL)) {
                    ForAll fa = (ForAll) msym.type;
                    fa.tvars = error2Object(fa.tvars);
                    mt = fa.asMethodType();
                } else {
                    mt = (Type.MethodType) msym.type;
                }
                mt.restype = error2Object(mt.restype);
                mt.argtypes = error2Object(mt.argtypes);
                mt.thrown = error2Object(mt.thrown);
                clearAnnotations(decl.sym.getMetadata());
                return super.visitMethod(node, p);
            }

            @Override
            public Void visitClass(ClassTree node, Void p) {
                JCClassDecl clazz = (JCTree.JCClassDecl) node;
                Symbol.ClassSymbol csym = clazz.sym;
                Type.ClassType ct = (Type.ClassType) csym.type;
                ct.all_interfaces_field = error2Object(ct.all_interfaces_field);
                ct.allparams_field = error2Object(ct.allparams_field);
                ct.interfaces_field = error2Object(ct.interfaces_field);
                ct.typarams_field = error2Object(ct.typarams_field);
                ct.supertype_field = error2Object(ct.supertype_field);
                super.visitClass(node, p);
                for (JCTree def : clazz.defs) {
                    if (def.hasTag(JCTree.Tag.ERRONEOUS)) {
                        clazz.defs = com.sun.tools.javac.util.List.filter(clazz.defs, def);
                    }
                }
                return null;
            }

            private void clearAnnotations(SymbolMetadata metadata) {
                if (metadata == null)
                    return;

                //TODO: type annotations, etc.
                com.sun.tools.javac.util.List<Attribute.Compound> annotations = metadata.getDeclarationAttributes();
                com.sun.tools.javac.util.List<Attribute.Compound> prev = null;
                while (annotations.nonEmpty()) {
                    if (isErroneous(annotations.head.type)) {
                        if (prev == null) {
                            metadata.reset();
                            metadata.setDeclarationAttributes(annotations.tail);
                        } else {
                            prev.tail = annotations.tail;
                        }
                    }
                    prev = annotations;
                    annotations = annotations.tail;
                }
            }

            private boolean isErroneous(TypeMirror type) {
                return type == null || type.getKind() == TypeKind.ERROR || type.getKind() == TypeKind.NONE || type.getKind() == TypeKind.OTHER;
            }

            private Set<Type> seen = Collections.newSetFromMap(new IdentityHashMap<>());

            private Type error2Object(Type t) {
                if (t == null)
                    return null;

                if (isErroneous(t)) {
                    return syms.objectType;
                }

                if (!seen.add(t))
                    return t;

                switch (t.getKind()) {
                    case DECLARED: {
                        resolveErrors((ClassType) t);
                        break;
                    }
                    case WILDCARD: {
                        Type.WildcardType wt = ((Type.WildcardType) t);
                        wt.type = error2Object(wt.type);
                        TypeVar tv = wt.bound;
                        tv.bound = error2Object(tv.bound);
                        tv.lower = error2Object(tv.lower);
                        break;
                    }
                }
                return t;
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
    }
}
