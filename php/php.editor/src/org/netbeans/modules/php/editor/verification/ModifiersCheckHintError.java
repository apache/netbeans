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
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenId;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
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
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class ModifiersCheckHintError extends HintErrorRule {
    private List<Hint> hints;
    private FileObject fileObject;
    private BaseDocument doc;
    private boolean currectClassHasAbstractMethod = false;

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
            Collection<? extends InterfaceScope> declaredInterfaces = ModelUtils.getDeclaredInterfaces(fileScope);
            for (InterfaceScope interfaceScope : declaredInterfaces) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                processInterfaceScope(interfaceScope);
            }
        }
    }

    @Override
    @Messages("ModifiersCheckHintDispName=Modifiers Checker")
    public String getDisplayName() {
        return Bundle.ModifiersCheckHintDispName();
    }

    private void processClassScope(ClassScope classScope) {
        Collection<? extends FieldElement> declaredFields = classScope.getDeclaredFields();
        for (FieldElement fieldElement : declaredFields) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processFieldElement(fieldElement);
        }
        Collection<? extends MethodScope> declaredMethods = classScope.getDeclaredMethods();
        for (MethodScope methodScope : declaredMethods) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            processMethodScope(methodScope);
        }
        if (currectClassHasAbstractMethod) {
            processPossibleAbstractClass(classScope);
        }
        currectClassHasAbstractMethod = false;
    }

    @Messages({
        "# {0} - Field name",
        "# {1} - Modifier name",
        "InvalidField=Field \"{0}\" can not be declared {1}"
    })
    private void processFieldElement(FieldElement fieldElement) {
        PhpModifiers phpModifiers = fieldElement.getPhpModifiers();
        List<HintFix> fixes;
        String invalidModifier;
        if (phpModifiers.isAbstract()) {
            invalidModifier = "abstract"; //NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, fieldElement.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
        } else if (phpModifiers.isFinal()) {
            invalidModifier = "final"; //NOI18N
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, invalidModifier, fieldElement.getOffset()));
            hints.add(new SimpleHint(Bundle.InvalidField(fieldElement.getName(), invalidModifier), fieldElement.getNameRange(), fixes));
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
        if (phpModifiers.isAbstract() && phpModifiers.isFinal()) {
            fixes = new ArrayList<>();
            fixes.add(new RemoveModifierFix(doc, "abstract", methodScope.getOffset())); //NOI18N
            fixes.add(new RemoveModifierFix(doc, "final", methodScope.getOffset())); //NOI18N
            hints.add(new SimpleHint(Bundle.AbstractFinalMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && methodScope.getBlockRange() != null) {
            fixes = Collections.<HintFix>singletonList(new RemoveBodyFix(doc, methodScope));
            hints.add(new SimpleHint(Bundle.AbstractWithBlockMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        } else if (phpModifiers.isAbstract() && phpModifiers.isPrivate()) {
            fixes = Collections.<HintFix>singletonList(new RemoveModifierFix(doc, "private", methodScope.getOffset())); //NOI18N
            hints.add(new SimpleHint(Bundle.AbstractPrivateMethod(methodScope.getName()), methodScope.getNameRange(), fixes));
        }
        if (phpModifiers.isAbstract()) {
            currectClassHasAbstractMethod = true;
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

        public RemoveModifierFix(BaseDocument doc, String modifier, int elementOffset) {
            super(doc);
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
            int startOffset = getStartOffset(doc, elementOffset);
            int length = elementOffset - startOffset;
            String replaceText = doc.getText(startOffset, length).replace(modifier, "").replaceAll("^\\s+", ""); //NOI18N
            edits.replace(startOffset, length, replaceText, true, 0);
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
            doc.readUnlock();
        }
        return retval;
    }

}
