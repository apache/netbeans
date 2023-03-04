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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.lookup.ServiceProvider;

/**
 * URLMapper for the nbinst URL protocol.
 * The mapper handles only the translation from URL into FileObjects.
 * The opposite conversion is not needed, it is handled by the default URLMapper.
 * The format of the nbinst URL is nbinst://host/path.
 * The host part is optional, if presents it specifies the name of the supplying module.
 * The path is mandatory and specifies the relative path from the ${netbeans.home}, ${netbeans.user}
 * or ${netbeans.dirs}.
 * @author  Tomas Zezula
 */
@ServiceProvider(service=URLMapper.class)
public class NbinstURLMapper extends URLMapper {
    
    public static final String PROTOCOL = "nbinst";     //NOI18N
    private static final Logger LOG = Logger.getLogger(NbinstURLMapper.class.getName());
    
    /** Creates a new instance of NbInstURLMapper */
    public NbinstURLMapper() {
    }

    /**
     * Returns FileObjects for given URL
     * @param url the URL for which the FileObjects should be find.
     * @return FileObject[], returns null in case of unknown protocol.
     */
    public @Override FileObject[] getFileObjects(URL url) {
        if (PROTOCOL.equals(url.getProtocol())) {
            File f = decodeURL(url);
            if (f != null) {
                FileObject fo = FileUtil.toFileObject(f);
                if (fo != null) {
                    return new FileObject[] {fo};
                } else {
                    LOG.log(Level.WARNING, "could find no FileObject for {0}", f);
                }
            }
        }
        return null;
    }

    /**
     * Returns null, the translation into URL is doen by default URLMapper
     */
    public @Override URL getURL(FileObject fo, int type) {
        return null;
    }

    /**
     * Resolves the nbinst URL into a disk file.
     * @param url to be resolved
     * @return corresponding file, returns null if unknown url protocol.
     */
    static File decodeURL (URL url) {
        assert url != null;
        try {
            URI uri = new URI (url.toExternalForm());
            String protocol = uri.getScheme();
            if (PROTOCOL.equals(protocol)) {
                String module = uri.getAuthority(); // URI.host is null if CNB contains '_'
                String path = uri.getPath();
                if (path.length()>0) {
                    String relpath = path.substring(1).replaceFirst("/$", ""); // NOI18N
                    return InstalledFileLocator.getDefault().locate(relpath, module, false);
                }
            }
        } catch (URISyntaxException x) {
            LOG.log(Level.WARNING, null, x);
        }
        return null;
    }

}
