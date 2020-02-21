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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.refactoring.actions;

import java.util.Collection;
import java.util.HashSet;
import javax.swing.text.Document;
import org.netbeans.api.editor.EditorRegistry;
import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.api.model.xref.CsmReferenceResolver;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.Utilities;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.spi.CsmActionsImplementationProvider;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.refactoring.ui.ChangeParametersUI;
import org.netbeans.modules.cnd.refactoring.ui.EncapsulateFieldUI;
import org.netbeans.modules.cnd.refactoring.ui.InlineUI;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.nodes.Node;
import org.openide.util.Lookup;

/**
 * C++ specific actions provider (based on org.netbeans.modules.refactoring.java.ui.JavaRefactoringActionsProvider)
 *
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.refactoring.spi.CsmActionsImplementationProvider.class, position=100)
public class CsmRefactoringActionsProvider extends CsmActionsImplementationProvider {
    private static final String CHANGE_PARAMETERS_TRACKING = "CHANGE_PARAMETERS"; // NOI18N
    private static final String ENCAPSULATE_FIELDS_TRACKING = "ENCAPSULATE_FIELDS"; // NOI18N
    private static final String INLINE_TRACKING = "INLINE_REFACTORING"; // NOI18N
    
    public CsmRefactoringActionsProvider() {
    }
    
    @Override
    public boolean canChangeParameters(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1) {
            return false;
        }
        CsmObject ref = CsmRefactoringUtils.findContextObject(lookup);
        if (RefactoringActionsProvider.isFromEditor(lookup)) {
            // if inside function but not in destructor => allow to change it's parameters
            CsmContext editorContext = CsmContext.create(lookup);
            if (editorContext != null) {
                CsmFunction fun = editorContext.getEnclosingFunction();
                return fun != null;
            }
            return false;
        } else {
            return CsmKindUtilities.isFunction(ref);
        }
    }

    @Override
    public void doChangeParameters(Lookup lookup) {
        Runnable task;
        if (RefactoringActionsProvider.isFromEditor(lookup)) {
            task = new RefactoringActionsProvider.TextComponentTask(lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, CsmContext editorContext) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CHANGE_PARAMETERS_TRACKING, CsmRefactoringUtils.FROM_EDITOR_TRACKING);
                    return ChangeParametersUI.create(selectedElement, editorContext);
                }
            };
        } else {
            task = new RefactoringActionsProvider.NodeToElementTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, CHANGE_PARAMETERS_TRACKING);
                    return ChangeParametersUI.create(selectedElement, null);
                }
            };
        }
        task.run();
    }
    
    @Override
    public boolean canEncapsulateFields(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<>(lookup.lookupAll(Node.class));
        if (nodes.size() != 1) {
            return false;
        }
        CsmObject ref = CsmRefactoringUtils.findContextObject(lookup);
        if (RefactoringActionsProvider.isFromEditor(lookup)) {
            // if inside class => allow to encapsulate fields
            CsmContext editorContext = CsmContext.create(lookup);
            if (editorContext != null) {
                CsmClass cls = Utilities.extractEnclosingClass(editorContext);
                return cls != null;
            }
            return false;
        } else {
            return CsmKindUtilities.isField(ref) || CsmKindUtilities.isClass(ref);
        }
    }

    @Override
    public void doEncapsulateFields(Lookup lookup) {
        Runnable task;
        if (RefactoringActionsProvider.isFromEditor(lookup)) {
            task = new RefactoringActionsProvider.TextComponentTask(lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, CsmContext editorContext) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, ENCAPSULATE_FIELDS_TRACKING, CsmRefactoringUtils.FROM_EDITOR_TRACKING);
                    return EncapsulateFieldUI.create(selectedElement, editorContext);
                }
            };
        } else {
            task = new RefactoringActionsProvider.NodeToElementTask(lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, ENCAPSULATE_FIELDS_TRACKING);
                    return EncapsulateFieldUI.create(selectedElement, null);
                }
            };
        }
        task.run();
    }
    
    @Override
    public boolean canPerformInlineRefactoring(Lookup lookup) {
        Collection<? extends Node> nodes = new HashSet<>(lookup.lookupAll(Node.class));
        if(nodes.size() != 1) {
            return false;
        }
        CsmObject obj = CsmRefactoringUtils.findContextObject(lookup);
        if (CsmKindUtilities.isOffsetable(obj)) {
            Document doc = EditorRegistry.lastFocusedComponent().getDocument();
            CsmReference ref = CsmReferenceResolver.getDefault().findReference(doc, ((CsmOffsetable) obj).getStartOffset());
            if (CsmKindUtilities.isMacro(ref.getReferencedObject())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void doInlineRefactoring(Lookup lookup) {
        Runnable task;
        if (RefactoringActionsProvider.isFromEditor(lookup)) {
            task = new RefactoringActionsProvider.TextComponentTask(lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, CsmContext editorContext) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, INLINE_TRACKING, CsmRefactoringUtils.FROM_EDITOR_TRACKING);
                    return InlineUI.create(selectedElement, editorContext);
                }
            };
        } else {
            task = new RefactoringActionsProvider.NodeToElementTask(lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, INLINE_TRACKING);
                    return InlineUI.create(selectedElement, null);
                }
            };
        }
        task.run();
    }
    
//    private static TreePathHandle findSelectedClassMemberDeclaration(TreePathHandle path, final CompilationInfo info) {
//        TreePath resolved = path.resolve(info);
//        TreePath selected = findSelectedClassMemberDeclaration(resolved ,info);
//        if (selected == null) {
//            path = null;
//        } else if (selected != resolved) {
//            path = TreePathHandle.create(selected, info);
//        }
//        return path;
//    }
//
//    private static TreePath findSelectedClassMemberDeclaration(final TreePath path, final CompilationInfo javac) {
//        TreePath currentPath = path;
//        TreePath selection = null;
//        while (currentPath != null && selection == null) {
//            switch (currentPath.getLeaf().getKind()) {
//                case CLASS:
//                case NEW_CLASS:
//                case METHOD:
//                    selection = currentPath;
//                    break;
//                case VARIABLE:
//                    Element elm = javac.getTrees().getElement(currentPath);
//                    if (elm != null && elm.getKind().isField()) {
//                        selection = currentPath;
//                    }
//                    break;
//            }
//            if (selection != null && javac.getTreeUtilities().isSynthetic(selection)) {
//                selection = null;
//            }
//            if (selection == null) {
//                currentPath = currentPath.getParentPath();
//            }
//        }
//
//        if (selection == null && path != null) {
//            List<? extends Tree> typeDecls = path.getCompilationUnit().getTypeDecls();
//            if (!typeDecls.isEmpty()) {
//                selection = TreePath.getPath(path.getCompilationUnit(), typeDecls.get(0));
//            }
//        }
//        return selection;
//    }
    
}
