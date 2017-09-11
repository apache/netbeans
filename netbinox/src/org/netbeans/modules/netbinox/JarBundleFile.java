/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Oracle, Inc.
 */
package org.netbeans.modules.netbinox;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.eclipse.osgi.baseadaptor.BaseData;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.BundleFile;
import org.eclipse.osgi.baseadaptor.bundlefile.DirBundleFile;
import org.eclipse.osgi.baseadaptor.bundlefile.DirZipBundleEntry;
import org.eclipse.osgi.baseadaptor.bundlefile.MRUBundleFileList;
import org.eclipse.osgi.baseadaptor.bundlefile.ZipBundleFile;
import org.netbeans.core.netigso.spi.BundleContent;
import org.netbeans.core.netigso.spi.NetigsoArchive;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/** This is fake bundle. It is created by the Netbinox infrastructure to 
 * use the {@link NetigsoArchive} to get cached data and speed up the start.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class JarBundleFile extends BundleFile implements BundleContent {
    private BundleFile delegate;

    private static Map<Long,File> usedIds;

    private final MRUBundleFileList mru;
    private final BaseData data;
    private final NetigsoArchive archive;
    
    JarBundleFile(
        File base, BaseData data, NetigsoArchive archive,
        MRUBundleFileList mru, boolean isBase
    ) {
        super(base);

        long id;
        if (isBase) {
            id = data.getBundleID();
        } else {
            id = 100000 + base.getPath().hashCode();
        }

        boolean assertOn = false;
        assert assertOn = true;
        if (assertOn) {
            if (usedIds == null) {
                usedIds =  new HashMap<Long, File>();
            }
            File prev = usedIds.put(id, base);
            if (prev != null && !prev.equals(base)) {
                NetbinoxFactory.LOG.log(
                    Level.WARNING,
                    "same id: {0} for {1} and {2}", // NOI18N
                    new Object[]{id, base, prev}
                );
            }
        }

        this.archive = archive.forBundle(id, this);
        this.data = data;
        this.mru = mru;
    }


    private synchronized BundleFile delegate(String who, String what) {
        if (delegate == null) {
            NetbinoxFactory.LOG.log(Level.FINE, "opening {0} because of {1} needing {2}", new Object[]{data.getLocation(), who, what});
            try {
                delegate = new ZipBundleFile(getBaseFile(), data, mru) {
                    @Override
                    protected boolean checkedOpen() {
                        try {
                            return getZipFile() != null;
                        } catch (IOException ex) {
                            final File bf = new File(getBaseFile().getPath());
                            if (bf.isDirectory()) {
                                try {
                                    delegate = new DirBundleFile(bf, false);
                                    return false;
                                } catch (IOException dirEx) {
                                    NetbinoxFactory.LOG.log(Level.WARNING, 
                                        "Cannot create DirBundleFile for " + bf,
                                        dirEx
                                    );
                                }
                            }
                            NetbinoxFactory.LOG.log(Level.WARNING, "Cannot open bundle delegate {0}", bf);
                            if (!bf.isFile() || !bf.canRead()) {
                                delegate = EmptyBundleFile.EMPTY;
                                return false;
                            }
                        }
                        // no optimizations
                        return super.checkedOpen();
                    }
                };
            } catch (IOException ex) {
                NetbinoxFactory.LOG.log(Level.WARNING, "Error creating delegate for {0} because of {1}", new Object[] { getBaseFile(), data.getLocation() });
                delegate = EmptyBundleFile.EMPTY;
            }
        }
        return delegate;
    }

    @Override
    public File getBaseFile() {
        final File file = super.getBaseFile();
        class VFile extends File {

            public VFile() {
                super(file.getPath());
            }

            @Override
            public boolean isDirectory() {
                return false;
            }

            @Override
            public boolean isFile() {
                return true;
            }

            @Override
            public boolean exists() {
                return true;
            }

            @Override
            public File getAbsoluteFile() {
                return this;
            }

            @Override
            public long lastModified() {
                return data.getLastModified();
            }
        }
        return new VFile();
    }

    @Override
    public File getFile(String file, boolean bln) {
        byte[] exists = getCachedEntry(file);
        if (exists == null) {
            return null;
        }
        BundleFile d = delegate("getFile", file);
        return d == null ? null : d.getFile(file, bln);
    }

    @Override
    public byte[] resource(String name) throws IOException {
        BundleEntry u = findEntry("resource", name);
        if (u == null) {
            return null;
        }
        InputStream is = u.getInputStream();
        if (is == null) {
            return new byte[0];
        }
        byte[] arr = null;
        try {
            arr = new byte[is.available()];
            int pos = 0;
            for (;;) {
                int toRead = arr.length - pos;
                if (toRead == 0) {
                    break;
                }
                int len = is.read(arr, pos, toRead);
                if (len == -1) {
                    break;
                }
                pos += len;
            }
            if (pos != arr.length) {
                throw new IOException("Not read enough: " + pos + " should have been: " + arr.length); // NOI18N
            }
        } finally {
            is.close();
        }
        NetbinoxFactory.LOG.log(Level.FINE, "Loaded {1} bytes for {0}", new Object[] { name, arr.length }); // NOI18N
        return arr;
    }

    private BundleEntry findEntry(String why, final String name) {
        if (!name.equals("META-INF/MANIFEST.MF") && // NOI18N
            data != null && 
            data.getLocation() != null && 
            data.getLocation().startsWith("netigso://") // NOI18N
        ) { 
            String cnb = data.getLocation().substring(10);
            for (ModuleInfo mi : Lookup.getDefault().lookupAll(ModuleInfo.class)) {
                if (mi.getCodeNameBase().equals(cnb)) {
                    if (!mi.isEnabled()) {
                        break;
                    }
                    final URL url = mi.getClassLoader().getResource(name);
                    if (url != null) {
                        return new ModuleEntry(url, name);
                    } else {
                        break;
                    }
                }
            }
        }
        
        if ("/".equals(name)) { // NOI18N
            return new RootEntry(this); // NOI18N
        }
        
        BundleEntry u;
        for (;;) {
            BundleFile d = delegate(why, name);
            u = d.getEntry(name);
            if (u != null || d == delegate) {
                break;
            }
        }
        return u;
    }


    private byte[] getCachedEntry(String name) {
        try {
            return archive.fromArchive(name);
        } catch (IOException ex) {
            return null;
        }
    }

    @Override
    public BundleEntry getEntry(final String name) {
        if (!archive.isActive()) {
            return delegate("inactive", name).getEntry(name); // NOI18N
        }
        
        final byte[] arr = getCachedEntry(name);
        if (arr == null && !name.equals("/")) {
            return null;
        }
        return new CachingEntry(arr, name);
    }

    @Override
    public Enumeration<String> getEntryPaths(String prefix) {
        BundleFile d = delegate("getEntryPaths", prefix);
        if (d == null) {
            return Collections.enumeration(Collections.<String>emptyList());
        }
        return d.getEntryPaths(prefix);
    }

    @Override
    public synchronized void close() throws IOException {
        if (delegate != null) {
            delegate.close();
        }
    }

    @Override
    public void open() throws IOException {
        if (delegate != null) {
            delegate.open();
        }
    }

    @Override
    public boolean containsDir(String path) {
        return path.endsWith("/") && getEntry(path) != null;
    }

    private class CachingEntry extends BundleEntry {
        private final String name;
        private final int size;
        private final Reference<byte[]> arr;

        public CachingEntry(byte[] arr, String name) {
            this.size = arr.length;
            this.name = name;
            this.arr = new SoftReference<byte[]>(arr);
        }

        @Override
        public InputStream getInputStream() throws IOException {
            byte[] data = arr.get();
            // once used, let the array go...
            arr.clear();
            if (data == null) {
                data = getCachedEntry(name);
            }
            if (data == null) {
                throw new FileNotFoundException();
            }
            return new ByteArrayInputStream(data);
        }

        @Override
        public long getSize() {
            return size;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public long getTime() {
            return getBaseFile().lastModified();
        }

        @Override
        public URL getLocalURL() {
            return findEntry("getLocalURL", name).getLocalURL(); // NOI18N
        }

        @Override
        public URL getFileURL() {
            return findEntry("getFileURL", name).getFileURL(); // NOI18N
        }
    }
}
