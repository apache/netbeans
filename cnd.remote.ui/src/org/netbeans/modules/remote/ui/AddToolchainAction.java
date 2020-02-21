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

package org.netbeans.modules.remote.ui;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.netbeans.modules.cnd.api.toolchain.ui.ToolsPanelSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.openide.util.NbBundle;

/**
 *
 */
/*package*/ final class AddToolchainAction extends AbstractAction {

    private final ExecutionEnvironment execEnv;

    public AddToolchainAction(ExecutionEnvironment execEnv) {
        super(NbBundle.getMessage(AddToolchainAction.class, "AddToolchainMenuItem")); // NOI18N
        this.execEnv = execEnv;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ToolsPanelSupport.invokeNewCompilerSetWizard(execEnv, null);
    }
}
