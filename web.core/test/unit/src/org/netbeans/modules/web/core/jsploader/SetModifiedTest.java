/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import javax.swing.text.BadLocationException;
import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class SetModifiedTest extends NbTestCase {

    public SetModifiedTest(String name) {
        super(name);
    }

    public void testSetModified() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("index.jsp");
        DataObject dob = DataObject.find(f);
        System.out.println("dob=" + dob);

        dob.getLookup().lookup(EditorCookie.class).openDocument().insertString(0, "modified", null);
        assertTrue("Should be modified.", dob.isModified());
        dob.setModified(false);
        assertFalse("Should not be modified.", dob.isModified());
        assertNull("Should not have SaveCookie.", dob.getLookup().lookup(SaveCookie.class));
    }

    public void testModifySave() throws Exception {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("test.jsp");
        assertNotNull(fo);
        DataObject obj = DataObject.find(fo);

        assertNotNull(obj);
        assertFalse(obj.isModified());
        assertNull(obj.getCookie(SaveCookie.class));
        
        obj.getCookie(EditorCookie.class).openDocument().insertString(0, "hello", null);
        assertTrue(obj.isModified());
        assertNotNull(obj.getCookie(SaveCookie.class));

        obj.getCookie(SaveCookie.class).save();

        assertFalse(obj.isModified());
        assertNull(obj.getCookie(SaveCookie.class));
    }

    public void testUnmodifyViaSetModified() throws IOException, BadLocationException {
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject fo = fs.getRoot().createData("test.jsp");
        assertNotNull(fo);
        DataObject obj = DataObject.find(fo);

        assertNotNull(obj);
        assertFalse(obj.isModified());
        assertNull(obj.getCookie(SaveCookie.class));

        obj.getCookie(EditorCookie.class).openDocument().insertString(0, "hello", null);
        assertTrue(obj.isModified());
        assertNotNull(obj.getCookie(SaveCookie.class));

        //some QE unit tests needs to silently discard the changed made to the editor document
        obj.setModified(false);

        assertFalse(obj.isModified());
        assertNull(obj.getCookie(SaveCookie.class));
    }
}