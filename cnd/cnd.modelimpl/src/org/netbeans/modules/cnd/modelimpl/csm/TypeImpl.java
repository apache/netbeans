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
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameterType;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmTypedef;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmIncludeResolver;
import org.netbeans.modules.cnd.api.model.services.CsmInstantiationProvider;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil.ASTTokenVisitor.Action;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionStatementImpl;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver.SafeTemplateBasedProvider;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.netbeans.modules.cnd.utils.cache.TextCache;
import org.openide.util.CharSequences;

/**
 *
 */
public class TypeImpl extends OffsetableBase implements CsmType, SafeTemplateBasedProvider {
    private static final CharSequence NON_INITIALIZED_CLASSIFIER_TEXT = CharSequences.empty();
    private static final int MAX_CLASSIFIER_TEXT_LENGTH = 512;
    
    private static final byte FLAGS_TYPE_OF_TYPEDEF = 1;
    private static final byte FLAGS_REFERENCE = 1 << 1;
    private static final byte FLAGS_CONST = 1 << 2;
    private static final byte FLAGS_TYPE_WITH_CLASSIFIER = 1 << 3;
    private static final byte FLAGS_RVALREFERENCE = 1 << 4;
    private static final byte FLAGS_PACK_EXPANSION = 1 << 5;
    private static final byte FLAGS_VOLOTALE = 1 << 6;
    protected static final int LAST_USED_FLAG_INDEX = 7;
    
    private final byte pointerDepth;
    
    // bit mask of pointerDepth pointers for const qualifiers (supports only pointers with less than 32 depth)
    private final int constQualifiers; 
    private final int volatileQualifiers; 
    
    private final byte arrayDepth;
    private byte flags;
    private CharSequence classifierText;
    private volatile CachePair lastCache = EMPTY_CACHE_PAIR;

    // lazy initialization here, add new params only with addInstantiationParam method
    private ArrayList<CsmSpecializationParameter> instantiationParams = null;

