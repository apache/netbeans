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

import org.openide.filesystems.*;
import java.io.IOException;
import org.netbeans.junit.*;

/*
 * Checks whether a during a modify operation (copy, move) some
 * other thread can get a grip on unfinished and uncostructed
 * content on filesystem.
 *
 * @author Jaroslav Tulach
 */
public class Issue136931Test extends NbTestCase {
    private DataFolder root;
    private DataFolder to;
    private DataObject a;
    private DataObject res;

    /** Creates the test */
    public Issue136931Test(String name) {
        super(name);
    }

    // For each test setup a FileSystem and DataObjects
    public void testCreateFromTemplate () throws Exception {
        clearWorkDir();
        FileUtil.setMIMEType("attr", "text/x-art");
        FileUtil.setMIMEType("block", "text/x-block");
        FileObject fo = FileUtil.createData(
            FileUtil.getConfigRoot(),
            "Loaders/text/x-art/Factories/" + BLoader.class.getName().replace('.', '-') + ".instance"
        );
        FileObject bo = FileUtil.createData(
            FileUtil.getConfigRoot(),
            "Loaders/text/x-block/Factories/" + BLoader.class.getName().replace('.', '-') + ".instance"
        );
        
        String fsstruct [] = new String [] {
            "source/A.attr", 
            "B.attr",
            "dir/",
            "fake/A.instance"
        };
        TestUtilHid.destroyLocalFileSystem (getName());
        FileSystem fs = TestUtilHid.createLocalFileSystem (getWorkDir(), fsstruct);
        root = DataFolder.findFolder (fs.getRoot ());
        
        to = DataFolder.findFolder (fs.findResource (fsstruct[2]));
        
        fs.findResource (fsstruct[0]).setAttribute ("A", Boolean.TRUE);
        
        a = DataObject.find (fs.findResource (fsstruct[0]));

        assertEquals("Right loader for the template", BLoader.class, a.getLoader().getClass());
    
        res = a.createFromTemplate (to);

        assertTrue("Handle copy called correctly", PostDataObject.called);
    }
    
       
    /** Calls super to do some copying and then does some post processing
     */
    public static final class PostDataObject extends MultiDataObject {
        static boolean called;

        public PostDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
            super(fo, loader);
        }


        @Override
        protected DataObject handleCreateFromTemplate(DataFolder df, String name) throws IOException {
            DataObject retValue;
            retValue = super.handleCreateFromTemplate(df, name);

            FileObject artificial = FileUtil.createData(
                    retValue.getPrimaryFile().getParent(),
                    "x.block");

            DataObject obj = DataObject.find(artificial);
            assertEquals("Object really created", obj.getPrimaryFile(), artificial);
            assertEquals("Right loader", BLoader.class, obj.getLoader().getClass());

            called = true;

            return retValue;
        }

        @Override
        protected FileObject handleMove(DataFolder df) throws IOException {
            FileObject retValue;

            retValue = super.handleMove(df);
            return retValue;
        }
    }

    public static final class BLoader extends UniFileLoader {

        public BLoader() {
            super(DataObject.class.getName());
        }

        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("attr");
            getExtensions().addExtension("block");
        }

        protected String displayName() {
            return getClass().getName();
        }

        @Override
        protected org.openide.filesystems.FileObject findPrimaryFile(org.openide.filesystems.FileObject fileObject) {
            org.openide.filesystems.FileObject retValue;

            retValue = super.findPrimaryFile(fileObject);
            return retValue;
        }

        protected MultiDataObject createMultiObject(FileObject pf) throws IOException {
            return new PostDataObject(pf, this);
        }
    }
}

