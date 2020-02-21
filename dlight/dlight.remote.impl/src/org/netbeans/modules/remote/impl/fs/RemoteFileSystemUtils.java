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

package org.netbeans.modules.remote.impl.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.FileInfoProvider.SftpIOException;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.impl.RemoteLogger;
import static org.netbeans.modules.remote.impl.fs.RemoteFileObjectBase.composeName;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 */
public class RemoteFileSystemUtils {
    
    private static final int MAXSYMLINKS = Integer.getInteger("remote.max.sym.links", 20); // NOI18N
            
    private static final boolean TRUE_CASE_SENSITIVE_SYSTEM;

    private static boolean isWindows = Utilities.isWindows();

    private static final char[][] windowsReservedChars = new char[][] {
        { '<',  'L' },
        { '>',  'M' },
        { ':',  'C' },
        { '"',  'D' },
        { '/',  'F' },
        { '\\', 'B' },
        { '|',  'P' },
        { '?',  'Q' },
        { '*',  'A' }
    };

    private static final char windowsReservedNameChar = 'R'; // for CON, AUX, etc

    static {
        boolean caseSenstive;
        try {
            File tmpFile = File.createTempFile("CaseSensitiveFile", ".check"); // NOI18N
            String absPath = tmpFile.getAbsolutePath();
            absPath = absPath.toUpperCase();
            caseSenstive = !new File(absPath).exists();
            tmpFile.delete();
        } catch (IOException ex) {
            caseSenstive = Utilities.isUnix() && !Utilities.isMac();
        }
        TRUE_CASE_SENSITIVE_SYSTEM = caseSenstive;
    }

    public static boolean isSystemCaseSensitive() {
        return TRUE_CASE_SENSITIVE_SYSTEM;
    }

    private RemoteFileSystemUtils() {
    }
    
    public static ExecutionEnvironment getExecutionEnvironment(String hostName, int port) {
        ExecutionEnvironment result = null;
        for(ExecutionEnvironment env : ConnectionManager.getInstance().getRecentConnections()) {
            if (hostName.equals(env.getHost())) {
                if (port == 0 || port == env.getSSHPort()) {
                    result = env;
                    if (ConnectionManager.getInstance().isConnectedTo(env)) {
                        break;
                    }
                }
            }
        }
        return result;
    }

