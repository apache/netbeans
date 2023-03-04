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

package org.openide.filesystems;

import junit.framework.*;
import org.netbeans.junit.*;
//import org.openide.filesystems.hidden.*;

import java.io.*;
import java.beans.PropertyVetoException;
/**
 *
 * @author  rm111737
 * @version
 */
public class MultiFileSystem2Test extends FileSystemFactoryHid {

    /** Creates new MultiFileSystemTest */
    public MultiFileSystem2Test(Test test) {
        super(test);
    }

    public static void main(String args[]) throws  Exception{
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);                        
        suite.addTestSuite(AttributesTestHidden.class);
        suite.addTestSuite(FileSystemTestHid.class);                        
        suite.addTestSuite(FileObjectTestHid.class);        
        suite.addTestSuite(MultiFileObjectTestHid.class);
        /*failing tests*/        
        suite.addTestSuite(URLMapperTestHidden.class);        
        suite.addTestSuite(URLMapperTestInternalHidden.class);                        
        
        return new MultiFileSystem2Test (suite);
    }
    

    /**
     * 
     * @param testName name of test 
     * @return  array of FileSystems that should be tested in test named: "testName" */
    protected FileSystem[] createFileSystem (String testName, String[] resources) throws IOException {        
            FileSystem lfs = TestUtilHid.createLocalFileSystem("mfs2"+testName, new String[] {});
            FileSystem xfs = TestUtilHid.createXMLFileSystem(testName, resources);
            FileSystem mfs = new MultiFileSystem(lfs, xfs);
            try {
                mfs.setSystemName("mfs2test");
            } catch (PropertyVetoException e) {
                e.printStackTrace();  //To change body of catch statement use Options | File Templates.
            }
        return new FileSystem[] {mfs,lfs,xfs};
    }
    
    protected void destroyFileSystem (String testName) throws IOException {}            
}
