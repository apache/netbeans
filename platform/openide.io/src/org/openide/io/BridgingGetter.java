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
package org.openide.io;

import java.util.Collection;
import org.netbeans.spi.io.InputOutputProvider;
import org.openide.util.Lookup;
import org.openide.windows.IOProvider;

/**
 * Class for private contract between new api.io and this module. The new API
 * can retrieve this class using reflection and ask it for implementations of
 * output window, converted (bridged) to new SPI interface.
 *
 * @author jhavlin
 */
public class BridgingGetter {

    /**
     * Find the first IOProvider in default lookup and wrap it to
 BridgingInputOutputProvider.
     *
     * @return The default implementation of IOProvider from default lookup (not
     * the trivial one), or null if not available.
     */
    public InputOutputProvider<?, ?, ?, ?> getDefault() {
        IOProvider io = Lookup.getDefault().lookup(IOProvider.class);
        return io == null ? null : new BridgingInputOutputProvider(io);
    }

    /**
     * Find IOProvider of given name and wrap it to BridgingInputOutputProvider.
     *
     * @return IOProvider with specified name, or null if not available.
     */
    public InputOutputProvider<?, ?, ?, ?> get(String name) {
        if (name == null) {
            throw new NullPointerException(
                    "Provider name cannot be null");                    //NOI18N
        }
        Collection<? extends IOProvider> providers
                = Lookup.getDefault().lookupAll(IOProvider.class);
        for (IOProvider p : providers) {
            if (name.equals(p.getName())) {
                return new BridgingInputOutputProvider(p);
            }
        }
        return null;
    }
}
