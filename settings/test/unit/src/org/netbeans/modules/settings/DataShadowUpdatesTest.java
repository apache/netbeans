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
