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

package org.openide.filesystems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.openide.filesystems.DefaultURLMapperProxy;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.BaseUtilities;

/** Mapper from FileObject -> URL.
 * Should be registered in default lookup. For details see {@link Lookup#getDefault()}.
 * For all methods, if the passed-in file object is the root folder
 * of some filesystem, then it is assumed that any valid file object
 * in that filesystem may also have a URL constructed for it by means
 * of appending the file object's resource path to the URL of the root
 * folder. If this cannot work for all file objects on the filesystem,
 * the root folder must not be assigned a URL of that type. nbfs: URLs
 * of course always work correctly in this regard.
 * @since 2.16
 */
public abstract class URLMapper {
    /**
     * URL which works inside this VM.
     * Not guaranteed to work outside the VM (though it may).
     */
    public static final int INTERNAL = 0;

    /**
     * URL which works inside this machine.
     * Not guaranteed to work from other machines (though it may).
     * <div class="nonnormative">
     * Typical protocols used: <code>file</code> for disk files (see {@link File#toURI});
     * <code>jar</code> to wrap other URLs (e.g. <code>jar:file:/some/thing.jar!/some/entry</code>).
     * </div>
     */
    public static final int EXTERNAL = 1;

    /** URL which works from networked machines.*/
    public static final int NETWORK = 2;

    /** results with URLMapper instances*/
    private static Lookup.Result<URLMapper> result;
    private static final List<URLMapper> CACHE_JUST_COMPUTING = new ArrayList<URLMapper>();
    private static final ThreadLocal<List<URLMapper>> threadCache = new ThreadLocal<List<URLMapper>>();

    static {
        DefaultURLMapperProxy.setDefault(new DefaultURLMapper());
        reset();
    }

    /** Cache of all available URLMapper instances. */
    private static List<URLMapper> cache;

    /** Reset cache, for use from unit tests. */
    static void reset() {
        cache = null;
        result = Lookup.getDefault().lookupResult(URLMapper.class);
        result.addLookupListener(
            new LookupListener() {
                public void resultChanged(LookupEvent ev) {
                    synchronized (URLMapper.class) {
                        cache = null;
                    }
                }
            }
        );
    }

    /** Find a good URL for this file object which works according to type:
     * <ul>
     * <li>inside this VM
     * <li>inside this machine
     * <li>from networked machines
     * </ul>
     * @return a suitable URL, or (only if not {@link #INTERNAL}) null
     */
    public static URL findURL(FileObject fo, int type) {

        /** secondly registered URLMappers are asked to resolve URL */
        for (URLMapper mapper : getInstances()) {
            URL retVal = mapper.getURL(fo, type);

            if (retVal != null) {
                return retVal;
            }
        }

        // if not resolved yet then internal URL with nbfs protocol is returned
        // XXX this would be better handled by making DefaultURLMapper just return nbfs for INTERNAL when necessary!
        if (type == INTERNAL) {
            return FileURL.encodeFileObject(fo);
        }

        return null;
    }

    /** Get a good URL for this file object which works according to type:
     * <ul>
     * <li>inside this VM
     * <li>inside this machine
     * <li>from networked machines
     * </ul>
     * The implementation can't use neither {@link FileUtil#toFile} nor {@link FileUtil#toFileObject}
     * otherwise StackOverflowError maybe thrown.
     * @return a suitable URL, or null
     */
    public abstract URL getURL(FileObject fo, int type);

    /** Find an array of FileObjects for this URL.
     * Zero or more FOs may be returned.
     *
     * For each returned FO, it must be true that FO -> URL gives the
     * exact URL which was passed in, but depends on appropriate type
     * <code> findURL(FileObject fo, int type) </code>.
     * @param url to wanted FileObjects
     * @return a suitable array of FileObjects, or empty array if not successful
     * @since  2.22
     * @deprecated Use {@link #findFileObject} instead.
     */
    @Deprecated
    public static FileObject[] findFileObjects(URL url) {
        Set<FileObject> retSet = new LinkedHashSet<FileObject>();

        for (URLMapper mapper: getInstances()) {
            FileObject[] retVal = mapper.getFileObjects(url);

            if (retVal != null) {
                retSet.addAll(Arrays.asList(retVal));
            }
        }

        return retSet.toArray(new FileObject[0]);
    }

