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

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import org.netbeans.core.startup.layers.ModuleLayeredFileSystem;

import org.openide.filesystems.*;

import org.netbeans.core.startup.layers.SessionManager;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;

/** Default repository.
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.filesystems.Repository.class)
public final class NbRepository extends Repository {
    /** name of system folder to be located in the USER_DIR and HOME_DIR */
    static final String CONFIG_FOLDER = "config"; // NOI18N
    static {
        // make sure the factory for nbfs and other protocols is on
        Main.initializeURLFactory ();
    }

    /**
     * Create a repository based on the normal system file system.
     */
    public NbRepository () {
        super (createDefaultFileSystem());
    }

    /** Creates defalt file system.
    */
    private static FileSystem createDefaultFileSystem () {
        String systemDir = System.getProperty("system.dir"); // NOI18N
        
        if (systemDir != null) {
            // initialize the filesystem for this property 

            try {
                return SessionManager.getDefault().create(new File (systemDir), null, new File[0]);
            } catch (IOException ex) {
                ex.printStackTrace();
                throw new InternalError ();
            } catch (PropertyVetoException ex) {
                ex.printStackTrace();
                throw new InternalError ();
            }
        }
        
        File u = null;
        File h = null;
        List<File> extradirs = new ArrayList<File>();
        String homeDir = CLIOptions.getHomeDir ();
        if (homeDir != null) {
            // -----------------------------------------------------------------------------------------------------
            // 1. Initialization and checking of netbeans.home and netbeans.user directories

            File homeDirFile = new File (CLIOptions.getHomeDir ());
            if (!homeDirFile.exists ()) {
                System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome_notexists"));
                doExit (2);
            }
            if (!homeDirFile.isDirectory ()) {
                System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome1"));
                doExit (3);
            }

            h = new File (homeDirFile, CONFIG_FOLDER);
            
            // #27151: may also be additional install dirs
            String nbdirs = System.getProperty("netbeans.dirs");
            if (nbdirs != null) {
                StringTokenizer tok = new StringTokenizer(nbdirs, File.pathSeparator);
                while (tok.hasMoreTokens()) {
                    File f = new File(tok.nextToken(), CONFIG_FOLDER);
                    if (f.isDirectory()) {
                        extradirs.add(f);
                    }
                }
            }
        }
        String ud = CLIOptions.getUserDir ();
        MEMORY: if (!ud.equals("memory")) { // NOI18N
            File userDirFile = new File (ud);
            if (!userDirFile.exists ()) {
                System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome2"));
                if (CLIOptions.isFallbackToMemory()) {
                    break MEMORY;
                }
                doExit (4);
            }
            if (!userDirFile.isDirectory ()) {
                System.err.println (NbBundle.getMessage(NbRepository.class, "CTL_Netbeanshome3"));
                if (CLIOptions.isFallbackToMemory()) {
                    break MEMORY;
                }
                doExit (5);
            }
            u = new File (userDirFile, CONFIG_FOLDER);
        }

        Exception exc;
        try {
            return SessionManager.getDefault().create(u, h, extradirs.toArray(new File[0]));
        } catch (IOException ex) {
            exc = ex;
        } catch (PropertyVetoException ex) {
            exc = ex;
        } catch (RuntimeException ex) {
            exc = ex;
        }

        exc.printStackTrace ();
        System.err.println(NbBundle.getMessage(NbRepository.class, "CTL_Cannot_mount_system_fs"));
        doExit (3);
        return null;
    }

    
    //
    // methods that delegate to TM
    //
    
    private static void doExit (int value) {
        TopLogging.exit(value);
    }

    public List<URL> additionalLayers(List<URL> urls) {
        for (LayerProvider p : Lookup.getDefault().lookupAll(LayerProvider.class)) {
            List<URL> mix = new ArrayList<URL>(urls);
            mix.addAll(findLayers(p));
            urls = mix;
        }
        return urls;
    }

    @Override
    protected void refreshAdditionalLayers() {
        Main.getModuleSystem().getManager().mutex().writeAccess(new Mutex.Action<Void>() {
            @Override
            public Void run() {
                try {
                    ModuleLayeredFileSystem.getInstallationModuleLayer().setURLs(null);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
                return null;
            }
        });
    }
}
