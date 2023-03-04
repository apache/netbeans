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
package org.netbeans.modules.php.dbgp;

import java.util.concurrent.Callable;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.spi.executable.DebugStarter;
import org.openide.util.Cancellable;

/**
 * @author Radek Matous
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.netbeans.modules.php.spi.executable.DebugStarter.class)
public class DebuggerImpl implements DebugStarter {
    static final String SESSION_ID = "netbeans-PHP-DBGP-Session"; // NOI18N
    static final String ENGINE_ID = SESSION_ID + "/" + "PHP-Engine"; // NOI18N

    @Override
    public void start(Project project, Callable<Cancellable> run, DebugStarter.Properties properties) {
        SessionManager.getInstance().startNewSession(project, run, properties);
    }

    @Override
    public void stop() {
        SessionManager.getInstance().stopCurrentSession(true);
    }

    @Override
    public boolean isAlreadyRunning() {
        return SessionManager.getInstance().isAlreadyRunning();
    }

}
