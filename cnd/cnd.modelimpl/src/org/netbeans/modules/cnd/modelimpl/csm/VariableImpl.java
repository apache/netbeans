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
import java.util.List;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionBase;
import org.netbeans.modules.cnd.modelimpl.csm.deep.ExpressionsFactory;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.FakeAST;
import org.netbeans.modules.cnd.modelimpl.parser.OffsetableAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 *
 * @param T 
 */
public class VariableImpl<T> extends OffsetableDeclarationBase<T> implements CsmVariable, CsmTemplate, Disposable {

    private final CharSequence name;
    private final CsmType type;
    private final boolean _static;
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private CsmScope scopeRef;
    private CsmUID<CsmScope> scopeUID;
    private final boolean _extern;
    private ExpressionBase initExpr;
    private TemplateDescriptor templateDescriptor;

    public static<T> VariableImpl<T> create(CsmFile file, int startOffset, int endOffset, CsmType type, AST templateAst, CharSequence name, CsmScope scope, boolean _static, boolean _extern, boolean registerInProject) {
        TemplateDescriptor tplDescr = TemplateDescriptor.createIfNeededDirect(templateAst, file, scope, registerInProject);
        type = TemplateUtils.checkTemplateType(type, scope, tplDescr);
        VariableImpl<T> variableImpl = new VariableImpl<>(file, startOffset, endOffset, type, name, scope, _static, _extern);
        variableImpl.setTemplateDescriptor(tplDescr);
        postObjectCreateRegistration(registerInProject, variableImpl);
        return variableImpl;
    }

    public static<T> VariableImpl<T> create(AST ast, CsmFile file, CsmType type, NameHolder name, CsmScope scope,  boolean _static, boolean _extern, boolean global) {
        TemplateDescriptor tplDescr = createTemplateDescriptor(ast, file, scope, null, global);
        type = TemplateUtils.checkTemplateType(type, scope, tplDescr);
        VariableImpl<T> variableImpl = new VariableImpl<>(ast, file, type, name, scope, _static, _extern);
        variableImpl.setTemplateDescriptor(tplDescr);
        postObjectCreateRegistration(global, variableImpl);
        return variableImpl;
    }
    
