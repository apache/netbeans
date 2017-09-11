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

package org.netbeans.api.project;

import java.net.URL;
import java.util.Date;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Test functionality of TestUtil.
 * @author Jesse Glick
 */
public class TestUtilTest extends NbTestCase {

    public TestUtilTest(String name) {
        super(name);
    }

    public void testCreateFileFromContent() throws Exception {
        URL content = TestUtilTest.class.getResource("TestUtilTest.class");
        assertNotNull("have TestUtilTest.class", content);
        int length = content.openConnection().getContentLength();
        assertTrue("have some length", length > 0);
        FileObject scratch = TestUtil.makeScratchDir(this);
        assertTrue("scratch is a dir", scratch.isFolder());
        assertEquals("scratch is empty", 0, scratch.getChildren().length);
        FileObject a = TestUtil.createFileFromContent(content, scratch, "d/a");
        assertTrue("a is a file", a.isData());
        assertEquals("right path", "d/a", FileUtil.getRelativePath(scratch, a));
        assertEquals("right length", length, (int)a.getSize());
        FileObject b = TestUtil.createFileFromContent(null, scratch, "d2/b");
        assertTrue("b is a file", b.isData());
        assertEquals("right path", "d2/b", FileUtil.getRelativePath(scratch, b));
        assertEquals("b is empty", 0, (int)b.getSize());
        Date created = b.lastModified();
        Thread.sleep(1500); // Unix has coarse timestamp marking
        assertEquals("got same b back", b, TestUtil.createFileFromContent(null, scratch, "d2/b"));
        Date modified = b.lastModified();
        assertTrue("touched and changed timestamp", modified.after(created));
    }
    
}
