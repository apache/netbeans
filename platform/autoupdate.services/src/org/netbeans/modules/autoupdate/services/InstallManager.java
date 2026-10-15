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

package org.netbeans.modules.autoupdate.services;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.OperationException;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.core.startup.MainLookup;
import org.netbeans.core.startup.layers.LocalFileSystemEx;
import org.netbeans.spi.autoupdate.AutoupdateClusterCreator;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Jiri Rechtacek
 */
@ServiceProvider(service=InstalledFileLocator.class)
public class InstallManager extends InstalledFileLocator{
    
    // special directories in NB files layout
    static final String NBM_LIB = "lib"; // NOI18N
    static final String NBM_CORE = "core"; // NOI18N
    static final String NETBEANS_DIRS = "netbeans.dirs"; // NOI18N

    private static int countOfWarnings = 0;
    private static final int MAX_COUNT_OF_WARNINGS = 5;

    private static final Logger ERR = Logger.getLogger ("org.netbeans.modules.autoupdate.services.InstallManager");
    private static final List<File> clusters = new ArrayList<File>();
    
    static File findTargetDirectory (UpdateElement installed, UpdateElementImpl update, Boolean globalOrLocal, boolean useUserdirAsFallback) throws OperationException {
        File res;
        if (globalOrLocal == null) {
            globalOrLocal = isGlobalInstallation();
        }
        boolean isGlobal = globalOrLocal == null ? false : globalOrLocal;
        
        if (Boolean.FALSE.equals(globalOrLocal)) {
            ERR.log(Level.INFO, "Forced installation in userdir only for " + update.getUpdateElement());
            return getUserDir();
        }
        
        // if an update, overwrite the existing location, wherever that is.
        if (installed != null) {
            
            // adjust isGlobal to forced global if present
            isGlobal |= update.getInstallInfo ().isGlobal () != null && update.getInstallInfo ().isGlobal ();
            res = getInstallDir (installed, update, isGlobal, useUserdirAsFallback);
            
        } else {

            // #111384: fixed modules must be installed globally
            isGlobal |= update.isFixed ();

            // adjust isGlobal to forced global if present
            isGlobal |= update.getInstallInfo ().isGlobal () != null && update.getInstallInfo ().isGlobal ();
            
            final String targetCluster = update.getInstallInfo ().getTargetCluster ();

            // global or local
            if ((targetCluster != null && targetCluster.length () > 0) || isGlobal) {
                res = checkTargetCluster(update, targetCluster, isGlobal, useUserdirAsFallback);
                
                // handle non-existing clusters
                if (res == null && targetCluster != null) {
                    res = createNonExistingCluster (targetCluster);
                    if (res != null) {
                        res = checkTargetCluster(update, targetCluster, isGlobal, useUserdirAsFallback);
                    }
                }
                
                // target cluster still not found
                if (res == null) {
                    
                    // create UpdateTracking.EXTRA_CLUSTER_NAME
                    createNonExistingCluster (UpdateTracking.EXTRA_CLUSTER_NAME);
                    // check writable installation
                    res = checkTargetCluster(update, UpdateTracking.EXTRA_CLUSTER_NAME, isGlobal, useUserdirAsFallback);
                    
                    // no new cluster was created => use userdir
                    res = res == null? getUserDir () : res;
                    
                    if (targetCluster != null) {
                        ERR.log (Level.INFO, "Declared target cluster " + targetCluster + 
                                " in " + update.getUpdateElement () + " wasn't found or was read only. Will be used " + res);
                    } else {
                        ERR.log (Level.INFO, res + " will be used as target cluster");
                    }
                    
                }
                
            } else {
                // is local
                res = getUserDir ();
            }
        }
        ERR.log (Level.FINEST, "UpdateElement " + update.getUpdateElement () + " has the target cluster " + res);
        return res;
    }
    
    private static File checkTargetCluster(UpdateElementImpl update, String targetCluster, boolean isGlobal, boolean useUserdirAsFallback) throws OperationException {
        if (targetCluster == null || targetCluster.length () == 0) {
            return null;
        }
        File res = null;
        // is global or
        // does have a target cluster?
        for (File cluster : UpdateTracking.clusters (true)) {
            if (targetCluster.equals (cluster.getName ())) {
                boolean wasNew = ! cluster.exists ();
                if (Utilities.canWriteInCluster (cluster)) {
                    if (wasNew) {
                        cluster.mkdirs ();
                        extendSystemFileSystem (cluster);
                    }
                    res = cluster;
                } else {
                    if (! useUserdirAsFallback && isGlobal) {
                        ERR.log(Level.WARNING, "There is no write permission to write in target cluster " + targetCluster + " for " + update.getUpdateElement());
                        throw new OperationException(OperationException.ERROR_TYPE.WRITE_PERMISSION, update.getCodeName());
                    }
                    if (countOfWarnings++ < MAX_COUNT_OF_WARNINGS) {
                        ERR.log(Level.WARNING, "There is no write permission to write in target cluster " + targetCluster + " for " + update.getUpdateElement());
                    }
                    if (countOfWarnings == MAX_COUNT_OF_WARNINGS) {
                        ERR.log(Level.WARNING, "There is no write permission to write in target cluster " + targetCluster + " for more updates or plugins.");
                    }
                }
                break;
            }
        }

        return res;
    }
    
