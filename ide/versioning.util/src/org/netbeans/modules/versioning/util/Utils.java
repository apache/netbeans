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
package org.netbeans.modules.versioning.util;

import java.awt.Cursor;
import java.awt.EventQueue;
import javax.swing.tree.TreePath;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.awt.Actions;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.CloneableOpenSupport;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.versioning.spi.VCSContext;
import org.netbeans.modules.versioning.spi.VersioningSupport;

import javax.swing.text.Document;
import javax.swing.text.StyledDocument;
import javax.swing.text.EditorKit;
import javax.swing.text.BadLocationException;
import javax.swing.*;
import java.io.*;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.awt.Rectangle;
import java.awt.Point;
import java.text.MessageFormat;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.logging.LogRecord;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.spi.VersioningSystem;
import org.openide.ErrorManager;
import org.openide.awt.AcceleratorBinding;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileLock;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Utilities class.
 *
 * @author Maros Sandor
 */
public final class Utils {

    private static final Logger LOG = Logger.getLogger(Utils.class.getName());
    
    /**
     * Request processor for quick tasks.
     */
    private static final RequestProcessor vcsRequestProcessor = new RequestProcessor("Versioning", 1);

    /**
     * Request processor for long running tasks.
     */
    private static final RequestProcessor vcsBlockingRequestProcessor = new RequestProcessor("Versioning long tasks", 1);

    /**
     * Request processor for parallel tasks.
     */
    private static final RequestProcessor vcsParallelRequestProcessor = new RequestProcessor("Versioning parallel tasks", 5, true);

    /**
     * Metrics logger
     */
    private static final Logger METRICS_LOG = Logger.getLogger("org.netbeans.ui.metrics.vcs");

    /**
     * Metrics logger
     */
    private static final Logger UIGESTURES_LOG = Logger.getLogger("org.netbeans.ui.vcs"); //NOI18N

    /**
     * Keeps track about already logged metrics events
     */
    private static final Set<String> metrics = new HashSet<String>(3);

    private static File tempDir;
    
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

    private Utils() {
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

    /**
     * Runs the runnable in the Versioning RequestProcessor (with has throughput of 1). The runnable must not take long
     * to execute (connet through network, etc).
     *
     * @param runnable Runnable to run
     */
    public static void post(Runnable runnable) {
        post(runnable, 0);
    }

    /**
     * Runs the runnable in the Versioning RequestProcessor (which has throughput of 1). The runnable must not take long
     * to execute (connect through network, etc).
     *
     * @param runnable Runnable to run
     * @param timeToWait delay before starting the task
     */
    public static void post (Runnable runnable, int timeToWait) {
        vcsRequestProcessor.post(runnable, timeToWait);
    }

    /**
     * Runs the runnable in the Versioning RequestProcessor (which has throughput of 5).
     *
     * @param runnable Runnable to run
     * @param timeToWait delay before starting the task
     */
    public static void postParallel (Runnable runnable, int timeToWait) {
        vcsParallelRequestProcessor.post(runnable, timeToWait);
    }

    /**
     * Tests for ancestor/child file relationsip.
     *
     * @param ancestor supposed ancestor of the file
     * @param file a file
     * @return true if ancestor is an ancestor folder of file OR both parameters are equal, false otherwise
     */
    public static boolean isAncestorOrEqual(File ancestor, File file) {
        if (VersioningSupport.isFlat(ancestor)) {
            return ancestor.equals(file) || ancestor.equals(file.getParentFile()) && !file.isDirectory();
        }
        
        String filePath = file.getAbsolutePath();
        String ancestorPath = ancestor.getAbsolutePath();
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
            if (file.equals(ancestor)) return true;
        }
        return false;
    }

    /**
     * Tests whether all files belong to the same data object.
     *
     * @param files array of Files
     * @return true if all files share common DataObject (even null), false otherwise
     */
    public static boolean shareCommonDataObject(File[] files) {
        if (files == null || files.length < 2) return true;
        DataObject common = findDataObject(files[0]);
        for (int i = 1; i < files.length; i++) {
            DataObject dao = findDataObject(files[i]);
            if (dao != common && (dao == null || !dao.equals(common))) return false;
        }
        return true;
    }

    /**
     * @param file
     * @return Set<File> all files that belong to the same DataObject as the argument
     */
    public static Set<File> getAllDataObjectFiles(File file) {
        Set<File> filesToCheckout = new HashSet<File>(2);
        filesToCheckout.add(file);
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                Set<FileObject> fileObjects = dao.files();
                for (FileObject fileObject : fileObjects) {
                    filesToCheckout.add(FileUtil.toFile(fileObject));
                }
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return filesToCheckout;
    }

    /**
     * Some folders are special and versioning should not look for metadata in
     * them. Folders like /net with automount enabled may take a long time to
     * answer I/O on their children, so
     * <code>VCSFileProxy.exists("/net/.git")</code> will freeze until it
     * timeouts. You should call this method before asking any I/O on children
     * of this folder you are unsure to actually exist. This does not mean
     * however that whole subtree should be excluded from version control, only
     * that you should not look for the metadata directly in this folder.
     * Returns <code>true</code> if the given folder is among such folders.
     *
     * @param folderPath path to a folder to query
     * @return <code>true</code> if the folder identified by the given path
     * should be skipped when searching for metadata.
     * @since 1.71.0
     */
    public static boolean isForbiddenFolder (File folder) {
        return org.netbeans.modules.versioning.core.util.Utils.isForbiddenFolder(VCSFileProxy.createFileProxy(folder));
    }

