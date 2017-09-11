/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
