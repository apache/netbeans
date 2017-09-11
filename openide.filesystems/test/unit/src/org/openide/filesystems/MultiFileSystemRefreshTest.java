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

package org.openide.filesystems;

import org.netbeans.junit.*;
import junit.textui.TestRunner;

/**
 * Test that MultiFileSystem does not refresh more than it needs to
 * when you call setDelegates.
 * @see "#29354"
 * @author Jesse Glick
 */
public class MultiFileSystemRefreshTest extends NbTestCase implements FileChangeListener {

    public MultiFileSystemRefreshTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(MultiFileSystemRefreshTest.class));
    }

    private FileSystem fs1, fs2;
    protected void setUp() throws Exception {
        super.setUp();
        fs1 = TestUtilHid.createLocalFileSystem("mfsrefresh1"+getName() + "1", new String[] {
            "a/b/c.txt",
            "a/b/d.txt",
            "e/f.txt",
        });
        fs2 = TestUtilHid.createLocalFileSystem("mfsrefresh2"+getName() + "2", new String[] {
            "e/g.txt",
        });
    }
    protected void tearDown() throws Exception {
        TestUtilHid.destroyLocalFileSystem(getName() + "1");
        TestUtilHid.destroyLocalFileSystem(getName() + "2");
        super.tearDown();
    }
    
    private int count;

    public void testAttributes106242() throws Exception {
        MultiFileSystem mfs = new MultiFileSystem(fs1, fs2);
        FileObject eFs1 = fs1.findResource("e");
        assertNotNull(eFs1);
        FileObject eFs2 = fs2.findResource("e");        
        assertNotNull(eFs2);
        eFs1.setAttribute("e", 100);
        eFs2.setAttribute("e", 200);
        FileObject eMfs = mfs.findResource("e");
        assertEquals(100, eMfs.getAttribute("e"));
        mfs.setDelegates(new FileSystem[] {fs2, fs1});
        assertEquals(200, eMfs.getAttribute("e"));
        mfs.setDelegates(new FileSystem[] {fs1, fs2});
        assertEquals(100, eMfs.getAttribute("e"));        
    }
    
    public void testSetDelegatesFiring() throws Exception {
        MultiFileSystem mfs = new MultiFileSystem(fs1, fs2);
        //mfs.addFileChangeListener(this);
        FileObject a = mfs.findResource("a");
        assertNotNull(a);
        assertEquals(1, a.getChildren().length);
        a.addFileChangeListener(this);
        FileObject e = mfs.findResource("e");
        assertNotNull(e);
        assertEquals(2, e.getChildren().length);
        e.addFileChangeListener(this);
        count = 0;
        mfs.setDelegates(new FileSystem[] {fs1});
        System.err.println("setDelegates done");
        assertEquals(1, a.getChildren().length);
        assertEquals(1, e.getChildren().length);
        assertEquals(1, count);
    }
    
    public void fileAttributeChanged(FileAttributeEvent fe) {
        System.err.println("attr changed: " + fe);
        count++;
    }
    
    public void fileChanged(FileEvent fe) {
        System.err.println("changed: " + fe);
        count++;
    }
    
    public void fileDataCreated(FileEvent fe) {
        System.err.println("created: " + fe);
        count++;
    }
    
    public void fileDeleted(FileEvent fe) {
        System.err.println("deleted: " + fe);
        count++;
    }
    
    public void fileFolderCreated(FileEvent fe) {
        System.err.println("folder created: " + fe);
        count++;
    }
    
    public void fileRenamed(FileRenameEvent fe) {
        System.err.println("renamed: " + fe);
        count++;
    }
    
}
