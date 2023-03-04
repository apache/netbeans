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
package org.netbeans.modules.dlight.terminal.action;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import org.netbeans.modules.dlight.terminal.ui.TerminalContainerTopComponent;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.actions.Presenter;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;

/**
 *
 * @author Vladimir Voskresensky
 */
public abstract class TerminalAction extends AbstractAction implements Presenter.Toolbar {
    
    public static final String TERMINAL_ACTIONS_PATH = "Terminal/Actions"; // NOI18N

    public TerminalAction(String name, String descr, ImageIcon icon) {
        putValue(Action.NAME, name);
        putValue(Action.SHORT_DESCRIPTION, descr);
        putValue(Action.SMALL_ICON, icon);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
        final TerminalContainerTopComponent instance = TerminalContainerTopComponent.findInstance();
        instance.open();
        instance.requestActive();
        final IOContainer ioContainer = instance.getIOContainer();
        final IOProvider term = IOProvider.get("Terminal"); // NOI18N
        if (term != null) {
            final ExecutionEnvironment env = getEnvironment();
            if (env != null) {
                TerminalSupportImpl.openTerminalImpl(ioContainer, env.getDisplayName(), env, null, TerminalContainerTopComponent.SILENT_MODE_COMMAND.equals(e.getActionCommand()), true, 0);
            }
        }
    }

    @Override
    public Component getToolbarPresenter() {
        return TerminalSupportImpl.getToolbarPresenter(this);
    }

    protected abstract ExecutionEnvironment getEnvironment();
}
