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

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.deep.CsmGotoStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmLabel;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.util.CsmBaseUtilities;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceRepository;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceSupport;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.elements.DiffElement;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult;
import org.netbeans.modules.cnd.refactoring.support.ModificationResult.Difference;
import org.netbeans.modules.cnd.utils.FSPath;
import org.netbeans.modules.refactoring.spi.*;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.spi.RefactoringCommit;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * base class for C/C++ refactoring plug-ins
 * 
 */
public abstract class CsmRefactoringPlugin extends ProgressProviderAdapter implements RefactoringPlugin {
    static final Logger LOG = Logger.getLogger(CsmWhereUsedQueryPlugin.class.getName());

    protected volatile boolean cancelRequest = false;

    @Override
    public Problem preCheck() {
        // reset cancelled state to support refresh action of cancelled/stopped refactorings
        cancelRequest = false;
        return null;
    }

    @Override
    public Problem checkParameters() {
        return fastCheckParameters();
    }

    @Override
    public Problem fastCheckParameters() {
        return null;
    }

    @Override
    public final void cancelRequest() {
        cancelRequest = true;
    }

    protected final boolean isCancelled() {
        return cancelRequest;
    }

    protected abstract ModificationResult processFiles(Collection<CsmFile> files, AtomicReference<Problem> outProblem);

    private Collection<ModificationResult> processFiles(Iterable<? extends List<CsmFile>> fileGroups, AtomicReference<Problem> outProblem) {
        Collection<ModificationResult> results = new LinkedList<>();
        for (List<CsmFile> list : fileGroups) {
            if (isCancelled()) {
                // may be return partial "results"?
                return Collections.<ModificationResult>emptyList();
            }
            ModificationResult modification = processFiles(list, outProblem);
            if (modification != null) {
                results.add(modification);
            }
        }
        return results;
    }

    protected final Problem createAndAddElements(Collection<CsmFile> files, RefactoringElementsBag elements, AbstractRefactoring refactoring) {
        Iterable<? extends List<CsmFile>> fileGroups = groupByRoot(files);
        AtomicReference<Problem> outProblem = new AtomicReference<>(null);
        final Collection<ModificationResult> results = processFiles(fileGroups, outProblem);
        final Map<FileObject, Set<Difference>> antiDuplicates = new HashMap<>(1000);
        elements.registerTransaction(new RefactoringCommit(results));
        for (ModificationResult result : results) {
            for (FileObject fo : result.getModifiedFileObjects()) {
                Set<Difference> added = antiDuplicates.get(fo);
                if (added == null) {
                    added = new HashSet<>();
                    antiDuplicates.put(fo, added);
                }
                final Iterator<? extends Difference> differences = result.getDifferences(fo).iterator();
                while (differences.hasNext()) {
                    Difference dif = differences.next();
                    if (!added.contains(dif)) {
                        added.add(dif);
                        elements.add(refactoring, DiffElement.create(dif, fo, result));
                    } else {
                        // # 205913 - IllegalArgumentException: len=-23 < 0
                        LOG.log(Level.INFO, "remove duplicated {0} for {1}", new Object[] {dif, fo});
                        differences.remove();
                    }
                }
            }
        }
        return outProblem.get();
    }

    public static Problem createProblem(Problem prevProblem, boolean isFatal, String message) {
        Problem problem = new Problem(isFatal, message);
        if (prevProblem == null) {
            return problem;
        } else if (isFatal) {
            problem.setNext(prevProblem);
            return problem;
        } else {
            //problem.setNext(result.getNext());
            //result.setNext(problem);

            // [TODO] performance
            Problem p = prevProblem;
            while (p.getNext() != null) {
                p = p.getNext();
            }
            p.setNext(problem);
            return prevProblem;
        }
    }

    private Iterable<? extends List<CsmFile>> groupByRoot(Iterable<? extends CsmFile> files) {
        Map<CsmProject, List<CsmFile>> result = new HashMap<>();
        for (CsmFile file : files) {
            CsmProject prj = file.getProject();
            if (prj != null) {
                List<CsmFile> group = result.get(prj);
                if (group == null) {
                    group = new LinkedList<>();
                    result.put(prj, group);
                }
                group.add(file);
            }
        }
        return result.values();
    }

