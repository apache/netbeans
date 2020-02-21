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

package org.netbeans.modules.cnd.modelimpl.csm;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.util.MapHierarchy;
import org.openide.util.CharSequences;

/**
 * Common functions related with templates.
 * Typically used by CsmClass ans CsmFunction, which has to implement CsmTemplate,
 * but 
 */
public class TemplateUtils {
    
    private static final String UNNAMED_TEMPLATE_PARAMETER = "__nb_unnamed_param_"; //NOI18N

//    public static final byte MASK_TEMPLATE = 0x01;
//    public static final byte MASK_SPECIALIZATION = 0x02;

    public static CharSequence getSpecializationSuffix(AST qIdToken, List<CsmTemplateParameter> parameters) {
	StringBuilder sb  = new StringBuilder();
	for( AST child = qIdToken.getFirstChild(); child != null; child = child.getNextSibling() ) {
	    if( child.getType() == CPPTokenTypes.LESSTHAN ) {
		addSpecializationSuffix(child, sb, parameters);
		break;
	    }
	}
	return sb;
    }
    
    // in class our parser skips LESSTHAN symbols in templates...
    public static CharSequence getClassSpecializationSuffix(AST qIdToken, List<CsmTemplateParameter> parameters) {
	StringBuilder sb  = new StringBuilder();
        addSpecializationSuffix(qIdToken.getFirstChild(), sb, parameters);
	return sb;
    }
    
    public static final String TYPENAME_STRING = "class"; //NOI18N

    public static void addSpecializationSuffix(AST firstChild, StringBuilder res, List<CsmTemplateParameter> parameters) {
        addSpecializationSuffix(firstChild, res, parameters, false);
    }
    
    public static void addSpecializationSuffix(AST firstChild, StringBuilder res, List<CsmTemplateParameter> parameters, boolean checkForSpecialization) {
        int depth = 0;
        int paramsNumber = 0;
        StringBuilder sb = new StringBuilder(res); // NOI18N
        for (AST child = firstChild; child != null; child = child.getNextSibling()) {
            if (child.getType() == CPPTokenTypes.LESSTHAN) {
                depth++;
            }
                
            if (CPPTokenTypes.CSM_START <= child.getType() && child.getType() <= CPPTokenTypes.CSM_END) {
                AST grandChild = child.getFirstChild();
                if (grandChild != null) {
                    addSpecializationSuffix(grandChild, sb, parameters);
                    paramsNumber++;
                }
            } else if (child.getType() == CPPTokenTypes.LITERAL_template) {
                sb.append(AstUtil.getText(child));
                sb.append('<');
                AST grandChild = child.getFirstChild();
                if (grandChild != null) {
                    addSpecializationSuffix(grandChild, sb, parameters);
                }
                addGREATERTHAN(sb);
                sb.append(' ');
                paramsNumber++;
            } else if (child.getType() == CPPTokenTypes.GREATERTHAN) {
                addGREATERTHAN(sb);
                depth--;
                if (depth == 0) {
                    break;
                }
            } else {
                CharSequence text = AstUtil.getText(child);
                if (parameters != null) {
                    for (CsmTemplateParameter param : parameters) {
                        if (CharSequences.comparator().compare(param.getName(),text)==0) {
                            text = TYPENAME_STRING;
                            paramsNumber++;
                        }
                    }
                }
                assert text != null;
                assert text.length() > 0;
                if (sb.length() > 0) {
                    if (Character.isJavaIdentifierPart(sb.charAt(sb.length() - 1))) {
                        if (Character.isJavaIdentifierPart(text.charAt(0))) {
                            sb.append(' ');
                        }
                    }
                }
                sb.append(text);
            }
        }
        if(!checkForSpecialization || parameters == null || paramsNumber != parameters.size()) {
            res.append(sb.substring(res.length()));
        }
    }

    public static void addGREATERTHAN(StringBuilder sb) {
        // IZ#179276
        if (sb.length() > 0 && sb.charAt(sb.length() - 1) == '>') {
            sb.append(' ');
        }
        sb.append('>');
    }
    
    public static boolean isPartialClassSpecialization(AST ast) {
	if( ast.getType() == CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION ) {
	    for( AST node = ast.getFirstChild(); node != null; node = node.getNextSibling() ) {
		if( node.getType() == CPPTokenTypes.CSM_QUALIFIED_ID ) {
		    for( AST child = node.getFirstChild(); child != null; child = child.getNextSibling() ) {
			if( child.getType() == CPPTokenTypes.LESSTHAN ) {
			    return true;
			}
		    }
		}
	    }
	}
	return false;
    }

