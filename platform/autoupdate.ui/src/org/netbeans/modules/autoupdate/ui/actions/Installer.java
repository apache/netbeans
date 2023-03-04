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

package org.netbeans.modules.autoupdate.ui.actions;

import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    public static final RequestProcessor RP = new RequestProcessor("AutoUpdate-UI", 10); // NOI18N
    
    @Override
    public void restored () {
        // don't try to invoke at all in these special cases
        if (Boolean.getBoolean("netbeans.full.hack") || Boolean.getBoolean("netbeans.close")) { // NOI18N
            return;
        }
        AutoupdateCheckScheduler.signOn ();
    }
    
}
