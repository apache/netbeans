/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.io.IOException;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;


/**
 *
 * @author jhavlin
 */
public class SpecialSearchGroupTest extends NbTestCase {
    
    public SpecialSearchGroupTest(String name) {
        super(name);
    }
    
    public void testFilePathAsList() throws IOException {
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a = root.createFolder("a");
        FileObject b = a.createFolder("b");
        FileObject c = b.createData("c");
        
//        List<FileObject> path = SpecialSearchGroup.CommonSearchRoot.filePathAsList(c);
//
//        assertEquals(4, path.size());
//
//        assertEquals(root, path.get(0));
//        assertEquals(a, path.get(1));
//        assertEquals(b, path.get(2));
//        assertEquals(c, path.get(3));
    }
    
    public void testFindCommonPath() throws IOException {
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a = root.createFolder("a");
        FileObject b = a.createFolder("b");
        FileObject c = b.createData("c");
        
        FileObject b1 = a.createFolder("b1");
        FileObject b1c = b1.createData("b1c");
        
//        List<FileObject> path1 = filePathAsList(c);
//        List<FileObject> path2 = filePathAsList(b1c);
//
//        List<FileObject> commonPath = findCommonPath(path1, path2);
//
//        assertEquals(2, commonPath.size());
//
//        assertEquals(root, commonPath.get(0));
//        assertEquals(a, commonPath.get(1));
    }
    
    public void testFindCommonPathNegative() throws IOException {
        
        FileObject root1 = FileUtil.createMemoryFileSystem().getRoot();
        FileObject root2 = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a1 = root1.createFolder("a");
        FileObject b1 = a1.createData("b");        
        
        FileObject a2 = root2.createFolder("a");
        FileObject b2 = a2.createData("b");        
        
//        List<FileObject> p1 = filePathAsList(b1);
//        List<FileObject> p2 = filePathAsList(b2);
//
//        List<FileObject> common = findCommonPath(p1, p2);
//
//        assertTrue(common.isEmpty());
    }
    
    public void testFindCommonPathObject() throws IOException {
        
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a = root.createFolder("a");
        FileObject b = a.createFolder("b");
        FileObject c = b.createData("c");
        
        FileObject b1 = a.createFolder("b1");
        FileObject b1c = b1.createData("b1c");
        
//        SpecialSearchGroup.CommonSearchRoot csr = new SpecialSearchGroup.CommonSearchRoot(1);
//
//        csr.update(c);
//        csr.update(b1c);
//
//        assertEquals(a, csr.getFileObject());
    }
    
    public void testFindCommonPathObjectNegative() throws IOException {
        
        FileObject root1 = FileUtil.createMemoryFileSystem().getRoot();
        FileObject root2 = FileUtil.createMemoryFileSystem().getRoot();
        
        FileObject a1 = root1.createFolder("a");
        FileObject b1 = a1.createData("b");        
        
        FileObject a2 = root2.createFolder("a");
        FileObject b2 = a2.createData("b");        
        
//        SpecialSearchGroup.CommonSearchRoot csr = new SpecialSearchGroup.CommonSearchRoot();
//
//        csr.update(b1);
//        csr.update(b2);
//
//        assertNull(csr.getFileObject());
    }
}
