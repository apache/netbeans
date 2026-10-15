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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.autoupdate.updateprovider.InstalledModuleProvider;
import org.netbeans.updater.ModuleDeactivator;
import org.netbeans.updater.UpdateTracking;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.ModuleInfo;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/** Control if the module's file can be deleted and can delete them from disk.
 * <p> Deletes all files what are installed together with given module, info about
 * these files read from <code>update_tracking</code> file related to the module.
 * If this <code>update_tracking</code> doesn't exist the files cannot be deleted.
 * The Deleter waits until the module is enabled before start delete its files.
 *
 * @author  Jiri Rechtacek
 */
public final class ModuleEnableDisableDeleteHelper  {
    private static final ModuleEnableDisableDeleteHelper INSTANCE = new ModuleEnableDisableDeleteHelper();
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    
    private static final Logger err = Logger.getLogger (ModuleEnableDisableDeleteHelper.class.getName ()); // NOI18N
    
    private Set<File> storageFilesForDelete = null;
    
    public static ModuleEnableDisableDeleteHelper getInstance() {
        return INSTANCE;
    }
    
    public boolean canDelete (ModuleInfo moduleInfo) {
        if (moduleInfo == null) { // XXX: how come that moduleInfo is null?
            return false;
        }
        if (Utilities.isEssentialModule (moduleInfo)) {
            err.log(Level.FINE,
                    "Cannot delete module because module " +
                    moduleInfo.getCodeName() + " isEssentialModule.");
            return false;
        } else {
            return foundUpdateTracking (moduleInfo);
        }
    }
    
    public Collection<File> findControlFiles(Collection<ModuleInfo> modules, ProgressHandle handle) {
        if (modules == null) {
            throw new IllegalArgumentException ("ModuleInfo argument cannot be null.");
        }
        
        if (handle != null) {
            handle.switchToDeterminate (modules.size() + 1);
        }

        return doFindControlFiles(modules, handle);
    }

    private Collection<File> doFindControlFiles(Collection<ModuleInfo> modules, ProgressHandle handle) {
        Collection<File> configs = new HashSet<File> ();
        int i = 0;
        for (ModuleInfo moduleInfo : modules) {
            File config = locateConfigFile (moduleInfo);
            assert config != null : "Located config file for " + moduleInfo.getCodeName ();
            assert config.exists () : config + " config file must exists for " + moduleInfo.getCodeName ();
            err.log(Level.FINE, "Locate config file of " + moduleInfo.getCodeNameBase () + ": " + config);
            if(config!=null) {
                configs.add (config);
            }
            if (handle != null) {
                handle.progress (++i);
            }
        }

        return configs;
    }
    
    public Collection<File> markForDelete (Collection<ModuleInfo> modules, ProgressHandle handle) throws IOException {
        storageFilesForDelete = null;
        if (modules == null) {
            throw new IllegalArgumentException ("ModuleInfo argument cannot be null.");
        }
        
        if (handle != null) {
            handle.switchToDeterminate (modules.size () * 2 + 1);
        }

        Collection<File> configFiles = doFindControlFiles(modules, handle);
        int i = configFiles.size();

        getStorageFilesForDelete ().addAll (configFiles);
        
        for (ModuleInfo moduleInfo : modules) {
            removeModuleFiles(moduleInfo, true); 
            if (handle != null) {
                handle.progress (++i);
            }

        }
        return getStorageFilesForDelete ();
    }
    
