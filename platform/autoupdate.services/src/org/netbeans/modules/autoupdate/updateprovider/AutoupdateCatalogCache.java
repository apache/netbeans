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

package org.netbeans.modules.autoupdate.updateprovider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.autoupdate.services.AutoupdateSettings;
import org.openide.filesystems.FileUtil;
import org.openide.modules.Places;
import org.openide.util.Utilities;

/**
 *
 * @author Jiri Rechtacek
 * @author Dmitry Lipin
 */
public final class AutoupdateCatalogCache {
    
    private AutoupdateCatalogCache () {
        cacheDir = Places.getCacheSubdirectory("catalogcache"); // NOI18N
        assert cacheDir != null && cacheDir.exists () : "Cache directory " + cacheDir + " exists.";
        getLicenseDir().mkdirs();
        err.log (Level.FINE, "getCacheDirectory: {0}", cacheDir.getPath ());
    }
    
    private final File cacheDir;
    
    private static AutoupdateCatalogCache INSTANCE;
    
    private static final Logger err = Logger.getLogger (AutoupdateCatalogCache.class.getName());
    
    public static synchronized AutoupdateCatalogCache getDefault () {
        if (INSTANCE == null) {
            INSTANCE = new AutoupdateCatalogCache ();
        }
        return INSTANCE;
    }
    
    private synchronized File getCatalogCache () {
        assert cacheDir != null && cacheDir.exists() : "Cache directory " + cacheDir + " must exist.";
        return cacheDir;
    }
    
    public URL writeCatalogToCache (String codeName, URL original) throws IOException {
            URL url = null;
            File dir = getCatalogCache ();
            assert dir != null && dir.exists () : "Cache directory must exist.";
            File cache = new File (dir, codeName);

            copy(original, cache, false);

            try {
                url = Utilities.toURI (cache).toURL ();
            } catch (MalformedURLException ex) {
                assert false : ex;
            }
            return url;        
    }
    
    public URL getCatalogURL(String codeName) {
        File dir = getCatalogCache();
        File cache = new File(dir, codeName);
        synchronized (getLock(cache)) {
            if (cache.exists()) {
                if (cache.length() == 0) {
                    err.log(Level.INFO, "Cache file {0} exists and of zero size", cache);
                    return null;
                }
                URL url = null;
                try {
                    url = Utilities.toURI(cache).toURL();
                } catch (MalformedURLException ex) {
                    assert false : ex;
                }
                return url;
            } else {
                return null;
            }
        }
    }
    private File getLicenseDir() {
        return new File(getCatalogCache(), "licenses");
    }

    private File getLicenseFile(String name) {
        return new File(getLicenseDir(), name);
    }

    public String getLicense(String name) {
        return getLicense(name, null);
    }

    public String getLicense(String name, URL url) {
        File file = getLicenseFile(name);
        synchronized (name.intern()) {            
            if (!file.exists()) {
                if (url == null) {
                    return null;
                }
                try {
                    copy(url, file, true);
                } catch (IOException e) {
                    // if can`t get the license, treat it as empty but delete it on exit
                    err.log(Level.INFO, "Can`t store license from " + url + " to " + file, e);
                    try {
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                         //in case of error remove the license file and try to download it on the next start
                        file.deleteOnExit();

                    } catch (IOException ex) {
                        err.log(Level.INFO, "Can`t create empty license file", ex);
                    }
                }
            }
            return readLicenseFile(name);
        }
    }

    public void storeLicense(String name, String content) {
        File file = getLicenseFile(name);
        synchronized (name.intern()) {            
            if (file.exists() || content == null) {
                return;
            }
            writeToFile(content, file);
        }
    }
    
