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

import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.zend2.util.Zend2Utils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class Zend2PhpModuleActionsExtender extends PhpModuleActionsExtender {

    @Override
    public boolean isViewWithAction(FileObject fo) {
        return Zend2Utils.isViewWithAction(FileUtil.toFile(fo));
    }

    @Override
    public boolean isActionWithView(FileObject fo) {
        return Zend2Utils.isController(FileUtil.toFile(fo));
    }

    @Override
    public GoToViewAction getGoToViewAction(FileObject fo, int offset) {
        return new Zend2GoToViewAction(FileUtil.toFile(fo), offset);
    }

    @Override
    public GoToActionAction getGoToActionAction(FileObject fo, int offset) {
        return new Zend2GoToActionAction(FileUtil.toFile(fo));
    }

}
