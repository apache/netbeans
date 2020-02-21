/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
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
