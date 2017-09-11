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

package org.netbeans.modules.java.editor.codegen;

import com.sun.source.util.TreePath;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.ModificationResult;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.WorkingCopy;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.Repository;

/**
 *
 * @author Jan Lahoda
 */
public class GeneratorUtilsTest extends NbTestCase {
    
    public GeneratorUtilsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }

    static void writeIntoFile(FileObject file, String what) throws Exception {
        FileLock lock = file.lock();
        OutputStream out = file.getOutputStream(lock);
        
        try {
            out.write(what.getBytes());
        } finally {
            out.close();
            lock.releaseLock();
        }
    }
    
    static String readFromFile(FileObject fo) throws Exception {
        InputStream in = fo.getInputStream();
        
        int s = (int)fo.getSize();
        byte[] arr = new byte[s];
        assertEquals("All read", s, in.read(arr));
        return new String(arr);
    }
    
    public void testImplementAllAbstractMethods1() throws Exception {
        performTest("package test;\npublic class Test implements Runnable {\npublic Test(){\n}\n }\n", 54, new RunnableValidator());
    }
    
    public static interface Validator {
        
        public void validate(CompilationInfo info);
        
    }
    
    private final class RunnableValidator implements Validator {
        
        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            
            boolean foundRunMethod = false;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("run".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(foundRunMethod);
                        foundRunMethod = true;
                    }
                }
            }
            
            assertTrue(foundRunMethod);
        }
        
    }
    
    private final class SimpleFutureValidator extends FutureValidator {
        
        private String returnTypeName;
        
        public SimpleFutureValidator(String returnTypeName) {
            this.returnTypeName = returnTypeName;
        }
        
        protected TypeMirror returnType(CompilationInfo info) {
            TypeElement returnTypeElement = info.getElements().getTypeElement(returnTypeName);
            
            return returnTypeElement.asType();
        }
    }
    
    private abstract class FutureValidator implements Validator {
        
        protected abstract TypeMirror returnType(CompilationInfo info);

        public void validate(CompilationInfo info) {
            TypeElement test = info.getElements().getTypeElement("test.Test");
            TypeMirror returnType = returnType(info);
            
            boolean hasShortGet = false;
            boolean hasLongGet = false;
            
            for (ExecutableElement ee : ElementFilter.methodsIn(test.getEnclosedElements())) {
                if ("get".equals(ee.getSimpleName().toString())) {
                    if (ee.getParameters().isEmpty()) {
                        assertFalse(hasShortGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasShortGet = true;
                    }
                    if (ee.getParameters().size() == 2) {
                        assertFalse(hasLongGet);
                        assertTrue(info.getTypes().isSameType(returnType, ee.getReturnType()));
                        hasLongGet = true;
                    }
                }
            }
            
            assertTrue(hasShortGet);
            assertTrue(hasLongGet);
        }
        
    }
    
    private void performTest(String sourceCode, final int offset, final Validator validator) throws Exception {
        FileObject root = makeScratchDir(this);
        
        FileObject sourceDir = root.createFolder("src");
        FileObject buildDir = root.createFolder("build");
        FileObject cacheDir = root.createFolder("cache");
        
        FileObject source = sourceDir.createFolder("test").createData("Test.java");
        
        writeIntoFile(source, sourceCode);
        
        SourceUtilsTestUtil.prepareTest(sourceDir, buildDir, cacheDir, new FileObject[0]);
        
        JavaSource js = JavaSource.forFileObject(source);
        
        ModificationResult result = js.runModificationTask(new Task<WorkingCopy>() {

            public void run(WorkingCopy copy) throws Exception {
                copy.toPhase(Phase.RESOLVED);
                TreePath tp = copy.getTreeUtilities().pathFor(offset);
                GeneratorUtils.generateAllAbstractMethodImplementations(copy, tp);
            }
        });
        
        result.commit();
        
        js.runUserActionTask(new Task<CompilationController>() {
            public void run(CompilationController controller) throws Exception {
                System.err.println("text:");
                System.err.println(controller.getText());
                controller.toPhase(Phase.RESOLVED);
                
                assertEquals(controller.getDiagnostics().toString(), 0, controller.getDiagnostics().size());
                
                validator.validate(controller);
            }
        }, true);
    }
    
    /**Copied from org.netbeans.api.project.
     * Create a scratch directory for tests.
     * Will be in /tmp or whatever, and will be empty.
     * If you just need a java.io.File use clearWorkDir + getWorkDir.
     */
    public static FileObject makeScratchDir(NbTestCase test) throws IOException {
        test.clearWorkDir();
        File root = test.getWorkDir();
        assert root.isDirectory() && root.list().length == 0;
        FileObject fo = FileUtil.toFileObject(root);
        if (fo != null) {
            // Presumably using masterfs.
            return fo;
        } else {
            // For the benefit of those not using masterfs.
            LocalFileSystem lfs = new LocalFileSystem();
            try {
                lfs.setRootDirectory(root);
            } catch (PropertyVetoException e) {
                assert false : e;
            }
            Repository.getDefault().addFileSystem(lfs);
            return lfs.getRoot();
        }
    }
    
}
