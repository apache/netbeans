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

package org.netbeans.modules.cnd.debugger.common2.debugger.io;

import java.io.IOException;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;
import org.openide.util.Exceptions;
import org.openide.windows.InputOutput;

/**
 *
 */
class OutputPack extends IOPack {
    private final IOProxy ioProxy;
    private final InputOutput io;

    public OutputPack(TermComponent console,
            InputOutput io,
            ExecutionEnvironment exEnv) {
        super(console, exEnv, false);
        this.io = io;
        this.ioProxy = IOProxy.create(exEnv, io);
    }

    @Override
    public boolean start() {
        return true;
    }

    @Override
    public String[] getIOFiles() {
        return ioProxy.getIOFiles();
    }

    @Override
    public MacroMap updateEnv(MacroMap macroMap) {
        try {
            UnbufferSupport.initUnbuffer(exEnv, macroMap);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return macroMap;
    }

    @Override
    public void switchTo() {
        super.switchTo();
        io.select();
    }

    @Override
    public void close() {
        ioProxy.stop();
    }
}
