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
package org.netbeans.modules.debugger.jpda.ui.actions;

import java.util.Collections;
import java.util.Set;
import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.modules.debugger.jpda.ui.models.WatchesActionsProvider;
import org.netbeans.spi.debugger.ActionsProvider;
import org.netbeans.spi.debugger.ActionsProviderListener;

/**
 * New Watch action provider.
 * 
 * @author Martin Entlicher
 */
@ActionsProvider.Registration(path="netbeans-JPDASession", actions="newWatch")
public class NewWatchActionProvider extends ActionsProvider {

    @Override
    public Set getActions() {
        return Collections.singleton(ActionsManager.ACTION_NEW_WATCH);
    }

    @Override
    public void postAction(Object action, Runnable actionPerformedNotifier) {
        try {
            WatchesActionsProvider.newWatch();
        } finally {
            actionPerformedNotifier.run();
        }
    }
    
    @Override
    public void doAction(Object action) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                WatchesActionsProvider.newWatch();
            }
        });
    }

    @Override
    public boolean isEnabled(Object action) {
        return true;
    }

    @Override
    public void addActionsProviderListener(ActionsProviderListener l) {
    }

    @Override
    public void removeActionsProviderListener(ActionsProviderListener l) {
    }
    
}
