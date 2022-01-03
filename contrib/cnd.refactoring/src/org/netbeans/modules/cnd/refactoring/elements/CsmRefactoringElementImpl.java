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
package org.netbeans.modules.cnd.refactoring.elements;

import java.util.EnumSet;
import javax.swing.Icon;
import javax.swing.text.BadLocationException;
import javax.swing.text.StyledDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.plugins.CsmWhereUsedFilters;
import org.netbeans.modules.cnd.refactoring.support.ElementGripFactory;
import org.netbeans.modules.refactoring.spi.FiltersManager;
import org.netbeans.modules.refactoring.spi.RefactoringElementImplementation;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 */
public class CsmRefactoringElementImpl extends SimpleRefactoringElementImplementation 
        implements FiltersManager.Filterable {
    
    public enum RW {
        Read,
        Write,
        ReadWrite
    }
    private static final boolean LAZY = false;
    private final CsmReference elem;
    private final PositionBounds bounds;
    private final FileObject fo;
    private String displayText;
    private final Object enclosing;
    
    private final boolean isDecl;
    private final boolean isScope;
    private final boolean isInMacros;
    private final boolean isInDeadCode;
    private final boolean isInComments;
    private final RW rw;
    
    public CsmRefactoringElementImpl(PositionBounds bounds, 
            CsmReference elem, FileObject fo, String displayText, CsmObject referencedObject) {
        this.elem = elem;
        this.bounds = bounds;
        this.fo = fo;
        assert displayText != null || LAZY;
        this.displayText = displayText;
        Object composite = ElementGripFactory.getDefault().putInComposite(fo, elem);
        if (composite==null) {
            composite = fo;
        }  
        this.enclosing = composite;
        this.isDecl = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.DECLARATION, CsmReferenceKind.DEFINITION));
        this.isScope = isScope(elem);
        this.isInMacros = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.IN_PREPROCESSOR_DIRECTIVE));
        this.isInDeadCode = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.IN_DEAD_BLOCK));
        this.isInComments = CsmReferenceResolver.getDefault().isKindOf(elem, EnumSet.of(CsmReferenceKind.COMMENT));
        if (referencedObject != null && CsmKindUtilities.isVariable(referencedObject)) {
            this.rw = new ReadWriteTokenProcessor(elem).process();
        } else {
            this.rw = RW.Read;
        }
    }

    private boolean isScope(CsmReference ref) {
        CsmFile csmFile = ref.getContainingFile();
        int stToken = ref.getStartOffset();
        int endToken = ref.getEndOffset();
        CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
        StyledDocument stDoc = CsmUtilities.openDocument(ces);
        if (stDoc instanceof BaseDocument) {
            BaseDocument doc = (BaseDocument) stDoc;
            try {
                int startLine = LineDocumentUtils.getLineFirstNonWhitespace(doc, stToken);
                int endLine = LineDocumentUtils.getLineLastNonWhitespace(doc, endToken) + 1;
                String restText = doc.getText(endToken, endLine - startLine).trim();
                return restText.startsWith("::"); //NOI18N
            } catch (BadLocationException ex) {
                // skip
            }
        }
        return false;
    }

    @Override
    public String getText() {
        return elem.getText().toString();
    }

    @Override
    public String getDisplayText() {
        if (displayText == null && LAZY) {
            displayText = CsmReferenceSupport.getContextLineHtml(elem, true).toString();
        }
        return displayText;
    }

    @Override
    public void performChange() {
    }

    @Override
    public Lookup getLookup() {
        if (isInComments) {
            Icon icon = ImageUtilities.loadImageIcon("org/netbeans/modules/cnd/refactoring/resources/found_item_comment.png", false); //NOI18N
            return Lookups.fixed(elem, enclosing, icon);
        }
        return Lookups.fixed(elem, enclosing);
    }
    
    @Override
    public FileObject getParentFile() {
        return fo;
    }

    @Override
    public PositionBounds getPosition() {
        return bounds;
    }

    @Override
    public String toString() {
        return "{" + "bounds=" + bounds + ", displayText=" + displayText + ", enclosing=" + enclosing + ", fo=" + fo + '}'; // NOI18N
    }
    
    public static RefactoringElementImplementation create(CsmReference ref, boolean nameInBold, CsmObject referencedObject) {
        CsmFile csmFile = ref.getContainingFile();
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        PositionBounds bounds = CsmUtilities.createPositionBounds(ref);
        String displayText = LAZY ? null : CsmReferenceSupport.getContextLineHtml(ref, nameInBold).toString();
        return new CsmRefactoringElementImpl(bounds, ref, fo, displayText, referencedObject);
    }

    @Override
    public boolean filter(FiltersManager manager) {
        if (rw == RW.Read && !manager.isSelected(CsmWhereUsedFilters.READ.getKey())) {
            return false;
        }
        if (rw == RW.Write && !manager.isSelected(CsmWhereUsedFilters.WRITE.getKey())) {
            return false;
        }
        if (rw == RW.ReadWrite && !manager.isSelected(CsmWhereUsedFilters.READ_WRITE.getKey())) {
            return false;
        }
        if (isDecl && !manager.isSelected(CsmWhereUsedFilters.DECLARATIONS.getKey())) {
            return false;
        }
        if (isScope && !manager.isSelected(CsmWhereUsedFilters.SCOPE.getKey())) {
            return false;
        }
        if (isInMacros && !manager.isSelected(CsmWhereUsedFilters.MACROS.getKey())) {
            return false;
        }
        if (isInDeadCode && !manager.isSelected(CsmWhereUsedFilters.DEAD_CODE.getKey())) {
            return false;
        }
        if (isInComments && !manager.isSelected(CsmWhereUsedFilters.COMMENTS.getKey())) {
            return false;
        }
        return true;
    }
}
