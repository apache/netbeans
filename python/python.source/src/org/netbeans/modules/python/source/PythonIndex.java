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
package org.netbeans.modules.python.source;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.parsing.spi.indexing.PathRecognizer;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind.CAMEL_CASE;
import static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind.CASE_INSENSITIVE_PREFIX;
import static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind.CASE_INSENSITIVE_REGEXP;
import static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind.PREFIX;
import static org.netbeans.modules.parsing.spi.indexing.support.QuerySupport.Kind.REGEXP;
import org.netbeans.modules.python.source.elements.IndexedElement;
import org.netbeans.modules.python.api.PythonPlatform;
import org.netbeans.modules.python.api.PythonPlatformManager;
import org.netbeans.modules.python.source.impl.QuerySupportFactory;
import org.netbeans.modules.python.source.elements.IndexedPackage;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.python.antlr.ast.Import;
import org.python.antlr.ast.ImportFrom;
import org.python.antlr.ast.alias;

public class PythonIndex {
//    public static final Set<SearchScope> ALL_SCOPE = EnumSet.allOf(SearchScope.class);
//    public static final Set<SearchScope> SOURCE_SCOPE = EnumSet.of(SearchScope.SOURCE);
    static final String CLUSTER_URL = "cluster:"; // NOI18N
    static final String PYTHONHOME_URL = "python:"; // NOI18N
    private static final String STUB_MISSING = "stub_missing"; // NOI18N

    // The "functions" module is always imported by the interpreter, and ditto
    // for exceptions, constants, etc.
    public static Set<String> BUILTIN_MODULES = new HashSet<>();


    private static final Logger LOG = Logger.getLogger(PythonIndex.class.getName());
    public static final String OBJECT = "object"; // NOI18N
    static Map<String, Set<String>> wildcardImports = new HashMap<>();
    static Set<String> systemModules;
    // TODO - make weak?
    static Set<String> availableClasses;
    private static String clusterUrl = null;
    static {
        //BUILTIN_MODULES.add("objects"); // NOI18N -- just links to the others
        BUILTIN_MODULES.add("stdtypes"); // NOI18N
        //BUILTIN_MODULES.add("types"); // NOI18N
        BUILTIN_MODULES.add("exceptions"); // NOI18N
        BUILTIN_MODULES.add("functions"); // NOI18N
        BUILTIN_MODULES.add("constants"); // NOI18N
    }

    public static PythonIndex get(Collection<FileObject> roots) {
        // XXX no cache - is it needed?
        LOG.log(Level.FINE, "PythonIndex for roots: {0}", roots); //NOI18N
        return new PythonIndex(QuerySupportFactory.getQuerySupport(roots), false);
    }
    
    public static PythonIndex get(Project project) {
        Set<String> sourceIds = new HashSet<>();
        Set<String> libraryIds = new HashSet<>();
        Collection<? extends PathRecognizer> lookupAll = Lookup.getDefault().lookupAll(PathRecognizer.class);
        for (PathRecognizer pathRecognizer : lookupAll) {
            Set<String> source = pathRecognizer.getSourcePathIds();
            if (source != null) {
                sourceIds.addAll(source);
            }
            Set<String> library = pathRecognizer.getLibraryPathIds();
            if (library != null) {
                libraryIds.addAll(library);
            }
        }

        final Collection<FileObject> findRoots = QuerySupport.findRoots(project,
                sourceIds,
                libraryIds,
                Collections.<String>emptySet());
        return PythonIndex.get(findRoots);
    }
    
    private static final WeakHashMap<FileObject, PythonIndex> INDEX_CACHE = new WeakHashMap<>();
    public static PythonIndex get(FileObject fo) {
        PythonIndex index = INDEX_CACHE.get(fo);
        if (index == null) {
            LOG.log(Level.FINE, "Creating PythonIndex for FileObject: {0}", fo); //NOI18N
            index = new PythonIndex(QuerySupportFactory.getQuerySupport(fo), true);
            INDEX_CACHE.put(fo, index);
        }
        return index;
    }

    public static boolean isBuiltinModule(String module) {
        return BUILTIN_MODULES.contains(module) || STUB_MISSING.equals(module);
    }

    // For testing only
    public static void setClusterUrl(String url) {
        clusterUrl = url;
    }

    static String getPreindexUrl(String url) {
        // TODO - look up the correct platform to use!
        final PythonPlatformManager manager = PythonPlatformManager.getInstance();
        final String platformName = manager.getDefaultPlatform();
        PythonPlatform platform = manager.getPlatform(platformName);
        if (platform != null) {
            String s = platform.getHomeUrl();
            if (s != null) {
                if (url.startsWith(s)) {
                    url = PYTHONHOME_URL + url.substring(s.length());
                    return url;
                }
            }
        }
        
        String s = getClusterUrl();
        
        if (url.startsWith(s)) {
            return CLUSTER_URL + url.substring(s.length());
        }
        
        if (url.startsWith("jar:file:")) { // NOI18N
            String sub = url.substring(4);
            if (sub.startsWith(s)) {
                return CLUSTER_URL + sub.substring(s.length());
            }
        }
        
        return url;
    }

/** Get the FileObject corresponding to a URL returned from the index */
    public static FileObject getFileObject(String url) {
        return getFileObject(url, null);
    }

