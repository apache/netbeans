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

package org.netbeans.modules.java.editor.overridden;

import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClassIndex.SearchKind;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.java.source.Task;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.TopologicalSortException;
import org.openide.util.Utilities;

/**
 *
 * @author Jan Lahoda
 */
public class ComputeOverriders {

    private static final Logger LOG = Logger.getLogger(ComputeOverriders.class.getName());

    private final AtomicBoolean cancel;

    public ComputeOverriders(AtomicBoolean cancel) {
        this.cancel = cancel;
    }
    
    private static Set<URL> findReverseSourceRoots(final URL thisSourceRoot, Map<URL, List<URL>> sourceDeps, Map<URL, List<URL>> rootPeers, final FileObject thisFile) {
        long startTime = System.currentTimeMillis();

        try {
            //TODO: from SourceUtils (which filters out source roots that do not belong to open projects):
            //Create inverse dependencies
            final Map<URL, List<URL>> inverseDeps = new HashMap<URL, List<URL>> ();
            for (Map.Entry<URL,List<URL>> entry : sourceDeps.entrySet()) {
                final URL u1 = entry.getKey();
                final List<URL> l1 = entry.getValue();
                for (URL u2 : l1) {
                    List<URL> l2 = inverseDeps.get(u2);
                    if (l2 == null) {
                        l2 = new ArrayList<URL>();
                        inverseDeps.put (u2,l2);
                    }
                    l2.add (u1);
                }
            }
            //Collect dependencies
            final Set<URL> result = new HashSet<URL>();
            final LinkedList<URL> todo = new LinkedList<URL> ();
            todo.add (thisSourceRoot);
            List<URL> peers = rootPeers != null ? rootPeers.get(thisSourceRoot) : null;
            if (peers != null)
                todo.addAll(peers);
            while (!todo.isEmpty()) {
                final URL u = todo.removeFirst();
                if (!result.contains(u)) {
                    result.add (u);
                    final List<URL> ideps = inverseDeps.get(u);
                    if (ideps != null) {
                        todo.addAll (ideps);
                    }
                }
            }
            return result;
        } finally {
            long endTime = System.currentTimeMillis();

            Logger.getLogger("TIMER").log(Level.FINE, "Find Reverse Source Roots", //NOI18N
                    new Object[]{thisFile, endTime - startTime});
        }
    }

    private static FileObject findSourceRoot(FileObject file) {
        final ClassPath cp = file != null ? ClassPath.getClassPath(file, ClassPath.SOURCE) : null;
        //Null is a valid value for files which have no source path (default filesystem).
        return cp != null ? cp.findOwnerRoot(file) : null;
    }

    private Set<URL> findBinaryRootsForSourceRoot(FileObject sourceRoot, Map<URL, List<URL>> binaryDeps) {
//      BinaryForSourceQuery.findBinaryRoots(thisSourceRoot).getRoots();
        Set<URL> result = new HashSet<URL>();

        for (URL bin : binaryDeps.keySet()) {
            if (cancel.get()) return Collections.emptySet();
            for (FileObject s : SourceForBinaryQuery.findSourceRoots(bin).getRoots()) {
                if (s == sourceRoot) {
                    result.add(bin);
                }
            }
        }

        return result;
    }

    public Map<ElementHandle<? extends Element>, List<ElementDescription>> process(CompilationInfo info, TypeElement te, ExecutableElement ee, boolean interactive) {
        long startTime = System.currentTimeMillis();

        try {
            return processImpl(info, te, ee, interactive);
        } finally {
            Logger.getLogger("TIMER").log(Level.FINE, "Overridden - Total", //NOI18N
                new Object[] {info.getFileObject(), System.currentTimeMillis() - startTime});
        }
    }

