/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.loaders;

import java.util.Arrays;
import java.util.logging.Level;
import org.netbeans.junit.Log;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Index;
import org.openide.nodes.Node;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.util.Mutex;


/**
 * Tests Index cookio of DataFolder (when uses DataFilter).
 *
 * @author Jiri Rechtacek
 */
public class DataFolderIndexTest extends NbTestCase {
    DataFolder df;
    FileObject fo;
    ErrorManager ERR;
    
    /** Constructor required by JUnit.
     * @param testName method name to be used as testcase
     */
    public DataFolderIndexTest(String s) {
        super(s);
    }

    @Override
    protected int timeOut() {
        return 10000;
    }
    
    @Override
    protected void setUp () throws Exception {
        MockServices.setServices(Pool.class);
        
        ERR = org.openide.ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        FileObject old = FileUtil.getConfigFile("TestTemplates");
        if (old != null) {
            old.delete();
        }
        
        fo = FileUtil.getConfigRoot().createFolder("TestTemplates");
        df = DataFolder.findFolder(fo);
        assertNotNull("DataFolder found for AA", df);
        
        df.getPrimaryFile().createData("marie");
        df.getPrimaryFile().createData("jakub");
        df.getPrimaryFile().createData("eva");
        df.getPrimaryFile().createData("adam");
        
        assertNotNull("Folder " + df + " has a children.", df.getChildren());
        assertEquals("Folder " + df + " has 4 childs.", 4, df.getChildren().length);
    }
    
    public void testIndexWithoutInitialization() throws Exception {
        Node n = df.getNodeDelegate();
        
        Index fromNode = n.getLookup().lookup(Index.class);
        assertNotNull ("DataFolderNode has Index.", fromNode);

        int x = fromNode.getNodesCount();
        assertEquals("The same number of nodes like folder children", df.getChildren().length, x);
    }

    public void testIndexWithoutInitializationInReadAccess() throws Exception {
        CharSequence seq = Log.enable("", Level.INFO);
        
        org.openide.nodes.Children.MUTEX.readAccess(new Mutex.ExceptionAction () {
            public Object run () throws Exception {
                Node n = df.getNodeDelegate();

                Index fromNode = n.getLookup().lookup(Index.class);
                assertNotNull ("DataFolderNode has Index.", fromNode);

                int x = fromNode.getNodesCount();
                assertEquals("Folder has few children", 4, df.getChildren().length);
                assertEquals("Cannot initialize the count in nodes as we are in read access", 0, x);
                return null;
            }
        });
        
        if (seq.length() > 0) {
            fail("No messages shall be reported:\n" + seq);
        }
    }
    
    public void testIndexNodesWithoutInitialization() throws Exception {
        Node n = df.getNodeDelegate();
        
        Index fromNode = n.getLookup().lookup(Index.class);
        assertNotNull ("DataFolderNode has Index.", fromNode);

        int x = fromNode.getNodes().length;
        assertEquals("The same number of nodes like folder children", df.getChildren().length, x);
    }
    
    public void testWithoutFilter () throws Exception {
        testMatchingIndexes (df, df.getNodeDelegate ());
    }
    
    public void testIndexCookieOfferedOnlyWhenAppropriate() throws Exception {
        Node n = df.getNodeDelegate();
        assertNotNull("have an index cookie on SFS", n.getLookup().lookup(Index.class));
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        n = DataFolder.findFolder(lfs.getRoot()).getNodeDelegate();
        assertNull("have no index cookie on a local folder", n.getLookup().lookup(Index.class));
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject x = fs.getRoot().createFolder("x");
        FileObject y = fs.getRoot().createFolder("y");
        y.setAttribute("DataFolder.Index.reorderable", Boolean.TRUE);
        n = DataFolder.findFolder(x).getNodeDelegate();
        assertNull("have no index cookie on a random folder in a random FS", n.getLookup().lookup(Index.class));
        n = DataFolder.findFolder(y).getNodeDelegate();
        assertNotNull("do have index cookie if magic attr is set", n.getLookup().lookup(Index.class));
    }
    
    private void testMatchingIndexes (DataFolder f, Node n) {
        ERR.log("Starting the test");
        Node [] arr = n.getChildren ().getNodes (true);
        ERR.log("Children created");
        
        Index fromNode = n.getLookup().lookup(Index.class);
        assertNotNull ("DataFolderNode has Index.", fromNode);
        ERR.log("Index from node: " + fromNode);
        
        Index fromFolder = new DataFolder.Index (f, n);
        assertNotNull ("DataFolderNode has Index.", fromFolder);
        ERR.log("Index from folder: " + fromFolder);
        
        int fromNodeCount = fromNode.getNodesCount ();
        assertTrue ("Index contains some items", fromNodeCount > 0);
        ERR.log("count computed: " + fromNodeCount);
        
        int fromFolderCount = fromFolder.getNodesCount ();
        assertTrue ("Index contains some items", fromFolderCount > 0);
        
        Node[] arrNode = fromNode.getNodes ();
        Node[] arrFolder = fromFolder.getNodes ();
        
        ERR.log ("Node's index: " + Arrays.asList (arrNode));
        ERR.log ("Folder's index: " + Arrays.asList (arrFolder));
        
        for (int i = 0; i < arr.length; i++) {
            log("Computing index for [" + i + "] which is node " + arr[i]);
            int index = fromNode.indexOf (arr [i]);
            log("Index computed to be " + index);
            
            log("Computing index from the folder [" + i + "]: " + arr[i]);
            int folderIndex = fromFolder.indexOf (arr [i]);
            log("Folder index is to be " + folderIndex);
            
            if (folderIndex != index) {
                fail(
                    i + "th iteration - Node " + arr [i] + 
                    " has as same position in Node's Index [" + 
                    Arrays.asList (fromNode.getNodes ()) + "]" +
                    "as in folder's Index [" + 
                    Arrays.asList (fromFolder.getNodes ()) + "]. folder: " + 
                    folderIndex + " node: " + index
                );
            }
        }
    }
    
    public static final class Pool extends DataLoaderPool {
        public static DataLoader extra;
        
        
        protected java.util.Enumeration<? extends DataLoader> loaders () {
            if (extra == null) {
                return org.openide.util.Enumerations.empty ();
            } else {
                return org.openide.util.Enumerations.singleton (extra);
            }
        }
    }
}