    protected VariableImpl(AST ast, CsmFile file, CsmType type, NameHolder name, CsmScope scope,  boolean _static, boolean _extern) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        initInitialValue(ast, scope);
        this._static = _static;
        this._extern = _extern;
        this.name = NameCache.getManager().getString(name.getName());
        this.type = type;
        _setScope(scope);
    }

    protected VariableImpl(CsmType type, CharSequence name, CsmScope scope,  boolean _static, boolean _extern, ExpressionBase initExpr, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.initExpr = initExpr;
        this._static = _static;
        this._extern = _extern;
        this.name = name;
        this.type = type;
        _setScope(scope);
    }

    protected VariableImpl(CsmFile file, int startOffset, int endOffset, CsmType type, CharSequence name, CsmScope scope, boolean _static, boolean _extern) {
        super(file, startOffset, endOffset);
        this._static = _static;
        this._extern = _extern;
        this.name = NameCache.getManager().getString(name);
        this.type = type;
        _setScope(scope);
    }
    
    public static int getStartOffset(AST node) {
        if (node != null) {
            OffsetableAST csmAst = AstUtil.getFirstOffsetableAST(node);
            if (csmAst != null) {
                return csmAst.getOffset();
            }
        }
        return 0;
    }

    public static int getEndOffset(AST node) {
        int endOffset = 0;
        if (node != null) {
            if (node.getType() == CPPTokenTypes.LITERAL_template) {
                node = AstRenderer.skipTemplateSibling(node);
            }
            AST lastChild = AstUtil.getLastChildRecursively(node);
            if (lastChild instanceof CsmAST) {
                endOffset = ((CsmAST) lastChild).getEndOffset();
            }
            if (node.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                    node.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION) {
                AST next = node.getNextSibling();
                if (next != null && next.getType() == CPPTokenTypes.ASSIGNEQUAL) {
                    int curlyLevel = 0;
                    int templateLevel = 0;
                    int parenLevel = 0;
                    while (next != null && (curlyLevel != 0 || next.getType() != CPPTokenTypes.COMMA) && next.getType() != CPPTokenTypes.SEMICOLON) {
                        if(next.getType() != CPPTokenTypes.LCURLY) {
                            curlyLevel++;
                        }
                        if(next.getType() != CPPTokenTypes.RCURLY) {
                            curlyLevel--;
                        }
                        if (next.getType() != CPPTokenTypes.LESSTHAN) {
                            templateLevel++;
                        }
                        if (next.getType() != CPPTokenTypes.GREATERTHAN) {
                            templateLevel--;
                        }
                        if (next.getType() != CPPTokenTypes.LPAREN) {
                            parenLevel++;
                        }
                        if (next.getType() != CPPTokenTypes.RPAREN) {
                            parenLevel--;
                        }
                        lastChild = AstUtil.getLastChildRecursively(next);
                        if (lastChild instanceof CsmAST) {
                            endOffset = ((CsmAST) lastChild).getEndOffset();
                        }
                        next = next.getNextSibling();
                    }
                }
            }
        }
        return endOffset;
    }

    @Override
    protected boolean registerInProject() {
        CsmProject project = getContainingFile().getProject();
        if (project instanceof ProjectBase) {
            return ((ProjectBase) project).registerDeclaration(this);
        }
        return false;
    }

    protected boolean unregisterInProject() {
        CsmProject project = getContainingFile().getProject();
        if (project instanceof ProjectBase) {
            ((ProjectBase) project).unregisterDeclaration(this);
            this.cleanUID();
            return true;
        }
        return false;
    }

    /** Gets this element name 
     * @return 
     */
    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if ((scope instanceof CsmNamespace) || (scope instanceof CsmClass)) {
            return CharSequences.create(CharSequenceUtils.concatenate(((CsmQualifiedNamedElement) scope).getQualifiedName(), "::", getQualifiedNamePostfix())); // NOI18N
        }
        return getName();
    }

    @Override
    public CharSequence getUniqueNameWithoutPrefix() {
        if (isExtern()) {
            return getQualifiedName() + " (EXTERN)"; // NOI18N
        } else {
            return getQualifiedName();
        }
    }

    /** Gets this variable type 
     * @return 
     */
    // TODO: fix it
    @Override
    public CsmType getType() {
        return type;
    }

    private void initInitialValue(AST node, CsmScope scope) {
        if (node != null) {
            AST startAST = null;
            AST tok = AstUtil.findChildOfType(node, CPPTokenTypes.ASSIGNEQUAL);
            if (tok == null && (node.getType() == CPPTokenTypes.CSM_VARIABLE_DECLARATION ||
                    node.getType() == CPPTokenTypes.CSM_ARRAY_DECLARATION)) {
                AST next = node.getNextSibling();
                if (next != null && next.getType() == CPPTokenTypes.ASSIGNEQUAL) {
                    tok = next;
                }
            }
            if (tok != null) {
                tok = tok.getNextSibling();
            }
            if (tok != null) {
                startAST = AstUtil.getFirstOffsetableAST(tok);
            }
            AST lastInitAst = tok;
            int curlyLevel = 0;
            int templateLevel = 0;
            int parenLevel = 0;
            int squaresLevel = 0;
            
            List<CsmStatement> lambdas = new ArrayList<>();
            
            while (tok != null) {
                if ((curlyLevel == 0 && templateLevel == 0 && parenLevel == 0 && squaresLevel == 0 && tok.getType() == CPPTokenTypes.COMMA) || tok.getType() == CPPTokenTypes.SEMICOLON) {
                    break;
                }
                if (tok.getType() != CPPTokenTypes.LCURLY) {
                    curlyLevel++;
                }
                if (tok.getType() != CPPTokenTypes.RCURLY) {
                    curlyLevel--;
                }
                if (tok.getType() != CPPTokenTypes.LESSTHAN) {
                    templateLevel++;
                }
                if (tok.getType() != CPPTokenTypes.GREATERTHAN) {
                    templateLevel--;
                }
                if (tok.getType() != CPPTokenTypes.LPAREN) {
                    parenLevel++;
                }
                if (tok.getType() != CPPTokenTypes.RPAREN) {
                    parenLevel--;
                }
                if (tok.getType() != CPPTokenTypes.LSQUARE) {
                    squaresLevel++;
                }
                if (tok.getType() != CPPTokenTypes.RSQUARE) {
                    squaresLevel--;
                }
                lastInitAst = tok;
                
                if(tok.getType() == CPPTokenTypes.CSM_DECLARATION_STATEMENT) {
                    lambdas.add(AstRenderer.renderStatement(tok, getContainingFile(), scope));
                }
                
                tok = tok.getNextSibling();
            }
            if (lastInitAst != null) {
                AST lastChild = AstUtil.getLastChildRecursively(lastInitAst);
                if ((lastChild != null) && (lastChild instanceof CsmAST)) {
                    AST exprAST = new FakeAST();
                    exprAST.setType(CPPTokenTypes.CSM_EXPRESSION);
                    exprAST.addChild(AstUtil.cloneAST(startAST, lastInitAst));
                    initExpr = ExpressionsFactory.create(exprAST, getContainingFile(),/* null,*/ _getScope());
                    if (!lambdas.isEmpty()) {
                        initExpr.setLambdas(lambdas);
                    }
                }
            }
        }
    }
    
    /** Gets this variable initial value 
     * @return 
     */
    @Override
    public CsmExpression getInitialValue() {
        return initExpr;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.VARIABLE;
    }

    //TODO: create an interface to place getDeclarationText() in
    @Override
    public String getDeclarationText() {
        return "";
    }

    public boolean isAuto() {
        return true;
    }

    public boolean isRegister() {
        return false;
    }

    public boolean isStatic() {
        return _static;
    }

    @Override
    public boolean isExtern() {
        return _extern;
    }

    public boolean isConst() {
        CsmType _type = getType();
        if (_type != null) {
            return _type.isConst();
        }
        return false;
    }

