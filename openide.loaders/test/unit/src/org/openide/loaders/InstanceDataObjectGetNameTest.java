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
