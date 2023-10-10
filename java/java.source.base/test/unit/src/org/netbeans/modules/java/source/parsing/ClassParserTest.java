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
package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.code.Symbol;
import java.io.IOException;
import java.net.URI;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.test.MockMimeLookup;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author Tomas Zezula
 */
public class ClassParserTest extends NbTestCase {

    public ClassParserTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
        clearWorkDir();
        FileUtil.setMIMEType(FileObjects.CLASS, ClassParser.MIME_TYPE);
        MockMimeLookup.setInstances(MimePath.get(ClassParser.MIME_TYPE), new ClassParserFactory());
        TestJavaPlatformProviderImpl.ALLOW_INSTALL_FOLDERS = true;
    }

    @Override
    protected void tearDown() throws Exception {
        TestJavaPlatformProviderImpl.ALLOW_INSTALL_FOLDERS = false;
        super.tearDown();
    }

    public void testFileFromCTSym() throws IOException {
        final JavaPlatform jp = JavaPlatformManager.getDefault().getDefaultPlatform();
        assertNotNull(jp);
        final ClasspathInfo cpInfo = ClasspathInfo.create(jp.getBootstrapLibraries(), ClassPath.EMPTY, ClassPath.EMPTY);
        assertNotNull(cpInfo);
        final JavaSource js = JavaSource.create(cpInfo);
        final URI[] stackURI = new URI[1];
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(final CompilationController cc) throws Exception {
                final TypeElement stack = cc.getElements().getTypeElement("java.util.Stack");  //NOI18N
                assertNotNull(stack);
                stackURI[0] = ((Symbol.ClassSymbol)stack).classfile.toUri();
            }
        }, true);
        assertNotNull(stackURI[0]);
        final FileObject stackFo = URLMapper.findFileObject(stackURI[0].toURL());
        assertNotNull(stackFo);
        JavaSource cs = JavaSource.forFileObject(stackFo);
        assertNotNull(cs);
        cs.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(final CompilationController cc) throws Exception {
                final TypeElement stack = cc.getElements().getTypeElement("java.util.Stack");  //NOI18N
                assertNotNull(stack);
            }
        }, true);
    }
}