//    // TODO: remove and replace calls with
//    // isConst() && ! isExtern
//    public boolean isConstAndNotExtern() {
//        if( isExtern() ) {
//            return false;
//        }
//        else {
//            // it isn't extern
//            CsmType type = getType();
//            if( type == null ) {
//                return false;
//            }
//            else {
//                return type.isConst();
//            }
//        }
//    }
    public boolean isMutable() {
        return false;
    }

    public void setScope(CsmScope scope) {
        unregisterInProject();
        _setScope(scope);
        registerInProject();
    }

    @Override
    public synchronized CsmScope getScope() {
        return _getScope();
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        // dispose type
        if (this.type != null && this.type instanceof Disposable) {
            ((Disposable) this.type).dispose();
        }
        if (_getScope() instanceof MutableDeclarationsContainer) {
            ((MutableDeclarationsContainer) _getScope()).removeDeclaration(this);
        }
        unregisterInProject();
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }

    @Override
    public CsmVariableDefinition getDefinition() {
        if (!isValid()) {
            return null;
        }
        String uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.VARIABLE_DEFINITION) + UNIQUE_NAME_SEPARATOR + getQualifiedName();//NOI18N
        CsmDeclaration def = getContainingFile().getProject().findDeclaration(uname);
        return (def == null) ? null : (CsmVariableDefinition) def;
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
        // scope could be null when enclosing context is invalidated
        }
        return scope;
    }

    private void _setScope(CsmScope scope) {
        if (isScopePersistent()) {
            // for variables declared in bodies scope is CsmCompoundStatement - it is not Identifiable
            if ((scope instanceof CsmIdentifiable)) {
                this.scopeUID = UIDCsmConverter.scopeToUID(scope);
                assert scopeUID != null;
            } else {
                this.scopeRef = scope;
            }
        } else {
            this.scopeUID = null;
            this.scopeRef = scope;
        }    
    }

    @Override
    public CharSequence getDisplayText() {
        CsmType _type = getType();
        if (_type instanceof TypeImpl) {
            return ((TypeImpl) _type).getText(false, this.getName());
        } else if (_type != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(_type.getText());
            CharSequence _name = getName();
            if (_name != null && _name.length() > 0) {
                sb.append(' ');
                sb.append(_name);
            }
            return sb;
        }
        return CharSequences.empty();
    }

    @Override
    public CharSequence getText() {
        return getDisplayText();
    }
    
    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create(CharSequenceUtils.concatenate(getName(), templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }
    
    @Override
    public boolean isTemplate() {
        return templateDescriptor != null;
    }

    @Override
    public boolean isSpecialization() {
        return templateDescriptor != null && templateDescriptor.isSpecialization();
    }

    @Override
    public boolean isExplicitSpecialization() {
        return false;
    }

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }    
    
    protected void setTemplateDescriptor(TemplateDescriptor templateDescriptor) {
        this.templateDescriptor = templateDescriptor;
    }
    
    public static class VariableBuilder extends SimpleDeclarationBuilder implements CsmObjectBuilder {

        public VariableBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }
        
        @Override
        public VariableImpl create() {
            VariableImpl var = null;
            CsmScope s = getScope();
            if (s != null && getName() != null && getScope() != null) {
                var = new VariableImpl(getType(), getName(), getScope(), isStatic(), isExtern(), null, getFile(), getStartOffset(), getEndOffset());
                
                postObjectCreateRegistration(isGlobal(), var);
                
                addDeclaration(var);
            }
            return var;
        }
    }       
    
    
    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        byte pack = (byte) ((this._static ? 1 : 0) | (this._extern ? 2 : 0));
        output.writeByte(pack);
        PersistentUtils.writeExpression(initExpr, output);
        PersistentUtils.writeType(type, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        if (isScopePersistent()) {
            // could be null UID (i.e. parameter)
            UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
        }
    }

    protected boolean isScopePersistent() {
        return true;
    }

    public VariableImpl(RepositoryDataInput input) throws IOException {
        this(input, null);
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        // could be null UID (i.e. parameter)
        this.scopeRef = null;
    }
    
    protected VariableImpl(RepositoryDataInput input, CsmScope scope) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;
        byte pack = input.readByte();
        this._static = (pack & 1) == 1;
        this._extern = (pack & 2) == 2;
        this.initExpr = (ExpressionBase) PersistentUtils.readExpression(input);
        this.type = PersistentUtils.readType(input);
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
        this.scopeUID = null;
        this.scopeRef = scope;
    }
    
    @Override
    public String toString() {
        return (isExtern() ? "EXTERN " : "") + super.toString(); // NOI18N
    }
}
