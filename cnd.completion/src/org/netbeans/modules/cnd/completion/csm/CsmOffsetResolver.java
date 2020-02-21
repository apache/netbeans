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

package org.netbeans.modules.cnd.completion.csm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmFunctionInstantiation;
import org.netbeans.modules.cnd.api.model.CsmFunctionPointerType;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmInitializerListContainer;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import static org.netbeans.modules.cnd.completion.csm.CsmStatementResolver.findInnerExpression;
import org.netbeans.modules.cnd.completion.impl.xref.FileReferencesContext;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.utils.MutableObject;

/**
 * resolve file objects under offset
 */
public class CsmOffsetResolver {
    private CsmFile file;

    /** Creates a new instance of CsmOffsetResolver */
    public CsmOffsetResolver () {
    }

    public CsmOffsetResolver (CsmFile file) {
        this.file = file;
    }

    public CsmFile getFile () {
        return file;
    }

    public void setFile (CsmFile file) {
        this.file = file;
    }

    // ==================== help methods =======================================

    public static CsmObject findObject(CsmFile file, int offset, FileReferencesContext fileReferncesContext) {
        assert (file != null) : "can't be null file in findObject";
        // not interested in context, only object under offset
        CsmObject last = (CsmObject) CsmCacheManager.get(new CsmLastObjectKey(file, offset));
        if (last == null) {
            CsmContext context = new CsmContext(file, offset);
            last = findObjectWithContext(file, offset, context, fileReferncesContext);
            last = exploreTypeObject(context, last, offset);
            CsmCacheManager.put(new CsmContextKey(file, offset), context);
            CsmCacheManager.put(new CsmLastObjectKey(file, offset), last);
        }
        return last;
    }

