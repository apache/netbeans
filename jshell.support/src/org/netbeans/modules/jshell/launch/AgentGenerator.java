/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
 */
package org.netbeans.modules.jshell.launch;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import jdk.jshell.execution.Util;
import jdk.jshell.spi.ExecutionControl;
import jdk.jshell.spi.ExecutionEnv;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
abstract class AgentGenerator implements RemoteJShellAccessor, ShellLaunchListener {
    protected final ShellAgent agent;
    protected final String targetSpec;

    public AgentGenerator(ShellAgent agent, String targetSpec) {
        this.agent = agent;
        this.targetSpec = targetSpec;
    }

    @Override
    public String getTargetSpec() {
        return targetSpec;
    }

    @Override
    public JShellConnection getOpenedConnection() {
        return shellConnection;
    }

    @Override
    public ExecutionControl generate(ExecutionEnv ee, Map<String, String> optionsIgnored) throws Throwable {
        InputStream in;
        OutputStream out;
        JShellConnection c;
        try {
            c = getConnection();
            out = c.getAgentInput();
            in = c.getAgentOutput();
        } catch (IOException iOException) {
            ExecutionControl.EngineTerminationException x = 
                    new ExecutionControl.EngineTerminationException(iOException.getLocalizedMessage());
            x.initCause(iOException);
            return new BrokenExecutionControl(x);
        }
        
        Map<String, OutputStream> io = new HashMap<>();
        io.put("out", ee.userOut()); // NOI18N
        io.put("err", ee.userErr()); // NOI18N
        Throwable[] t = new Throwable[1];
        ExecutionControl res = Util.remoteInputOutput(
            in, out,
            io,
            Collections.emptyMap(), 
            (ObjectInput cmdIn, ObjectOutput cmdOut) -> {
                try {
                    return createExecControl(agent, cmdOut, cmdIn, c);
                } catch (RuntimeException | Error e) {
                    throw e;
                } catch (Throwable x) {
                    t[0] = x;
                    return null;
                }
            }
        );
        if (res == null) {
            if (t[0] != null) {
                throw t[0];
            }
            throw new IllegalStateException();
        }
        return res;
    }
    
    protected abstract ExecutionControl createExecControl(ShellAgent agent, ObjectOutput out, ObjectInput in, JShellConnection c) throws Throwable;
    
    private JShellConnection shellConnection;
    private boolean closed;

    @NbBundle.Messages({
        "MSG_AgentConnectionBroken2=Connection to Java Shell agent broken. Please re-run the project.", 
        "# {0} - error message", 
        "MSG_ErrorConnectingToAgent=Error connecting to Java Shell agent: {0}"
    })
    private JShellConnection getConnection() throws IOException {
        synchronized (this) {
            if (closed) {
                throw new IOException(Bundle.MSG_AgentConnectionBroken2());
            }
            if (shellConnection != null) {
                return shellConnection;
            }
        }
        try {
            JShellConnection x = agent.createConnection();
            ShellLaunchManager.getInstance().addLaunchListener(this);
            synchronized (this) {
                return this.shellConnection = x;
            }
        } catch (IOException ex) {
            StatusDisplayer.getDefault().setStatusText(Bundle.MSG_ErrorConnectingToAgent(ex.getLocalizedMessage()), 100);
            throw ex;
        }
    }

    @Override
    public void connectionInitiated(ShellLaunchEvent ev) {
    }

    @Override
    public void handshakeCompleted(ShellLaunchEvent ev) {
    }

    @Override
    public synchronized void connectionClosed(ShellLaunchEvent ev) {
        if (shellConnection == ev.getConnection()) {
            shellConnection = null;
        }
    }

    @Override
    public synchronized void agentDestroyed(ShellLaunchEvent ev) {
        closed = true;
    }
}