    /**
     * Some folders are special and versioning should not look for metadata in
     * them. Folders like /net with automount enabled may take a long time to
     * answer I/O on their children, so
     * <code>VCSFileProxy.exists("/net/.git")</code> will freeze until it
     * timeouts. You should call this method before asking any I/O on children
     * of this folder you are unsure to actually exist. This does not mean
     * however that whole subtree should be excluded from version control, only
     * that you should not look for the metadata directly in this folder.
     * Returns <code>true</code> if the given folder is among such folders.
     *
     * @param folderPath path to a folder to query
     * @return <code>true</code> if the folder identified by the given path
     * should be skipped when searching for metadata.
     * @since 1.71.0
     */
    public static boolean isForbiddenFolder (VCSFileProxy folder) {
        return org.netbeans.modules.versioning.core.util.Utils.isForbiddenFolder(folder);
    }

    private static DataObject findDataObject(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                return DataObject.find(fo);
            } catch (DataObjectNotFoundException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Checks if the file is to be considered as textuall.
     *
     * @param file file to check
     * @return true if the file can be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentText(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) return false;
        if (fo.getMIMEType().startsWith("text")) { // NOI18N
            return true;
        }
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getLookup().lookupItem(new Lookup.Template<EditorCookie>(EditorCookie.class)) != null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }

    /**
     * Copies all content from the supplied reader to the supplies writer and closes both streams when finished.
     *
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails
     */
    public static void copyStreamsCloseAll(OutputStream writer, InputStream reader) throws IOException {
        byte [] buffer = new byte[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    /**
     * Copies all content from the supplied reader to the supplies writer and closes both streams when finished.
     *
     * @param writer where to write
     * @param reader what to read
     * @throws IOException if any I/O operation fails
     */
    public static void copyStreamsCloseAll(Writer writer, Reader reader) throws IOException {
        char [] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, n);
        }
        writer.close();
        reader.close();
    }

    /**
     * Helper method to get an array of Strings from preferences.
     *
     * @param prefs storage
     * @param key key of the String array
     * @return List<String> stored List of String or an empty List if the key was not found (order is preserved)
     */
    public static List<String> getStringList(Preferences prefs, String key) {
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
    public static void put(Preferences prefs, String key, List<String> value) {
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

    /**
     * Convenience method for storing array of Strings with a maximum length with LRU policy. Supplied value is
     * stored at index 0 and all items beyond (maxLength - 1) index are discarded. <br>
     * If the value is already stored then it will be first removed from its old position.
     *
     * @param prefs storage
     * @param key key for the array
     * @param value String to store
     * @param maxLength maximum length of the stored array. won't be considered if &lt; 0
     */
    public static void insert(Preferences prefs, String key, String value, int maxLength) {
        List<String> newValues = getStringList(prefs, key);
        newValues.removeAll(Collections.<String>singleton(value));
        newValues.add(0, value);
        if (maxLength > -1 && newValues.size() > maxLength) {
            newValues.subList(maxLength, newValues.size()).clear();
        }
        put(prefs, key, newValues);
    }

    /**
     * Convenience method to remove a array of values from a in preferences stored array of Strings
     *
     * @param prefs storage
     * @param key key for the array
     * @param values Strings to remove
     */
    public static void removeFromArray(Preferences prefs, String key, List<String> values) {
        List<String> newValues = getStringList(prefs, key);
        newValues.removeAll(values);
        put(prefs, key, newValues);
    }

    /**
     * Convenience method to remove a value from a in preferences stored array of Strings
     *
     * @param prefs storage
     * @param key key for the array
     * @param value String to remove
     */
    public static void removeFromArray(Preferences prefs, String key, String value) {
        List<String> newValues = getStringList(prefs, key);
        newValues.removeAll(Collections.<String>singleton(value));
        put(prefs, key, newValues);
    }

    /**
     * Splits files/folders into 2 groups: flat folders and other files
     *
     * @param files array of files to split
     * @return File[][] the first array File[0] contains flat folders (@see #flatten for their direct descendants),
     * File[1] contains all other files
     */
    public static File[][] splitFlatOthers(File [] files) {
        Set<File> flat = new HashSet<File>(1);
        for (int i = 0; i < files.length; i++) {
            if (VersioningSupport.isFlat(files[i])) {
                flat.add(files[i]);
            }
        }
        if (flat.isEmpty()) {
            return new File[][] { new File[0], files };
        } else {
            Set<File> allFiles = new HashSet<File>(Arrays.asList(files));
            allFiles.removeAll(flat);
            return new File[][] {
                flat.toArray(new File[0]),
                allFiles.toArray(new File[0])
            };
        }
    }

    /**
     * Flattens the given collection of files and removes those that do not respect the flat folder logic,
     * i.e. those that lie deeper under a flat folder.
     * @param roots selected files with flat folders
     */
    public static Set<File> flattenFiles (File[] roots, Collection<File> files) {
        File[][] split = Utils.splitFlatOthers(roots);
        Set<File> filteredFiles = new HashSet<File>(files);
        if (split[0].length > 0) {
            outer:
            for (Iterator<File> it = filteredFiles.iterator(); it.hasNext(); ) {
                File f = it.next();
                // file is directly under a flat folder
                for (File flat : split[0]) {
                    if (f.getParentFile().equals(flat)) {
                        continue outer;
                    }
                }
                // file lies under a recursive folder
                for (File folder : split[1]) {
                    if (Utils.isAncestorOrEqual(folder, f)) {
                        continue outer;
                    }
                }
                it.remove();
            }
        }
        return filteredFiles;
    }

    /**
     * Recursively deletes the file or directory.
     *
     * @param file file/directory to delete
     */
    public static void deleteRecursively(File file) {
        File [] files = file.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                deleteRecursively(files[i]);
            }
        }
        file.delete();
    }

