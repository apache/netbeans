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

package org.openide.windows;

import org.openide.util.Lookup;

/**
 * Navigation (scrolling) in IO component.
  * <p>
 * Client usage:
 * <pre>
 *  InputOutput io = ...;
 *  // store current position of IO
 *  IOPosition.Position pos = IOPosition.currentPosition(io);
 *  ...
 *  // scroll to stored position
 *  pos.scrollTo();
 * </pre>
 * How to support {@link IOPosition} in own {@link IOProvider} implementation:
 * <ul>
 *   <li> {@link InputOutput} provided by {@link IOProvider} has to implement {@link org.openide.util.Lookup.Provider}
 *   <li> Extend {@link IOPosition} and implement its abstract methods
 *   <li> Place instance of {@link IOPosition} to {@link Lookup} provided by {@link InputOutput}
 * </ul>
 * @since 1.16
 * @author Tomas Holy
 */
public abstract class IOPosition {

    private static IOPosition find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOPosition.class);
        }
        return null;
    }

    public interface Position {
        void scrollTo();
    }

    /**
     * Gets current position (in number of chars) in IO
     * @param io IO to operate on
     * @return current position or null if not supported
     */
    public static Position currentPosition(InputOutput io) {
        IOPosition iop = find(io);
        return iop != null ? iop.currentPosition() : null;
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
     * Gets current position in IO
     * @return current position
     */
    protected abstract Position currentPosition();
}
