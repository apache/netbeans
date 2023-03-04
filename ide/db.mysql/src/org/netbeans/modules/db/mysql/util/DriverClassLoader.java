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

package org.netbeans.modules.db.mysql.util;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;

/**
 * A classloader that uses the search path for a JDBCDriver to build
 * a classloader that can load the Driver class 
 * 
 * @author David Van Couvering
 */
public class DriverClassLoader extends URLClassLoader {
    private static Logger LOGGER = 
            Logger.getLogger(DriverClassLoader.class.getName());
    
    public DriverClassLoader(JDBCDriver driver) {
        super(new URL[] {});
        
        // Go through the URLs, and if any of them have the nbinst: protocol,
        // convert these to full-path URLs usable by the classloader
        URL[] urls = driver.getURLs();
        
        for ( URL url : urls ) {
            if ("nbinst".equals(url.getProtocol())) { // NOI18N
                // try to get a file: URL for the nbinst: URL
                FileObject fo = URLMapper.findFileObject(url);
                if (fo == null) {
                    LOGGER.log(Level.WARNING, 
                        "Unable to find file object for driver url " + url);
                    continue;
                }
                
                URL localURL = URLMapper.findURL(fo, URLMapper.EXTERNAL);
                if (localURL == null) {
                    LOGGER.log(Level.WARNING, 
                        "Unable to get file url for nbinst url " + url);
                    continue;
                }
                
                super.addURL(localURL);
            } else {
                super.addURL(url);
            }
        }                                       
    }

        
    protected PermissionCollection getPermissions(CodeSource codesource) {
        Permissions permissions = new Permissions();
        permissions.add(new AllPermission());
        permissions.setReadOnly();
        
        return permissions;
    }
    
    public String toString() {
        return "DbURLClassLoader[urls=" + Arrays.asList(getURLs()) + "]"; // NOI18N
    }


}
