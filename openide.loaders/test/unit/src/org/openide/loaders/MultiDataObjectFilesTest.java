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

import junit.textui.TestRunner;

import org.openide.filesystems.*;
import java.io.IOException;
import java.util.*;
import org.netbeans.junit.*;

/** Test functionality of FilesSet object returned from MultiFileObject.files()
 * method.
 *
 * @author Petr Hamernik
 */
public class MultiDataObjectFilesTest extends NbTestCase {

    public MultiDataObjectFilesTest(String name) {
        super(name);
    }

    public void testFilesSet () throws Exception {
        DataLoader loader = DataLoader.getLoader(SimpleLoader.class);
        AddLoaderManuallyHid.addRemoveLoader(loader, true);

        
        // create directory structur description
        String[] fsstruct = new String[] {
            "A.primary", "A.a", "A.b", 
            "B.x0", "B.zx", "B.secondary", 
            "C.a0", "C.a5", "C.a1", "C.a4", 
            "A.primary0", "A.secondary", "A.zx", "A.x0",
            "C.a2", "C.a3", "C.primary",
            "B.primary", "B.b", "B.primary0", "B.a"
        };
            
        // clean and create new filesystems
        TestUtilHid.destroyLocalFileSystem(getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem(getWorkDir(), fsstruct);

        DataFolder folder = DataFolder.findFolder(fs.getRoot());

        DataObject[] children = folder.getChildren();
        assertTrue ("DataObjects were not recognized correctly.", children.length == 3);
        for (int i = 0; i < children.length; i++) {
            DataObject obj = children[i];
            Set files = obj.files();
            
            Iterator it = files.iterator();
            FileObject primary = (FileObject) it.next();
            assertEquals("Primary file is not returned first for "+obj.getName(), primary, obj.getPrimaryFile());

            FileObject last = null;
            while (it.hasNext()) {
                FileObject current = (FileObject) it.next();
                if (last != null) {
                    assertTrue("FileObjects are not alphabetically", last.getNameExt().compareTo(current.getNameExt()) < 0);
                }
                last = current;
            }
        }
        
        TestUtilHid.destroyLocalFileSystem(getName());
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class);
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                return fo.hasExt("primary") ? fo : FileUtil.findBrother(fo, "primary");
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FileEntry(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return new FileEntry(obj, secondaryFile);
        }
    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
    }

}
