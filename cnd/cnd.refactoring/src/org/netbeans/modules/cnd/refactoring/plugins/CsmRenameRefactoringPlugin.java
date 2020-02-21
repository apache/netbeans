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
package org.netbeans.modules.cnd.refactoring.plugins;


import org.netbeans.modules.refactoring.api.Problem;
import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.cnd.api.lexer.CndLexerUtilities;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.services.CsmCacheManager;
import org.netbeans.modules.cnd.api.model.services.CsmVirtualInfoQuery;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.*;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.spi.CsmRenameExtraObjectsProvider;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.support.Interrupter;
import org.netbeans.modules.refactoring.api.RenameRefactoring;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * The actual Renaming refactoring work for C/C++. The skeleton (name checks etc.) based
 * on the Java refactoring module.
 * 
 *
 * @todo Complete this. Most of the prechecks are not implemented - and the refactorings themselves need a lot of work.
 */
public class CsmRenameRefactoringPlugin extends CsmModificationRefactoringPlugin {

    private final RenameRefactoring refactoring;
    // objects affected by refactoring
    private Collection<CsmObject> referencedObjects;
    
    /** Creates a new instance of RenameRefactoring */
    public CsmRenameRefactoringPlugin(RenameRefactoring rename) {
        super(rename);
        this.refactoring = rename;
    }

    @Override
    public Problem fastCheckParameters() {
        Problem fastCheckProblem = null;
        String newName = refactoring.getNewName();
        final CsmObject refObj = getStartReferenceObject();
        String oldName = CsmRefactoringUtils.getSimpleText(refObj);
        
        if (oldName.equals(newName)) {
            fastCheckProblem = createProblem(fastCheckProblem, true, getString("ERR_NameNotChanged")); // NOI18N
            return fastCheckProblem;
        }
        
        String errorFmtStr = null;
        if (CsmKindUtilities.isFile(refObj)) {
            if (!checkFileName(newName)) {
                errorFmtStr = getString("ERR_InvalidFileName"); //NOI18N
            }
        } else if (!CndLexerUtilities.isCppIdentifier(newName)) {
            errorFmtStr = getString("ERR_InvalidIdentifier"); //NOI18N
        }
        if (errorFmtStr != null) {
            String msg = new MessageFormat(errorFmtStr).format(new Object[]{newName});
            fastCheckProblem = createProblem(fastCheckProblem, true, msg);
            return fastCheckProblem;
        }
        return fastCheckProblem;
    }
    
