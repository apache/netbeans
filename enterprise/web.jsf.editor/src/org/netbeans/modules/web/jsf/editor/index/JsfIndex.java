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
package org.netbeans.modules.web.jsf.editor.index;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;

/**
 * Generic index mining data from both embedded and binary indexes
 *
 * @author marekfukala
 */
public class JsfIndex {

    public static JsfIndex create(ClassPath sourceCp, ClassPath compileCp, ClassPath executeCp, ClassPath bootCp) {
        return new JsfIndex(sourceCp, compileCp, executeCp, bootCp);
    }
    private final FileObject[] roots;
    private final FileObject[] binaryRoots;

    private final ThreadLocal<QuerySupport> indexCacheEmbedding = new ThreadLocal<>();
    private final ThreadLocal<QuerySupport> indexCacheBinary = new ThreadLocal<>();
    private final ThreadLocal<QuerySupport> indexCacheCustom = new ThreadLocal<>();

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    /**
     * Creates a new instance of JsfIndex
     */
    private JsfIndex(ClassPath sourceCp, ClassPath compileCp, ClassPath executeCp, ClassPath bootCp) {
        //#179930 - merge compile and execute classpath, remove once #180183 resolved
        Collection<FileObject> cbRoots = new HashSet<>();
        cbRoots.addAll(Arrays.asList(compileCp.getRoots()));
        cbRoots.addAll(Arrays.asList(executeCp.getRoots()));
        binaryRoots = cbRoots.toArray(new FileObject[]{});

        Collection<FileObject> croots = new HashSet<>();
        //add source roots
        croots.addAll(Arrays.asList(sourceCp.getRoots()));
        //add boot and compile roots (sources if available)
        for(ClassPath cp : new ClassPath[]{compileCp, bootCp}) {
            for(FileObject root : cp.getRoots()) {
                URL rootUrl = root.toURL();
                FileObject[] sourceRoots = SourceForBinaryQuery.findSourceRoots(rootUrl).getRoots();
                if(sourceRoots.length == 0) {
                    //add the binary root
                    croots.add(root);
                } else {
                    //add the found source roots
                    croots.addAll(Arrays.asList(sourceRoots));
                }
            }
        }
        
        roots = croots.toArray(new FileObject[]{});
    }

    private synchronized QuerySupport createBinaryIndex() throws IOException {
        QuerySupport result = indexCacheBinary.get();
        if (result == null) {
            result = QuerySupport.forRoots(JsfBinaryIndexer.INDEXER_NAME, JsfBinaryIndexer.INDEXER_VERSION, binaryRoots);
            indexCacheBinary.set(result);
        }
        return result;
    }

    private synchronized QuerySupport createCustomIndex() throws IOException {
        QuerySupport result = indexCacheCustom.get();
        if (result == null) {
            result = QuerySupport.forRoots(JsfCustomIndexer.INDEXER_NAME, JsfCustomIndexer.INDEXER_VERSION, roots);
            indexCacheCustom.set(result);
        }
        return result;
    }

    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    public void notifyChange() {
        changeSupport.fireChange();
    }

