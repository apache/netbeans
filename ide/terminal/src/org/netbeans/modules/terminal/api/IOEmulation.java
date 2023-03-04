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
 * Capability of an InputOutput which allows ...
 * <ul>
 * <li>
 * Querying and setting of
 * emulation type using termcap/terminfo terminal types.
 * <br>
 * The default implementation of InputOutput has a terminal type of
 * "dumb".
 * <li>
 * Controlling whether the IO depends on an external agent, typically a pty,
 * to implement a "line discipline" or emulates it itself.
 * <br>
 * Line discipline is the functionality which handles things like
 *   <ul>
 *   <li>Line buffering.
 *   <li>CR/LF conversions.
 *   <li>Backspace.
 *   <li>Tabs.
 *   </ul>
 * The default implementation of InputOutput has disciplined set to true.
 * </ul>
 * @author ivan
 */
public abstract class IOEmulation {

    private static IOEmulation find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOEmulation.class);
        }
        return null;
    }

    /**
     * Return the terminal type supported by this IO.
     * @param io IO to operate on.
     * @return terminal type.
     */
    public static String getEmulation(InputOutput io) {
	IOEmulation ior = find(io);
	if (ior != null)
	    return ior.getEmulation();
	else
	    return "dumb";		// NOI18N

    }

    /**
     * Return whether this IO implements it's own line discipline.
     * @param io IO to operate on.
     * @return If true this IO implements it's own line discipline.
     */
    public static boolean isDisciplined(InputOutput io) {
	IOEmulation ior = find(io);
	if (ior != null)
	    return ior.isDisciplined();
	else
	    return true;
    }

    /**
     * Return whether this IO implements it's own line discipline.
     * @param io IO to operate on.
     */
    public static void setDisciplined(InputOutput io) {
	IOEmulation ior = find(io);
	if (ior != null)
	    ior.setDisciplined();
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
     * Return the terminal type supported by this IO.
     * @return terminal type.
     */
    protected abstract String getEmulation();

    /**
     * Return whether this IO implements it's own line discipline.
     * @return If true this IO implements it's own line discipline.
     */
    protected abstract boolean isDisciplined();

    /**
     * Declare that this IO should implement it's own line discipline.
     */
    protected abstract void setDisciplined();
}
