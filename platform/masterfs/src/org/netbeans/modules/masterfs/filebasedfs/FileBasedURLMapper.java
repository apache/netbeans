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

package org.netbeans.modules.masterfs.filebasedfs;

import java.net.URISyntaxException;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.BaseFileObj;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.RootObj;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

public final class FileBasedURLMapper extends URLMapper {
    private static final Logger LOG = Logger.getLogger(FileBasedURLMapper.class.getName());
    
    @Override
    public final URL getURL(final FileObject fo, final int type) {
        if (type == URLMapper.NETWORK) {
            return null;
        }
        URL retVal = null;
        try {
            if (fo instanceof BaseFileObj)  {
                final BaseFileObj bfo = (BaseFileObj) fo;
                retVal = FileBasedURLMapper.fileToURL(bfo.getFileName().getFile(), fo);
            } else if (fo instanceof RootObj<?>) {
                final RootObj<?> rfo = (RootObj<?>) fo;
                return getURL(rfo.getRealRoot(), type);                
            }
        } catch (MalformedURLException e) {
            retVal = null;
        }
        return retVal;
    }

    public final FileObject[] getFileObjects(final URL url) {
        if (!"file".equals(url.getProtocol())) {  //NOI18N
            return null;
        }
        // return null for UNC root
        if(url.getPath().equals("//") || url.getPath().equals("////")) {  //NOI18N
            return null;
        }
        //TODO: review and simplify         
        FileObject retVal = null;
        File file;
        try {
            file = FileUtil.normalizeFile(BaseUtilities.toFile(url.toURI()));
        } catch (URISyntaxException e) {
            LOG.log(Level.INFO, "URL=" + url, e); // NOI18N
            return null;
        } catch (IllegalArgumentException iax) {
            LOG.log(Level.INFO, "URL=" + url, iax); // NOI18N
            return null;
        }
        
        retVal = FileBasedFileSystem.getFileObject(file, FileObjectFactory.Caller.ToFileObject);
        return new FileObject[]{retVal};
    }

    private static URL fileToURL(final File file, final FileObject fo) throws MalformedURLException {
        URL retVal = toURI(file, fo.isFolder()).toURL();
        if (fo.isFolder()) {
            // #155742 - URL for folder must always end with slash
            final String urlDef = retVal.toExternalForm();
            final String pathSeparator = "/";//NOI18N
            if (!urlDef.endsWith(pathSeparator)) {
                retVal = new URL(urlDef + pathSeparator);
            }
        }
        return retVal;
    }
    /** {@link BaseUtilities#toURI} replacement.
     * #171330: we know whether given
     * FileObject is a file or folder, so we can eliminate {@link File#isDirectory}
     * disk touch which is needed otherwise.
     * Might be useful as an API method.
     */
    private static URI toURI(final File file, boolean isDirectory) {
        return toURI(file.getAbsolutePath(), isDirectory, File.separatorChar);
    }
    
    static URI toURI(String path, boolean isDirectory, char separator) {
        String sp = slashify(path, isDirectory, separator);
        try {
            return new URI("file", null, sp, null);  //NOI18N
        } catch (URISyntaxException x) {
            try {
                //can be a path in form "\\wsl$\<server>\", '$' not allowed
                //in the host part, put everything into path
                //note this URI is *not* normalized:
                return new URI("file", null, "//" + sp, null);  //NOI18N
            } catch (URISyntaxException ex) {
                throw new Error(x);		// Can't happen
            }
        }
    }

    private static String slashify(String p, boolean isDirectory, char separatorChar) {
        if (separatorChar != '/') {  //NOI18N
            p = p.replace(separatorChar, '/');  //NOI18N
        }
        if (!p.startsWith("/")) {  //NOI18N
            p = "/" + p;  //NOI18N
        }
        if (!p.endsWith("/") && isDirectory) {  //NOI18N
            p = p + "/";  //NOI18N
        }
        return p;
    }
}
