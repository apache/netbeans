/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */
package org.netbeans.modules.html.editor.indexing;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.html.editor.api.HtmlKit;
import org.netbeans.modules.html.editor.api.gsf.HtmlParserResult;
import org.netbeans.modules.html.editor.api.index.HtmlIndex;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * HTML content indexer.
 * 
 * TODO the file changes event should be aggregated and fire one event once the indexing finishes!!!
 *
 * @author mfukala@netbeans.org
 */
public class HtmlIndexer extends EmbeddingIndexer {

    private static final Logger LOGGER = Logger.getLogger(HtmlIndexer.class.getSimpleName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    public static final String REFERS_KEY = "imports"; //NOI18N
    
    private static RequestProcessor RP = new RequestProcessor();

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        try {
            if(LOG) {
                FileObject fo = parserResult.getSnapshot().getSource().getFileObject();
                LOGGER.log(Level.FINE, "indexing {0}", fo.getPath()); //NOI18N
            }

            HtmlFileModel model = new HtmlFileModel((HtmlParserResult)parserResult);

            IndexingSupport support = IndexingSupport.getInstance(context);
            IndexDocument document = support.createDocument(indexable);

            storeEntries(model.getReferences(), document, REFERS_KEY);

            support.addDocument(document);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private static void fireChange(final FileObject fo) {
        // handle events firing in separate thread:
        RP.post(new Runnable() {
            @Override
            public void run() {
                fireChangeImpl(fo);
            }
        });
    }
    
    static private void fireChangeImpl(FileObject fo) {
        Project p = FileOwnerQuery.getOwner(fo);
        if (p == null) {
            // no project to notify
            return;
        }
        try {
            HtmlIndex index = HtmlIndex.get(p, false);
            if (index != null) {
                index.notifyChange();
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private void storeEntries(Collection<? extends Entry> entries, IndexDocument doc, String key) {
        if (!entries.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            Iterator<? extends Entry> i = entries.iterator();
            while (i.hasNext()) {
                sb.append(i.next().getName());
                if (i.hasNext()) {
                    sb.append(','); //NOI18N
                }
            }
            sb.append(';'); //end of string
            doc.addPair(key, sb.toString(), true, true);
        }
    }

    public static class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "html"; //NOI18N
        public static final int VERSION = 2;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(snapshot)) {
                return new HtmlIndexer();
            } else {
                return null;
            }
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : deleted) {
                    is.removeDocuments(i);
                }
                if (context.getRoot() != null) {
                    fireChange(context.getRoot());
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
                if (context.getRoot() != null) {
                    fireChange(context.getRoot());
                }
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, null, ioe);
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

        private boolean isIndexable(Snapshot snapshot) {
            //index all files possibly containing css
            return HtmlKit.HTML_MIME_TYPE.equals(snapshot.getMimeType());
        }
    }
}
