/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.refactoring.java.ui;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.swing.JEditorPane;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.MoveRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.java.api.JavaRefactoringUtils;
import org.netbeans.modules.refactoring.spi.ui.CustomRefactoringPanel;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUIBypass;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.lookup.Lookups;

public class MoveClassUI implements RefactoringUI, RefactoringUIBypass {

    private DataObject javaObject;
    private MoveClassPanel panel;
    private MoveRefactoring refactoring;
    private String targetPkgName = "";
    private boolean disable;
    private final boolean needsByPass;
    private TreePathHandle javaClass;
    private FileObject targetFolder;
    private PasteType pasteType;
    private final String sourceName;

    public MoveClassUI(DataObject javaObject) {
        this(javaObject, null, null, false);
    }

    public MoveClassUI(DataObject javaObject, FileObject targetFolder, PasteType pasteType) {
        this(javaObject, targetFolder, pasteType, false);
    }
    
    public MoveClassUI(TreePathHandle javaClass, String sourceName) {
        this(javaClass, null, null, sourceName);
    }

    public MoveClassUI(DataObject javaObject, FileObject targetFolder, PasteType pasteType, boolean needsByPass) {
        this.needsByPass = needsByPass;
        this.disable = targetFolder != null;
        this.targetFolder = targetFolder;
        this.javaObject = javaObject;
        this.sourceName = javaObject.getName();
        this.pasteType = pasteType;
        this.refactoring = new MoveRefactoring(Lookups.fixed(javaObject.getPrimaryFile()));
        this.refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(javaObject.getPrimaryFile()));
    }

    public MoveClassUI(TreePathHandle javaClass, FileObject targetFolder, PasteType pasteType, String sourceName) {
        this.needsByPass = false;
        this.disable = targetFolder != null;
        this.javaClass = javaClass;
        this.sourceName = sourceName;
        this.targetFolder = targetFolder;
        this.pasteType = pasteType;
        this.refactoring = new MoveRefactoring(Lookups.fixed(javaClass));
        this.refactoring.getContext().add(JavaRefactoringUtils.getClasspathInfoFor(javaClass.getFileObject()));
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(MoveClassUI.class, "LBL_MoveClass");
    }

    @Override
    public String getDescription() {
        return NbBundle.getMessage(MoveClassUI.class, "DSC_MoveClass", sourceName, packageName());
    }

    @Override
    public boolean isQuery() {
        return false;
    }

    @Override
    public CustomRefactoringPanel getPanel(ChangeListener parent) {
        if (panel == null) {
            final String pkgName;
            if (targetFolder != null) {
                pkgName = getPackageName(targetFolder);
            } else if (javaObject != null) {
                pkgName = getPackageName(javaObject.getPrimaryFile().getParent());
            } else {
                pkgName = getPackageName(javaClass.getFileObject().getParent());
            }

            final FileObject target;
            if (targetFolder != null) {
                target = targetFolder;
            } else if (javaObject != null) {
                target = javaObject.getPrimaryFile();
            } else {
                target = javaClass.getFileObject();
            }

            panel = new MoveClassPanel(parent,
                    pkgName,
                    NbBundle.getMessage(MoveClassUI.class, "LBL_MoveClassNamed", sourceName),
                    NbBundle.getMessage(MoveClassUI.class, "LBL_MoveWithoutReferences"),
                    target);

            panel.setCombosEnabled(!disable);
            panel.setRefactoringBypassRequired(needsByPass);
        }
        return panel;
    }

    private static String getPackageName(FileObject file) {
        ClassPath cp = ClassPath.getClassPath(file, ClassPath.SOURCE);
        return cp.getResourceName(file, '.', false);
    }

    private String packageName() {
        return targetPkgName.trim().length() == 0 ? NbBundle.getMessage(MoveClassUI.class, "LBL_DefaultPackage") : targetPkgName.trim();
    }

    private Problem setParameters(boolean checkOnly) {
        if (panel == null) {
            return null;
        }
        targetPkgName = panel.getPackageName();

        URL url = URLMapper.findURL(panel.getRootFolder(), URLMapper.EXTERNAL);
        try {
            TreePathHandle targetClass = panel.getTargetClass();
            if(targetClass != null) {
                refactoring.setTarget(Lookups.singleton(targetClass));
            } else {
                refactoring.setTarget(Lookups.singleton(new URL(url.toExternalForm() + panel.getPackageName().replace('.', '/')))); // NOI18N
            }
        } catch (MalformedURLException ex) {
            Exceptions.printStackTrace(ex);
        }
        if (checkOnly) {
            return refactoring.fastCheckParameters();
        } else {
            return refactoring.checkParameters();
        }
    }

    @Override
    public Problem checkParameters() {
        return setParameters(true);
    }

    @Override
    public Problem setParameters() {
        return setParameters(false);
    }

    @Override
    public AbstractRefactoring getRefactoring() {
        return refactoring;
    }

    @Override
    public boolean hasParameters() {
        return true;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.refactoring.java.ui.MoveClassUI"); // NOI18N
    }

    @Override
    public boolean isRefactoringBypassRequired() {
        return needsByPass || panel != null && panel.isRefactoringBypassRequired();
    }

    @Override
    public void doRefactoringBypass() throws IOException {
        pasteType.paste();
    }

    public static JavaRefactoringUIFactory factory(Lookup lookup) {
        return new RefactoringUIFactory(lookup);
    }

    private static class RefactoringUIFactory implements JavaRefactoringUIFactory {

        private final Lookup lookup;

        public RefactoringUIFactory(Lookup lookup) {
            this.lookup = lookup;
        }

        @Override
        public RefactoringUI create(CompilationInfo info, TreePathHandle[] handles, FileObject[] files, NonRecursiveFolder[] packages) {
            PasteType paste = RefactoringActionsProvider.getPaste(lookup);
            FileObject tar = RefactoringActionsProvider.getTarget(lookup);

            if (files != null && (files.length > 1 ||
                    (files.length == 1 && files[0].isFolder()) ||
                    (files.length == 1 && "package-info".equals(files[0].getName())))) {
                Set<FileObject> s = new HashSet<FileObject>();
                s.addAll(Arrays.asList(files));
                return new MoveClassesUI(s, tar, paste);
            }
            if(handles.length < 1) {
                if(tar != null) {
                    try {
                        assert files.length > 0;
                        return new MoveClassUI(DataObject.find(files[0]), tar, paste, true);
                    } catch (DataObjectNotFoundException ex) {
                        Exceptions.printStackTrace(ex);
                        return null;
                    }
                } else {
                    return null;
                }
            }

            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (ec == null) {
                try {
                    if (files == null) {
                        if (handles.length == 1 && handles[0].getElementHandle() != null && handles[0].getElementHandle().getKind() == ElementKind.CLASS) {
                            CompilationUnitTree compilationUnit = handles[0].resolve(info).getCompilationUnit();
                            if(compilationUnit.getTypeDecls().size() == 1) {
                                return new MoveClassUI(DataObject.find(handles[0].getFileObject()), tar, paste);
                            } else {
                                return new MoveClassUI(handles[0], tar, paste, handles[0].resolveElement(info).getSimpleName().toString());
                            }
                        }
                        return new MoveMembersUI(handles);
                    } else {
                        return new MoveClassUI(DataObject.find(files[0]), tar, paste);
                    }
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            TreePathHandle selectedElement = handles[0];
            TreePath enclosingClassPath = JavaRefactoringUtils.findEnclosingClass(info, selectedElement.resolve(info), true, true, true, true, true);
            Element e = info.getTrees().getElement(enclosingClassPath);
            if (e == null) {
                return null;
            }
            JEditorPane textC = NbDocument.findRecentEditorPane(ec);;
            if (textC == null) {
                try {
                    return new MoveClassUI(DataObject.find(files[0]), tar, paste);
                } catch (DataObjectNotFoundException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return null;
            }
            int startOffset = textC.getSelectionStart();
            int endOffset = textC.getSelectionEnd();
            if (startOffset == endOffset) {
                return doCursorPosition(info, selectedElement, startOffset);
            } else {
                if (!(e.getKind().isClass() || e.getKind().isInterface())) {
                    e = info.getElementUtilities().enclosingTypeElement(e);
                }
                Collection<TreePathHandle> tphs = new ArrayList<TreePathHandle>();
                SourcePositions sourcePositions = info.getTrees().getSourcePositions();
                for (Element ele : e.getEnclosedElements()) {
                    Tree leaf = info.getTrees().getPath(ele).getLeaf();
                    long start = sourcePositions.getStartPosition(info.getCompilationUnit(), leaf);
                    long end = sourcePositions.getEndPosition(info.getCompilationUnit(), leaf);
                    if ((start >= startOffset && start <= endOffset)
                            || (end >= startOffset && end <= endOffset)) {
                        tphs.add(TreePathHandle.create(ele, info));
                    }
                }
                if (tphs.isEmpty()) {
                    return doCursorPosition(info, selectedElement, startOffset);
                }
                return new MoveMembersUI(tphs.toArray(new TreePathHandle[tphs.size()]));
            }
        }

        private RefactoringUI doCursorPosition(CompilationInfo info, TreePathHandle selectedElement, int position) throws RuntimeException {
            List<? extends TypeElement> topLevelElements = info.getTopLevelElements();
            Trees trees = info.getTrees();
            SourcePositions sourcePositions = trees.getSourcePositions();
            CompilationUnitTree compilationUnit = info.getCompilationUnit();

            for (TypeElement typeElement : topLevelElements) {
                ClassTree topLevelClass = trees.getTree(typeElement);
                long startPosition = sourcePositions.getStartPosition(compilationUnit, topLevelClass);
                long endPosition = sourcePositions.getEndPosition(compilationUnit, topLevelClass);
                if (position > startPosition && position < endPosition) {
                    for (Element element : typeElement.getEnclosedElements()) {
                        /* We need to go through all members to see if the position
                         * is on a member, if we try to get the element from the
                         * TreePath we could get the type of the member instead
                         * of the member itself.*/
                        Tree member = trees.getTree(element);
                        long startMember = sourcePositions.getStartPosition(compilationUnit, member);
                        long endMember = sourcePositions.getEndPosition(compilationUnit, member);
                        if (position > startMember && position < endMember) {
                            TreePathHandle tph = TreePathHandle.create(element, info);
                            return new MoveMembersUI(tph);
                        }
                    }
                    try {
                        if(topLevelElements.size() == 1) {
                            return new MoveClassUI(DataObject.find(info.getFileObject()));
                        } else {
                            return new MoveClassUI(TreePathHandle.create(typeElement, info), typeElement.getSimpleName().toString());
                        }
                    } catch (DataObjectNotFoundException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
//                    if (selectedElement.resolve(info).getLeaf().getKind() == Tree.Kind.COMPILATION_UNIT) {
            try {
                return new MoveClassUI(DataObject.find(info.getFileObject()));
            } catch (DataObjectNotFoundException ex) {
                throw new RuntimeException(ex);
            }
//                    }
        }
    }
}
