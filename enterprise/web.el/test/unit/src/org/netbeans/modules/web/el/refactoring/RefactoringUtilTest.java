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

package org.netbeans.modules.web.el.refactoring;

import java.io.IOException;
import javax.lang.model.type.TypeKind;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.parsing.impl.indexing.CacheFolder;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Erno Mononen
 */
public class RefactoringUtilTest extends NbTestCase {

    public RefactoringUtilTest(final String name) {
        super(name);
    }

    @Override
    public void setUp() throws IOException {
        clearWorkDir();
        final FileObject wd = FileUtil.toFileObject(FileUtil.normalizeFile(getWorkDir()));
        assertNotNull(wd);
        final FileObject cf = FileUtil.createFolder(wd, "index");   //NOI18N
        assertNotNull(cf);
        CacheFolder.setCacheFolder(cf);
    }

    @Test
    public void testEncodeAndHighlight() {
        System.out.println(CacheFolder.getCacheFolder()
        );
        String text = "#{bar.foo}";
        OffsetRange expressionOffset = new OffsetRange(0, 10);
        OffsetRange nodeOffset = new OffsetRange(6, 9);
        String result = RefactoringUtil.encodeAndHighlight(text, expressionOffset, nodeOffset);
        assertEquals("#{bar.<b>foo</b>}", result);
    }

    @Test
    public void testEncodeAndHighlight2() {
        String text = "<h v=\"#{bar.foo}#{bar.foo}\"/>";
        OffsetRange expressionOffset = new OffsetRange(6, 16);
        OffsetRange nodeOffset = new OffsetRange(6, 9);
        String result = RefactoringUtil.encodeAndHighlight(text, expressionOffset, nodeOffset);

        expressionOffset = new OffsetRange(16, 26);
        result = RefactoringUtil.encodeAndHighlight(text, expressionOffset, nodeOffset);
        assertEquals("&lt;h v=\"#{bar.foo}#{bar.<b>foo</b>}\"/&gt;", result);
    }

    @Test
    public void testGetPropertyName() {
        assertEquals("foo", RefactoringUtil.getPropertyName("getFoo"));
        assertEquals("foo", RefactoringUtil.getPropertyName("isFoo"));
        assertEquals("foo", RefactoringUtil.getPropertyName("foo"));
        assertEquals("FOO", RefactoringUtil.getPropertyName("getFOO"));
        assertEquals("FOo", RefactoringUtil.getPropertyName("getFOo"));

        assertEquals("gettyFoo", RefactoringUtil.getPropertyName("gettyFoo"));
        assertEquals("issyFoo", RefactoringUtil.getPropertyName("issyFoo"));
    }

    @Test
    public void testGetPropertyNameIsPrefix() throws IOException {
        ClassPath bootCP = JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
        JavaSource js = JavaSource.create(ClasspathInfo.create(bootCP, bootCP, bootCP));
        assertNotNull(js);
        js.runUserActionTask(new Task<CompilationController>() {
            @Override
            public void run(CompilationController info) throws Exception {
                assertEquals("isFoo", RefactoringUtil.getPropertyName("isFoo",
                        info.getElements().getTypeElement("java.lang.Boolean").asType()));
                assertEquals("foo", RefactoringUtil.getPropertyName("isFoo",
                        info.getTypes().getPrimitiveType(TypeKind.BOOLEAN)));
                assertEquals("isFoo", RefactoringUtil.getPropertyName("isFoo",
                        info.getElements().getTypeElement("java.lang.String").asType()));
                assertEquals("isFoo", RefactoringUtil.getPropertyName("isFoo",
                        info.getTypes().getPrimitiveType(TypeKind.INT)));
            }
        }, true);
    }

}