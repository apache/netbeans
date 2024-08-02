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

package org.openide.text;


import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.netbeans.junit.*;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import org.netbeans.api.scripting.Scripting;
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
        // Ensure polyglot can be loaded cleanly and executed. Normally set by
        // org.netbeans.libs.graalsdk.impl.Installer, when loaded as a netbeans
        // module
        System.setProperty("polyglot.engine.WarnInterpreterOnly", "false");
        System.setProperty("truffle.UseFallbackRuntime", "true");

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
        
        ScriptEngineManager sem = Scripting.createManager();
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