    private String readLicenseFile(String name) {
        File file = getLicenseFile(name);
        FileInputStream fr = null;
        synchronized (name.intern()) {
            try {
                fr = new FileInputStream(file);
                byte[] buffer = new byte[8192];
                int n;
                StringBuilder sb = new StringBuilder();
                while ((n = fr.read(buffer)) != -1) {
                    sb.append(new String(buffer, 0, n, "utf-8"));//NOI18N
                }
                return sb.toString();
            } catch (IOException e) {
                err.log(Level.INFO, "Can`t read license from file " + file, e);
                return null;
            } finally {
                if (fr != null) {
                    try {
                        fr.close();
                    } catch (IOException e) {
                        err.log(Level.INFO, "Can`t read close input stream for " + file, e);
                    }
                }
            }
        }
    }
    private void writeToFile(String content, File file) {
        FileOutputStream fw = null;
        try {
            fw = new FileOutputStream(file);
            fw.write(content.getBytes("utf-8")); //NOI18N
        } catch (IOException e) {
            err.log(Level.INFO, "Can`t write to " + file, e);
        } finally {
            if (fw != null) {
                try {
                    fw.flush();
                    fw.close();
                } catch (IOException e) {
                    err.log(Level.INFO, "Can`t output stream for " + file, e);
                }
            }
        }
    }
    
    private void copy (final URL sourceUrl, final File cache, final boolean allowZeroSize) throws IOException {
        // -- create NetworkListener
        // -- request stream
        // -- report success or IOException
        // -- if success then do copy
        
        err.log(Level.FINE, "Processing URL: {0}", sourceUrl); // NOI18N
        
        String prefix = "";
        while (prefix.length () < 3) {
            prefix += cache.getName();
        }
        final File temp = Files.createTempFile(cache.getParentFile ().toPath(), prefix, null).toFile (); //NOI18N
        temp.deleteOnExit();        

        DownloadListener nwl = new DownloadListener(sourceUrl, temp, allowZeroSize);
        
        NetworkAccess.Task task = NetworkAccess.createNetworkAccessTask (sourceUrl, AutoupdateSettings.getOpenConnectionTimeout (), nwl, true);
        task.waitFinished ();
        nwl.notifyException ();
        synchronized(getLock(cache)) {
            updateCachedFile(cache, temp);
            assert cache.exists() : "Cache " + cache + " exists.";
            err.log(Level.FINER, "Cache file {0} was wrote from original URL {1}", new Object[]{cache, sourceUrl});
            if (cache.exists() && cache.length() == 0) {
                err.log(Level.INFO, "Written cache size is zero bytes");
            }
        }
    }

    public String getLock(File cache) {
        return cache.getAbsolutePath().intern();
    }
    public String getLock(URL cache) throws IOException {
        try {
            return getLock(Utilities.toFile(cache.toURI()));
        } catch (URISyntaxException ex) {
            throw new IOException(ex);
        }
    }
    
    private void updateCachedFile(File cache, File temp) {
        if (cache.exists() && !cache.delete()) {
            err.log(Level.INFO, "Cannot delete cache {0}", cache);
            try {
               Thread.sleep(200);
            } catch (InterruptedException ie) {
                assert false : ie;
            }
            cache.delete();
        }

        if (temp.length() == 0) {
            err.log(Level.INFO, "Temp cache size is zero bytes");
        }

        if (!temp.renameTo(cache)) {
            err.log(Level.INFO, "Cannot rename temp {0} to cache {1}", new Object[]{temp, cache});
            err.log(Level.INFO, "Trying to copy {0} to cache {1}", new Object[] {temp, cache});
            try {
                FileOutputStream os = new FileOutputStream(cache);
                FileInputStream is = new FileInputStream(temp);
                FileUtil.copy(is, os);
                os.close();
                is.close();
                temp.delete();
            } catch (IOException ex) {
                err.log(Level.INFO, "Cannot even copy: {0}", ex.getMessage());
                err.log(Level.FINE, null, ex);
            }
        }

        if (cache.exists() && cache.length() == 0) {
            err.log(Level.INFO, "Final cache size is zero bytes");
        }
    }
}
