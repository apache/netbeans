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

package org.netbeans.modules.csl.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.csl.api.ElementHandle;
import org.netbeans.modules.csl.api.IndexSearcher;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.csl.navigation.Icons;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;
import org.netbeans.spi.jumpto.symbol.SymbolDescriptor;
import org.netbeans.spi.jumpto.symbol.SymbolProvider;
import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.filesystems.FileObject;

/**
 *
 * @author vita
 */
public class TypeAndSymbolProvider {

    public String name() {
        return typeProvider ? "CSL-TypeProvider" : "CSL-SymbolProvider"; //NOI18N
    }

    public String getDisplayName() {
        return GsfTaskProvider.getAllLanguageNames();
    }

    public void cancel() {
        synchronized (this) {
            cancelled = true;
        }
    }

    public void cleanup() {
//        synchronized (this) {
//            cachedRoots = null;
//            cachedRootsProjectRef = null;
//        }
    }

    public static final class TypeProviderImpl extends TypeAndSymbolProvider implements TypeProvider {
        public TypeProviderImpl() {
            super(true);
        }

        public void computeTypeNames(Context context, Result result) {
            Set<? extends IndexSearcher.Descriptor> descriptors = compute(
                    context.getText(),
                    context.getSearchType(),
                    context.getProject()
            );

            if (descriptors != null) {
                for(IndexSearcher.Descriptor d : descriptors) {
                    result.addResult(new TypeWrapper(d));
                }
            }
        }
    } // End of TypeProviderProxy class

    public static final class SymbolProviderImpl extends TypeAndSymbolProvider implements SymbolProvider {
        public SymbolProviderImpl() {
            super(false);
        }
        
        public void computeSymbolNames(Context context, Result result) {
            Set<? extends IndexSearcher.Descriptor> descriptors = compute(
                    context.getText(),
                    context.getSearchType(),
                    context.getProject()
            );

            if (descriptors != null) {
                for(IndexSearcher.Descriptor d : descriptors) {
                    result.addResult(new SymbolWrapper(d));
                }
            }
        }
    } // End of SymbolProviderProxy class
    // ------------------------------------------------------------------------
    // Private implementation
    // ------------------------------------------------------------------------

    private static final Logger LOG = Logger.getLogger(TypeAndSymbolProvider.class.getName());
    private static final IndexSearcher.Helper HELPER = new IndexSearcher.Helper() {
        public Icon getIcon(ElementHandle element) {
            return Icons.getElementIcon(element.getKind(), element.getModifiers());
        }

        public void open(FileObject fileObject, ElementHandle element) {
            Source js = Source.create(fileObject);
            if (js != null) {
                UiUtils.open(js, element);
            }
        }
    };
    
    private final boolean typeProvider;

    private boolean cancelled;
//    private Map<Language, Collection<FileObject>> cachedRoots = null;
//    private Reference<Project> cachedRootsProjectRef = null;

    private TypeAndSymbolProvider(boolean typeProvider) {
        this.typeProvider = typeProvider;
    }

    protected final Set<? extends IndexSearcher.Descriptor> compute(String text, SearchType searchType, Project project) {
        resume();

        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Looking for '" + text + "', searchType=" + searchType + ", project=" + project); //NOI18N
        }
        
        Set<IndexSearcher.Descriptor> results = new HashSet<IndexSearcher.Descriptor>();
        for(Language language : LanguageRegistry.getInstance()) {
            if (isCancelled()) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Search '" + text + "', searchType=" + searchType + ", project=" + project + " cancelled"); //NOI18N
                }
                return null;
            }
            
            IndexSearcher searcher = language.getIndexSearcher();
            if (searcher == null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("No IndexSearcher for " + language); //NOI18N
                }
                continue;
            }

            Set<? extends IndexSearcher.Descriptor> languageResults;
//            Collection<FileObject> searchRoots = getRoots(project, language);
//            if (LOG.isLoggable(Level.FINE)) {
//                if (typeProvider) {
//                    LOG.fine("Querying " + searcher + " for types in " + searchRoots); //NOI18N
//                } else {
//                    LOG.fine("Querying " + searcher + " for symbols in " + searchRoots); //NOI18N
//                }
//            }

            Object [] o = t2t(searchType, text);
            if (typeProvider) {
                languageResults = searcher.getTypes(project, (String) o[1], (QuerySupport.Kind) o[0], HELPER);
            } else {
                languageResults = searcher.getSymbols(project, (String) o[1], (QuerySupport.Kind) o[0], HELPER);
            }

            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine(searcher + " found " + languageResults); //NOI18N
            }
            
            if (languageResults != null) {
                results.addAll(languageResults);
            }
        }

        return results;
    }

    private void resume() {
        synchronized (this) {
            cancelled = false;
        }
    }

    private boolean isCancelled() {
        synchronized (this) {
            return cancelled;
        }
    }

