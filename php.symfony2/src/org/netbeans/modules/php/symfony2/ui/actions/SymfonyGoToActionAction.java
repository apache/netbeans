/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.symfony2.ui.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.symfony2.util.SymfonyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

final class SymfonyGoToActionAction extends GoToActionAction {

    private static final Logger LOGGER = Logger.getLogger(SymfonyGoToActionAction.class.getName());

    private final PhpModule phpModule;
    private final FileObject view;


    SymfonyGoToActionAction(PhpModule phpModule, FileObject view) {
        assert phpModule != null;
        assert view != null;
        this.phpModule = phpModule;
        this.view = view;
    }

    @Override
    public boolean goToAction() {
        FileObject sources = phpModule.getSourceDirectory();
        if (sources == null) {
            LOGGER.log(Level.INFO, "No Source Files for project {0}", phpModule.getDisplayName());
            return false;
        }
        FileObject controller = SymfonyUtils.getController(sources, view);
        if (controller != null) {
            UiUtils.open(controller, getActionMethodOffset(controller));
            return true;
        }
        return false;
    }

    private int getActionMethodOffset(FileObject controller) {
        String actionMethodName = SymfonyUtils.getActionMethodName(view.getName());
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        int firstClassOffset = -1;
        for (PhpClass phpClass : editorSupport.getClasses(controller)) {
            if (phpClass.getName().equals(controller.getName())) {
                for (PhpType.Method method : phpClass.getMethods()) {
                    if (actionMethodName.equals(method.getName())) {
                        return method.getOffset();
                    }
                }
                return phpClass.getOffset();
            }
            if (firstClassOffset == -1) {
                firstClassOffset = phpClass.getOffset();
            }
        }
        if (firstClassOffset != -1) {
            return firstClassOffset;
        }
        return DEFAULT_OFFSET;
    }

}
