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

package org.netbeans.modules.cnd.api.model.util;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmClassifierBasedTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmClosureClassifier;
import org.netbeans.modules.cnd.api.model.CsmClosureType;
import org.netbeans.modules.cnd.api.model.CsmCompoundClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmErrorDirective;
import org.netbeans.modules.cnd.api.model.CsmExpressionBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerClassifier;
import org.netbeans.modules.cnd.api.model.CsmFunctional;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInitializerListContainer;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModule;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProgram;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypeBasedSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariadicSpecializationParameter;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;


/**
 * Utulity functions to prevent using of "instanceof" on CsmObjects for 
 * determining type/kind of Csm element
 *
 */
public class CsmKindUtilities {

    private CsmKindUtilities() {
        
    }

    public static boolean isProject(Object obj) {
        return obj instanceof CsmProject;
    }
    
    public static boolean isCsmObject(Object obj) {
        if (obj instanceof CsmObject) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidable(CsmObject obj) {
        if (obj instanceof CsmValidable) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isQualified(CsmObject obj) {
        if (obj instanceof CsmQualifiedNamedElement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isDeclaration(CsmObject obj) {
        if (obj instanceof CsmDeclaration) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isBuiltIn(CsmObject obj) {
        if (isDeclaration(obj)) {
            return ((CsmDeclaration)obj).getKind() == CsmDeclaration.Kind.BUILT_IN;
        } else {
            return false;
        }
    }
    
    public static boolean isInstantiation(CsmObject obj) {
        return obj instanceof CsmInstantiation;
    }

    public static boolean isSpecialization(CsmObject obj) {
        return (obj instanceof CsmTemplate) && ((CsmTemplate)obj).isSpecialization();
    }
    
    public static boolean isTemplateInstantiation(CsmObject obj) {
        return obj instanceof CsmInstantiation;
    }
    
    public static boolean isTemplateParameterType(CsmObject obj) {
        return (obj instanceof CsmTemplateParameterType);
    }
    
    public static boolean isTemplate(CsmObject obj) {
        return (obj instanceof CsmTemplate) && ((CsmTemplate)obj).isTemplate();
    }
    
    public static boolean isTemplateParameter(CsmObject obj) {
        return (obj instanceof CsmTemplateParameter);
    }

    public static boolean isClassifierBasedTemplateParameter(CsmObject obj) {
        return (obj instanceof CsmClassifierBasedTemplateParameter);
    }

    public static boolean isSpecalizationParameter(CsmObject obj) {
        return (obj instanceof CsmSpecializationParameter);
    }

    public static boolean isTypeBasedSpecalizationParameter(CsmObject obj) {
        return (obj instanceof CsmTypeBasedSpecializationParameter);
    }

    public static boolean isExpressionBasedSpecalizationParameter(CsmObject obj) {
        return (obj instanceof CsmExpressionBasedSpecializationParameter);
    }

    public static boolean isVariadicSpecalizationParameter(CsmObject obj) {
        return (obj instanceof CsmVariadicSpecializationParameter);
    }
    
    public static boolean isFunctionExplicitInstantiation(CsmObject obj) {
        if (isDeclaration(obj)) {
            return ((CsmDeclaration)obj).getKind() == CsmDeclaration.Kind.FUNCTION_INSTANTIATION;
        } else {
            return false;
        }
    }
    
    public static boolean isCastOperator(CsmObject obj) {
        return isFunction(obj) && 
               ((CsmFunction) obj).isOperator() && 
               ((CsmFunction) obj).getOperatorKind() == CsmFunction.OperatorKind.CONVERSION;
    }
    
    public static boolean isFunctionPointerType(CsmObject obj) {
        return (obj instanceof CsmFunctionPointerType);
    }
    
    public static boolean isClosureType(CsmObject obj) {
        return (obj instanceof CsmClosureType);
    }

    public static boolean isType(CsmObject obj) {
        if (obj instanceof CsmType) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTypedef(CsmObject obj) {
        if (isDeclaration(obj)) {
            return ((CsmDeclaration)obj).getKind() == CsmDeclaration.Kind.TYPEDEF;
        } else {
            return false;
        }
    }
    
    public static boolean isTypeAlias(CsmObject obj) {
        if (isDeclaration(obj)) {
            return ((CsmDeclaration)obj).getKind() == CsmDeclaration.Kind.TYPEALIAS;
        } else {
            return false;
        }
    }    
    
    public static boolean isTypedefOrTypeAlias(CsmObject obj) {
        return isTypedef(obj) || isTypeAlias(obj);
    }        
    
    public static boolean isStatement(CsmObject obj) {
        if (obj instanceof CsmStatement) {
            return true;
        } else {
            return false;
        }          
    }

    public static boolean isDeclarationStatement(CsmObject obj) {
        if (obj instanceof CsmDeclarationStatement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isCompoundStatement(CsmObject obj) {
        if (isStatement(obj)) {
            return ((CsmStatement)obj).getKind() == CsmStatement.Kind.COMPOUND;
        } else {
            return false;
        }          
    }
    
    public static boolean isReturnStatement(CsmObject obj) {
        if (isStatement(obj)) {
            return ((CsmStatement)obj).getKind() == CsmStatement.Kind.RETURN;
        } else {
            return false;
        }          
    }    

    public static boolean isTryCatchStatement(CsmObject obj) {
        if (isStatement(obj)) {
            return ((CsmStatement)obj).getKind() == CsmStatement.Kind.TRY_CATCH;
        } else {
            return false;
        }
    }
    
    public static boolean isGotoStatement(CsmObject obj) {
        return (obj instanceof CsmGotoStatement);
    }

    public static boolean isLabel(CsmObject obj) {
        return (obj instanceof CsmLabel);
    }

    public static boolean isOffsetable(Object obj) {
        if (obj instanceof CsmOffsetable) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isOffsetableDeclaration(Object obj) {
        if (obj instanceof CsmOffsetableDeclaration) {
            return true;
        } else {
            return false;
        }
    }
      
    public static boolean isNamedElement(CsmObject obj) {
        if (obj instanceof CsmNamedElement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isNamedElement(Object obj) {
        if (obj instanceof CsmNamedElement) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEnum(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.ENUM; 
        } else {
            return false;
        }
    }
    
    public static boolean isEnumerator(Object obj) {
        if (obj instanceof CsmEnumerator) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isClassifier(CsmObject obj) {
        if (obj instanceof CsmClassifier) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isCompoundClassifier(CsmObject obj) {
        if (obj instanceof CsmCompoundClassifier) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isClass(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return 
                kind == CsmDeclaration.Kind.CLASS 
                || kind == CsmDeclaration.Kind.STRUCT 
                || kind == CsmDeclaration.Kind.UNION;
        } else {
            return false;
        }
    }

    public static boolean isUnion(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.UNION;
        } else {
            return false;
        }
    }
    
    public static boolean isClassForwardDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION;
        } else {
            return false;
        }
    }

    public static boolean isEnumForwardDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration) obj).getKind();
            return kind == CsmDeclaration.Kind.ENUM_FORWARD_DECLARATION;
        } else {
            return false;
        }
    }

    public static boolean isScope(CsmObject obj) {
        if (obj instanceof CsmScope) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isScopeElement(CsmObject obj) {
        if (obj instanceof CsmScopeElement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isFunctional(CsmObject obj) {
        return (obj instanceof CsmFunctional);
    }    
    
    /*
     * checks if object is function declaration or function definition
     * it's safe to cast to CsmFunction
     */
    public static boolean isFunction(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION ||
                    kind == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                    kind == CsmDeclaration.Kind.FUNCTION_LAMBDA ||
                    kind == CsmDeclaration.Kind.FUNCTION_FRIEND ||
                    kind == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION;
        } else {
            return false;
        }
    }   
    
    public static boolean isFunctionPointerClassifier(CsmObject obj) {
        return (obj instanceof CsmFunctionPointerClassifier);
    }
    
    public static boolean isClosureClassifier(CsmObject obj) {
        return (obj instanceof CsmClosureClassifier);
    }

    public static boolean isLambda(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION_LAMBDA;
        } else {
            return false;
        }
    }
    
    public static boolean isParameter(CsmObject obj) {
        return (obj instanceof CsmParameter);
    }

    /*
     * checks if object is function operator
     */
    public static boolean isOperator(CsmObject obj) {
        if (isFunction(obj)) {
            return ((CsmFunction)obj).isOperator();
        }
        return false;
    }   

    /*
     * checks if object is function declaration
     * it's safe to cast to CsmFunction which is not CsmFunctionDefinition
     */
    public static boolean isFunctionDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION ||
                   kind == CsmDeclaration.Kind.FUNCTION_FRIEND;
        } else {
            return false;
        }
    }   
    
    /*
     * checks if object is function definition
     * it's safe to cast to CsmFunction or CsmFunctionDefinition
     */
    public static boolean isFunctionDefinition(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                    kind == CsmDeclaration.Kind.FUNCTION_LAMBDA ||
                    kind == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION;
        } else {
            return false;
        }
    } 
    
    public static boolean isFile(CsmObject obj) {
        if (obj instanceof CsmFile) {
            return true;
        } else {
            return false;
        }
    }  
    
    public static boolean isInheritance(CsmObject obj) {
        if (obj instanceof CsmInheritance) {
            return true;
        } else {
            return false;
        }
    } 

    public static boolean isNamespace(Object obj) {
        if (obj instanceof CsmNamespace) {
            return true;
        } else {
            return false;
        }
    } 
    
    public static boolean isNamespaceDefinition(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION;
        } else {
            return false;
        }
    }   
    
    public static boolean isClassMember(CsmObject obj) {
        if (obj instanceof CsmMember) {
	    if (isClass(obj) ) {
		return isClass(((CsmClass) obj).getScope());
	    } else {
		return true;
	    }   
        } else {
            return false;
        }
    }    
    
    public static boolean isVariable(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.VARIABLE ||
                   kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ;
        } else {
            return false;
        }        
    }

    public static boolean isVariableDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.VARIABLE ;
        } else {
            return false;
        }        
    }

