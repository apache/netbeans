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

import com.jcraft.jsch.JSchException;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelParams;
import org.netbeans.modules.nativeexecution.JschSupport.ChannelStreams;
import org.netbeans.modules.nativeexecution.api.util.MacroMap;
import org.netbeans.modules.nativeexecution.api.util.UnbufferSupport;
import org.netbeans.modules.nativeexecution.support.EnvWriter;
import org.openide.util.Exceptions;

public final class RemoteNativeProcess extends AbstractNativeProcess {

    private static final int startupErrorExitValue = 184;
    private ChannelStreams streams = null;

    public RemoteNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void create() throws Throwable {
        if (isInterrupted()) {
            throw new InterruptedException();
        }

        final String commandLine = info.getCommandLineForShell();
        final MacroMap envVars = info.getEnvironment().clone();

        // Setup LD_PRELOAD to load unbuffer library...
        if (info.isUnbuffer()) {
            UnbufferSupport.initUnbuffer(info.getExecutionEnvironment(), envVars);
        }

        if (isInterrupted()) {
            throw new InterruptedException();
        }

        ChannelParams params = new ChannelParams();
        params.setX11Forwarding(info.getX11Forwarding());

        // To execute a command we use shell (/bin/sh) as a trampoline ..
        streams = JschSupport.startCommand(info.getExecutionEnvironment(), "/bin/sh -s", params); // NOI18N

        setErrorStream(streams.err);
        setInputStream(streams.out);
        setOutputStream(streams.in);

        // 1. get the PID of the shell
        streams.in.write("echo $$\n".getBytes()); // NOI18N
        streams.in.flush();

        final String workingDirectory = info.getWorkingDirectory(true);

        // 2. cd to working directory
        if (workingDirectory != null) {
            streams.in.write(EnvWriter.getBytes(
                    "cd \"" + workingDirectory + "\" || exit " + startupErrorExitValue + "\n", true)); // NOI18N
        }

        // 3. setup env
        EnvWriter ew = new EnvWriter(streams.in, true);
        ew.write(envVars);

        // 4. additional setup
        if (info.getInitialSuspend()) {
            streams.in.write("ITS_TIME_TO_START=\n".getBytes()); // NOI18N
            streams.in.write("trap 'ITS_TIME_TO_START=1' CONT\n".getBytes()); // NOI18N
            streams.in.write("while [ -z \"$ITS_TIME_TO_START\" ]; do sleep 1; done\n".getBytes()); // NOI18N
        }

        if (info.isRedirectError()) {
            streams.in.write(("exec 2>&1\n").getBytes()); // NOI18N
        }

        // 5. finally do exec
        streams.in.write(EnvWriter.getBytes("exec " + commandLine + "\n", true)); // NOI18N
        streams.in.flush();

        readPID(streams.out);
    }

    @Override
    public int waitResult() throws InterruptedException {
        if (streams == null || streams.channel == null) {
            return -1;
        }

        try {
            while (streams.channel.isConnected()) {
                Thread.sleep(200);
            }

            finishing();

            int exitValue = streams.channel.getExitStatus();

            if (exitValue == startupErrorExitValue) {
                exitValue = -1;
            }

            return exitValue;
        } finally {
            if (streams != null) {
                try {
                    ConnectionManagerAccessor.getDefault().closeAndReleaseChannel(getExecutionEnvironment(), streams.channel);
                } catch (JSchException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

    public boolean isAlive() {
        if (streams == null || streams.channel == null) {
            return false;
        }

        return streams.channel.isConnected();
    }
}