    public static FileObject getFileObject(String url, FileObject context) {
        try {
            if (url.startsWith(PYTHONHOME_URL)) {
                Iterator<String> it = null;
                
                // TODO - look up the right platform for the given project
                //if (context != null) {
                //    Project project = FileOwnerQuery.getOwner(context);
                //    if (project != null) {
                //        PythonPlatform platform = PythonPlatform.platformFor(project);
                //        if (platform != null) {
                //            it = Collections.singleton(platform).iterator();
                //        }
                //    }
                //}
                
                PythonPlatformManager manager = PythonPlatformManager.getInstance();
                if (it == null) {
                    it = manager.getPlatformList().iterator();
                }
                while (it.hasNext()) {
                    String name = it.next();
                    PythonPlatform platform = manager.getPlatform(name);
                    if (platform != null) {
                        String u = platform.getHomeUrl();
                        if (u != null) {
                            try {
                                u = u + url.substring(PYTHONHOME_URL.length());
                                FileObject fo = URLMapper.findFileObject(new URL(u));
                                if (fo != null) {
                                    return fo;
                                }
                            } catch (MalformedURLException mue) {
                                Exceptions.printStackTrace(mue);
                            }
                        }
                    }
                }
                
                return null;
            } else if (url.startsWith(CLUSTER_URL)) {
                url = getClusterUrl() + url.substring(CLUSTER_URL.length()); // NOI18N
                if (url.contains(".egg!/")) { // NOI18N
                    url = "jar:" + url; // NOI18N
                }
            }
            
            return URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        
        return null;
    }
    
    static String getClusterUrl() {
        if (clusterUrl == null) {
            File f =
                    InstalledFileLocator.getDefault().locate("modules/org-netbeans-modules-python-editor.jar", null, false); // NOI18N
            
            if (f == null) {
                throw new RuntimeException("Can't find cluster");
            }
            
            f = new File(f.getParentFile().getParentFile().getAbsolutePath());
            
            try {
                f = f.getCanonicalFile();
                clusterUrl = f.toURI().toURL().toExternalForm();
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }

        return clusterUrl;
    }

    private final QuerySupport index;
    private final boolean updateCache;
    
    /** Creates a new instance of PythonIndex */
    private PythonIndex(QuerySupport index, boolean updateCache) {
        this.index = index;
        this.updateCache = updateCache;
    }
    
    private boolean search(String fieldName, String fieldValue, QuerySupport.Kind kind, Set<? super IndexResult> result, final String... fieldsToLoad) {
        try {
            result.addAll(index.query(fieldName, fieldValue, kind, fieldsToLoad));
            return true;
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);

            return false;
        } catch (UnsupportedOperationException iuoe) {
            return false;
        }
    }

    public Set<IndexedElement> getModules(String name, final QuerySupport.Kind kind) {
        final Set<IndexResult> result = new HashSet<>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }

        // TODO - handle case insensitive searches etc?
        String field = PythonIndexer.FIELD_MODULE_NAME;

        search(field, name, kind, result, PythonIndexer.FIELD_MODULE_ATTR_NAME, PythonIndexer.FIELD_MODULE_NAME);

        final Set<IndexedElement> modules = new HashSet<>();

        for (IndexResult map : result) {
            URL url = map.getUrl();
            if (url == null) {
                continue;
            }
            String path = url.toExternalForm();
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
            if (STUB_MISSING.equals(module)) {
                continue;
            }

            IndexedElement element = new IndexedElement(module, ElementKind.MODULE, path, null, null, null);

            String attrs = map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME);
            if (attrs != null && attrs.indexOf('D') != -1) {
                element.setFlags(IndexedElement.DEPRECATED);
            }

            String rhs = path.substring(path.lastIndexOf('/') + 1);
            element.setRhs(rhs);
            modules.add(element);
        }

        return modules;
    }

    public Set<IndexedPackage> getPackages(String name, final QuerySupport.Kind kind) {
        final Set<IndexResult> result = new HashSet<>();

        String field = PythonIndexer.FIELD_MODULE_NAME;
        search(field, name, kind, result, PythonIndexer.FIELD_MODULE_NAME);

        final Set<IndexedPackage> packages = new HashSet<>();

        for (IndexResult map : result) {
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);

            String pkgName = null;
            String pkg = null;

            int nextNextDot = -1;
            int lastDot = module.lastIndexOf('.');
            int nameLength = name.length();
            if (nameLength < lastDot) {
                int nextDot = module.indexOf('.', nameLength);
                if (nextDot != -1) {
                    pkg = module.substring(0, nextDot);
                    nextNextDot = module.indexOf('.', nextDot + 1);
                    int start = module.lastIndexOf('.', name.length());
                    if (start == -1) {
                        start = 0;
                    } else {
                        start++;
                    }
                    pkgName = module.substring(start, nextDot);
                }
            } else if (lastDot != -1) {
                pkgName = module.substring(lastDot + 1);
                pkg = module;
            }

            if (pkgName != null) {
                String url = map.getUrl().toExternalForm();
                IndexedPackage element = new IndexedPackage(pkgName, pkg, url, nextNextDot != -1);
                element.setRhs("");
                packages.add(element);
            }
        }

        return packages;
    }
    
    public Set<IndexedElement> getClasses(String name, final QuerySupport.Kind kind, PythonParserResult context, boolean includeDuplicates) {
        final Set<IndexResult> result = new HashSet<>();

        //        if (!isValid()) {
        //            LOGGER.fine(String.format("LuceneIndex[%s] is invalid!\n", this.toString()));
        //            return;
        //        }
        String field;

        switch (kind) {
            case EXACT:
            case PREFIX:
            case CAMEL_CASE:
            case REGEXP:
                field = PythonIndexer.FIELD_CLASS_NAME;
                
                break;
                
            case CASE_INSENSITIVE_PREFIX:
            case CASE_INSENSITIVE_REGEXP:
            case CASE_INSENSITIVE_CAMEL_CASE:
                field = PythonIndexer.FIELD_CASE_INSENSITIVE_CLASS_NAME;
                
                break;
                
            default:
                throw new UnsupportedOperationException(kind.toString());
        }

        search(field, name, kind, result, PythonIndexer.FIELD_IN, PythonIndexer.FIELD_CLASS_ATTR_NAME, PythonIndexer.FIELD_CLASS_NAME);

        Set<String> uniqueClasses = includeDuplicates ? null : new HashSet<String>();

        final Set<IndexedElement> classes = new HashSet<>();

        for (IndexResult map : result) {
            String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            if (clz == null) {
                // A module without classes
                continue;
            }
            String url = map.getUrl().toExternalForm();
            String module = map.getValue(PythonIndexer.FIELD_IN);
            boolean isBuiltin = isBuiltinModule(module);

            String fqn = clz; // No further namespaces in Python, right?
            if (!includeDuplicates) {
                if (!uniqueClasses.contains(fqn)) { // use a map to point right to the class
                    uniqueClasses.add(fqn);
                    IndexedElement element = new IndexedElement(clz, ElementKind.CLASS, url, module, null, null);
                    if (isBuiltin) {
                        element.setRhs("<i>builtin</i>");
                    }
                    String attrs = map.getValue(PythonIndexer.FIELD_CLASS_ATTR_NAME);
                    if (attrs != null) {
                        int flags = IndexedElement.decode(attrs, 0, 0);
                        element.setFlags(flags);
                    }
                    element.setInherited(true);

                    classes.add(element);
                } // else: Possibly pick the best version... based on which items have documentation attributes etc.
            } else {
                IndexedElement element = new IndexedElement(clz, ElementKind.CLASS, url, module, null, null);
                classes.add(element);
            }
        }

        return classes;
    }

