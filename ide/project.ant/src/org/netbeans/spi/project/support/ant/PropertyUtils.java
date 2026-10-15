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

package org.netbeans.spi.project.support.ant;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ProjectManager;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbCollections;
import org.openide.util.RequestProcessor;

/**
 * Support for working with Ant properties and property files.
 * @author Jesse Glick
 */
public class PropertyUtils {
    
    private PropertyUtils() {}
    
    /**
     * Location in user directory of per-user global properties.
     * May be null if <code>netbeans.user</code> is not set.
     */
    static File userBuildProperties() {
        File nbuser = Places.getUserDirectory();
        if (nbuser != null) {
            return FileUtil.normalizeFile(new File(nbuser, "build.properties")); // NOI18N
        } else {
            return null;
        }
    }
    
    private static Map<File,Reference<PropertyProvider>> globalPropertyProviders = new HashMap<File,Reference<PropertyProvider>>();
    private static EditableProperties currentGlobalProperties;
    private static File currentGlobalPropertiesFile;
    private static long currentGlobalPropertiesLastModified;
    private static long currentGlobalPropertiesLength;
    
    /**
     * Load global properties defined by the IDE in the user directory.
     * Currently loads ${netbeans.user}/build.properties if it exists.
     * <p>
     * Acquires read access.
     * <p>
     * To listen to changes use {@link #globalPropertyProvider}.
     * @return user properties (empty if missing or malformed)
     */
    public static EditableProperties getGlobalProperties() {
        return ProjectManager.mutex().readAccess(new Mutex.Action<EditableProperties>() {
            public EditableProperties run() {
                File ubp = userBuildProperties();
                if (ubp != null && ubp.isFile() && ubp.canRead()) {
                    long lastModified = ubp.lastModified();
                    long length = ubp.length();
                    if (ubp.equals(currentGlobalPropertiesFile) &&
                            lastModified == currentGlobalPropertiesLastModified &&
                            length == currentGlobalPropertiesLength) {
                        return currentGlobalProperties.cloneProperties();
                    }
                    try {
                        InputStream is = new FileInputStream(ubp);
                        try {
                            EditableProperties properties = new EditableProperties(true);
                            properties.load(is);
                            currentGlobalProperties = properties.cloneProperties();
                            currentGlobalPropertiesFile = ubp;
                            currentGlobalPropertiesLastModified = lastModified;
                            currentGlobalPropertiesLength = length;
                            return properties;
                        } finally {
                            is.close();
                        }
                    } catch (IOException e) {
                        Logger.getLogger(PropertyUtils.class.getName()).log(Level.INFO, null, e);
                    }
                }
                // Missing or erroneous.
                return new EditableProperties(true);
            }
        });
    }
    
