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

import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of InputOutput to help manage and track Stream connections
 * to an InputOutput.
 * @author ivan
 */
public abstract class IOConnect {

    public static final String PROP_CONNECTED = "IOConnect.PROP_CONNECTED"; // NOI18N

    private static IOConnect find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOConnect.class);
        }
        return null;
    }

    /**
     * Return whether any streams are connected to this IO.
     * <b>
     * An IO is "disconnected" in it's default state, before any of getOut(),
     * getErr() or IOTerm.connect() are called.
     * <b>
     * An IO is "connected" if any of getOut(), getErr() or
     * IOTerm.connect() are called.
     * <b>
     * An IO is "disconnected" after all of getIn().close(), getErr().close() and
     * IOTerm.disconnect() or disconnectAll() are called.
     * <b>
     * Only a "disconnected" IO is eligible for reuse via
     * {@link org.openide.windows.IOProvider#getIO(String, boolean)}
     * @param io
     */
    public static boolean isConnected(InputOutput io) {
	IOConnect ioc = find(io);
	if (ioc != null)
	    return ioc.isConnected();
	else
	    return false;
    }

    /**
     * Disconnects all of getIn() and getOut() and any streams connected
     * via IOTerm.connect().
     * @param io
     * @param continuation See {@link IOTerm#disconnect}.
     */
    public static void disconnectAll(InputOutput io, Runnable continuation) {
	IOConnect ioc = find(io);
	if (ioc != null)
	    ioc.disconnectAll(continuation);
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    protected abstract boolean isConnected();

    protected abstract void disconnectAll(Runnable continuation);
}
