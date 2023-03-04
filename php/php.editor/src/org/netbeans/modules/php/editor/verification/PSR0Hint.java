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
package org.netbeans.modules.php.editor.verification;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Identifier;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NamespaceName;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TypeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public abstract class PSR0Hint extends HintRule {

    @Override
    public void invoke(PHPRuleContext context, List<Hint> result) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                CheckVisitor checkVisitor = createVisitor(fileObject, context.doc);
                phpParseResult.getProgram().accept(checkVisitor);
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                result.addAll(checkVisitor.getHints());
            }
        }
    }

    abstract CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument);

    public static class NamespaceDeclarationHint extends PSR0Hint {
        private static final String HINT_ID = "PSR0.Hint.Namespace"; //NOI18N

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new NamespaceVisitor(this, fileObject, baseDocument);
        }

        private static final class NamespaceVisitor extends CheckVisitor {

            public NamespaceVisitor(PSR0Hint psr0hint, FileObject fileObject, BaseDocument baseDocument) {
                super(psr0hint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("PSR0WrongNamespaceNameHintText=Namespace declaration name doesn't correspond to current directory structure.")
            public void visit(NamespaceDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                NamespaceName namespaceName = node.getName();
                if (namespaceName != null) {
                    String currentNamespaceName = CodeUtils.extractQualifiedName(namespaceName);
                    String namespaceNameToPath = currentNamespaceName.replace('\\', File.separatorChar);
                    String fileDirPath = getFile().getParent();
                    if (!fileDirPath.contains(namespaceNameToPath)) {
                        createHint(namespaceName, Bundle.PSR0WrongNamespaceNameHintText());
                    }
                }
                super.visit(node);
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("PSR0NamespaceHintDesc=")
        public String getDescription() {
            return Bundle.PSR0NamespaceHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR0NamespaceHintDisp=Namespace Declaration")
        public String getDisplayName() {
            return Bundle.PSR0NamespaceHintDisp();
        }
    }

    public static class TypeDeclarationHint extends PSR0Hint {
        private static final String HINT_ID = "PSR0.Hint.Type"; //NOI18N

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new TypeVisitor(this, fileObject, baseDocument);
        }

        private static final class TypeVisitor extends CheckVisitor {
            private static final String PHP_FILE_EXTENSION = ".php"; //NOI18N

            public TypeVisitor(PSR0Hint psr0hint, FileObject fileObject, BaseDocument baseDocument) {
                super(psr0hint, fileObject, baseDocument);
            }

            @Override
            public void visit(ClassDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processTypeDeclaration(node);
                super.visit(node);
            }

            @Override
            public void visit(InterfaceDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processTypeDeclaration(node);
                super.visit(node);
            }

            @Override
            public void visit(TraitDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processTypeDeclaration(node);
                super.visit(node);
            }

            @NbBundle.Messages("PSR0WrongTypeNameHintText=Type declaration name doesn't correspond to current file path.")
            private void processTypeDeclaration(TypeDeclaration node) {
                String currentTypeName = CodeUtils.extractTypeName(node);
                String typeNameToPath = currentTypeName.replace('_', File.separatorChar);
                String filePath = getFile().getPath();
                if (!filePath.endsWith(typeNameToPath + PHP_FILE_EXTENSION)) {
                    Identifier name = node.getName();
                    createHint(name, Bundle.PSR0WrongTypeNameHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("PSR0TypeHintDesc=")
        public String getDescription() {
            return Bundle.PSR0TypeHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR0TypeHintDisp=Type Declaration")
        public String getDisplayName() {
            return Bundle.PSR0TypeHintDisp();
        }
    }

    private abstract static class CheckVisitor extends DefaultVisitor {
        private final PSR0Hint psr0hint;
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final File file;
        private final List<Hint> hints;

        public CheckVisitor(PSR0Hint psr0hint, FileObject fileObject, BaseDocument baseDocument) {
            this.psr0hint = psr0hint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.file = FileUtil.toFile(fileObject);
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return hints;
        }

        @NbBundle.Messages({
            "# {0} - Text which describes the violation",
            "PSR0ViolationHintText=PSR-0 Violation:\n{0}"
        })
        protected void createHint(ASTNode node, String message) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), node.getEndOffset());
            if (psr0hint.showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        psr0hint,
                        Bundle.PSR0ViolationHintText(message),
                        fileObject,
                        offsetRange,
                        null,
                        500));
            }
        }

        protected File getFile() {
            return file;
        }

    }

    @Override
    public boolean getDefaultEnabled() {
        return false;
    }
}