    private Map<ElementHandle<? extends Element>, List<ElementDescription>> processImpl(CompilationInfo info, TypeElement te, ExecutableElement ee, boolean interactive) {
        FileObject file = info.getFileObject();
        FileObject thisSourceRoot;
        if (te != null ) {
            thisSourceRoot = findSourceRoot(SourceUtils.getFile(te, info.getClasspathInfo()));
        } else {
            thisSourceRoot = findSourceRoot(file);
        }
        
        if (thisSourceRoot == null) {
            return null;
        }


        //XXX: special case "this" source root (no need to create a new JS and load the classes again for it):
//        reverseSourceRoots.add(thisSourceRoot);

//        LOG.log(Level.FINE, "reverseSourceRoots: {0}", reverseSourceRoots); //NOI18N

//                if (LOG.isLoggable(Level.FINE)) {
//                    LOG.log(Level.FINE, "method: {0}", ee.toString()); //NOI18N
//                }


        final Map<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>> methods = new HashMap<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>>();

        if (ee == null) {
            if (te == null) {
                fillInMethods(info.getTopLevelElements(), methods);
            } else {
                methods.put(ElementHandle.create(te), Collections.<ElementHandle<ExecutableElement>>emptyList());
            }
        } else {
            TypeElement owner = (TypeElement) ee.getEnclosingElement();

            methods.put(ElementHandle.create(owner), Collections.singletonList(ElementHandle.create(ee)));
        }

        final Map<ElementHandle<? extends Element>, List<ElementDescription>> overriding = new HashMap<ElementHandle<? extends Element>, List<ElementDescription>>();

        long startTime = System.currentTimeMillis();
        long[] classIndexTime = new long[1];
        final Map<URL, Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>> users = computeUsers(info, thisSourceRoot, methods.keySet(), classIndexTime, interactive);
        long endTime = System.currentTimeMillis();

        if (users == null) {
            return null;
        }

        Logger.getLogger("TIMER").log(Level.FINE, "Overridden Candidates - Class Index", //NOI18N
            new Object[] {file, classIndexTime[0]});
        Logger.getLogger("TIMER").log(Level.FINE, "Overridden Candidates - Total", //NOI18N
            new Object[] {file, endTime - startTime});

	FileObject currentFileSourceRoot = findSourceRoot(file);

	if (currentFileSourceRoot != null) {
            URL rootURL = currentFileSourceRoot.toURL();
            Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>> overridingHandles = users.remove(rootURL);

            if (overridingHandles != null) {
                computeOverridingForRoot(rootURL, overridingHandles, methods, overriding);
            }
	}

        for (Map.Entry<URL, Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>> data : users.entrySet()) {
	    computeOverridingForRoot(data.getKey(), data.getValue(), methods, overriding);
        }

	if (cancel.get()) return null;

        return overriding;
    }

    private void computeOverridingForRoot(URL root,
	                                  Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>> overridingHandles,
					  Map<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>> methods,
					  Map<ElementHandle<? extends Element>, List<ElementDescription>> overridingResult) {
	for (Map.Entry<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>> deps : overridingHandles.entrySet()) {
	    if (cancel.get()) return ;
	    findOverriddenAnnotations(root, deps.getValue(), deps.getKey(), methods.get(deps.getKey()), overridingResult);
	}
    }

    private static void fillInMethods(Iterable<? extends TypeElement> types, Map<ElementHandle<TypeElement>, List<ElementHandle<ExecutableElement>>> methods) {
        for (TypeElement te : types) {
            List<ElementHandle<ExecutableElement>> l = new LinkedList<ElementHandle<ExecutableElement>>();

            for (ExecutableElement ee : ElementFilter.methodsIn(te.getEnclosedElements())) {
                l.add(ElementHandle.create(ee));
            }

            methods.put(ElementHandle.create(te), l);

            fillInMethods(ElementFilter.typesIn(te.getEnclosedElements()), methods);
        }
    }
    private Set<ElementHandle<TypeElement>> computeUsers(URL source, Set<ElementHandle<TypeElement>> base, long[] classIndexCumulative) {
        ClasspathInfo cpinfo = ClasspathInfo.create(ClassPath.EMPTY, ClassPath.EMPTY, ClassPathSupport.createClassPath(source));
        
        return computeUsers(cpinfo, ClassIndex.SearchScope.SOURCE, base, classIndexCumulative);
    }
    
