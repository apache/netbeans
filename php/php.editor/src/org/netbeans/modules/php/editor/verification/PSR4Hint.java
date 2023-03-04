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
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
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
 * Check class name corresponds to PSR-4 rules.
 *
 * @see https://www.php-fig.org/psr/psr-4/
 */
public abstract class PSR4Hint extends HintRule {

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

    public static class NamespaceDeclarationHint extends PSR4Hint {

        private static final String HINT_ID = "PSR4.Hint.Namespace"; //NOI18N

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new NamespaceVisitor(this, fileObject, baseDocument);
        }

        private static final class NamespaceVisitor extends CheckVisitor {

            public NamespaceVisitor(PSR4Hint psr4hint, FileObject fileObject, BaseDocument baseDocument) {
                super(psr4hint, fileObject, baseDocument);
            }

            @Override
            @NbBundle.Messages("PSR4WrongNamespaceNameHintText=Namespace declaration name doesn't correspond to current directory structure.")
            public void visit(NamespaceDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                NamespaceName namespaceName = node.getName();
                if (namespaceName != null && getFile() != null) {
                    int endOffset = namespaceName.getEndOffset();
                    File currentDir = getFile().getParentFile();
                    ListIterator<Identifier> segmentsIterator = namespaceName.getSegments().listIterator(namespaceName.getSegments().size());
                    while (segmentsIterator.hasPrevious()) {
                        if (CancelSupport.getDefault().isCancelled()) {
                            return;
                        }
                        Identifier segment = segmentsIterator.previous();
                        if (!segment.getName().equals(currentDir.getName())) {
                            createHint(namespaceName, Bundle.PSR4WrongNamespaceNameHintText(), endOffset);
                            break;
                        }
                        // Move end offset by length of directory name plus separator.
                        endOffset -= currentDir.getName().length() + 1;
                        currentDir = currentDir.getParentFile();
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
        @NbBundle.Messages("PSR4NamespaceHintDesc=Namespace MUST match (case-sensitive) file directory.")
        public String getDescription() {
            return Bundle.PSR4NamespaceHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR4NamespaceHintDisp=Namespace Declaration")
        public String getDisplayName() {
            return Bundle.PSR4NamespaceHintDisp();
        }
    }

    public static class TypeDeclarationHint extends PSR4Hint {

        private static final String HINT_ID = "PSR4.Hint.Type"; //NOI18N

        @Override
        CheckVisitor createVisitor(FileObject fileObject, BaseDocument baseDocument) {
            return new TypeVisitor(this, fileObject, baseDocument);
        }

        private static final class TypeVisitor extends CheckVisitor {

            private static final String PHP_FILE_EXTENSION = ".php"; //NOI18N

            public TypeVisitor(PSR4Hint psr4hint, FileObject fileObject, BaseDocument baseDocument) {
                super(psr4hint, fileObject, baseDocument);
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

            @Override
            public void visit(EnumDeclaration node) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processTypeDeclaration(node);
                super.visit(node);
            }

            @NbBundle.Messages("PSR4WrongTypeNameHintText=Type declaration name doesn't correspond to current file name.")
            private void processTypeDeclaration(TypeDeclaration node) {
                File file = getFile();
                if (file == null) {
                    return;
                }
                String filename = file.getName();
                String currentTypeName = CodeUtils.extractTypeName(node);
                if (!filename.equals(currentTypeName + PHP_FILE_EXTENSION)) {
                    Identifier name = node.getName();
                    createHint(name, Bundle.PSR4WrongTypeNameHintText());
                }
            }

        }

        @Override
        public String getId() {
            return HINT_ID;
        }

        @Override
        @NbBundle.Messages("PSR4TypeHintDesc=Type name MUST match (case-sensitive) file name.")
        public String getDescription() {
            return Bundle.PSR4TypeHintDesc();
        }

        @Override
        @NbBundle.Messages("PSR4TypeHintDisp=Type Declaration")
        public String getDisplayName() {
            return Bundle.PSR4TypeHintDisp();
        }
    }

    private abstract static class CheckVisitor extends DefaultVisitor {

        private final PSR4Hint psr4hint;
        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final File file;
        private final List<Hint> hints;

        public CheckVisitor(PSR4Hint psr4hint, FileObject fileObject, BaseDocument baseDocument) {
            this.psr4hint = psr4hint;
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.file = FileUtil.toFile(fileObject);
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return Collections.unmodifiableList(hints);
        }

        protected void createHint(ASTNode node, String message) {
            int endOffset = node.getEndOffset();
            createHint(node, message, endOffset);
        }

        @NbBundle.Messages({
            "# {0} - Text which describes the violation",
            "PSR4ViolationHintText=PSR-4 Violation:\n{0}"
        })
        protected void createHint(ASTNode node, String message, int endOffset) {
            OffsetRange offsetRange = new OffsetRange(node.getStartOffset(), endOffset);
            if (psr4hint.showHint(offsetRange, baseDocument)) {
                hints.add(new Hint(
                        psr4hint,
                        Bundle.PSR4ViolationHintText(message),
                        fileObject,
                        offsetRange,
                        null,
                        500));
            }
        }

        @CheckForNull
        protected File getFile() {
            return file;
        }

    }

    @Override
    public boolean getDefaultEnabled() {
        return true;
    }
}
