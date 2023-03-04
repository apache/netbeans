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
package org.netbeans.modules.php.zend2.ui.actions;

import java.io.File;
import org.netbeans.modules.csl.api.UiUtils;
import org.netbeans.modules.php.api.editor.EditorSupport;
import org.netbeans.modules.php.api.editor.PhpBaseElement;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.zend2.util.Zend2Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class Zend2GoToViewAction extends GoToViewAction {

    private static final long serialVersionUID = -657646571321L;

    private final File file;
    private final int offset;


    public Zend2GoToViewAction(File file, int offset) {
        assert Zend2Utils.isController(file);
        this.file = file;
        this.offset = offset;
    }

    @Override
    public boolean goToView() {
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        PhpBaseElement phpElement = editorSupport.getElement(FileUtil.toFileObject(file), offset);
        if (phpElement == null) {
            return false;
        }
        File view = Zend2Utils.getView(file, phpElement);
        if (view != null) {
            FileObject fo = FileUtil.toFileObject(view);
            if (fo != null) {
                UiUtils.open(fo, DEFAULT_OFFSET);
                return true;
            }
        }
        return false;
    }
}
