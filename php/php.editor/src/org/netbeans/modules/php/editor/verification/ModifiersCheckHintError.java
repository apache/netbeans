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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Attribute;
import org.netbeans.modules.php.editor.parser.astnodes.AttributeDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.BodyDeclaration.Modifier;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ModifiersCheckHintError extends HintErrorRule {
    private List<Hint> hints;
    private FileObject fileObject;
    private BaseDocument doc;
    private boolean currentClassHasAbstractMethod = false;

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() == null) {
            return;
        }
        this.hints = hints;
        this.doc = context.doc;
        FileScope fileScope = context.fileScope;
        fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
        if (fileScope != null && fileObject != null) {
            Collection<? extends ClassScope> declaredClasses = ModelUtils.getDeclaredClasses(fileScope);
            for (ClassScope classScope : declaredClasses) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processClassScope(classScope);
            }
            for (TraitScope traitScope : ModelUtils.getDeclaredTraits(fileScope)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processTraitScope(traitScope);
            }
            Collection<? extends InterfaceScope> declaredInterfaces = ModelUtils.getDeclaredInterfaces(fileScope);
            for (InterfaceScope interfaceScope : declaredInterfaces) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processInterfaceScope(interfaceScope);
            }
            CheckVisitor checkVisitor = new CheckVisitor();
            phpParseResult.getProgram().accept(checkVisitor);
            checkReadonlyFieldDeclarationsWithDefaultValue(hints, checkVisitor.getReadonlyFieldDeclarationsWithDefaultValue());
            checkDuplicatedClassModifiers(hints, checkVisitor.getDuplicatedClassModifiers());
            checkInvalidReadonlyClassAttributes(hints, checkVisitor.getInvalidReadonlyClassAttributes());
        }
    }

    @NbBundle.Messages({
        "# {0} - field name",
        "ModifiersCheckHintError.realdonlyFieldWithDefaultValue=Readonly property \"{0}\" can''t have default value"
    })
    private void checkReadonlyFieldDeclarationsWithDefaultValue(List<Hint> hints, List<SingleFieldDeclaration> fields) {
        for (SingleFieldDeclaration fieldDeclaration : fields) {
            // e.g. public readonly int $prop = 1;
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            Expression value = fieldDeclaration.getName();
            if (value != null) {
                addHint(
                        CodeUtils.getOffsetRagne(value),
                        Bundle.ModifiersCheckHintError_realdonlyFieldWithDefaultValue(fieldDeclaration.getName()),
                        hints,
                        Collections.emptyList()
                );
            }
        }
    }

    @Messages({
        "# {0} - modifier",
        "ModifiersCheckHintError.duplicatedClassModifiers=\"{0}\" is duplicated"
    })
    private void checkDuplicatedClassModifiers(List<Hint> hints, List<Map.Entry<ClassDeclaration.Modifier, Set<OffsetRange>>> modifiers) {
        for (Map.Entry<ClassDeclaration.Modifier, Set<OffsetRange>> modifier : modifiers) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            OffsetRange lastPosition = null;
            for (OffsetRange offsetRange : modifier.getValue()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (lastPosition == null) {
                    lastPosition = offsetRange;
                    continue;
                }
                if (lastPosition.compareTo(offsetRange) <= 0) {
                    lastPosition = offsetRange;
                }
            }
            assert lastPosition != null;
            addHint(
                    lastPosition,
                    Bundle.ModifiersCheckHintError_duplicatedClassModifiers(modifier.getKey().name().toLowerCase()),
                    hints,
                    Collections.<HintFix>singletonList(new RemoveModifierFix(doc, modifier.getKey().toString(), lastPosition.getStart(), lastPosition))
            );
        }
    }

    @Messages({
        "# {0} - attribute name",
        "# {1} - readonly class name",
        "ModifiersCheckHintError.invalidReadonlyClassAttributes=Cannot apply \"#[{0}]\" to readonly class \"{1}\""
    })
    private void checkInvalidReadonlyClassAttributes(List<Hint> hints, List<Pair<ClassDeclaration, AttributeDeclaration>> invalidReadonlyClassAttributes) {
        for (Pair<ClassDeclaration, AttributeDeclaration> attribute : invalidReadonlyClassAttributes) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            addHint(
                    CodeUtils.getOffsetRagne(attribute.second()),
                    Bundle.ModifiersCheckHintError_invalidReadonlyClassAttributes(CodeUtils.extractQualifiedName(attribute.second().getAttributeName()), attribute.first().getName().getName()),
                    hints,
                    Collections.emptyList()
            );
        }
    }

    private void addHint(OffsetRange offsetRange, String description, List<Hint> hints, List<HintFix> fixes) {
        hints.add(new Hint(
                this,
                description,
                fileObject,
                offsetRange,
                fixes,
                500
        ));
    }

    @Override
    @Messages("ModifiersCheckHintDispName=Modifiers Checker")
    public String getDisplayName() {
        return Bundle.ModifiersCheckHintDispName();
    }

    private void processClassScope(ClassScope classScope) {
        processClassModifiers(classScope);
        Collection<? extends FieldElement> declaredFields = classScope.getDeclaredFields();
        for (FieldElement fieldElement : declaredFields) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processFieldElement(fieldElement, classScope.isReadonly());
        }
        Collection<? extends MethodScope> declaredMethods = classScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processMethodScope(methodScope);
        }
        if (currentClassHasAbstractMethod) {
            processPossibleAbstractClass(classScope);
        }
        currentClassHasAbstractMethod = false;
    }

    private void processTraitScope(TraitScope traitScope) {
        Collection<? extends FieldElement> declaredFields = traitScope.getDeclaredFields();
        for (FieldElement fieldElement : declaredFields) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processFieldElement(fieldElement);
        }
        Collection<? extends MethodScope> declaredMethods = traitScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processMethodScope(methodScope);
        }
    }

    @Messages({
        "InvalidFinalModifierWithAbstractModifier=Cannot use \"final\" modifier with \"abstract\" modifier",
        "# {0} - class name",
        "# {1} - super class name",
        "InvalidClassExtendsFinalClass=Class \"{0}\" cannot extend final class \"{1}\""
    })
    private void processClassModifiers(ClassScope classScope) {
        if (classScope.isAbstract() && classScope.isFinal()) {
            // abstract final class Example {}
            List<HintFix> fixes = new ArrayList<>();
            fixes.add(new RemoveModifierFix(doc, PhpModifiers.ABSTRACT_MODIFIER, classScope.getOffset()));
            fixes.add(new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, classScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidFinalModifierWithAbstractModifier(), classScope.getNameRange(), fixes));
        }
        for (ClassScope superClass : classScope.getSuperClasses()) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (superClass.isFinal()) {
                // final class FinalClass {}
                // class ChildClass extends FinalClass {}
                hints.add(new SimpleHint(Bundle.InvalidClassExtendsFinalClass(classScope.getName(), superClass.getName()),
                        classScope.getNameRange(), Collections.emptyList()));
                break;
            }
        }
        processReadonlyClass(classScope);
    }

    @Messages({
        "# {0} - class name",
        "# {1} - super class name",
        "InvalidClassExtendsReadonlyClass=Non-readonly class \"{0}\" cannot extend readonly class \"{1}\"",
        "# {0} - class name",
        "# {1} - super class name",
        "InvalidReadonlyClassExtendsNonReadonlyClass=Readonly class \"{0}\" cannot extends non-readonly class \"{1}\"",
    })
    private void processReadonlyClass(ClassScope classScope) {
        if (classScope.isReadonly()) {
            for (ClassScope superClass : classScope.getSuperClasses()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                List<HintFix> fixes;
                if (!superClass.isReadonly()) {
                    // class ParentClass{}
                    // readonly class ChildClass extends ParentClass {}
                    fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, PhpModifiers.READONLY_MODIFIER, classScope.getOffset()));
                    hints.add(new SimpleHint(Bundle.InvalidReadonlyClassExtendsNonReadonlyClass(classScope.getName(), superClass.getName()),
                            classScope.getNameRange(), fixes));
                    break;
                }
            }
        } else {
            // readonly class ParentClass{}
            // class ChildClass extends ParentClass {}
            for (ClassScope superClass : classScope.getSuperClasses()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (superClass.isReadonly()) {
                    List<HintFix> fixes = Collections.<HintFix>singletonList(new AddModifierFix(doc, PhpModifiers.READONLY_MODIFIER, classScope.getOffset()));
                    hints.add(new SimpleHint(Bundle.InvalidClassExtendsReadonlyClass(classScope.getName(), superClass.getName()), classScope.getNameRange(), fixes));
                    break;
                }
            }

        }
    }

    @Messages({
        "# {0} - Field name",
        "# {1} - Modifier name",
        "InvalidField=Field \"{0}\" can not be declared {1}",
        "# {0} - Field name",
        "InvalidReadonlyProperty=Readonly property \"{0}\" must have type",
        "# {0} - Field name",
        "InvalidStaticReadonlyProperty=Static property \"{0}\" cannot be readonly"
    })
    private void processFieldElement(FieldElement fieldElement) {
        processFieldElement(fieldElement, false);
    }

    private void processFieldElement(FieldElement fieldElement, boolean isReadonlyClass) {
        PhpModifiers phpModifiers = fieldElement.getPhpModifiers();
        List<HintFix> fixes;
        String invalidModifier;
        if (phpModifiers.isAbstract()) {
            invalidModifier = PhpModifiers.ABSTRACT_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, fieldElement.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isFinal()) {
            invalidModifier = PhpModifiers.FINAL_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, fieldElement.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isStatic() && (phpModifiers.isReadonly() || isReadonlyClass)) {
            // e.g. public static readonly $prop;
            hints.add(new SimpleHint(Bundle.InvalidStaticReadonlyProperty(fieldElement.getName()), fieldElement.getNameRange(), Collections.emptyList()));
        } else if ((phpModifiers.isReadonly() || isReadonlyClass)
                && fieldElement.getDefaultTypeNames().isEmpty()) {
            // e.g. public readonly $prop;
            hints.add(new SimpleHint(Bundle.InvalidReadonlyProperty(fieldElement.getName()), fieldElement.getNameRange(), Collections.emptyList()));
        }
    }

    @Messages({
        "# {0} - Method name",
        "AbstractFinalMethod=Method \"{0}\" can not be declared abstract and final",
        "# {0} - Method name",
        "AbstractWithBlockMethod=Abstract method \"{0}\" can not contain body",
        "# {0} - Method name",
        "AbstractPrivateMethod=Abstract method \"{0}\" can not be declared private"
    })
    private void processMethodScope(MethodScope methodScope) {
        PhpModifiers phpModifiers = methodScope.getPhpModifiers();
        List<HintFix> fixes;
        boolean isTrait = methodScope.getInScope() instanceof TraitScope;
        if (phpModifiers.isAbstract() && phpModifiers.isFinal()) {
            fixes = new ArrayList<>();
            fixes.add(new RemoveModifierFix(doc, "abstract", methodScope.getOffset())); //NOI18N
            fixes.add(new RemoveModifierFix(doc, "final", methodScope.getOffset())); //NOI18N
            hints.add(new SimpleHint(Bundle.AbstractFinalMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && methodScope.getBlockRange() != null) {
            fixes = Collections.<HintFix>singletonList(new RemoveBodyFix(doc, methodScope));
            hints.add(new SimpleHint(Bundle.AbstractWithBlockMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && phpModifiers.isPrivate() && !isTrait) {
            // the following is valid in a trait scope
            // abstract private function abstractMethod();
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, "private", methodScope.getOffset())); //NOI18N
            hints.add(new SimpleHint(Bundle.AbstractPrivateMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        }
        if (phpModifiers.isAbstract() && !isTrait) {
            currentClassHasAbstractMethod = true;
        }
    }

    private void processInterfaceScope(InterfaceScope interfaceScope) {
        Collection<? extends MethodScope> declaredMethods = interfaceScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processInterfaceMethodScope(methodScope);
        }

        // PHP7.1
        Collection<? extends ClassConstantElement> declaredConstants = interfaceScope.getDeclaredConstants();
        for (ClassConstantElement declaredConstant : declaredConstants) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processInterfaceClassConstant(declaredConstant);
        }
    }

    @Messages({
        "# {0} - Method name",
        "# {1} - Modifier name",
        "InvalidIfaceMethod=Interface method \"{0}\" can not be declared {1}",
        "# {0} - Method name",
        "IfaceMethodWithBlock=Interface method \"{0}\" can not contain body"
    })
    private void processInterfaceMethodScope(MethodScope methodScope) {
        PhpModifiers phpModifiers = methodScope.getPhpModifiers();
        List<HintFix> fixes;
        String invalidModifier;
        if (phpModifiers.isPrivate()) {
            invalidModifier = "private"; //NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isProtected()) {
            invalidModifier = "protected"; //NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isFinal()) {
            invalidModifier = "final"; //NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (methodScope.getBlockRange() != null && methodScope.getBlockRange().getLength() != 1) {
            fixes = Collections.<HintFix>singletonList(new RemoveBodyFix(doc, methodScope));
            hints.add(new SimpleHint(Bundle.IfaceMethodWithBlock(methodScope.getName()), methodScope.getNameRange(), fixes));
        }
    }

    @Messages({
        "# {0} - Constant name",
        "# {1} - Modifier name",
        "InvalidIfaceConstant=Interface const \"{0}\" can not be declared {1}",
    })
    private void processInterfaceClassConstant(ClassConstantElement classConstant) {
        PhpModifiers phpModifiers = classConstant.getPhpModifiers();
        List<HintFix> fixes;
        String invalidModifier;
        if (phpModifiers.isPrivate()) {
            invalidModifier = "private"; // NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, classConstant.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceConstant(classConstant.getName(), invalidModifier), classConstant.getNameRange(), fixes));
        } else if (phpModifiers.isProtected()) {
            invalidModifier = "protected"; // NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, classConstant.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceConstant(classConstant.getName(), invalidModifier), classConstant.getNameRange(), fixes));
        }
    }

    @Messages({
        "# {0} - Class name",
        "PossibleAbstractClass=Class \"{0}\" contains abstract methods and must be declared abstract",
        "# {0} - Class name",
        "FinalPossibleAbstractClass=Class \"{0}\" contains abstract methods and can not be declared final"
    })
    private void processPossibleAbstractClass(ClassScope classScope) {
        List<HintFix> fixes;
        if (!classScope.isAbstract()) {
            fixes = Collections.<HintFix>singletonList(new AddModifierFix(doc, "abstract", classScope.getOffset())); //NOI18N
            hints.add(new SimpleHint(Bundle.PossibleAbstractClass(classScope.getName()), classScope.getNameRange(), fixes));
        }
        if (classScope.isFinal()) {
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, "final", classScope.getOffset())); //NOI18N
            hints.add(new SimpleHint(Bundle.FinalPossibleAbstractClass(classScope.getName()), classScope.getNameRange(), fixes));
        }
    }

    private class SimpleHint extends Hint {

        public SimpleHint(String description, OffsetRange range, List<HintFix> fixes) {
            super(ModifiersCheckHintError.this, description, fileObject, range, fixes, 500);
        }

        public SimpleHint(String description, OffsetRange range) {
            this(description, range, null);
        }

    }

    private abstract class AbstractHintFix implements HintFix {
        protected final BaseDocument doc;

        public AbstractHintFix(BaseDocument doc) {
            this.doc = doc;
        }

        @Override
        public boolean isSafe() {
            return true;
        }

        @Override
        public boolean isInteractive() {
            return false;
        }

    }

    private class RemoveBodyFix extends AbstractHintFix {
        private final MethodScope methodScope;

        public RemoveBodyFix(BaseDocument doc, MethodScope methodScope) {
            super(doc);
            this.methodScope = methodScope;
        }

        @Override
        @Messages({
            "# {0} - Method name",
            "RemoveBodyFixDesc=Remove body of the method: {0}"
        })
        public String getDescription() {
            return Bundle.RemoveBodyFixDesc(methodScope.getName());
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(methodScope.getBlockRange().getStart(), methodScope.getBlockRange().getLength(), ";", true, 0); //NOI18N
            edits.apply();
        }

    }

    private class RemoveModifierFix extends AbstractHintFix {

        private final String modifier;
        private final int elementOffset;
        private final OffsetRange offsetRange;

        public RemoveModifierFix(BaseDocument doc, String modifier, int elementOffset) {
            this(doc, modifier, elementOffset, null);
        }

        public RemoveModifierFix(BaseDocument doc, String modifier, int elementOffset, OffsetRange offsetRange) {
            super(doc);
            this.offsetRange = offsetRange;
            this.modifier = modifier;
            this.elementOffset = elementOffset;
        }

        @Override
        @Messages({
            "# {0} - Modifier name",
            "RemoveModifierFixDesc=Remove modifier: {0}"
        })
        public String getDescription() {
            return Bundle.RemoveModifierFixDesc(modifier);
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            if (offsetRange != null) {
                edits.replace(offsetRange.getStart(), offsetRange.getLength() + 1, "", true, 0); // NOI18N +1:whitespace
            } else {
                int startOffset = getStartOffset(doc, elementOffset);
                int length = elementOffset - startOffset;
                String replaceText = doc.getText(startOffset, length).replace(modifier, "").replaceAll("^\\s+", ""); //NOI18N
                edits.replace(startOffset, length, replaceText, true, 0);
            }
            edits.apply();
        }

    }

    private class AddModifierFix extends AbstractHintFix {
        private final String modifier;
        private final int elementOffset;

        public AddModifierFix(BaseDocument doc, String modifier, int elementOffset) {
            super(doc);
            this.modifier = modifier;
            this.elementOffset = elementOffset;
        }

        @Override
        @Messages({
            "# {0} - Modifier name",
            "AddModifierFixDesc=Add modifier: {0}"
        })
        public String getDescription() {
            return Bundle.AddModifierFixDesc(modifier);
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            int startOffset = getStartOffset(doc, elementOffset);
            int length = elementOffset - startOffset;
            String replaceText = modifier + " " + doc.getText(startOffset, length); //NOI18N
            edits.replace(startOffset, length, replaceText, true, 0);
            edits.apply();
        }

    }

    private static int getStartOffset(final BaseDocument doc, final int elementOffset) {
        int retval = 0;
        doc.readLock();
        try {
            TokenSequence<? extends PHPTokenId> ts = LexUtilities.getPHPTokenSequence(doc, elementOffset);
            if (ts != null) {
                ts.move(elementOffset);
                TokenId lastTokenId = null;
                while (ts.movePrevious()) {
                    Token t = ts.token();
                    if (t.id() != PHPTokenId.PHP_PUBLIC && t.id() != PHPTokenId.PHP_PROTECTED && t.id() != PHPTokenId.PHP_PRIVATE
                            && t.id() != PHPTokenId.PHP_STATIC && t.id() != PHPTokenId.PHP_FINAL && t.id() != PHPTokenId.PHP_ABSTRACT
                            && t.id() != PHPTokenId.PHP_FUNCTION && t.id() != PHPTokenId.WHITESPACE && t.id() != PHPTokenId.PHP_CLASS
                            && t.id() != PHPTokenId.PHP_CONST && t.id() != PHPTokenId.PHP_READONLY) {
                        ts.moveNext();
                        if (lastTokenId == PHPTokenId.WHITESPACE) {
                            ts.moveNext();
                        }
                        retval = ts.offset();
                        break;
                    }
                    lastTokenId = t.id();
                }
            }
        } finally {
            doc.readUnlock();
        }
        return retval;
    }

    private static final class CheckVisitor extends DefaultVisitor {

        private final List<SingleFieldDeclaration> readonlyFieldDeclarationsWithDefaultValue = new ArrayList<>();
        private final List<Pair<ClassDeclaration, AttributeDeclaration>> invalidReadonlyClassAttributes = new ArrayList<>();
        private final List<Map.Entry<ClassDeclaration.Modifier, Set<OffsetRange>>> duplicatedClassModifiers = new ArrayList<>();

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            checkDuplicatedClassModifiers(node);
            if (node.getModifiers().containsKey(ClassDeclaration.Modifier.READONLY)) {
                checkInvalidReadonlyClassAttributes(node);
            }
            super.visit(node);
        }

        private void checkDuplicatedClassModifiers(ClassDeclaration node) {
            for (Map.Entry<ClassDeclaration.Modifier, Set<OffsetRange>> entry : node.getModifiers().entrySet()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                if (entry.getValue().size() > 1) {
                    duplicatedClassModifiers.add(entry);
                }
            }
        }

        private void checkInvalidReadonlyClassAttributes(ClassDeclaration node) {
            // #[AllowDynamicProperties]
            for (Attribute attribute : node.getAttributes()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                for (AttributeDeclaration attributeDeclaration : attribute.getAttributeDeclarations()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    String attributeName = CodeUtils.extractQualifiedName(attributeDeclaration.getAttributeName());
                    if ("AllowDynamicProperties".equals(attributeName)) { // NOI18N
                        invalidReadonlyClassAttributes.add(Pair.of(node, attributeDeclaration));
                        break;
                    }
                }
            }
        }

        @Override
        public void visit(FieldsDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (Modifier.isReadonly(node.getModifier())) {
                // e.g. public readonly int $prop = 1;
                for (SingleFieldDeclaration field : node.getFields()) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    if (field.getValue() != null) {
                        readonlyFieldDeclarationsWithDefaultValue.add(field);
                    }
                }
            }
            super.visit(node);
        }

        public List<SingleFieldDeclaration> getReadonlyFieldDeclarationsWithDefaultValue() {
            return Collections.unmodifiableList(readonlyFieldDeclarationsWithDefaultValue);
        }

        public List<Map.Entry<ClassDeclaration.Modifier, Set<OffsetRange>>> getDuplicatedClassModifiers() {
            return Collections.unmodifiableList(duplicatedClassModifiers);
        }

        public List<Pair<ClassDeclaration, AttributeDeclaration>> getInvalidReadonlyClassAttributes() {
            return Collections.unmodifiableList(invalidReadonlyClassAttributes);
        }

    }

}
