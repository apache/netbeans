/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.php.project.ui.actions;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.text.TextAction;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.FileUtils;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.util.PhpProjectUtils;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.spi.framework.PhpFrameworkProvider;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.ContextAwareAction;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public final class GoToActionOrViewAction extends TextAction implements ContextAwareAction {
    private static final long serialVersionUID = -28799856213215454L;

    private static final GoToActionOrViewAction INSTANCE = new GoToActionOrViewAction();
    private static final int DEFAULT_OFFSET = 0;

    private GoToActionOrViewAction() {
        super(getFullName());
        // copied from BaseAction from php.api module
        putValue("noIconInMenu", true); // NOI18N
        putValue(NAME, getFullName());
        putValue(SHORT_DESCRIPTION, getFullName());
        putValue("menuText", getPureName()); // NOI18N
    }

    public static GoToActionOrViewAction getInstance() {
        return INSTANCE;
    }

    private static String getFullName() {
        return NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_PhpPrefix", getPureName());
    }

    private static String getPureName() {
        return NbBundle.getMessage(GoToActionOrViewAction.class, "LBL_GoToActionOrView");
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        FileObject fo = FileUtils.getFileObject(actionContext);
        return getGoToAction(fo, getOffset(actionContext));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        FileObject fo = NbEditorUtilities.getFileObject(getTextComponent(e).getDocument());
        Action action = getGoToAction(fo, getTextComponent(e).getCaretPosition());
        if (action != null) {
            action.actionPerformed(e);
        }
    }

    private Action getGoToAction(FileObject fo, int offset) {
        if (fo == null) {
            return null;
        }
        PhpProject phpProject = PhpProjectUtils.getPhpProject(fo);
        if (phpProject == null) {
            return null;
        }

        // the 1st wins
        final PhpModule phpModule = phpProject.getPhpModule();
        for (PhpFrameworkProvider frameworkProvider : phpProject.getFrameworks()) {
            PhpModuleActionsExtender actionsExtender = frameworkProvider.getActionsExtender(phpModule);
            if (actionsExtender != null) {
                if (actionsExtender.isActionWithView(fo)) {
                    GoToViewAction goToViewAction = actionsExtender.getGoToViewAction(fo, offset);
                    if (goToViewAction == null) {
                        throw new IllegalStateException(fo.getPath() + " is action with view so GoToView instance must be returned by " + frameworkProvider.getIdentifier());
                    }
                    return goToViewAction;
                } else if (actionsExtender.isViewWithAction(fo)) {
                    GoToActionAction goToActionAction = actionsExtender.getGoToActionAction(fo, offset);
                    if (goToActionAction == null) {
                        throw new IllegalStateException(fo.getPath() + " is view with action so GoToAction instance must be returned by " + frameworkProvider.getIdentifier());
                    }
                    return goToActionAction;
                }
            }
        }
        return null;
    }

    private int getOffset(Lookup context) {
        EditorCookie editorCookie = context.lookup(EditorCookie.class);
        if (editorCookie != null) {
            return getOffset(editorCookie);
        }
        FileObject fo = FileUtils.getFileObject(context);
        if (fo == null) {
            return DEFAULT_OFFSET;
        }
        try {
            editorCookie = DataObject.find(fo).getLookup().lookup(EditorCookie.class);
            return getOffset(editorCookie);
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return DEFAULT_OFFSET;
    }

    private int getOffset(EditorCookie editorCookie) {
        if (editorCookie == null) {
            return DEFAULT_OFFSET;
        }
        JEditorPane[] openedPanes = editorCookie.getOpenedPanes();
        if (openedPanes == null || openedPanes.length == 0) {
            return DEFAULT_OFFSET;
        }
        return openedPanes[0].getCaretPosition();
    }
}
