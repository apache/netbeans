/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.refactoring.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmFileInfoQuery;
import org.netbeans.modules.cnd.api.model.services.CsmMacroExpansion;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceKind;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.api.InlineRefactoring;
import org.netbeans.modules.cnd.refactoring.spi.CsmInlineExtraObjectProvider;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 *
 */
public class InlinePlugin extends CsmModificationRefactoringPlugin {
    private final InlineRefactoring refactoring;
    private Collection<CsmObject> referencedObjects;
    
    public InlinePlugin(InlineRefactoring refactoring) {
        super(refactoring);
        this.refactoring = refactoring;
    }
    
    @Override
    protected final void processFile(CsmFile file, ModificationResult mr, AtomicReference<Problem> outProblem) {
        List<CsmReference> sortedRefs = new ArrayList<>();
        if (file == null) {
            return;
        }
        if (refactoring.getApplyPlace().equals(InlineRefactoring.Apply.IN_PLACE)) {
            JTextComponent component = EditorRegistry.lastFocusedComponent();
            final int caret = component.getCaretPosition();
            Document originalDoc = component.getDocument();
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(originalDoc, caret);
            if (ref == null) {
                return;
            }
            sortedRefs.add(ref);
        } else {
            Collection<CsmReference> refs = new ArrayList<>();
            for (CsmObject o : referencedObjects) {
                refs.addAll(CsmReferenceRepository.getDefault().getReferences(o, file, EnumSet.of(CsmReferenceKind.DIRECT_USAGE), Interrupter.DUMMY));
            }
            if (!refs.isEmpty()) {
                sortedRefs = new ArrayList<>(refs);
                Collections.sort(sortedRefs, new Comparator<CsmReference>() {
                    @Override
                    public int compare(CsmReference o1, CsmReference o2) {
                        return o1.getStartOffset() - o2.getStartOffset();
                    }
                });
            }
        }
        processRefactoredReferences(sortedRefs, file, mr);
    }
    
    private void processRefactoredReferences(List<CsmReference> sortedRefs, CsmFile file, ModificationResult mr) {
        for (CsmReference ref : sortedRefs) {
            CsmObject obj = ref.getReferencedObject();
            if (CsmKindUtilities.isMacro(obj) && (!isInMacro(file, ref))) {
                CsmMacro macro = (CsmMacro) obj;
                int refLine = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, ref.getStartOffset())[0];
                int objLine = CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, macro.getStartOffset())[0];
                if (!(refLine == objLine && (ref.getContainingFile().equals(macro.getContainingFile())))) {
                    String oldText = macro.getName().toString();

                    CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(file);
                    Document doc = CsmUtilities.openDocument(ces);
                    String newText = CsmMacroExpansion.expand(doc, file, ref.getStartOffset(), ref.getEndOffset(), true);
                    if (newText != null && (!newText.isEmpty())) {
                        String descr = NbBundle.getMessage(InlinePlugin.class, "TXT_Preview_Entity_escription") + " " +oldText;  // NOI18N
                        ModificationResult.Difference diff = CsmRefactoringUtils.rename(  ref.getStartOffset()
                                                                                        , ref.getEndOffset() + getMacroParametersEndOffset(file, macro, ref.getEndOffset())
                                                                                        , ces
                                                                                        , oldText
                                                                                        , newText
                                                                                        , descr);
                        assert diff != null;
                        mr.addDifference(file.getFileObject(), diff);
                    }
                }
            }
        }
    }
    
    private boolean isInMacro(CsmFile file, CsmReference reference) {
        CsmFileInfoQuery fiq = CsmFileInfoQuery.getDefault();
        int stopOffset = reference.getStartOffset();
        int line = fiq.getLineColumnByOffset(file, stopOffset)[0];
        int offset = (int) fiq.getOffset(file, line, 1);
        CharSequence codeLine = file.getText(offset, stopOffset);
        for (int i = 0, fin = codeLine.length(); i < fin; i++) {
            char character = codeLine.charAt(i);
            if (character == '#') {
                return true;
            } else if (Character.isWhitespace(character)) {
                continue;
            } else {
                break;
            }
        }
        return false;
    }
    
    public static int getMacroParametersEndOffset(CsmFile file, CsmMacro macro, int pos) {
        if (macro.getParameters() != null && (!macro.getParameters().isEmpty())) {
            int offset = 0;
            int bracketCount = 0;
            char ch;
            boolean isWhitespace = false;
            do {
                ch = file.getText(pos, ++pos).charAt(0);
                if (Character.isWhitespace(ch)) {
                    offset++;
                    isWhitespace = true;
                } else {
                    isWhitespace = false;
                    switch (ch) {
                        case '(' :
                            bracketCount++;
                            break;
                        case ')' :
                            bracketCount--;
                            break;
                        default :
                            break;
                    }
                    offset++;
                }
            } while (bracketCount > 0 || isWhitespace);
            return offset;
        }
        return 0;
    }
    
    @Override
    public Problem preCheck() {
        Problem preCheckProblem = null;
        fireProgressListenerStart(RenameRefactoring.PRE_CHECK, 6);
        CsmRefactoringUtils.waitParsedAllProjects();
        fireProgressListenerStep();
        CsmCacheManager.enter();
        try {
            if (this.referencedObjects == null) {
                initReferencedObjects();
                fireProgressListenerStep();
            }
            preCheckProblem = isResovledElement(getStartReferenceObject());
            if (preCheckProblem != null) {
                return preCheckProblem;
            }
            return preCheckProblem;
        } finally {
            CsmCacheManager.leave();
        }
    }
    
    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
        if (refactoring.getApplyPlace().equals(InlineRefactoring.Apply.IN_PROJECT)) {
            Collection<? extends CsmObject> objs = getRefactoredObjects();
            if (objs == null || objs.isEmpty()) {
                return Collections.emptySet();
            }
            Collection<CsmFile> files = new HashSet<>();
            CsmFile startFile = getStartCsmFile();
            for (CsmObject obj : objs) {
                Collection<CsmProject> prjs = CsmRefactoringUtils.getRelatedCsmProjects(obj, null);
                CsmProject[] ar = prjs.toArray(new CsmProject[prjs.size()]);
                refactoring.getContext().add(ar);
                files.addAll(getRelevantFiles(startFile, obj, refactoring));
            }
            return files;
        } else {
            Collection<CsmFile> res = new HashSet<>(1);
            res.add(getStartCsmFile());
            return res;
        }
    }
    
    private Collection<CsmObject> getRefactoredObjects() {
        return referencedObjects == null ? Collections.<CsmObject>emptyList() : Collections.unmodifiableCollection(referencedObjects);
    }
    
    private CsmFile getStartCsmFile() {
        CsmFile startFile = CsmRefactoringUtils.getCsmFile(getStartReferenceObject());
        if (startFile == null) {
            if (getEditorContext() != null) {
                startFile = getEditorContext().getFile();
            }
        }
        return startFile;
    }
    
    private void initReferencedObjects() {
        CsmObject primaryObject = CsmRefactoringUtils.getReferencedElement(getStartReferenceObject());
        if (primaryObject != null) {            
            Collection<CsmObject> objects = new HashSet<>();
            objects.add(primaryObject);
            for (CsmInlineExtraObjectProvider provider : Lookup.getDefault().lookupAll(CsmInlineExtraObjectProvider.class)) {
                objects.addAll(provider.getExtraObjects(primaryObject));
            }
            this.referencedObjects = new LinkedHashSet<>();
            for (CsmObject csmObject : objects) {
                referencedObjects.addAll(getEqualObjects(csmObject));
            }
        }
    }
    
}
