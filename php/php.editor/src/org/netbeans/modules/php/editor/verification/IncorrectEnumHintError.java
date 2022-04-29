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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelElement;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.impl.Type;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.ASTNode;
import org.netbeans.modules.php.editor.parser.astnodes.CaseDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.EnumDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Statement;
import org.netbeans.modules.php.editor.parser.astnodes.UseTraitStatement;
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
        "IncorrectEnumHintError.incorrectEnumProperties=Enum cannot have properties",
        "# {0} - the trait name",
        "IncorrectEnumHintError.incorrectEnumPropertiesWithTrait=Enum cannot have properties, but \"{0}\" has properties",
        "IncorrectEnumHintError.incorrectEnumConstructor=Enum cannot have a constructor",
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

            // incorrect property with trait
            Collection<? extends EnumScope> declaredEnums = ModelUtils.getDeclaredEnums(fileScope);
            for (EnumScope declaredEnum : declaredEnums) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                checkTraits(declaredEnum.getTraits(), declaredEnum, hints, checkVisitor.getUseTraits());
                checkConstructor(declaredEnum.getDeclaredMethods(), hints);
            }
        }
    }

    private void checkTraits(Collection<? extends TraitScope> traits, EnumScope enumScope, List<Hint> hints, Map<EnumDeclaration, UseTraitStatement> useTraits) {
        for (TraitScope trait : traits) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkTraits(trait.getTraits(), enumScope, hints, useTraits);
            Collection<? extends FieldElement> declaredFields = trait.getDeclaredFields();
            if (!declaredFields.isEmpty()) {
                UseTraitStatement useTraitStatement = null;
                for (Map.Entry<EnumDeclaration, UseTraitStatement> entry : useTraits.entrySet()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    if (entry.getKey().getName().getStartOffset() == enumScope.getNameRange().getStart()) {
                        useTraitStatement = entry.getValue();
                        break;
                    }

                }
                addHint(useTraitStatement, Bundle.IncorrectEnumHintError_incorrectEnumPropertiesWithTrait(trait.getName()), hints);
            }
        }
    }

    private void checkConstructor(Collection<? extends MethodScope> methods, List<Hint> hints) {
        for (MethodScope method : methods) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (method.isConstructor()) {
                addHint(method, Bundle.IncorrectEnumHintError_incorrectEnumConstructor(), hints);
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

    private void addHint(ModelElement element, String description, List<Hint> hints) {
        addHint(element, description, hints, Collections.emptyList());
    }

    private void addHint(ModelElement element, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(
                this,
                description,
                fileObject,
                element.getNameRange(),
                fixes,
                500
        ));
    }

    //~ Inner classes
    private static final class CheckVisitor extends DefaultVisitor {

        private final Set<CaseDeclaration> incorrectEnumCases = new HashSet<>();
        private final Set<Expression> incorrectBackingTypes = new HashSet<>();
        private final Set<FieldsDeclaration> incorrectEnumProperties = new HashSet<>();
        private final Map<EnumDeclaration, UseTraitStatement> useTraits = new HashMap<>();

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
            checkStatements(node.getBody().getStatements(), node);
        }

        private void checkStatements(List<Statement> statements, EnumDeclaration node) {
            for (Statement statement : statements) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (statement instanceof FieldsDeclaration) {
                    incorrectEnumProperties.add((FieldsDeclaration) statement);
                } else if (statement instanceof UseTraitStatement) {
                    useTraits.put(node, (UseTraitStatement) statement);
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

        public Map<EnumDeclaration, UseTraitStatement> getUseTraits() {
            return Collections.unmodifiableMap(useTraits);
        }

    }
}
