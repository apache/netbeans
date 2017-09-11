/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
