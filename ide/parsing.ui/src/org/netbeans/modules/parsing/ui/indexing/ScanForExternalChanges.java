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

package org.netbeans.modules.parsing.ui.indexing;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.parsing.impl.indexing.LogContext;
import org.netbeans.modules.parsing.impl.indexing.RepositoryUpdater;
import org.openide.util.NbBundle;

/**
 *
 * @author vita
 */
public final class ScanForExternalChanges extends AbstractAction {

    public ScanForExternalChanges() {
        super(NbBundle.getMessage(ScanForExternalChanges.class, "ScanForExternalChanges_name")); //NOI18N
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //Cannot use IndexingManager API as it needs log
        //the ScanForExternalChanges into special UI logger.
        RepositoryUpdater.getDefault().refreshAll(
                false,
                false,
                true,
                LogContext.create(LogContext.EventType.UI, null));
    }

    @Override
    public boolean isEnabled() {
        return !IndexingManager.getDefault().isIndexing();
    }

    @Override
    public void setEnabled(boolean newValue) {
        // ignore, the action is only enabled when there is no scan in progress
    }
}
