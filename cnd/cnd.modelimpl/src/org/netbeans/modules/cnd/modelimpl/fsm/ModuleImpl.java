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

import java.util.*;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.modelimpl.csm.FunctionImpl;
import org.netbeans.modules.cnd.modelimpl.csm.MutableDeclarationsContainer;
import org.netbeans.modules.cnd.modelimpl.csm.NamespaceImpl;
import org.netbeans.modules.cnd.modelimpl.csm.VariableImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;

/**
 */
public final class ModuleImpl extends OffsetableDeclarationBase<CsmNamespaceDefinition>
    implements CsmModule, CsmNamespaceDefinition, MutableDeclarationsContainer, Disposable {

    private final List<CsmUID<CsmOffsetableDeclaration>> declarations;
    private final CharSequence name;

    private ModuleImpl(CsmFile file, int startOffset, int endOffset, String name) {
        super(file, startOffset, endOffset);
        declarations = new ArrayList<>();
        this.name = NameCache.getManager().getString(name);
    }

    public static ModuleImpl create(CsmFile file, int startOffset, int endOffset, String name) {
        ModuleImpl moduleImpl = new ModuleImpl(file, startOffset, endOffset, name);
        postObjectCreateRegistration(true, moduleImpl);
        return moduleImpl;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_DEFINITION;
    }

    @Override
    public Collection<CsmOffsetableDeclaration> getDeclarations() {
        Collection<CsmOffsetableDeclaration> decls;
        synchronized (declarations) {
            decls = UIDCsmConverter.UIDsToDeclarations(declarations);
        }
        return decls;
    }

    public Iterator<CsmOffsetableDeclaration> getDeclarations(CsmFilter filter) {
        Iterator<CsmOffsetableDeclaration> out;
        synchronized (declarations) {
            out = UIDCsmConverter.UIDsToDeclarationsFiltered(declarations, filter);
         }
         return out;
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
    public CsmNamespace getNamespace() {
        return null;
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
        return getContainingFile();
    }

    @Override
    public boolean isInline() {
        return false;
    }

    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        List<CsmScopeElement> l = new ArrayList<>();
        for (Iterator<CsmOffsetableDeclaration> iter = getDeclarations().iterator(); iter.hasNext();) {
            CsmDeclaration decl = iter.next();
            if (isOfMyScope(decl)) {
                l.add(decl);
            }
        }
        return l;
    }

    private boolean isOfMyScope(CsmDeclaration decl) {
        if (decl instanceof VariableImpl<?>) {
            return ! NamespaceImpl.isNamespaceScope((VariableImpl<?>) decl, false);
        } else if (decl instanceof FunctionImpl<?>) {
            return ! NamespaceImpl.isNamespaceScope((FunctionImpl<?>) decl);

        } else {
            return false;
        }
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

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent

    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.declarations, output, true);

        assert this.name != null;
        PersistentUtils.writeUTF(name, output);

        if (getName().length() == 0) {
            writeUID(output);
        }
    }

    public ModuleImpl(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        int collSize = input.readInt();
        if (collSize < 0) {
            declarations = new ArrayList<>();
        } else {
            declarations = new ArrayList<>(collSize);
        }
        factory.readUIDCollection(declarations, input, collSize);

        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;

        if (getName().length() == 0) {
            readUID(input);
        }
    }
}

