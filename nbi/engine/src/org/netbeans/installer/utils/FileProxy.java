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

package org.netbeans.installer.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.utils.helper.ExtendedUri;

/**
 *
 * @author Danila_Dugurov
 */
//todo: This class already ready to die.
public class FileProxy {
    private static final String RESOURCE_SCHEME = "resource";
    public static final String RESOURCE_SCHEME_PREFIX = RESOURCE_SCHEME + ":";
    
    private final File tmpDir = new File(System.getProperty("java.io.tmpdir"), "tmp");
    private final Map<String, File> cache = new HashMap<String, File>();
    {
        tmpDir.mkdirs();
        tmpDir.deleteOnExit();
    }
    
    public static final FileProxy proxy = new FileProxy();
    
    public static FileProxy getInstance() {
        return proxy;
    }
    
    public void deleteFile(String uri) throws IOException {
        final File file = cache.get(uri);
        if (uri != null && uri.startsWith("file")) return;
        if (file != null) FileUtils.deleteFile(file);
        cache.remove(uri);
    }
    
    public void deleteFile(ExtendedUri uri) throws IOException {
         if ((uri.getLocal() != null) &&
                        !uri.getLocal().equals(uri.getRemote()) &&
                        !uri.getAlternates().contains(uri.getLocal())) {
             deleteFile(uri.getRemote());
             uri.setLocal(null);
         }
     }
    public void deleteFile(URI uri) throws IOException {
        deleteFile(uri.toString());
    }
    
    public void deleteFile(URL url) throws IOException {
        deleteFile(url.toString());
    }
    
//    public File getFile(URL url) throws DownloadException {
//        return getFile(url, null, false);
//    }
    
    public File getFile(String uri) throws DownloadException {
        return getFile(uri, null, null);
    }
    
    public File getFile(String uri, boolean deleteOnExit) throws DownloadException {
        return getFile(uri, null, null, deleteOnExit);
    }
    
    public File getFile(ExtendedUri uri, Progress progress) throws DownloadException {
        return getFile(uri.getRemote(), progress, null, false);
    }
    
    public File getFile(String uri, ClassLoader loader) throws DownloadException {
        return getFile(uri, null, loader);
    }
    public File getFile(String uri, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
        return getFile(uri, null, loader, deleteOnExit);
    }
    public File getFile(URI uri, Progress progress)  throws DownloadException {
        return getFile(uri, progress, null, false);
    }
    
    public File getFile(String uri, Progress progress, ClassLoader loader) throws DownloadException{
        return getFile(uri, progress, loader, false);
    }
    
    public File getFile(String uri, Progress progress, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
        final URI myUri;
        try {
            myUri = new URI(uri);
        } catch (URISyntaxException ex) {
            throw new DownloadException("uri:" + uri, ex);
        }
        return getFile(myUri, progress, loader, deleteOnExit);
    }
    
    public File getFile(URI uri, boolean deleteOnExit) throws DownloadException {
        return getFile(uri, null, null, deleteOnExit);
    }
    
    public File getFile(URI uri) throws DownloadException {
        return getFile(uri, null, null, false);
    }
    
    public File getFile(URI uri, Progress progress, ClassLoader loader, boolean deleteOnExit) throws DownloadException {
        final String cacheKey = uri.toString() + 
                (loader != null ? "#" + loader.toString() : "");
        
        if (cache.containsKey(cacheKey) && cache.get(cacheKey).exists()) {
            return cache.get(cacheKey);
        }
        if (uri.getScheme().equals("file")) {
            File file = new File(uri);
            if (!file.exists()) throw new DownloadException("file not exist: " + uri);
            return file;
        } else if (uri.getScheme().equals(RESOURCE_SCHEME)) {
            OutputStream out  = null;
            try {
                String path = uri.getSchemeSpecificPart();
                File file = new File(tmpDir, path.substring(path.lastIndexOf('/')));
                String fileName = file.getName();
                File parent = file.getParentFile();
                for (int i = 0; file.exists(); i++) {
                    file = new File(parent, fileName + "." + i);
                }
                file.createNewFile();
                if (deleteOnExit) {
                    file.deleteOnExit();
                }
                final InputStream resource = (loader != null ? loader: getClass().getClassLoader()).getResourceAsStream(uri.getSchemeSpecificPart());
                out = new FileOutputStream(file);
                if (resource == null) throw new DownloadException(RESOURCE_SCHEME_PREFIX + uri.getSchemeSpecificPart() + " not found");
                StreamUtils.transferData(resource, out);
                cache.put(cacheKey, file);
                return file;
            } catch(IOException ex) {
                throw new DownloadException("I/O error has occures", ex);
            } finally {
                if (out != null)
                    try {
                        out.close();
                    } catch (IOException ignord) {}
            }
        } else if (uri.getScheme().startsWith("http")) {
            throw new DownloadException("unsupported sheme: " + uri.getScheme());
//            try {
//                final File file = getFile(uri.toURL(), progress, deleteOnExit);
//                cache.put(cacheKey, file);
//                return file;
//            } catch(MalformedURLException ex) {
//                throw new DownloadException("malformed url: " + uri, ex);
//            }
        }
        throw new DownloadException("unsupported sheme: " + uri.getScheme());
    }
    
//    protected File getFile(final URL url, final Progress progress, boolean deleteOnExit) throws DownloadException {
//        try {
//            final DownloadProgress dlProgress = new DownloadProgress(progress, url);
//            DownloadManager.instance.registerListener(dlProgress);
//            File file = null;
//            file = FileProvider.getProvider().get(url);
//            if (deleteOnExit) file.deleteOnExit();
//            return file;
//        } catch (DownloadException e) {
//            if (UiMode.getCurrentUiMode() == UiMode.SWING) {
//                new ProxySettingsDialog().execute();
//                return getFile(url, progress, deleteOnExit);
//            } else {
//                throw e;
//            }
//        }
//    }
}
