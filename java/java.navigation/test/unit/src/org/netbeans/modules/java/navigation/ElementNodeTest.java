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

package org.netbeans.modules.java.navigation;

import com.sun.source.util.TreePath;
import java.io.File;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtilsTestUtil;
import org.netbeans.api.java.source.TestUtilities;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.navigation.ElementNode.Description;
import org.netbeans.modules.java.source.TestUtil;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;

/**
 *
 * @author Jan Lahoda
 */
public class ElementNodeTest extends NbTestCase {

    public ElementNodeTest(String name) {
        super(name);
    }

    public void testLookup() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test {}");

        TreePath tp = new TreePath(new TreePath(info.getCompilationUnit()), info.getCompilationUnit().getTypeDecls().get(0));
        TreePathHandle tph = TreePathHandle.create(tp, info);
        Element el = info.getTrees().getElement(tp);

        assertNotNull(el);

        final AtomicInteger counter = new AtomicInteger();
        
        Description d = Description.element(new ClassMemberPanelUI() {
            @Override public FileObject getFileObject() {
                counter.incrementAndGet();
                return info.getFileObject();
            }
        }, "test", ElementHandle.create(el), info.getClasspathInfo(), Collections.emptySet(), -1, false,
            el.getEnclosingElement().getKind() == ElementKind.PACKAGE);
        
        Node n = new ElementNode(d);

        assertEquals("#164874: should not compute FileObject eagerly", 0, counter.get());
        
        assertEquals(info.getFileObject(), n.getLookup().lookup(FileObject.class));
        assertEquals(1, counter.get());

        assertEquals(tph.resolve(info).getLeaf(), n.getLookup().lookup(TreePathHandle.class).resolve(info).getLeaf());
    }
    
    public void testNoFileObject() throws Exception {
        prepareTest("test/Test.java", "package test; public class Test {}");

        TreePath tp = new TreePath(new TreePath(info.getCompilationUnit()), info.getCompilationUnit().getTypeDecls().get(0));
        Element el = info.getTrees().getElement(tp);

        assertNotNull(el);

        final AtomicInteger counter = new AtomicInteger();

        Description d = Description.element(new ClassMemberPanelUI() {
            @Override public FileObject getFileObject() {
                counter.incrementAndGet();
                return null;
            }
        }, "test", ElementHandle.create(el), info.getClasspathInfo(), Collections.emptySet(), -1, false, el.getEnclosingElement().getKind() == ElementKind.PACKAGE);


        Node n = new ElementNode(d);

        assertEquals("#164874: should not compute FileObject eagerly", 0, counter.get());

        assertNull(n.getLookup().lookup(FileObject.class));

        assertEquals(1, counter.get());
    }

    protected void setUp() throws Exception {
        SourceUtilsTestUtil.prepareTest(new String[0], new Object[0]);
    }
    
    private CompilationInfo info;

    private void prepareTest(String filename, String code) throws Exception {
        clearWorkDir();
        
        File work = getWorkDir();
        FileObject workFO = FileUtil.toFileObject(work);

        assertNotNull(workFO);

        FileObject sourceRoot = workFO.createFolder("src");
        FileObject buildRoot  = workFO.createFolder("build");
        FileObject cache = workFO.createFolder("cache");
        FileObject packageRoot = sourceRoot.createFolder("test");

        SourceUtilsTestUtil.prepareTest(sourceRoot, buildRoot, cache);

        FileObject testSource = FileUtil.createData(packageRoot, filename);

        assertNotNull(testSource);

        TestUtilities.copyStringToFile(FileUtil.toFile(testSource), code);

        JavaSource js = JavaSource.forFileObject(testSource);

        assertNotNull(js);

        info = SourceUtilsTestUtil.getCompilationInfo(js, JavaSource.Phase.RESOLVED);

        assertNotNull(info);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

}
