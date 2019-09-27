/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.docker.ui.output;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.terminal.api.IOConnect;
import org.openide.windows.InputOutput;

/**
 *
 * @author Petr Hejl
 */
public class TerminalInputStream extends FilterInputStream {

    private static final Logger LOGGER = Logger.getLogger(TerminalInputStream.class.getName());

    private final InputOutput io;

    private final Closeable[] close;

    public TerminalInputStream(InputOutput io, InputStream in, Closeable... close) {
        super(in);
        this.io = io;
        this.close = close;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        try {
            int i = super.read(b, off, len);
            if (i < 0) {
                closeTerminal();
            }
            return i;
        } catch (IOException ex) {
            closeTerminal();
            throw ex;
        }
    }

    @Override
    public int read() throws IOException {
        try {
            int i = super.read();
            if (i < 0) {
                closeTerminal();
            }
            return i;
        } catch (IOException ex) {
            closeTerminal();
            throw ex;
        }
    }

    private void closeTerminal() {
        for (Closeable c : close) {
            try {
                if (c != null) {
                    c.close();
                }
            } catch (IOException ex) {
                LOGGER.log(Level.FINE, null, ex);
            }
        }
        // disconnect all is needed as we call getOut().reset()
        // because of that getOut()
        if (IOConnect.isSupported(io)) {
            IOConnect.disconnectAll(io, null);
        }
        //IOTerm.disconnect(io, null);
        //LOGGER.log(Level.INFO, "Closing terminal", new Exception());
    }

}