    /** Find an appropriate instance of FileObject that addresses this URL
     *
     * @param url URL to be converted to file object
     * @return file object corresponding to URL or null if no one was found
     * @since  4.29
     */
    public static FileObject findFileObject(URL url) {
        if (url == null) {
            throw new NullPointerException("Cannot pass null URL to URLMapper.findFileObject"); // NOI18N
        }

        /** first basic implementation */
        FileObject[] results = null;

        Iterator<URLMapper> instances = getInstances().iterator();

        while (instances.hasNext() && ((results == null) || (results.length == 0))) {
            URLMapper mapper = instances.next();

            results = mapper.getFileObjects(url);
        }

        return ((results != null) && (results.length > 0)) ? results[0] : null;
    }

    /**
     * Get an array of FileObjects for this URL.
     * There is no reason to return array
     * with size greater than one because method {@link #findFileObject findFileObject}
     * uses just the first element (subsequent elements will not be accepted anyway).
     * The implementation cannot use either {@link FileUtil#toFile} nor {@link FileUtil#toFileObject}
     * or StackOverflowError may be thrown.
     * <p class="nonnormative">The only reason to return an array here
     * is for backward compatibility.</p>
     * @param url to wanted FileObjects
     * @return an array of FileObjects with size no greater than one, or null
     * @since 2.22
     */
    public abstract FileObject[] getFileObjects(URL url);

    /** Returns all available instances of URLMapper.
     * @return list of URLMapper instances
     */
    private static List<URLMapper> getInstances() {
        synchronized (URLMapper.class) {
            if (cache != null) {
                if ((cache != CACHE_JUST_COMPUTING) || (threadCache.get() == CACHE_JUST_COMPUTING)) {
                    return cache;
                }
            }

            // Set cache to empty array here to prevent infinite loop.
            // See issue #41358, #43359
            cache = CACHE_JUST_COMPUTING;
            threadCache.set(CACHE_JUST_COMPUTING);
        }

        ArrayList<URLMapper> res = null;

        try {
            res = new ArrayList<URLMapper>(result.allInstances());
            {
                // XXX hack to put default last, since we cannot easily adjust META-INF/services/o.o.f.URLM order to our tastes
                // (would need to ask *all other* impls to be earlier somewhere)
                URLMapper def = null;
                Iterator<URLMapper> it = res.iterator();
                while (it.hasNext()) {
                    URLMapper m = it.next();
                    if (m instanceof DefaultURLMapperProxy) {
                        def = m;
                        it.remove();
                        break;
                    }
                }
                if (def != null) {
                    res.add(def);
                }
            }
            return res;
        } finally {
            synchronized (URLMapper.class) {
                if (cache == CACHE_JUST_COMPUTING) {
                    cache = res;
                }

                threadCache.set(null);
            }
        }
    }

    /*** Basic impl. for JarFileSystem, LocalFileSystem, MultiFileSystem */
    private static class DefaultURLMapper extends URLMapper {
        DefaultURLMapper() {
        }

        // implements  URLMapper.getFileObjects(URL url)
        public FileObject[] getFileObjects(URL url) {
            String prot = url.getProtocol();

            if (prot.equals(FileURL.PROTOCOL)) { //// NOI18N

                FileObject retVal = FileURL.decodeURL(url);

                return (retVal == null) ? null : new FileObject[] { retVal };
            }

            if (prot.equals("jar")) { //// NOI18N

                return getFileObjectsForJarProtocol(url);
            }

            if (prot.equals("file")) { //// NOI18N

                File f = toFile(url);

                if (f != null) {
                    FileObject[] foRes = findFileObjectsInRepository(f);

                    if ((foRes != null) && (foRes.length > 0)) {
                        return foRes;
                    }
                }
            }

            return null;
        }

