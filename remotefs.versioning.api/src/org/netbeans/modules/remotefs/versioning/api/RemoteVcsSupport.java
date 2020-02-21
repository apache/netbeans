/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */
package org.netbeans.modules.remotefs.versioning.api;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFileChooser;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.remotefs.versioning.spi.RemoteVcsSupportImplementation;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.*;

/**
 *
 */
public final class RemoteVcsSupport {

    private RemoteVcsSupport() {
    }

    /**
     * @param proxy defines FS and initial selection
     * @return file chooser or null if no providers found
     */
    public static JFileChooser createFileChooser(VCSFileProxy proxy) {
        final File file = proxy.toFile();
        if (file !=  null) {
            JFileChooser chooser = new JFileChooser();
            chooser.setSelectedFile(file);
            return  chooser;
        } else {
            return getImpl().createFileChooser(proxy);
        }
    }

    public static VCSFileProxy getSelectedFile(JFileChooser chooser) {
        return getImpl().getSelectedFile(chooser);
    }

    public static FileSystem getFileSystem(VCSFileProxy proxy) {
        return getImpl().getFileSystem(proxy);
    }

    public static FileSystem[] getFileSystems() {
        return getImpl().getFileSystems();
    }

    public static FileSystem[] getConnectedFileSystems() {
        return getImpl().getConnectedFileSystems();
    }

    public static FileSystem getDefaultFileSystem() {
        return getImpl().getDefaultFileSystem();
    }

    public static boolean isSymlink(VCSFileProxy proxy) {
        return getImpl().isSymlink(proxy);
    }
    
    static String readSymbolicLinkPath(VCSFileProxy file)  throws IOException {
        return getImpl().readSymbolicLinkPath(file);
    }

    public static boolean canRead(VCSFileProxy proxy) {
        return getImpl().canRead(proxy);
    }
    
    public static boolean canRead(VCSFileProxy base, String subdir) {
        return getImpl().canRead(base, subdir);
    }

    public static VCSFileProxy getCanonicalFile(VCSFileProxy proxy) throws IOException {
        return getImpl().getCanonicalFile(proxy);
    }

    public static String getCanonicalPath(VCSFileProxy proxy) throws IOException {
        return getImpl().getCanonicalPath(proxy);
    }    

    public static VCSFileProxy getHome(VCSFileProxy proxy) {
        return getImpl().getHome(proxy);
    }

    public static boolean isMac(VCSFileProxy proxy) {
        return getImpl().isMac(proxy);
    }

    static boolean isSolaris(VCSFileProxy file) {
        return getImpl().isSolaris(file);
    }

    public static boolean isUnix(VCSFileProxy proxy) {
        return getImpl().isUnix(proxy);
    }

    public static long getSize(VCSFileProxy proxy) {
        return getImpl().getSize(proxy);
    }

    public static String getFileSystemKey(FileSystem proxy) {
        return getImpl().getFileSystemKey(proxy);
    }
    
    public static boolean isConnectedFileSystem(FileSystem file) {
        return getImpl().isConnectedFileSystem(file);
    }

    public static void connectFileSystem(FileSystem file) {
        getImpl().connectFileSystem(file);
    }

    public static String toString(VCSFileProxy proxy) {
        return getImpl().toString(proxy);
    }

    public static VCSFileProxy fromString(String proxy) {
        return getImpl().fromString(proxy);
    }
    
    public static FileSystem readFileSystem(DataInputStream is) throws IOException {
        return getImpl().readFileSystem(is);
    }

    public static void writeFileSystem(DataOutputStream os, FileSystem fs) throws IOException {
        getImpl().writeFileSystem(os, fs);
    }

    public static OutputStream getOutputStream(VCSFileProxy proxy) throws IOException {
        return getImpl().getOutputStream(proxy);
    }

    public static Charset getEncoding(VCSFileProxy proxy) {
        FileObject fo = proxy.toFileObject();
        Charset encoding = null;
        if (fo != null) {
            encoding = FileEncodingQuery.getEncoding(fo);
        }
        if(encoding == null) {
            if (proxy.toFile() == null) {
                encoding = Charset.forName(System.getProperty("cnd.remote.charset","UTF-8"));
            } else {
                encoding = Charset.defaultCharset();
            }
        }
        return encoding;
    }
    
    public static void delete(VCSFileProxy file) {
        getImpl().delete(file);
    }

    /**
     * Deletes on disconnect
     * @param file file to delete
     */
    public static void deleteOnExit(VCSFileProxy file) {
        getImpl().deleteOnExit(file);
    }

    static void deleteExternally(VCSFileProxy file) {
        getImpl().deleteExternally(file);
    }

    public static void setLastModified(VCSFileProxy file, VCSFileProxy referenceFile) {
        getImpl().setLastModified(file, referenceFile);
    }
    
    public static URI toURI(VCSFileProxy file) {
        return getImpl().toURI(file);
    }
    
    public static URL toURL(VCSFileProxy file) {
        return getImpl().toURL(file);
    }

    /**
     * All proxies should belong to the SAME FILE SYSTEM.
     * In other words, either have geFile() returning not null
     * or getFileObject().getFileSystem() return thr same value.
     */
    public static void refreshFor(VCSFileProxy... proxies)  throws ConnectException, IOException {
        if (proxies.length == 0) {
            return;
        }        
        if (proxies[0].toFile() != null) {
            File[] files = new File[proxies.length];
            for (int i = 0; i < proxies.length; i++) {
                final File f = proxies[i].toFile();
                assert f != null;
                files[i] = f;
            }
            FileUtil.refreshFor(files);
        } else {
            FileSystem fs = getFileSystem(proxies[0]);
            String[] paths = new String[proxies.length];
            for (int i = 0; i < proxies.length; i++) {
                paths[i] = proxies[i].getPath();
                assert getFileSystem(proxies[i]) == fs;
            }
            refreshFor(fs, paths);
        }
    }

    public static void refreshFor(FileSystem fs, String... paths) throws ConnectException, IOException {
        getImpl().refreshFor(fs, paths);
    }

    private static RemoteVcsSupportImplementation getImpl() {
        RemoteVcsSupportImplementation impl = Lookup.getDefault().lookup(RemoteVcsSupportImplementation.class);
        if (impl == null) {
            throw new IllegalStateException("No provider found for " + //NOI18N
                    RemoteVcsSupportImplementation.class.getName());
        }
        return impl;
    }
}
