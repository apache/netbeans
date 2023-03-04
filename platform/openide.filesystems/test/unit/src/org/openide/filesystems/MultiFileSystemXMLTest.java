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

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.net.URL;
import junit.framework.Test;
import org.netbeans.junit.*;

/**
 *
 * @author  rm111737
 * @version
 */
public class MultiFileSystemXMLTest extends FileSystemFactoryHid
implements XMLFileSystemTestHid.Factory {

    /** Creates new XMLFileSystemTest */
    public MultiFileSystemXMLTest(Test test) {
        super(test);
    }

    public static Test suite() {
        NbTestSuite suite = new NbTestSuite();
        suite.addTestSuite(FileSystemTestHid.class);
        suite.addTestSuite(FileObjectTestHid.class);
        suite.addTestSuite(XMLFileSystemTestHid.class);        
                
        return new MultiFileSystemXMLTest(suite);
    }

    protected void destroyFileSystem(String testName) throws IOException {}
    
    protected FileSystem[] createFileSystem(String testName, String[] resources) throws IOException {
        return new FileSystem[] {TestUtilHid.createXMLFileSystem(testName, resources)};
    }

    public FileSystem createLayerSystem(String testName, URL[] layers) throws IOException {
        MFS mfs = new MFS();
        try {
            mfs.xfs.setXmlUrls(layers);
        } catch (PropertyVetoException ex) {
            throw (IOException)new IOException().initCause(ex);
        }
        return mfs;
    }

    public boolean setXmlUrl(org.openide.filesystems.FileSystem fs, URL[] layers) throws IOException {
        MFS mfs = (MFS)fs;
        try {
            mfs.xfs.setXmlUrls(layers);
        } catch (PropertyVetoException ex) {
            throw (IOException)new IOException().initCause(ex);
        }
        return true;
    }
    

    private static final class MFS extends MultiFileSystem {
        private XMLFileSystem xfs;
        public MFS() {
            this(new XMLFileSystem());
        }

        private MFS(XMLFileSystem fs) {
            super(fs);
            this.xfs = fs;
        }
    }
}
