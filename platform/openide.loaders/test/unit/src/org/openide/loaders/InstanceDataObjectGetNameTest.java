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

package org.openide.loaders;

import java.io.File;
import java.io.FileWriter;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;


/** Check that we cache getName
 * @author Jaroslav Tulach
 */
public class InstanceDataObjectGetNameTest extends NbTestCase {
    private DataObject obj;
    private FileSystem fs;

    /** Creates new DataFolderTest */
    public InstanceDataObjectGetNameTest(String name) {
        super (name);
    }
    
    private static String name;
    private static int cnt;
    public static String computeName() {
        cnt++;
        return name;
    }
    
    protected void setUp () throws Exception {
        
        cnt = 0;
        
        File f = new File(getWorkDir(), "layer.xml");
        FileWriter w = new FileWriter(f);
        w.write("<filesystem><file name='x.instance'> ");
        w.write("  <attr name='name' methodvalue='" + InstanceDataObjectGetNameTest.class.getName() + ".computeName'/> ");
        w.write("</file></filesystem> ");
        w.close();

        fs = new MultiFileSystem(new FileSystem[] { 
            FileUtil.createMemoryFileSystem(), 
            new XMLFileSystem(f.toURL())
        });
        FileObject fo = fs.findResource("x.instance");
        assertNotNull(fo);
        
        assertNull(fo.getAttribute("name"));
        assertEquals("One call", 1, cnt);
        // clean
        cnt = 0;

        obj = DataObject.find(fo);
        
        assertEquals("No calls now", 0, cnt);
    }
    
    public void testNameIsCached() throws Exception {
        if (!(obj instanceof InstanceDataObject)) {
            fail("We need IDO : " + obj);
        }
        
        name = "Ahoj";
        assertEquals("We can influence a name", "Ahoj", obj.getName());
        assertEquals("one call", 1, cnt);
        assertEquals("Name stays the same", "Ahoj", obj.getName());
        assertEquals("no new call", 1, cnt);
        
        name = "kuk";
        assertEquals("Name stays the same", "Ahoj", obj.getName());
        assertEquals("no new call", 1, cnt);

        obj.getPrimaryFile().setAttribute("someattr", "new");
        
        assertEquals("Name changes as attribute changes fired", "kuk", obj.getName());
        assertEquals("of course new call is there", 2, cnt);
        
    }
}
