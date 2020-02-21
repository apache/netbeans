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

package org.netbeans.modules.cnd.modelimpl.fsm;

import org.netbeans.modules.cnd.api.model.CsmDeclaration.Kind;
import org.netbeans.modules.cnd.modelimpl.csm.*;
import java.util.*;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmFunction.OperatorKind;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.Exceptions;

/**
 *
 * @param T
 */
public final class ProgramImpl<T> extends OffsetableDeclarationBase<T>
        implements CsmProgram, CsmFunctionDefinition, CsmNamespaceDefinition, MutableDeclarationsContainer, Disposable, RawNamable {

    private final CharSequence name;
    private final CharSequence rawName;
    private CsmUID<CsmScope> scopeUID;
    private final List<CsmUID<CsmOffsetableDeclaration>> declarations;

    private ProgramImpl(String name, CsmFile file, int startOffset, int endOffset, CsmType type, CsmScope scope) {
        super(file, startOffset, endOffset);

        this.name = QualifiedNameCache.getManager().getString(name);
        rawName = this.name;
        try {
            _setScope(scope);
        } catch (AstRendererException ex) {
            Exceptions.printStackTrace(ex);
        }
        declarations = new ArrayList<>();
    }

    public static<T> ProgramImpl<T> create(String name, CsmFile file, int startOffset, int endOffset, CsmType type, CsmScope scope) {
        ProgramImpl<T> programImpl = new ProgramImpl<>(name, file, startOffset, endOffset, type, scope);
        postObjectCreateRegistration(true, programImpl);
        return programImpl;
    }

    @Override
    public DefinitionKind getDefinitionKind() {
        return DefinitionKind.REGULAR;
    }

    @Override
    public Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_DEFINITION;
    }

    @Override
    public CharSequence getQualifiedName() {
        return name;
    }

    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
    }

    @Override
    public CharSequence getDeclarationText() {
        return name;
    }

    @Override
    public CsmFunctionDefinition getDefinition() {
        return this;
    }

    @Override
    public CsmFunction getDeclaration() {
        return this;
    }

    @Override
    public boolean isOperator() {
        return false;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.NONE;
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public boolean isStatic() {
        return false;
    }

    @Override
    public CsmType getReturnType() {
        return TypeFactory.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile()); // NOI18N
    }

    @Override
    public CsmFunctionParameterList getParameterList() {
        return null;
    }

    @Override
    public Collection<CsmParameter> getParameters() {
        return Collections.<CsmParameter>emptyList();
    }

    @Override
    public CharSequence getSignature() {
        return ""; // NOI18N
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return Collections.<CsmScopeElement>emptyList();
    }

    @Override
    public CharSequence[] getRawName() {
        return AstUtil.toRawName(rawName);
    }

    private void _setScope(CsmScope scope) throws AstRendererException {
        // for functions declared in bodies scope is CsmCompoundStatement - it is not Identifiable
        if ((scope instanceof CsmIdentifiable)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert scopeUID != null;
        }
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
        return scope;
    }

    @Override
    public CsmCompoundStatement getBody() {
        return null;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls;
        synchronized (declarations) {
            decls = UIDCsmConverter.UIDsToDeclarations(declarations);
        }
        return decls;
    }

    @Override
    public CsmNamespace getNamespace() {
        return null;
    }

    @Override
    public void addDeclaration(CsmOffsetableDeclaration decl) {
        CsmUID<CsmOffsetableDeclaration> uid = RepositoryUtils.put(decl);
        assert uid != null;
        synchronized (declarations) {
            UIDUtilities.insertIntoSortedUIDList(uid, declarations);
        }
        if (decl instanceof FunctionImpl<?>) {
            FunctionImpl<?> f = (FunctionImpl<?>) decl;
            if (!NamespaceImpl.isNamespaceScope(f)) {
                f.setScope(this);
            }
        }
        // update repository
        RepositoryUtils.put(this);
    }

    @Override
    public void removeDeclaration(CsmOffsetableDeclaration declaration) {
        CsmUID<CsmOffsetableDeclaration> uid = UIDCsmConverter.declarationToUID(declaration);
        assert uid != null;
        synchronized (declarations) {
            declarations.remove(uid);
        }
        RepositoryUtils.remove(uid, declaration);
        // update repository
        RepositoryUtils.put(this);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, int end, CharSequence name) {
        CsmUID<CsmOffsetableDeclaration> out = null;
        synchronized (declarations) {
            out = UIDUtilities.findExistingUIDInList(declarations, start, end, name);
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, CharSequence name, CsmDeclaration.Kind kind) {
        CsmUID<CsmOffsetableDeclaration> out = null;
        synchronized (declarations) {
            out = UIDUtilities.findExistingUIDInList(declarations, start, name, kind);
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    @Override
    public void dispose() {
        super.dispose();
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        Collection<CsmOffsetableDeclaration> decls;
        List<CsmUID<CsmOffsetableDeclaration>> uids;
        synchronized (declarations) {
            decls = getDeclarations();
            uids = new ArrayList<>(declarations);
            declarations.clear();
        }
        Utils.disposeAll(decls);
        RepositoryUtils.remove(uids);
    }

//
//    private final CharSequence name;
//    private final CsmType returnType;
//    private final FunctionParameterListImpl parameterList;
//    private CharSequence signature;
//
//    // only one of scopeRef/scopeAccessor must be used
//    private /*final*/ CsmScope scopeRef;// can be set in onDispose or contstructor only
//    private CsmUID<CsmScope> scopeUID;
//
//    private final CharSequence[] rawName;
//
//    private static final byte FLAGS_VOID_PARMLIST = 1 << 0;
//    private static final byte FLAGS_STATIC = 1 << 1;
//    private static final byte FLAGS_CONST = 1 << 2;
//    private static final byte FLAGS_OPERATOR = 1 << 3;
//    private static final byte FLAGS_INVALID = 1 << 4;
//    protected static final int LAST_USED_FLAG_INDEX = 4;
//    private byte flags;
//
//    private static final boolean CHECK_SCOPE = false;
//
//    public ProgramImpl(String name, CsmFile file, CsmType type, CsmScope scope) {
//
//        super(file, 0, 0);
//
//        this.name = name;
//        this.rawName = null;
//        this.returnType = type;
//        this.parameterList = null;
//
//
////        name = QualifiedNameCache.getManager().getString(initName(ast));
////        if (name.length()==0) {
////            throw new AstRendererException((FileImpl)file, this.getStartOffset(), "Empty function name."); // NOI18N
////        }
////        rawName = initRawName(ast);
////        AST child = ast.getFirstChild();
////        if (child != null) {
////            setStatic(child.getType() == CPPTokenTypes.LITERAL_static);
////        } else {
////            System.err.println("function ast " + ast.getText() + " without childs in file " + file.getAbsolutePath());
////        }
////        if (!isStatic()) {
////            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
////                               name, true, true, false);
////            Iterator<CsmFunction> it = CsmSelect.getStaticFunctions(file, filter);
////            while(it.hasNext()){
////                CsmFunction fun = it.next();
////                if( name.equals(fun.getName()) ) {
////                    // we don't check signature here since file-level statics
////                    // is C-style construct
////                    setStatic(true);
////                    break;
////                }
////            }
////        }
////
////        // change scope to file for static methods, but only to prevent
////        // registration in global  namespace
////        if(scope instanceof CsmNamespace) {
////            if( !NamespaceImpl.isNamespaceScope(this) ) {
////                    scope = file;
////            }
////        }
////
////        _setScope(scope);
////
////        if (global){
////            RepositoryUtils.hang(this); // "hang" now and then "put" in "register()"
////        } else {
////            Utils.setSelfUID(this);
////        }
////        boolean _const = initConst(ast);
////        setFlags(FLAGS_CONST, _const);
////        StringBuilder clsTemplateSuffix = new StringBuilder();
////        templateDescriptor = createTemplateDescriptor(ast, this, clsTemplateSuffix, global);
////        classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
////        if(type != null) {
////            returnType = type;
////        } else {
////            returnType = initReturnType(ast);
////        }
////
////        // set parameters, do it in constructor to have final fields
////        this.parameterList = createParameterList(ast, !global);
////        if (this.parameterList == null || this.parameterList.isEmpty()) {
////            setFlags(FLAGS_VOID_PARMLIST, isVoidParameter(ast));
////        } else {
////            setFlags(FLAGS_VOID_PARMLIST, false);
////        }
////        if (name.toString().startsWith(OPERATOR) &&
////                (name.length() > OPERATOR.length()) &&
////                !Character.isJavaIdentifierPart(name.charAt(OPERATOR.length()))) { // NOI18N
////            setFlags(FLAGS_OPERATOR, true);
////        }
////        if (register) {
////            registerInProject();
////        }
////        if (this.parameterList == null) {
////            System.err.println("NO PARAM LIST FOR FUNC:" + name + " at " + AstUtil.getOffsetString(ast) + " in " + file.getAbsolutePath());
////        }
//    }
//
//    public void setScope(CsmScope scope) {
//        unregisterInProject();
//        try {
//            this._setScope(scope);
//        } catch(AstRendererException e) {
//            DiagnosticExceptoins.register(e);
//        }
//        registerInProject();
//    }
//
//    private void _setScope(CsmScope scope) throws AstRendererException {
//        // for functions declared in bodies scope is CsmCompoundStatement - it is not Identifiable
//        if ((scope instanceof CsmIdentifiable)) {
//            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
//            assert (scopeUID != null || scope == null);
//        } else {
//            this.scopeRef = scope;
//        }
//    }
//
//    /**
//     * Return true, if this is a definition of function
//     * that is declared in some other place
//     * (in other words, that is prefixed with class or namespace.
//     * Otherwise - for simple functions with body as the one below:
//     * void foo() {}
//     * returns false
//     */
//    public boolean isPureDefinition() {
//        return getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
//                getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION;
//    }
//
//    protected boolean hasFlags(byte mask) {
//        return (flags & mask) == mask;
//    }
//
//    protected void setFlags(byte mask, boolean value) {
//        if (value) {
//            flags |= mask;
//        } else {
//            flags &= ~mask;
//        }
//    }
//
//    public boolean isStatic() {
//        return hasFlags(FLAGS_STATIC);
//    }
//
//    protected void setStatic(boolean value) {
//        setFlags(FLAGS_STATIC, value);
//    }
//
//    private AST findParameterNode(AST node) {
//        AST ast = AstUtil.findChildOfType(node, CPPTokenTypes.CSM_PARMLIST);
//        if (ast != null) {
//            // for K&R-style
//            AST ast2 = AstUtil.findSiblingOfType(ast.getNextSibling(), CPPTokenTypes.CSM_KR_PARMLIST);
//            if (ast2 != null) {
//                ast = ast2;
//            }
//        }
//        return ast;
//    }
//
//    protected CharSequence getScopeSuffix() {
//        return CharSequences.empty();
//    }
//
//    protected String initName(AST node) {
//        return findFunctionName(node);
//    }
//
//    protected CharSequence[] initRawName(AST node) {
//        return findFunctionRawName(node);
//    }
//
//    public CharSequence getDisplayName() {
//        return getName(); // NOI18N
//    }
//
//    public boolean isVoidParameterList(){
//        return hasFlags(FLAGS_VOID_PARMLIST);
//    }
//
//    private static String extractName(AST token){
//        int type = token.getType();
//        if( type == CPPTokenTypes.ID ) {
//            return token.getText();
//        } else if( type == CPPTokenTypes.CSM_QUALIFIED_ID ) {
//            AST last = AstUtil.getLastChild(token);
//            if( last != null) {
//                if( last.getType() == CPPTokenTypes.ID ) {
//                    return last.getText();
//                } else {
////		    if( first.getType() == CPPTokenTypes.LITERAL_OPERATOR ) {
//                    AST operator = AstUtil.findChildOfType(token, CPPTokenTypes.LITERAL_OPERATOR);
//                    if( operator != null ) {
//                        StringBuilder sb = new StringBuilder(operator.getText());
//                        sb.append(' ');
//                        for( AST next = operator.getNextSibling(); next != null; next = next.getNextSibling() ) {
//                            sb.append(next.getText());
//                        }
//                        return sb.toString();
//                    } else {
//                        AST first = token.getFirstChild();
//                        if (first.getType() == CPPTokenTypes.ID) {
//                            return first.getText();
//                        }
//                    }
//                }
//            }
//        }
//        return "";
//    }
//
//    private static String findFunctionName(AST ast) {
//        if( CastUtils.isCast(ast) ) {
//            return CastUtils.getFunctionName(ast);
//        }
//        AST token = AstUtil.findMethodName(ast);
//        if (token != null){
//            return extractName(token);
//        }
//        return "";
//    }
//
//    private static CharSequence[] findFunctionRawName(AST ast) {
//        if( CastUtils.isCast(ast) ) {
//            return CastUtils.getFunctionRawName(ast);
//        }
//        return AstUtil.getRawNameInChildren(ast);
//    }
//
//    protected boolean isCStyleStatic() {
//        return isStatic() && CsmKindUtilities.isFile(getScope());
//    }
//
//    protected void registerInProject() {
//        if (isCStyleStatic()) {
//            // do NOT register in project C-style static funcions!
//            return;
//        }
//        CsmProject project = getContainingFile().getProject();
//        if( project instanceof ProjectBase ) {
//	    // implicitely calls RepositoryUtils.put()
//            ((ProjectBase) project).registerDeclaration(this);
//        }
//    }
//
//    private void unregisterInProject() {
//        CsmProject project = getContainingFile().getProject();
//        if( project instanceof ProjectBase ) {
//            ((ProjectBase) project).unregisterDeclaration(this);
//            this.cleanUID();
//        }
//    }
//
//
//    /** Gets this element name
//     * @return name
//     */
//    public CharSequence getName() {
//        return name;
//    }
//
//    public CharSequence getQualifiedName() {
//        CsmScope scope = getScope();
//        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) || (scope instanceof CsmNamespaceDefinition) ) {
//            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
//            if( scopeQName != null && scopeQName.length() > 0 ) {
//                return CharSequences.create(scopeQName.toString() + getScopeSuffix() + "::" + getQualifiedNamePostfix()); // NOI18N
//            }
//        }
//        return getName();
//    }
//
//    public CharSequence[] getRawName() {
//        return rawName;
//    }
//
//    @Override
//    public CharSequence getUniqueNameWithoutPrefix() {
//        return getQualifiedName().toString() + getSignature().toString().substring(getName().length());
//    }
//
//    public Kind getKind() {
//        return CsmDeclaration.Kind.FUNCTION;
//    }
//
//    /** Gets this function's declaration text
//     * @return declaration text
//     */
//    public String getDeclarationText() {
//        return "";
//    }
//
//    /**
//     * Gets this function definition
//     * TODO: describe getDefiition==this ...
//     * @return definition
//     */
//    public CsmFunctionDefinition getDefinition() {
//        if( isCStyleStatic() ) {
//            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
//                               getName(), true, true, false);
//            Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(getContainingFile(), filter);
//            while(it.hasNext()){
//                CsmDeclaration decl = it.next();
//                if( CsmKindUtilities.isFunctionDefinition(decl) ) {
//                    if( getName().equals(decl.getName()) ) {
//                        CsmFunctionDefinition fun = (CsmFunctionDefinition) decl;
//                        if( getSignature().equals(fun.getSignature())) {
//                            return fun;
//                        }
//                    }
//                }
//            }
//            return null;
//        }
//        String uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
//        CsmProject prj = getContainingFile().getProject();
//        CsmFunctionDefinition def = findDefinition(prj, uname);
//        if (def == null) {
//            for (CsmProject lib : prj.getLibraries()){
//                def = findDefinition(lib, uname);
//                if (def != null) {
//                    break;
//                }
//            }
//        }
//        if (def == null && (prj instanceof ProjectBase)) {
//            for(CsmProject dependent : ((ProjectBase)prj).getDependentProjects()){
//                def = findDefinition(dependent, uname);
//                if (def != null) {
//                    break;
//                }
//            }
//        }
//        if (def == null) {
//            uname = Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix();
//            def = findDefinition(prj, uname);
//            if (def == null) {
//                for (CsmProject lib : prj.getLibraries()) {
//                    def = findDefinition(lib, uname);
//                    if (def != null) {
//                        break;
//                    }
//                }
//            }
//            if (def == null && (prj instanceof ProjectBase)) {
//                for (CsmProject dependent : ((ProjectBase) prj).getDependentProjects()) {
//                    def = findDefinition(dependent, uname);
//                    if (def != null) {
//                        break;
//                    }
//                }
//            }
//        }
//        return def;
//    }
//
//    // method try to find definition in case cast operator definition is declared without scope
//    private CsmDeclaration fixCastOperator(CsmProject prj, String uname) {
//        int i = uname.indexOf("operator "); // NOI18N
//        if (i > 0) {
//            String s = uname.substring(i + 9);
//            int j = s.lastIndexOf("::"); // NOI18N
//            if (j > 0) {
//                s = uname.substring(0, i + 9) + " " + s.substring(j + 2); // NOI18N
//                return prj.findDeclaration(s);
//            }
//        }
//        return null;
//    }
//
//    private CsmFunctionDefinition findDefinition(CsmProject prj, String uname){
//        CsmDeclaration res = prj.findDeclaration(uname);
//        if (res == null && this.isOperator()) {
//            res = fixCastOperator(prj, uname);
//        }
//        if (res instanceof CsmFunctionDefinition) {
//            return (CsmFunctionDefinition)res;
//        }
//        if (prj instanceof ProjectBase) {
//            int parmSize = getParameters().size();
//            boolean isVoid = isVoidParameterList();
//            String from = uname.substring(0, uname.indexOf('(')+1);
//            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)prj).findDeclarationsByPrefix(from);
//            CsmFunctionDefinition candidate = null;
//            for(CsmOffsetableDeclaration decl : decls){
//                CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
//                int candidateParamSize = def.getParameters().size();
//                if (!isVoid && parmSize == 0) {
//                    if (!ProjectBase.isCppFile(decl.getContainingFile())){
//                        return def;
//                    }
//                }
//                if (parmSize == candidateParamSize) {
//                    // TODO check overloads
//                    if (candidate == null) {
//                        candidate = def;
//                    } else {
//                        return null;
//                    }
//                }
//            }
//            return candidate;
//        }
//        return null;
//    }
//
//    /**
//     * Gets this function body.
//     * The same as the following call:
//     * (getDefinition() == null) ? null : getDefinition().getBody();
//     *
//     * TODO: perhaps it isn't worth keeping duplicate to getDefinition().getBody()? (though convenient...)
//     * @return body of function
//     */
//    public CsmCompoundStatement getBody() {
//        return null;
//    }
//
//    public boolean isInline() {
//        return false;
//    }
//
////    public boolean isVirtual() {
////        return false;
////    }
////
////    public boolean isExplicit() {
////        return false;
////    }
//
//    private CsmType initReturnType(AST node) {
//        CsmType ret = null;
//        AST token = getTypeToken(node);
//        if( token != null ) {
//            ret = AstRenderer.renderType(token, getContainingFile());
//        }
//        if( ret == null ) {
//            ret = TypeFactory.createBuiltinType("int", (AST) null, 0,  null/*getAst().getFirstChild()*/, getContainingFile()); // NOI18N
//        }
//        return TemplateUtils.checkTemplateType(ret, ProgramImpl.this);
//    }
//
//    public CsmType getReturnType() {
//        return returnType;
//    }
//
//    private static AST getTypeToken(AST node) {
//        for( AST token = node.getFirstChild(); token != null; token = token.getNextSibling() ) {
//            int type = token.getType();
//            switch( type ) {
//                case CPPTokenTypes.CSM_TYPE_BUILTIN:
//                case CPPTokenTypes.CSM_TYPE_COMPOUND:
//                case CPPTokenTypes.LITERAL_typename:
//                case CPPTokenTypes.LITERAL_struct:
//                case CPPTokenTypes.LITERAL_class:
//                case CPPTokenTypes.LITERAL_union:
//                    return token;
//                default:
//                    if( AstRenderer.isCVQualifier(type) ) {
//                        return token;
//                    }
//            }
//        }
//        return null;
//    }
//
//    private FunctionParameterListImpl createParameterList(AST funAST, boolean isLocal) {
//        return null;//FunctionParameterListImpl.create(getContainingFile(), funAST, this, isLocal);
//    }
//
//    private boolean isVoidParameter(AST node) {
//        AST ast = findParameterNode(node);
//        return AstRenderer.isVoidParameter(ast);
//    }
//
//    public FunctionParameterListImpl  getParameterList() {
//        return parameterList;
//    }
//
//    public Collection<CsmParameter>  getParameters() {
//        return _getParameters();
//    }
//
//    public CsmScope getScope() {
//        return _getScope();
//    }
//
//    public CharSequence getSignature() {
//        if( signature == null ) {
//            signature = QualifiedNameCache.getManager().getString(createSignature());
//        }
//        return signature;
//    }
//
//    public CsmFunction getDeclaration() {
//        return this;
//    }
//
//    public boolean isOperator() {
//        return hasFlags(FLAGS_OPERATOR);
//    }
//
//    public OperatorKind getOperatorKind() {
//        OperatorKind out = OperatorKind.NONE;
//        if (isOperator()) {
//            String strName = getName().toString();
//            int start = strName.indexOf(OPERATOR);
//            assert start >= 0 : "must have word \"operator\" in name";
//            start += OPERATOR.length();
//            String signText = strName.substring(start).trim();
//            OperatorKind binaryKind = OperatorKind.getKindByImage(signText, true);
//            OperatorKind nonBinaryKind = OperatorKind.getKindByImage(signText, false);
//            if (binaryKind != OperatorKind.NONE && nonBinaryKind != OperatorKind.NONE) {
//                // select the best
//                int nrParams = getNrParameters();
//                if (nrParams == 0) {
//                    out = nonBinaryKind;
//                } else if (nrParams == 1) {
//                    if (CsmKindUtilities.isClass(getScope())) {
//                        out = binaryKind;
//                    } else {
//                        out = nonBinaryKind;
//                    }
//                } else if (nrParams == 2) {
//                    out = binaryKind;
//                } else {
//                    out = nonBinaryKind;
//                }
//            } else {
//                out = (binaryKind != OperatorKind.NONE) ? binaryKind : nonBinaryKind;
//            }
//        }
//        return out;
//    }
//
//    public Collection<CsmScopeElement> getScopeElements() {
//        Collection<CsmScopeElement> l = new ArrayList<CsmScopeElement>();
//        l.addAll(getParameters());
//        return l;
//    }
//
//    private CharSequence createSignature() {
//        // TODO: this fake implementation for Deimos only!
//        // we should resolve parameter types and provide
//        // kind of canonical representation here
//        StringBuilder sb = new StringBuilder(getName());
//        InstantiationProviderImpl.appendParametersSignature(getParameters(), sb);
//        if( isConst() ) {
//            sb.append(" const"); // NOI18N
//        }
//        return sb;
//    }
//
//    @Override
//    public void dispose() {
//        super.dispose();
//        onDispose();
//        CsmScope scope = _getScope();
//        if( scope instanceof MutableDeclarationsContainer ) {
//            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
//        }
//        this.unregisterInProject();
//        _disposeParameters();
//        setFlags(FLAGS_INVALID, true);
//    }
//
//    @Override
//    public boolean isValid() {
//        return !hasFlags(FLAGS_INVALID) && super.isValid();
//    }
//
//    private synchronized void onDispose() {
//        if (this.scopeRef == null) {
//            // restore container from it's UID
//            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
//            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
//        }
//    }
//
//    private static boolean initConst(AST node) {
//        AST token = node.getFirstChild();
//        while( token != null &&  token.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
//            token = token.getNextSibling();
//        }
//        while( token != null ) {
//            if (AstRenderer.isConstQualifier(token.getType())) {
//                return true;
//            }
//            token = token.getNextSibling();
//        }
//        return false;
//    }
//
//    /**
//     * isConst was originally in MethodImpl;
//     * but this methods needs internally in FunctionDefinitionImpl
//     * to create proper signature.
//     * Therefor it's moved here as a protected method.
//     */
//    protected boolean isConst() {
//        return hasFlags(FLAGS_CONST);
//    }
//
//    private synchronized CsmScope _getScope() {
//        CsmScope scope = this.scopeRef;
//        if (scope == null) {
//            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
//            // this is possible situation when scope is already invalidated (see IZ#154264)
//            //assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
//        }
//        return scope;
//    }
//
//    private Collection<CsmParameter> _getParameters() {
//        if (this.parameterList == null) {
//            return Collections.<CsmParameter>emptyList();
//        } else {
//            return parameterList.getParameters();
//        }
//    }
//
//    private int getNrParameters() {
//        if (isVoidParameterList() || this.parameterList == null) {
//            return 0;
//        } else {
//            return this.parameterList.getNrParameters();
//        }
//    }
//
//    private void _disposeParameters() {
//        if (this.parameterList != null) {
//            parameterList.dispose();
//        }
//    }
//
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
//        PersistentUtils.writeType(this.returnType, output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
//        PersistentUtils.writeParameterList(this.parameterList, output);
        PersistentUtils.writeUTF(this.rawName, output);

//        // not null UID
//        assert !CHECK_SCOPE || this.scopeUID != null;
        factory.writeUID(this.scopeUID, output);

        factory.writeUIDCollection(this.declarations, output, true);

//        PersistentUtils.writeUTF(this.signature, output);
//        output.writeByte(flags);
    }

    public ProgramImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
//        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
//        this.parameterList = (FunctionParameterListImpl) PersistentUtils.readParameterList(input);
        this.rawName = PersistentUtils.readUTF(input, NameCache.getManager());

        this.scopeUID = factory.readUID(input);

        int collSize = input.readInt();
        if (collSize < 0) {
            declarations = new ArrayList<>();
        } else {
            declarations = new ArrayList<>(collSize);
        }
        factory.readUIDCollection(declarations, input, collSize);

//        // not null UID
//        assert !CHECK_SCOPE || this.scopeUID != null;
//        this.scopeRef = null;
//
//        this.signature = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
//        this.flags = input.readByte();
    }
}
