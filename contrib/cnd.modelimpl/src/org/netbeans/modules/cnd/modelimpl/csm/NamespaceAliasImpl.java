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
import java.util.List;
import org.netbeans.lib.editor.util.CharSequenceUtilities;
import org.netbeans.modules.cnd.antlr.collections.AST;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceAlias;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.CsmVisibility;
import org.netbeans.modules.cnd.apt.utils.APTUtils;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.AstUtil;
import org.netbeans.modules.cnd.modelimpl.csm.core.FileImpl;
import org.netbeans.modules.cnd.modelimpl.csm.core.OffsetableDeclarationBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.csm.core.RawNamable;
import org.netbeans.modules.cnd.modelimpl.csm.core.Utils;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
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
 * Implements CsmNamespaceAlias
 *
 */
public final class NamespaceAliasImpl extends OffsetableDeclarationBase<CsmNamespaceAlias> implements CsmNamespaceAlias, RawNamable {

    private final CharSequence alias;
    private final CharSequence namespace;
    private final CharSequence rawName;
    private CsmUID<CsmNamespace> referencedNamespaceUID = null;
    private CsmUID<CsmScope> scopeUID = null;

    private NamespaceAliasImpl(AST ast, CsmFile file, CsmScope scope) {
        super(file, getStartOffset(ast), getEndOffset(ast));
        _setScope(scope);
        rawName = createRawName(ast);
        alias = NameCache.getManager().getString(AstUtil.getText(ast));
        AST token = ast.getFirstChild();
        while (token != null && token.getType() != CPPTokenTypes.ASSIGNEQUAL) {
            token = token.getNextSibling();
        }
        StringBuilder sb = new StringBuilder();
        if (token == null) {
            if (FileImpl.reportErrors) {
                int ln = ast.getLine();
                int col = ast.getColumn();
                AST child = ast.getFirstChild();
                if (child != null) {
                    ln = child.getLine();
                    col = child.getColumn();
                }
                System.err.println("Corrupted AST for namespace alias in "
                        + file.getAbsolutePath() + ' ' + ln + ":" + col); // NOI18N
            }
            namespace = CharSequences.empty();
        } else {
            for (token = token.getNextSibling(); token != null; token = token.getNextSibling()) {
                sb.append(AstUtil.getText(token));
            }
            namespace = QualifiedNameCache.getManager().getString(sb);
        }
    }

    private NamespaceAliasImpl(CharSequence alias, CharSequence namespace, CharSequence rawName, CsmScope scope, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        _setScope(scope);
        this.rawName = rawName;
        this.alias = alias;
        this.namespace = namespace;
    }

    public static NamespaceAliasImpl create(AST ast, CsmFile file, CsmScope scope, boolean global) {
        NamespaceAliasImpl namespaceAliasImpl = new NamespaceAliasImpl(ast, file, scope);
        if (!global) {
            Utils.setSelfUID(namespaceAliasImpl);
        }
        return namespaceAliasImpl;
    }

    private void _setScope(CsmScope scope) {
        this.scopeUID = UIDCsmConverter.scopeToUID(scope);
        assert (scopeUID != null || scope == null);
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
        assert (scope != null || this.scopeUID == null) : "null object for UID " + this.scopeUID;
        return scope;
    }

    @Override
    public CsmNamespace getReferencedNamespace() {
        if (!isValid()) {
            return null;
        }

        CsmNamespace res = null;

        if (CharSequenceUtilities.startsWith(namespace, APTUtils.SCOPE)) {
            // If namespace has '::' as prefix, then it is a full path from global namspace

            res = ((ProjectBase) (getContainingFile().getProject())).findNamespace(
                    namespace.subSequence(APTUtils.SCOPE.length(), namespace.length()),
                    true);
        } else {
            // Otherwise we must search it in all namespaces from current down to global

            CsmScope scope = getScope();

            if (scope instanceof CsmNamespace) {
                CsmNamespace currentNamespace = (CsmNamespace) scope;

                do {
                    if (!currentNamespace.isGlobal()) {
                        StringBuilder sb = new StringBuilder(currentNamespace.getQualifiedName());
                        sb.append(APTUtils.SCOPE);
                        sb.append(namespace);
                        res = ((ProjectBase) (getContainingFile().getProject())).findNamespace(sb, true);
                    }
                } while (res == null && ((currentNamespace = currentNamespace.getParent()) != null));
            }

            if (res == null) {
                // target namespace is not in nested namespaces => search it in global
                res = ((ProjectBase) (getContainingFile().getProject())).findNamespace(namespace, true);
            }
        }
        return res;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return CsmDeclaration.Kind.NAMESPACE_ALIAS;
    }

    @Override
    public CharSequence getAlias() {
        return alias;
    }

    @Override
    public CharSequence getName() {
        return getAlias();
    }

