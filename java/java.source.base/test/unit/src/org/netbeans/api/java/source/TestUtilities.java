/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.api.java.source;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import org.netbeans.api.annotations.common.CheckForNull;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.source.indexing.TransactionContext;
import org.netbeans.modules.java.source.usages.BinaryAnalyser;
import org.netbeans.modules.java.source.usages.ClassIndexImpl;
import org.netbeans.modules.java.source.usages.ClassIndexManager;
import org.netbeans.modules.java.source.usages.IndexUtil;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Utilities;

/**
 * Utilities to aid unit testing java.source module.
 *
 * @author Jaroslav Tulach
 * @author Tom Ball
 * @author Tomas Zezula
 */
public final class TestUtilities {
    
    private static final String EXPLICIT_JDK9_HOME = null;
    private static final String PROP_JDK9_HOME = "jdk9.home";   //NOI18N
    
    // do not instantiate
    private TestUtilities() {}
    
    /**
     * Waits for the end of the background scan, this helper method 
     * is designed for tests which require to wait for the end of initial scan.
     * The method can be used as a barrier but it is not guaranteed that the
     * background scan will not start again after return from this method, the
     * test is responsible for it itself. In general it's safer to use {@link JavaSource#runWhenScanFinished}
     * method and do the critical action inside the run method.
     * @param timeout the maximum time to wait
     * @param unit the time unit of the timeout argument
     * @return true if the scan finished, false when the timeout elapsed before the end of the scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     */
    public static boolean waitScanFinished (final long timeout, final TimeUnit unit) throws InterruptedException {
        assert unit != null;
        final ClasspathInfo cpInfo = ClasspathInfo.create(ClassPathSupport.createClassPath(new URL[0]),
                ClassPathSupport.createClassPath(new URL[0]), null);
        assert cpInfo != null;
        final JavaSource js = JavaSource.create(cpInfo);
        assert js != null;
        try {
            Future<Void> future = js.runWhenScanFinished(new Task<CompilationController>() {
                public void run(CompilationController parameter) throws Exception {
                }
            }, true);
            future.get(timeout,unit);
            return true;
        } catch (IOException ioe) {
            //Actually never thrown
        }
        catch (ExecutionException ee) {
            //Actually never thrown
        }
        catch (TimeoutException timeoutEx) {
        }
        return false;
    }
    
//    /**
//     * Disables use of {@link LibraryManager} in the {@link GlobalSourcePath}. The tests
//     * which don't register {@link LibraryProvider} or {@link LibraryTypeProvider} may
//     * use this method to disable use of {@link LibraryManager} in the {@link GlobalSourcePath}.
//     * @param use false value disables use of {@link LibraryManager}
//     */
//    public static void setUseLibraries (final boolean use) {
//        //GlobalSourcePathTestUtil.setUseLibraries(use);
//        // IMO this in fact did nothing... If your tests are failing due to this
//        // please file a defect on editor/parsing & indexing and we will try to help.
//    }
    
    /**
     * Sets a root folder of the java source caches. This method may be used by tests
     * which need to do an initial compilation, they require either {@link ClassIndex} or
     * need to work with a group of related java files.
     * @param cacheFolder the folder used by java infrastructure as a cache,
     * has to exist and must be a folder.
     */
    public static void setCacheFolder (final File cacheFolder) {
        IndexUtil.setCacheFolder(cacheFolder);
    }
    
    /**
     * Returns a string which contains the contents of a file.
     *
     * @param f the file to be read
     * @return the contents of the file(s).
     */
    public static final String copyFileToString (java.io.File f) throws java.io.IOException {
        int s = (int)f.length ();
        byte[] data = new byte[s];
        int len = new FileInputStream (f).read (data);
        if (len != s)
            throw new EOFException("truncated file");
        return new String (data);
    }
    
    /**
     * Returns a string which contains the contents of a GZIP compressed file.
     *
     * @param f the file to be read
     * @return the contents of the file(s).
     */
    public static final String copyGZipFileToString (java.io.File f) throws java.io.IOException {
        GZIPInputStream is = new GZIPInputStream(new FileInputStream(f));
        byte[] arr = new byte[256 * 256];
        int first = 0;
        for(;;) {
            int len = is.read(arr, first, arr.length - first);
            if (first + len < arr.length) {
                return new String(arr, 0, first + len);
            }
        }
    }
    
    /**
     * Copies a string to a specified file.
     *
     * @param f the file to use.
     * @param content the contents of the returned file.
     * @return the created file
     */
    public static final File copyStringToFile (File f, String content) throws Exception {
        FileOutputStream os = new FileOutputStream(f);
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        FileUtil.copy(is, os);
        os.close ();
        is.close();
            
        return f;
    }
    
