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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.openide.filesystems.*;

import java.util.ArrayList;


/** Testing that a change in a pool triggers notification of a change in DataFolder's
 * children.
 *
 * @author  Jaroslav Tulach
 */
public class DataFolderRefreshTest extends LoggingTestCaseHid {
    private ArrayList hold = new ArrayList();
    private org.openide.ErrorManager err;

    private FileObject root;

    /** Creates new DataFolderTest */
    public DataFolderRefreshTest (String name) {
        super (name);
    }
    
    @Override
    protected void setUp () throws Exception {
        err = org.openide.ErrorManager.getDefault().getInstance("TEST-" + getName());

        registerIntoLookup(new FolderInstanceTest.Pool());
        
        DataLoaderPool pool = DataLoaderPool.getDefault ();
        assertNotNull (pool);
        assertEquals (FolderInstanceTest.Pool.class, pool.getClass ());
        
        clearWorkDir ();
        
        root = FileUtil.createFolder(
            FileUtil.getConfigRoot(),
            "dir"
        );
        
        FileUtil.createData(root, "s1.simple");
        FileUtil.createData(root, "s2.simple");
    }

    public void testIsChangeFired() throws Exception {
        DataLoader l = DataLoader.getLoader(DataLoaderOrigTest.SimpleUniFileLoader.class);
        err.log("Add loader: " + l);
        FolderInstanceTest.Pool.setExtra(l);
        err.log("Loader added");
        
        DataFolder f = DataFolder.findFolder(root);
        class C implements PropertyChangeListener {
            PropertyChangeEvent ev;
            
            public void propertyChange(PropertyChangeEvent evt) {
                assertNull("Only one event", this.ev);
                this.ev = evt;
            }
        }
        
        C c = new C();
        f.addPropertyChangeListener(c);
        
        DataObject[] arr = f.getChildren();
        
        assertEquals("Two objects", 2, arr.length);
        assertEquals("Loader1", arr[0].getLoader(), l);
        assertEquals("Loader2", arr[1].getLoader(), l);
        
        FolderInstanceTest.Pool.setExtra(null);
        
        arr = f.getChildren();
        
        assertNotNull("A change event delivered", c.ev);
        assertEquals("children", DataFolder.PROP_CHILDREN, c.ev.getPropertyName());
        
        
        assertEquals("Two objects", 2, arr.length);
        assertEquals("Loader1", arr[0].getLoader(), DataLoaderPool.getDefaultFileLoader());
        assertEquals("Loader2", arr[1].getLoader(), DataLoaderPool.getDefaultFileLoader());
    }
}
