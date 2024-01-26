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

package org.netbeans.modules.php.editor.index;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;
import org.netbeans.modules.php.api.util.FileUtils;
import static org.netbeans.modules.php.api.util.FileUtils.PHP_MIME_TYPE;
import org.netbeans.modules.php.editor.NavUtils;
import org.netbeans.modules.php.editor.completion.PhpTypeCompletionProviderWrapper.PhpTypeCompletionProvider;
import org.netbeans.modules.php.editor.elements.IndexQueryImpl;
import org.netbeans.modules.php.editor.model.ConstantElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.FunctionScope;
import org.netbeans.modules.php.editor.model.Model;
import org.netbeans.modules.php.editor.model.Model.Type;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.NamespaceScope;
import org.netbeans.modules.php.editor.model.TypeScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.PHPDocTypeNode;
import org.netbeans.modules.php.editor.parser.astnodes.Program;
import org.netbeans.modules.php.editor.parser.astnodes.Scalar;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.netbeans.modules.php.project.api.PhpSourcePath;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomasz.Slota@Sun.COM
 */
public final class PHPIndexer extends EmbeddingIndexer {
    @MIMEResolver.ExtensionRegistration(
        extension = { "php", "php3", "php4", "php5", "phtml", "inc", "phpt" },
        displayName = "#PHPResolver",
        mimeType = PHP_MIME_TYPE,
        position = 282
    )
    @NbBundle.Messages("PHPResolver=PHP Files")
    private static final Logger LOG = Logger.getLogger(PHPIndexer.class.getName());
    // a workaround for issue #132388
    private static final List<String> INDEXABLE_EXTENSIONS = FileUtil.getMIMETypeExtensions(FileUtils.PHP_MIME_TYPE);

    public static final String FIELD_BASE = "base"; //NOI18N
    public static final String FIELD_EXTEND = "extend"; //NOI18N
    public static final String FIELD_CLASS = "clz"; //NOI18N
    public static final String FIELD_SUPER_CLASS = "superclz"; //NOI18N
    public static final String FIELD_IFACE = "iface"; //NOI18N
    public static final String FIELD_SUPER_IFACE = "superiface"; //NOI18N
    public static final String FIELD_CONST = "const"; //NOI18N
    public static final String FIELD_CLASS_CONST = "clz.const"; //NOI18N
    public static final String FIELD_FIELD = "field"; //NOI18N
    public static final String FIELD_METHOD = "method"; //NOI18N
    public static final String FIELD_CONSTRUCTOR = "constructor"; //NOI18N
    public static final String FIELD_INCLUDE = "include"; //NOI18N
    public static final String FIELD_IDENTIFIER = "identifier_used"; //NOI18N
    public static final String FIELD_IDENTIFIER_DECLARATION = "identifier_declaration"; //NOI18N
    public static final String FIELD_NAMESPACE = "ns"; //NOI18N
    public static final String FIELD_TRAIT = "trait"; //NOI18N
    public static final String FIELD_USED_TRAIT = "usedtrait"; //NOI18N
    public static final String FIELD_TRAIT_CONFLICT_RESOLUTION = "traitconf"; //NOI18N
    public static final String FIELD_TRAIT_METHOD_ALIAS = "traitmeth"; //NOI18N
    public static final String FIELD_ENUM = "enum"; //NOI18N
    public static final String FIELD_ENUM_CASE = "enum.case"; //NOI18N
    public static final String FIELD_ATTRIBUTE_CLASS = "attribute.clz"; //NOI18N

    public static final String FIELD_VAR = "var"; //NOI18N
    /** This field is for fast access top level elemnts. */
    public static final String FIELD_TOP_LEVEL = "top"; //NOI18N

