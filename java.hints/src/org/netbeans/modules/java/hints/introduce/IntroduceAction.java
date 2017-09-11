/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2007-2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.hints.introduce;

import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import org.netbeans.api.java.lexer.JavaTokenId;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.progress.ProgressUtils;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.java.editor.overridden.PopupUtil;
import org.netbeans.modules.java.hints.infrastructure.HintAction;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

public final class IntroduceAction extends HintAction {
    
    private final IntroduceKind type;

    /** Property identifier for menu text, neccessary for display in menu */
    private static final String MENU_TEXT = "menuText"; //NOI18N
    /** Property identifier for popup textm, neccessary for display popup */
    private static final String POPUP_TEXT = "popupText"; //NOI18N
    

    private IntroduceAction(IntroduceKind type) {
        super(type.getKey());
        this.type = type;
        switch (type) {
            case CREATE_CONSTANT:
                putValue(NAME, type.getKey());
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"));
                break;
            case CREATE_VARIABLE:
                putValue(NAME, type.getKey());
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"));
                break;
            case CREATE_FIELD:
                putValue(NAME, type.getKey());
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"));
                break;
            case CREATE_METHOD:
                putValue(NAME, type.getKey());
                putValue(SHORT_DESCRIPTION, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                putValue(MENU_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                putValue(POPUP_TEXT, NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"));
                break;
        }

        setEnabled(true);
    }

    protected void perform(JavaSource js, JTextComponent pane, int[] selection) {
        String error = doPerformAction(js, pane, selection);
        
        if (error != null) {
            String errorText = NbBundle.getMessage(IntroduceAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);
            
            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }
    
    private String doPerformAction(final JavaSource js, JTextComponent pane, final int[] span) {
        final List<Candidate> candidates = new ArrayList<Candidate>();
        final AtomicBoolean cancel = new AtomicBoolean();
        final String[] errorMessage = new String[1];
        final boolean proposeCandidates = span[0] == span[1] && type != IntroduceKind.CREATE_METHOD;
        
        ProgressUtils.runOffEventDispatchThread(new Runnable() {

            public void run() {
                try {
                    js.runUserActionTask(new Task<CompilationController>() {

                        public void run(CompilationController parameter) throws Exception {
                            parameter.toPhase(Phase.RESOLVED);
                            if (cancel.get()) {
                                return;
                            }
                            if (proposeCandidates) {
                                TreePath tp = pathFor(parameter, span[0]);
                                Set<Tree> seenTrees = Collections.newSetFromMap(new IdentityHashMap<Tree, Boolean>());

                                while (tp != null) {
                                    TreePath currentPath = tp;

                                    tp = tp.getParentPath();

                                    if (currentPath.getLeaf().getKind() == Kind.PARENTHESIZED) {
                                        currentPath = new TreePath(currentPath, ((ParenthesizedTree) currentPath.getLeaf()).getExpression());
                                    }

                                    if (!seenTrees.add(currentPath.getLeaf())) continue;
                                    
                                    Map<IntroduceKind, Fix> fixes = new EnumMap<IntroduceKind, Fix>(IntroduceKind.class);
                                    Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);
                                    int start = (int) parameter.getTrees().getSourcePositions().getStartPosition(parameter.getCompilationUnit(), currentPath.getLeaf());
                                    int end   = (int) parameter.getTrees().getSourcePositions().getEndPosition(parameter.getCompilationUnit(), currentPath.getLeaf());

                                    if (end > start && start != (-1)) {
                                        IntroduceHint.computeError(parameter, start, end, fixes, errorMessages, cancel);

                                        Fix f = fixes.get(type);

                                        if (f != null) {
                                            candidates.add(new Candidate(parameter.getText().substring(start, end), start, end, f));
                                        }
                                    }

                                }
                            } else {
                                Map<IntroduceKind, Fix> fixes = new EnumMap<IntroduceKind, Fix>(IntroduceKind.class);
                                Map<IntroduceKind, String> errorMessages = new EnumMap<IntroduceKind, String>(IntroduceKind.class);

                                IntroduceHint.computeError(parameter, span[0], span[1], fixes, errorMessages, cancel);

                                Fix f = fixes.get(type);
                                
                                if (f != null) {
                                    candidates.add(new Candidate(null, -1, -1, f));
                                } else {
                                    errorMessage[0] = errorMessages.get(type);
                                }
                            }
                        }
                    }, true);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }, NbBundle.getMessage(IntroduceAction.class, "LBL_Indroduce_Action"), cancel, false);

        if (cancel.get()) {
            return null;
        }

        Fix fix;

        if (proposeCandidates) {
            if (!candidates.isEmpty()) {
                Point l = new Point(-1, -1);

                try {
                    Rectangle pos = pane.modelToView(pane.getCaretPosition());
                    l = new Point(pos.x + pos.width, pos.y + pos.height);
                    SwingUtilities.convertPointToScreen(l, pane);
                } catch (BadLocationException ex) {
                    Exceptions.printStackTrace(ex);
                }

                String label = NbBundle.getMessage(IntroduceAction.class, "LBL_PickExpression");

                PopupUtil.showPopup(new MethodCandidateChooser(label, candidates, pane.getDocument()), label, l.x, l.y, true, -1);
                return null;
            } else {
                return "ERR_No_Valid_Expressions_Found";
            }
        } else {
            fix = candidates.isEmpty() ? null : candidates.get(0).fix;
        }
        
        if (fix != null) {
            try {
                fix.implement();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }

            return null;
        }

        if (errorMessage[0] != null) {
            return errorMessage[0];
        }

        return "ERR_Invalid_Selection"; //XXX  //NOI18N
    }

    static final class Candidate {
        final String displayName;
        final int start;
        final int end;
        final Fix fix;

        Candidate(String displayName, int start, int end, Fix fix) {
            this.displayName = displayName;
            this.start = start;
            this.end = end;
            this.fix = fix;
        }
    }

    @Override
    protected boolean requiresSelection() {
        return false;
    }

    private static TreePath pathFor(CompilationInfo info, int pos) {
        TokenSequence<JavaTokenId> ts = info.getTokenHierarchy().tokenSequence(JavaTokenId.language());

        ts.move(info.getSnapshot().getEmbeddedOffset(pos));

        if (ts.moveNext() && ts.token().id() == JavaTokenId.IDENTIFIER) {
            pos = ts.offset() + 1;
        }

        return info.getTreeUtilities().pathFor(pos);
    }

    public static IntroduceAction createVariable() {
        return new IntroduceAction(IntroduceKind.CREATE_VARIABLE);
    }
    
    public static IntroduceAction createConstant() {
        return new IntroduceAction(IntroduceKind.CREATE_CONSTANT);
    }
    
    public static IntroduceAction createField() {
        return new IntroduceAction(IntroduceKind.CREATE_FIELD);
    }

    public static IntroduceAction createMethod() {
        return new IntroduceAction(IntroduceKind.CREATE_METHOD);
    }

    public static Object createVariableGlobal() {
        return new GlobalActionImpl(NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"), IntroduceKind.CREATE_VARIABLE.getKey());
    }

    public static Object createConstantGlobal() {
        return new GlobalActionImpl(NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction"), IntroduceKind.CREATE_CONSTANT.getKey());
    }

    public static Object createFieldGlobal() {
        return new GlobalActionImpl(NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction"), IntroduceKind.CREATE_FIELD.getKey());
    }

    public static Object createMethodGlobal() {
        return new GlobalActionImpl(NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"), IntroduceKind.CREATE_METHOD.getKey());
    }

    private static final class GlobalActionImpl extends MainMenuAction implements Presenter.Popup {

        private final String menuItemText;
        private final String actionName;

        public GlobalActionImpl(String menu, String action) {
            this.menuItemText = menu;
            this.actionName = action;
            setMenu();
        }

        @Override
        protected String getMenuItemText() {
            return menuItemText;
        }

        @Override
        protected String getActionName() {
            return actionName;
        }

        @Override
        public JMenuItem getPopupPresenter() {
            return getMenuPresenter();
        }

    }

}
