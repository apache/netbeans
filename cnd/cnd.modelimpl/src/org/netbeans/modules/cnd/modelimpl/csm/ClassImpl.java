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

import org.netbeans.modules.cnd.modelimpl.content.file.FileContent;
import org.netbeans.modules.cnd.modelimpl.parser.CsmAST;
import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.antlr.collections.AST;
import java.io.IOException;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.apt.support.lang.APTLanguageSupport;
import org.netbeans.modules.cnd.modelimpl.csm.InheritanceImpl.InheritanceBuilder;
import org.netbeans.modules.cnd.modelimpl.parser.generated.CPPTokenTypes;
import org.netbeans.modules.cnd.modelimpl.csm.core.*;
import org.netbeans.modules.cnd.modelimpl.repository.PersistentUtils;
import org.netbeans.modules.cnd.modelimpl.repository.RepositoryUtils;
import org.netbeans.modules.cnd.modelimpl.uid.UIDCsmConverter;
import org.netbeans.modules.cnd.modelimpl.uid.UIDObjectFactory;
import org.openide.util.CharSequences;
import org.netbeans.modules.cnd.modelimpl.debug.DiagnosticExceptoins;
import org.netbeans.modules.cnd.modelimpl.impl.services.SelectImpl;
import org.netbeans.modules.cnd.modelimpl.parser.spi.CsmParserProvider;
import org.netbeans.modules.cnd.modelimpl.uid.UIDUtilities;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataInput;
import org.netbeans.modules.cnd.repository.spi.RepositoryDataOutput;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.cache.CharSequenceUtils;

/**
 * Implements CsmClass
 */
