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

package org.netbeans.core.startup.layers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import junit.framework.Test;
import org.netbeans.junit.NbTestSuite;
import org.openide.filesystems.AttributesTestHidden;
import org.openide.filesystems.FileObjectTestHid;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemFactoryHid;
import org.openide.filesystems.FileSystemTestHid;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.TestUtilHid;
import org.openide.filesystems.XMLFileSystem;
import org.openide.filesystems.XMLFileSystemTestHid;

/**
 *
 * @author Radek Matous
 */
public class BinaryFSBehindMultiFSTest extends FileSystemFactoryHid
implements XMLFileSystemTestHid.Factory {
    public BinaryFSBehindMultiFSTest(Test test) {
        super(test);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(AttributesTestHidden.class);
        suite.addTestSuite(XMLFileSystemTestHid.class);
         
        return new BinaryFSBehindMultiFSTest(suite);
    }
    
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        XMLFileSystem xfs = (XMLFileSystem)TestUtilHid.createXMLFileSystem(testName, resources);
        LayerCacheManager bm = LayerCacheManager.manager(true);
        return new FileSystem[] {BinaryCacheManagerTest.store(bm, Arrays.asList(xfs.getXmlUrls()))};
    }

    protected void destroyFileSystem(String testName) throws IOException {
    }

    private File getWorkDir() {
        String workDirProperty = System.getProperty("workdir");//NOI18N
        workDirProperty = (workDirProperty != null) ? workDirProperty : System.getProperty("java.io.tmpdir");//NOI18N                 
        return new File(workDirProperty);
    }

    public FileSystem createLayerSystem(String testName, URL[] layers) throws IOException {
        LayerCacheManager bm = LayerCacheManager.manager(true);
        return new MultiFileSystem(
            BinaryCacheManagerTest.store(bm, Arrays.asList(layers))
        );
    }

    public boolean setXmlUrl(FileSystem fs, URL[] layers) throws IOException {
        return false;
    }
}
