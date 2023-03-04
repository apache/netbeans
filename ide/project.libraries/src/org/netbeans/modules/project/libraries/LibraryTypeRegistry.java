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
package org.netbeans.modules.project.libraries;

import javax.swing.event.ChangeListener;
import org.netbeans.spi.project.libraries.LibraryTypeProvider;
import org.openide.util.ChangeSupport;
import org.openide.util.Lookup;

public abstract class LibraryTypeRegistry {

    private static LibraryTypeRegistry instance;
    private final ChangeSupport changeSupport;

    protected LibraryTypeRegistry () {
        this.changeSupport = new ChangeSupport(this);
    }

    public abstract LibraryTypeProvider[] getLibraryTypeProviders ();

    public final LibraryTypeProvider getLibraryTypeProvider (String libraryType) {
        assert libraryType != null;
        final LibraryTypeProvider[] providers = getLibraryTypeProviders();
        for (LibraryTypeProvider provider : providers) {
            if (libraryType.equals(provider.getLibraryType())) {
                return provider;
            }
        }
        return null;
    }


    public final void addChangeListener (final ChangeListener listener) {
        assert listener != null;
        this.changeSupport.addChangeListener(listener);
    }

    public final void removeChangeListener (final ChangeListener listener) {
        assert listener != null;
        this.changeSupport.removeChangeListener(listener);
    }

    protected final void fireChange() {
        this.changeSupport.fireChange();
    }

    public static synchronized LibraryTypeRegistry getDefault () {
        if (instance == null) {
            instance = Lookup.getDefault().lookup(LibraryTypeRegistry.class);
            if (instance == null) {
                throw new IllegalStateException("No LibraryTypeRegistry in default Lookup");   //NOI18N
            }
        }
        return instance;
    }

}
