/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.project.libraries;

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = LibraryTypeRegistry.class)
public final class LibraryTypeRegistryImpl extends LibraryTypeRegistry {
    private static final String REGISTRY = "org-netbeans-api-project-libraries/LibraryTypeProviders";              //NOI18N
    private static final Logger LOG = Logger.getLogger(LibraryTypeRegistryImpl.class.getName());

    private final Lookup.Result<LibraryTypeProvider> result;

    public LibraryTypeRegistryImpl() {
        final Lookup lookup = Lookups.forPath(REGISTRY);
        assert lookup != null;
        result = lookup.lookupResult(LibraryTypeProvider.class);
        result.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                fireChange();
            }
        });
    }

    public LibraryTypeProvider[] getLibraryTypeProviders () {
        assert result != null;
        final Collection<? extends LibraryTypeProvider> instances = result.allInstances();
        LOG.log(Level.FINE, "found providers: {0}", instances);
        return instances.toArray(new LibraryTypeProvider[0]);
    }
}
