/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.cnd.api.toolchain.ui;

import org.netbeans.modules.cnd.api.toolchain.ToolsCacheManager;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.modules.cnd.api.remote.ui.ServerListUI;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.Lookup;

/**
 * Two modules - cnd.remote and cnd.core -
 * knows more about displaying server list dialog:
 * they share ToolsCacheManager.
 * That's why we had to extend ServerListDisplayer.
 *
 */
public abstract class ServerListUIEx extends ServerListUI {

    /**
     * Displays server list dialog.
     * Allows to add, remove or modify servers in the list
     * @return true in the case user pressed OK, otherwise
     */
    protected abstract boolean showServerListDialogImpl(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv);

    /** For use as custom editors */
    protected abstract JComponent getServerListComponentImpl(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv);

    public static boolean showServerListDialog(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv) {
        ServerListUI displayer = Lookup.getDefault().lookup(ServerListUI.class);
        if (displayer != null) {
            if (displayer instanceof ServerListUIEx) {
                return ((ServerListUIEx) displayer).showServerListDialogImpl(cacheManager, selectedEnv);
            } else {
                Logger.getLogger("cnd.remote.logger").log( //NOI18N
                        Level.WARNING, "{0}should extend {1}", new Object[]{displayer.getClass().getName(), ServerListUIEx.class.getSimpleName()}); //NOI18N
                return false;
            }
        } else {
            Logger.getLogger("cnd.remote.logger").log( //NOI18N
                    Level.WARNING, "Can not find {0}", ServerListUIEx.class.getSimpleName()); //NOI18N
            return false;
        }
    }

    public static JComponent getServerListComponent(ToolsCacheManager cacheManager, AtomicReference<ExecutionEnvironment> selectedEnv) {
        ServerListUI displayer = Lookup.getDefault().lookup(ServerListUI.class);
        if (displayer != null) {
            if (displayer instanceof ServerListUIEx) {
                return ((ServerListUIEx) displayer).getServerListComponentImpl(cacheManager, selectedEnv);
            } else {
                Logger.getLogger("cnd.remote.logger").log( //NOI18N
                        Level.WARNING, "{0}should extend {1}", new Object[]{displayer.getClass().getName(), ServerListUIEx.class.getSimpleName()}); //NOI18N
                return new JPanel();
            }
        } else {
            Logger.getLogger("cnd.remote.logger").log( //NOI18N
                    Level.WARNING, "Can not find {0}", ServerListUIEx.class.getSimpleName()); //NOI18N
            return new JPanel();
        }
    }

    public static void save(ToolsCacheManager cacheManager, JComponent component) {
        if (component instanceof Save) {
            ((Save) component).save(cacheManager);
        }
    }
}
