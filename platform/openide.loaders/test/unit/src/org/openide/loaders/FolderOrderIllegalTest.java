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

package org.openide.loaders;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import org.openide.filesystems.FileObject;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/** Trying to emulate issue 122295.
 *
 * @author Jaroslav Tulach
 */
public class FolderOrderIllegalTest extends NbTestCase {
    Logger LOG;
    
    public FolderOrderIllegalTest(String name) {
        super(name);
    }
    @Override
    protected Level logLevel() {
        return Level.FINEST;
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        
        LOG = Logger.getLogger("org.openide.loaders.FolderList." + getName());
    }
    public void testRenameBehaviour() throws Exception {
        File dir = new File(getWorkDir(), "dir");
        dir.mkdirs();
        File old = new File(getWorkDir(), "old");
        old.mkdirs();
        for (int i = 0; i < 10; i++) {
            File fJava = new File(old, "F" + i + ".java");
            fJava.createNewFile();
        }

        FileObject root = FileUtil.toFileObject(getWorkDir());
        assertNotNull("root found", root);
        
        final DataFolder f = DataFolder.findFolder(root.getFileObject("old"));
        final DataFolder target = DataFolder.findFolder(root.getFileObject("dir"));
        
        class R implements Runnable {
            public void run() {
                try {
                    LOG.info("runnable waiting");
                    DataObject obj = DataObject.find(f.getPrimaryFile().getFileObject("F5.java"));
                    obj.move(target);
                    LOG.info("done");
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        R run = new R();
        RequestProcessor RP = new RequestProcessor("move");

        Logger listenTo = Logger.getLogger("org.openide.loaders.FolderList");
        String order = 
                "THREAD: move MSG:.*runnable waiting.*" +
                "THREAD: Folder recognizer MSG:.*carefullySort on.*" +
                "THREAD: Folder recognizer MSG:.*carefullySort before getOrder.*" +
                "THREAD: move MSG:.*done";
        Log.controlFlow(listenTo, Logger.getLogger("global"), order, 500);
        RP.post(run);
        
        DataObject[] arr = f.getChildren();
        
        String txt = Arrays.toString(arr).replace(", ", "\n");
        assertEquals("All 10:\n" + txt, 10, arr.length);//fail("OK:\n" + txt);
    }
}
