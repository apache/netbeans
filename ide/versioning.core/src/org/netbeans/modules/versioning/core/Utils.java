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
package org.netbeans.modules.versioning.core;

import org.netbeans.modules.versioning.core.util.VCSSystemProvider;
import org.openide.util.RequestProcessor;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.awt.Actions;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.netbeans.api.fileinfo.NonRecursiveFolder;

import javax.swing.*;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSForbiddenFolderProvider;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.modules.Places;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Utilities for Versioning SPI classes. 
 * 
 * @author Maros Sandor
 */
public class Utils {
    
    /**
     * Request processor for long running tasks.
     */
    private static final RequestProcessor vcsBlockingRequestProcessor = new RequestProcessor("Versioning long tasks", 1, false, false);

    /**
     * Keeps the nb masterfilesystem
     */
    private static FileSystem filesystem;

    /**
     * Keeps excluded/unversioned folders
     */
    private static String [] unversionedFolders;

    /**
     * Keeps forbidden folders without metadata
     */
    private static final Set<String> forbiddenFolders;
    static {
        Set<String> files = new HashSet<String>();
        try {
            String forbidden = System.getProperty("versioning.forbiddenFolders", ""); //NOI18N
            files.addAll(Arrays.asList(forbidden.split("\\;"))); //NOI18N
            files.remove(""); //NOI18N
        } catch (Exception e) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, e.getMessage(), e);
        }
        forbiddenFolders = files;
    }

    /**
     * Request processor for parallel tasks.
     */
    private static final RequestProcessor vcsParallelRequestProcessor = new RequestProcessor("Versioning parallel tasks", 10, false, true);
    
    /**
     * Constructs a VCSContext out of a Lookup, basically taking all Nodes inside. 
     * Nodes are converted to Files based on their nature. 
     * For example Project Nodes are queried for their SourceRoots and those roots become the root files of this context.
     * 
     * @param lookup a lookup
     * @return VCSContext containing nodes from Lookup
     */ 
    public static VCSContext contextForLookup(Lookup lookup) {
        Lookup.Result<Node> result = lookup.lookup(new Lookup.Template<Node>(Node.class));
        Collection<? extends Node> nodes = result.allInstances();
        return VCSContext.forNodes(nodes.toArray(new Node[0]));
    }
        
    public static VCSContext contextForFileObjects(Set<? extends FileObject> files) {
        Set<VCSFileProxy> roots = new HashSet<VCSFileProxy>(files.size());
        if (files instanceof NonRecursiveFolder) {
            FileObject folder = ((NonRecursiveFolder) files).getFolder();
            VCSFileProxy file = createFlatFileProxy(folder);
            if(file != null) {
                roots.add(file);
            }
        } else {
            for (FileObject fo : files) {
                VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
                if (file != null) {
                    roots.add(file);
                }
            }
        }
        return SPIAccessor.IMPL.createContextForFiles(roots, files);
    }
    
    public static VCSFileProxy createFlatFileProxy(FileObject fo) {
        return APIAccessor.IMPL.createFlatFileProxy(fo);
    }
    
    /**
     * Tests for ancestor/child file relationsip.
     * 
     * @param ancestor supposed ancestor of the file
     * @param file a file
     * @return true if ancestor is an ancestor folder of file OR both parameters are equal, false otherwise
     */
    public static boolean isAncestorOrEqual(VCSFileProxy ancestor, VCSFileProxy file) {
        if (APIAccessor.IMPL.isFlat(ancestor)) {
            return ancestor.equals(file) || ancestor.equals(file.getParentFile()) && !file.isDirectory();
        }
        return isAncestorOrEqual(ancestor, ancestor.getPath(), file);
    }

    /**
     * Tests for ancestor/child file relationsip.
     * 
     * @param ancestorPath the supposed ancestors path 
     * @param file a file 
     * @return true if ancestor is an ancestor folder of file OR both parameters are equal, false otherwise
     */    
    public static boolean isAncestorOrEqual(String ancestorPath, VCSFileProxy file) {
        return isAncestorOrEqual(null, ancestorPath, file);
    }
    
    private static boolean isAncestorOrEqual(VCSFileProxy ancestor, String ancestorPath, VCSFileProxy file) {
        if(ancestorPath == null) {
            assert ancestor != null;
            ancestorPath = ancestor.getPath();
        }
        String filePath = file.getPath();
        if(Utilities.isWindows()) {
            if(filePath.indexOf("~") < 0 && ancestorPath.indexOf("~") < 0) {
                if(filePath.length() < ancestorPath.length()) {
                    return false;
                }
            }
        } else if (Utilities.isMac()) {
            // Mac is not case sensitive, cannot use the else statement
            if(filePath.length() < ancestorPath.length()) {
                return false;
            }
        } else {
            if(!filePath.startsWith(ancestorPath)) {
                return false;
            }
        }

        // get sure as it still could be something like:
        // ancestor: /home/dil
        // file:     /home/dil1/dil2
        for (; file != null; file = file.getParentFile()) {
            if(ancestor == null && APIAccessor.IMPL.isLocalFile(file)) {
                ancestor = APIAccessor.IMPL.createFileProxy(ancestorPath);
            }
            if(ancestor == null) {
                // XXX have to rely on path because of fileproxy being created from 
                // io.file even if it was originaly stored from a remote
                if (file.getPath().equals(ancestorPath)) return true; 
            } else {
                if (file.equals(ancestor)) return true; 
            }
        }
        return false;
    }

    /**
     * Creates a menu item from an action.
     * 
     * @param action an action
     * @return JMenuItem
     */
    public static JMenuItem toMenuItem(Action action) {
        JMenuItem item;
        if (action instanceof Presenter.Menu) {
            item = ((Presenter.Menu) action).getMenuPresenter();
        } else if (action instanceof Presenter.Popup) {
            item = ((Presenter.Popup) action).getPopupPresenter();
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, false);
        }
        return item;
    }

    /**
     * Creates a task that will run in the Versioning RequestProcessor (with has throughput of 1). The runnable may take long
     * to execute (connet through network, etc).
     * 
     * @param runnable Runnable to run
     * @return RequestProcessor.Task created task
     */
    public static RequestProcessor.Task createTask(Runnable runnable) {
        return vcsBlockingRequestProcessor.create(runnable);
    }

    public static void postParallel (Runnable runnable) {
        vcsParallelRequestProcessor.post(runnable);
    }
    
    public static JSeparator createJSeparator() {
        JMenu menu = new JMenu();
        menu.addSeparator();
        return (JSeparator)menu.getPopupMenu().getComponent(0);
    }

    /**
     * Calls {@link java.io.File#isFile()} and returns it's result.
     * But loggs a warning if {@link java.io.File#isFile()} blocks for a defined amount of time.
     * @param file file to test
     * @return result of {@link java.io.File#isFile()}
     */
    public static boolean isFile (VCSFileProxy file) {
        long startTime = System.currentTimeMillis();
        try {
            return file.isFile();
        } finally {
            logLasting(file, System.currentTimeMillis() - startTime,
                    "Utils.isFile: java.io.File.isFile takes too much time ({0} ms): {1}, stacktrace:"); //NOI18N
        }
    }

    /**
     * Calls {@link java.io.File#exists()} and returns it's result.
     * But loggs a warning if {@link java.io.File#exists()} blocks for a defined amount of time.
     * @param file file to test
     * @return result of {@link java.io.File#exists()}
     */
    public static boolean exists (VCSFileProxy file) {
        long startTime = System.currentTimeMillis();
        try {
            return file.exists();
        } finally {
            logLasting(file, System.currentTimeMillis() - startTime,
                    "Utils.exists: java.io.File.exists takes too much time ({0} ms): {1}, stacktrace:"); //NOI18N
        }
    }

    /**
     * Calls {@link java.io.File#canWrite()} and returns it's result.
     * But loggs a warning if {@link java.io.File#canWrite()} blocks for a defined amount of time.
     * @param file file to test
     * @return result of {@link java.io.File#canWrite()}
     */
    public static boolean canWrite (VCSFileProxy file) {
        long startTime = System.currentTimeMillis();
        try {
            return file.canWrite();
        } finally {
            logLasting(file, System.currentTimeMillis() - startTime,
                    "Utils.canWrite: java.io.File.canWrite takes too much time ({0} ms): {1}, stacktrace:"); //NOI18N
        }
    }

    public static String[] getUnversionedFolders () {
        if (unversionedFolders == null) {
            String[] files;
            try {
                String uf = VersioningSupport.getPreferences().get("unversionedFolders", ""); //NOI18N
                String ufProp = System.getProperty("versioning.unversionedFolders", ""); //NOI18N
                StringBuilder sb = new StringBuilder(uf);
                File nbUserdir = Places.getUserDirectory();
                if (nbUserdir != null && !isVersionUserdir()) { 
                    if (sb.length() > 0) {
                        sb.append(';');
                    }
                    sb.append(nbUserdir.getAbsolutePath());
                }
                if (!ufProp.isEmpty()) {
                    if (sb.length() > 0) {
                        sb.append(';');
                    }
                    sb.append(ufProp);
                }
                if (sb.length() == 0) {
                    files = new String[0];
                } else {
                    String [] paths = sb.toString().split("\\;"); //NOI18N
                    files = new String[paths.length];
                    int idx = 0;
                    for (String path : paths) {
                        files[idx++] = path;
                    }
                }
            } catch (Exception e) {
                files = new String[0];
                Logger.getLogger(Utils.class.getName()).log(Level.INFO, e.getMessage(), e);
            }
            unversionedFolders = files;
        }
        return unversionedFolders;
    }

    public static boolean isForbiddenFolder (VCSFileProxy folder) {
        Collection<? extends VCSForbiddenFolderProvider> forbiddenFolderProviders = Lookup.getDefault().lookupAll(VCSForbiddenFolderProvider.class);
        for (VCSForbiddenFolderProvider provider : forbiddenFolderProviders) {
            if (provider.isForbiddenFolder(folder)) {
                return true;
            }
        }
        return forbiddenFolders.contains(folder.getPath());
    }

    static boolean isVersionUserdir() {
        return "true".equals(System.getProperty("versioning.netbeans.user.versioned", "false")); // NOI18N
    }

    /**
     * Helper method to get an array of Strings from preferences.
     *
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList (Preferences prefs, String key) {
        List<String> retval = new ArrayList<String>();
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key)) {
                    int idx = Integer.parseInt(k.substring(k.lastIndexOf('.') + 1));
                    retval.add(idx + "." + prefs.get(k, null));
                }
            }
            List<String> rv = new ArrayList<String>(retval.size());
            rv.addAll(retval);
            for (String s : retval) {
                int pos = s.indexOf('.');
                int index = Integer.parseInt(s.substring(0, pos));
                rv.set(index, s.substring(pos + 1));
            }
            return rv;
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
            return new ArrayList<String>(0);
        }
    }

    /**
     * Stores a List of Strings into Preferences node under the given key.
     *
     * @param prefs storage
     * @param key key of the String array
     * @param value List of Strings to write (order will be preserved)
     */
    public static void put (Preferences prefs, String key, List<String> value) {
        try {
            String[] keys = prefs.keys();
            for (int i = 0; i < keys.length; i++) {
                String k = keys[i];
                if (k != null && k.startsWith(key + ".")) {
                    prefs.remove(k);
                }
            }
            int idx = 0;
            for (String s : value) {
                prefs.put(key + "." + idx++, s);
            }
        } catch (BackingStoreException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, null, ex);
        }
    }

    private static void logLasting (VCSFileProxy file, long last, String message) {
        boolean allowed = false;
        assert allowed = true;
        if (allowed && last > 1500) {
            StackTraceElement[] stElements = Thread.currentThread().getStackTrace();
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (StackTraceElement e : stElements) {
                if (i++ > 1) {
                    if (i == 8) {
                        sb.append("...\n");                         // NOI18N
                        break;
                    } else {
                        sb.append(e.toString()).append("\n");       // NOI18N
                    }
                }
            }
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, message, new String[]{Long.toString(last), file.getPath()});
            Logger.getLogger(Utils.class.getName()).log(Level.INFO, sb.toString());
        }
    }

    static String getSystemMenuName(VCSSystemProvider.VersioningSystem system) {
        if(VersioningManager.getInstance().isLocalHistory(system)) {
            return NbBundle.getMessage(Utils.class, "CTL_LocalHistoryMenuName"); // NOI18N
        } else {
            return system.getDisplayName();
        }    
    }
    
    public static VCSFileProxy toFileProxy(URI uri) {
        FileObject fo = toFileObject(uri);
        return fo != null ? VCSFileProxy.createFileProxy(fo) : null;
    }
    
    public static FileObject toFileObject(URI uri) {
        FileObject fo = null;
        try {
            fo = URLMapper.findFileObject(uri.toURL());
        } catch (MalformedURLException ex) {
            VersioningManager.LOG.log(Level.WARNING, uri != null ? uri.toString() : null, ex);
        }
        if(fo == null) {
            // file doesn't exists? use parent for the query then.
            // By the means of VCS it has to be collocated in the same way.
            String path = uri.getPath();
            URI parent;
            try {
                parent = path.endsWith("/") ? uri.resolve("..") : new URI(uri + "/").resolve(".."); // NOI18N
                path = parent.getPath();
                uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
            } catch (URISyntaxException ex) {
                VersioningManager.LOG.log(Level.WARNING, path, ex);
                return null;
            }
            fo = toFileObject(uri);
        }
        return fo;
    }    
}
