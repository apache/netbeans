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

package org.netbeans.api.java.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Processor;
import javax.lang.model.element.*;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.ElementScanner6;

import com.sun.source.tree.*;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Preview;
import com.sun.tools.javac.code.Scope.NamedImportScope;
import com.sun.tools.javac.code.Scope.StarImportScope;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.*;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.comp.Check;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import javax.lang.model.util.ElementScanner14;

import javax.swing.SwingUtilities;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.matching.Matcher;
import org.netbeans.api.java.source.matching.Occurrence;
import org.netbeans.api.java.source.matching.Pattern;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.lib.nbjavac.services.NBNames;
import org.netbeans.modules.java.preprocessorbridge.spi.ImportProcessor;
import org.netbeans.modules.java.source.ElementHandleAccessor;
import org.netbeans.modules.java.source.ElementUtils;
import org.netbeans.modules.java.source.JavadocHelper;
import org.netbeans.modules.java.source.ModuleNames;
import org.netbeans.modules.java.source.indexing.FQN2Files;
import org.netbeans.modules.java.source.indexing.JavaCustomIndexer;
import org.netbeans.modules.java.source.parsing.ClassParser;
import org.netbeans.modules.java.source.parsing.ClasspathInfoProvider;
import org.netbeans.modules.java.source.parsing.FileObjects;
import org.netbeans.modules.java.source.parsing.Hacks;
import org.netbeans.modules.java.source.parsing.JavacParser;
import org.netbeans.modules.java.source.save.DiffContext;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClasspathInfoAccessor;
import org.netbeans.modules.java.source.usages.ExecutableFilesIndex;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.NestedClass;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Dusan Balek
 */
public class SourceUtils {

    private static final Logger LOG = Logger.getLogger(SourceUtils.class.getName());

    private SourceUtils() {}

    /**
     * @since 0.21
     */
    public static TokenSequence<JavaTokenId> getJavaTokenSequence(final TokenHierarchy hierarchy, final int offset) {
        if (hierarchy != null) {
            TokenSequence<?> ts = hierarchy.tokenSequence();
            while(ts != null && (offset == 0 || ts.moveNext())) {
                ts.move(offset);
                if (ts.language() == JavaTokenId.language()) {
                    return (TokenSequence<JavaTokenId>)ts;
                }
                if (!ts.moveNext() && !ts.movePrevious()) {
                    return null;
                }
                ts = ts.embedded();
            }
        }
        return null;
    }

    /**
     * Find duplicates for provided expression
     * @param info CompilationInfo
     * @param searchingFor expression which is being searched
     * @param scope scope for search
     * @param cancel option to cancel find duplicates
     * @return set of TreePaths representing duplicates
     * @since 0.85
     */
    public static Set<TreePath> computeDuplicates(CompilationInfo info, TreePath searchingFor, TreePath scope, AtomicBoolean cancel) {
        Set<TreePath> result = new HashSet<>();

        for (Occurrence od : Matcher.create(info).setCancel(cancel).setSearchRoot(scope).match(Pattern.createSimplePattern(searchingFor))) {
            result.add(od.getOccurrenceRoot());
        }

        return result;
    }

    public static boolean checkTypesAssignable(CompilationInfo info, TypeMirror from, TypeMirror to) {
        Context c = ((JavacTaskImpl) info.impl.getJavacTask()).getContext();
        if (from.getKind() == TypeKind.TYPEVAR) {
            Types types = Types.instance(c);
            TypeVar t = types.substBound((TypeVar)from, com.sun.tools.javac.util.List.of((Type)from), com.sun.tools.javac.util.List.of(types.boxedTypeOrType((Type)to)));
            return info.getTypes().isAssignable(t.getUpperBound(), to)
                    || info.getTypes().isAssignable(to, t.getUpperBound());
        }
        if (from.getKind() == TypeKind.WILDCARD) {
            from = Types.instance(c).wildUpperBound((Type)from);
        }
        return Check.instance(c).checkType(null, (Type)from, (Type)to).getKind() != TypeKind.ERROR;
    }

    public static TypeMirror getBound(WildcardType wildcardType) {
        Type.TypeVar bound = ((Type.WildcardType)wildcardType).bound;
        return bound != null ? bound.getUpperBound() : null;
    }

