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
package org.netbeans.modules.php.editor.verification;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import static org.netbeans.modules.php.editor.PredefinedSymbols.Attributes.DEPRECATED;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.impl.VariousUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Attributed;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Attribute "Deprecated" (#[\Deprecated]) can target function, method, and
 * class constant(enum case) in PHP 8.4.
 *
 * #[\Deprecated] cannot target type and field. The follwing cases are errors.
 * <pre>
 * #[\Deprecated] // error
 * class DeprecatedClass {
 *     #[\Deprecated] // error
 *     public string $deprecatedField = "deprecated";
 * }
 * #[Deprecated] // error
 * trait DeprecatedTrait {}
 * </pre>
 *
 * PHP Fatal error: Attribute "Deprecated" cannot target class (allowed targets:
 * function, method, class constant)
 */
public class IncorrectDeprecatedAttributeHintError extends HintErrorRule {

    private FileObject fileObject;

    @NbBundle.Messages("IncorrectDeprecatedAttributeHintError.displayName=Incorrect Deprecated Attribute")
    @Override
    public String getDisplayName() {
        return Bundle.IncorrectDeprecatedAttributeHintError_displayName();
    }

    @NbBundle.Messages({
        "# {0} - attribute name",
        "# {1} - type",
        "IncorrectDeprecatedAttributeHintError.incorrect.deprecated.attribute.desc=Attribute \"{0}\" cannot target {1}.",
        "IncorrectDeprecatedAttributeHintError.type=type",
        "IncorrectDeprecatedAttributeHintError.field=field",
    })
    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null && appliesTo(getPhpVersion())) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor(fileScope);
            phpParseResult.getProgram().accept(checkVisitor);
            addIncorrectAttributeHint(
                    checkVisitor.getIncorrectTypeAttributes(),
                    Bundle.IncorrectDeprecatedAttributeHintError_incorrect_deprecated_attribute_desc(
                            DEPRECATED.getName(),
                            Bundle.IncorrectDeprecatedAttributeHintError_type()
                    ),
                    hints
            );
            addIncorrectAttributeHint(
                    checkVisitor.getIncorrectFieldAttributes(),
                    Bundle.IncorrectDeprecatedAttributeHintError_incorrect_deprecated_attribute_desc(
                            DEPRECATED.getName(),
                            Bundle.IncorrectDeprecatedAttributeHintError_field()
                    ),
                    hints
            );
        }
    }

    protected PhpVersion getPhpVersion() {
        return CodeUtils.getPhpVersion(fileObject);
    }

    private boolean appliesTo(PhpVersion phpVersion) {
        return phpVersion.compareTo(PhpVersion.PHP_84) >= 0;
    }

    private void addIncorrectAttributeHint(List<AttributeDeclaration> attributeDeclarations, String description, List<Hint> hints) {
        for (AttributeDeclaration attributeDeclaration : attributeDeclarations) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addHint(attributeDeclaration, description, hints);
        }
    }

    private void addHint(ASTNode node, String description, List<Hint> hints) {
        addHint(node, description, hints, Collections.emptyList());
    }

    private void addHint(ASTNode node, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(
                this,
                description,
                fileObject,
                new OffsetRange(node.getStartOffset(), node.getEndOffset()),
                fixes,
                500
        ));
    }

    //~ Inner class
    private static final class CheckVisitor extends DefaultVisitor {

        private final FileScope fileScope;
        private final List<AttributeDeclaration> incorrectTypeAttributes = new ArrayList<>();
        private final List<AttributeDeclaration> incorrectFieldAttributes = new ArrayList<>();

        public CheckVisitor(FileScope fileScope) {
            this.fileScope = fileScope;
        }

        public List<AttributeDeclaration> getIncorrectTypeAttributes() {
            return Collections.unmodifiableList(incorrectTypeAttributes);
        }

        public List<AttributeDeclaration> getIncorrectFieldAttributes() {
            return Collections.unmodifiableList(incorrectFieldAttributes);
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isAttributed()) {
                checkAttributes(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isAttributed()) {
                checkAttributes(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isAttributed()) {
                checkAttributes(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(EnumDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isAttributed()) {
                checkAttributes(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isAnonymous() && node.isAttributed()) {
                checkAttributes(node);
            }
            super.visit(node);
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (node.isAttributed()) {
                checkAttributes(node);
            }
            super.visit(node);
        }

        private void checkAttributes(ASTNode node) {
            if (!(node instanceof Attributed)) {
                return;
            }
            for (Attribute attribute : ((Attributed) node).getAttributes()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                checkAttribute(attribute, node);
            }
        }

        private void checkAttribute(Attribute attribute, ASTNode node) {
            for (AttributeDeclaration attributeDeclaration : attribute.getAttributeDeclarations()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                String attributeName = CodeUtils.extractQualifiedName(attributeDeclaration.getAttributeName());
                if (!VariousUtils.isPredefinedAttributeName(DEPRECATED, attributeName, fileScope, attributeDeclaration.getStartOffset())) {
                    continue;
                }
                addIncorrectAttribute(node, attributeDeclaration);
                break;
            }
        }

        private void addIncorrectAttribute(ASTNode node, AttributeDeclaration attributeDeclaration) {
            if (CodeUtils.isTypeDeclaration(node)) {
                incorrectTypeAttributes.add(attributeDeclaration);
            } else if (node instanceof FieldsDeclaration) {
                incorrectFieldAttributes.add(attributeDeclaration);
            } else {
                assert false : "TypeDeclaration or FieldsDeclaration is expected, but got " + node.getClass().getName(); // NOI18N
            }
        }
    }
}
