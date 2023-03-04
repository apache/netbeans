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

package org.netbeans.modules.terminal.api;

import java.io.InputStream;
import java.io.OutputStream;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which provides direct access to a Term.
 * @author ivan
 */
public abstract class IOTerm {

    private static IOTerm find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOTerm.class);
        }
        return null;
    }
    
    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }


    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     */
    public static void connect(InputOutput io, OutputStream pin, InputStream pout, InputStream perr) {
	connect(io, pin, pout, perr, null);
    }
    
    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     * @param charset The name of a supported
     *         {@link java.nio.charset.Charset </code>charset<code>}, null for system default
     */
    public static void connect(InputOutput io, OutputStream pin, InputStream pout, InputStream perr, String charset) {
        connect(io, pin, pout, perr, charset, null);
    }
    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     * @param charset The name of a supported
     *         {@link java.nio.charset.Charset </code>charset<code>}, null for system default
     * @param postConnectTask the task to be executed *after* the connection is established
     */
    public static void connect(InputOutput io, OutputStream pin, InputStream pout, InputStream perr, String charset, Runnable postConnectTask) {        
        IOTerm iot = find(io);
	if (iot != null) {
	    iot.connect(pin, pout, perr, charset, postConnectTask);
        }
    }

    /**
     * Disconnect previously connected Streams and free resources.
     * Arrange to wait until all pending output from a terminated or exited
     * process has been rendered in the terminal and then call
     * continuation.run() on the EDT thread.
     * Only then can connect() be called again.
     * @param continuation The continuation to run after all output has been
     *        drained.
     */
    public static void disconnect(InputOutput io, Runnable continuation) {
	IOTerm iot = find(io);
	if (iot != null)
	    iot.disconnect(continuation);
	else
	    return;
    }


    public static void setReadOnly(InputOutput io, boolean isReadOnly) {
        IOTerm iot = find(io);
	if (iot == null) {
            return;
        }
	iot.setReadOnly(isReadOnly);      
    }

    public static void requestFocus(InputOutput io) {
        IOTerm iot = find(io);
	if (iot == null) {
            return;
        }
	iot.requestFocus();
    }

    /**
     * Connect an I/O stream pair or triple to this Term.
     *
     * @param pin Input (and paste operations) to the sub-process.
     *             this stream.
     * @param pout Main output from the sub-process. Stuff received via this
     *             stream will be rendered on the screen.
     * @param perr Error output from process. May be null if the error stream
     *		   is already absorbed into 'pout' as the case might be with
     *             ptys.
     * @param charset The name of a supported
     *         {@link java.nio.charset.Charset </code>charset<code>}, null for system default
     * @param postConnectTask the task to be executed *after* the connection is established
     */    
    protected abstract void connect(OutputStream pin, InputStream pout, InputStream perr, String charset, Runnable postConnectTask);

    protected abstract void disconnect(Runnable continuation);
    
    protected abstract void setReadOnly(boolean isReadOnly);
    
    protected abstract void requestFocus();
}