    protected Collection<CsmFile> getRelevantFiles(CsmFile startFile, CsmObject referencedObject, AbstractRefactoring refactoring) {
        if (CsmKindUtilities.isFile(referencedObject)) {
            return CsmIncludeHierarchyResolver.getDefault().getFiles((CsmFile)referencedObject);
        }
        CsmObject enclScope = referencedObject == null ? null : CsmRefactoringUtils.getEnclosingElement(referencedObject);
        CsmFile scopeFile = null;
        if (enclScope == null && !CsmKindUtilities.isNamespace(referencedObject)) {
            return Collections.<CsmFile>emptyList();
        }
        if (CsmKindUtilities.isFunction(enclScope)) {
            scopeFile = ((CsmOffsetable) enclScope).getContainingFile();
        } else if (CsmKindUtilities.isNamespaceDefinition(enclScope)) {
            CsmNamespace ns = ((CsmNamespaceDefinition) enclScope).getNamespace();
            if (ns != null && ns.getName().length() == 0) {
                // this is unnamed namespace and has file local visibility
                // if declared in source file which is not included anywhere
                if (isDeclarationInLeafFile(enclScope)) {
                    scopeFile = ((CsmNamespaceDefinition) enclScope).getContainingFile();
                }
            }
        } else if (CsmKindUtilities.isFunction(referencedObject)) {
            // this is possible file local function
            // if declared in source file which is not included anywhere
            if (CsmBaseUtilities.isFileLocalFunction((CsmFunction) referencedObject)) {
                if (isDeclarationInLeafFile(referencedObject)) {
                    scopeFile = ((CsmFunction) referencedObject).getContainingFile();
                }
            }
        }
        if (startFile.equals(scopeFile)) {
            return Collections.singleton(scopeFile);
        } else {
            Collection<CsmFile> relevantFiles = new HashSet<>();
            Collection<CsmProject> relevantPrjs = new HashSet<>();
            CsmProject[] prjs = refactoring.getContext().lookup(CsmProject[].class);
            CsmFile declFile = CsmRefactoringUtils.getCsmFile(referencedObject);
            if (prjs == null || prjs.length == 0 || declFile == null) {
                CsmProject prj = startFile.getProject();
                relevantPrjs.add(prj);
            } else {
                Collection<FileObject> toCheck = new HashSet<>();
                for (FileObject curFO : Arrays.asList(declFile.getFileObject(), startFile.getFileObject())) {
                    if (curFO != null) {
                        toCheck.add(curFO);
                    }
                }
                Collection<CsmProject> declProjects = new HashSet<>();
                for (FileObject curFO : toCheck) {
                    CsmFile[] csmFiles = CsmModelAccessor.getModel().findFiles(FSPath.toFSPath(curFO), false, false);
                    for (CsmFile csmFile : csmFiles) {
                        CsmProject declPrj = csmFile.getProject();
                        if (declPrj != null) {
                            declProjects.add(declPrj);
                        }
                    }
                }
                for (CsmProject declPrj : declProjects) {
                    for (CsmProject csmProject : prjs) {
                        // if the same project or declaration from shared library
                        if (csmProject.equals(declPrj) || csmProject.getLibraries().contains(declPrj)) {
                            relevantPrjs.add(csmProject);
                        }
                    }
                }
            }
            if (CndTraceFlags.TEXT_INDEX) {
                CharSequence name = "";
                if (CsmKindUtilities.isNamedElement(referencedObject)) {
                    name = ((CsmNamedElement)referencedObject).getName();
                } else if (CsmKindUtilities.isStatement(referencedObject)) {
                    if (referencedObject instanceof CsmLabel) {
                        name = ((CsmLabel)referencedObject).getLabel();
                    } else if (referencedObject instanceof CsmGotoStatement){
                        name = ((CsmGotoStatement)referencedObject).getLabel();
                    }
                }

                name = CsmRefactoringUtils.getRefactoredName(referencedObject, name.toString());
                final CsmReferenceRepository xRef = CsmReferenceRepository.getDefault();
                relevantFiles.addAll(xRef.findRelevantFiles(relevantPrjs, name));
            } else {
                for (CsmProject csmProject : relevantPrjs) {
                    relevantFiles.addAll(csmProject.getAllFiles());
                }
            }
            return relevantFiles;
        }
    }

    private boolean isDeclarationInLeafFile(CsmObject obj) {
        boolean out = false;
        if (CsmKindUtilities.isOffsetable(obj)) {
            CsmFile file = ((CsmOffsetable) obj).getContainingFile();
            // check that file is not included anywhere yet
            out = CsmIncludeHierarchyResolver.getDefault().getFiles(file).isEmpty();
        }
        return out;
    }

    protected Problem isResovledElement(CsmObject ref) {
        if (ref == null) {
            //reference is null or is not valid.
            return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElNotAvail")); // NOI18N
        } else {
            CsmObject referencedObject = CsmRefactoringUtils.getReferencedElement(ref);
            if (referencedObject == null) {
                return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElementNotResolved"));
            }
            if (!CsmBaseUtilities.isValid(referencedObject)) {
                return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_ElementNotResolved"));
            }
            if (CsmKindUtilities.isFunctionDefinition(referencedObject)) {
                CsmFunction functionDeclaration = CsmBaseUtilities.getFunctionDeclaration((CsmFunction)referencedObject);
                if (functionDeclaration == null) {
                    return new Problem(true, NbBundle.getMessage(CsmRefactoringPlugin.class, "DSC_MethodElementWithoutDeclaration"));
                }
            }
            // element is still available
            return null;
        }
    }

    protected final Collection<? extends CsmObject> getEqualObjects(CsmObject csmObject) {
        if (CsmKindUtilities.isOffsetableDeclaration(csmObject)) {
            CsmOffsetableDeclaration decl = (CsmOffsetableDeclaration) csmObject;
//            CharSequence uniqueName = decl.getUniqueName();
            CsmFile file = decl.getContainingFile();
            if (file != null) {
                FileObject fo = file.getFileObject();
                FSPath fsPath = FSPath.toFSPath(fo);
                CsmFile[] findFiles = CsmModelAccessor.getModel().findFiles(fsPath, false, false);
                Collection<CsmObject> out = new HashSet<>(findFiles.length);
                out.add(csmObject);
                CsmSelect.CsmFilter filter = CsmSelect.getFilterBuilder().createOffsetFilter(decl.getStartOffset()+1);
                for (CsmFile csmFile : findFiles) {
                    if (!file.equals(csmFile)) {
                        Iterator<CsmOffsetableDeclaration> declarations = CsmSelect.getDeclarations(csmFile, filter);
                        while (declarations.hasNext()) {
                            CsmOffsetableDeclaration other = declarations.next();
                            if (CsmReferenceSupport.sameDeclaration(other, decl)) {
                                out.add(other);
                            }
                        }
                    }
                }
                return out;
            }
        }
        return Collections.singleton(csmObject);
    }
}
