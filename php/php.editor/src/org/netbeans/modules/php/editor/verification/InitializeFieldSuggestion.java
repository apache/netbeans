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
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.csl.api.EditList;
import org.netbeans.modules.csl.api.Hint;
import org.netbeans.modules.csl.api.HintFix;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.support.CancelSupport;
import org.netbeans.modules.php.api.PhpVersion;
import org.netbeans.modules.php.editor.CodeUtils;
import org.netbeans.modules.php.editor.parser.PHPParseResult;
import org.netbeans.modules.php.editor.parser.astnodes.Block;
import org.netbeans.modules.php.editor.parser.astnodes.ClassDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.ClassInstanceCreation;
import org.netbeans.modules.php.editor.parser.astnodes.Expression;
import org.netbeans.modules.php.editor.parser.astnodes.FieldsDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.FormalParameter;
import org.netbeans.modules.php.editor.parser.astnodes.FunctionDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.InterfaceDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.MethodDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.NullableType;
import org.netbeans.modules.php.editor.parser.astnodes.Reference;
import org.netbeans.modules.php.editor.parser.astnodes.SingleFieldDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.TraitDeclaration;
import org.netbeans.modules.php.editor.parser.astnodes.Variable;
import org.netbeans.modules.php.editor.parser.astnodes.Variadic;
import org.netbeans.modules.php.editor.parser.astnodes.visitors.DefaultVisitor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class InitializeFieldSuggestion extends SuggestionRule {

    private static final String SUGGESTION_ID = "Initialize.Field.Suggestion"; //NOI18N

    @Override
    public void invoke(PHPRuleContext context, List<Hint> hints) {
        PHPParseResult phpParseResult = (PHPParseResult) context.parserResult;
        if (phpParseResult.getProgram() != null) {
            FileObject fileObject = phpParseResult.getSnapshot().getSource().getFileObject();
            if (fileObject != null) {
                int caretOffset = getCaretOffset();
                final BaseDocument doc = context.doc;
                OffsetRange lineBounds = VerificationUtils.createLineBounds(caretOffset, doc);
                if (lineBounds.containsInclusive(caretOffset)) {
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    ConstructorVisitor constructorVisitor = new ConstructorVisitor(fileObject, doc);
                    phpParseResult.getProgram().accept(constructorVisitor);
                    if (CancelSupport.getDefault().isCancelled()) {
                        return;
                    }
                    hints.addAll(constructorVisitor.getHints());
                }
            }
        }
    }

    protected PhpVersion getPhpVersion(@NullAllowed FileObject fileObject) {
        return fileObject == null ? PhpVersion.getDefault() : CodeUtils.getPhpVersion(fileObject);
    }

    private final class ConstructorVisitor extends DefaultVisitor {

        private final FileObject fileObject;
        private final BaseDocument baseDocument;
        private final ArrayList<Hint> hints;
        private List<FormalParameter> formalParameters;
        private List<String> declaredFields;
        private List<String> usedVariables;
        private boolean isInConstructor;
        private int typeBodyStartOffset;
        private int constructorBodyEndOffset;

        private ConstructorVisitor(FileObject fileObject, BaseDocument baseDocument) {
            this.fileObject = fileObject;
            this.baseDocument = baseDocument;
            this.hints = new ArrayList<>();
        }

        public List<Hint> getHints() {
            return Collections.unmodifiableList(hints);
        }

        @Override
        public void visit(ClassDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            typeBodyStartOffset = node.getBody().getStartOffset() + 1;
            declaredFields = new ArrayList<>();
            usedVariables = new ArrayList<>();
            super.visit(node);
            typeBodyStartOffset = 0;
        }

        @Override
        public void visit(ClassInstanceCreation node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (!node.isAnonymous()) {
                return;
            }
            Block body = node.getBody();
            assert body != null : node;
            typeBodyStartOffset = body.getStartOffset() + 1;
            declaredFields = new ArrayList<>();
            usedVariables = new ArrayList<>();
            super.visit(node);
            typeBodyStartOffset = 0;
        }

        @Override
        public void visit(TraitDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            typeBodyStartOffset = node.getBody().getStartOffset() + 1;
            declaredFields = new ArrayList<>();
            usedVariables = new ArrayList<>();
            super.visit(node);
            typeBodyStartOffset = 0;
        }

        @Override
        public void visit(InterfaceDeclaration node) {
            // do not process ifaces
        }

        @Override
        public void visit(SingleFieldDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            String fieldName = CodeUtils.extractVariableName(node.getName());
            if (fieldName != null && declaredFields != null) {
                declaredFields.add(fieldName);
            }
        }

        @Override
        public void visit(MethodDeclaration node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            // [NETBEANS-4443] PHP 8.0 Constructor Property Promotion
            if (CodeUtils.isConstructor(node)) {
                processConstructorPropertyPromotion(node);
            }
            FunctionDeclaration function = node.getFunction();
            if (CodeUtils.isConstructor(node) && function.getBody() != null && function.getBody().isCurly()) {
                formalParameters = new ArrayList<>(function.getFormalParameters());
                isInConstructor = true;
                constructorBodyEndOffset = function.getBody().getEndOffset() - 1;
                scan(function.getBody());
                isInConstructor = false;
                createHints();
            }
        }

        private void processConstructorPropertyPromotion(MethodDeclaration node) {
            for (FormalParameter formalParameter : node.getFunction().getFormalParameters()) {
                if (CancelSupport.getDefault().isCancelled()) {
                    return;
                }
                // scan paramters as fields
                FieldsDeclaration fieldsDeclaration = FieldsDeclaration.create(formalParameter);
                if (fieldsDeclaration != null) {
                    // e.g.
                    // private int $param,
                    // private string $param = "default value"
                    String paramName = CodeUtils.extractFormalParameterName(formalParameter);
                    if (paramName != null) {
                        usedVariables.add(paramName);
                    }
                    scan(fieldsDeclaration);
                }
            }
        }

        private void createHints() {
            for (ParameterToInit parameterToInit : createParametersToInit()) {
                hints.add(parameterToInit.createHint(fileObject, baseDocument));
            }
        }

        private List<ParameterToInit> createParametersToInit() {
            List<ParameterToInit> result = new ArrayList<>();
            for (FormalParameter formalParameter : formalParameters) {
                String parameterName = extractParameterName(formalParameter.getParameterName());
                if (parameterName != null && !parameterName.isEmpty()) {
                    List<Initializer> initializers = new ArrayList<>();
                    if (!usedVariables.contains(parameterName)) {
                        initializers.add(new FieldAssignmentInitializer(constructorBodyEndOffset, parameterName));
                    }
                    if (!declaredFields.contains(parameterName)) {
                        initializers.add(new FieldDeclarationInitializer(typeBodyStartOffset, formalParameter, getPhpVersion(fileObject)));
                    }
                    if (!initializers.isEmpty()) {
                        result.add(new ParameterToInit(formalParameter, initializers));
                    }
                }
            }
            return result;
        }

        @Override
        public void visit(Variable node) {
            if (CancelSupport.getDefault().isCancelled()) {
                return;
            }
            if (isInConstructor) {
                String variableName = CodeUtils.extractVariableName(node);
                if (variableName != null) {
                    usedVariables.add(variableName);
                }
            }
        }

    }

    private final class ParameterToInit {

        private final FormalParameter formalParameter;
        private final List<Initializer> initializers;

        public ParameterToInit(FormalParameter formalParameter, List<Initializer> initializers) {
            this.formalParameter = formalParameter;
            this.initializers = initializers;
        }

        public String getName() {
            return extractParameterName(formalParameter.getParameterName());
        }

        @NbBundle.Messages({
            "# {0} - Field name",
            "InitializeFieldSuggestionText=Initialize Field: {0}"
        })
        public Hint createHint(FileObject fileObject, BaseDocument baseDocument) {
            OffsetRange offsetRange = new OffsetRange(formalParameter.getStartOffset(), formalParameter.getEndOffset());
            return new Hint(
                    InitializeFieldSuggestion.this,
                    Bundle.InitializeFieldSuggestionText(getName()),
                    fileObject,
                    offsetRange,
                    Collections.<HintFix>singletonList(new Fix(this, baseDocument)),
                    500);
        }

        public void initialize(EditList editList) {
            for (Initializer initializer : initializers) {
                initializer.initialize(editList);
            }
        }

    }

    private interface Initializer {

        void initialize(EditList editList);

    }

    private abstract static class InitializerImpl implements Initializer {

        private final int offset;

        public InitializerImpl(int offset) {
            this.offset = offset;
        }

        @Override
        public void initialize(EditList editList) {
            editList.replace(offset, 0, getInitString(), true, 0);
        }

        public abstract String getInitString();

    }

    private static class FieldDeclarationInitializer extends InitializerImpl {

        private final String initString;

        public FieldDeclarationInitializer(int offset, FormalParameter node, PhpVersion phpVersion) {
            super(offset);

            Expression parameterType = node.getParameterType();
            if (parameterType instanceof NullableType) {
                parameterType = ((NullableType) parameterType).getType();
            }
            String typeName = parameterType == null ? null : CodeUtils.extractQualifiedName(parameterType);
            String parameterName = extractParameterName(node.getParameterName());

            StringBuilder sb = new StringBuilder();
            sb.append("\n"); // NOI18N
            if (typeName != null && !phpVersion.hasPropertyTypes()) {
                // type part
                sb.append("/**\n * @var "); // NOI18N
                sb.append(typeName);
                if (node.isNullableType()) {
                    sb.append("|null"); // NOI18N
                }
                sb.append("\n */\n"); // NOI18N
            }
            sb.append("private "); // NOI18N
            if (typeName != null && phpVersion.hasPropertyTypes()) {
                // typed properties are supported since PHP 7.4
                // https://wiki.php.net/rfc/typed_properties_v2
                if (node.isNullableType()) {
                    sb.append(CodeUtils.NULLABLE_TYPE_PREFIX);
                }
                sb.append(typeName).append(" "); // NOI18N
            }
            sb.append(parameterName).append(";\n"); // NOI18N
            initString = sb.toString();
        }

        @Override
        public String getInitString() {
            return initString;
        }

    }

    private static class FieldAssignmentInitializer extends InitializerImpl {

        private final String initString;

        public FieldAssignmentInitializer(int offset, String parameterName) {
            super(offset);
            initString = "$this->" + parameterName.substring(1) + " = " + parameterName + ";\n"; //NOI18N
        }

        @Override
        public String getInitString() {
            return initString;
        }

    }

    private static final class Fix implements HintFix {

        private final ParameterToInit parameterToInit;
        private final BaseDocument baseDocument;

        private Fix(ParameterToInit parameterToInit, BaseDocument baseDocument) {
            this.parameterToInit = parameterToInit;
            this.baseDocument = baseDocument;
        }

        @Override
        @NbBundle.Messages({
            "# {0} - Field name",
            "InitializeFieldSuggestionFix=Initialize Field: {0}"
        })
        public String getDescription() {
            return Bundle.InitializeFieldSuggestionFix(parameterToInit.getName());
        }

        @Override
        public void implement() throws Exception {
            EditList editList = new EditList(baseDocument);
            parameterToInit.initialize(editList);
            editList.apply();
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

    @CheckForNull
    private static String extractParameterName(Expression parameterNameExpression) {
        String result = null;
        if (parameterNameExpression instanceof Variable) {
            result = CodeUtils.extractVariableName((Variable) parameterNameExpression);
        } else if (parameterNameExpression instanceof Reference) {
            Reference reference = (Reference) parameterNameExpression;
            Expression expression = reference.getExpression();
            if (expression instanceof Variadic) {
                expression = ((Variadic) reference.getExpression()).getExpression();
            }
            if (expression instanceof Variable) {
                result = CodeUtils.extractVariableName((Variable) expression);
            }
        } else if(parameterNameExpression instanceof Variadic) { // #249306
            Variadic variadic = (Variadic) parameterNameExpression;
            Expression expression = variadic.getExpression();
            if (expression instanceof Variable) {
                result = CodeUtils.extractVariableName((Variable) expression);
            }
        }
        return result;
    }

    @Override
    public String getId() {
        return SUGGESTION_ID;
    }

    @Override
    @NbBundle.Messages("InitializeFieldSuggestionDesc=Initializes field with a parameter passed to constructor.")
    public String getDescription() {
        return Bundle.InitializeFieldSuggestionDesc();
    }

    @Override
    @NbBundle.Messages("InitializeFieldSuggestionDisp=Initialize Field in Constructor")
    public String getDisplayName() {
        return Bundle.InitializeFieldSuggestionDisp();
    }

}
