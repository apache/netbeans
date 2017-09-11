/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
            return SessionManager.getDefault().create(u, h, extradirs.toArray(new File[extradirs.size()]));
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
