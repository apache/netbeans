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
