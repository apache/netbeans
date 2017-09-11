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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.masterfs;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileUtil;

/** Simulating issue 109462.
 */
public class RenameLeavesLockTest extends NbTestCase {
    Logger LOG;
    
    public RenameLeavesLockTest(String name) {
        super(name);
    }

    protected Level logLevel() {
        return Level.WARNING;
    }
    
    

    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
    }
    public void testRenameBehaviour() throws Exception {
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File fJava = new File(dir, "F.java");
        fJava.createNewFile();

        //LocalFileSystem lfs = new LocalFileSystem();
        //lfs.setRootDirectory(getWorkDir());
        FileObject root = FileUtil.toFileObject(getWorkDir());
        //FileObject root = lfs.getRoot();
        assertNotNull("root found", root);
        
        FileObject f = root.getFileObject("dir/F.java");
        assertNotNull("file found", f);
        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("F.java", all[0]);
        }
        FileLock lock = f.lock();
        assertTrue(f.isLocked());
        f.rename(lock, "Jarda", "java");
        f.rename(lock, "F", "java");
        f.rename(lock, "Jarda", "java");
        assertTrue(f.isLocked());
        lock.releaseLock();
        assertFalse(f.isLocked());
        

        //Issue 109462, this is failing:
        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("Jarda.java", all[0]);
        }
    }
    
}
