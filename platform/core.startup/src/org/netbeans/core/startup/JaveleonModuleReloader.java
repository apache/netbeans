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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import javax.swing.SwingUtilities;
import org.netbeans.Events;
import org.netbeans.InvalidException;
import org.netbeans.JaveleonModule;
import org.netbeans.Module;
import org.netbeans.ModuleManager;
import org.netbeans.Util;
import org.openide.modules.Dependency;
import org.openide.util.Exceptions;

/**
 *
 * @author Allan Gregersen
 */
class JaveleonModuleReloader {

    private static JaveleonModuleReloader reloader = new JaveleonModuleReloader();

    static JaveleonModuleReloader getDefault() {
        return reloader;
    }

    /** This map ensures that the layer handling done by
     * NBInstaller.loadLayer()is consistent with the modules
     * registered with the currently installed layers.
     */
    private HashMap<String, Module> registeredModules = new HashMap<String, Module>();

    // Use JaveleonModuleReloader.getDefault() to get the singleton instance
    private JaveleonModuleReloader() {
    }

    boolean reloadJaveleonModule(File jar, ModuleManager mgr, NbInstaller installer, Events ev) throws IOException {
        if (!JaveleonModule.incrementGlobalId()) {
            // oops, we shouldn't end up in here, since Javeleon was
            // supposed to be present given the above test succeeeded!
            // Oh well, just fall back to normal reload operation then
            return false;
        }
        System.err.println("Start Javeleon module update...");

        // the existing module if any
        Module m = null;
        // the new updated module
        JaveleonModule tm = null;
        // Anything that needs to have class loaders refreshed
        List<Module> dependents;
        // First see if this refers to an existing module.
        for (Module module : mgr.getModules()) {
            if (module.getJarFile() != null) {
                if (jar.equals(module.getJarFile())) {
                    // Hah, found it.
                    m = module;
                    tm = createJaveleonModule(mgr, jar, new ModuleHistory(jar.getAbsolutePath()));
                    break;
                }
            }
        }
        if(m == null) {
            return false;
        }

        // now find dependent modules which need to be class loader migrated
        dependents = mgr.simulateJaveleonReload(m);

        // setup the class loader for the new Javeleon module
        // That's all we need to do to update the module with Javeleon!
        setupClassLoaderForJaveleonModule(mgr, tm);
        refreshLayer(m, tm, installer, mgr);

        // OK so far, then create new Javeleon modules for the
        // dependent modules and create new classloaders for
        // them as well
        for (Module m3 : dependents) {
            File moduleJar = m3.getJarFile();
            JaveleonModule toRefresh = createJaveleonModule(mgr, moduleJar, new ModuleHistory(moduleJar.getAbsolutePath()));
            setupClassLoaderForJaveleonModule(mgr, toRefresh);
            refreshLayer(m3, toRefresh, installer, mgr);
        }
        // done...      
        System.err.println("Javeleon finished module update...");
        MainLookup.systemClassLoaderChanged(mgr.getClassLoader());
        ev.log(Events.FINISH_DEPLOY_TEST_MODULE, jar);
        return true;
    }

    private JaveleonModule createJaveleonModule(ModuleManager mgr, File jar, Object history) throws IOException {
        try {
            return new JaveleonModule(mgr, jar.getAbsoluteFile(), history, mgr.getEvents());
        } catch (IOException ex) {
            System.err.println("EXCEPTION IN MGR.createJav...");
            throw ex;
        }
    }

    private void setupClassLoaderForJaveleonModule(ModuleManager mgr, JaveleonModule javeleonModule) throws InvalidException {
        try {
            // Calculate the parents to initialize the classloader with.
            Dependency[] dependencies = javeleonModule.getDependenciesArray();
            Set<Module> parents = new HashSet<Module>(dependencies.length * 4 / 3 + 1);
            for (Dependency dep : dependencies) {
                if (dep.getType() != Dependency.TYPE_MODULE) {
                    // Token providers do *not* go into the parent classloader
                    // list. The providing module must have been turned on first.
                    // But you cannot automatically access classes from it.
                    continue;
                }
                String name = (String) Util.parseCodeName(dep.getName())[0];
                Module parent = mgr.get(name);
                // Should not happen:
                if (parent == null) {
                    throw new IOException("Parent " + name + " not found!"); // NOI18N
                }
                parents.add(parent);
            }
            javeleonModule.classLoaderUp(parents);
//            classLoader.append(new ClassLoader[]{javeleonModule.getClassLoader()});
        } catch (IOException ioe) {
            InvalidException ie = new InvalidException(javeleonModule, ioe.toString());
            ie.initCause(ioe);
            throw ie;
        }
    }

