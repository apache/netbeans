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
package org.netbeans.modules.cnd.refactoring.introduce;

import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.deep.CsmExpressionStatement;
import org.netbeans.modules.cnd.refactoring.actions.RefactoringKind;
import org.netbeans.modules.cnd.refactoring.api.CsmContext;
import org.netbeans.modules.cnd.refactoring.hints.ExpressionFinder;
import org.netbeans.modules.cnd.refactoring.hints.infrastructure.HintAction;
import org.netbeans.modules.editor.MainMenuAction;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.netbeans.spi.editor.hints.ErrorDescription;
import org.netbeans.spi.editor.hints.Fix;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.actions.Presenter;
import org.openide.windows.TopComponent;

/**
 * based on  org.netbeans.modules.java.hints.introduce.IntroduceAction
 */
public class IntroduceAction extends HintAction {

    private final RefactoringKind type;
    /** Property identifier for menu text, neccessary for display in menu */
    private static final String MENU_TEXT = "menuText"; //NOI18N
    /** Property identifier for popup textm, neccessary for display popup */
    private static final String POPUP_TEXT = "popupText"; //NOI18N

    private IntroduceAction(RefactoringKind type) {
        super(type.getKey());
        this.type = type;
        putValue(NAME, type.getKey());
        String displayText = getMenuItemText(type);
        putValue(SHORT_DESCRIPTION,displayText);
        putValue(POPUP_TEXT,displayText);
        putValue(MENU_TEXT,displayText);
    }

    @Override
    protected void perform(CsmContext context) {
        String error = doPerformAction(context);

        if (error != null) {
            String errorText = NbBundle.getMessage(IntroduceAction.class, error);
            NotifyDescriptor nd = new NotifyDescriptor.Message(errorText, NotifyDescriptor.ERROR_MESSAGE);

            DialogDisplayer.getDefault().notifyLater(nd);
        }
    }

    private String doPerformAction(CsmContext context) {
        final Map<RefactoringKind, Fix> fixes = new EnumMap<>(RefactoringKind.class);
        final Map<RefactoringKind, String> errorMessages = new EnumMap<>(RefactoringKind.class);

        try {
            computeError(context, fixes, errorMessages, new AtomicBoolean());
            Fix fix = fixes.get(type);

            if (fix != null) {
                fix.implement();

                return null;
            }

            String errorMessage = errorMessages.get(type);

            if (errorMessage != null) {
                return errorMessage;
            }

            return "ERR_Invalid_Selection"; //XXX  //NOI18N
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public static IntroduceAction createVariable() {
        return new IntroduceAction(RefactoringKind.CREATE_VARIABLE);
    }

    public static IntroduceAction createConstant() {
        return new IntroduceAction(RefactoringKind.CREATE_CONSTANT);
    }

    public static IntroduceAction createField() {
        return new IntroduceAction(RefactoringKind.CREATE_FIELD);
    }

    public static IntroduceAction createMethod() {
        return new IntroduceAction(RefactoringKind.CREATE_METHOD) {

            @Override
            protected void perform(CsmContext context) {
                final IntroduceMethodUI ui = IntroduceMethodUI.create(null, context);
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        TopComponent activetc = TopComponent.getRegistry().getActivated();
                        UI.openRefactoringUI(ui, activetc);
                    }
                });
            }
        };
    }

    private static String getMenuItemText(RefactoringKind type) {
        switch (type) {
            case CREATE_CONSTANT:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceConstantAction");
            case CREATE_VARIABLE:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction");
            case CREATE_FIELD:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceFieldAction");
            case CREATE_METHOD:
                return NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction");
            default:
                return null;
        }
    }

    private List<ErrorDescription> computeError(CsmContext info, Map<RefactoringKind, Fix> fixesMap, Map<RefactoringKind, String> errorMessage, AtomicBoolean cancel) {
        List<ErrorDescription> hints = new LinkedList<>();
        if (type == RefactoringKind.CREATE_VARIABLE) {
            detectIntroduceVariable(fixesMap, info.getFile(), info.getCaretOffset(), info.getStartOffset(), info.getEndOffset(), info.getDocument(), cancel, info.getFileObject(), info.getComponent());
        }
        return hints;
    }

    private void detectIntroduceVariable(Map<RefactoringKind, Fix> fixesMap, CsmFile file, int caretOffset, int selectionStart, int selectionEnd, final Document doc, final AtomicBoolean canceled, final FileObject fileObject, JTextComponent comp) {
        ExpressionFinder expressionFinder = new ExpressionFinder(doc, file, caretOffset, selectionStart, selectionEnd, canceled);
        ExpressionFinder.StatementResult res = expressionFinder.findExpressionStatement();
        if (res == null) {
            return;
        }
        if (canceled.get()) {
            return;
        }
        CsmExpressionStatement expression = res.getExpression();
        if (expression != null) {
            fixesMap.put(RefactoringKind.CREATE_VARIABLE, new ExtendedAssignmentVariableFix(expression.getExpression(), doc, fileObject));
        }
        if (res.getContainer() != null && res.getStatementInBody() != null && comp != null && selectionStart < selectionEnd) {
            if (/*CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionStart)[0] ==
                  CsmFileInfoQuery.getDefault().getLineColumnByOffset(file, selectionEnd)[0] &&
                */expressionFinder.isExpressionSelection()) {
                if (!(res.getContainer().getStartOffset() == selectionStart &&
                        res.getContainer().getEndOffset() == selectionEnd)) {
                    CsmOffsetable applicableTextExpression = expressionFinder.applicableTextExpression();
                    if (applicableTextExpression != null) {
                        List<Pair<Integer, Integer>> occurrences = res.getOccurrences(applicableTextExpression);
                        fixesMap.put(RefactoringKind.CREATE_VARIABLE, new ExtendedIntroduceVariableFix(res.getStatementInBody(), applicableTextExpression, occurrences, doc, comp, fileObject));
                    }
                }
            }
        }
    }

    public static Object createVariableGlobal() {
        return new GlobalActionImpl(NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceVariableAction"), RefactoringKind.CREATE_VARIABLE.getKey());
    }

    public static Object createMethodGlobal() {
        return new GlobalActionImpl(NbBundle.getMessage(IntroduceAction.class, "CTL_IntroduceMethodAction"), RefactoringKind.CREATE_METHOD.getKey());
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
 
