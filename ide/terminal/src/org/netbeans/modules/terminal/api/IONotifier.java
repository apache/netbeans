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

import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which allows the receiving of property
 * change notifications.
 * All notifications are delivered on the EDT!
 * @author ivan
 */
public abstract class IONotifier {

    private static IONotifier find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IONotifier.class);
        }
        return null;
    }

    public static void addPropertyChangeListener(InputOutput io, PropertyChangeListener listener) {
	IONotifier ion = find(io);
	if (ion != null)
	    ion.addPropertyChangeListener(listener);
    }

    public static void removePropertyChangeListener(InputOutput io, PropertyChangeListener listener) {
	IONotifier ion = find(io);
	if (ion != null)
	    ion.removePropertyChangeListener(listener);
    }

    public static void addVetoableChangeListener(InputOutput io, VetoableChangeListener listener ) {
	IONotifier ion = find(io);
	if (ion != null)
	    ion.addVetoableChangeListener(listener);
    }

    public static void removeVetoableChangeListener(InputOutput io, VetoableChangeListener listener ) {
	IONotifier ion = find(io);
	if (ion != null)
	    ion.removeVetoableChangeListener(listener);
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    protected abstract void addPropertyChangeListener(PropertyChangeListener listener);

    protected abstract void removePropertyChangeListener(PropertyChangeListener listener);

    protected abstract void addVetoableChangeListener(VetoableChangeListener listener );

    protected abstract void removeVetoableChangeListener(VetoableChangeListener listener );
}