    // --------------- BOTH EMBEDDING && BINARY INDEXES ------------------
    public Collection<String> getAllCompositeLibraryNames() {
        Collection<String> col = new ArrayList<>();
        try {
            //aggregate data from both indexes
            col.addAll(getAllCompositeLibraryNames(createBinaryIndex()));
            col.addAll(getAllCompositeLibraryNames(createCustomIndex()));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return col;
    }

    private Collection<String> getAllCompositeLibraryNames(QuerySupport index) {
        Collection<String> libNames = new ArrayList<>();
        try {
            Collection<? extends IndexResult> results = index.query(CompositeComponentModel.LIBRARY_NAME_KEY, "", QuerySupport.Kind.PREFIX, CompositeComponentModel.LIBRARY_NAME_KEY);
            for (IndexResult result : results) {
                String libraryName = result.getValue(CompositeComponentModel.LIBRARY_NAME_KEY);
                if (libraryName != null) {
                    libNames.add(libraryName);
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return libNames;
    }

    public Collection<String> getCompositeLibraryComponents(String libraryName) {
        Collection<String> col = new ArrayList<>();
        try {
            //aggregate data from both indexes
            col.addAll(getCompositeLibraryComponents(createBinaryIndex(), libraryName));
            col.addAll(getCompositeLibraryComponents(createCustomIndex(), libraryName));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return col;
    }

    private Collection<String> getCompositeLibraryComponents(QuerySupport index, String libraryName) {
        Collection<String> components = new ArrayList<>();
        try {
            Collection<? extends IndexResult> results = index.query(CompositeComponentModel.LIBRARY_NAME_KEY, libraryName, QuerySupport.Kind.EXACT, CompositeComponentModel.LIBRARY_NAME_KEY);
            for (IndexResult result : results) {
                FileObject file = result.getFile();
                if (file != null) {
                    components.add(file.getName());
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return components;
    }

    public CompositeComponentModel getCompositeComponentModel(String libraryName, String componentName) {
        //try both indexes, the embedding one first
        try {
            CompositeComponentModel model = getCompositeComponentModel(createCustomIndex(), libraryName, componentName);
            return model != null ? model : getCompositeComponentModel(createBinaryIndex(), libraryName, componentName);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public Map<FileObject, CompositeComponentModel> getCompositeComponentModels(String libraryName) {
        //try both indexes, the embedding one first
        Map<FileObject, CompositeComponentModel> models = new HashMap<>();
        try {
            models.putAll(getCompositeComponentModels(createCustomIndex(), libraryName));
            models.putAll(getCompositeComponentModels(createBinaryIndex(), libraryName));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return models;
    }
    
    private Map<FileObject, CompositeComponentModel> getCompositeComponentModels(QuerySupport index, String libraryName) {
        Map<FileObject, CompositeComponentModel> modelsMap = new HashMap<>();
        try {
            Collection<? extends IndexResult> results = index.query(CompositeComponentModel.LIBRARY_NAME_KEY, libraryName, QuerySupport.Kind.EXACT,
                    CompositeComponentModel.LIBRARY_NAME_KEY,
                    CompositeComponentModel.INTERFACE_ATTRIBUTES_KEY,
                    CompositeComponentModel.HAS_IMPLEMENTATION_KEY,
                    CompositeComponentModel.INTERFACE_FACETS,
                    CompositeComponentModel.INTERFACE_DESCRIPTION_KEY);
            for (IndexResult result : results) {
                FileObject file = result.getFile(); //expensive? use result.getRelativePath?
                if (file != null) {
                    CompositeComponentModel model = (CompositeComponentModel) JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class).loadFromIndex(result);
                    modelsMap.put(file, model);
                }
            }
            
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return modelsMap;
    }
    
    private CompositeComponentModel getCompositeComponentModel(QuerySupport index, String libraryName, String componentName) {
        try {
            Collection<? extends IndexResult> results = index.query(CompositeComponentModel.LIBRARY_NAME_KEY, libraryName, QuerySupport.Kind.EXACT,
                    CompositeComponentModel.LIBRARY_NAME_KEY,
                    CompositeComponentModel.INTERFACE_ATTRIBUTES_KEY,
                    CompositeComponentModel.HAS_IMPLEMENTATION_KEY,
                    CompositeComponentModel.INTERFACE_FACETS,
                    CompositeComponentModel.INTERFACE_DESCRIPTION_KEY);
            for (IndexResult result : results) {
                FileObject file = result.getFile(); //expensive? use result.getRelativePath?
                if (file != null) {
                    String fileName = file.getName();
                    //the filename w/o extenstion is the component name
                    if (fileName.equals(componentName)) {
                        return (CompositeComponentModel) JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class).loadFromIndex(result);
                    }
                }

            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Collection<IndexedFile> getAllFaceletsLibraryDescriptors() {
        Collection<IndexedFile> files = new ArrayList<>();
        try {
            //order of the following queries DOES matter! read comment #3 in FaceletsLibrarySupport.parseLibraries()
            queryFaceletsLibraryDescriptors(createBinaryIndex(), files);
            queryFaceletsLibraryDescriptors(createCustomIndex(), files);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return files;
    }

    private void queryFaceletsLibraryDescriptors(QuerySupport index, Collection<IndexedFile> files) throws IOException {
        Collection<? extends IndexResult> results = index.query(
                JsfIndexSupport.FACELETS_LIBRARY_MARK_KEY,
                "true", //NOI18N
                QuerySupport.Kind.EXACT,
                JsfIndexSupport.FACELETS_LIBRARY_MARK_KEY,
                JsfIndexSupport.TIMESTAMP_KEY,
                JsfIndexSupport.FILE_CONTENT_CHECKSUM);

        convertToFiles(results, files);
    }

    public IndexedFile getTagLibraryDescriptor(String namespace) {
        try {
            IndexedFile file = findTLD(createCustomIndex(), namespace);
            return file != null ? file : findTLD(createBinaryIndex(), namespace);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    //returns the first indexed TLD file matching the namespace
    private IndexedFile findTLD(QuerySupport index, String namespace) throws IOException {
        Collection<? extends IndexResult> results = index.query(
                JsfIndexSupport.LIBRARY_NAMESPACE_KEY,
                namespace, //NOI18N
                QuerySupport.Kind.EXACT,
                JsfIndexSupport.TLD_LIBRARY_MARK_KEY,
                JsfIndexSupport.TIMESTAMP_KEY,
                JsfIndexSupport.FILE_CONTENT_CHECKSUM);

        //filter TLD descriptors since the query returns even facelets library descriptors
        for (IndexResult result : results) {
            if(result.getValue(JsfIndexSupport.TLD_LIBRARY_MARK_KEY) == null) {
                continue; //facelets lib. descr., ignore
            }
            FileObject file = result.getFile();
            if (file != null) {
                long timestamp = Long.parseLong(result.getValue(JsfIndexSupport.TIMESTAMP_KEY));
                String md5checksum = result.getValue(JsfIndexSupport.FILE_CONTENT_CHECKSUM);
                return new IndexedFile(timestamp, md5checksum, file);
            }
        }

        return null;

    }

    private void convertToFiles(Collection<? extends IndexResult> results, Collection<IndexedFile> files) {
        for (IndexResult result : results) {
            FileObject file = result.getFile();
            if (file != null) {
                long timestamp = Long.parseLong(result.getValue(JsfIndexSupport.TIMESTAMP_KEY));
                String md5checksum = result.getValue(JsfIndexSupport.FILE_CONTENT_CHECKSUM);
                files.add(new IndexedFile(timestamp, md5checksum, file));
            }
        }
    }


    public Collection<ResourcesMappingModel.Resource> getAllStaticResources() {
        Collection<ResourcesMappingModel.Resource> resources = new ArrayList<>();
        try {
            QuerySupport index = createCustomIndex();
            Collection<? extends IndexResult> results = index.query(ResourcesMappingModel.STATIC_RESOURCES_KEY, "", QuerySupport.Kind.PREFIX, ResourcesMappingModel.STATIC_RESOURCES_KEY);
            for (IndexResult result : results) {
                String resourceString = result.getValue(ResourcesMappingModel.STATIC_RESOURCES_KEY);
                resources.addAll(ResourcesMappingModel.parseResourcesFromString(resourceString));
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return resources;
    }

}
