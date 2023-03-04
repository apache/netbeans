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

import org.openide.filesystems.*;
import org.openide.filesystems.FileSystem; // override java.io.FileSystem
import org.openide.nodes.Node;

import java.io.*;
import java.util.logging.Level;

import java.util.logging.Logger;
import org.netbeans.junit.*;

/** Test of folders move. Originally written for testing #8705.
 *
 * @author  Petr Hamernik
 */
public class DataFolderMoveTest extends LoggingTestCaseHid {
    private Logger err;


    /** Creates new DataFolderTest */
    public DataFolderMoveTest(String name) {
        super (name);
    }

    @Override
    protected int timeOut() {
        return 60000;
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }

    
    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
        err = Logger.getLogger("test." + getName());
    }
    
    
    private static final int FS_WIDTH = 5;
    private static final int FS_DEPTH = 4;
    private static final int TXT_SIZE = 20000;
    private static final int CYCLE = 4;

    @RandomlyFails
    public void testMoveFolders() throws Exception {
        
        // create directory structur description
        String[] fsstruct = new String[FS_WIDTH * (FS_DEPTH + 1)];
        int index = 0;
        StringBuffer nameBuf = new StringBuffer();
        for (int i = 0; i < FS_WIDTH; i++) {
            nameBuf.setLength(0);
            for (int j = 0; j < FS_DEPTH; j++) {
                nameBuf.append("pack");
                nameBuf.append(i);
                nameBuf.append(j);
                nameBuf.append("/");
                fsstruct[index++] = nameBuf.toString();
            }
            nameBuf.append("test.txt");
            fsstruct[index++] = nameBuf.toString();
        }

        // clean and create new filesystems
        TestUtilHid.destroyLocalFileSystem(getName() + "A");
        TestUtilHid.destroyLocalFileSystem(getName() + "B");
        
        FileSystem fsA = TestUtilHid.createLocalFileSystem(new File (getWorkDir (), "A"), fsstruct);
        FileSystem fsB = TestUtilHid.createLocalFileSystem(new File (getWorkDir (), "B"), new String[] {});

        // create directory structure
        for (int i = 0; i < fsstruct.length; i++) {
            if (fsstruct[i].endsWith("test.txt")) {
                FileObject obj = fsA.findResource(fsstruct[i]);
                FileLock lock = obj.lock();
                OutputStream out = obj.getOutputStream(lock);
                for (int j = 0; j < TXT_SIZE; j++) {
                    out.write('a');
                }
                out.close();
                lock.releaseLock();
            }
        }

        // data folders - roots
        final DataFolder[] roots = new DataFolder[] {
            DataFolder.findFolder(fsA.findResource("")),
            DataFolder.findFolder(fsB.findResource(""))
        };

        // node delegates of roots
        final Node[] fsNodes = new Node[] {
            roots[0].getNodeDelegate(),
            roots[1].getNodeDelegate()
        };

        try {
            for (int k = 0; k < CYCLE; k++) {
                final int src = (k % 2 == 0) ? 0 : 1;
                final int dest = (src == 0) ? 1 : 0;

                err.info("Copy cycle "+k+" (from "+src+" to "+dest+")");
                
                final boolean[] working = new boolean[] { true };

                // thread moving whole directory structure from src to dest FS
                Thread t = new Thread("moving thread") {
                    @Override
                    public void run() {
                        try {
                            DataObject[] objects = roots[src].getChildren();
                            for (int i = 0; i < objects.length; i++) {
                                err.info("moving for " + i + " object: " + objects[i]);
                                objects[i].move(roots[dest]);
                                err.info("done for " + i + " object: " + objects[i]);
                            }
                        }
                        catch (Exception e) {
                            err.log(Level.WARNING, "exception while moving", e);
                        }
                        finally {
                            synchronized (working) {
                                working[0] = false;
                                working.notifyAll();
                            }
                            err.info("settings working to false");
                        }
                    }
                };

                
                // moving started
                t.start();

                // during moving try to obtain children nodes
                // child node is created for temporary DataFolder;
                // When moving is complete, datafolder of this node is not valid.

                boolean failed = false;
                while (working[0]) {
                    err.info("test nodes while working: " + working[0]);
                    failed = testNodes(fsNodes[dest], false);
                    err.info("did it failed or not?: " + failed);
                    synchronized (working) {
                        if (working[0]) {
                            err.info("waiting");
                            working.wait(500);
                            err.info("waiting done");
                        }
                    }
                }
                if (failed) {
                    err.info("Failed, sleeping...");
                    try {
                        Thread.sleep(3000);
                    } 
                    catch (InterruptedException ie) {
                        ie.printStackTrace();
                    }
                    err.info("end of waiting");
                }
                
                testNodes(fsNodes[dest], true);
            }
        }
        finally {
            err.info("cleanup in finally");
            // clean
            TestUtilHid.destroyLocalFileSystem(getName() + "A");
            TestUtilHid.destroyLocalFileSystem(getName() + "B");
        }
    }
    
    private boolean testNodes(Node n, boolean callFail) {
        boolean failed = false;
        Node[] nodes = n.getChildren().getNodes();
        for (int j = 0; j < nodes.length; j++) {
            DataObject dobj = nodes[j].getCookie(DataObject.class);
            if (!dobj.isValid()) {
                failed = true;
                try {
                    Thread.sleep(500);
                } 
                catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
                if (callFail) {
                    fail("Found Invalid Object: ["+j+"/"+nodes.length+"]: "+dobj+" / Node:"+nodes[j]);
                }
            }
        }
        return failed;
    }
}
