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

package org.openide.modules;

import org.openide.util.Lookup;

/**
 * Information about the set of available {@linkplain ModuleInfo modules}.
 * @since org.openide.modules 7.19
 */
public class Modules {
    /**
     * Constructor for subclasses.
     */
    protected Modules() {}
    
    /**
     * Gets the singleton set of modules.
     * An implementation of this service should be registered by the module system.
     * The fallback implementation implements {@link #ownerOf} using a linear search.
     * @return the default instance
     */
    public static Modules getDefault() {
        Modules impl = Lookup.getDefault().lookup(Modules.class);
        if (impl == null) {
            impl = new Modules();
        }
        return impl;
    }

    /* Finds a module with given code name base.
     * @param cnb the {@link ModuleInfo#getCodeNameBase() code name base} of a module
     * @return the found module or <code>null</code>, if such module is not known
     *   to the system
     * @since 7.37
     */
    public ModuleInfo findCodeNameBase(String cnb) {
        for (ModuleInfo module : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (cnb.equals(module.getCodeNameBase())) {
                return module;
            }
        }
        return null;
    }
    
    /**
     * Finds the module which loaded a class.
     * @param clazz a class
     * @return the owner of the class, or null if it is not owned by any module
     * @see ModuleInfo#owns
     */
    public ModuleInfo ownerOf(Class<?> clazz) {
        for (ModuleInfo module : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
            if (module.owns(clazz)) {
                return module;
            }
        }
        return null;
    }
}
