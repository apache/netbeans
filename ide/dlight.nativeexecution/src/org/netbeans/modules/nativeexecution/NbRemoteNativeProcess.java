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
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author Andrew
 */
public final class NbRemoteNativeProcess extends NbNativeProcess {

    private JschSupport.ChannelStreams streams = null;

    public NbRemoteNativeProcess(NativeProcessInfo info) {
        super(info);
    }

    @Override
    protected void createProcessImpl(List<String> command) throws Throwable {
        JschSupport.ChannelParams params = new JschSupport.ChannelParams();
        params.setX11Forwarding(info.getX11Forwarding());

        StringBuilder sb = new StringBuilder();

        for (String arg : command) {
            sb.append('\'').append(arg).append('\'').append(' '); // NOI18N
        }

        streams = JschSupport.startCommand(info.getExecutionEnvironment(), sb.toString(), params); // NOI18N
        setErrorStream(streams.err);
        setInputStream(streams.out);
        setOutputStream(streams.in);
    }

    public boolean isAlive() {
        if (streams == null || streams.channel == null) {
            return false;
        }

        return streams.channel.isConnected();
    }

    @Override
    protected int waitResultImpl() throws InterruptedException {
        if (streams == null || streams.channel == null) {
            return -1;
        }

        try {
            while (streams.channel.isConnected()) {
                Thread.sleep(200);
            }
            
            finishing();

            return streams.channel.getExitStatus();
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
}
