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
package org.netbeans.modules.git.ui.repository.remote;

import java.io.File;
import org.netbeans.modules.git.ui.actions.SingleRepositoryAction;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;

/**
 *
 * @author Christian Lenz
 */
@ActionID(id = "org.netbeans.modules.git.ui.repository.remote.AddRemoteAction", category = "Git")
@ActionRegistration(displayName = "#LBL_AddRemoteAction_Name")
public class AddRemoteAction extends SingleRepositoryAction {
    @Override
    protected void performAction(File repository, File[] roots, VCSContext context) {
        new AddRemoteConfig().addRemote(repository);
    }
}
