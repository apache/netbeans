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

import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.textcache.QualifiedNameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.netbeans.modules.cnd.modelimpl.textcache.NameCache;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;
import org.openide.util.CharSequences;

/**
 * Common ancestor for ClassImpl and EnumImpl
 */
public abstract class ClassEnumBase<T> extends OffsetableDeclarationBase<T> implements CsmCompoundClassifier, CsmMember {

    private final CharSequence name;
    private /*final*/ CharSequence qualifiedName;
    // only one of scopeRef/scopeAccessor must be used (based on USE_REPOSITORY)
    private CsmScope scopeRef;// can be set in onDispose or contstructor only
    private CsmUID<CsmScope> scopeUID;
    private boolean isValid = true;
    private boolean _static = false;
    private CsmVisibility visibility = CsmVisibility.PRIVATE;
    // keep enclosing typeds and enclosing variables in one collection
    private final List<CsmUID<CsmOffsetableDeclaration>> enclosingElements;

    protected ClassEnumBase(NameHolder name, CsmFile file, AST ast) {
        this(name, file, getStartOffset(ast), getEndOffset(ast));
    }

    protected ClassEnumBase(NameHolder name, CsmFile file, int start, int end) {
        super(file, start, end);
        enclosingElements = Collections.synchronizedList(new ArrayList<CsmUID<CsmOffsetableDeclaration>>(0));
        assert name != null;
        this.name = NameCache.getManager().getString(name.getName());
    }

    protected ClassEnumBase(CharSequence name, CharSequence qName, CsmFile file, int startOffset, int endOffset) {
        super(file, startOffset, endOffset);
        enclosingElements = Collections.synchronizedList(new ArrayList<CsmUID<CsmOffsetableDeclaration>>(0));
        this.name = NameCache.getManager().getString(name);
        this.qualifiedName = QualifiedNameCache.getManager().getString(qName);
    }

    public static int getEndOffset(AST node) {
        if (node != null) {
            AST rcurly = AstUtil.findChildOfType(node, CPPTokenTypes.RCURLY);
            if (rcurly instanceof CsmAST) {
                return ((CsmAST) rcurly).getEndOffset();
            } else {
                // this could be unnamed struct/union/class with enclosing typedef
                switch (node.getType()) {
                    case CPPTokenTypes.LITERAL_enum:
                    case CPPTokenTypes.LITERAL_class:
                    case CPPTokenTypes.LITERAL_struct:
                    case CPPTokenTypes.LITERAL_union:
                        rcurly = AstUtil.findSiblingOfType(node, CPPTokenTypes.RCURLY);
                }
                if (rcurly instanceof CsmAST) {
                    return ((CsmAST) rcurly).getEndOffset();
                }
                return OffsetableBase.getEndOffset(node);
            }
        }
        return 0;
    }

    @Override
    public final CharSequence getName() {
//        if (name != null && CharSequences.indexOf(name, "::") > 0) { // NOI18N
//            String n = name.toString();
//            String suffix = n.substring(n.lastIndexOf("::") + 2); // NOI18N
//            return NameCache.getManager().getString(suffix);
//        }
        return name;
    }