    /**
     * Searches for common filesystem parent folder for given files.
     *
     * @param a first file
     * @param b second file
     * @return File common parent for both input files with the longest filesystem path or null of these files
     * have not a common parent
     */
    public static File getCommonParent(File a, File b) {
        for (;;) {
            if (a.equals(b)) {
                return a;
            } else if (a.getAbsolutePath().length() > b.getAbsolutePath().length()) {
                a = a.getParentFile();
                if (a == null) return null;
            } else {
                b = b.getParentFile();
                if (b == null) return null;
            }
        }
    }

    public static String getStackTrace() {
        Exception e = new Exception();
        e.fillInStackTrace();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }

    /**
     * Copied from org.netbeans.api.xml.parsers.DocumentInputSource to save whole module dependency.
     *
     * @param doc a Document to read
     * @return Reader a reader that reads document's text
     */
    public static Reader getDocumentReader(final Document doc) {
        final String[] str = new String[1];
        Runnable run = new Runnable() {
            @Override
            public void run () {
                try {
                    str[0] = doc.getText(0, doc.getLength());
                } catch (javax.swing.text.BadLocationException e) {
                    // impossible
                    LOG.log(Level.INFO, null, e);
                }
            }
        };
        doc.render(run);
        return new StringReader(str[0]);
    }

    /**
     * For popups invoked by keyboard determines best location for it.
     *
     * @param table source of popup event
     * @return Point best location for menu popup
     */
    public static Point getPositionForPopup(JTable table) {
        int idx = table.getSelectedRow();
        if (idx == -1) idx = 0;
        Rectangle rect = table.getCellRect(idx, 1, true);
        return rect.getLocation();
    }

    /**
     * For popups invoked by keyboard determines best location for it.
     *
     * @param list source of popup event
     * @return Point best location for menu popup
     */
    public static Point getPositionForPopup(JList list) {
        int idx = list.getSelectedIndex();
        if (idx == -1) idx = 0;
        Rectangle rect = list.getCellBounds(idx, idx);
        rect.x += 10; rect.y += rect.height;
        return rect.getLocation();
    }

