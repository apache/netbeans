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
package org.netbeans.modules.timers;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import javax.swing.JFrame;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jan Lahoda
 */
public class TimesCollectorPeerTest extends NbTestCase {

    public TimesCollectorPeerTest(String name) {
        super(name);
    }
    
    public void testHandleDelete() throws Exception {
        FileObject dir  = makeScratchDir(this);
        FileObject file = dir.createData("test.txt");
        
        TimesCollectorPeer.getDefault().reportTime(file, "test", "test", 0);
        
        file.delete();
        
        assertTrue(TimesCollectorPeer.getDefault().getFiles().isEmpty());
        
        JFrame f = new JFrame();
        
        f.add(new TimeComponentPanel());
        
        f.setVisible(true);
        
        file = dir.createData("test.txt");
        
        TimesCollectorPeer.getDefault().reportTime(file, "test", "test", 0);
        
        file.delete();
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
    @Override
    protected boolean runInEQ() {
        return true;
    }
}