    private static CsmObject findObjectWithContext(CsmFile file, int offset, CsmContext context, FileReferencesContext fileReferncesContext) {
        assert (file != null) : "can't be null file in findObject";
        CsmObject last = null;
        if (context == null) {
            // create dummy context
            context = new CsmContext(file, offset);
        }
        CsmObject lastObj = CsmDeclarationResolver.findInnerFileObject(file, offset, context, fileReferncesContext);
        last = lastObj;
        // for functions search deeper
        if (CsmKindUtilities.isFunction(lastObj)) {
            CsmFunction fun = (CsmFunction)lastObj;
            // check if offset in return value
            CsmType retType = fun.getReturnType();
            if (!CsmOffsetUtilities.sameOffsets(fun, retType) && CsmOffsetUtilities.isInObject(retType, offset)) {
                context.setLastOwner(fun);
                context.setLastObject(retType);
                return retType;
            }
            // check template parameters
            if (CsmKindUtilities.isTemplate(fun)) {
                Collection<CsmTemplateParameter> templateParams = ((CsmTemplate)fun).getTemplateParameters();
                CsmTemplateParameter templateParam = CsmOffsetUtilities.findObject(templateParams, context, offset);
                if (templateParam != null && !CsmOffsetUtilities.sameOffsets(fun,templateParam)) {
                    context.setLastObject(templateParam);
                    while(CsmKindUtilities.isTemplate(templateParam)) {
                        templateParams = ((CsmTemplate)templateParam).getTemplateParameters();
                        CsmTemplateParameter innerTemplateParam = CsmOffsetUtilities.findObject(templateParams, context, offset);
                        if(innerTemplateParam != null && !CsmOffsetUtilities.sameOffsets(templateParam,innerTemplateParam)) {
                            context.setLastObject(innerTemplateParam);
                            templateParam = innerTemplateParam;
                        } else {
                            break;
                        }
                    }
                    return templateParam;
                }
            }
            // check if offset in parameters
            @SuppressWarnings("unchecked")
            Collection<CsmParameter> params = fun.getParameters();
            CsmParameter param = CsmOffsetUtilities.findObject(params, context, offset);
            if (param != null && !CsmOffsetUtilities.sameOffsets(fun, param)) {
                CsmType type = param.getType();
                if (CsmOffsetUtilities.isInObject(type, offset)) {
                    context.setLastObject(type);
                    return type;
                }
                if (findInnerExpression(param.getInitialValue(), offset, context)) {
                    return context.getLastObject();
                }
                context.setLastObject(param);
                return param;
            }
            // check for constructor initializers
            if (CsmKindUtilities.isConstructor(lastObj)) {
                CsmInitializerListContainer ctor = (CsmInitializerListContainer)lastObj;
                for (CsmExpression izer : ctor.getInitializerList()) {
                    if (!CsmOffsetUtilities.sameOffsets(lastObj, izer) && CsmOffsetUtilities.isInObject(izer, offset)) {
                        for (CsmStatement csmStatement : izer.getLambdas()) {
                            CsmDeclarationStatement lambda = (CsmDeclarationStatement)csmStatement;
                            if ((!CsmOffsetUtilities.sameOffsets(lastObj, lambda) || lambda.getStartOffset() != lambda.getEndOffset()) && CsmOffsetUtilities.isInObject(lambda, offset)) {
                                // offset is in body, try to find inners statement
                                if (CsmStatementResolver.findInnerObject(lambda, offset, context)) {
                                    // if found exact object => return it, otherwise return last found scope
                                    CsmObject found = context.getLastObject();
                                    if (!CsmOffsetUtilities.sameOffsets(lambda, found)) {
                                        context.setLastObject(found);
                                        return found;
                                    }
                                }
                            }
                        }
                        context.setLastObject(izer);
                        return izer;
                    }
                }
            }
            // for function definition search deeper in body's statements
            if (CsmKindUtilities.isFunctionDefinition(lastObj)) {
                CsmFunctionDefinition funDef = (CsmFunctionDefinition)lastObj;
                CsmCompoundStatement body = funDef.getBody();
                if ((!CsmOffsetUtilities.sameOffsets(funDef, body) || body.getStartOffset() != body.getEndOffset()) && CsmOffsetUtilities.isInObject(body, offset)) {
                    last = null;
                    // offset is in body, try to find inners statement
                    if (CsmStatementResolver.findInnerObject(body, offset, context)) {
                        // if found exact object => return it, otherwise return last found scope
                        CsmObject found = context.getLastObject();
                        CsmScope lastScope = context.getLastScope();
                        if (!CsmOffsetUtilities.sameOffsets(body, found) && !CsmOffsetUtilities.sameOffsets(lastScope, found)) {
                            lastObj = last = found;
                        }
                    }
                }
            }
        }

        if (CsmKindUtilities.isClass(lastObj)) {
            // check if in inheritance part
            CsmClass clazz = (CsmClass)lastObj;
            Collection<CsmInheritance> inherits = clazz.getBaseClasses();
            CsmInheritance inh = CsmOffsetUtilities.findObject(inherits, context, offset);
            if (inh != null && !CsmOffsetUtilities.sameOffsets(clazz, inh)) {
                context.setLastObject(inh);
                last = inh;
            }
        } else if (CsmKindUtilities.isVariable(lastObj)) {
            CsmType type = ((CsmVariable)lastObj).getType();
            if (!CsmOffsetUtilities.sameOffsets(lastObj, type) && CsmOffsetUtilities.isInObject(type, offset)) {
                // Function pointer type contains the whole declaration (except initilizer)
                // and will be handled later.
                if (!CsmKindUtilities.isFunctionPointerType(type) && !CsmUtilities.isAutoType(type)) {
                    context.setLastOwner(lastObj);
                    context.setLastObject(type);
                    last = type;
                }
            }
            if (findInnerExpression(((CsmVariable)lastObj).getInitialValue(), offset, context)) {
                lastObj = last = context.getLastObject();
            }
        } else if (CsmKindUtilities.isClassForwardDeclaration(lastObj) || CsmKindUtilities.isEnumForwardDeclaration(lastObj)) {
            // check template parameters
            if (CsmKindUtilities.isTemplate(lastObj)) {
                Collection<CsmTemplateParameter> templateParams = ((CsmTemplate)lastObj).getTemplateParameters();
                CsmTemplateParameter templateParam = CsmOffsetUtilities.findObject(templateParams, context, offset);
                if (templateParam != null && !CsmOffsetUtilities.sameOffsets(lastObj, templateParam)) {
                    context.setLastObject(templateParam);
                    return templateParam;
                }
            }
        } else if (CsmKindUtilities.isFriend(lastObj)) {
            // check template parameters
            if (CsmKindUtilities.isTemplate(lastObj)) {
                Collection<CsmTemplateParameter> templateParams = ((CsmTemplate)lastObj).getTemplateParameters();
                CsmTemplateParameter templateParam = CsmOffsetUtilities.findObject(templateParams, context, offset);
                if (templateParam != null && !CsmOffsetUtilities.sameOffsets(lastObj, templateParam)) {
                    context.setLastObject(templateParam);
                    return templateParam;
                }
            }
        } else if (CsmKindUtilities.isFunctionExplicitInstantiation(lastObj)) {
            CsmFunctionInstantiation fun = (CsmFunctionInstantiation)lastObj;
            // check if offset in parameters
            @SuppressWarnings("unchecked")
            Collection<CsmParameter> params = fun.getParameters();
            CsmParameter param = CsmOffsetUtilities.findObject(params, context, offset);
            if (param != null && !CsmOffsetUtilities.sameOffsets(fun, param)) {
                CsmType type = param.getType();
                if (CsmOffsetUtilities.isInObject(type, offset)) {
                    context.setLastOwner(fun);
                    context.setLastObject(type);
                    return type;
                }
                context.setLastObject(param);
                return param;
            }
        }
        return last;
    }
    
