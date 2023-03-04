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

package org.netbeans.modules.autoupdate.pluginimporter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.autoupdate.InstallSupport;
import org.netbeans.api.autoupdate.OperationContainer;
import org.netbeans.api.autoupdate.UpdateElement;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jiri Rechtacek
 */
public class PluginImporter {

    private final Collection<UpdateUnit> plugins;
    private boolean inspected = false;
    private Collection<UpdateElement> installed = null;
    private Collection<UpdateElement> toInstall = null;
    private Collection<UpdateElement> toImport = null;
    private Collection<UpdateElement> broken = null;

    private static final String TRACKING_FILE_NAME = "update_tracking"; // NOI18N
    private static final String ELEMENT_MODULE = "module"; // NOI18N
    private static final String ELEMENT_VERSION = "module_version"; // NOI18N
    private static final String ATTR_LAST = "last"; // NOI18N
    private static final String ATTR_FILE_NAME = "name"; // NOI18N
    private static final String MODULES = "Modules"; // NOI18N
    private static final String LAST_MODIFIED = ".lastModified"; // NOI18N

    private static final Logger LOG = Logger.getLogger (PluginImporter.class.getName ());

    public PluginImporter (Collection<UpdateUnit> foundPlugins) {
        plugins = foundPlugins;
    }

    public void reinspect () {
        inspected = false;
        inspect();
    }
    private void inspect () {
        if (inspected) {
            return ;
        }
        long start = System.currentTimeMillis();
        installed = new HashSet<UpdateElement> ();
        toImport = new HashSet<UpdateElement> ();
        toInstall = new HashSet<UpdateElement> ();
        broken = new HashSet<UpdateElement> ();

        Collection<UpdateElement> candidate2import = new HashSet<UpdateElement> ();
        List<UpdateUnit> updateUnits = UpdateManager.getDefault ().getUpdateUnits (UpdateManager.TYPE.MODULE);
        Map<String, UpdateUnit> cnb2uu = new HashMap<String, UpdateUnit> (updateUnits.size ());
        for (UpdateUnit u : updateUnits) {
            cnb2uu.put (u.getCodeName (), u);
        }

        for (UpdateUnit unit : plugins) {

            // save information about plugins on common Update Center
            UpdateUnit remoteUnit = cnb2uu.get (unit.getCodeName ());
            UpdateElement remoteElement = null;
            SpecificationVersion remoteSpec = null;
            if (remoteUnit != null && ! remoteUnit.getAvailableUpdates ().isEmpty ()) {
                remoteElement = remoteUnit.getAvailableUpdates ().get (0);
                remoteSpec = remoteElement.getSpecificationVersion() == null ? null : new SpecificationVersion(remoteElement.getSpecificationVersion());
            }

            if (unit.getInstalled () != null) {
                if (! unit.getAvailableUpdates ().isEmpty ()) {
                    UpdateElement el = unit.getAvailableUpdates ().get (0);
                    if (remoteElement != null) {
                        SpecificationVersion spec = el.getSpecificationVersion () == null ? null : new SpecificationVersion (el.getSpecificationVersion ());
                        if (spec != null && spec.compareTo (remoteSpec) > 0) {
                            candidate2import.add (el);
                        }
                    } else {
                        candidate2import.add (el);
                    }
                }
                installed.add (unit.getInstalled ());
            } else if (unit.isPending()) {
                LOG.log(Level.INFO, "Plugin " + unit.getCodeName() + " is not installed but is in pending state - i.e. will be installed upon restart, skipping");
            } else {
                assert ! unit.getAvailableUpdates ().isEmpty () : "If " + unit + " isn't installed thus has available updates.";
                UpdateElement el = unit.getAvailableUpdates ().get (0);
                if (remoteElement != null) {
                    SpecificationVersion spec = el.getSpecificationVersion () == null ? null : new SpecificationVersion (el.getSpecificationVersion ());
                    if (spec != null && spec.compareTo (remoteSpec) > 0) {
                        candidate2import.add (el);
                    } else {
                        toInstall.add (remoteElement);
                    }
                } else {
                    candidate2import.add (el);
                }
            }
        }
        for (UpdateElement el : candidate2import) {
            OperationContainer<InstallSupport> oc = el.getUpdateUnit ().getInstalled () == null ?
                OperationContainer.createForInstall () :
                OperationContainer.createForUpdate ();
            try {
                OperationContainer.OperationInfo info = oc.add (el);
                oc.add (candidate2import);
                if (isBlacklisted(el)) {
                    LOG.info("Plugin " + el + " is on blacklist thus will not be imported.");
                } else if (info.getBrokenDependencies ().isEmpty ()) {
                    toImport.add (el);
                } else {
                    LOG.log (Level.INFO, "Plugin " + el + // NOI18N
                            " cannot be install because not all dependencies can be match: " + info.getBrokenDependencies ()); // NOI18N
                    broken.add (el);
                }
            } catch (IllegalArgumentException iae) {
                LOG.log (Level.INFO, iae.getLocalizedMessage (), iae);
                broken.add (el);
            }
        }
        long end = System.currentTimeMillis();
        LOG.log (Level.INFO, "Inspecting plugins took " + (end - start) + " ms"); // NOI18N

        inspected = true;
    }

