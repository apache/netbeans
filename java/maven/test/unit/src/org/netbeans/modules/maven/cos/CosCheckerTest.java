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

package org.netbeans.modules.maven.cos;

import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mkleint
 */
public class CosCheckerTest {

    public CosCheckerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    /**
     * Test of extractDebugJVMOptions method, of class CosChecker.
     */
    @Test
    public void testExtractDebugJVMOptions() throws Exception {
        String arg = "-Xmx256m  -Xdebug -Djava.compiler=none -agentlib:jdwp=transport=something -Xam -Xnoagent -Xrunjdwp:transport=dt_socket,server=n,address=${jpda.address}";
        List<String> args = CosChecker.extractDebugJVMOptions(arg);
        assertEquals(args.get(0), "-Xmx256m");
        assertEquals(args.get(1), "-Xam");
        assertEquals(args.size(), 2);
    }

}
