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

import java.lang.ref.WeakReference;
import java.util.HashSet;
import junit.textui.TestRunner;
import org.openide.filesystems.*;
import java.util.Enumeration;
import org.openide.loaders.DataObjectPool.Item;
import org.openide.nodes.Node;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.Repository;
import org.netbeans.junit.*;

/** How big is default data object?
 * @author Jaroslav Tulach
 */
public class DataObjectSizeTest extends NbTestCase {
    static FileSystem lfs;
    static DataObject original;

    public DataObjectSizeTest(String name) {
        super(name);
    }
    
    protected void setUp() throws java.lang.Exception {
        if (original == null) {
            String fsstruct [] = new String [] {
                "folder/original.txt", 
            };
            TestUtilHid.destroyLocalFileSystem (getName());
            lfs = TestUtilHid.createLocalFileSystem (getWorkDir (), fsstruct);
            FileObject fo = FileUtil.createData (lfs.getRoot (), "/folder/original.txt");
            assertNotNull(fo);
            original = DataObject.find (fo);

            assertFalse ("Not a folder", original instanceof DataFolder);
        }
    }
    
    public void testThatThereIsJustOneItemIssue42857 () throws Exception {
        Object[] exclude = {
            original.getLoader (),
            original.getPrimaryFile (),
            org.openide.util.Utilities.activeReferenceQueue (),
        };
        
        assertSize ("If we exclude all the static things, like loader and " +
            " reference queue and things we do not have control upon like file object" +
            " we should get some reasonable size for the data object. " + original, 
            java.util.Collections.singleton (original), 280, exclude
        );
    }
    
    public void testNumberOfDataObjectPoolItemsIssue42857 () throws Exception {
        class CountItems implements MemoryFilter {
            HashSet items = new HashSet ();
            
            public boolean reject(java.lang.Object obj) {
                if (obj instanceof Item) {
                    Item item = (Item) obj;
                    try {
                        DataObject dobj = item.getDataObjectOrNull();
                        if (dobj == null) {
                            // Unreproducible NPE in NB-Core-Build #672
                            return false;
                        }
                        if (dobj.getPrimaryFile().getFileSystem().isDefault()) {
                            return false;
                        }
                        items.add(obj);
                    } catch (FileStateInvalidException fileStateInvalidException) {
                        return false;
                    }
                }
                
                return false;
            }
        }
        CountItems cnt = new CountItems ();
        assertSize (
            "Just iterate thru all the objects available and count Items", 
            java.util.Collections.singleton (DataObjectPool.getPOOL ()), 
            Integer.MAX_VALUE,
            cnt
        );
        
        if (cnt.items.size () != 1) {
            fail ("There should be one item, but was " + cnt.items.size () + "\n" + cnt.items);
        }
    }
}
