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
import java.io.IOException;
import org.netbeans.junit.*;


/**
 * @author  David Strupl
 */
public class MemoryFileSystemTest extends FileSystemFactoryHid {
    public MemoryFileSystemTest(Test test) {
        super(test);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(RepositoryTestHid.class);
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(MemoryFSTestHid.class);
        
        return new MemoryFileSystemTest(suite);
    }
    
    
    @Override
    protected void destroyFileSystem(String testName) throws IOException {
    }
    
    @Override
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException{
        return new FileSystem[] {new MemoryFileSystem(resources)};
    }
}