//    /** Return the most distant method in the hierarchy that is overriding the given method, or null
//     * @todo Make this method actually compute most distant ancestor
//     * @todo Use arglist arity comparison to reject methods that are not overrides...
//     */
//    public IndexedMethod getOverridingMethod(String className, String methodName) {
//        Set<IndexedElement> methods = getInheritedElements(className, methodName, QuerySupport.Kind.EXACT);
//
//        // TODO - this is only returning ONE match, not the most distant one. I really need to
//        // produce a PythonIndex method for this which can walk in there and do a decent job!
//
//        for (IndexedElement method : methods) {
//            if (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) {
//                // getInheritedMethods may return methods ON fqn itself
//                if (!method.getIn().equals(className)) {
//                    return (IndexedMethod)method;
//                }
//            }
//        }
//
//        return null;
//    }
    /** Get the super implementation of the given method */
    public Set<IndexedElement> getOverridingMethods(String className, String function) {
        Set<IndexedElement> methods = getInheritedElements(className, function, QuerySupport.Kind.EXACT, true);

        // TODO - remove all methods that are in the same file
        if (methods.size() > 0) {
            Set<IndexedElement> result = new HashSet<>(methods.size());
            for (IndexedElement element : methods) {
                if (!className.equals(element.getClz())) {
                    result.add(element);
                }
            }
            methods = result;
        }

        return methods;
//        // TODO - this is only returning ONE match, not the most distant one. I really need to
//        // produce a PythonIndex method for this which can walk in there and do a decent job!
//
//        for (IndexedElement method : methods) {
//            if (method.getKind() == ElementKind.METHOD || method.getKind() == ElementKind.CONSTRUCTOR) {
//                // getInheritedMethods may return methods ON fqn itself
//                if (!method.getIn().equals(className)) {
//                    return (IndexedMethod)method;
//                }
//            }
//        }
//
//        return null;
    }

    /** Get the super class of the given class */
    public Set<IndexedElement> getSuperClasses(String className) {
        final Set<IndexResult> result = new HashSet<>();

        search(PythonIndexer.FIELD_CLASS_NAME, className, QuerySupport.Kind.EXACT, result, PythonIndexer.FIELD_EXTENDS_NAME, PythonIndexer.FIELD_CLASS_NAME);

        Set<String> classNames = new HashSet<>();
        for (IndexResult map : result) {
            String[] extendsClasses = map.getValues(PythonIndexer.FIELD_EXTENDS_NAME);
            if (extendsClasses != null && extendsClasses.length > 0) {
                for (String clzName : extendsClasses) {
                    classNames.add(clzName);
                }
            }
        }

        String[] terms = { PythonIndexer.FIELD_IN, PythonIndexer.FIELD_CLASS_NAME };

        Set<IndexedElement> superClasses = new HashSet<>();

        for (String superClz : classNames) {
            result.clear();
            search(PythonIndexer.FIELD_CLASS_NAME, superClz, QuerySupport.Kind.EXACT, result, terms);
            for (IndexResult map : result) {
                assert superClz.equals(map.getValue(PythonIndexer.FIELD_CLASS_NAME));
                String url = map.getUrl().toExternalForm();
                String module = map.getValue(PythonIndexer.FIELD_IN);
                IndexedElement clz = new IndexedElement(superClz, ElementKind.CLASS, url, module, null, null);
                superClasses.add(clz);
            }
        }

        return superClasses;
    }

    /**
     * Get the set of inherited (through super classes and mixins) for the given fully qualified class name.
     * @param classFqn FQN: module1::module2::moduleN::class
     * @param prefix If kind is QuerySupport.Kind.PREFIX/CASE_INSENSITIVE_PREFIX, a prefix to filter methods by. Else,
     *    if kind is QuerySupport.Kind.EXACT filter methods by the exact name.
     * @param kind Whether the prefix field should be taken as a prefix or a whole name
     */
    public Set<IndexedElement> getInheritedElements(String classFqn, String prefix, QuerySupport.Kind kind) {
        return getInheritedElements(classFqn, prefix, kind, false);
    }

    public Set<IndexedElement> getInheritedElements(String classFqn, String prefix, QuerySupport.Kind kind, boolean includeOverrides) {
        boolean haveRedirected = false;

        if (classFqn == null) {
            classFqn = OBJECT;
            haveRedirected = true;
        }

        //String field = PythonIndexer.FIELD_FQN_NAME;
        Set<IndexedElement> elements = new HashSet<>();
        Set<String> scannedClasses = new HashSet<>();
        Set<String> seenSignatures = new HashSet<>();

        if (prefix == null) {
            prefix = "";
        }

//        String searchUrl = null;
//        if (context != null) {
//            try {
//                searchUrl = context.getFile().getFileObject().getURL().toExternalForm();
//            } catch (FileStateInvalidException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }

        addMethodsFromClass(prefix, kind, classFqn, elements, seenSignatures, scannedClasses,
                haveRedirected, false, includeOverrides, 0);

        return elements;
    }

    /** Return whether the specific class referenced (classFqn) was found or not. This is
     * not the same as returning whether any classes were added since it may add
     * additional methods from parents (Object/Class).
     */
    private boolean addMethodsFromClass(String prefix, QuerySupport.Kind kind, String classFqn, Set<IndexedElement> elements, Set<String> seenSignatures, Set<String> scannedClasses, boolean haveRedirected, boolean inheriting, boolean includeOverrides, int depth) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = PythonIndexer.FIELD_CLASS_NAME;

        Set<IndexResult> result = new HashSet<>();

        String[] terms = {PythonIndexer.FIELD_IN,
                          PythonIndexer.FIELD_EXTENDS_NAME,
                          PythonIndexer.FIELD_MEMBER,
                          PythonIndexer.FIELD_CLASS_NAME};
        
        
        search(searchField, classFqn, QuerySupport.Kind.EXACT, result, terms);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        List<String> extendsClasses = null;

        String classIn = null;
        int fqnIndex = classFqn.lastIndexOf("::"); // NOI18N

        if (fqnIndex != -1) {
            classIn = classFqn.substring(0, fqnIndex);
        }
        int prefixLength = prefix.length();

        for (IndexResult map : result) {
            assert map != null;

            String url = map.getUrl().toExternalForm();
            String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            String module = map.getValue(PythonIndexer.FIELD_IN);

            if (extendsClasses == null) {
                String[] ext = map.getValues(PythonIndexer.FIELD_EXTENDS_NAME);
                if (ext != null && ext.length > 0) {
                    if (extendsClasses == null) {
                        extendsClasses = Arrays.asList(ext);
                    } else {
                        extendsClasses = new ArrayList<>(extendsClasses);
                        extendsClasses.addAll(Arrays.asList(ext));
                    }
                }
            }

            String[] members = map.getValues(PythonIndexer.FIELD_MEMBER);

            if (members != null) {
                for (String signature : members) {
                    // Prevent duplicates when method is redefined
                    if (includeOverrides || !seenSignatures.contains(signature)) {
                        if (signature.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                if (signature.charAt(prefixLength) != ';') {
                                    continue;
                                }
                            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, prefix, 0, prefix.length())) {
                                continue;
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                        (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                            }

                            if (!includeOverrides) {
                                seenSignatures.add(signature);
                            }
                            IndexedElement element = IndexedElement.create(signature, module, url, clz);
                            // TODO - filter out private? Or let code completer do that? Probably should, in case
                            // we have more rights when inheriting
                            element.setSmart(!haveRedirected);
                            element.setInherited(inheriting);
                            if (includeOverrides) {
                                element.setOrder(depth);
                            }
                            elements.add(element);
                        }
                    }
                }
            }
        }

        if (classFqn.equals(OBJECT)) {
            return foundIt;
        }

        if (extendsClasses == null || extendsClasses.size() == 0) {
            addMethodsFromClass(prefix, kind, OBJECT, elements, seenSignatures, scannedClasses,
                    true, true, includeOverrides, depth + 1);
        } else {
            // We're not sure we have a fully qualified path, so try some different candidates
            for (String extendsClass : extendsClasses) {
                if (!addMethodsFromClass(prefix, kind, extendsClass, elements, seenSignatures,
                        scannedClasses, haveRedirected, true, includeOverrides, depth + 1)) {
                    // Search by classIn
                    String fqn = classIn;

                    while (fqn != null) {
                        if (addMethodsFromClass(prefix, kind, fqn + "::" + extendsClass, elements,
                                seenSignatures, scannedClasses, haveRedirected, true, includeOverrides, depth + 1)) {
                            break;
                        }

                        int f = fqn.lastIndexOf("::"); // NOI18N

                        if (f == -1) {
                            break;
                        } else {
                            fqn = fqn.substring(0, f);
                        }
                    }
                }
            }
        }

        return foundIt;
    }
    
    
    public Set<IndexedElement> getAllMembers(String name, QuerySupport.Kind kind, PythonParserResult context, boolean includeDuplicates) {
        final Set<IndexResult> result = new HashSet<>();
        // TODO - handle case sensitivity better...
        String field = PythonIndexer.FIELD_MEMBER;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        String searchUrl = null;
        if (context != null) {
            searchUrl = context.getSnapshot().getSource().getFileObject().toURL().toExternalForm();
        }

        String[] terms = {PythonIndexer.FIELD_IN,
                          PythonIndexer.FIELD_EXTENDS_NAME,
                          PythonIndexer.FIELD_MEMBER,
                          PythonIndexer.FIELD_CLASS_NAME};

        search(field, name, kind, result, terms);

//        Set<String> uniqueClasses = null;
//        if (includeDuplicates) {
//            uniqueClasses = null;
//        } else if (uniqueClasses == null) {
//            uniqueClasses = new HashSet<String>();
//        }

        final Set<IndexedElement> members = new HashSet<>();
        int nameLength = name.length();

        for (IndexResult map : result) {
            String[] signatures = map.getValues(PythonIndexer.FIELD_MEMBER);
            if (signatures != null && signatures.length > 0) {
                String url = map.getUrl().toExternalForm();
                String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                String module = map.getValue(PythonIndexer.FIELD_IN);
                boolean inherited = searchUrl == null || !searchUrl.equals(url);

                for (String signature : signatures) {
                    if (originalKind == QuerySupport.Kind.EXACT) {
                        if (signature.charAt(nameLength) != ';') {
                            continue;
                        }
                    } else if (originalKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else {
                        // REGEXP, CAMELCASE filtering etc. not supported here
                        assert (originalKind == QuerySupport.Kind.PREFIX) ||
                                (originalKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                    }

                    IndexedElement element = IndexedElement.create(signature, module, url, clz);
                    element.setInherited(inherited);
                    members.add(element);
                }
            }
        }

        return members;
    }
    
    public Set<IndexedElement> getAllElements(String name, QuerySupport.Kind kind, PythonParserResult context, boolean includeDuplicates) {
        final Set<IndexResult> result = new HashSet<>();
        // TODO - handle case sensitivity better...
        String field = PythonIndexer.FIELD_ITEM;
        QuerySupport.Kind originalKind = kind;
        if (kind == QuerySupport.Kind.EXACT) {
            // I can't do exact searches on methods because the method
            // entries include signatures etc. So turn this into a prefix
            // search and then compare chopped off signatures with the name
            kind = QuerySupport.Kind.PREFIX;
        }

        String searchUrl = null;
        if (context != null) {
            searchUrl = context.getSnapshot().getSource().getFileObject().toURL().toExternalForm();
        }

        String[] terms = { PythonIndexer.FIELD_ITEM,
                           PythonIndexer.FIELD_MODULE_NAME };

        search(field, name, kind, result, terms);

        final Set<IndexedElement> elements = new HashSet<>();
        int nameLength = name.length();

        for (IndexResult map : result) {
            String[] signatures = map.getValues(PythonIndexer.FIELD_ITEM);
            if (signatures != null && signatures.length > 0) {
                String url = map.getUrl().toExternalForm();
                String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
                boolean inherited = searchUrl == null || !searchUrl.equals(url);

                for (String signature : signatures) {
                    if (originalKind == QuerySupport.Kind.EXACT) {
                        if (signature.charAt(nameLength) != ';') {
                            continue;
                        }
                    } else if (originalKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, name, 0, name.length())) {
                        continue;
                    } else {
                        // REGEXP, CAMELCASE filtering etc. not supported here
                        assert (originalKind == QuerySupport.Kind.PREFIX) ||
                                (originalKind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                    }

                    IndexedElement element = IndexedElement.create(signature, module, url, null);
                    if (element.isPrivate() && !url.equals(searchUrl)) {
                        continue;
                    }
                    element.setInherited(inherited);
                    elements.add(element);
                }
            }
        }

        return elements;
    }

    public Set<String> getBuiltinSymbols() {
        Set<String> modules = new HashSet<>();

        // The "functions" module is always imported by the interpreter, and ditto
        // for exceptions, constants, etc.
        //modules.add("objects"); // NOI18N -- just links to the others
        modules.addAll(BUILTIN_MODULES);

        Set<String> symbols = new HashSet<>(250);

        String[] terms = { PythonIndexer.FIELD_MODULE_NAME,
                           PythonIndexer.FIELD_ITEM };

        // Look up all symbols
        for (String module : modules) {
            final Set<IndexResult> result = new HashSet<>();
            // TODO - handle case sensitivity better...
            String field = PythonIndexer.FIELD_MODULE_NAME;
            QuerySupport.Kind kind = QuerySupport.Kind.EXACT;

            search(field, module, kind, result, terms);

            for (IndexResult map : result) {
                String[] signatures = map.getValues(PythonIndexer.FIELD_ITEM);
                if (signatures != null) {
                    for (String signature : signatures) {
                        int semi = signature.indexOf(';');
                        assert semi != -1;
                        int flags = IndexedElement.decode(signature, semi + 3, 0);
                        if ((flags & IndexedElement.PRIVATE) != 0) {
                            // Skip private symbols - can't import those
                            continue;
                        }
                        String name = signature.substring(0, semi);
                        symbols.add(name);
                    }
                }
            }
        }

        // Computed as described below
        String[] MISSING = {
            "Ellipsis", "False", "IndentationError", "None", "NotImplemented", "TabError", // NOI18N
            "True", "__debug__", "__doc__", "__name__", "copyright", "credits", "exit", "license", // NOI18N
            "quit" // NOI18N
        };
        for (String s : MISSING) {
            symbols.add(s);
        }
        symbols.add("__builtins__"); // NOI18N
        symbols.add("__file__"); // NOI18N

        //// COMPUTING MISSING SYMBOLS:
        //// My builtin .rst files don't seem to define all the builtin symbols that the Python
        //// interpreter is configured with.  I generated these pretty trivially; run
        //// python and type "dir(__builtins__)" and you end up a list like the below:
        ////String[] EXTRA_BUILTINS = {"ArithmeticError", "AssertionError", "AttributeError", "BaseException",
        //    "DeprecationWarning", "EOFError", "Ellipsis", "EnvironmentError", "Exception", "False",
        //    "FloatingPointError", "FutureWarning", "GeneratorExit", "IOError", "ImportError",
        //    "ImportWarning", "IndentationError", "IndexError", "KeyError", "KeyboardInterrupt",
        //    "LookupError", "MemoryError", "NameError", "None", "NotImplemented", "NotImplementedError",
        //    "OSError", "OverflowError", "PendingDeprecationWarning", "ReferenceError", "RuntimeError",
        //    "RuntimeWarning", "StandardError", "StopIteration", "SyntaxError", "SyntaxWarning", "SystemError",
        //    "SystemExit", "TabError", "True", "TypeError", "UnboundLocalError", "UnicodeDecodeError",
        //    "UnicodeEncodeError", "UnicodeError", "UnicodeTranslateError", "UnicodeWarning", "UserWarning",
        //    "ValueError", "Warning", "ZeroDivisionError", "__debug__", "__doc__", "__import__", "__name__",
        //    "abs", "all", "any", "apply", "basestring", "bool", "buffer", "callable", "chr", "classmethod",
        //    "cmp", "coerce", "compile", "complex", "copyright", "credits", "delattr", "dict", "dir", "divmod",
        //    "enumerate", "eval", "execfile", "exit", "file", "filter", "float", "frozenset", "getattr",
        //    "globals", "hasattr", "hash", "help", "hex", "id", "input", "int", "intern", "isinstance",
        //    "issubclass", "iter", "len", "license", "list", "locals", "long", "map", "max", "min", "object",
        //    "oct", "open", "ord", "pow", "property", "quit", "range", "raw_input", "reduce", "reload",
        //    "repr", "reversed", "round", "set", "setattr", "slice", "sorted", "staticmethod", "str", "sum",
        //    "super", "tuple", "type", "unichr", "unicode", "vars", "xrange", "zip"};
        //// Most of these will be defined by my index search. However, for the missing ones, let's add them
        //// in. The following code computes the delta and produces a source-like string for it.
        //// It also counts the total symbol map size so we can pick a reasonable default:
        //List<String> asList = Arrays.asList(EXTRA_BUILTINS);
        //Set<String> asSet = new HashSet<String>(asList);
        //asSet.removeAll(symbols);
        //List<String> missing = new ArrayList<String>(asSet);
        //Collections.sort(missing);
        //int width = 0;
        //StringBuilder sb = new StringBuilder();
        //for (String s : missing) {
        //    sb.append('"');
        //    sb.append(s);
        //    sb.append('"');
        //    sb.append(',');
        //    sb.append(' ');
        //    width += s.length()+4;
        //    if (width > 70) {
        //        sb.append("\n");
        //        width = 0;
        //    }
        //}
        //String missingCode = "String[] MISSING = {\n" + sb.toString() + "\n};\n";
        //symbols.addAll(asList);
        //int requiredSetSize = symbols.size();

        return symbols;
    }

    @SuppressWarnings("unchecked")
    public Set<String> getImportsFor(String ident, boolean includeSymbol) {
        Set<String> modules = new HashSet<>(10);

        final Set<IndexResult> result = new HashSet<>();
        search(PythonIndexer.FIELD_MODULE_NAME, ident, QuerySupport.Kind.EXACT, result, PythonIndexer.FIELD_MODULE_NAME);
        for (IndexResult map : result) {
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
            if (module != null) {
                // TODO - record more information about this, such as the FQN
                // so it's easier for the user to disambiguate
                modules.add(module);
            }
        }

        // TODO - handle case sensitivity better...
        String field = PythonIndexer.FIELD_ITEM;
        QuerySupport.Kind kind = QuerySupport.Kind.PREFIX; // We're storing encoded signatures so not exact matches

        String[] terms = { PythonIndexer.FIELD_ITEM,
                           PythonIndexer.FIELD_MODULE_NAME };

        result.clear();
        search(field, ident, kind, result, terms);
        String match = ident + ";";

        MapSearch:
        for (IndexResult map : result) {
            String module = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
            if (module == null) {
                continue;
            }

            if (module.indexOf('-') != -1) {
                // Don't include modules with -; these aren't real module
                // names (usually python scripts in directories containing a dash
                // that I incorrectly compute a module name for
                continue;
            }

            String[] members = map.getValues(PythonIndexer.FIELD_ITEM);
            if (members == null || members.length == 0) {
                continue;
            }

            int semi = match.length() - 1;

            for (String signature : members) {
                if (signature.startsWith(match)) {
                    if (includeSymbol) {
                        int flags = IndexedElement.decode(signature, semi + 3, 0);
                        if ((flags & IndexedElement.PRIVATE) != 0) {
                            // Skip private symbols - can't import those
                            continue;
                        }
                        String sig = ident;
                        char type = signature.charAt(semi + 1);
                        if (type == 'F') {
                            int sigStart = signature.indexOf(';', semi + 3) + 1;
                            int sigEnd = signature.indexOf(';', sigStart);
                            sig = ident + "(" + signature.substring(sigStart, sigEnd) + ")"; // NOI18N
                        } else if (type == 'I') {
                            // Don't provide modules that just -import- the symbol
                            continue;
                        }
                        if (!sig.equals(module)) {
                            modules.add(module + ": " + sig); // NOI18N
                        } else {
                            modules.add(module);
                        }
                    } else {
                        modules.add(module);
                    }
                    continue MapSearch;
                }
            }
        }

        return modules;
    }
    
    public Set<IndexedElement> getImportedElements(String prefix, QuerySupport.Kind kind, PythonParserResult context, List<Import> imports, List<ImportFrom> importsFrom) {
        // TODO - separate methods from variables?? E.g. if you have method Foo() and class Foo
        // coming from different places
        
        
//        Set<String> imported = new HashSet<String>();
//
        Set<IndexedElement> elements = new HashSet<>();

        // Look up the imports and compute all the symbols we get from the import
        Set<String> modules = new HashSet<>();

        // ImportsFrom require no index lookup
        for (ImportFrom from : importsFrom) {
            if (ImportManager.isFutureImport(from)) {
                continue;
            }
            List<alias> names = from.getInternalNames();
            if (names != null) {
                for (alias at : names) {
                    if ("*".equals(at.getInternalName())) { // NOI18N
                        modules.add(from.getInternalModule());
//                    } else {
//                        String name = at.getInternalAsname() != null ? at.getInternalAsname() : at.getInternalName();
//                        assert name.length() > 0;
//                        imported.add(name);
                    }
                }
            }
        }

//        for (Import imp : imports) {
//            if (imp.names != null) {
//                for (alias at : imp.getInternalNames()) {
//                    if (at.getInternalAsname() != null) {
//                        String name = at.getInternalAsname();
//                        assert name.length() > 0;
//                        imported.add(name);
//                    } else {
//                        imported.add(at.getInternalName());
//                    }
//                }
//            }
//        }
//
//
//        // Create variable items for the locally imported symbols
//        for (String name : imported) {
//            if (name.startsWith(prefix)) {
//                if (kind == QuerySupport.Kind.EXACT) {
//                    // Ensure that the method is not longer than the prefix
//                    if ((name.length() > prefix.length()) &&
//                            (name.charAt(prefix.length()) != '(') &&
//                            (name.charAt(prefix.length()) != ';')) {
//                        continue;
//                    }
//                } else {
//                    // REGEXP, CAMELCASE filtering etc. not supported here
//                    assert (kind == QuerySupport.Kind.PREFIX) ||
//                    (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
//                }
//    String url = null;
//                ElementKind elementKind = ElementKind.VARIABLE;
//                if (Character.isUpperCase(name.charAt(0))) {
//                    // Class?
//                    elementKind = ElementKind.CLASS;
//                }
//                IndexedElement element = new IndexedElement(name, elementKind, url, null);
//                element.setSmart(true);
//                elements.add(element);
//                // TODO - imported class symbls should be shown as classes!
//            }
//        }

        // Always include the current file as imported
        String moduleName = null;
        if (context != null) {
            moduleName = PythonUtils.getModuleName(context.getSnapshot().getSource().getFileObject());
            modules.add(moduleName);
        }

        modules.addAll(BUILTIN_MODULES);

        addImportedElements(prefix, kind, modules, elements, null);

        return elements;
    }
    public Set<String> getImportedFromWildcards(List<ImportFrom> importsFrom) {
        Set<String> symbols = new HashSet<>(100);

        // Look up the imports and compute all the symbols we get from the import
        Set<String> modules = new HashSet<>();

        // ImportsFrom require no index lookup
        for (ImportFrom from : importsFrom) {
            List<alias> names = from.getInternalNames();
            if (names != null) {
                for (alias at : names) {
                    if ("*".equals(at.getInternalName())) { // NOI18N
                        modules.add(from.getInternalModule());
                    }
                }
            }
        }

        String[] terms = { PythonIndexer.FIELD_ITEM, PythonIndexer.FIELD_MODULE_NAME };

        // Look up all symbols
        for (String module : modules) {
            // TODO - cache builtins?
            Set<String> moduleSymbols = symbols;
            boolean isSystem = isSystemModule(module);
            if (isSystem) {
                Set<String> s = wildcardImports.get(module);
                if (s != null) {
                    symbols.addAll(s);
                    continue;
                } else {
                    moduleSymbols = new HashSet<>(100);
                }
            }


            final Set<IndexResult> result = new HashSet<>();
            // TODO - handle case sensitivity better...

            search(PythonIndexer.FIELD_MODULE_NAME, module, QuerySupport.Kind.EXACT, result, terms);

            for (IndexResult map : result) {
                String[] items = map.getValues(PythonIndexer.FIELD_ITEM);
                if (items != null) {
                    for (String signature : items) {
                        int semi = signature.indexOf(';');
                        assert semi != -1;
                        int flags = IndexedElement.decode(signature, semi + 3, 0);
                        if ((flags & IndexedElement.PRIVATE) != 0) {
                            // Skip private symbols - can't import those
                            continue;
                        }

                        String name = signature.substring(0, semi);
                        moduleSymbols.add(name);
                    }
                }
            }

            if (isSystem) {
                assert moduleSymbols != symbols;
                symbols.addAll(moduleSymbols);
                wildcardImports.put(module, moduleSymbols);
            }
        }

        return symbols;
    }
    
    public Set<IndexedElement> getImportedElements(String prefix, QuerySupport.Kind kind, Set<String> modules, Set<String> systemModuleHolder) {
        Set<IndexedElement> elements = new HashSet<>();

        addImportedElements(prefix, kind, modules, elements, systemModuleHolder);

        return elements;
    }

    public boolean isSystemModule(String module) {
        if (systemModules == null) {
            systemModules = new HashSet<>(800); // measured: 623
            String[] terms = { PythonIndexer.FIELD_MODULE_ATTR_NAME,
                               PythonIndexer.FIELD_MODULE_NAME };
            final Set<IndexResult> result = new HashSet<>();

            // This doesn't work because the attrs field isn't searchable:
            //search(PythonIndexer.FIELD_MODULE_ATTR_NAME, "S", QuerySupport.Kind.PREFIX, result, ALL_SCOPE, terms);
            //for (IndexResult map : result) {
            //    assert map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME).indexOf("S") != -1;
            //    systemModules.add(map.getValue(PythonIndexer.FIELD_MODULE_NAME));
            //}

            search(PythonIndexer.FIELD_MODULE_NAME, "", QuerySupport.Kind.PREFIX, result, terms);

            for (IndexResult map : result) {
                String attrs = map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME);
                if (attrs != null && attrs.indexOf('S') != -1) {
                    String mod = map.getValue(PythonIndexer.FIELD_MODULE_NAME);
                    systemModules.add(mod);
                }
            }
        }

        return systemModules.contains(module);
    }

    public boolean isLowercaseClassName(String clz) {
        if (availableClasses == null) {
            availableClasses = new HashSet<>(300); // measured: 193
            final Set<IndexResult> result = new HashSet<>();

            search(PythonIndexer.FIELD_CLASS_NAME, "", QuerySupport.Kind.PREFIX, result, PythonIndexer.FIELD_CLASS_NAME);

            for (IndexResult map : result) {
                String c = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                if (c != null && !Character.isUpperCase(c.charAt(0))) {
                    availableClasses.add(c);
                }
            }
        }

        return availableClasses.contains(clz);
    }
    
    public void addImportedElements(String prefix, QuerySupport.Kind kind, Set<String> modules, Set<IndexedElement> elements, Set<String> systemModuleHolder) {
        
        String[] terms = { PythonIndexer.FIELD_ITEM,
                           PythonIndexer.FIELD_MODULE_ATTR_NAME,
                           PythonIndexer.FIELD_MODULE_NAME };

        // Look up all symbols
        for (String module : modules) {
            boolean isBuiltin = isBuiltinModule(module);
            boolean isSystem = isBuiltin;

            final Set<IndexResult> result = new HashSet<>();
            // TODO - handle case sensitivity better...

            search(PythonIndexer.FIELD_MODULE_NAME, module, QuerySupport.Kind.EXACT, result, terms);
            int prefixLength = prefix.length();

            for (IndexResult map : result) {
                String url = map.getUrl().toExternalForm();
                String[] items = map.getValues(PythonIndexer.FIELD_ITEM);
                if (items != null) {
                    String attrs = map.getValue(PythonIndexer.FIELD_MODULE_ATTR_NAME);
                    if (attrs != null && attrs.indexOf('S') != -1) {
                        isSystem = true;
                    }
                    for (String signature : items) {
                        if (signature.startsWith(prefix)) {
                            if (kind == QuerySupport.Kind.EXACT) {
                                if (signature.charAt(prefixLength) != ';') {
                                    continue;
                                }
                            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !signature.regionMatches(true, 0, prefix, 0, prefix.length())) {
                                continue;
                            } else {
                                // REGEXP, CAMELCASE filtering etc. not supported here
                                assert (kind == QuerySupport.Kind.PREFIX) ||
                                        (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX);
                            }

                            IndexedElement element = IndexedElement.create(signature, module, url, null);
                            if (element.isPrivate()) {
                                continue;
                            }
                            if (isBuiltin) {
                                element.setRhs("<i>builtin</i>");
                            } else {
                                element.setSmart(true);
                            }
                            element.setInherited(true);
                            elements.add(element);
                        }
                    }
                }
            }

            if (systemModuleHolder != null && isSystem) {
                systemModuleHolder.add(module);
            }
        }
    }

    public Set<IndexedElement> getExceptions(String prefix, QuerySupport.Kind kind) {
        final Set<IndexResult> result = new HashSet<>();
        String[] terms = { PythonIndexer.FIELD_EXTENDS_NAME,
                              PythonIndexer.FIELD_CLASS_NAME,
                              PythonIndexer.FIELD_CLASS_ATTR_NAME,
                              PythonIndexer.FIELD_IN };
        search(PythonIndexer.FIELD_EXTENDS_NAME, "", QuerySupport.Kind.PREFIX, result, terms); // NOI18N
        Map<String, String> extendsMap = new HashMap<>(100);
        // First iteration: Compute inheritance hierarchy
        for (IndexResult map : result) {

            String superClass = map.getValue(PythonIndexer.FIELD_EXTENDS_NAME);
            if (superClass != null) {
                String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
                if (clz != null) {
                    extendsMap.put(clz, superClass);
                }
            }
        }

        // Compute set of classes that extend Exception

        Set<String> exceptionClasses = new HashSet<>();
        Set<String> notExceptionClasses = new HashSet<>();
        exceptionClasses.add("Exception"); // NOI18N
        Outer:
        for (String cls : extendsMap.keySet()) {
            if (notExceptionClasses.contains(cls)) {
                continue;
            } else if (!exceptionClasses.contains(cls)) {
                // See if this extends exception:
                String c = cls;
                int depth = 0;
                while (c != null) {
                    c = extendsMap.get(c);
                    String prev = null;
                    if (c != null) {
                        if (exceptionClasses.contains(c)) {
                            exceptionClasses.add(cls);
                            continue Outer;
                        }
                        depth++;
                        if (depth == 15) {
                            // we're probably going in circles, perhaps a extends b extends a.
                            // This doesn't really happen in Python, but can happen when there
                            // are unrelated classes with the same name getting treated as one here -
                            // class a in library X, and class a in library Y,
                            break;
                        }
                    } else if (prev != null) {
                        notExceptionClasses.add(prev);
                        break;
                    }
                }
                notExceptionClasses.add(cls);
            }
        }

        // Next add elements for all the exceptions
        final Set<IndexedElement> classes = new HashSet<>();
        for (IndexResult map : result) {
            String clz = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            if (clz == null || !exceptionClasses.contains(clz)) {
                continue;
            }

            if ((kind == QuerySupport.Kind.PREFIX) && !clz.startsWith(prefix)) {
                continue;
            } else if (kind == QuerySupport.Kind.CASE_INSENSITIVE_PREFIX && !clz.regionMatches(true, 0, prefix, 0, prefix.length())) {
                continue;
            } else if (kind == QuerySupport.Kind.EXACT && !clz.equals(prefix)) {
                continue;
            }

            String url = map.getUrl().toExternalForm();
            String module = map.getValue(PythonIndexer.FIELD_IN);
            IndexedElement element = new IndexedElement(clz, ElementKind.CLASS, url, module, null, null);
            String attrs = map.getValue(PythonIndexer.FIELD_CLASS_ATTR_NAME);
            if (attrs != null) {
                int flags = IndexedElement.decode(attrs, 0, 0);
                element.setFlags(flags);
            }
            classes.add(element);
        }

        return classes;
    }

    /** Find the subclasses of the given class name, with the POSSIBLE fqn from the
     * context of the usage. */
    public Set<IndexedElement> getSubClasses(String fqn, String possibleFqn, String name, boolean directOnly) {
        //String field = PythonIndexer.FIELD_FQN_NAME;
        Set<IndexedElement> classes = new HashSet<>();
        Set<String> scannedClasses = new HashSet<>();
        Set<String> seenClasses = new HashSet<>();

        if (fqn != null) {
            addSubclasses(fqn, classes, seenClasses, scannedClasses, directOnly);
        } else {
            fqn = possibleFqn;
            if (name.equals(possibleFqn)) {
                fqn = null;
            }

            // Try looking at the libraries too
            while ((classes.size() == 0) && (fqn != null && fqn.length() > 0)) {
                // TODO - use the boolvalue from addclasses instead!
                boolean found = addSubclasses(fqn + "::" + name, classes, seenClasses, scannedClasses, directOnly);
                if (found) {
                    return classes;
                }

                int f = fqn.lastIndexOf("::");

                if (f == -1) {
                    break;
                } else {
                    fqn = fqn.substring(0, f);
                }
            }

            if (classes.size() == 0) {
                addSubclasses(name, classes, seenClasses, scannedClasses, directOnly);
            }
        }

        return classes;
    }
    
    private boolean addSubclasses(String classFqn, Set<IndexedElement> classes, Set<String> seenClasses, Set<String> scannedClasses, boolean directOnly) {
        // Prevent problems with circular includes or redundant includes
        if (scannedClasses.contains(classFqn)) {
            return false;
        }

        scannedClasses.add(classFqn);

        String searchField = PythonIndexer.FIELD_EXTENDS_NAME;

        Set<IndexResult> result = new HashSet<>();

        String[] terms = { PythonIndexer.FIELD_IN,
                              PythonIndexer.FIELD_EXTENDS_NAME,
                              PythonIndexer.FIELD_CLASS_ATTR_NAME,
                              PythonIndexer.FIELD_CLASS_NAME };

        search(searchField, classFqn, QuerySupport.Kind.EXACT, result, terms);

        boolean foundIt = result.size() > 0;

        // If this is a bogus class entry (no search rsults) don't continue
        if (!foundIt) {
            return foundIt;
        }

        for (IndexResult map : result) {
            String className = map.getValue(PythonIndexer.FIELD_CLASS_NAME);
            if (className != null && !seenClasses.contains(className)) {
                String url = map.getUrl().toExternalForm();
                String module = map.getValue(PythonIndexer.FIELD_IN);
                IndexedElement clz = new IndexedElement(className, ElementKind.CLASS, url, module, null, null);
                String attrs = map.getValue(PythonIndexer.FIELD_CLASS_ATTR_NAME);
                if (attrs != null) {
                    int flags = IndexedElement.decode(attrs, 0, 0);
                    clz.setFlags(flags);
                }
                classes.add(clz);

                seenClasses.add(className);

                if (!directOnly) {
                    addSubclasses(className, classes, seenClasses, scannedClasses, directOnly);
                }
            }
        }

        return foundIt;
    }
}
