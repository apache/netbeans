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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction.OperatorKind;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmSpecializationParameter;
import org.netbeans.modules.cnd.api.model.CsmTemplate;
import org.netbeans.modules.cnd.api.model.CsmTemplateParameter;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmCompoundStatement;
import org.netbeans.modules.cnd.api.model.support.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmExpressionResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstRenderer;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.Disposable;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.RawNamable;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.impl.services.InstantiationProviderImpl;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import static org.netbeans.modules.cnd.modelimpl.repository.KeyUtilities.UID_INTERNAL_DATA_PREFIX;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
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
public class FunctionImpl<T> extends OffsetableDeclarationBase<T>
        implements CsmFunction, Disposable, RawNamable, CsmTemplate {
    
    private static final Logger LOG = Logger.getLogger(FunctionImpl.class.getName());
    
    protected static final int UID_EXTRA_SUFFIX_MAX_LENGTH = 64;
    
    protected static final String UID_EXTRA_SUFFIX_TOO_LONG = "<truncated>"; // NOI18N
     
    /*package*/ static final String OPERATOR = "operator"; // NOI18N;

    private final CharSequence name;
    private CsmType returnType;
    private FunctionParameterListImpl parameterList;
    private CharSequence signature;
    private CharSequence uidExtraSuffix;

    // only one of scopeRef/scopeAccessor must be used
    private /*final*/ CsmScope scopeRef;// can be set in onDispose or contstructor only
    private CsmUID<CsmScope> scopeUID;

    private final CharSequence rawName;

    private TemplateDescriptor templateDescriptor;

    protected CharSequence classTemplateSuffix;

    private static final short FLAGS_VOID_PARMLIST = 1 << 0;
    private static final short FLAGS_STATIC = 1 << 1;
    private static final short FLAGS_CONST = 1 << 2;
    private static final short FLAGS_OPERATOR = 1 << 3;
    private static final short FLAGS_INVALID = 1 << 4;
    private static final short FLAGS_VOLATILE = 1 << 5;
    private static final short FLAGS_LVALUE_REF = 1 << 6; //the & ref-qualifier
    private static final short FLAGS_RVALUE_REF = 1 << 7; //the && ref-qualifier
    protected static final int LAST_USED_FLAG_INDEX = 7;
    private short flags;
    public static class CV_RL {
        public boolean _const;
        public boolean _volatile;
        public boolean _lvalue;
        public boolean _rvalue;
        public static CV_RL isConst(boolean isConst){
            CV_RL res = new CV_RL();
            res._const = isConst;
            return res;
        }
    }
    
    protected FunctionImpl(CharSequence name, CharSequence rawName, CsmScope scope, boolean _static, CV_RL _const, CsmFile file, int startOffset, int endOffset, boolean global) {
        super(file, startOffset, endOffset);

        this.name = name;
        this.rawName = rawName;
        
        setFlags(FLAGS_STATIC, _static);
        _setScope(scope);
        setFlags(FLAGS_CONST, _const._const);
        setFlags(FLAGS_VOLATILE, _const._volatile);
        setFlags(FLAGS_LVALUE_REF, _const._lvalue);
        setFlags(FLAGS_RVALUE_REF, _const._rvalue);
        if (CharSequenceUtils.startsWith(name, OPERATOR) &&
                (name.length() > OPERATOR.length()) &&
                !Character.isJavaIdentifierPart(name.charAt(OPERATOR.length()))) { // NOI18N
            setFlags(FLAGS_OPERATOR, true);
        }
    }
    
    public static<T> FunctionImpl<T> create(AST ast, CsmFile file, FileContent fileContent, CsmType type, CsmScope scope, boolean global, Map<Integer, CsmObject> objects) throws AstRendererException {
        int startOffset = getStartOffset(ast);
        int endOffset = getEndOffset(ast);
        
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        CharSequence name = QualifiedNameCache.getManager().getString(nameHolder.getName());
        if (name.length() == 0) {
            throw AstRendererException.throwAstRendererException((FileImpl) file, ast, startOffset, "Empty function name."); // NOI18N
        }
        CharSequence rawName = initRawName(ast);
        
        boolean _static = AstRenderer.FunctionRenderer.isStatic(ast, file, fileContent, name);
        FunctionImpl.CV_RL _const = AstRenderer.FunctionRenderer.isConst(ast);

        scope = AstRenderer.FunctionRenderer.getScope(scope, file, _static, false);

        FunctionImpl<T> functionImpl = new FunctionImpl<>(name, rawName, scope, _static, _const, file, startOffset, endOffset, global);        
        temporaryRepositoryRegistration(ast, global, functionImpl);
        
        StringBuilder clsTemplateSuffix = new StringBuilder();
        TemplateDescriptor templateDescriptor = createTemplateDescriptor(ast, file, functionImpl, clsTemplateSuffix, global);
        CharSequence classTemplateSuffix = NameCache.getManager().getString(clsTemplateSuffix);
        
        functionImpl.setTemplateDescriptor(templateDescriptor, classTemplateSuffix);
        functionImpl.setReturnType(type != null ? type : AstRenderer.FunctionRenderer.createReturnType(ast, functionImpl, file, objects));
        functionImpl.setParameters(AstRenderer.FunctionRenderer.createParameters(ast, functionImpl, file, fileContent), 
                AstRenderer.FunctionRenderer.isVoidParameter(ast));
        
        postObjectCreateRegistration(global, functionImpl);
        nameHolder.addReference(fileContent, functionImpl);
        return functionImpl;
    }
    
    /**
     * Method is used to get extra suffix for UID (like signature, scope, etc)
     * @return signature field
     */
    public final CharSequence getUIDExtraSuffix() {
        return uidExtraSuffix;
    }
    
    protected CharSequence createUIDExtraSuffix(AST ast) {
        // Do it only if needed
        if (AstUtil.hasExpandedTokens(ast)) {
            AST params = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_PARMLIST);
            if (params != null) {
                boolean first = true;
                StringBuilder sb = new StringBuilder(UID_INTERNAL_DATA_PREFIX); 
                sb.append("("); // NOI18N
                AST param = AstUtil.findChildOfType(params, CPPTokenTypes.CSM_PARAMETER_DECLARATION);
                while (param != null) {
                    AST stopAt = AstUtil.findChildOfType(param, CPPTokenTypes.CSM_VARIABLE_DECLARATION);
                    if (stopAt == null) {
                        stopAt = AstUtil.findChildOfType(param, CPPTokenTypes.CSM_PARMLIST);
                    }
                    ASTSignatureStringizer stringizer = new ASTSignatureStringizer(stopAt);
                    AstUtil.visitAST(stringizer, param);
                    if (!first) {
                        sb.append(","); // NOI18N
                    }
                    String stringParam = stringizer.getText();
                    if ((sb.length() + stringParam.length()) < UID_EXTRA_SUFFIX_MAX_LENGTH) {
                        sb.append(stringParam);
                    } else {
                        sb.append(UID_EXTRA_SUFFIX_TOO_LONG);
                        break;
                    }
                    first = false;
                    param = AstUtil.findSiblingOfType(param.getNextSibling(), CPPTokenTypes.CSM_PARAMETER_DECLARATION);
                }
                sb.append(")"); // NOI18N
                return sb.toString();
            }
        }
        return null;
    }
    
    protected static<T> void temporaryRepositoryRegistration(AST ast, boolean global, FunctionImpl<T> fun) {
        CharSequence uidExtraSuffix = fun.createUIDExtraSuffix(ast);
        if (uidExtraSuffix != null) {
            fun.uidExtraSuffix = QualifiedNameCache.getManager().getString(uidExtraSuffix);
        } else {
            fun.uidExtraSuffix = null;
        }
        temporaryRepositoryRegistration(global, fun);
    }

    protected void setTemplateDescriptor(TemplateDescriptor templateDescriptor, CharSequence classTemplateSuffix) {
        this.templateDescriptor = templateDescriptor;
        this.classTemplateSuffix = classTemplateSuffix;
    }

    protected void setReturnType(CsmType returnType) {
        this.returnType = returnType;
    }

    protected void setParameters(FunctionParameterListImpl parameterList, boolean voidParamList) {
        if (parameterList == null) {
            LOG.log(Level.WARNING, "NO PARAM LIST FOR FUNC:{0} at {1} in {2}", new Object[]{name, getStartOffset(), getContainingFile()}); // NOI18N
        }        
        this.parameterList = parameterList;
        setFlags(FLAGS_VOID_PARMLIST, voidParamList);
    }

    public void setScope(CsmScope scope) {
        unregisterInProject();
        _setScope(scope);
        registerInProject();
    }

    private void _setScope(CsmScope scope) {
        // for functions declared in bodies scope is CsmCompoundStatement - it is not Identifiable
        if ((scope instanceof CsmIdentifiable)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert scopeUID != null;
        } else {
            this.scopeRef = scope;
        }
    }

    /**
     * Return true, if this is a definition of function
     * that is declared in some other place
     * (in other words, that is prefixed with class or namespace.
     * Otherwise - for simple functions with body as the one below:
     * void foo() {}
     * returns false
     */
    public boolean isPureDefinition() {
        return getKind() == CsmDeclaration.Kind.FUNCTION_DEFINITION ||
                getKind() == CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION;
    }

    protected boolean hasFlags(short mask) {
        return (flags & mask) == mask;
    }

    protected final void setFlags(short mask, boolean value) {
        if (value) {
            flags |= mask;
        } else {
            flags &= ~mask;
        }
    }

    @Override
    public final boolean isStatic() {
        return hasFlags(FLAGS_STATIC);
    }

    protected final void setStatic(boolean value) {
        setFlags(FLAGS_STATIC, value);
    }

    protected CharSequence getScopeSuffix() {
        return classTemplateSuffix != null ? classTemplateSuffix : CharSequences.empty();
    }

    protected static CharSequence initRawName(AST node) {
        return findFunctionRawName(node);
    }

    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create(CharSequenceUtils.concatenate(getName(), templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }

    public List<CsmTemplateParameter> getInheritedTemplateParameters() {
        List<CsmTemplateParameter> allTemplateParams = getTemplateParameters();
        List<CsmTemplateParameter> params = new ArrayList<>();
        if(allTemplateParams != null) {
            int inheritedTemplateParametersNumber = (templateDescriptor != null) ? templateDescriptor.getInheritedTemplateParametersNumber() : 0;
            if(allTemplateParams.size() > inheritedTemplateParametersNumber) {
                Iterator<CsmTemplateParameter> iter = allTemplateParams.iterator();
                for (int i = 0; i < inheritedTemplateParametersNumber && iter.hasNext(); i++) {
                    params.add(iter.next());
                }
            }
        }
        return params;
    }

    public List<CsmTemplateParameter> getOwnTemplateParameters() {
        List<CsmTemplateParameter> allTemplateParams = getTemplateParameters();
        List<CsmTemplateParameter> params = new ArrayList<>();
        if(allTemplateParams != null) {
            int inheritedTemplateParametersNumber = (templateDescriptor != null) ? templateDescriptor.getInheritedTemplateParametersNumber() : 0;
            if(allTemplateParams.size() > inheritedTemplateParametersNumber) {
                Iterator<CsmTemplateParameter> iter = allTemplateParams.iterator();
                for (int i = 0; i < inheritedTemplateParametersNumber && iter.hasNext(); i++) {
                    iter.next();
                }
                for ( ;iter.hasNext();) {
                    params.add(iter.next());
                }
            }
        }
        return params;
    }

    public boolean isVoidParameterList(){
        return hasFlags(FLAGS_VOID_PARMLIST);
    }

    private static CharSequence findFunctionRawName(AST ast) {
        if( CastUtils.isCast(ast) ) {
            return CastUtils.getFunctionRawName(ast);
        }
        return AstUtil.getRawNameInChildren(ast);
    }

    public boolean isCStyleStatic() {
        return isStatic() && CsmKindUtilities.isFile(getScope());
    }

    @Override
    protected boolean registerInProject() {
        if (isCStyleStatic()) {
            // do NOT register in project C-style static funcions!
            return false;
        }
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
	    // implicitely calls RepositoryUtils.put()
            return ((ProjectBase) project).registerDeclaration(this);
        }
        return false;
    }

    private void unregisterInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).unregisterDeclaration(this);
            this.cleanUID();
        }
    }


    /** Gets this element name
     * @return name
     */
    @Override
    public CharSequence getName() {
        return name;
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if( (scope instanceof CsmNamespace) || (scope instanceof CsmClass) || (scope instanceof CsmNamespaceDefinition) ) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if( scopeQName != null && scopeQName.length() > 0 ) {
                if (!CsmKindUtilities.isSpecialization(scope)) {
                    return CharSequences.create(CharSequenceUtils.concatenate(scopeQName, getScopeSuffix(), "::", getQualifiedNamePostfix())); // NOI18N
                } else {
                    return CharSequences.create(CharSequenceUtils.concatenate(scopeQName, "::", getQualifiedNamePostfix())); // NOI18N
                }
                    
            }
        }
        return getName();
    }

    @Override
    public CharSequence[] getRawName() {
        return AstUtil.toRawName(rawName);
    }

    @Override
    public CharSequence getUniqueNameWithoutPrefix() {
        final CharSequence sign = getSignature();
        return CharSequenceUtils.concatenate(getQualifiedName(), sign.subSequence(getName().length(), sign.length()));
    }

    @Override
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION;
    }

    /** Gets this function's declaration text
     * @return declaration text
     */
    @Override
    public String getDeclarationText() {
        return "";
    }

    /**
     * Gets this function definition
     * TODO: describe getDefiition==this ...
     * @return definition
     */
    @Override
    public CsmFunctionDefinition getDefinition() {
        return getDefinition(null);
    }

    public CsmFunctionDefinition getDefinition(CsmClass baseClass) {
        if (!isValid()) {
            return null;
        }
        if( isCStyleStatic() ) {
            CsmFilter filter = CsmSelect.getFilterBuilder().createNameFilter(
                               getName(), true, true, false);
            Iterator<CsmOffsetableDeclaration> it = CsmSelect.getDeclarations(getContainingFile(), filter);
            while(it.hasNext()){
                CsmDeclaration decl = it.next();
                if( CsmKindUtilities.isFunctionDefinition(decl) ) {
                    if( getName().equals(decl.getName()) ) {
                        CsmFunctionDefinition fun = (CsmFunctionDefinition) decl;
                        if( getSignature().equals(fun.getSignature())) {
                            return fun;
                        }
                    }
                }
            }
            return null;
        }
        String uname;
        if(baseClass == null) {
            uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix(); //NOI18N
        } else {
            uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + // NOI18N
                OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR +
                baseClass.getQualifiedName().toString() + "::" + getSignature(); // NOI18N
        }
        CsmProject prj = getContainingFile().getProject();
        CsmFunctionDefinition def = findDefinition(prj, uname);
        if (def == null) {
            for (CsmProject lib : prj.getLibraries()){
                def = findDefinition(lib, uname);
                if (def != null) {
                    break;
                }
            }
        }
        if (def == null && (prj instanceof ProjectBase)) {
            for(CsmProject dependent : ((ProjectBase)prj).getDependentProjects()){
                def = findDefinition(dependent, uname);
                if (def != null) {
                    break;
                }
            }
        }
        if (def == null) {
            if(baseClass == null) {
                uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) + UNIQUE_NAME_SEPARATOR + getUniqueNameWithoutPrefix(); //NOI18N
            } else {
                uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) + // NOI18N
                        OffsetableDeclarationBase.UNIQUE_NAME_SEPARATOR +
                        baseClass.getQualifiedName().toString() + "::" + getSignature(); // NOI18N
            }
            def = findDefinition(prj, uname);
            if (def == null) {
                for (CsmProject lib : prj.getLibraries()) {
                    def = findDefinition(lib, uname);
                    if (def != null) {
                        break;
                    }
                }
            }
            if (def == null && (prj instanceof ProjectBase)) {
                for (CsmProject dependent : ((ProjectBase) prj).getDependentProjects()) {
                    def = findDefinition(dependent, uname);
                    if (def != null) {
                        break;
                    }
                }
            }
        }
        if(def == null && this instanceof FriendFunctionImpl) {
            // Bug 196157 - Template friend functions highlighting problems
            List<CsmSpecializationParameter> specializationParameters = ((FriendFunctionImpl)this).getSpecializationParameters();
            if(!specializationParameters.isEmpty()) {
                StringBuilder tparams = new StringBuilder();
                tparams.append('<'); // NOI18N
                for(int i = 0; i < specializationParameters.size(); i++) {
                    if(i != 0) {
                        tparams.append(','); // NOI18N
                    }
                    tparams.append("class"); // NOI18N
                }
                tparams.append('>'); // NOI18N                
                StringBuilder params = new StringBuilder();
                InstantiationProviderImpl.appendParametersSignature(getParameters(), params);
                uname = ""+Utils.getCsmDeclarationKindkey(CsmDeclaration.Kind.FUNCTION_DEFINITION) + UNIQUE_NAME_SEPARATOR + //NOI18N
                        getQualifiedName().toString() + tparams.toString() + params.toString();
                def = findDefinition(prj, uname);
                if (def == null) {
                    for (CsmProject lib : prj.getLibraries()){
                        def = findDefinition(lib, uname);
                        if (def != null) {
                            break;
                        }
                    }
                }
                if (def == null && (prj instanceof ProjectBase)) {
                    for(CsmProject dependent : ((ProjectBase)prj).getDependentProjects()){
                        def = findDefinition(dependent, uname);
                        if (def != null) {
                            break;
                        }
                    }
                }
            }
        }
        
        return def;
    }

    // method try to find definition in case cast operator definition is declared without scope
    // Why here? Must be in MethodImpl.
    private CsmDeclaration fixCastOperator(CsmProject prj, String uname) {
        CsmDeclaration result = null;
        int i = uname.indexOf(OPERATOR + " "); // NOI18N
        if (i > 0) {
            String leftPartOfFQN = uname.substring(0, i + 9);
            String rightPartOfFQN = uname.substring(i + 9);
            int j = rightPartOfFQN.lastIndexOf(APTUtils.SCOPE);
            if (j > 0) {
                String simplyfiedFQN = leftPartOfFQN + " " + rightPartOfFQN.substring(j + 2); // NOI18N
                result = prj.findDeclaration(simplyfiedFQN);
            }
            if (result == null && prj instanceof ProjectBase) {
                CsmClassifier ourClassifier = null;
                
                Collection<CsmOffsetableDeclaration> decls = ((ProjectBase) prj).findDeclarationsByPrefix(leftPartOfFQN);
                
                for (CsmOffsetableDeclaration candidate : decls) {
                    if (CsmKindUtilities.isCastOperator(candidate)) {
                        if (ourClassifier == null) {
                            ourClassifier = getCastOperatorCastEntity(this);
                            if (!checkResolvedClassifier(ourClassifier)) {
                                break;
                            }
                        }
                        
                        FunctionImpl operator = (FunctionImpl) candidate;
                        CsmClassifier defClassifier = getCastOperatorCastEntity(operator);
                        if (checkResolvedClassifier(defClassifier)) {
                            if (defClassifier.getQualifiedName().toString().equals(ourClassifier.getQualifiedName().toString())) {
                                result = candidate;
                                break;
                            } else if (CsmKindUtilities.isTemplateParameter(ourClassifier) && CsmKindUtilities.isTemplateParameter(defClassifier)) {
                                result = candidate;
                                break;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    protected CsmClassifier getCastOperatorCastEntity(CsmFunction operator) {
        assert CsmKindUtilities.isCastOperator(operator) : "Must be cast operator!"; // NOI18N
     
        CsmType retType = operator.getReturnType();
        CsmClassifier castClassifier = retType != null ? CsmClassifierResolver.getDefault().getTypeClassifier(retType, retType.getContainingFile(), retType.getStartOffset(), true) : null;
        if (!checkResolvedClassifier(castClassifier) || (CsmKindUtilities.isTemplateParameter(castClassifier) && !retType.isTemplateBased())) {                                
            retType = CsmExpressionResolver.resolveType(retType.getText(), retType.getContainingFile(), retType.getStartOffset(), null);
            castClassifier = retType != null ? CsmClassifierResolver.getDefault().getTypeClassifier(retType, retType.getContainingFile(), retType.getStartOffset(), true) : null;
        }                        
        if (!checkResolvedClassifier(castClassifier)) {
            castClassifier = null;
        }
        return castClassifier;
    }
    
    protected boolean checkResolvedClassifier(CsmClassifier cls) {
        return CsmBaseUtilities.isValid(cls) || CsmKindUtilities.isBuiltIn(cls);
    }

    public static boolean isObjectVisibleInFile(CsmFile currentFile, CsmOffsetableDeclaration item) {
        CsmFile file = item.getContainingFile();
        if (file == null) {
            return false;
        }
        if (file.equals(currentFile)) {
            return true;
        }
        return ((ProjectBase) currentFile.getProject()).getGraphStorage().isFileIncluded(currentFile, file);
    }

    private CsmFunctionDefinition findDefinition(CsmProject prj, String uname){
        Collection<CsmOffsetableDeclaration> defs = prj.findDeclarations(uname);
        CsmDeclaration res = null;
        if (defs.isEmpty()) {
            if (CsmKindUtilities.isCastOperator(this)) {
                res = fixCastOperator(prj, uname);
            }
        } else if (defs.size() == 1) {
            res = defs.iterator().next();
        } else {
            for(CsmOffsetableDeclaration decl : defs) {
                if (decl  instanceof CsmFunctionDefinition) {
                    if (isObjectVisibleInFile(decl.getContainingFile(), this)) {
                        res = decl;
                        break;
                    }
                    if (res == null) {
                        res = decl;
                    }
                }
            }
        }
        if (res instanceof CsmFunctionDefinition) {
            return (CsmFunctionDefinition)res;
        }
        if (prj instanceof ProjectBase) {
            int parmSize = getParameters().size();
            boolean isVoid = isVoidParameterList();
            String from = uname.substring(0, uname.indexOf('(')+1);
            Collection<CsmOffsetableDeclaration> decls = ((ProjectBase)prj).findDeclarationsByPrefix(from);
            CsmFunctionDefinition candidate = null;
            for(CsmOffsetableDeclaration decl : decls){
                if (CsmKindUtilities.isFunctionDefinition(decl)) {
                    CsmFunctionDefinition def = (CsmFunctionDefinition) decl;
                    int candidateParamSize = def.getParameters().size();
                    if (!isVoid && parmSize == 0) {
                        if (!Utils.isCppFile(decl.getContainingFile())){
                            return def;
                        }
                    }
                    if (parmSize == candidateParamSize) {
                        // TODO check overloads
                        if (candidate == null) {
                            candidate = def;
                        } else {
//                          return null;
                        }
                    }
                }
            }
            return candidate;
        }
        return null;
    }

    /**
     * Returns true if this class is template, otherwise false.
     * @return flag indicated if function is template
     */
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

    /**
     * Gets this function body.
     * The same as the following call:
     * (getDefinition() == null) ? null : getDefinition().getBody();
     *
     * TODO: perhaps it isn't worth keeping duplicate to getDefinition().getBody()? (though convenient...)
     * @return body of function
     */
    public CsmCompoundStatement getBody() {
        return null;
    }

    @Override
    public boolean isInline() {
        return false;
    }

//    public boolean isVirtual() {
//        return false;
//    }
//
//    public boolean isExplicit() {
//        return false;
//    }

    @Override
    public CsmType getReturnType() {
        return returnType;
    }

    @Override
    public FunctionParameterListImpl  getParameterList() {
        return parameterList;
    }

    @Override
    public Collection<CsmParameter>  getParameters() {
        return _getParameters();
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
    }

    @Override
    public CharSequence getSignature() {         
        if( signature == null ) {
            signature = QualifiedNameCache.getManager().getString(createSignature(getName(), getParameters(), getOwnTemplateParameters(), isConst(), isVolatile(), isLValue(), isRValue()));
        }
        assert !signature.toString().startsWith(UID_INTERNAL_DATA_PREFIX) : "Signature requested when object is not fully constructed!"; // NOI18N
        return signature;
    }   

    @Override
    public CsmFunction getDeclaration() {
        return this;
    }

    @Override
    public boolean isOperator() {
        return hasFlags(FLAGS_OPERATOR);
    }
    
    @Override
    public OperatorKind getOperatorKind() {
        OperatorKind out = OperatorKind.NONE;
        if (isOperator()) {
            String strName = getName().toString();
            int start = strName.indexOf(OPERATOR);
            assert start >= 0 : "must have word \"operator\" in name";
            start += OPERATOR.length();
            String signText = strName.substring(start).trim();
            OperatorKind binaryKind = OperatorKind.getKindByImage(signText, true);
            OperatorKind nonBinaryKind = OperatorKind.getKindByImage(signText, false);
            if (binaryKind != OperatorKind.NONE && nonBinaryKind != OperatorKind.NONE) {
                // select the best
                int nrParams = getNrParameters();
                if (nrParams == 0) {
                    out = nonBinaryKind;
                } else if (nrParams == 1) {
                    if (CsmKindUtilities.isClass(getScope())) {
                        out = binaryKind;
                    } else {
                        out = nonBinaryKind;
                    }
                } else if (nrParams == 2) {
                    out = binaryKind;
                } else {
                    out = nonBinaryKind;
                }
            } else if (binaryKind != OperatorKind.NONE) {
                out = binaryKind;
            } else if (nonBinaryKind != OperatorKind.NONE) {
                out = nonBinaryKind;
            } else {
                out = signText.length() > 0 ? OperatorKind.CONVERSION : OperatorKind.NONE;
            }
        }
        return out;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = new ArrayList<>();
        l.addAll(getParameters());
        return l;
    }

    static CharSequence createSignature(CharSequence name, Collection<CsmParameter> params, List<CsmTemplateParameter> templateParams, boolean isConstQualifier, boolean isVolatileQualifier, boolean isLValueQualifier, boolean isRValueQualifier) {
        // TODO: this fake implementation for Deimos only!
        // we should resolve parameter types and provide
        // kind of canonical representation here
        StringBuilder sb = new StringBuilder(name);
        appendTemplateSignature(sb, templateParams);
        InstantiationProviderImpl.appendParametersSignature(params, sb);
        if (isConstQualifier) {
            sb.append(" const"); // NOI18N
        }
        if (isVolatileQualifier) {
            sb.append(" volatile"); // NOI18N
        }
        if (isLValueQualifier) {
            sb.append(" &"); // NOI18N
        }
        if (isRValueQualifier) {
            sb.append(" &&"); // NOI18N
        }
        return sb;
    }

    private static void appendTemplateSignature(StringBuilder sb, List<CsmTemplateParameter> templateParams) {
        InstantiationProviderImpl.appendTemplateParamsSignature(templateParams, sb);
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        CsmScope scope = _getScope();
        if( scope instanceof MutableDeclarationsContainer ) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
        this.unregisterInProject();
        _disposeParameters();
        setFlags(FLAGS_INVALID, true);
    }

    @Override
    public boolean isValid() {
        return !hasFlags(FLAGS_INVALID) && super.isValid();
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }

    /**
     * isConst was originally in MethodImpl;
     * but this methods needs internally in FunctionDefinitionImpl
     * to create proper signature.
     * Therefor it's moved here as a protected method.
     */
    protected boolean isConst() {
        return hasFlags(FLAGS_CONST);
    }

    protected boolean isVolatile() {
        return hasFlags(FLAGS_VOLATILE);
    }

    protected boolean isLValue() {
        return hasFlags(FLAGS_LVALUE_REF);
    }

    protected boolean isRValue() {
        return hasFlags(FLAGS_RVALUE_REF);
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            // this is possible situation when scope is already invalidated (see IZ#154264)
            //assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    private Collection<CsmParameter> _getParameters() {
        if (this.parameterList == null) {
            return Collections.<CsmParameter>emptyList();
        } else {
            return parameterList.getParameters();
        }
    }

    private int getNrParameters() {
        if (isVoidParameterList() || this.parameterList == null) {
            return 0;
        } else {
            return this.parameterList.getNrParameters();
        }
    }

    private void _disposeParameters() {
        if (this.parameterList != null) {
            parameterList.dispose();
        }
    }

    // help method in base class to choose on of declarations for fun definition
    protected final CsmFunction chooseDeclaration(Collection<CsmDeclaration> decls) {
        CsmFunction out = null;
        if (decls.size() == 1) {
            out = (CsmFunction) decls.iterator().next();
        } else {
            // choose declaration based on file name
            CsmFile sortFile = null;
            for (CsmDeclaration decl : decls) {
                CsmFunction fun = (CsmFunction) decl;
                CsmFile containingFile = fun.getContainingFile();
                if (sortFile == null) {
                    sortFile = containingFile;
                    out = fun;
                } else if (CharSequences.comparator().compare(sortFile.getAbsolutePath(), containingFile.getAbsolutePath()) > 0) {
                    sortFile = containingFile;
                    out = fun;
                }
            }
        }
        return out;
    }

    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 47 * hash + Objects.hashCode(this.signature);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        final FunctionImpl<?> other = (FunctionImpl<?>) obj;
        if (!Objects.equals(this.signature, other.signature)) {
            return false;
        }
        return true;
    }
    
    public static class FunctionBuilder extends SimpleDeclarationBuilder {

        protected FunctionBuilder() {
        }

        public FunctionBuilder(SimpleDeclarationBuilder builder) {
            super(builder);
        }
    
        @Override
        public FunctionImpl create() {
            CsmScope scope = AstRenderer.FunctionRenderer.getScope(getScope(), getFile(), isStatic(), false);

            FunctionImpl fun = new FunctionImpl(getName(), getRawName(), scope, isStatic(), CV_RL.isConst(isConst()), getFile(), getStartOffset(), getEndOffset(), isGlobal());
            
            init(fun);
            
            return fun;
        }
        
        protected void init(FunctionImpl fun) {
            temporaryRepositoryRegistration(isGlobal(), fun);

            setTemplateDescriptor(fun);
            setReturnType(fun);
            setParameters(fun);

            postObjectCreateRegistration(isGlobal(), fun);
            addReference(fun);
            
            addDeclaration(fun);
        }
        
        protected void setReturnType(FunctionImpl fun) {
            fun.setReturnType(getType());
        }

        protected void setParameters(FunctionImpl fun) {
            if(getParametersListBuilder() instanceof FunctionParameterListBuilder) {
                ((FunctionParameterListBuilder)getParametersListBuilder()).setScope(fun);
                fun.setParameters(((FunctionParameterListBuilder)getParametersListBuilder()).create(), true);
            }
        }
        
        protected void setTemplateDescriptor(FunctionImpl fun) {
            if(getTemplateDescriptorBuilder() != null) {
                fun.setTemplateDescriptor(getTemplateDescriptor(), NameCache.getManager().getString(CharSequences.create(""))); // NOI18N
            }
        }        

    }          
    
    public static class ASTSignatureStringizer extends AstUtil.ASTTokensStringizer {
        
        private final AST stopAt;

        public ASTSignatureStringizer(AST stopAt) {
            this.stopAt = stopAt;
        }

        @Override
        public Action visit(AST token) {
            if (token == stopAt) {
                return Action.ABORT;
            } else if (token.getType() == CPPTokenTypes.LPAREN || token.getType() == CPPTokenTypes.RPAREN) {
                return Action.CONTINUE;
            }
            return super.visit(token);
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);
        PersistentUtils.writeUTF(uidExtraSuffix, output);
        PersistentUtils.writeType(this.returnType, output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        PersistentUtils.writeParameterList(this.parameterList, output);
        PersistentUtils.writeUTF(this.rawName, output);
        factory.writeUID(this.scopeUID, output);
        PersistentUtils.writeUTF(this.signature, output);
        output.writeShort(flags);
        PersistentUtils.writeUTF(getScopeSuffix(), output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
    }

    public FunctionImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;
        this.uidExtraSuffix = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.returnType = PersistentUtils.readType(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.parameterList = (FunctionParameterListImpl) PersistentUtils.readParameterList(input, this);
        this.rawName = PersistentUtils.readUTF(input, NameCache.getManager());
        this.scopeUID = factory.readUID(input);
        this.scopeRef = null;
        this.signature = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        this.flags = input.readShort();
        this.classTemplateSuffix = PersistentUtils.readUTF(input, NameCache.getManager());
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
    }
}
