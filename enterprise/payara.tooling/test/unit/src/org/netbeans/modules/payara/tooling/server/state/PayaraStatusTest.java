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
package org.netbeans.modules.payara.tooling.server.state;

import java.util.Properties;
import org.netbeans.modules.payara.tooling.CommonTest;
import org.netbeans.modules.payara.tooling.admin.CommandHttpTest;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.testng.annotations.Test;

/**
 * Test Payara server status check.
 * <p/>
 * @author Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class PayaraStatusTest extends CommonTest {

    @Test
    public void testPayaraStatusOffline() {
        Properties properties = jdkProperties();
        PayaraServer server = CommandHttpTest.payaraServer();

    }
}
