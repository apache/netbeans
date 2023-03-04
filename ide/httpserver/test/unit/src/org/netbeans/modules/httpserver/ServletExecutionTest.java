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

package org.netbeans.modules.httpserver;

import java.io.DataInputStream;
import java.io.InputStream;
import java.net.URL;
import org.netbeans.junit.NbTestCase;

/** Test for ServletExecutionTest.
 * It tries to connect to servlet provided by additional module.
 *
 * @author Radim Kubacki
 */
public class ServletExecutionTest extends NbTestCase {

    public ServletExecutionTest(String testName) {
        super (testName);
    }

    public void testServletExecution() throws Exception {
        HttpServerSettings settings = HttpServerSettings.getDefault();
        log("Starting HTTP server");
        settings.setRunning(true);
        
        assertTrue("HTTP server has to run", settings.isRunning());
        URL url = new URL("http", "localhost", settings.getPort(), "/servlet/org.netbeans.modules.servlettest.ModuleServlet");
        log("Connecting to "+url.toExternalForm());
        InputStream is = url.openStream();
        log(new DataInputStream(is).readLine());
    }
    
}