    public static AST getTemplateStart(AST ast) {
        for (AST child = ast; child != null; child = child.getNextSibling()) {
            if (child.getType() == CPPTokenTypes.LITERAL_template) {
                return child;
            }
        }
        return null;
    }
    
    public static List<CsmTemplateParameter> getTemplateParameters(AST ast, CsmFile file, CsmScope scope, boolean global) {
        assert (ast != null && ast.getType() == CPPTokenTypes.LITERAL_template);
        List<CsmTemplateParameter> res = new ArrayList<>();
        AST parameterStart = null;
        boolean variadic = false;
        boolean inDefaultValue = false;
        
        int unnamedCount = 0; // number of unnamed parameters
        
        for (AST child = ast.getFirstChild(); child != null; child = child.getNextSibling()) {
            switch (child.getType()) {
                case CPPTokenTypes.LITERAL_class:
                case CPPTokenTypes.LITERAL_typename:
                    if (!inDefaultValue) {
                        parameterStart = child;
                        variadic = false;
                    }
                    break;
                case CPPTokenTypes.ELLIPSIS:
                    variadic = true;
                    break;
                case CPPTokenTypes.IDENT:
                    // now create parameter
                    AST fakeAST = null;
                    if (parameterStart == null) {
                        fakeAST = parameterStart = child;
                    } else {
                        // Fix for IZ#138099: unresolved identifier for functions' template parameter.
                        // The fakeAST is needed to initialize TemplateParameter with correct offsets.
                        // Without it TemplateParameter would span either "class"/"typename" keyword
                        // or parameter name, but not both.
                        fakeAST = AstUtil.createAST(parameterStart, child);
                    }
                    res.add(new TemplateParameterImpl(
                            parameterStart, 
                            AstUtil.getText(child), 
                            OffsetableBase.getStartOffset(fakeAST),
                            OffsetableBase.getEndOffset(fakeAST),                            
                            file, 
                            scope, 
                            variadic, 
                            global
                    ));
                    parameterStart = null;
                    break;
                case CPPTokenTypes.CSM_PARAMETER_DECLARATION:
                    // now create parameter
                    parameterStart = child;
                    AST varDecl = child.getFirstChild();
                    // skip qualifiers
                    // IZ#156679 : Constant in template is highlighted as invalid identifier
                    if (varDecl != null) {
                        varDecl = AstRenderer.getFirstSiblingSkipQualifiers(varDecl);
                    }
                    // skip "typename"
                    if (varDecl != null && varDecl.getType() == CPPTokenTypes.LITERAL_typename) {
                        varDecl = varDecl.getNextSibling();
                    }
                    if (varDecl != null && (varDecl.getType() == CPPTokenTypes.LITERAL_enum || 
                                            varDecl.getType() == CPPTokenTypes.LITERAL_struct || 
                                            varDecl.getType() == CPPTokenTypes.LITERAL_class ||
                                            varDecl.getType() == CPPTokenTypes.LITERAL_union)) 
                    {
                        varDecl = varDecl.getNextSibling();
                        if(varDecl == null || (varDecl.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND && varDecl.getType() != CPPTokenTypes.CSM_QUALIFIED_ID)) {
                            break;                                    
                        }                                
                    }
                    while (varDecl != null && varDecl.getNextSibling() != null && varDecl.getNextSibling().getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                        varDecl = varDecl.getNextSibling();
                    }                    
                    // check for existense of CSM_VARIABLE_DECLARATION branch
                    if (varDecl != null && varDecl.getNextSibling() != null &&
                            varDecl.getNextSibling().getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION) {
                        // CSM_VARIABLE_DECLARATION branch has priority
                        varDecl = varDecl.getNextSibling();
                    }
                    if (varDecl != null) {
                        switch (varDecl.getType()) {
                            case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                                AST pn = varDecl.getFirstChild();
                                if (pn != null && pn.getType() == CPPTokenTypes.ELLIPSIS) {
                                    pn = pn.getNextSibling();
                                }
                                if (pn != null) {
                                    res.add(new TemplateParameterImpl(parameterStart, AstUtil.getText(pn), file, scope, variadic, global));
                                }
                                break;
                            case CPPTokenTypes.CSM_TYPE_BUILTIN:
                            case CPPTokenTypes.CSM_TYPE_COMPOUND: {
                                boolean added = false;
                                for(AST p = varDecl.getFirstChild(); p != null; p = p.getNextSibling()){
                                    if (p.getType() == CPPTokenTypes.IDENT) {
                                       res.add(new TemplateParameterImpl(parameterStart, AstUtil.getText(p), file, scope, variadic, global));
                                       added = true;
                                       break;
                                    }
                                }
                                if (!added) {
                                    res.add(new TemplateParameterImpl(parameterStart, UNNAMED_TEMPLATE_PARAMETER + unnamedCount, file, scope, variadic, global)); 
                                    unnamedCount++;
                                }
                                break;
                            }
                        }
                    }
                    parameterStart = null;
                    break;
                case CPPTokenTypes.CSM_TEMPLATE_TEMPLATE_PARAMETER:
                    parameterStart = child;
                    for (AST paramChild = child.getFirstChild(); paramChild != null; paramChild = paramChild.getNextSibling()) {
                        if (paramChild.getType() == CPPTokenTypes.IDENT) {
                            // IZ 141842 : If template parameter declared as a template class, its usage is unresolved
                            // Now all IDs of template template parameter are added to template parameters of template.
                            // When CsmClassifierBasedTemplateParameter will be finished, this should be replaced. 
                            res.add(new TemplateParameterImpl(parameterStart, AstUtil.getText(paramChild), file, scope, variadic, global));
                        }
                    }
                    parameterStart = null;
                    break;
                case CPPTokenTypes.COMMA:
                case CPPTokenTypes.ASSIGNEQUAL:
                    if (parameterStart != null) {
                        // Unnamed parameters
                        res.add(new TemplateParameterImpl(parameterStart, UNNAMED_TEMPLATE_PARAMETER + unnamedCount, file, scope, variadic, global)); 
                        unnamedCount++;
                    }
                    inDefaultValue = (child.getType() == CPPTokenTypes.ASSIGNEQUAL); // parsing " = <type or expr>" part
                    parameterStart = null;
                    break;
            }
        }
        if (parameterStart != null) {
            // Unnamed parameter
            res.add(new TemplateParameterImpl(parameterStart, UNNAMED_TEMPLATE_PARAMETER + unnamedCount, file, scope, variadic, global)); 
            unnamedCount++;
        }        
        return res;
    }

