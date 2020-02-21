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


import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import static org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * Represent pointer to function type
 */
public final class TypeFunPtrImpl extends TypeImpl implements CsmFunctionPointerType {

    private CsmType returnType;
    private Collection<CsmParameter> functionParameters;
    private short functionPointerDepth;
    
    private CsmUID<CsmScope> scopeUID;    
    private transient CsmScope scopeRef;
    

    TypeFunPtrImpl(CsmFile file, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, int startOffset, int endOffset) {
        super(file, false, pointerDepth, reference, arrayDepth, _const, _volatile, startOffset, endOffset);
        functionParameters = null;
        returnType = null;
    }
    
    // package-local - for facory only
    TypeFunPtrImpl(TypeFunPtrImpl type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
        super(type, pointerDepth, reference, arrayDepth, _const, _volatile);
        if(type.functionParameters != null) {
            functionParameters = new ArrayList<>(type.functionParameters);
        } else {
            functionParameters = null;
        }
        returnType = type.returnType != null ? type.returnType : null;
        functionPointerDepth = type.functionPointerDepth;
    }

    // package-local - for facory only
    TypeFunPtrImpl(TypeFunPtrImpl type, List<CsmSpecializationParameter> instantiationParams) {
        super(type, instantiationParams);
        if(type.functionParameters != null) {
            functionParameters = new ArrayList<>(type.functionParameters);
        } else {
            functionParameters = null;
        }
        returnType = type.returnType != null ? type.returnType : null;
        functionPointerDepth = type.functionPointerDepth;
    }
    
    // package-local - for facory only
    TypeFunPtrImpl(CsmFile file, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, int startOffset, int endOffset, Collection<CsmParameter> parameters, CsmType returnType) {
        super(file, false, pointerDepth, reference, arrayDepth, _const, _volatile, startOffset, endOffset);
        this.functionParameters = parameters;
        this.returnType = returnType;
    }    
    
    // package-local
    void setReturnType(CsmType type) {
        this.returnType = type;
    }
    
