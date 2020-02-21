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
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmOffsetable.Position;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import static org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer.isCVQualifier;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase.NameBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableIdentifiableBase.OffsetableIdentifiableBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;

/**
 *
 */
public class TypeFactory {

    public static CsmType getVarArgType() {
        // for now we use null
        return null;
    }
    
    private TypeFactory() {}
    
    public static TypeImpl createBuiltinType(CharSequence text, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        CsmBuiltIn builtin = BuiltinTypes.getBuiltIn(text);
        return createType(builtin, ptrOperator, arrayDepth, ast, file);
    }

    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file) {
        return createType(classifier, ptrOperator, arrayDepth, ast, file, TypeImpl.getStartOffset(ast), TypeImpl.getEndOffset(ast));
    }
    
    public static TypeImpl createType(CsmClassifier classifier, AST ptrOperator, int arrayDepth, AST ast, CsmFile file, int startOffset, int endOffset) {
        int refence = 0;
        int pointerDepth = 0;
        if (ptrOperator != null &&
            (ptrOperator.getType() == CPPTokenTypes.CSM_CLASS_DECLARATION ||
            ptrOperator.getType() == CPPTokenTypes.CSM_ENUM_DECLARATION)) {
            ptrOperator = ptrOperator.getFirstChild();
            int count = 0; 
            boolean findBody = false;
            boolean findStruct = false;
            for (; ptrOperator != null; ptrOperator = ptrOperator.getNextSibling()){
                switch( ptrOperator.getType() ) {
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_class:
                    case CPPTokenTypes.LITERAL_enum:
                    case CPPTokenTypes.LITERAL_union:
                        findStruct = true;
                        continue;
                    case CPPTokenTypes.LCURLY:
                        findBody = true;
                        count++;
                        continue;
                    case CPPTokenTypes.RCURLY:
                        count--;
                        if (findStruct && count == -1){
                            count = 0;
                            findStruct = false;
                            findBody = true;
                        }
                        continue;
                    default:
                        if (findBody && count == 0) {
                            break;
                        }
                        continue;
                }
                break;
            }
        }
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {
            //for( AST token = ptrOperator.getFirstChild(); token != null; token = token.getNextSibling() ) {
                AST token = ptrOperator.getFirstChild();
                switch( token.getType() ) {
                    case CPPTokenTypes.STAR:
                        pointerDepth++;
                        break;
                    case CPPTokenTypes.AND: // r-value reference
                        refence = 2;
                        break;
                    case CPPTokenTypes.AMPERSAND: 
                        refence = 1;
                        break;
                }
            //}
            ptrOperator = ptrOperator.getNextSibling();
        }
        return new TypeImpl(classifier, pointerDepth, refence, arrayDepth, ast, file, startOffset, endOffset);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth) {
        return createType(ast, file, ptrOperator, arrayDepth, null);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmScope scope) {
        return createType(ast, file, ptrOperator, arrayDepth, null, scope);
    }
    
    private static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope) {
        return createType(ast, file, ptrOperator, arrayDepth, parent, scope, false);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters) {
        return createType(ast, file, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, false);
    }

    public static TypeImpl createType(AST ast, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(ast, null, file, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, inTypedef);
    }
    
    public static TypeImpl createType(AST asts[], CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(asts, null, file, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, inTypedef);
    }    

    static int getReferenceValue(CsmType type) {
        return getReferenceValue(type.isReference(), type.isRValueReference());
    }
    
    static int getReferenceValue(boolean isReference, boolean isRValueReference) {
        if (isRValueReference) {
            return 2;
        } else if (isReference) {
            return 1;
        }
        return 0;
    }    
    
    public static TypeImpl createType(AST ast, CsmClassifier classifier, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(ast, classifier, file, null, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, inTypedef);
    }

    public static TypeImpl createType(AST asts[], CsmClassifier classifier, CsmFile file,  AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(asts, classifier, file, null, ptrOperator, arrayDepth, parent, scope, inFunctionParameters, inTypedef);
    }    
    
    public static TypeImpl createType(AST ast, CsmClassifier classifier, CsmFile file, FileContent content, AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(ast, classifier, file, content, ptrOperator, arrayDepth, parent, scope, null, inFunctionParameters, inTypedef);
    }
    
    public static TypeImpl createType(AST asts[], CsmClassifier classifier, CsmFile file, FileContent content, AST ptrOperator, int arrayDepth, CsmType parent, CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        return createType(asts, classifier, file, content, ptrOperator, arrayDepth, parent, scope, null, inFunctionParameters, inTypedef);
    }    
    
    public static TypeImpl createType(AST ast, 
                                      CsmClassifier classifier, 
                                      CsmFile file, 
                                      FileContent content, 
                                      AST ptrOperator, 
                                      int arrayDepth, 
                                      CsmType parent, 
                                      CsmScope scope, 
                                      List<CsmTemplateParameter> additionalTemplateParams,
                                      boolean inFunctionParameters, 
                                      boolean inTypedef) 
    {
        return createType(new AST[]{ast}, classifier, file, content, ptrOperator, arrayDepth, parent, scope, additionalTemplateParams, inFunctionParameters, inTypedef);
    }    

    public static TypeImpl createType(AST asts[], 
                                      CsmClassifier classifier, 
                                      CsmFile file, 
                                      FileContent content, 
                                      AST ptrOperator, 
                                      int arrayDepth, 
                                      CsmType parent, 
                                      CsmScope scope, 
                                      List<CsmTemplateParameter> additionalTemplateParams,
                                      boolean inFunctionParameters, 
                                      boolean inTypedef) 
    {
        int reference = 0;
        int pointerDepth = 0;
        
        while( ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR ) {        
            ASTPointerDepthCounter visitor = new ASTPointerDepthCounter();
            TypeImpl.visitPointerOperator(visitor, ptrOperator);                    
            pointerDepth += visitor.getPointerDepth();        
            reference = CsmTypes.TypeDescriptor.combineReferences(reference, visitor.getReference());
            ptrOperator = ptrOperator.getNextSibling();
        }

        int returnTypePointerDepth = pointerDepth;
        AST lookahead = ptrOperator;
        while (lookahead != null) {
            if (lookahead.getType() == CPPTokenTypes.RPAREN) {
                // ptrOperator relates to function pointer, not to return type
                returnTypePointerDepth = 0;
                break;
            } else if (lookahead.getType() == CPPTokenTypes.LPAREN) {
                // OK, no need to look further
                break;
            }
            lookahead = lookahead.getNextSibling();
        }

        TypeImpl type;

        boolean functionPointerType = false;
        
        AST ast = asts[0];
        AST typeStart = AstRenderer.getFirstSiblingSkipQualifiers(ast);
        
        if (typeStart != null && typeStart.getType() == CPPTokenTypes.CSM_TYPE_ATOMIC) {
            // Render type_name inside atomic specifier as type_name inside specialization or function params
            type = AstRenderer.renderType(typeStart.getFirstChild(), file, true, scope, true); 
            if (type != null) {
                if (type instanceof TypeFunPtrImpl) {
                    TypeFunPtrImpl ptrT = (TypeFunPtrImpl) type;
                    return new TypeFunPtrImpl(
                            ptrT, 
                            ptrT.getPointerDepth() + pointerDepth, 
                            CsmTypes.TypeDescriptor.combineReferences(getReferenceValue(ptrT), reference), 
                            ptrT.getArrayDepth() + arrayDepth, 
                            ptrT.isConst() || (TypeImpl.initConstQualifiers(ast) != 0), 
                            ptrT.isVolatile() || (TypeImpl.initVolatileQualifiers(ast) != 0)
                    );
                }
                return new TypeImpl(
                    type, 
                    type.getPointerDepth() + pointerDepth, 
                    CsmTypes.TypeDescriptor.combineReferences(getReferenceValue(type), reference), 
                    type.getArrayDepth() + arrayDepth, 
                    type.isConst() || (TypeImpl.initConstQualifiers(ast) != 0), 
                    type.isVolatile() || (TypeImpl.initVolatileQualifiers(ast) != 0)
                );
            }
            return TypeFactory.createBuiltinType("_Atomic", ptrOperator, arrayDepth, ast, file); // NOI18N
        } else if (typeStart != null && typeStart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND && DeclTypeImpl.isDeclType(typeStart.getFirstChild())) {
            type = new DeclTypeImpl(
                typeStart.getFirstChild(), 
                file, 
                scope, 
                TypeImpl.initIsPackExpansion(ast),
                pointerDepth, 
                reference, 
                arrayDepth, 
                TypeImpl.initConstQualifiers(ast), 
                TypeImpl.initVolatileQualifiers(ast), 
                TypeImpl.getStartOffset(ast), 
                TypeImpl.getEndOffset(ast, inFunctionParameters)
            );
        } else if (DeclTypeImpl.isDeclType(typeStart)) {
            type = new DeclTypeImpl(
                typeStart, 
                file, 
                scope, 
                TypeImpl.initIsPackExpansion(ast),
                pointerDepth, 
                reference, 
                arrayDepth, 
                TypeImpl.initConstQualifiers(ast), 
                TypeImpl.initVolatileQualifiers(ast), 
                TypeImpl.getStartOffset(ast), 
                TypeImpl.getEndOffset(ast, inFunctionParameters)
            );
        } else if (parent != null) {
            type = NestedType.create(
                parent, 
                file, 
                parent.isPackExpansion(),
                parent.getPointerDepth(), 
                getReferenceValue(parent), 
                parent.getArrayDepth(), 
                parent.isConst(), 
                parent.isVolatile(), 
                parent.getStartOffset(), 
                parent.getEndOffset()
            );
        } else if (TypeFunPtrImpl.isFunctionPointerParamList(asts, inFunctionParameters, inTypedef)) {
            type = new TypeFunPtrImpl(
                file,
                returnTypePointerDepth,
                reference,
                arrayDepth, 
                TypeImpl.initIsConst(ast), 
                TypeImpl.initIsVolatile(ast),
                TypeImpl.getStartOffset(ast),
                TypeFunPtrImpl.getEndOffset(ast)
            );
            ((TypeFunPtrImpl)type).init(asts, scope, inFunctionParameters, inTypedef);
            functionPointerType = true; 
        } else {
            type = new TypeImpl(
                file, 
                TypeImpl.initIsPackExpansion(ast),
                pointerDepth, 
                reference, 
                arrayDepth, 
                TypeImpl.initConstQualifiers(ast), 
                TypeImpl.initVolatileQualifiers(ast), 
                TypeImpl.getStartOffset(ast), 
                TypeImpl.getEndOffset(ast, inFunctionParameters)
            );
        }

        // TODO: pass extra parameters to the constructor insdead of calling methods!!!
        
        ///// INIT CLASSFIER stuff
        if( typeStart != null) {
            if (typeStart.getType() == CPPTokenTypes.LITERAL_struct ||
                typeStart.getType() == CPPTokenTypes.LITERAL_class ||
                typeStart.getType() == CPPTokenTypes.LITERAL_union ||
                typeStart.getType() == CPPTokenTypes.LITERAL_enum) 
            {
                typeStart = typeStart.getNextSibling();
            }            
        }
        if( typeStart == null)
            /*(tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
            tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
            tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID )*/ {
            //return null;
        } else {
            if(classifier != null) {
                type._setClassifier(classifier);
                type.setClassifierText(classifier.getName());
            } else if( typeStart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ) {
                CsmClassifier cls = BuiltinTypes.getBuiltIn(typeStart);
                type._setClassifier(cls);
                type.setClassifierText(cls.getName());
            } else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                AST tokFirstId;
                try {
                    if (typeStart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN ||
                        typeStart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND ||
                        typeStart.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                        tokFirstId = typeStart.getFirstChild();
                    } else {
                        tokFirstId = typeStart;
                    }
                    if( tokFirstId == null ) {
                        // this is unnormal; but we should be able to work even on incorrect AST
                        //return null;
                    } else {
                        //Check for global type
                        if (tokFirstId.getType() ==  CPPTokenTypes.SCOPE) {
                            type = NestedType.create(null, file, type.isPackExpansion(), type.getPointerDepth(), getReferenceValue(type), type.getArrayDepth(), type.isConst(), type.isVolatile(), type.getStartOffset(), type.getEndOffset());
                            tokFirstId = tokFirstId.getNextSibling();
                        }
                        //TODO: we have AstRenderer.getNameTokens, it is better to use it here
                        List<CharSequence> l = new ArrayList<>();
                        int templateDepth = 0;
                        AST templateTypeQuals = null;
                        StringBuilder sb = new StringBuilder();
                        for( AST namePart = tokFirstId; namePart != null; namePart = namePart.getNextSibling() ) {
                            if( templateDepth == 0 && namePart.getType() == CPPTokenTypes.IDENT ) {
                                CharSequence text = AstUtil.getText(namePart);
                                sb.append(text);
                                l.add(NameCache.getManager().getString(text));
                                //l.add(namePart.getText());
                            } else if (templateDepth == 0 && namePart.getType() == CPPTokenTypes.CSM_TYPE_DECLTYPE) {
                                CharSequence text = AstUtil.getText(namePart.getFirstChild());
                                sb.append(text);
                                l.add(NameCache.getManager().getString(text));                                
                            } else if( namePart.getType() == CPPTokenTypes.LESSTHAN ) {
                                // the beginning of template parameters
                                templateDepth++;
                                type.initInstantiationParams();
                            } else if( namePart.getType() == CPPTokenTypes.GREATERTHAN ) {
                                // the beginning of template parameters
                                templateDepth--;
                            } else {
                                //assert namePart.getType() == CPPTokenTypes.SCOPE;
                                if( templateDepth == 0) {
                                    if (namePart.getType() == CPPTokenTypes.SCOPE) {
                                        if(functionPointerType) {
                                            sb.append("::"); // NOI18N
                                            continue;
                                        } else {
                                            // We're done here, start filling nested type
                                            type.setClassifierText(NameCache.getManager().getString(sb));
                                            type.setQName(l.toArray(new CharSequence[l.size()]));
                                            type = createType(namePart.getNextSibling(), file, ptrOperator, arrayDepth, TemplateUtils.checkTemplateType(type, scope, additionalTemplateParams), scope);
                                            break;
                                        }
                                    } else {
                                        if (TraceFlags.DEBUG) {
                                            StringBuilder tokenText = new StringBuilder();
                                            tokenText.append('[').append(AstUtil.getText(namePart));
                                            if (namePart.getNumberOfChildren() == 0) {
                                                tokenText.append(", line=").append(namePart.getLine()); // NOI18N
                                                tokenText.append(", column=").append(namePart.getColumn()); // NOI18N
                                            }
                                            tokenText.append(']');
                                            System.err.println("Incorect token: expected '::', found " + tokenText.toString());
                                        }
                                    }
                                } else {
                                    // Initialize instantiation params
                                    // TODO: maybe we need to filter out some more tokens
                                    if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN
                                            || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                                            || AstUtil.isElaboratedKeyword(namePart)) {
                                        CsmType t = AstRenderer.renderType(templateTypeQuals != null ? templateTypeQuals : namePart, file, true, scope, true);
                                        type.addInstantiationParam(new TypeBasedSpecializationParameterImpl(TemplateUtils.checkTemplateType(t, scope), scope));
                                    }
                                    if (namePart.getType() == CPPTokenTypes.CSM_EXPRESSION) {
                                        type.addInstantiationParam(ExpressionBasedSpecializationParameterImpl.create(ExpressionStatementImpl.create(namePart, type.getContainingFile(), scope),
                                                type.getContainingFile(), OffsetableBase.getStartOffset(namePart), OffsetableBase.getEndOffset(namePart)));
                                    }
                                }
                            }
                            
                            // Handle const/volatile quals for template parameter
                            if (templateDepth > 0 && isCVQualifier(namePart.getType())) {
                                if (templateTypeQuals == null) {
                                    templateTypeQuals = namePart;
                                }
                            } else {
                                templateTypeQuals = null; // Maybe too cautious.
                            }
                        }
                        if (!type.isInitedClassifierText()) {
                            type.setClassifierText(NameCache.getManager().getString(sb));
                            type.setQName(l.toArray(new CharSequence[l.size()]));
                        }
                    }
                } catch( Exception e ) {
                    DiagnosticExceptoins.register(e);
                }
            }
        }
        return type;
    }

    public static class TypeBuilder extends OffsetableIdentifiableBuilder {
        
        private NameBuilder nameBuilder;
        private StringBuilder specifierBuilder;
        
        private int pointerDepth = 0;
        private final int arrayDepth = 0;

        private int reference;
        private boolean _const;
        private boolean _volatile;
        
        private boolean typedef = false;
        
        private CsmClassifier cls;
        
        
        private CsmScope scope;

        final ArrayList<CsmSpecializationParameter> instantiationParams = new ArrayList<>();
        
        public void setNameBuilder(NameBuilder nameBuilder) {
            this.nameBuilder = nameBuilder;
        }

        public NameBuilder getNameBuilder() {
            return nameBuilder;
        }
                
        public void setClassifier(CsmClassifier cls) {
            this.cls = cls;
        }
        
        public CsmClassifier getClassifier() {
            return cls;
        }        

        public void setTypedef() {
            this.typedef = true;
        }

        public void setConst() {
            this._const = true;
        }

        public void setVolatile() {
            this._volatile = true;
        }
        
        public void incPointerDepth() {
            this.pointerDepth++;
        }
        
        public void setReference() {
            this.reference = 1;
        }

        public void setRValueReference() {
            this.reference = 2;
        }
        
        public void setSimpleTypeSpecifier(CharSequence specifier) {
            if(specifierBuilder == null) {
                specifierBuilder = new StringBuilder(specifier);
            } else {
                specifierBuilder.append(" "); // NOI18N
                specifierBuilder.append(specifier);
            }
        }
        
        public CsmScope getScope() {
            return scope;
        }

        public void setScope(CsmScope scope) {
            assert scope != null;
            this.scope = scope;
        }        
        public CsmType create() {
            assert scope != null;
            
            TypeImpl type = null;
            boolean first = true;
            if(nameBuilder != null) {
                for (NameBuilder.NamePart namePart : nameBuilder.getNames()) {
                    if(first) {
                        first = false;
                        List<CharSequence> nameList = new ArrayList<>();
                        type = new TypeImpl(getFile(), false, pointerDepth, reference, arrayDepth, _const, _volatile, getStartOffset(), getEndOffset());
                        nameList.add(namePart.getPart());
                        type.setClassifierText(namePart.getPart());
                        type.setQName(nameList.toArray(new CharSequence[nameList.size()]));
                    } else {
                        List<CharSequence> nameList = new ArrayList<>();
                        type = NestedType.create(TemplateUtils.checkTemplateType(type, scope), getFile(), false, type.getPointerDepth(), getReferenceValue(type), type.getArrayDepth(), type.isConst(), type.isVolatile(), type.getStartOffset(), type.getEndOffset());
                        nameList.add(namePart.getPart());
                        type.setClassifierText(namePart.getPart());
                        type.setQName(nameList.toArray(new CharSequence[nameList.size()]));                    
                    }
                    for (SpecializationDescriptor.SpecializationParameterBuilder param : namePart.getParams()) {
                        param.setScope(getScope());
                        type.addInstantiationParam(param.create());
                    }
                }
            } else if (specifierBuilder != null) {
                CsmClassifier classifier = BuiltinTypes.getBuiltIn(specifierBuilder);
                type = new TypeImpl(classifier, pointerDepth, reference, arrayDepth, _const, _volatile, getFile(), getStartOffset(), getEndOffset());
            } else if (cls != null) {
                type = new TypeImpl(cls, pointerDepth, reference, arrayDepth, _const, _volatile, getFile(), getStartOffset(), getEndOffset());
                type.setTypeOfTypedef();    
            }
            return TemplateUtils.checkTemplateType(type, scope);
        }

        @Override
        public String toString() {
            return "TypeBuilder{" + "nameBuilder=" + nameBuilder + ", specifierBuilder=" + specifierBuilder + //NOI18N
                    ", pointerDepth=" + pointerDepth + ", arrayDepth=" + arrayDepth + //NOI18N
                    ", reference=" + reference + ", _const=" + _const + ", typedef=" + typedef + //NOI18N
                    ", cls=" + cls + ", scope=" + scope + ", instantiationParams=" + instantiationParams + //NOI18N
                    super.toString() + '}'; //NOI18N
        }
    }
    
    public static CsmType createType(CsmType type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
        if(type.getPointerDepth() == pointerDepth &&
            type.isReference() == (reference > 0) &&
            type.isRValueReference() == (reference == 2) &&
            type.getArrayDepth() == arrayDepth &&
            type.isConst() == _const &&
            type.isVolatile() == _volatile) {
            return type;
        }
        if(type instanceof NestedType) {
            return new NestedType((NestedType)type, pointerDepth, reference, arrayDepth, _const, _volatile);
        }
        if(type instanceof TypeFunPtrImpl) {
            return new TypeFunPtrImpl((TypeFunPtrImpl)type, pointerDepth, reference, arrayDepth, _const, _volatile);
        }
        if(type instanceof TemplateParameterTypeImpl) {
            return new TemplateParameterTypeImpl((TemplateParameterTypeImpl)type, pointerDepth, reference, arrayDepth, _const, _volatile);
        }
        if(type instanceof TypeImpl) {
            return new TypeImpl((TypeImpl)type, pointerDepth, reference, arrayDepth, _const, _volatile);
        }        
        if (type instanceof CsmTemplateParameterType) {
            return new TemplateParameterTypeWrapper(type, pointerDepth, reference, arrayDepth, _const, _volatile);
        }        
        return new TypeWrapper(type, pointerDepth, reference, arrayDepth, _const, _volatile);
    }

    public static CsmType createType(CsmType type, List<CsmSpecializationParameter> instantiationParams) {
        if(type instanceof NestedType) {
            return new NestedType((NestedType)type, instantiationParams);
        }
        if(type instanceof TypeFunPtrImpl) {
            return new TypeFunPtrImpl((TypeFunPtrImpl)type, instantiationParams);
        }
        if(type instanceof TemplateParameterTypeImpl) {
            return new TemplateParameterTypeImpl((TemplateParameterTypeImpl)type, instantiationParams);
        }
        if(type instanceof TypeImpl) {
            return new TypeImpl((TypeImpl)type, instantiationParams);
        }
        return type;
    }

    public static CsmType createSimpleType(CsmClassifier cls, CsmFile file, int startOffset, int endOffset) {
        TypeImpl type = new TypeImpl(file, false, 0, 0, 0, false, false, startOffset, endOffset);
        type.setClassifierText(cls.getName());
        List<CharSequence> l = new ArrayList<>();
        l.add(cls.getName());
        type.setQName(l.toArray(new CharSequence[l.size()]));
        type.initClassifier(cls);
        return type;
    }
    
    public static CsmType createFunPtrType(CsmFile file, 
                                           int pointerDepth, 
                                           int reference, 
                                           int arrayDepth, 
                                           boolean _const, 
                                           boolean _volatile, 
                                           int startOffset, 
                                           int endOffset,
                                           Collection<CsmParameter> functionParams,
                                           CsmType returnType)
    {
        return new TypeFunPtrImpl(
                file, 
                pointerDepth,
                reference, 
                arrayDepth,
                _const, 
                _volatile, 
                startOffset, 
                endOffset, 
                functionParams,
                returnType
        );
    }
    
    
    private static class TypeWrapper implements CsmType {
        protected CsmType type;
        protected int pointerDepth;
        protected int reference;
        protected int arrayDepth;
        protected boolean _const;
        protected boolean _volatile;

        public TypeWrapper(CsmType type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
            this.type = type;
            this.pointerDepth = pointerDepth;
            this.reference = reference;
            this.arrayDepth = arrayDepth;
            this._const = _const;
            this._volatile = _volatile;
        }

        @Override
        public CsmClassifier getClassifier() {
            return type.getClassifier();
        }

        @Override
        public CharSequence getClassifierText() {
            return type.getClassifierText();
        }

        @Override
        public boolean isInstantiation() {
            return type.isInstantiation();
        }

        @Override
        public boolean hasInstantiationParams() {
            return type.hasInstantiationParams();
        }

        @Override
        public List<CsmSpecializationParameter> getInstantiationParams() {
            return type.getInstantiationParams();
        }

        @Override
        public boolean isPackExpansion() {
            return type.isPackExpansion();
        }

        @Override
        public int getArrayDepth() {
            return arrayDepth;
        }

        @Override
        public boolean isPointer() {
            return pointerDepth > 0;
        }

        @Override
        public int getPointerDepth() {
            return pointerDepth;
        }

        @Override
        public boolean isReference() {
            return reference > 0;
        }

        @Override
        public boolean isRValueReference() {
            return reference == 2;
        }

        @Override
        public boolean isConst() {
            return _const;
        }

        @Override
        public boolean isVolatile() {
            return _volatile;
        }

        @Override
        public boolean isBuiltInBased(boolean resolveTypeChain) {
            return type.isBuiltInBased(resolveTypeChain);
        }

        @Override
        public boolean isTemplateBased() {
            return type.isTemplateBased();
        }

        @Override
        public CharSequence getCanonicalText() {
            return getText();
        }

        @Override
        public CsmFile getContainingFile() {
            return type.getContainingFile();
        }

        @Override
        public int getStartOffset() {
            return type.getStartOffset();
        }

        @Override
        public int getEndOffset() {
            return type.getEndOffset();
        }

        @Override
        public Position getStartPosition() {
            return type.getStartPosition();
        }

        @Override
        public Position getEndPosition() {
            return type.getEndPosition();
        }

        @Override
        public CharSequence getText() {
            return format();
        }

        public CharSequence format() {
            StringBuilder sb = new StringBuilder();
	        if (isConst()) {
                sb.append("const "); // NOI18N
            }
            sb.append(getClassifier().getQualifiedName());
            for (int i = 0; i < getPointerDepth(); i++) {
                sb.append('*'); // NOI18N
            }
            if (isReference()) {
                sb.append('&'); // NOI18N
            }
            for (int i = 0; i < getArrayDepth(); i++) {
                sb.append("[]"); // NOI18N
            }
            return sb;
        }

        @Override
        public String toString() {
            return "WRAPPED TYPE: " + format().toString();  // NOI18N
        }        
    }
    
    private static class TemplateParameterTypeWrapper extends TypeWrapper implements CsmTemplateParameterType {

        public TemplateParameterTypeWrapper(CsmType type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
            super(type, pointerDepth, reference, arrayDepth, _const, _volatile);
        }

        @Override
        public CsmTemplateParameter getParameter() {
            return ((CsmTemplateParameterType) type).getParameter();
        }

        @Override
        public CsmType getTemplateType() {
            return ((CsmTemplateParameterType) type).getTemplateType();
        }

    }        
    
    private static class ASTPointerDepthCounter implements AstUtil.ASTTokenVisitor {
    
        private int pointerDepth;
        
        private int reference;

        @Override
        public Action visit(AST token) {
            switch( token.getType() ) {
                case CPPTokenTypes.STAR:
                    ++pointerDepth;
                    break;
                case CPPTokenTypes.AND: // r-value reference
                    reference = 2;
                    break;
                case CPPTokenTypes.AMPERSAND: 
                    reference = 1;
                    break;
                    
                default: {
                    if (!AstRenderer.isConstQualifier(token.getType())) {
                        // Original code looked only on first token, 
                        // now we will break if token isn't pointer/reference
                        // or const qualifier
                        return Action.ABORT; 
                    }
                }
            }
            return Action.CONTINUE;
        }        

        public int getPointerDepth() {
            return pointerDepth;
        }

        public int getReference() {
            return reference;
        }
    }
    
}
