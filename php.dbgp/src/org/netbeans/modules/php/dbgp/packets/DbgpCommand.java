/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.php.dbgp.packets;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.Session;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.netbeans.modules.php.dbgp.DebuggerOptions;
import org.netbeans.modules.php.dbgp.SessionId;
import org.netbeans.modules.php.dbgp.SessionManager;

/**
 * @author ads
 *
 */
public abstract class DbgpCommand {
    private static final Logger LOGGER = Logger.getLogger(DbgpCommand.class.getName());
    protected static final String SPACE = " "; // NOI18N
    private static final String DATA_SEPARATOR = " -- "; // NOI18N
    private static final String TRANSACTION_OPT = " -i "; // NOI18N
    private String command;
    private String transactionId;

    DbgpCommand(String command, String transactionId) {
        this.command = command;
        this.transactionId = transactionId;
    }

    public void send(OutputStream out) throws IOException {
        String encodedData = null;
        if (getData() != null) {
            encodedData = encodeData();
        }
        StringBuilder dataToSend = new StringBuilder(getCommand());
        dataToSend.append(getArgumentString());
        if (encodedData != null) {
            dataToSend.append(DATA_SEPARATOR);
            dataToSend.append(encodedData);
        }
        LOGGER.log(Level.FINE, "command to send : {0}", dataToSend); // NOI18N
        byte[] bytes = dataToSend.toString().getBytes(Charset.defaultCharset());
        byte[] sendBytes = new byte[bytes.length + 1];
        System.arraycopy(bytes, 0, sendBytes, 0, bytes.length);
        sendBytes[ bytes.length] = 0;
        out.write(sendBytes);
        out.flush();
    }

    private String encodeData() throws IOException {
        Session session = DebuggerManager.getDebuggerManager().getCurrentSession();
        byte[] bytes = getData().getBytes(Charset.defaultCharset());
        if (session != null) {
            SessionId sessionId = session.lookupFirst(null, SessionId.class);
            if (sessionId != null) {
                DebugSession debugSession = SessionManager.getInstance().getSession(sessionId);
                if (debugSession != null) {
                    DebuggerOptions options = debugSession.getOptions();
                    if (options != null) {
                        String projectEncoding = options.getProjectEncoding();
                        bytes = getData().getBytes(projectEncoding);
                    }
                }
            }
        }
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String getTransactionId() {
        return transactionId;
    }

    public abstract boolean wantAcknowledgment();

    protected String getData() {
        return null;
    }

    public String getCommand() {
        return command;
    }

    protected String getArguments() {
        return "";
    }

    private String getArgumentString() {
        if (getArguments() != null && getArguments().length() > 0) {
            return TRANSACTION_OPT + transactionId + " " + getArguments();
        } else {
            return TRANSACTION_OPT + transactionId;
        }
    }

    @Override
    public String toString() {
        StringBuilder dataToSend = new StringBuilder(getCommand());
        dataToSend.append(getArgumentString());
        String encodedData = null;
        if (getData() != null) {
            try {
                encodedData = encodeData();
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        if (encodedData != null) {
            dataToSend.append(DATA_SEPARATOR);
            dataToSend.append(encodedData);
        }
        return dataToSend.toString();
    }

}
