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
package org.netbeans.modules.refactoring.plugins;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.CopyRefactoring;
import org.netbeans.modules.refactoring.api.RefactoringSession;
import org.netbeans.modules.refactoring.api.SingleCopyRefactoring;
import org.netbeans.modules.refactoring.spi.RefactoringPlugin;
import org.netbeans.modules.refactoring.spi.RefactoringPluginFactory;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.lookup.Lookups;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Ralph Ruijs
 */
public class FilesCopyPluginTest extends NbTestCase {
    
    private FileObject f;
    private FileObject f1;

    public FilesCopyPluginTest(String name) {
        super(name);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        MockLookup.setInstances(new RefactoringPluginFactory() {

            @Override
            public RefactoringPlugin createInstance(AbstractRefactoring refactoring) {
                return new FilesCopyPlugin(refactoring);
            }
        });
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Before
    public void setUp() throws IOException {
        clearWorkDir();
        f = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "test");
        OutputStream outputStream = f.getOutputStream();
        outputStream.write("test".getBytes());
        outputStream.close();
        f1 = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "test1");
        outputStream = f1.getOutputStream();
        outputStream.write("test1".getBytes());
        outputStream.close();
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of preCheck method, of class FilesCopyPlugin.
     */
    @Test
    public void testChecks() {
        FilesCopyPlugin copyPlugin = new FilesCopyPlugin(null);
        assertEquals(null, copyPlugin.preCheck());
        assertEquals(null, copyPlugin.fastCheckParameters());
        assertEquals(null, copyPlugin.checkParameters());
    }

    /**
     * Test of prepare method, of class FilesCopyPlugin.
     */
    @Test
    public void testPrepare() throws Exception {
        RefactoringSession session = RefactoringSession.create("junit");
        CopyRefactoring copyRefactoring = new CopyRefactoring(Lookups.fixed(f));
        File target = new File(getWorkDirPath() + File.separatorChar + "junit" + File.separatorChar);
        copyRefactoring.setTarget(Lookups.fixed(target.toURI().toURL()));
        copyRefactoring.prepare(session);
        session.doRefactoring(true);
        FileObject newOne = copyRefactoring.getContext().lookup(FileObject.class);
        assertEquals(newOne.asText(), f.asText());
        
        session = RefactoringSession.create("junit1");
        target = new File(getWorkDirPath() + File.separatorChar + "junit1" + File.separatorChar);
        copyRefactoring = new CopyRefactoring(Lookups.fixed(f, f1));
        copyRefactoring.setTarget(Lookups.fixed(target.toURI().toURL()));
        copyRefactoring.prepare(session);
        session.doRefactoring(true);
        FileObject[] newOnes = copyRefactoring.getContext().lookup(FileObject[].class);
        assertEquals(newOnes[0].asText(), f.asText());
        assertEquals(newOnes[0].getName(), f.getName());
        assertEquals(newOnes[1].asText(), f1.asText());
        assertEquals(newOnes[1].getName(), f1.getName());
        
        session = RefactoringSession.create("junit2");
        SingleCopyRefactoring singleCopyRefactoring = new SingleCopyRefactoring(Lookups.fixed(f));
        target = new File(getWorkDirPath() + File.separatorChar + "junit" + File.separatorChar);
        singleCopyRefactoring.setTarget(Lookups.fixed(target.toURI().toURL()));
        String newName = "HelloWorld";
        singleCopyRefactoring.setNewName(newName);
        singleCopyRefactoring.prepare(session);
        session.doRefactoring(true);
        FileObject newOneRenamed = singleCopyRefactoring.getContext().lookup(FileObject.class);
        assertEquals(newOneRenamed.asText(), f.asText());
        assertEquals(newOneRenamed.getName(), newName);
    }
}
