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

package org.netbeans.modules.mercurial;

import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.modules.masterfs.filebasedfs.BaseFileObjectTestHid;
import org.openide.filesystems.*;
import org.openide.filesystems.test.StatFiles;


/**
 * Count read/write/delete file access and print results.
 */
public class MercurialFileSystemTestStat extends MercurialFileSystemTestCase {

    public MercurialFileSystemTestStat(Test test) {
        super(test);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestBaseHid.accessMonitor = new StatFiles();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TestBaseHid.accessMonitor.getResults().dump();
    }
    
    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();        
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(URLMapperTestHidden.class);
        suite.addTestSuite(FileUtilTestHidden.class);                
        suite.addTestSuite(FileUtilJavaIOFileHidden.class);                
        suite.addTestSuite(BaseFileObjectTestHid.class);            
        return new MercurialFileSystemTestStat(suite);
    }
}
