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

package org.netbeans.libs.jstestdriver;

import java.io.File;
import org.netbeans.api.extexecution.print.LineConvertor;
import org.netbeans.libs.jstestdriver.api.ServerListener;
import org.netbeans.libs.jstestdriver.api.TestListener;

/**
 *
 */
public interface JsTestDriverImplementation {

    void startServer(File jsTestDriverJar, int port, boolean strictMode, ServerListener listener);
    void stopServer();
    boolean isRunning();
    boolean wasStartedExternally();
    
    void runTests(File jsTestDriverJar, String serverURL, boolean strictMode, File baseFolder, File configFile, 
            String testsToRun, TestListener listener, LineConvertor lineConvertor);
}
