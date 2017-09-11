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
import org.netbeans.junit.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import org.openide.*;
import org.openide.cookies.CloseCookie;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.LineCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.PrintCookie;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;

public class MultiDataObjectVersion1Test extends NbTestCase {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    ErrorManager err;
    
    
    public MultiDataObjectVersion1Test (String name) {
        super (name);
    }

    @Override
    protected Level logLevel() {
        return Level.INFO;
    }

    @Override
    protected int timeOut() {
        return 45000;
    }

    @Override
    public void setUp() throws Exception {
        clearWorkDir();
        
        super.setUp();
        
        MockServices.setServices(Pool.class);
        
        err = ErrorManager.getDefault().getInstance("TEST-" + getName());
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(getWorkDir());
        fs = lfs;
        FileUtil.createData(fs.getRoot(), "from/x.prima");
        FileUtil.createData(fs.getRoot(), "from/x.seconda");
        FileUtil.createFolder(fs.getRoot(), "to/");
        
        one = DataObject.find(fs.findResource("from/x.prima"));
        assertEquals(SimpleObject.class, one.getClass());
        
        from = one.getFolder();
        to = DataFolder.findFolder(fs.findResource("to/"));
        
        assertEquals("Nothing there", 0, to.getPrimaryFile().getChildren().length);
    }
    

    public void testWhatIsInTheLookup() throws Exception {
        assertTrue(this.one instanceof SimpleObject);
        SimpleObject s = (SimpleObject)this.one;

        assertEquals("DO in lookup", s, s.getLookup().lookup(DataObject.class));
        assertEquals("Fo in lookup", s.getPrimaryFile(), s.getLookup().lookup(FileObject.class));
        assertEquals("no node created yet", 0, s.cnt);
        Node lkpNd = s.getLookup().lookup(Node.class);
        assertEquals("now node created", 1, s.cnt);
        assertEquals("Node in lookup", s.getNodeDelegate(), lkpNd);
    }

    public void testEditorInLookup() throws Exception {
        assertTrue(this.one instanceof SimpleObject);
        SimpleObject s = (SimpleObject)this.one;

        LineCookie line = s.getLookup().lookup(LineCookie.class);
        assertNotNull("Line cookie is there", line);
        assertEquals("Edit cookie", line, s.getLookup().lookup(EditCookie.class));
        assertEquals("Editor cookie", line, s.getLookup().lookup(EditorCookie.class));
        assertEquals("Editor objservable cookie", line, s.getLookup().lookup(EditorCookie.Observable.class));
        assertEquals("SaveAsCapable", line, s.getLookup().lookup(SaveAsCapable.class));
        assertEquals("Open cookie", line, s.getLookup().lookup(OpenCookie.class));
        assertEquals("Close cookie", line, s.getLookup().lookup(CloseCookie.class));
        assertEquals("Print cookie", line, s.getLookup().lookup(PrintCookie.class));
    }

    
    public static final class Pool extends DataLoaderPool {
        @Override
        protected Enumeration loaders() {
            return Enumerations.singleton(SimpleLoader.getLoader(SimpleLoader.class));
        }
    }
    
    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class);
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        @Override
        protected FileObject findPrimaryFile(FileObject fo) {
            if (!fo.isFolder()) {
                // emulate the behaviour of form data object
                
                /* emulate!? this one is written too well ;-)
                FileObject primary = FileUtil.findBrother(fo, "prima");
                FileObject secondary = FileUtil.findBrother(fo, "seconda");
                
                if (primary == null || secondary == null) {
                    return null;
                }
                
                if (primary != fo && secondary != fo) {
                    return null;
                }
                 */
                
                // here is the common code for the worse behaviour
                if (fo.hasExt("prima")) {
                    return FileUtil.findBrother(fo, "seconda") != null ? fo : null;
                }
                
                if (fo.hasExt("seconda")) {
                    return FileUtil.findBrother(fo, "prima");
                }
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
        private int cnt;
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
            registerEditor("mime/type", false);
        }

        @Override
        protected int associateLookup() {
            return 1;
        }

        @Override
        protected Node createNodeDelegate() {
            cnt++;
            return super.createNodeDelegate();
        }
        
        
    }

}
