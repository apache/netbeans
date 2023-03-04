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

package org.netbeans.modules.terminal.ioprovider;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;

import org.openide.util.lookup.ServiceProvider;

import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.OutputWriter;

/**
 * An implementation of {@link IOProvider} based on
 * {@link org.netbeans.lib.terminalemulator.Term}.
 * Lookup id is "Terminal".
 * <p>
 * <pre>
	IOProvider iop = IOProvider.get("Terminal");
        if (iop == null)
            iop = IOProvider.getDefault();
 * </pre>
 * @author ivan
 */

@ServiceProvider(service = IOProvider.class, position=200)

public final class TerminalIOProvider extends IOProvider {

    private static final List<TerminalInputOutput> list =
	    new ArrayList<TerminalInputOutput>();

    @Override
    public String getName() {
        return "Terminal";      // NOI18N
    }

    @Override
    public InputOutput getIO(String name, Action[] additionalActions) {
	// FIXUP: to try from CND
	return getIO(name, true, additionalActions, null);
    }

    @Override
    public InputOutput getIO(String name, boolean newIO) {
	return getIO(name, newIO, null, null);
    }

    @Override
    public InputOutput getIO(String name, Action[] actions, IOContainer ioContainer) {
	return getIO(name, true, actions, ioContainer);
    }

    @Override
    public InputOutput getIO(String name,
	                      boolean newIO,
			      Action[] actions,
			      IOContainer ioContainer) {

	synchronized (list) {
	    Set<TerminalInputOutput> candidates = new HashSet<TerminalInputOutput>();

	    if (!newIO && name != null) {
		// find candidates for reuse
		for (TerminalInputOutput tio : list) {
		    if (name.equals(tio.name()) && !tio.terminal().isConnected())
			candidates.add(tio);
		}
	    }

	    TerminalInputOutput tio = null;
	    if (candidates.isEmpty()) {
		// newIO == true || nothing found to reuse
		if (ioContainer == null)
		    ioContainer = IOContainer.getDefault();
		tio = new TerminalInputOutput(name, actions, ioContainer);
		list.add(tio);
	    } else {
		for (TerminalInputOutput candidate : candidates) {
		    if (tio == null)
			tio = candidate;
		    else
			candidate.closeInputOutput();
		}
	    }
	    return tio;
	}
    }

    /**
     * This operation is not supported because standard NetBeans output are
     * is not Term based.
     * For that matter, neither Tim nor anyone else remembers what's it for.
     * @return nothing. Always throws UnsupportedOperationException.
     */
    @Override
    public OutputWriter getStdOut() {
        throw new UnsupportedOperationException("Not supported yet.");	// NOI18N
    }

    static void remove(TerminalInputOutput io) {
	synchronized (list) {
	    list.remove(io);
	}
    }

}
