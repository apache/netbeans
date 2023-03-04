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
import java.util.*;
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
import org.netbeans.modules.parsing.spi.indexing.ConstrainedBinaryIndexer;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.jsf.editor.JsfSupportImpl;
import org.netbeans.modules.web.jsf.editor.facelets.FaceletsLibraryDescriptor;
import org.netbeans.modules.web.jsfapi.api.JsfSupport;
import org.netbeans.modules.web.jsfapi.spi.JsfSupportProvider;
import org.netbeans.modules.web.jsfapi.spi.LibraryUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Looks for .taglib.xml and .tld descriptors and composite component libraries in binary files
 *
 * @author marekfukala
 */
@ConstrainedBinaryIndexer.Registration(
namePattern = ".*\\.tld|.*\\.taglib\\.xml|.*\\.xhtml",
indexVersion = JsfBinaryIndexer.INDEXER_VERSION,
indexerName = JsfBinaryIndexer.INDEXER_NAME)
public class JsfBinaryIndexer extends ConstrainedBinaryIndexer {

    private static final Logger LOGGER = Logger.getLogger(JsfBinaryIndexer.class.getSimpleName());
    private static final String CONTENT_UNKNOWN = "content/unknown";    //NOI18N
    static final int INDEXER_VERSION = 11; //NOI18N
    static final String INDEXER_NAME = "jsfBinary"; //NOI18N

    @Override
    protected void index(Map<String, ? extends Iterable<? extends FileObject>> files, Context context) {
        LOGGER.log(Level.FINE, "indexing {0}", context.getRoot()); //NOI18N

        if (context.getRoot() == null) {
            return;
        }

        processTlds(files.get(CONTENT_UNKNOWN), context);

        processFaceletsLibraryDescriptors(files.get(CONTENT_UNKNOWN), context);

        processFaceletsCompositeLibraries(files.get(CONTENT_UNKNOWN), context);

    }

    private void processTlds(Iterable<? extends FileObject> files, Context context) {
        if(files == null) {
            return ;
        }
        for (FileObject file : findLibraryDescriptors(files, JsfIndexSupport.TLD_LIB_SUFFIX)) {
            try {
                String namespace = FaceletsLibraryDescriptor.parseNamespace(file.getInputStream(), "taglib", "uri");
                if (namespace != null) {
                    JsfIndexSupport.indexTagLibraryDescriptor(context, file, namespace);
                    LOGGER.log(Level.FINE, "The file {0} indexed as a TLD (namespace={1})", new Object[]{file, namespace}); //NOI18N
                }
            } catch (IOException ex) {
                LOGGER.info(String.format("Error parsing %s file: %s", file.getPath(), ex.getMessage()));//NOI18N
            }
        }

    }

    private void processFaceletsLibraryDescriptors(Iterable<? extends FileObject> files, Context context) {
        if(files == null) {
            return ;
        }
        for (FileObject file : findLibraryDescriptors(files,JsfIndexSupport.FACELETS_LIB_SUFFIX)) {
            //no special mimetype for facelet library descriptor AFAIK
            if (file.getNameExt().endsWith(JsfIndexSupport.FACELETS_LIB_SUFFIX)) {
                try {
                    String namespace = FaceletsLibraryDescriptor.parseNamespace(file.getInputStream());
                    if (namespace != null) {
                        JsfIndexSupport.indexFaceletsLibraryDescriptor(context, file, namespace);
                        LOGGER.log(Level.FINE, "The file {0} indexed as a Facelets Library Descriptor", file); //NOI18N
                    }
                } catch (IOException ex) {
                    LOGGER.info(String.format("Error parsing %s file: %s", file.getPath(), ex.getMessage()));//NOI18N
                }
            }
        }

    }

    private void processFaceletsCompositeLibraries(Iterable<? extends FileObject> files, Context context) {
        if(files == null) {
            return ;
        }
        try {
            //look for /META-INF/resources/<folder>/*.xhtml
            //...and index as normal composite library
            final JsfPageModelFactory compositeComponentModelFactory = JsfPageModelFactory.getFactory(CompositeComponentModel.Factory.class);
            final IndexingSupport sup = IndexingSupport.getInstance(context);
            for (final FileObject file : files) {
                if (CompositeComponentModel.isCompositeLibraryMember(file)) {
                    Source source = Source.create(file);
                    try {
                        ParserManager.parse(Collections.singleton(source), new UserTask() {

                            @Override
                            public void run(ResultIterator resultIterator) throws Exception {
                                for (Embedding e : resultIterator.getEmbeddings()) {
                                    if (e.getMimeType().equals("text/html")) {
                                        //NOI18N
                                        HtmlParserResult result = (HtmlParserResult) resultIterator.getResultIterator(e).getParserResult();
                                        CompositeComponentModel ccmodel = (CompositeComponentModel) compositeComponentModelFactory.getModel(result);
                                        if (ccmodel != null) {
                                            //looks like a composite component
                                            IndexDocument doc = sup.createDocument(file);
                                            ccmodel.storeToIndex(doc);
                                            sup.addDocument(doc);

                                            LOGGER.log(Level.FINE, "Composite Libraries Scan: Model created for file {0}", file); //NOI18N
                                        }
                                    }
                                }
                            }
                        });
                    } catch (ParseException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

    }
    public static Collection<FileObject> findLibraryDescriptors(Iterable<? extends FileObject> fos, String suffix) {
        Collection<FileObject> files = new ArrayList<>();
        for (FileObject file : fos) {
            if (file.getNameExt().toLowerCase(Locale.US).endsWith(suffix)) { //NOI18N
                //found library, create a new instance and cache it
                files.add(file);
            }
        }
        return files;
    }

    @Override
    protected void scanFinished(Context context) {
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
    
}
