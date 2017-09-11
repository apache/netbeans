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

package org.netbeans.modules.apisupport.project.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public class UtilTest extends NbTestCase {
    
    public UtilTest(String name) {
        super(name);
    }

    @Override protected void setUp() throws Exception {
        clearWorkDir();
    }
    
    public void testLoadProperties() throws Exception {
        File props = new File(getWorkDir(), "testing.properties");
        OutputStream propsOS = new FileOutputStream(props);
        PrintWriter pw = new PrintWriter(propsOS);
        try {
            pw.println("property1=some value");
            pw.println("property2=other value");
        } finally {
            pw.close();
        }
        EditableProperties ep = Util.loadProperties(FileUtil.toFileObject(props));
        assertEquals("property1", "some value", ep.getProperty("property1"));
        assertEquals("property2", "other value", ep.getProperty("property2"));
        try {
            File notFile = new File(getWorkDir(), "i_am_not_file");
            notFile.mkdir();
            Util.loadProperties(FileUtil.toFileObject(notFile));
            fail("FileNotFoundException should be thrown");
        } catch (FileNotFoundException fnfe) {
            // fine expected exception has been thrown
        }
    }
    
    public void testStoreProperties() throws Exception {
        FileObject propsFO = FileUtil.createData(FileUtil.toFileObject(getWorkDir()), "testing.properties");
        EditableProperties props = Util.loadProperties(propsFO);
        assertTrue("empty props", props.isEmpty());
        props.setProperty("property1", "some value");
        Util.storeProperties(propsFO, props);
        
        BufferedReader reader = new BufferedReader(
                new FileReader(new File(getWorkDir(), "testing.properties")));
        try {
            assertEquals("stored property", "property1=some value", reader.readLine());
        } finally {
            reader.close();
        }
    }
    
    // XXX testLoadManifest()
    // XXX testStoreManifest()
    
}
