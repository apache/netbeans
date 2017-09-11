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

package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileUtil;

/** Test things about node delegates.
 * Note: if you mess with file status changes in this test, you may effectively
 * break the testLeakAfterStatusChange test.
 *
 * @author Jesse Glick
 */
public class DataRenameTest extends NbTestCase {
    Logger LOG;
    private DataObject my;
    private FileObject root;
    private File dir;
    
    public DataRenameTest(String name) {
        super(name);
    }

    @Override
    protected Level logLevel() {
        return Level.WARNING;
    }
    
    

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("test." + getName());
        
        MockServices.setServices(Pool.class);
        File fJava = new File(getWorkDir(), "F.java");
        fJava.createNewFile();
        dir = new File(getWorkDir(), "dir");
        dir.mkdirs();

        //LocalFileSystem lfs = new LocalFileSystem();
        //lfs.setRootDirectory(getWorkDir());
        root = FileUtil.toFileObject(getWorkDir());
        //FileObject root = lfs.getRoot();
        assertNotNull("root found", root);
        
        my = DataObject.find(root.getFileObject("F.java"));
    }
    public void testRenameBehaviour() throws Exception {
        assertEquals(WithRenameObject.class, my.getClass());
        
        DataFolder f = DataFolder.findFolder(root.getFileObject("dir"));
        
        DataObject res = my.createFromTemplate(f);

        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("F.java", all[0]);
        }
        
        res.rename("Jarda");

        {
            String[] all = dir.list();
            assertEquals("One: " + Arrays.asList(all), 1, all.length);
            assertEquals("Jarda.java", all[0]);
        }
    }
    
    public void testRenameToNull() throws IOException {
        try {
            my.rename(null);
            fail("The rename should fail with IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // OK
        }
    }

    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders () {
            return org.openide.util.Enumerations.<DataLoader>singleton(MyLoader.getLoader(MyLoader.class));
        }
    }
    
    public static final class MyLoader extends UniFileLoader {
        public MyLoader() {
            super(WithRenameObject.class.getName ());
            getExtensions().addExtension("java");
        }
        protected String displayName() {
            return "TwoPart";
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new WithRenameObject(this, primaryFile);
        }
    }
    public static final class WithRenameObject extends MultiDataObject {
        public WithRenameObject(MyLoader l, FileObject folder) throws DataObjectExistsException {
            super(folder, l);
        }

        @Override
        protected FileObject handleRename(String name) throws IOException {
            return super.handleRename(name);
        }
        
    }
    
}