    @Override
    public CharSequence getQualifiedName() {
        CsmScope scope = getScope();
        if ((scope instanceof CsmNamespace) || (scope instanceof CsmNamespaceDefinition)) {
            CharSequence scopeQName = ((CsmQualifiedNamedElement) scope).getQualifiedName();
            if (scopeQName != null && scopeQName.length() > 0) {
                return CharSequences.create(CharSequenceUtils.concatenate(scopeQName, "::", getQualifiedNamePostfix())); // NOI18N
            }
        }
        return getName();
    }

    private static CharSequence createRawName(AST node) {
        AST token = node.getFirstChild();
        while (token != null && token.getType() != CPPTokenTypes.ASSIGNEQUAL) {
            token = token.getNextSibling();
        }
        if (token != null) {
            token = token.getNextSibling();
            if (token != null && token.getType() == CPPTokenTypes.CSM_QUALIFIED_ID) {
                return AstUtil.getRawName(token.getFirstChild());
            }
        }
        return CharSequences.empty();
    }

    @Override
    public CharSequence[] getRawName() {
        return AstUtil.toRawName(rawName);
    }

    @Override
    public String toString() {
        return "" + getKind() + ' ' + alias + '=' + namespace + getPositionString(); // NOI18N
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
    }

    @Override
    public void dispose() {
        super.dispose();
        CsmScope scope = _getScope();
        if (scope instanceof MutableDeclarationsContainer) {
            ((MutableDeclarationsContainer) scope).removeDeclaration(this);
        }
    }

    public static class NamespaceAliasBuilder implements CsmObjectBuilder {

        private CharSequence name;// = CharSequences.empty();
        private CharSequence namespaceName;// = CharSequences.empty();
        private int nameStartOffset;
        private int nameEndOffset;
        private CsmDeclaration.Kind kind = CsmDeclaration.Kind.CLASS;
        private CsmFile file;
        private final FileContent fileContent;
        private int startOffset;
        private int endOffset;
        CsmVisibility visibility;
        private CsmObjectBuilder parent;
        private CsmScope scope;
        private NamespaceAliasImpl instance;
        private final List<CsmOffsetableDeclaration> declarations = new ArrayList<>();

        public NamespaceAliasBuilder(FileContent fileContent) {
            assert fileContent != null;
            this.fileContent = fileContent;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
        }

        public void setName(CharSequence name, int startOffset, int endOffset) {
            if (this.name == null) {
                this.name = name;
                this.nameStartOffset = startOffset;
                this.nameEndOffset = endOffset;
            }
        }

        public void setNamespaceName(CharSequence namespaceName) {
            this.namespaceName = namespaceName;
        }

        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }

        public CharSequence getName() {
            return name;
        }

        public void setFile(CsmFile file) {
            this.file = file;
        }

        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public void setParent(CsmObjectBuilder parent) {
            this.parent = parent;
        }

        public void addDeclaration(CsmOffsetableDeclaration decl) {
            this.declarations.add(decl);
        }

        private NamespaceAliasImpl getNamespaceAliasInstance() {
            if (instance != null) {
                return instance;
            }
            MutableDeclarationsContainer container = null;
            if (parent == null) {
                container = fileContent;
            } else {
                if (parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    container = ((NamespaceDefinitionImpl.NamespaceBuilder) parent).getNamespaceDefinitionInstance();
                }
            }
            if (container != null && name != null) {
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(startOffset, name, kind);
                if (decl != null && NamespaceAliasImpl.class.equals(decl.getClass())) {
                    instance = (NamespaceAliasImpl) decl;
                }
            }
            return instance;
        }

        public void setScope(CsmScope scope) {
            assert scope != null;
            this.scope = scope;
        }

        public CsmScope getScope() {
            if (scope != null) {
                return scope;
            }
            if (parent == null) {
                scope = (NamespaceImpl) file.getProject().getGlobalNamespace();
            } else {
                if (parent instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    scope = ((NamespaceDefinitionImpl.NamespaceBuilder) parent).getNamespace();
                }
            }
            return scope;
        }

        public NamespaceAliasImpl create() {
            NamespaceAliasImpl using = getNamespaceAliasInstance();
            CsmScope s = getScope();
            if (using == null && s != null && name != null && getScope() != null) {
                using = new NamespaceAliasImpl(name, namespaceName, name, scope, file, startOffset, endOffset);
                if (parent != null) {
                    ((NamespaceDefinitionImpl.NamespaceBuilder) parent).addDeclaration(using);
                } else {
                    fileContent.addDeclaration(using);
                }
            }
            if (getScope() instanceof CsmNamespace) {
                ((NamespaceImpl) getScope()).addDeclaration(using);
            }
            return using;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // iml of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.alias != null;
        PersistentUtils.writeUTF(alias, output);
        assert this.namespace != null;
        PersistentUtils.writeUTF(namespace, output);
        PersistentUtils.writeUTF(this.rawName, output);

        // save cached namespace
        UIDObjectFactory.getDefaultFactory().writeUID(this.referencedNamespaceUID, output);
        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);
    }

    public NamespaceAliasImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.alias = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.alias != null;
        this.namespace = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.namespace != null;
        this.rawName = PersistentUtils.readUTF(input, NameCache.getManager());

        // read cached namespace
        this.referencedNamespaceUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
    }
}
