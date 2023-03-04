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

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Enumerations;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/** Test things about shadows and broken shadows, etc.
 * @author Jaroslav Tulach
 */
public class DataShadowLookupTest extends NbTestCase
implements java.net.URLStreamHandlerFactory {
    /** original object */
    private DataObject original;
    /** folder to work with */
    private DataFolder folder;
    
    static {
        // to handle nbfs urls...
      //  java.net.URL.setURLStreamHandlerFactory (new DataShadowLookupTest(null));
        MockLookup.setInstances(new Pool());
    }
    
    public DataShadowLookupTest (String name) {
        super(name);
    }

    protected @Override Level logLevel() {
        return Level.INFO;
    }
    
    protected @Override void setUp() throws Exception {
        
        FileObject[] delete = FileUtil.getConfigRoot().getChildren();
        for (int i = 0; i < delete.length; i++) {
            delete[i].delete();
        }

        
        FileObject fo = FileUtil.createData (FileUtil.getConfigRoot (), getName () + "/folder/original.string");
        assertNotNull(fo);
        original = DataObject.find (fo);
        assertFalse ("Just to be sure that this is not shadow", original instanceof DataShadow);
        assertEquals ("It is the right class", StringObject.class, original.getClass ());
        fo = FileUtil.createFolder (FileUtil.getConfigRoot (), getName () + "/modify");
        assertNotNull(fo);
        assertTrue (fo.isFolder ());
        folder = DataFolder.findFolder (fo);
    }
    
    public java.net.URLStreamHandler createURLStreamHandler(String protocol) {
        if (protocol.equals ("nbfs")) {
            return FileUtil.nbfsURLStreamHandler ();
        }
        return null;
    }
    
    public void testStringIsInLookupOfDataShadow() throws Exception {
        DataShadow shade = original.createShadow(folder);

        {
            String s = original.getNodeDelegate().getLookup().lookup(String.class);
            assertNotNull("String is in the original's lookup", s);
        }
        
        assertSame(shade.getOriginal(), original);
        String s = shade.getNodeDelegate().getLookup().lookup(String.class);
        assertNotNull("String is in the lookup", s);
        assertEquals("It is the name of the original", original.getName(), s);
    }

    private static final class Pool extends DataLoaderPool {
        protected Enumeration<? extends DataLoader> loaders() {
            return Enumerations.singleton(StringLoader.findObject(StringLoader.class, true));
        }
        
    }
    
    private static final class StringLoader extends UniFileLoader {
        public StringLoader() {
            super("org.openide.loaders.DataShadowLookupTest$StringObject");
            getExtensions().addExtension("string");
        }
        
        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new StringObject(this, primaryFile);
        }
        
    } // end of StringLoader
    
    private static final class StringObject extends MultiDataObject {
        public StringObject(StringLoader l, FileObject fo) throws DataObjectExistsException {
            super(fo, l);
        }

        protected @Override Node createNodeDelegate() {
            return new DataNode(this, Children.LEAF, Lookups.singleton(getName()));
        }
    } // end of StringObject
    
    
}
