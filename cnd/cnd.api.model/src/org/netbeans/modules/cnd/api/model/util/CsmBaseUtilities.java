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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnumForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.spi.model.CsmBaseUtilitiesProvider;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 *
 */
public class CsmBaseUtilities {

    private static final int MAX_DEPTH = 20;


    /** Creates a new instance of CsmBaseUtilities */
    private CsmBaseUtilities() {
    }

    public static boolean isValid(CsmObject obj) {
        if (CsmKindUtilities.isValidable(obj)) {
            return ((CsmValidable)obj).isValid();
        } else {
            return obj != null;
        }
    }

    public static boolean isUnresolved(Object obj) {
        return CsmBaseUtilitiesProvider.getDefault().isUnresolved(obj);
    }

    public static boolean isPointer(CsmType type) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (CsmKindUtilities.isFunctionPointerType(type)) {
                return (type.getPointerDepth() > 0);
            }
            if (type.isPointer()) {
                return true;
            }
            CsmClassifier cls = type.getClassifier();
            if (CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return false;
    }

    public static int isReference(CsmType type) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (type.isReference()) {
                if (type.isRValueReference()) {
                    return CsmTypes.TypeDescriptor.RVALUE_REFERENCE;
                }
                return CsmTypes.TypeDescriptor.REFERENCE;
            }
            CsmClassifier cls = type.getClassifier();
            if (CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return 0;
    }

    public static CsmFunctionPointerType tryGetFunctionPointerType(CsmType type) {
        int iteration = MAX_DEPTH;
        while (type != null && iteration != 0) {
            if (CsmKindUtilities.isFunctionPointerType(type)) {
                return (CsmFunctionPointerType) type;
            }
            CsmClassifier cls = type.getClassifier();
            if (CsmKindUtilities.isTypedef(cls) || CsmKindUtilities.isTypeAlias(cls)) {
                CsmTypedef td = (CsmTypedef) cls;
                type = td.getType();
            } else {
                break;
            }
            iteration--;
        }
        return null;
    }
    
    public static CsmExpression tryGetDecltypeExpression(CsmType type) {
        return CsmBaseUtilitiesProvider.getDefault().getDecltypeExpression(type);
    }

    /**
     * Checks if variable has external linkage
     * @param var
     * @return true if variable has external linkage, false otherwise
     */
    public static boolean isGlobalVariable(CsmVariable var) {
        return CsmBaseUtilitiesProvider.getDefault().isGlobalVariable(var);
    }

    public static boolean isGlobalNamespace(CsmScope scope) {
        if (CsmKindUtilities.isNamespace(scope)) {
            return ((CsmNamespace)scope).isGlobal();
        }
        return false;
    }

    public static CsmScope getLastCommonScope(CsmScope first, CsmScope second) {
        if (first == null || second == null) {
            return null;
        }
        if (first.equals(second)) {
            return first;
        }
        int firstDepth = getScopeDepth(first);
        int secondDepth = getScopeDepth(second);
        while (firstDepth > secondDepth && CsmKindUtilities.isScopeElement(first)) {
            first = ((CsmScopeElement) first).getScope();
            --firstDepth;
        }
        while (firstDepth < secondDepth && CsmKindUtilities.isScopeElement(second)) {
            second = ((CsmScopeElement) second).getScope();
            --secondDepth;
        }
        if (firstDepth == secondDepth) {
            while (!Objects.equals(first, second) && CsmKindUtilities.isScopeElement(first) && CsmKindUtilities.isScopeElement(second)) {
                first = ((CsmScopeElement) first).getScope();
                second = ((CsmScopeElement) second).getScope();
            }
            if (Objects.equals(first, second)) {
                return first;
            }
        }
        return null;
    }
    
    public static Collection<CsmNamespace> getInlinedNamespaces(CsmNamespace ns, Collection<CsmProject> libs) {
        if (libs != null && !libs.isEmpty()) {
            List<CsmNamespace> inlinedNamespaces = new ArrayList<CsmNamespace>(ns.getInlinedNamespaces());
            for (CsmProject lib : libs) {
                CsmNamespace libNmsp = lib.findNamespace(ns.getQualifiedName());
                if (libNmsp != null) {
                    inlinedNamespaces.addAll(libNmsp.getInlinedNamespaces());
                }
            }
            List<CsmNamespace> indirectlyInlinedNamespaces = new ArrayList<CsmNamespace>();
            for (CsmProject lib : libs) {
                for (CsmNamespace inlinedNs : inlinedNamespaces) {
                    if (lib != inlinedNs.getProject()) {
                        CsmNamespace potentialInlinedNmsp = lib.findNamespace(inlinedNs.getQualifiedName());
                        // if it is marked as inlined, we do not need to add it, as it must already be in the list.
                        if (potentialInlinedNmsp != null && !potentialInlinedNmsp.isInline()) {
                            indirectlyInlinedNamespaces.add(potentialInlinedNmsp);
                        }
                    }
                }
            }
            inlinedNamespaces.addAll(indirectlyInlinedNamespaces);
            return inlinedNamespaces;
        }
        return ns.getInlinedNamespaces();
    }
    