    @SuppressWarnings("SleepWhileInLoop")
    public void delete (final ModuleInfo[] modules, ProgressHandle handle) throws IOException {
        storageFilesForDelete = null;
        if (modules == null) {
            throw new IllegalArgumentException ("ModuleInfo argument cannot be null.");
        }
        
        if (handle != null) {
            handle.switchToDeterminate (modules.length + 1);
        }
        int i = 0;
        
        for (ModuleInfo moduleInfo : modules) {
            err.log(Level.FINE,"Locate and remove config file of " + moduleInfo.getCodeNameBase ());
            removeControlModuleFile(moduleInfo, false);
        }

        if (handle != null) {
            handle.progress (++i);
        }
        
        refreshModuleList ();
        
        int rerunWaitCount = 0;
        for (ModuleInfo moduleInfo : modules) {
            err.log(Level.FINE, "Locate and remove config file of " + moduleInfo.getCodeNameBase ());                       
            if (handle != null) {
                handle.progress (moduleInfo.getDisplayName (), ++i);
            }
            for (; rerunWaitCount < 100 && !isModuleUninstalled(moduleInfo); rerunWaitCount++) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    err.log (Level.INFO, "Overflow checks of uninstalled module " + moduleInfo.getCodeName ());
                    Thread.currentThread().interrupt();
                }
            }
            removeModuleFiles(moduleInfo, false); 
        }
    }
    
    private boolean isModuleUninstalled(ModuleInfo moduleInfo) {
        return (InstalledModuleProvider.getInstalledModules ().get (moduleInfo.getCodeNameBase()) == null);
    }

    private File locateConfigFile (ModuleInfo m) {
        String configFile = ModuleDeactivator.CONFIG + '/' + ModuleDeactivator.MODULES + '/' + m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        return InstalledFileLocator.getDefault ().locate (configFile, m.getCodeNameBase (), false);
    }
    
    private Collection<File> locateAllConfigFiles (ModuleInfo m) {
        Collection<File> configFiles = new HashSet<File> ();
        String configFileName = m.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        for (File cluster : UpdateTracking.clusters (true)) {
            File configFile = new File (new File (new File (cluster, ModuleDeactivator.CONFIG), ModuleDeactivator.MODULES), configFileName);
            if (configFile.exists ()) {
                configFiles.add (configFile);
            }
        }
        return configFiles;
    }
    
    private void removeControlModuleFile (ModuleInfo m, boolean markForDelete) throws IOException {
        File configFile;
        while ((configFile = locateConfigFile (m)) != null && ! getStorageFilesForDelete ().contains (configFile)) {
            if (configFile != null && configFile.exists ()) {
                //FileUtil.toFileObject (configFile).delete ();
                if (markForDelete) {
                    err.log(Level.FINE, "Control file " + configFile + " is marked for delete.");
                    getStorageFilesForDelete ().add (configFile);
                } else {
                    err.log(Level.FINE, "Try delete the config File " + configFile);
                    configFile.delete ();
                    err.log(Level.FINE, "Control file " + configFile + " is deleted.");
                }
            } else {
                err.log(Level.FINE,
                        "Warning: Config File " + configFile + " doesn\'t exist!");
            }
        }
    }
    
    private boolean foundUpdateTracking (ModuleInfo moduleInfo) {
        File updateTracking = Utilities.locateUpdateTracking (moduleInfo);
        if (updateTracking != null && updateTracking.exists ()) {
            //err.log ("Find UPDATE_TRACKING: " + updateTracking + " found.");
            // check the write permission
            if (! Utilities.canWrite (updateTracking)) {
                err.log(Level.FINE,
                        "Cannot delete module " + moduleInfo.getCodeName() +
                        " because is forbidden to write in directory " +
                        updateTracking.getParentFile ().getParent ());
                return false;
            } else {
                return true;
            }
        } else {
            err.log(Level.FINE,
                    "Cannot delete module " + moduleInfo.getCodeName() +
                    " because no update_tracking file found.");
            return false;
        }
    }
            
    private void removeModuleFiles (ModuleInfo m, boolean markForDelete) throws IOException {
        err.log (Level.FINE, "Entry removing files of module " + m);
        File updateTracking;
        while ((updateTracking = Utilities.locateUpdateTracking (m)) != null && ! getStorageFilesForDelete ().contains (updateTracking)) {
            removeModuleFilesInCluster (m, updateTracking, markForDelete);
        }
        err.log (Level.FINE, "Exit removing files of module " + m);
    }
    
    private void removeModuleFilesInCluster (ModuleInfo moduleInfo, File updateTracking, boolean markForDelete) throws IOException {
        err.log(Level.FINE, "Read update_tracking " + updateTracking + " file.");
        Set<String> moduleFiles = readModuleFiles (getModuleConfiguration (updateTracking));
        String configFile = ModuleDeactivator.CONFIG + '/' + ModuleDeactivator.MODULES + '/' + moduleInfo.getCodeNameBase ().replace ('.', '-') + ".xml"; // NOI18N
        
        if (moduleFiles.contains (configFile)) {
            File file = InstalledFileLocator.getDefault ().locate (configFile, moduleInfo.getCodeNameBase (), false);
            if(file!=null && file.exists() && !getStorageFilesForDelete ().contains (file)) {
                err.log(Level.WARNING, "Config file " + configFile +
                        " must be already removed or marked for remove but still exist as file " + file +
                        " and not found in StorageFilesForDelete : " + getStorageFilesForDelete());
            }
        }
        
        for (String fileName : moduleFiles) {
            if (fileName.equals (configFile)) {
                continue;
            }
            Set <File> files = InstalledFileLocator.getDefault ().locateAll (fileName, moduleInfo.getCodeNameBase (), false);
            File file = null;
            if (files.size() > 0) {
                file = files.iterator().next();
                if (files.size() > 1) {
                    boolean found = false;
                    for (File f : files) {
                        if (f.getPath().startsWith(updateTracking.getParentFile().getParentFile().getPath())) {
                            file = f;
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        err.log(Level.WARNING,
                                "InstalledFileLocator doesn't choose the right file with file name " + fileName
                                + " for module " + moduleInfo.getCodeNameBase()
                                + " since a few files were returned : " + Arrays.toString(files.toArray()) +
                                ", and since none are in the same cluster as update tracking file " + updateTracking +
                                " will use " + file);
                    }
                }
            }

            if (file == null) {
                err.log (Level.WARNING, "InstalledFileLocator doesn't locate file " + fileName + " for module " + moduleInfo.getCodeNameBase ());
                continue;
            }
            if (file.equals (updateTracking)) {
                continue;
            }
            assert file.exists () : "File " + file + " exists.";
            if (file.exists ()) {
                if (markForDelete) {
                    err.log(Level.FINE, "File " + file + " is marked for delete.");
                    getStorageFilesForDelete ().add (file);
                } else {
                    try {
                        FileObject fo = FileUtil.toFileObject (file);
                        //assert fo != null || !file.exists() : file.getAbsolutePath();
                        if (fo != null) {
                            fo.lock().releaseLock();
                        }
                        File f = file;
                        while (f.delete()) {
                            f = f.getParentFile(); // remove empty dirs too
                        }
                    } catch (IOException ioe) {
                        assert false : "Waring: IOException " + ioe.getMessage () + " was caught. Propably file lock on the file.";
                        err.log(Level.FINE,
                                "Waring: IOException " + ioe.getMessage() +
                                " was caught. Propably file lock on the file.");
                        err.log(Level.FINE,
                                "Try call File.deleteOnExit() on " + file);
                        file.deleteOnExit ();
                    }
                    err.log(Level.FINE, "File " + file + " is deleted.");
                }
            }
        }
        
        FileObject trackingFo = FileUtil.toFileObject (updateTracking);
        FileLock lock = null;
        
        try {
            lock = (trackingFo != null) ? trackingFo.lock() : null;        
            if (markForDelete) {
                err.log(Level.FINE, "Tracking file " + updateTracking + " is marked for delete.");
                getStorageFilesForDelete ().add (updateTracking);
            } else {
                updateTracking.delete ();
                err.log(Level.FINE, "Tracking file " + updateTracking + " is deleted.");
            }
        } finally {
            if (lock != null) {
                lock.releaseLock();
            }
        }
        err.log(Level.FINE, "File " + updateTracking + " is deleted.");
    }
    
    private Node getModuleConfiguration (File moduleUpdateTracking) {
        Document document = null;
        InputStream is;
        try {
            is = new BufferedInputStream (new FileInputStream (moduleUpdateTracking));
            InputSource xmlInputSource = new InputSource (is);
            document = XMLUtil.parse (xmlInputSource, false, false, null, org.openide.xml.EntityCatalog.getDefault ());
            if (is != null) {
                is.close ();
            }
        } catch (SAXException saxe) {
            err.log(Level.WARNING, "SAXException when reading " + moduleUpdateTracking, saxe);
            //for issue #158186 investigation purpose need to add additional logging to see what is corrupted and how
            FileReader reader=null;
            try {
                reader=new FileReader(moduleUpdateTracking);
                char[] text=new char[1024];
                String fileContent="";
                while(reader.read(text)>0)
                {
                    fileContent+=String.copyValueOf(text);
                }
                err.log(Level.WARNING, "SAXException in file:\n------FILE START------\n " + fileContent+"\n------FILE END-----\n");
            }
            catch(Exception ex)
            {
                //don't need to fail in logging
            }
            finally
            {
                if(reader!=null)
                {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        //don't need any info from logging fail
                    }
                }
            }
            return null;
        } catch (IOException ioe) {
            err.log(Level.WARNING, "IOException when reading " + moduleUpdateTracking, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        return getModuleElement (document.getDocumentElement ());
    }
    
    private Node getModuleElement (Element element) {
        Node lastElement = null;
        assert ELEMENT_MODULE.equals (element.getTagName ()) : "The root element is: " + ELEMENT_MODULE + " but was: " + element.getTagName ();
        NodeList listModuleVersions = element.getElementsByTagName (ELEMENT_VERSION);
        for (int i = 0; i < listModuleVersions.getLength (); i++) {
            lastElement = getModuleLastVersion (listModuleVersions.item (i));
            if (lastElement != null) {
                break;
            }
        }
        return lastElement;
    }
    
    private Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }
    
    private Set<String> readModuleFiles (Node version) {
        if (version == null) {
            return Collections.emptySet();
        }
        NodeList fileNodes = version.getChildNodes ();
        if (fileNodes == null) {
            return Collections.emptySet();
        }
        Set<String> files = new HashSet<String> ();
        for (int i = 0; i < fileNodes.getLength (); i++) {
            if (fileNodes.item (i).hasAttributes ()) {
                NamedNodeMap map = fileNodes.item (i).getAttributes ();
                files.add (map.getNamedItem (ATTR_FILE_NAME).getNodeValue ());
                err.log(Level.FINE,
                        "Mark to delete: " +
                        map.getNamedItem(ATTR_FILE_NAME).getNodeValue());
            }
        }
        return files;
    }

    private void refreshModuleList () {
        // XXX: the modules list should be delete automatically when config/Modules/module.xml is removed
        FileObject modulesRoot = FileUtil.getConfigFile(ModuleDeactivator.MODULES); // NOI18N
        err.log (Level.FINE, "Call refresh on " + modulesRoot + " file object.");
        if (modulesRoot != null) {
            modulesRoot.refresh ();
        }
    }
    
    private Set<File> getStorageFilesForDelete () {
        if (storageFilesForDelete == null) {
            storageFilesForDelete = new HashSet<File> ();
        }
        return storageFilesForDelete;
    }
}