    private Set</*TopComponent*/?> getOpenTopComponents(ClassLoader loader) {
         try {
            Class<?> classWindowManager = loader.loadClass("org.openide.windows.WindowManager");
            Object manager = classWindowManager.getMethod("getDefault").invoke(null);
            Object registry = classWindowManager.getMethod("getRegistry").invoke(manager);
            Class<?> classRegistry = loader.loadClass("org.openide.windows.TopComponent$Registry");
            return (Set) classRegistry.getMethod("getOpened").invoke(registry);
        } catch (Exception ex) {
            //Exceptions.printStackTrace(ex);
            return Collections.emptySet();
        }
    }

    private void restoreOpenTopComponents(final ClassLoader loader, final Set</*TopComponent*/?> openTCs) {
        if (openTCs == null || openTCs.isEmpty()) {
            return;
        }

        // TopComponent.open must be called from the AWT thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Class<?> classTopComponent = loader.loadClass("org.openide.windows.TopComponent");
                    for (Object topComponent : openTCs) {
                        classTopComponent.getMethod("open").invoke(topComponent);
                    }
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

            }
        });
    }

    private void refreshLayer(Module original, Module newModule, NbInstaller installer, ModuleManager mgr) {
        try {            
            boolean changed = layersChanged(original, newModule);
            Set</*TopComponent*/?> openTCs = null;
            // Always refresh the layer. Exsitng instances created from the
            // layer will be retained and their identity preserved in the updated
            // module.

            if (changed) {
                openTCs = getOpenTopComponents(mgr.getClassLoader());
                Module registeredModule = getAndClearRegisteredModule(original);
                installer.loadLayers(Collections.singletonList(registeredModule), false);
                //installer.unload(Collections.singletonList(registeredModule));
                installer.dispose(registeredModule);
            }
            mgr.replaceJaveleonModule(original, newModule);
            
            if (changed) {
                installer.prepare(newModule);
                installer.loadLayers(Collections.singletonList(newModule), true);
                //installer.load(Collections.singletonList(newModule));
                registerModule(newModule);
                
                restoreOpenTopComponents(mgr.getClassLoader(), openTCs);
            }
            if (!changed && !(original instanceof JaveleonModule)) {
                // make sure to register the original module for later unloading
                // of the original module that installed the layer.
                registerModule(original);
            }
        }
        catch (InvalidException ex) {
            // shouldn't happen ever
        }
        catch (Throwable ex) {
            ex.printStackTrace(System.err);
        }
    }

    private boolean layersChanged(Module m1, Module m2) {
        return ((CRC32Layer(m1) != CRC32Layer(m2)) || (CRC32GeneratedLayer(m1) != CRC32GeneratedLayer(m2)));
    }

    private long calculateChecksum(URL layer) {
        if (layer == null) {
            return -1;
        }
        try {
            InputStream is = layer.openStream();
            try {
                CheckedInputStream cis = new CheckedInputStream(is, new CRC32());
                // Compute the CRC32 checksum
                byte[] buf = new byte[1024];
                while (cis.read(buf) >= 0) {
                }
                cis.close();
                return cis.getChecksum().getValue();
            } finally {
                is.close();
            }
        } catch (IOException e) {
            return -1;
        }
    }

    private long CRC32Layer(Module m) {
        String layerResource = m.getManifest().getMainAttributes().getValue("OpenIDE-Module-Layer"); // NOI18N
        String osgi = m.getManifest().getMainAttributes().getValue("Bundle-SymbolicName"); // NOI18N
        if (layerResource != null && osgi == null) {
            URL layer = m.getClassLoader().getResource(layerResource);
            return calculateChecksum(layer);
        }
        return -1;
    }

    private long CRC32GeneratedLayer(Module m) {
        String layerRessource = "META-INF/generated-layer.xml"; // NOI18N
        URL layer = m.getClassLoader().getResource(layerRessource);
        return calculateChecksum(layer);
    }

    private Module getAndClearRegisteredModule(Module original) {
        return (registeredModules.containsKey(original.getCodeNameBase())) ?
            registeredModules.remove(original.getCodeNameBase()) :
            original;
    }

    private void registerModule(Module newModule) {
        registeredModules.put(newModule.getCodeNameBase(), newModule);
    }

}
