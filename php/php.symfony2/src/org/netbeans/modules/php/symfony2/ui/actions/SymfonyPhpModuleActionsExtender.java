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

import java.util.Arrays;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.spi.framework.PhpModuleActionsExtender;
import org.netbeans.modules.php.spi.framework.actions.GoToActionAction;
import org.netbeans.modules.php.spi.framework.actions.GoToViewAction;
import org.netbeans.modules.php.spi.framework.actions.RunCommandAction;
import org.netbeans.modules.php.symfony2.util.SymfonyUtils;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle.Messages;

/**
 * Symfony 2/3 actions extender.
 */
public final class SymfonyPhpModuleActionsExtender extends PhpModuleActionsExtender {

    private static final List<Action> ACTIONS = Arrays.<Action>asList(
            CacheClearAction.getInstance(),
            CacheWarmupAction.getInstance());

    private final PhpModule phpModule;


    public SymfonyPhpModuleActionsExtender(PhpModule phpModule) {
        assert phpModule != null;
        this.phpModule = phpModule;
    }


    @Messages("SymfonyPhpModuleActionsExtender.menu.name=Symfony")
    @Override
    public String getMenuName() {
        return Bundle.SymfonyPhpModuleActionsExtender_menu_name();
    }

    @Override
    public RunCommandAction getRunCommandAction() {
        return SymfonyRunCommandAction.getInstance();
    }

    @Override
    public List<? extends Action> getActions() {
        return ACTIONS;
    }

    @Override
    public boolean isViewWithAction(FileObject fo) {
        return SymfonyUtils.isViewWithAction(fo);
    }

    @Override
    public boolean isActionWithView(FileObject fo) {
        return SymfonyUtils.isController(fo);
    }

    @Override
    public GoToActionAction getGoToActionAction(FileObject fo, int offset) {
        return new SymfonyGoToActionAction(phpModule, fo);
    }

    @Override
    public GoToViewAction getGoToViewAction(FileObject fo, int offset) {
        return new SymfonyGoToViewAction(phpModule, fo, offset);
    }

}
