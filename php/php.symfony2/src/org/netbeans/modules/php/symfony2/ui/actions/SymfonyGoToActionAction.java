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
