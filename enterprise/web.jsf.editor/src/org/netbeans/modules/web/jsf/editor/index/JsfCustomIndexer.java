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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.parsing.api.Embedding;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexer;
import org.netbeans.modules.parsing.spi.indexing.CustomIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.JsfUtils;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptor;
import org.netbeans.modules.web.jsfapi.api.DefaultLibraryInfo;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 * Looks for .taglib.xml, .tld descriptors and composite components on source path
 *
 * @author marekfukala, Martin Fousek <marfous@netbeans.org>
 */
public class JsfCustomIndexer extends CustomIndexer {

    static final String INDEXER_NAME = "jsfCustomIndexer"; //NOI18N
    static final int INDEXER_VERSION = 7;
    public static final Logger LOGGER = Logger.getLogger(JsfCustomIndexer.class.getName());

    @Override
    protected void index(Iterable<? extends Indexable> files, Context context) {
        LOGGER.log(Level.FINE, "JsfCustomIndexer: indexing {0}", context.getRoot()); //NOI18N
        if (context.getRoot() == null) {
            return;
        }

        List<URL> toRescan = new ArrayList<>();
        for (Indexable i : files) {
            URL indexableURL = i.getURL();
            if (indexableURL == null) {
                continue;
            }
            FileObject file = URLMapper.findFileObject(indexableURL);
            if (file == null) {
                continue;
            }

            if (JsfIndexSupport.isFaceletsLibraryDescriptor(file)) {
                processFaceletsLibraryDescriptors(file, context);
            } else if (JsfIndexSupport.isTagLibraryDescriptor(file)) {
                processTlds(file, context);
            } else if (JsfUtils.XHTML_MIMETYPE.equals(file.getMIMEType())) {
                processFaceletsCompositeLibraries(file, context, toRescan);
            }
        }

        // issue #226968 - rescan files which could contain composites
        if (!toRescan.isEmpty() && !context.isSupplementaryFilesIndexing()) {
            context.addSupplementaryFiles(context.getRootURI(), toRescan);
        }
    }

    private void processTlds(FileObject file, Context context) {
        LOGGER.log(Level.FINE, "indexing {0}", file); //NOI18N

        try (InputStream is = file.getInputStream()) {
            String namespace = FaceletsLibraryDescriptor.parseNamespace(is);
            if (namespace != null) {
                JsfIndexSupport.indexTagLibraryDescriptor(context, file, namespace);
                LOGGER.log(Level.FINE, "The file {0} indexed as a Tag Library Descriptor", file); //NOI18N
            }
        } catch (IOException ex) {
            LOGGER.info(String.format("Error parsing %s file: %s", file.getPath(), ex.getMessage()));
        }
    }

    private void processFaceletsLibraryDescriptors(FileObject file, Context context) {
        LOGGER.log(Level.FINE, "indexing {0}", file); //NOI18N

        try (InputStream is = file.getInputStream()) {
            String namespace = FaceletsLibraryDescriptor.parseNamespace(is);
            if (namespace != null) {
                JsfIndexSupport.indexFaceletsLibraryDescriptor(context, file, namespace);
                LOGGER.log(Level.FINE, "The file {0} indexed as a Facelets Library Descriptor", file); //NOI18N
            }
        } catch (IOException ex) {
            LOGGER.info(String.format("Error parsing %s file: %s", file.getPath(), ex.getMessage()));
        }
    }

    private void processFaceletsCompositeLibraries(final FileObject file, final Context context, final List<URL> toRescan) {
        try {
            Source source = Source.create(file);
            ParserManager.parse(Collections.singleton(source), new UserTask() {
                @Override
                public void run(ResultIterator ri) throws Exception {
                    for (Embedding e : ri.getEmbeddings()) {
                        if (e.getMimeType().equals("text/html")) { //NOI18N
                            HtmlParserResult parserResult = (HtmlParserResult) ri.getResultIterator(e).getParserResult();
                            FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                            List<IndexDocument> documents = new LinkedList<>();
                            IndexingSupport support = IndexingSupport.getInstance(context);

                            // file could contain and use composite component library
                            if (containsNonStandardNamespaces(parserResult.getNamespaces())) {
                                toRescan.add(fo.toURL());
                            }

                            //get JSF models and index them
                            Collection<JsfPageModel> models = JsfPageModelFactory.getModels(parserResult);
                            for (JsfPageModel model : models) {
                                IndexDocument document = support.createDocument(file);
                                model.storeToIndex(document);
                                documents.add(document);
                            }
                            LOGGER.log(Level.FINE, "indexing {0}, found {1} document.", new Object[]{fo.getPath(), documents.size()}); //NOI18N

                            //add the documents to the index
                            for (IndexDocument d : documents) {
                                support.addDocument(d);
                            }
                        }
                    }
                }

            });
        } catch (ParseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static boolean containsNonStandardNamespaces(Map<String, String> namespaces) {
        for (String ns : namespaces.keySet()) {
            if (DefaultLibraryInfo.forNamespace(ns) == null && !"http://www.w3.org/1999/xhtml".equals(ns)) { //NOI18N
                return true;
            }
        }
        return false;
    }

    public static class Factory extends CustomIndexerFactory {

        @Override
        public CustomIndexer createIndexer() {
            return new JsfCustomIndexer();
        }

        @Override
        public boolean supportsEmbeddedIndexers() {
            return false;
        }

        @Override
        public boolean scanStarted(Context context) {
            try {
                return IndexingSupport.getInstance(context).isValid();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
                return false;
            }
        }

        @Override
        public void scanFinished(Context context) {
            //notify the FaceletsLibrarySupport that the libraries might have changed.
            if (context.getRoot() != null) {  //looks like can be null
                for (Project p : LibraryUtils.getOpenedJSFProjects()) {
                    JsfSupport support = JsfSupportProvider.get(p);
                    if (support != null) {
                        JsfSupportImpl jsfSupportImpl = (JsfSupportImpl) support;
                        if (Arrays.stream(jsfSupportImpl.getClassPathRoots()).anyMatch(f -> f.equals(context.getRoot()))) {
                            jsfSupportImpl.indexedContentPossiblyChanged();
                            jsfSupportImpl.getIndex().notifyChange();   
                        }
                    }
                }
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            //no-op
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            //no-op
        }

        @Override
        public String getIndexerName() {
            return INDEXER_NAME;
        }

        @Override
        public int getIndexVersion() {
            return INDEXER_VERSION;
        }
    }
}
