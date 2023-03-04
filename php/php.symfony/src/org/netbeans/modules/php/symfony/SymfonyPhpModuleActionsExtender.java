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

package org.netbeans.modules.php.symfony;

import java.util.Collections;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.netbeans.modules.php.symfony.ui.actions.ClearCacheAction;
import org.netbeans.modules.php.symfony.ui.actions.SymfonyGoToActionAction;
import org.netbeans.modules.php.symfony.ui.actions.SymfonyGoToViewAction;
import org.netbeans.modules.php.symfony.ui.actions.SymfonyRunCommandAction;
import org.netbeans.modules.php.symfony.util.SymfonyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

/**
 * @author Tomas Mysik
 */
public class SymfonyPhpModuleActionsExtender extends PhpModuleActionsExtender {
    private static final List<Action> ACTIONS = Collections.<Action>singletonList(ClearCacheAction.getInstance());

    @Override
    public String getMenuName() {
        return NbBundle.getMessage(SymfonyPhpModuleActionsExtender.class, "LBL_MenuName");
    }

    @Override
    public List<? extends Action> getActions() {
        return ACTIONS;
    }

    @Override
    public RunCommandAction getRunCommandAction() {
        return SymfonyRunCommandAction.getInstance();
    }

    @Override
    public boolean isViewWithAction(FileObject fo) {
        return SymfonyUtils.isViewWithAction(fo);
    }

    @Override
    public boolean isActionWithView(FileObject fo) {
        return SymfonyUtils.isAction(fo);
    }

    @Override
    public GoToActionAction getGoToActionAction(FileObject fo, int offset) {
        return new SymfonyGoToActionAction(fo);
    }

    @Override
    public GoToViewAction getGoToViewAction(FileObject fo, int offset) {
        return new SymfonyGoToViewAction(fo, offset);
    }
}