    // FIX for lazy resolver calls
    private CharSequence[] qname = null;
    private volatile CsmUID<CsmClassifier> classifierUID;

    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, int reference, int arrayDepth, AST ast, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.initClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <=2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        boolean _const = isTypeDefAST(ast) ? initIsConst(ast.getFirstChild()) : initIsConst(ast);
        this.constQualifiers = isTypeDefAST(ast) ? initConstQualifiers(ast.getFirstChild()) : initConstQualifiers(ast);
        this.volatileQualifiers = isTypeDefAST(ast) ? initVolatileQualifiers(ast.getFirstChild()) : initVolatileQualifiers(ast);
        setFlags(FLAGS_CONST, _const);
        if (classifier == null) {
            CndUtils.assertTrueInConsole(false, "why null classifier?");
            this.initClassifier(initClassifier(ast));
            this.classifierText = initClassifierText(ast);
        } else {
            setFlags(FLAGS_TYPE_WITH_CLASSIFIER, true);
            CharSequence typeName = classifier.getName();
            if (typeName == null || typeName.length()==0){
                this.classifierText = initClassifierText(ast);
            } else {
                this.classifierText = typeName;
            }
        }
        if (this.classifierText == null) {
            CndUtils.assertTrueInConsole(false, "why null classifierText?"+classifier);
            this.classifierText = NON_INITIALIZED_CLASSIFIER_TEXT;
        }
        trimInstantiationParams();
    }
    
    // package-local - for facory only
    TypeImpl(CsmClassifier classifier, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.initClassifier(classifier);
        this.pointerDepth = (byte) pointerDepth;
        this.constQualifiers = _const ? 1 : 0;
        this.volatileQualifiers = _volatile ? 1 : 0;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <= 2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
        setFlags(FLAGS_VOLOTALE, _volatile);
        setFlags(FLAGS_TYPE_WITH_CLASSIFIER, true);
        this.classifierText = classifier.getName();
        trimInstantiationParams();
    }  
    
    // package-local - for facory only
    TypeImpl(CsmFile file, boolean packExpansion, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile, int startOffset, int endOffset) {
        this(file, packExpansion, pointerDepth, reference, arrayDepth, _const ? 1 : 0, _volatile ? 1 : 0, startOffset, endOffset);
    }    
    
    // package-local - for facory only
    TypeImpl(CsmFile file, boolean packExpansion, int pointerDepth, int reference, int arrayDepth, int _constQualifiers, int _volatileQualifiers, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.classifierText = NON_INITIALIZED_CLASSIFIER_TEXT;
        this.pointerDepth = (byte) pointerDepth;
        this.constQualifiers = _constQualifiers;
        this.volatileQualifiers = _volatileQualifiers;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <= 2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, ((_constQualifiers & 1) != 0)); // this is mistake (first const qualifier is the deepest one)
        setFlags(FLAGS_VOLOTALE, ((_volatileQualifiers & 1) != 0));
        setFlags(FLAGS_PACK_EXPANSION, packExpansion);
        trimInstantiationParams();
    }       

    // package-local - for factory only
    TypeImpl(TypeImpl type, int pointerDepth, int reference, int arrayDepth, boolean _const, boolean _volatile) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) pointerDepth;
        this.constQualifiers = _const ? 1 : 0;
        this.volatileQualifiers = _volatile ? 1 : 0;
        // nothing, & or && as reference specifier
        // too slow my typing is too slow
        assert reference >= 0 && reference <= 2 : "unexpected " + reference;
        setFlags(FLAGS_REFERENCE, reference > 0);
        setFlags(FLAGS_RVALREFERENCE, reference == 2);
        this.arrayDepth = (byte) arrayDepth;
        setFlags(FLAGS_CONST, _const);
        setFlags(FLAGS_VOLOTALE, _volatile);
        setFlags(FLAGS_TYPE_OF_TYPEDEF, type.isTypeOfTypedef());
        
        this.classifierUID = type.classifierUID;
        this.qname = type.qname;
        this.classifierText = type.classifierText;
        addAllInstantiationParams(type.instantiationParams);
        trimInstantiationParams();
    }

    public void setTypeOfTypedef() {
        setFlags(FLAGS_TYPE_OF_TYPEDEF, true);
    }

    protected boolean hasFlags(byte mask) {
        return (flags & mask) == mask;
    }

    private void setFlags(byte mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
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

    // package-local - for factory only
    TypeImpl(TypeImpl type, List<CsmSpecializationParameter> instantiationParams) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) type.getPointerDepth();
        this.constQualifiers = type.constQualifiers;
        this.volatileQualifiers = type.volatileQualifiers;
        setFlags(FLAGS_REFERENCE, type.isReference());
        this.arrayDepth = (byte) type.getArrayDepth();
        setFlags(FLAGS_CONST, type.isConst());
        setFlags(FLAGS_VOLOTALE, type.isVolatile());
        setFlags(FLAGS_TYPE_OF_TYPEDEF, type.isTypeOfTypedef());
        setFlags(FLAGS_TYPE_WITH_CLASSIFIER, type.isTypeWithClassifier());

        this.classifierUID = type.classifierUID;
        this.qname = type.qname;
        this.classifierText = type.classifierText;
        addAllInstantiationParams(instantiationParams);
        trimInstantiationParams();
    }


    // package-local
    TypeImpl(CsmType type) {
        super(type.getContainingFile(), type.getStartOffset(), type.getEndOffset());

        this.pointerDepth = (byte) type.getPointerDepth();
        this.constQualifiers = type.isConst() ? 1 : 0;
        this.volatileQualifiers = type.isVolatile() ? 1 : 0;
        setFlags(FLAGS_REFERENCE, type.isReference());
        this.arrayDepth = (byte) type.getArrayDepth();
        setFlags(FLAGS_CONST, type.isConst());
        setFlags(FLAGS_VOLOTALE, type.isVolatile());

        if (type instanceof TypeImpl) {
            TypeImpl ti = (TypeImpl) type;
            setFlags(FLAGS_TYPE_OF_TYPEDEF, ti.isTypeOfTypedef());
            setFlags(FLAGS_TYPE_WITH_CLASSIFIER, ti.isTypeWithClassifier());
            this.classifierUID = ti.classifierUID;
            this.qname = ti.qname;
            this.classifierText = ti.classifierText;
            addAllInstantiationParams(ti.instantiationParams);
        }
        trimInstantiationParams();
    }

     /*TypeImpl(AST ast, CsmFile file, int pointerDepth, boolean reference, int arrayDepth) {
        this(null, pointerDepth, reference, arrayDepth, ast, file, null);
     }*/
    
    public static int getStartOffset(AST node) {
        if (AstUtil.isElaboratedKeyword(node)) {
            AST ast = node.getNextSibling();
            return OffsetableBase.getStartOffset(ast != null ? ast : node);
        }
        return OffsetableBase.getStartOffset(node);
    }
    
    public static int getEndOffset(AST node) {
        return getEndOffset(node, false);
    }

    public static int getEndOffset(AST node, boolean greedy) {
        AST ast = node;
        if( ast == null ) {
            return 0;
        }
        if (isTypeDefAST(ast)) {
            return OffsetableBase.getEndOffset(ast);
        }
        ast = getLastNode(ast, greedy);
        if (ast == null && AstUtil.isElaboratedKeyword(node)) {
            ast = node.getNextSibling();
            return OffsetableBase.getEndOffset(ast != null ? ast : node);
        }
        if( ast instanceof OffsetableAST ) {
            return ((OffsetableAST) ast).getEndOffset();
        }
        return OffsetableBase.getEndOffset(node);
    }

    private static AST getLastNode(AST first, boolean greedy) {
        AST last = first;
        int parensDepth = 0;
        int squareBracketsDepth = 0;
        int angleBracketsDepth = 0;
        if(last != null) {   
            outer:
            for( AST token = last.getNextSibling(); token != null; token = token.getNextSibling() ) {
                switch( token.getType() ) {                    
                    case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                    case CPPTokenTypes.CSM_VARIABLE_LIKE_FUNCTION_DECLARATION:
                    case CPPTokenTypes.CSM_QUALIFIED_ID:
                    case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                        if (AstUtil.isElaboratedKeyword(last)) {
                            last = token;
                            continue;
                        }
                        return AstUtil.getLastChildRecursively(last);
                        
                    case CPPTokenTypes.LPAREN: 
                        ++parensDepth;
                        // LPAREN cannot be last - entity's end should be on the left side of LPAREN
                        // if there are no type modificators after lparen (*, &, ...)
                        break;
                        
                    case CPPTokenTypes.RPAREN:
                        if (--parensDepth < 0 && !(angleBracketsDepth > 0 || squareBracketsDepth > 0)) {
                            break outer;
                        }
                        last = token;
                        break;
                        
                    case CPPTokenTypes.LESSTHAN:
                        ++angleBracketsDepth;
                        // LESSTHAN cannot be last - entity's end should be on the left side of LESSTHAN
                        break;
                        
                    case CPPTokenTypes.GREATERTHAN:
                        if (--angleBracketsDepth < 0 && !(parensDepth > 0 || squareBracketsDepth > 0)) {
                            break outer;
                        }
                        last = token;
                        break;
                        
                    case CPPTokenTypes.LSQUARE:
                        ++squareBracketsDepth;
                        // LSQUARE cannot be last - entity's end should be on the left side of LSQUARE
                        break;
                        
                    case CPPTokenTypes.RSQUARE:
                        if (--squareBracketsDepth < 0 && !(parensDepth > 0 || angleBracketsDepth > 0)) {
                            break outer;
                        }
                        last = token;
                        break;      
                        
                    case CPPTokenTypes.COMMA:
                        if (!(squareBracketsDepth > 0 || parensDepth > 0 || angleBracketsDepth > 0)) {
                            break outer;
                        }
                        last = token;
                        break;                              
                                            
                    default:
                        last = token;
                }
            }
            if (greedy) {
                return AstUtil.getLastChildRecursively(last);
            }
        }
        return null;
    }

    @Override
    public boolean isReference() {
        return hasFlags(FLAGS_REFERENCE);
    }

    @Override
    public boolean isRValueReference() {
        return hasFlags(FLAGS_RVALREFERENCE);
    }
    
    @Override
    public boolean isPointer() {
        return pointerDepth > 0;
    }

    @Override
    public boolean isPackExpansion() {
        return hasFlags(FLAGS_PACK_EXPANSION);
    }

    private boolean isTypeOfTypedef() {
        return hasFlags(FLAGS_TYPE_OF_TYPEDEF);
    }

    private boolean isTypeWithClassifier() {
        return hasFlags(FLAGS_TYPE_WITH_CLASSIFIER);
    }

    @Override
    public List<CsmSpecializationParameter> getInstantiationParams() {
        return instantiationParams == null ? Collections.<CsmSpecializationParameter>emptyList() : instantiationParams;
    }

    @Override
    public boolean isInstantiation() {
        return instantiationParams != null;
    }

    @Override
    public boolean hasInstantiationParams() {
        return instantiationParams != null;
    }

    /** Though it returns the same for now, it's better if its name differs */
    protected boolean isInstantiationOrSpecialization() {
        return instantiationParams != null;
    }
    
    final void trimInstantiationParams() {
        if (instantiationParams != null) {
            instantiationParams.trimToSize();
        }
    }
    
    final void initInstantiationParams() {
        if (instantiationParams == null) {
            instantiationParams = new ArrayList<>();
        }
    }
    
    final void addAllInstantiationParams(Collection<CsmSpecializationParameter> params) {
        if (params == null || params.isEmpty()) {
            return;
        }
        if (instantiationParams == null) {
            instantiationParams = new ArrayList<>(params.size());
        }
        instantiationParams.addAll(params);
    }
    
    final void addInstantiationParam(CsmSpecializationParameter param) {
        if (instantiationParams == null) {
            instantiationParams = new ArrayList<>();
        }
        instantiationParams.add(param);
    }

    @Override
    public boolean isTemplateBased() {
        return isTemplateBased(new HashSet<CsmType>());
    }

    @Override
    public boolean isTemplateBased(Set<CsmType> visited) {
        CsmClassifier classifier = getClassifier();
        if (CsmKindUtilities.isTypedef(classifier) || CsmKindUtilities.isTypeAlias(classifier)) {
            if (visited.contains(this)) {
                return false;
            }
            visited.add(this);
            CsmType type = ((CsmTypedef)classifier).getType();
            if (type instanceof SafeTemplateBasedProvider) {
                return ((SafeTemplateBasedProvider)type).isTemplateBased(visited);
            } else {
                return type.isTemplateBased();
            }
        }
        return false;
    }

    public static boolean initIsConst(AST node) {
        if( node != null ) {
            for( AST token = node; token != null; token = token.getNextSibling() ) {
                int tokenType = token.getType();
                if (AstRenderer.isConstQualifier(tokenType)) {
                    return true;
                } else if (tokenType == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                               tokenType == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    return false;
                }
            }
        }
        return false;
    }

    public static boolean initIsVolatile(AST node) {
        if( node != null ) {
            for( AST token = node; token != null; token = token.getNextSibling() ) {
                int tokenType = token.getType();
                if (AstRenderer.isVolatileQualifier(tokenType)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean initIsPackExpansion(AST node) {
        if (node != null) {
            for (AST token = node; token != null; token = token.getNextSibling()) {
                int tokenType = token.getType();
                if (tokenType == CPPTokenTypes.ELLIPSIS) {
                    return true;
                } else if (tokenType == CPPTokenTypes.CSM_VARIABLE_DECLARATION
                    || tokenType == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
                    return token.getFirstChild() != null 
                        && token.getFirstChild().getType() == CPPTokenTypes.ELLIPSIS;
                }
            }
        }
        return false;
    }
    
    public static int initConstQualifiers(AST node) {
        int result = 0;
        
        if (node != null) {
            
            int constQualifierPosition = 1;
            
            for (AST token = node; token != null; token = token.getNextSibling()) {
                int tokenType = token.getType();                
               
                if (AstRenderer.isConstQualifier(tokenType)) {
                    result |= constQualifierPosition;
                } else if (tokenType == CPPTokenTypes.CSM_PTR_OPERATOR) {                    
                    ASTPointerOperatorQualifiersCollector visitor = new ASTPointerOperatorQualifiersCollector(result, constQualifierPosition, true);
                    
                    visitPointerOperator(visitor, token);
                    
                    result = visitor.getQualifiers();                  
                    
                    constQualifierPosition = visitor.getQualifierPosition();                    
                } else if (tokenType == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    return result;
                } else if (tokenType == CPPTokenTypes.LPAREN) {
                    // if two tokens in a row are LPAREN and RPAREN, it is probably cast operator: type ()
                    if (checkTokensRow(token.getNextSibling(), CPPTokenTypes.RPAREN)) {
                        return result;
                    }
                }
            }
        }
        
        return result;        
    }
    
    public static int initVolatileQualifiers(AST node) {
        int result = 0;
        
        if (node != null) {
            
            int volatileQualifierPosition = 1;
            
            for (AST token = node; token != null; token = token.getNextSibling()) {
                int tokenType = token.getType();                
               
                if (AstRenderer.isVolatileQualifier(tokenType)) {
                    result |= volatileQualifierPosition;
                } else if (tokenType == CPPTokenTypes.CSM_PTR_OPERATOR) {                    
                    ASTPointerOperatorQualifiersCollector visitor = new ASTPointerOperatorQualifiersCollector(result, volatileQualifierPosition, false);
                    
                    visitPointerOperator(visitor, token);
                    
                    result = visitor.getQualifiers();                  
                    
                    volatileQualifierPosition = visitor.getQualifierPosition();                    
                } else if (tokenType == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_ARRAY_DECLARATION ||
                           tokenType == CPPTokenTypes.CSM_QUALIFIED_ID) {
                    return result;
                } else if (tokenType == CPPTokenTypes.LPAREN) {
                    // if two tokens in a row are LPAREN and RPAREN, it is probably cast operator: type ()
                    if (checkTokensRow(token.getNextSibling(), CPPTokenTypes.RPAREN)) {
                        return result;
                    }
                }
            }
        }
        
        return result;        
    }

    /**
     * Visits tokens inside pointer operators
     * 
     * @param visitor
     * @param ptrOperator
     * @return true if visiting was finished successfully, false if aborted
     */
    public static boolean visitPointerOperator(AstUtil.ASTTokenVisitor visitor, AST ptrOperator) {
        if (ptrOperator != null && ptrOperator.getType() == CPPTokenTypes.CSM_PTR_OPERATOR) {
            for (AST insideToken = ptrOperator.getFirstChild(); insideToken != null; insideToken = insideToken.getNextSibling()) {
                if (insideToken.getType() != CPPTokenTypes.CSM_PTR_OPERATOR) {
                    if (visitor.visit(insideToken) == Action.ABORT) {
                        return false;
                    }                        
                } else {
                    if (!visitPointerOperator(visitor, insideToken)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }
    
    private static boolean checkTokensRow(AST token, int ... expectedTokens) {
        for (int expected : expectedTokens) {
            if (token == null) {
                return false;
            }
            if (token.getType() != expected) {
                return false;
            }
            token = token.getNextSibling();
        }        
        return true;
    }

    @Override
    public boolean isConst() {
        return isConst(0);
    }

    public boolean isConst(int pointerDepth) {
        return isConst(constQualifiers, pointerDepth);
    }
    
    static boolean isConst(int constQualifiers, int pointerDepth) {
        return (constQualifiers & (1 << pointerDepth)) != 0;
    }

    public boolean isVolatile(int pointerDepth) {
        return isVolatile(volatileQualifiers, pointerDepth);
    }
    
    static boolean isVolatile(int volatileQualifiers, int pointerDepth) {
        return (volatileQualifiers & (1 << pointerDepth)) != 0;
    }

    @Override
    public boolean isVolatile() {
        return hasFlags(FLAGS_VOLOTALE);
    }
    
    @Override
    public CharSequence getCanonicalText() {
        CharSequence text = getClassifierText();
        if (isInstantiationOrSpecialization()) {
            text = CharSequenceUtils.concatenate(text, Instantiation.getInstantiationCanonicalText(this.instantiationParams));
        }
	return decorateText(text, this, true, null);
    }

    /*package*/static CharSequence getCanonicalText(CsmType type) {
        CharSequence canonicalText = null;
        if (type instanceof CsmTemplateParameterType) {
            CsmTemplateParameterType parType = (CsmTemplateParameterType) type;
            CsmTemplateParameter par = parType.getParameter();
            if (CsmKindUtilities.isClassifierBasedTemplateParameter(par)) {
                canonicalText = TemplateUtils.TYPENAME_STRING;
            }
        }
        if (canonicalText == null) {
            canonicalText = type.getCanonicalText();
        }
        return canonicalText;
    }


    // package
    public CharSequence getOwnText() {
        if (qname != null && qname.length>0) {
            return qname[qname.length-1];
        } else {
            return "";
        }
    }

    @Override
    public CharSequence getText() {
	// TODO: resolve typedefs
        CharSequence instantiationText = getInstantiationText(this);
        if (instantiationText.length() == 0) {
            return decorateText(getClassifierText(), this, false, null);
        } else {
            return decorateText(CharSequenceUtils.concatenate(getClassifierText(), instantiationText), this, false, null);
        }
    }

    protected CharSequence getText(boolean canonical, CharSequence variableNameToInsert) {
        CharSequence instantiationText = getInstantiationText(this);
        if (instantiationText.length() == 0) {
            return decorateText(getClassifierText(), this, canonical, variableNameToInsert);
        } else {
            return decorateText(CharSequenceUtils.concatenate(getClassifierText(), instantiationText), this, canonical, variableNameToInsert);
        }
    }

    public CharSequence decorateText(CharSequence classifierText, CsmType decorator, boolean canonical, CharSequence variableNameToInsert) {
        if (decorator.isConst() || decorator.isVolatile() || decorator.getPointerDepth() > 0 || 
            decorator.isReference() || decorator.getArrayDepth() > 0 ||
            variableNameToInsert != null) {
            StringBuilder sb = new StringBuilder();
            if( decorator.isConst() ) {
                sb.append("const "); // NOI18N
            }
            if( decorator.isVolatile() ) {
                sb.append("volatile "); // NOI18N
            }
            sb.append(classifierText);
            for( int i = 1; i <= decorator.getPointerDepth(); i++ ) {
                sb.append('*');
                if (isConst(i)) {
                    sb.append(" const"); // NOI18N
                } else if (isVolatile(i)) {
                    sb.append(" volatile"); // NOI18N
                }
            }
            if (decorator.isRValueReference()) {
                sb.append("&&"); // NOI18N
            } else if( decorator.isReference() ) {
                sb.append('&');
            }
            if(canonical) {
                for( int i = 0; i < decorator.getArrayDepth(); i++ ) {
                    sb.append("*"); // NOI18N
                }
                if (variableNameToInsert != null && !(variableNameToInsert.length() == 0)) {
                    sb.append(' ');
                    sb.append(variableNameToInsert);
                }
            } else {
                if (variableNameToInsert != null && !(variableNameToInsert.length() == 0)) {
                    sb.append(' ');
                    sb.append(variableNameToInsert);
                }              
                for( int i = 0; i < decorator.getArrayDepth(); i++ ) {
                    sb.append("[]"); // NOI18N
                }
            }
            return sb;
        }
        return classifierText;
    }

    final CharSequence initClassifierText(AST node) {
        if( node == null ) {
            CsmClassifier classifier = _getClassifier();
            return classifier == null ? "" : classifier.getName();
        }
        else {
            StringBuilder sb = new StringBuilder();
            addText(sb, AstRenderer.getFirstSiblingSkipQualifiers(node));
            return TextCache.getManager().getString(sb);
//            return sb.toString();
        }
    }

    /*
     * Add text without instantiation params
     */
    private static boolean addText(StringBuilder sb, AST ast) {
        if( ! (ast instanceof FakeAST) ) {
            if( sb.length() > 0 ) {
                sb.append(' ');
            }
            sb.append(AstUtil.getText(ast));
        }
        if (sb.length() < MAX_CLASSIFIER_TEXT_LENGTH) {
            int curDepth = 0;
            for( AST token = ast.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if (token.getType() == CPPTokenTypes.LESSTHAN) {
                    curDepth++;
                    continue;
                } else if (token.getType() == CPPTokenTypes.GREATERTHAN) {
                    curDepth--;
                    continue;
                }
                if (curDepth == 0) {
                    if (!addText(sb,  token)) {
                        return false;
                    }
                }
            }
            return true;
        } else {
            sb.append("..."); // NOI18N
            return false;
        }
    }

    public static CharSequence getInstantiationText(CsmType type) {
        if (type.hasInstantiationParams()) {
            StringBuilder sb = new StringBuilder();
            sb.append('<');
            boolean first = true;
            for (CsmSpecializationParameter param : type.getInstantiationParams()) {
                if (first) {
                    first = false;
                } else {
                    sb.append(',');
                }
                sb.append(param.getText());
            }
            TemplateUtils.addGREATERTHAN(sb);
            return sb;
        }
	return CharSequences.empty();
    }

    @Override
    public CharSequence getClassifierText() {
        return classifierText;
    }

    boolean isInitedClassifierText() {
        return classifierText != NON_INITIALIZED_CLASSIFIER_TEXT;
    }

    void setClassifierText(CharSequence classifierText) {
        this.classifierText = classifierText;
    }

    void setQName(CharSequence[] qname) {
        this.qname = qname;
    }

    @Override
    public CsmClassifier getClassifier() {
        return getClassifier(null, false);
    }
    
    public CsmClassifier getClassifier(List<CsmInstantiation> instantiations, boolean specialize) {
        CsmClassifier classifier = _getClassifier();
        boolean needToRender = true;
        if (CsmBaseUtilities.isValid(classifier)) {
            // skip
            needToRender = false;
            Resolver parent = ResolverFactory.getCurrentResolver();
            if (!isTypeWithClassifier() && (qname != null) && (parent != null) && !CsmKindUtilities.isBuiltIn(classifier)) {
                // check visibility of classifier
                if (ForwardClass.isForwardClass(classifier) || !CsmIncludeResolver.getDefault().isObjectVisible(parent.getStartFile(), classifier)) {
                    needToRender = true;
                    classifier = null;
                }
            }
        }
        if (needToRender) {
            CachePair newCachePair = new CachePair(FileImpl.getParseCount(), ResolverFactory.getCurrentStartFile(this));
            if (classifier == null || !newCachePair.equals(lastCache)) {                
                if (qname != null && qname.length > 0) {
                    classifier = renderClassifier(qname);
                } else if (classifierText.length() > 0) {
                    classifier = renderClassifier(new CharSequence[] { classifierText });
                }
                synchronized (this) {
                    _setClassifier(classifier);
                    lastCache = newCachePair;
                }
                classifier = _getClassifier();
            }
        }
        if (isInstantiation() && CsmKindUtilities.isTemplate(classifier) && !((CsmTemplate)classifier).getTemplateParameters().isEmpty()) {
            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            CsmObject obj;
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(this);
                try {
                    if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) classifier, this, specialize);
                        if(CsmKindUtilities.isInstantiation(obj)) {
                            if (instantiations == null) {
                                instantiations = new ArrayList<>();
                            }
                            instantiations.add((CsmInstantiation)obj);
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
                obj = specialize((CsmClassifier) obj, instantiations);
                classifier = (CsmClassifier) obj;
            }
        }
        return classifier;
    }

    public CsmObject specialize(CsmClassifier classifier, List<CsmInstantiation> instantiations) {
        CsmObject obj = classifier;
        if(instantiations != null && !instantiations.isEmpty()) {
            List<CsmInstantiation> originalInstantiations = new ArrayList<>();
            while (CsmKindUtilities.isInstantiation(obj)) {
                originalInstantiations.add((CsmInstantiation)obj);
                obj = ((CsmInstantiation)obj).getTemplateDeclaration();
            }

            CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
            if (ip instanceof InstantiationProviderImpl) {
                Resolver resolver = ResolverFactory.createResolver(this);
                try {
                    if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                        for (int i = instantiations.size() - 1; i > 0; i--) {
                            obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) obj, instantiations.get(i), false);
                        }
                        obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) obj, instantiations.get(0), true);
                    }
                } finally {
                    ResolverFactory.releaseResolver(resolver);
                }
            }

            if(!CsmKindUtilities.isSpecialization(obj)) {
                while (CsmKindUtilities.isInstantiation(obj)) {
                    obj = ((CsmInstantiation)obj).getTemplateDeclaration();
                }
                if (ip instanceof InstantiationProviderImpl) {
                    Resolver resolver = ResolverFactory.createResolver(this);
                    try {
                        if (!resolver.isRecursionOnResolving(Resolver.INFINITE_RECURSION)) {
                            for (int i = originalInstantiations.size() - 1; i >= 0; i--) {
                                obj = ((InstantiationProviderImpl) ip).instantiate((CsmTemplate) obj, originalInstantiations.get(i), false);
                            }
                        }
                    } finally {
                        ResolverFactory.releaseResolver(resolver);
                    }
                }
            }
        }
        return obj;
    }     
    
    protected CsmClassifier renderClassifier(CharSequence[] qname) {
        CsmClassifier result = null;
        if (!isValid()) {
            return result;
        }
        Resolver resolver = ResolverFactory.createResolver(this);
        try {
            boolean searchSpecializations = !TraceFlags.COMPLETE_EXPRESSION_EVALUATOR;
            if (isInstantiationOrSpecialization() && searchSpecializations) {
                CharSequence[] specializationQname = new CharSequence[qname.length];
                final int last = qname.length - 1;
                StringBuilder sb = new StringBuilder(qname[last]);
                sb.append(Instantiation.getInstantiationCanonicalText(this.instantiationParams));
                specializationQname[last] = sb.toString();
                System.arraycopy(qname, 0, specializationQname, 0, last);
                CsmObject o = resolver.resolve(specializationQname, Resolver.CLASSIFIER);
                if( CsmKindUtilities.isClassifier(o) ) {
                    result = (CsmClassifier) o;
                }
                if (result == null) {
                    specializationQname[last] = qname[last].toString() + "<>"; //NOI18N
                    o = resolver.resolve(specializationQname, Resolver.CLASSIFIER);
                    if( CsmKindUtilities.isClassifier(o) ) {
                        result = (CsmClassifier) o;
                    }
                }
            }
            if (result == null) {
                CsmObject o = resolver.resolve(qname, Resolver.CLASSIFIER);
                if( CsmKindUtilities.isClassifier(o) ) {
                    result = (CsmClassifier) o;
                }
            }
            if (isInstantiationOrSpecialization() && !searchSpecializations) {
                // If we do not need specializations we must check that resolver returns us not a specialization
                if (CsmKindUtilities.isClassifier(result) && CsmKindUtilities.isSpecialization(result)) {
                    CsmInstantiationProvider ip = CsmInstantiationProvider.getDefault();
                    Collection<CsmOffsetableDeclaration> baseDecls = ip.getBaseTemplate(result);
                    for (CsmOffsetableDeclaration decl : baseDecls) {
                        if (CsmKindUtilities.isClassifier(decl) && !CsmKindUtilities.isSpecialization(decl)) {
                            result = (CsmClassifier) decl;
                            break;
                        }
                    }
                }
                // And not instantiation as well (TODO: maybe that should be outside this if block)
                while (CsmKindUtilities.isInstantiation(result) && CsmKindUtilities.isClassifier(((CsmInstantiation) result).getTemplateDeclaration())) {
                    result = (CsmClassifier) ((CsmInstantiation) result).getTemplateDeclaration();
                }
            }
        } finally {
            ResolverFactory.releaseResolver(resolver);
        }
        if( result == null ) {
            result = ProjectBase.getDummyForUnresolved(qname, this);
        }
        return result;
    }

    private CsmClassifier initClassifier(AST node) {
        AST tokType = AstRenderer.getFirstSiblingSkipQualifiers(node);
        if (tokType == null ||
                (tokType.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN &&
                tokType.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND) &&
                tokType.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
            return null;
        }

        if (tokType.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN) {
            return BuiltinTypes.getBuiltIn(tokType);
        } else { // tokType.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
            try {
                CsmAST tokFirstId = (CsmAST) tokType.getFirstChild();
                if (tokFirstId == null) {
                    // this is unnormal; but we should be able to work even on incorrect AST
                    return null;
                }

                //Resolver resolver = ResolverFactory.createResolver(getContainingFile(), firstOffset);
                // gather name components into string array
                // for example, for std::vector new CharSequence[] { "std", "vector" }

                //TODO: we have AstRenderer.getNameTokens, it is better to use it here
                List<CharSequence> l = new ArrayList<>();
                int templateDepth = 0;
                for (AST namePart = tokFirstId; namePart != null; namePart = namePart.getNextSibling()) {
                    if (templateDepth == 0 && namePart.getType() == CPPTokenTypes.IDENT) {
                        l.add(NameCache.getManager().getString(AstUtil.getText(namePart)));
                    } else if (namePart.getType() == CPPTokenTypes.LESSTHAN) {
                        // the beginning of template parameters
                        templateDepth++;
                    } else if (namePart.getType() == CPPTokenTypes.GREATERTHAN) {
                        // the beginning of template parameters
                        templateDepth--;
                    } else {
                        //assert namePart.getType() == CPPTokenTypes.SCOPE;
                        if (templateDepth == 0) {
                            if (namePart.getType() != CPPTokenTypes.SCOPE) {
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
                            // TODO: maybe we need to filter out some more tokens
                            if (namePart.getType() == CPPTokenTypes.CSM_TYPE_BUILTIN
                                    || namePart.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND
                                    || namePart.getType() == CPPTokenTypes.LITERAL_struct) {
                                CsmType type = AstRenderer.renderType(namePart, getContainingFile(), true, null, false); // last two params just dummy ones
                                addInstantiationParam(new TypeBasedSpecializationParameterImpl(type, null)); // TODO: null?
                            }
                            if (namePart.getType() == CPPTokenTypes.CSM_EXPRESSION) {
                                addInstantiationParam(ExpressionBasedSpecializationParameterImpl.create(ExpressionStatementImpl.create(namePart, getContainingFile(), null),
                                        getContainingFile(), OffsetableBase.getStartOffset(namePart), OffsetableBase.getEndOffset(namePart)));
                            }
                        }
                    }
                }
                qname = l.toArray(new CharSequence[l.size()]);
            /*CsmObject o = resolver.resolve(qname);
            if( CsmKindUtilities.isClassifier(o) ) {
            result = (CsmClassifier) o;
            }
            //		else if( CsmKindUtilities.isTypedef(o) ) {
            //		    CsmTypedef td = (CsmTypedef) o;
            //		    CsmType type = td.getType();
            //		    if( type != null ) {
            //			result = type.getClassifier();
            //		    }
            //		}
            if( result == null ) {
            result = ((ProjectBase) getContainingFile().getProject()).getDummyForUnresolved(qname, getContainingFile(), offset);
            }*/
            } catch (Exception e) {
                DiagnosticExceptoins.register(e);
            }
        }
        return null;
    }    

    @Override
    public int getArrayDepth() {
        return arrayDepth;
    }

    @Override
    public int getPointerDepth() {
        return pointerDepth;
    }

    protected CsmClassifier _getClassifier() {
        CsmClassifier classifier = null;
        if (classifierUID != null) {
            classifier = UIDCsmConverter.UIDtoDeclaration(classifierUID);
        } else {
            FileImpl file = (FileImpl) getContainingFile();
            if (file != null) {
                CsmReference typeReference = file.getResolvedReference(new CsmTypeReferenceImpl(this));
                if (typeReference != null) {
                    CsmObject referencedObject = typeReference.getReferencedObject();
                    if (CsmKindUtilities.isClassifier(referencedObject)) {
                        classifier = (CsmClassifier) referencedObject;
                        //System.out.println("Hit "+classifier);
                    }
                }
            }
        }
        // can be null if cached one was removed
        return classifier;
    }
    
    protected final boolean isClassifierInitialized() {
        return classifierUID != null;
    }

    public final void initClassifier(CsmClassifier classifier) {
        this.classifierUID = UIDCsmConverter.declarationToUID(classifier);
        assert (classifierUID != null || classifier == null);
    }

    final void _setClassifier(final CsmClassifier classifier) {
        // remove old cached value
        CsmFile csmFile = getContainingFile();
        if(csmFile instanceof FileImpl) {
            FileImpl fileImpl = (FileImpl) csmFile;
            fileImpl.removeResolvedReference(new CsmTypeReferenceImpl(this));
            CsmUID<CsmClassifier> cUID = UIDCsmConverter.declarationToUID(classifier);
            this.classifierUID = cUID;
            // register new cached value
            if (cUID != null && classifier != null && !CsmKindUtilities.isBuiltIn(classifier) && CsmBaseUtilities.isValid(classifier) 
                  && !CsmKindUtilities.isTypedef(classifier) && !CsmKindUtilities.isTypeAlias(classifier)
                //&& !CsmKindUtilities.isTemplate(classifier) && !isInstantiation()
               ) {
               fileImpl.addResolvedReference(new CsmTypeReferenceImpl(this), classifier);
            }
            assert (cUID != null || classifier == null);
        }
    }

    @Override
    public boolean isBuiltInBased(boolean resolveTypeChain) {
        CsmClassifier classifier;
        if (resolveTypeChain) {
            classifier = getClassifier();
            if (CsmKindUtilities.isTypedef(classifier) || CsmKindUtilities.isTypeAlias(classifier)) {
                return ((CsmTypedef)classifier).getType().isBuiltInBased(true);
            }
        } else {
            classifier = _getClassifier();
        }
        return CsmKindUtilities.isBuiltIn(classifier);
    }


    @Override
    public String toString() {
        return "TYPE " + getText()  + getOffsetString(); // NOI18N
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of persistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        output.writeByte(pointerDepth);
        output.writeInt(constQualifiers);
        output.writeInt(volatileQualifiers);
        output.writeByte(arrayDepth);
        output.writeByte(flags);
        assert this.classifierText != null;
        PersistentUtils.writeUTF(classifierText, output);

        PersistentUtils.writeStrings(qname, output);
        PersistentUtils.writeSpecializationParameters(instantiationParams, output);
        
        CsmUID<?> uid = this.classifierUID;
        if(!UIDProviderIml.isPersistable(uid)) {
            uid = null;
        }
        UIDObjectFactory.getDefaultFactory().writeUID(uid, output);
    }

    // Proxy list to be able to work with null instantiationParams collection
    private class ProxyParamsList extends AbstractList<CsmSpecializationParameter> {
        @Override
        public boolean add(CsmSpecializationParameter e) {
            if (instantiationParams == null) {
                instantiationParams = new ArrayList<>();
            }
            return instantiationParams.add(e);
        }
        
        @Override
        public CsmSpecializationParameter get(int index) {
            if (instantiationParams != null) {
                return instantiationParams.get(index);
            }
            throw new IndexOutOfBoundsException("Index: "+index+", Size: 0"); //NOI18N
        }

        @Override
        public int size() {
            if (instantiationParams != null) {
                return instantiationParams.size();
            }
            return 0;
        }
    }

    public TypeImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.pointerDepth = input.readByte();
        this.constQualifiers = input.readInt();
        this.volatileQualifiers = input.readInt();
        this.arrayDepth= input.readByte();
        this.flags = input.readByte();
        this.classifierText = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.classifierText != null;

        this.qname = PersistentUtils.readStrings(input, NameCache.getManager());
        PersistentUtils.readSpecializationParameters(new ProxyParamsList(), input);
        trimInstantiationParams();
        this.classifierUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
    
    private final static CachePair EMPTY_CACHE_PAIR = new CachePair(-1, null);
    
    private static final class CachePair {
        private final int parseCount;
        private final CsmUID<CsmFile> fileUID;

        public CachePair(int parseCount, CsmUID<CsmFile> fileUID) {
            this.parseCount = parseCount;
            this.fileUID = fileUID;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CachePair other = (CachePair) obj;
            if (this.parseCount != other.parseCount) {
                return false;
            }
            if (this.fileUID != other.fileUID && (this.fileUID == null || !this.fileUID.equals(other.fileUID))) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 37 * hash + this.parseCount;
            hash = 37 * hash + (this.fileUID != null ? this.fileUID.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "CachePair{" + "parseCount=" + parseCount + ", fileUID=" + fileUID + '}'; // NOI18N
        }
    }

    private static class CsmTypeReferenceImpl implements CsmReference {
        private final TypeImpl type;

        public CsmTypeReferenceImpl(TypeImpl type) {
            this.type = type;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.DIRECT_USAGE;
        }

        @Override
        public CsmObject getReferencedObject() {
            return null;
        }

        @Override
        public CsmObject getOwner() {
            return null;
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            return null;
        }

        @Override
        public CsmFile getContainingFile() {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException();
        }

        @Override
        public CharSequence getText() {
            return type.classifierText;
        }

        @Override
        public String toString() {
            return type.classifierText+"["+type.getStartOffset()+","+type.getEndOffset()+"]"; //NOI18N
        }
    }
    
    
    private static class ASTPointerOperatorQualifiersCollector implements AstUtil.ASTTokenVisitor {
        private int qualifiers;
        private int qualifierPosition;
        private final boolean isConst; // const or volatile
        
        
        public ASTPointerOperatorQualifiersCollector(int constQualifiers, int qualifierPosition, boolean isConst) {
            this.qualifiers = constQualifiers;
            this.qualifierPosition = qualifierPosition;
            this.isConst = isConst;
        }        

        @Override
        public Action visit(AST token) {
            int insideTokenType = token.getType();

            if (insideTokenType == CPPTokenTypes.STAR) {
                qualifierPosition <<= 1;
            } else if (isConst && AstRenderer.isConstQualifier(insideTokenType)) {
                qualifiers |= qualifierPosition;
            } else if (!isConst && AstRenderer.isVolatileQualifier(insideTokenType)) {
                qualifiers |= qualifierPosition;
            }
            
            return Action.CONTINUE;
        }
        
        public int getQualifierPosition() {
            return qualifierPosition;
        }
        
        public int getQualifiers() {
            return qualifiers;
        }                                              
    }
}
