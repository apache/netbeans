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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.netbeans.api.templates.CreateDescriptor;
import org.netbeans.api.templates.CreateFromTemplateDecorator;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.test.MockLookup;

/**
 *
 * @author sdedic
 */
public class CreateFromTemplateDecoratorTest extends NbTestCase {

    public CreateFromTemplateDecoratorTest(String name) {
        super(name);
    }
    
    private Deco decorator = new Deco();
    private FileObject root;
    private FileObject fo;
    private FileObject target;
    private DataFolder folder;
    private DataObject obj;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.init();
        root = FileUtil.createMemoryFileSystem().getRoot();
        fo = FileUtil.createData(root, "simpleObject.txt");
        folder = DataFolder.findFolder(target = FileUtil.createFolder(root, "target"));
        obj = DataObject.find(fo);
    }
    
    private boolean decorated;
    private int decoCount;
    
    private List<FileObject> created = new ArrayList<>();
    
    public void testPreCreateDecorator() throws Exception {
        MockLookup.setLayersAndInstances(new Deco() {
            @Override
            public boolean isBeforeCreation() {
                return true;
            }
            
            @Override
            public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
                assertSize("No files should have been created", createdFiles, 0);
                decorated = true;
                assertEquals(0, target.getChildren().length);
                return null;
            }
        });
        Map<String,String> parameters = Collections.singletonMap("type", "empty");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        assertTrue(decorated);
        assertNotNull(n);
    }
    
    public void testPostDecorator() throws Exception {
        MockLookup.setLayersAndInstances(new Deco() {
            @Override
            public boolean isAfterCreation() {
                return true;
            }
            
            @Override
            public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
                assertEquals("Main files is not present", 1, createdFiles.size());
                decorated = true;
                assertEquals(1, target.getChildren().length);
                return null;
            }
        });
        Map<String,String> parameters = Collections.singletonMap("type", "empty");
        DataObject n = obj.createFromTemplate(folder, "complex", parameters);
        assertTrue(decorated);
        assertNotNull(n);
    }

    public void testDecoratorSeesPrecedingFiles() throws Exception {
        MockLookup.setLayersAndInstances(new Deco() {
            @Override
            public boolean isBeforeCreation() {
                return true;
            }
            
            @Override
            public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
                assertEquals("No files should have been created", 0, createdFiles.size());
                decoCount++;
                assertEquals(0, folder.getPrimaryFile().getChildren().length);
                FileObject f = FileUtil.createData(target, "sideEffect1.txt");
                created.add(f);
                return Collections.singletonList(f);
            }
        }, new Deco() {
            @Override
            public boolean isBeforeCreation() {
                return true;
            }
            
            @Override
            public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
                decoCount++;
                assertEquals(1, target.getChildren().length);
                assertEquals(created, createdFiles);
                FileObject f = FileUtil.createData(target, "sideEffect2.txt");
                created.add(f);
                return Collections.singletonList(f);
            }
        },
        new Deco() {
            @Override
            public boolean isAfterCreation() {
                return true;
            }
            
            @Override
            public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
                created.add(0, folder.getPrimaryFile().getFileObject("n.txt"));
                decoCount++;
                assertEquals(3, target.getChildren().length);
                FileObject f = FileUtil.createData(target, "sideEffect3.txt");
                created.add(f);
                return Collections.singletonList(f);
            }
        }, new Deco() {
            @Override
            public boolean isAfterCreation() {
                return true;
            }
            
            @Override
            public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
                decoCount++;
                assertEquals(4,target.getChildren().length);
                assertEquals(created, createdFiles);
                FileObject f = FileUtil.createData(target, "sideEffect4.txt");
                created.add(f);
                return Collections.singletonList(f);
            }
            
        });
        List<FileObject> fos = new FileBuilder(fo, target).name("n").build();
        assertEquals(5, fos.size());
        assertEquals(created, fos);
        assertEquals(4, decoCount);
    }
    
    static class Deco implements CreateFromTemplateDecorator {
        boolean before;
        boolean after;
        Callable<Void> callback;
        
        @Override
        public boolean isBeforeCreation() {
            return before;
        }

        @Override
        public boolean isAfterCreation() {
            return after;
        }

        @Override
        public boolean accept(CreateDescriptor desc) {
            return true;
        }

        @Override
        public List<FileObject> decorate(CreateDescriptor desc, List<FileObject> createdFiles) throws IOException {
            return null;
        }
    }
}
