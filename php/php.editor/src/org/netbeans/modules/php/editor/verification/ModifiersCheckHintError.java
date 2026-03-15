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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenUtilities;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.PredefinedSymbols;
import org.netbeans.modules.php.editor.api.PhpModifiers;
import org.netbeans.modules.php.editor.lexer.LexUtilities;
import org.netbeans.modules.php.editor.lexer.PHPTokenId;
import org.netbeans.modules.php.editor.model.ClassConstantElement;
import org.netbeans.modules.php.editor.model.ClassScope;
import org.netbeans.modules.php.editor.model.EnumScope;
import org.netbeans.modules.php.editor.model.FieldElement;
import org.netbeans.modules.php.editor.model.FileScope;
import org.netbeans.modules.php.editor.model.InterfaceScope;
import org.netbeans.modules.php.editor.model.MethodScope;
import org.netbeans.modules.php.editor.model.ModelUtils;
import org.netbeans.modules.php.editor.model.PropertyHookScope;
import org.netbeans.modules.php.editor.model.Scope;
import org.netbeans.modules.php.editor.model.TraitScope;
import org.netbeans.modules.php.editor.model.TypeScope;
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
    private TokenSequence<PHPTokenId> ts;
    private boolean currentClassHasAbstractMember = false;
    private static final Set<PHPTokenId> MODIFIERS = Set.of(
            PHPTokenId.PHP_PUBLIC,
            PHPTokenId.PHP_PROTECTED,
            PHPTokenId.PHP_PRIVATE,
            PHPTokenId.PHP_PUBLIC_SET,
            PHPTokenId.PHP_PROTECTED_SET,
            PHPTokenId.PHP_PRIVATE_SET,
            PHPTokenId.PHP_STATIC,
            PHPTokenId.PHP_READONLY,
            PHPTokenId.PHP_ABSTRACT,
            PHPTokenId.PHP_FINAL
    );

    protected PhpVersion getPhpVersion() {
        return CodeUtils.getPhpVersion(fileObject);
    }

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
        TokenHierarchy<?> tokenHierarchy = phpParseResult.getSnapshot().getTokenHierarchy();
        ts = LexUtilities.getPHPTokenSequence(tokenHierarchy, 0);
        if (fileScope != null && fileObject != null && ts != null) {
            ts.moveNext();
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
            for (EnumScope enumScope : ModelUtils.getDeclaredEnums(fileScope)) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processEnumScope(enumScope);
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
        processClassConstants(classScope.getDeclaredConstants());
        processFields(classScope.getDeclaredFields(), classScope.isReadonly());
        processMethods(classScope.getDeclaredMethods());
        if (currentClassHasAbstractMember) {
            processPossibleAbstractClass(classScope);
        }
        currentClassHasAbstractMember = false;
    }

    private void processEnumScope(EnumScope enumScope) {
        processClassConstants(enumScope.getDeclaredConstants());
        processMethods(enumScope.getDeclaredMethods());
    }

    private void processTraitScope(TraitScope traitScope) {
        if (getPhpVersion().hasConstantsInTraits()) {
            processClassConstants(traitScope.getDeclaredConstants());
        }
        processFields(traitScope.getDeclaredFields());
        processMethods(traitScope.getDeclaredMethods());
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

    private void processClassConstants(Collection<? extends ClassConstantElement> declaredConstants) {
        for (ClassConstantElement declaredConstant : declaredConstants) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processClassConstantElement(declaredConstant);
        }
    }

    @Messages({
        "# {0} - constant name",
        "# {1} - Modifier name",
        "InvalidConst=Constant \"{0}\" cannot be declared {1}",
        "InvalidPrivateConst=Private type constants cannot be final.",
    })
    private void processClassConstantElement(ClassConstantElement constantElement) {
        PhpModifiers phpModifiers = constantElement.getPhpModifiers();
        List<HintFix> fixes;
        String invalidModifier;
        boolean isFirstConst = isFirstConstant(constantElement.getOffset());
        int typeStart = getTypeStart(constantElement.getOffset(), constantElement.getDeclaredType());
        if (phpModifiers.isAbstract()) {
            // abstract const
            invalidModifier = PhpModifiers.ABSTRACT_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidConst(constantElement.getName(), invalidModifier), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (phpModifiers.isFinal() && !getPhpVersion().hasFinalConst()) {
            // final const as of PHP 8.1
            invalidModifier = PhpModifiers.FINAL_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidConst(constantElement.getName(), invalidModifier), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (phpModifiers.isFinal() && phpModifiers.isPrivate()) {
            // final private string const
            fixes = new ArrayList<>();
            fixes.add(new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, typeStart));
            fixes.add(new RemoveModifierFix(doc, PhpModifiers.VISIBILITY_PRIVATE, typeStart));
            addSimpleHint(Bundle.InvalidPrivateConst(), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (phpModifiers.isStatic()) {
            // e.g. public static const int CONSTANT = 1;
            invalidModifier = PhpModifiers.STATIC_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidConst(constantElement.getName(), invalidModifier), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (phpModifiers.isReadonly()) {
            // e.g. readonly const int CONSTANT = 1;
            invalidModifier = PhpModifiers.READONLY_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidConst(constantElement.getName(), invalidModifier), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (Modifier.isSetVisibilityModifier(phpModifiers.toFlags())) {
            // e.g. private(set) const CONSTANT = 1;
            invalidModifier = getSetVisibility(phpModifiers);
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidConst(constantElement.getName(), invalidModifier), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (hasMultipleAccessTypeModifiers(phpModifiers)) {
            // e.g. public private const;
            fixes = getMultipleAccessTypeModifiersFixes(phpModifiers, typeStart);
            addSimpleHint(Bundle.MultipleAccesTypeModifiers(), constantElement.getNameRange(), fixes, isFirstConst);
        } else if (hasMultipleSameModifier(typeStart)){
            // e.g. public public const CONSTANT = 1;
            String multipleSameModifier = getMultipleSameModifiers(ts, typeStart);
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, multipleSameModifier, typeStart));
            addSimpleHint(Bundle.InvalidMultipleModifiers(multipleSameModifier), constantElement.getNameRange(), fixes, isFirstConst);
        }
    }

    private void addSimpleHint(String description, OffsetRange range, List<HintFix> fixes, boolean isFirstElement) {
        hints.add(new SimpleHint(description, range, getAvailableFixes(fixes, isFirstElement)));
    }

    private boolean isFirstConstant(int startOffset) {
        boolean isFirst = true;
        int originalOffset = ts.offset();
        try {
            ts.move(startOffset);
            while (ts.movePrevious()) {
                if (ts.token().id() == PHPTokenId.PHP_CONST) {
                    break;
                } else if (ts.token().id() == PHPTokenId.PHP_TOKEN
                        && TokenUtilities.equals(ts.token().text(), ",")) { // NOI18N
                    isFirst = false;
                    break;
                }
            }
        } finally {
            ts.move(originalOffset);
            ts.moveNext();
        }
        return isFirst;
    }

    private List<HintFix> getAvailableFixes(List<HintFix> fixes, boolean isFirstElement) {
        return isFirstElement ? fixes : Collections.emptyList();
    }

    private void processFields(Collection<? extends FieldElement> declaredFields) {
        processFields(declaredFields, false);
    }

    private void processFields(Collection<? extends FieldElement> declaredFields, boolean isReadonly) {
        for (FieldElement fieldElement : declaredFields) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processFieldElement(fieldElement, isReadonly);
        }
    }

    @Messages({
        "# {0} - Field name",
        "# {1} - Modifier name",
        "InvalidField=Field \"{0}\" cannot be declared {1}",
        "# {0} - Field name",
        "InvalidReadonlyProperty=Readonly property \"{0}\" must have type",
        "# {0} - Field name",
        "InvalidEmptyTypeAsymmetricVisiblilityProperty=Asymmetric visibility property \"{0}\" must have type",
        "# {0} - Field name",
        "InvalidStaticReadonlyProperty=Static property \"{0}\" cannot be readonly",
        "# {0} - Field name",
        "InvalidStaticHookedProperty=Static property \"{0}\" cannot have property hooks",
        "InvalidStaticAsymmetricVisibilityProperty=Static property may not have asymmetric visibility",
        "# {0} - Visibility",
        "# {1} - Field name",
        "# {2} - Set visibility",
        "InvalidAsymmetricProperty=Visibility({0}) of property \"{1}\" must not be weaker than set visibility({2})",
        "InvalidFinalPrivateProperty=Private field cannot be final.",
        "InvalidAbstractPrivateProperty=Hooked property cannot be both private and abstract",
    })
    private void processFieldElement(FieldElement fieldElement, boolean isReadonlyClass) {
        PhpModifiers phpModifiers = fieldElement.getPhpModifiers();
        int typeStart = getTypeStart(fieldElement.getOffset() - 1, fieldElement.getDefaultType()); // -1:$
        if (phpModifiers.isAbstract() && !FieldElement.isHooked(fieldElement)) {
            String invalidModifier = PhpModifiers.ABSTRACT_MODIFIER;
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, invalidModifier, typeStart));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isFinal() && !getPhpVersion().hasFinalProperty()) {
            String invalidModifier = PhpModifiers.FINAL_MODIFIER;
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, invalidModifier, typeStart));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isFinal() && phpModifiers.isPrivate()) {
            // final private int $prop;
            List<HintFix> fixes = List.of(
                    new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, typeStart),
                    new RemoveModifierFix(doc, PhpModifiers.VISIBILITY_PRIVATE, typeStart)
            );
            hints.add(new SimpleHint(Bundle.InvalidFinalPrivateProperty(), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isStatic() && (phpModifiers.isReadonly() || isReadonlyClass)) {
            // e.g. public static readonly $prop;
            List<HintFix> fixes = new ArrayList<>();
            fixes.add(new RemoveModifierFix(doc, PhpModifiers.STATIC_MODIFIER, typeStart));
            if (phpModifiers.isReadonly()) {
                fixes.add(new RemoveModifierFix(doc, PhpModifiers.READONLY_MODIFIER, typeStart));
            }
            hints.add(new SimpleHint(Bundle.InvalidStaticReadonlyProperty(fieldElement.getName()), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isStatic() && FieldElement.isHooked(fieldElement)) {
            // public static $prop {get{}}
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, PhpModifiers.STATIC_MODIFIER, typeStart));
            hints.add(new SimpleHint(Bundle.InvalidStaticHookedProperty(fieldElement.getName()), fieldElement.getNameRange(), fixes));
        } else if ((phpModifiers.isReadonly() || isReadonlyClass)
                && FieldElement.isHooked(fieldElement)) {
            String invalidModifier = PhpModifiers.READONLY_MODIFIER;
            List<HintFix> fixes = isReadonlyClass ? List.of() : List.of(new RemoveModifierFix(doc, invalidModifier, typeStart));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
        } else if ((phpModifiers.isReadonly() || isReadonlyClass)
                && fieldElement.getDefaultTypeNames().isEmpty()) {
            // e.g. public readonly $prop;
            hints.add(new SimpleHint(Bundle.InvalidReadonlyProperty(fieldElement.getName()), fieldElement.getNameRange(), Collections.emptyList()));
        } else if (Modifier.isSetVisibilityModifier(phpModifiers.toFlags()) && fieldElement.getDefaultTypeNames().isEmpty()) {
            // e.g. public private(set) $prop;
            hints.add(new SimpleHint(Bundle.InvalidEmptyTypeAsymmetricVisiblilityProperty(fieldElement.getName()), fieldElement.getNameRange(), Collections.emptyList()));
        } else if (hasMultipleAccessTypeModifiers(phpModifiers)) {
            // e.g. public private $prop; public(set) private(set) $prop;
            List<HintFix> fixes = getMultipleAccessTypeModifiersFixes(phpModifiers, typeStart);
            hints.add(new SimpleHint(Bundle.MultipleAccesTypeModifiers(), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isStatic() && Modifier.isSetVisibilityModifier(phpModifiers.toFlags())) {
            // e.g. static private(set) $prop;
            String invalidModifier = getSetVisibility(phpModifiers);
            List<HintFix> fixes = List.of(
                    new RemoveModifierFix(doc, PhpModifiers.STATIC_MODIFIER, typeStart),
                    new RemoveModifierFix(doc, invalidModifier, typeStart)
            );
            hints.add(new SimpleHint(Bundle.InvalidStaticAsymmetricVisibilityProperty(), fieldElement.getNameRange(), fixes));
        } else if (isVisibilityWeakerThanSet(phpModifiers)) {
            // e.g. private public(set) $prop;
            String visibility = getVisibility(phpModifiers);
            String setVisibility = getSetVisibility(phpModifiers);
            hints.add(new SimpleHint(Bundle.InvalidAsymmetricProperty(visibility, fieldElement.getName(), setVisibility),
                    fieldElement.getNameRange(), List.of()));
        } else if (hasMultipleSameModifier(typeStart)) {
            String multipleSameModifier = getMultipleSameModifiers(ts, typeStart);
            // e.g. public public $prop;
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, multipleSameModifier, typeStart));
            hints.add(new SimpleHint(Bundle.InvalidMultipleModifiers(multipleSameModifier), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isPrivate() && phpModifiers.isAbstract() && FieldElement.isHooked(fieldElement)) {
            // e.g. private abstract $prop {get; set;}
            List<HintFix> fixes = List.of(
                    new RemoveModifierFix(doc, PhpModifiers.VISIBILITY_PRIVATE, typeStart),
                    new RemoveModifierFix(doc, PhpModifiers.ABSTRACT_MODIFIER, typeStart)
            );
            hints.add(new SimpleHint(Bundle.InvalidAbstractPrivateProperty(), fieldElement.getNameRange(), fixes));
        }
        Scope inScope = fieldElement.getInScope();
        if (phpModifiers.isAbstract() && FieldElement.isHooked(fieldElement) && isClass(inScope) && !isAnonymousClass(inScope)) {
            currentClassHasAbstractMember = true;
        }

        // property hooks
        if (fieldElement instanceof FieldElement.HookedFieldElement hookedFieldElement) {
            processPropertyHooks(hookedFieldElement.getPropertyHooks(), hookedFieldElement.getPhpModifiers().isAbstract());
        }
    }

    @Messages({
        "AbstractPropertyHookWithBlock=Abstract property hook must specify at least one abstract hook"
    })
    private void processPropertyHooks(Collection<? extends PropertyHookScope> propertyHooks, boolean isAbstract) {
        List<PropertyHookScope> hasBodies = new ArrayList<>(propertyHooks.size());
        for (PropertyHookScope propertyHook : propertyHooks) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processPropertyHook(propertyHook);
            if (propertyHook.hasBody()) {
                hasBodies.add(propertyHook);
            }
        }
        if (isAbstract && hasBodies.size() == propertyHooks.size()) {
            // public abstract $prop {get{} set{}}
            for (PropertyHookScope hook : hasBodies) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                hints.add(new SimpleHint(Bundle.AbstractPropertyHookWithBlock(), hook.getNameRange()));
            }
        }
    }

    @Messages({
        "# {0} - Property hook name",
        "# {1} - Modifier name",
        "InvalidPropertyHook=Property hook \"{0}\" cannot be declared {1}",
         "# {0} - Property hook name",
        "NonAbstractPropertyHookWithoutBlock=Non-abstract property hook \"{0}\" must have a body",
        "# {0} - Property hook name",
        "InvalidPrivateFinalPropertyHook=Private property hook \"{0}\" cannot be final"
   })
    private void processPropertyHook(PropertyHookScope propertyHook) {
        PhpModifiers phpModifiers = propertyHook.getPhpModifiers();
        String invalidModifier = getInvalidPropertyHookModifier(phpModifiers);
        FieldElement field = (FieldElement) propertyHook.getInScope();
        if (!StringUtils.isEmpty(invalidModifier)) {
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, invalidModifier, propertyHook.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidPropertyHook(propertyHook.getName(), invalidModifier), propertyHook.getNameRange(), fixes));
        } else if (!propertyHook.hasBody() && !propertyHook.getInScope().getPhpModifiers().isAbstract()) {
            // non-abstract property hook must have a body
            // e.g. public $prop {get;}
            hints.add(new SimpleHint(Bundle.NonAbstractPropertyHookWithoutBlock(propertyHook.getName()), propertyHook.getNameRange()));
        } else if (phpModifiers.isFinal()) {
            // e.g. private $prop {final get {}}
            PhpModifiers fieldModifier = propertyHook.getInScope().getPhpModifiers();
            if (fieldModifier.isPrivate()) {
                List<HintFix> fixes = List.of(new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, propertyHook.getOffset()));
                hints.add(new SimpleHint(Bundle.InvalidPrivateFinalPropertyHook(propertyHook.getName()), propertyHook.getNameRange(), fixes));
            }
        }
    }

    private String getInvalidPropertyHookModifier(PhpModifiers phpModifiers) {
        // final modifier is OK
        List<String> invalidModifiers = List.of(
                PhpModifiers.ABSTRACT_MODIFIER, PhpModifiers.READONLY_MODIFIER, PhpModifiers.STATIC_MODIFIER,
                PhpModifiers.VISIBILITY_PUBLIC, PhpModifiers.VISIBILITY_PROTECTED, PhpModifiers.VISIBILITY_PRIVATE,
                PhpModifiers.VISIBILITY_PUBLIC_SET, PhpModifiers.VISIBILITY_PROTECTED_SET, PhpModifiers.VISIBILITY_PRIVATE_SET
        );
        return getInvalidModifier(phpModifiers, invalidModifiers);
    }

    private void processMethods(Collection<? extends MethodScope> declaredMethods) {
        for (MethodScope methodScope : declaredMethods) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processMethodScope(methodScope);
        }
    }

    @Messages({
        "# {0} - Method name",
        "AbstractFinalMethod=Method \"{0}\" cannot be declared abstract and final",
        "# {0} - Method name",
        "AbstractWithBlockMethod=Abstract method \"{0}\" cannot contain body",
        "# {0} - Method name",
        "AbstractPrivateMethod=Abstract method \"{0}\" cannot be declared private",
        "# {0} - method name",
        "# {1} - Modifier name",
        "InvalidMethodModifier=Method \"{0}\" cannot be declared {1}",
        "FinalPrivateMethod=Private methods cannot be final as they are never overridden by other classes",
        "AbstractMethodInAnonymousClass=Anonymous class cannot declare abstract methods",
        "AbstractMethodInEnum=Enum cannot declare abstract methods",
    })
    private void processMethodScope(MethodScope methodScope) {
        PhpModifiers phpModifiers = methodScope.getPhpModifiers();
        List<HintFix> fixes;
        Scope inScope = methodScope.getInScope();
        boolean isClass = isClass(inScope);
        boolean isAnonClass = isAnonymousClass(inScope);
        boolean isTrait = inScope instanceof TraitScope;
        boolean isEnum = inScope instanceof EnumScope;
        if (phpModifiers.isAbstract() && isAnonClass) {
            fixes = new ArrayList<>();
            if (methodScope.getBlockRange() != null) {
                // e.g. abstract public function method(): void {}
                fixes.add(new RemoveModifierFix(doc, PhpModifiers.ABSTRACT_MODIFIER, methodScope.getOffset()));
            }
            hints.add(new SimpleHint(Bundle.AbstractMethodInAnonymousClass(), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && isEnum) {
            fixes = new ArrayList<>();
            if (methodScope.getBlockRange() != null) {
                // e.g. abstract public function method(): void {}
                fixes.add(new RemoveModifierFix(doc, PhpModifiers.ABSTRACT_MODIFIER, methodScope.getOffset()));
            }
            hints.add(new SimpleHint(Bundle.AbstractMethodInEnum(), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && phpModifiers.isFinal()) {
            fixes = new ArrayList<>();
            if (isClass || isTrait) {
                fixes.add(new RemoveModifierFix(doc, PhpModifiers.ABSTRACT_MODIFIER, methodScope.getOffset()));
            }
            fixes.add(new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.AbstractFinalMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && methodScope.getBlockRange() != null) {
            fixes = Collections.<HintFix>singletonList(new RemoveBodyFix(doc, methodScope));
            hints.add(new SimpleHint(Bundle.AbstractWithBlockMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && phpModifiers.isPrivate() && !isTrait) {
            // the following is valid in a trait scope
            // abstract private function abstractMethod();
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, PhpModifiers.VISIBILITY_PRIVATE, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.AbstractPrivateMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isFinal() && phpModifiers.isPrivate()) {
            // e.g. final private function method() {}
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.FinalPrivateMethod(), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isReadonly()) {
            // e.g. readonly function method() {}
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, PhpModifiers.READONLY_MODIFIER, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidMethodModifier(methodScope.getName(), PhpModifiers.READONLY_MODIFIER), methodScope.getNameRange(), fixes));
        } else if (Modifier.isSetVisibilityModifier(phpModifiers.toFlags())) {
            // e.g. private(set) function method() {}
            String invalidModifier = getSetVisibility(phpModifiers);
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidMethodModifier(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (hasMultipleAccessTypeModifiers(phpModifiers)) {
            // public private function example(){}
            fixes = getMultipleAccessTypeModifiersFixes(phpModifiers, methodScope.getOffset());
            hints.add(new SimpleHint(Bundle.MultipleAccesTypeModifiers(), methodScope.getNameRange(), fixes));
        } else if (hasMultipleSameModifier(methodScope.getOffset())) {
            addInvalidMultipleSameModifierHint(methodScope);
        }
        if (phpModifiers.isAbstract() && isClass && !isAnonClass) {
            currentClassHasAbstractMember = true;
        }
    }

    private boolean isClass(Scope scope) {
        return scope instanceof ClassScope;
    }

    private boolean isAnonymousClass(Scope scope) {
        if (scope instanceof ClassScope classScope) {
            return classScope.isAnonymous();
        }
        return false;
    }

    private void addInvalidMultipleSameModifierHint(MethodScope methodScope) {
        String multipleSameModifier = getMultipleSameModifiers(ts, methodScope.getOffset());
        if (!StringUtils.isEmpty(multipleSameModifier)) {
            // e.g. public public function method() {}
            List<HintFix> fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, multipleSameModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidMultipleModifiers(multipleSameModifier), methodScope.getNameRange(), fixes));
        }
    }

    private boolean hasMultipleSameModifier(int startOffset) {
        String multipleSameModifier = getMultipleSameModifiers(ts, startOffset);
        return !StringUtils.isEmpty(multipleSameModifier);
    }

    @Messages("MultipleAccesTypeModifiers=Multiple access type modifiers are not allowed")
    private boolean hasMultipleAccessTypeModifiers(PhpModifiers modifiers) {
        return ((modifiers.isPublic() && !modifiers.isImplicitPublic()) && modifiers.isProtected())
                || ((modifiers.isPublic() && !modifiers.isImplicitPublic()) && modifiers.isPrivate())
                || (modifiers.isProtected() && modifiers.isPrivate())
                || (modifiers.isPublicSet() && modifiers.isProtectedSet()) // set visibility
                || (modifiers.isPublicSet() && modifiers.isPrivateSet()) // set visibility
                || (modifiers.isProtectedSet() && modifiers.isPrivateSet()); // set visibility
    }

    @Messages({
        "# {0} - modifier",
        "InvalidMultipleModifiers=Multiple {0} modifiers are not allowed"
    })
    private String getMultipleSameModifiers(TokenSequence<? extends PHPTokenId> ts, int startOffset) {
        String sameModifier = CodeUtils.EMPTY_STRING;
        if (ts != null) {
            int originalOffset = ts.offset();
            try {
                ts.move(startOffset);
                TokenId lastTokenId = null;
                Set<PHPTokenId> tokens = new HashSet<>();
                // find a modifier because a statement may has multiple declarations
                // e.g. public const int CONST1 = 1, CONST2 = 2;
                boolean foundModifier = false;
                while (ts.movePrevious()) {
                    Token<? extends PHPTokenId> token = ts.token();
                    if (token.id() == PHPTokenId.PHP_SEMICOLON
                            || token.id() == PHPTokenId.PHP_CURLY_CLOSE
                            || token.id() == PHPTokenId.PHP_CURLY_OPEN) {
                        break;
                    }
                    if (MODIFIERS.contains(token.id())
                            || token.id() == PHPTokenId.PHP_CLASS
                            || token.id() == PHPTokenId.PHP_FUNCTION
                            || token.id() == PHPTokenId.PHP_CONST) {
                        foundModifier = true;
                        ts.moveNext();
                        break;
                    }
                }
                if (foundModifier) {
                    while (ts.movePrevious()) {
                        Token<? extends PHPTokenId> token = ts.token();
                        if (!MODIFIERS.contains(token.id())
                                && token.id() != PHPTokenId.PHP_CLASS
                                && token.id() != PHPTokenId.PHP_FUNCTION
                                && token.id() != PHPTokenId.PHP_CONST
                                && token.id() != PHPTokenId.WHITESPACE) {
                            ts.moveNext();
                            if (lastTokenId == PHPTokenId.WHITESPACE) {
                                ts.moveNext();
                            }
                            break;
                        } else if (token.id() != PHPTokenId.WHITESPACE && !tokens.add(token.id())) {
                            sameModifier = token.text().toString();
                            break;
                        }
                        lastTokenId = token.id();
                    }
                }
            } finally {
                ts.move(originalOffset);
                ts.moveNext();
            }
        }
        return sameModifier;
    }

    private String getVisibility(PhpModifiers phpModifiers) {
        String visibility = CodeUtils.EMPTY_STRING;
        if (phpModifiers.isPublic()) {
            visibility = PhpModifiers.VISIBILITY_PUBLIC;
        } else if (phpModifiers.isProtected()) {
            visibility = PhpModifiers.VISIBILITY_PROTECTED;
        } else if (phpModifiers.isPrivate()) {
            visibility = PhpModifiers.VISIBILITY_PRIVATE;
        }
        return visibility;
    }

    private String getSetVisibility(PhpModifiers phpModifiers) {
        String visibility = CodeUtils.EMPTY_STRING;
        if (phpModifiers.isPublicSet()) {
            visibility = PhpModifiers.VISIBILITY_PUBLIC_SET;
        } else if (phpModifiers.isProtectedSet()) {
            visibility = PhpModifiers.VISIBILITY_PROTECTED_SET;
        } else if (phpModifiers.isPrivateSet()) {
            visibility = PhpModifiers.VISIBILITY_PRIVATE_SET;
        }
        return visibility;
    }

    private int getTypeStart(int elementOffset, String declaredType) {
        int offset = elementOffset;
        if (!StringUtils.isEmpty(declaredType)) {
            offset -= declaredType.length() + 1; // +1:whitespace
        }
        return offset;
    }

    private boolean isVisibilityWeakerThanSet(PhpModifiers phpModifiers) {
        return (phpModifiers.isPublicSet() && (phpModifiers.isProtected() || phpModifiers.isPrivate()))
                || (phpModifiers.isProtectedSet() && phpModifiers.isPrivate());
    }

    private List<HintFix> getMultipleAccessTypeModifiersFixes(PhpModifiers modifiers, int offset) {
        List<HintFix> fixes = new ArrayList<>();
        List<String> visibilities = new ArrayList<>();
        List<String> setVisibilities = new ArrayList<>();
        if (modifiers.isPublic() && !modifiers.isImplicitPublic()) {
            visibilities.add(PhpModifiers.VISIBILITY_PUBLIC);
        }
        if (modifiers.isProtected()) {
            visibilities.add(PhpModifiers.VISIBILITY_PROTECTED);
        }
        if (modifiers.isPrivate()) {
            visibilities.add(PhpModifiers.VISIBILITY_PRIVATE);
        }
        if (modifiers.isPublicSet()) {
            visibilities.add(PhpModifiers.VISIBILITY_PUBLIC_SET);
        }
        if (modifiers.isProtectedSet()) {
            visibilities.add(PhpModifiers.VISIBILITY_PROTECTED_SET);
        }
        if (modifiers.isPrivateSet()) {
            visibilities.add(PhpModifiers.VISIBILITY_PRIVATE_SET);
        }
        if (visibilities.size() >= 2) { // 2: minimum combination of visibilities
            for (String visibility : visibilities) {
                fixes.add(new RemoveModifierFix(doc, visibility, offset));
            }
        }
        if (setVisibilities.size() >= 2) { // 2: minimum combination of set visibilities
            for (String visibility : setVisibilities) {
                fixes.add(new RemoveModifierFix(doc, visibility, offset));
            }
        }
        return fixes;
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

        // PHP 8.4 interface can have hooked fields
        if (interfaceScope instanceof TypeScope.FieldDeclarable fieldDeclarable) {
            Collection<? extends FieldElement> declaredFields = fieldDeclarable.getDeclaredFields();
            for (FieldElement declaredField : declaredFields) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processInterfaceField(declaredField);
            }
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
            invalidModifier = PhpModifiers.VISIBILITY_PRIVATE;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isProtected()) {
            invalidModifier = PhpModifiers.VISIBILITY_PROTECTED;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isFinal()) {
            invalidModifier = PhpModifiers.FINAL_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (hasAbstractModifier(methodScope)) {
            // don't use phpModifiers.isAbstract() because all interface methods are merked as abstract
            invalidModifier = PhpModifiers.ABSTRACT_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isReadonly()) {
            invalidModifier = PhpModifiers.READONLY_MODIFIER;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (Modifier.isSetVisibilityModifier(phpModifiers.toFlags())) {
            invalidModifier = getSetVisibility(phpModifiers);
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, methodScope.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfaceMethod(methodScope.getName(), invalidModifier), methodScope.getNameRange(), fixes));
        } else if (methodScope.getBlockRange() != null && methodScope.getBlockRange().getLength() != 1) {
            fixes = Collections.<HintFix>singletonList(new RemoveBodyFix(doc, methodScope));
            hints.add(new SimpleHint(Bundle.IfaceMethodWithBlock(methodScope.getName()), methodScope.getNameRange(), fixes));
        }
        addInvalidMultipleSameModifierHint(methodScope);
    }

    private boolean hasAbstractModifier(MethodScope methodScope) {
        boolean hasAbstractModifier = false;
        int startOffset = getStartOffset(methodScope.getOffset());
        if (ts != null) {
            int originalOffset = ts.offset();
            try {
                ts.move(methodScope.getOffset());
                while (ts.movePrevious() && (startOffset <= ts.offset())) {
                    if (ts.token().id() == PHPTokenId.PHP_ABSTRACT) {
                        hasAbstractModifier =true;
                        break;
                    }
                }
            } finally {
                ts.move(originalOffset);
                ts.moveNext();
            }
        }
        return hasAbstractModifier;
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
        boolean isFirstConst = isFirstConstant(classConstant.getOffset());
        int typeStart = getTypeStart(classConstant.getOffset(), classConstant.getDeclaredType());
        if (phpModifiers.isPrivate()) {
            invalidModifier = PhpModifiers.VISIBILITY_PRIVATE;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidIfaceConstant(classConstant.getName(), invalidModifier), classConstant.getNameRange(), fixes, isFirstConst);
        } else if (phpModifiers.isProtected()) {
            invalidModifier = PhpModifiers.VISIBILITY_PROTECTED;
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, typeStart));
            addSimpleHint(Bundle.InvalidIfaceConstant(classConstant.getName(), invalidModifier), classConstant.getNameRange(), fixes, isFirstConst);
        } else {
            processClassConstantElement(classConstant);
        }
    }

    @Messages({
        "# {0} - Field name",
        "# {1} - Modifier name",
        "InvalidIfaceField=Interface field \"{0}\" cannot be declared {1}",
    })
    private void processInterfaceField(FieldElement fieldElement) {
        PhpModifiers phpModifiers = fieldElement.getPhpModifiers();
        String invalidVisibility = getInvalidInterfaceFieldVisibility(phpModifiers);
        List<String> invalidModifiers = List.of(PhpModifiers.FINAL_MODIFIER, PhpModifiers.ABSTRACT_MODIFIER, PhpModifiers.READONLY_MODIFIER);
        String invalidModifier = getInvalidModifier(phpModifiers, invalidModifiers);
        int typeStart = getTypeStart(fieldElement.getOffset() - 1, fieldElement.getDefaultType()); // -1:$
        if (!StringUtils.isEmpty(invalidModifier)) {
            addInvalidInterfaceFiledHint(fieldElement, invalidModifier, typeStart);
        } else if (!StringUtils.isEmpty(invalidVisibility)) {
            if (PhpModifiers.isSetVisibilityModifier(phpModifiers.toFlags()) && phpModifiers.isPublic() && !phpModifiers.isImplicitPublic()) {
                addInvalidInterfaceFiledHint(fieldElement, invalidVisibility, typeStart);
            } else {
                addInvalidInterfaceFiledHint(fieldElement, invalidVisibility, PhpModifiers.VISIBILITY_PUBLIC, typeStart);
            }
        }

        // propertry hooks
        if (FieldElement.isHooked(fieldElement)) {
            FieldElement.HookedFieldElement hookedField = (FieldElement.HookedFieldElement) fieldElement;
            Collection<? extends PropertyHookScope> propertyHooks = hookedField.getPropertyHooks();
            for (PropertyHookScope propertyHook : propertyHooks) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processInterfacePropertyHook(propertyHook);
            }
        }
    }

    private String getInvalidInterfaceFieldVisibility(PhpModifiers phpModifiers) {
        String visibility = CodeUtils.EMPTY_STRING;
        if (phpModifiers.isPublic() && !phpModifiers.isImplicitPublic()) {
            if (phpModifiers.isProtectedSet()
                    || phpModifiers.isPrivateSet()) {
                visibility = getSetVisibility(phpModifiers);
            }
        } else if (phpModifiers.isPrivate()
                || phpModifiers.isProtected()
                || phpModifiers.isProtectedSet()
                || phpModifiers.isPrivateSet()) {
            visibility = getVisibility(phpModifiers);
            if (StringUtils.isEmpty(visibility)) {
                visibility = getSetVisibility(phpModifiers);
            }
        }
        return visibility;
    }

    private void addInvalidInterfaceFiledHint(FieldElement fieldElement, String invalidModifier, int typeStart) {
        List<HintFix> fixes = List.of(new RemoveModifierFix(doc, invalidModifier, typeStart));
        hints.add(new SimpleHint(Bundle.InvalidIfaceField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
    }

    private void addInvalidInterfaceFiledHint(FieldElement fieldElement, String invalidModifier, String newModifier, int typeStart) {
        List<HintFix> fixes = List.of(new ChangeModifierFix(doc, invalidModifier, newModifier, typeStart));
        hints.add(new SimpleHint(Bundle.InvalidIfaceField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
    }

    @Messages({
        "# {0} - Property hook name",
        "# {1} - Modifier name",
        "InvalidIfacePropertyHook=Interface property hook \"{0}\" cannot be declared {1}",
        "# {0} - Property hook name",
        "IfacePropertyHookWithBlock=Interface property hook \"{0}\" cannot contain body"
    })
    private void processInterfacePropertyHook(PropertyHookScope propertyHook) {
        PhpModifiers phpModifiers = propertyHook.getPhpModifiers();
        String invalidModifier = getInvalidInterfacePropertyHookModifier(phpModifiers);
        if (!StringUtils.isEmpty(invalidModifier)) {
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, invalidModifier, propertyHook.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidIfacePropertyHook(propertyHook.getName(), invalidModifier), propertyHook.getNameRange(), fixes));
        } else if (propertyHook.getBlockRange() != null && propertyHook.getBlockRange().getLength() != 1) {
            List<HintFix> fixes = List.of(new RemoveBodyFix(doc, propertyHook));
            hints.add(new SimpleHint(Bundle.IfacePropertyHookWithBlock(propertyHook.getName()), propertyHook.getNameRange(), fixes));
        }
    }

    private String getInvalidInterfacePropertyHookModifier(PhpModifiers phpModifiers){
        List<String> invalidModifiers = List.of(
                PhpModifiers.ABSTRACT_MODIFIER, PhpModifiers.FINAL_MODIFIER, PhpModifiers.READONLY_MODIFIER, PhpModifiers.STATIC_MODIFIER,
                PhpModifiers.VISIBILITY_PUBLIC, PhpModifiers.VISIBILITY_PROTECTED, PhpModifiers.VISIBILITY_PRIVATE,
                PhpModifiers.VISIBILITY_PUBLIC_SET, PhpModifiers.VISIBILITY_PROTECTED_SET, PhpModifiers.VISIBILITY_PRIVATE_SET
        );
        return getInvalidModifier(phpModifiers, invalidModifiers);
    }

    private String getInvalidModifier(PhpModifiers phpModifiers, List<String> invalidModifiers) {
        String invalidModifier = CodeUtils.EMPTY_STRING;
        for (String modifier : invalidModifiers) {
            switch (modifier) {
                case PhpModifiers.ABSTRACT_MODIFIER -> {
                    if (phpModifiers.isAbstract()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.FINAL_MODIFIER -> {
                    if (phpModifiers.isFinal()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.READONLY_MODIFIER -> {
                    if (phpModifiers.isReadonly()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.STATIC_MODIFIER -> {
                    if (phpModifiers.isStatic()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.VISIBILITY_PUBLIC -> {
                    if (phpModifiers.isPublic() && !phpModifiers.isImplicitPublic()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.VISIBILITY_PROTECTED -> {
                    if (phpModifiers.isProtected()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.VISIBILITY_PRIVATE -> {
                    if (phpModifiers.isPrivate()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.VISIBILITY_PUBLIC_SET -> {
                    if (phpModifiers.isPublicSet()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.VISIBILITY_PROTECTED_SET -> {
                    if (phpModifiers.isProtectedSet()) {
                        invalidModifier = modifier;
                    }
                }
                case PhpModifiers.VISIBILITY_PRIVATE_SET -> {
                    if (phpModifiers.isPrivateSet()) {
                        invalidModifier = modifier;
                    }
                }
                default -> {
                    assert false : modifier;
                }
            }
            if (!StringUtils.isEmpty(invalidModifier)) {
                break;
            }
        }
        return invalidModifier;
    }

    @Messages({
        "# {0} - Class name",
        "PossibleAbstractClass=Class \"{0}\" contains abstract members and must be declared abstract",
        "# {0} - Class name",
        "FinalPossibleAbstractClass=Class \"{0}\" contains abstract members and can not be declared final"
    })
    private void processPossibleAbstractClass(ClassScope classScope) {
        if (!classScope.isAbstract()) {
            List<HintFix> fixes = List.of(new AddModifierFix(doc, PhpModifiers.ABSTRACT_MODIFIER, classScope.getOffset()));
            hints.add(new SimpleHint(Bundle.PossibleAbstractClass(classScope.getName()), classScope.getNameRange(), fixes));
        }
        if (classScope.isFinal()) {
            List<HintFix> fixes = List.of(new RemoveModifierFix(doc, PhpModifiers.FINAL_MODIFIER, classScope.getOffset()));
            hints.add(new SimpleHint(Bundle.FinalPossibleAbstractClass(classScope.getName()), classScope.getNameRange(), fixes));
        }
    }

    private class SimpleHint extends Hint {

        public SimpleHint(String description, OffsetRange range, @NullAllowed List<HintFix> fixes) {
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

        private final Scope scope;

        public RemoveBodyFix(BaseDocument doc, Scope scope) {
            super(doc);
            assert scope instanceof MethodScope || scope instanceof PropertyHookScope
                    : "Unexpected scope: " + scope; // NOI18N
            this.scope = scope;
        }

        @Override
        @Messages({
            "RemoveBodyFix.Method=method",
            "RemoveBodyFix.PropertyHook=property hook",
            "# {0} - member name",
            "# {1} - name",
            "RemoveBodyFixDesc=Remove body of the {0}: {1}"
        })
        public String getDescription() {
            return Bundle.RemoveBodyFixDesc(getMemberTypeName(), scope.getName());
        }

        private String getMemberTypeName() {
            if (scope instanceof MethodScope) {
                return Bundle.RemoveBodyFix_Method();
            } else if (scope instanceof PropertyHookScope) {
                return Bundle.RemoveBodyFix_PropertyHook();
            }
            return "Unknown"; // NOI18N
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            edits.replace(scope.getBlockRange().getStart(), scope.getBlockRange().getLength(), ";", true, 0); //NOI18N
            edits.apply();
        }

    }

    private class RemoveModifierFix extends ChangeModifierFix {

        public RemoveModifierFix(BaseDocument doc, String modifier, int elementOffset) {
            this(doc, modifier, elementOffset, null);
        }

        public RemoveModifierFix(BaseDocument doc, String modifier, int elementOffset, OffsetRange offsetRange) {
            super(doc, modifier, CodeUtils.EMPTY_STRING, elementOffset, offsetRange);
        }

        @Override
        @Messages({
            "# {0} - Modifier name",
            "RemoveModifierFixDesc=Remove modifier: {0}"
        })
        public String getDescription() {
            return Bundle.RemoveModifierFixDesc(modifier);
        }
    }

    private class ChangeModifierFix extends AbstractHintFix {

        protected final String modifier;
        private final int elementOffset;
        private final OffsetRange offsetRange;
        private final String newModifier;

        public ChangeModifierFix(BaseDocument doc, String modifier, String newModifier, int elementOffset) {
            this(doc, modifier, newModifier, elementOffset, null);
        }

        public ChangeModifierFix(BaseDocument doc, String modifier, String newModifier, int elementOffset, OffsetRange offsetRange) {
            super(doc);
            this.offsetRange = offsetRange;
            this.modifier = modifier;
            this.newModifier = newModifier;
            this.elementOffset = elementOffset;
        }

        @Override
        @Messages({
            "# {0} - Old modifier name",
            "# {1} - New modifier name",
            "ChangeModifierFixDesc=Change modifier: {0} to {1}"
        })
        public String getDescription() {
            return Bundle.ChangeModifierFixDesc(modifier, newModifier);
        }

        @Override
        public void implement() throws Exception {
            EditList edits = new EditList(doc);
            if (offsetRange != null) {
                edits.replace(offsetRange.getStart(), offsetRange.getLength() + 1, "", true, 0); // NOI18N +1:whitespace
            } else {
                int startOffset = getStartOffset(elementOffset);
                int length = elementOffset - startOffset;
                String text = doc.getText(startOffset, length);
                // add " " to avoid matching unexpected modifiers
                // e.g. public public(set)
                int lastIndexOfModifier = text.lastIndexOf(modifier + " "); // NOI18N
                if (lastIndexOfModifier != -1) {
                    // public public private(set)
                    length -= lastIndexOfModifier;
                    startOffset += lastIndexOfModifier;
                    text = text.substring(lastIndexOfModifier);
                }
                String newMod = StringUtils.isEmpty(newModifier) ? CodeUtils.EMPTY_STRING : newModifier + " "; // NOI18N
                String replaceText = text.replace(modifier + " ", newMod).replaceAll("^\\s+", CodeUtils.EMPTY_STRING); //NOI18N
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
            int startOffset = getStartOffset(elementOffset);
            int length = elementOffset - startOffset;
            String replaceText = modifier + " " + doc.getText(startOffset, length); //NOI18N
            edits.replace(startOffset, length, replaceText, true, 0);
            edits.apply();
        }

    }

    private int getStartOffset(final int elementOffset) {
        int retval = 0;
        int originalOffset = 0;
        try {
            if (ts != null) {
                originalOffset = ts.offset();
                ts.move(elementOffset);
                TokenId lastTokenId = null;
                while (ts.movePrevious()) {
                    Token<? extends PHPTokenId> t = ts.token();
                        if (!MODIFIERS.contains(t.id())
                                && t.id() != PHPTokenId.PHP_CLASS
                                && t.id() != PHPTokenId.PHP_FUNCTION
                                && t.id() != PHPTokenId.WHITESPACE
                                && t.id() != PHPTokenId.PHP_CONST) {
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
            ts.move(originalOffset);
            ts.moveNext();
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
                    if (PredefinedSymbols.Attributes.ALLOW_DYNAMIC_PROPERTIES.getName().equals(attributeName)) {
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
