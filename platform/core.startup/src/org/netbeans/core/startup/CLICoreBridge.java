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

package org.netbeans.core.startup;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.CLIHandler;
import org.netbeans.Module;
import org.netbeans.core.startup.layers.ModuleLayeredFileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Handler for core.jar options.
 * @author Jaroslav Tulach
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.CLIHandler.class)
public class CLICoreBridge extends CLIHandler {
    /**
     * Create a default handler.
     */
    public CLICoreBridge() {
        super(WHEN_INIT);
    }
    
    protected int cli(Args arguments) {
        Lookup clis = Lookup.getDefault();
        Collection<? extends CLIHandler> handlers = clis.lookupAll(CLIHandler.class);
        int h = notifyHandlers(arguments, handlers, WHEN_EXTRA, true, true);
        if (h == 0) {
            h = CoreBridge.getDefault().cli(
                arguments.getArguments(),
                arguments.getInputStream(),
                arguments.getOutputStream(),
                arguments.getErrorStream(),
                arguments.getCurrentDirectory()
            );
        }
        return h;
    }

    protected void usage(PrintWriter w) {
        if (MainLookup.isStarted()) {
            Lookup clis = Lookup.getDefault();
            Collection<? extends CLIHandler> handlers = clis.lookupAll(CLIHandler.class);
            showHelp(w, handlers, WHEN_EXTRA);
            w.flush();
            return;
        }

        CLIOptions.fallbackToMemory();
        ModuleSystem moduleSystem;
        try {
            moduleSystem = new ModuleSystem(FileUtil.getConfigRoot().getFileSystem());
        } catch (IOException ioe) {
            // System will be screwed up.
            throw new IllegalStateException("Module system cannot be created", ioe); // NOI18N
        }

//        moduleSystem.loadBootModules();
        moduleSystem.readList();
        
        
        ArrayList<URL> urls = new ArrayList<URL>();
        for (Module m : moduleSystem.getManager().getModules()) {
            for (File f : m.getAllJars()) {
                try {
                    urls.add(BaseUtilities.toURI(f).toURL());
                }
                catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        
        URLClassLoader loader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
        MainLookup.systemClassLoaderChanged(loader);
        try {
            final List<URL> layers = ModuleLayeredFileSystem.collectLayers(loader);
            XMLFileSystem xfs = new XMLFileSystem();
            xfs.setXmlUrls(layers.toArray(new URL[0]));
            MainLookup.register(xfs);
            MainLookup.modulesClassPathInitialized(Lookups.forPath("Services/OptionProcessors")); // NOI18N
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
        Lookup clis = Lookup.getDefault();
        Collection<? extends CLIHandler> handlers = clis.lookupAll(CLIHandler.class);
        showHelp(w, handlers, WHEN_EXTRA);
        w.flush();
    }
}