    private boolean checkFileName(String str) {
        return !str.contains("\\") && !str.contains("/"); // NOI18N
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
            CsmObject directReferencedObject = CsmRefactoringUtils.getReferencedElement(getStartReferenceObject());
            // check read-only elements
            preCheckProblem = checkIfModificationPossible(preCheckProblem, directReferencedObject);
            fireProgressListenerStop();
            return preCheckProblem;
        } finally {
            CsmCacheManager.leave();
        }
    }

    private static String getString(String key) {
        return NbBundle.getMessage(CsmRenameRefactoringPlugin.class, key);
    }

    private void initReferencedObjects() {
        CsmObject primaryObject = CsmRefactoringUtils.getReferencedElement(getStartReferenceObject());
        if (primaryObject != null) {            
            Collection<CsmObject> objects = new HashSet<>();
            objects.add(primaryObject);
            for (CsmRenameExtraObjectsProvider provider : Lookup.getDefault().lookupAll(CsmRenameExtraObjectsProvider.class)) {
                objects.addAll(provider.getExtraObjects(primaryObject));
            }
            this.referencedObjects = new LinkedHashSet<>();
            Collection<CsmObject> allObjects = new HashSet<>();            
            for (CsmObject csmObject : objects) {
                allObjects.addAll(getEqualObjects(csmObject));
            }
            for (CsmObject referencedObject : allObjects) {
                if (CsmKindUtilities.isClass(referencedObject)) {
                    // for class we need to add all needed elements
                    this.referencedObjects.addAll(getRenamingClassObjects((CsmClass)referencedObject));
                } else if (CsmKindUtilities.isConstructor(referencedObject) || CsmKindUtilities.isDestructor(referencedObject)) {
                    // for constructor/destructor we need to add all needed elements
                    CsmFunction fun = (CsmFunction)referencedObject;
                    CsmClass cls = CsmBaseUtilities.getFunctionClass(fun);
                    if (cls != null) {
                        this.referencedObjects.addAll(getRenamingClassObjects(cls));
                    }
                } else if (CsmKindUtilities.isMethod(referencedObject)) {
                    CsmMethod method = (CsmMethod) CsmBaseUtilities.getFunctionDeclaration((CsmFunction) referencedObject);
                    this.referencedObjects.add(method);
                    if (CsmVirtualInfoQuery.getDefault().isVirtual(method)) {
                        this.referencedObjects.addAll(CsmVirtualInfoQuery.getDefault().getOverriddenMethods(method, true));
                        assert !this.referencedObjects.isEmpty() : "must be at least start object " + method;
                    }
                } else if (CsmKindUtilities.isFile(referencedObject)) {
                    // use all csm files associated with file object
                    CsmFile file = (CsmFile) referencedObject;
                    FileObject fileObject = file.getFileObject();
                    try {
                        DataObject dob = DataObject.find(fileObject);
                        if (dob != null) {
                            CsmFile[] csmFiles = CsmUtilities.getCsmFiles(dob, true, false);
                            this.referencedObjects.addAll(Arrays.asList(csmFiles));
                        }
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                } else {
                    this.referencedObjects.add(referencedObject);
                }
            }
        }
    }

    @Override
    protected Collection<CsmFile> getRefactoredFiles() {
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

    private Collection<CsmObject> getRefactoredObjects() {
        return referencedObjects == null ? Collections.<CsmObject>emptyList() : Collections.unmodifiableCollection(referencedObjects);
    }

    private Collection<? extends CsmObject> getRenamingClassObjects(CsmClass clazz) {
        Collection<CsmObject> out = new ArrayList<>(5);
        if (clazz != null) {
            out.add(clazz);
            for (CsmMember member : clazz.getMembers()) {
                if (CsmKindUtilities.isConstructor(member)) {
                    out.add(member);
                } else if (CsmKindUtilities.isDestructor(member)) {
                    out.add(member);
                }
            }
        }
        return out;
    }

    @Override
    protected final void processFile(CsmFile csmFile, ModificationResult mr, AtomicReference<Problem> outProblem) {
        Collection<? extends CsmObject> refObjects = getRefactoredObjects();
        assert refObjects != null && refObjects.size() > 0 : "method must be called for resolved element";
        FileObject fo = CsmUtilities.getFileObject(csmFile);
        Collection<CsmReference> refs = new LinkedHashSet<>();
        // do not interrupt refactoring
        for (CsmObject obj : refObjects) {
            // if we rename file, check include directives
            if (CsmKindUtilities.isFile(obj)) {
                CsmFile includedFile = (CsmFile) obj;
                Collection<CsmInclude> includes = csmFile.getIncludes();
                for (CsmInclude csmInclude : includes) {
                    if (includedFile.equals(csmInclude.getIncludeFile())) {
                        if (csmInclude.getStartOffset() < 0) {
                            // -inclide directive from item properties
                            // unsupported rename
                            Problem problem = new Problem(false, NbBundle.getMessage(CsmRenameRefactoringPlugin.class, "ERR_IcludedByCompileOption", csmFile.getAbsolutePath(), includedFile.getAbsolutePath())); //NOI18N
                            Problem parent = outProblem.get();
                            if (parent == null) {
                                outProblem.set(problem);
                            } else {
                                while(parent.getNext() != null) {
                                    parent = parent.getNext();
                                }
                                parent.setNext(problem);
                            }
                        } else {
                            refs.add(CsmReferenceSupport.createObjectReference(includedFile, csmInclude));
                        }
                    }
                }
            } else {
                Collection<CsmReference> curRefs = CsmReferenceRepository.getDefault().getReferences(obj, csmFile, CsmReferenceKind.ALL, Interrupter.DUMMY);
                refs.addAll(curRefs);
            }
        }
        Collection<CsmReference> extraRefs = getExtraRenameModificationsInFile(refObjects, csmFile, CsmReferenceKind.ALL);
        refs.addAll(extraRefs); 
        if (!refs.isEmpty()) {
            List<CsmReference> sortedRefs = new ArrayList<>(refs);
            Collections.sort(sortedRefs, new Comparator<CsmReference>() {
                @Override
                public int compare(CsmReference o1, CsmReference o2) {
                    return o1.getStartOffset() - o2.getStartOffset();
                }
            });
            CloneableEditorSupport ces = CsmUtilities.findCloneableEditorSupport(csmFile);
            processRefactoredReferences(sortedRefs, fo, ces, mr);
        }
    }
    
    private void processRefactoredReferences(List<CsmReference> sortedRefs, FileObject fo, CloneableEditorSupport ces, ModificationResult mr) {
        String newName = refactoring.getNewName();
        for (CsmReference ref : sortedRefs) {
            String oldText = ref.getText().toString();
            String newText = CsmRefactoringUtils.getReplaceText(ref, newName, refactoring);
            if (newText != null) {
                String descr = CsmRefactoringUtils.getReplaceDescription(ref, refactoring);
                Difference diff = CsmRefactoringUtils.rename(ref.getStartOffset(), ref.getEndOffset(), ces, oldText, newText, descr);
                assert diff != null;
                mr.addDifference(fo, diff);
            }
        }
    }

    private Collection<CsmReference> getExtraRenameModificationsInFile(Collection<? extends CsmObject> objs, CsmFile csmFile, Set<CsmReferenceKind> kinds) {
        Collection<CsmReference> out = new HashSet<>();
        for (CsmRenameExtraObjectsProvider prov : Lookup.getDefault().lookupAll(CsmRenameExtraObjectsProvider.class)) {
            out.addAll(prov.getExtraFileReferences(objs, csmFile, kinds));
        }
        return out;
    }
}
