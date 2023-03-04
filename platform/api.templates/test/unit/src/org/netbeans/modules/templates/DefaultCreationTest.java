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
package org.netbeans.modules.templates;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataLoader;
import org.openide.loaders.DataLoaderPool;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.FileEntry;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Enumerations;
import org.openide.util.MapFormat;

/**
 *
 * @author sdedic
 */
public class DefaultCreationTest extends NbTestCase {

    public DefaultCreationTest(String name) {
        super(name);
    }
    
    /**
     * Checks that FileEntry.Format properly formats the template. Part of parameters is provided
     * by the DataLoader while part is provided by the API caller.
     * 
     * @throws Exception 
     */
    public void testCreateDefaultFormat() throws Exception {
        MockServices.setServices(Pool.class);
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject templ = FileUtil.createData(root, "simpleTemplate.prim");
        String txt = "{a}{b}";
        OutputStream os = templ.getOutputStream();
        os.write(txt.getBytes());
        os.close();
        
        DataObject obj = DataObject.find(templ);
        DataFolder folder = DataFolder.findFolder(FileUtil.createFolder(root, "target"));
        Map m = new HashMap();
        m.put("a", "eeee");
        DataObject x = obj.createFromTemplate(folder, "nue", m);
        
        assertEquals("eeeexxxx\n", x.getPrimaryFile().asText());
    }

    public static final class Pool extends DataLoaderPool {
        protected Enumeration<DataLoader> loaders() {
            return Enumerations.<DataLoader>array(new DataLoader[] { 
                SimpleLoader.getLoader(SimpleLoader.class),
            });
        }
    }

    public static final class SimpleLoader extends MultiFileLoader {
        public SimpleLoader() {
            super(SimpleObject.class.getName());
        }
        protected String displayName() {
            return "SimpleLoader";
        }
        protected FileObject findPrimaryFile(FileObject fo) {
            if (fo.hasExt("prim")) {
                return fo;
            }
            return null;
        }
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new SimpleObject(this, primaryFile);
        }
        protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
            return new FE(obj, primaryFile);
        }
        protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject obj, FileObject secondaryFile) {
            return null;
        }
    }
    
    private static final class FE extends FileEntry.Format {
        public FE(MultiDataObject mo, FileObject fo) {
            super(mo, fo);
        }

        @Override
        protected java.text.Format createFormat(FileObject target, String n, String e) {
            Map m = new HashMap();
            m.put("b", "xxxx");
            return new MapFormat(Collections.unmodifiableMap(m));
        }

    }
    
    public static final class SimpleObject extends MultiDataObject {
        public SimpleObject(SimpleLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }
        
        @Override
        public String getName() {
            return getPrimaryFile().getNameExt();
        }
    }

}