    /**
     * Used for constructs like
     * class A {
     *     struct B;
     *     // ...
     * };
     * struct A::B {
     *     // ...
     * }
     * See BZ #131625
     * @return  For "struct A::B" above, the method returns its forward declaration "struct B;"
     */
    protected final ClassImpl.MemberForwardDeclaration findMemberForwardDeclaration(CsmScope scope) {
        if (name != null && CharSequences.indexOf(name, "::") > 0) { // NOI18N
            String n = name.toString();
            String prefix = n.substring(0, n.lastIndexOf("::")); // NOI18N
            String suffix = n.substring(n.lastIndexOf("::") + 2); // NOI18N
            if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                CharSequence qn;
                if (ns.isGlobal()) {
                    qn = prefix;
                } else {
                    qn = CharSequenceUtils.concatenate(ns.getQualifiedName(), "::", prefix); // NOI18N
                }
                Collection<CsmClassifier> defs = ns.getProject().findClassifiers(qn);
                ClassImpl.MemberForwardDeclaration out = null;
                for(CsmClassifier cls : defs) {
                    if (CsmKindUtilities.isClass(cls)) {
                        CsmClass container = (CsmClass) cls;
                        Iterator<CsmMember> it = CsmSelect.getClassMembers(container,
                                CsmSelect.getFilterBuilder().createNameFilter(suffix, true, true, false));
                        if (it.hasNext()) {
                            CsmMember m = it.next();
                            if (m instanceof ClassImpl.MemberForwardDeclaration) {
                                if (FunctionImpl.isObjectVisibleInFile(getContainingFile(), m)){
                                    out = (ClassImpl.MemberForwardDeclaration) m;
                                }
                                if (out == null) {
                                    out = (ClassImpl.MemberForwardDeclaration) m;
                                }
                            }
                        }
                    }
                }
                return out;
            }
        }
        return null;
    }

    /** Initializes scope */
    protected final void initScope(CsmScope scope) {
        assert !this.equals(scope) : "scope can not be recursive " + this + " vs. " + scope;
        if (UIDCsmConverter.isIdentifiable(scope)) {
            this.scopeUID = UIDCsmConverter.scopeToUID(scope);
            assert (this.scopeUID != null || scope == null) : "null UID for class scope " + scope;
            this.scopeRef = null;
        } else {
            // in the case of classes/enums inside bodies
            this.scopeRef = scope;
        }
        initQualifiedName(scope);
    }

    /** Initializes qualified name */
    protected final void initQualifiedName(CsmScope scope) {
        CharSequence qualifiedNamePostfix = getQualifiedNamePostfix();
        CharSequence qualName;
        if (CsmKindUtilities.isNamespace(scope)) {
            qualName = Utils.getQualifiedName(qualifiedNamePostfix.toString(), (CsmNamespace) scope);
        } else if (CsmKindUtilities.isClass(scope)) {
            int last = CharSequenceUtils.lastIndexOf(qualifiedNamePostfix, "::"); // NOI18N
            if (last >= 0) { // NOI18N
                qualifiedNamePostfix = qualifiedNamePostfix.toString().substring(last + 2); // NOI18N
            }
            qualName = CharSequenceUtils.concatenate(((CsmClass) scope).getQualifiedName(), "::", qualifiedNamePostfix); // NOI18N
        } else {
            qualName = qualifiedNamePostfix;
        }
        // substitution of qual name has to be atomic, because this instance can
        // already be registered and be in repository. Field qualifiedName always
        // must be compact string because only such strings are allowed in repository
        this.qualifiedName = QualifiedNameCache.getManager().getString(qualName);
        // can't register here, because descendant class' constructor hasn't yet finished!
        // so registering is a descendant class' responsibility
    }

    @Override
    abstract public Kind getKind();

    protected final void register(CsmScope scope, boolean registerUnnamedInNamespace) {

        RepositoryUtils.put(this);
        boolean registerInNamespace = registerUnnamedInNamespace;
        if (Utils.canRegisterDeclaration(this)) {
            registerInNamespace = registerInProject();
        }
        if (registerInNamespace) {
            if (getContainingClass() == null) {
                if (CsmKindUtilities.isNamespace(scope)) {
                    ((NamespaceImpl) scope).addDeclaration(this);
                }
            }
        }
    }

    @Override
    protected boolean registerInProject() {
        return ((ProjectBase) getContainingFile().getProject()).registerDeclaration(this);
    }

    private void unregisterInProject() {
        ((ProjectBase) getContainingFile().getProject()).unregisterDeclaration(this);
        this.cleanUID();
    }

    /**
     * Some classifiers, such as forward class declarations,
     * are registered fake classes prematurely,
     * to help the model cope with the absence of "real" classifiers
     *
     * In this case, as soon as "real" classifier appears,
     * it should replace such fake one;
     * and if the "real" one already exists,
     * then the fake one should not be registered
     *
     * @param another
     * @return true is this one is "weaker" than other onw,
     * i.e. this one should be substituted by other
     */
    public boolean shouldBeReplaced(CsmClassifier another) {
        return false;
    }

    public NamespaceImpl getContainingNamespaceImpl() {
        CsmScope scope = getScope();
        return (scope instanceof NamespaceImpl) ? (NamespaceImpl) scope : null;
    }

    @Override
    public CharSequence getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public CsmScope getScope() {
        return _getScope();
    }

    private synchronized CsmScope _getScope() {
        CsmScope scope = this.scopeRef;
        if (scope == null) {
            scope = UIDCsmConverter.UIDtoScope(this.scopeUID);
//            there could be a situation when scope is already disposed (like in onDispose())
//            assert (scope != null || this.scopeUID == null || !isValid()) : "null object for UID " + this.scopeUID;
        }
        return scope;
    }

    @Override
    public void dispose() {
        super.dispose();
        onDispose();
        if (getContainingNamespaceImpl() != null) {
            getContainingNamespaceImpl().removeDeclaration(this);
        }
        unregisterInProject();
        isValid = false;
    }

    private synchronized void onDispose() {
        if (this.scopeRef == null) {
            // restore container from it's UID if not directly initialized
            this.scopeRef = UIDCsmConverter.UIDtoScope(this.scopeUID);

            // there could be a situation when scope is already disposed
            // i.e. like in  #191610 -  unresolved reference to class declared with outer scope
//            assert (this.scopeRef != null || this.scopeUID == null) : "empty scope for UID " + this.scopeUID + " when dispose " + this;
        }
    }

    @Override
    public boolean isValid() {
        return isValid && super.isValid();
    }

    @Override
    public CsmClass getContainingClass() {
        CsmScope scope = getScope();
        return CsmKindUtilities.isClass(scope) ? (CsmClass) scope : null;
    }

    @Override
    public CsmVisibility getVisibility() {
        return visibility;
    }

    public void setVisibility(CsmVisibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public boolean isStatic() {
        return _static;
    }

    public void setStatic(boolean _static) {
        this._static = _static;
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        output.writeBoolean(this.isValid);

        assert this.name != null;
        PersistentUtils.writeUTF(name, output);

        assert this.qualifiedName != null;
        PersistentUtils.writeUTF(qualifiedName, output);

        UIDObjectFactory.getDefaultFactory().writeUID(this.scopeUID, output);

        output.writeBoolean(this._static);

        assert this.visibility != null;
        PersistentUtils.writeVisibility(this.visibility, output);

        // write UID for unnamed classifier
        if (getName().length() == 0) {
            super.writeUID(output);
        }
        UIDObjectFactory.getDefaultFactory().writeUIDCollection(enclosingElements, output, true);
    }

    protected ClassEnumBase(RepositoryDataInput input) throws IOException {
        super(input);
        this.isValid = input.readBoolean();

        this.name = PersistentUtils.readUTF(input, NameCache.getManager());
        assert this.name != null;

        this.qualifiedName = PersistentUtils.readUTF(input, QualifiedNameCache.getManager());
        assert this.qualifiedName != null;

        this.scopeUID = UIDObjectFactory.getDefaultFactory().readUID(input);
        this.scopeRef = null;

        this._static = input.readBoolean();

        this.visibility = PersistentUtils.readVisibility(input);
        assert this.visibility != null;

        // restore UID for unnamed classifier
        if (getName().length() == 0) {
            super.readUID(input);
        }
        int collSize = input.readInt();
        if (collSize < 0) {
            enclosingElements = Collections.synchronizedList(new ArrayList<CsmUID<CsmOffsetableDeclaration>>(0));
        } else {
            enclosingElements = Collections.synchronizedList(new ArrayList<CsmUID<CsmOffsetableDeclaration>>(collSize));
        }
        UIDObjectFactory.getDefaultFactory().readUIDCollection(enclosingElements, input, collSize);
    }

    @Override
    public Collection<CsmTypedef> getEnclosingTypedefs() {
        Collection<CsmTypedef> out = new ArrayList<>(0);
        synchronized(enclosingElements) {
            for (CsmUID<? extends CsmDeclaration> uid : enclosingElements) {
                CsmDeclaration.Kind kind = UIDUtilities.getKind(uid);
                if (kind == CsmDeclaration.Kind.TYPEDEF || kind == CsmDeclaration.Kind.TYPEALIAS) {
                    CsmDeclaration obj = UIDCsmConverter.UIDtoCsmObject(uid);
                    if (obj != null) {
                        out.add((CsmTypedef) obj);
                    }
                }
            }
        }
        return out;
    }

    @Override
    public Collection<CsmVariable> getEnclosingVariables() {
        Collection<CsmVariable> out = new ArrayList<>(0);
        synchronized(enclosingElements) {
            for (CsmUID<? extends CsmDeclaration> uid : enclosingElements) {
                CsmDeclaration.Kind kind = UIDUtilities.getKind(uid);
                if (kind == CsmDeclaration.Kind.VARIABLE) {
                    CsmDeclaration obj = UIDCsmConverter.UIDtoCsmObject(uid);
                    if (obj != null) {
                        out.add((CsmVariable) obj);
                    }
                }
            }
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    public void addEnclosingTypedef(CsmTypedef typedef) {
        final CsmUID<? extends CsmOffsetableDeclaration> uid = UIDs.get(typedef);
        enclosingElements.add((CsmUID<CsmOffsetableDeclaration>)uid);
    }

    @SuppressWarnings("unchecked")
    public void addEnclosingVariable(CsmVariable var) {
        final CsmUID<? extends CsmOffsetableDeclaration> uid = UIDs.get(var);
        enclosingElements.add((CsmUID<CsmOffsetableDeclaration>)uid);
    }
}
