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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.el;

import com.sun.el.parser.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.web.el.spi.ELPlugin;

/**
 * Expression Language indexer.
 *
 * TODO: should store node offsets to the index
 *
 * @author Erno Mononen
 */
public final class ELIndexer extends EmbeddingIndexer {

    private static final Logger LOGGER = Logger.getLogger(ELIndexer.class.getName());

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        ELParserResult elResult = (ELParserResult) parserResult;
        if (!elResult.hasElements()) {
            return;
        }

        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOGGER.log(Level.WARNING, null, ioe);
            return;
        }
        new Analyzer(elResult, support).analyze();
    }

    private static class Analyzer implements NodeVisitor {

        private final ELParserResult parserResult;
        private final IndexingSupport support;
        private final List<IndexDocument> documents = new ArrayList<>();

        public Analyzer(ELParserResult parserResult, IndexingSupport support) {
            this.parserResult = parserResult;
            this.support = support;
        }

        void analyze() {
            for (final ELElement each : parserResult.getElements()) {
                if (each.isValid()) {
                    IndexDocument doc = support.createDocument(parserResult.getFileObject());
                    documents.add(doc);
                    addPair(Fields.EXPRESSION, each.getExpression().getPreprocessedExpression());
                    each.getNode().accept(this);
                    support.addDocument(doc);
                }
            }
        }

        private IndexDocument getCurrent() {
            assert !documents.isEmpty() : "No current document";
            return documents.get(documents.size() - 1);
        }

        public List<IndexDocument> getDocuments() {
            return documents;
        }
        
        private void addPair(String key, String value) {
            if(value != null) {
                getCurrent().addPair(key, value, true, true);
            }
        }

        @Override
        public void visit(Node node) throws ELException {
            if (node instanceof AstIdentifier) {
                String identifier = ((AstIdentifier) node).getImage();
                addPair(Fields.IDENTIFIER, identifier);
            } else if (NodeUtil.isMethodCall(node)) {
                String method = node.getImage();
                addPair(Fields.METHOD, method);
            } else if (node instanceof AstDotSuffix) {
                String property = ((AstDotSuffix) node).getImage();
                addPair(Fields.PROPERTY, property);
            }
        }
    }

    public static final class Fields {

        public static final String SEPARATOR = "|";
        public static final String EXPRESSION = "expression";
        public static final String IDENTIFIER = "identifier";
        public static final String IDENTIFIER_FULL_EXPRESSION = "identifier_full_expression";
        public static final String PROPERTY = "property";
        public static final String PROPERTY_OWNER = "property_owner";
        public static final String PROPERTY_FULL_EXPRESSION = "property_full_expression";
        public static final String METHOD = "method";
        public static final String METHOD_FULL_EXPRESSION = "method_full_expression";

        static String encode(String... values) {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < values.length; i++) {
                result.append(values[i]);
                if (i + 1 < values.length) {
                    result.append(SEPARATOR);
                }

            }
            return result.toString();
        }

        public static String[] split(String field) {
            return field.split("\\" + SEPARATOR);
        }
    }

    public static final class Factory extends EmbeddingIndexerFactory {

        static final String NAME = "EL"; //NOI18N
        static final int VERSION = 1;
        private static Collection<String> INDEXABLE_MIMETYPES;

        private static synchronized Collection<String> getIndexableMimeTypes() {
            if(INDEXABLE_MIMETYPES == null) {
                INDEXABLE_MIMETYPES = new ArrayList<>();
                for(ELPlugin plugin : ELPlugin.Query.getELPlugins()) {
                    INDEXABLE_MIMETYPES.addAll(plugin.getMimeTypes());
                }
            }
            return INDEXABLE_MIMETYPES;
        }

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new ELIndexer();
            }
            return null;
        }

        private static boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            return getIndexableMimeTypes().contains(indexable.getMimeType());
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
        }

        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }
    }
}