    public static boolean isVariableDefinition(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ;
        } else {
            return false;
        }        
    }
    
    /* 
     * Local variable is CsmVariable object declared through
     * CsmDeclaration as part of CsmDeclarationStatement's declarators list
     * or it's CsmParameter
     * for file local variables and global variables return false
     */
    public static boolean isLocalVariable(CsmObject obj) {
        if (isVariable(obj)) {
            // can't be class member
            if (isClassMember(obj)) {
                return false;
            }
            // check scope
            CsmScope scope = ((CsmVariable)obj).getScope();
            return !isFile(scope) && !isNamespace(scope);
        } else {
            return false;
        }
    }  
    
    public static boolean isFileLocalVariable(CsmObject obj) {
        if (isVariable(obj)) {
            return isFile(((CsmVariable)obj).getScope());
        } else {
            return false;
        }
    }      

    public static boolean isFileLocalFunction(CsmObject obj) {
        if (isFunction(obj)) {
            return isFile(((CsmFunction)obj).getScope());
        } else {
            return false;
        }
    }

    public static boolean isGlobalVariable(CsmObject obj) {
        if (isVariable(obj)) {
            // global variable has scope - namespace
            return isNamespace(((CsmVariable)obj).getScope());
        } else {
            return false;
        }
    }   
    
    public static boolean isParamVariable(CsmObject obj) {
        if (isVariable(obj)) {
            assert (!(obj instanceof CsmParameter) || !isClassMember(obj)) : "parameter is not class member";
            return obj instanceof CsmParameter;
        } else {
            return false;
        }
    }  
    
    public static boolean isField(CsmObject obj) {
        if (isVariable(obj)) {
            return isClassMember(obj);
        } else {
            return false;
        }
    }  
    
    public static boolean isGlobalFunction(CsmObject obj) {
        if (isFunction(obj)) {
            return !isClassMember(CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj));
        } else {
            return false;
        }
    }       
    
    /**
     * checks if passed object is method definition or method declaration
     * after this check it is safe to cast only to CsmFunction (not CsmMethod),
     * but it is guaranteed that CsmBaseUtilities.getFunctionDeclaration((CsmFunction) obj) is CsmMethod
     * @see isMethodDeclaration
     */
    public static boolean isMethod(CsmObject obj) {
        if (isFunction(obj)) {
            return isClassMember(CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj));
        } else {
            return false;
        }
    }
    
    /**
     * checks if passed object is method declaration;
     * after this check it is safe to cast to CsmMethod
     */
    public static boolean isMethodDeclaration(CsmObject obj) {
        if (isFunction(obj)) {
            return isClassMember(obj);
        } else {
            return false;
        }
    }     

    /**
     * checks if passed object is method definition;
     * after this check it is safe to cast to CsmFunctionDefinition (not CsmMethod)
     * @see isMethodDeclaration
     */
    public static boolean isMethodDefinition(CsmObject obj) {
        if (isFunctionDefinition(obj)) {
            return isClassMember(CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj));
        } else {
            return false;
        }
    }     

    /**
     * checks if passed object is constructor definition or declaration;
     * after this check it is safe to cast to CsmFunction or CsmInitializerListContainer
     */    
    public static boolean isConstructor(CsmObject obj) {
        return obj instanceof CsmInitializerListContainer;
    }   
    
    public static boolean isDestructor(CsmObject obj) {
        if (isMethod(obj)) {
            CsmFunction funDecl = CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj);
            // check constructor by ~ at the begining of the name
            if (funDecl != null && funDecl.getName().length() > 0) {
                return funDecl.getName().charAt(0) == '~'; //NOI18N
            }
        } else if (isFunctionDefinition(obj)) {
            CsmFunction fun = (CsmFunction) obj;
            // check constructor by ~ at the begining of the name
            if (fun.getName().length() > 0) {
                return fun.getName().charAt(0) == '~'; //NOI18N
            }
        }
        return false;
    }     

    public static boolean isExpression(CsmObject obj) {
        return obj instanceof CsmExpression;
    }
    
    public static boolean isErrorDirective(CsmObject obj) {
        return obj instanceof CsmErrorDirective;
    }
    
    public static boolean isMacro(CsmObject obj) {
        return obj instanceof CsmMacro;
    }
    
    public static boolean isInclude(CsmObject obj) {
        return obj instanceof CsmInclude;
    }

    public static boolean isUsing(CsmObject obj) {
        return (obj instanceof CsmUsingDeclaration) ||
               (obj instanceof CsmUsingDirective);
    }

    public static boolean isNamespaceAlias(CsmObject obj) {
        return obj instanceof CsmNamespaceAlias;        
    }
    
    public static boolean isUsingDirective(CsmObject obj) {
        return obj instanceof CsmUsingDirective;
    }

    public static boolean isUsingDeclaration(CsmObject obj) {
        return obj instanceof CsmUsingDeclaration;
    }

    public static boolean isFriend(CsmObject obj) {
        return obj instanceof CsmFriend;
    }

    public static boolean isFriendClass(CsmObject obj) {
        return obj instanceof CsmFriendClass;
    }

    public static boolean isFriendMethod(CsmObject obj) {
        return obj instanceof CsmFriendFunction;
    }

    public static boolean isExternVariable(CsmDeclaration decl) {
        if (isVariable(decl)) {
            return ((CsmVariable)decl).isExtern();
        }
        return false;
    }

    // Fortran

    public static boolean isProgram(CsmObject obj) {
        return obj instanceof CsmProgram;
    }

    public static boolean isModule(CsmObject obj) {
        return obj instanceof CsmModule;
    }

}
