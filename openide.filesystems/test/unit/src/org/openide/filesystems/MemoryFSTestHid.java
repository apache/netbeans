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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author Radek Matous
 */
public class MemoryFSTestHid extends TestBaseHid {

    /** Creates a new instance of MemoryFSTestHid */
    public MemoryFSTestHid(String testName) {
        super(testName);
    }

    protected String[] getResources(String testName) {
        return new String[]{};
    }


    public void test58331() throws Exception {
        FileObject p = this.testedFS.getRoot();
        FileObject fo = p.createData("test58331");//NOI18N
        assertEquals(fo.getParent(), p);
        String n = fo.getName();
        fo.delete();
        fo.refresh();
        fo.isFolder(); 
        p.createData(n);
    }

    public void testRootAttributes () throws Exception {
        FileObject file = FileUtil.createData(this.testedFS.getRoot(), "/folder/file");
        assertNotNull(file);
        FileObject root = this.testedFS.getRoot();
        assertNotNull(root);
        file.setAttribute("name", "value");
        assertEquals(file.getAttribute("name"), "value");
        root.setAttribute("rootName", "rootValue");
        assertEquals(root.getAttribute("rootName"), "rootValue");        
    }

    public void testURLs() throws Exception {
        FileObject file = FileUtil.createData(testedFS.getRoot(), "/folder/file");
        OutputStream os = file.getOutputStream();
        os.write("hello".getBytes());
        os.close();
        file.setAttribute("mimeType", "text/x-hello");
        URL u = file.toURL();
        assertEquals("/folder/file", u.getPath());
        URLConnection conn = u.openConnection();
        conn.connect();
        assertEquals(5, conn.getContentLength());
        assertEquals(file.lastModified().getTime(), conn.getLastModified());
        assertEquals("text/x-hello", conn.getContentType());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(conn.getInputStream(), baos);
        assertEquals("hello", baos.toString());
        assertEquals(file, URLMapper.findFileObject(u));
        assertEquals(null, URLMapper.findURL(file, URLMapper.EXTERNAL));
        assertEquals(null, URLMapper.findURL(file, URLMapper.NETWORK));
        assertEquals(u, new URL(file.getParent().toURL(), file.getNameExt()));
        assertEquals(testedFS.getRoot(), URLMapper.findFileObject(testedFS.getRoot().toURI().toURL()));
        assertEquals(file.getParent(), URLMapper.findFileObject(file.getParent().toURI().toURL()));
    }

}