    private Set<ElementHandle<TypeElement>> computeUsers(ClasspathInfo cpinfo, SearchScope scope, Set<ElementHandle<TypeElement>> base, long[] classIndexCumulative) {
        long startTime = System.currentTimeMillis();

        try {
            List<ElementHandle<TypeElement>> l = new LinkedList<ElementHandle<TypeElement>>(base);
            Set<ElementHandle<TypeElement>> result = new HashSet<ElementHandle<TypeElement>>();
            Set<ElementHandle<TypeElement>> seen = new HashSet<ElementHandle<TypeElement>>();

            while (!l.isEmpty()) {
                if (cancel.get()) return null;
                
                ElementHandle<TypeElement> eh = l.remove(0);

                if (!seen.add(eh)) continue;

                result.add(eh);
                Set<ElementHandle<TypeElement>> typeElements = cpinfo.getClassIndex().getElements(eh, Collections.singleton(SearchKind.IMPLEMENTORS), EnumSet.of(scope));
                //XXX: Canceling
                if (typeElements != null) {
                    l.addAll(typeElements);
                }
            }
            return result;
        } finally {
            classIndexCumulative[0] += (System.currentTimeMillis() - startTime);
        }
    }

    static List<URL> reverseSourceRootsInOrderOverride;

    private List<URL> reverseSourceRootsInOrder(CompilationInfo info, URL thisSourceRoot, FileObject thisSourceRootFO, Map<URL, List<URL>> sourceDeps, Map<URL, List<URL>> binaryDeps, Map<URL, List<URL>> rootPeers, boolean interactive) {
        if (reverseSourceRootsInOrderOverride != null) {
            return reverseSourceRootsInOrderOverride;
        }

        final Set<URL> sourceRootsSet = new HashSet<>();

        if (sourceDeps.containsKey(thisSourceRoot)) {
            sourceRootsSet.addAll(findReverseSourceRoots(thisSourceRoot, sourceDeps, rootPeers, info.getFileObject()));
        }
        for (URL binary : findBinaryRootsForSourceRoot(thisSourceRootFO, binaryDeps)) {
            final List<URL> deps = binaryDeps.get(binary);
            if (deps != null) {
                sourceRootsSet.addAll(deps);
            }
        }
        List<URL> sourceRoots;
        try {
            sourceRoots = new LinkedList<URL>(Utilities.topologicalSort(sourceDeps.keySet(), sourceDeps));
        } catch (TopologicalSortException ex) {
            if (interactive) {
                Exceptions.attachLocalizedMessage(ex,NbBundle.getMessage(GoToImplementation.class, "ERR_CycleInDependencies"));
                Exceptions.printStackTrace(ex);
            } else {
                LOG.log(Level.FINE, null, ex);
            }
            return null;
        }

        sourceRoots.retainAll(sourceRootsSet);

        Collections.reverse(sourceRoots);

        return sourceRoots;
    }
    
    private Map<URL, Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>> computeUsers(CompilationInfo info, FileObject thisSourceRoot, Set<ElementHandle<TypeElement>> baseHandles, long[] classIndexCumulative, boolean interactive) {
        Map<URL, List<URL>> sourceDeps = getDependencies(false);
        Map<URL, List<URL>> binaryDeps = getDependencies(true);

        if (sourceDeps == null || binaryDeps == null) {
            if (interactive) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(GoToImplementation.class, "ERR_NoDependencies"), NotifyDescriptor.ERROR_MESSAGE);

                DialogDisplayer.getDefault().notifyLater(nd);
            } else {
                LOG.log(Level.FINE, NbBundle.getMessage(GoToImplementation.class, "ERR_NoDependencies"));
            }
            
