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

package org.netbeans.spi.project.support.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;

// here because AntBasedTestUtil.countTextDiffs cannot be moved to openide.util
public class EditablePropertiesTest extends NbTestCase {

    public EditablePropertiesTest(String name) {
        super(name);
    }
    
    // test that modifications changes only necessary parts
    public void testVersionability() throws Exception {
        clearWorkDir();
        
        EditableProperties ep = loadTestProperties();
        
        EditableProperties ep2 = ep.cloneProperties();
        ep2.setProperty("key24", "new value of key 24");
        String dest = getWorkDirPath()+File.separatorChar+"mod1.properties";
        saveProperties(ep2, dest);
        int res[] = compare(filenameOfTestProperties(), dest);
        assertEquals("One line modified", 1, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.setProperty("key23", "new value of key23");
        dest = getWorkDirPath()+File.separatorChar+"mod2.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("Four lines modified", 4, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.put("newkey", "new value");
        dest = getWorkDirPath()+File.separatorChar+"mod3.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("One line added", 1, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        assertNotNull(ep2.get("key14"));
        ep2.remove("key14");
        assertNull(ep2.get("key14"));
        dest = getWorkDirPath()+File.separatorChar+"mod4.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("Two lines removed", 2, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.setProperty("key21", new String[]{"first line;", "second line;", "third line"});
        dest = getWorkDirPath()+File.separatorChar+"mod5.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("Four lines modified", 4, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        ep2.setProperty("key21", "first line;second line;third line");
        String dest2 = getWorkDirPath()+File.separatorChar+"mod6.properties";
        saveProperties(ep2, dest2);
        res = compare(dest, dest2);
        assertEquals("Four lines modified", 4, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
    }

    // test that changing comments work and modify only comments
    public void testComment() throws Exception {
        clearWorkDir();
        
        EditableProperties ep = loadTestProperties();
        
        EditableProperties ep2 = ep.cloneProperties();
        ep2.setComment("key10", new String[]{"# this is new comment for property key 10"}, false);
        String dest = getWorkDirPath()+File.separatorChar+"comment1.properties";
        saveProperties(ep2, dest);
        int res[] = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("One line added", 1, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.setComment("key1", new String[]{"# new comment", "# new comment second line"}, true);
        dest = getWorkDirPath()+File.separatorChar+"comment2.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No lines modified", 0, res[0]);
        assertEquals("Two lines added", 2, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.setComment("key26", new String[]{"# changed comment"}, false);
        dest = getWorkDirPath()+File.separatorChar+"comment3.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("One line modified", 1, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.setComment("key25", new String[]{"# one line comment"}, false);
        dest = getWorkDirPath()+File.separatorChar+"comment4.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("Two lines modified", 2, res[0]);
        assertEquals("No lines added", 0, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
        ep2 = ep.cloneProperties();
        ep2.setComment("key26", ep2.getComment("key26"), true);
        dest = getWorkDirPath()+File.separatorChar+"comment5.properties";
        saveProperties(ep2, dest);
        res = compare(filenameOfTestProperties(), dest);
        assertEquals("No line modified", 0, res[0]);
        assertEquals("One line added", 1, res[1]);
        assertEquals("No lines removed", 0, res[2]);
        
    }

    
    // helper methods:
    
    
    private String filenameOfTestProperties() {
        // #50987: never use URL.path for this purpose...
        return Utilities.toFile(URI.create(EditablePropertiesTest.class.getResource("data/test.properties").toExternalForm())).getAbsolutePath();
    }
    
    private EditableProperties loadTestProperties() throws IOException {
        URL u = EditablePropertiesTest.class.getResource("data/test.properties");
        EditableProperties ep = new EditableProperties(false);
        InputStream is = u.openStream();
        try {
            ep.load(is);
        } finally {
            is.close();
        }
        return ep;
    }
    
    private void saveProperties(EditableProperties ep, String path) throws Exception {
        OutputStream os = new FileOutputStream(path);
        try {
            ep.store(os);
        } finally {
            os.close();
        }
    }

    private int[] compare(String f1, String f2) throws Exception {
        Reader r1 = null;
        Reader r2 = null;
        try {
            r1 = new InputStreamReader(new FileInputStream(f1), "ISO-8859-1");
            r2 = new InputStreamReader(new FileInputStream(f2), "ISO-8859-1");
            return AntBasedTestUtil.countTextDiffs(r1, r2);
        } finally {
            if (r1 != null) {
                r1.close();
            }
            if (r2 != null) {
                r2.close();
            }
        }
    }
    
}
