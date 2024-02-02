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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import org.apache.lucene.document.Document;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullUnknown;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.source.indexing.JavaIndex;
import org.netbeans.modules.java.source.usages.ClassIndexFactory;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexImplEvent;
import org.netbeans.modules.java.source.usages.ClassIndexImplListener;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.ClassIndexManagerEvent;
import org.netbeans.modules.java.source.usages.ClassIndexManagerListener;
import org.netbeans.modules.java.source.usages.DocumentUtil;
import org.netbeans.modules.parsing.impl.Utilities;
import org.netbeans.modules.parsing.impl.indexing.PathRegistry;
import org.netbeans.modules.parsing.lucene.support.Convertor;
import org.netbeans.modules.parsing.lucene.support.Convertors;
import org.netbeans.modules.parsing.lucene.support.Index;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 * The ClassIndex provides access to information stored in the 
 * persistent index. It can be used to obtain list of packages
 * or declared types. It can be also used to obtain a list of
 * source files referencing given type (usages of given type).
 *
 * @author Petr Hrebejk, Tomas Zezula
 */
public final class ClassIndex {
    
    private static final Logger LOGGER = Logger.getLogger(ClassIndex.class.getName());
    
    //INV: Never null
    private final ClassPath bootPath;
    //INV: Never null
    private final ClassPath classPath;
    //INV: Never null
    private final ClassPath sourcePath;

    //INV: Never null
    //@GuardedBy (this)
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")    //NOI18N
    private final Set<URL> oldSources;
    //INV: Never null
    //@GuardedBy (this)
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")    //NOI18N
    private final Set<URL> oldBoot;    
    //INV: Never null
    //@GuardedBy (this)
    @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"},justification="URLs have never host part")    //NOI18N
    private final Set<URL> oldCompile;
    //INV: Never null
    //@GuardedBy (this)
    private final Set<ClassIndexImpl> sourceIndeces;
    //INV: Never null
    //@GuardedBy (this)
    private final Set<ClassIndexImpl> depsIndeces;

    private final Collection<ClassIndexListener> listeners = new ConcurrentLinkedQueue<ClassIndexListener>();
    private final SPIListener spiListener = new SPIListener ();

