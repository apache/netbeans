/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.test;

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
