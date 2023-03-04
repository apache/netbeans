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
package org.netbeans.modules.jshell.launch;

import java.util.Map;
import org.netbeans.lib.nbjshell.NbExecutionControl;
import org.netbeans.lib.nbjshell.RemoteJShellService;

/**
 *
 * @author sdedic
 */
public final class BrokenExecutionControl implements NbExecutionControl, RemoteJShellService {
    private final EngineTerminationException originalException;

    public BrokenExecutionControl(EngineTerminationException originalException) {
        this.originalException = originalException;
    }
    
    @Override
    public Map<String, String> commandVersionInfo() {
        return null;
    }

    @Override
    public void load(ClassBytecodes[] cbs) throws ClassInstallException, NotImplementedException, EngineTerminationException {
        throw originalException;
    }

    @Override
    public void redefine(ClassBytecodes[] cbs) throws ClassInstallException, NotImplementedException, EngineTerminationException {
        throw originalException;
    }

    @Override
    public String invoke(String string, String string1) throws RunException, EngineTerminationException, InternalException {
        throw originalException;
    }

    @Override
    public String varValue(String string, String string1) throws RunException, EngineTerminationException, InternalException {
        throw originalException;
    }

    @Override
    public void addToClasspath(String string) throws EngineTerminationException, InternalException {
        throw originalException;
    }

    @Override
    public void stop() throws EngineTerminationException, InternalException {
        throw originalException;
    }

    @Override
    public Object extensionCommand(String string, Object o) throws RunException, EngineTerminationException, InternalException {
        throw originalException;
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requestShutdown() {
        return false;
    }

    @Override
    public void closeStreams() {
    }

    @Override
    public String getTargetSpec() {
        return null;
    }

    @Override
    public void suppressClasspathChanges(boolean b) {
    }

    @Override
    public ExecutionControlException getBrokenException() {
        return originalException;
    }
    
}
