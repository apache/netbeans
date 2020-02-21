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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.Resolver;
import org.netbeans.modules.cnd.modelimpl.csm.resolver.ResolverFactory;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 */
public final class VariableDefinitionImpl extends VariableImpl<CsmVariableDefinition> implements CsmVariableDefinition {
    
    private CsmUID<CsmVariable> declarationUID;
    private CharSequence qualifiedName;
    private final CharSequence[] classOrNspNames;

    /** Creates a new instance of VariableDefinitionImpl */
    private VariableDefinitionImpl(AST ast, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern) {
        super(ast, file, type, name, null,_static, _extern);
        classOrNspNames = getClassOrNspNames(ast);
    }

    private VariableDefinitionImpl(CharSequence name, CharSequence[] classOrNspNames, CsmType type, TemplateDescriptor templateDescriptor, boolean _static, boolean _extern, ExpressionBase initExpr, CsmFile file, int startOffset, int endOffset) {
        super(type, name, null, _static, _extern, initExpr, file, startOffset, endOffset);
        setTemplateDescriptor(templateDescriptor);
        this.classOrNspNames = classOrNspNames;
    }
    
    public static VariableDefinitionImpl create(AST ast, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern) {
        VariableDefinitionImpl variableDefinitionImpl = new VariableDefinitionImpl(ast, file, type, name, _static, _extern);
        variableDefinitionImpl.setTemplateDescriptor(createTemplateDescriptor(ast, file, null, null, true));
        postObjectCreateRegistration(true, variableDefinitionImpl);
        return variableDefinitionImpl;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.VARIABLE_DEFINITION;
    }
    
    @Override
    public CsmVariable getDeclaration() {
        CsmVariable declaration = _getDeclaration(); 
	if( declaration == null ) {
            _setDeclaration(null);
	    declaration = findDeclaration();
            _setDeclaration(declaration);
	}
	return declaration;
    }

    private CsmVariable _getDeclaration() {
        // null object is OK here, because of changed cached reference
        return UIDCsmConverter.UIDtoDeclaration(this.declarationUID);
    }
    
    private void _setDeclaration(CsmVariable decl) {
        this.declarationUID = UIDCsmConverter.declarationToUID(decl);
        assert declarationUID != null || decl == null;
    }
    
    @Override
    public CharSequence getQualifiedName() {
	if( qualifiedName == null ) {
	    qualifiedName = QualifiedNameCache.getManager().getString(findQualifiedName());
	}
	return qualifiedName;
    }

    @Override
    protected boolean registerInProject() {
        CharSequence prevFQN = qualifiedName;
        boolean out = super.registerInProject();
        if (false && CndUtils.isDebugMode()) {
            if (prevFQN != null && !prevFQN.equals(findQualifiedName())) {
                assert prevFQN.equals(findQualifiedName());
            }
        }
        return out;
    }

    @Override
    protected boolean unregisterInProject() {
        if (false && CndUtils.isDebugMode()) {
            assert qualifiedName != null;
            if (!qualifiedName.equals(findQualifiedName())) {
                assert qualifiedName.equals(findQualifiedName());
            }
        }
        return super.unregisterInProject();
    }

    private CharSequence findQualifiedName() {
        CsmVariable declaration = _getDeclaration();
	if( declaration != null ) {
	    return declaration.getQualifiedName();
	}
	CsmObject owner = findOwner();
	if( owner instanceof CsmQualifiedNamedElement  ) {
	    return CharSequenceUtils.concatenate(((CsmQualifiedNamedElement) owner).getQualifiedName(), "::", getQualifiedNamePostfix()); // NOI18N
	}
	else {
	    CharSequence[] cnn = classOrNspNames;
	    CsmNamespaceDefinition nsd = findNamespaceDefinition();
	    StringBuilder sb = new StringBuilder();
	    if( nsd != null ) {
		sb.append(nsd.getQualifiedName());
	    }
	    if( cnn != null ) {
		for (int i = 0; i < cnn.length; i++) {
		    if( sb.length() > 0 ) {
			sb.append("::"); // NOI18N
		    }
		    sb.append(cnn[i]);
		}
	    }
	    if( sb.length() == 0 ) {
		sb.append("unknown>"); // NOI18N
	    }
	    sb.append("::"); // NOI18N
	    sb.append(getQualifiedNamePostfix());
	    return sb;
	}
    }

    private CsmNamespaceDefinition findNamespaceDefinition() {
        CsmFilter filter = CsmSelect.getFilterBuilder().createKindFilter(Kind.NAMESPACE_DEFINITION);
        return findNamespaceDefinition(CsmSelect.getDeclarations(getContainingFile(), filter), filter);
    }
    
