/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