    public Collection<UpdateElement> getPluginsToImport () {
        inspect ();
        return toImport;
    }

    public Collection<UpdateElement> getInstalledPlugins () {
        inspect ();
        return installed;
    }

    public Collection<UpdateElement> getPluginsAvailableToInstall () {
        inspect ();
        return toInstall;
    }

    public Collection<UpdateElement> getBrokenPlugins () {
        inspect ();
        return broken;
    }

    public void importPlugins (Collection<UpdateElement> plugins, File src, File dest, ProgressHandle handle) throws IOException {
        if(handle!=null) {
            handle.setInitialDelay(0);
            handle.start(plugins.size());
        }
        List<String> configs = new ArrayList<String> (plugins.size ());
        int completed = 0;
        for (UpdateElement el : plugins) {
            if(handle != null) {
                String name = el.getDisplayName();
                if(name==null) {
                    name = el.getCodeName();
                }
                String detail = NbBundle.getMessage(PluginImporter.class, "PluginImporter.Importing.Plugin", name);//NOI18N
                handle.progress(detail, completed ++);
            }
            String cnb = el.getCodeName ();

            // 1. find all plugin's resources
            Collection<String> toCopy = getPluginFiles (src, cnb, locateUpdateTracking (cnb, src));
            if (toCopy.isEmpty()) {
                continue;
            }

            // 2. copy them
            for (String path : toCopy) {
                copy (path, src, dest);
            }

            // 3. find config file
            String path = "config/Modules/" + cnb.replace ('.', '-') + ".xml"; // NOI18N
            configs.add (path);
        }

        // 4. find and copy config files in the end
        for (String path : configs) {
            copy (path, src, dest);
        }

        // #252928 (fragment modules)
        if (getPluginsToImport().isEmpty()) {
            refreshModuleList();
        } else {
            String restartMsg = NbBundle.getMessage(PluginImporter.class, "PluginImporter.Importing.RestartNeeded");//NOI18N
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(restartMsg, NotifyDescriptor.YES_NO_OPTION);
            Object result = DialogDisplayer.getDefault().notify(nd);
            if (result.equals(NotifyDescriptor.OK_OPTION)) {
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            }
        }

        if(handle!=null) {
            handle.finish();
        }
    }

    private static void copy (String path, File sourceFolder, File destFolder) throws IOException {
        LOG.finest ("Copy " + path + " from " + sourceFolder + " to " + destFolder);
        File src = new File (sourceFolder, path);
        assert src.exists () : src + " exists.";
        src = FileUtil.normalizeFile (src);
        FileObject srcFO = FileUtil.toFileObject (src);

        File destFO = new File (destFolder, path);
        destFO.getParentFile ().mkdirs ();
        File dest = destFO.getParentFile ();
        dest = FileUtil.normalizeFile (dest);
        FileObject destFolderFO = FileUtil.toFileObject (dest);

        File destFile;
        if ((destFile = new File (dest, srcFO.getNameExt ())).exists ()) {
            if (! destFile.delete ()) {
                // if failed delete of the destFile => don't copy, otherwise will cause #159188
                return ;
            }
        }
        FileObject res = FileUtil.copyFile (srcFO, destFolderFO, srcFO.getName ());
        LOG.finest (srcFO + " was copied to " + destFolderFO + ". Result is: " + res);
    }

    private static Collection<String> getPluginFiles (File cluster, String cnb, File updateTracking) {
        Collection<String> res = new HashSet<String> ();
        LOG.log(Level.FINE, "Read update_tracking " + updateTracking + " file.");
        Node updateTrackingConf = getUpdateTrackingConf(updateTracking);
        if (updateTrackingConf == null) {
            return Collections.emptySet();
        }
        Set<String> moduleFiles = readModuleFiles(updateTrackingConf);
        String configFile = "config/Modules/" + cnb.replace ('.', '-') + ".xml"; // NOI18N

        moduleFiles.remove (configFile);

        for (String fileName : moduleFiles) {
            File file = new File (cluster, fileName);
            if (! file.exists ()) {
                LOG.log (Level.WARNING, "File " + file + " doesn't exist for module " + cnb);
                continue;
            }
            if (file.equals (updateTracking)) {
                continue;
            }
            res.add (fileName);
        }

        res.add (TRACKING_FILE_NAME + '/' + cnb.replace ('.', '-') + ".xml"); // NOI18N);

        LOG.log(Level.FINEST, cnb + " has files: " + res);
        return res;
    }

