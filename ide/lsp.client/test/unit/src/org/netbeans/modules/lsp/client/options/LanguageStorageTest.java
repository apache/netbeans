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
package org.netbeans.modules.lsp.client.options;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.api.lexer.Language;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.lsp.client.options.LanguageStorage.LanguageDescription;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.Repository;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author lahvac
 */
public class LanguageStorageTest extends NbTestCase {
    
    public LanguageStorageTest(String name) {
        super(name);
    }
    
    public void testStore() throws IOException {
        clearWorkDir();

        FileObject root = FileUtil.toFileObject(getWorkDir());
        FileObject config = root.createFolder("config");
        FileObject data = root.createFolder("data");
        FileSystem testWriteFS = new FileSystem() {
            @Override
            public String getDisplayName() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public boolean isReadOnly() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public FileObject getRoot() {
                throw new UnsupportedOperationException("Not supported.");
            }

            @Override
            public FileObject findResource(String name) {
                return config.getFileObject(name);
            }
        };
        
        Repository repo = new Repository(testWriteFS);

        MockLookup.setInstances(repo);

        assertSame(repo, Repository.getDefault());
        assertNull(MimeLookup.getLookup("text/x-ext-t").lookup(Language.class));

        FileObject grammar = FileUtil.createData(data, "any.json");
        try (OutputStream out = grammar.getOutputStream()) {
            out.write("{ \"scopeName\" : \"test\" }".getBytes(StandardCharsets.UTF_8));
        }
        FileObject testFO = FileUtil.createData(root, "test.txt");
        assertEquals("content/unknown", FileUtil.getMIMEType(testFO));
        DataObject testDO = DataObject.find(testFO);
        assertEquals("org.openide.loaders.DefaultDataObject", testDO.getClass().getName());

        LanguageStorage.store(Arrays.asList(new LanguageDescription("t", "txt", FileUtil.toFile(grammar).getAbsolutePath(), null, "txt", null, false)));
        assertEquals("text/x-ext-t", FileUtil.getMIMEType(testFO));

        DataObject recognized = DataObject.find(testFO);

        assertEquals(GenericDataObject.class, recognized.getClass());
        assertEquals("org.openide.loaders.DefaultDataObject", testDO.getClass().getName()); //ensure the DO cannot be GCed

        Image icon = recognized.getNodeDelegate().getIcon(BeanInfo.ICON_COLOR_16x16);
        String url = ((URL) icon.getProperty("url", null)).getFile();
        assertTrue(url.contains("/org/openide/nodes/defaultNode.png"));
        Language<?> l = MimeLookup.getLookup("text/x-ext-t").lookup(Language.class);
        assertNotNull(l);

        LanguageStorage.store(Arrays.asList(new LanguageDescription("t", "txt", FileUtil.toFile(grammar).getAbsolutePath(), null, "txt", null, false)));

        LanguageStorage.store(Collections.emptyList());
        
        assertEquals("content/unknown", FileUtil.getMIMEType(testFO));
        assertEquals("org.openide.loaders.DefaultDataObject", DataObject.find(testFO).getClass().getName());
        assertEquals(GenericDataObject.class, recognized.getClass()); //ensure the DO cannot be GCed
    }
    
}
