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
package org.netbeans.modules.parsing.nb;

import org.netbeans.modules.parsing.implspi.TaskProcessorControl;
import org.openide.modules.ModuleInstall;
import org.openide.util.RequestProcessor;
import org.openide.windows.WindowManager;

/**
 * Manages a module's lifecycle. Remember that an installer is optional and
 * often not needed at all.
 */
public class Installer extends ModuleInstall {
    private static volatile boolean closed;

    public static boolean isClosed() {
        return closed;
    }

    @Override
    public void restored () {
        super.restored();
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            @Override
            public void run () {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        TaskProcessorControl.initialize();
                    }
                });
            }
        });
    }

    @Override
    public void close() {
        super.close();
        closed = true;
    }

}
