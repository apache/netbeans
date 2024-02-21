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

package org.netbeans;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;

/**
 * A ProxyClassLoader capable of loading classes from a set of jar files
 * and local directories.
 *
 * @author  Petr Nejedly
 */
public class JarClassLoader extends ProxyClassLoader {
    //
    // When making changes to this file, check if
    // platform/netbinox/src/org/netbeans/modules/netbinox/JarBundleFile.java
    // should also be adjusted. At least the multi-release handling is similar.
    //
    
    private static Stamps cache;
    private static final String META_INF = "META-INF/";
    private static final Name MULTI_RELEASE = new Name("Multi-Release");
    private static final int BASE_VERSION = 8;
    private static final int RUNTIME_VERSION;

    static {
        int version;
        try {
            Object runtimeVersion = Runtime.class.getMethod("version").invoke(null);
            version = (int) runtimeVersion.getClass().getMethod("major").invoke(runtimeVersion);
        } catch (ReflectiveOperationException ex) {
            version = BASE_VERSION;
        }
        RUNTIME_VERSION = version;
    }
    
    static Archive archive = new Archive(); 

    static void initializeCache() {
        cache = Stamps.getModulesJARs();
        archive = new Archive(cache);
        PackageAttrsCache.initialize();
    }
    
    /**
     * Creates a new archive or updates existing archive with the necessary
     * resources gathered so far. It also stops gatheing and serving
     * additional request, if it was still doing so.
     */    
    public static void saveArchive() {
        if (cache != null) {
            try {
                archive.save(cache);
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "saving archive", ioe);
            }
        } else {
            archive.stopGathering();
            archive.stopServing();
        }
    }
    
    /** Check whether the archive has already been populated during 
     * previous executions.
     * 
     * @return true, if the archive is ready and non-empty
     * @since 2.61
     */
    public static boolean isArchivePopulated() {
        return archive != null && archive.isPopulated();
    }
    
    static {
        ProxyURLStreamHandlerFactory.register();
    }
    
    private static final Logger LOGGER = Logger.getLogger(JarClassLoader.class.getName());

    private Source[] sources;
    private Module module;
    
    /** Creates new JarClassLoader.
     * Gives transitive flag as true.
     */
    public JarClassLoader(List<File> files, ClassLoader[] parents) {
        this(files, parents, true, null);
    }
    
    public JarClassLoader(List<File> files, ClassLoader[] parents, boolean transitive) {
        this(files, parents, transitive, null);
    }
    /** Creates new JarClassLoader.
     * @since org.netbeans.core/1 > 1.6
     * @see ProxyClassLoader#ProxyClassLoader(ClassLoader[],boolean)
     */
    public JarClassLoader(List<File> files, ClassLoader[] parents, boolean transitive, Module mod) {
        super(parents, transitive);
        this.module = mod;
        List<Source> l = new ArrayList<Source>(files.size());
        try {
            for (File file : files) {
                l.add(Source.create(file, this));
            }
        } catch (IOException exc) {
            throw new IllegalArgumentException(exc.getMessage());
        }
        sources = l.toArray(new Source[0]);
        // overlaps with old packages doesn't matter,PCL uses sets.
        addCoveredPackages(getCoveredPackages(module, sources));
    }

    final void addURL(URL location) throws IOException, URISyntaxException {
        File f = BaseUtilities.toFile(location.toURI());
        assert f.exists() : "URL must be existing local file: " + location;

        List<Source> arr = new ArrayList<Source>(Arrays.asList(sources));
        arr.add(new JarSource(f));

        synchronized (sources) {
            sources = arr.toArray(new Source[0]);
        }

        // overlaps with old packages doesn't matter,PCL uses sets.
        addCoveredPackages(getCoveredPackages(module, sources));
    }

    /** Allows to specify the right permissions, OneModuleClassLoader does it differently.
     */
    protected PermissionCollection getPermissions( CodeSource cs ) {           
        return Policy.getPolicy().getPermissions(cs);       
    }        
    
    
    protected Package definePackage(String name, Manifest man, URL url)
    throws IllegalArgumentException {
        if (man == null) {
            return definePackage(name, null, null, null, null, null, null, null);
        }

        String path = name.replace('.', '/').concat("/"); // NOI18N
        String[] arr = PackageAttrsCache.findPackageAttrs(url, man, path);
        URL sealBase = "true".equalsIgnoreCase(arr[6]) ? url : null; // NOI18N
        return definePackage(name, arr[0], arr[1], arr[2],
            arr[3], arr[4], arr[5], sealBase);
    }
    
    /**
     * Bytecode patching helper
     */
    private PatchByteCode patchingBytecode;
    
    byte[] getClassData(String name) {
        String path = name.replace('.', '/').concat(".class"); // NOI18N
        for( int i=0; i<sources.length; i++ ) {
            final Source src = sources[i];
            byte[] data = src.getClassData(path);
            if (data != null) {
                return data;
            }
        }
        return null;
    }
    
    @Override
    protected Class<?> doLoadClass(String pkgName, String name) {
        String path = name.replace('.', '/').concat(".class"); // NOI18N
        
        // look up the Sources and return a class based on their content
        for( int i=0; i<sources.length; i++ ) {
            final Source src = sources[i];
            byte[] data = src.getClassData(path);
            if (data == null) continue;

            synchronized (sources) {
                if (patchingBytecode == null) {
                    Enumeration<URL> res = findResources("META-INF/.bytecodePatched"); // NOI18N
                    if (res.hasMoreElements()) {
                        LOGGER.log(Level.FINE, "Patching bytecode in {0}", this);
                    }
                    patchingBytecode = PatchByteCode.fromStream(res, this);
                }
            }
            try {
                data = patchingBytecode.apply(name, data);
            } catch (Exception x) {
                LOGGER.log(Level.INFO, "Could not bytecode-patch " + name, x);
            }
            
            // Note that we assume that if we are defining a class in this package,
            // we should also define the package! Thus recurse==false.
            // However special packages might be defined in a parent and then we want
            // to have the same Package object, proper sealing check, etc.; so be safe,
            // overhead is probably small (check in parents, nope, check super which
            // delegates to system loaders).
            Package pkg = getPackageFast(pkgName, true);
            if (pkg != null) {
                // XXX full sealing check, URLClassLoader does something more
                if (pkg.isSealed() && !pkg.isSealed(src.getURL())) throw new SecurityException("sealing violation"); // NOI18N
            } else {
                class DelayedManifest extends Manifest {
                    private Manifest delegate;
                    
                    private Manifest delegate() {
                        if (delegate == null) {
                            Manifest m;
                            m = module == null || src != sources[0] ? src.getManifest() : module.getManifest();
                            if (m == null) {
                                m = new Manifest();
                            }
                            delegate = m;
                            return m;
                        }
                        return delegate;
                    }
                    
                    @Override
                    public Attributes getMainAttributes() {
                        return delegate().getMainAttributes();
                    }

                    @Override
                    public Attributes getAttributes(String name) {
                        return delegate().getAttributes(name);
                    }

                    @Override
                    public Map<String, Attributes> getEntries() {
                        return delegate().getEntries();
                    }
                }
                Manifest man = new DelayedManifest();

                try {
                    definePackage(pkgName, man, src.getURL());
                } catch (IllegalArgumentException x) {
                    // #156478: possibly a race condition defining packages in parallel parents? Ignore.
                    LOGGER.log(Level.FINE, null, x);
                }
            }
            try {
                data = NbInstrumentation.patchByteCode(this, name, src.getProtectionDomain(), data);
            } catch (IllegalClassFormatException ex) {
                LOGGER.log(Level.WARNING, "Problems patching" + name, ex);
            }
            return defineClass (name, data, 0, data.length, src.getProtectionDomain());
        } 
        return null;
    }
    // look up the jars and return a resource based on a content of jars
    @Override
    public URL findResource(String name) {
        for( int i=0; i<sources.length; i++ ) {
            URL item = sources[i].getResource(name);
            if (item != null) return item;
        }
	return null;
    }

    @Override
    public Enumeration<URL> findResources(String name) {
        Vector<URL> v = new Vector<URL>(3);
        // look up the jars and return a resource based on a content of jars

        for( int i=0; i<sources.length; i++ ) {
            URL item = sources[i].getResource(name);
            if (item != null) v.add(item);
        }
        return v.elements();
    }
    
    public @Override void destroy() {
        super.destroy ();
        for (Source src : sources) {
            try {
                src.destroy();
            } catch (IOException ioe) {
                LOGGER.log(Level.WARNING, "could not destroy " + src, ioe);
            }
        }
    }

    /** package-private method useful only for testing.
     * Used from JarClassLoaderTest to force close before reopening. */
    void releaseJars() throws IOException {
        for (Source src : sources) {
            if (src instanceof JarSource) {
                ((JarSource)src).doCloseJar();
            }
        }
    }

    abstract static class Source {
        private URL url;
        private ProtectionDomain pd;
        protected JarClassLoader jcl;
        private static Map<String,Source> sources = new HashMap<String, Source>();
        private Boolean multiRelease;
        
        public Source(URL url) {
            this.url = url;
        }
        
        public final URL getURL() {
            return url;
        }

        public abstract String getPath();
        
        public final ProtectionDomain getProtectionDomain() {
            if (pd == null) {
                CodeSource cs = new CodeSource(url, (Certificate[])null);
                pd = new ProtectionDomain(cs, jcl.getPermissions(cs));
            }
            return pd;
        }
  
        public final URL getResource(String name) {
            try {
                return doGetResource(name);
            } catch (Exception e) {
                // can't get the resource. E.g. already closed JarFile
                LOGGER.log(Level.FINE, null, e);
            }
            return null;
        }
        
        protected abstract URL doGetResource(String name) throws IOException;
        
        public final byte[] getClassData(String path) {
            try {
                return readClass(path);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "looking up " + path, e);
            }
            return null;
        }

        protected abstract byte[] readClass(String path) throws IOException;

        public Manifest getManifest() {
            return null;
        }

        protected abstract void listCoveredPackages(Set<String> known, StringBuffer save);
        
        protected void destroy() throws IOException {
            // relatively slow (millis instead of micros),
            // but rare enough to not matter
            sources.values().remove(this);
        }
        
        static Source create(File f, JarClassLoader jcl) throws IOException {
            boolean directory;
            if (f.getName().endsWith("jar")) {
                directory = false;
            } else {
                directory = f.isDirectory();
            }
            Source src = directory ? new DirSource(f) : new JarSource(f);
            src.jcl = jcl;
            // should better use the same string as other indexes
            // this way, there are currently 3 similar long Strings per
            // JarClassLoader instance - its URL, its identifier
            // in Archive.sources map and this one
            sources.put(src.getPath(), src);
            return src;
        }

        @Override
        public String toString() {
            return url.toString();
        }

        protected boolean isMultiRelease() {
            Manifest man = getManifest();
            if(man == null) {
                return false;
            }
            if(multiRelease != null) {
                return multiRelease;
            }
            if (man.getMainAttributes().containsKey(MULTI_RELEASE)) {
                String multiReleaseString = (String) man.getMainAttributes().get(MULTI_RELEASE);
                multiRelease = Boolean.valueOf(multiReleaseString);
            } else {
                multiRelease = false;
            }
            return multiRelease;
        }

    }
    
    static void dumpFiles(File f, int retry) {
        for (;;) {
            if (f == null) {
                LOGGER.log(Level.INFO, "file {0} is null. # of retries {1}", new Object[]{f, retry}); // NOI18N
                break;
            }
            if (f.exists()) {
                LOGGER.log(Level.INFO, "file {0} exists. # of retries {1}", new Object[]{f, retry}); // NOI18N
                if (f.isDirectory()) {
                    LOGGER.log(Level.INFO, "{0} is directory and contains: {1}", new Object[]{f, Arrays.toString(f.list())}); // NOI18N
                } else {
                    LOGGER.log(Level.INFO, "{0} isDirectory: {1}, isFile: {2} size: {3}", new Object[]{f, f.isDirectory(), f.isFile(), f.length()}); // NOI18N
                }
                break;
            }
            LOGGER.log(Level.INFO, "{0} does not exist, # of retries {1}", new Object[]{f, retry}); // NOI18N
            f = f.getParentFile();
        }
    }

    static class JarSource extends Source implements ArchiveResources {
        private String resPrefix;
        private File file;

        private Future<JarFile> fjar;
        private boolean dead;
        private int requests;
        private int used;
        private volatile int[] versions;
        private volatile Reference<Manifest> manifest;
        /** #141110: expensive to repeatedly look for them */
        private final Set<String> nonexistentResources = Collections.synchronizedSet(new HashSet<String>());
        private final Set<File> warnedFiles = Collections.synchronizedSet(new HashSet<File>()); // #183696
        
        JarSource(File file) throws IOException {
            this(file, toURI(file));
        }
        private JarSource(File file, String resPrefix) throws IOException {
            super(new URL(resPrefix)); // NOI18N
            this.resPrefix = resPrefix; // NOI18N;
            this.file = file;
        }

        @Override
        public String getPath() {
            return file.getPath();
        }

        private static String toURI(final File file) {
            class VFile extends File {
                public VFile() {
                    super(file.getPath());
                }

                @Override
                public boolean isDirectory() {
                    return false;
                }

                @Override
                public File getAbsoluteFile() {
                    return this;
                }
            }
            return "jar:" + BaseUtilities.toURI(new VFile()) + "!/"; // NOI18N
        }

        @Override
        public Manifest getManifest() {
            {
                Manifest man;
                if (manifest != null && (man = manifest.get()) != null) {
                    return man;
                }
            }
            try {
                byte[] arr = archive.getData(this, "META-INF/MANIFEST.MF");
                if (arr == null) {
                    return null;
                }
                final Manifest man = new Manifest(new ByteArrayInputStream(arr));
                manifest = new SoftReference<Manifest>(man);
                return man;
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Cannot read manifest for " + getPath(), ex);
                return null;
            }
        }
        
        JarFile getJarFile(final String forWhat) throws IOException {
            FutureTask<JarFile> init = null;
            synchronized(sources) {
                requests++;
                used++;
                if (fjar == null) {
                    fjar = sources.get(this);
                    if (fjar == null) {
                        fjar = init = new FutureTask<JarFile>(new Callable<JarFile>() {
                            @Override
                            public JarFile call() throws IOException {
                                int retry = 0;
                                for (;;) {
                                    try {
                                        long now = System.currentTimeMillis();
                                        JarFile ret;
                                        try {
                                            ret = new JarFile(file, false);
                                        } catch (FileNotFoundException | NoSuchFileException ex) {
                                            throw (ZipException)new ZipException(ex.getMessage()).initCause(ex);
                                        }
                                        long took = System.currentTimeMillis() - now;
                                        opened(JarClassLoader.JarSource.this, forWhat);
                                        if (took > 500) {
                                            LOGGER.log(Level.WARNING, "Opening {0} took {1} ms", new Object[]{file, took}); // NOI18N
                                        }
                                        return ret;
                                    } catch (ZipException zip) {
                                        if (file.exists() && retry++ < 3) {
                                            LOGGER.log(Level.WARNING, "Error opening " + file + " (exists=" + file.exists() + ") retry: " + retry, zip); // NOI18N
                                            opened(JarClassLoader.JarSource.this, "ziperror");
                                            continue;
                                        }
                                        dumpFiles(file, retry);
                                        throw zip;
                                    }
                                }
                            }
                        });
                        sources.put(this, fjar);
                    }
                }
            }
            if (init != null) init.run();
            return callGet();
        }
        
        private void releaseJarFile() {
            synchronized(sources) {
                assert used > 0;
                used--;
            }
        }
        
        
        @Override
        protected URL doGetResource(String name) throws IOException  {
            byte[] buf = archive.getData(this, name);
            if (buf == null) return null;
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "Loading {0} from {1}", new Object[] {name, file.getPath()});
            }
            try {
                return new URL(null, resPrefix + new URI(null, name, null).getRawPath(), new JarURLStreamHandler(jcl));
            } catch (URISyntaxException x) {
                throw new IOException(name + " in " + resPrefix + ": " + x.toString(), x);
            }
        }
        
        @Override
        protected byte[] readClass(String path) throws IOException {
            try {
                if ((! path.startsWith(META_INF)) && isMultiRelease() && RUNTIME_VERSION > BASE_VERSION) {
                    int[] vers = getVersions();
                    for (int version: vers) {
                        byte[] data = archive.getData(this, "META-INF/versions/" + version + "/" + path);
                        if (data != null) {
                            return data;
                        }
                    }
                }
                return archive.getData(this, path);
            } catch (ZipException ex) {
                dumpFiles(file, -1);
                throw ex;
            }
        }

        /**
         * @return versions for which a {@code META-INF/versions/NUMBER} entry exists.
         * The order is from largest version to lowest. Only versions supported by
         * the runtime VM are reported.
         */
        private int[] getVersions() {
            if (versions != null) {
                return versions;
            }
            try {
                Set<Integer> vers = new TreeSet<>(Collections.reverseOrder());
                for(int i = BASE_VERSION; i <= RUNTIME_VERSION; i++) {
                    String directory = "META-INF/versions/" + i;
                    byte[] data = archive.getData(this, directory);
                    if (data != null && data.length == 0) {
                        vers.add(i);
                    }
                }
                int[] ret = new int[vers.size()];
                int i = 0;
                for (Integer ver : vers) {
                    ret[i++] = ver;
                }
                versions = ret;
                return ret;
            } catch (IOException ioe) {
                if (warnedFiles.add(file)) {
                    LOGGER.log(Level.WARNING, "problems with " + file, ioe);
                    dumpFiles(file, -1);
                }
            }
            return new int[0];
        }

        @Override
        public byte[] resource(String path) throws IOException {
            if (nonexistentResources.contains(path)) {
                return null;
            }
            JarFile jf;
            try {
                jf = getJarFile(path);
            } catch (ZipException ex) {
                if (warnedFiles.add(file)) {
                    LOGGER.log(Level.INFO, "Cannot open " + file, ex);
                    dumpFiles(file, -1);
                }
                return null;
            }
            try {
                ZipEntry ze = jf.getEntry(path);
                if (ze == null) {
                    nonexistentResources.add(path);
                    return null;
                }

                if (LOGGER.isLoggable(Level.FINER)) {
                    LOGGER.log(Level.FINER, "Loading {0} from {1}", new Object[] {path, file.getPath()});
                }
            
                int len = (int)ze.getSize();
                byte[] data = new byte[len];
                InputStream is = jf.getInputStream(ze);
                int count = 0;
                while (count < len) {
                    count += is.read(data, count, len-count);
                }
                return data;
            } finally {
                releaseJarFile();
            }
        }


        @Override
        protected void listCoveredPackages(Set<String> known, StringBuffer save) {
            try {
                JarFile src = getJarFile("pkg");

                Enumeration<JarEntry> en = src.entries();
                while (en.hasMoreElements()) {
                    JarEntry je = en.nextElement();
                    if (! je.isDirectory()) {
                        String itm = je.getName();
                        int slash = itm.lastIndexOf('/');
                        if (slash == -1) {
                            // resource in default package
                            String res = "default/" + je.getName();
                            if (known.add(res)) {
                                save.append(res).append(',');
                            }
                            continue;
                        }
                        if (itm.startsWith("META-INF/")) {
                            String res = itm.substring(8); // "/services/pkg.Service"
                            if (known.add(res)) save.append(res).append(',');
                            continue;
                        }
                        String pkg = slash > 0 ? itm.substring(0, slash).replace('/','.') : "";
                        if (known.add(pkg)) save.append(pkg).append(',');
                    }
                }
            } catch (ZipException x) { // Unix
                if (warnedFiles.add(file)) {
                    LOGGER.log(Level.INFO, "Cannot open " + file, x);
                    dumpFiles(file, -1);
                }
            } catch (FileNotFoundException x) { // Windows
                if (warnedFiles.add(file)) {
                    LOGGER.log(Level.INFO, "Cannot open " + file, x);
                    dumpFiles(file, -1);
                }
            } catch (IOException ioe) {
                if (warnedFiles.add(file)) {
                    LOGGER.log(Level.WARNING, "problems with " + file, ioe);
                    dumpFiles(file, -1);
                }
            } finally {
                releaseJarFile();
            }
        }

        
        @Override
        protected void destroy() throws IOException {
            super.destroy();
            if (dead) {
                return;
            }
            
            File orig = file;

            if (!orig.isFile()) {
                // Can happen when a test module is deleted:
                // the physical JAR has already been deleted
                // when the module was disabled. In this case it
                // is possible that a classloader request for something
                // in the JAR could still come in. Does it matter?
                // See comment in Module.cleanup.
                return;
            }
            
            String name = orig.getName();
            String prefix, suffix;
            int idx = name.lastIndexOf('.');
            if (idx == -1) {
                prefix = name;
                suffix = null;
            } else {
                prefix = name.substring(0, idx);
                suffix = name.substring(idx);
            }
            
            while (prefix.length() < 3) prefix += "x"; // NOI18N
            File temp = Files.createTempFile(prefix, suffix).toFile();
            temp.deleteOnExit();

            InputStream is = new FileInputStream(orig);
            try {
                OutputStream os = new FileOutputStream(temp);
                try {
                    byte[] buf = new byte[4096];
                    int j;
                    while ((j = is.read(buf)) != -1) {
                        os.write(buf, 0, j);
                    }
                } finally {
                    os.close();
                }
            } finally {
                is.close();
            }
 
            doCloseJar();
            file = temp;
            dead = true;
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "#21114: replacing {0} with {1}", new Object[] {orig, temp});
            }
        }
        
        private JarFile callGet() throws IOException {
            boolean interrupted = false;
            JarFile ret;
            for (;;) {
                try {
                    ret = fjar.get();
                    break;
                } catch (InterruptedException ex) {
                    interrupted = true;
                } catch (ExecutionException ex) {
                    Throwable cause = ex.getCause();
                    if (cause instanceof IOException) {
                        // This is important for telling general IOException from ZipException
                        // down the stack.
                        throw (IOException)cause;
                    } else if (cause instanceof ThreadDeath) {
                        throw (ThreadDeath) cause; // #201098
                    } else {
                        throw new IOException(cause);
                    }
                }
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
            return ret;
        }

        private void doCloseJar() throws IOException {
            JarFile jar = null;
            synchronized(sources) {
                if (fjar != null) {
                    jar = callGet();
                    if (sources.remove(this) == null) {
                        LOGGER.warning("Can't remove " + this);
                    }
                    LOGGER.log(Level.FINE, "Closing JAR {0}", jar.getName());
                    fjar = null;
                    LOGGER.log(Level.FINE, "Remaining open JARs: {0}", sources.size());
                }
            }
            if (jar != null) jar.close();
        }

        /** Delete any temporary JARs we were holding on to.
         * Also close any other JARs in our list.
         */
        @Override
        protected void finalize() throws Throwable {
            super.finalize();
            
            doCloseJar();

            if (dead) {
                LOGGER.log(Level.FINE, "#21114: closing and deleting temporary JAR {0}", file);
                if (file.isFile() && !file.delete()) {
                    LOGGER.log(Level.FINE, "(but failed to delete {0})", file);
                }
            }
        }

        // JarFile pool tracking
        private static final Map<JarSource, Future<JarFile>> sources = new HashMap<JarSource, Future<JarFile>>();
        private static int LIMIT = Integer.getInteger("org.netbeans.JarClassLoader.limit_fd", 300);

        static void opened(JarSource source, String forWhat) {
            synchronized (sources) {
                if (sources.size() > LIMIT) {
                    // close something
                    JarSource toClose = toClose(source);
                    try {
                        toClose.doCloseJar();
                    } catch (IOException ioe) {
                        LOGGER.log(Level.INFO, "closing " + toClose, ioe);
                    }
                }
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.log(Level.FINE, "Opening module JAR {0} for {1}", new Object[] {source.file, forWhat});
                    LOGGER.log(Level.FINE, "Currently open JARs: {0}", sources.size());
                }
            }
        }

        // called under lock(sources) 
        private static JarSource toClose(JarSource notThisOne) {
            assert Thread.holdsLock(sources);
             
            int min = Integer.MAX_VALUE; 
            JarSource candidate = null; 
            for (JarSource act : sources.keySet()) {
                // aging: slight exponential decay of all opened sources?
                act.requests = 5*act.requests/6;
                
                if (act.used > 0) continue;
                if (act.requests < min) { 
                    min = act.requests; 
                    candidate = act; 
                } 
            } 
             
            assert candidate != null;
            assert candidate != notThisOne : "Closing just opened JarSource: " + notThisOne;
            return candidate; 
        }

        @Override
        public String getIdentifier() {
            String tmp = getURL().toExternalForm();
            if (tmp.startsWith("jar:file:") && tmp.endsWith("!/")) {
                String path = tmp.substring(9, tmp.length() - 2).replace("%20", " ");
                if (BaseUtilities.isWindows()) {
                    if (path.startsWith("/")) { // NOI18N
                        path = path.substring(1);
                    }
                    path = path.replace('/', File.separatorChar);
                }
                return Stamps.findRelativePath(path) + "!/";
            }
            return tmp;
        }
    }

    static class DirSource extends Source {
        File dir;
        Manifest manifest;
        
        DirSource(File file) throws MalformedURLException {
            super(BaseUtilities.toURI(file).toURL());
            dir = file;
        }

        @Override
        public Manifest getManifest() {
            Manifest mf = manifest;
            if (mf != null) {
                return mf;
            }
            File maniF = new File(new File(dir, "META-INF"), "MANIFEST.MF");
            mf = new Manifest();
            if (maniF.canRead()) {
                try (InputStream istm = new FileInputStream(maniF)) {
                    mf.read(istm);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return manifest = mf;
        }
        
        public String getPath() {
            return dir.getPath();
        }

        protected URL doGetResource(String name) throws MalformedURLException {
            File resFile = new File(dir, name);
            return resFile.exists() ? BaseUtilities.toURI(resFile).toURL() : null;
        }
        
        protected byte[] readClass(String path) throws IOException {
            File clsFile = new File(dir, path.replace('/', File.separatorChar));
            if (!clsFile.exists()) return null;
            
            int len = (int)clsFile.length();
            byte[] data = new byte[len];
            InputStream is = new FileInputStream(clsFile);
            try {
                int count = 0;
                while (count < len) {
                    count += is.read(data, count, len - count);
                }
                return data;
            } finally {
                is.close();
            }
        }
        
        protected void listCoveredPackages(Set<String> known, StringBuffer save) {
            appendAllChildren(known, save, dir, "");
        }
        
        private static void appendAllChildren(Set<String> known, StringBuffer save, File dir, String prefix) {
            boolean populated = false;
            for (File f : dir.listFiles()) {
                if (f.isDirectory()) {
                    appendAllChildren(known, save, f, prefix + f.getName() + '.');
                } else {
                    if (prefix.length() == 0) {
                        // resource in default package
                        String res = "default/" + f.getName();
                        if (known.add(res)) {
                            save.append(res).append(',');
                        }
                        continue;
                    }
                    populated = true;
                    if (prefix.startsWith("META-INF.")) {
                       String res = prefix.substring(8).replace('.', '/').concat(f.getName());
                       if (known.add(res)) save.append(res).append(',');
                    }
                }
            }
            if (populated) {
                String pkg = prefix;
                if (pkg.endsWith(".")) pkg = pkg.substring(0, pkg.length()-1);
                if (known.add(pkg)) save.append(pkg).append(',');
            }
        }
    }
    
    private static Iterable<String> getCoveredPackages(Module mod, Source[] sources) {
        if (mod != null) {
            Set<String> ret = mod.getCoveredPackages();
            if (ret != null) {
                return ret;
            }
        }
        
        Set<String> known = new HashSet<String>();
        Manifest m = mod == null ? null : mod.getManifest();
        if (m != null) {
            Attributes attr = m.getMainAttributes();
            String pack = attr.getValue("Covered-Packages"); // NOI18N
            if (pack != null) {
                known.addAll(Arrays.asList(pack.split(",", -1)));
                mod.registerCoveredPackages(known);
                return known;
            }
        }
        
        // not precomputed/cached, analyze
        StringBuffer save = new StringBuffer();
        for (Source s : sources) s.listCoveredPackages(known, save);

        if (save.length() > 0) save.setLength(save.length()-1);
        if (mod != null) {
            mod.registerCoveredPackages(known);
        }
        return known;
    }
    
    static class JarURLStreamHandler extends URLStreamHandler {

        private static final URLStreamHandler fallback = new URLStreamHandler() {
            protected @Override URLConnection openConnection(URL u) throws IOException {
                return new URL(u.toString()).openConnection();
            }
        };

        private final URLStreamHandler originalJarHandler;
        private ClassLoader loader;

        JarURLStreamHandler(URLStreamHandler originalJarHandler) {
            this.originalJarHandler = originalJarHandler;
        }

        private JarURLStreamHandler(ClassLoader l) {
            this(fallback);
            this.loader = l;
        }

        /**
         * Creates URLConnection for URL with res protocol.
         * @param u URL for which the URLConnection should be created
         * @return URLConnection
         * @throws IOException
         */
        @Override
        protected JarURLConnection openConnection(URL u) throws IOException {
            String url = u.getFile();//toExternalForm();
            int bang = url.indexOf("!/");
            if (bang == -1) {
                throw new IOException("Malformed JAR-protocol URL: " + u);
            }
            String filePath = url.substring(0, bang);
            String jar;
            AGAIN: for (;;) try {
                final URI uri = new URI(filePath);
                if (uri.getScheme().equals("file")) {
                    jar = BaseUtilities.toFile(uri).getPath();
                } else {
                    jar = null;
                }
                break;
            } catch (URISyntaxException x) {
                if (filePath.contains(" ")) {
                    filePath = filePath.replace(" ", "%20");
                    continue;
                }
                throw new IOException(x);
            }
            Source _src = Source.sources.get(jar);
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINER, "openConnection for {0} jar: {1} src: {2}", new Object[]{u, jar, _src});
            }
            if (_src == null) {
                try {
                    Method m = URLStreamHandler.class.getDeclaredMethod("openConnection", URL.class);
                    m.setAccessible(true);
                    JarURLConnection ret = (JarURLConnection) m.invoke(originalJarHandler, u);
                    if (LOGGER.isLoggable(Level.FINER)) {
                        LOGGER.log(Level.FINER, "Calling original {0} yields {1}", new Object[]{originalJarHandler, ret});
                    }
                    return ret;
                } catch (Exception e) {
                    throw (IOException) new IOException(e.toString()).initCause(e);
                }
            }
            String _name = url.substring(bang + 2);
            try {
                _name = new URI(_name).getPath();
            } catch (URISyntaxException x) {
                throw (IOException) new IOException("Decoding " + u + ": " + x).initCause(x);
            }
            if (LOGGER.isLoggable(Level.FINER)) {
                LOGGER.log(Level.FINER, "creating NbJarURLConnection({0},{1},{2})", new Object[]{u, _src, _name});
            }
            return new NbJarURLConnection (u, _src, _name, loader);
        }
        
        @Override
        protected void parseURL(URL u, String spec, int start, int limit) {
            if (spec.startsWith("/")) {
                setURL(
                    u, "jar", u.getHost(), u.getPort(), 
                    u.getAuthority(), u.getUserInfo(), 
                    u.getFile().replaceFirst("!/.*$", "!" + spec), // NOI18N
                    u.getQuery(), u.getRef()
                ); 
            } else {
                super.parseURL(u, spec, start, limit);
            }
        }

    }

    /** URLConnection for URL with res protocol.
     *
     */
    private static class NbJarURLConnection extends JarURLConnection {
        private JarSource src;
        private final String name;
        private byte[] data;
        private InputStream iStream;
        private final ClassLoader loader;

        /**
         * Creates new URLConnection
         * @param url the parameter for which the connection should be
         * created
         */
        private NbJarURLConnection(URL url, Source src, String name, ClassLoader l) throws MalformedURLException {
            super(url);
            this.src = (JarSource)src;
            this.name = name;
            this.loader = l;
        }

        private boolean isFolder() {
            return name.length() == 0 || name.endsWith("/");
        }

        public void connect() throws IOException {
            if (isFolder()) {
                return; // #139087: odd but harmless
            }
            if (data == null) {
                data = src.getClassData(name);
                if (data == null) {
                    throw new FileNotFoundException(getURL().toString());
                }
            }
        }

        @Override
        public long getLastModified() {
            return Stamps.getModulesJARs().lastModified();
        }

        @Override
        public String getContentType() {
            String contentType = guessContentTypeFromName(name);
            if (contentType == null) {
                contentType = "content/unknown";
            }
            return contentType;
        }

        public @Override int getContentLength() {
            if (isFolder()) {
                return -1;
            }
            try {
                this.connect();
                return data.length;
            } catch (IOException e) {
                return -1;
            }
        }


        public @Override InputStream getInputStream() throws IOException {
            if (isFolder()) {
                throw new IOException("Cannot open a folder"); // NOI18N
            }
            this.connect();
            if (iStream == null) iStream = new ByteArrayInputStream(data);
            return iStream;
        }

        @Override
        public JarFile getJarFile() throws IOException {
            return new JarFile(src.file); // #134424
        }

        @SuppressWarnings("rawtypes")
        public @Override Object getContent(Class[] classes) throws IOException {
            if (Arrays.asList(classes).contains(ClassLoader.class)) {
                return loader;
            } else {
                return super.getContent(classes);
            }
        }
    }
}