    private static File createNonExistingCluster (String targetCluster) {
        File res = null;
        for (AutoupdateClusterCreator creator : Lookup.getDefault ().lookupAll (AutoupdateClusterCreator.class)) {
            File possibleCluster = Trampoline.SPI.findCluster (targetCluster, creator);
            if (possibleCluster != null) {
                try {
                    ERR.log (Level.FINE, "Found cluster candidate " + possibleCluster + " for declared target cluster " + targetCluster);
                    File[] dirs = Trampoline.SPI.registerCluster (targetCluster, possibleCluster, creator);

                    // it looks good, generate new netbeans.dirs
                    res = possibleCluster;

                    StringBuffer sb = new StringBuffer ();
                    String sep = "";
                    for (File dir : dirs) {
                        sb.append (sep);
                        sb.append(dir.getPath());
                        sep = File.pathSeparator;
                    }

                    System.setProperty(NETBEANS_DIRS, sb.toString ());
                    File f = new File(new File(getUserDir(), Utilities.DOWNLOAD_DIR), NETBEANS_DIRS);
                    if (!f.exists()) {
                        f.getParentFile().mkdirs();
                        f.createNewFile();
                    }
                    OutputStream os = new FileOutputStream(f);
                    try {
                        os.write(sb.toString().getBytes());
                    } finally {
                        os.close();
                    }
                    ERR.log (Level.FINE, "Was written new netbeans.dirs " + sb);

                    break;

                } catch (IOException ioe) {
                    ERR.log (Level.INFO, ioe.getMessage (), ioe);
                }
            }
        }
        return res;
    }

    private static void extendSystemFileSystem(File cluster) {
        try {
            File extradir = new File(cluster, ModuleDeactivator.CONFIG);
            extradir.mkdir();
            LocalFileSystemEx lfse = new LocalFileSystemEx();
            lfse.setRootDirectory(extradir);
            MainLookup.register(lfse);
            synchronized (InstallManager.class) {
                clusters.add(cluster);
            }
        } catch (PropertyVetoException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        } catch (IOException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        }
    }
    
    // can be null for fixed modules
    private static File getInstallDir (UpdateElement installed, UpdateElementImpl update, boolean isGlobal, boolean useUserdirAsFallback) throws OperationException {
        File res = null;
        UpdateElementImpl i = Trampoline.API.impl (installed);
        assert i instanceof ModuleUpdateElementImpl : "Impl of " + installed + " instanceof ModuleUpdateElementImpl";
        
        Module m = Utilities.toModule (((ModuleUpdateElementImpl) i).getModuleInfo ());
        File jarFile = m == null ? null : m.getJarFile ();
        
        if (jarFile == null) {
            // only fixed module cannot be located
            ERR.log (Level.FINE, "No install dir for " + installed + " (It's ok for fixed). Is fixed? " + Trampoline.API.impl (installed).isFixed ());
            String targetCluster = update.getInstallInfo ().getTargetCluster ();
            if (targetCluster != null) {
                for (File cluster : UpdateTracking.clusters (false)) {
                    if (targetCluster.equals (cluster.getName ())) {
                        res = cluster;
                        break;
                    }
                }
            }
            if (res == null) {
                // go to platform if no cluster is known
                res = UpdateTracking.getPlatformDir ();
            }
        } else {
            
            /* comment out for xtesting
            FileObject searchForFO = FileUtil.toFileObject (configFile);
            for (File cluster : UpdateTracking.clusters (true)) {       
                cluster = FileUtil.normalizeFile(cluster);
                if (FileUtil.isParentOf (FileUtil.toFileObject (cluster), searchForFO)) {
                    res = cluster;
                    break;
                }*/
            
            for (File cluster : UpdateTracking.clusters (true)) {       
                cluster = FileUtil.normalizeFile (cluster);
                if (isParentOf (cluster, jarFile)) {
                    res = cluster;
                    break;
                }
            }
        }

        if (res == null || ! Utilities.canWriteInCluster (res)) {
            if (! useUserdirAsFallback && isGlobal) {
                ERR.log(Level.WARNING, "There is no write permission to write in target cluster " + res + " for " + update.getUpdateElement());
                throw new OperationException(OperationException.ERROR_TYPE.WRITE_PERMISSION, update.getCodeName());
            }
            // go to userdir if no writable cluster is known
            if (countOfWarnings++ < MAX_COUNT_OF_WARNINGS) {
                ERR.log(Level.WARNING, "There is no write permission to write in target cluster " + res + " for " + update.getUpdateElement());
            }
            if (countOfWarnings == MAX_COUNT_OF_WARNINGS) {
                ERR.log(Level.WARNING, "There is no write permission to write in target cluster " + res + " for more updates or plugins.");
            }
            res = UpdateTracking.getUserDir ();
        }
        ERR.log (Level.FINEST, "Install dir of " + installed + " is " + res);
        
        return res;
    }
    