    private static final List<String> ALL_FIELDS = new LinkedList<>(
            Arrays.asList(
                new String[] {
                    FIELD_BASE,
                    FIELD_EXTEND,
                    FIELD_CLASS,
                    FIELD_IFACE,
                    FIELD_CONST,
                    FIELD_CLASS_CONST,
                    FIELD_FIELD,
                    FIELD_METHOD,
                    FIELD_CONSTRUCTOR,
                    FIELD_INCLUDE,
                    FIELD_IDENTIFIER,
                    FIELD_VAR,
                    FIELD_TOP_LEVEL,
                    FIELD_NAMESPACE,
                    FIELD_TRAIT,
                    FIELD_USED_TRAIT,
                    FIELD_TRAIT_CONFLICT_RESOLUTION,
                    FIELD_TRAIT_METHOD_ALIAS,
                    FIELD_ENUM,
                    FIELD_ENUM_CASE,
                    FIELD_ATTRIBUTE_CLASS,
                }
            )
    );

    public static List<String> getAllFields() {
        return new LinkedList<>(ALL_FIELDS);
    }

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        PHPParseResult r = (PHPParseResult) parserResult;
        if (r.getProgram() == null) {
            return;
        }
        final FileObject fileObject = r.getSnapshot().getSource().getFileObject();
        assert r.getDiagnostics().isEmpty() || !PhpSourcePath.FileType.INTERNAL.equals(PhpSourcePath.getFileType(fileObject)) : fileObject.getPath();

        IndexQueryImpl.clearNamespaceCache();
        PhpTypeCompletionProvider.getInstance().clearCache();
        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ex) {
            LOG.log(Level.WARNING, null, ex);
            return;
        }
        Model model = r.getModel(Type.COMMON);
        final FileScope fileScope = model.getFileScope();
        for (TypeScope typeScope : ModelUtils.getDeclaredTypes(fileScope)) {
            IndexDocument typeDocument = support.createDocument(indexable);
            typeScope.addSelfToIndex(typeDocument);
            support.addDocument(typeDocument);
        }

        IndexDocument defaultDocument = support.createDocument(indexable);
        for (FunctionScope functionScope : ModelUtils.getDeclaredFunctions(fileScope)) {
            functionScope.addSelfToIndex(defaultDocument);
        }
        for (ConstantElement constantElement : ModelUtils.getDeclaredConstants(fileScope)) {
            constantElement.addSelfToIndex(defaultDocument);
        }
        for (NamespaceScope nsElement : fileScope.getDeclaredNamespaces()) {
            nsElement.addSelfToIndex(defaultDocument);
        }
        support.addDocument(defaultDocument);

        final IndexDocument identifierDocument = support.createDocument(indexable);
        Program program = r.getProgram();
        program.accept(new IdentifierVisitor(identifierDocument));
        support.addDocument(identifierDocument);
    }

    private static final class IdentifierVisitor extends DefaultVisitor {
        private final IndexDocument identifierDocument;

        public IdentifierVisitor(IndexDocument identifierDocument) {
            this.identifierDocument = identifierDocument;
        }

        @Override
        public void visit(Program node) {
            scan(node.getStatements());
            scan(node.getComments());
        }

        @Override
        public void visit(Scalar scalar) {
            String stringValue = scalar.getStringValue();
            if (stringValue != null && stringValue.trim().length() > 0
                    && scalar.getScalarType() == Scalar.Type.STRING && !NavUtils.isQuoted(stringValue)) {
                addSignature(IdentifierSignatureFactory.create(stringValue));
            }
            super.visit(scalar);
        }

        @Override
        public void visit(Identifier identifier) {
            addSignature(IdentifierSignatureFactory.createIdentifier(identifier));
            super.visit(identifier);
        }

        @Override
        public void visit(PHPDocTypeNode node) {
            addSignature(IdentifierSignatureFactory.create(node));
            super.visit(node);
        }

        private void addSignature(final IdentifierSignature signature) {
            signature.save(identifierDocument);
        }

    }

    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "php"; // NOI18N
        public static final int VERSION = 39;

        @Override
        public EmbeddingIndexer createIndexer(final Indexable indexable, final Snapshot snapshot) {

            if (isIndexable(indexable, snapshot)) {
                return new PHPIndexer();
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
            FileObject fileObject = snapshot.getSource().getFileObject();
            return INDEXABLE_EXTENSIONS.contains(fileObject.getExt().toLowerCase());
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
    } // End of Factory class
}
