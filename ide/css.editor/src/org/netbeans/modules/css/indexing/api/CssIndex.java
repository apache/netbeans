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
package org.netbeans.modules.css.indexing.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.editor.csl.CssLanguage;
import org.netbeans.modules.css.indexing.CssIndexModelSupport;
import org.netbeans.modules.css.indexing.CssIndexer;
import org.netbeans.modules.css.refactoring.api.RefactoringElementType;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.DependenciesGraph.Node;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * An instance of the indexer which can be held until the source roots are valid.
 *
 * TODO: Release the cached value of css index once any of the underlying data
 * models changes (mainly the classpath).
 *
 * @author mfukala@netbeans.org
 */
public class CssIndex {

    private static final Logger LOGGER = Logger.getLogger(CssIndex.class.getSimpleName());

    private static final String SCSS_EXT = "scss"; //NOI18N
    private static final String SASS_EXT = "sass"; //NOI18N

    private static final Map<Project, CssIndex> INDEXES = new WeakHashMap<>();
    private static final String VIRTUAL_ELEMENT_MARKER_STR = Character.toString(CssIndexer.VIRTUAL_ELEMENT_MARKER);

     /**
     * Creates a new instance of {@link CssIndex}.
     *
     * @param project The project for which you want to get the index for.
     * @return non null instance of the {@link CssIndex}
     * @throws IOException
     */
    public static CssIndex create(Project project) throws IOException {
        return new CssIndex(project);
    }

    /**
     * Gets an instance of {@link CssIndex}. The instance may be cached.
     *
     * @since 1.34
     * @param project The project for which you want to get the index for.
     * @return non null instance of the {@link CssIndex}
     * @throws IOException
     */
    public static CssIndex get(Project project) throws IOException {
        if(project == null) {
            throw new NullPointerException();
        }
        synchronized (INDEXES) {
            CssIndex index = INDEXES.get(project);
            if(index == null) {
                index = create(project);
                INDEXES.put(project, index);
            }
            return index;
        }
    }

    private final QuerySupport querySupport;
    private final Collection<FileObject> sourceRoots;
    private final ChangeSupport changeSupport;

    private AllDependenciesMaps allDepsCache;
    private long allDepsCache_hashCode;