    public static List<CsmSpecializationParameter> getSpecializationParameters(AST ast, CsmFile file, CsmScope scope, boolean global) {
        assert (ast != null);
        List<CsmSpecializationParameter> res = new ArrayList<>();
        AST start;
        for (start = ast.getFirstChild(); start != null; start = start.getNextSibling()) {
            if (start.getType() == CPPTokenTypes.LESSTHAN) {
                start = start.getNextSibling();
                break;
            }
        }
        if (start != null) {
            AST ptr = null;
            AST type = null;
            for (AST child = start; child != null; child = child.getNextSibling()) {
                switch (child.getType()) {
                    case CPPTokenTypes.CSM_PTR_OPERATOR:
                        ptr = child;
                        break;
                    case CPPTokenTypes.CSM_TYPE_BUILTIN:
                    case CPPTokenTypes.CSM_TYPE_COMPOUND:
                        type = child;
                        break;
                    case CPPTokenTypes.CSM_EXPRESSION:
                        res.add(ExpressionBasedSpecializationParameterImpl.create(ExpressionStatementImpl.create(child, file, scope),
                                file, OffsetableBase.getStartOffset(child), OffsetableBase.getEndOffset(child)));
                        break;
                    case CPPTokenTypes.COMMA:
                    case CPPTokenTypes.GREATERTHAN:
                        if (type != null) {
                            res.add(new TypeBasedSpecializationParameterImpl(
                                TemplateUtils.checkTemplateType(TypeFactory.createType(type, file, ptr, 0, null, scope, true, false), scope),
                                scope,
                                file, 
                                OffsetableBase.getStartOffset(type), 
                                OffsetableBase.getEndOffset(type)
                            ));
                        }
                        type = null;
                        ptr = null;
                        break;
                }
            }
        }
        return res;
    }
    
    public static CsmType checkTemplateType(CsmType type, CsmObject scope) {
        return checkTemplateType(type, scope, (List<CsmTemplateParameter>) null);
    }
    