    /**
     * Returns a list of completions for an annotation attribute value suggested by
     * annotation processors.
     *
     * @param info the CompilationInfo used to resolve annotation processors
     * @param element the element being annotated
     * @param annotation the (perhaps partial) annotation being applied to the element
     * @param member the annotation member to return possible completions for
     * @param userText source code text to be completed
     * @return suggested completions to the annotation member
     *
     * @since 0.57
     */
    public static List<? extends Completion> getAttributeValueCompletions(CompilationInfo info, Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
        List<Completion> completions = new LinkedList<>();
        if (info.getPhase().compareTo(Phase.ELEMENTS_RESOLVED) >= 0) {
            String fqn = ((TypeElement) annotation.getAnnotationType().asElement()).getQualifiedName().toString();
            Iterable<? extends Processor> processors =
                    JavacParser.ProcessorHolder.instance(info.impl.getJavacTask().getContext()).getProcessors();
            if (processors != null) {
                for (Processor processor : processors) {
                    boolean match = false;
                    for (String sat : processor.getSupportedAnnotationTypes()) {
                        if ("*".equals(sat)) { //NOI18N
                            match = true;
                            break;
                        } else if (sat.endsWith(".*")) { //NOI18N
                            sat = sat.substring(0, sat.length() - 1);
                            if (fqn.startsWith(sat)) {
                                match = true;
                                break;
                            }
                        } else if (fqn.equals(sat)) {
                            match = true;
                            break;
                        }
                    }
                    if (match) {
                        try {
                            for (Completion c : processor.getCompletions(element, annotation, member, userText)) {
                                completions.add(c);
                            }
                        } catch (Exception e) {
                            Logger.getLogger(processor.getClass().getName()).log(Level.INFO, e.getMessage(), e);
                        }
                    }
                }
            }
        }
        return completions;
    }

    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept packages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     * @deprecated use {@link ElementUtilities#enclosingTypeElement(javax.lang.model.element.Element)}
     */
    public static @Deprecated TypeElement getEnclosingTypeElement( Element element ) throws IllegalArgumentException {
        return ElementUtilities.enclosingTypeElementImpl(element);
    }

    public static TypeElement getOutermostEnclosingTypeElement( Element element ) {

	Element ec =  getEnclosingTypeElement( element );
	if (ec == null) {
	    ec = element;
	}

	while( ec.getEnclosingElement().getKind().isClass() ||
	       ec.getEnclosingElement().getKind().isInterface() ) {

	    ec = ec.getEnclosingElement();
	}

	return (TypeElement)ec;
    }

    /** Finds a source name that the {@code element} originates from. In case
     * of {@code element} being created via {@link JavaSource#forFileObject(org.openide.filesystems.FileObject) source file}
     * it should be the name (without any path) of the source file. For elements
     * originating from {@code .class} file the returned value corresponds to
     * the value of {@code SourceFile} attribute, if present.
     *
     * @param element element of a source file
     * @return the (short) name of source file that this elements originates
     *    from or {@code null}, if the name isn't known
     * @since 2.60
     */
    public static String findSourceFileName(Element element) {
        if (element instanceof ClassSymbol) {
            ClassSymbol s = (ClassSymbol) element;
            if (s.sourcefile != null) {
                return s.sourcefile.getName();
            }
        }
        return null;
    }

    /**
     * Returns an array containing the JVM signature of the {@link ElementHandle}.
     * @param handle to obtain the JVM signature for.
     * @return an array containing the JVM signature. The signature depends on
     * the {@link ElementHandle}'s {@link ElementKind}. For class or package
     * it returns a single element array containing the class (package) binary
     * name (JLS section 13.1). For field (method) it returns three element array
     * containing owner class binary name (JLS section 13.1) in the first element,
     * field (method) name in the second element and JVM type (JVM method formal
     * parameters (JVMS section 2.10.1)) in the third element.
     * @since 0.84
     */
    @NonNull
    public static String[] getJVMSignature(@NonNull final ElementHandle<?> handle) {
        Parameters.notNull("handle", handle);   //NOI18N
        return ElementHandleAccessor.getInstance().getJVMSignature(handle);
    }


    /**Resolve full qualified name in the given context. Adds import statement as necessary.
     * Returns name that resolved to a given FQN in given context (either simple name
     * or full qualified name). Handles import conflicts.
     *
     * <br><b>Note:</b> if the <code>info</code> passed to this method is not an instance of {@link WorkingCopy},
     * missing import statement is added from a separate modification task executed asynchronously.
     * <br><b>Note:</b> after calling this method, it is not permitted to rewrite copy.getCompilationUnit().
     *
     * @param info CompilationInfo over which the method should work
     * @param context in which the fully qualified should be resolved
     * @param fqn the fully qualified name to resolve
     * @return either a simple name or a FQN that will resolve to given fqn in given context
     */
    public static String resolveImport(final CompilationInfo info, final TreePath context, final String fqn) throws NullPointerException, IOException {
        if (info == null) {
            throw new NullPointerException();
        }
        if (context == null) {
            throw new NullPointerException();
        }
        if (fqn == null) {
            throw new NullPointerException();
        }

        CodeStyle cs = DiffContext.getCodeStyle(info);
        if (cs.useFQNs()) {
            return fqn;
        }
        final CompilationUnitTree cut = info.getCompilationUnit();
        final Trees trees = info.getTrees();
        final Scope scope = trees.getScope(context);
        String qName = fqn;
        StringBuilder sqName = new StringBuilder();
        boolean clashing = false;
        ElementUtilities eu = info.getElementUtilities();
        ElementUtilities.ElementAcceptor acceptor = new ElementUtilities.ElementAcceptor() {
            @Override
            public boolean accept(Element e, TypeMirror type) {
                return (e.getKind().isClass() || e.getKind().isInterface()) && trees.isAccessible(scope, (TypeElement)e);
            }
        };
        Element el = info.getTrees().getElement(new TreePath(cut));
        ModuleElement modle = el != null ? info.getElements().getModuleOf(el) : null;
        Element toImport = null;
        while(qName != null && qName.length() > 0) {
            int lastDot = qName.lastIndexOf('.');
            Element element;
            if ((element = (modle != null ? info.getElements().getTypeElement(modle, qName) : info.getElements().getTypeElement(qName))) != null) {
                clashing = false;
                String simple = qName.substring(lastDot < 0 ? 0 : lastDot + 1);
                if (sqName.length() > 0) {
                    sqName.insert(0, '.');
                }
                sqName.insert(0, simple);
                if (cs.useSingleClassImport() && (toImport == null || !cs.importInnerClasses())) {
                    toImport = element;
                }
                boolean matchFound = false;
                for(Element e : eu.getLocalMembersAndVars(scope, acceptor)) {
                    if (simple.contentEquals(e.getSimpleName())) {
                        //either a clash or already imported:
                        if (qName.contentEquals(((TypeElement)e).getQualifiedName())) {
                            return sqName.toString();
                        } else {
                            clashing = true;
                        }
                        matchFound = true;
                        break;
                    }
                }
                if (!matchFound) {
                    for(TypeElement e : eu.getGlobalTypes(acceptor)) {
                        if (simple.contentEquals(e.getSimpleName())) {
                            //either a clash or already imported:
                            if (qName.contentEquals(e.getQualifiedName())) {
                                return sqName.toString();
                            } else {
                                clashing = true;
                            }
                            break;
                        }
                    }
                }
                if (cs.importInnerClasses()) {
                    break;
                }
            } else if ((element = (modle != null ? info.getElements().getPackageElement(modle, qName) : info.getElements().getPackageElement(qName))) != null) {
                if (toImport == null || GeneratorUtilities.checkPackagesForStarImport(qName, cs)) {
                    toImport = element;
                }
                break;
            }
            qName = lastDot < 0 ? null : qName.substring(0, lastDot);
        }
        if (clashing || toImport == null) {
            return fqn;
        }

        //not imported/visible so far by any means:
        String topLevelLanguageMIMEType = info.getFileObject().getMIMEType();
        if ("text/x-java".equals(topLevelLanguageMIMEType)){ //NOI18N
            final Set<Element> elementsToImport = Collections.singleton(toImport);
            if (info instanceof WorkingCopy) {
                CompilationUnitTree nue = (CompilationUnitTree) ((WorkingCopy)info).resolveRewriteTarget(cut);
                ((WorkingCopy)info).rewrite(info.getCompilationUnit(), GeneratorUtilities.get((WorkingCopy)info).addImports(nue, elementsToImport));
                ((WorkingCopy)info).invalidateSourceAfter = true;
            } else {
                final ElementHandle handle = ElementHandle.create(toImport);
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ModificationResult.runModificationTask(Collections.singletonList(info.getSnapshot().getSource()), new UserTask() {
                                @Override
                                public void run(ResultIterator resultIterator) throws Exception {
                                    WorkingCopy copy = WorkingCopy.get(resultIterator.getParserResult());
                                    copy.toPhase(Phase.ELEMENTS_RESOLVED);
                                    Element elementToImport = handle.resolve(copy);
                                    if (elementToImport == null) {
                                        // the text was possibly changed ?
                                        return;
                                    }
                                    copy.rewrite(copy.getCompilationUnit(), GeneratorUtilities.get(copy).addImports(copy.getCompilationUnit(), Collections.singleton(elementToImport)));
                                }
                            }).commit();
                        } catch (Exception e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            }
            // only import symbols if import generation succeeded
            JCCompilationUnit unit = (JCCompilationUnit) info.getCompilationUnit();
            if (toImport.getKind() == ElementKind.PACKAGE) {
                StarImportScope importScope = new StarImportScope(unit.packge);
                importScope.prependSubScope(unit.starImportScope);
                importScope.prependSubScope(((PackageSymbol)toImport).members());
                unit.starImportScope = importScope;
            } else {
                Class<NamedImportScope> nisClazz = NamedImportScope.class;
                NamedImportScope importScope;
                try {
                    importScope = nisClazz.getConstructor(Symbol.class, com.sun.tools.javac.code.Scope.class).newInstance(unit.packge, unit.toplevelScope);
                } catch (ReflectiveOperationException ex) {
                    try {
                        importScope = nisClazz.getConstructor(Symbol.class).newInstance(unit.packge);
                    } catch (ReflectiveOperationException ex2) {
                        throw new IllegalStateException(ex2);
                    }
                }
                for (Symbol symbol : unit.namedImportScope.getSymbols()) {
                    importScope.importType(symbol.owner.members(), symbol.owner.members(), symbol);
                }
                importScope.importType(((Symbol)toImport).owner.members(), ((Symbol)toImport).owner.members(), (Symbol) toImport);
                unit.namedImportScope = importScope;
            }
        } else { // embedded java, look up the handler for the top level language
            Lookup lookup = MimeLookup.getLookup(MimePath.get(topLevelLanguageMIMEType));
            Collection<? extends ImportProcessor> instances = lookup.lookupAll(ImportProcessor.class);

            for (ImportProcessor importsProcesor : instances) {
                importsProcesor.addImport(info.getDocument(), fqn);
            }

        }
        return sqName.toString();
    }

    /**
     * Returns a {@link FileObject} in which the Element is defined.
     * @param element for which the {@link FileObject} should be located
     * @param cpInfo the classpaths context
     * @return the defining {@link FileObject} or null if it cannot be
     * found
     *
     * @deprecated use {@link #getFile(ElementHandle, ClasspathInfo)}
     */
    @Deprecated
    public static FileObject getFile (Element element, final ClasspathInfo cpInfo) {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("cpInfo", cpInfo);   //NOI18N

        Element prev = isPkgOrMdl(element.getKind()) ? element : null;
        while (!isPkgOrMdl(element.getKind())) {
            prev = element;
            element = element.getEnclosingElement();
        }
        final ElementKind kind = prev.getKind();
        if (!(kind.isClass() || kind.isInterface() || isPkgOrMdl(kind))) {
            return null;
        }
        final ElementHandle<? extends Element> handle = ElementHandle.create(prev);
        return getFile (handle, cpInfo, null);
    }

    /**
     * Returns a {@link FileObject} of the source file in which the handle is declared.
     * @param handle to find the {@link FileObject} for
     * @param cpInfo classpaths for resolving handle
     * @return {@link FileObject} or null when the source file cannot be found
     */
    public static FileObject getFile (final ElementHandle<? extends Element> handle, final ClasspathInfo cpInfo) {
      return getFile(handle, cpInfo, new String[0]);
    }

    /**
     * Returns a {@link FileObject} of the source file in which the handle is declared.
     * @param handle to find the {@link FileObject} for
     * @param cpInfo classpaths for resolving handle
     * @param names suggested file names
     * @return {@link FileObject} or null when the source file cannot be found
     * @since 2.60
     */
    public static FileObject getFile (final ElementHandle<? extends Element> handle, final ClasspathInfo cpInfo, String... names) {
        Parameters.notNull("handle", handle);
        Parameters.notNull("cpInfo", cpInfo);
        try {
            boolean pkg = handle.getKind() == ElementKind.PACKAGE;
            String[] signature = handle.getSignature();
            assert signature.length >= 1;
            final ClassPath[] cps =
                new ClassPath[] {
                    cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.OUTPUT),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.BOOT),
                    createClassPath(cpInfo,ClasspathInfo.PathKind.COMPILE),
                };
            String pkgName, className = null;
            Predicate<FileObject> filter = (p) -> true;
            if (pkg) {
                pkgName = FileObjects.convertPackage2Folder(signature[0]);
            } else if (handle.getKind() == ElementKind.MODULE) {
                pkgName = "";   //NOI18N
                className = FileObjects.MODULE_INFO;
                final String moduleName = handle.getQualifiedName();
                JavaFileManager fm = ClasspathInfoAccessor.getINSTANCE().createFileManager(cpInfo, null);
                JavaFileManager.Location loc = fm.getLocationForModule(StandardLocation.MODULE_PATH, moduleName);
                if (loc == null) {
                    loc = fm.getLocationForModule(StandardLocation.SYSTEM_MODULES, moduleName);
                    if (loc == null) {
                        loc = fm.getLocationForModule(StandardLocation.UPGRADE_MODULE_PATH, moduleName);
                    }
                }
                if (loc != null) {
                    JavaFileObject jfo = fm.getJavaFileForInput(loc, className, JavaFileObject.Kind.CLASS);
                    FileObject fo = jfo != null ? URLMapper.findFileObject(jfo.toUri().toURL()) : null;
                    if (fo != null) {
                        FileObject foundFo = findSourceForBinary(fo.getParent(), fo, signature[0], pkgName, className, false, names);
                        if (foundFo != null) {
                            return foundFo;
                        }
                    }
                }
                filter = (fo) -> moduleName.equals(SourceUtils.getModuleName(fo.toURL()));
            } else {
                int index = signature[0].lastIndexOf('.');                          //NOI18N
                if (index<0) {
                    pkgName = "";                                             //NOI18N
                    className = signature[0];
                }
                else {
                    pkgName = FileObjects.convertPackage2Folder(signature[0].substring(0,index));
                    className = signature[0].substring(index+1);
                }
            }
            final List<Pair<FileObject,ClassPath>> fos = findAllResources(pkgName, filter, cps);
            for (Pair<FileObject,ClassPath> pair : fos) {
                FileObject root = pair.second().findOwnerRoot(pair.first());
                if (root == null) {
                    continue;
                }
                FileObject foundFo = findSourceForBinary(root, pair.first(), signature[0], pkgName, className, pkg, names);
                if (foundFo != null) {
                    return foundFo;
                }
            }
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        return null;
    }