    public static String escapeFileName(String name) {
//        if (RemoteFileSystem.CACHE_FILE_NAME.equals(name)) {
//            name = name + '_';
//        } else if (RemoteFileSystem.ATTRIBUTES_FILE_NAME.equals(name)) {
//            name = name + '_';
//        }
        if (name.startsWith(RemoteFileSystem.RESERVED_PREFIX)) {
            name = RemoteFileSystem.RESERVED_PREFIX_ESCAPED + name.substring(RemoteFileSystem.RESERVED_PREFIX.length());
        }
        if (!isWindows) {
            return name;
        }
        StringBuilder sb = new StringBuilder();

        // Escape reserved names -
        // CON, PRN, AUX, NUL, COM1-COM9, LPT1-LPT9 with or without extensions

        if (name.startsWith("CON") || name.startsWith("PRN") || name.startsWith("AUX") || name.startsWith("NUL")) { // NOI18N
            if (name.length() == 3 || name.charAt(3) == '.') {
                sb.append('_').append(windowsReservedNameChar).append(name);
                return sb.toString();
            }
        }
        if (name.startsWith("COM") || name.startsWith("LPT")) { // NOI18N
            if (name.length() > 3) {
                char c = name.charAt(3);
                if ('1' <= c && c <= '9') {
                    if (name.length() == 4 || name.charAt(4) == '.') {
                        sb.append('_').append(windowsReservedNameChar).append(name);
                        return sb.toString();
                    }
                }

            }
        }

        // First, check whether we need to escape
        if (!containsReservedCharacters(name) && name.indexOf('_') < 0) {
            return name;
        }

        // Escape reserved characters
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                sb.append("__"); //NOI18N
            } else {
                boolean added = false;
                for (int j = 0; j < windowsReservedChars.length; j++) {
                    if (c == windowsReservedChars[j][0]) {
                        sb.append('_').append(windowsReservedChars[j][1]);
                        added = true;
                    }
                }
                if (!added) {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private char unescapeChar(char c) {
        for (int j = 0; j < windowsReservedChars.length; j++) {
            if (c == windowsReservedChars[j][1]) {
                return windowsReservedChars[j][0];
            }
        }
        return 0;
    }

    public static String unescapeFileName(String name) {
        if (!isWindows) {
            return name;
        }
        if (name.length() < 2 || name.indexOf('_') < 0) {
            return name;
        }
        if (name.charAt(0) == '_' && name.charAt(1) == windowsReservedNameChar) {
            return name.substring(2);
        }
        StringBuilder sb = new StringBuilder();
        boolean escape = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (escape) {
                escape = false;
                if (c == '_') {
                    sb.append('_');
                } else {
                    boolean added = false;
                    for (int j = 0; j < windowsReservedChars.length; j++) {
                        if (c == windowsReservedChars[j][1]) {
                            sb.append(windowsReservedChars[j][0]);
                            added = true;
                            break;
                        }
                    }
                    if (!added) {
                        RemoteLogger.getInstance().log(Level.SEVERE, "Incorrect name to unescape: ''{0}''", name);
                    }
                }
            } else {
                if (c == '_') {
                    escape = true;
                    if ((i+1) == name.length()) { // shouldn't be last one
                        RemoteLogger.getInstance().log(Level.SEVERE, "Incorrect name to unescape: ''{0}''", name);
                    }
                } else {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    private static boolean containsReservedCharacters(String name) {
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            for (int j = 0; j < windowsReservedChars.length; j++) {
                if (c == windowsReservedChars[j][0]) {
                    return true;
                }
            }
        }
        return false;
    }


    /*pakage*/ static void testSetWindows(boolean isWin) {
        isWindows = isWin;
    }
    
    public static String normalize(String absPath) {
        return absPath; // TODO: implement! XXX:rfs XXX:fullRemote 
    }
    
    public static FileObject getCanonicalFileObject(FileObject fo) throws IOException {
        if (fo instanceof RemoteFileObject) {
            return getCanonicalFileObject(((RemoteFileObject) fo).getImplementor()).getOwnerFileObject();
        }
        return fo;
    }

    public static RemoteFileObjectBase getCanonicalFileObject(RemoteFileObjectBase fileObject) throws IOException {
        RemoteFileObjectBase candidate = fileObject;
        for(int i = 0; i < MAXSYMLINKS; i++) {
            if (candidate instanceof RemoteLinkBase) {
                RemoteFileObjectBase delegate = ((RemoteLinkBase) candidate).getDelegateImpl();
                if (delegate == null) {
                    throw new FileNotFoundException("Null delegate for remote link " + candidate); //NOI18N // new IOException sic!
                }
                if (delegate instanceof RemoteLinkBase) {
                    candidate = delegate;
                    continue;
                }
                return delegate;
            } else {
                return candidate;
            }
        }
        throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemoteFileSystemUtils.class, 
                "EXC_DeepSymLinks", fileObject.getDisplayName(), MAXSYMLINKS)); //NOI18N
    }

    public static RemoteDirectory getCanonicalParent(RemoteFileObjectBase fo) throws IOException {
        RemoteFileObjectBase parent = fo.getParent();
        if (parent == null) {
            return null;
        } else if (parent instanceof RemoteDirectory) {
            return (RemoteDirectory) parent;
        } else {
            RemoteLogger.assertTrueInConsole(parent instanceof RemoteLinkBase, 
                    "Unexpected parent class, should be RemoteLinkBase: " + parent.getClass().getName()); //NOI18N
            RemoteFileObjectBase canonical = getCanonicalFileObject(parent);
            if (canonical instanceof RemoteDirectory) {
                return (RemoteDirectory) canonical;
            } else {
                return null;
            }
        }
    }

    private static class DummyInputStream extends InputStream {
        @Override
        public int read() throws IOException {
            return -1;
        }        
    }

    public static InputStream createDummyInputStream() {
        return new DummyInputStream();
    }    
    
    public static boolean isFileNotFoundException(Throwable ex) {
        while (ex != null) {
            if (ex instanceof FileNotFoundException) {
                return true;
            }
            if (ex instanceof SftpIOException) {
                switch (((SftpIOException) ex).getId()) {
                    case SftpIOException.SSH_FX_NO_SUCH_FILE:
                    case SftpIOException.SSH_FX_PERMISSION_DENIED:
                        return true;
                }
            }
            ex = ex.getCause();
        }
        return false;
    }
    
    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if (text != null) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    }
    
    public static String getConnectExceptionMessage(ExecutionEnvironment env) {
        return NbBundle.getMessage(RemoteFileSystemUtils.class, "NotConnectedExceptionMessage", env.getDisplayName());
    }

    public static boolean isUnitTestMode() {
        return Boolean.getBoolean("cnd.mode.unittest") | Boolean.getBoolean("nativeexecution.mode.unittest"); // NOI18N
    }

    @org.netbeans.api.annotations.common.SuppressWarnings("NP") // Three state
    public static Boolean isLinux(ExecutionEnvironment env) {
        if (HostInfoUtils.isHostInfoAvailable(env)) {
            try {
                HostInfo hi = HostInfoUtils.getHostInfo(env);
                return hi.getOSFamily() == HostInfo.OSFamily.LINUX;
            } catch (IOException | ConnectionManager.CancellationException ex) {
                Exceptions.printStackTrace(ex); // should never be the case if isHostInfoAvailable retured true
            }
            // should never be the case if isHostInfoAvailable retured true
            
        }
        return null;
    }
    
    public static void deleteRecursively(File file) {
        if (file != null) {
            file.delete();
            if (file.isDirectory()) {
                for (File child : file.listFiles()) {
                    deleteRecursively(child);
                }
            }
        }
    }

    public static void reportUnexpectedTimeout(TimeoutException ex, RemoteFileObjectBase fo) {
        RemoteLogger.getInstance().log(Level.FINE, "Unexpected TimeoutException with zero timeout " + fo, ex);
    }

    public static void reportUnexpectedTimeout(TimeoutException ex, String path) {
        RemoteLogger.getInstance().log(Level.FINE, "Unexpected TimeoutException with zero timeout " + path, ex);
    }
    
    /**
     * Unpacks a ZIP stream to disk. All entries are unpacked. 
     * Parent directories are created as needed (even if not mentioned in the ZIP); 
     * empty ZIP directories are created too. Existing files are overwritten.
     * @param zip a ZIP stream. It will NOT be closed.
     * @param dir the base directory in which to unpack (need not yet exist)
     * @throws IOException in case of problems
     */
    public static void unpackZipFile(InputStream zipStream, File dir) throws IOException {
        byte[] buf = new byte[8192];
        ZipInputStream zis = new ZipInputStream(zipStream);
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            int slash = name.lastIndexOf('/');                
            if (slash >= 0) {
                File baseDir = new File(dir, name.substring(0, slash).replace('/', File.separatorChar));
                if (!baseDir.isDirectory() && !baseDir.mkdirs()) {
                    throw new IOException("could not make " + baseDir); // NOI18N
                }
            }                
            if (slash != name.length() - 1) {
                File f = new File(dir, name.replace('/', File.separatorChar));
                OutputStream os = new FileOutputStream(f);
                try {
                    int read;
                    while ((read = zis.read(buf)) != -1) {
                        os.write(buf, 0, read);
                    }
                } finally {
                    os.close();
                }
            }
        }
    }    

