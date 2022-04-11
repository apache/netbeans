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

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * Handle incorrect declaration of Enumeration.
 */
public class IncorrectEnumHintError extends HintErrorRule {

    private FileObject fileObject;

    @Override
    @NbBundle.Messages("IncorrectEnumHintError.displayName=Incorrect Declaration of Enumeration")
    public String getDisplayName() {
        return Bundle.IncorrectEnumHintError_displayName();
    }

    @Override
    @NbBundle.Messages({
        "IncorrectEnumHintError.incorrectEnumCases=\"case\" can only be in enums",
        "IncorrectEnumHintError.incorrectEnumBackingTypes=Backing type must be \"string\" or \"int\"",
        "IncorrectEnumHintError.incorrectEnumProperties=Enums cannot have properties",
    })
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            for (CaseDeclaration incorrectEnumCase : checkVisitor.getIncorrectEnumCases()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(incorrectEnumCase, Bundle.IncorrectEnumHintError_incorrectEnumCases(), hints);
            }
            for (Expression incorrectBackingTypes : checkVisitor.getIncorrectBackingTypes()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(incorrectBackingTypes, Bundle.IncorrectEnumHintError_incorrectEnumBackingTypes(), hints);
            }
            for (FieldsDeclaration incorrectEnumProperty : checkVisitor.getIncorrectEnumProperties()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                addHint(incorrectEnumProperty, Bundle.IncorrectEnumHintError_incorrectEnumProperties(), hints);
            }
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

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final Set<CaseDeclaration> incorrectEnumCases = new HashSet<>();
        private final Set<Expression> incorrectBackingTypes = new HashSet<>();
        private final Set<FieldsDeclaration> incorrectEnumProperties = new HashSet<>();

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getAttributes());
            scan(node.getName());
            scan(node.getSuperClass());
            scan(node.getInterfaes());
            checkEnumCases(node.getBody().getStatements());
        }

        private void checkEnumCases(List<Statement> statements) {
            for (Statement statement : statements) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (statement instanceof CaseDeclaration) {
                    incorrectEnumCases.add((CaseDeclaration) statement);
                }
                scan(statement);
            }
        }

        @Override
        public void visit(EnumDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            scan(node.getAttributes());
            scan(node.getName());
            scan(node.getInterfaes());
            Expression backingType = node.getBackingType();
            if (backingType != null) {
                String name = CodeUtils.extractQualifiedName(backingType);
                // only string or int
                if (!Type.STRING.equals(name) && !Type.INT.equals(name)) {
                    incorrectBackingTypes.add(backingType);
                }
            }
            scan(node.getBackingType());
            checkFields(node.getBody().getStatements());
        }

        private void checkFields(List<Statement> statements) {
            for (Statement statement : statements) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (statement instanceof FieldsDeclaration) {
                    incorrectEnumProperties.add((FieldsDeclaration) statement);
                }
                scan(statement);
            }
        }

        public Set<CaseDeclaration> getIncorrectEnumCases() {
            return Collections.unmodifiableSet(incorrectEnumCases);
        }

        public Set<Expression> getIncorrectBackingTypes() {
            return Collections.unmodifiableSet(incorrectBackingTypes);
        }

        public Set<FieldsDeclaration> getIncorrectEnumProperties() {
            return Collections.unmodifiableSet(incorrectEnumProperties);
        }

    }
}
