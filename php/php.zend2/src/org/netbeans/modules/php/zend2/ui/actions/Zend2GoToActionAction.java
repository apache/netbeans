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
import org.netbeans.modules.php.api.editor.PhpClass;
import org.netbeans.modules.php.api.editor.PhpType;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.zend2.util.Zend2Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

public class Zend2GoToActionAction extends GoToActionAction {

    private static final long serialVersionUID = -5246856421654L;

    private final File file;

    public Zend2GoToActionAction(File file) {
        assert Zend2Utils.isViewWithAction(file);
        this.file = file;
    }

    @Override
    public boolean goToAction() {
        File controller = Zend2Utils.getController(file);
        if (controller != null) {
            FileObject fo = FileUtil.toFileObject(controller);
            if (fo != null) {
                UiUtils.open(fo, getActionMethodOffset(fo));
                return true;
            }
        }
        return false;
    }

    private int getActionMethodOffset(FileObject controller) {
        String actionMethodName = Zend2Utils.getActionName(file);
        EditorSupport editorSupport = Lookup.getDefault().lookup(EditorSupport.class);
        for (PhpClass phpClass : editorSupport.getClasses(controller)) {
            if (phpClass.getName().endsWith(Zend2Utils.CONTROLLER_CLASS_SUFFIX)) {
                if (actionMethodName != null) {
                    for (PhpType.Method method : phpClass.getMethods()) {
                        if (actionMethodName.equals(method.getName())) {
                            return method.getOffset();
                        }
                    }
                }
                return phpClass.getOffset();
            }
        }
        return DEFAULT_OFFSET;
    }

}
