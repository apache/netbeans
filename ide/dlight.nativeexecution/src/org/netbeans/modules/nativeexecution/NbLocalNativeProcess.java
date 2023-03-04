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
package org.netbeans.modules.nativeexecution;

import java.util.List;
import org.netbeans.modules.nativeexecution.api.util.WindowsSupport;

/**
 *
 * @author Andrew
 */
public final class NbLocalNativeProcess extends NbNativeProcess {

    private Process process = null;

    public NbLocalNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void createProcessImpl(List<String> command) throws Throwable {
        final ProcessBuilder pb = new ProcessBuilder(command);

        if (isWindows()) {
            // If cygwin is not in the PATH - pty will not start as
            // cygwin1.dll will not be found. 
            // set PATH to cygwin's bin dir (others are not importnt for nbstart)
            String pathKey = WindowsSupport.getInstance().getPathKey();
            pb.environment().put(pathKey, WindowsSupport.getInstance().getActiveShell().bindir.getAbsolutePath());
        }

        process = pb.start();
        setErrorStream(process.getErrorStream());
        setInputStream(process.getInputStream());
        setOutputStream(process.getOutputStream());
    }

    @Override
    protected int waitResultImpl() throws InterruptedException {
        if (process == null) {
            return -1;
        }
        int rc = process.waitFor();
        finishing();
        return rc;
    }
}
