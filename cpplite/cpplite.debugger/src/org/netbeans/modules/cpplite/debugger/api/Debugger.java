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
package org.netbeans.modules.cpplite.debugger.api;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.api.extexecution.base.ExplicitProcessParameters;

import org.netbeans.modules.cpplite.debugger.CPPLiteDebugger;
import org.netbeans.modules.cpplite.debugger.CPPLiteDebuggerConfig;

/**
 *
 * @author lahvac
 */
public class Debugger {

    @Deprecated
    public static Process startInDebugger(List<String> command) throws IOException {
        return startInDebugger(command, new File(System.getProperty("user.dir")));
    }

    public static Process startInDebugger(List<String> command, File directory) throws IOException {
        CPPLiteDebugger[] debugger = new CPPLiteDebugger[] { null };
        ExplicitProcessParameters processParameters = ExplicitProcessParameters.builder().workingDirectory(directory).build();
        Process engineProcess = CPPLiteDebugger.startDebugging(
                new CPPLiteDebuggerConfig(command, processParameters, true, null, "gdb"),
                engine -> {
                    debugger[0] = engine.lookupFirst(null, CPPLiteDebugger.class);
                });
        debugger[0].execRun();
        return engineProcess;
    }
}