    /**
     * For popups invoked by keyboard determines best location for it.
     *
     * @param tree source of popup event
     * @return Point best location for menu popup
     */
    public static Point getPositionForPopup(JTree tree) {
        TreePath path = tree.getSelectionPath();
        if (path == null) path = tree.getPathForRow(0);
        Rectangle rect = tree.getPathBounds(path);
        rect.x += 10; rect.y += rect.height;
        return rect.getLocation();
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
        } else {
            item = new JMenuItem();
            Actions.connect(item, action, false);
        }
        return item;
    }

    /**
     * Creates a temporary folder. The folder has deleteOnExit flag set.
     * @return
     */
    public static File getTempFolder() {
        return getTempFolder(true);
    }

    /**
     * Creates a temporary folder. The folder will have deleteOnExit flag set to <code>deleteOnExit</code>.
     * @return
     */
    public static File getTempFolder(boolean deleteOnExit) {
        File tmpDir = getTempDir(deleteOnExit);
        for (;;) {
            File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
            if (!dir.exists() && dir.mkdirs()) {
                if (deleteOnExit) {
                    dir.deleteOnExit();
                }
                return FileUtil.normalizeFile(dir);
            }
        }
    }

    /**
     * Utility method to word-wrap a String.
     *
     * @param s String to wrap
     * @param maxLineLength maximum length of one line. If less than 1 no wrapping will occurr
     * @return String wrapped string
     */
    public static String wordWrap(String s, int maxLineLength) {
        int n = s.length() - 1;
        if (maxLineLength < 1 || n < maxLineLength) return s;
        StringBuilder sb = new StringBuilder();

        int currentWrap = 0;
        for (;;) {
            int nextWrap = currentWrap + maxLineLength - 1;
            if (nextWrap >= n) {
                sb.append(s.substring(currentWrap));
                break;
            }
            int idx = s.lastIndexOf(' ', nextWrap + 1);
            if (idx > currentWrap) {
                sb.append(s.substring(currentWrap, idx).trim());
                currentWrap = idx + 1;
            } else {
                sb.append(s.substring(currentWrap, nextWrap + 1));
                currentWrap = nextWrap + 1;
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    /**
     * Computes display name of an action based on its context.
     *
     * @param clazz caller class for bundle location
     * @param baseName base bundle name
     * @param ctx action's context
     * @return String full name of the action, eg. Show "File.java" Annotations
     */
    public static String getActionName(Class clazz, String baseName, VCSContext ctx) {
        Set<File> nodes = ctx.getRootFiles();
        int objectCount = nodes.size();
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        Node[] activatedNodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length;

        if (objectCount == 0) {
            return NbBundle.getBundle(clazz).getString(baseName);
        } else if (objectCount == 1) {
            if (projectsOnly) {
                String dispName = ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
                return NbBundle.getMessage(clazz, baseName + "_Context",  // NOI18N
                                                dispName);
            }
            String name;
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                name = fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    name = dao.getPrimaryFile().getNameExt();
                } else {
                    name = activatedNodes[0].getDisplayName();
                }
            }
            return MessageFormat.format(NbBundle.getBundle(clazz).getString(baseName + "_Context"), name); // NOI18N
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(clazz).getString(baseName + "_Projects"), objectCount); // NOI18N
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(clazz).getString(baseName + "_Context_Multiple"), objectCount); // NOI18N
        }
    }

    /**
     * Computes display name of a context.
     *
     * @param ctx a context
     * @return String short display name of the context, eg. File.java, 3 Files, 2 Projects, etc.
     */
    public static String getContextDisplayName(VCSContext ctx) {
        // TODO: reuse this code in getActionName()
        Set<File> nodes = ctx.getFiles();
        int objectCount = nodes.size();
        // if all nodes represent project node the use plain name
        // It avoids "Show changes 2 files" on project node
        // caused by fact that project contains two source groups.

        Node[] activatedNodes = ctx.getElements().lookupAll(Node.class).toArray(new Node[0]);
        boolean projectsOnly = true;
        for (int i = 0; i < activatedNodes.length; i++) {
            Node activatedNode = activatedNodes[i];
            Project project =  (Project) activatedNode.getLookup().lookup(Project.class);
            if (project == null) {
                projectsOnly = false;
                break;
            }
        }
        if (projectsOnly) objectCount = activatedNodes.length;

        if (objectCount == 0) {
            return null;
        } else if (objectCount == 1) {
            if (projectsOnly) {
                return ProjectUtils.getInformation((Project) activatedNodes[0].getLookup().lookup(Project.class)).getDisplayName();
            }
            FileObject fo = (FileObject) activatedNodes[0].getLookup().lookup(FileObject.class);
            if (fo != null) {
                return fo.getNameExt();
            } else {
                DataObject dao = (DataObject) activatedNodes[0].getLookup().lookup(DataObject.class);
                if (dao instanceof DataShadow) {
                    dao = ((DataShadow) dao).getOriginal();
                }
                if (dao != null) {
                    return dao.getPrimaryFile().getNameExt();
                } else {
                    return activatedNodes[0].getDisplayName();
                }
            }
        } else {
            if (projectsOnly) {
                try {
                    return MessageFormat.format(NbBundle.getBundle(Utils.class).getString("MSG_ActionContext_MultipleProjects"), objectCount);  // NOI18N
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getBundle(Utils.class).getString("MSG_ActionContext_MultipleFiles"), objectCount);  // NOI18N
        }
    }

    /**
     * Open a read-only view of the file in editor area.
     *
     * @param fo a file to open
     * @param revision revision of the file
     * @return editor support opening the file
     */
    public static CloneableEditorSupport openFile(FileObject fo, String revision) {
        ViewEnv env = new ViewEnv(fo);
        CloneableEditorSupport ces = new ViewCES(env, fo.getNameExt() + " @ " + revision, FileEncodingQuery.getEncoding(fo)); // NOI18N
        ces.view();
        return ces;
    }

    /**
     * Opens a file in the editor area.
     *
     * @param file a File to open
     */
    public static void openFile(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                final OpenCookie oc = dao.getLookup().lookup(OpenCookie.class);
                if (oc != null) {
                    Mutex.EVENT.readAccess(new Runnable() {

                        @Override
                        public void run () {
                            oc.open();
                        }
                    });
                }
            } catch (DataObjectNotFoundException e) {
                // nonexistent DO, do nothing
            }
        }
    }

    private static final Object ENCODING_LOCK = new Object();
    private static Map<FileObject, Charset> fileToCharset;
    private static Map<File, FileObject> fileToFileObject;

    /**
     * Retrieves the Charset for the referenceFile and associates it weakly with
     * the given file. A following getAssociatedEncoding() call for
     * the file will then return the referenceFile-s Charset.
     *
     * @param referenceFile the file which charset has to be used when encoding file
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding(File referenceFile, File file) {
        FileObject refFO = FileUtil.toFileObject(referenceFile);
        if (refFO == null || refFO.isFolder()) {
            return;
        }
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null || fo.isFolder()) {
            return;
        }
        Charset c = FileEncodingQuery.getEncoding(refFO);
        if (c != null) {
            synchronized(ENCODING_LOCK) {
                if (fileToFileObject == null) {
                    fileToFileObject = new WeakHashMap<File, FileObject>();
                }
                fileToFileObject.put(file, fo);
            }
            associateEncoding(fo, c);
        }
    }
    
    /**
     * Retrieves the Charset for the referenceFile and associates it weakly with
     * the given file. A following getAssociatedEncoding() call for
     * the file will then return the referenceFile-s Charset.
     *
     * @param referenceFile the file which charset has to be used when encoding file
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding(FileObject refFo, FileObject fo) {
        if(refFo == null || refFo.isFolder()) {
            return;
        }
        if(fo == null || fo.isFolder()) {
            return;
        }
        Charset c = FileEncodingQuery.getEncoding(refFo);
        associateEncoding(fo, c);
    }

    /**
     * Associates a given charset weakly with
     * the given file. A following getAssociatedEncoding() call for
     * the file will then return the referenceFile-s Charset.
     *
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding (File file, Charset charset) {
        FileObject fo = FileUtil.toFileObject(file);
        if(fo == null) {
            LOG.log(Level.WARNING, "associateEncoding() no file object available for {0}", file); // NOI18N
            return;
        }
        associateEncoding(fo, charset);
    }
    
    /**
     * Associates a given charset weakly with
     * the given file. A following getAssociatedEncoding() call for
     * the file will then return the referenceFile-s Charset.
     *
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding (FileObject file, Charset charset) {
        if(charset == null) {
            return;
        }
        synchronized(ENCODING_LOCK) {
            if(fileToCharset == null) {
                fileToCharset = new WeakHashMap<FileObject, Charset>();
            }
            fileToCharset.put(file, charset);
        }
    }

    /**
     * Returns a charset for the given file if it was previously registered via associateEncoding()
     *
     * @param fo file for which the encoding has to be retrieved
     * @return the charset the given file has to be encoded with
     */
    public static Charset getAssociatedEncoding(FileObject fo) {
        try {
            synchronized(ENCODING_LOCK) {
                if(fileToCharset == null || fileToCharset.isEmpty() || fo == null || fo.isFolder()) {
                    return null;
                }
                Charset c = fileToCharset.get(fo);
                return c;
            }
        } catch (Throwable t) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, t);
            return  null;
        }
    }

    public static Reader createReader(File file) throws FileNotFoundException {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo == null) {
            return new FileReader(file);
        } else {
            return createReader(fo);
        }
    }

    public static Reader createReader(FileObject file) throws FileNotFoundException {
        return new InputStreamReader(file.getInputStream(), FileEncodingQuery.getEncoding(file));
    }

    /**
     * Convenience method for awkward Logger invocation.
     *
     * @param caller caller object for logger name determination
     * @param e exception that defines the error
     */
    public static void logInfo(Class caller, Throwable e) {
        Logger.getLogger(caller.getName()).log(Level.INFO, e.getMessage(), e);
    }

    /**
     * Convenience method for awkward Logger invocation.
     *
     * @param caller caller object for logger name determination
     * @param e exception that defines the error
     */
    public static void logWarn(Class caller, Throwable e) {
        Logger.getLogger(caller.getName()).log(Level.WARNING, e.getMessage(), e);
    }

    /**
     * Convenience method for awkward Logger invocation.
     *
     * @param caller caller object for logger name determination
     * @param e exception that defines the error
     */
    public static void logError(Object caller, Throwable e) {
        Logger.getLogger(caller.getClass().getName()).log(Level.SEVERE, e.getMessage(), e);
    }

    /**
     * Convenience method for awkward Logger invocation.
     *
     * @param caller caller object for logger name determination
     * @param e exception that defines the error
     */
    public static void logFine(Object caller, Exception e) {
        Logger.getLogger(caller.getClass().getName()).log(Level.FINE, e.getMessage(), e);
    }

    /**
     * Convenience method for awkward Logger invocation.
     *
     * @param caller caller object for logger name determination
     * @param e exception that defines the error
     */
    public static void logWarn(Object caller, Throwable e) {
        logWarn(caller.getClass(), e);
    }

    /**
     * Logs a vcs client usage.
     *
     * @param vcs - the particular vcs "SVN", "CVS", "CC", "HG", ...
     * @param client - the particular vcs cient "CLI", "JAVAHL", "JAVALIB"
     */
    public static void logVCSClientEvent(String vcs, String client) {
        String key = "USG_VCS_CLIENT"  + vcs;
        if (checkMetricsKey(key)) return;
        LogRecord rec = new LogRecord(Level.INFO, "USG_VCS_CLIENT");
        rec.setParameters(new Object[] { vcs, client });
        rec.setLoggerName(METRICS_LOG.getName());
        METRICS_LOG.log(rec);
    }

    /**
     * Logs a vcs external repository name.
     *
     * @param vcs - the particular vcs "SVN", "CVS", "CC", "HG", "GIT", ...
     * @param repositoryUrl - external repository url to log or null if the repository is local
     */
    public static void logVCSExternalRepository (String vcs, String repositoryUrl) {
        String repositoryIdent = getKnownRepositoryFor(repositoryUrl);
        String key = "USG_VCS_REPOSITORY" + vcs + repositoryIdent; //NOI18N
        if (checkMetricsKey(key)) return;
        LogRecord rec = new LogRecord(Level.INFO, "USG_VCS_REPOSITORY"); //NOI18N
        rec.setParameters(new Object[] { vcs, repositoryIdent });
        rec.setLoggerName(METRICS_LOG.getName());
        METRICS_LOG.log(rec);
    }

    /**
     * Logs a vcs client action usage.
     *
     * @param vcs - the particular vcs "SVN", "CVS", "CC", "HG", ...
     */
    public static void logVCSActionEvent(String vcs) {
        String key = "USG_VCS_ACTION"  + vcs;
        if (checkMetricsKey(key)) return;
        LogRecord rec = new LogRecord(Level.INFO, "USG_VCS_ACTION");
        rec.setParameters(new Object[] { vcs });
        rec.setLoggerName(METRICS_LOG.getName());
        METRICS_LOG.log(rec);
    }
    
    /**
     * Logs vcs command usage.
     *
     * @param vcs - the particular vcs "GIT", "HG", ...
     * @param time - time in millis the command took to finish
     * @param modifications - number of modified/created/deleted files during 
     * the command's progress
     * @param command - command name
     * @param external - true if the command was invoked externally
     * (e.g. on commandline) and not from within the IDE.
     */
    public static void logVCSCommandUsageEvent (String vcs, long time,
            long modifications, String command, boolean external) {
        if (command == null) {
            command = "UNKNOWN"; //NOI18N
        }
        LogRecord rec = new LogRecord(Level.INFO, "USG_VCS_CMD"); //NOI18N
        String cmdType = external ? "EXTERNAL" : "INTERNAL";
        rec.setResourceBundle(NbBundle.getBundle(Utils.class));
        rec.setResourceBundleName(Utils.class.getPackage().getName() + ".Bundle"); //NOI18N
        rec.setParameters(new Object[] { vcs, time, modifications, command, cmdType });
        rec.setLoggerName(UIGESTURES_LOG.getName());
        UIGESTURES_LOG.log(rec);
    }

    private static boolean checkMetricsKey(String key) {
        synchronized (metrics) {
            if (metrics.contains(key)) {
                return true;
            } else {
                metrics.add(key);
            }
        }
        return false;
    }

    private static String getKnownRepositoryFor (String repositoryUrl) {
        if (repositoryUrl == null) {
            return "LOCAL"; //NOI18N
        }
        repositoryUrl = repositoryUrl.toLowerCase();
        if (repositoryUrl.contains("github.com")) { //NOI18N
            return "GITHUB"; //NOI18N
        } else if (repositoryUrl.contains("bitbucket.org")) { //NOI18N
            return "BITBUCKET"; //NOI18N
        } else if (repositoryUrl.contains("sourceforge.net")) { //NOI18N
            return "SOURCEFORGE"; //NOI18N
        } else if (repositoryUrl.contains("netbeans.org")) { //NOI18N
            return "NETBEANS"; //NOI18N
        } else if (repositoryUrl.contains(".eclipse.org")) { //NOI18N
            return "ECLIPSE"; //NOI18N
        } else {
            return "OTHER"; //NOI18N
        }
    }

    /**
     * Sets or resets r/o flag.
     *
     * @param file a file to modify
     * @param ro true to make the file r/o, false to make the file r/w
     */
    public static void setReadOnly(File file, boolean readOnly) {
        // TODO: update for Java6
        String [] args;
        if (Utilities.isWindows()) {
            args = new String [] {"attrib", readOnly ? "+r": "-r", file.getName()}; //NOI18N
        } else {
            args = new String [] {"chmod", readOnly ? "u-w": "u+w", file.getName()}; //NOI18N
        }
        try {
            Process process = Runtime.getRuntime().exec(args, null, file.getParentFile());
            process.waitFor();
        } catch (Exception e) {
            logWarn(Utils.class, e);
        }
    }

    /**
     * Checks and removes from the given string all patterns being a word in braces
     * unless they are listed in supportedVariables<br>
     *
     * e.g.:<br>
     * string:  [{status}{folder}{dil}]<br>
     * supportedVariables: "{status}", "{folder}" <br>
     * will result to:<br>
     * [{status}{folder}]<br>
     *
     * @param string to be checked string
     * @param vars supported variables
     * @return 
     */
    public static String skipUnsupportedVariables(String string, String[] supportedVariables) {
        String ret = string;
        Pattern p = Pattern.compile("\\{\\w*\\}");
        Matcher m = p.matcher(string);
        while(m.find()) {
            String g = m.group();
            boolean isVar = false;
            for (String var : supportedVariables) {
                if(var.equals(g)) {
                    isVar=true;
                    break;
                }
            }
            if(!isVar) {
                ret = ret.replace(g, "");
            }
        }
        return ret;
    }

    /**
     * Checks if the context was originally created from files, not from nodes and if so
     * then it tries to determine if those original files are part of a single DataObject.
     * Call only if the context was created from files (not from nodes), otherwise always returns false.
     *
     * @param ctx context to be checked
     * @return true if the context was created from files of the same DataObject
     */
    public static boolean isFromMultiFileDataObject (VCSContext ctx) {
        if (ctx != null) {
            Collection<? extends Set> allSets = ctx.getElements().lookupAll(Set.class);
            if (allSets != null) {
                for (Set contextElements : allSets) {
                    // private contract with org.openide.loaders - original files from multifile dataobjects are passed as
                    // org.openide.loaders.DataNode$LazyFilesSet
                    if ("org.openide.loaders.DataNode$LazyFilesSet".equals(contextElements.getClass().getName())) { //NOI18N
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private static File getTempDir (boolean deleteOnExit) {
        if (tempDir == null) {
            File tmpDir = new File(System.getProperty("java.io.tmpdir"));   // NOI18N
            for (;;) {
                File dir = new File(tmpDir, "vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
                if (!dir.exists() && dir.mkdirs()) {
                    tempDir = FileUtil.normalizeFile(dir);
                    if (deleteOnExit) {
                        tempDir.deleteOnExit();
                    }
                    break;
                }
            }
        }
        return tempDir;
    }

    private static class ViewEnv implements CloneableEditorSupport.Env {

        private final FileObject    file;
        private static final long serialVersionUID = -5788777967029507963L;

        public ViewEnv(FileObject file) {
            this.file = file;
        }

        @Override
        public InputStream inputStream() throws IOException {
            return file.getInputStream();
        }

        @Override
        public OutputStream outputStream() throws IOException {
            throw new IOException();
        }

        @Override
        public Date getTime() {
            return file.lastModified();
        }

        @Override
        public String getMimeType() {
            return file.getMIMEType();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener l) {
        }

        @Override
        public void addVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public void removeVetoableChangeListener(VetoableChangeListener l) {
        }

        @Override
        public boolean isValid() {
            return file.isValid();
        }

        @Override
        public boolean isModified() {
            return false;
        }

        @Override
        public void markModified() throws IOException {
            throw new IOException();
        }

        @Override
        public void unmarkModified() {
        }

        @Override
        public CloneableOpenSupport findCloneableOpenSupport() {
            return null;
        }
    }

    private static class ViewCES extends CloneableEditorSupport {

        private final String name;
        private final Charset charset;

        public ViewCES(Env env, String name, Charset charset) {
            super(env);
            this.name = name;
            this.charset = charset;
        }

        @Override
        protected void loadFromStreamToKit(StyledDocument doc, InputStream stream, EditorKit kit) throws IOException, BadLocationException {
            kit.read(new InputStreamReader(stream, charset), doc, 0);
        }

        @Override
        protected String messageSave() {
            return name;
        }

        @Override
        protected String messageName() {
            return name;
        }

        @Override
        protected String messageToolTip() {
            return name;
        }

        @Override
        protected String messageOpening() {
            return name;
        }

        @Override
        protected String messageOpened() {
            return name;
        }

        @Override
        protected boolean asynchronousOpen() {
            return false;
        }
    }

    /**
     * Determines versioning systems that manage files in given context.
     * 
     * @param ctx VCSContext to examine
     * @return VersioningSystem systems that manage this context or an empty array if the context is not versioned
     */
    public static VersioningSystem[] getOwners(VCSContext ctx) {
        Set<File> files = ctx.getRootFiles();
        Set<VersioningSystem> owners = new HashSet<VersioningSystem>();
        for (File file : files) {
            VersioningSystem vs = VersioningSupport.getOwner(file);
            if (vs != null) {
                owners.add(vs);
            }
        }
        return (VersioningSystem[]) owners.toArray(new VersioningSystem[0]);
    }

    /**
     * Returns hash value for the given byte array and algoritmus in a hex string form.
     * @param alg Algoritmus to compute the hash value (see also Appendix A in the
     * Java Cryptography Architecture API Specification &amp; Reference </a>
     * for information about standard algorithm names.)
     * @param bytes byte array
     * @return hash value as a string
     * @throws java.security.NoSuchAlgorithmException
     */
    public static String getHash(String alg, byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance(alg);
        md5.update(bytes);
        byte[] md5digest = md5.digest();
        String ret = ""; // NOI18N
        for (int i = 0; i < md5digest.length; i++) {
            String hex = Integer.toHexString(md5digest[i] & 0x000000FF);
            if (hex.length() == 1) {
                hex = "0" + hex; // NOI18N
            }
            ret += hex + (i < md5digest.length - 1 ? ":" : ""); // NOI18N
        }
        return ret;
    }

    /**
     * Returns files from all opened top components
     * @return set of opened files
     */
    public static Set<File> getOpenFiles() {
        TopComponent[] comps = TopComponent.getRegistry().getOpened().toArray(new TopComponent[0]);
        Set<File> openFiles = new HashSet<File>(comps.length);
        for (TopComponent tc : comps) {
            Node[] nodes = tc.getActivatedNodes();
            if (nodes == null) {
                continue;
            }
            for (Node node : nodes) {
                File file = node.getLookup().lookup(File.class);
                if (file == null) {
                    FileObject fo = node.getLookup().lookup(FileObject.class);
                    if (fo != null && fo.isData()) {
                        file = FileUtil.toFile(fo);
                    }
                }
                if (file != null) {
                    openFiles.add(file);
                }
            }
        }
        return openFiles;
    }

    /**
     * Switches the wait cursor on the NetBeans glasspane of/on
     * 
     * @param on
     */
    public static void setWaitCursor(final boolean on) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                JFrame mainWindow = (JFrame) WindowManager.getDefault().getMainWindow();
                mainWindow
                    .getGlassPane()
                    .setCursor(Cursor.getPredefinedCursor(
                        on ?
                        Cursor.WAIT_CURSOR :
                        Cursor.DEFAULT_CURSOR));
                mainWindow.getGlassPane().setVisible(on);
            }
        };
        if(EventQueue.isDispatchThread()) {
            r.run();
        } else {
            EventQueue.invokeLater(r);
        }
    }
    
    /**
     * Returns the {@link Project} {@link File} for the given context
     *
     * @param VCSContext
     * @return File of Project Directory
     */
    public static File getProjectFile(VCSContext context){
        return getProjectFile(getProject(context));
    }

    /**
     * Returns {@link Project} for the given context
     * 
     * @param context
     * @return 
     */
    public static Project getProject(VCSContext context){
        if (context == null) return null;
        return getProject(context.getRootFiles().toArray(new File[context.getRootFiles().size()]));
    }
    
    public static Project getProject (File[] files) {
        for (File file : files) {
            /* We may be committing a LocallyDeleted file */
            if (!file.exists()) file = file.getParentFile();
            FileObject fo = FileUtil.toFileObject(file);
            if(fo == null) {
                LOG.log(Level.FINE, "Utils.getProjectFile(): No FileObject for {0}", file); // NOI18N
            } else {
                Project p = FileOwnerQuery.getOwner(fo);
                if (p != null) {
                    return p;
                } else {
                    LOG.log(Level.FINE, "Utils.getProjectFile(): No project for {0}", file); // NOI18N
                }
            }
        }
        return null;
    }
    
    /**
     * Returns the {@link Project} {@link File} for the given {@link Project}
     * 
     * @param project
     * @return 
     */
    public static File getProjectFile(Project project){
        if (project == null) return null;

        FileObject fo = project.getProjectDirectory();
        return  FileUtil.toFile(fo);
    }

    /**
     * Returns all root files for the given {@link Project}
     * 
     * @param project
     * @return 
     */
    public static File[] getProjectRootFiles(Project project){
        if (project == null) return null;
        Set<File> set = new HashSet<File>();

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup [] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        for (int j = 0; j < sourceGroups.length; j++) {
            SourceGroup sourceGroup = sourceGroups[j];
            FileObject srcRootFo = sourceGroup.getRootFolder();
            File rootFile = FileUtil.toFile(srcRootFo);
            set.add(rootFile);
        }
        return set.toArray(new File[0]);
    }    
    
    public static void setAcceleratorBindings(String pathPrefix, Action... actions) {
        for (Action a : actions) {
            if(a == null) continue;
            Action foAction;
            if(a instanceof SystemActionBridge) {
                foAction = ((SystemActionBridge) a).getDelegate();
            } else {
                foAction = a;
            }
            if(!pathPrefix.endsWith("/")) {                                     // NOI18N
                pathPrefix += "/";                                              // NOI18N
            }
            FileObject fo = FileUtil.getConfigFile(pathPrefix + foAction.getClass().getName().replaceAll("\\.", "-") + ".instance"); // NOI18N
            if(fo != null) {   
                AcceleratorBinding.setAccelerator(a, fo);
            }
        }
    }
    
    public static Action getAcceleratedAction(String path) {
        // or use Actions.forID
        Action a = FileUtil.getConfigObject(path, Action.class);
        FileObject fo = FileUtil.getConfigFile(path);
        if(fo != null) {
            AcceleratorBinding.setAccelerator(a, fo);
        }
        return a;
    }
    
    /**
     * Guesses the line-ending used in the file.
     * Default OS line-ending is used when file has no newlines.
     * 
     * @param fo file to get line-ending for.
     * @param lock file lock
     * @return 
     */
    public static String getLineEnding (FileObject fo, FileLock lock) {
        if (!lock.isValid()) {
            throw new IllegalStateException();
        }
        String newLineStr = (String) fo.getAttribute(FileObject.DEFAULT_LINE_SEPARATOR_ATTR);
        if (newLineStr == null || newLineStr.isEmpty()) {
            newLineStr = System.getProperty("line.separator"); //NOI18N
        }
        
        try (InputStream is = new BufferedInputStream(fo.getInputStream())) {
            String lineEnding = getLineEnding(is);
            if (!lineEnding.isEmpty()) {
                newLineStr = lineEnding;
            }
        } catch (IOException ex) {
            
        }
        
        return newLineStr;
    }
    
    static String getLineEnding (InputStream is) throws IOException {
        String newLineStr = "";
        
        byte [] buffer = new byte[1024];
        int n;
        boolean finished = false;
        List<String> allowed = Arrays.asList(new String[] { "\n", "\r", "\r\n" });
        while (!finished && (n = is.read(buffer)) != -1) {
            for (int i = 0; i < n; ++i) {
                byte c = buffer[i];
                if ((c == '\n' || c == '\r') && allowed.contains(newLineStr + (char) c)) {
                    newLineStr += (char) c;
                } else if (!newLineStr.isEmpty()) {
                    finished = true;
                    break;
                }
            }
        }
        
        return newLineStr;
    }

}