    /**
     * Encodes a type of the name kind used by 
     * {@link ClassIndex#getDeclaredTypes} method.
     *
     */
    public enum NameKind {
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes}
         * is an exact simple name of the package or declared type.
         */
        SIMPLE_NAME,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} 
         * is an case sensitive prefix of the package or declared type name.
         */
        PREFIX,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an case insensitive prefix of the declared type name.
         */
        CASE_INSENSITIVE_PREFIX,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an camel case of the declared type name.
         */
        CAMEL_CASE,
        
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an regular expression of the declared type name.
         */
        REGEXP,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * an case insensitive regular expression of the declared type name.
         */
        CASE_INSENSITIVE_REGEXP,
        
        /**
         * The name parameter of the {@link ClassIndex#getDeclaredTypes} is 
         * a camel case or case insensitive prefix of the declared type name.
         * For example all these names NPE, NulPoEx, NULLPOInter leads to NullPointerException returned.
         * @since 0.28.0
         */
        CAMEL_CASE_INSENSITIVE
    };
    
    
    /**
     * Encodes a reference type,
     * used by {@link ClassIndex#getElements} and {@link ClassIndex#getResources}
     * to restrict the search.
     */
    public enum SearchKind {
        
        /**
         * The returned class has to extend or implement given element
         */
        IMPLEMENTORS,
        
        /**
         * The returned class has to call method on given element
         */
        METHOD_REFERENCES,
        
        /**
         * The returned class has to access a field on given element
         */
        FIELD_REFERENCES,
        
        /**
         * The returned class contains references to the element type
         */
        TYPE_REFERENCES,

        /**
         * The returned class contains a lambda implementation of given functional interface.
         * @since 2.9
         */
        FUNCTIONAL_IMPLEMENTORS;
    };
    
    /**
     * Default predefined {@link SearchScopeType}s
     */
    public enum SearchScope implements SearchScopeType {
        /**
         * Search is done in source path
         */
        SOURCE {
            @Override
            public boolean isSources() {return true;}
            @Override
            public boolean isDependencies() {return false;}
        },
        /**
         * Search is done in compile and boot path
         */
        DEPENDENCIES {
            @Override
            public boolean isSources() {return false;}
            @Override
            public boolean isDependencies() {return true;}
        };

        @Override
        @CheckForNull
        public Set<? extends String> getPackages() {
            return null;
        }
    };

    /**
     * ResourceType used by {@link ClassIndex#getResources} to get results in
     * @since 2.5
     */
    public static enum ResourceType {

        /**
         * Resources from a source root.
         */
        SOURCE,

        /**
         * Resources from a binary root.
         */
        BINARY;
    }

    /**
     * Scope used by {@link ClassIndex} to search in
     * @since 0.82
     */
    public static interface SearchScopeType {
        /**
         * Limits search only into given packages.
         * @return set of packages to search in or null which
         * means all packages
         */
        @CheckForNull
        Set<? extends String> getPackages();

        /**
         * Search in source path.
         * @return if true search in done in sources
         */
        boolean isSources();

        /**
         * Search in dependent libraries bootpath,  compilepath.
         * @return if true search in done in dependent libraries
         */
        boolean isDependencies();
    }

    static {
	ClassIndexImpl.FACTORY = new ClassIndexFactoryImpl();
    }
    
    ClassIndex(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {
        this(bootPath,classPath,sourcePath,true);
    }
    
    ClassIndex( final @NonNull ClassPath bootPath,
            final @NonNull ClassPath classPath,
            final @NonNull ClassPath sourcePath,
            final boolean supportsChanges
            ) {
        assert bootPath != null;
        assert classPath != null;
        assert sourcePath != null;
        this.bootPath = bootPath;
        this.classPath = classPath;
        this.sourcePath = sourcePath;
        this.oldBoot = new HashSet<URL>();
        this.oldCompile = new  HashSet<URL>();
        this.oldSources = new HashSet<URL>();
        this.depsIndeces = new HashSet<ClassIndexImpl>();
        this.sourceIndeces = new HashSet<ClassIndexImpl>();
        if (supportsChanges) {
            this.bootPath.addPropertyChangeListener(WeakListeners.propertyChange(spiListener, this.bootPath));
            this.classPath.addPropertyChangeListener(WeakListeners.propertyChange(spiListener, this.classPath));
            this.sourcePath.addPropertyChangeListener(WeakListeners.propertyChange(spiListener, this.sourcePath));
        }
        reset (true, true);
    }
    
    
    /**
     * Adds an {@link ClassIndexListener}. The listener is notified about the
     * changes of declared types in this {@link ClassIndex}
     * @param listener to be added
     */
    public void addClassIndexListener (final @NonNull ClassIndexListener listener) {
        assert listener != null;
        listeners.add (listener);
    }
    
    /**
     * Removes an {@link ClassIndexListener}. The listener is notified about the
     * changes of declared types in this {@link ClassIndex}
     * @param listener to be removed
     */
    public void removeClassIndexListener (final @NonNull ClassIndexListener listener) {
        assert listener != null;
        listeners.remove(listener);
    }
    
    
    /**
     * Returns a set of {@link ElementHandle}s containing reference(s) to given type element.
     * @param element the {@link ElementHandle} of a {@link TypeElement} for which usages should be found
     * @param searchKind type of reference, {@link SearchKind}
     * @param scope to search in {@link SearchScope}
     * @return set of {@link ElementHandle}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<ElementHandle<TypeElement>> getElements (
            final @NonNull ElementHandle<TypeElement> element,
            final @NonNull Set<SearchKind> searchKind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        return searchImpl(
            element,
            searchKind,
            scope,
            new Convertor<ClassIndexImpl, Convertor<Document,ElementHandle<TypeElement>>>(){
                @NonNull
                @Override
                public Convertor<Document, ElementHandle<TypeElement>> convert(@NonNull final ClassIndexImpl p) {
                    return DocumentUtil.typeElementConvertor();
                }
            });
    }

    /**
     * Returns a set of {@link ElementHandle}s containing reference(s) to given package element.
     * @param element the {@link ElementHandle} of a {@link PackageElement} for which usages should be found
     * @param searchKind type of reference, {@link SearchKind}
     * @param scope to search in {@link SearchScope}
     * @return set of {@link ElementHandle}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     * @since 0.89
     */
    public @NullUnknown Set<ElementHandle<TypeElement>> getElementsForPackage (
            final @NonNull ElementHandle<PackageElement> element,
            final @NonNull Set<SearchKind> searchKind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        return searchImpl(
            element,
            searchKind,
            scope,
            new Convertor<ClassIndexImpl, Convertor<Document,ElementHandle<TypeElement>>>(){
                @NonNull
                @Override
                public Convertor<Document, ElementHandle<TypeElement>> convert(@NonNull final ClassIndexImpl p) {
                    return DocumentUtil.typeElementConvertor();
                }
            });
    }

    /**
     * Returns a set of source files containing reference(s) to given type element.
     * @param element the {@link ElementHandle} of a {@link TypeElement} for which usages should be found
     * @param searchKind type of reference, {@link SearchKind}
     * @param scope to search in {@link SearchScope}
     * @return set of {@link FileObject}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<FileObject> getResources (
            final @NonNull ElementHandle<TypeElement> element,
            final @NonNull Set<SearchKind> searchKind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        return searchImpl(
            element,
            searchKind,
            scope,
            new Convertor<ClassIndexImpl, Convertor<Document,FileObject>>() {
                @NonNull
                @Override
                public Convertor<Document, FileObject> convert(@NonNull final ClassIndexImpl p) {
                    return DocumentUtil.fileObjectConvertor (ClassIndex.ResourceType.SOURCE, p.getSourceRoots());
                }
            });
    }
    
    /**
     * Returns a set of source or binary files containing reference(s) to given type element.
     * @param element the {@link ElementHandle} of a {@link TypeElement} for which usages should be found
     * @param searchKind type of reference, {@link SearchKind}
     * @param scope to search in {@link SearchScope}
     * @param resourceType to return resource in, {@link ResourceType}. The {@link ResourceType#BINARY}
     * produces no result for source roots. For binary roots it does not do SourceForBinaryQuery translation and
     * returns class files. The {@link ResourceType#SOURCE} for binary roots does SourceForBinaryQuery translation
     * and returns corresponding java files or no result when there is no SourceForBinaryQuery configured for given binary root.
     * The {@link ResourceType#SOURCE} is preferred to {@link ResourceType#BINARY}.
     * @return set of {@link FileObject}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     * @since 2.5
     */
    public @NullUnknown Set<FileObject> getResources (
            final @NonNull ElementHandle<TypeElement> element,
            final @NonNull Set<SearchKind> searchKind,
            final @NonNull Set<? extends SearchScopeType> scope,
            final @NonNull Set<ResourceType> resourceType) {
        return searchImpl(
            element,
            searchKind,
            scope,
            new Convertor<ClassIndexImpl, Convertor<Document,FileObject>>() {
                @NonNull
                @Override
                public Convertor<Document, FileObject> convert(@NonNull final ClassIndexImpl p) {
                    final List<Convertor<Document,FileObject>> base = new ArrayList<>(ResourceType.values().length);
                    if (resourceType.contains(ResourceType.SOURCE)) {
                        final FileObject[] roots = p.getSourceRoots();
                        if (roots.length > 0) {
                            base.add(DocumentUtil.fileObjectConvertor(ResourceType.SOURCE, roots));
                        }
                    }
                    if (resourceType.contains(ResourceType.BINARY)) {
                        final FileObject[] roots = p.getBinaryRoots();
                        if (roots.length > 0) {
                            base.add(DocumentUtil.fileObjectConvertor(ResourceType.BINARY, roots));
                        }
                    }
                    return Convertors.firstNonNull(base);
                }
            });
    }

    /**
     * Returns a set of source files containing reference(s) to given package element.
     * @param element the {@link ElementHandle} of a {@link PackageElement} for which usages should be found
     * @param searchKind type of reference, {@link SearchKind}
     * @param scope to search in {@link SearchScope}
     * @return set of {@link FileObject}s containing the reference(s)
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     * @since 0.89
     */
    public @NullUnknown Set<FileObject> getResourcesForPackage (
            final @NonNull ElementHandle<PackageElement> element,
            final @NonNull Set<SearchKind> searchKind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        return searchImpl(
            element,
            searchKind,
            scope,
            new Convertor<ClassIndexImpl, Convertor<Document,FileObject>>() {
                @NonNull
                @Override
                public Convertor<Document, FileObject> convert(@NonNull final ClassIndexImpl p) {
                    return DocumentUtil.fileObjectConvertor (ClassIndex.ResourceType.SOURCE, p.getSourceRoots());
                }
            });
    }

    @NullUnknown
    private <T> Set<T> searchImpl(
            @NonNull final ElementHandle<? extends Element> element,
            @NonNull final Set<SearchKind> searchKind,
            @NonNull final Set<? extends SearchScopeType> scope,
            @NonNull final Convertor<? super ClassIndexImpl,Convertor<Document, T>> convertor) {
        Parameters.notNull("element", element); //NOI18N
        Parameters.notNull("element.signatue", element.getSignature()[0]);  //NOI18N
        Parameters.notNull("searchKind", searchKind);   //NOI18N
        Parameters.notNull("scope", scope); //NOI18N
        Parameters.notNull("convertor", convertor); //NOI18N
        final Set<T> result = new HashSet<T> ();
        final Set<ClassIndexImpl.UsageType> ut =  encodeSearchKind(element.getKind(),searchKind);
        if (!ut.isEmpty()) {
            try {
                final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
                for (ClassIndexImpl query : queries) {
                    try {
                        query.search(
                            element,
                            ut,
                            scope,
                            convertor.convert(query),
                            result);
                    } catch (Index.IndexClosedException e) {
                        logClosedIndex (query);
                    } catch (IOException e) {
                        Exceptions.printStackTrace(e);
                    }
                }
            } catch (InterruptedException e) {
                return null;
            }
        }
        return Collections.unmodifiableSet(result);
    }
    /**
     * Returns {@link ElementHandle}s for all declared types in given classpath corresponding to the name.
     * @param name case sensitive prefix, case insensitive prefix, exact simple name,
     * camel case or regular expression depending on the kind parameter.
     * @param kind of the name {@link NameKind}
     * @param scope to search in {@link SearchScope}
     * @return set of all matched declared types
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<ElementHandle<TypeElement>> getDeclaredTypes (
            final @NonNull String name,
            final @NonNull NameKind kind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        return searchImpl(name, kind, scope, DocumentUtil.typeElementConvertor());        
    }
    
    /**
     * Returns {@link ElementHandle}s for all declared modules in given classpath corresponding to the name.
     * @param name case sensitive prefix, case insensitive prefix, exact simple name,
     * camel case or regular expression depending on the kind parameter.
     * @param kind of the name {@link NameKind}
     * @param scope to search in {@link SearchScope}
     * @return set of all matched modules
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     * @since 2.23
     */
    @NullUnknown
    public Set<ElementHandle<ModuleElement>> getDeclaredModules (
            final @NonNull String name,
            final @NonNull NameKind kind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        return searchImpl(name, kind, scope, DocumentUtil.moduleElementConvertor());
    }
    
    @NullUnknown
    private <T extends Element> Set<ElementHandle<T>> searchImpl(
            final @NonNull String name,
            final @NonNull NameKind kind,
            final @NonNull Set<? extends SearchScopeType> scope,
            final Convertor<Document, ElementHandle<T>> ehConvertor) {
        assert name != null;
        assert kind != null;
        final Set<ElementHandle<T>> result = new HashSet<>();        
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);        
        try {
            for (ClassIndexImpl query : queries) {
                try {
                    query.getDeclaredElements (
                        name,
                        kind,
                        scope,
                        DocumentUtil.declaredTypesFieldSelector(false, false),
                        ehConvertor,
                        result);
                } catch (Index.IndexClosedException e) {
                    logClosedIndex (query);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(
                        Level.FINE,
                        "ClassIndex found {0} elements\n",  //NOI18N
                        result.size());
            }
            return Collections.unmodifiableSet(result);
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    /**
     * Returns descriptions of symbols found on the given classpath and matching the additional criteria.
     * @param name case sensitive prefix, case insensitive prefix, exact simple name,
     * camel case or regular expression depending on the kind parameter.
     * @param kind of the name {@link NameKind}
     * @param scope to search in {@link SearchScope}
     * @return iterable of {@link Symbols} describing found symbols matching the specified criteria.
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     * @since 0.117
     */
    public @NullUnknown Iterable<Symbols> getDeclaredSymbols(
            final @NonNull String name,
            final @NonNull NameKind kind,
            final @NonNull Set<? extends SearchScopeType> scope) {
        Parameters.notNull("name", name);
        Parameters.notNull("kind", kind);
        final Map<ElementHandle<TypeElement>,Set<String>> result = new HashMap<ElementHandle<TypeElement>,Set<String>>();
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);        
        final Convertor<Document, ElementHandle<TypeElement>> thConvertor = DocumentUtil.typeElementConvertor();
        try {
            for (ClassIndexImpl query : queries) {
                try {
                    query.getDeclaredElements(
                        name,
                        kind,
                        thConvertor,
                        result);
                } catch (Index.IndexClosedException e) {
                    logClosedIndex (query);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(
                        Level.FINE,
                        "ClassIndex.getDeclaredTypes returned {0} elements\n",  //NOI18N
                        result.size());
            }
            List<Symbols> finalResult = new ArrayList<Symbols>();
            for (Entry<ElementHandle<TypeElement>, Set<String>> e : result.entrySet()) {
                finalResult.add(new Symbols(e.getKey(), e.getValue()));
            }
            return Collections.unmodifiableList(finalResult);
        } catch (InterruptedException e) {
            return null;
        }
    }
    
    /**Description of found symbols (methods, constructors, fields) for one enclosing type.
     * Returned from {@link #getDeclaredSymbols(java.lang.String, org.netbeans.api.java.source.ClassIndex.NameKind, java.util.Set) }.
     * 
     * @since 0.117
     */
    public static final class Symbols {
        private final ElementHandle<TypeElement> enclosingType;
        private final Set<String> symbols;

        Symbols(ElementHandle<TypeElement> enclosingType, Set<String> symbols) {
            this.enclosingType = enclosingType;
            this.symbols = symbols;
        }

        /**The type that contains some symbols matching the required criterie.
         * 
         * @return enclosing type
         */
        public ElementHandle<TypeElement> getEnclosingType() {
            return enclosingType;
        }

        /**The simple names of all symbols matching the criteria inside the given enclosing type.
         * 
         * @return simple names of matching symbols
         */
        public Set<String> getSymbols() {
            return symbols;
        }
        
    }
    
    /**
     * Returns names af all packages in given classpath starting with prefix.
     * @param prefix of the package name
     * @param directOnly if true treats the packages as folders and returns only
     * the nearest component of the package.
     * @param scope to search in {@link SearchScope}
     * @return set of all matched package names
     * It may return null when the caller is a CancellableTask&lt;CompilationInfo&gt; and is cancelled
     * inside call of this method.
     */
    public @NullUnknown Set<String> getPackageNames (
            final @NonNull String prefix,
            boolean directOnly,
            final @NonNull Set<? extends SearchScopeType> scope) {
        assert prefix != null;
        final Set<String> result = new HashSet<String> ();        
        final Iterable<? extends ClassIndexImpl> queries = this.getQueries (scope);
        try {
            for (ClassIndexImpl query : queries) {
                try {
                    query.getPackageNames (prefix, directOnly, result);
                } catch (Index.IndexClosedException e) {
                    logClosedIndex (query);
                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                }
            }
            return Collections.unmodifiableSet(result);
        } catch (InterruptedException e) {
            return null;
        }
    }

    /**
     * Creates a search scope limited to list of packages.
     * @param base the base search scope to restrict
     * @param pkgs a list of packages in which the search should be performed
     * @return a newly created search scope
     * @since 0.82
     */
    @NonNull
    public static SearchScopeType createPackageSearchScope(
            @NonNull final SearchScopeType base,
            @NonNull final String... pkgs) {
        Parameters.notNull("base", base);   //NOI18N
        Parameters.notNull("pkgs", pkgs);   //NOI18N
        final Set<String> pkgSet = new HashSet<String>(Arrays.asList(pkgs));
        final Set<? extends String> basePkgs = base.getPackages();
        if (basePkgs != null) {
            pkgSet.addAll(basePkgs);
        }
        final Set<String> newPkgs = Collections.unmodifiableSet(pkgSet);
        return new SearchScopeType() {
            @Override
            public Set<? extends String> getPackages() {
                return newPkgs;
            }

            @Override
            public boolean isSources() {
                return base.isSources();
            }

            @Override
            public boolean isDependencies() {
                return base.isDependencies();
            }
        };
    }

    // <editor-fold defaultstate="collapsed" desc="Private implementation">
    private static class ClassIndexFactoryImpl implements ClassIndexFactory {
        
	public ClassIndex create(final ClassPath bootPath, final ClassPath classPath, final ClassPath sourcePath) {            
	    return new ClassIndex(bootPath, classPath, sourcePath);
        }
	
    }
    
    //Private methods
    
    private static void logClosedIndex (final ClassIndexImpl query) {
        assert query != null;
        LOGGER.info("Ignoring closed index: " + query.toString());  //NOI18N
    }
    
    
    private  void reset (final boolean source, final boolean deps) {
        ProjectManager.mutex().readAccess(new Runnable() {

            public void run() {
                synchronized (ClassIndex.this) {
                    if (source) {            
                        for (ClassIndexImpl impl : sourceIndeces) {
                            impl.removeClassIndexImplListener(spiListener);
                        }
                        sourceIndeces.clear();
                        oldSources.clear();
                        createQueriesForRoots (sourcePath, true, sourceIndeces, oldSources);
                    }
                    if (deps) {
                        for (ClassIndexImpl impl : depsIndeces) {
                            impl.removeClassIndexImplListener(spiListener);
                        }
                        depsIndeces.clear();
                        oldBoot.clear();
                        oldCompile.clear();
                        createQueriesForRoots (bootPath, false, depsIndeces,  oldBoot);                
                        createQueriesForRoots (classPath, false, depsIndeces, oldCompile);	    
                    }
                }
            }
        });        
    }

    private Iterable<? extends ClassIndexImpl> getQueries (final Set<? extends SearchScopeType> scope) {
        final Set<ClassIndexImpl> result = new HashSet<ClassIndexImpl> ();
        synchronized (this) {
            for (SearchScopeType s : scope) {
                if (s.isSources()) {
                    result.addAll(this.sourceIndeces);
                }
                if (s.isDependencies()) {
                    result.addAll(this.depsIndeces);
                }
            }
        }
        LOGGER.log(
                Level.FINE,
                "ClassIndex.queries[Scope={0}, sourcePath={1}, bootPath={2}, classPath={3}] => {4}\n",  //NOI18N
                new Object[] {
                    scope,
                    sourcePath,
                    bootPath,
                    classPath,
                    result
                });
        return result;
    }

    private void createQueriesForRoots (final ClassPath cp, final boolean sources, final Set<? super ClassIndexImpl> queries, final Set<? super URL> oldState) {
        final PathRegistry preg = PathRegistry.getDefault();
        List<ClassPath.Entry> entries = cp.entries();
	for (ClassPath.Entry entry : entries) {
            URL[] srcRoots;
            if (!sources) {
                srcRoots = preg.sourceForBinaryQuery(entry.getURL(), cp, true);
                if (srcRoots == null) {
                    srcRoots = new URL[] {entry.getURL()};
                }
            }
            else {
                srcRoots = new URL[] {entry.getURL()};
            }
            for (URL srcRoot : srcRoots) {
                oldState.add (srcRoot);
                ClassIndexImpl ci = ClassIndexManager.getDefault().getUsagesQuery(srcRoot, true);
                if (ci != null) {
                    ci.addClassIndexImplListener(spiListener);
                    queries.add (ci);
                }
            }
	}
    }
    
    
    private static Set<ClassIndexImpl.UsageType> encodeSearchKind (final ElementKind elementKind, final Set<ClassIndex.SearchKind> kind) {
        assert kind != null;
        final Set<ClassIndexImpl.UsageType> result = EnumSet.noneOf(ClassIndexImpl.UsageType.class);
        for (ClassIndex.SearchKind sk : kind) {
            switch (sk) {
                case METHOD_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.METHOD_REFERENCE);
                    break;
                case FIELD_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.FIELD_REFERENCE);
                    break;
                case TYPE_REFERENCES:
                    result.add(ClassIndexImpl.UsageType.TYPE_REFERENCE);
                    break;
                case IMPLEMENTORS:
                    switch( elementKind) {
                        case INTERFACE:
                        case ANNOTATION_TYPE:
                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
                            break;
                        case CLASS:
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        case ENUM:
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        case OTHER:
                        case PACKAGE:
                            result.add(ClassIndexImpl.UsageType.SUPER_INTERFACE);
                            result.add(ClassIndexImpl.UsageType.SUPER_CLASS);
                            break;
                        default:
                            if (elementKind.name().equals("RECORD")) {
                                //no subclasses(?)
                                break;
                            }
                            throw new IllegalArgumentException ();                                        
                    }
                    break;
                case FUNCTIONAL_IMPLEMENTORS:
                    result.add(ClassIndexImpl.UsageType.FUNCTIONAL_IMPLEMENTORS);
                    break;
                default:
                    throw new IllegalArgumentException ();                    
            }
        }
        return result;
    }           
    
    private class SPIListener implements ClassIndexImplListener, ClassIndexManagerListener, PropertyChangeListener {

        private final AtomicBoolean attached = new AtomicBoolean();

        @Override
        public void typesAdded (@NonNull final ClassIndexImplEvent event) {
            assert event != null;
            fireByWorker(() -> {
                assertParserEventThread();
                final TypesEvent _event = new TypesEvent (
                        ClassIndex.this,
                        event.getRoot(),
                        event.getModule(),
                        event.getTypes());
                for (ClassIndexListener l : listeners) {
                    l.typesAdded(_event);
                }
            });
        }

        @Override
        public void typesRemoved (@NonNull final ClassIndexImplEvent event) {
            assert event != null;
            fireByWorker(() -> {
                assertParserEventThread();
                final TypesEvent _event = new TypesEvent (
                        ClassIndex.this,
                        event.getRoot(),
                        event.getModule(),
                        event.getTypes());
                for (ClassIndexListener l : listeners) {
                    l.typesRemoved(_event);
                }
            });
        }

        @Override
        public void typesChanged (@NonNull final ClassIndexImplEvent event) {
            assert event != null;
            fireByWorker(() -> {
                assertParserEventThread();
                final TypesEvent _event = new TypesEvent (
                        ClassIndex.this,
                        event.getRoot(),
                        event.getModule(),
                        event.getTypes());
                for (ClassIndexListener l : listeners) {
                    l.typesChanged(_event);
                }
            });
        }

        @Override
        public void classIndexAdded (final ClassIndexManagerEvent event) {
            assert event != null;
            final Set<? extends URL> roots = event.getRoots();
            assert roots != null;
            final List<URL> ar = new LinkedList<>();
            boolean srcF = containsRoot (sourcePath,roots,ar, false);
            boolean depF = containsRoot (bootPath, roots, ar, true);
            depF |= containsRoot (classPath, roots, ar, true);
            if (srcF || depF) {
                reset (srcF, depF);
                fireByWorker(() -> {
                    assertParserEventThread();
                    final RootsEvent e = new RootsEvent(ClassIndex.this, ar);
                    for (ClassIndexListener l : listeners) {
                        l.rootsAdded(e);
                    }
                });
            }
        }

        public void classIndexRemoved (final ClassIndexManagerEvent event) {
            //Not important handled by propertyChange from ClassPath
        }

        private void attachClassIndexManagerListener () {
            if (!attached.getAndSet(true)) {
                final ClassIndexManager manager = ClassIndexManager.getDefault();
                manager.addClassIndexManagerListener(WeakListeners.create(ClassIndexManagerListener.class, (ClassIndexManagerListener) this, manager));
            }
        }

        @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")    //NOI18N
        private boolean containsRoot (final ClassPath cp, final Set<? extends URL> roots, final List<? super URL> affectedRoots, final boolean translate) {
            final List<ClassPath.Entry> entries = cp.entries();
            final PathRegistry preg = PathRegistry.getDefault();
            boolean result = false;
            for (ClassPath.Entry entry : entries) {
                URL url = entry.getURL();
                URL[] srcRoots = null;
                if (translate) {
                    srcRoots = preg.sourceForBinaryQuery(entry.getURL(), cp, false);
                }
                if (srcRoots == null) {
                    if (roots.contains(url)) {
                        affectedRoots.add(url);
                        result = true;
                    }
                }
                else {
                    for (URL _url : srcRoots) {
                        if (roots.contains(_url)) {
                            affectedRoots.add(url);
                            result = true;
                        }
                    }
                }
            }
            return result;
        }

        private boolean containsNewRoot (final ClassPath cp, final Set<? extends URL> roots,
                final List<? super URL> newRoots, final List<? super URL> removedRoots,
                final Set<? super URL> attachListener, final boolean translate) throws IOException {
            final List<ClassPath.Entry> entries = cp.entries();
            final PathRegistry preg = PathRegistry.getDefault();
            boolean result = false;
            for (ClassPath.Entry entry : entries) {
                URL url = entry.getURL();
                URL[] srcRoots = null;
                if (translate) {
                    srcRoots = preg.sourceForBinaryQuery(entry.getURL(), cp, false);
                }
                if (srcRoots == null) {
                    if (!roots.remove(url)) {
                        if (JavaIndex.isIndexed(url)) {
                            newRoots.add (url);
                            result = true;
                        } else {
                            attachListener.add(url);
                        }
                    }
                }
                else {
                    for (URL _url : srcRoots) {
                        if (!roots.remove(_url)) {
                            if (JavaIndex.isIndexed(_url)) {
                                newRoots.add (_url);
                                result = true;
                            } else {
                                attachListener.add(_url);
                            }
                        }
                    }
                }
            }
            result |= !roots.isEmpty();
            Collection<? super URL> c = removedRoots;
            c.addAll(roots);
            return result;
        }

        @org.netbeans.api.annotations.common.SuppressWarnings(value={"DMI_COLLECTION_OF_URLS"}, justification="URLs have never host part")    //NOI18N
        public void propertyChange(PropertyChangeEvent evt) {
            if (ClassPath.PROP_ENTRIES.equals (evt.getPropertyName())) {
                final List<URL> newRoots = new LinkedList<URL>();
                final List<URL> removedRoots = new  LinkedList<URL> ();
                boolean dirtySource = false;
                boolean dirtyDeps = false;
                try {
                    Object source = evt.getSource();
                    final Set<URL> unknownRoots = new HashSet<URL>();
                    if (source == ClassIndex.this.sourcePath) {
                        Set<URL> copy;
                        synchronized (ClassIndex.this) {
                            copy = new HashSet<URL>(oldSources);
                        }
                        dirtySource = containsNewRoot(sourcePath, copy, newRoots, removedRoots, unknownRoots, false);
                    }
                    else if (source == ClassIndex.this.classPath) {
                        Set<URL> copy;
                        synchronized (ClassIndex.this) {
                            copy = new HashSet<URL>(oldCompile);
                        }
                        dirtyDeps = containsNewRoot(classPath, copy, newRoots, removedRoots, unknownRoots, true);
                    }
                    else if (source == ClassIndex.this.bootPath) {
                        Set<URL> copy;
                        synchronized (ClassIndex.this) {
                            copy = new HashSet<URL>(oldBoot);
                        }
                        dirtyDeps = containsNewRoot(bootPath, copy, newRoots, removedRoots, unknownRoots, true);
                    }
                    if (!unknownRoots.isEmpty()) {
                        attachClassIndexManagerListener();
                        final ClassIndexManager mgr = ClassIndexManager.getDefault();
                        for (Iterator<URL> it = unknownRoots.iterator(); it.hasNext();) {
                            final URL url = it.next();
                            if (!JavaIndex.isIndexed(url)) {
                                it.remove();
                            }
                        }
                        if (!unknownRoots.isEmpty()) {
                            classIndexAdded(new ClassIndexManagerEvent(mgr, unknownRoots));
                        }
                    }
                    if (dirtySource || dirtyDeps) {
                        ClassIndex.this.reset(dirtySource, dirtyDeps);
                        final RootsEvent ae = newRoots.isEmpty() ? null : new RootsEvent(ClassIndex.this, newRoots);
                        final RootsEvent re = removedRoots.isEmpty() ? null : new RootsEvent(ClassIndex.this, removedRoots);
                        //Threading warning:
                        //The Javadoc promises that events are fired under javac lock,
                        //reschedule firing to the Java Worker Thread which runs under javac lock,
                        //trying to access javac lock in this thread may cause deadlock with Java Worker Thread
                        //because the classpath events are fired under the project mutex and it's legal to
                        //aquire project mutex in the CancellableTask.run()
                        fireByWorker(new Runnable() {
                            @Override
                            public void run() {
                                assertParserEventThread();
                                if (ae != null) {
                                    for (ClassIndexListener l : listeners) {
                                        l.rootsAdded(ae);
                                    }
                                }
                                if (re != null) {
                                    for (ClassIndexListener l : listeners) {
                                        l.rootsRemoved(re);
                                    }
                                }
                            }
                        });
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }
    }

    private void fireByWorker (final Runnable action) {
        assert action != null;
        if (Utilities.isTaskProcessorThread()) {
            action.run();
        } else {
            Utilities.scheduleSpecialTask(action, Lookup.getDefault(), 0);
        }
    }

    private static void assertParserEventThread() {
        assert Utilities.isTaskProcessorThread();
    }
    //</editor-fold>
}