    private static boolean isParentOf (File parent, File child) {
        File tmp = child.getParentFile ();
        while (tmp != null && ! parent.equals (tmp)) {
            tmp = tmp.getParentFile ();
        }
        return tmp != null;
    }
    
    static File getUserDir () {
        return UpdateTracking.getUserDir ();
    }
    
    static boolean needsRestart (boolean isUpdate, UpdateElementImpl update, File dest) {
        assert update.getInstallInfo () != null : "Each UpdateElement must know own InstallInfo but " + update;
        boolean isForcedRestart = update.getInstallInfo ().needsRestart () != null && update.getInstallInfo ().needsRestart ();
        boolean needsRestart = isForcedRestart || isUpdate;
        if (! needsRestart) {
            // handle installation into core or lib directory
            needsRestart = willInstallInSystem (dest);
        }
        return needsRestart;
    }

    private static boolean willInstallInSystem (File nbmFile) {
        boolean res = false;
        try {
            JarFile jf = new JarFile (nbmFile);
            try {
                for (JarEntry entry : Collections.list (jf.entries ())) {
                    String entryName = entry.getName ();
                    if (entryName.startsWith (NBM_CORE + "/") || entryName.startsWith (NBM_LIB + "/")) {
                        res = true;
                        break;
                    }
                }
            } finally {
                jf.close();
            }
        } catch (IOException ioe) {
            ERR.log (Level.INFO, ioe.getMessage (), ioe);
        }
        
        return res;
    }

    @Override
    public File locate(String relativePath, String codeNameBase, boolean localized) {
        // Rarely returns anything so don't bother optimizing.
        Set<File> files = locateAll(relativePath, codeNameBase, localized);
        return files.isEmpty() ? null : files.iterator().next();
    }

    public @Override Set<File> locateAll(String relativePath, String codeNameBase, boolean localized) {
        synchronized (InstallManager.class) {
            if (clusters.isEmpty()) {
                return Collections.<File>emptySet();
            }
        }
        // XXX #28729: use codeNameBase to search only in the appropriate places
        if (relativePath.length() == 0) {
            throw new IllegalArgumentException("Cannot look up \"\" in InstalledFileLocator.locate"); // NOI18N
        }
        if (relativePath.charAt(0) == '/') {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not start with '/': " + relativePath); // NOI18N
        }
        int slashIdx = relativePath.lastIndexOf('/');
        if (slashIdx == relativePath.length() - 1) {
            throw new IllegalArgumentException("Paths passed to InstalledFileLocator.locate should not end in '/': " + relativePath); // NOI18N
        }
        
        String prefix, name;
        if (slashIdx != -1) {
            prefix = relativePath.substring(0, slashIdx + 1);
            name = relativePath.substring(slashIdx + 1);
            assert name.length() > 0;
        } else {
            prefix = "";
            name = relativePath;
        }
        if (localized) {
            int i = name.lastIndexOf('.');
            String baseName, ext;
            if (i == -1) {
                baseName = name;
                ext = "";
            } else {
                baseName = name.substring(0, i);
                ext = name.substring(i);
            }
            String[] suffixes = org.netbeans.Util.getLocalizingSuffixesFast();
            Set<File> files = new HashSet<File>();
            for (String suffixe : suffixes) {
                String locName = baseName + suffixe + ext;
                files.addAll(locateExactPath(prefix, locName));
            }
            return files;
        } else {
            return locateExactPath(prefix, name);
        }
        
    }

    /** Search all top dirs for a file. */
    private static Set<File> locateExactPath(String prefix, String name) {
        Set<File> files = new HashSet<File>();
        synchronized(InstallManager.class) {
            File[] dirs = clusters.toArray(new File[0]);
            for (File dir : dirs) {
                File f = makeFile(dir, prefix, name);
                if (f.exists()) {                    
                    files.add(f);
                }
            }            
        }        
        return files;
    }
    
    private static File makeFile(File dir, String prefix, String name) {        
        return FileUtil.normalizeFile(new File(dir, prefix.replace('/', File.separatorChar) + name));
    }
    
    private static Boolean isGlobalInstallation() {
        String s = System.getProperty("plugin.manager.install.global"); // NOI18N
        
        if (Boolean.parseBoolean(s)) {
            return Boolean.TRUE;
        } else if (Boolean.FALSE.toString().equalsIgnoreCase(s)) {
            return Boolean.FALSE;
        } else {
            return null;
        }
    }

}
