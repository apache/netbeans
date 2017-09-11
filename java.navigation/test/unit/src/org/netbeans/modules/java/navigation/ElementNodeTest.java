/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009-2010 Sun Microsystems, Inc.
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
