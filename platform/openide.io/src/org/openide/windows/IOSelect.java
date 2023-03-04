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

import java.util.Set;
import org.openide.util.Lookup;
import org.openide.util.Parameters;

/**
 * Capability of an InputOutput of finer grained selection of a component.
 * <p>
 * InputOutput.select() does too much.
 * @author ivan
 * @since 1.23
 */
public abstract class IOSelect {

    /**
     * Additional operations to perform when issuing {@link IOSelect#select}.
     * @author ivan
     */
    public static enum AdditionalOperation {
	/**
	 * Additionally issue open() on the TopComponent containing the InputOutput.
	 */
	OPEN,

	/**
	 * Additionally issue requestVisible() on the TopComponent containing the InputOutput.
	 */
	REQUEST_VISIBLE,

	/**
	 * Additionally issue requestActive() on the TopComponent containing the InputOutput.
	 */
	REQUEST_ACTIVE
    }

    private static IOSelect find(InputOutput io) {
        if (io instanceof Lookup.Provider) {
            Lookup.Provider p = (Lookup.Provider) io;
            return p.getLookup().lookup(IOSelect.class);
        }
        return null;
    }

    /**
     * With an empty 'extraOps' simply selects this io
     * without involving it's containing TopComponent.
     * <p>
     * For example:
     * <pre>
     * if (IOSelect.isSupported(io) {
     *     IOSelect.select(io, EnumSet.noneOf(IOSelect.AdditionalOperation.class));
     * }
     * </pre>
     * <p>
     * If this capability is not supported then regular InputOutput.select()
     * will be called.
     * @param io InputOutput to operate on.
     * @param extraOps Additional operations to apply to the containing
     * TopComponent.
     */
    public static void select(InputOutput io, Set<AdditionalOperation> extraOps) {
	Parameters.notNull("extraOps", extraOps);	// NOI18N
	IOSelect ios = find(io);
	if (ios != null)
	    ios.select(extraOps);
	else
	    io.select();	// fallback
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
     * With an empty 'extraOps' simply selects this io
     * without involving it's containing TopComponent.
     * @param extraOps Additional operations to apply to the containing
     * TopComponent.
     */
    protected abstract void select(Set<AdditionalOperation> extraOps);
}