    public static CsmType checkTemplateType(CsmType type, CsmObject scope, TemplateDescriptor templateDescriptor) {
        return checkTemplateType(type, scope, templateDescriptor != null ? templateDescriptor.getTemplateParameters() : null);
    }
    
    public static CsmType checkTemplateType(CsmType type, CsmObject scope, List<CsmTemplateParameter> additionalParams) {
        if (!(type instanceof TypeImpl)) {            
            return type;
        }

        if (type instanceof NestedType) {
            NestedType nestedType = (NestedType) type;
            type = NestedType.create(checkTemplateType(nestedType.getParent(), scope, additionalParams), nestedType);
        }
        
        // Check return type in function pointer
        if (CsmKindUtilities.isFunctionPointerType(type)) {
            TypeFunPtrImpl fpt = (TypeFunPtrImpl) type;
            CsmType returnType = fpt.getReturnType();
            CsmType newReturnType = checkTemplateType(returnType, scope, additionalParams);
            if (newReturnType != returnType) {
                fpt.setReturnType(newReturnType);
            }
        }
        
        // Check instantiation parameters
        if (type.isInstantiation()) {
            TypeImpl typeImpl = (TypeImpl) type;
            List<CsmSpecializationParameter> params = typeImpl.getInstantiationParams();
            for (int i = 0; i < params.size(); i++) {
                CsmSpecializationParameter instParam = params.get(i);
                if (CsmKindUtilities.isTypeBasedSpecalizationParameter(instParam)) {
                    CsmType newType = checkTemplateType(((CsmTypeBasedSpecializationParameter) instParam).getType(), scope, additionalParams);
                    if (newType != instParam) {
                        if (CsmKindUtilities.isScope(scope)) {
                            params.set(i, new TypeBasedSpecializationParameterImpl(newType, (CsmScope) scope));
                        } else if (CsmKindUtilities.isScopeElement(scope)) {
                            params.set(i, new TypeBasedSpecializationParameterImpl(newType, ((CsmScopeElement) scope).getScope()));
                        } else {
                            params.set(i, new TypeBasedSpecializationParameterImpl(newType, null));
                        }                        
                    }
                }
            }
        }
        
        // first check additional params
        if (additionalParams != null) {
            CsmType paramType = checkTemplateType(type, additionalParams);
            if (paramType != type) {
                return paramType;
            }
        }        
        
        // then check scope and super classes if needed
        while (scope != null) {
            if (CsmKindUtilities.isTemplate(scope)) {
                CsmType paramType = checkTemplateType(type, ((CsmTemplate)scope).getTemplateParameters());
                if (paramType != type) {
                    return paramType;
                }
            }
            // then check class or super class
            if (scope instanceof CsmScopeElement) {
                scope = ((CsmScopeElement)scope).getScope();
            } else {
                break;
            }
        }        
        
        return type;
    }   

    public static MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> gatherMapping(CsmInstantiation inst) {
            MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapHierarchy = new MapHierarchy<>(inst.getMapping());
            
            while(CsmKindUtilities.isInstantiation(inst.getTemplateDeclaration())) {
                inst = (CsmInstantiation) inst.getTemplateDeclaration();
                mapHierarchy.push(inst.getMapping());
            }
            
            return mapHierarchy;
    }
    
    public static MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> gatherMapping(List<CsmInstantiation> instantiations) {
            MapHierarchy<CsmTemplateParameter, CsmSpecializationParameter> mapHierarchy = new MapHierarchy<>();
            
            for (CsmInstantiation instantiation : instantiations) {
                mapHierarchy.push(instantiation.getMapping());
            }
            
            return mapHierarchy;
    }    

    public static boolean isTemplateQualifiedName(String name) {
        return name.contains("<"); // NOI18N
    }

    public static String getTemplateQualifiedNameWithoutSiffix(String name) {
        return name.replaceAll("<.*", ""); // NOI18N
    }
    
    private static CsmType checkTemplateType(CsmType type, List<CsmTemplateParameter> params) {
        if (params != null && !params.isEmpty()) {
            CharSequence classifierText = ((TypeImpl)type).getClassifierText();
            for (CsmTemplateParameter param : params) {
                if (CharSequences.comparator().compare(param.getName(), classifierText) == 0) {
                    return new TemplateParameterTypeImpl(type, param);
                }
            }
        }     
        return type;
    }        

    private TemplateUtils() {
    }
}
