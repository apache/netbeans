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

import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.dbgp.DebugSession;
import org.openide.util.NbBundle;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.w3c.dom.Node;

/**
 * @author ads
 *
 */
public class DbgpStream extends DbgpMessage {
    public enum StreamType {
        STDOUT,
        STDERR;

        @Override
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

    }

    DbgpStream(Node node, StreamType type) {
        super(node);
    }

    @Override
    @NbBundle.Messages("LBL_PhpDebuggerConsole=PHP Debugger Console")
    public void process(DebugSession session, DbgpCommand command) {
        byte[] buffer;
        try {
            buffer = Base64.getDecoder().decode(getNodeValue(getNode()));
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(DbgpStream.class.getName()).log(Level.WARNING, null, ex);
            buffer = new byte[0];
        }
        InputOutput io = IOProvider.getDefault().getIO(Bundle.LBL_PhpDebuggerConsole(), false);
        io.getOut().println(new String(buffer, Charset.defaultCharset()));
        io.getOut().close();
    }

}
