/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.dbgp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.text.MessageFormat;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.breakpoints.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

class ServerThread extends SingleThread {
    private static final int TIMEOUT = 10000;
    private static final String PORT_OCCUPIED = "MSG_PortOccupied"; // NOI18N
    private int myPort;
    private ServerSocket myServer;
    private AtomicBoolean isStopped;

    ServerThread() {
        super();
        isStopped = new AtomicBoolean(false);
    }

    @Override
    public void run() {
        isStopped = new AtomicBoolean(false);
        DebugSession debugSession = getDebugSession();
        ProxyClient proxy = null;
        if (debugSession != null && createServerSocket(debugSession)) {
            proxy = ProxyClient.getInstance(debugSession.getOptions());
            if (proxy != null) {
                proxy.register();
            }
            debugSession.startBackend();
            while (!isStopped() && getDebugSession() != null) {
                try {
                    Socket sessionSocket = myServer.accept();
                    if (!isStopped.get() && sessionSocket != null) {
                        debugSession.startProcessing(sessionSocket);
                    }
                } catch (SocketTimeoutException e) {
                    log(e, Level.FINEST);
                } catch (IOException e) {
                    log(e);
                }
            }
            closeSocket();
        }
        if (proxy != null) {
            proxy.unregister();
        }
    }

    private DebugSession getDebugSession() {
        DebugSession retval = DebuggerManager.getDebuggerManager().getCurrentEngine().lookupFirst(null, DebugSession.class);
        if (retval == null) {
            Session[] sessions = DebuggerManager.getDebuggerManager().getSessions();
            for (Session session : sessions) {
                retval = session.lookupFirst(null, DebugSession.class);
                if (retval != null) {
                    break;
                }
            }
        }
        return retval;
    }

    private void log(Throwable exception) {
        log(exception, Level.FINE);
    }

    private void log(Throwable exception, Level level) {
        Logger.getLogger(ServerThread.class.getName()).log(level, null, exception);
    }

    private boolean createServerSocket(DebugSession debugSession) {
        synchronized (ServerThread.class) {
            try {
                myPort = debugSession.getOptions().getPort();
                myServer = new ServerSocket(myPort);
                myServer.setSoTimeout(TIMEOUT);
                myServer.setReuseAddress(true);
            } catch (IOException e) {
                String mesg = NbBundle.getMessage(ServerThread.class, PORT_OCCUPIED);
                mesg = MessageFormat.format(mesg, myPort);
                NotifyDescriptor descriptor = new NotifyDescriptor.Confirmation(mesg, JOptionPane.YES_NO_OPTION);
                Object choice = DialogDisplayer.getDefault().notify(descriptor);
                if (choice.equals(JOptionPane.YES_OPTION)) {
                    Utils.openPhpOptionsDialog();
                }
                log(e);
                return false;
            }
            return true;
        }
    }

    private void closeSocket() {
        synchronized (ServerThread.class) {
            if (myServer == null) {
                return;
            }
            try {
                if (!myServer.isClosed()) {
                    myServer.close();
                }
            } catch (IOException e) {
                log(e);
            }
        }
    }

    @Override
    public boolean cancel() {
        isStopped.set(true);
        closeSocket();
        return true;
    }

    private boolean isStopped() {
        return isStopped.get();
    }

}
