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

package org.netbeans.modules.cnd.refactoring.codegen;

import java.awt.Dialog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionParameterList;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelutil.ui.ElementNode;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.codegen.ui.OperatorsPanel;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.GeneratorUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.spi.editor.codegen.CodeGenerator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class OperatorGenerator implements CodeGenerator {

    public static final class Factory implements CodeGenerator.Factory {

        @Override
        public List<? extends CodeGenerator> create(Lookup context) {
            ArrayList<CodeGenerator> ret = new ArrayList<>();
            JTextComponent component = context.lookup(JTextComponent.class);
            CsmContext path = context.lookup(CsmContext.class);
            if (component == null || path == null) {
                return ret;
            }
            CsmClass typeElement = path.getEnclosingClass();
            if (typeElement == null) {
                return ret;
            }
            List<CsmObject> pathList = path.getPath();
            CsmObject last = pathList.get(pathList.size()-1);
            if (!(CsmKindUtilities.isClass(last) || CsmKindUtilities.isField(last))) {
                return ret;
            }
//            CsmObject objectUnderOffset = path.getObjectUnderOffset();
//            final List<Pair<CsmField,ConstructorGenerator.Inited>> fields = new ArrayList<>();
//            final List<CsmConstructor> constructors = new ArrayList<>();
//            final Map<CsmClass,List<CsmConstructor>> inheritedConstructors = new HashMap<>();
//            CsmCacheManager.enter();
//            try {
//            // check base class
//            for (CsmInheritance csmInheritance : typeElement.getBaseClasses()) {
//                CsmClass baseClass = CsmInheritanceUtilities.getCsmClass(csmInheritance);
//                if (baseClass != null) {
//                    List<CsmConstructor> list = new ArrayList<>();
//                    for (CsmMember member : baseClass.getMembers()) {
//                        if (CsmKindUtilities.isConstructor(member) &&
//                            CsmInheritanceUtilities.matchVisibility(member, CsmVisibility.PROTECTED) &&
//                            !isCopyConstructor(baseClass, (CsmConstructor)member)) {
//                            list.add((CsmConstructor)member);
//                        }
//                    }
//                    if (!list.isEmpty()) {
//                        inheritedConstructors.put(baseClass, list);
//                    }
//                }
//            }
//            GeneratorUtils.scanForFieldsAndConstructors(typeElement, fields, constructors);
//            } finally {
//                CsmCacheManager.leave();
//            }
//            ElementNode.Description constructorDescription = null;
//            if (!inheritedConstructors.isEmpty()) {
//                List<ElementNode.Description> baseClassesDescriptions = new ArrayList<>();
//                for (Map.Entry<CsmClass,List<CsmConstructor>> entry : inheritedConstructors.entrySet()) {
//                    List<ElementNode.Description> constructorDescriptions = new ArrayList<>();
//                    for(CsmConstructor c : entry.getValue()) {
//                        constructorDescriptions.add(ElementNode.Description.create(c, null, true, false));
//                    }
//                    baseClassesDescriptions.add(ElementNode.Description.create(entry.getKey(), constructorDescriptions, false, false));
//                }
//                constructorDescription = ElementNode.Description.create(typeElement, baseClassesDescriptions, false, false);
//            }
//            ElementNode.Description fieldsDescription = null;
//            if (!fields.isEmpty()) {
//                List<ElementNode.Description> fieldDescriptions = new ArrayList<>();
//                for (Pair<CsmField,ConstructorGenerator.Inited> variableElement : fields) {
//                    switch(variableElement.second()) {
//                        case must:
//                            fieldDescriptions.add(ElementNode.Description.create(variableElement.first(), null, true, true));
//                            break;
//                        case may:
//                            fieldDescriptions.add(ElementNode.Description.create(variableElement.first(), null, true, variableElement.equals(objectUnderOffset)));
//                            break;
//                        case cannot:
//                            fieldDescriptions.add(ElementNode.Description.create(variableElement.first(), null, false, false));
//                            break;
//                    }
//                }
//                fieldsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, fieldDescriptions, false, false)), false, false);
//            }
//            if (constructorDescription == null && fieldsDescription == null) {
//                return ret;
//            }
            List<ElementNode.Description> operators;
            ElementNode.Description operatorsDescription;
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.EQ, false, false), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorAssignment")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MOD_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MOD, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.DIV_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.DIV, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MUL_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MUL, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.PLUS_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.PLUS, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MINUS_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MINUS, false, true), null, true, false));
            
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.PLUS_PLUS, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.PLUS_PLUS, false, false, 1), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MINUS_MINUS, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.MINUS_MINUS, false, false, 1), null, true, false));

            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorArithmetic")); //NOI18N

            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.TILDE, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.AND_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.AND, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.OR_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.OR, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.XOR_EQ, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.XOR, false, true), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorBitwise")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.ARROW, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.ARROW, true, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.ARRAY, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.ARRAY, true, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.POINTER, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.POINTER, true, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.ADDRESS, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.ADDRESS, true, true), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorPointer")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.EQ_EQ, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.NOT_EQ, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.GREATER, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.GREATER_EQ, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.LESS, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.LESS_EQ, false, true), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorRelational")); //NOI18N
            
            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.NOT, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.AND_AND, false, true), null, true, false));
            operators.add(ElementNode.Description.create(new StubMethodImpl(typeElement, CsmFunction.OperatorKind.OR_OR, false, true), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorLogical")); //NOI18N

            operators = new ArrayList<>();
            operators.add(ElementNode.Description.create(new StubFriendImpl(typeElement, CsmFunction.OperatorKind.LEFT_SHIFT, false, false), null, true, false));
            operators.add(ElementNode.Description.create(new StubFriendImpl(typeElement, CsmFunction.OperatorKind.RIGHT_SHIFT, false, false), null, true, false));
            operatorsDescription = ElementNode.Description.create(typeElement, Collections.singletonList(ElementNode.Description.create(typeElement, operators, false, false)), false, false);
            ret.add(new OperatorGenerator(component, path, typeElement, operatorsDescription, "LBL_operatorFriendStream")); //NOI18N
            
            return ret;
        }
        
//        private boolean isCopyConstructor(CsmClass cls, CsmConstructor constructor) {
//            Collection<CsmParameter> parameters = constructor.getParameters();
//            if (parameters.size() == 1) {
//                CsmParameter p = parameters.iterator().next();
//                CsmType paramType = p.getType();
//                if (paramType.isReference()) {
//                    if (cls.equals(paramType.getClassifier())) {
//                        return true;
//                    }
//                }
//            }
//            return false;
//        }
    }
    
    private final JTextComponent component;
    private final ElementNode.Description operators;
    private final CsmContext contextPath;
    private final CsmClass type;
    private final String id;

    /** Creates a new instance of ConstructorGenerator */
    private OperatorGenerator(JTextComponent component, CsmContext path, CsmClass type, ElementNode.Description operators, String id) {
        this.component = component;
        this.operators = operators;
        this.contextPath = path;
        this.type = type;
        this.id=id;
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(ConstructorGenerator.class, id);
    }

    @Override
    public void invoke() {
        UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CsmRefactoringUtils.GENERATE_TRACKING, "CONSTRUCTOR"); // NOI18N
        if (operators != null) {
            final OperatorsPanel panel = new OperatorsPanel(operators);
            DialogDescriptor dialogDescriptor = GeneratorUtils.createDialogDescriptor(panel, NbBundle.getMessage(ConstructorGenerator.class, "LBL_generate_operator")); //NOI18N
            Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
            try {
                dialog.setVisible(true);
            } catch (Throwable th) {
                if (!(th.getCause() instanceof InterruptedException)) {
                    throw new RuntimeException(th);
                }
                dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
            } finally {
                dialog.dispose();
            }
            if (dialogDescriptor.getValue() != dialogDescriptor.getDefaultValue()) {
                return;
            }
            GeneratorUtils.generateOperators(contextPath,  type, panel.getOperatorsToGenerate());
        }
    }

    private static abstract class StubFunctionImpl implements CsmFunction {
        protected final CsmClass parent;
        private final CsmFunction.OperatorKind kind;
        private final boolean constResult;
        private final boolean constOperator;
        private final int posfix;
        private String name;
        private String parameters;
        private String specifiers;
        private String returns;
        private String body;

        public StubFunctionImpl(CsmClass parent, CsmFunction.OperatorKind kind, boolean constResult, boolean constOperator) {
            this.parent = parent;
            this.kind = kind;
            this.constResult = constResult;
            this.constOperator = constOperator;
            this.posfix = 0;
            init();
        }

        public StubFunctionImpl(CsmClass parent, CsmFunction.OperatorKind kind, boolean constResult, boolean constOperator, int postfix) {
            this.parent = parent;
            this.kind = kind;
            this.constResult = constResult;
            this.constOperator = constOperator;
            this.posfix = 1;
            init();
        }

        private void init() {
            switch (kind) {
                case MOD_EQ:
                    name = "operator %="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MOD_EQ", getTemplateType()); // NOI18N
                    break;
                case MOD:
                    name = "operator %"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MOD", getTemplateType()); // NOI18N
                    break;
                case DIV_EQ:
                    name = "operator /="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "DIV_EQ", getTemplateType()); // NOI18N
                    break;
                case DIV:
                    name = "operator /"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "DIV", getTemplateType()); // NOI18N
                    break;
                case MUL_EQ:
                    name = "operator *="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MUL_EQ", getTemplateType()); // NOI18N
                    break;
                case MUL:
                    name = "operator *"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MUL", getTemplateType()); // NOI18N
                    break;
                case PLUS_EQ:
                    name = "operator +="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "PLUS_EQ", getTemplateType()); // NOI18N
                    break;
                case PLUS:
                    name = "operator +"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "PLUS", getTemplateType()); // NOI18N
                    break;
                case MINUS_EQ:
                    name = "operator -="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MINUS_EQ", getTemplateType()); // NOI18N
                    break;
                case MINUS:
                    name = "operator -"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "MINUS", getTemplateType()); // NOI18N
                    break;
                case PLUS_PLUS:
                    name = "operator ++"; // NOI18N
                    if (posfix == 0) {
                        parameters = ""; // NOI18N
                        returns = getTemplateType()+"&"; // NOI18N
                        body = NbBundle.getMessage(ConstructorGenerator.class, "PLUS_PLUS", getTemplateType()); // NOI18N
                    } else {
                        parameters = "int"; // NOI18N
                        returns = getTemplateType();
                        body = NbBundle.getMessage(ConstructorGenerator.class, "PLUS_PLUS_POSTFIX", getTemplateType()); // NOI18N
                    }
                    break;
                case MINUS_MINUS:
                    name = "operator --"; // NOI18N
                    if (posfix == 0) {
                        parameters = ""; // NOI18N
                        returns = getTemplateType()+"&"; // NOI18N
                        body = NbBundle.getMessage(ConstructorGenerator.class, "MINUS_MINUS", getTemplateType()); // NOI18N
                    } else {
                        parameters = "int"; // NOI18N
                        returns = getTemplateType();
                        body = NbBundle.getMessage(ConstructorGenerator.class, "MINUS_MINUS_POSTFIX", getTemplateType()); // NOI18N
                    }
                    break;
                case TILDE:
                    name = "operator ~"; // NOI18N
                    parameters = ""; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "TILDE", getTemplateType()); // NOI18N
                    break;
                case AND_EQ:
                    name = "operator &="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "AND_EQ", getTemplateType()); // NOI18N
                    break;
                case AND:
                    name = "operator &"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "AND", getTemplateType()); // NOI18N
                    break;
                case OR_EQ:
                    name = "operator |="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "OR_EQ", getTemplateType()); // NOI18N
                    break;
                case OR:
                    name = "operator |"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "OR", getTemplateType()); // NOI18N
                    break;
                case XOR_EQ:
                    name = "operator ^="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "XOR_EQ", getTemplateType()); // NOI18N
                    break;
                case XOR:
                    name = "operator ^"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType();
                    body = NbBundle.getMessage(ConstructorGenerator.class, "XOR", getTemplateType()); // NOI18N
                    break;
                case EQ_EQ:
                    name = "operator =="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "EQ_EQ", getTemplateType()); // NOI18N
                    break;
                case NOT_EQ:
                    name = "operator !="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "NOT_EQ", getTemplateType()); // NOI18N
                    break;
                case EQ:
                    name = "operator ="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = getTemplateType()+"&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "ASSIGNMENT", getTemplateType()); // NOI18N
                    break;
                case GREATER:
                    name = "operator >"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "GREATER", getTemplateType()); // NOI18N
                    break;
                case GREATER_EQ:
                    name = "operator >="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "GREATER_EQ", getTemplateType()); // NOI18N
                    break;
                case LESS:
                    name = "operator <"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "LESS", getTemplateType()); // NOI18N
                    break;
                case LESS_EQ:
                    name = "operator <="; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "LESS_EQ", getTemplateType()); // NOI18N
                    break;
                case NOT:
                    name = "operator !"; // NOI18N
                    parameters = ""; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "NOT", getTemplateType()); // NOI18N
                    break;
                case OR_OR:
                    name = "operator ||"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "OR_OR", getTemplateType()); // NOI18N
                    break;
                case AND_AND:
                    name = "operator &&"; // NOI18N
                    parameters = "const " + getTemplateType() + "& right"; // NOI18N
                    returns = "bool"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "AND_AND", getTemplateType()); // NOI18N
                    break;
                case LEFT_SHIFT:
                    specifiers=getTemplatePrefix("friend");// NOI18N
                    name = "operator <<"; // NOI18N
                    parameters = "std::ostream& os, const " + getTemplateType() + "& obj"; // NOI18N
                    returns = "std::ostream&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "LEFT_SHIFT", getTemplateType()); // NOI18N
                    break;
                case RIGHT_SHIFT:
                    specifiers=getTemplatePrefix("friend");// NOI18N
                    name = "operator >>"; // NOI18N
                    parameters = "std::ostream& is, const " + getTemplateType() + "& obj"; // NOI18N
                    returns = "std::ostream&"; // NOI18N
                    body = NbBundle.getMessage(ConstructorGenerator.class, "RIGHT_SHIFT", getTemplateType()); // NOI18N
                    break;
                case ARROW:
                    name = "operator ->"; // NOI18N
                    parameters = ""; // NOI18N
                    returns = "value_t*"; // NOI18N
                    if (constOperator) {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "ARROW_CONST", getTemplateType()); // NOI18N
                    } else {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "ARROW", getTemplateType()); // NOI18N
                    }
                    break;
                case POINTER:
                    name = "operator *"; // NOI18N
                    parameters = ""; // NOI18N
                    returns = "value_t&"; // NOI18N
                    if (constOperator) {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "POINTER_CONST", getTemplateType()); // NOI18N
                    } else {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "POINTER", getTemplateType()); // NOI18N
                    }
                    break;
                case ARRAY:
                    name = "operator []"; // NOI18N
                    parameters = "std::size_t index"; // NOI18N
                    returns = "value_t&"; // NOI18N
                    if (constOperator) {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "ARRAY_CONST", getTemplateType()); // NOI18N
                    } else {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "ARRAY", getTemplateType()); // NOI18N
                    }    
                    break;
                case ADDRESS:
                    name = "operator &"; // NOI18N
                    parameters = ""; // NOI18N
                    returns = "value_t"; // NOI18N
                    if (constOperator) {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "ADDRESS_CONST", getTemplateType()); // NOI18N
                    } else {
                        body = NbBundle.getMessage(ConstructorGenerator.class, "ADDRESS", getTemplateType()); // NOI18N
                    }    
                    break;
            }
        }

        private String getTemplatePrefix(String prefix) {
            StringBuilder res = new StringBuilder();
            if (CsmKindUtilities.isTemplate(parent)) {
                final CsmTemplate template = (CsmTemplate)parent;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    res.append("template<");//NOI18N
                    boolean first = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!first) {
                            res.append(", "); //NOI18N
                        }
                        first = false;
                        res.append(param.getName());
                    }
                    res.append(">");//NOI18N
                    res.append('\n');//NOI18N
                }
            }
            res.append(prefix);
            return res.toString();
        }

        private String getTemplateType() {
            StringBuilder res = new StringBuilder();
            res.append(parent.getName());
            if (CsmKindUtilities.isTemplate(parent)) {
                final CsmTemplate template = (CsmTemplate)parent;
                List<CsmTemplateParameter> templateParameters = template.getTemplateParameters();
                if (templateParameters.size() > 0) {
                    res.append("<");//NOI18N
                    boolean first = true;
                    for(CsmTemplateParameter param : templateParameters) {
                        if (!first) {
                            res.append(", "); //NOI18N
                        }
                        first = false;
                        res.append(param.getName());
                    }
                    res.append(">");//NOI18N
                }
            }
            return res.toString();
        }
        
        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.FUNCTION;
        }

        @Override
        public CharSequence getUniqueName() {
            return "F:"+name; //NOI18N
        }

        @Override
        public CharSequence getQualifiedName() {
            return name;
        }

        @Override
        public CharSequence getName() {
            return name;
        }

        @Override
        public CsmScope getScope() {
            return parent;
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public CsmFile getContainingFile() {
            return parent.getContainingFile();
        }

        @Override
        public int getStartOffset() {
            return -1;
        }

        @Override
        public int getEndOffset() {
            return -1;
        }

        @Override
        public CsmOffsetable.Position getStartPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmOffsetable.Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            StringBuilder buf = new StringBuilder();
            if (specifiers!=null) {
                buf.append(specifiers);
                buf.append(' '); // NOI18N
            }
            if (constResult) {
                buf.append("const "); // NOI18N
            }
            buf.append(returns);
            buf.append(' '); // NOI18N
            buf.append(name);
            buf.append('('); // NOI18N
            buf.append(parameters);
            buf.append(")"); // NOI18N
            if (constOperator) {
                buf.append(" const"); // NOI18N
            }
            buf.append(" {\n"); // NOI18N
            buf.append(body);
            buf.append("\n}"); // NOI18N
            return buf.toString();
        }

        @Override
        public CharSequence getDeclarationText() {
            return getText();
        }

        @Override
        public CsmFunctionDefinition getDefinition() {
            return null;
        }

        @Override
        public CsmFunction getDeclaration() {
            return this;
        }

        @Override
        public boolean isOperator() {
            return true;
        }

        @Override
        public CsmFunction.OperatorKind getOperatorKind() {
            return kind;
        }

        @Override
        public boolean isInline() {
            return true;
        }

        @Override
        public CsmFunctionParameterList getParameterList() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmType getReturnType() {
            return new CsmType() {

                @Override
                public CsmClassifier getClassifier() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public CharSequence getClassifierText() {
                    return returns;
                }

                @Override
                public boolean isInstantiation() {
                    return false;
                }

                @Override
                public boolean hasInstantiationParams() {
                    return false;
                }

                @Override
                public List<CsmSpecializationParameter> getInstantiationParams() {
                    return Collections.emptyList();
                }

                @Override
                public int getArrayDepth() {
                    return 0;
                }

                @Override
                public boolean isPointer() {
                    return false;
                }

                @Override
                public int getPointerDepth() {
                    return 0;
                }

                @Override
                public boolean isReference() {
                    return false;
                }

                @Override
                public boolean isRValueReference() {
                    return false;
                }

                @Override
                public boolean isConst() {
                    return false;
                }

                @Override
                public boolean isVolatile() {
                    return false;
                }

                @Override
                public boolean isPackExpansion() {
                    return false;
                }

                @Override
                public boolean isBuiltInBased(boolean resolveTypeChain) {
                    return false;
                }

                @Override
                public boolean isTemplateBased() {
                    return false;
                }

                @Override
                public CharSequence getCanonicalText() {
                    return returns;
                }

                @Override
                public CsmFile getContainingFile() {
                    return parent.getContainingFile();
                }

                @Override
                public int getStartOffset() {
                    return -1;
                }

                @Override
                public int getEndOffset() {
                    return -1;
                }

                @Override
                public Position getStartPosition() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public Position getEndPosition() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public CharSequence getText() {
                    return returns;
                }
            };
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getSignature() {
            StringBuilder buf = new StringBuilder();
            buf.append(name);
            buf.append('('); // NOI18N
            buf.append(parameters);
            buf.append(")"); // NOI18N
            if (constOperator) {
                buf.append(" const"); // NOI18N
            }
            return buf.toString();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return Collections.emptyList();
        }
    }

    private static final class StubMethodImpl extends StubFunctionImpl implements CsmMethod {

        public StubMethodImpl(CsmClass parent, CsmFunction.OperatorKind kind, boolean constResult, boolean constOperator) {
            super(parent, kind, constResult, constOperator);
        }

        public StubMethodImpl(CsmClass parent, CsmFunction.OperatorKind kind, boolean constResult, boolean constOperator, int postfix) {
            super(parent, kind, constResult, constOperator, postfix);
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.FUNCTION_FRIEND;
        }

        @Override
        public boolean isAbstract() {
            return false;
        }

        @Override
        public boolean isVirtual() {
            return false;
        }

        @Override
        public boolean isOverride() {
            return false;
        }

        @Override
        public boolean isFinal() {
            return false;
        }

        @Override
        public boolean isExplicit() {
            return false;
        }

        @Override
        public boolean isConst() {
            return false;
        }

        @Override
        public boolean isVolatile() {
            return false;
        }

        @Override
        public boolean isLValue() {
            return false;
        }

        @Override
        public boolean isRValue() {
            return false;
        }

        @Override
        public CsmClass getContainingClass() {
            return parent;
        }

        @Override
        public CsmVisibility getVisibility() {
            return CsmVisibility.NONE;
        }

        @Override
        public boolean isStatic() {
            return false;
        }
    }
    private static final class StubFriendImpl extends StubFunctionImpl implements CsmFriendFunction {

        public StubFriendImpl(CsmClass parent, OperatorKind kind, boolean isConst, boolean constOperator) {
            super(parent, kind, isConst, constOperator);
        }

        @Override
        public CsmFunction getReferencedFunction() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CsmClass getContainingClass() {
            return parent;
        }
    }
}
