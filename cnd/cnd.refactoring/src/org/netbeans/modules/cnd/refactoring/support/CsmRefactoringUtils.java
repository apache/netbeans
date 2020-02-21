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
package org.netbeans.modules.cnd.refactoring.support;

import java.util.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Position;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.project.Project;
import org.netbeans.cnd.api.lexer.CppTokenId;
import org.netbeans.cnd.api.lexer.DoxygenTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnum;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilter;
import org.netbeans.modules.cnd.api.model.services.CsmSelect.CsmFilterBuilder;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.util.UIDs;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.api.project.NativeProjectRegistry;
import org.netbeans.modules.cnd.modelutil.CsmDisplayUtilities;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.spi.CsmRefactoringNameProvider;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionRef;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
public final class CsmRefactoringUtils {
    public static final String USG_CND_REFACTORING = "USG_CND_REFACTORING"; // NOI18N
    public static final String GENERATE_TRACKING = "GENERATE"; // NOI18N
    public static final String FROM_EDITOR_TRACKING = "FROM_EDITOR"; // NOI18N

    private CsmRefactoringUtils() {
    }

    public static boolean isElementInOpenProject(CsmFile csmFile) {
        if (csmFile == null) {
            return false;
        }
        Object p = csmFile.getProject().getPlatformProject();
        if (p != null) {
            for (NativeProject prj : NativeProjectRegistry.getDefault().getOpenProjects()) {
                if (prj.equals(p)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean isRefactorable(FileObject fo) {
        if (fo != null && (FileUtil.getArchiveFile(fo) != null || !fo.canWrite())) {
            return false;
        }
        return true;
    }
    
    public static CsmObject convertToCsmObjectIfNeeded(CsmObject referencedObject) {
        if (CsmKindUtilities.isInclude(referencedObject)) {
            referencedObject = ((CsmInclude) referencedObject).getIncludeFile();
        } else if (CsmKindUtilities.isFunctionDefinition(referencedObject)) {
            CsmFunction decl = CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
            if (decl != null) {
                referencedObject = decl;
            }
        }
        return referencedObject;
    }

    public static Collection<CsmProject> getContextCsmProjects(CsmObject contextObject) {
        Collection<CsmProject> prjs = new HashSet<>();
        CsmFile contextFile = null;
        if (CsmKindUtilities.isOffsetable(contextObject)) {
            contextFile = ((CsmOffsetable)contextObject).getContainingFile();
        } else if (CsmKindUtilities.isFile(contextObject)) {
            contextFile = (CsmFile)contextObject;
        }
        CsmProject csmProject = null;
        if (contextFile != null) {
            csmProject = contextFile.getProject();
            prjs.add(csmProject);
            if (false) {
                // try another projects which could share the same file
                FileObject fileObject = CsmUtilities.getFileObject(contextFile);
                if (fileObject != null) {
                    CsmFile[] csmFiles;
                    try {
                        csmFiles = CsmUtilities.getCsmFiles(DataObject.find(fileObject), false, false);
                    } catch (DataObjectNotFoundException ex) {
                        csmFiles = new CsmFile[0];
                    }
                    for (CsmFile csmFile : csmFiles) {
                        prjs.add(csmFile.getProject());
                    }
                }
            }
        } else if (CsmKindUtilities.isNamespace(contextObject)) {
            prjs.add(((CsmNamespace)contextObject).getProject());
        }
        return prjs;
    }

    public static void waitParsedAllProjects() {
        for (CsmProject prj : CsmModelAccessor.getModel().projects()) {
            prj.waitParse();
        }
    }

    public static Collection<CsmProject> getRelatedCsmProjects(CsmObject origObject, CsmProject p) {
        Collection<CsmProject> out = Collections.<CsmProject>emptyList();
        if (p != null) {
            out = Collections.singleton(p);
        } else {
            if (true) {
                // for now return all...
                Collection<CsmProject> all = CsmModelAccessor.getModel().projects();
                out = new HashSet<>(all);
            } else {
                out = new HashSet<>();
                Collection<CsmProject> prjs = getContextCsmProjects(origObject);
                out.addAll(prjs);
                boolean addLibs = false;
                for (CsmProject prj : out) {
                    if (prj != null && prj.isArtificial()) {
                        addLibs = true;
                    }
                }
                if (addLibs) {
                    // add all libraries as well
                    Collection<CsmProject> all = CsmModelAccessor.getModel().projects();
                    Set<CsmProject> libs = new HashSet<>();
                    for (CsmProject csmProject : all) {
                        libs.addAll(csmProject.getLibraries());
                    }
                    out.addAll(libs);
                }
            }
        }
        return out;
    }
    
    public static Collection<Project> getContextProjects(CsmObject contextObject) {
        Collection<CsmProject> csmProjects = getContextCsmProjects(contextObject);
        Collection<Project> out = new ArrayList<>();
        for (CsmProject csmProject : csmProjects) {
            if (csmProject != null) {
                Object o = csmProject.getPlatformProject();
                if (o instanceof NativeProject) {
                    o = ((NativeProject)o).getProject();
                }
                if (o instanceof Project) {
                    out.add((Project)o);
                }
            }
        }
        return out;
    }
        
    public static CsmObject getReferencedElement(CsmObject csmObject) {
        if (csmObject instanceof CsmReference) {
            return getReferencedElement(((CsmReference)csmObject).getReferencedObject());
        } else {
            return csmObject;
        }
    } 

    private static final Lookup.Result<CsmRefactoringNameProvider> renameProviders = Lookup.getDefault().lookupResult(CsmRefactoringNameProvider.class);

    public static String getReplaceText(CsmReference ref, String newName, AbstractRefactoring refactoring) {
        for (CsmRefactoringNameProvider provider : renameProviders.allInstances()) {
            String newText = provider.getReplaceText(ref, newName, refactoring);
            if (newText != null) {
                newName = newText;
            }
        }
        return newName;
    }
    
    public static String getReplaceDescription(CsmReference ref, AbstractRefactoring refactoring) {
        for (CsmRefactoringNameProvider provider : renameProviders.allInstances()) {
            String descr = provider.getReplaceDescription(ref, refactoring);
            if (descr != null) {
                return descr;
            }
        }
        return getReplaceDescription(ref, ref.getText().toString());    
    }

    private static String getReplaceDescription(CsmReference ref, String targetName) {
        boolean decl = CsmReferenceResolver.getDefault().isKindOf(ref, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
        String out = NbBundle.getMessage(CsmRefactoringUtils.class, decl ? "UpdateDeclRef" : "UpdateRef", targetName);
        return out;
    }
    
    public static String getSimpleText(CsmObject element) {
        String text = "";
        if (element != null) {
            if (CsmKindUtilities.isFile(element)) {
                text = CsmUtilities.getFileObject(((CsmFile)element)).getName();
            } else if (CsmKindUtilities.isNamedElement(element)) {
                text = ((CsmNamedElement) element).getName().toString();
            } else if (CsmKindUtilities.isStatement(element)) {
                text = ((CsmStatement)element).getText().toString();
            } else if (CsmKindUtilities.isOffsetable(element) ) {
                text = ((CsmOffsetable)element).getText().toString();
            }
            // cut off destructor prefix
            if (text.startsWith("~")) { // NOI18N
                text = text.substring(1);
            }
            text = getRefactoredName(element, text);
        }
        return text;
    }

    public static String getRefactoredName(CsmObject element, String text) {
        for (CsmRefactoringNameProvider provider : renameProviders.allInstances()) {
            String newName = provider.getRefactoredName(element, text);
            if (newName != null) {
                text = newName;
            }
        }
        return text;
    }

    public static ModificationResult.Difference rename(int startOffset, int endOffset, CloneableEditorSupport ces,
            String oldName, String newName, String descr) {
        assert oldName != null;
        assert newName != null;
        PositionRef startPos = ces.createPositionRef(startOffset, Position.Bias.Forward);
        PositionRef endPos = ces.createPositionRef(endOffset, Position.Bias.Backward);
        ModificationResult.Difference diff = new ModificationResult.Difference(ModificationResult.Difference.Kind.CHANGE, startPos, endPos, oldName, newName, descr);
        return diff;
    }
    
    public static FileObject getFileObject(CsmObject object) {
        CsmFile container = null;
        if (CsmKindUtilities.isFile(object)) {
            container = (CsmFile)object;
        } else if (CsmKindUtilities.isOffsetable(object)) {
            container = ((CsmOffsetable)object).getContainingFile();
        }
        return container == null ? null : CsmUtilities.getFileObject(container);
    }
    
    public static CsmObject findContextObject(Lookup lookup) {
        CsmFile file = null;
        CsmObject out = null;
        Collection<? extends CsmObject> coll = lookup.lookupAll(CsmObject.class);
        for (CsmObject obj : coll) {
            if (CsmKindUtilities.isFile(obj)) {
                // try to find something smaller
                file = (CsmFile) obj;
            } else if (CsmKindUtilities.isInclude(obj)) {
                if (((CsmInclude) obj).getStartOffset() < 0) {
                    // -inclide directive from item properties
                    // unsupported rename
                } else {
                    file = ((CsmInclude) obj).getIncludeFile();
                }
            } else {
                out = obj;
                break;
            }
        }
        if (out == null) {
            CsmUID<?> uid = lookup.lookup(CsmUID.class);
            if (uid != null) {
                out = (CsmObject) uid.getObject();
            }
            if (out == null) {
                Node node = lookup.lookup(Node.class);
                if (node != null) {
                    out = CsmReferenceResolver.getDefault().findReference(node);
                }
            }            
        }
        if (out == null) {
            out = file;
        }
        return out;
    }
    
    public static <T extends CsmObject> CsmUID<T> getHandler(T element) {
        return element == null ? null : UIDs.get(element);
    }
    
    public static <T> T getObject(CsmUID<T> handler) {
        return handler == null ? null : handler.getObject();
    }
    
    public static boolean isSupportedReference(CsmObject ref) {
        return ref != null;
    }    
    
    public static String getHtml(CsmObject obj) {
        if (CsmKindUtilities.isOffsetable(obj)) {
            return getHtml((CsmOffsetable)obj);
        } else if (CsmKindUtilities.isFile(obj)) {
            return CsmDisplayUtilities.htmlize(((CsmFile)obj).getName().toString());
        } else {
            return obj.toString();
        }
    }

    public static CsmFile getCsmFile(CsmObject csmObject) {
        if (CsmKindUtilities.isFile(csmObject)) {
            return ((CsmFile) csmObject);
        } else if (CsmKindUtilities.isOffsetable(csmObject)) {
            return ((CsmOffsetable) csmObject).getContainingFile();
        }
        return null;
    }

    public static Collection<CsmFunction> getConstructors(CsmClass cls) {
        Collection<CsmFunction> out = new ArrayList<>();
        CsmFilterBuilder filterBuilder = CsmSelect.getFilterBuilder();
        CsmSelect.CsmFilter filter = filterBuilder.createCompoundFilter(
                CsmSelect.FUNCTION_KIND_FILTER,
                filterBuilder.createNameFilter(cls.getName(), true, true, false));
        Iterator<CsmMember> classMembers = CsmSelect.getClassMembers(cls, filter);
        while (classMembers.hasNext()) {
            CsmMember csmMember = classMembers.next();
            if (CsmKindUtilities.isConstructor(csmMember)) {
                out.add((CsmConstructor) csmMember);
            }
        }
        return out;
    }

    public static CsmObject getEnclosingElement(CsmObject decl) {
        assert decl != null;
//        while (decl instanceof CsmReference) {
//            decl = ((CsmReference)decl).getOwner();
//        }
        if (CsmKindUtilities.isOffsetable(decl)) {
            return findInnerFileObject((CsmOffsetable)decl);
        }
//
//        CsmObject scopeElem = decl instanceof CsmReference ? ((CsmReference)decl).getOwner() : decl;
//        while (CsmKindUtilities.isScopeElement(scopeElem)) {
//            CsmScope scope = ((CsmScopeElement)scopeElem).getScope();
//            if (isLangContainerFeature(scope)) {
//                return scope;
//            } else if (CsmKindUtilities.isScopeElement(scope)) {
//                scopeElem = ((CsmScopeElement)scope);
//            } else {
//                if (scope == null) { System.err.println("scope element without scope " + scopeElem); }
//                break;
//            }
//        }
//        if (CsmKindUtilities.isOffsetable(decl)) {
//            return ((CsmOffsetable)decl).getContainingFile();
//        }
        return null;
    }
    
    /*package*/ static boolean isLangContainerFeature(CsmObject obj) {
        assert obj != null;
        return CsmKindUtilities.isFunction(obj) ||
                    CsmKindUtilities.isClass(obj) ||
                    CsmKindUtilities.isEnum(obj) ||
                    CsmKindUtilities.isNamespaceDefinition(obj) ||
                    CsmKindUtilities.isFile(obj);
    }
    
    private static String getHtml(CsmOffsetable obj) {
        CsmFile csmFile = obj.getContainingFile();        
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        BaseDocument doc = null;
        String displayText = null;
        if (ces != null) {
            Document d = CsmUtilities.openDocument(ces);
            if (d instanceof BaseDocument) {
                doc = (BaseDocument) d;
            }
        }
        if (doc != null) {
            try {            
                int stOffset = obj.getStartOffset();
                int endOffset = obj.getEndOffset();
                int endLineOffset = 1;
                if (CsmKindUtilities.isNamespaceDefinition((CsmObject)obj) ||
                        CsmKindUtilities.isEnum((CsmObject)obj)) {
                    endOffset = stOffset;
                    //endLineOffset = 0;
                } else if (CsmKindUtilities.isFunctionDefinition((CsmObject)obj)) {
                    endOffset = ((CsmFunctionDefinition)obj).getBody().getStartOffset()-1;
                } else if (CsmKindUtilities.isClass((CsmObject)obj)) {
                    endOffset = ((CsmClass)obj).getLeftBracketOffset()-1;
                }
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stOffset);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endOffset) + endLineOffset;
                displayText = CsmDisplayUtilities.getLineHtml(startLine, endLine, -1, -1, doc);
            } catch (BadLocationException ex) {
            }            
        }
        if (displayText == null) {
            displayText = CsmDisplayUtilities.htmlize(obj.getText().toString());
        }
        return displayText;
    }

    ////////////////////////////////////////////////////////////////////////////
    // by-offset methods
    
    private static boolean isInObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if ((offs.getStartOffset() <= offset) &&
                (offset <= offs.getEndOffset())) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isBeforeObject(CsmObject obj, int offset) {
        if (!CsmKindUtilities.isOffsetable(obj)) {
            return false;
        }
        CsmOffsetable offs = (CsmOffsetable)obj;
        if (offset < offs.getStartOffset()) {
            return true;
        } else {
            return false;
        }
    }
    
    public static CsmObject findInnerFileObject(CsmFile file, int offset) {
        assert (file != null) : "can't be null file in findInnerFileObject";
        // check file declarations
        CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(offset);
        CsmObject lastObject = findInnerDeclaration(CsmSelect.getDeclarations(file, filter), offset);
//        // check macros if needed
//        lastObject = lastObject != null ? lastObject : findObject(file.getMacros(), context, offset);
        return lastObject;
    }
    
    private static CsmDeclaration findInnerDeclaration(final Iterator<? extends CsmDeclaration> it, final int offset) {
        CsmDeclaration innerDecl = null;
        if (it != null) {
            // continue till has next and not yet found
            while (it.hasNext()) {
                CsmDeclaration decl = (CsmDeclaration) it.next();
                assert (decl != null) : "can't be null declaration";
                if (isInObject(decl, offset) && isLangContainerFeature(decl)) {
                    // we are inside declaration, but try to search deeper
                    innerDecl = findInnerDeclaration(decl, offset);
                    if (innerDecl != null) {
                        return innerDecl;
                    } else {
                        return decl;
                    }
                } else if (isBeforeObject(decl, offset)) {
                    break;
                }
            }
        }
        return innerDecl;
    }
        
    // must check before call, that offset is inside outDecl
    private static CsmDeclaration findInnerDeclaration(CsmDeclaration outDecl, int offset) {
        assert (isInObject(outDecl, offset)) : "must be in outDecl object!";
        Iterator<? extends CsmDeclaration> it = null;
        if (CsmKindUtilities.isNamespace(outDecl)) { 
            CsmNamespace ns = (CsmNamespace)outDecl;
            it = ns.getDeclarations().iterator();
        } else if (CsmKindUtilities.isNamespaceDefinition(outDecl)) {
            it = ((CsmNamespaceDefinition) outDecl).getDeclarations().iterator();
        } else if (CsmKindUtilities.isClass(outDecl)) {
            CsmClass cl  = (CsmClass)outDecl;
            it = cl.getMembers().iterator();
        } else if (CsmKindUtilities.isEnum(outDecl)) {
            CsmEnum en = (CsmEnum)outDecl;
            it = en.getEnumerators().iterator();
        }
        return findInnerDeclaration(it, offset);
    }      
    
    private static CsmObject findInnerFileObject(CsmOffsetable csmOffsetable) {
        final CsmFile containingFile = csmOffsetable.getContainingFile();
        if (containingFile != null) {
            int offset = csmOffsetable.getStartOffset();
            if (offset > 0) {
                // trying to find previous enclosing element => start with previous offset
                CsmObject obj = findInnerFileObject(containingFile, offset-1);
                if (obj != null) {
                    // previous element can be just previous declaration on file level => it is not enclosing
                    if (CsmKindUtilities.isOffsetable(obj) && (((CsmOffsetable)obj).getEndOffset() < offset)) {
                        return containingFile;
                    }
                    return obj;
                }
            }
        }
        return containingFile;
    } 
    
    public static Collection<CsmReference> getComments(final CsmFile file, final String text) {
        final Collection<CsmReference> comments = new ArrayList<>();
        final Document doc = CsmUtilities.getDocument(file);
        if (doc != null) {
            doc.render(new Runnable() {
                @Override
                public void run() {
                    TokenHierarchy<Document> hi = TokenHierarchy.get(doc);
                    TokenSequence<?> ts = hi.tokenSequence();
                    while (ts.moveNext()) {
                        Token<?> token = ts.token();
                        if (CppTokenId.COMMENT_CATEGORY.equals(token.id().primaryCategory())) {
                            TokenSequence<?> te = ts.embedded();
                            if (te != null) {
                                while (te.moveNext()) {
                                    Token<?> commentToken = te.token();
                                    if (commentToken.id() == DoxygenTokenId.IDENT) {
                                        if (text.contentEquals(commentToken.text())) {
                                            int offset = commentToken.offset(hi);
                                            comments.add(new CsmCommentReferenceImpl(text, offset, offset + commentToken.length(), file));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            });
        }
        return comments;
    }
    
    @ServiceProvider(service=CsmRefactoringNameProvider.class, position=100)
    public static final class CsmRefactoringNameProviderImpl implements CsmRefactoringNameProvider {

        @Override
        public String getRefactoredName(CsmObject object, String current) {
            return null;
        }

        @Override
        public String getReplaceText(CsmReference ref, String newText, AbstractRefactoring refactoring) {
            String out = null;
            final CsmObject referencedObject = ref.getReferencedObject();
            if (CsmKindUtilities.isFile(referencedObject)) {
                final CsmObject owner = ref.getOwner();
                assert CsmKindUtilities.isInclude(owner) : "include directive is expected " + owner;
                FileObject file = CsmUtilities.getFileObject((CsmFile) referencedObject);
                if (file != null) {
                    String oldText = ref.getText().toString();
                    // check if include directive really contains file name and not macro expression
                    String fileNameExt = file.getNameExt();
                    final int lastIndexOf = oldText.lastIndexOf(fileNameExt);
                    if (lastIndexOf > 0) {
                        String fileName = file.getName();
                        out = oldText.substring(0, lastIndexOf) + newText + oldText.substring(lastIndexOf + fileName.length());
                    }
                }
            }
            return out;
        }

        @Override
        public String getReplaceDescription(CsmReference ref, AbstractRefactoring refactoring) {
            String out = null;
            final CsmObject referencedObject = ref.getReferencedObject();
            if (CsmKindUtilities.isFile(referencedObject)) {
                String oldText = ref.getText().toString();
                return NbBundle.getMessage(CsmRefactoringUtils.class, "UpdateInclude", oldText);                    
            }
            return out;
        }
        
    }
    
    private static class CsmCommentReferenceImpl implements CsmReference {

        private final CharSequence label;
        private final CsmFile file;
        private final int startOffset;
        private final int endOffset;

        public CsmCommentReferenceImpl(CharSequence label, int startOffset, int endOffset, CsmFile file) {
            this.label = label;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.file = file;
        }

        @Override
        public CsmReferenceKind getKind() {
            return CsmReferenceKind.COMMENT;
        }

        @Override
        public CsmObject getReferencedObject() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public CsmObject getOwner() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public CsmObject getClosestTopLevelObject() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public CsmFile getContainingFile() {
            return file;
        }

        @Override
        public int getStartOffset() {
            return startOffset;
        }

        @Override
        public int getEndOffset() {
            return endOffset;
        }

        @Override
        public Position getStartPosition() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public Position getEndPosition() {
            throw new UnsupportedOperationException("Not supported."); //NOI18N
        }

        @Override
        public CharSequence getText() {
            return label;
        }

        @Override
        public String toString() {
            return "" + label + "[" + getKind() + "] "; //NOI18N
        }
    }
}