    // <editor-fold desc="Copy-pastes from FileObject and/or FileUtil" defaultstate="collapsed">

   /** Copy-paste from FileObject.copy */
    public static FileObject copy(FileObject source, FileObject target, String name, String ext) throws IOException {

        if (source.isFolder()) {
            if (FileUtil.isParentOf(source, target)) {
                throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileSystemUtils.class, 
                        "EXC_OperateChild", source.getPath(), target.getPath())); // NOI18N
            }
        }

        final String from = source.getPath();
        final String newNameExt = composeName(name, ext);
        final String newPath = target.getPath() + '/' + newNameExt;
        if (target instanceof RemoteFileObject && source instanceof RemoteFileObject) {
            ExecutionEnvironment env = ((RemoteFileObject) source).getExecutionEnvironment();
            if (env.equals(((RemoteFileObject) target).getExecutionEnvironment())
                    && RemoteFileSystemTransport.canCopy(env, from, newPath)) {
                try {
                    List<IOException> subdirectoryExceptions = new ArrayList<>();
                    DirEntryList entries = RemoteFileSystemTransport.copy(env, from, newPath, subdirectoryExceptions);
                    ((RemoteFileObject) target).getImplementor().postDeleteOrCreateChild(null, entries);
                    FileObject fo = target.getFileObject(newNameExt);
                    if (fo == null) {
                        throw RemoteExceptions.createIOException(NbBundle.getMessage(RemoteFileSystemUtils.class, 
                                "EXC_NullFoAfterCopy", RemoteFileObjectBase.getDisplayName(env, newPath))); //NOI18N
                    } else {
                        FileUtil.copyAttributes(source, fo);
                        if (!subdirectoryExceptions.isEmpty()) {
                            throw subdirectoryExceptions.get(0);
                        }
                        return fo;
                    }
                } catch (InterruptedException ex) {
                    throw RemoteExceptions.createInterruptedIOException(ex.getLocalizedMessage(), ex);
                } catch (ExecutionException | TimeoutException ex) {
                    if (RemoteFileSystemUtils.isFileNotFoundException(ex)) {
                        throw RemoteExceptions.createFileNotFoundException(NbBundle.getMessage(RemoteFileSystemUtils.class, 
                                "EXC_CantCopyFromTo", from, newPath, ex.getLocalizedMessage()), ex); //NOI18N
                    } else {
                        throw RemoteExceptions.createIOException(ex.getLocalizedMessage(), ex);
                    }
                }
            }
        }

        if (source.isFolder()) {
            FileObject peer = target.createFolder(name);
            FileUtil.copyAttributes(source, peer);
            for (FileObject fo : source.getChildren()) {
                fo.copy(peer, fo.getName(), fo.getExt());
            }
            return peer;
        } else {        
            FileObject dest = RemoteFileSystemUtils.copyFileImpl(source, target, name, ext);
            return dest;
        }
    }

    /** Copies file to the selected folder.
     * This implementation simply copies the file by stream content.
    * @param source source file object
    * @param destFolder destination folder
    * @param newName file name (without extension) of destination file
    * @param newExt extension of destination file
    * @return the created file object in the destination folder
    * @exception IOException if <code>destFolder</code> is not a folder or does not exist; the destination file already exists; or
    *      another critical error occurs during copying
    */
    static FileObject copyFileImpl(FileObject source, FileObject destFolder, String newName, String newExt)
    throws IOException {
        FileObject dest = destFolder.createData(newName, newExt);

        FileLock lock = null;
        InputStream bufIn = null;
        OutputStream bufOut = null;

        try {
            lock = dest.lock();
            bufIn = source.getInputStream();
            bufOut = dest.getOutputStream(lock);

            FileUtil.copy(bufIn, bufOut);
            FileUtil.copyAttributes(source, dest);
        } finally {
            if (bufIn != null) {
                bufIn.close();
            }

            if (bufOut != null) {
                bufOut.close();
            }

            if (lock != null) {
                lock.releaseLock();
            }
        }

        return dest;
    }
    
    // </editor-fold>
}
