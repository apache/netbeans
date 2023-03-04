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
package org.netbeans.modules.settings;

import java.io.File;
import java.lang.reflect.Method;
import org.netbeans.core.startup.layers.SystemFileSystem;
import org.netbeans.junit.NbTestCase;
import org.openide.actions.NewTemplateAction;
import org.openide.actions.SaveAsTemplateAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.LocalFileSystem;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataShadow;

/**
 *
 * @author Svatopluk Dedic <sdedic@netbeans.org>
 */
public class DataShadowUpdatesTest extends NbTestCase {

    public DataShadowUpdatesTest(String name) {
        super(name);
    }

    /**
     * The test simulates a situation in which a .shadow file in the user
     * configuration is reverted, and an original contents/atributes,
     * with a different target, are revealed.
     * <p/>
     * The DataShadow should at least produce the appropriate cookies.
     * 
     * @throws Exception T
     */
    public void testShadowFileReverted() throws Exception {

        clearWorkDir();
        
        File wd = getWorkDir();
        File homeDir = new File(wd, "home");
        File localDir = new File(wd, "data");
        localDir.mkdirs();
        homeDir.mkdirs();
        
        LocalFileSystem lfs1 = new LocalFileSystem();
        lfs1.setRootDirectory(homeDir);
        FileObject r = lfs1.getRoot();

        Class c = SystemFileSystem.class;
        Method m = c.getDeclaredMethod("create", File.class, File.class, File[].class);
        m.setAccessible(true);
        
        FileSystem mfs = (FileSystem)m.invoke(null,
                localDir,
                homeDir,
                new File[] {} 
        );
        
        FileObject file1 = r.createData("org-openide-actions-NewTemplateAction.instance");
        FileObject file2 = r.createData("org-openide-actions-SaveAsTemplateAction.instance");
        FileObject shadow = r.createData("file.shadow");
        shadow.setAttribute("originalFile", "org-openide-actions-NewTemplateAction.instance");
        
        FileObject mfsRoot = mfs.getRoot();
        FileObject mfsShadow = mfsRoot.getFileObject("file.shadow");
        // must create the file contents, otherwise canRevert does not work either
        mfsShadow.getOutputStream().close();
        mfsShadow.setAttribute("originalFile", "org-openide-actions-SaveAsTemplateAction.instance");
        
        DataShadow s = (DataShadow)DataObject.find(mfsShadow);
        assertEquals(
                file2.getPath(),
                s.getOriginal().getPrimaryFile().getPath());
        
        assertTrue(mfsShadow.canRevert());
        
        InstanceCookie ic = s.getCookie(InstanceCookie.class);
        assertSame(SaveAsTemplateAction.class, ic.instanceClass());
        
        mfsShadow.revert();
        
        s = (DataShadow)DataObject.find(mfsShadow);
        ic = s.getCookie(InstanceCookie.class);
        assertSame(NewTemplateAction.class, ic.instanceClass());
        assertEquals(
                file1.getPath(),
                s.getOriginal().getPrimaryFile().getPath());
        
    }
    
    
}