        private FileObject[] findFileObjectsInRepository(File f) {
            if (!f.equals(FileUtil.normalizeFile(f))) {
                throw new IllegalArgumentException(
                    "Parameter file was not " + // NOI18N
                    "normalized. Was " + f + " instead of " + FileUtil.normalizeFile(f)
                ); // NOI18N
            }

            @SuppressWarnings("deprecation") // keep for backward compatibility w/ NB 3.x
            Enumeration<? extends FileSystem> en = Repository.getDefault().getFileSystems();
            LinkedList<FileObject> list = new LinkedList<FileObject>();
            String fileName = f.getAbsolutePath();

            while (en.hasMoreElements()) {
                FileSystem fs = en.nextElement();
                String rootName = null;
                FileObject fsRoot = fs.getRoot();
                File root = findFileInRepository(fsRoot);

                if (root == null) {
                    Object rootPath = fsRoot.getAttribute("FileSystem.rootPath"); //NOI18N

                    if (rootPath instanceof String) {
                        rootName = (String) rootPath;
                    } else {
                        continue;
                    }
                }

                if (rootName == null) {
                    rootName = root.getAbsolutePath();
                }

                /**root is parent of file*/
                if (fileName.indexOf(rootName) == 0) {
                    String res = fileName.substring(rootName.length()).replace(File.separatorChar, '/');
                    FileObject fo = fs.findResource(res);
                    File file2Fo = (fo != null) ? findFileInRepository(fo) : null;

                    if ((fo != null) && (file2Fo != null) && f.equals(file2Fo)) {
                        if (fo.getClass().toString().indexOf("org.netbeans.modules.masterfs.MasterFileObject") != -1) { //NOI18N
                            list.addFirst(fo);
                        } else {
                            list.addLast(fo);
                        }
                    }
                }
            }

            FileObject[] results = new FileObject[list.size()];
            list.toArray(results);

            return results;
        }

        // implements  URLMapper.getURL(FileObject fo, int type)
        public URL getURL(FileObject fo, int type) {
            if (fo == null) {
                return null;
            }

            if (type == NETWORK) {
                return null;
            }

            if (fo instanceof MultiFileObject && (type == INTERNAL)) {
                // Stick to nbfs protocol, otherwise URL calculations
                // get messed up. See #39613.
                return null;
            }

            File fFile = findFileInRepository(fo);

            if (fFile != null) {
                try {
                    return toURL(fFile, fo);
                } catch (MalformedURLException mfx) {
                    assert false : mfx;

                    return null;
                }
            }

            URL retURL = null;
            FileSystem fs = null;

            try {
                fs = fo.getFileSystem();
            } catch (FileStateInvalidException fsex) {
                return null;
            }

            if (fs instanceof JarFileSystem) {
                JarFileSystem jfs = (JarFileSystem) fs;
                File f = jfs.getJarFile();

                if (f == null) {
                    return null;
                }

                try {
                    // XXX clumsy; see ArchiveURLMapper for possible cleaner style
                    String toReplace = "__EXCLAMATION_REPLACEMENT__";//NOI18N
                    retURL = new URL(
                            "jar:" + BaseUtilities.toURI(new File(f,toReplace + fo.getPath())).toString().replaceFirst("/"+toReplace,"!/") + // NOI18N
                            ((fo.isFolder() && !fo.isRoot()) ? "/" : "")
                        ); // NOI18N
                } catch (MalformedURLException mfx) {
                    mfx.printStackTrace();

                    return null;
                }
            } else if (fs instanceof XMLFileSystem) {
                URL retVal = null;

                try {
                    retVal = ((XMLFileSystem) fs).getURL(fo.getPath());

                    if (retVal == null) {
                        return null;
                    }

                    if (type == INTERNAL) {
                        return retVal;
                    }

                    boolean isInternal = retVal.getProtocol().startsWith("nbres"); //NOI18N

                    if ((type == EXTERNAL) && !isInternal) {
                        return retVal;
                    }

                    return null;
                } catch (FileNotFoundException fnx) {
                    return null;
                }
            }

            return retURL;
        }