    private CsmNamespaceDefinition findNamespaceDefinition(Iterator<CsmOffsetableDeclaration> it, CsmFilter filter) {
        while(it.hasNext()) {
            CsmOffsetableDeclaration decl = it.next();
            if (decl.getStartOffset() > this.getStartOffset()) {
                break;
            }
            if (decl.getKind() == CsmDeclaration.Kind.NAMESPACE_DEFINITION) {
                if (this.getEndOffset() < decl.getEndOffset()) {
                    CsmNamespaceDefinition nsdef = (CsmNamespaceDefinition) decl;
                    CsmNamespaceDefinition inner = findNamespaceDefinition(CsmSelect.getDeclarations(nsdef, filter), filter);
                    return (inner == null) ? nsdef : inner;
                }
            }
        }
        return null;
    }

    private CsmVariable findDeclaration() {
        if (!isValid()) {
            return null;
        }
        String uname = CharSequenceUtils.toString(CsmDeclaration.Kind.VARIABLE.toString(), UNIQUE_NAME_SEPARATOR, getQualifiedName());
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
	if( def == null ) {
	    CsmObject owner = findOwner();
	    if( owner instanceof CsmClass ) {
                CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false);
		def = findByName(CsmSelect.getClassMembers((CsmClass)owner, filter), getName());
	    }  else if( owner instanceof CsmNamespace ) {
                CsmFilter filter = CsmSelect.getFilterBuilder().createCompoundFilter(
                         CsmSelect.getFilterBuilder().createKindFilter(CsmDeclaration.Kind.VARIABLE),
                         CsmSelect.getFilterBuilder().createNameFilter(getName(), true, true, false));
                Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(((CsmNamespace)owner), filter);
                while (it.hasNext()) {
                    def = it.next();
                }
	    }
	}
        return (CsmVariable) def;
    }

    private CsmVariable findByName(Iterator<CsmMember> it, CharSequence name) {
	while(it.hasNext()) {
	    CsmMember decl = it.next();
	    if( decl.getName().equals(name) ) {
		if( decl instanceof  CsmVariable ) { // paranoja
		    return (CsmVariable) decl;
		}
	    }	
	}
	return null;
    }

    /** @return either class or namespace */
    private CsmObject findOwner() {
	CharSequence[] cnn = classOrNspNames;
	if( cnn != null && cnn.length > 0) {
            CsmObject obj = null;
            Resolver resolver = ResolverFactory.createResolver(this);
            try {
                obj = resolver.resolve(cnn, Resolver.CLASSIFIER | Resolver.NAMESPACE);
            } finally {
                ResolverFactory.releaseResolver(resolver);
            }
	    if( obj instanceof CsmClass ) {
                return (CsmClass) obj;
	    }
	    else if( obj instanceof CsmNamespace ) {
		return (CsmNamespace) obj;
	    }
	}
	return null;
    }    
    
    private static CharSequence[] getClassOrNspNames(AST ast) {
        AST qid = getQialifiedId(ast);
        if( qid == null ) {
            return null;
        }
        int cnt = qid.getNumberOfChildren();
        if( cnt >= 1 ) {
            List<CharSequence> l = new ArrayList<>();
            for( AST token = qid.getFirstChild(); token != null; token = token.getNextSibling() ) {
                if( token.getType() == CPPTokenTypes.IDENT ) {
                    if( token.getNextSibling() != null ) {
                        CharSequence name = AstUtil.getText(token);
                        l.add(NameCache.getManager().getString(name));
                    }
                }
            }
            return  l.toArray(new CharSequence[l.size()]);
        }
        return null;
    }
    
    private static AST getQialifiedId(AST ast){
        AST varAst = ast;
        for( AST token = varAst.getFirstChild(); token != null; token = token.getNextSibling() ) {
            switch( token.getType() ) {
                case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                    return token.getFirstChild();
                case CPPTokenTypes.CSM_QUALIFIED_ID:
                case CPPTokenTypes.IDENT:
                    return token;
            }
        }
        return null;
    }
    
    public static class VariableDefinitionBuilder extends VariableBuilder implements CsmObjectBuilder {

        public VariableDefinitionBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }
        
        @Override
        public VariableDefinitionImpl create() {
            VariableDefinitionImpl var = new VariableDefinitionImpl(getName(), getScopeNames(), getType(), getTemplateDescriptor(), isStatic(), isExtern(), null, getFile(), getStartOffset(), getEndOffset());

            postObjectCreateRegistration(isGlobal(), var);

            addDeclaration(var);
            return var;
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);    
        PersistentUtils.writeUTF(this.qualifiedName, output);
        PersistentUtils.writeStrings(this.classOrNspNames, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.declarationUID, output);
    }  
    
    public VariableDefinitionImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.classOrNspNames = PersistentUtils.readStrings(input, NameCache.getManager());
        this.declarationUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }

}
