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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Allan Gregersen
 */
public final class JaveleonModule extends StandardModule {

    private static final Logger LOG = Logger.getLogger(JaveleonModule.class.getName());

    public static final boolean isJaveleonPresent;
    private static final Method incrementGlobalId;
    private static final Method registerClassLoader;
    static {
        Method _incrementGlobalId = null;
        Method _registerClassLoader = null;
        try {
            _incrementGlobalId = Class.forName("org.javeleon.reload.ReloadModule").getDeclaredMethod("incrementGlobalId");
            _registerClassLoader = Class.forName("org.javeleon.reload.ReloadFacade").getDeclaredMethod("registerClassLoader", ClassLoader.class, String.class);
        } catch (ClassNotFoundException x) {
            // Javeleon was not present... nothing to do then!
        } catch (Exception x) {
            LOG.log(Level.INFO, "Could not load Javeleon integration", x);
        }
        isJaveleonPresent = _incrementGlobalId != null && _registerClassLoader != null;
        incrementGlobalId = _incrementGlobalId;
        registerClassLoader = _registerClassLoader;
    }

    public static boolean incrementGlobalId() {
        assert isJaveleonPresent;
        try {
            incrementGlobalId.invoke(null);
            return true;
        } catch (Exception x) {
            LOG.log(Level.INFO, "Could not reload", x);
            return false;
        }
    }


    /**
     * Registers a module class loader according to module CNB.
     * No-op if {@link #isJaveleonPresent} is false (no need to guard).
     */
    static void registerClassLoader(ClassLoader loader, String codeNameBase) {
        if (isJaveleonPresent) {
            try {
                registerClassLoader.invoke(null, loader, codeNameBase);
            } catch (Exception x) {
                LOG.log(Level.INFO, "Could not register " + codeNameBase, x);
            }
        }
    }

    private static HashMap<String,ClassLoader> currentClassLoaders = new HashMap<String, ClassLoader>();


    public JaveleonModule(ModuleManager mgr, File jar, Object history, Events ev) throws IOException {
        super(mgr, ev, jar, history, true, false, false);
        setEnabled(true);
    }

    @Override
    protected ClassLoader createNewClassLoader(List<File> classp, List<ClassLoader> parents) {
        ClassLoader cl = super.createNewClassLoader(classp, parents);
        currentClassLoaders.put(getCodeNameBase(), cl);
        return cl;
    }

    /** public for use from JaveleonModuleReloader */
    public @Override void classLoaderUp(Set<Module> parents) throws IOException {
        super.classLoaderUp(parents);
    }

    @Override
    protected ClassLoader getParentLoader(Module parent) {
        if(currentClassLoaders.containsKey(parent.getCodeNameBase()))
            return currentClassLoaders.get(parent.getCodeNameBase());
        else
            return parent.getClassLoader();
    }

    @Override
    public String toString() {
        return "Javeleon module " + getJarFile().toString();
    }

    @Override
    protected void classLoaderDown() {
        // do not touch the class loader... Javeleon system will handle it
    }

    @Override
    public final void reload() throws IOException {
        // Javeleon will do this
    }

    @Override
    protected void cleanup() {
        // do nothing
    }

    @Override
    public void destroy() {
        // do nothing
    }
}
