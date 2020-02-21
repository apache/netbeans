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

package org.netbeans.modules.remote.ui.spi;

import java.io.IOException;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.modules.remote.api.RemoteException;
import org.netbeans.modules.remote.api.ServerRecord;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;

/**
 *
 */
public interface ServerSetupProvider {

    /**
     * Checks whether the record is correctly set up.
     * Fast method. Can be run in UI thread.
     * @param record record to check setup for
     * @param message a reference to the message that should be displayed in the case the record is NOT set up
     * @return true if the record is set up, otherwise false
     * TODO: should it throw?
     */
    public boolean checkSetUp(ServerRecord record, AtomicReference<String> message);

    public void repairSetUp(ServerRecord record) throws RemoteException, IOException, CancellationException;

    public ServerSetupWorker createSetupWorker(ExecutionEnvironment env);
}
