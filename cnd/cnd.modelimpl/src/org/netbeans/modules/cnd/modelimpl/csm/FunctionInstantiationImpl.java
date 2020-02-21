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

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionParameterListImpl.FunctionParameterListBuilder;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 */
public final class FunctionInstantiationImpl extends OffsetableDeclarationBase<CsmFunctionInstantiation> implements CsmFunctionInstantiation {

    private final FunctionParameterListImpl parameterList;

    private /*final*/ CsmScope scopeRef;// can be set in onDispose or contstructor only
    private CsmUID<CsmScope> scopeUID;

    private final CharSequence name;

    private FunctionInstantiationImpl(AST ast, CsmFile file, CsmScope scope, NameHolder nameHolder, FunctionParameterListImpl params) throws AstRendererException {
        super(file, getStartOffset(ast), getEndOffset(ast));
        this.parameterList = params;
        _setScope(scope);
        name = QualifiedNameCache.getManager().getString(nameHolder.getName());
    }

    private FunctionInstantiationImpl(CharSequence name, CsmScope scope, FunctionParameterListImpl params, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        this.parameterList = params;
        _setScope(scope);
        this.name = name;
    }

    public static FunctionInstantiationImpl create(AST ast, CsmFile file, FileContent fileContent, boolean register) throws AstRendererException {
        NameHolder nameHolder = NameHolder.createFunctionName(ast);
        FunctionParameterListImpl params = FunctionParameterListImpl.create(file, fileContent, ast, null);
        FunctionInstantiationImpl res =  new FunctionInstantiationImpl(ast, file, null, nameHolder, params);
        postObjectCreateRegistration(register, res);
        nameHolder.addReference(fileContent, res);
        return res;
    }

    @Override
    public Kind getKind() {
        return CsmDeclaration.Kind.FUNCTION_INSTANTIATION;
    }

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
                return CharSequences.create(CharSequenceUtils.concatenate(scopeQName, "::", getQualifiedNamePostfix())); // NOI18N
            }
        }
        return getName();
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
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

    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
            // this is possible situation when scope is already invalidated (see IZ#154264)
            //assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        Collection<CsmScopeElement> l = new ArrayList<>();
        l.addAll(getParameters());
        return l;
    }

    @Override
    public Collection<CsmParameter>  getParameters() {
        return _getParameters();
    }

    private Collection<CsmParameter> _getParameters() {
        if (this.parameterList == null) {
            return Collections.<CsmParameter>emptyList();
        } else {
            return parameterList.getParameters();
        }
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
    }

    private void _disposeParameters() {
        if (this.parameterList != null) {
            parameterList.dispose();
        }
    }

    private void unregisterInProject() {
        CsmProject project = getContainingFile().getProject();
        if( project instanceof ProjectBase ) {
            ((ProjectBase) project).unregisterDeclaration(this);
            this.cleanUID();
        }
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);
            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID;
        }
    }


    public static class FunctionInstantiationBuilder extends SimpleDeclarationBuilder {

        @Override
        public FunctionInstantiationImpl create() {
            FunctionInstantiationImpl fun = new FunctionInstantiationImpl(getName(), null, ((FunctionParameterListBuilder)getParametersListBuilder()).create(), getFile(), getStartOffset(), getEndOffset());

            postObjectCreateRegistration(isGlobal(), fun);
            getNameHolder().addReference(getFileContent(), fun);

            return fun;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent

    public FunctionInstantiationImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.name = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.name != null;

        this.parameterList = (FunctionParameterListImpl) PersistentUtils.readParameterList(input, this);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        this.scopeUID = factory.readUID(input);
        this.scopeRef = null;
    }

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);

        PersistentUtils.writeParameterList(this.parameterList, output);

        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUID(this.scopeUID, output);
    }

}
