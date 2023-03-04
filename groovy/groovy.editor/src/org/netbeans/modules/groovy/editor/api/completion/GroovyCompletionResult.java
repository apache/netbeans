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

package org.netbeans.modules.groovy.editor.api.completion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.SourceUnit;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.csl.api.CodeCompletionResult;
import org.netbeans.modules.csl.api.CompletionProposal;
import org.netbeans.modules.csl.api.ElementKind;
import org.netbeans.modules.csl.spi.DefaultCompletionResult;
import org.netbeans.modules.groovy.editor.imports.ImportHelper;
import org.netbeans.modules.groovy.editor.api.ASTUtils;
import org.netbeans.modules.groovy.editor.api.completion.util.CompletionContext;
import org.netbeans.modules.groovy.editor.imports.ImportUtils;
import org.openide.filesystems.FileObject;

/**
 * Groovy specific implementation of {@link CodeCompletionResult}. This implementation
 * is needed for fast import (post processing insert item).
 *
 * @author Martin Janicek
 */
public class GroovyCompletionResult extends DefaultCompletionResult {

    private final CompletionContext context;
    private final ModuleNode root;
    private final FileObject fo;

    public GroovyCompletionResult(List<CompletionProposal> list, CompletionContext context) {
        super(list, false);

        this.context = context;
        this.root = ASTUtils.getRoot(context.getParserResult());
        this.fo = context.getSourceFile();
    }

    @Override
    public void afterInsert(@NonNull CompletionProposal item) {
        // See issue #235175
        if (root == null) {
            return;
        }

        // Don't add import statement if we are completing import statement - see #228587
        if (context.isBehindImportStatement()) {
            return;
        }

        if (item instanceof CompletionItem.TypeItem) {
            CompletionItem.TypeItem typeItem = (CompletionItem.TypeItem) item;
            
            // Don't add import statement for default imports
            if (ImportUtils.isDefaultlyImported(typeItem.getFqn())) {
                return;
            }
        }

        final ElementKind kind = item.getKind();
        final String name = item.getName();
        
        if (kind == ElementKind.CLASS || kind == ElementKind.INTERFACE || kind == ElementKind.CONSTRUCTOR) {
            List<String> imports = ImportCollector.collect(root);

            if (!imports.contains(name)) {
                ImportHelper.resolveImport(fo, root.getPackageName(), name);
            }
        }
    }
    
    private static final class ImportCollector extends ClassCodeVisitorSupport {

        private final ModuleNode moduleNode;
        private final List<String> imports;

        private ImportCollector(ModuleNode moduleNode) {
            this.moduleNode = moduleNode;
            this.imports = new ArrayList<String>();
        }

        public static List<String> collect(ModuleNode root) {
            ImportCollector collector = new ImportCollector(root);
            collector.visitImports(root);
            return collector.imports;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return moduleNode.getContext();
        }

        @Override
        public void visitImports(ModuleNode node) {
            for (ImportNode importNode : node.getImports()) {
                imports.add(importNode.getType().getNameWithoutPackage());
            }
            super.visitImports(node);
        }
    }
}