    /**
     * Edit global properties defined by the IDE in the user directory.
     * <p>
     * Acquires write access.
     * @param properties user properties to set
     * @throws IOException if they could not be stored
     * @see #getGlobalProperties
     */
    public static void putGlobalProperties(final EditableProperties properties) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    File ubp = userBuildProperties();
                    if (ubp != null) {
                        long lastModified = ubp.lastModified();
                        long length = ubp.length();
                        if (ubp.equals(currentGlobalPropertiesFile) &&
                                lastModified == currentGlobalPropertiesLastModified &&
                                length == currentGlobalPropertiesLength &&
                                properties.equals(currentGlobalProperties)) {
                            return null;
                        }
                        FileObject bp = FileUtil.toFileObject(ubp);
                        if (bp == null) {
                            if (!ubp.exists()) {
                                ubp.getParentFile().mkdirs();
                                new FileOutputStream(ubp).close();
                                assert ubp.isFile() : "Did not actually make " + ubp;
                            }
                            bp = FileUtil.toFileObject(ubp);
                            if (bp == null) {
                                // XXX ugly (and will not correctly notify changes) but better than nothing:
                                Logger.getLogger(this.getClass().getName()).log(
                                    Level.WARNING, 
                                    "Warning - cannot properly write to {0}; "
                                    + "might be because your user directory is on a Windows UNC path (issue #46813)? "
                                    + "If so, try using mapped drive letters.", 
                                    ubp);
                                OutputStream os = new FileOutputStream(ubp);
                                try {
                                    properties.store(os);
                                } finally {
                                    os.close();
                                }
                                return null;
                            }
                        }
                        OutputStream os = bp.getOutputStream();
                        try {
                            properties.store(os);
                        } finally {
                            os.close();
                        }
                        currentGlobalProperties = properties.cloneProperties();
                        currentGlobalPropertiesFile = ubp;
                        currentGlobalPropertiesLastModified = lastModified;
                        currentGlobalPropertiesLength = length;
                    } else {
                        throw new IOException("Do not know where to store build.properties; must set netbeans.user!"); // NOI18N
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException)e.getException();
        }
    }
    
    /**
     * Create a property evaluator based on {@link #getGlobalProperties}
     * and {@link #putGlobalProperties}.
     * It will supply global properties and fire changes when this file
     * is changed.
     * @return a property producer
     */
    public static synchronized PropertyProvider globalPropertyProvider() {
        File ubp = userBuildProperties();
        if (ubp != null) {
            Reference<PropertyProvider> globalPropertyProvider = globalPropertyProviders.get(ubp);
            if (globalPropertyProvider != null) {
                PropertyProvider pp = globalPropertyProvider.get();
                if (pp != null) {
                    return pp;
                }
            }
            PropertyProvider gpp = propertiesFilePropertyProvider(ubp);
            globalPropertyProviders.put(ubp, new SoftReference<PropertyProvider>(gpp));
            return gpp;
        } else {
            return fixedPropertyProvider(Collections.<String,String>emptyMap());
        }
    }

    /**
     * Create a property provider based on a properties file.
     * The file need not exist at the moment; if it is created or deleted an appropriate
     * change will be fired. If its contents are changed on disk a change will also be fired.
     * @param propertiesFile a path to a (possibly nonexistent) *.properties file
     * @return a supplier of properties from such a file
     * @see Properties#load
     */
    public static PropertyProvider propertiesFilePropertyProvider(File propertiesFile) {
        assert propertiesFile != null;
        return new FilePropertyProvider(propertiesFile);
    }
    
    /**
     * Provider based on a named properties file.
     */
    private static final class FilePropertyProvider implements PropertyProvider, FileChangeListener {
        
        private static final RequestProcessor RP = new RequestProcessor("PropertyUtils.FilePropertyProvider.RP"); // NOI18N
        
        private final File properties;
        private final ChangeSupport cs = new ChangeSupport(this);
        private Map<String,String> cached = null;
        private long cachedTime = 0L;
        
        public FilePropertyProvider(File properties) {
            this.properties = properties;
            FileUtil.addFileChangeListener(this, properties);
        }
        
        public Map<String,String> getProperties() {
            long currTime = properties.lastModified();
            if (cached == null || cachedTime != currTime) {
                cachedTime = currTime;
                cached = loadProperties();
            }
            return cached;
        }
        
        private Map<String,String> loadProperties() {
            // XXX does this need to run in PM.mutex.readAccess?
            if (properties.isFile() && properties.canRead()) {
                try {
                    InputStream is = new FileInputStream(properties);
                    try {
                        Properties props = new Properties();
                        props.load(is);
                        return NbCollections.checkedMapByFilter(props, String.class, String.class, true);
                    } catch (IllegalArgumentException iae) {
                        Logger.getLogger(PropertyUtils.class.getName()).log(Level.WARNING, "Property file: " + properties.getPath(), iae);
                    } finally {
                        is.close();
                    }
                } catch (IOException e) {
                    Logger.getLogger(PropertyUtils.class.getName()).log(Level.INFO, null, e);
                }
            }
            // Missing or erroneous.
            return Collections.emptyMap();
        }
        
        private void fireChange() {
            cachedTime = -1L; // force reload
            if (!cs.hasListeners()) {
                return;
            }
            final Mutex.Action<Void> action = new Mutex.Action<Void>() {
                public Void run() {
                    cs.fireChange();
                    return null;
                }
            };
            if (ProjectManager.mutex().isWriteAccess()) {
                // Run it right now. postReadRequest would be too late.
                ProjectManager.mutex().readAccess(action);
            } else if (ProjectManager.mutex().isReadAccess()) {
                // Run immediately also. No need to switch to read access.
                action.run();
            } else {
                // Not safe to acquire a new lock, so run later in read access.
                RP.post(new Runnable() {
                    public void run() {
                        ProjectManager.mutex().readAccess(action);
                    }
                });
            }
        }
        
        public synchronized void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }
        
        public synchronized void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        public void fileFolderCreated(FileEvent fe) {
            fireChange();
        }

        public void fileDataCreated(FileEvent fe) {
            fireChange();
        }

        public void fileChanged(FileEvent fe) {
            fireChange();
        }

        public void fileRenamed(FileRenameEvent fe) {
            fireChange();
        }

        public void fileAttributeChanged(FileAttributeEvent fe) {
            fireChange();
        }

        public void fileDeleted(FileEvent fe) {
            fireChange();
        }

        @Override
        public String toString() {
            return "FilePropertyProvider[" + properties + ":" + getProperties() + "]"; // NOI18N
        }
    }
    
    private static final Pattern RELATIVE_SLASH_SEPARATED_PATH = Pattern.compile("[^:/\\\\.][^:/\\\\]*(/[^:/\\\\.][^:/\\\\]*)*"); // NOI18N
    
    /**
     * Find an absolute file path from a possibly relative path.
     * @param basedir base file for relative filename resolving; must be an absolute path
     * @param filename a pathname which may be relative or absolute and may
     *                 use / or \ as the path separator
     * @return an absolute file corresponding to it
     * @throws IllegalArgumentException if basedir is not absolute
     */
    public static File resolveFile(File basedir, String filename) throws IllegalArgumentException {
        if (basedir == null) {
            throw new NullPointerException("null basedir passed to resolveFile"); // NOI18N
        }
        if (filename == null) {
            throw new NullPointerException("null filename passed to resolveFile"); // NOI18N
        }
        if (!basedir.isAbsolute()) {
            throw new IllegalArgumentException("nonabsolute basedir passed to resolveFile: " + basedir); // NOI18N
        }
        File f;
        if (RELATIVE_SLASH_SEPARATED_PATH.matcher(filename).matches()) {
            // Shortcut - simple relative path. Potentially faster.
            f = new File(basedir, filename.replace('/', File.separatorChar));
        } else {
            // All other cases.
            String machinePath = filename.replace('/', File.separatorChar).replace('\\', File.separatorChar);
            f = new File(machinePath);
            if (!f.isAbsolute()) {
                f = new File(basedir, machinePath);
            }
            assert f.isAbsolute();
        }
        return FileUtil.normalizeFile(f);
    }
    
    /**
     * Produce a machine-independent relativized version of a filename from a basedir.
     * Unlike {@link URI#relativize} this will produce "../" sequences as needed.
     * @param basedir a directory to resolve relative to (need not exist on disk)
     * @param file a file or directory to find a relative path for
     * @return a relativized path (slash-separated), or null if it is not possible (e.g. different DOS drives);
     *         just <em>.</em> in case the paths are the same
     * @throws IllegalArgumentException if the basedir is known to be a file and not a directory
     */
    public static @CheckForNull String relativizeFile(File basedir, File file) {
        if (basedir.isFile()) {
            throw new IllegalArgumentException("Cannot relative w.r.t. a data file " + basedir); // NOI18N
        }
        if (basedir.equals(file)) {
            return "."; // NOI18N
        }
        StringBuilder b = new StringBuilder();
        File base = basedir;
        String filepath = file.getAbsolutePath();
        while (!filepath.startsWith(slashify(base.getAbsolutePath()))) {
            base = base.getParentFile();
            //#231704 when 2 UNC paths have different case, they are equal.
            //better to show as non-relative rather than throw an assert.
            if (base == null || (BaseUtilities.isWindows() && "\\\\".equals(base.getAbsolutePath()))) {
                return null;
            }
            if (base.equals(file)) {
                // #61687: file is a parent of basedir
                b.append(".."); // NOI18N
                return b.toString();
            }
            b.append("../"); // NOI18N
        }
        URI u = BaseUtilities.toURI(base).relativize(BaseUtilities.toURI(file));
        assert !u.isAbsolute() : u + " from " + basedir + " and " + file + " with common root " + base;
        if(u.getPath().isEmpty()) {
            return "."; // NOI18N
        }
        b.append(u.getPath());
        if (b.charAt(b.length() - 1) == '/') {
            // file is an existing directory and file.toURI ends in /
            // we do not want the trailing slash
            b.setLength(b.length() - 1);
        }
        return b.toString();
    }
    
    private static String slashify(String path) {
        if (path.endsWith(File.separator)) {
            return path;
        } else {
            return path + File.separatorChar;
        }
    }
    
    /*public? */ static FileObject resolveFileObject(FileObject basedir, String filename) {
        // an absolute path, or \-separated, or . or .. components, etc.; use the safer method.
        return FileUtil.toFileObject(resolveFile(FileUtil.toFile(basedir), filename));
    }
    
    /*public? */ static String resolvePath(File basedir, String path) {
        StringBuilder b = new StringBuilder();
        String[] toks = tokenizePath(path);
        for (int i = 0; i < toks.length; i++) {
            if (i > 0) {
                b.append(File.pathSeparatorChar);
            }
            b.append(resolveFile(basedir, toks[i]).getAbsolutePath());
        }
        return b.toString();
    }
    
    /**
     * Split an Ant-style path specification into components.
     * Tokenizes on <code>:</code> and <code>;</code>, paying
     * attention to DOS-style components such as <em>C:\FOO</em>.
     * Also removes any empty components.
     * @param path an Ant-style path (elements arbitrary) using DOS or Unix separators
     * @return a tokenization of that path into components
     */
    public static String[] tokenizePath(String path) {
        List<String> l = new ArrayList<String>();
        StringTokenizer tok = new StringTokenizer(path, ":;", true); // NOI18N
        char dosHack = '\0';
        char lastDelim = '\0';
        int delimCount = 0;
        while (tok.hasMoreTokens()) {
            String s = tok.nextToken();
            if (s.length() == 0) {
                // Strip empty components.
                continue;
            }
            if (s.length() == 1) {
                char c = s.charAt(0);
                if (c == ':' || c == ';') {
                    // Just a delimiter.
                    lastDelim = c;
                    delimCount++;
                    continue;
                }
            }
            if (dosHack != '\0') {
                // #50679 - "C:/something" is also accepted as DOS path
                if (lastDelim == ':' && delimCount == 1 && (s.charAt(0) == '\\' || s.charAt(0) == '/')) {
                    // We had a single letter followed by ':' now followed by \something or /something
                    s = "" + dosHack + ':' + s;
                    // and use the new token with the drive prefix...
                } else {
                    // Something else, leave alone.
                    l.add(Character.toString(dosHack));
                    // and continue with this token too...
                }
                dosHack = '\0';
            }
            // Reset count of # of delimiters in a row.
            delimCount = 0;
            if (s.length() == 1) {
                char c = s.charAt(0);
                if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                    // Probably a DOS drive letter. Leave it with the next component.
                    dosHack = c;
                    continue;
                }
            }
            l.add(s);
        }
        if (dosHack != '\0') {
            //the dosHack was the last letter in the input string (not followed by the ':')
            //so obviously not a drive letter.
            //Fix for issue #57304
            l.add(Character.toString(dosHack));
        }
        return l.toArray(new String[0]);
    }

    private static final Pattern VALID_PROPERTY_NAME = Pattern.compile("[-._a-zA-Z0-9]+"); // NOI18N

    /**
     * Checks whether the name is usable as Ant property name.
     * @param name name to check for usability as Ant property
     * @return true if name is usable otherwise false
     */
    public static boolean isUsablePropertyName(String name) {
        return VALID_PROPERTY_NAME.matcher(name).matches();
    }

    /**
     * Returns name usable as Ant property which is based on the given
     * name. All forbidden characters are either removed or replaced with
     * suitable ones.
     * @param name name to use as base for Ant property name
     * @return name usable as Ant property name
     */
    public static String getUsablePropertyName(String name) {
        if (isUsablePropertyName(name)) {
            return name;
        }
        StringBuilder sb = new StringBuilder(name);
        for (int i=0; i<sb.length(); i++) {
            if (!isUsablePropertyName(sb.substring(i,i+1))) {
                sb.replace(i,i+1,"_");
            }
        }
        return sb.toString();
    }
    
    /**
     * Create a trivial property producer using only a fixed list of property definitions.
     * Its values are constant, and it never fires changes.
     * @param defs a map from property names to values (it is illegal to modify this map
     *             after passing it to this method)
     * @return a matching property producer
     */
    public static PropertyProvider fixedPropertyProvider(Map<String,String> defs) {
        return new FixedPropertyProvider(defs);
    }
    
    private static final class FixedPropertyProvider implements PropertyProvider {
        
        private final Map<String,String> defs;
        
        public FixedPropertyProvider(Map<String,String> defs) {
            this.defs = defs;
        }
        
        public Map<String,String> getProperties() {
            return defs;
        }
        
        public void addChangeListener(ChangeListener l) {}
        
        public void removeChangeListener(ChangeListener l) {}
        
    }
    
    /**
     * Create a property evaluator based on a series of definitions.
     * <p>
     * Each batch of definitions can refer to properties within itself
     * (so long as there is no cycle) or any previous batch.
     * However the special first provider cannot refer to properties within itself.
     * </p>
     * <p>
     * This implementation acquires {@link ProjectManager#mutex} for all operations, in read mode,
     * and fires changes synchronously. It also expects changes to be fired from property
     * providers in read (or write) access.
     * </p>
     * @param preprovider an initial context (may be null)
     * @param providers a sequential list of property groups
     * @return an evaluator
     */
    public static PropertyEvaluator sequentialPropertyEvaluator(PropertyProvider preprovider, PropertyProvider... providers) {
        return new SequentialPropertyEvaluator(preprovider, providers);
    }

    /**
     * Creates a property provider similar to {@link #globalPropertyProvider}
     * but which can use a different global properties file.
     * If a specific file is pointed to, that is loaded; otherwise behaves like {@link #globalPropertyProvider}.
     * Permits behavior similar to command-line Ant where not erroneous, but using the IDE's
     * default global properties for projects which do not yet have this property registered.
     * @param findUserPropertiesFile an evaluator in which to look up <code>propertyName</code>
     * @param propertyName a property pointing to the global properties file (typically <code>"user.properties.file"</code>)
     * @param basedir a base directory to use when resolving the path to the global properties file, if relative
     * @return a provider of global properties
     * @since org.netbeans.modules.project.ant/1 1.14
     */
    public static PropertyProvider userPropertiesProvider(PropertyEvaluator findUserPropertiesFile, String propertyName, File basedir) {
        return new UserPropertiesProvider(findUserPropertiesFile, propertyName, basedir);
    }
    private static final class UserPropertiesProvider extends FilterPropertyProvider implements PropertyChangeListener {
        private final PropertyEvaluator findUserPropertiesFile;
        private final String propertyName;
        private final File basedir;
        public UserPropertiesProvider(PropertyEvaluator findUserPropertiesFile, String propertyName, File basedir) {
            super(computeDelegate(findUserPropertiesFile, propertyName, basedir));
            this.findUserPropertiesFile = findUserPropertiesFile;
            this.propertyName = propertyName;
            this.basedir = basedir;
            findUserPropertiesFile.addPropertyChangeListener(this);
        }
        public void propertyChange(PropertyChangeEvent ev) {
            if (propertyName.equals(ev.getPropertyName())) {
                setDelegate(computeDelegate(findUserPropertiesFile, propertyName, basedir));
            }
        }
        private static PropertyProvider computeDelegate(PropertyEvaluator findUserPropertiesFile, String propertyName, File basedir) {
            String userPropertiesFile = findUserPropertiesFile.getProperty(propertyName);
            if (userPropertiesFile != null) {
                // Have some defined global properties file, so read it and listen to changes in it.
                File f = PropertyUtils.resolveFile(basedir, userPropertiesFile);
                if (f.equals(PropertyUtils.userBuildProperties())) {
                    // Just to share the cache.
                    return PropertyUtils.globalPropertyProvider();
                } else {
                    return PropertyUtils.propertiesFilePropertyProvider(f);
                }
            } else {
                // Use the in-IDE default.
                return PropertyUtils.globalPropertyProvider();
            }
        }
    }

}
