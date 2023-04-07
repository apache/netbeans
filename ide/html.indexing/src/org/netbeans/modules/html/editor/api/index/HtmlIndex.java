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
package org.netbeans.modules.html.editor.api.index;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.common.api.DependenciesGraph;
import org.netbeans.modules.web.common.api.DependenciesGraph.Node;
import org.netbeans.modules.web.common.api.FileReference;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

import static java.util.Arrays.asList;

/**
 * An instance of the indexer which can be held until the source roots are valid.
 * 
 * TODO: Release the cached value of html index once any of the underlying data 
 * models changes (mainly the classpath).
 * 
 * @author marekfukala
 */
public class HtmlIndex {

    /**
     * Name of the index file.
     */
    public static final String NAME = "html"; //NOI18N
    
    /**
     * Index version.
     */
    public static final int VERSION = 3;

    /**
     * Name of the field with references.
     */
    public static final String REFERS_KEY = "imports"; //NOI18N

    private static final Map<Project, HtmlIndex> INDEXES = new WeakHashMap<>();

    /**
     * Returns per-project cached instance of HtmlIndex
     * 
     */
    public static HtmlIndex get(Project project) throws IOException {
        return get(project, true);
    }
    
    public static HtmlIndex get(Project project, boolean createIfNeccesary) throws IOException {
        if(project == null) {
            throw new NullPointerException();
        }
        synchronized (INDEXES) {
            HtmlIndex index = INDEXES.get(project);
            if(index == null && createIfNeccesary) {
                index = new HtmlIndex(project);
                INDEXES.put(project, index);
            }
            return index;
        }
    }

    private final QuerySupport querySupport;
    private ChangeSupport changeSupport;

    /** Creates a new instance of JsfIndex */
    private HtmlIndex(Project project) throws IOException {
        //QuerySupport now refreshes the roots indexes so it can held until the source roots are valid
        Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                null /* all source roots */,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        this.querySupport = QuerySupport.forRoots(NAME, VERSION, sourceRoots.toArray(new FileObject[]{}));
        this.changeSupport = new ChangeSupport(this);
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    public void removeChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }
    
    // TODO: should not be in the API; for now it is OK; need to talk to Marek
    // whether this approach to notification of changes makes any sense or should
    // be done completely differently
    public void notifyChange() {
        changeSupport.fireChange();
    }
    
    
    /**
     *
     * @param keyName
     * @param value
     * @return returns a collection of files which contains the keyName key and the
     * value matches the value regular expression
     */
    public Collection<FileObject> find(String keyName, String value) {
        try {
            Collection<FileObject> matchedFiles = new LinkedList<>();
            Collection<? extends IndexResult> results = querySupport.query(keyName, value, QuerySupport.Kind.REGEXP, keyName);
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
     * Gets two maps wrapped in the AllDependenciesMaps class which contains
     * all dependencies defined by imports in the current project.
     *
     * @return instance of AllDependenciesMaps
     * @throws IOException
     */
    public AllDependenciesMaps getAllDependencies() throws IOException {
        Collection<? extends IndexResult> results = filterDeletedFiles(querySupport.query(
                REFERS_KEY, "", QuerySupport.Kind.PREFIX, REFERS_KEY));
        Map<FileObject, Collection<FileReference>> source2dests = new HashMap<>();
        Map<FileObject, Collection<FileReference>> dest2sources = new HashMap<>();
        for (IndexResult result : results) {
            String[] imports = result.getValues(REFERS_KEY);
            if (imports != null) {
                FileObject file = result.getFile();
                Collection<FileReference> imported = new HashSet<>();
                for (String importedFileName : imports) {
                    //resolve the file
                    FileReference resolvedReference = WebUtils.resolveToReference(file, importedFileName);
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
        }

        return new AllDependenciesMaps(source2dests, dest2sources);

    }

    /**
     * Returns list of all remote URLs
     */
    public List<URL> getAllRemoteDependencies() throws IOException {
        Collection<? extends IndexResult> results = filterDeletedFiles(querySupport.query(REFERS_KEY, "", QuerySupport.Kind.PREFIX, REFERS_KEY));
        Set<String> paths = new HashSet<>();
        for (IndexResult result : results) {
            String[] importsValue = result.getValues(REFERS_KEY);
            if(importsValue != null) {
                paths.addAll(asList(importsValue));
            }
        }
        List<URL> urls = new ArrayList<>();
        for (String p : paths) {
            // #215468 - better handling of protocol-relative JavaScript files:
            if (p.startsWith("//")) { // NOI18N
                p = "http:" + p; // NOI18N
            }
            // TODO: any better way to pick only remote URLs?
            if (p.startsWith("http")) { // NOI18N
                try {
                    urls.add(new URL(p));
                } catch (MalformedURLException ex) {
                    // #219118 - ignore invalid URLs silently
                }
            }
        }
        return urls;
    }
    

    /**
     * Gets all 'related' files to the given html file object.
     *
     * @param htmlFile
     * @return a collection of all files which either imports or are imported
     * by the given htmlFile both directly and indirectly (transitive relation)
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

    public static class AllDependenciesMaps {

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
        public Map<FileObject, Collection<FileReference>> getDest2source() {
            return dest2source;
        }

        /**
         *
         * @return map of fileobject -> collection of fileobject(s) describing
         * relations between css file defined by import directive. The key represents
         * a fileobject which imports the files from the value's collection.
         */
        public Map<FileObject, Collection<FileReference>> getSource2dest() {
            return source2dest;
        }

    }

}
