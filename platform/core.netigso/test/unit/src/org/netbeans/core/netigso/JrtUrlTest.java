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
package org.netbeans.core.netigso;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import static org.junit.Assert.assertEquals;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

public class JrtUrlTest {
    @Before
    public void assumeJDK9andNewer() {
        try {
            URL test = new URL("jrt://java.compiler/");
        } catch (MalformedURLException ex) {
            Assume.assumeNoException("Only run the test on JDK9+", ex);
        }

    }

    @Test
    public void initFrameWorkAndThenCreateTheUrl() throws Exception {
        Framework framework;
        for (FrameworkFactory ff : ServiceLoader.load(FrameworkFactory.class)) {
            Map<String, String> config = new HashMap<String, String>();
            framework = ff.newFramework(config);
            framework.init();
            framework.start();
            break;
        }

        URL test = new URL("jrt://java.compiler/");
        assertEquals("jrt", test.getProtocol());
    }
}
