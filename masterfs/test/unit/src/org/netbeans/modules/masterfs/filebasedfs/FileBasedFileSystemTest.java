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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.masterfs.filebasedfs;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.masterfs.providers.CheckProviders;
import org.openide.filesystems.*;

/**
 * @author rmatous
 */
public class FileBasedFileSystemTest extends FileSystemFactoryHid {
    public FileBasedFileSystemTest(Test test) {
        super(test);
    }
    
    @Override
    protected void setServices(Class<?>... services) {
        List<Class<?>> arr = new ArrayList<Class<?>>();
        arr.addAll(Arrays.asList(services));
        arr.add(FileBasedURLMapper.class);
        MockServices.setServices(arr.toArray(new Class<?>[0]));
    }

    public static Test suite() {
        return new FileBasedFileSystemTest(suite(false));
    }
    static NbTestSuite suite(boolean created) {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);
        suite.addTestSuite(FileUtilJavaIOFileHidden.class);
        suite.addTestSuite(BaseFileObjectTestHid.class);
        suite.addTestSuite(TempFileObjectTestHid.class);
        suite.addTest(new CheckProviders(created));
        return suite;
    }
        
    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        return new File(workDirProperty);
    }
            
    @Override
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        FileObjectFactory.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir());
        assertNotNull(workFo);
        for (int i = 0; i < resources.length; i++) {
            String res = resources[i];
            if (res.endsWith("/")) {
                assertNotNull(FileUtil.createFolder(workFo,res));
            } else {
                assertNotNull(FileUtil.createData(workFo,res));
            }
        }
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    @Override
    protected void destroyFileSystem(String testName) throws IOException {}    

    @Override
    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir()).getPath();
    }
}
