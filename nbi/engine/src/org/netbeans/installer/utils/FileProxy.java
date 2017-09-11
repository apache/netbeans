/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and Distribution
 * License("CDDL") (collectively, the "License"). You may not use this file except in
 * compliance with the License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html or nbbuild/licenses/CDDL-GPL-2-CP. See the
 * License for the specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header Notice in
 * each file and include the License file at nbbuild/licenses/CDDL-GPL-2-CP.  Oracle
 * designates this particular file as subject to the "Classpath" exception as
 * provided by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the License Header,
 * with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original Software
 * is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun Microsystems, Inc. All
 * Rights Reserved.
 * 
 * If you wish your version of this file to be governed by only the CDDL or only the
 * GPL Version 2, indicate your decision by adding "[Contributor] elects to include
 * this software in this distribution under the [CDDL or GPL Version 2] license." If
 * you do not indicate a single choice of license, a recipient has the option to
 * distribute your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above. However, if
 * you add GPL Version 2 code and therefore, elected the GPL Version 2 license, then
 * the option applies only if the new code is made subject to such option by the
 * copyright holder.
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
import org.netbeans.installer.downloader.DownloadManager;
import org.netbeans.installer.downloader.services.FileProvider;
import org.netbeans.installer.utils.exceptions.DownloadException;
import org.netbeans.installer.downloader.DownloadProgress;
import org.netbeans.installer.utils.helper.UiMode;
import org.netbeans.installer.utils.progress.Progress;
import org.netbeans.installer.downloader.ui.ProxySettingsDialog;
import org.netbeans.installer.utils.helper.ExtendedUri;

/**
 *
 * @author Danila_Dugurov
 */
//todo: This class already ready to die.
public class FileProxy {
    private static final String RESOURCE_SCHEME = "resource";
    public static final String RESOURCE_SCHEME_PREFIX = RESOURCE_SCHEME + ":";
    
    private final File tmpDir = new File(DownloadManager.getInstance().getLocalDirectory(), "tmp");
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
    
    public File getFile(URL url) throws DownloadException {
        return getFile(url, null, false);
    }
    
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
            try {
                final File file = getFile(uri.toURL(), progress, deleteOnExit);
                cache.put(cacheKey, file);
                return file;
            } catch(MalformedURLException ex) {
                throw new DownloadException("malformed url: " + uri, ex);
            }
        }
        throw new DownloadException("unsupported sheme: " + uri.getScheme());
    }
    
    protected File getFile(final URL url, final Progress progress, boolean deleteOnExit) throws DownloadException {
        try {
            final DownloadProgress dlProgress = new DownloadProgress(progress, url);
            DownloadManager.instance.registerListener(dlProgress);
            File file = null;
            file = FileProvider.getProvider().get(url);
            if (deleteOnExit) file.deleteOnExit();
            return file;
        } catch (DownloadException e) {
            if (UiMode.getCurrentUiMode() == UiMode.SWING) {
                new ProxySettingsDialog().execute();
                return getFile(url, progress, deleteOnExit);
            } else {
                throw e;
            }
        }
    }
}
