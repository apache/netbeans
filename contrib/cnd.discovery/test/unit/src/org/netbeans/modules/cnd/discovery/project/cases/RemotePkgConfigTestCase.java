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
package org.netbeans.modules.cnd.discovery.project.cases;

import org.junit.Test;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.api.toolchain.CompilerSetManager;
import org.netbeans.modules.cnd.discovery.project.MakeProjectTestBase;
import org.netbeans.modules.cnd.remote.sync.FtpSyncFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.test.NativeExecutionTestSupport;

/**
 *
 */
public class RemotePkgConfigTestCase extends MakeProjectTestBase {

    private ExecutionEnvironment env;

    public RemotePkgConfigTestCase() {
        super("RemotePkgConfig");
        System.setProperty("cnd.remote.default.sync", FtpSyncFactory.ID);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        env = NativeExecutionTestSupport.getTestExecutionEnvironment("intel-S2");
        if (env == null) {
            System.err.println("REMOTE IS NOT SET UP CORRECTLY. Check ~/.cndtestrc");
            return;
        }
        ConnectionManager.getInstance().connectTo(env);
        ServerRecord record = ServerList.get(env);
        record.setUp();
        ServerList.addServer(record.getExecutionEnvironment(), record.getDisplayName(), record.getSyncFactory(), true, true);
        record.validate(true);
        CompilerSetManager.get(env).initialize(true, true, null);
        CompilerSetManager.get(env).finishInitialization();
    }

    @Override
    protected ExecutionEnvironment getEE() {
        return env;
    }

    @Test
    public void testPkgConfig() throws Exception {
        performTestProject("http://pkgconfig.freedesktop.org/releases/pkg-config-0.25.tar.gz", null, true, "");
    }

}
