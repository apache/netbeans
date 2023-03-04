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

package org.netbeans.core.ui.warmup;

import java.awt.EventQueue;
import java.util.logging.LogManager;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.openide.actions.ToolsAction;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.windows.OnShowing;
import org.openide.windows.TopComponent;


/**
 * Warm-up task for initing context menu
 *
 * @author  Tomas Pavek, Peter Zavadsky
 */
@OnShowing
public final class ContextMenuWarmUpTask implements Runnable {
    @Override
    public void run() {
        assert EventQueue.isDispatchThread();
        // For first context menu.
        org.openide.actions.ActionManager.getDefault().getContextActions();
        JMenuItem mi = new javax.swing.JMenuItem();
        warmUpToolsPopupMenuItem();
    }

    /** Warms up tools action popup menu item. */
    private static void warmUpToolsPopupMenuItem() {
        SystemAction toolsAction = SystemAction.get(ToolsAction.class);
        if(toolsAction instanceof ContextAwareAction) {
            // Here is important to create proper lookup
            // to warm up Tools sub actions.
            Lookup lookup = new org.openide.util.lookup.ProxyLookup(
                new Lookup[] {
                    // This part of lookup causes warm up of Node (cookie) actions.
                    new AbstractNode(Children.LEAF).getLookup(),
                    // This part of lookup causes warm up of Callback actions.
                    new TopComponent().getLookup()
                }
            );
            
            Action action = ((ContextAwareAction)toolsAction)
                                .createContextAwareInstance(lookup);
            if(action instanceof Presenter.Popup) {
                JMenuItem toolsMenuItem = ((Presenter.Popup)action)
                                                .getPopupPresenter();
                if(toolsMenuItem instanceof Runnable) {
                    // This actually makes the warm up.
                    // See ToolsAction.Popup impl.
                    ((Runnable)toolsMenuItem).run();
                }
            }
        }
    }
}
