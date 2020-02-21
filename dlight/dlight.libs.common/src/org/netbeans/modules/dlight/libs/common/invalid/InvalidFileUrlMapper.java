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
package org.netbeans.modules.dlight.libs.common.invalid;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import org.netbeans.modules.dlight.libs.common.InvalidFileObjectSupport;
import org.netbeans.modules.dlight.libs.common.PathUtilities;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;

/**
 *
 */
@org.openide.util.lookup.ServiceProvider(service = org.openide.filesystems.URLMapper.class)
public class InvalidFileUrlMapper extends URLMapper {

    @Override
    public FileObject[] getFileObjects(URL url) {
// Let's better return null - it's more likely that clients process it properly than an invalid
//        if (url.getProtocol().equals(InvalidFileURLStreamHandler.PROTOCOL)) {
//            String path = unescapePath(url);
//            FileObject fo = InvalidFileObjectSupport.getInvalidFileObject(InvalidFileObjectSupport.getDummyFileSystem(), path);
//            if (fo != null) {
//                return new FileObject[] { fo };
//            }
//        }
        return null;
    }

    @Override
    public URL getURL(FileObject fo, int type) {
        if (fo instanceof InvalidFileObject) {            
            try {
                // If we create an "invlalid://..." URL (which I tried to do first of all)
                // and somebody saves it and later on tries to get file object - it will get an invalid one
                // even if the file is already created.
                // So we'll try creating a "real" URL
                FileSystem fs = fo.getFileSystem();
                String root;
                if (fs == InvalidFileObjectSupport.getDummyFileSystem()) {
                    root = InvalidFileURLStreamHandler.PROTOCOL_PREFIX;
                } else {                    
                    root = fs.getRoot().toURL().toExternalForm();
                }
                String path = PathUtilities.escapePathForUseInURL(fo.getPath());
                String res;
                if (root.endsWith("/")) { // NOI18N
                    res = root + (path.startsWith("/") ? path.substring(1) : path); // NOI18N
                } else {
                    res = root + (path.startsWith("/") ? "" : "/") + path; // NOI18N
                }                
                return new URL(res);
            } catch (MalformedURLException | FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }    
    
//    private static String unescapePath(URL url) {
//        String path = url.getFile();
//        if (path.contains("%")) { //NOI18N
//            try {
//                return url.toURI().getPath();
//            } catch (URISyntaxException ex) {
//                Exceptions.printStackTrace(ex);
//            }
//        }
//        return path;
//    }
    
}