    // Function updates context and last object if current last has type or is even type and this type is scope itself
    private static CsmObject exploreTypeObject(CsmContext context, CsmObject last, int offset) {
        if (CsmKindUtilities.isTypedefOrTypeAlias(last)) { 
            CsmType type = ((CsmTypedef) last).getType();
            if (CsmKindUtilities.isFunctionPointerType(type)) {
                CsmObject inner = updateAndFindInFunctionType(context, (CsmFunctionPointerType) type, offset);
                if (inner != null) {
                    last = inner;
                }
            }
        } else if (CsmKindUtilities.isVariable(last)) {
            CsmType type = ((CsmVariable) last).getType();
            if (CsmKindUtilities.isFunctionPointerType(type)) {
                CsmObject inner = updateAndFindInFunctionType(context, (CsmFunctionPointerType) type, offset);
                if (inner != null) {
                    last = inner;
                }
            }
        } else if (CsmKindUtilities.isFunctionPointerType(last)) {
            CsmObject inner = updateAndFindInFunctionType(context, (CsmFunctionPointerType) last, offset);
            if (inner != null) {
                last = inner;
            }
        }
        return last;
    }
    
    private static CsmObject updateAndFindInFunctionType(CsmContext context, CsmFunctionPointerType funType, int offset) {
        if (CsmOffsetUtilities.isInObject(funType, offset)) {
            // add scope to context
            CsmContextUtilities.updateContext(funType, offset, context);
            
            Collection<CsmParameter> params = funType.getParameters();
            CsmParameter param = CsmOffsetUtilities.findObject(params, context, offset);            
            
            if (param != null && !CsmOffsetUtilities.sameOffsets(funType, param)) {
                CsmType type = param.getType();
                
                if (CsmKindUtilities.isFunctionPointerType(type)) {
                    CsmObject inner = updateAndFindInFunctionType(context, (CsmFunctionPointerType) type, offset);
                    if (inner != null) {
                        return inner;
                    }
                }
//                if (CsmOffsetUtilities.isInObject(type, offset)) {
//                    context.setLastObject(type);
//                    return type;
//                }
                context.setLastObject(param);
                return param;
            }
        }
        return null;
    }

    public static CsmContext findContext(CsmFile file, int offset, FileReferencesContext fileReferncesContext) {
        CsmContext context = new CsmContext(file, offset);
        findObjectWithContext(file, offset, context, fileReferncesContext);
        exploreTypeObject(context, context.getLastObject(), offset);
        return context;
    }
    
    public static CsmContext findContextFromScope(CsmFile file, int offset, CsmScope contextScope) {
        CsmContext context = new CsmContext(file, offset);
        List<CsmScope> scopes = new ArrayList<CsmScope>();
        scopes.add(contextScope);
        while (CsmKindUtilities.isScopeElement(contextScope) && !CsmKindUtilities.isFile(contextScope)) {
            contextScope = ((CsmScopeElement) contextScope).getScope();
            scopes.add(0, contextScope);
        }
        for (CsmScope scope : scopes) {
            CsmContextUtilities.updateContext(scope, offset, context);
        }
        context.setLastObject(context.getLastScope());
        exploreTypeObject(context, context.getLastObject(), offset);
        return context;
    }
    
    // Most likely caching of contexts is senseless (it is fast to get a new one)
    private abstract static class AbstractResolvePointKey {
        
        private final CsmFile file; 
        
        private final int offset;

        public AbstractResolvePointKey(CsmFile file, int offset) {
            this.file = file;
            this.offset = offset;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + (this.file != null ? this.file.hashCode() : 0);
            hash = 97 * hash + this.offset;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final AbstractResolvePointKey other = (AbstractResolvePointKey) obj;
            if (this.file != other.file && (this.file == null || !this.file.equals(other.file))) {
                return false;
            }
            if (this.offset != other.offset) {
                return false;
            }
            return true;
        }
    }
    
    private static class CsmContextKey extends AbstractResolvePointKey {

        public CsmContextKey(CsmFile file, int offset) {
            super(file, offset);
        }
    }
    
    private static class CsmLastObjectKey extends AbstractResolvePointKey {

        public CsmLastObjectKey(CsmFile file, int offset) {
            super(file, offset);
        }
    }
}
