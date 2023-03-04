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

package org.netbeans.modules.masterfs.watcher;

import java.io.IOException;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class MoveUnlocksWatcherTest extends NbTestCase {

    
    public MoveUnlocksWatcherTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testMovedUnlocks() throws IOException {
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        FileObject fromFile = fo.createData("move.txt");
        FileObject toFolder = fo.createFolder("toFolder");
        
        FileObject toFile = fromFile.move(fromFile.lock(), toFolder, fromFile.getName(), fromFile.getExt());

        assertFalse("Not locked anymore: " + fo, Watcher.isLocked(fo));
        assertFalse("Not locked anymore: " + fromFile, Watcher.isLocked(fromFile));
        assertFalse("Not locked anymore: " + toFolder, Watcher.isLocked(toFolder));
        assertFalse("Not locked anymore: " + toFile, Watcher.isLocked(toFile));
    }

}
