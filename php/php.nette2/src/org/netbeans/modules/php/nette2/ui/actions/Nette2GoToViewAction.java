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
package org.netbeans.modules.php.nette2.ui.actions;

import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.nette2.utils.EditorUtils;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
class Nette2GoToViewAction extends GoToViewAction {
    private final FileObject fileObject;
    private final int offset;

    public Nette2GoToViewAction(FileObject fileObject, int offset) {
        this.fileObject = fileObject;
        this.offset = offset;
    }

    @Override
    public boolean goToView() {
        boolean result = false;
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        PhpBaseElement phpElement = editorSupport.getElement(fileObject, offset);
        if (phpElement != null) {
            FileObject view = EditorUtils.getView(fileObject, phpElement);
            if (view != null) {
                UiUtils.open(view, DEFAULT_OFFSET);
                result = true;
            }
        }
        return result;
    }

}