    private static FileObject findSourceForBinary(FileObject binaryRoot, FileObject binary, String signature, String pkgName, String className, boolean isPkg, String[] names) throws IOException {
        FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(binaryRoot.toURL()).getRoots();
        ClassPath sourcePath = ClassPathSupport.createClassPath(sourceRoots);
        LinkedList<FileObject> folders = new LinkedList<>(sourcePath.findAllResources(pkgName));
        if (isPkg) {
            return folders.isEmpty() ? binary : folders.get(0);
        }
        final boolean caseSensitive = isCaseSensitive ();
        final List<String> fnames = new ArrayList<>();
        fnames.addAll(getSourceFileNames(className));
        if (names != null) {
            fnames.addAll(Arrays.asList(names));
        }
        folders.addFirst(binary);
        if (fnames.size() == 1) {
            FileObject match = findMatchingChild(fnames.get(0), folders, caseSensitive);
            if (match != null) {
                return match;
            }
        } else {
            for (String candidate : (List<String>)fnames) {
                FileObject match = findMatchingChild(candidate, folders, caseSensitive);
                if (match != null) {
                    FileObject ownerRoot = sourcePath.entries().isEmpty() ? binaryRoot : sourcePath.findOwnerRoot(match);
                    FQN2Files fQN2Files = ownerRoot != null ? FQN2Files.forRoot(ownerRoot.toURL()) : null;
                    if (fQN2Files == null || !fQN2Files.check(signature, match.toURL())) {
                        return match;
                    }
                }
            }
        }
        return sourceRoots.length == 0 ? findSource(signature,binaryRoot) : findSource(signature,sourceRoots);
    }

