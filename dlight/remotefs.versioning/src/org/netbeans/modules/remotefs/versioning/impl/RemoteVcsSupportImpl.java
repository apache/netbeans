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
package org.netbeans.modules.remotefs.versioning.impl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JFileChooser;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironmentFactory;
import org.netbeans.modules.nativeexecution.api.HostInfo;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.HostInfoUtils;
import org.netbeans.modules.remote.api.ui.FileChooserBuilder;
import org.netbeans.modules.remote.impl.fileoperations.spi.RemoteVcsSupportUtil;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.modules.remotefs.versioning.spi.RemoteVcsSupportImplementation;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.util.Utilities;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(service = RemoteVcsSupportImplementation.class)
public class RemoteVcsSupportImpl implements RemoteVcsSupportImplementation {

    public RemoteVcsSupportImpl() {
    }

    
    @Override
    public JFileChooser createFileChooser(VCSFileProxy proxy) {
        FileSystem fs = getFileSystem(proxy);
        FileChooserBuilder fcb = new FileChooserBuilder(FileSystemProvider.getExecutionEnvironment(fs));
        FileChooserBuilder.JFileChooserEx chooser = fcb.createFileChooser(proxy.getPath());
        return chooser;
    }

    @Override
    public VCSFileProxy getSelectedFile(JFileChooser chooser) {
        if (chooser instanceof FileChooserBuilder.JFileChooserEx) {
            final FileChooserBuilder.JFileChooserEx chooserEx = (FileChooserBuilder.JFileChooserEx) chooser;
            FileObject fo = chooserEx.getSelectedFileObject();
            if (fo != null) {
                return VCSFileProxy.createFileProxy(fo);
            } else {
                File file = chooser.getSelectedFile();
                if (file != null) {
                    String path = file.getPath();
                    ExecutionEnvironment env = chooserEx.getExecutionEnvironment();
                    FileSystem fileSystem = FileSystemProvider.getFileSystem(env);
                    return VCSFileProxy.createFileProxy(VCSFileProxy.createFileProxy(fileSystem.getRoot()), path);
                }
            }
        } else {
            File file = chooser.getSelectedFile();
            if (file != null) {
                return VCSFileProxy.createFileProxy(file);
            }
        }
        return null;
    }