    public static boolean isInlinedNamespace(CsmNamespace ns, Collection<CsmProject> libs) {
        if (!ns.isGlobal()) {
            if (ns.isInline()) {
                return true;
            }
            if (libs != null && !libs.isEmpty()) {
                for (CsmProject lib : libs) {
                    CsmNamespace libNmsp = lib.findNamespace(ns.getQualifiedName());
                    if (libNmsp != null && libNmsp.isInline()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean sameSignature(CsmFunction checkDecl, CsmFunction targetDecl) {
        // we treat const and non-const functions as the same
        CharSequence sigCheck = checkDecl.getSignature().toString().replace("const", "").trim(); // NOI18N // NOI18N
        CharSequence sigTarget = targetDecl.getSignature().toString().replace("const", "").trim(); // NOI18N // NOI18N
        if (sigCheck.equals(sigTarget)) {
            return true;
        }
        return false;
    }

    public static boolean isInlineFunction(CsmFunction fun) {
        if (fun.isInline()) {
            return true;
        }
        CsmScope outScope = fun.getScope();
        if (outScope == null || isGlobalNamespace(outScope)) {
            return false;
        } else {
            CsmFunction decl = CsmBaseUtilities.getFunctionDeclaration(fun);
            if (decl == null || !CsmKindUtilities.isMethod(fun)) {
                return false;
            } else {
                return outScope.equals(((CsmMethod)decl).getContainingClass());
            }
        }
    }

    public static boolean isStaticContext(CsmFunction fun) {
        assert (fun != null) : "must be not null";
        // static context is in global functions and static methods
        if (CsmKindUtilities.isGlobalFunction(fun)) {
            return true;
        } else {
            CsmFunction funDecl = getFunctionDeclaration(fun);
            if (CsmKindUtilities.isClassMember(funDecl)) {
                return ((CsmMember)funDecl).isStatic();
            }
        }
        return false;
    }

    private static boolean TRACE_XREF_REPOSITORY = Boolean.getBoolean("cnd.modelimpl.trace.xref.repository");

    public static CsmFunction getOperator(CsmClassifier cls, CsmFunction.OperatorKind opKind) {
        if (!CsmKindUtilities.isClass(cls)) {
            return null;
        }
        for (CsmMember member : ((CsmClass)cls).getMembers()) {
            if (CsmKindUtilities.isOperator(member)) {
                if (((CsmFunction)member).getOperatorKind() == opKind) {
                    return (CsmFunction)member;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param target
     * @return new CsmObject[] { declaration, definion }
     */
    public static CsmObject[] getDefinitionDeclaration(CsmObject target, boolean unboxInstantiation) {
        CsmObject decl;
        CsmObject def;
        if (unboxInstantiation && CsmKindUtilities.isTemplateInstantiation(target)) {
            target = ((CsmInstantiation)target).getTemplateDeclaration();
        }
        if (CsmKindUtilities.isVariableDefinition(target)) {
            decl = ((CsmVariableDefinition)target).getDeclaration();
            if (decl == null) {
                decl = target;
                if (TRACE_XREF_REPOSITORY) {
                    System.err.println("not found declaration for variable definition " + target);
                }
            }
            def = target;
        } else if (CsmKindUtilities.isVariableDeclaration(target)) {
            decl = target;
            def = ((CsmVariable)target).getDefinition();
        } else if (CsmKindUtilities.isFunctionDefinition(target)) {
            decl = ((CsmFunctionDefinition)target).getDeclaration();
            if (decl == null) {
                decl = target;
                if (TRACE_XREF_REPOSITORY) {
                    System.err.println("not found declaration for function definition " + target);
                }
            }
            def = target;
        } else if (CsmKindUtilities.isFunctionDeclaration(target)) {
            decl = target;
            def = ((CsmFunction)target).getDefinition();
        } else if (CsmKindUtilities.isClassForwardDeclaration(target)) {
            CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) target;
            if (fd.getCsmClass() != null){
                decl = target;
                def = fd.getCsmClass();
            } else {
                decl = target;
                def = null;
            }
        } else if (CsmKindUtilities.isEnumForwardDeclaration(target)) {
            CsmEnumForwardDeclaration fd = (CsmEnumForwardDeclaration) target;
            if (fd.getCsmEnum() != null) {
                decl = target;
                def = fd.getCsmEnum();
            } else {
                decl = target;
                def = null;
            }
        } else if (CsmKindUtilities.isClass(target)) {
            CsmClass cls = (CsmClass)target;
            Collection<CsmClassifier> classifiers = Collections.emptySet();
            CsmFile file = cls.getContainingFile();
            if(file != null) {
                CsmProject project = file.getProject();
                if(project != null) {
                    classifiers = project.findClassifiers(cls.getQualifiedName());
                }
            }
            if (classifiers.contains(cls)) {
                decl = target;
                def = null;
            } else if (!classifiers.isEmpty()) {
                decl = classifiers.iterator().next();
                def = cls;
                if (TRACE_XREF_REPOSITORY) {
                    System.err.printf("not found declaration for self: %s; use %s%n", target, decl);
                }
            } else {
                decl = target;
                def = null;
            }
        } else {
            decl = target;
            def = null;
        }
        assert decl != null;
        return new CsmObject[] { decl, def };
    }

    public static CsmClass getFunctionClass(CsmFunction fun) {
        return CsmBaseUtilitiesProvider.getDefault().getFunctionClass(fun);
    }

    public static CsmClass getFunctionClassByQualifiedName(CsmFunction fun) {
        if (fun != null) {
            String className = fun.getQualifiedName().toString().replaceAll("(.*)::.*", "$1"); // NOI18N
            CsmObject obj = CsmClassifierResolver.getDefault().findClassifierUsedInFile(className, fun.getContainingFile(), false);
            if (CsmKindUtilities.isClassifier(obj)) {
                CsmClassifier cls = (CsmClassifier) obj;
                cls = CsmClassifierResolver.getDefault().getOriginalClassifier(cls, fun.getContainingFile());
                if (CsmKindUtilities.isClass(cls)) {
                    return (CsmClass) cls;
                }
            }
        }
        return null;
    }

    public static CsmClass getObjectClass(CsmObject obj) {
        CsmClass objClass = null;
        if (CsmKindUtilities.isFunction(obj)) {
            objClass = CsmBaseUtilities.getFunctionClass((CsmFunction)obj);
        } else if (CsmKindUtilities.isClass(obj)) {
            objClass = (CsmClass)obj;
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            objClass = getObjectClass(((CsmEnumerator)obj).getEnumeration());
        } else if (CsmKindUtilities.isScopeElement(obj)) {
            CsmScope scope = ((CsmScopeElement)obj).getScope();
            if (CsmKindUtilities.isClass(scope)) {
                objClass = (CsmClass)scope;
            }
        }
        return objClass;
    }

    public static CsmNamespace getObjectNamespace(CsmObject obj) {
        CsmNamespace objNs = null;
        if (CsmKindUtilities.isNamespace(obj)) {
            objNs = (CsmNamespace)obj;
        } else if (CsmKindUtilities.isFunction(obj)) {
            objNs = CsmBaseUtilities.getFunctionNamespace((CsmFunction)obj);
        } else if (CsmKindUtilities.isClass(obj)) {
            objNs = CsmBaseUtilities.getClassNamespace((CsmClassifier)obj);
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            objNs = getObjectNamespace(((CsmEnumerator)obj).getEnumeration());
        } else if (CsmKindUtilities.isScopeElement(obj)) {
            CsmScope scope = ((CsmScopeElement)obj).getScope();
            if (CsmKindUtilities.isNamespace(scope)) {
                objNs = (CsmNamespace)scope;
            }
        }
        return objNs;
    }

    public static CsmNamespace getFunctionNamespace(CsmFunction fun) {
        return CsmBaseUtilitiesProvider.getDefault().getFunctionNamespace(fun);
    }

    public static CsmNamespace getClassNamespace(CsmClassifier cls) {
        return CsmBaseUtilitiesProvider.getDefault().getClassNamespace(cls);
    }

    public static CsmFunction getFunctionDeclaration(CsmFunction fun) {
        return CsmBaseUtilitiesProvider.getDefault().getFunctionDeclaration(fun);
    }

    public static boolean isFileLocalFunction(CsmFunction fun) {
        CsmFunction decl = getFunctionDeclaration(fun);
        if (decl != null && CsmKindUtilities.isFile(decl.getScope())) {
            return true;
        }
        return false;
    }

    public static boolean isDeclarationFromUnnamedNamespace(CsmObject obj) {
        if (CsmKindUtilities.isScopeElement(obj)) {
            CsmScope scope = ((CsmScopeElement)obj).getScope();
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                return ((CsmNamespaceDefinition)scope).getName().length() == 0;
            } else if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace)scope;
                return !ns.isGlobal() && ns.getName().length() == 0;
            }
        }
        return false;
    }

    public static CsmClass getContextClass(CsmOffsetableDeclaration contextDeclaration) {
        if (contextDeclaration == null) {
            return null;
        }
        CsmClass clazz = null;
        if (CsmKindUtilities.isClass(contextDeclaration)) {
            clazz = (CsmClass)contextDeclaration;
        } else if (CsmKindUtilities.isClassMember(contextDeclaration)) {
            clazz = ((CsmMember)contextDeclaration).getContainingClass();
        } else if (CsmKindUtilities.isFunction(contextDeclaration)) {
            clazz = getFunctionClass((CsmFunction)contextDeclaration);
        }
        return clazz;
    }

    public static CsmFunction getContextFunction(CsmOffsetableDeclaration contextDeclaration) {
        if (contextDeclaration == null) {
            return null;
        }
        CsmFunction fun = null;
        if (CsmKindUtilities.isFunction(contextDeclaration)) {
            fun = (CsmFunction)contextDeclaration;
        }
        return fun;
    }

    public static CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile) {
        return CsmClassifierResolver.getDefault().getOriginalClassifier(orig, contextFile);
    }

    @Deprecated
    public static CsmClassifier getClassifier(CsmType type, CsmFile contextFile, int offset, boolean resolveTypeChain) {
        return CsmClassifierResolver.getDefault().getTypeClassifier(type, contextFile, offset, resolveTypeChain);
    }
    
    public static CsmClassifier getClassifier(CsmType type, CsmScope contextScope, CsmFile contextFile, int offset, boolean resolveTypeChain) {
        return CsmClassifierResolver.getDefault().getTypeClassifier(type, contextScope, contextFile, offset, resolveTypeChain);
    }

    public static CsmObject findClosestTopLevelObject(CsmObject csmTopLevelObject) {
        while(csmTopLevelObject != null) {
            if (CsmKindUtilities.isScope(csmTopLevelObject) && stopOnScope((CsmScope)csmTopLevelObject)) {
                return csmTopLevelObject;
            } else if (CsmKindUtilities.isScopeElement(csmTopLevelObject)) {
                CsmScopeElement elem = (CsmScopeElement) csmTopLevelObject;
                csmTopLevelObject = ((CsmScopeElement)csmTopLevelObject).getScope();
                if (CsmKindUtilities.isDeclaration(elem) && stopOnScope((CsmScope) csmTopLevelObject)) {
                    // Here we should filter out declarations which are in top level
                    // scope but not top level themselves.
                    if (CsmKindUtilities.isParameter(elem) && CsmKindUtilities.isFunction(csmTopLevelObject)) {
                        continue;
                    }
                    // we have top level declaration or decl with unresolved scope
                    return elem;
                }
                // else let scope to be analyzed
            } else if(CsmKindUtilities.isInclude(csmTopLevelObject)) {
                return (CsmInclude)csmTopLevelObject;
            } else if(CsmKindUtilities.isMacro(csmTopLevelObject)) {
                return (CsmMacro)csmTopLevelObject;
            } else {
                assert false : "unexpected top level object " + csmTopLevelObject;
                break;
            }
        }
        return csmTopLevelObject;
    }

    private static boolean stopOnScope(CsmScope scope) {
        if (scope == null) {
            return true;
        } else if (CsmKindUtilities.isFile(scope)) {
            return true;
        } else if (CsmKindUtilities.isNamespaceDefinition(scope)) {
            return true;
        } else if (CsmKindUtilities.isNamespace(scope)) {
            return true;
        } else if (CsmKindUtilities.isFunctionPointerType(scope)) {
            CsmScope parentScope = ((CsmFunctionPointerType) scope).getScope();
            return stopOnScope(parentScope);
        } else {
            // special check for local classes and functions
            if (CsmKindUtilities.isFunction(scope) || CsmKindUtilities.isClass(scope)) {
                CsmScope parentScope = ((CsmScopeElement)scope).getScope();
                return stopOnScope(parentScope);
            }
        }
        return false;
    }

    private static int getScopeDepth(CsmScope scope) {
        int depth = 1;
        while (CsmKindUtilities.isScopeElement(scope)) {
            scope = ((CsmScopeElement) scope).getScope();
            ++depth;
        }
        return depth;
    }
}
