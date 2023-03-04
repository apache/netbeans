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

import org.netbeans.modules.php.nette2.utils.EditorUtils;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Ondrej Brejla <obrejla@netbeans.org>
 */
public class Nette2PhpModuleActionsExtender extends PhpModuleActionsExtender {

    @Override
    public boolean isViewWithAction(FileObject fileObject) {
        return EditorUtils.isViewWithAction(fileObject);
    }

    @Override
    public boolean isActionWithView(FileObject fileObject) {
        return EditorUtils.isAction(fileObject);
    }

    @Override
    public GoToActionAction getGoToActionAction(FileObject fileObject, int offset) {
        return new Nette2GoToActionAction(fileObject);
    }

    @Override
    public GoToViewAction getGoToViewAction(FileObject fileObject, int offset) {
        return new Nette2GoToViewAction(fileObject, offset);
    }

}