    /** Creates a new instance of JsfIndex */
    private CssIndex(Project project) throws IOException {
        //QuerySupport now refreshes the roots indexes so it can held until the source roots are valid
        sourceRoots = QuerySupport.findRoots(project,
                null /* all source roots */,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        this.querySupport = QuerySupport.forRoots(CssIndexer.Factory.NAME, CssIndexer.Factory.VERSION, sourceRoots.toArray(new FileObject[]{}));        
        this.changeSupport = new ChangeSupport(this);
    }

    /**
     * Adds a {@link ChangeListener} so one may listen on changes in the index.
     *
     * @param changeListener
     *
     * @since 1.34
     */
    public void addChangeListener(ChangeListener changeListener) {
        changeSupport.addChangeListener(changeListener);
    }

     /**
     * Removes the {@link ChangeListener}.
     *
     * @param changeListener
     *
     * @since 1.34
     */
    public void removeChangeListener(ChangeListener changeListener) {
        changeSupport.removeChangeListener(changeListener);
    }

    // TODO: should not be in the API; for now it is OK; need to talk to Marek
    // whether this approach to notification of changes makes any sense or should
    // be done completely differently
    public void notifyChange() {
        changeSupport.fireChange();
    }

    /**
     * Creates an instance of {@link CssIndexModel} for the given file and factory type.
     *
     * @param <T> the type of requested {@link CssIndexModel}
     * @param factoryClass class of the {@link CssIndexModelFactory}
     * @param file the file you want to get the model for
     * @return instance of the model or null if the model cann't be build upon the requested file index data.
     * @throws IOException
     */
    public <T extends CssIndexModel> T getIndexModel(Class factoryClass, FileObject file) throws IOException {
        if(file == null) {
            throw new NullPointerException("The file argument cannot be null!");
        }
        CssIndexModelFactory<T> factory = CssIndexModelSupport.getFactory(factoryClass);
        if(factory == null) {
            throw new IllegalArgumentException(String.format("No %s class registered as a system service!", factoryClass.getName()));
        }
        final Collection<String> fieldsToLoad = factory.getIndexKeys();
        final Collection<? extends IndexResult> results = querySupport.getQueryFactory().file(file).execute(fieldsToLoad.toArray(new String[0]));
        if (!results.isEmpty()) {
            return factory.loadFromIndex(results.iterator().next());
        }
        return null;
    }

    /**
     * Creates a map of file to {@link CssIndexModel}.
     *
     * @see #getIndexModel(java.lang.Class, org.openide.filesystems.FileObject) 
     *
     * @param <T> the type of requested {@link CssIndexModel}
     * @param factoryClass class of the {@link CssIndexModelFactory}
     * @return instance of the model or null if the model cann't be build upon the requested file index data.
     * @throws IOException
     */
    public <T extends CssIndexModel> Map<FileObject, T> getIndexModels(Class<CssIndexModelFactory<T>> factoryClass) throws IOException {
        CssIndexModelFactory<T> factory = CssIndexModelSupport.getFactory(factoryClass);
        if(factory == null) {
            throw new IllegalArgumentException(String.format("No %s class registered as a system service!", factoryClass.getName()));
        }

        Collection<? extends IndexResult> results =
                    querySupport.query(CssIndexer.CSS_CONTENT_KEY, "", QuerySupport.Kind.PREFIX, factory.getIndexKeys().toArray(new String[0]));

        Map<FileObject, T> file2model = new HashMap<>();
        for(IndexResult result : results) {
            file2model.put(result.getFile(), factory.loadFromIndex(result));
        }
        return file2model;
    }

    /**
     *
     * @param id
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findIds(String id) {
        return find(RefactoringElementType.ID, id);
    }

    /**
     * Returns a collection of file containing declaration of the css id.
     * Note that the generic {@link #findIds(java.lang.String)} method
     * also takes into account the usages of the css id in html code.
     *
     * @since 1.28
     *
     * @param id name of the css id
     * @return
     */
    public Collection<FileObject> findIdDeclarations(String id) {
        return find(RefactoringElementType.ID, id, false);
    }

    /**
     * @since 1.28
     *
     * @return map of all id declarations. See {@link #findAll(org.netbeans.modules.css.refactoring.api.RefactoringElementType)}
     */
    public Map<FileObject, Collection<String>> findAllIdDeclarations() {
        return findAll(RefactoringElementType.ID, false);
    }

    /**
     *
     * @param clazz
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findClasses(String clazz) {
        return find(RefactoringElementType.CLASS, clazz);
    }

   /**
     * @since 1.28
     *
     * @return map of all class declarations. See {@link #findAll(org.netbeans.modules.css.refactoring.api.RefactoringElementType)}
     */
    public Map<FileObject, Collection<String>> findAllClassDeclarations() {
        return findAll(RefactoringElementType.CLASS, false);
    }

    /**
     * Returns a collection of file containing declaration of the css class.
     * Note that the generic {@link #findClasses(java.lang.String)} method
     * also takes into account the usages of the css class in html code.
     *
     * @since 1.28
     *
     * @param clazz name of the css class
     * @return
     */
    public Collection<FileObject> findClassDeclarations(String clazz) {
        return find(RefactoringElementType.CLASS, clazz, false);
    }

    /**
     *
     * @param htmlElement
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findHtmlElement(String htmlElement) {
        return find(RefactoringElementType.ELEMENT, htmlElement);
    }

    /**
     *
     * @param colorCode
     * @return collection of files defining exactly the given element
     */
    public Collection<FileObject> findColor(String colorCode) {
        return find(RefactoringElementType.COLOR, colorCode);
    }

    /**
     *
     * @param prefix
     * @return map of fileobject to collection of classes defined in the file starting with prefix
     */
    public Map<FileObject, Collection<String>> findClassesByPrefix(String prefix) {
        return findByPrefix(RefactoringElementType.CLASS, prefix);
    }

    /**
     *
     * @param prefix
     * @return map of fileobject to collection of ids defined in the file starting with prefix
     */
    public Map<FileObject, Collection<String>> findIdsByPrefix(String prefix) {
        return findByPrefix(RefactoringElementType.ID, prefix);
    }

    /**
     *
     * @param prefix
     * @return map of fileobject to collection of colors defined in the file starting with prefix
     */
    public Map<FileObject, Collection<String>> findColorsByPrefix(String prefix) {
        return findByPrefix(RefactoringElementType.COLOR, prefix);
    }

    public Map<FileObject, Collection<String>> findByPrefix(RefactoringElementType type, String prefix) {
        String keyName = type.getIndexKey();
        Map<FileObject, Collection<String>> map = new HashMap<>();
        try {
            Collection<? extends IndexResult> results = querySupport.query(keyName, prefix, QuerySupport.Kind.PREFIX, keyName);
            for (IndexResult result : results) {
                String[] elements = result.getValues(keyName);
                for(String e : elements) {
                    String val = e;
                    if(val.startsWith(prefix)) {
                        if(val.endsWith(VIRTUAL_ELEMENT_MARKER_STR)) {
                            //strip the marker
                            val = val.substring(0, val.length() - 1);
                        }
                        FileObject file = result.getFile();
                        if(file != null) {
                            map.computeIfAbsent(file, f -> new LinkedList<>())
                                .add(val);
                        } // else file deleted and index not updated yet
                    }
                }

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;
    }

    public Map<FileObject, Collection<String>> findAll(RefactoringElementType type) {
        return findAll(type, true);
    }

    private Map<FileObject, Collection<String>> findAll(RefactoringElementType type, boolean includeVirtualElements) {
        String keyName = type.getIndexKey();
        Map<FileObject, Collection<String>> map = new HashMap<>();
        try {
            Collection<? extends IndexResult> results =
                    querySupport.query(keyName, "", QuerySupport.Kind.PREFIX, keyName);

            for (IndexResult result : filterDeletedFiles(results)) {
                String[] elements = result.getValues(keyName);
                for (String e : elements) {
                    String val;
                    if(e.endsWith(VIRTUAL_ELEMENT_MARKER_STR)) {
                        if(includeVirtualElements) {
                            //strip the marker
                            val = e.substring(0, e.length() - 1);
                        } else {
                            continue; //ignore
                        }
                    } else {
                        val = e;
                    }
                    map.computeIfAbsent(result.getFile(), f -> new LinkedList<>())
                        .add(val);
                }

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return map;
    }

    /**
     *
     * @param type
     * @param value
     * @return returns a collection of files which contains elements with the
     * given name and with the given type
     */
    public Collection<FileObject> find(RefactoringElementType type, String value) {
        return find(type, value, true);
    }

    private Collection<FileObject> find(RefactoringElementType type, String value, boolean includeVirtualElements) {
        String keyName = type.getIndexKey();
        try {
            StringBuilder searchExpression = new StringBuilder();
            searchExpression.append(encodeValueForRegexp(value));
            if(includeVirtualElements) {
                searchExpression.append(CssIndexer.VIRTUAL_ELEMENT_MARKER);
                searchExpression.append('?'); //!?
            }

            Collection<FileObject> matchedFiles = new LinkedList<>();
            Collection<? extends IndexResult> results = querySupport.query(keyName, searchExpression.toString(), QuerySupport.Kind.REGEXP, keyName);
            for (IndexResult result : filterDeletedFiles(results)) {
                matchedFiles.add(result.getFile());
            }
            return matchedFiles;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return Collections.emptyList();
    }

    /**
     * Get all stylesheets from the project.
     *
     * @return
     */
    public Collection<FileObject> getAllIndexedFiles() {
        try {
            Collection<? extends IndexResult> results = querySupport.query(CssIndexer.CSS_CONTENT_KEY, "", QuerySupport.Kind.PREFIX, CssIndexer.CSS_CONTENT_KEY);
            Collection<FileObject> stylesheets = new LinkedList<>();
            for(IndexResult result : filterDeletedFiles(results)) {
                if(result.getFile().getMIMEType().equals(CssLanguage.CSS_MIME_TYPE)) {
                    stylesheets.add(result.getFile());
                }
            }
            return stylesheets;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return Collections.emptyList();
        }
    }

    /**
     * Gets all 'related' files to the given css file object.
     *
     * @param cssFile
     * @return a collection of all files which either imports or are imported
     * by the given cssFile both directly and indirectly (transitive relation)
     */
    public DependenciesGraph getDependencies(FileObject cssFile) {
        try {
            DependenciesGraph deps = new DependenciesGraph(cssFile);
            AllDependenciesMaps alldeps = getAllDependencies();
            resolveDependencies(deps.getSourceNode(), alldeps.getSource2dest(), alldeps.getDest2source());
            return deps;
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }

    /**
     * Gets two maps wrapped in the AllDependenciesMaps class which contains
     * all dependencies defined by imports in the current project.
     *
     * @return instance of AllDependenciesMaps
     * @throws IOException
     */
    public AllDependenciesMaps getAllDependencies() throws IOException {
        if(LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, "getAllDependencies", new Throwable());
        }
        long freshAllDeps_hashCode = CssIndexer.getImportsHashCodeForRoots(sourceRoots);
        StringBuilder sb = new StringBuilder();
        for(FileObject sr : sourceRoots) {
            sb.append(sr.getPath());
            sb.append(',');
        }

        LOGGER.log(Level.FINE, "Fresh deps hash code for roots {0} is {1}", new Object[]{sb.toString(), freshAllDeps_hashCode});

        if(allDepsCache != null) {
            LOGGER.fine("allDepsCache is NOT null");
            //verify whether the cache is still valid
            if(allDepsCache_hashCode == freshAllDeps_hashCode) {
                LOGGER.fine("Returning cached dependencies.");
                return allDepsCache;
            }
        } else {
            LOGGER.fine("allDepsCache is null");
        }

        //not cached or invalidated
        allDepsCache = createAllDependencies();
        allDepsCache_hashCode = freshAllDeps_hashCode;
        LOGGER.log(Level.FINE, "Created new dependencties map with with hashcode {0}", allDepsCache_hashCode);

        if(LOGGER.isLoggable(Level.FINE)) {
            StringBuilder deps = new StringBuilder();
            deps.append("dest2source:\n");
            for(Map.Entry<FileObject, Collection<FileReference>> entry : allDepsCache.dest2source.entrySet()) {
                FileObject dest = entry.getKey();
                Collection<FileReference> source = entry.getValue();
                deps.append(dest.getNameExt());
                deps.append("->");
                for(FileReference fref : source) {
                    deps.append(fref.source().getNameExt());
                    deps.append(',');
                }
                deps.append('\n');
            }
            deps.append("source2dest:\n");
            for(Map.Entry<FileObject, Collection<FileReference>> entry : allDepsCache.source2dest.entrySet()) {
                FileObject source = entry.getKey();
                Collection<FileReference> dest = entry.getValue();
                deps.append(source.getNameExt());
                deps.append("->");
                for(FileReference fref : dest) {
                    deps.append(fref.target().getNameExt());
                    deps.append(',');
                }
                deps.append('\n');
            }
            LOGGER.log(Level.FINE, deps.toString());
        }

        return allDepsCache;
    }

    private AllDependenciesMaps createAllDependencies() throws IOException {
        Collection<? extends IndexResult> results = filterDeletedFiles(querySupport.query(CssIndexer.IMPORTS_KEY, "", QuerySupport.Kind.PREFIX, CssIndexer.IMPORTS_KEY));
        Map<FileObject, Collection<FileReference>> source2dests = new HashMap<>();
        Map<FileObject, Collection<FileReference>> dest2sources = new HashMap<>();
        for (IndexResult result : results) {
            FileObject file = result.getFile();
            String[] imports = result.getValues(CssIndexer.IMPORTS_KEY);
            Collection<FileReference> imported = new HashSet<>();
            for (String importedFileName : imports) {
                //resolve the file
                FileReference resolvedReference = resolveImport(file, importedFileName);
                if (resolvedReference != null) {
                    imported.add(resolvedReference);
                    //add reverse dependency
                    Collection<FileReference> sources = dest2sources.get(resolvedReference.target());
                    if (sources == null) {
                        sources = new HashSet<>();
                        dest2sources.put(resolvedReference.target(), sources);
                    }
                    sources.add(resolvedReference);
                }
            }
            source2dests.put(file, imported);
        }

        return new AllDependenciesMaps(source2dests, dest2sources);

    }

    //some hardcoded SASS logic here, may be refactored to some nice resolver SPI though :-)
    @SuppressWarnings("AssignmentToMethodParameter")
    private FileReference resolveImport(final FileObject source, String importedFileName) {
        //possibly remove the query part of the link
        int qmIndex = importedFileName.indexOf("?"); //NOI18N
        if(qmIndex >= 0) {
            importedFileName = importedFileName.substring(0, qmIndex);
        }

        //first try the original file reference
        FileReference resolvedReference = WebUtils.resolveToReference(source, importedFileName);
        if(resolvedReference != null) {
            return resolvedReference;
        }

        //The SASS import spec: http://sass-lang.com/docs/yardoc/file.SASS_REFERENCE.html#import
        //
        //check if the importedFileName already contains an extension
        int dotIndex = importedFileName.lastIndexOf('.');
        String extension = dotIndex == -1 ? null : importedFileName.substring(dotIndex + 1);

        if(extension == null
                || (!SASS_EXT.equalsIgnoreCase(extension) && !SCSS_EXT.equals(extension))) {
            //no extension at all or the extension is not SASS or SCSS

            //if the original reference is not resolved to an existing file
            //so first try to append the .scss extension
            String impliedScssExt = createImpliedFileName(importedFileName, SCSS_EXT, false); //NOI18N
            resolvedReference = WebUtils.resolveToReference(source, impliedScssExt);
            if(resolvedReference != null) {
                return resolvedReference;
            }

            //lets try to imply the leading underscore for sass partials
            String impliedUnderscoreAndScssExt = createImpliedFileName(importedFileName, SCSS_EXT, true); //NOI18N
            resolvedReference = WebUtils.resolveToReference(source, impliedUnderscoreAndScssExt);
            if(resolvedReference != null) {
                return resolvedReference;
            }

            //if still nothing then try .sass extension as a last resort
            String impliedSassExt = createImpliedFileName(importedFileName, SASS_EXT, false); //NOI18N
            resolvedReference = WebUtils.resolveToReference(source, impliedSassExt);
            if(resolvedReference != null) {
                return resolvedReference;
            }

             //lets try to imply the leading underscore for sass partials
            String impliedUnderscoreAndSassExt = createImpliedFileName(importedFileName, SCSS_EXT, true); //NOI18N
            resolvedReference = WebUtils.resolveToReference(source, impliedUnderscoreAndSassExt);
            if(resolvedReference != null) {
                return resolvedReference;
            }

        } else if(SASS_EXT.equalsIgnoreCase(extension) || SCSS_EXT.equalsIgnoreCase(extension)) {
            //lets try to imply the leading underscore for sass partials
            String impliedUnderscoreAndSassExt = createImpliedFileName(importedFileName, null, true);
            resolvedReference = WebUtils.resolveToReference(source, impliedUnderscoreAndSassExt);
            if(resolvedReference != null) {
                return resolvedReference;
            }

        }

        return null; //give up

    }

    /* test */ static String createImpliedFileName(@NonNull String original, String extension, boolean underscore) {
        if(extension == null && !underscore) {
            //no change
            return original;
        }

        if(!underscore) {
            return new StringBuilder().append(original).append('.').append(extension).toString();
        } else {
            //imply underscore
            //1. find the last part - the filename
            int separatorIndex = original.lastIndexOf('/');
            if(separatorIndex == -1) {
                separatorIndex = original.lastIndexOf('\\');
            }
            if(separatorIndex == -1) {
                //just the filename, no folder
                return new StringBuilder()
                        .append('_')
                        .append(original)
                        .append(extension == null ? "" : '.')
                        .append(extension == null ? "" : extension)
                        .toString();
            } else {
                return new StringBuilder()
                        .append(original.substring(0, separatorIndex + 1)) //including the separatorx
                        .append('_')
                        .append(original.substring(separatorIndex + 1)) //the filename
                        .append(extension == null ? "" : '.')
                        .append(extension == null ? "" : extension)
                        .toString();
            }
        }
    }

    private void resolveDependencies(Node base, Map<FileObject, Collection<FileReference>> source2dests, Map<FileObject, Collection<FileReference>> dest2sources) {
        FileObject baseFile = base.getFile();
        Collection<FileReference> destinations = source2dests.get(baseFile);
        if (destinations != null) {
            //process destinations (file this one refers to)
            for(FileReference destinationReference : destinations) {
                FileObject destination = destinationReference.target();
                Node node = base.getDependencyGraph().getNode(destination);
                if(base.addReferedNode(node)) {
                    //recurse only if we haven't been there yet
                    resolveDependencies(node, source2dests, dest2sources);
                }
            }
        }
        Collection<FileReference> sources = dest2sources.get(baseFile);
        if(sources != null) {
            //process sources (file this one is refered by)
            for(FileReference sourceReference : sources) {
                FileObject source = sourceReference.source();
                Node node = base.getDependencyGraph().getNode(source);
                if(base.addReferingNode(node)) {
                    //recurse only if we haven't been there yet
                    resolveDependencies(node, source2dests, dest2sources);
                }
            }
        }

    }

    //if an indexed file is delete and IndexerFactory.filesDeleted() hasn't removed
    //the entris from index yet, then we may receive IndexResult-s with null file.
    //Please note that the IndexResult.getFile() result is cached, so the IndexResult.getFile()
    //won't become null after the query is run, but the file will simply become invalid.
    private Collection<? extends IndexResult> filterDeletedFiles(Collection<? extends IndexResult> queryResult) {
        Collection<IndexResult> filtered = new ArrayList<>();
        for(IndexResult result : queryResult) {
            if(result.getFile() != null) {
                filtered.add(result);
            }
        }
        return filtered;
    }

    private static final String REGEXP_CHARS_TO_ENCODE = ".\\?*+&:{}[]()^$";
    static String encodeValueForRegexp(String value) {
        StringBuilder encoded = new StringBuilder();

        out: for(int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            for(int j = 0; j < REGEXP_CHARS_TO_ENCODE.length(); j++) {
                if(c == REGEXP_CHARS_TO_ENCODE.charAt(j)) {
                    encoded.append('\\');
                    encoded.append(c);
                    continue out;
                }
            }
            encoded.append(c);
        }
        return encoded.toString();
    }

    public static class AllDependenciesMaps {

        @SuppressWarnings("PackageVisibleField")
        Map<FileObject, Collection<FileReference>> source2dest, dest2source;

        public AllDependenciesMaps(Map<FileObject, Collection<FileReference>> source2dest, Map<FileObject, Collection<FileReference>> dest2source) {
            this.source2dest = source2dest;
            this.dest2source = dest2source;
        }

        /**
         *
         * @return reversed map of getSource2dest() (imported file -> collection of
         * importing files)
         */
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Map<FileObject, Collection<FileReference>> getDest2source() {
            return dest2source;
        }

        /**
         *
         * @return map of fileobject -> collection of fileobject(s) describing
         * relations between css file defined by import directive. The key represents
         * a fileobject which imports the files from the value's collection.
         */
        @SuppressWarnings("ReturnOfCollectionOrArrayField")
        public Map<FileObject, Collection<FileReference>> getSource2dest() {
            return source2dest;
        }

    }

}
