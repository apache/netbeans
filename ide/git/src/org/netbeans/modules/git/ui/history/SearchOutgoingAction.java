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

package org.netbeans.modules.git.ui.history;

import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

/**
 *
 * @author ondra
 */
@ActionID(id = "org.netbeans.modules.git.ui.history.SearchOutgoingAction", category = "Git")
@ActionRegistration(displayName = "#LBL_SearchOutgoingAction_Name", lazy = false)
@NbBundle.Messages({
    "LBL_SearchOutgoingAction_Name=Show &Outgoing",
    "LBL_SearchOutgoingAction_PopupName=Show Outgoing for Repository"
})
public class SearchOutgoingAction extends SearchOutgoing {

    public SearchOutgoingAction () {
        super(false);
    }

}
