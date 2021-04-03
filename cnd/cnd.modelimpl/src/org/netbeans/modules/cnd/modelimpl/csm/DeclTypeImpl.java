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

import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.support.CsmTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.ParserThreadManager;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionsFactory;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.CharSequences;

/**
 * TODO: cache resolved type for last instantiations.
 * 
 */
public class DeclTypeImpl extends TypeImpl {
        
    public static boolean isDeclType(AST node) {
        // Note that "typeof" and variations are also considered to be decltype
        if (node != null && node.getFirstChild() != null && AstUtil.findChildOfType(node, CPPTokenTypes.CSM_EXPRESSION) != null) {
            CharSequence childText = AstUtil.getText(node.getFirstChild());
            return CsmTypes.isDecltype(childText);
        }
        return false;
    }    
    
    
    private final CsmExpression typeExpression;
    
    private volatile CsmType cachedType; // TODO: use CsmCacheManager to cache type
    

    DeclTypeImpl(AST ast, CsmFile file, CsmScope scope, boolean packExpansion, int pointerDepth, int reference, int arrayDepth, int constQualifiers, int _volatileQualifiers, int startOffset, int endOffset) {
        super(file, packExpansion, pointerDepth, reference, arrayDepth, constQualifiers, _volatileQualifiers, startOffset, endOffset);
        AST expressionAst = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_EXPRESSION);
        this.typeExpression = ExpressionsFactory.create(expressionAst, file, scope);
    }

    public CsmExpression getTypeExpression() {
        return typeExpression;
    }   
    
    @Override
    public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {    
        CsmClassifier classifier = null;
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                CsmType type = resolve(instantiations);
                classifier = type != null ? type.getClassifier() : null;
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        if (classifier == null) {
            classifier = BuiltinTypes.getBuiltIn(
                    isInitedClassifierText() ? getClassifierText() : CppTokenId.DECLTYPE.fixedText() // Unresolved?
            ); 
        }
        return classifier;
    }    

    @Override
    public boolean isPointer() {
        return isPointer(null);
    }
    
    public boolean isPointer(List<CsmInstantiation> instantiations) {
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                CsmType type = resolve(instantiations);
                return type != null ? type.isPointer() : false;
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        return false;
    }

    @Override
    public boolean isReference() {
        return isReference(null);
    }
    
    public boolean isReference(List<CsmInstantiation> instantiations) {
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                CsmType type = resolve(instantiations);
                return type != null ? type.isReference() : false;
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        return false;
    }

    @Override
    public boolean isConst() {
        return isConst(null);
    }
    
    public boolean isConst(List<CsmInstantiation> instantiations) {
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                CsmType type = resolve(instantiations);
                return type != null ? type.isConst() : false;
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        return false;
    }    

    @Override
    public boolean isRValueReference() {
        return isRValueReference(null);
    }
    
    public boolean isRValueReference(List<CsmInstantiation> instantiations) {
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                CsmType type = resolve(instantiations);
                return type != null ? type.isRValueReference(): false;
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        return false;
    }

    private boolean canUseCache(List<CsmInstantiation> instantiations) {
        // We are allowed to use cache only if context is null
        return instantiations == null;
    }
    
    private CsmType resolve(List<CsmInstantiation> instantiations) {
        CsmType type = null;
        
        if (ParserThreadManager.instance().isParserThread()) {
            return type;
        }

        if (canUseCache(instantiations)) {
            type = cachedType;
        }

        if (type == null) {
            if (canUseCache(instantiations)) {
                if (cachedType == null) {
                    type = CsmExpressionResolver.resolveType(typeExpression, instantiations);
                    cachedType = type;
                } else {
                    type = cachedType;
                }
            } else {
                type = CsmExpressionResolver.resolveType(typeExpression, instantiations);
            }
        }
        
        return (type != this) ? type : null;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        PersistentUtils.writeExpression(typeExpression, output);
    }

    public DeclTypeImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.typeExpression = (ExpressionBase) PersistentUtils.readExpression(input);
    }    
}