    @Override
    public FileSystem getFileSystem(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return FileSystemProvider.getFileSystem(ExecutionEnvironmentFactory.getLocal());
        } else {
            VCSFileProxy root = getRootFileProxy(proxy);
            try {
                // TODO: make it more effective
                return root.toFileObject().getFileSystem();
            } catch (FileStateInvalidException ex) {
                throw new IllegalStateException(ex);
            }
        }
    }

    private VCSFileProxy getRootFileProxy(VCSFileProxy proxy) {
        VCSFileProxy root = proxy;
        while (root.getParentFile() != null) {
            root = root.getParentFile();
        }
        return root;
    }

    @Override
    public FileSystem[] getFileSystems() {
        // TODO: get list from cnd.remote !!!
        List<ExecutionEnvironment> execEnvs = ConnectionManager.getInstance().getRecentConnections();
        List<FileSystem> fileSystems = new ArrayList<>(execEnvs.size());
        for (ExecutionEnvironment env : execEnvs) {
            if (env.isRemote()) {
                fileSystems.add(FileSystemProvider.getFileSystem(env));
            }
        }
        return fileSystems.toArray(new FileSystem[fileSystems.size()]);
    }

    @Override
    public FileSystem[] getConnectedFileSystems() {
        return RemoteVcsSupportUtil.getConnectedFileSystems();
    }

    /**
     * If default host is remote and is connected, then returns its file system, otherwise null
     * @return 
     */
    @Override
    public FileSystem getDefaultFileSystem() {
        return RemoteVcsSupportUtil.getDefaultFileSystem();
    }

    @Override
    public boolean isSymlink(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            Path path = file.toPath();
            return Files.isSymbolicLink(path);
        } else {
            return RemoteVcsSupportUtil.isSymbolicLink(getFileSystem(proxy), proxy.getPath());
        }
    }

    @Override
    public String readSymbolicLinkPath(VCSFileProxy proxy) throws IOException {
        File file = proxy.toFile();
        if (file != null) {
            Path path = file.toPath();
            return Files.readSymbolicLink(path).toString();
        } else {
            return RemoteVcsSupportUtil.readSymbolicLinkPath(getFileSystem(proxy), proxy.getPath());
        }
    }

    @Override
    public boolean canRead(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return file.canRead();
        } else {
            return RemoteVcsSupportUtil.canRead(getFileSystem(proxy), proxy.getPath());
        }
    }

    @Override
    public boolean canRead(VCSFileProxy base, String subdir) {
        File baseFile = base.toFile();
        if (baseFile != null) {
            if (baseFile.isFile()) {
                return false;
            }
            return new File(baseFile, subdir).canRead();
        } else {
            if (base.isFile()) {
                return false;
            }
            String path = base.getPath().trim();
            path += ((path.endsWith("/") || subdir.startsWith("/")) ? "" : "/") + subdir; // NOI18N
            return RemoteVcsSupportUtil.canRead(getFileSystem(base), path);
        }
    }    

    @Override
    public String getCanonicalPath(VCSFileProxy proxy) throws IOException {
        File file = proxy.toFile();
        if (file != null) {
            File canonicalFile = file.getCanonicalFile();
            return canonicalFile.getAbsolutePath();
        } else {
            String canonical = RemoteVcsSupportUtil.getCanonicalPath(getFileSystem(proxy), proxy.getPath());
            return (canonical == null) ? proxy.getPath() : canonical;
        }    
    }

    @Override
    public VCSFileProxy getCanonicalFile(VCSFileProxy proxy) throws IOException {
        File file = proxy.toFile();
        if (file != null) {
            File canonicalFile = file.getCanonicalFile();
            return VCSFileProxy.createFileProxy(canonicalFile);
        } else {
            String canonical = RemoteVcsSupportUtil.getCanonicalPath(getFileSystem(proxy), proxy.getPath());
            if (canonical == null) {
                return proxy;
            } else {
                VCSFileProxy root = getRootFileProxy(proxy);
                return VCSFileProxy.createFileProxy(root, canonical);
            }
        }
    }

    private void reportHostInfoNotAvailable(ExecutionEnvironment env) {
        // TODO: is this correct error processing?
        IllegalStateException ex = new IllegalStateException("Host info is not available for " + env); //NOI18N
        Logger.getLogger(RemoteVcsSupportImpl.class.getName()).log(Level.SEVERE, null, ex);
    }
    
    private VCSFileProxy getFakeHome(FileSystem fs) {
        // TODO: is this correct error processing?
        VCSFileProxy root = VCSFileProxy.createFileProxy(fs.getRoot());
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
        return VCSFileProxy.createFileProxy(root, "/home/" + env.getUser()); //NOI18N
    }
    
    @Override
    public VCSFileProxy getHome(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return VCSFileProxy.createFileProxy(new File(System.getProperty("user.home")));
        } else {
            FileSystem fs = getFileSystem(proxy);
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    String userDir = HostInfoUtils.getHostInfo(env).getUserDir();
                    VCSFileProxy root = getRootFileProxy(proxy);
                    return VCSFileProxy.createFileProxy(root, userDir);
                } catch (IOException ex) {
                    Logger.getLogger(RemoteVcsSupportImpl.class.getName()).log(Level.SEVERE, null, ex);
                    return getFakeHome(fs);
                } catch (ConnectionManager.CancellationException ex) {
                    // do not report CancellationException
                    return getFakeHome(fs);
                }
            } else {
                reportHostInfoNotAvailable(env);
                return getFakeHome(fs);
            }
        }     
    }

    @Override
    public boolean isMac(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return Utilities.isMac();
        } else {
            FileSystem fs = getFileSystem(proxy);
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    return HostInfoUtils.getHostInfo(env).getOSFamily() == HostInfo.OSFamily.MACOSX;
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    Logger.getLogger(RemoteVcsSupportImpl.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            } else {
                reportHostInfoNotAvailable(env);
                return false;
            }
        }     
    }

    @Override
    public boolean isSolaris(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return System.getProperty("os.name").startsWith("SunOS"); // NOI18N
        } else {
            FileSystem fs = getFileSystem(proxy);
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    return HostInfoUtils.getHostInfo(env).getOSFamily() == HostInfo.OSFamily.SUNOS;
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    Logger.getLogger(RemoteVcsSupportImpl.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            } else {
                reportHostInfoNotAvailable(env);
                return false;
            }
        }
    }

    @Override
    public boolean isUnix(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return Utilities.isUnix();
        } else {
            FileSystem fs = getFileSystem(proxy);
            ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(fs);
            if (HostInfoUtils.isHostInfoAvailable(env)) {
                try {
                    switch (HostInfoUtils.getHostInfo(env).getOSFamily()) {
                        case LINUX:
                        case MACOSX:
                        case FREEBSD:
                        case SUNOS:
                            return true;
                        case WINDOWS:
                            return false;
                        case UNKNOWN:
                            return false;
                        default:
                            throw new IllegalStateException("Unexpected OSFamily: " + this); //NOI18N
                    }
                } catch (IOException | ConnectionManager.CancellationException ex) {
                    Logger.getLogger(RemoteVcsSupportImpl.class.getName()).log(Level.SEVERE, null, ex);
                    return false;
                }
            } else {                
                reportHostInfoNotAvailable(env);
                return false;
            }
        }     
    }

    @Override
    public long getSize(VCSFileProxy proxy) {
        File file = proxy.toFile();
        if (file != null) {
            return file.length();
        } else {
            return RemoteVcsSupportUtil.getSize(getFileSystem(proxy), proxy.getPath());
        }
    }

    @Override
    public OutputStream getOutputStream(VCSFileProxy proxy) throws IOException {
        File file = proxy.toFile();
        if (file != null) {
            File parent = file.getParentFile();
            if (!parent.exists()) {
                parent.mkdirs();
            }
            return new FileOutputStream(file);
        } else {
            return RemoteVcsSupportUtil.getOutputStream(getFileSystem(proxy), proxy.getPath());
        }
    }

    @Override
    public String getFileSystemKey(FileSystem fs) {
        final String toUrl = FileSystemProvider.toUrl(fs, "/"); // NOI18N
        return toUrl.substring(0, toUrl.indexOf('/')); //NOI18N
    }

    @Override
    public boolean isConnectedFileSystem(FileSystem file) {
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(file);
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    @Override
    public void connectFileSystem(FileSystem file) {
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(file);
        ConnectionManager.getInstance().connect(env);
    }

    @Override
    public String toString(VCSFileProxy proxy) {
        return FileSystemProvider.toUrl(getFileSystem(proxy), proxy.getPath());
    }

    @Override
    public VCSFileProxy fromString(String proxyString) {
        FileSystem fs = FileSystemProvider.urlToFileSystem(proxyString);
        VCSFileProxy rootProxy = VCSFileProxy.createFileProxy(fs.getRoot());
        return VCSFileProxy.createFileProxy(rootProxy, proxyString);
    }

    @Override
    public void delete(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            deleteRecursively(javaFile);
        } else {
            RemoteVcsSupportUtil.delete(getFileSystem(file), file.getPath());
        }
    }

    /**
     * Deletes on disconnect
     * @param file file to delete
     */
    @Override
    public void deleteOnExit(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            javaFile.deleteOnExit();
        } else {
            RemoteVcsSupportUtil.deleteOnExit(getFileSystem(file), file.getPath());
        }
    }

    @Override
    public void deleteExternally(VCSFileProxy file) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            deleteRecursively(javaFile);
        } else {
            RemoteVcsSupportUtil.deleteExternally(getFileSystem(file), file.getPath());
        }
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    deleteRecursively(files[i]);
                }
            }
        }
        file.delete();
    }

    @Override
    public void setLastModified(VCSFileProxy file, VCSFileProxy referenceFile) {
        File javaFile = file.toFile();
        if (javaFile != null) {
            javaFile.setLastModified(referenceFile.lastModified());
        } else {
            RemoteVcsSupportUtil.setLastModified(getFileSystem(file), file.getPath(), referenceFile.getPath());
        }
    }

    @Override
    public FileSystem readFileSystem(DataInputStream is)  throws IOException {
        String uri = is.readUTF();
        try {
            return FileSystemProvider.getFileSystem(new URI(uri));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }

    @Override
    public void writeFileSystem(DataOutputStream os, FileSystem fs) throws IOException {
        os.writeUTF(fs.getRoot().toURI().toString());
    }

    @Override
    public void refreshFor(FileSystem fs, String... paths) throws ConnectException, IOException {
        RemoteVcsSupportUtil.refreshFor(fs, paths);
    }    

    @Override
    public URI toURI(VCSFileProxy file) {
        return RemoteVcsSupportUtil.toURI(getFileSystem(file), file.getPath());
    }

    @Override
    public URL toURL(VCSFileProxy file) {
        return RemoteVcsSupportUtil.toURL(getFileSystem(file), file.getPath());
    }    
}
