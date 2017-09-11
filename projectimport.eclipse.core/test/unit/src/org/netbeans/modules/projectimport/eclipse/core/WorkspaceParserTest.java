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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * @author Martin Krauskopf
 */
public final class WorkspaceParserTest extends NbTestCase {
    
    public WorkspaceParserTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
    }
    
    public void testGetLocation() throws Exception {
        String tempFilePath = new File(System.getProperty("java.io.tmpdir"), "tmp").getAbsolutePath();
        assertRightPath(tempFilePath, tempFilePath);
        assertRightPath("URI//file:" + tempFilePath, tempFilePath);
        assertRightPath("URI//whatever:" + tempFilePath, tempFilePath); 
    }
    
    private void assertRightPath(final String rawPath, final String expectedPath) throws IOException {
        byte[] pathB = rawPath.getBytes();
        byte[] locationContent = new byte[18 + pathB.length];
        locationContent[17] = (byte) pathB.length;
        System.arraycopy(pathB, 0, locationContent, 18, pathB.length);
        ByteArrayInputStream bis = new ByteArrayInputStream(locationContent);
        assertEquals("right path", expectedPath, WorkspaceParser.getLocation(bis).getAbsolutePath());
    }
    
    public void testParseJSFLibraryRegistryV2() throws IOException {
        FileObject fo = FileUtil.toFileObject(new File(getDataDir(), "JSFLibraryRegistryV2.xml"));
        FileObject dest = FileUtil.createFolder(new File(getWorkDir(), "wk/.metadata/.plugins/org.eclipse.jst.jsf.core/"));
        FileUtil.copyFile(fo, dest, fo.getName(), fo.getExt());
        Workspace wk = new Workspace(new File(getWorkDir(), "wk"));
        WorkspaceParser parser = new WorkspaceParser(wk);
        parser.parseJSFLibraryRegistryV2();
        List<String> libContent = wk.getUserLibraries().get("my-jsf");
        assertNotNull(libContent);
        assertEquals(6, libContent.size());
        assertEquals("/home/david/netbeans-6.1/enterprise6/modules/ext/jsf-1_2/commons-beanutils.jar", libContent.get(0));
        assertEquals("/home/david/netbeans-6.1/enterprise6/modules/ext/jsf-1_2/commons-collections.jar", libContent.get(1));
        libContent = wk.getUserLibraries().get("last-one");
        assertNotNull(libContent);
        assertEquals(1, libContent.size());
        assertEquals(new File(wk.getDirectory(), "smth/smthC.jar").getPath(), libContent.get(0));
    }
    
}
