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

package org.netbeans.modules.terminal.api.ui;

import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which controls whether it is visible
 * as a tab or not. Note that this is orthogonal to whether the
 * window/TopComponent containing this IO becomes visible or not.
 * <p>
 * Support for this capability not only depends on which IOProvider this IO
 * originated from but also which IOContainer it is contained in.
 * @author ivan
 */
public abstract class IOVisibility {

    public static final String PROP_VISIBILITY = "IOVisibility.PROP_VISIBILITY"; // NOI18N

    private static IOVisibility find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOVisibility.class);
        }
        return null;
    }

    /**
     * Control the visibility of this I/O.
     * setVisible(true) is roughly equivalent to {@link org.openide.windows.IOSelect#select}
     * with an empty <code>extraOps</code>.
     * setVisible(false) will unconditionally remove this IO from it's container.
     * I.e. it is not the same as X'ing the tab or Closing from the context menu,
     * operations which are tempered by isClosable() and vetoing.
     * @param visible
     */
    public static void setVisible(InputOutput io, boolean visible) {
	IOVisibility iov = find(io);
	if (iov != null)
	    iov.setVisible(visible);
    }

    /**
     * Control whether this IO is closable. When closable...
     * <ul>
     * <li>The X on the tab goes away.
     * <li>Close actions are disabled.
     * <li>Close all tabs actions will close only closable tabs.
     * <li>setVisible(false) is still effective!
     * <li>closeInputOutput() is still effective!
     * </ul>
     * @param io
     * @param closable
     */
    public static void setClosable(InputOutput io, boolean closable) {
	IOVisibility iov = find(io);
	if (iov != null)
	    iov.setClosable(closable);
    }

    public static boolean isClosable(InputOutput io) {
	IOVisibility iov = find(io);
	if (iov != null)
	    return iov.isClosable();
	else
	    return true;
    }

    /**
     * Checks whether this feature is supported for provided IO.
     * The availability of this capability also depends on which IOContainer
     * The IO belongs to.
     * @param io IO to check on
     * @return true if supported
     */
    public static boolean isSupported(InputOutput io) {
	IOVisibility iov = find(io);
	if (iov == null)
	    return false;
	else
	    return iov.isSupported();
    }

    protected abstract void setVisible(boolean visible);
    protected abstract void setClosable(boolean closable);
    protected abstract boolean isClosable();
    protected abstract boolean isSupported();
}
