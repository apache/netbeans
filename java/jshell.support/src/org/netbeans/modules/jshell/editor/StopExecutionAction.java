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
package org.netbeans.modules.jshell.editor;

import java.awt.event.ActionEvent;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.EditorActionRegistration;
import org.netbeans.api.progress.BaseProgressUtils;
import org.netbeans.modules.jshell.support.ShellSession;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@EditorActionRegistration(
    mimeType = "text/x-repl", // NOI18N
    name = StopExecutionAction.NAME,
    popupPosition = 20003,
    iconResource= "org/netbeans/modules/jshell/resources/stop.png" // NOI18N
)
@NbBundle.Messages(
        "LBL_AttemptingStop=Attempting to stop Java Shell execution"
)
public class StopExecutionAction extends ShellActionBase {
    public static final String NAME = "jshell-stop";

    public StopExecutionAction() {
        super(NAME);
    }

    @Override
    protected void doPerformAction(ActionEvent evt, JTextComponent target, ShellSession session) {
        BaseProgressUtils.runOffEventDispatchThread(session::stopExecutingCode, 
                    Bundle.LBL_AttemptingStop(), new AtomicBoolean(false), false, 100, 2000);
    }
    
}
