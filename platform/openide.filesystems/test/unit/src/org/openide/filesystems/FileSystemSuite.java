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

public class FileSystemSuite extends NbTestCase {

    public FileSystemSuite(java.lang.String testName) {
        super(testName);
    }

    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite("FileSystemTest");


/*
        suite.addTest(XMLFileSystemTest.suite());
        suite.addTest(LocalFileSystemTest.suite());
        suite.addTest(JarFileSystemTest.suite());                                
        //suite.addTest(MultiFileSystemTest.suite());                        
        suite.addTest(MultiFileSystem1Test.suite());                                
        //suite.addTest(MultiFileSystem2Test.suite());                                
        //suite.addTest(MultiFileSystem3Test.suite());                                
*/

        return suite;
    }
    
}
