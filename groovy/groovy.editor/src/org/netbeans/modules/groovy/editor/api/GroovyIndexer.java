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
package org.netbeans.modules.groovy.editor.api;

import groovyjarjarasm.asm.Opcodes;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.MethodNode;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.groovy.editor.api.StructureAnalyzer.AnalysisResult;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTClass;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTElement;
import org.netbeans.modules.groovy.editor.api.elements.index.IndexedElement;
import org.netbeans.modules.groovy.editor.api.parser.GroovyParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.Parser.Result;
import org.netbeans.modules.parsing.spi.indexing.Context;
import org.netbeans.modules.parsing.spi.indexing.Indexable;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.codehaus.groovy.ast.FieldNode;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.groovy.editor.api.lexer.LexUtilities;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTField;
import org.netbeans.modules.groovy.editor.api.elements.ast.ASTMethod;
import org.netbeans.modules.groovy.editor.compiler.ClassNodeCache;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexer;
import org.netbeans.modules.parsing.spi.indexing.EmbeddingIndexerFactory;
import org.netbeans.modules.parsing.spi.indexing.support.IndexDocument;
import org.netbeans.modules.parsing.spi.indexing.support.IndexingSupport;

/**
 *
 * @author Tor Norbye
 * @author Martin Adamek
 */
public class GroovyIndexer extends EmbeddingIndexer {

    // class
    static final String FQN_NAME = "fqn"; //NOI18N
    static final String CLASS_NAME = "class"; //NOI18N
    static final String CASE_INSENSITIVE_CLASS_NAME = "class-ig"; //NOI18N
    // not indexed
    static final String IN = "in"; //NOI18N
    /** Attributes: hh;nnnn where hh is a hex representing flags in IndexedClass, and nnnn is the documentation length */
    static final String CLASS_ATTRS = "attrs"; //NOI18N

    // method
    static final String METHOD_NAME = "method"; //NOI18N

    // constructor
    static final String CONSTRUCTOR = "ctor"; //NOI18N

    // field
    static final String FIELD_NAME = "field"; //NOI18N

    /** Attributes: "i" -> private, "o" -> protected, ", "s" - static/notinstance, "d" - documented */
    //static final String ATTRIBUTE_NAME = "attribute"; //NOI18N

    private static FileObject preindexedDb;

    // some statistics about the indexer

    private static long indexerRunTime = 0;
    private static long indexerFirstRun = 0;
    private static long filesIndexed = 0;

    private static final Logger LOG = Logger.getLogger(GroovyIndexer.class.getName());

    @Override
    protected void index(Indexable indexable, Result parserResult, Context context) {
        long indexerThisStartTime = System.currentTimeMillis();

        if (indexerFirstRun == 0) {
            indexerFirstRun = indexerThisStartTime;
        }

        GroovyParserResult r = ASTUtils.getParseResult(parserResult);
        ASTNode root = ASTUtils.getRoot(r);

        if (root == null) {
            return;
        }

        IndexingSupport support;
        try {
            support = IndexingSupport.getInstance(context);
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
            return;
        }

        TreeAnalyzer analyzer = new TreeAnalyzer(r, support, indexable);
        analyzer.analyze();

        for(IndexDocument doc : analyzer.getDocuments()) {
            support.addDocument(doc);
        }

        filesIndexed++;
        long indexerThisStopTime = System.currentTimeMillis();
        long indexerThisRunTime = indexerThisStopTime - indexerThisStartTime;
        indexerRunTime += indexerThisRunTime;

        LOG.log(Level.FINEST, "Indexed File                : {0}", r.getSnapshot().getSource().getFileObject());
        LOG.log(Level.FINEST, "Indexing time (ms)          : {0}", indexerThisRunTime);

        LOG.log(Level.FINEST, "Number of files indexed     : {0}", filesIndexed);
        LOG.log(Level.FINEST, "Time spend indexing (ms)    : {0}", indexerRunTime);
        LOG.log(Level.FINEST, "Avg indexing time/file (ms) : {0}", indexerRunTime/filesIndexed);
        LOG.log(Level.FINEST, "Time betw. 1st and Last idx : {0}", indexerThisStopTime - indexerFirstRun);
        LOG.log(Level.FINEST, "---------------------------------------------------------------------------------");
    }

    public FileObject getPreindexedDb() {
        return preindexedDb;
    }

    public static final class Factory extends EmbeddingIndexerFactory {

        public static final String NAME = "groovy"; // NOI18N
        public static final int VERSION = 8;

        @Override
        public EmbeddingIndexer createIndexer(Indexable indexable, Snapshot snapshot) {
            if (isIndexable(indexable, snapshot)) {
                return new GroovyIndexer();
            } else {
                return null;
            }
        }

        @Override
        public int getIndexVersion() {
            return VERSION;
        }

        @Override
        public String getIndexerName() {
            return NAME;
        }

        private boolean isIndexable(Indexable indexable, Snapshot snapshot) {
            String extension = snapshot.getSource().getFileObject().getExt();

            if (extension.equals("groovy")) { // NOI18N
                return true;
            }
            return false;
        }

