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
package org.netbeans.modules.remotefs.versioning.api;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.remotefs.versioning.api.ProcessUtils.ExitStatus;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.netbeans.modules.versioning.core.api.VersioningSupport;
import org.netbeans.modules.versioning.core.spi.VCSContext;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.loaders.DataShadow;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 *
 */
public final class VCSFileProxySupport {
    
    private VCSFileProxySupport(){
    }

    public static final class VCSFileProxyComparator implements Comparator<VCSFileProxy> {

        @Override
        public int compare(VCSFileProxy o1, VCSFileProxy o2) {
            return o1.getPath().compareTo(o2.getPath());
        }
    }
    
    public static void deleteExternally(VCSFileProxy file) {
        RemoteVcsSupport.deleteExternally(file);
    }
    
    public static void delete(VCSFileProxy file) {
        // TODO: should not it return status or throw an exception on failure?
        RemoteVcsSupport.delete(file);
    }

    /**
     * Deletes on disconnect
     * @param file file to delete
     */
    public static void deleteOnExit(VCSFileProxy file) {
        RemoteVcsSupport.deleteOnExit(file);
    }

    /**
     * Sets file timestamp equals to the timestamp of another (reference) file
     * @param file file to set timestamp
     * @param referenceFile reference file
     * NB: both files must be on the same file system !!!
     */
    public static void setLastModified(VCSFileProxy file, VCSFileProxy referenceFile) {
        RemoteVcsSupport.setLastModified(file, referenceFile);
    }
    
