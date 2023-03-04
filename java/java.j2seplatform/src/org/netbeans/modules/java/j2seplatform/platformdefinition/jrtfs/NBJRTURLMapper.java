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

package org.netbeans.modules.java.j2seplatform.platformdefinition.jrtfs;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Pair;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=URLMapper.class)
public class NBJRTURLMapper extends URLMapper {

    private static final Logger LOG = Logger.getLogger(NBJRTURLMapper.class.getName());

    @Override
    public URL getURL(FileObject fo, int type) {
        if (type == NETWORK)
            return null;

        try {
            if (fo.getFileSystem() instanceof NBJRTFileSystem) {
                String path = fo.getPath();
                if (fo.isFolder() && !path.isEmpty()) {
                    path += "/";    //NOI18N
                }
                //URI.resolve does not work as the nbjrt URI is opaque
                return new URL(String.format(
                    "%s%s", //NOI18N
                    ((NBJRTFileSystem) fo.getFileSystem()).getRootURL(),
                    path));
            }
        } catch (IOException | URISyntaxException ex) {
            LOG.log(Level.FINE, null, ex);
        }
        return null;
    }

    @Override
    public FileObject[] getFileObjects(URL url) {
        final Pair<URL,String> parsed = NBJRTUtil.parseURL(url);
        if (parsed != null) {
            final URL root = parsed.first();
            final String pathInImage = parsed.second();
            FileSystem fs = NBJRTFileSystemProvider.getDefault().getFileSystem(root);
            if (fs != null) {
                final FileObject fo = fs.getRoot().getFileObject(pathInImage);
                if (fo != null) {
                    return new FileObject[]{
                        fo
                    };
                }
            }
        }
        return null;
    }

}
