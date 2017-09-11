/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.debugger.jpda.projects;

import java.io.File;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author martin
 */
public class FieldLNCacheTest extends NbTestCase {
    
    public FieldLNCacheTest(String name) {
        super(name);
    }
    
    public void testRelease() throws Exception {
        FieldLNCache fc = new FieldLNCache();
        File testFile = File.createTempFile("test", "tst");
        testFile.deleteOnExit();
        FileObject testFO = FileUtil.toFileObject(testFile);
        fc.putLine("testURL", "testClass", "testField", testFO, 42);
        assertEquals(new Integer(42), fc.getLine("testURL", "testClass", "testField"));
        assertNull(fc.getLine("testURL", "testClass", "testField2"));
        
        WeakReference testFORef = new WeakReference(testFO);
        testFO = null;
        assertGC("FileObject", testFORef);
        assertNull(fc.getLine("testURL", "testClass", "testField"));
        
        testFO = FileUtil.toFileObject(testFile);
        assertNull(fc.getLine("testURL", "testClass", "testField"));
        fc.putLine("testURL", "testClass", "testField", testFO, 42);
        assertEquals(new Integer(42), fc.getLine("testURL", "testClass", "testField"));
        PrintStream printStream = new PrintStream(testFO.getOutputStream());
        printStream.print("Changed.");
        printStream.close();
        assertNull(fc.getLine("testURL", "testClass", "testField")); // is reset after change
        
        fc.putLine("testURL", "testClass", "testField", testFO, 43);
        assertEquals(new Integer(43), fc.getLine("testURL", "testClass", "testField"));
        testFO.delete();
        assertNull(fc.getLine("testURL", "testClass", "testField"));
    }
    
}
