/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
