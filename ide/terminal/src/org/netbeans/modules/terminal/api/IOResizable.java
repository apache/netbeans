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

import java.awt.Dimension;
import org.openide.util.Lookup;
import org.openide.windows.InputOutput;

/**
 * Capability of an InputOutput which provides notification of
 * size changes.
 * @author ivan
 */
public abstract class IOResizable {

    /**
     * Use IONotifier with PROP_SIZE and IOResizable.Size as values
     */
    public static final String PROP_SIZE = "IOResizable.PROP_SIZE"; // NOI18N

    public static final class Size {
	public final Dimension cells;
	public final Dimension pixels;

	public Size(Dimension cells, Dimension pixels) {
	    this.cells = cells;
	    this.pixels = pixels;
	}
    }

    private static IOResizable find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOResizable.class);
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
}
