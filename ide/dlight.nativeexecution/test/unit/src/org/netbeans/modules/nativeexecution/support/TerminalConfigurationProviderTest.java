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
package org.netbeans.modules.nativeexecution.support;

import org.junit.Test;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import static org.junit.Assert.*;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminal;
import org.netbeans.modules.nativeexecution.api.util.ExternalTerminalProvider;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.nativeexecution.test.NativeExecutionBaseTestCase;

/**
 *
 * @author ak119685
 */
public class TerminalConfigurationProviderTest extends NativeExecutionBaseTestCase {

    public TerminalConfigurationProviderTest(String name) {
        super(name);
    }

    @Test
    public void testProvider() throws Exception {
        ExecutionEnvironment execEnv = ExecutionEnvironmentFactory.getLocal();
        HostInfo hi = HostInfoUtils.getHostInfo(execEnv);
        String terminal;
        if (hi.getOSFamily() == HostInfo.OSFamily.WINDOWS) {
            terminal = "cmd.exe";
        } else {
            terminal = "xterm";
        }
        ExternalTerminal term = ExternalTerminalProvider.getTerminal(execEnv, terminal);
        assertNotNull(term);
    }
}
