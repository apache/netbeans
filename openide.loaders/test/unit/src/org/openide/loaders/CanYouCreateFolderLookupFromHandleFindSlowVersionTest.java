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
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Enumeration;
import java.util.List;
import javax.swing.JButton;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;


/** It must be possible to create lookup anytime, if there is no deadlock,
 * even if the recognition of FolderLookup takes really long time.
 *
 * @author Jaroslav Tulach
 */
public class CanYouCreateFolderLookupFromHandleFindSlowVersionTest extends LoggingTestCaseHid {
    
    /** Creates a new instance of CanYouQueryFolderLookupFromHandleFindTest */
    public CanYouCreateFolderLookupFromHandleFindSlowVersionTest(String s) {
        super(s);
    }
    
    protected void setUp() {
        registerIntoLookup(new Pool());
    }
    
    public void testCreateAndImmediatellyQueryWhenThereIsALotfSlowDataObjectsTheLookup() throws Exception {
        MyLoader m = MyLoader.getLoader(MyLoader.class);
        m.button = FileUtil.createFolder(FileUtil.getConfigRoot(), "FolderLookup");
        DataObject instance = InstanceDataObject.create(DataFolder.findFolder(m.button), "SomeName", JButton.class);
        m.instanceFile = instance.getPrimaryFile();
        for (int i = 0; i < 15; i++) {
            m.button.createData("slow" + i + ".slow");
        }
        
        
        WeakReference ref = new WeakReference(instance);
        instance = null;
        assertGC("Object must disappear first", ref);
        
        FileObject any = FileUtil.getConfigRoot().createData("Ahoj.txt");
        DataObject obj = DataObject.find(any);
        
        assertEquals("The right object found", m, obj.getLoader());
        assertNotNull("Value found", m.v);
        assertEquals("Button", JButton.class, m.v.getClass());
        assertNotNull("Lookup created", m.lookup);
        assertEquals("All slow files recognized", 15, m.slowCnt);
    }
    
    
    public static final class MyLoader extends UniFileLoader {
        public FileObject button;
        public Object v;
        public Lookup lookup;
        
        public InstanceDataObject created;
        
        private FileObject instanceFile;
        
        private DataObject middleCreation;

        private int slowCnt;
        
        public MyLoader() throws IOException {
            super("org.openide.loaders.MultiDataObject");
        }
        
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("slow")) {
                slowCnt++;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail("No failures, please");
                }
                return null;
            }
            
            if (!fo.hasExt("txt")) {
                return null;
            }
            
            assertNull("First invocation", lookup);
            
            FolderLookup l = new FolderLookup(DataFolder.findFolder(button));
            lookup = l.getLookup();
            v = lookup.lookup(JButton.class);
            assertNotNull("The instance computed", v);
            
            return fo;
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new MultiDataObject(primaryFile, this);
        }
    }
    
    private static final class Pool extends DataLoaderPool {
        static List loaders;
        
        public Pool() {
        }
        
        public Enumeration loaders() {
            return Enumerations.singleton(DataLoader.getLoader(MyLoader.class));
        }
    }
    
}
