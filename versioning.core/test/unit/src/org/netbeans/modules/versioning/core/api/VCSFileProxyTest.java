/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.versioning.core.api;

import java.io.File;
import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author tomas
 */
public class VCSFileProxyTest extends NbTestCase {

    public VCSFileProxyTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp(); 
        File userdir = new File(getWorkDir(), "userdir");
        userdir.mkdirs();
        System.setProperty("netbeans.user", userdir.getAbsolutePath());
        System.setProperty("org.netbeans.modules.masterfs.watcher.disable", "true");
    }
    
    public void testIsDirectoryFolderFO() throws IOException {
        VCSFileProxy proxy = getFolderProxy("directory" + System.currentTimeMillis());
        assertNotNull(proxy);
        assertTrue(proxy.isDirectory());
        assertFalse(proxy.isFile());
    }
    
    public void testIsDirectoryFileFO() throws IOException {
        VCSFileProxy proxy = getFileProxy("file"+ System.currentTimeMillis());
        assertNotNull(proxy);
        assertFalse(proxy.isDirectory());        
        assertTrue(proxy.isFile());        
    }
    
    public void testCachedIsDirectoryChangedForFolderFO() throws IOException {
        VCSFileProxy proxy = getFolderProxy("something"+ System.currentTimeMillis());
        assertNotNull(proxy);
        assertTrue(proxy.isDirectory());
        assertFalse(proxy.isFile());
        
        // delete folder ...
        File f = proxy.toFile();
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
        FileObject fo = FileUtil.toFileObject(f);
        f.delete();
        assertFalse(f.exists());
        // ... and recreate as file
        f.createNewFile();
        assertTrue(f.exists());
        assertTrue(f.isFile());
        fo.refresh();
        
        assertFalse(proxy.isDirectory());
        assertTrue(proxy.isFile());
    }
    
    public void testCachedIsDirectoryChangedForFileFO() throws IOException {
        VCSFileProxy proxy = getFileProxy("something"+ System.currentTimeMillis());
        assertNotNull(proxy);
        assertFalse(proxy.isDirectory());
        assertTrue(proxy.isFile());
        
        // delete file ...
        File f = proxy.toFile();
        assertTrue(f.exists());
        assertTrue(f.isFile());
        FileObject fo = FileUtil.toFileObject(f);
        f.delete();
        assertFalse(f.exists());
        // ... and recreate as folder
        f.mkdirs();
        assertTrue(f.exists());
        assertTrue(f.isDirectory());
        fo.refresh();
        
        assertTrue(proxy.isDirectory());
        assertFalse(proxy.isFile());
    }

    private VCSFileProxy getFolderProxy(String name) throws IOException {
        File d = new File(getWorkDir(), name);
        d.delete();
        d.mkdirs();
        FileObject fo = FileUtil.toFileObject(d);
        assertNotNull(fo);
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        return proxy;
    }

    private VCSFileProxy getFileProxy(String name) throws IOException {
        File f = new File(getWorkDir(), name);
        f.delete();
        f.createNewFile();
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        VCSFileProxy proxy = VCSFileProxy.createFileProxy(fo);
        return proxy;
    }
}