    private static File locateUpdateTracking (String cnb, File cluster) {
        String fileNameToFind = TRACKING_FILE_NAME + '/' + cnb.replace ('.', '-') + ".xml"; // NOI18N
        File ut = new File (cluster, fileNameToFind);
        if (ut.exists ()) {
            return ut;
        }
        throw new IllegalArgumentException (ut + " doesn't exist."); // NOI18N
    }

    private static Node getUpdateTrackingConf (File moduleUpdateTracking) {
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
            LOG.log(Level.WARNING, "SAXException when reading " + moduleUpdateTracking + ", cause: " + saxe);
            //for issue #217118 investigation what is corrupted and how
            FileReader reader = null;
            try {
                reader = new FileReader(moduleUpdateTracking);
                char[] text = new char[1024];
                String fileContent = "";
                while (reader.read(text) > 0) {
                    fileContent += String.copyValueOf(text);
                }
                LOG.log(Level.WARNING, "SAXException in file:\n------FILE START------\n " + fileContent + "\n------FILE END-----\n");
            } catch (Exception ex) {
                //don't need to fail in logging
            } finally {
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException ex) {
                        //don't need any info from logging fail
                    }
                }
            }
            return null;
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }

        assert document.getDocumentElement () != null : "File " + moduleUpdateTracking + " must contain <module> element.";
        return getModuleElement (document.getDocumentElement ());
    }

    private static Node getModuleElement (Element element) {
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

    private static Node getModuleLastVersion (Node version) {
        Node attrLast = version.getAttributes ().getNamedItem (ATTR_LAST);
        assert attrLast != null : "ELEMENT_VERSION must contain ATTR_LAST attribute.";
        if (Boolean.valueOf (attrLast.getNodeValue ()).booleanValue ()) {
            return version;
        } else {
            return null;
        }
    }

    private static Set<String> readModuleFiles (Node version) {
        Set<String> files = new HashSet<String> ();
        NodeList fileNodes = version.getChildNodes ();
        for (int i = 0; i < fileNodes.getLength (); i++) {
            if (fileNodes.item (i).hasAttributes ()) {
                NamedNodeMap map = fileNodes.item (i).getAttributes ();
                files.add (map.getNamedItem (ATTR_FILE_NAME).getNodeValue ());
                LOG.log(Level.FINE,
                        "File for import: " +
                        map.getNamedItem(ATTR_FILE_NAME).getNodeValue());
            }
        }
        return files;
    }

    private static void refreshModuleList () {
        // XXX: the modules list should be refresh automatically when config/Modules/ changes
        final FileObject modulesRoot = FileUtil.getConfigFile(MODULES);
        LOG.log (Level.FINE,
                "It\'s a hack: Call refresh on " + modulesRoot +
                " file object.");
        if (modulesRoot != null) {
            try {
                FileUtil.runAtomicAction (new FileSystem.AtomicAction () {

                    @Override
                    public void run () throws IOException {
                        modulesRoot.getParent ().refresh ();
                        modulesRoot.refresh ();
                    }
                });
            } catch (IOException ex) {
                Exceptions.printStackTrace (ex);
            }
        }
    }

    public static void touchLastModified (File cluster) {
        try {
            File stamp = new File (cluster, LAST_MODIFIED);
            if (! stamp.createNewFile ()) {
                stamp.setLastModified (System.currentTimeMillis ());
                if (! stamp.setLastModified (System.currentTimeMillis ())) {
                    stamp.delete ();
                    stamp = new File (cluster, LAST_MODIFIED);
                    stamp.setLastModified (System.currentTimeMillis ());
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.INFO, ex.getMessage(), ex);
        }
    }
    
    private static boolean isBlacklisted(UpdateElement el) {
        String blacklist = System.getProperty ("plugin.import.blacklist", ""); // NOI18N
        if (! blacklist.isEmpty()) {
            blacklist = blacklist + ','; // NOI18N
        }
        blacklist = blacklist + NbBundle.getMessage(PluginImporter.class, "plugin.import.blacklist"); // NOI18N
        LOG.fine("Blacklist: " + blacklist);
        StringTokenizer tokens = new StringTokenizer(blacklist, ",");
        while(tokens.hasMoreTokens()) {
            if (el.getCodeName().equals(tokens.nextToken())) {
                return true;
            }
        }
        return false;
    }
}
