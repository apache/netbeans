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

import java.util.*;

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.openide.util.CharSequences;

/**
 * Implements CsmNamespaceDefinition
 */
public final class NamespaceDefinitionImpl extends OffsetableDeclarationBase<CsmNamespaceDefinition>
    implements CsmNamespaceDefinition, MutableDeclarationsContainer, Disposable {

    private final List<CsmUID<CsmOffsetableDeclaration>> declarations;
    
    private final CharSequence name;
    
    // only one of namespaceRef/namespaceUID must be used (based on USE_REPOSITORY/USE_UID_TO_CONTAINER)
    private /*final*/ NamespaceImpl namespaceRef;// can be set in onDispose or contstructor only
    private volatile CsmUID<CsmNamespace> namespaceUID;
    private final int leftBracketPos;
    
    private final boolean inline;
    
    private NamespaceDefinitionImpl(AST ast, CsmFile file, NamespaceImpl parent) {
        this(NameCache.getManager().getString(AstUtil.getText(ast)), parent, file, getStartOffset(ast), getEndOffset(ast), calcLeftBracketPos(ast), isInlineDefinition(ast));
    }

    private NamespaceDefinitionImpl(CharSequence name, NamespaceImpl parent, CsmFile file, int startOffset, int endOffset, int leftBracketPos, boolean inline) {
        super(file, startOffset, endOffset);
        declarations = new ArrayList<>();
        this.name = name;
        this.inline = inline;
        
        this.namespaceRef = null;
        
        this.leftBracketPos = leftBracketPos != -1 ? leftBracketPos : startOffset;

        // set parent ns, do it in constructor to have final fields
        namespaceUID = ((ProjectBase) file.getProject()).addNamespaceDefinition(parent, this);
        assert namespaceUID != null;
    }

     public static NamespaceDefinitionImpl findOrCreateNamespaceDefionition(MutableDeclarationsContainer container, AST ast, NamespaceImpl parentNamespace, FileImpl containerfile) {
        int start = getStartOffset(ast);
        int end = getEndOffset(ast);
        CharSequence name = NameCache.getManager().getString(AstUtil.getText(ast)); // otherwise equals returns false
        // #147376 Strange navigator behavior in header
        CsmOffsetableDeclaration candidate = container.findExistingDeclaration(start, name, CsmDeclaration.Kind.NAMESPACE_DEFINITION);
        
        if(TraceFlags.DYNAMIC_TESTS_TRACE) {
            if (containerfile.getName().toString().equals("FieldInfos.cpp") || containerfile.getName().toString().equals("CompoundFile.cpp")) { // NOI18N
                System.out.println("FieldInfos.cpp ns candidate " + candidate + " for name " + name); // NOI18N
            }
        }
        
        if (CsmKindUtilities.isNamespaceDefinition(candidate)) {
            return (NamespaceDefinitionImpl) candidate;
        } else {
//            assert !TraceFlags.CPP_PARSER_ACTION : candidate + " " + name + " " + AstUtil.getFirstCsmAST(ast).getLine() + " " + containerfile.getAbsolutePath() + " " + container ;
            NamespaceDefinitionImpl ns = new NamespaceDefinitionImpl(ast, containerfile, parentNamespace);
            container.addDeclaration(ns);
            return ns;
        }
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
        // look for the object with the same start position and the same name
        // TODO: for now we are in O(n), but better to be O(ln n) speed
        synchronized (declarations) {
            out = UIDUtilities.findExistingUIDInList(declarations, start, end, name);
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, CharSequence name, CsmDeclaration.Kind kind) {
        CsmUID<CsmOffsetableDeclaration> out = null;
        // look for the object with the same start position and the same name
        // TODO: for now we are in O(n), but better to be O(ln n) speed
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
        if (decl instanceof VariableImpl<?>) {
            VariableImpl<?> v = (VariableImpl<?>) decl;
            if (!NamespaceImpl.isNamespaceScope(v, false)) {
                v.setScope(this);
            }
        }
        if (decl instanceof FunctionImpl<?>) {
            FunctionImpl<?> f = (FunctionImpl<?>) decl;
            if (!NamespaceImpl.isNamespaceScope(f)) {
                f.setScope(this);
            }
        }
        if (decl instanceof CsmUsingDirective) {
            NamespaceImpl logicalNs = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
            logicalNs.addUsingDirective((CsmUsingDirective) decl);
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
    public CharSequence getQualifiedName() {
        CsmNamespace ns = getNamespace();
        return ns != null ? ns.getQualifiedName() : getName();
    }

    @Override
    public CsmNamespace getNamespace() {
        return _getNamespaceImpl();
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
        return inline;
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
    
    public Iterator<CsmScopeElement> getScopeElements(CsmFilter filter) {
        List<CsmScopeElement> l = new ArrayList<>();
        for (Iterator<CsmOffsetableDeclaration> iter = getDeclarations(filter); iter.hasNext();) {
            CsmDeclaration decl = iter.next();
            if (isOfMyScope(decl)) {
                l.add(decl);
            }
        }
        return l.iterator();
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
        onDispose();
        //NB: we're copying declarations, because dispose can invoke this.removeDeclaration
        Collection<CsmOffsetableDeclaration> decls;
        List<CsmUID<CsmOffsetableDeclaration>> uids;
        synchronized (declarations) {
            decls = getDeclarations();
            uids = new ArrayList<>(declarations);
            declarations.clear();
            //declarations  = Collections.synchronizedList(new ArrayList<CsmUID<CsmOffsetableDeclaration>>());
        }     
        NamespaceImpl ns = _getNamespaceImpl();
        assert ns != null;                 
        for (CsmOffsetableDeclaration decl : decls) {
            if (decl instanceof CsmUsingDirective) {
                ns.removeUsingDirective((CsmUsingDirective) decl);
            }
        }                        
        Utils.disposeAll(decls);            
        RepositoryUtils.remove(uids);                              
        ((ProjectBase) getProject()).removeNamespaceDefinition(this);
    }

    private synchronized void onDispose() {
        if (this.namespaceRef == null) {
            // restore container from it's UID
            this.namespaceRef = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
            assert this.namespaceRef != null || this.namespaceUID == null : "no object for UID " + this.namespaceUID;
        }
    }
    
    private static int calcLeftBracketPos(AST node) {
        AST lcurly = AstUtil.findChildOfType(node, CPPTokenTypes.LCURLY);
        return (lcurly instanceof CsmAST) ? ((CsmAST) lcurly).getOffset() : -1;
    }
    
    private static boolean isInlineDefinition(AST node) {
        AST inline = AstUtil.findChildOfType(node, CPPTokenTypes.LITERAL_inline);
        return inline != null;
    }
    
    public int getLeftBracketOffset() {
        return leftBracketPos;
    }
    
    private synchronized NamespaceImpl _getNamespaceImpl() {
        NamespaceImpl impl = this.namespaceRef;
        if (impl == null) {
            impl = (NamespaceImpl) UIDCsmConverter.UIDtoNamespace(this.namespaceUID);
            assert impl != null || this.namespaceUID == null : "null object for UID " + this.namespaceUID;
        }
        return impl;
    }

    public static class NamespaceBuilder implements CsmObjectBuilder {
        
        private CharSequence name = CharSequences.empty();
        private String qName;
        private CsmFile file;
        private FileContent fileContent;
        private int startOffset;
        private int bodyStartOffset;
        private int endOffset;
        private NamespaceBuilder parent;
        private boolean inline;

        private NamespaceImpl namespace;
        private NamespaceDefinitionImpl instance;
        private final List<CsmOffsetableDeclaration> declarations = new ArrayList<>();
        
        public void setName(CharSequence name) {
            this.name = name;
            // for now without scope
            qName = name.toString();
        }

        public CharSequence getName() {
            return name;
        }
        
        public void setFile(CsmFile file) {
            this.file = file;
            this.fileContent = ((FileImpl)file).getParsingFileContent();
        }
        
        public void setEndOffset(int endOffset) {
            this.endOffset = endOffset;
        }

        public void setStartOffset(int startOffset) {
            this.startOffset = startOffset;
        }

        public void setBodyStartOffset(int bodyStartOffset) {
            this.bodyStartOffset = bodyStartOffset;
        }
        
        public void setParentNamespace(NamespaceBuilder parent) {
            this.parent = parent;
        }
        
        public void setInline(boolean inline) {
            this.inline = inline;
        }

        public void addDeclaration(CsmOffsetableDeclaration decl) {
            this.declarations.add(decl);
        }
        
        public NamespaceDefinitionImpl getNamespaceDefinitionInstance() {
            if(instance != null) {
                return instance;
            }
            MutableDeclarationsContainer container;
            if (parent == null) {
                container = fileContent;
            } else {
                container = parent.getNamespaceDefinitionInstance();
            }
            if(container != null) {
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(startOffset, name, CsmDeclaration.Kind.NAMESPACE_DEFINITION);
                if (CsmKindUtilities.isNamespaceDefinition(decl)) {
                    instance = (NamespaceDefinitionImpl) decl;
                }
            }
            return instance;
        }
        
        public NamespaceImpl getNamespace() {
            if(namespace != null) {
                return namespace;
            }
            if(parent != null) {
                namespace = ((ProjectBase) file.getProject()).findNamespaceCreateIfNeeded(parent.getNamespace(), name);
            } else {
                namespace = ((ProjectBase) file.getProject()).findNamespaceCreateIfNeeded((NamespaceImpl)((ProjectBase) file.getProject()).getGlobalNamespace(), name);
            }
            return namespace;
        }
        
        public NamespaceDefinitionImpl create() {
            NamespaceDefinitionImpl ns = getNamespaceDefinitionInstance();
            if (ns == null) {
                NamespaceImpl parentNamespace = parent != null ? parent.getNamespace() : (NamespaceImpl)((ProjectBase) file.getProject()).getGlobalNamespace();
                ns = new NamespaceDefinitionImpl(name, parentNamespace, file, startOffset, endOffset, bodyStartOffset, inline);
                if(parent != null) {
                    parent.addDeclaration(ns);
                } else {
                    fileContent.addDeclaration(ns);
                }
                for (CsmOffsetableDeclaration decl : declarations) {
                    ns.addDeclaration(decl);
                }                
            }
            return ns;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);  
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.declarations, output, true);
        
        // not null
        assert this.namespaceUID != null;
        factory.writeUID(this.namespaceUID, output);
        
        assert this.name != null;
        PersistentUtils.writeUTF(name, output);

        if (getName().length() == 0) {
            writeUID(output);
        }
        
        output.writeInt(leftBracketPos);
        output.writeBoolean(inline);
    }  

    public NamespaceDefinitionImpl(RepositoryDataInput input) throws IOException {
        super(input);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        int collSize = input.readInt();
        if (collSize < 0) {
            declarations = new ArrayList<>();
        } else {
            declarations = new ArrayList<>(collSize);
        }
        factory.readUIDCollection(declarations, input, collSize);
        
        this.namespaceUID = factory.readUID(input);
        // not null UID
        assert this.namespaceUID != null;
        this.namespaceRef = null;    
        
        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;

        if (getName().length() == 0) {
            readUID(input);
        }
        
        this.leftBracketPos = input.readInt();
        this.inline = input.readBoolean();
    }
}
