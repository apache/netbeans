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

package org.netbeans.modules.options.classic;

import javax.swing.ActionMap;
import javax.swing.JPanel;
import javax.swing.text.DefaultEditorKit;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;

/** Common panel to be used in core beaninfo and editors.
 */
public class ExplorerPanel extends JPanel implements ExplorerManager.Provider {
    private ExplorerManager manager = new ExplorerManager();

    public ExplorerPanel() {
        manager = new ExplorerManager();
        ActionMap map = getActionMap();
        map.put(DefaultEditorKit.copyAction, ExplorerUtils.actionCopy(manager));
        map.put(DefaultEditorKit.cutAction, ExplorerUtils.actionCut(manager));
        map.put(DefaultEditorKit.pasteAction, ExplorerUtils.actionPaste(manager));
        map.put("delete", ExplorerUtils.actionDelete(manager, true));
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }

    @Override
    public void addNotify() {
        super.addNotify();
        ExplorerUtils.activateActions(manager, true);
    }

    @Override
    public void removeNotify() {
        ExplorerUtils.activateActions(manager, false);
        super.removeNotify();
    }
}

