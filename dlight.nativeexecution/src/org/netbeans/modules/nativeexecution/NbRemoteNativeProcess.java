/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
