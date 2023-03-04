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

package org.netbeans.modules.terminal.test;

import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which allows unit tests to access
 * useful information and functionality. Sort-of an analog of the
 * JTAG interface used in microelectronics.
 */
public abstract class IOTest {

    private static IOTest find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOTest.class);
        }
        return null;
    }

    /**
     * Return true if the Task queue associated with this IO's provider
     * has no more work items.
     * @param io IO to operate on.
     * @return If true the Task queue associated with this IO's provider
     * has no more work items.
     */
    public static boolean isQuiescent(InputOutput io) {
	IOTest ior = find(io);
	if (ior != null) {
	    return ior.isQuiescent();
	} else {
	    assert false : "isQuiesent isn't implemented";
	    return false;
	}
    }

    /**
     * Simulate the user issuing the Close action or clicking on
     * the tab close "X".
     * We need this because IOVisibility.setVisible(false) is an
     * unconditional close and we'd like to test isClosable()
     * and vetoing.
     */
    public static void performCloseAction(InputOutput io) {
	IOTest ior = find(io);
	if (ior != null) {
	    ior.performCloseAction();
	}
    }

    /**
     * Checks whether this feature is supported for provided IO
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
        return find(io) != null;
    }

    protected abstract boolean isQuiescent();
    protected abstract void performCloseAction();
}