    /**
     * Copies a string to a specified file.
     *
     * @param f the {@link FilObject} to use.
     * @param content the contents of the returned file.
     * @return the created file
     */
    public static final FileObject copyStringToFile (FileObject f, String content) throws Exception {
        OutputStream os = f.getOutputStream();
        InputStream is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
        FileUtil.copy(is, os);
        os.close ();
        is.close();
            
        return f;
    }   

    private static final ClassPath EMPTY = ClassPathSupport.createClassPath(new URL[0]);
    
    /**
     * Prepare Java caches for given binary roots.
     * 
     * @param urls to analyze
     */
    public static final void analyzeBinaries(final Collection<URL> urls) throws IOException {
        final ClasspathInfo cpInfo = ClasspathInfo.create(EMPTY, EMPTY, EMPTY);
        final ClassIndexManager mgr  = ClassIndexManager.getDefault();
        final JavaSource js = JavaSource.create(cpInfo);
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController parameter) throws Exception {                
                for (final URL url : urls) {
                    TransactionContext ctx = TransactionContext.beginStandardTransaction(url, false, ()->true, false);
                    try {
                        final ClassIndexImpl cii = mgr.createUsagesQuery(url, false);
                        final BinaryAnalyser ba = cii.getBinaryAnalyser();            
                        ba.analyse(url);
                    } finally {
                        ctx.commit();
                    }
                }
            }
        }, true);
    }
    
    /**
     * Returns a home of the JDK 9.
     * The JDK 9 home is resolved in the following order:
     * <ol>
     *  <li>Explicitely given by {@link TestUtilities#EXPLICIT_JDK9_HOME}</li>
     *  <li>Given by system property TestUtilities#PROP_JDK9_HOME}</li>
     *  <li>Tests run on JDK 9</li>
     * </ol>     
     * @return the JDK 9 home or null if not found
     * @throws IOException in case of invalid (non existent) JDK 9 home
     */
    @CheckForNull
    public static File getJava9Home() throws IOException {
        File java9Home = null;
        if (EXPLICIT_JDK9_HOME != null) {
            java9Home = FileUtil.normalizeFile(new File(EXPLICIT_JDK9_HOME));
            if (java9Home.isDirectory()) {
                return java9Home;
            } else {
                throw new IOException(String.format(
                        "The jdk 9 home: %s does not exist.",   //NOI18N
                        java9Home.getAbsolutePath()));
            }
        }
        String propVal;
        if ((propVal = System.getProperty(PROP_JDK9_HOME)) != null) {
            java9Home = FileUtil.normalizeFile(new File(propVal));
            if (java9Home.isDirectory()) {
                return java9Home;
            } else {
                throw new IOException(String.format(
                        "The jdk 9 home: %s does not exist.",   //NOI18N
                        java9Home.getAbsolutePath()));
            }
        }
        propVal = System.getProperty("java.specification.version");   //NOI18N
        if (new SpecificationVersion("9").compareTo(new SpecificationVersion(propVal)) <= 0) {  //NOI18N
            propVal = System.getProperty("java.home");  //NOI18N
            if (propVal != null) {
                java9Home = FileUtil.normalizeFile(new File(propVal));
                if (java9Home.isDirectory()) {
                    return java9Home;
                } else {
                    throw new IOException(String.format(
                            "The jdk 9 home: %s does not exist.",   //NOI18N
                            java9Home.getAbsolutePath()));
                }
            }
        }
        return null;
    }
    
    @CheckForNull
    public static Path getJRTFS() throws IOException {
        final File java9 = getJava9Home();
        if (java9 == null) {
            return null;
        }
        final File jrtFsProvider = new File(java9,"jrt-fs.jar"); //NOI18N
        if (jrtFsProvider.exists() && jrtFsProvider.isFile() && jrtFsProvider.canRead()) {
            final ClassLoader cl = new URLClassLoader(new URL[]{
                BaseUtilities.toURI(jrtFsProvider).toURL()
            });
            final ServiceLoader<FileSystemProvider> sl = ServiceLoader.load(FileSystemProvider.class, cl);
            FileSystemProvider jrtp = null;
            for (FileSystemProvider fsp : sl) {
                if ("jrt".equals(fsp.getScheme())) {    //NOI18N
                    jrtp = fsp;
                    break;
                }
            }
            if (jrtp != null) {
                return jrtp.getPath(URI.create("jrt:/"));   //NOI18N
            }
        }
        return null;
    }
    
}
