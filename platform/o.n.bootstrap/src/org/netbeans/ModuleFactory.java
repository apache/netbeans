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

package org.netbeans;

import java.io.File;
import java.io.IOException;
import java.util.jar.Manifest;

/**
 * Allows creation of custom modules. The factories are searched in
 * the default lookup (org.openide.util.Lookup.getDefault()). If there is one
 * it is used - if there are more of them arbitrary one is used (so please make
 * sure that there is only one present in the installation). If there is none
 * in the default lookup the system will use an instance of this class.
 *
 * @author David Strupl
 */
public class ModuleFactory {

    /**
     * This method creates a "standard" module. Standard modules can be
     * disabled, reloaded, autoloaded (loaded only when needed).
     * @see StandardModule
     */
    public Module create(File jar, Object history, boolean reloadable,
            boolean autoload, boolean eager, ModuleManager mgr, Events ev)
    throws IOException {
        final Boolean osgiStatus = mgr.isOSGi(jar);
        if (Boolean.TRUE.equals(osgiStatus)) {
            return new NetigsoModule(null, jar, mgr, ev, history, reloadable, autoload, eager);
        }
        Module m;
        try {
            m = new StandardModule(mgr, ev, jar, history, reloadable, autoload, eager);
            if (osgiStatus == null) {
                m.dataWithCheck();
            }
        } catch (InvalidException ex) {
            Manifest mani = ex.getManifest();
            if (mani != null) {
                String name = mani.getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
                if (name == null) {
                    throw ex;
                }
                m = new NetigsoModule(mani, jar, mgr, ev, history, reloadable, autoload, eager);
                if (osgiStatus == null) {
                    m.dataWithCheck();
                }
            } else {
                throw ex;
            }
        }
        return m;
    }
    
    /**
     * This method creates a "fixed" module. Fixed modules cannot be
     * realoaded, are always enabled and are typically present on the
     * classpath.
     * @see FixedModule
     * @since 2.7
     */
    public Module createFixed(Manifest mani, Object history, ClassLoader loader, boolean autoload, boolean eager,
            ModuleManager mgr, Events ev) throws InvalidException {
        Module m = new FixedModule(mgr, ev, mani, history, loader, autoload, eager);
        return m;
    }
    /**
     * Allows specifying different parent classloader of all modules classloaders.
     */
    public ClassLoader getClasspathDelegateClassLoader(ModuleManager mgr, ClassLoader del) {
        return del;
    }
    
    /**
     * If this method returns true the parent the original classpath
     * classloader will be removed from the parent classloaders of a module classloader.
     */
    public boolean removeBaseClassLoader() {
        return false;
    }
    
}