    public static boolean mkdir(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.mkdir();
        } else {
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(file.getParentFile().getPath(), null, false, new ProcessUtils.Canceler(), file,
                    "mkdir", file.getPath()); //NOI18N
            if (!status.isOK()) {
                LOG.log(Level.INFO, "mkdir {0} failed: {1}", new Object[]{file.getPath(), status.toString()}); //NOI18N
                return false;
            } else {
                return refreshImpl(file);
            }
        }
    }
    
    public static boolean mkdirs(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.mkdirs();
        } else {
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(null, null, false, new ProcessUtils.Canceler(), file,
                    "mkdir", "-p", file.getPath()); //NOI18N
            if (!status.isOK()) {
                LOG.log(Level.INFO, "mkdir -p {0} failed: {1}", new Object[]{file, status}); //NOI18N
                return false;
            } else {
                return refreshImpl(file);
            }
        }
    }
    
    public static boolean setExecutable(VCSFileProxy file, boolean b) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.setExecutable(b);
        } else {
            // TODO: 
            throw new UnsupportedOperationException();
        }
    }

    public static VCSFileProxy createSymbolicLink(VCSFileProxy link, String relPath) throws IOException {
        File javaFile = link.toFile();
        if (javaFile != null) {
            Path createdSymbolicLink = Files.createSymbolicLink(Paths.get(javaFile.getPath()), Paths.get(relPath));
            return VCSFileProxy.createFileProxy(createdSymbolicLink.toFile());
        } else {
            // TODO: 
            throw new UnsupportedOperationException();
        }
    }

    public static VCSFileProxy fromURI(URI uri) {
        if ("file".equals(uri.getScheme())) { // NOI18N
            return VCSFileProxy.createFileProxy(new File(uri));
        } else {
            try {
                List<String> segments = new ArrayList<>();
                FileObject fo = findExistingParent(uri, segments);
                VCSFileProxy res = VCSFileProxy.createFileProxy(fo);
                for (int i = segments.size() - 1; i >= 0; i--) {
                    res = VCSFileProxy.createFileProxy(res, segments.get(i));
                }
                return res;
            } catch (URISyntaxException ex) {
                LOG.log(Level.INFO, null, ex);
            } catch (MalformedURLException ex) {
                LOG.log(Level.INFO, null, ex);
            }
            return null;
        }
    }

    private static FileObject findExistingParent(URI first, List<String> segments) throws MalformedURLException, URISyntaxException {
        URI uri = first;
        while (true) {
            URL url =  uri.toURL();
            FileObject fo = URLMapper.findFileObject(url);
            if (fo != null) {
                return fo;
            }
            String path = uri.getPath();
            int i = path.lastIndexOf('/'); //NOI18N
            if (i < 0) {
                i = path.lastIndexOf('\\'); //NOI18N
            }
            if (i < 0) {
                throw new MalformedURLException("URI "+first.toString()); //NOI18N
            }
            segments.add(path.substring(i+1));
            path = path.substring(0, i);
            uri = new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
        }
    }
    
    public static URI toURI(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            return javaFile.toURI();
        }
        URI res = RemoteVcsSupport.toURI(file);
        if (res != null) {
            return res;
        }
        // Ideally, the code below should be thrown away - RemoteVcsSupport.toURI
        // should do all the job. But I'm too afraid of doing this right before 8.2 patch.
        // So the below is a "just in case" fallback. VK.
        try {
            List<String> segments = new ArrayList<>();
            FileObject fo = findExistingParent(file, segments);
            res = fo.toURI();
            for (int i = segments.size() - 1; i >= 0; i--) {
                String path;
                if (res.getPath().endsWith("/")) { //NOI18N
                    path = res.getPath()+segments.get(i);
                } else {
                    path = res.getPath()+"/"+segments.get(i); //NOI18N
                }
                res = new URI(res.getScheme(), res.getUserInfo(), res.getHost(), res.getPort(), path, res.getQuery(), res.getFragment());
            }
            return res;
        } catch (URISyntaxException ex) {
            LOG.log(Level.INFO, null, ex);
        } catch (FileNotFoundException ex) {
            LOG.log(Level.INFO, null, ex);
        }
        return null;
    }

    private static FileObject findExistingParent(VCSFileProxy file, List<String> segments) throws FileNotFoundException {
        while (true) {
            FileObject fo = file.toFileObject();
            if (fo != null) {
                return fo;
            }
            segments.add(file.getName());
            file = file.getParentFile();
            if (file == null) {
                throw new FileNotFoundException();
            }
        }
    }
    
    public static boolean isSymlink(VCSFileProxy file) {
        return RemoteVcsSupport.isSymlink(file);
    }
    
    public static String readSymbolicLinkPath(VCSFileProxy file) throws IOException {
        return RemoteVcsSupport.readSymbolicLinkPath(file);
    }
    
    public static boolean canRead(VCSFileProxy file) {
        return RemoteVcsSupport.canRead(file);
    }
    
    public static boolean canRead(VCSFileProxy base, String subdir) {
        return RemoteVcsSupport.canRead(base, subdir);
    }
    
    public static boolean createNew(VCSFileProxy file) throws IOException {
        VCSFileProxy parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            mkdirs(parentFile);
        }
         ExitStatus status = ProcessUtils.executeInDir(parentFile.getPath(), null, false, new ProcessUtils.Canceler(), file,
                 "touch", file.getName()); //NOI18N
        if (!status.isOK()) {
            LOG.log(Level.INFO, "touch {0} failed: {1}", new Object[]{file, status}); //NOI18N
            throw new IOException(status.toString());
        }
        return refreshImpl(file);
    }
    
    public static OutputStream getOutputStream(VCSFileProxy file) throws IOException {
        return RemoteVcsSupport.getOutputStream(file);
    }

    public static long length(VCSFileProxy file) {
        return RemoteVcsSupport.getSize(file);
    }
    
    public static byte[] readFully(VCSFileProxy file, int max) throws IOException {
        InputStream inputStream = file.getInputStream(false);
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            int i;
            while((i = inputStream.read()) != -1) {
                b.write(i);
                if (--max == 0) {
                    break;
                }
            }
            return b.toByteArray();
        } finally {
            inputStream.close();
        }
    }

    public static String getCanonicalPath(VCSFileProxy file) throws IOException {
        return RemoteVcsSupport.getCanonicalPath(file);
    }

    public static VCSFileProxy getCanonicalFile(VCSFileProxy file) throws IOException {
        return RemoteVcsSupport.getCanonicalFile(file);
    }
    
    public static VCSFileProxy generateTemporaryFile(VCSFileProxy file, String name) {
        VCSFileProxy tmp = VCSFileProxy.createFileProxy(file, name);
        while (tmp.exists()) {
            tmp = VCSFileProxy.createFileProxy(file, name + Long.toString(System.currentTimeMillis()));
        }
        return tmp;
    }

    public static VCSFileProxy createTempFile(VCSFileProxy file, String prefix, String suffix, boolean deleteOnExit) throws IOException {
        File javaFile = file.toFile();
        if (javaFile != null) {
            File res = File.createTempFile(prefix, suffix);
            res.deleteOnExit();
            return VCSFileProxy.createFileProxy(res);
        } else {
            // TODO: review it!
            if (suffix == null) {
                suffix = ".tmp"; //NOI18N
            }
            VCSFileProxy res = VCSFileProxy.createFileProxy(file.toFileObject().createData(prefix+Long.toString(System.currentTimeMillis()), suffix));
            if (deleteOnExit) {
                VCSFileProxySupport.deleteOnExit(res);
            }
            return res;
        }
    }
    
    /**
     * Creates a temporary folder. The folder will have deleteOnExit flag set to <code>deleteOnExit</code>.
     * @return
     */
    public static VCSFileProxy getTempFolder(VCSFileProxy file, boolean deleteOnExit) throws FileStateInvalidException, IOException {
        FileObject tmpDir = VCSFileProxySupport.getFileSystem(file).getTempFolder();
        for (;;) {
            try {
                FileObject dir = tmpDir.createFolder("vcs-" + Long.toString(System.currentTimeMillis())); // NOI18N
                VCSFileProxy res = VCSFileProxy.createFileProxy(dir).normalizeFile();
                if (deleteOnExit) {
                    VCSFileProxySupport.deleteOnExit(res);
                }
                return res;
            } catch (IOException ex) {
                continue;
            }
        }
    }
    
    public static boolean renameTo(VCSFileProxy from, VCSFileProxy to){
        File javaFile = from.toFile();
        if (javaFile != null) {
            return javaFile.renameTo(to.toFile());
        } else {
            // TODO: rewrite it with using sftp
            ExitStatus status = ProcessUtils.executeInDir(from.getParentFile().getPath(), null, false, new ProcessUtils.Canceler(),
                    from, "mv", "-f", from.getName(), to.getPath()); //NOI18N
            if (!status.isOK()) {
                LOG.log(Level.INFO, "mv -f {0} {1} failed: {2}", new Object[]{from, to, status});   //NOI18N                        
                return false;
            } else {
                return refreshPairImpl(from.getParentFile(), to.getParentFile());
            }
        }
    }

    public static void copyDirFiles(VCSFileProxy sourceDir, VCSFileProxy targetDir, boolean preserveTimestamp) {
        VCSFileProxy[] files = sourceDir.listFiles();

        if(files==null || files.length == 0) {
            mkdirs(targetDir);
            if(preserveTimestamp) {
                setLastModified(targetDir, sourceDir);
            }
            return;
        }
        if(preserveTimestamp) {
            setLastModified(targetDir, sourceDir);
        }
        for (int i = 0; i < files.length; i++) {
            try {
                VCSFileProxy target = VCSFileProxy.createFileProxy(targetDir, files[i].getName()).normalizeFile();
                if(files[i].isDirectory()) {
                    copyDirFiles(files[i], target, preserveTimestamp);
                } else {
                    copyFile(files[i], target);
                    if(preserveTimestamp) {
                        setLastModified(target, files[i]);
                    }
                }
            } catch (IOException ex) {
                Logger.getLogger(VCSFileProxySupport.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
            }
        }
        refreshImpl(targetDir);
    }

    public static boolean copyFile(VCSFileProxy from, VCSFileProxy to) throws IOException {
        if (from == null || to == null) {
            throw new NullPointerException("from and to files must not be null"); // NOI18N
        }
        InputStream inputStream = null;
        try {
            inputStream = from.getInputStream(false);
            copyStreamToFile(inputStream, to);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
        }
        refreshImpl(to);
        return true;
    }
    
    /**
     * Returns the first found file whose filename is the same (in a case insensitive way) as given <code>file</code>'s.
     * @param file
     * @return the first found file with the same name, but ignoring case, or <code>null</code> if no such file is found.
     */
    public static String getExistingFilenameInParent(VCSFileProxy file) {
        String filename = null;
        if (file == null) {
            return filename;
        }
        VCSFileProxy parent = file.getParentFile();
        if (parent == null) {
            return filename;
        }
        VCSFileProxy[] children = parent.listFiles();
        if (children == null) {
            return filename;
        }
        for (VCSFileProxy child : children) {
            if (file.getName().equalsIgnoreCase(child.getName())) {
                filename = child.getName();
                break;
            }
        }
        return filename;
    }
    
    /**
     * Copies the specified sourceFile to the specified targetFile.
     * It <b>closes</b> the input stream.
     */
    public static void copyStreamToFile(InputStream inputStream, VCSFileProxy targetFile) throws IOException {
        OutputStream outputStream = null;
        try {            
            outputStream = VCSFileProxySupport.getOutputStream(targetFile);
            try {
                byte[] buffer = new byte[32768];
                for (int readBytes = inputStream.read(buffer);
                     readBytes > 0;
                     readBytes = inputStream.read(buffer)) {
                    outputStream.write(buffer, 0, readBytes);
                }
            } catch (IOException ex) {
                VCSFileProxySupport.delete(targetFile);
                throw ex;
            }
        }
        finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                }
                catch (IOException ex) {
                    // ignore
                }
            }
            refreshImpl(targetFile);
        }
    }
    
    public static boolean isRemoteFileSystem(VCSFileProxy file) {
        return file.toFile() == null;
    }
    
    public static VCSFileProxy getResource(VCSFileProxy file, String absPath) {
        if (!absPath.startsWith("/")) { //NOI18N
            assert absPath.startsWith("/") : "Path "+absPath+"must be absolute";
        }
        VCSFileProxy parent;
        while (true) {
            parent = file.getParentFile();
            if (parent == null) {
                parent = file;
                break;
            }
            file = parent;
        }
        return VCSFileProxy.createFileProxy(parent, absPath.substring(1));
    }

    public static VCSFileProxy getResource(FileSystem fileSystem, String absPath) {
        VCSFileProxy root = VCSFileProxy.createFileProxy(fileSystem.getRoot());
        if (absPath.startsWith("/")) { //NOI18N
            return VCSFileProxy.createFileProxy(root, absPath.substring(1));
        } else {
            // Abs Path should start with "/". Assertion?
            return VCSFileProxy.createFileProxy(root, absPath);
        }
    }
    
    public static VCSFileProxy getHome(VCSFileProxy file){
        return RemoteVcsSupport.getHome(file);
    }

    /**
     * Creates or gets the user-localhost folder to persist svn configurations.
     * @return
     */
    public static VCSFileProxy getRemotePeristenceFolder(FileSystem fileSystem) throws FileStateInvalidException, IOException {
        FileObject tmpDir = fileSystem.getTempFolder();
        String userName = "user";  //NOI18N //RemoteExecutionEnvironment.getUser();
        String folderName = "svn_" + userName; // NOI18N
        FileObject res = tmpDir.getFileObject(folderName);
        if (res == null || !res.isValid()) {
            res = tmpDir.createFolder(folderName);
        }
        // TODO: create subfolder with hash of localhost
        return VCSFileProxy.createFileProxy(res);
    }
    
    public static boolean isMac(VCSFileProxy file) {
        return RemoteVcsSupport.isMac(file);
    }

    public static boolean isSolaris(VCSFileProxy file) {
        return RemoteVcsSupport.isSolaris(file);
    }

    public static boolean isUnix(VCSFileProxy file){
        return RemoteVcsSupport.isUnix(file);
    }
    
    public static String getFileSystemKey(FileSystem file) {
        String fileSystemKey = RemoteVcsSupport.getFileSystemKey(file);
        fileSystemKey = fileSystemKey.replace(':', '_');
        fileSystemKey = fileSystemKey.replace('@', '_');
        return fileSystemKey;
    }
    
    public static boolean isConnectedFileSystem(FileSystem file) {
        return RemoteVcsSupport.isConnectedFileSystem(file);
    }

    public static void connectFileSystem(FileSystem file) {
        RemoteVcsSupport.connectFileSystem(file);
    }
    
    public static String toString(VCSFileProxy file) {
        return RemoteVcsSupport.toString(file);
    }
    
    public static VCSFileProxy fromString(String file) {
        return RemoteVcsSupport.fromString(file);
    }

    /**
     * 
     * @param proxy defines FS and initial selection
     * @return 
     */
    public static JFileChooser createFileChooser(VCSFileProxy proxy) {
        return RemoteVcsSupport.createFileChooser(proxy);
    }

    public static VCSFileProxy getSelectedFile(JFileChooser chooser) {
        return RemoteVcsSupport.getSelectedFile(chooser);
    }
    
    public static FileSystem getDefaultFileSystem() {
        return RemoteVcsSupport.getDefaultFileSystem();
    }

    public static FileSystem[] getFileSystems() {
        return RemoteVcsSupport.getFileSystems();
    }

    public static FileSystem[] getConnectedFileSystems() {
        return RemoteVcsSupport.getConnectedFileSystems();
    }

    public static FileSystem getFileSystem(VCSFileProxy file) {
        return RemoteVcsSupport.getFileSystem(file);
    }
    
    public static FileSystem readFileSystem(DataInputStream is) throws IOException {
        return RemoteVcsSupport.readFileSystem(is);
    }

    public static void writeFileSystem(DataOutputStream os, FileSystem fs) throws IOException {
        RemoteVcsSupport.writeFileSystem(os, fs);
    }

    private static boolean refreshPairImpl(VCSFileProxy fromParent, VCSFileProxy toParent) {
        if (fromParent != null && toParent != null) { // paranoidal check
            if (toParent.equals(fromParent)) {
                return refreshImpl(fromParent);
            } else {
                return refreshImpl(fromParent, toParent);
            }
        }
        return true;
    }
    
    private static boolean refreshImpl(VCSFileProxy... files) {
        try {
            RemoteVcsSupport.refreshFor(files);
        } catch (IOException ex) {
            if (LOG.isLoggable(Level.FINE)) {
                StringBuilder sb = new StringBuilder("Error refreshing "); //NOI18N
                for (VCSFileProxy f : files) {
                    if (sb.length() > 0) {
                        sb.append(", "); // NOI18N
                    }
                    sb.append(f);
                }
                LOG.log(Level.FINE, sb.toString(), ex);
            }
            return false;
        }
        return true;
    }

