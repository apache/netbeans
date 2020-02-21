/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
