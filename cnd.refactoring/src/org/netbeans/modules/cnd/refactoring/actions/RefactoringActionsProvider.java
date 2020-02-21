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
package org.netbeans.modules.cnd.refactoring.actions;

import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.modules.cnd.refactoring.ui.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmModelState;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmIncludeHierarchyResolver;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.refactoring.spi.CheckModificationHook;
import org.netbeans.modules.cnd.refactoring.spi.CsmRenameExtraObjectsProvider;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.support.CsmRefactoringUtils;
import org.netbeans.modules.cnd.utils.UIGesturesSupport;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * provides support for refactoring actions
 * 
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.refactoring.spi.ui.ActionsImplementationProvider.class, position = 150)
public class RefactoringActionsProvider extends ActionsImplementationProvider {

    /** Creates a new instance of RefactoringActionsProvider */
    public RefactoringActionsProvider() {
    }

    @Override
    public boolean canFindUsages(Lookup lookup) {
        CsmObject ctx = CsmRefactoringUtils.findContextObject(lookup);
        if (CsmRefactoringUtils.isSupportedReference(ctx)) {
            return true;
        }
        return false;
    }

    private static final String FIND_USAGES_TRACKING = "FIND_USAGES"; // NOI18N
    private static final String RENAME_TRACKING = "RENAME"; // NOI18N
    private static final Result<CsmRenameExtraObjectsProvider> providersResult = Lookup.getDefault().lookupResult(CsmRenameExtraObjectsProvider.class);
    
    @Override
    public void doFindUsages(final Lookup lookup) {
        Runnable task;
        if (isFromEditor(lookup)) {
            task = new TextComponentTask(lookup) {
                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, CsmContext editorContext) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, FIND_USAGES_TRACKING, CsmRefactoringUtils.FROM_EDITOR_TRACKING); // NOI18N
                    return new WhereUsedQueryUI(selectedElement);
                }
            };
        } else {
            task = new NodeToElementTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, FIND_USAGES_TRACKING); // NOI18N
                    return new WhereUsedQueryUI(selectedElement);
                }
            };
        }
        task.run();
    }

    /**
     * returns true if refactorable element is selected
     */
    @Override
    public boolean canRename(Lookup lookup) {
        if (CsmModelAccessor.getModelState() != CsmModelState.ON) {
            return false;
        }
        Set<? extends Node> nodes = new HashSet<>(lookup.lookupAll(Node.class));
        // only one node can be renamed at once or no nodes, but csm object
        if (nodes.size() == 1 || nodes.isEmpty()) {
            CsmObject ctx = CsmRefactoringUtils.findContextObject(lookup);
            if (CsmRefactoringUtils.isSupportedReference(ctx)) {
                if (CsmKindUtilities.isFile(ctx)) {
                    // propose refactoring only for files included somewhere,
                    // so sources will do usual rename without extra dialogs
                    List<CsmFile> allFiles = new ArrayList(lookup.lookupAll(CsmFile.class));
                    if (allFiles.isEmpty()) {
                        allFiles.add((CsmFile)ctx);
                    }
                    boolean included = false;
                    for (CsmFile csmFile : allFiles) {
                        Collection<CsmFile> includers = CsmIncludeHierarchyResolver.getDefault().getFiles(csmFile);
                        included |= !includers.isEmpty();
                        if (!included) {
                            for (CsmRenameExtraObjectsProvider prov : providersResult.allInstances()) {
                                if (prov.needsRefactorRename(csmFile)) {
                                    included = true;
                                    break;
                                }
                            }
                        }
                        if (included) {
                            break;
                        }
                    }
                    return included;
                }
                return true;
            }
        }
        return false;
    }
    
    @Override
    public void doRename(final Lookup lookup) {
        Runnable task;
        if (isFromEditor(lookup)) {
            task = new TextComponentTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement, CsmContext editorContext) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, RENAME_TRACKING, CsmRefactoringUtils.FROM_EDITOR_TRACKING);
                    return new RenameRefactoringUI(selectedElement, null);
                }
            };
        } else {
            task = new NodeToElementTask(lookup) {

                @Override
                protected RefactoringUI createRefactoringUI(CsmObject selectedElement) {
                    UIGesturesSupport.submit(CsmRefactoringUtils.USG_CND_REFACTORING, RENAME_TRACKING);
                    String newName = getName(lookup);
                    return new RenameRefactoringUI(selectedElement, newName);
                }
            };
        }
        task.run();
    }

    /*package*/ static abstract class TextComponentTask implements Runnable {

        private RefactoringUI ui;
        private Lookup lookup;
        private final CsmContext editorContext;
        public TextComponentTask(Lookup lkp) {
            this.lookup = lkp;
            this.editorContext = CsmContext.create(lkp);
        }

        @Override
        public final void run() {
            CsmObject ctx = CsmRefactoringUtils.findContextObject(lookup);
            if (ctx == null && editorContext == null) {
                //inform user, that we were not able to start refactoring.
                Toolkit.getDefaultToolkit().beep();
                return;
            }
            ui = createRefactoringUI(ctx, editorContext);
            if (ui != null) {
                openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRefactorLoc"));
            }
        }

        protected abstract RefactoringUI createRefactoringUI(CsmObject selectedElement, CsmContext editorContext);
    }

    /*package*/ static abstract class NodeToElementTask implements Runnable {

        private Lookup context;
        private RefactoringUI ui;

        public NodeToElementTask(Lookup context) {
            this.context = context;
        }

        public void cancel() {
        }

        @Override
        public final void run() {
            CsmObject ctx = CsmRefactoringUtils.findContextObject(context);
            if (!CsmRefactoringUtils.isSupportedReference(ctx)) {
                return;
            }
            ui = createRefactoringUI(ctx);
            Collection<? extends CheckModificationHook> hooks = context.lookupAll(CheckModificationHook.class);
            for (CheckModificationHook hook : hooks) {
                ui.getRefactoring().getContext().add(hook);
            }

            if (ui != null) {
                openRefactoringUI(ui);
            } else {
                JOptionPane.showMessageDialog(null, NbBundle.getMessage(RefactoringActionsProvider.class, "ERR_CannotRefactorLoc"));
            }
        }

        protected abstract RefactoringUI createRefactoringUI(CsmObject selectedElement);
    }

    static void openRefactoringUI(final RefactoringUI ui) {
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                TopComponent activetc = TopComponent.getRegistry().getActivated();
                UI.openRefactoringUI(ui, activetc);
            }
        });
    }
    
    static boolean isFromEditor(Lookup lookup) {
        EditorCookie ec = lookup.lookup(EditorCookie.class);
        return ec != null && CsmUtilities.findRecentEditorPaneInEQ(ec) != null;
    }
    
    static String getName(Lookup look) {
        ExplorerContext ren = look.lookup(ExplorerContext.class);
        if (ren == null) {
            return null;
        }
        // this name is without extension, because later on it is passed to 
        // general rename refactoring which will rename file
        return ren.getNewName(); //NOI18N
    }
}