//<editor-fold defaultstate="collapsed" desc="methods from org.netbeans.modules.versioning.util.Utils">
    public static boolean isAncestorOrEqual(VCSFileProxy ancestor, VCSFileProxy file) {
        String ancestorPath = ancestor.getPath();
        String filePath = file.getPath();
        if (VCSFileProxySupport.isMac(ancestor)) {
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
            if (file.equals(ancestor)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Opens a file in the editor area.
     *
     * @param file a File to open
     */
    public static void openFile(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                OpenCookie oc = dao.getLookup().lookup(OpenCookie.class);
                if (oc != null) {
                    oc.open();
                }
            } catch (DataObjectNotFoundException e) {
                // nonexistent DO, do nothing
            }
        }
    }
    
    /**
     * Splits files/folders into 2 groups: flat folders and other files
     *
     * @param files array of files to split
     * @return File[][] the first array File[0] contains flat folders (
     * @see #flatten for their direct descendants), File[1] contains all other
     * files
     */
    public static VCSFileProxy[][] splitFlatOthers(VCSFileProxy[] files) {
        Set<VCSFileProxy> flat = new HashSet<>(1);
        for (int i = 0; i < files.length; i++) {
            if (VersioningSupport.isFlat(files[i])) {
                flat.add(files[i]);
            }
        }
        if (flat.isEmpty()) {
            return new VCSFileProxy[][]{new VCSFileProxy[0], files};
        } else {
            Set<VCSFileProxy> allFiles = new HashSet<>(Arrays.asList(files));
            allFiles.removeAll(flat);
            return new VCSFileProxy[][]{
                        flat.toArray(new VCSFileProxy[flat.size()]),
                        allFiles.toArray(new VCSFileProxy[allFiles.size()])
                    };
        }
    }
    
    /**
     * Flattens the given collection of files and removes those that do not respect the flat folder logic,
     * i.e. those that lie deeper under a flat folder.
     * @param roots selected files with flat folders
     * @param files
     * @return 
     */
    public static Set<VCSFileProxy> flattenFiles (VCSFileProxy[] roots, Collection<VCSFileProxy> files) {
        VCSFileProxy[][] split = splitFlatOthers(roots);
        Set<VCSFileProxy> filteredFiles = new HashSet<>(files);
        if (split[0].length > 0) {
            outer:
            for (Iterator<VCSFileProxy> it = filteredFiles.iterator(); it.hasNext(); ) {
                VCSFileProxy f = it.next();
                // file is directly under a flat folder
                for (VCSFileProxy flat : split[0]) {
                    if (f.getParentFile().equals(flat)) {
                        continue outer;
                    }
                }
                // file lies under a recursive folder
                for (VCSFileProxy folder : split[1]) {
                    if (isAncestorOrEqual(folder, f)) {
                        continue outer;
                    }
                }
                it.remove();
            }
        }
        return filteredFiles;
    }
    
    /**
     * Checks if the context was originally created from files, not from nodes
     * and if so then it tries to determine if those original files are part of
     * a single DataObject. Call only if the context was created from files (not
     * from nodes), otherwise always returns false.
     *
     * @param ctx context to be checked
     * @return true if the context was created from files of the same DataObject
     */
    public static boolean isFromMultiFileDataObject(VCSContext ctx) {
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
    
    /**
     * Tests whether all files belong to the same data object.
     *
     * @param files array of Files
     * @return true if all files share common DataObject (even null), false
     * otherwise
     */
    public static boolean shareCommonDataObject(VCSFileProxy[] files) {
        if (files == null || files.length < 2) {
            return true;
        }
        DataObject common = findDataObject(files[0]);
        for (int i = 1; i < files.length; i++) {
            DataObject dao = findDataObject(files[i]);
            if (dao != common && (dao == null || !dao.equals(common))) {
                return false;
            }
        }
        return true;
    }

    private static DataObject findDataObject(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
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
     * @param file
     * @return Set<File> all files that belong to the same DataObject as the
     * argument
     */
    public static Set<VCSFileProxy> getAllDataObjectFiles(VCSFileProxy file) {
        Set<VCSFileProxy> filesToCheckout = new HashSet<>(2);
        filesToCheckout.add(file);
        FileObject fo = file.toFileObject();
        if (fo != null) {
            try {
                DataObject dao = DataObject.find(fo);
                Set<FileObject> fileObjects = dao.files();
                for (FileObject fileObject : fileObjects) {
                    filesToCheckout.add(VCSFileProxy.createFileProxy(fileObject));
                }
            } catch (DataObjectNotFoundException e) {
                // no dataobject, never mind
            }
        }
        return filesToCheckout;
    }
    
    /**
     * Searches for common filesystem parent folder for given files.
     *
     * @param a first file
     * @param b second file
     * @return File common parent for both input files with the longest
     * filesystem path or null of these files have not a common parent
     */
    public static VCSFileProxy getCommonParent(VCSFileProxy a, VCSFileProxy b) {
        for (;;) {
            if (a.equals(b)) {
                return a;
            } else if (a.getPath().length() > b.getPath().length()) {
                a = a.getParentFile();
                if (a == null) {
                    return null;
                }
            } else {
                b = b.getParentFile();
                if (b == null) {
                    return null;
                }
            }
        }
    }
    
    /**
     * Checks if the file is to be considered as textuall.
     *
     * @param file file to check
     * @return true if the file can be edited in NetBeans text editor, false otherwise
     */
    public static boolean isFileContentText(VCSFileProxy file) {
        FileObject fo = file.toFileObject();
        if (fo == null) {
            return false;
        }
        if (fo.getMIMEType().startsWith("text")) { // NOI18N
            return true;
        }
        try {
            DataObject dao = DataObject.find(fo);
            return dao.getLookup().lookupItem(new Lookup.Template<>(EditorCookie.class)) != null;
        } catch (DataObjectNotFoundException e) {
            // not found, continue
        }
        return false;
    }
    
    private static Map<VCSFileProxy, Charset> fileToCharset;
    private static final Logger LOG = Logger.getLogger("remote.vcs.logger"); //NOI18N
    private static final Object ENCODING_LOCK = new Object();

    /**
     * Retrieves the Charset for the referenceFile and associates it weakly with
     * the given file. A following getAssociatedEncoding() call for the file
     * will then return the referenceFile-s Charset.
     *
     * @param referenceFile the file which charset has to be used when encoding
     * file
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding(VCSFileProxy referenceFile, VCSFileProxy file) {
        FileObject fo = referenceFile.toFileObject();
        if (fo == null || fo.isFolder()) {
            return;
        }
        Charset c = RemoteVcsSupport.getEncoding(referenceFile);
        if (c == null) {
            return;
        }
        synchronized(ENCODING_LOCK) {
            if (fileToCharset == null) {
                fileToCharset = new WeakHashMap<>();
            }
            fileToCharset.put(file, c);
        }
    }

    /**
     * Returns a charset for the given file if it was previously registered via
     * associateEncoding()
     *
     * @param fo file for which the encoding has to be retrieved
     * @return the charset the given file has to be encoded with
     */
    public static Charset getAssociatedEncoding(FileObject fo) {
        try {
            if (fo == null) {
                return null;
            }
            synchronized(ENCODING_LOCK) {
                if (fileToCharset == null || fileToCharset.isEmpty()) {
                    return null;
                }
            }
            if (fo.isFolder()) {
                return null;
            }
            VCSFileProxy file = VCSFileProxy.createFileProxy(fo);
            if (file == null) {
                return null;
            }
            synchronized(ENCODING_LOCK) {
                return fileToCharset.get(file);
            }
        } catch (Throwable t) {
            LOG.log(Level.INFO, null, t);

            return null;
        }
    }

    /**
     * Associates a given charset weakly with
     * the given file. A following getAssociatedEncoding() call for
     * the file will then return the referenceFile-s Charset.
     *
     * @param file file to be encoded with the referenceFile-s charset
     *
     */
    public static void associateEncoding (VCSFileProxy file, Charset charset) {
        FileObject fo = file.toFileObject();
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
        VCSFileProxy fo = VCSFileProxy.createFileProxy(file);
        if (fo == null) {
            return;
        }
        synchronized(ENCODING_LOCK) {
            if(fileToCharset == null) {
                fileToCharset = new WeakHashMap<VCSFileProxy, Charset>();
            }
            fileToCharset.put(fo, charset);
        }
    }
    
    public static String getContextDisplayName(VCSContext ctx) {
        // TODO: reuse this code in getActionName()
        Set<VCSFileProxy> nodes = ctx.getFiles();
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
        if (projectsOnly) {
            objectCount = activatedNodes.length;
        }

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
                    return MessageFormat.format(NbBundle.getMessage(VCSFileProxySupport.class, "MSG_ActionContext_MultipleProjects"), objectCount);  // NOI18N
                } catch (MissingResourceException ex) {
                    // ignore use files alternative bellow
                }
            }
            return MessageFormat.format(NbBundle.getMessage(VCSFileProxySupport.class, "MSG_ActionContext_MultipleFiles"), objectCount);  // NOI18N
        }
    }
    
    public static Project getProject (VCSFileProxy[] files) {
        for (VCSFileProxy file : files) {
            /* We may be committing a LocallyDeleted file */
            if (!file.exists()) {
                file = file.getParentFile();
            }
            FileObject fo = file.toFileObject();
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
    public static VCSFileProxy getProjectFile(Project project){
        if (project == null) {
            return null;
        }

        FileObject fo = project.getProjectDirectory();
        return  VCSFileProxy.createFileProxy(fo);
    }
    
    /**
     * Returns files from all opened top components
     * @return set of opened files
     */
    public static Set<VCSFileProxy> getOpenFiles() {
        TopComponent[] comps = TopComponent.getRegistry().getOpened().toArray(new TopComponent[0]);
        Set<VCSFileProxy> openFiles = new HashSet<VCSFileProxy>(comps.length);
        for (TopComponent tc : comps) {
            Node[] nodes = tc.getActivatedNodes();
            if (nodes == null) {
                continue;
            }
            for (Node node : nodes) {
                VCSFileProxy file = node.getLookup().lookup(VCSFileProxy.class);
                if (file == null) {
                    FileObject fo = node.getLookup().lookup(FileObject.class);
                    if (fo != null && fo.isData()) {
                        file = VCSFileProxy.createFileProxy(fo);
                    }
                }
                if (file != null) {
                    openFiles.add(file);
                }
            }
        }
        return openFiles;
    }

//</editor-fold>
    
//<editor-fold defaultstate="collapsed" desc="methods from org.netbeans.modules.versioning.util.FileUtils">
    
    /**
     * Reads the data from the <code>file</code> and returns it as an array of bytes.
     * @param file file to be read
     * @return file contents as a byte array
     * @throws java.io.IOException
     */
    public static byte[] getFileContentsAsByteArray(VCSFileProxy file) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024 * 5);
        BufferedInputStream bis = null;
        try {
            bis = new BufferedInputStream(file.getInputStream(false));
            byte[] buffer = new byte[1024];
            for (int byteRead = bis.read(buffer); byteRead > 0; byteRead = bis.read(buffer)) {
                baos.write(buffer, 0, byteRead);
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
        }
        return baos.toByteArray();
    }
    
//</editor-fold>

}