public class ClassImpl extends ClassEnumBase<CsmClass> implements CsmClass, CsmTemplate, SelectImpl.FilterableMembers,
        DeclarationsContainer {

    private final CsmDeclaration.Kind kind;
    private final List<CsmUID<CsmMember>> members;
    private final List<CsmUID<CsmFriend>> friends;
    private final ArrayList<CsmUID<CsmInheritance>> inheritances;
    private TemplateDescriptor templateDescriptor = null;
    private /*final*/ int leftBracketPos;

//    public ClassImpl(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file) {
//        this(kind, name, namespace, file, null);
//    }
//
//    public ClassImpl(CsmDeclaration.Kind kind, String name, NamespaceImpl namespace, CsmFile file, CsmClass containingClass) {
//        super(name, namespace, file, containingClass, null);
//        leftBracketPos = 0;
//        this.kind = CsmDeclaration.Kind.CLASS;
//        register();
//    }
    protected ClassImpl(NameHolder name, AST ast, CsmFile file) {
        // we call findId(..., true) because there might be qualified name - in the case of nested class template specializations
        this(name, ast, file, getStartOffset(ast), getEndOffset(ast));
    }

    protected ClassImpl(NameHolder name, AST ast, CsmFile file, int start, int end) {
        // we call findId(..., true) because there might be qualified name - in the case of nested class template specializations
        super(name, file, start, end);
        members = new ArrayList<>();
        friends = new ArrayList<>(0);
        inheritances = new ArrayList<>(0);
        kind = findKind(ast);
    }

    protected ClassImpl(NameHolder name, CsmDeclaration.Kind kind, int leftBracketPos, CsmFile file, int startOffset, int endOffset) {
        super(name, file, startOffset, endOffset);
        members = new ArrayList<>();
        friends = new ArrayList<>(0);
        inheritances = new ArrayList<>(0);
        this.kind = kind;
        this.leftBracketPos = leftBracketPos;
    }

    private ClassImpl(CsmFile file, CsmScope scope, CharSequence name, CsmDeclaration.Kind kind, int startOffset, int endOffset) {
        super(name, name, file, startOffset, endOffset);
        members = new ArrayList<>();
        friends = new ArrayList<>(0);
        inheritances = new ArrayList<>(0);
        this.kind = kind;
        initScope(scope);
    }

    public static ClassImpl create(CsmFile file, CsmScope scope, CharSequence name, CsmDeclaration.Kind kind, int startOffset, int endOffset, boolean register) {
        ClassImpl classImpl = new ClassImpl(file, scope, name, kind, startOffset, endOffset);
        temporaryRepositoryRegistration(register, classImpl);
        if (register) {
            classImpl.register(classImpl.getScope(), false);
        }
        return classImpl;
    }

    public void init(CsmScope scope, AST ast, CsmFile file, FileContent fileContent, String language, String languageFlavor, boolean register, DeclarationsContainer container) throws AstRendererException {
        initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initClassDefinition(scope);
        render(ast, file, fileContent, language, languageFlavor, !register, container);
        if (register) {
            register(getScope(), false);
        }
    }
//
//    public void init2(CsmScope scope, AST ast, boolean register) {
//        initScope(scope);
////        temporaryRepositoryRegistration(register, this);
//        initClassDefinition(scope);
//        render(ast, !register);
//        if (register) {
//            register(getScope(), false);
//        }
//    }

    public void init3(CsmScope scope, boolean register) {
        initScope(scope);
        temporaryRepositoryRegistration(register, this);
        initClassDefinition(scope);
        if (register) {
            register(getScope(), false);
        }
    }

    private void initClassDefinition(CsmScope scope) {
        ClassImpl.MemberForwardDeclaration mfd = findMemberForwardDeclaration(scope);
        if (mfd instanceof ClassImpl.ClassMemberForwardDeclaration && CsmKindUtilities.isClass(this)) {
            ClassImpl.ClassMemberForwardDeclaration fd = (ClassImpl.ClassMemberForwardDeclaration) mfd;
            fd.setCsmClass((CsmClass) this);
            CsmClass containingClass = fd.getContainingClass();
            if (containingClass != null) {
                // this is our real scope, not current namespace
                initScope(containingClass);
            }
        }
    }

    public final void render(AST ast, CsmFile file, FileContent fileContent, String language, String languageFlavor, boolean localClass, DeclarationsContainer container) {
        new ClassAstRenderer(file, language, languageFlavor, fileContent, CsmVisibility.PRIVATE, localClass, container).render(ast);
        leftBracketPos = initLeftBracketPos(ast);
    }

    public final void fixFakeRender(String language, String languageFlavor, FileContent fileContent, CsmVisibility visibility, AST ast, boolean localClass) {
        new ClassAstRenderer(fileContent.getFile(), language, languageFlavor, fileContent, visibility, localClass, null).render(ast);
    }

    protected static ClassImpl findExistingClassImplInContainer(DeclarationsContainer container, AST ast) {
        ClassImpl out = null;
        if (container != null) {
            CharSequence name = CharSequences.create(AstUtil.findId(ast, CPPTokenTypes.RCURLY, true));
            name = (name == null) ? CharSequences.empty() : name;
            int start = getStartOffset(ast);
            int end = getEndOffset(ast);
            CsmOffsetableDeclaration existing = container.findExistingDeclaration(start, end, name);
            if (existing instanceof ClassImpl) {
                out = (ClassImpl) existing;
    //                System.err.printf("found existing %s in %s\n", existing, container); // NOI18N
    //            } else {
    //                System.err.printf("not found %s [%d-%d] in %s\n", name, start, end, container); // NOI18N
            }
        }
        return out;
    }

    public static ClassImpl create(AST ast, CsmScope scope, CsmFile file, String language, String languageFlavor, FileContent fileContent, boolean register, DeclarationsContainer container) throws AstRendererException {
        ClassImpl impl = findExistingClassImplInContainer(container, ast);
        if (impl != null && !(ClassImpl.class.equals(impl.getClass()))) {
            // not our instance
            impl = null;
        }
        NameHolder nameHolder = null;
        if (impl == null) {
            nameHolder = NameHolder.createClassName(ast);
            impl = new ClassImpl(nameHolder, ast, file);
        }
        // fix for Bug 215225 - Infinite loop in TemplateUtils.checkTemplateType
        if(scope != null && scope instanceof ClassImpl) {
            ClassImpl scopeCls = (ClassImpl)scope;
            if(impl.getStartOffset() == scopeCls.getStartOffset() &&
                    impl.getEndOffset() == scopeCls.getEndOffset() &&
                    impl.getKind().equals(scopeCls.getKind()) &&
                    impl.getName().equals(scopeCls.getName())) {
                return null;
            }
        }
        impl.init(scope, ast, file, fileContent, language, languageFlavor, register, container);
        if (nameHolder != null) {
            nameHolder.addReference(fileContent, impl);
        }
        return impl;
    }

    protected void setTemplateDescriptor(TemplateDescriptor td) {
        templateDescriptor = td;
    }

    @Override
    public CsmDeclaration.Kind getKind() {
        return this.kind;
    }

    @Override
    public Collection<CsmMember> getMembers() {
        Collection<CsmMember> out;
        synchronized (members) {
            out = UIDCsmConverter.UIDsToDeclarations(members);
        }
        return out;
    }

    @Override
    public Iterator<CsmMember> getMembers(CsmFilter filter) {
        Collection<CsmUID<CsmMember>> uids = new ArrayList<>();
        synchronized (members) {
            uids.addAll(members);
        }
        return UIDCsmConverter.UIDsToDeclarations(uids, filter);
    }

    @Override
    public Collection<CsmFriend> getFriends() {
        Collection<CsmFriend> out;
        synchronized (friends) {
            out = UIDCsmConverter.UIDsToDeclarations(friends);
        }
        return out;
    }

    @Override
    public Collection<CsmInheritance> getBaseClasses() {
        Collection<CsmInheritance> out;
        synchronized (inheritances) {
            out = UIDCsmConverter.UIDsToInheritances(inheritances);
        }
        return out;
    }

    @Override
    public boolean isTemplate() {
        return templateDescriptor != null;
    }

    @Override
    public boolean isSpecialization() {
        return false;
    }

    @Override
    public boolean isExplicitSpecialization() {
        return false;
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, int end, CharSequence name) {
        CsmUID<? extends CsmOffsetableDeclaration> out = null;
        synchronized (members) {
            out = UIDUtilities.findExistingUIDInList(members, start, end, name);
//            if (FileImpl.traceFile(getContainingFile().getAbsolutePath())) {
//                System.err.printf("%s found %s [%d-%d] in \n\t%s\n", (out == null) ? "NOT " : "", name, start, end, members);
//            }
        }
        if (out == null) {
            // check friends
            synchronized (friends) {
                out = UIDUtilities.findExistingUIDInList(friends, start, end, name);
            }
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    @Override
    public CsmOffsetableDeclaration findExistingDeclaration(int start, CharSequence name, CsmDeclaration.Kind kind) {
        CsmUID<? extends CsmOffsetableDeclaration> out = null;
        if(kind != CsmDeclaration.Kind.CLASS_FRIEND_DECLARATION &&
            kind != CsmDeclaration.Kind.FUNCTION_FRIEND &&
            kind != CsmDeclaration.Kind.FUNCTION_FRIEND_DEFINITION) {
            synchronized (members) {
                out = UIDUtilities.findExistingUIDInList(members, start, name, kind);
            }
        } else {
            // check friends
            synchronized (friends) {
                out = UIDUtilities.findExistingUIDInList(friends, start, name, kind);
            }
        }
        return UIDCsmConverter.UIDtoDeclaration(out);
    }

    protected void addMember(CsmMember member, boolean global) {
        if (global) {
            RepositoryUtils.put(member);
        }
        CsmUID<CsmMember> uid = UIDCsmConverter.declarationToUID(member);
        assert uid != null;
        synchronized (members) {
//            members.add(uid);
            UIDUtilities.insertIntoSortedUIDList(uid, members);
        }
    }

    protected void addInheritance(CsmInheritance inheritance, boolean global) {
        if (global) {
            RepositoryUtils.put(inheritance);
        }
        CsmUID<CsmInheritance> uid = UIDCsmConverter.inheritanceToUID(inheritance);
        assert uid != null;
        synchronized (inheritances) {
            UIDUtilities.insertIntoSortedUIDList(uid, inheritances);
        }
    }

    private void addFriend(CsmFriend friend, boolean global) {
        if (global) {
            RepositoryUtils.put(friend);
        }
        CsmUID<CsmFriend> uid = UIDCsmConverter.declarationToUID(friend);
        assert uid != null;
        synchronized (friends) {
//            friends.add(uid);
            UIDUtilities.insertIntoSortedUIDList(uid, friends);
        }
    }

    private int initLeftBracketPos(AST node) {
        AST lcurly = AstUtil.findChildOfType(node, CPPTokenTypes.LCURLY);
        return (lcurly instanceof CsmAST) ? ((CsmAST) lcurly).getOffset() : getStartOffset();
    }

    @Override
    public int getLeftBracketOffset() {
        return leftBracketPos;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Collection<CsmScopeElement> getScopeElements() {
        return (Collection<CsmScopeElement>) (Collection<?>) getMembers();
    }

    @Override
    public void dispose() {
        super.dispose();
        _clearMembers();
        _clearFriends();
        _clearInheritances();
    }

    private void _clearMembers() {
        Collection<CsmMember> members2dispose = getMembers();
        Utils.disposeAll(members2dispose);
        synchronized (members) {
            RepositoryUtils.remove(this.members);
        }
    }

    private void _clearInheritances() {
        Collection<CsmInheritance> inheritances2dispose = getBaseClasses();
        Utils.disposeAll(inheritances2dispose);
        synchronized (inheritances) {
            RepositoryUtils.remove(this.inheritances);
        }
    }

    private void _clearFriends() {
        Collection<CsmFriend> friends2dispose = getFriends();
        Utils.disposeAll(friends2dispose);
        synchronized (friends) {
            RepositoryUtils.remove(this.friends);
        }
    }

    private CsmDeclaration.Kind findKind(AST ast) {
        for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
            switch (token.getType()) {
                // class / struct / union
                case CPPTokenTypes.LITERAL_class:
                    return CsmDeclaration.Kind.CLASS;
                case CPPTokenTypes.LITERAL_union:
                    return CsmDeclaration.Kind.UNION;
                case CPPTokenTypes.LITERAL_struct:
                    return CsmDeclaration.Kind.STRUCT;
            }
        }
        return CsmDeclaration.Kind.CLASS;
    }

    @Override
    public CharSequence getDisplayName() {
        return (templateDescriptor != null) ? CharSequences.create(CharSequenceUtils.concatenate(getName(), templateDescriptor.getTemplateSuffix())) : getName(); // NOI18N
    }

    @Override
    public List<CsmTemplateParameter> getTemplateParameters() {
        return (templateDescriptor != null) ? templateDescriptor.getTemplateParameters() : Collections.<CsmTemplateParameter>emptyList();
    }

    public static interface MemberBuilder {
        CsmMember create(CsmParserProvider.ParserErrorDelegate gelegate);
        void setScope(CsmScope scope);
        void setVisibility(CsmVisibility visibility);
    }

    public static class ClassBuilder extends SimpleDeclarationBuilder implements MemberBuilder {

        private CsmDeclaration.Kind kind = CsmDeclaration.Kind.CLASS;
        private List<MemberBuilder> memberBuilders = new ArrayList<>();
        private List<SimpleDeclarationBuilder> friendBuilders = new ArrayList<>();
        private List<InheritanceBuilder> inheritanceBuilders = new ArrayList<>();

        private ClassImpl instance;
        private CsmVisibility visibility = CsmVisibility.PUBLIC;
        private CsmVisibility currentMemberVisibility = CsmVisibility.PUBLIC;
        private int leftBracketPos;

        public ClassBuilder() {
        }

        protected ClassBuilder(ClassBuilder builder) {
            super(builder);
            kind = builder.kind;
            memberBuilders = builder.memberBuilders;
            inheritanceBuilders = builder.inheritanceBuilders;
        }

        @Override
        public void setVisibility(CsmVisibility visibility) {
            this.visibility = visibility;
        }

        public void setCurrentMemberVisibility(CsmVisibility visibility) {
            this.currentMemberVisibility = visibility;
        }

        public CsmVisibility getCurrentMemberVisibility() {
            return currentMemberVisibility;
        }

        public void setKind(Kind kind) {
            this.kind = kind;
            switch (kind) {
                case CLASS:
                    setCurrentMemberVisibility(CsmVisibility.PRIVATE);
                    break;
                case STRUCT: case UNION:
                    setCurrentMemberVisibility(CsmVisibility.PUBLIC);
                    break;
            }
        }

        public void setLeftBracketPos(int leftBracketPos) {
            this.leftBracketPos = leftBracketPos;
        }

        public Kind getKind() {
            return kind;
        }

        public void addMemberBuilder(MemberBuilder builder) {
            this.memberBuilders.add(builder);
        }

        public void addFriendBuilder(SimpleDeclarationBuilder builder) {
            this.friendBuilders.add(builder);
        }

        public List<MemberBuilder> getMemberBuilders() {
            return memberBuilders;
        }

        public List<SimpleDeclarationBuilder> getFriendBuilders() {
            return friendBuilders;
        }

        public void addInheritanceBuilder(InheritanceBuilder i) {
            this.inheritanceBuilders.add(i);
        }

        public List<InheritanceBuilder> getInheritanceBuilders() {
            return inheritanceBuilders;
        }

        private ClassImpl getClassDefinitionInstance() {
            if(instance != null) {
                return instance;
            }
            MutableDeclarationsContainer container = null;
            if (getParent() == null) {
                container = getFileContent();
            } else {
                if(getParent() instanceof NamespaceDefinitionImpl.NamespaceBuilder) {
                    container = ((NamespaceDefinitionImpl.NamespaceBuilder)getParent()).getNamespaceDefinitionInstance();
                }
            }
            if(container != null && getName() != null) {
                CsmOffsetableDeclaration decl = container.findExistingDeclaration(getStartOffset(), getName(), getKind());
                if (decl != null && ClassImpl.class.equals(decl.getClass())) {
                    instance = (ClassImpl) decl;
                }
            }
            return instance;
        }

        @Override
        public ClassImpl create(CsmParserProvider.ParserErrorDelegate delegate) {
            ClassImpl cls = getClassDefinitionInstance();
            CsmScope s = getScope();
            if (cls == null && s != null && getName() != null && getEndOffset() != 0) {
                instance = cls = new ClassImpl(getNameHolder(), getKind(), leftBracketPos, getFile(), getStartOffset(), getEndOffset());
                cls.setVisibility(visibility);
                cls.init3(s, isGlobal());
                if(getTemplateDescriptorBuilder() != null) {
                    cls.setTemplateDescriptor(getTemplateDescriptor());
                }
                for (InheritanceBuilder inheritanceBuilder : getInheritanceBuilders()) {
                    inheritanceBuilder.setScope(cls);
                    InheritanceImpl inst = inheritanceBuilder.create();
                    cls.addInheritance(inst, isGlobal());
                }
                for (MemberBuilder builder : getMemberBuilders()) {
                    builder.setScope(cls);
                    CsmMember inst = builder.create(delegate);
                    if(inst != null) {
                        cls.addMember(inst, isGlobal());
                    } else {
                        CsmParserProvider.registerParserError(delegate, "Skip unrecognized member for builder '"+builder, getFile(), getStartOffsetImpl(builder, this)); //NOI18N
                    }
                }
                for (SimpleDeclarationBuilder builder : getFriendBuilders()) {
                    builder.setScope(cls);
                    CsmFriend inst = (CsmFriend)builder.create();
                    if(inst != null) {
                        cls.addFriend(inst, isGlobal());
                    }
                }
                getNameHolder().addReference(getFileContent(), cls);
                addDeclaration(cls);
            }
            return cls;
        }

        @Override
        public String toString() {
            return "ClassBuilder{" + "kind=" + kind + ", memberBuilders=" + memberBuilders + //NOI18N
                    ", friendBuilders=" + friendBuilders + ", inheritanceBuilders=" + inheritanceBuilders + //NOI18N
                    ", instance=" + instance + super.toString() + '}'; //NOI18N
        }

        public static int getStartOffsetImpl(MemberBuilder child, OffsetableBuilder parent) {
            int startOffset = -1;
            if (child instanceof OffsetableBuilder) {
                startOffset = ((OffsetableBuilder)child).getStartOffset();
            }
            if (startOffset < 0) {
                startOffset = parent.getStartOffset();
            }
            return startOffset;
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    // impl of SelfPersistent
    @Override
    public void write(RepositoryDataOutput output) throws IOException {
        super.write(output);
        assert this.kind != null;
        writeKind(this.kind, output);
        PersistentUtils.writeTemplateDescriptor(templateDescriptor, output);
        output.writeInt(this.leftBracketPos);
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        factory.writeUIDCollection(this.members, output, true);
        factory.writeUIDCollection(this.friends, output, true);
        factory.writeUIDCollection(this.inheritances, output, true);
    }

    public ClassImpl(RepositoryDataInput input) throws IOException {
        super(input);
        this.kind = readKind(input);
        this.templateDescriptor = PersistentUtils.readTemplateDescriptor(input);
        this.leftBracketPos = input.readInt();
        UIDObjectFactory factory = UIDObjectFactory.getDefaultFactory();
        int collSize = input.readInt();
        if (collSize <= 0) {
            members = new ArrayList<>(0);
        } else {
            members = new ArrayList<>(collSize);
        }
        factory.readUIDCollection(this.members, input, collSize);
        collSize = input.readInt();
        if (collSize <= 0) {
            friends = new ArrayList<>(0);
        } else {
            friends = new ArrayList<>(collSize);
        }
        factory.readUIDCollection(this.friends, input, collSize);

        collSize = input.readInt();
        if (collSize <= 0) {
            inheritances = new ArrayList<>(0);
        } else {
            inheritances = new ArrayList<>(collSize);
        }
        factory.readUIDCollection(this.inheritances, input, collSize);
    }
    private static final int CLASS_KIND = 1;
    private static final int UNION_KIND = 2;
    private static final int STRUCT_KIND = 3;

    private static void writeKind(CsmDeclaration.Kind kind, RepositoryDataOutput output) throws IOException {
        int kindHandler;
        if (kind == CsmDeclaration.Kind.CLASS) {
            kindHandler = CLASS_KIND;
        } else if (kind == CsmDeclaration.Kind.UNION) {
            kindHandler = UNION_KIND;
        } else {
            assert kind == CsmDeclaration.Kind.STRUCT;
            kindHandler = STRUCT_KIND;
        }
        output.writeByte(kindHandler);
    }

    private static CsmDeclaration.Kind readKind(RepositoryDataInput input) throws IOException {
        int kindHandler = input.readByte();
        CsmDeclaration.Kind kind;
        switch (kindHandler) {
            case CLASS_KIND:
                kind = CsmDeclaration.Kind.CLASS;
                break;
            case UNION_KIND:
                kind = CsmDeclaration.Kind.UNION;
                break;
            case STRUCT_KIND:
                kind = CsmDeclaration.Kind.STRUCT;
                break;
            default:
                throw new IllegalArgumentException("illegal handler " + kindHandler); // NOI18N
        }
        return kind;
    }

    private class ClassAstRenderer extends AstRenderer {
        private final DeclarationsContainer container;
        private final boolean renderingLocalContext;
        private CsmVisibility curentVisibility;

        public ClassAstRenderer(CsmFile containingFile, String language, String languageFlavor, FileContent fileContent, CsmVisibility curentVisibility, boolean renderingLocalContext, DeclarationsContainer parentContainer) {
            super((FileImpl) containingFile, fileContent, language, languageFlavor, null);
            this.renderingLocalContext = renderingLocalContext;
            this.curentVisibility = curentVisibility;
            this.container = parentContainer;
        }

        @Override
        protected boolean isRenderingLocalContext() {
            return renderingLocalContext;
        }

        @Override
        protected VariableImpl<CsmField> createVariable(AST offsetAst, AST templateAst, CsmFile file, CsmType type, NameHolder name, boolean _static, boolean _extern,
                MutableDeclarationsContainer container1, MutableDeclarationsContainer container2, CsmScope scope) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            FieldImpl field = FieldImpl.create(
                    offsetAst, 
                    file, 
                    fileContent, 
                    type, 
                    templateAst, 
                    name, 
                    ClassImpl.this, 
                    curentVisibility, 
                    _static, 
                    _extern, 
                    !isRenderingLocalContext()
            );
            ClassImpl.this.addMember(field,!isRenderingLocalContext());
            return field;
        }

        @Override
        public void render(AST ast) {
            Pair typedefs;
            AST child;
            for (AST token = ast.getFirstChild(); token != null; token = token.getNextSibling()) {
                try {
                    switch (token.getType()) {
                        //case CPPTokenTypes.CSM_TEMPLATE_PARMLIST:
                        case CPPTokenTypes.LITERAL_template:{
                            List<CsmTemplateParameter> params = TemplateUtils.getTemplateParameters(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext());
                            final CharSequence classSpecializationSuffix = TemplateUtils.getClassSpecializationSuffix(token, null);
                            CharSequence name = CharSequenceUtils.concatenate("<", classSpecializationSuffix, ">"); // NOI18N
                            setTemplateDescriptor(params, name, classSpecializationSuffix.length() > 0);
                            break;
                        }
                        case CPPTokenTypes.CSM_BASE_SPECIFIER:
                            addInheritance(InheritanceImpl.create(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext()), !isRenderingLocalContext());
                            break;
                        // class / struct / union
                        case CPPTokenTypes.LITERAL_class:
                            break;
                        case CPPTokenTypes.LITERAL_union:
                            curentVisibility = CsmVisibility.PUBLIC;
                            break;
                        case CPPTokenTypes.LITERAL_struct:
                            curentVisibility = CsmVisibility.PUBLIC;
                            break;

                        // visibility
                        case CPPTokenTypes.LITERAL_public:
                            curentVisibility = CsmVisibility.PUBLIC;
                            break;
                        case CPPTokenTypes.LITERAL_private:
                            curentVisibility = CsmVisibility.PRIVATE;
                            break;
                        case CPPTokenTypes.LITERAL_protected:
                            curentVisibility = CsmVisibility.PROTECTED;
                            break;

                        // inner classes and enums
                        case CPPTokenTypes.CSM_CLASS_DECLARATION:
                        case CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION: {
                            CsmScope currentScope = ClassImpl.this;
                            DeclarationsContainer currentContainer = ClassImpl.this;
                            if (APTLanguageSupport.getInstance().isLanguageC(language)) {
                                if (!isRenderingLocalContext()) {
                                    if (!isUnnamedStructOrUnion(token)) {
                                        currentScope = getContainingFile().getProject().getGlobalNamespace();
                                        currentContainer = getFileContent();
                                    }
                                } else {
                                    currentScope = null;
                                    currentContainer = container; // Used to render local declarations
                                }
                            }
                            ClassImpl innerClass = createClass(token, currentScope, currentContainer);
                            processClassEnum(innerClass, token);
                            break;
                        }

                        case CPPTokenTypes.CSM_ENUM_DECLARATION:
                            EnumImpl innerEnum = createEnum(token, ClassImpl.this, ClassImpl.this);
                            processClassEnum(innerEnum, token);
                            checkInnerIncludes(innerEnum, Collections.<CsmObject>emptyList());
                            break;

                        case CPPTokenTypes.CSM_ENUM_FWD_DECLARATION:
                        {
                            EnumMemberForwardDeclaration fd = renderEnumForwardDeclaration(token);
                            if (fd != null) {
                                addMember(fd, !isRenderingLocalContext());
                                fd.init(token, ClassImpl.this, !isRenderingLocalContext());
                                break;
                            }
                            break;
                        }
                        // other members
                        case CPPTokenTypes.CSM_CTOR_DEFINITION:
                        case CPPTokenTypes.CSM_CTOR_TEMPLATE_DEFINITION:
                            addMember(ConstructorDDImpl.createConstructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()), !isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_CTOR_DECLARATION:
                        case CPPTokenTypes.CSM_CTOR_TEMPLATE_DECLARATION:
                            addMember(ConstructorImpl.createConstructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_DTOR_DEFINITION:
                        case CPPTokenTypes.CSM_DTOR_TEMPLATE_DEFINITION:
                            addMember(DestructorDDImpl.createDestructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_DTOR_DECLARATION:
                            addMember(DestructorImpl.createDestructor(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.CSM_FIELD:
                            child = token.getFirstChild();
                            if (hasFriendPrefix(child)) {
                                addFriend(renderFriendClass(token), !isRenderingLocalContext());
                            } else {
                                if (renderVariable(token, null, null, ClassImpl.this.getContainingNamespaceImpl(), false)) {
                                    break;
                                }
                                typedefs = renderTypedef(token, (FileImpl) getContainingFile(), fileContent, ClassImpl.this, null);
                                if (!typedefs.getTypesefs().isEmpty()) {
                                    for (CsmTypedef typedef : typedefs.getTypesefs()) {
                                        // It could be important to register in project before add as member...
                                        if (!isRenderingLocalContext()) {
                                            ((FileImpl) getContainingFile()).getProjectImpl(true).registerDeclaration(typedef);
                                        }
                                        addMember((CsmMember) typedef,!isRenderingLocalContext());
                                        if (typedefs.getEnclosingClassifier() != null) {
                                            typedefs.getEnclosingClassifier().addEnclosingTypedef(typedef);
                                        }
                                    }
                                    if (typedefs.getEnclosingClassifier() != null && !ForwardClass.isForwardClass(typedefs.getEnclosingClassifier())) {
                                        addMember(typedefs.getEnclosingClassifier(), !isRenderingLocalContext());
                                    }
                                    break;
                                }
                                if (renderBitField(token)) {
                                    break;
                                }
                                ClassMemberForwardDeclaration fd = renderClassForwardDeclaration(token);
                                if (fd != null) {
                                    addMember(fd,!isRenderingLocalContext());
                                    fd.init(token, ClassImpl.this, !isRenderingLocalContext());
                                    break;
                                }
                            }
                            break;
                        case CPPTokenTypes.CSM_USING_DECLARATION: {
                            UsingDeclarationImpl using = UsingDeclarationImpl.create(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext(), curentVisibility);
                            addMember(using, !isRenderingLocalContext());
                            break;
                        }
                        case CPPTokenTypes.CSM_TEMPL_FWD_CL_OR_STAT_MEM:
                            {
                                child = token.getFirstChild();
                                if (hasFriendPrefix(child)) {
                                    addFriend(renderFriendClass(token), !isRenderingLocalContext());
                                } else {
                                    ClassMemberForwardDeclaration fd = renderClassForwardDeclaration(token);
                                    if (fd != null) {
                                        addMember(fd, !isRenderingLocalContext());
                                        fd.init(token, ClassImpl.this, !isRenderingLocalContext());
                                        break;
                                    } else {
                                        // C++14 template variables. Check is downgraded to the most generic one
                                        // because we want to accept this code even in C++98 standard.
                                        if (APTLanguageSupport.getInstance().isLanguageCpp(language)) {
                                            renderVariable(token, null, null, ClassImpl.this, false);
                                            break;
                                        }
                                    }
                                }
                            }
                            break;
                        case CPPTokenTypes.CSM_FUNCTION_DECLARATION:
                        case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DECLARATION:
                        case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DECLARATION:
                        case CPPTokenTypes.CSM_USER_TYPE_CAST_DECLARATION:
                        case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DECLARATION:
                            child = token.getFirstChild();
                            if (child != null) {
                                if (hasFriendPrefix(child)) {
                                    CsmScope scope = getFriendScope();
                                    CsmFriendFunction friend;
                                    CsmFunction func;
                                    if (isMemberDefinition(token)) {
                                        FriendFunctionImplEx impl = FriendFunctionImplEx.create(token, getContainingFile(), fileContent, ClassImpl.this, scope, !isRenderingLocalContext());
                                        func = impl;
                                        friend = impl;
                                    } else {
                                        FriendFunctionImpl impl = FriendFunctionImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, scope, !isRenderingLocalContext());
                                        friend = impl;
                                        func = impl;
                                        if(!isRenderingLocalContext()) {
                                            if (scope instanceof NamespaceImpl) {
                                                ((NamespaceImpl) scope).addDeclaration(func);
                                            } else {
                                                ((NamespaceImpl) getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                            }
                                        }
                                    }
                                    //((FileImpl)getContainingFile()).addDeclaration(func);
                                    addFriend(friend,!isRenderingLocalContext());
                                } else {
                                    addMember(MethodImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()), !isRenderingLocalContext());
                                }
                            }
                            break;
                        case CPPTokenTypes.CSM_FUNCTION_DEFINITION:
                        case CPPTokenTypes.CSM_FUNCTION_RET_FUN_DEFINITION:
                        case CPPTokenTypes.CSM_FUNCTION_TEMPLATE_DEFINITION:
                        case CPPTokenTypes.CSM_USER_TYPE_CAST_DEFINITION:
                        case CPPTokenTypes.CSM_USER_TYPE_CAST_TEMPLATE_DEFINITION:
                            child = token.getFirstChild();
                            if (hasFriendPrefix(child)) {
                                CsmScope scope = getFriendScope();
                                CsmFriendFunction friend;
                                CsmFunction func;
                                if (isMemberDefinition(token)) {
                                    FriendFunctionDefinitionImpl impl = FriendFunctionDefinitionImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, null, !isRenderingLocalContext());
                                    func = impl;
                                    friend = impl;
                                } else {
                                    FriendFunctionDDImpl impl = FriendFunctionDDImpl.create(token, getContainingFile(), fileContent, ClassImpl.this, scope, !isRenderingLocalContext());
                                    friend = impl;
                                    func = impl;
                                    if (!isRenderingLocalContext()) {
                                        if (scope instanceof NamespaceImpl) {
                                            ((NamespaceImpl) scope).addDeclaration(func);
                                        } else {
                                            ((NamespaceImpl) getContainingFile().getProject().getGlobalNamespace()).addDeclaration(func);
                                        }
                                    }
                                }
                                //((FileImpl)getContainingFile()).addDeclaration(func);
                                addFriend(friend,!isRenderingLocalContext());
                            } else {
                                addMember( MethodDDImpl.<CsmMethod>create(token, getContainingFile(), fileContent, ClassImpl.this, curentVisibility, !isRenderingLocalContext()),!isRenderingLocalContext());
                            }
                            break;
                        case CPPTokenTypes.CSM_VISIBILITY_REDEF:
                            UsingDeclarationImpl using = UsingDeclarationImpl.create(token, getContainingFile(), ClassImpl.this, !isRenderingLocalContext(), curentVisibility);
                            addMember(using, !isRenderingLocalContext());
                            break;
                        case CPPTokenTypes.RCURLY:
                            break;
                        case CPPTokenTypes.CSM_VARIABLE_DECLARATION:
                        case CPPTokenTypes.CSM_ARRAY_DECLARATION:
                            //new VariableImpl(
                            break;
                    }
                } catch (AstRendererException e) {
                    DiagnosticExceptoins.register(e);
                }
            }
            checkInnerIncludes(ClassImpl.this, ClassImpl.this.getMembers());
        }

        private void processClassEnum(ClassEnumBase innerClassEnum, AST innerClassAST) {
            Pair typedefs;
            if(innerClassEnum != null) {
                typedefs = renderTypedef(innerClassAST, innerClassEnum, ClassImpl.this);
                if (!typedefs.getTypesefs().isEmpty()) {
                    for (CsmTypedef typedef : typedefs.getTypesefs()) {
                        // It could be important to register in project before add as member...
                        if (!isRenderingLocalContext()) {
                            ((FileImpl) getContainingFile()).getProjectImpl(true).registerDeclaration(typedef);
                        }
                        addMember((CsmMember) typedef,!isRenderingLocalContext());
                        if (typedefs.getEnclosingClassifier() != null){
                            typedefs.getEnclosingClassifier().addEnclosingTypedef(typedef);
                        }
                    }
                    if (typedefs.getEnclosingClassifier() != null && !ForwardClass.isForwardClass(typedefs.getEnclosingClassifier())) {
                        addMember(typedefs.getEnclosingClassifier(), !isRenderingLocalContext());
                    }
                }
                renderVariableInClassifier(innerClassAST, innerClassEnum, null, null);
            }
        }

        private CsmScope getFriendScope() {
            CsmScope scope = ClassImpl.this.getScope();
            while (CsmKindUtilities.isClass(scope)) {
               CsmScope newScope = ((CsmClass)scope).getScope();
               if (newScope != null) {
                   scope = newScope;
               } else {
                   break;
               }
            }
            return scope;
        }

        @Override
        protected EnumImpl createEnum(AST token, CsmScope scope, DeclarationsContainer container) {
            EnumImpl innerEnum = EnumImpl.create(token, scope, getContainingFile(), fileContent, !isRenderingLocalContext());
            innerEnum.setVisibility(curentVisibility);
            addMember(innerEnum,!isRenderingLocalContext());
            return innerEnum;
        }

        @Override
        protected ClassImpl createClass(AST token, CsmScope innerScope, DeclarationsContainer container) throws AstRendererException {
            ClassImpl innerClass = TemplateUtils.isPartialClassSpecialization(token)
                    ? ClassImplSpecialization.create(token, innerScope, getContainingFile(), language, languageFlavor, getFileContent(), !isRenderingLocalContext(), container)
                    : ClassImpl.create(token, innerScope, getContainingFile(), language, languageFlavor, getFileContent(), !isRenderingLocalContext(), container);
            if (innerClass != null) {
                innerClass.setVisibility(curentVisibility);
                if (innerScope == ClassImpl.this) {
                    addMember(innerClass, !isRenderingLocalContext());
                } else {
                    // In C language named strutures are members of global namespace
                    assert APTLanguageSupport.getInstance().isLanguageC(language);
                    if (container instanceof MutableDeclarationsContainer) {
                        ((MutableDeclarationsContainer) container).addDeclaration(innerClass);
                    }
                }
            }
            return innerClass;
        }

        private void setTemplateDescriptor(List<CsmTemplateParameter> params, CharSequence name, boolean specialization) {
            templateDescriptor = new TemplateDescriptor(params, name, specialization, !isRenderingLocalContext());
        }
        
        private boolean isUnnamedStructOrUnion(AST cls) {
            if (cls.getType() == CPPTokenTypes.CSM_CLASS_DECLARATION 
                    || cls.getType() == CPPTokenTypes.CSM_TEMPLATE_CLASS_DECLARATION) {
                // Look for qualified id. Name should appear as CSM_QUALIFIED_ID in AST
                if (AstUtil.findChildOfType(cls, CPPTokenTypes.CSM_QUALIFIED_ID) != null) {
                    return false;
                }
                // Look for variable declaration. Like in the following construction:
                // struct AAA {
                //   struct {
                //     ...
                //   } field; <- this is variable declaration
                // };
//                if (AstUtil.findChildOfType(cls, CPPTokenTypes.CSM_VARIABLE_DECLARATION) != null) {
//                    return false;
//                }
            }
            return true;
        }

        private boolean hasFriendPrefix(AST child) {
            if (child == null) {
                return false;
            }
            if (child.getType() == CPPTokenTypes.LITERAL_friend) {
                return true;
            } else if (child.getType() == CPPTokenTypes.LITERAL_template) {
                final AST nextSibling = child.getNextSibling();
                if (nextSibling != null && nextSibling.getType() == CPPTokenTypes.LITERAL_friend) {
                    // friend template declaration
                    return true;
                }
            }
            return false;
        }

        private CsmFriend renderFriendClass(AST ast) throws AstRendererException {
            AST firstChild = ast.getFirstChild();
            AST child = firstChild;
            if (child.getType() == CPPTokenTypes.LITERAL_friend) {
                child = child.getNextSibling();
            }
            if (child != null && child.getType() == CPPTokenTypes.LITERAL_typename) {
                child = child.getNextSibling();
            }
            if (child != null && child.getType() == CPPTokenTypes.LITERAL_template) {
                child = child.getNextSibling();
            }
            CsmClassForwardDeclaration cfd = null;
            AST qid = null;
            if (child != null &&
                    (child.getType() == CPPTokenTypes.LITERAL_struct ||
                    child.getType() == CPPTokenTypes.LITERAL_class)) {
                // check if we want to have class forward
                // we don't want for AA::BB names
                qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
                CharSequence[] nameParts = AstRenderer.getNameTokens(qid);
                if (nameParts != null && nameParts.length == 1) {
                    // also we don't want for templates references
                    AST templStart = TemplateUtils.getTemplateStart(ast.getFirstChild());
                    if (templStart == null) {
                        CsmScope scope = ClassImpl.this.getScope();
                        while (!CsmKindUtilities.isNamespace(scope) && CsmKindUtilities.isScopeElement(scope)) {
                            scope = ((CsmScopeElement)scope).getScope();
                        }
                        if (!CsmKindUtilities.isNamespace(scope)) {
                            scope = getContainingFile().getProject().getGlobalNamespace();
                        }
                        cfd = super.createForwardClassDeclaration(ast, null, (FileImpl) getContainingFile(), scope);
                        if (!isRenderingLocalContext()) {
                            if (true) { // always put in repository, because it's an element of global NS
                                RepositoryUtils.put(cfd);
                            }
                            ((NamespaceImpl) scope).addDeclaration(cfd);
                        }
                    }
                }
            } else if (child != null && (child.getType() == CPPTokenTypes.CSM_TYPE_COMPOUND)) {
                //class G;
                //class X {
                //  friend G;
                //};
                qid = child;
            } else {
                // FIXME: is it valid or exceptional branch?
                qid = AstUtil.findChildOfType(ast, CPPTokenTypes.CSM_QUALIFIED_ID);
            }
            return FriendClassImpl.create(firstChild, qid, cfd, (FileImpl) getContainingFile(), ClassImpl.this, !isRenderingLocalContext());
        }

        private ClassMemberForwardDeclaration renderClassForwardDeclaration(AST token) {
            AST typeAST = token.getFirstChild();
            if (typeAST == null) {
                return null;
            }
            if (typeAST.getType() == CPPTokenTypes.LITERAL_template) {
                typeAST = typeAST.getNextSibling();
            }
            if (typeAST == null ||
                    (typeAST.getType() != CPPTokenTypes.LITERAL_struct &&
                    typeAST.getType() != CPPTokenTypes.LITERAL_class)) {
                return null;
            }
            AST idAST = typeAST.getNextSibling();
            if (idAST == null || idAST.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) {
                return null;
            }
            return ClassMemberForwardDeclaration.create(getContainingFile(), ClassImpl.this, token, curentVisibility, !isRenderingLocalContext());
        }

        private EnumMemberForwardDeclaration renderEnumForwardDeclaration(AST token) {
            AST typeAST = token.getFirstChild();
            if (typeAST == null) {
                return null;
            }
            if (typeAST.getType() == CPPTokenTypes.LITERAL_template) {
                typeAST = typeAST.getNextSibling();
            }
            if (typeAST == null || (typeAST.getType() != CPPTokenTypes.LITERAL_enum)) {
                return null;
            }
            AST idAST = typeAST.getNextSibling();
            if (idAST != null &&
                    (idAST.getType() == CPPTokenTypes.LITERAL_struct ||
                     idAST.getType() == CPPTokenTypes.LITERAL_class)) {
                idAST = idAST.getNextSibling();
            }
            if (idAST == null || (idAST.getType() != CPPTokenTypes.CSM_QUALIFIED_ID &&
                                  idAST.getType() != CPPTokenTypes.IDENT)) {
                return null;
            }
            if (!EnumMemberForwardDeclaration.isCorrectOpaqueEnumDeclaration(token)) {
                return null;
            }
            return EnumMemberForwardDeclaration.create(getContainingFile(), ClassImpl.this, token, curentVisibility, !isRenderingLocalContext());
        }

        private boolean renderBitField(AST token) {

            AST typeAST = token.getFirstChild();
            if (typeAST == null) {
                return false;
            }
            typeAST = getFirstSiblingSkipQualifiers(typeAST);
            if (typeAST == null) {
                return false;
            }
            if (typeAST.getType() != CPPTokenTypes.CSM_TYPE_BUILTIN) {
                if (typeAST.getType() == CPPTokenTypes.LITERAL_enum) {
                    typeAST = typeAST.getNextSibling();
                }
                if (typeAST == null || (typeAST.getType() != CPPTokenTypes.CSM_QUALIFIED_ID) && (typeAST.getType() != CPPTokenTypes.CSM_TYPE_COMPOUND)) {
                    return false;
                }
            }

            // common type for all bit fields
            CsmType type = TypeFactory.createType(typeAST, getContainingFile(), null, 0);
            typeAST = getFirstSiblingSkipQualifiers(typeAST.getNextSibling());
            if (typeAST == null) {
                return false;
            }
            boolean bitFieldAdded = renderBitFieldImpl(token, typeAST, type, null);
            return bitFieldAdded;
        }

        @Override
        protected boolean renderBitFieldImpl(AST startOffsetAST, AST idAST, CsmType type, ClassEnumBase<?> classifier) {
            boolean cont = true;
            boolean added = false;
            AST start = startOffsetAST;
            AST prev = idAST;
            while (cont) {
                boolean unnamed = false;
                AST colonAST;
                if (idAST == null) {
                    break;
                } else if (idAST.getType() == CPPTokenTypes.IDENT) {
                    colonAST = idAST.getNextSibling();
                } else if (idAST.getType() == CPPTokenTypes.COLON){
                    colonAST = idAST;
                    unnamed = true;
                } else {
                    break;
                }

                if (colonAST == null || colonAST.getType() != CPPTokenTypes.COLON) {
                    break;
                }

                AST expAST = colonAST.getNextSibling();
                if (expAST == null || expAST.getType() != CPPTokenTypes.CSM_EXPRESSION) {
                    break;
                }
                prev = expAST.getNextSibling();

                // there could be next bit fields as well
                if (prev != null && prev.getType() == CPPTokenTypes.COMMA) {
                    // bit fields separated by comma
                    // byte f:1, g:2, h:5;
                    start = idAST;
                } else {
                    cont = false;
                    if (added) {
                        start = idAST;
                    }
                }
                if(!unnamed) {
                    NameHolder nameHolder = NameHolder.createSimpleName(idAST);
                    FieldImpl field = FieldImpl.create(start, getContainingFile(), fileContent, type, null, nameHolder, ClassImpl.this, curentVisibility, !isRenderingLocalContext());
                    ClassImpl.this.addMember(field,!isRenderingLocalContext());
                    if (classifier != null) {
                        classifier.addEnclosingVariable(field);
                    }
                }
                added = true;
                if (cont) {
                    idAST = prev.getNextSibling();
                }
            }
            return added;
        }

        @Override
        protected CsmTypedef createTypedef(AST ast, FileImpl file, CsmObject container, CsmType type, CharSequence name) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            return MemberTypedef.create(getContainingFile(), ClassImpl.this, ast, type, name, curentVisibility, !isRenderingLocalContext());
        }

        @Override
        protected CsmTypeAlias createTypeAlias(AST ast, FileImpl file, CsmObject container, CsmType type, CharSequence name) {
            type = TemplateUtils.checkTemplateType(type, ClassImpl.this);
            return MemberTypeAliasImpl.create(getContainingFile(), ClassImpl.this, ast, type, name, curentVisibility, !isRenderingLocalContext());
        }

        @Override
        protected CsmClassForwardDeclaration createForwardClassDeclaration(AST ast, MutableDeclarationsContainer container, FileImpl file, CsmScope scope) {
            ClassMemberForwardDeclaration fd = ClassMemberForwardDeclaration.create(getContainingFile(), ClassImpl.this, ast, curentVisibility, !isRenderingLocalContext());
            addMember(fd,!isRenderingLocalContext());
            fd.init(ast, ClassImpl.this, !isRenderingLocalContext());
            return fd;
        }
    }

    public static final class MemberTypedef extends TypedefImpl implements CsmMember {

        private final CsmVisibility visibility;

        private MemberTypedef(CsmFile file, CsmClass containingClass, AST ast, CsmType type, CharSequence name, CsmVisibility curentVisibility) {
            super(ast, file, containingClass, type, name);
            visibility = curentVisibility;
        }

        public static MemberTypedef create(CsmFile file, CsmClass containingClass, AST ast, CsmType type, CharSequence name, CsmVisibility curentVisibility, boolean global) {
            MemberTypedef memberTypedef = new MemberTypedef(file, containingClass, ast, type, name, curentVisibility);
            if (!global) {
                Utils.setSelfUID(memberTypedef);
            }
            return memberTypedef;
        }

        private MemberTypedef(CsmType type, CharSequence name, CsmVisibility visibility, CsmClass containingClass, CsmFile file, int startOffset, int endOffset) {
            super(type, name, containingClass, file, startOffset, endOffset);
            this.visibility = visibility;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmVisibility getVisibility() {
            return visibility;
        }

        @Override
        public CsmClass getContainingClass() {
            return (CsmClass) getScope();
        }

        public static class MemberTypedefBuilder extends TypedefBuilder implements CsmObjectBuilder, MemberBuilder {
            private CsmVisibility visibility = CsmVisibility.PUBLIC;

            public MemberTypedefBuilder(SimpleDeclarationBuilder builder) {
                super(builder);
            }

            @Override
            public void setVisibility(CsmVisibility visibility) {
                this.visibility = visibility;
            }

            @Override
            public MemberTypedef create(CsmParserProvider.ParserErrorDelegate delegate) {
                CsmClass cls = (CsmClass) getScope();

                CsmType type = null;
                if(getTypeBuilder() != null) {
                    getTypeBuilder().setScope(getScope());
                    type = getTypeBuilder().create();
                }
                if(type == null) {
                    type = TypeFactory.createSimpleType(BuiltinTypes.getBuiltIn("int"), getFile(), getStartOffset(), getStartOffset()); // NOI18N
                }

                MemberTypedef td = new MemberTypedef(type, getName(), visibility, cls, getFile(), getStartOffset(), getEndOffset());

                if (!isGlobal()) {
                    Utils.setSelfUID(td);
                }

                return td;
            }

        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert this.visibility != null;
            PersistentUtils.writeVisibility(this.visibility, output);
        }

        public MemberTypedef(RepositoryDataInput input) throws IOException {
            super(input);
            this.visibility = PersistentUtils.readVisibility(input);
            assert this.visibility != null;
        }
    }

    public static final class MemberTypeAliasImpl extends TypeAliasImpl implements CsmMember {

        private final CsmVisibility visibility;

        public static MemberTypeAliasImpl create(CsmFile file, CsmClass containingClass, AST ast, CsmType type, CharSequence name, CsmVisibility curentVisibility, boolean global) {
            MemberTypeAliasImpl memberTypedef = new MemberTypeAliasImpl(file, containingClass, ast, type, name, curentVisibility);
            if (!global) {
                Utils.setSelfUID(memberTypedef);
            }
            return memberTypedef;
        }

        private MemberTypeAliasImpl(CsmFile file, CsmClass containingClass, AST ast, CsmType type, CharSequence name, CsmVisibility curentVisibility) {
            super(ast, file, containingClass, type, name);
            visibility = curentVisibility;
        }

        private MemberTypeAliasImpl(CsmType type, CharSequence name, CsmVisibility visibility, CsmClass containingClass, CsmFile file, int startOffset, int endOffset) {
            super(type, name, containingClass, file, startOffset, endOffset);
            this.visibility = visibility;
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmVisibility getVisibility() {
            return visibility;
        }

        @Override
        public CsmClass getContainingClass() {
            return (CsmClass) getScope();
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert this.visibility != null;
            PersistentUtils.writeVisibility(this.visibility, output);
        }

        public MemberTypeAliasImpl(RepositoryDataInput input) throws IOException {
            super(input);
            this.visibility = PersistentUtils.readVisibility(input);
            assert this.visibility != null;
        }
    }

    public static interface MemberForwardDeclaration {}

    public static final class ClassMemberForwardDeclaration extends ClassForwardDeclarationImpl
            implements CsmMember, CsmClassifier, MemberForwardDeclaration {

        private final CsmVisibility visibility;
        private CsmUID<CsmClass> classDefinition;
        private final CsmUID<CsmClass> containerUID;
        private CsmClass containerRef;

        private ClassMemberForwardDeclaration(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            super(ast, file, register);
            visibility = curentVisibility;
            containerUID = UIDCsmConverter.declarationToUID(containingClass);
        }

        private ClassMemberForwardDeclaration(CharSequence name, TemplateDescriptor templateDescriptor, CsmClass containingClass, CsmVisibility curentVisibility, CsmFile file, int startOffset, int endOffset) {
            super(name, templateDescriptor, file, startOffset, endOffset);
            visibility = curentVisibility;
            containerUID = UIDCsmConverter.declarationToUID(containingClass);
        }

        public static ClassMemberForwardDeclaration create(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            ClassMemberForwardDeclaration res = new ClassMemberForwardDeclaration(file, containingClass, ast, curentVisibility, register);
            postObjectCreateRegistration(register, res);
            return res;
        }

        @Override
        protected final boolean registerInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                return ((ProjectBase) project).registerDeclaration(this);
            }
            return false;
        }

        private void unregisterInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                ((ProjectBase) project).unregisterDeclaration(this);
                this.cleanUID();
            }
        }

        @Override
        public void dispose() {
            super.dispose();
            onDispose();
            CsmScope scope = getScope();
            if (scope instanceof MutableDeclarationsContainer) {
                ((MutableDeclarationsContainer) scope).removeDeclaration(this);
            }
            unregisterInProject();
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmVisibility getVisibility() {
            return visibility;
        }

        private synchronized void onDispose() {
            if (containerRef == null) {
                containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
        }

        @Override
        public synchronized CsmClass getContainingClass() {
            CsmClass out = containerRef;
            if (out == null) {
                out = containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
            return out;
        }

        @Override
        public CsmScope getScope() {
            return getContainingClass();
        }

        @Override
        public CsmClass getCsmClass() {
            CsmClass cls = UIDCsmConverter.UIDtoClass(classDefinition);
            // we need to replace i.e. ForwardClass stub
            if (cls != null && cls.isValid() && !ForwardClass.isForwardClass(cls)) {
                return cls;
            } else {
                cls = super.getCsmClass();
                setCsmClass(cls);
            }
            return cls;
        }

        @Override
        protected ForwardClass createForwardClassIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
            ForwardClass cls = super.createForwardClassIfNeed(ast, scope, registerInProject);
            classDefinition = UIDCsmConverter.declarationToUID((CsmClass)cls);
            if (cls != null) {
                RepositoryUtils.put(this);
            }
            return cls;
        }

        public void setCsmClass(CsmClass cls) {
            classDefinition = UIDCsmConverter.declarationToUID(cls);
        }

        @Override
        public CharSequence getQualifiedName() {
            CsmClass cls = getContainingClass();
            if (cls == null) {
                cls = getContainingClass();
            }
            return CharSequences.create(CharSequenceUtils.concatenate(cls.getQualifiedName(), "::", getName())); // NOI18N
        }

        public static class ClassMemberForwardDeclarationBuilder extends ClassForwardDeclarationBuilder implements MemberBuilder {
            private CsmVisibility visibility = CsmVisibility.PUBLIC;

            public ClassMemberForwardDeclarationBuilder() {
            }

            public ClassMemberForwardDeclarationBuilder(SimpleDeclarationBuilder builder) {
                super(builder);
            }

            @Override
            public void setVisibility(CsmVisibility visibility) {
                this.visibility = visibility;
            }

            @Override
            public ClassMemberForwardDeclaration create(CsmParserProvider.ParserErrorDelegate delegate) {
                TemplateDescriptor td = null;
                if(getTemplateDescriptorBuilder() != null) {
                    getTemplateDescriptorBuilder().setScope(getScope());
                    td = getTemplateDescriptorBuilder().create();
                }

                ClassMemberForwardDeclaration fc = new ClassMemberForwardDeclaration(getName(), td, (CsmClass)getScope(), visibility, getFile(), getStartOffset(), getEndOffset());

                postObjectCreateRegistration(isGlobal(), fc);

                return fc;
            }
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert visibility != null;
            PersistentUtils.writeVisibility(visibility, output);
            assert containerUID != null;
            UIDObjectFactory.getDefaultFactory().writeUID(containerUID, output);
            UIDObjectFactory.getDefaultFactory().writeUID(classDefinition, output);
        }

        public ClassMemberForwardDeclaration(RepositoryDataInput input) throws IOException {
            super(input);
            visibility = PersistentUtils.readVisibility(input);
            assert visibility != null;
            containerUID = UIDObjectFactory.getDefaultFactory().readUID(input);
            assert containerUID != null;
            classDefinition = UIDObjectFactory.getDefaultFactory().readUID(input);
        }
    }

    public static final class EnumMemberForwardDeclaration extends EnumForwardDeclarationImpl
            implements CsmMember, CsmClassifier, MemberForwardDeclaration {

        private final CsmVisibility visibility;
        private CsmUID<CsmEnum> enumDefinition;
        private final CsmUID<CsmClass> containerUID;
        private CsmClass containerRef;

        private EnumMemberForwardDeclaration(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            super(ast, file, register);
            visibility = curentVisibility;
            containerUID = UIDCsmConverter.declarationToUID(containingClass);
        }

        public static EnumMemberForwardDeclaration create(CsmFile file, CsmClass containingClass, AST ast, CsmVisibility curentVisibility, boolean register) {
            EnumMemberForwardDeclaration res = new EnumMemberForwardDeclaration(file, containingClass, ast, curentVisibility, register);
            postObjectCreateRegistration(register, res);
            return res;
        }

        @Override
        protected final boolean registerInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                return ((ProjectBase) project).registerDeclaration(this);
            }
            return false;
        }

        private void unregisterInProject() {
            CsmProject project = getContainingFile().getProject();
            if (project instanceof ProjectBase) {
                ((ProjectBase) project).unregisterDeclaration(this);
                this.cleanUID();
            }
        }

        @Override
        public void dispose() {
            super.dispose();
            onDispose();
            CsmScope scope = getScope();
            if (scope instanceof MutableDeclarationsContainer) {
                ((MutableDeclarationsContainer) scope).removeDeclaration(this);
            }
            unregisterInProject();
        }

        @Override
        public boolean isStatic() {
            return false;
        }

        @Override
        public CsmVisibility getVisibility() {
            return visibility;
        }

        private synchronized void onDispose() {
            if (containerRef == null) {
                containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
        }

        @Override
        public synchronized CsmClass getContainingClass() {
            CsmClass out = containerRef;
            if (out == null) {
                out = containerRef = UIDCsmConverter.UIDtoClass(containerUID);
            }
            return out;
        }

        @Override
        public CsmScope getScope() {
            return getContainingClass();
        }

        @Override
        public CsmEnum getCsmEnum() {
            CsmEnum enm = UIDCsmConverter.UIDtoDeclaration(enumDefinition);
            // we need to replace i.e. ForwardEnum stub
            if (enm != null && enm.isValid() && !ForwardEnum.isForwardEnum(enm)) {
                return enm;
            } else {
                enm = super.getCsmEnum();
                setCsmEnum(enm);
            }
            return enm;
        }

        @Override
        protected ForwardEnum createForwardEnumIfNeed(AST ast, CsmScope scope, boolean registerInProject) {
            ForwardEnum enm = super.createForwardEnumIfNeed(ast, scope, registerInProject);
            enumDefinition = UIDCsmConverter.declarationToUID((CsmEnum) enm);
            if (enm != null) {
                RepositoryUtils.put(this);
            }
            return enm;
        }

        public void setCsmEnum(CsmEnum cls) {
            enumDefinition = UIDCsmConverter.declarationToUID(cls);
        }

        @Override
        public CharSequence getQualifiedName() {
            CsmClass cls = getContainingClass();
            if (cls == null) {
                cls = getContainingClass();
            }
            return CharSequences.create(CharSequenceUtils.concatenate(cls.getQualifiedName(), "::", getName())); // NOI18N
        }

        ////////////////////////////////////////////////////////////////////////////
        // impl of SelfPersistent
        @Override
        public void write(RepositoryDataOutput output) throws IOException {
            super.write(output);
            assert visibility != null;
            PersistentUtils.writeVisibility(visibility, output);
            assert containerUID != null;
            UIDObjectFactory.getDefaultFactory().writeUID(containerUID, output);
            UIDObjectFactory.getDefaultFactory().writeUID(enumDefinition, output);
        }

        public EnumMemberForwardDeclaration(RepositoryDataInput input) throws IOException {
            super(input);
            visibility = PersistentUtils.readVisibility(input);
            assert visibility != null;
            containerUID = UIDObjectFactory.getDefaultFactory().readUID(input);
            assert containerUID != null;
            enumDefinition = UIDObjectFactory.getDefaultFactory().readUID(input);
        }
    }
}
