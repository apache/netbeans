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

package org.openide.text;


import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.netbeans.junit.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.openide.*;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;

public class TemplatesMimeTypeTest extends NbTestCase {
    FileSystem fs;
    DataObject one;
    DataFolder from;
    DataFolder to;
    ErrorManager err;
    
    
    public TemplatesMimeTypeTest (String name) {
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
    

    public void testMimeType() throws IOException {
        assertTrue(this.one instanceof SimpleObject);
        SimpleObject s = (SimpleObject)this.one;

        CloneableEditorSupport sup = s.getLookup().lookup(CloneableEditorSupport.class);
        assertEquals("content/unknown", sup.cesEnv().getMimeType());
        
        ScriptEngineManager sem = new ScriptEngineManager();
        ScriptEngine eng = sem.getEngineByName("javascript");
        assertNotNull("java script engine found", eng);
        
        s.getPrimaryFile().setAttribute("javax.script.ScriptEngine", "javascript");
        
        assertEquals("Now the editor mime type is derived from the template", "application/javascript", sup.cesEnv().getMimeType());
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