    private static FileObject findMatchingChild(String sourceFileName, Collection<FileObject> folders, boolean caseSensitive) {
        final Match matchSet = caseSensitive ? new CaseSensitiveMatch(sourceFileName) : new CaseInsensitiveMatch(sourceFileName);
        for (FileObject folder : folders) {
            FileObject[] children = folder.getChildren();
            Arrays.sort(children, Comparator.comparing(FileObject::getNameExt)); // for determinism
            for (FileObject child : children) {
                if (matchSet.apply(child)) {
                    return child;
                }
            }
        }
        return null;
    }

    @NonNull
    private static List<Pair<FileObject, ClassPath>> findAllResources(
            @NonNull final String resourceName,
            @NonNull final Predicate<? super FileObject> rootsFilter,
            @NonNull final ClassPath... cps) {
        final List<Pair<FileObject,ClassPath>> result = new ArrayList<>();
        for (ClassPath cp : cps) {
            for (FileObject fo : cp.findAllResources(resourceName)) {
                final FileObject root = cp.findOwnerRoot(fo);
                if (root != null && rootsFilter.test(root)) {
                    result.add(Pair.<FileObject,ClassPath>of(fo, cp));
                }
            }
        }
        return result;
    }

    private static FileObject findSource (final String binaryName, final FileObject... fos) throws IOException {
        final ClassIndexManager cim = ClassIndexManager.getDefault();
        try {
            for (FileObject fo : fos) {
                ClassIndexImpl ci = cim.getUsagesQuery(fo.toURL(), true);
                if (ci != null) {
                    String sourceName = ci.getSourceName(binaryName);
                    if (sourceName != null) {
                        FileObject result = fo.getFileObject(sourceName);
                        if (result != null) {
                            return result;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            //canceled, pass - returns null
        }
        return null;
    }

    private abstract static class Match {

        private final String name;

        Match(final String names) {
            this.name = names;
        }

        final boolean apply(final FileObject fo) {
            if (fo.isFolder()) {
                return false;
            }
            if (fo.getNameExt().equals(name)) {
                return true;
            }
            final String foName = fo.getName();
            return match(foName,name) && isJava(fo);
        }

        protected abstract boolean match(String name1, String name2);

        private boolean isJava(final FileObject fo) {
            return  "java".equalsIgnoreCase(fo.getExt()) && fo.isData(); //NOI18N
        }
    }

    private static class CaseSensitiveMatch extends Match {

        CaseSensitiveMatch(final String name) {
            super(name);
        }

        @Override
        protected boolean match(String name1, String name2) {
            return name1.equals(name2);
        }
    }

    private static class CaseInsensitiveMatch extends Match {

        CaseInsensitiveMatch(final String name) {
            super(name);
        }

        @Override
        protected boolean match(String name1, String name2) {
            return name1.equalsIgnoreCase(name2);
        }
    }

    /**
     * Finds {@link URL} of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * @param element to find the Javadoc for
     * @param cpInfo classpaths used to resolve (currently unused)
     * @return the URL of the javadoc page or null when the javadoc is not available.
     * @deprecated use {@link SourceUtils#getJavadoc(javax.lang.model.element.Element)}
     * or {@link SourceUtils#getPreferredJavadoc(javax.lang.model.element.Element)}
     */
    @Deprecated
    public static URL getJavadoc (final Element element, final ClasspathInfo cpInfo) {
        final Collection<? extends URL> res = getJavadoc(element);
        return res.isEmpty() ?
            null :
            res.iterator().next();
    }

    /**
     * Returns preferred Javadoc {@link URL}.
     * Threading: The method parses the javadoc to find out the used doclet,
     * so it should not be called from EDT.
     * @param element to find the Javadoc for
     * @return the URL of the javadoc page or null when the javadoc is not available.
     * @since 0.134
     */
    @CheckForNull
    public static URL getPreferredJavadoc(@NonNull final Element element) {
        Parameters.notNull("element", element); //NOI18N
        final JavadocHelper.TextStream page = JavadocHelper.getJavadoc(element);
        if (page == null) {
            return null;
        }
        return page.getLocation();
    }

    /**
     * Finds {@link URL}s of a javadoc page for given element when available. This method
     * uses {@link JavadocForBinaryQuery} to find the javadoc page for the give element.
     * For {@link PackageElement} it returns the package-summary.html for given package.
     * Due to the https://bugs.openjdk.java.net/browse/JDK-8025633 there are more possible
     * URLs for {@link ExecutableElement}s, this method returns all of them.
     * @param element to find the Javadoc for
     * @return the URLs of the javadoc page or an empty collection when the javadoc is not available.
     * @since 0.133
     */
    @NonNull
    public static Collection<? extends URL> getJavadoc (
        @NonNull final Element element) {
        Parameters.notNull("element", element); //NOI18N
        final JavadocHelper.TextStream page = JavadocHelper.getJavadoc(element);
        if (page == null) {
            return Collections.<URL>emptySet();
        } else {
            page.close();
            return page.getLocations();
        }
    }

    /**
     * Tests whether the initial scan is in progress.
     */
    public static boolean isScanInProgress () {
        return IndexingManager.getDefault().isIndexing();
    }

    /**
     * Waits for the end of the initial scan, this helper method
     * is designed for tests which require to wait for end of initial scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     * @deprecated use {@link JavaSource#runWhenScanFinished}
     */
    @Deprecated
    public static void waitScanFinished () throws InterruptedException {
        try {
            class T extends UserTask implements ClasspathInfoProvider {
                private final ClassPath EMPTY_PATH = ClassPathSupport.createClassPath(new URL[0]);
                private final ClasspathInfo cpinfo = ClasspathInfo.create(EMPTY_PATH, EMPTY_PATH, EMPTY_PATH);
                @Override
                public void run(ResultIterator resultIterator) throws Exception {
                    // no-op
                }

                @Override
                public ClasspathInfo getClasspathInfo() {
                    return cpinfo;
                }
            }
            Future<Void> f = ParserManager.parseWhenScanFinished(JavacParser.MIME_TYPE, new T());
            if (!f.isDone()) {
                f.get();
            }
        } catch (Exception ex) {
        }
    }


    /**
     * Returns the dependent source path roots for given source root.
     * It returns all the open project source roots which have either
     * direct or transitive dependency on the given source root.
     * @param root to find the dependent roots for
     * @return {@link Set} of {@link URL}s containing at least the
     * incoming root, never returns null.
     * @since 0.10
     */
    @NonNull
    @org.netbeans.api.annotations.common.SuppressWarnings(value = {"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")
    public static Set<URL> getDependentRoots (@NonNull final URL root) {
        return getDependentRoots(root, true);
    }

    /**
     * Returns the dependent source path roots for given source root. It returns
     * all the source roots which have either direct or transitive dependency on
     * the given source root.
     *
     * @param root to find the dependent roots for
     * @param filterNonOpenedProjects true if the results should only contain roots for
     * opened projects
     * @return {@link Set} of {@link URL}s containing at least the incoming
     * root, never returns null.
     * @since 0.110
     */
    @NonNull
    @org.netbeans.api.annotations.common.SuppressWarnings(value = {"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")
    public static Set<URL> getDependentRoots(
        @NonNull final URL root,
        final boolean filterNonOpenedProjects) {
        final FileObject rootFO = URLMapper.findFileObject(root);
        if (rootFO != null) {
            return mapToURLs(QuerySupport.findDependentRoots(rootFO,filterNonOpenedProjects));
        } else {
            return Collections.<URL>singleton(root);
        }
    }

    //Helper methods

    /**
     * Returns true if the given file is a class file
     * @param file
     * @return true if the given file is a class file
     * @since 2.51
     */
    public static boolean isClassFile(@NonNull final FileObject file) {
        return FileObjects.CLASS.equals(file.getExt()) || ClassParser.MIME_TYPE.equals(file.getMIMEType(ClassParser.MIME_TYPE));
    }

    /**
     * Returns classes declared in the given source file which have the main method.
     * @param fo source file
     * @return the classes containing main method
     * @throws IllegalArgumentException when file does not exist or is not a java source file.
     */
    public static Collection<ElementHandle<TypeElement>> getMainClasses (final @NonNull FileObject fo) {
        Parameters.notNull("fo", fo);   //NOI18N
        if (!fo.isValid()) {
            throw new IllegalArgumentException ("FileObject : " + FileUtil.getFileDisplayName(fo) + " is not valid.");  //NOI18N
        }
        if (fo.isVirtual()) {
            throw new IllegalArgumentException ("FileObject : " + FileUtil.getFileDisplayName(fo) + " is virtual.");  //NOI18N
        }
        final JavaSource js = JavaSource.forFileObject(fo);
        if (js == null) {
            throw new IllegalArgumentException ();
        }
        try {
            final LinkedHashSet<ElementHandle<TypeElement>> result = new LinkedHashSet<> ();
            js.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(final CompilationController control) throws Exception {
                    if (control.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED).compareTo (JavaSource.Phase.ELEMENTS_RESOLVED)>=0) {
                        final List<TypeElement>  types = new ArrayList<>();
                        final ElementScanner6<Void,Void> visitor = new ElementScanner14<Void, Void>() {
                            @Override
                            public Void visitType(TypeElement e, Void p) {
                                if (e.getEnclosingElement().getKind() == ElementKind.PACKAGE
                                   || e.getModifiers().contains(Modifier.STATIC)) {
                                    types.add(e);
                                    return super.visitType(e, p);
                                } else {
                                    return null;
                                }
                            }

                        };
                        visitor.scan(control.getTopLevelElements(), null);
                        for (TypeElement type : types) {
                            for (ExecutableElement exec :  ElementFilter.methodsIn(control.getElements().getAllMembers(type))) {
                                if (SourceUtils.isMainMethod(exec)) {
                                    result.add (ElementHandle.create(type));
                                }
                            }
                        }
                    }
                }

            }, true);
            return result;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return Collections.<ElementHandle<TypeElement>>emptySet();
        }
    }

    /**
     * Returns true when the class contains main method.
     * @param qualifiedName the fully qualified name of class
     * @param cpInfo the classpath used to resolve the class
     * @return true when the class contains a main method
     */
    public static boolean isMainClass (final String qualifiedName, ClasspathInfo cpInfo) {
        return isMainClass(qualifiedName, cpInfo, false);
    }

    /**
     * Returns true when the class contains main method.
     * @param qualifiedName the fully qualified name of class
     * @param cpInfo the classpath used to resolve the class
     * @param optimistic when true does only index check without parsing the file.
     * The optimistic check is faster but it works only for source file not for binaries
     * for which index does not exist. It also does not handle inheritance of the main method.
     * @return true when the class contains a main method
     * @since 0.71
     */
    public static boolean isMainClass (final String qualifiedName, ClasspathInfo cpInfo, boolean optimistic) {
        if (qualifiedName == null || cpInfo == null) {
            throw new IllegalArgumentException ();
        }
        //Fast path check by index - main in sources
        for (ClassPath.Entry entry : cpInfo.getClassPath(PathKind.SOURCE).entries()) {
            final Iterable<? extends URL> mainClasses = ExecutableFilesIndex.DEFAULT.getMainClasses(entry.getURL());
            try {
                final URI root = entry.getURL().toURI();
                for (URL mainClass : mainClasses) {
                    try {
                        URI relative = root.relativize(mainClass.toURI());
                        final String resourceNameNoExt = FileObjects.stripExtension(relative.getPath());
                        final String ffqn = FileObjects.convertFolder2Package(resourceNameNoExt,'/');  //NOI18N
                        if (qualifiedName.equals(ffqn)) {
                            final ClassPath bootCp = cpInfo.getClassPath(PathKind.BOOT);
                            if (bootCp.findResource(resourceNameNoExt + '.' + FileObjects.CLASS)!=null) {
                                //Resource in platform, fall back to slow path
                                break;
                            } else {
                                return true;
                            }
                        }
                    } catch (URISyntaxException e) {
                        LOG.log(Level.INFO, "Ignoring fast check for file: {0} due to: {1}", new Object[]{mainClass.toString(), e.getMessage()}); //NOI18N
                    }
                }
            } catch (URISyntaxException e) {
                LOG.log(Level.INFO, "Ignoring fast check for root: {0} due to: {1}", new Object[]{entry.getURL().toString(), e.getMessage()}); //NOI18N
            }
        }

        final boolean[] result = new boolean[]{false};
        if (!optimistic) {
            //Slow path fallback - for main in libraries
            JavaSource js = JavaSource.create(cpInfo);
            try {
                js.runUserActionTask(new Task<CompilationController>() {

                    @Override
                    public void run(CompilationController control) throws Exception {
                        control.toPhase(Phase.ELEMENTS_RESOLVED);
                        final JavacElements elms = (JavacElements)control.getElements();
                        TypeElement type = ElementUtils.getTypeElementByBinaryName(control, qualifiedName);
                        if (type == null) {
                            return;
                        }
                        List<? extends ExecutableElement> methods = ElementFilter.methodsIn(elms.getAllMembers(type));
                        for (ExecutableElement method : methods) {
                            if (SourceUtils.isMainMethod(method)) {
                                result[0] = true;
                                break;
                            }
                        }
                    }

                }, true);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        return result[0];
    }

    /**
     * Returns true if the method is a main method
     * @param method to be checked
     * @return true when the method is a main method
     */
    public static boolean isMainMethod (final ExecutableElement method) {
        if (!mainCandidate(method)) {
            return false;
        }

        Context ctx = ((NBNames) ((Symbol.MethodSymbol)method).name.table.names).getContext();
        Source source = Source.instance(ctx);
        Preview preview = Preview.instance(ctx);

        // old launch protocol before JDK 25
        if (source.compareTo(Source.JDK21) < 0 || (source.compareTo(Source.JDK25) < 0 && !preview.isEnabled())) {
            long flags = ((Symbol.MethodSymbol)method).flags();

            if (((flags & Flags.PUBLIC) == 0) || ((flags & Flags.STATIC) == 0)) {
                return false;
            }
            return !method.getParameters().isEmpty();
        }

        // new launch prototocol from JEP 512:
        int currentMethodPriority = mainMethodPriority(method);
        int highestPriority = Integer.MAX_VALUE;

        for (ExecutableElement sibling : ElementFilter.methodsIn(method.getEnclosingElement().getEnclosedElements())) {
            if (mainCandidate(sibling)) {
                highestPriority = Math.min(highestPriority, mainMethodPriority(sibling));
                if (highestPriority < currentMethodPriority) {
                    break;
                }
            } 
        }

        return currentMethodPriority == highestPriority;
    }

    private static boolean mainCandidate(ExecutableElement method) {
        if (!"main".contentEquals(method.getSimpleName())) {                //NOI18N
            return false;
        }
        long flags = ((Symbol.MethodSymbol)method).flags();
        if ((flags & Flags.PRIVATE) != 0) {
            return false;
        }
        if (method.getReturnType().getKind() != TypeKind.VOID) {
            return false;
        }
        List<? extends VariableElement> params = method.getParameters();
        if (params.size() > 1) {
            return false;
        } else if (params.size() == 1) {
            TypeMirror param = params.get(0).asType();
            if (param.getKind() != TypeKind.ARRAY) {
                return false;
            }
            ArrayType array = (ArrayType) param;
            TypeMirror compound = array.getComponentType();
            if (compound.getKind() != TypeKind.DECLARED) {
                return false;
            }
            if (!"java.lang.String".contentEquals(((TypeElement)((DeclaredType)compound).asElement()).getQualifiedName())) {    //NOI18N
                return false;
            }
        }
        return true;
    }

    // 0 is highest
    private static int mainMethodPriority(ExecutableElement method) {
        long flags = ((Symbol.MethodSymbol)method).flags();
        boolean isStatic = (flags & Flags.STATIC) != 0;
        boolean hasParams = !method.getParameters().isEmpty();
        if (isStatic) {
            return hasParams ? 0 : 1;
        } else {
            return hasParams ? 2 : 3;
        }
    }

    /**
     * Returns classes declared under the given source roots which have the main method.
     * @param sourceRoots the source roots
     * @return the classes containing the main methods
     * Currently this method is not optimized and may be slow
     */
    public static Collection<ElementHandle<TypeElement>> getMainClasses (final FileObject[] sourceRoots) {
        final List<ElementHandle<TypeElement>> result = new LinkedList<> ();
        for (final FileObject root : sourceRoots) {
            try {
                final File rootFile = FileUtil.toFile(root);
                final ClassPath bootPath = ClassPath.getClassPath(root, ClassPath.BOOT);
                final ClassPath compilePath = ClassPath.getClassPath(root, ClassPath.COMPILE);
//                final ClassPath srcPath = ClassPathSupport.createClassPath(new FileObject[] {root});
                final ClassPath srcPath = ClassPath.getClassPath(root, ClassPath.SOURCE);
                final ClassPath systemModules = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_BOOT_PATH);
                final ClassPath modulePath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_COMPILE_PATH);
                final ClassPath allUnnamed = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_CLASS_PATH);
                final ClassPath moduleSourcePath = ClassPath.getClassPath(root, JavaClassPathConstants.MODULE_SOURCE_PATH);
                final ClasspathInfo cpInfo = new ClasspathInfo.Builder(bootPath)
                        .setClassPath(compilePath)
                        .setSourcePath(srcPath)
                        .setModuleBootPath(systemModules)
                        .setModuleCompilePath(modulePath)
                        .setModuleClassPath(allUnnamed)
                        .setModuleSourcePath(moduleSourcePath)
                        .build();
                JavaSource js = JavaSource.create(cpInfo);
                js.runUserActionTask((CompilationController control) -> {
                    control.toPhase(Phase.ELEMENTS_RESOLVED);
                    final URL rootURL = root.toURL();
                    Iterable<? extends URL> mainClasses = ExecutableFilesIndex.DEFAULT.getMainClasses(rootURL);
                    List<ElementHandle<TypeElement>> classes = new LinkedList<>();
                    for (URL mainClass : mainClasses) {
                        File mainFo = BaseUtilities.toFile(URI.create(mainClass.toExternalForm()));
                        if (mainFo.exists()) {
                            classes.addAll(JavaCustomIndexer.getRelatedTypes(mainFo, rootFile));
                        }
                    }
                    for (ElementHandle<TypeElement> cls : classes) {
                        TypeElement te = cls.resolve(control);
                        if (te != null) {
                            Iterable<? extends ExecutableElement> methods = ElementFilter.methodsIn(te.getEnclosedElements());
                            for (ExecutableElement method : methods) {
                                if (isMainMethod(method)) {
                                    if (isIncluded(cls, control.getClasspathInfo())) {
                                        result.add (cls);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }, false);
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
                return Collections.<ElementHandle<TypeElement>>emptySet();
            }
        }
        return result;
    }

    private static boolean isIncluded (final ElementHandle<TypeElement> element, final ClasspathInfo cpInfo) {
        FileObject fobj = getFile(element,cpInfo);
        if (fobj == null) {
            //Not source
            return true;
        }
        ClassPath sourcePath = cpInfo.getClassPath(ClasspathInfo.PathKind.SOURCE);
        for (ClassPath.Entry e : sourcePath.entries()) {
            FileObject root = e.getRoot ();
            if (root != null && FileUtil.isParentOf(root,fobj)) {
                return e.includes(fobj);
            }
        }
        return true;
    }

    private static boolean isCaseSensitive () {
        return ! new File ("a").equals (new File ("A"));    //NOI18N
    }

    /**
     * Returns candidate filenames given a classname.
     * @return a single name (top-level class, no $) or multiple as the JLS permits $ in class names.
     */
    private static List<String> getSourceFileNames(String classFileName) {
        int index = classFileName.lastIndexOf('$');
        if (index == -1) {
            return Collections.singletonList(classFileName);
        }
        List<String> ll = new ArrayList<>(3);
        ll.add(classFileName);
        while (index >= 0) {
            ll.add(classFileName.substring(0, index));
            index = classFileName.lastIndexOf('$', index - 1);
        }
        return ll;
    }

    /**
     * Resolves all captured type variables to their respective wildcards in the given type.
     * @param info CompilationInfo over which the method should work
     * @param tm type to resolve
     * @return resolved type
     *
     * @since 0.136
     */
    public static TypeMirror resolveCapturedType(CompilationInfo info, TypeMirror tm) {
        TypeMirror type = resolveCapturedTypeInt(info, tm);

        if (type.getKind() == TypeKind.WILDCARD) {
            TypeMirror tmirr = ((WildcardType) type).getExtendsBound();
            tmirr = tmirr != null ? tmirr : ((WildcardType) type).getSuperBound();
            if (tmirr != null) {
                return tmirr;
            } else { //no extends, just '?
                TypeElement tel = info.getElements().getTypeElement("java.lang.Object"); // NOI18N
                return tel == null ? null : tel.asType();
            }

        }

        return type;
    }

    private static TypeMirror resolveCapturedTypeInt(CompilationInfo info, TypeMirror tm) {
        if (tm == null) {
            return tm;
        }

        TypeMirror orig = resolveCapturedType(tm);

        if (orig != null) {
            tm = orig;
        }

        if (tm.getKind() == TypeKind.WILDCARD) {
            TypeMirror extendsBound = ((WildcardType) tm).getExtendsBound();
            TypeMirror rct = resolveCapturedTypeInt(info, extendsBound != null ? extendsBound : ((WildcardType) tm).getSuperBound());
            if (rct != null) {
                return rct.getKind() == TypeKind.WILDCARD ? rct : info.getTypes().getWildcardType(extendsBound != null ? rct : null, extendsBound == null ? rct : null);
            }
        }

        if (tm.getKind() == TypeKind.DECLARED) {
            DeclaredType dt = (DeclaredType) tm;
            TypeElement el = (TypeElement) dt.asElement();
            if (((DeclaredType)el.asType()).getTypeArguments().size() != dt.getTypeArguments().size()) {
                return info.getTypes().getDeclaredType(el);
            }

            List<TypeMirror> typeArguments = new LinkedList<>();

            for (TypeMirror t : dt.getTypeArguments()) {
                typeArguments.add(resolveCapturedTypeInt(info, t));
            }

            final TypeMirror enclosingType = dt.getEnclosingType();
            if (enclosingType.getKind() == TypeKind.DECLARED) {
                return info.getTypes().getDeclaredType((DeclaredType) enclosingType, el, typeArguments.toArray(new TypeMirror[0]));
            } else {
                return info.getTypes().getDeclaredType(el, typeArguments.toArray(new TypeMirror[0]));
            }
        }

        if (tm.getKind() == TypeKind.ARRAY) {
            ArrayType at = (ArrayType) tm;
            TypeMirror componentType = resolveCapturedTypeInt(info, at.getComponentType());
            switch (componentType.getKind()) {
                case VOID:
                case EXECUTABLE:
                case WILDCARD:  // heh!
                case PACKAGE:
                    break;
                default:
                    return info.getTypes().getArrayType(componentType);
            }
        }

        return tm;
    }
    /**
     * @since 0.24
     */
    public static WildcardType resolveCapturedType(TypeMirror type) {
        if (type instanceof Type.CapturedType) {
            return ((Type.CapturedType) type).wildcard;
        } else {
            return null;
        }
    }

    /**
     * Returns all elements of the given scope that are declared after given position in a source.
     * @param path to the given search scope
     * @param pos position in the source
     * @param sourcePositions
     * @param trees
     * @return collection of forward references
     *
     * @since 0.136
     */
    public static Collection<? extends Element> getForwardReferences(TreePath path, int pos, SourcePositions sourcePositions, Trees trees) {
        HashSet<Element> refs = new HashSet<>();
        Element el;

        while(path != null) {
            switch(path.getLeaf().getKind()) {
                case VARIABLE:
                    el = trees.getElement(path);
                    if (el != null) {
                        refs.add(el);
                    }
                    TreePath parent = path.getParentPath();
                    if (TreeUtilities.CLASS_TREE_KINDS.contains(parent.getLeaf().getKind())) {
                        boolean isStatic = ((VariableTree)path.getLeaf()).getModifiers().getFlags().contains(Modifier.STATIC);
                        for(Tree member : ((ClassTree)parent.getLeaf()).getMembers()) {
                            if (member.getKind() == Tree.Kind.VARIABLE && sourcePositions.getStartPosition(path.getCompilationUnit(), member) >= pos &&
                                    (isStatic || !((VariableTree)member).getModifiers().getFlags().contains(Modifier.STATIC))) {
                                el = trees.getElement(new TreePath(parent, member));
                                if (el != null) {
                                    refs.add(el);
                                }
                            }
                        }
                    }
                    break;
                case ENHANCED_FOR_LOOP:
                    EnhancedForLoopTree efl = (EnhancedForLoopTree)path.getLeaf();
                    if (sourcePositions.getEndPosition(path.getCompilationUnit(), efl.getExpression()) >= pos) {
                        el = trees.getElement(new TreePath(path, efl.getVariable()));
                        if (el != null) {
                            refs.add(el);
                        }
                    }
            }
            path = path.getParentPath();
        }
        return refs;
    }

    /**
     * Returns names of all modules within given scope.
     * @param info the CompilationInfo used to resolve modules
     * @param scope to search in {@link ClassIndex.SearchScope}
     * @return set of module names
     * @since 2.23
     */
    public static Set<String> getModuleNames(CompilationInfo info, final @NonNull Set<? extends ClassIndex.SearchScopeType> scope) {
        Set<String> ret = new HashSet<>();
        JavaFileManager jfm = info.impl.getJavacTask().getContext().get(JavaFileManager.class);
        if (jfm != null) {
            List<JavaFileManager.Location> toSearch = new ArrayList<>();
            for (ClassIndex.SearchScopeType s : scope) {
                if (s.isSources()) {
                    toSearch.add(StandardLocation.MODULE_SOURCE_PATH);
                }
                if (s.isDependencies()) {
                    toSearch.add(StandardLocation.MODULE_PATH);
                    toSearch.add(StandardLocation.UPGRADE_MODULE_PATH);
                    toSearch.add(StandardLocation.SYSTEM_MODULES);
                }
            }
            try {
                for (JavaFileManager.Location searchLocation : toSearch) {
                    for (Set<JavaFileManager.Location> locations : jfm.listLocationsForModules(searchLocation)) {
                        for (JavaFileManager.Location location : locations) {
                            ret.add(jfm.inferModuleName(location));
                        }
                    }
                }
            } catch (IOException ioe) {}
        }
        return ret;
    }

    /**
     * Returns the name of the module.
     * @param rootUrl the binary root
     * @return the module name or null when no or invalid module
     * @since 2.23
     */
    @CheckForNull
    public static String getModuleName(@NonNull final URL rootUrl) {
        return getModuleName(rootUrl, false);
    }

    /**
     * Returns the name of the module.
     * @param rootUrl the binary root
     * @param canUseSources
     * @return the module name or null when no or invalid module
     * @since 2.23
     */
    @CheckForNull
    public static String getModuleName(
            @NonNull final URL rootUrl,
            @NonNull final boolean canUseSources) {
        return ModuleNames.getInstance().getModuleName(rootUrl, canUseSources);
    }

    /**
     * Returns a module name parsed from the given module-info.java.
     * @param moduleInfo the module-info java file to parse the module name from
     * @return the module name or null
     * @since 2.28
     */
    @CheckForNull
    public static String parseModuleName(
            @NonNull final FileObject moduleInfo) {
        return ModuleNames.parseModuleName(moduleInfo);
    }

    // --------------- Helper methods of getFile () -----------------------------
    private static ClassPath createClassPath (ClasspathInfo cpInfo, PathKind kind) throws MalformedURLException {
	return ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(cpInfo, kind);
    }

    // --------------- End of getFile () helper methods ------------------------------

    @NonNull
    private static Set<URL> mapToURLs(
        @NonNull final Collection<? extends FileObject> fos) {
        final Set<URL> res = new HashSet<>(fos.size());
        for (FileObject fo : fos) {
            res.add(fo.toURL());
        }
        return res;
    }


    private static boolean isPkgOrMdl(@NonNull final ElementKind kind) {
        return kind == ElementKind.PACKAGE || kind == ElementKind.MODULE;
    }

    /**
     * Extracts diagnostic params from a diagnostic. Gets under hood of Javac
     * Diagnostic objects and extracts parameters which are otherwise just used
    * to produce a message. <b>Keep in mind that the positions and types of parameters
     * may change in each nbjavac update!</b>
     * @param d diagnostic
     * @param index parameter index to extract
     * @return parameter value, null if index is out of range
     * @since 2.20
     */
    public static Object getDiagnosticParam(Diagnostic<?> d, int index) {
        return Hacks.getDiagnosticParam(d, index);
    }

    /**
     * Ensure that the given file is parsed from source, in the context of the
     * provided parser instance.
     *
     * Please note this only has an effect before invoking {@link CompilationController#toPhase(org.netbeans.api.java.source.JavaSource.Phase) }.
     *
     * @param cc the parser instance that should be augmented
     * @param file the input source file
     * @since 2.46
     */
    public static void forceSource(CompilationController cc, FileObject file) {
        if (cc.getPhase() != Phase.MODIFIED) {
            throw new IllegalStateException("Must invoke before running toPhase!");
        }
        cc.addForceSource(file);
    }

    /**
     * Computes class name for the corresponding input source file.
     *
     * @param info the ClasspathInfo used to resolve
     * @param relativePath input source file path relative to the corresponding source root
     * @param nestedClass nested class which name is searched
     * @return class name for the corresponding input source file
     * @since 2.74
     */
    public static String classNameFor(ClasspathInfo info, String relativePath, NestedClass nestedClass) {
        ClassPath cachedCP = ClasspathInfoAccessor.getINSTANCE().getCachedClassPath(info, PathKind.COMPILE);
        int idx = relativePath.indexOf('.');
        String rel = idx < 0 ? relativePath : relativePath.substring(0, idx);
        String className = rel.replace('/', '.');
        int lastDotIndex = className.lastIndexOf('.');
        String fqnForNestedClass = null;
        if (lastDotIndex > -1 && nestedClass != null) {
            String packageName = className.substring(0, lastDotIndex);
            fqnForNestedClass = nestedClass.getFQN(packageName, "$");
        }
        FileObject rsFile = cachedCP.findResource(rel + '.' + FileObjects.RS);
        if (rsFile != null) {
            List<String> lines = new ArrayList<>();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(rsFile.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = in.readLine())!=null) {
                    if (className.equals(line)) {
                        return className;
                    } else if (fqnForNestedClass != null && fqnForNestedClass.equals(line)) {
                        return line;
                    }
                    lines.add(line);
                }
            } catch (IOException ioe) {}
            if (!lines.isEmpty()) {
                return lines.get(0);
            }
        }
        return className;
    }
}