            return null;
        }

        URL thisSourceRootURL = thisSourceRoot.toURL();

        Map<URL, List<URL>> rootPeers = getRootPeers();
        List<URL> sourceRoots = reverseSourceRootsInOrder(info, thisSourceRootURL, thisSourceRoot, sourceDeps, binaryDeps, rootPeers, interactive);

        if (sourceRoots == null) {
            return null;
        }

        baseHandles = new HashSet<ElementHandle<TypeElement>>(baseHandles);

        for (Iterator<ElementHandle<TypeElement>> it = baseHandles.iterator(); it.hasNext(); ) {
            if (cancel.get()) return null;
            if (it.next().getBinaryName().contentEquals("java.lang.Object")) {
                it.remove();
                break;
            }
        }

        Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>> auxHandles = new HashMap<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>();

        if (!sourceDeps.containsKey(thisSourceRootURL)) {
            Set<URL> binaryRoots = new HashSet<URL>();
            
            for (URL sr : sourceRoots) {
                List<URL> deps = sourceDeps.get(sr);

                if (deps != null) {
                    binaryRoots.addAll(deps);
                }
            }

            binaryRoots.retainAll(binaryDeps.keySet());

            for (ElementHandle<TypeElement> handle : baseHandles) {
                Set<ElementHandle<TypeElement>> types = computeUsers(ClasspathInfo.create(ClassPath.EMPTY, ClassPathSupport.createClassPath(binaryRoots.toArray(new URL[0])), ClassPath.EMPTY), SearchScope.DEPENDENCIES, Collections.singleton(handle), classIndexCumulative);

                if (types == null/*canceled*/ || cancel.get()) {
                    return null;
                }
                
                auxHandles.put(handle, types);
            }
        }
        
        Map<URL, Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>> result = new LinkedHashMap<URL, Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>>();

        for (URL file : sourceRoots) {
            for (ElementHandle<TypeElement> base : baseHandles) {
                if (cancel.get()) return null;
                
                Set<ElementHandle<TypeElement>> baseTypes = new HashSet<ElementHandle<TypeElement>>();

                baseTypes.add(base);

                Set<ElementHandle<TypeElement>> aux = auxHandles.get(base);

                if (aux != null) {
                    baseTypes.addAll(aux);
                }

                for (URL dep : sourceDeps.get(file)) {
                    Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>> depTypesMulti = result.get(dep);
                    Set<ElementHandle<TypeElement>> depTypes = depTypesMulti != null ? depTypesMulti.get(base) : null;

                    if (depTypes != null) {
                        baseTypes.addAll(depTypes);
                    }
                }

                Set<ElementHandle<TypeElement>> types = computeUsers(file, baseTypes, classIndexCumulative);

                if (types == null/*canceled*/ || cancel.get()) {
                    return null;
                }
                
                types.removeAll(baseTypes);

                Map<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>> currentUsers = result.get(file);

                if (currentUsers == null) {
                    result.put(file, currentUsers = new LinkedHashMap<ElementHandle<TypeElement>, Set<ElementHandle<TypeElement>>>());
                }

                currentUsers.put(base, types);
            }
        }

        return result;
    }

    private void findOverriddenAnnotations(
            URL sourceRoot,
            final Set<ElementHandle<TypeElement>> users,
            final ElementHandle<TypeElement> originalType,
            final List<ElementHandle<ExecutableElement>> methods,
            final Map<ElementHandle<? extends Element>, List<ElementDescription>> overriding) {
        if (!users.isEmpty()) {
            FileObject sourceRootFile = URLMapper.findFileObject(sourceRoot);
            ClasspathInfo cpinfo = ClasspathInfo.create(sourceRootFile);

            JavaSource js = JavaSource.create(cpinfo);

            try {
                js.runUserActionTask(new Task<CompilationController>() {
                    public void run(CompilationController controller) throws Exception {
                        controller.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                        Set<Element> seenElements = new HashSet<Element>();
                        Element resolvedOriginalType = originalType.resolve(controller);

                        if (resolvedOriginalType == null) {
                            return ;
                        }
                        
                        for (ElementHandle<TypeElement> typeHandle : users) {
                            if (cancel.get()) return ;
                            TypeElement type = typeHandle.resolve(controller);

                            if (type == null || !seenElements.add(type)) {
                                continue;
                            }

                            Types types = controller.getTypes();

                            if (types.isSubtype(types.erasure(type.asType()), types.erasure(resolvedOriginalType.asType()))) {
                                List<ElementDescription> classOverriders = overriding.get(originalType);

                                if (classOverriders == null) {
                                    overriding.put(originalType, classOverriders = new LinkedList<ElementDescription>());
                                }

                                classOverriders.add(new ElementDescription(controller, type, true));

                                for (ElementHandle<ExecutableElement> originalMethodHandle : methods) {
                                    ExecutableElement originalMethod = originalMethodHandle.resolve(controller);

                                    if (originalMethod != null) {
                                        ExecutableElement overrider = getImplementationOf(controller, originalMethod, type);

                                        if (overrider == null) {
                                            continue;
                                        }

                                        List<ElementDescription> overriddingMethods = overriding.get(originalMethodHandle);

                                        if (overriddingMethods == null) {
                                            overriding.put(originalMethodHandle, overriddingMethods = new ArrayList<ElementDescription>());
                                        }

                                        overriddingMethods.add(new ElementDescription(controller, overrider, true));
                                    } else {
                                        Logger.getLogger("global").log(Level.SEVERE, "IsOverriddenAnnotationHandler: originalMethod == null!"); //NOI18N
                                    }
                                }
                            }
                        }
                    }
                }, true);
            } catch (Exception e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private static ExecutableElement getImplementationOf(CompilationInfo info, ExecutableElement overridee, TypeElement implementor) {
        for (ExecutableElement overrider : ElementFilter.methodsIn(implementor.getEnclosedElements())) {
            if (info.getElements().overrides(overrider, overridee, implementor)) {
                return overrider;
            }
        }

        return null;
    }

    static Map<URL, List<URL>> dependenciesOverride;
    
    private static Map<URL, List<URL>> getDependencies(boolean binary) {
        if (dependenciesOverride != null) {
            return dependenciesOverride;
        }
        
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

        if (l == null) {
            return null;
        }

        Class<?> clazz = null;
        String method = null;

        try {
            clazz = l.loadClass("org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController");
            method = binary ? "getBinaryRootDependencies" : "getRootDependencies";
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GoToImplementation.class.getName()).log(Level.FINE, null, ex);
            try {
                clazz = l.loadClass("org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater");
                method = binary ? "getDependencies" : "doesnotexist";
            } catch (ClassNotFoundException inner) {
                Logger.getLogger(GoToImplementation.class.getName()).log(Level.FINE, null, inner);
                return null;
            }
        }

        try {
            Method getDefault = clazz.getDeclaredMethod("getDefault");
            Object instance = getDefault.invoke(null);
            Method dependenciesMethod = clazz.getDeclaredMethod(method);

            return (Map<URL, List<URL>>) dependenciesMethod.invoke(instance);
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException | ClassCastException ex) {
            Logger.getLogger(GoToImplementation.class.getName()).log(Level.FINE, null, ex);
            return null;
        }
    }

    static Map<URL, List<URL>> rootPeers;

    private static Map<URL, List<URL>> getRootPeers() {
        if (rootPeers != null) {
            return rootPeers;
        }
        
        ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);

        if (l == null) {
            return null;
        }

        Class<?> clazz = null;
        String method = null;

        try {
            clazz = l.loadClass("org.netbeans.modules.parsing.impl.indexing.friendapi.IndexingController");
            method = "getRootPeers";
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(GoToImplementation.class.getName()).log(Level.FINE, null, ex);
            return null;
        }

        try {
            Method getDefault = clazz.getDeclaredMethod("getDefault");
            Object instance = getDefault.invoke(null);
            Method peersMethod = clazz.getDeclaredMethod(method);

            return (Map<URL, List<URL>>) peersMethod.invoke(instance);
        } catch (ReflectiveOperationException | IllegalArgumentException | SecurityException | ClassCastException ex) {
            Logger.getLogger(GoToImplementation.class.getName()).log(Level.FINE, null, ex);
            return null;
        }
    }
}