//    private Collection<FileObject> getRoots(Project project, Language language) {
//        synchronized (this) {
//            if (cachedRoots != null) {
//                Project cachedRootsProject = cachedRootsProjectRef != null ? cachedRootsProjectRef.get() : null;
//                if (cachedRootsProject == project) {
//                    Collection<FileObject> roots = cachedRoots.get(language);
//                    if (roots != null) {
//                        return roots;
//                    }
//                } else {
//                    cachedRoots = null;
//                    cachedRootsProjectRef = null;
//                }
//            }
//        }
//
//        Collection<FileObject> roots = GsfUtilities.getRoots(project, language.getSourcePathIds(), language.getBinaryPathIds());
//
//        synchronized (this) {
//            if (cancelled) {
//                return null;
//            }
//
//            if (cachedRoots == null) {
//                cachedRoots = new HashMap<Language, Collection<FileObject>>();
//                cachedRootsProjectRef = project == null ? null : new WeakReference<Project>(project);
//            }
//
//            cachedRoots.put(language, roots);
//            return roots;
//        }
//    }

    private static Object [] t2t(SearchType searchType, String text) {
        switch(searchType) {
            case EXACT_NAME:
                return new Object [] { QuerySupport.Kind.EXACT, text };
            case CASE_INSENSITIVE_EXACT_NAME:
                return new Object [] { QuerySupport.Kind.CASE_INSENSITIVE_REGEXP, NameMatcherFactory.wildcardsToRegexp(text,false) };
            case PREFIX:
                return new Object [] { QuerySupport.Kind.PREFIX, text };
            case CASE_INSENSITIVE_PREFIX:
                return new Object [] { QuerySupport.Kind.CASE_INSENSITIVE_PREFIX, text };
            case REGEXP:
                return new Object [] { QuerySupport.Kind.REGEXP, NameMatcherFactory.wildcardsToRegexp(text,true) };
            case CASE_INSENSITIVE_REGEXP:
                return new Object [] { QuerySupport.Kind.CASE_INSENSITIVE_REGEXP, NameMatcherFactory.wildcardsToRegexp(text,true) };
            case CAMEL_CASE:
                return new Object [] { QuerySupport.Kind.CAMEL_CASE, text };
            case CASE_INSENSITIVE_CAMEL_CASE:
                return new Object [] { QuerySupport.Kind.CASE_INSENSITIVE_CAMEL_CASE, text };
            default:
                throw new IllegalStateException("Can't translate " + searchType + " to QuerySupport.Kind"); //NOI18N
        }
    }
    
    private static final class TypeWrapper extends TypeDescriptor {

        private final IndexSearcher.Descriptor delegated;

        public TypeWrapper(IndexSearcher.Descriptor delegated) {
            this.delegated = delegated;
        }

        @Override
        public String getSimpleName() {
            return delegated.getSimpleName();
        }

        @Override
        public String getOuterName() {
            return delegated.getOuterName();
        }

        @Override
        public String getTypeName() {
            return delegated.getTypeName();
        }

        @Override
        public String getContextName() {
            String s = delegated.getContextName();
            if (s != null) {
                return " (" + s + ")"; //NOI18N
            } else {
                return null;
            }
        }

        @Override
        public Icon getIcon() {
            return delegated.getIcon();
        }

        @Override
        public String getProjectName() {
            return delegated.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegated.getProjectIcon();
        }

        @Override
        public FileObject getFileObject() {
            return delegated.getFileObject();
        }

        @Override
        public int getOffset() {
            return delegated.getOffset();
        }

        @Override
        public void open() {
            delegated.open();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof TypeWrapper)) {
                return false;
            }
            final TypeWrapper other = (TypeWrapper) obj;
            return this.delegated == null ?
                    other.delegated == null :
                    this.delegated.equals(other.delegated);
        }

        @Override
        public int hashCode() {
            return delegated == null ?
                0 :
                delegated.hashCode();
        }



    } // End of TypeWrapper class

    private static final class SymbolWrapper extends SymbolDescriptor {
        private final IndexSearcher.Descriptor delegated;

        private SymbolWrapper(IndexSearcher.Descriptor delegated) {
            this.delegated = delegated;
        }

        @Override
        public Icon getIcon() {
            return delegated.getIcon();
        }

        @Override
        public String getProjectName() {
            return delegated.getProjectName();
        }

        @Override
        public Icon getProjectIcon() {
            return delegated.getProjectIcon();
        }

        @Override
        public FileObject getFileObject() {
            return delegated.getFileObject();
        }

        @Override
        public int getOffset() {
            return delegated.getOffset();
        }

        @Override
        public void open() {
            delegated.open();
        }

        @Override
        public String getSymbolName() {
            return delegated.getSimpleName();
        }

        @Override
        public String getOwnerName() {
            String owner = delegated.getContextName();
            if (owner == null) {
                owner = ""; //NOI18N
            }
            return owner;
        }
    } // End of SymbolWrapper class
}