        private static URL toURL(File fFile, FileObject fo) throws MalformedURLException {
            URL retVal = BaseUtilities.toURI(fFile).toURL();
            if (retVal != null && fo.isFolder()) {
                // #155742,160333 - URL for folder must always end with slash
                final String urlDef = retVal.toExternalForm();
                final String pathSeparator = "/";//NOI18N
                if (!urlDef.endsWith(pathSeparator)) {
                    retVal = new URL(urlDef + pathSeparator);
                }
            }
            return retVal;
        }

        private static File findFileInRepository(FileObject fo) {
            File f = (File) fo.getAttribute("java.io.File"); // NOI18N

            return (f != null) ? FileUtil.normalizeFile(f) : null;
        }

        private static FileObject[] getFileObjectsForJarProtocol(URL url) {
            FileObject retVal = null;
            JarURLParser jarUrlParser = new JarURLParser(url);
            File file = jarUrlParser.getJarFile();
            String entryName = jarUrlParser.getEntryName();

            if (file != null) {
                JarFileSystem fs = findJarFileSystem(file);

                if (fs != null) {
                    if (entryName == null) {
                        entryName = ""; // #39190: root of JAR
                    }

                    retVal = fs.findResource(entryName);
                }
            }

            return (retVal == null) ? null : new FileObject[] { retVal };
        }

        private static JarFileSystem findJarFileSystem(File jarFile) {
            JarFileSystem retVal = null;
            @SuppressWarnings("deprecation") // keep for backward compatibility w/ NB 3.x
            Enumeration<? extends FileSystem> en = Repository.getDefault().getFileSystems();

            while (en.hasMoreElements()) {
                FileSystem fs = en.nextElement();

                if (fs instanceof JarFileSystem) {
                    File fsJarFile = ((JarFileSystem) fs).getJarFile();

                    if (fsJarFile.equals(jarFile)) {
                        retVal = (JarFileSystem) fs;

                        break;
                    }
                }
            }

            return retVal;
        }

        private static File toFile(URL u) {
            if (u == null) {
                throw new NullPointerException();
            }

            try {
                URI uri = new URI(u.toExternalForm());

                return FileUtil.normalizeFile(BaseUtilities.toFile(uri));
            } catch (URISyntaxException use) {
                // malformed URL
                return null;
            } catch (IllegalArgumentException iae) {
                // not a file: URL
                return null;
            }
        }

        private static class JarURLParser {
            private File jarFile;
            private String entryName;

            JarURLParser(URL originalURL) {
                parse(originalURL);
            }

            /** copy & pasted from JarURLConnection.parse*/
            void parse(URL originalURL) {
                String spec = originalURL.getFile();

                int separator = spec.indexOf('!');

                if (separator != -1) {
                    try {
                        jarFile = toFile(new URL(spec.substring(0, separator++)));
                        entryName = null;
                    } catch (MalformedURLException e) {
                        return;
                    }

                    /* if ! is the last letter of the innerURL, entryName is null */
                    if (++separator != spec.length()) {
                        try {
                            // XXX new URI("substring").getPath() might be better?
                            entryName = URLDecoder.decode(spec.substring(separator),"UTF-8");
                        } catch (UnsupportedEncodingException ex) {
                            return;
                        }
                    }
                }
            }

            File getJarFile() {
                return jarFile;
            }

            String getEntryName() {
                return entryName;
            }
        }
    }
}
