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

package org.netbeans.modules.extexecution.base;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Petr Hejl
 */
public class ExternalProcessBuilderTest extends NbTestCase {

    public ExternalProcessBuilderTest(String name) {
        super(name);
    }

    public void testEnvironment() {
        ExternalProcessBuilder creator = new ExternalProcessBuilder("command");
        creator = creator.addEnvironmentVariable("test1", "value1");
        creator = creator.addEnvironmentVariable("test2", "value2");

        Map<String, String> env = new HashMap<String, String>(
                creator.buildEnvironment(Collections.<String, String>emptyMap()));
        assertEquals("value1", env.remove("test1"));
        assertEquals("value2", env.remove("test2"));
        assertTrue(env.isEmpty());
    }

    public void testPath() {
        ExternalProcessBuilder creator = new ExternalProcessBuilder("command");
        Map<String, String> original = new HashMap<String, String>();
        original.put("PATH", "original");

        // original path
        Map<String, String> env = new HashMap<String, String>(
                creator.buildEnvironment(original));
        assertEquals("original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        // some added path
        File addedPath = new File("addedPath");
        creator = creator.prependPath(addedPath);
        env = new HashMap<String, String>(creator.buildEnvironment(original));
        assertEquals(addedPath.getAbsolutePath().replace(" ", "\\ ") + File.pathSeparator + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());

        // yet another path
        File nextPath = new File("nextPath");
        creator = creator.prependPath(nextPath);
        env = new HashMap<String, String>(creator.buildEnvironment(original));
        assertEquals(
                nextPath.getAbsolutePath().replace(" ", "\\ ") + File.pathSeparator
                + addedPath.getAbsolutePath().replace(" ", "\\ ") + File.pathSeparator
                + "original", env.remove("PATH"));
        assertTrue(env.isEmpty());
    }

    public void testImmutability() throws IOException {
        ExternalProcessBuilder builder = new ExternalProcessBuilder("ls");

        assertNotSame(builder, builder.addArgument("test"));
        assertNotSame(builder, builder.addEnvironmentVariable("test", "test"));
        assertNotSame(builder, builder.prependPath(getWorkDir()));
        assertNotSame(builder, builder.redirectErrorStream(true));
        assertNotSame(builder, builder.workingDirectory(getWorkDir()));
    }
}