    void init(AST asts[], CsmScope scope, boolean inFunctionParameters, boolean inTypedef) {
        initFunctionPointerParamList(asts, this, inFunctionParameters, inTypedef);
        
        // Initialize scope
        this.scopeRef = scope;
        if ((scope instanceof CsmIdentifiable)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert scopeUID != null;
        }
        
        // Initialize return type   
        AST ast = asts[0];
        AST typeASTStart = ast;
        AST typeASTEnd = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_TYPE_BUILTIN);
        if (typeASTEnd != null) {
            if (CPPTokenTypes.LITERAL_auto == typeASTEnd.getFirstChild().getType()) {
                typeASTEnd = AstUtil.findSiblingOfType(typeASTEnd.getNextSibling(), CPPTokenTypes.CSM_TYPE_BUILTIN);
            }
        }
        if (typeASTEnd == null) {
            typeASTEnd = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_TYPE_COMPOUND);
            if (typeASTEnd == null) {
                typeASTEnd = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
                if (typeASTEnd == null) {
                    typeASTEnd = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_TYPE_DECLTYPE);
                    typeASTStart = typeASTEnd;
                    if (typeASTEnd == null) {
                        typeASTEnd = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_TYPE_ATOMIC);
                        typeASTStart = typeASTEnd;
                    }
                }
            }            
        }
        
        AST fakeTypeAst = AstUtil.cloneAST(typeASTStart, typeASTEnd);
        
        initFunctionReturnType(fakeTypeAst, this, getContainingFile());
        
        setClassifierText(NameCache.getManager().getString(decorateText("", this, false, null)));
    }

    @Override
    protected CsmClassifier _getClassifier() {
        return new FunctionPointerImpl(this);
    }
    
    @Override
    public CsmClassifier getClassifier() {
        return getClassifier(null, false);
    }

    @Override
    public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
        if (instantiations != null && !instantiations.isEmpty()) {
            CsmClassifier classifier = _getClassifier();
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            CsmObject obj = classifier;
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(this);
                try {
                    if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        ListIterator<CsmInstantiation> iter = instantiations.listIterator(instantiations.size());
                        while (iter.hasPrevious()) {
                            CsmInstantiation instantiation = iter.previous();
                            obj = ((InstantiationProviderImpl)ip).instantiate((CsmTemplate) obj, instantiation, false);
                        }
                    } else {
                        return null;
                    }
                } finally {
                    ResolverFactory.releaseResolver(resolver);
                }
            } else {
                obj = ip.instantiate((CsmTemplate) classifier, this);
            }
            if (CsmKindUtilities.isClassifier(obj)) {
                return (CsmClassifier)obj;
            }
        }
        return _getClassifier();
    }

    @Override
    public int getPointerDepth() {
        return functionPointerDepth;
    }

    @Override
    public boolean isReference() {
        return false; // Not implemented
    }

    @Override
    public boolean isRValueReference() {
        return false; // Not implemented
    }

    @Override
    public Collection<CsmParameter> getParameters() {
        if (functionParameters == null) {
            return Collections.<CsmParameter>emptyList();
        } else {
            return Collections.unmodifiableCollection(functionParameters);
        }
    }
    
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = new ArrayList<>();
        l.addAll(getParameters());
        return l;
    }    

    @Override
    public CsmScope getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
        }
        return scope;
    }

    @Override
    public CsmType getReturnType() {
        return returnType;
    }

    @Override
    public CharSequence decorateText(CharSequence classifierText, CsmType decorator, boolean canonical, CharSequence variableNameToInsert) {
        StringBuilder sb = new StringBuilder();
        if (decorator.isConst()) {
            sb.append("const "); // NOI18N
        }
        sb.append(getReturnType().getText());
        sb.append('(');
        for (int i = 0; i < functionPointerDepth; i++) {
            sb.append('*');
            if (variableNameToInsert != null) {
                sb.append(variableNameToInsert);
            }
        }
        if (decorator.isReference()) {
            sb.append('&');
        }        
        for( int i = 0; i < decorator.getArrayDepth(); i++ ) {
            sb.append("[]"); // NOI18N
        }               
        sb.append(')');
        InstantiationProviderImpl.appendParametersSignature(getParameters(), sb);
        return sb;
    }

    @Override
    public boolean isPointer() {
        // function pointer is always pointer
        return true;
    }

    public static boolean isFunctionPointerParamList(AST asts[], boolean inFunctionParameters) {
        return isFunctionPointerParamList(asts, inFunctionParameters, false);
    }

    public static boolean isFunctionPointerParamList(AST asts[], boolean inFunctionParameters, boolean inTypedef) {
        return initFunctionPointerParamList(asts, null, inFunctionParameters, inTypedef);
    }

    private static boolean initFunctionPointerParamList(AST asts[], TypeFunPtrImpl instance, boolean inFunctionParams, boolean inTypedef) {
        AST ast = asts[asts.length - 1];
        
        AST separator = AstUtil.findSiblingOfType(ast, CPPTokenTypes.COMMA);
        if (separator != null) {
            ast = AstUtil.cloneAST(ast, separator);
        }
        
        FileContent fileContent = null;
        AST next = null;
        // find opening brace
        AST brace = AstUtil.findSiblingOfType(ast, CPPTokenTypes.LPAREN);
        if (brace != null) {
            // check whether it's followed by asterisk
            next = brace.getNextSibling();
            if (next == null) {
                return false;
            }
            if (inFunctionParams && next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                // this is start of function params
                next = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
            } else if (inFunctionParams && next.getType() == CPPTokenTypes.RPAREN) {
                if (AstUtil.findSiblingOfType(ast, CPPTokenTypes.ASSIGNEQUAL, brace) != null) {
                    // int a = foo()
                    return false;
                }
                // int()
                next = null;
            } else if (inTypedef && next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                // typedef void foo_type(...);
            } else if (next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
                // skip adjacent asterisks
                do {
                    next = next.getNextSibling();
                    if (instance != null) {
                        ++instance.functionPointerDepth;
                    }
                } while (next != null && next.getType() == CPPTokenTypes.CSM_PTR_OPERATOR);
            } else if(inTypedef || (inFunctionParams && next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID)) {
                brace = AstUtil.findLastSiblingOfType(ast, CPPTokenTypes.LPAREN);
                AST parmList = brace.getNextSibling();
                if(parmList != null && parmList.getType() == CPPTokenTypes.CSM_PARMLIST) {
                    // typedef void (foo_type)(...);
                    // or
                    // fun-ptr param without '*' like: void foo(void (fun)(void));
                    if (inTypedef) {
                        next = parmList;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (inFunctionParams && next == null) {
            next = AstUtil.findSiblingOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
        }
        if (inFunctionParams && next == null && brace != null) {
            // This could be a function type without name, like int(int)
            next = new FakeAST();
            next.setType(CPPTokenTypes.CSM_QUALIFIED_ID);
            next.setNextSibling(brace);
        }
        if (inFunctionParams && next != null && next.getType() == CPPTokenTypes.RPAREN) {
            next = next.getNextSibling();
        }

        if (next == null) {
            return false;
        }

        // check that it's followed by exprected token
        if (next.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                next.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
            // fine. this could be variable of function type
        } else if (next.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
            AST lookahead = next.getNextSibling();
            AST lookahead2 = lookahead == null ? null : lookahead.getNextSibling();
            if (lookahead != null && lookahead.getType() == CPPTokenTypes.RPAREN) {
                // OK. This could be function type in typedef - in this case we get
                // CSM_QUALIFIED_ID instead of CSM_VARIABLE_DECLARATION.
            } else if (inFunctionParams && lookahead != null && lookahead.getType() == CPPTokenTypes.LPAREN
                    && lookahead2 != null && lookahead2.getType() == CPPTokenTypes.CSM_PARMLIST) {
                // OK. This could be function as a parameter
                next = lookahead;
            } else {
                next = lookahead;
                // check function returns function
                // skip LPAREN (let's not assume it's obligatory)
                if (next == null || next.getType() != CPPTokenTypes.LPAREN) {
                    return false;
                }
                next = next.getNextSibling();
                if (next == null) {
                    return false;
                }
                // skip params of fun itself
                if (next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                    next = next.getNextSibling();
                    if (next == null) {
                        return false;
                    }
                }
                // params of fun are closed with RPAREN
                if (next.getType() != CPPTokenTypes.RPAREN) {
                    return false;
                }
            }
        }

        // typedef void foo_type(...);
        if (inTypedef && next.getType() == CPPTokenTypes.CSM_PARMLIST) {
            if (instance != null) {
                // NB: null passed as scope correspond to null passed to PersistentUtils.readParameters in ctor
                instance.functionParameters = AstRenderer.renderParameters(next, instance.getContainingFile(), fileContent, null);
            }
            return true;
        }       
        
        next = next.getNextSibling();
        
        // skip except specification
        if (next != null && (next.getType() == CPPTokenTypes.LITERAL_throw || next.getType() == CPPTokenTypes.LITERAL_noexcept)) {
            next = AstUtil.findSiblingOfType(next, CPPTokenTypes.RPAREN);
            if (next != null) {
                next = next.getNextSibling(); // closing brace of except specification
            }
        }
        
        if (inFunctionParams) {
            // () without CMS_PARMLIST or <bla()>
            if (next == null) {
                return true;
            } else if (next.getType() == CPPTokenTypes.GREATERTHAN) {
                return true;
            }
        }

        // last step: verify that it's followed with a closing brace        
        if (next != null && next.getType() == CPPTokenTypes.RPAREN) {
            next = next.getNextSibling();
            // skip LPAREN (let's not assume it's obligatory)
            if (next != null && next.getType() == CPPTokenTypes.LPAREN) {
                next = next.getNextSibling();
            }
            if (next == null) {
                return false;
            }
            if (next.getType() == CPPTokenTypes.CSM_PARMLIST) {
                if (instance != null) {
                    instance.functionParameters = AstRenderer.renderParameters(next, instance.getContainingFile(), fileContent, null);
                }
                return true;
            } else if (next.getType() == CPPTokenTypes.RPAREN) {
                return true;
            } else {
                return false;
            }
        } else if (inFunctionParams && next != null && next.getType() == CPPTokenTypes.CSM_PARMLIST) {
            if (instance != null) {
                instance.functionParameters = AstRenderer.renderParameters(next, instance.getContainingFile(), fileContent, null);
            }
            return true;
        } else {
            return false;
        }
    }
    
    private void initFunctionReturnType(AST typeAST, CsmScope scope, CsmFile file) {
        if (typeAST != null) {
            AST fakeParent = new FakeAST();
            fakeParent.addChild(typeAST);
            returnType = AstRenderer.FunctionRenderer.createReturnType(fakeParent, scope, file);
        } else {
            returnType = TypeFactory.createBuiltinType("int", (AST) null, 0,  null, file); // NOI18N
        }
        int retPointerDepth = super.getPointerDepth();
        int retReference = TypeFactory.getReferenceValue(super.isReference(), super.isRValueReference());
        boolean retIsConst = super.isConst();
        boolean retIsVolatile = super.isVolatile();
        returnType = TypeFactory.createType(returnType, retPointerDepth, retReference, returnType.getArrayDepth(), retIsConst, retIsVolatile);
    }
    
    public static int getEndOffset(AST node) {
        AST ast = node;
        if( ast == null ) {
            return 0;
        }
        if (isTypeDefAST(ast)) {
            return OffsetableBase.getEndOffset(ast);
        }
        
        AST lparen = AstUtil.findSiblingOfType(ast, CPPTokenTypes.LPAREN), nextlparen;
        while ((nextlparen = AstUtil.findSiblingOfType(lparen.getNextSibling(), CPPTokenTypes.LPAREN)) != null) {
            lparen = nextlparen;
        }
        
        if (lparen != null) {
            AST rparen = AstUtil.findSiblingOfType(lparen, CPPTokenTypes.RPAREN);
            if (rparen != null) {
                AST pointerTo = AstUtil.findSiblingOfType(rparen, CPPTokenTypes.POINTERTO);
                if (pointerTo != null) {
                    AST type = AstUtil.findSiblingOfType(pointerTo, CPPTokenTypes.CSM_TYPE_BUILTIN);
                    type = type != null ? type : AstUtil.findSiblingOfType(pointerTo, CPPTokenTypes.CSM_TYPE_COMPOUND);
                    type = type != null ? type : AstUtil.findSiblingOfType(pointerTo, CPPTokenTypes.CSM_TYPE_DECLTYPE);
                    if (type != null) {
                        return OffsetableBase.getEndOffset(type);
                    }
                }
                return OffsetableBase.getEndOffset(rparen);
            }
        }
        
        // It seems that the code below never should be executed
        
        if( ast instanceof CsmAST ) {
            return ((CsmAST) ast).getEndOffset();
        }
        return OffsetableBase.getEndOffset(node);
    }
    
    private static AST getLastNode(AST first) {
        AST last = first;
        for( AST token = last; token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                case CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION:
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    return AstUtil.getLastChildRecursively(last);
                case CPPTokenTypes.RPAREN:
                    return AstUtil.getLastChildRecursively(token);
                default:
                    last = token;
            }
        }        
        return null;
    }    

    private static boolean isTypeDefAST(AST ast){
        if (ast != null ) {
            if (ast.getType() == CPPTokenTypes.CSM_FIELD ||
                ast.getType() == CPPTokenTypes.CSM_GENERIC_DECLARATION) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void dispose() {
        super.dispose();
    }
    
    // Dummy classifier for function pointer
    private static class FunctionPointerImpl implements CsmFunctionPointerClassifier, CsmTemplate {

        private final TypeFunPtrImpl type;

        public FunctionPointerImpl(TypeFunPtrImpl type) {
            this.type = type;
        }

        @Override
        public CharSequence getDisplayName() {
            return getName();
        }

        @Override
        public List<CsmTemplateParameter> getTemplateParameters() {
            return null;
        }

        @Override
        public boolean isExplicitSpecialization() {
            return false;
        }

        @Override
        public boolean isSpecialization() {
            return false;
        }

        @Override
        public boolean isTemplate() {
            return false;
        }

        @Override
        public CsmType getReturnType() {
            return type.getReturnType();
        }

        @Override
        public Collection<CsmParameter> getParameters() {
            return type.getParameters();
        }

        @Override
        public CharSequence getSignature() {
            return FunctionImpl.createSignature(getName(), getParameters(), null, type.isConst(), false, false, false);
        }

        @Override
        public CsmDeclaration.Kind getKind() {
            return CsmDeclaration.Kind.FUNCTION_TYPE;
        }

        @Override
        public CharSequence getUniqueName() {
            return CharSequences.create(CharSequenceUtils.concatenate(Utils.getCsmDeclarationKindkey(getKind()), UNIQUE_NAME_SEPARATOR, type.getCanonicalText())); //NOI18N
        }

        @Override
        public CharSequence getQualifiedName() {
            return String.valueOf(getReturnType().getCanonicalText()) + getSignature(); // NOI18N
        }

        @Override
        public CharSequence getName() {
            return ""; // NOI18N
        }

        @Override
        public CsmScope getScope() {
            return type.getScope();
        }

        @Override
        public boolean isValid() {
            return type.isValid();
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
            return type.getText();
        }

        @Override
        public Collection<CsmScopeElement> getScopeElements() {
            return type.getScopeElements();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof FunctionPointerImpl) {
                FunctionPointerImpl other = (FunctionPointerImpl) obj;
                return Objects.equals(other.getQualifiedName(), getQualifiedName());
            }
            return false;
        }

        @Override
        public int hashCode() {
            return getQualifiedName().hashCode();
        }

        @Override
        public String toString() {
            return String.valueOf(getQualifiedName());
        }
    }    

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        output.writeShort(functionPointerDepth);
        PersistentUtils.writeParameters(functionParameters, output);
        UIDObjectFactory.getDefaultFactory().writeUID(scopeUID, output);
        PersistentUtils.writeType(returnType, output);
    }

    public TypeFunPtrImpl(RepositoryDataInput input) throws IOException {
        super(input);
        functionPointerDepth = input.readShort();
        functionParameters = PersistentUtils.readParameters(input, null);
        scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        returnType = PersistentUtils.readType(input);
    }
}
