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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;

import java.io.File;
import java.io.IOException;
import java.security.Permission;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.masterfs.filebasedfs.children.ChildrenSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 */
public class NoLockWhenRefreshIOTest extends NbTestCase {
    static {
        assertFalse("No lock & preload the code", ChildrenSupport.isLock());
    }

    
    Logger LOG;
    
    public NoLockWhenRefreshIOTest(String testName) {
        super(testName);
    }
            
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("test." + getName());
        try {
            clearWorkDir();
        } catch (IOException ex) {
            LOG.log(Level.WARNING, "Cannot clear work dir for some reason", ex);
        }
        
        File dir = new File(getWorkDir(), "dir");
        dir.mkdir();
        
        for (int i = 0; i < 100; i++) {
            new File(dir, "x" + i + ".txt").createNewFile();
            new File(dir, "d" + i).mkdir();
        }
        
        assertEquals("Two hundred", 200, dir.list().length);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    public void testRefreshOfAFolder() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject dir = fo.getFileObject("dir");
        assertNotNull("dir found", dir);
        System.setSecurityManager(new AssertNoLockManager(getWorkDirPath()));
        List<FileObject> arr = Arrays.asList(dir.getChildren());
        dir.refresh();
        List<FileObject> arr2 = Arrays.asList(dir.getChildren());
        
        assertEquals("Same results", arr, arr2);
    }
    
    
    /**
     * Test for bug 228470.
     *
     * @throws java.io.IOException
     */
    public void testGetChild() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject dir = fo.getFileObject("dir");
        assertNotNull("dir found", dir);
        System.setSecurityManager(new AssertNoLockManager(getWorkDirPath()));
        FileObject fileObject = dir.getFileObject("x50.txt");
        assertNotNull(fileObject);
    }

    private static class AssertNoLockManager extends SecurityManager {
        final String prefix;

        public AssertNoLockManager(String p) {
            prefix = p;
        }

        @Override
        public void checkRead(String string) {
            if (string.startsWith(prefix)) {
                assertFalse("No lock", ChildrenSupport.isLock());
            }
        }

        @Override
        public void checkRead(String string, Object o) {
            checkRead(string);
        }

        @Override
        public void checkPermission(Permission prmsn) {
        }

        @Override
        public void checkPermission(Permission prmsn, Object o) {
        }
    }
        
}
