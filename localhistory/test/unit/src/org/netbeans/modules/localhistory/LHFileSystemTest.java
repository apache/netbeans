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

package org.netbeans.modules.localhistory;

import junit.framework.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.Filter;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.localhistory.utils.FileUtils;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedFileSystem;
import org.netbeans.modules.masterfs.filebasedfs.FileBasedURLMapper;
import org.netbeans.modules.masterfs.filebasedfs.fileobjects.FileObjectFactory;
import org.netbeans.modules.versioning.core.api.VCSFileProxy;
import org.openide.filesystems.*;


/**
 * @author Tomas Stupka
 */
public class LHFileSystemTest extends FileSystemFactoryHid {

    public LHFileSystemTest(Test test) {
        super(test);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        File f = getWorkDir("userdir");
        // cleanup first
        FileUtils.deleteRecursively(f.getParentFile());

        f.mkdirs();
        System.setProperty("netbeans.user", f.getAbsolutePath());
        MockServices.setServices(new Class[] {FileBasedURLMapper.class});

        // ensure test files are handled by LH
        System.setProperty("netbeans.localhistory.historypath", System.getProperty("work.dir"));
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public static void main(String args[]) {
        junit.textui.TestRunner.run(suite());
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();        
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                
        suite.addTestSuite(FileUtilJavaIOFileHidden.class);                
        Filter filter = new Filter();
        filter.setExcludes(new Filter.IncludeExclude[] {
            new Filter.IncludeExclude("testFileUtilToFileObjectIsValid", "fails occassionaly")
        });
        suite.setFilter(filter);
        suite.addTestSuite(BaseFileObjectTestHid.class);            
        return new LHFileSystemTest(suite);
    }
    
    private File getWorkDir(String testName) {
        String workDirProperty = System.getProperty("work.dir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N
        File f = new File(workDirProperty + "/" + testName);
        f.mkdirs();
        return f;
    }

    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {                       
        FileObjectFactory.reinitForTests();
        FileObject workFo = FileBasedFileSystem.getFileObject(getWorkDir(testName));
        assertNotNull(workFo);
        List<File> files = new ArrayList<File>(resources.length);
        for (int i = 0; i < resources.length; i++) {            
            String res = resources[i];
            FileObject fo;
            if (res.endsWith("/")) {
                fo = FileUtil.createFolder(workFo,res);
                assertNotNull(fo);
            } else {
                fo = FileUtil.createData(workFo,res);
                assertNotNull(fo);
            }            
            files.add(FileUtil.toFile(fo));            
        }        
        return new FileSystem[]{workFo.getFileSystem()};
    }
    
    protected void destroyFileSystem(String testName) throws IOException {}    

    protected String getResourcePrefix(String testName, String[] resources) {
        return FileBasedFileSystem.getFileObject(getWorkDir(testName)).getPath();
    }

    private String getPath(File f) {
        return FileUtils.getPath(VCSFileProxy.createFileProxy(f));
    }

}
