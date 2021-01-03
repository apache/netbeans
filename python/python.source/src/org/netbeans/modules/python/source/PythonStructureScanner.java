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
package org.netbeans.modules.python.source;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import org.netbeans.modules.python.source.lexer.PythonLexerUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.api.StructureItem;
import org.netbeans.modules.csl.api.StructureScanner;
import org.netbeans.modules.csl.api.StructureScanner.Configuration;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.python.api.Util;
import org.netbeans.modules.python.source.scopes.ScopeInfo;
import org.netbeans.modules.python.source.scopes.SymInfo;
import org.netbeans.modules.python.source.scopes.SymbolTable;
import org.openide.util.Exceptions;
import org.python.antlr.PythonTree;
import org.python.antlr.Visitor;
import org.python.antlr.ast.ClassDef;
import org.python.antlr.ast.FunctionDef;
import org.python.antlr.ast.Str;

/**
 * This class analyzes the structure of a Python parse tree
 * and infers structure (navigation items, folds, etc.)
 *
 */
public class PythonStructureScanner implements StructureScanner {
    private static final Logger LOGGER = Logger.getLogger(Util.class.getName());
    
    public static AnalysisResult analyze(PythonParserResult info) {
        AnalysisResult analysisResult = new AnalysisResult();

        PythonTree root = PythonAstUtils.getRoot(info);
        if (root != null) {
            SymbolTable scopes = PythonAstUtils.getParseResult(info).getSymbolTable();
            StructureVisitor visitor = new StructureVisitor(scopes);
            try {
                visitor.visit(root);
                analysisResult.setElements(visitor.getRoots());
            } catch(RuntimeException ex) {
                // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                if (ex.getMessage().startsWith("Unexpected node: <mismatched token: [@")) {
                   LOGGER.log(Level.FINE, ex.getMessage());
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        return analysisResult;
    }

    @Override
    public List<? extends StructureItem> scan(ParserResult info) {
        PythonParserResult parseResult = PythonAstUtils.getParseResult(info);
        if (parseResult == null) {
            return Collections.emptyList();
        }

        return getStructure(parseResult).getElements();
    }
    
    public PythonStructureScanner.AnalysisResult getStructure(PythonParserResult result) {
        // TODO Cache ! (Used to be in PythonParserResult
        AnalysisResult analysisResult = PythonStructureScanner.analyze(result);
        return analysisResult;
    }

    @Override
    public Map<String, List<OffsetRange>> folds(ParserResult info) {
        PythonParserResult result = PythonAstUtils.getParseResult(info);
        PythonTree root = PythonAstUtils.getRoot(result);
        if (root == null) {
            return Collections.emptyMap();
        }

        //TranslatedSource source = result.getTranslatedSource();
        //
        //AnalysisResult ar = result.getStructure();
        //
        //List<?extends AstElement> elements = ar.getElements();
        //List<StructureItem> itemList = new ArrayList<StructureItem>(elements.size());

        BaseDocument doc = GsfUtilities.getDocument(result.getSnapshot().getSource().getFileObject(), false);
        if (doc != null) {
            try {
                doc.readLock(); // For Utilities.getRowEnd() access
                FoldVisitor visitor = new FoldVisitor((PythonParserResult) info, doc);
                visitor.visit(root);
                List<OffsetRange> codeBlocks = visitor.getCodeBlocks();

                Map<String, List<OffsetRange>> folds = new HashMap<>();
                folds.put("codeblocks", codeBlocks); // NOI18N

                return folds;
            } catch (RuntimeException ex) {
                // Fix for https://netbeans.org/bugzilla/show_bug.cgi?id=255247
                if (ex.getMessage().startsWith("Unexpected node: <mismatched token: [@")) {
                   LOGGER.log(Level.FINE, ex.getMessage());
                } else {
                    throw ex;
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                doc.readUnlock();
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Configuration getConfiguration() {
        return new Configuration(true, true, -1);
    }

    private static class FoldVisitor extends Visitor {
        private List<OffsetRange> codeBlocks = new ArrayList<>();
        private PythonParserResult info;
        private BaseDocument doc;

        private FoldVisitor(PythonParserResult info, BaseDocument doc) {
            this.info = info;
            this.doc = doc;
        }

        private void addFoldRange(PythonTree node) {
            OffsetRange astRange = PythonAstUtils.getRange(node);

            OffsetRange lexRange = PythonLexerUtils.getLexerOffsets(info, astRange);
            if (lexRange != OffsetRange.NONE) {
                try {
                    int startRowEnd = Utilities.getRowEnd(doc, lexRange.getStart());
                    if (startRowEnd < lexRange.getEnd()) {
                        codeBlocks.add(new OffsetRange(startRowEnd, lexRange.getEnd()));
                    }
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        @Override
        public Object visitClassDef(ClassDef node) throws Exception {
            addFoldRange(node);

            return super.visitClassDef(node);
        }

        @Override
        public Object visitFunctionDef(FunctionDef node) throws Exception {
            addFoldRange(node);

            return super.visitFunctionDef(node);
        }

        @Override
        public Object visitStr(Str node) throws Exception {
            addFoldRange(node);
            return super.visitStr(node);
        }

        public List<OffsetRange> getCodeBlocks() {
            return codeBlocks;
        }
    }

    private static class StructureVisitor extends Visitor {
        List<PythonStructureItem> roots = new ArrayList<>();
        List<PythonStructureItem> stack = new ArrayList<>();
        SymbolTable scopes;

        StructureVisitor(SymbolTable scopes) {
            this.scopes = scopes;
        }

        private List<PythonStructureItem> getRoots() {
            return roots;
        }

        @Override
        public Object visitClassDef(ClassDef def) throws Exception {
            PythonStructureItem item = new PythonStructureItem(scopes, def);
            add(item);

            ScopeInfo scope = scopes.getScopeInfo(def);
            if (scope != null && scope.attributes.size() > 0) {
                for (Map.Entry<String, SymInfo> entry : scope.attributes.entrySet()) {
                    // TODO - sort these puppies? Right now their natural order will be
                    // random (hashkey dependent) instead of by source position or by name
                    SymInfo sym = entry.getValue();
                    if (sym.node != null) {
                        String name = entry.getKey();
                        PythonStructureItem attribute = new PythonStructureItem(scopes, sym.node, name, ElementKind.ATTRIBUTE);
                        item.add(attribute);
                    }
                }
            }

            stack.add(item);
            Object result = super.visitClassDef(def);
            stack.remove(stack.size() - 1);

            return result;
        }

        @Override
        public Object visitFunctionDef(FunctionDef def) throws Exception {
            PythonStructureItem item = new PythonStructureItem(scopes, def);

            add(item);
            stack.add(item);
            Object result = super.visitFunctionDef(def);
            stack.remove(stack.size() - 1);

            return result;
        }

        private void add(PythonStructureItem child) {
            PythonStructureItem parent = stack.size() > 0 ? stack.get(stack.size() - 1) : null;
            if (parent == null) {
                roots.add(child);
            } else {
                parent.add(child);
            }
        }
    }

    public static class AnalysisResult {
        //private List<?extends AstElement> elements;
        private List<PythonStructureItem> elements;

        private AnalysisResult() {
        }

        //private void setElements(List<?extends AstElement> elements) {
        private void setElements(List<PythonStructureItem> elements) {
            this.elements = elements;
        }

        //public List<?extends AstElement> getElements() {
        public List<PythonStructureItem> getElements() {
            if (elements == null) {
                return Collections.emptyList();
            }
            return elements;
        }
    }
}
