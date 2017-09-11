/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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

    private final static int startupErrorExitValue = 184;
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
