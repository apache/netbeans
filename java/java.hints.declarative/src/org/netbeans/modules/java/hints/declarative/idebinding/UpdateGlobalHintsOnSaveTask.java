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
package org.netbeans.modules.java.hints.declarative.idebinding;

import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.java.hints.declarative.DeclarativeHintTokenId;
import org.netbeans.modules.java.hints.spiimpl.RulesManager;
import org.netbeans.spi.editor.document.OnSaveTask;
import org.netbeans.spi.editor.document.OnSaveTask.Factory;
import org.openide.util.RequestProcessor;

/**
 * Reloads the global hint if the file changed after a save action.
 * @author mbien
 */
@MimeRegistration(mimeType=DeclarativeHintTokenId.MIME_TYPE, service=OnSaveTask.Factory.class, position = 3000)
public final class UpdateGlobalHintsOnSaveTask implements Factory {

    @Override
    public OnSaveTask createTask(OnSaveTask.Context context) {

        return new OnSaveTask() {

            private RequestProcessor.Task delay;

            @Override
            public void performTask() {
                // stolen from org.netbeans.modules.java.hints.spiimpl.options.HintsPanel#saveButtonActionPerformed
                // must be on EDT, reloads global hints from hint files
                // todo: this reloads *all* global hints, which isn't optimal
                SwingUtilities.invokeLater(() -> RulesManager.getInstance().reload());
            }

            @Override
            public void runLocked(Runnable task) {
                // the file isn't actually saved yet, so we delay the task a bit
                // it seems to be working fine even without a delay due to the timing of the EDT
                delay = RequestProcessor.getDefault().post(task, 200);
            }

            @Override
            public boolean cancel() {
                return delay.cancel();
            }
        };
    }

}
