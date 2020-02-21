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

package org.netbeans.modules.cnd.debugger.dbx.debugging;

import org.netbeans.modules.cnd.debugger.common2.debugger.debugging.DebuggingViewModel;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.viewmodel.TreeModel;

/**
 *
 *
 */
@DebuggerServiceRegistration(path="netbeans-DbxSession/DebuggingView",
                             types={TreeModel.class/*, AsynchronousModelFilter.class*/},
                             position=10000)
public class DbxDebuggingViewModel extends DebuggingViewModel {
    public DbxDebuggingViewModel(ContextProvider lookupProvider) {
        super(lookupProvider);
    }
}
