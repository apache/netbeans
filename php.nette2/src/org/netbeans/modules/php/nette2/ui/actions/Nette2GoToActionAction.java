/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.nette2.ui.actions;

import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.nette2.utils.Constants;
import org.netbeans.modules.php.nette2.utils.EditorUtils;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class Nette2GoToActionAction extends GoToActionAction {
    private static final int NO_OFFSET = -1;
    private final FileObject fileObject;
    private int methodOffset = NO_OFFSET;

    public Nette2GoToActionAction(FileObject fileObject) {
        this.fileObject = fileObject;
    }

    @Override
    public boolean goToAction() {
        boolean result = false;
        FileObject action = EditorUtils.getAction(fileObject);
        if (action != null) {
            UiUtils.open(action, getActionMethodOffset(action));
            result = true;
        }
        return result;
    }

    private int getActionMethodOffset(FileObject action) {
        String actionMethodName = EditorUtils.getActionName(fileObject);
        String renderMethodName = EditorUtils.getRenderName(fileObject);
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        for (PhpClass phpClass : editorSupport.getClasses(action)) {
            if (phpClass.getName().endsWith(Constants.NETTE_PRESENTER_SUFFIX)) {
                methodOffset(actionMethodName, phpClass);
                methodOffset(renderMethodName, phpClass);

                return methodOffset == NO_OFFSET ? phpClass.getOffset() : methodOffset;
            }
        }
        return DEFAULT_OFFSET;
    }

    private void methodOffset(String methodName, PhpClass phpClass) {
        if (methodName != null && methodOffset == NO_OFFSET) {
            for (PhpClass.Method method : phpClass.getMethods()) {
                if (methodName.equals(method.getName())) {
                    methodOffset = method.getOffset();
                }
            }
        }
    }

}
