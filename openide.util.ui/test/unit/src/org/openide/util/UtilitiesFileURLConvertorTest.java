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

package org.openide.util;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import org.netbeans.junit.*;
import junit.textui.TestRunner;

/** Test <code>File</code> &#8596; <code>URL</code> conversion.
 * @author Jesse Glick
 * @see "#29711"
 */
public class UtilitiesFileURLConvertorTest extends NbTestCase {

    public UtilitiesFileURLConvertorTest(String name) {
        super(name);
    }

    public static void main(String[] args) {
        TestRunner.run(new NbTestSuite(UtilitiesFileURLConvertorTest.class));
    }
    
    public void testFileURLConversion() throws Exception {
        File f = File.createTempFile("foo", ".txt");
        check(f);
        File tmp = f.getParentFile();
        f = new File(tmp, "with a space.txt");
        check(f);
        // May seem strange but (#27330) hashes in paths are not so unheard-of.
        // Importantly, some Unix VC system (which?) uses them.
        f = new File(tmp, "strange#characters.txt");
        check(f);
        f = new File(tmp, "stranger?characters.txt");
        check(f);
    }
    
    private void check(File f) throws Exception {
        f.deleteOnExit();
        URL u = Utilities.toURL(f);
        assertEquals("correct protocol", "file", u.getProtocol());
        System.err.println("URL=" + u);
        //assertTrue(u.toExternalForm().indexOf(f.getName()) != -1);
        File f2 = Utilities.toFile(u);
        assertEquals("converts back to same file", f, f2);
        assertEquals("no dangling references", null, u.getRef());
        assertEquals("no dangling queries", null, u.getQuery());
        OutputStream os = null;
        try {
            os = new FileOutputStream(f);
        } catch (FileNotFoundException e) {
            // OK, OS does not permit such a file
            return;         
        }
        os.write(1);
        os.write(2);
        os.write(3);
        os.write(4);
        os.write(5);
        os.close();
        URLConnection conn = u.openConnection();
        conn.connect();
        assertEquals("URL connection works", 5, conn.getContentLength());
    }
    
}
