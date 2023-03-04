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
package org.netbeans.spi.extexecution.base;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.extexecution.base.ProcessParametersAccessor;

/**
 *
 * @author Petr Hejl
 */
public class ProcessParametersTest extends NbTestCase {

    public ProcessParametersTest(String name) {
        super(name);
    }

    public void testParameters() {
        Map<String, String> variables = new HashMap<String, String>();
        variables.put("key1", "value1");
        variables.put("key2", "value2");

        ProcessParameters params = ProcessParametersAccessor.getDefault().createProcessParameters(
                "ls", "/home", Collections.singletonList("argument"), true, variables);

        assertEquals("ls", params.getExecutable());
        assertEquals("/home", params.getWorkingDirectory());
        assertTrue(params.isRedirectErrorStream());

        assertEquals(1, params.getArguments().size());
        assertEquals("argument", params.getArguments().get(0));

        assertEquals(2, params.getEnvironmentVariables().size());
        assertEquals("value1", params.getEnvironmentVariables().get("key1"));
        assertEquals("value2", params.getEnvironmentVariables().get("key2"));
    }
}
