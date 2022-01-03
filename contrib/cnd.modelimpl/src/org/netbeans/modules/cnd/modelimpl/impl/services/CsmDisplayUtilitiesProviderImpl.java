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
package org.netbeans.modules.cnd.modelimpl.impl.services;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmField;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.Instantiation;
import static org.netbeans.modules.cnd.modelimpl.csm.Instantiation.CsmSpecializationParamTextProvider;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl.InstantiationParametersInfo;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import static org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities.htmlize;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.Predicate;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.TypeInfoCollector;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.SmartTypeUnrollPredicate;
import org.netbeans.modules.cnd.modelutil.CsmUtilities.Qualificator;
import org.netbeans.modules.cnd.modelutil.spi.CsmDisplayUtilitiesProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 */

@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.modelutil.spi.CsmDisplayUtilitiesProvider.class)
public final class CsmDisplayUtilitiesProviderImpl extends CsmDisplayUtilitiesProvider {
    
    private final static char SPACE = ' '; // NOI18N 

    @Override
    public CharSequence getTooltipText(CsmObject item) {
        try {
            CharSequence tooltipText = null;
            if (CsmKindUtilities.isMethod(item)) {
                CharSequence functionDisplayName = getFunctionText((CsmFunction)item);
                CsmMethod meth = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction)item);
                CsmClass methodDeclaringClass = ((CsmMember) meth).getContainingClass();
                CharSequence displayClassName = methodDeclaringClass.getQualifiedName();
                String key = "DSC_MethodTooltip";  // NOI18N
                if (CsmKindUtilities.isConstructor(item)) {
                    key = "DSC_ConstructorTooltip";  // NOI18N
                } else if (CsmKindUtilities.isDestructor(item)) {
                    key = "DSC_DestructorTooltip";  // NOI18N
                }
                tooltipText = getHtmlizedString(key, functionDisplayName, displayClassName);
            } else if (CsmKindUtilities.isFunction(item)) {
                CharSequence functionDisplayName = getFunctionText((CsmFunction)item);
                tooltipText = getHtmlizedString("DSC_FunctionTooltip", functionDisplayName); // NOI18N
            } else if (CsmKindUtilities.isClass(item)) {
                CsmDeclaration.Kind classKind = ((CsmDeclaration) item).getKind();
                String key;
                if (classKind == CsmDeclaration.Kind.STRUCT) {
                    key = "DSC_StructTooltip"; // NOI18N
                } else if (classKind == CsmDeclaration.Kind.UNION) {
                    key = "DSC_UnionTooltip"; // NOI18N
                } else {
                    key = "DSC_ClassTooltip"; // NOI18N
                }
                tooltipText = getHtmlizedString(key, ((CsmClassifier) item).getQualifiedName());
            } else if (CsmKindUtilities.isTypedef(item) || CsmKindUtilities.isTypeAlias(item)) {
                CharSequence tdName = ((CsmTypedef) item).getQualifiedName();
                tooltipText = getHtmlizedString("DSC_TypedefTooltip", tdName, ((CsmTypedef) item).getText()); // NOI18N
            } else if (CsmKindUtilities.isEnum(item)) {
                tooltipText = getHtmlizedString("DSC_EnumTooltip", ((CsmEnum) item).getQualifiedName()); // NOI18N
            } else if (CsmKindUtilities.isEnumerator(item)) {
                CsmEnumerator enmtr = ((CsmEnumerator) item);
                tooltipText = getHtmlizedString("DSC_EnumeratorTooltip", enmtr.getName(), enmtr.getEnumeration().getQualifiedName()); // NOI18N
            } else if (CsmKindUtilities.isField(item)) {
                CharSequence fieldName = ((CsmField) item).getName();
                CsmClass containingClass = ((CsmField) item).getContainingClass();
                CharSequence displayClassName = containingClass.getQualifiedName();
                CharSequence classKind = "class";//NOI18N
                if (containingClass.getKind() == CsmDeclaration.Kind.STRUCT) {
                    classKind = "struct"; //NOI18N
                } else if (containingClass.getKind() == CsmDeclaration.Kind.UNION) {
                    classKind = "union"; // NOI18N
                }
                tooltipText = getHtmlizedString("DSC_FieldTooltip", fieldName, classKind, displayClassName, ((CsmField) item).getText()); // NOI18N
            } else if (CsmKindUtilities.isParamVariable(item)) {
                CharSequence varName = ((CsmParameter) item).getName();
                tooltipText = getHtmlizedString("DSC_ParameterTooltip", varName, ((CsmParameter) item).getText()); // NOI18N
            } else if (CsmKindUtilities.isVariable(item)) {
                CharSequence varName = ((CsmVariable) item).getName();
                tooltipText = getHtmlizedString("DSC_VariableTooltip", varName, getVariableText((CsmVariable) item)); // NOI18N
            } else if (CsmKindUtilities.isFile(item)) {
                CharSequence fileName = ((CsmFile) item).getName();
                tooltipText = getHtmlizedString("DSC_FileTooltip", fileName); // NOI18N
            } else if (CsmKindUtilities.isNamespace(item)) {
                CharSequence nsName = ((CsmNamespace) item).getQualifiedName();
                tooltipText = getHtmlizedString("DSC_NamespaceTooltip", nsName); // NOI18N
            } else if (CsmKindUtilities.isMacro(item)) {
                CsmMacro macro = (CsmMacro)item;
                switch (macro.getKind()){
                    case DEFINED:
                        tooltipText = getHtmlizedString("DSC_MacroTooltip", macro.getName(), macro.getText()); // NOI18N
                        break;
                    case COMPILER_PREDEFINED:
                        tooltipText = getHtmlizedString("DSC_SysMacroTooltip", macro.getName(), macro.getText()); // NOI18N
                        break;
                    case POSITION_PREDEFINED:
                        tooltipText = getHtmlizedString("DSC_PosMacroTooltip", macro.getName(), macro.getText()); // NOI18N
                        break;
                    case USER_SPECIFIED:
                        tooltipText = getHtmlizedString("DSC_ProjectMacroTooltip", macro.getName(), macro.getText()); // NOI18N
                        break;
                    default:
                        throw new IllegalArgumentException("unexpected macro kind:" + macro.getKind() + " in macro:" + macro); // NOI18N
                }
            } else if (CsmKindUtilities.isErrorDirective(item)) {
                tooltipText = getHtmlizedString("DSC_ErrorDirectiveTooltip", ((CsmErrorDirective)item).getErrorMessage()); // NOI18N
            } else if (CsmKindUtilities.isInclude(item)) {
                CsmInclude incl = (CsmInclude)item;
                CsmFile target = incl.getIncludeFile();
                if (target == null) {
                    if (incl.getIncludeState() == CsmInclude.IncludeState.Recursive) {
                        tooltipText = getHtmlizedString("DSC_IncludeRecursiveTooltip", incl.getText());  // NOI18N
                    } else {
                        tooltipText = getHtmlizedString("DSC_IncludeErrorTooltip", incl.getText());  // NOI18N
                    }
                } else {
                    if (target.getProject().isArtificial()) {
                        tooltipText = getHtmlizedString("DSC_IncludeLibraryTooltip", target.getAbsolutePath());// NOI18N
                    } else {
                        tooltipText = getHtmlizedString("DSC_IncludeTooltip", target.getAbsolutePath(), target.getProject().getName());  // NOI18N
                    }
                }
            } else if (CsmKindUtilities.isQualified(item)) {
                tooltipText = ((CsmQualifiedNamedElement) item).getQualifiedName();
            } else if (CsmKindUtilities.isLabel(item)) {
                tooltipText = getHtmlizedString("DSC_LabelTooltip", ((CsmLabel)item).getLabel());  // NOI18N
            } else if (CsmKindUtilities.isNamedElement(item)) {
                tooltipText = ((CsmNamedElement)item).getName();
            } else {
                tooltipText = "unhandled object " + item;  // NOI18N
            }
            return tooltipText;
        } catch (Exception e) {
            CndUtils.assertTrueInConsole(false, "can not get text for " + item + " due to " + e); // NOI18N
            return ""; // NOI18N
        }
    }

    @Override
    public CharSequence getTypeText(CsmType type, boolean expandInstantiations, boolean evaluateExpressions) {
        if (type != null) {
            StringBuilder itemTextBuilder = new StringBuilder();
            
            DisplayTypeInfoCollector dtc = new DisplayTypeInfoCollector();
            type = CsmUtilities.iterateTypeChain(type, dtc);
            List<Qualificator> quals = dtc.infoCollector.qualificators;
            ListIterator<Qualificator> qualIter = quals.listIterator(quals.size());
            Qualificator qual = qualIter.hasPrevious() ? qualIter.previous() : null;
            // Place the first const before classifier
            if (Qualificator.CONST.equals(qual)) {
                appendQual(itemTextBuilder, qual).append(SPACE);
                qual = qualIter.hasPrevious() ? qualIter.previous() : null;
            }
            // Append classifier
            CsmClassifier resolvedCls = type.getClassifier();
            if (resolvedCls != null) {
                itemTextBuilder.append(resolvedCls.getQualifiedName());
                if (type.isInstantiation() && type.hasInstantiationParams()) {
                    itemTextBuilder.append(Instantiation.getInstantiationCanonicalText(type.getInstantiationParams()));
                } else if (CsmKindUtilities.isInstantiation(resolvedCls)) {
                    List<Pair<CsmSpecializationParameter, List<CsmInstantiation>>> fullInstParams = InstantiationProviderImpl.getInstantiationParams(resolvedCls);
                    InstantiationParametersInfo paramsInfo = new InstantiationProviderImpl.InstantiationParametersInfoImpl(resolvedCls, fullInstParams);
                    itemTextBuilder.append(Instantiation.getInstantiationCanonicalText(paramsInfo, new SpecParamsTextProvider(expandInstantiations, evaluateExpressions)));
                }
            } else {
                itemTextBuilder.append(type.getClassifierText()); 
            }
            // Append other qualifiers until reference is reached
            boolean needSpace = true;
            while (qual != null && !Qualificator.REFERENCE.equals(qual) && !Qualificator.RVALUE_REFERENCE.equals(qual)) {
                if (needSpace) {
                    itemTextBuilder.append(SPACE);
                }
                appendQual(itemTextBuilder, qual);
                Qualificator prev = qualIter.hasPrevious() ? qualIter.previous() : null;
                needSpace = isSpaceRequired(qual, prev);
                qual = prev;
            }
            // if there was reference, then add it
            if (qual != null) {     
                appendQual(needSpace ? itemTextBuilder.append(SPACE) : itemTextBuilder, qual);
            }
            
            return itemTextBuilder.toString();
        }
        return "<null>"; // NOI18N;
    }
    
    private CharSequence getVariableText(CsmVariable var) {
        CharSequence itemText = var.getText();
        if (CsmUtilities.isAutoType(var.getType())) {
            DisplayResolvedTypeHandler handler = new DisplayResolvedTypeHandler();
            CsmExpressionResolver.resolveType(
                    var.getName(), 
                    var.getContainingFile(), 
                    var.getType().getEndOffset(),
                    null,
                    handler
            );
            if (handler.typeText != null) {
                return handler.typeText;
            }
        }
        return itemText;
    }
    
    private CharSequence getFunctionText(CsmFunction fun) {
        StringBuilder txt = new StringBuilder();
        if (CsmKindUtilities.isMethod(fun)) {
            if (((CsmMethod) CsmBaseUtilities.getFunctionDeclaration(fun)).isVirtual()) {
                txt.append("virtual "); // NOI18N
            }
        }
        txt.append(fun.getReturnType().getText()).append(' '); // NOI18N
        // NOI18N
        txt.append(fun.getName());
        txt.append('(');
        @SuppressWarnings("unchecked")
        Iterator<CsmParameter> params = fun.getParameters().iterator();
        while(params.hasNext()) {
            CsmParameter param = params.next();
            txt.append(param.getText());
            if (params.hasNext()) {
                txt.append(", "); // NOI18N
            }
        }
        txt.append(')');
        if (CsmKindUtilities.isMethod(fun)) {
            CsmMethod mtd  = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration(fun);
            if (mtd.isConst()) {
                txt.append(" const"); // NOI18N
            }
            if (mtd.isAbstract()) {
                txt.append(" = 0"); // NOI18N
            }
        }
        return txt.toString();
    }
    
    private static boolean isSpaceRequired(Qualificator current, Qualificator next) {
        return Qualificator.CONST.equals(current) || Qualificator.CONST.equals(next);
    }    

    private static String getHtmlizedString(String key, CharSequence value) {
        return getString(key, htmlize(value));
    }

    private static String getHtmlizedString(String key, CharSequence value1, CharSequence value2) {
        return getString(key, htmlize(value1), htmlize(value2));
    }

    private static String getHtmlizedString(String key, CharSequence value1, CharSequence value2, CharSequence value3, CharSequence value4) {
        return getString(key, htmlize(value1), htmlize(value2), htmlize(value3), htmlize(value4));
    }

    private static String getString(String key, CharSequence value) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value);
    }    
    
    private static String getString(String key, CharSequence value1, CharSequence value2) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, value1, value2);
    } 
    
    private static String getString(String key, CharSequence value1, CharSequence value2, CharSequence value3, CharSequence value4) {
        return NbBundle.getMessage(CsmDisplayUtilities.class, key, new Object[] {value1, value2, value3, value4});
    }     
    
    private static StringBuilder appendQual(StringBuilder sb, Qualificator qual) {
        for (CppTokenId token : qual.getTokens()) {
            sb.append(token.fixedText());
        }
        return sb;
    }    
    
    private class DisplayResolvedTypeHandler implements CsmExpressionResolver.ResolvedTypeHandler {
        
        public CharSequence typeText;

        @Override
        public void process(CsmType resolvedType) {
            typeText = getTypeText(resolvedType, true, false);
        }        
    }
    
    private class SpecParamsTextProvider implements CsmSpecializationParamTextProvider {
        
        private final boolean expandInstantiations;
        
        private final boolean evaluateExpressions;

        public SpecParamsTextProvider(boolean expandInstantiations, boolean evaluateExpressions) {
            this.expandInstantiations = expandInstantiations;
            this.evaluateExpressions = evaluateExpressions;
        }
        
        @Override
        public CharSequence getSpecParamText(CsmSpecializationParameter param, CsmType paramType, List<CsmInstantiation> context) {
            if (CsmKindUtilities.isTypeBasedSpecalizationParameter(param)) {
                if (expandInstantiations) {
                    return getTypeText(paramType, expandInstantiations, evaluateExpressions);
                } else {
                    return paramType.getCanonicalText();
                }
            }
            if (CsmKindUtilities.isExpressionBasedSpecalizationParameter(param)) {
                if (evaluateExpressions) {
                    // TODO: support evaluate expressions
                    return param.getText();
                } else {
                    return param.getText();
                }
            }
            if (CsmKindUtilities.isVariadicSpecalizationParameter(param)) {
                // TODO: expand variadics
                return param.getText();
            }
            return ""; // NOI18N
        }        
    }
    
    private static class DisplayTypeInfoCollector implements Predicate<CsmType> {
        
        public final TypeInfoCollector infoCollector = new TypeInfoCollector();
        
        private final SmartTypeUnrollPredicate smartPredicate = new SmartTypeUnrollPredicate();
        
        @Override
        public boolean check(CsmType value) {
            infoCollector.check(value);
            return smartPredicate.check(value);
        }
    }    
}
