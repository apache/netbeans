/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
