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
