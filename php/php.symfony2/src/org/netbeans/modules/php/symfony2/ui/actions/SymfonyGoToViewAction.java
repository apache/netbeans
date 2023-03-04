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
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.symfony2.preferences.SymfonyPreferences;
import org.netbeans.modules.php.symfony2.util.SymfonyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

final class SymfonyGoToViewAction extends GoToViewAction {

    private static final Logger LOGGER = Logger.getLogger(SymfonyGoToViewAction.class.getName());

    private final PhpModule phpModule;
    private final FileObject controller;
    private final int offset;


    SymfonyGoToViewAction(PhpModule phpModule, FileObject controller, int offset) {
        assert phpModule != null;
        assert controller != null;
        this.phpModule = phpModule;
        this.controller = controller;
        this.offset = offset;
    }

    @Override
    public boolean goToView() {
        FileObject sources = phpModule.getSourceDirectory();
        if (sources == null) {
            LOGGER.log(Level.INFO, "No Source Files for project {0}", phpModule.getDisplayName());
            return false;
        }
        FileObject appDir = sources.getFileObject(SymfonyPreferences.getAppDir(phpModule));
        if (appDir == null) {
            LOGGER.log(Level.INFO, "No App dir for project {0}", phpModule.getDisplayName());
            return false;
        }
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        PhpBaseElement phpElement = editorSupport.getElement(controller, offset);
        if (phpElement == null) {
            return false;
        }
        FileObject view = SymfonyUtils.getView(controller, appDir, phpElement);
        if (view != null) {
            UiUtils.open(view, DEFAULT_OFFSET);
            return true;
        }
        return false;
    }

}
