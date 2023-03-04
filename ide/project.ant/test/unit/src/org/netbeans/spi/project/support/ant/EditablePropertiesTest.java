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
import java.nio.charset.StandardCharsets;
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
            r1 = new InputStreamReader(new FileInputStream(f1), StandardCharsets.ISO_8859_1);
            r2 = new InputStreamReader(new FileInputStream(f2), StandardCharsets.ISO_8859_1);
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
