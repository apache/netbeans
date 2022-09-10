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
package org.netbeans.modules.languages.antlr;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.editor.mimelookup.MimeRegistrations;
import org.netbeans.modules.languages.antlr.v3.Antlr3Language;
import org.netbeans.modules.languages.antlr.v4.Antlr4Language;
import org.netbeans.modules.languages.antlr.v4.Antlr4ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

public class AntlrIndexer extends EmbeddingIndexer {

    // Used to split
    public static final String SEPARATOR = "\\";
    public static final String FIELD_IMPORT = "import";
    public static final String FIELD_DECLARATION = "declaration";
    public static final String FIELD_CASE_INSENSITIVE_DECLARATION = "ci-declaration";
    public static final String FIELD_OCCURRENCE = "occurrence";
    public static final String FIELD_CASE_INSENSITIVE_OCCURRENCE = "ci-occurrence";

    private static final Logger LOG = Logger.getLogger(AntlrIndexer.class.getName());

    @Override
    protected void index(Indexable indexable, Parser.Result parserResult, Context context) {
        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
            return;
        }

        IndexDocument id = support.createDocument(indexable);

        if (parserResult instanceof AntlrParserResult) {
            Map<String, AntlrParserResult.Reference> refs = ((AntlrParserResult) parserResult).references;
            refs.values().stream().forEach(r -> {
                String declarationValue =  r.name + SEPARATOR + r.defOffset.getStart() + SEPARATOR + r.defOffset.getEnd();
                id.addPair(FIELD_DECLARATION, declarationValue, true, true);
                id.addPair(FIELD_CASE_INSENSITIVE_DECLARATION, ci(declarationValue), true, true);
                if(r.occurances != null) {
                    r.occurances.stream().forEach(c2 -> {
                        String occurrenceValue = r.name + SEPARATOR + c2.getStart() + SEPARATOR + c2.getEnd();
                        id.addPair(FIELD_OCCURRENCE, occurrenceValue, true, true);
                        id.addPair(FIELD_CASE_INSENSITIVE_OCCURRENCE, ci(declarationValue), true, true);
                    });
                }
            });
        }

        if (parserResult instanceof Antlr4ParserResult) {
            ((Antlr4ParserResult) parserResult).getImports()
                    .forEach(s -> {
                        id.addPair(FIELD_IMPORT, s, true, true);
                    });
        }

        support.addDocument(id);
    }

    @MimeRegistrations({
        @MimeRegistration(
                mimeType = Antlr3Language.MIME_TYPE,
                service =  EmbeddingIndexerFactory.class
        ),
        @MimeRegistration(
                mimeType = Antlr4Language.MIME_TYPE,
                service =  EmbeddingIndexerFactory.class
        )
    })
    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "antlr"; // NOI18N
        public static final int VERSION = 4;
        private static final int PRIORITY = 100;

        @Override
        public EmbeddingIndexer createIndexer(final Indexable indexable, final Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new AntlrIndexer();
            } else {
                return null;
            }
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            return Antlr3Language.MIME_TYPE.equals(snapshot.getMimeType())
                    || Antlr4Language.MIME_TYPE.equals(snapshot.getMimeType());
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : deleted) {
                    is.removeDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for (Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public int getPriority() {
            return PRIORITY;
        }
    }


    /**
     * Find all imports of the specified file (including transitive ones).
     *
     * @param qs
     * @param sourceFile
     * @return
     */
    public static List<FileObject> transitiveImports(QuerySupport qs, FileObject sourceFile) {
        List<FileObject> result = new ArrayList<>();
        LinkedList<FileObject> toScan = new LinkedList<>();
        Set<String> seen = new HashSet<>();
        result.add(sourceFile);
        toScan.add(sourceFile);
        seen.add(sourceFile.getName());
        while(! toScan.isEmpty()) {
            FileObject target = toScan.poll();
            try {
                qs.getQueryFactory()
                        .file(target)
                        .execute(AntlrIndexer.FIELD_IMPORT)
                        .forEach(c -> {
                            for(String value: c.getValues(AntlrIndexer.FIELD_IMPORT)) {
                                if(! seen.contains(value)) {
                                    seen.add(value);
                                }
                                FileObject foAntlr;
                                if(Antlr3Language.MIME_TYPE.equals(sourceFile.getMIMEType())) {
                                    foAntlr = sourceFile.getParent().getFileObject(value, "g");
                                } else {
                                    foAntlr = sourceFile.getParent().getFileObject(value, "g4");
                                }
                                if(foAntlr.canRead()) {
                                    toScan.add(foAntlr);
                                    result.add(foAntlr);
                                }
                            }
                        });
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return result;
    }

    private static QuerySupport getQuerySupport(final Collection<FileObject> roots) {
        try {
            return QuerySupport.forRoots(
                    AntlrIndexer.Factory.NAME,
                    AntlrIndexer.Factory.VERSION,
                    roots.toArray(new FileObject[0]));
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public static QuerySupport getQuerySupport(final FileObject source) {
        return getQuerySupport(QuerySupport.findRoots(source,
                null,
                null,
                Collections.<String>emptySet()));
    }

    /**
     * Create string representation for case insensitive search
     */
    private static String ci(String declarationValue) {
        return declarationValue.toLowerCase(Locale.ENGLISH);
    }
}
