/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