        @Override
        public void filesDeleted(Iterable<? extends Indexable> deleted, Context context) {
            try {
                IndexingSupport support = IndexingSupport.getInstance(context);
                for (Indexable indexable : deleted) {
                    support.removeDocuments(indexable);
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public void rootsRemoved(final Iterable<? extends URL> removedRoots) {

        }
        
        @Override
        public void filesDirty(Iterable<? extends Indexable> dirty, Context context) {
            try {
                IndexingSupport is = IndexingSupport.getInstance(context);
                for(Indexable i : dirty) {
                    is.markDirtyDocuments(i);
                }
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        @Override
        public boolean scanStarted(Context context) {
            ClassNodeCache.createThreadLocalInstance();
            return super.scanStarted(context);
        }

        @Override
        public void scanFinished(Context context) {            
            ClassNodeCache.clearThreadLocalInstance();
            super.scanFinished(context);
        }
    }
    
    private static class TreeAnalyzer {

        private final FileObject file;
        private final IndexingSupport support;
        private final Indexable indexable;
        private final GroovyParserResult result;
        private final List<IndexDocument> documents = new ArrayList<IndexDocument>();

        private String url;
        private BaseDocument doc;
        
        private TreeAnalyzer(GroovyParserResult result, IndexingSupport support, Indexable indexable) {
            this.result = result;
            this.file = result.getSnapshot().getSource().getFileObject();
            this.support = support;
            this.indexable = indexable;
        }

        List<IndexDocument> getDocuments() {
            return documents;
        }

        public void analyze() {
            this.doc = LexUtilities.getDocument(result, true);

            url = file.toURL().toExternalForm();

            // Make relative URLs for urls in the libraries
            url = GroovyIndex.getPreindexUrl(url);

            AnalysisResult ar = result.getStructure();
            List<? extends ASTElement> children = ar.getElements();

            if ((children == null) || (children.size() == 0)) {
                return;
            }

            for (ASTElement child : children) {
                switch (child.getKind()) {
                    case CLASS:
                        analyzeClass((ASTClass) child);
                        break;
                }
            }

        }

        private void analyzeClass(ASTClass element) {
            IndexDocument document = support.createDocument(indexable);
            documents.add(document);
            indexClass(element, document);

            for (ASTElement child : element.getChildren()) {
                switch (child.getKind()) {
                    case METHOD:
                        indexMethod((ASTMethod) child, document);
                        break;
                    case CONSTRUCTOR:
                        indexConstructor((ASTMethod) child, document);
                        break;
                    case FIELD:
                        indexField((ASTField) child, document);
                        break;
                }
            }
        }

        private void indexClass(ASTClass element, IndexDocument document) {
            final String name = element.getName();
            document.addPair(FQN_NAME, element.getFqn(), true, true);
            document.addPair(CLASS_NAME, name, true, true);
            document.addPair(CASE_INSENSITIVE_CLASS_NAME, name.toLowerCase(), true, true);
        }

        private void indexField(ASTField child, IndexDocument document) {

            StringBuilder sb = new StringBuilder(child.getName());
            FieldNode node = (FieldNode) child.getNode();

            sb.append(';').append(org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                    node.getType().getName()));

            int flags = getFieldModifiersFlag(child.getModifiers());
            if (flags != 0 || child.isProperty()) {
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
            }

            if (child.isProperty()) {
                sb.append(';');
                sb.append(child.isProperty());
            }

            // TODO - gather documentation on fields? naeh
            document.addPair(FIELD_NAME, sb.toString(), true, true);
        }

        private void indexConstructor(ASTMethod constructor, IndexDocument document) {
            StringBuilder sb = new StringBuilder();
            sb.append(constructor.getName());
            sb.append(';');

            List<String> params = constructor.getParameterTypes();
            if (!params.isEmpty()) {
                for (String paramName : params) {
                    sb.append(paramName);
                    sb.append(",");
                }

                // Removing last ","
                sb.deleteCharAt(sb.length() - 1);
            }

            Set<Modifier> modifiers = constructor.getModifiers();

            int flags = getMethodModifiersFlag(modifiers);
            if (flags != 0) {
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
            }

            document.addPair(CONSTRUCTOR, sb.toString(), true, true);
        }

        private void indexMethod(ASTMethod child, IndexDocument document) {

            MethodNode childNode = (MethodNode) child.getNode();
            StringBuilder sb = new StringBuilder(ASTUtils.getDefSignature(childNode));

            sb.append(';').append(org.netbeans.modules.groovy.editor.java.Utilities.translateClassLoaderTypeName(
                    childNode.getReturnType().getName()));

            Set<Modifier> modifiers = child.getModifiers();

            int flags = getMethodModifiersFlag(modifiers);

            if (flags != 0) {
                sb.append(';');
                sb.append(IndexedElement.flagToFirstChar(flags));
                sb.append(IndexedElement.flagToSecondChar(flags));
            }

            document.addPair(METHOD_NAME, sb.toString(), true, true);
        }

    }

    // note that default field modifier is private
    private static int getFieldModifiersFlag(Set<Modifier> modifiers) {
        int flags = modifiers.contains(Modifier.STATIC) ? Opcodes.ACC_STATIC : 0;
        if (modifiers.contains(Modifier.PUBLIC)) {
            flags |= Opcodes.ACC_PUBLIC;
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            flags |= Opcodes.ACC_PROTECTED;
        }

        return flags;
    }

    // note that default method (and class) modifier is public
    private static int getMethodModifiersFlag(Set<Modifier> modifiers) {
        int flags = modifiers.contains(Modifier.STATIC) ? Opcodes.ACC_STATIC : 0;
        if (modifiers.contains(Modifier.PRIVATE)) {
            flags |= Opcodes.ACC_PRIVATE;
        } else if (modifiers.contains(Modifier.PROTECTED)) {
            flags |= Opcodes.ACC_PROTECTED;
        }

        return flags;
    }

}
