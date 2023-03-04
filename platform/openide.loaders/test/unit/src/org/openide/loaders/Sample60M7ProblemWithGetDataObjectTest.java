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
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.text.DataEditorSupport;
import org.openide.util.Lookup;
import org.openide.nodes.Children;
import org.openide.nodes.CookieSet;
import org.openide.util.Enumerations;
import org.openide.util.NbBundle;
import org.openide.util.test.MockLookup;

/** Simulates the deadlock from issue 60917
 * @author Jaroslav Tulach
 */
public class Sample60M7ProblemWithGetDataObjectTest extends NbTestCase {
    
    public Sample60M7ProblemWithGetDataObjectTest(String name) {
        super(name);
    }
    
    @Override
    protected void setUp() throws Exception {
        MockLookup.setInstances(new DataLoaderPool() {
            @Override
            protected Enumeration<? extends DataLoader> loaders() {
                return Enumerations.singleton(DataLoader.getLoader(Sample60M6DataLoader.class));
            }
        });
    }
    
    public void testHasDataObjectInItsLookup() throws Exception {
        FileObject sample = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "sample/S.sample");
        DataObject obj = DataObject.find(sample);
        assertEquals(Sample60M6DataLoader.class, obj.getLoader().getClass());
        
        assertEquals("Object is in its own node's lookup", obj, obj.getNodeDelegate().getLookup().lookup(DataObject.class));
        assertEquals("Object is in its own lookup", obj, obj.getLookup().lookup(DataObject.class));
        assertEquals("Object is own node's cookie", obj, obj.getNodeDelegate().getCookie(DataObject.class));
        assertEquals("Object is own cookie", obj, obj.getCookie(DataObject.class));
    }
    
    static class Sample60M6DataObject extends MultiDataObject
    implements Lookup.Provider {

        public Sample60M6DataObject(FileObject pf, Sample60M6DataLoader loader) throws DataObjectExistsException, IOException {
            super(pf, loader);
            CookieSet cookies = getCookieSet();
            cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        }

        @Override
        protected Node createNodeDelegate() {
            return new Sample60M6DataNode(this, getLookup());
        }

        @Override
        public Lookup getLookup() {
            return getCookieSet().getLookup();
        }
    }

    private static class Sample60M6DataLoader extends UniFileLoader {

        public static final String REQUIRED_MIME = "text/x-sample";

        private static final long serialVersionUID = 1L;

        public Sample60M6DataLoader() {
            super("org.openide.loaders.Sample60M7ProblemWithGetDataObjectTest$Sample60M6DataObject");
        }

        @Override
        protected String defaultDisplayName() {
            return NbBundle.getMessage(Sample60M6DataLoader.class, "LBL_Sample60M6_loader_name");
        }

        @Override
        protected void initialize() {
            super.initialize();
            getExtensions().addExtension("sample");
        }

        protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
            return new Sample60M6DataObject(primaryFile, this);
        }

        @Override
        protected String actionsContext() {
            return "Loaders/" + REQUIRED_MIME + "/Actions";
        }

    }
    private static class Sample60M6DataNode extends DataNode {
        private Sample60M6DataNode(Sample60M6DataObject obj) {
            super(obj, Children.LEAF);
        }
        Sample60M6DataNode(Sample60M6DataObject obj, Lookup lookup) {
            super(obj, Children.LEAF, lookup);
        }
    }
    
}
