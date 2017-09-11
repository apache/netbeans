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

package org.netbeans.upgrade;
import java.io.File;
import java.net.URL;

import org.openide.filesystems.FileObject;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.LocalFileSystem;
import org.openide.filesystems.MultiFileSystem;
import org.openide.filesystems.XMLFileSystem;

/** Tests copying of attributes during upgrade when .nbattrs file is stored on the
 * local filesystem while the respective fileobject is stored on the XML filesystem.
 *
 * @author sherold
 */
public final class AutoUpgradeTest extends org.netbeans.junit.NbTestCase {
    public AutoUpgradeTest (String name) {
        super (name);
    }
    
    protected void setUp() throws java.lang.Exception {
        super.setUp();
    }
    
    
    public void testDoUpgrade() throws Exception {
        File wrkDir = getWorkDir();
        clearWorkDir();
        File old = new File(wrkDir, "old");
        old.mkdir();
        File config = new File(old, "config");
        config.mkdir();
        
        LocalFileSystem lfs = new LocalFileSystem();
        lfs.setRootDirectory(config);
        // filesystem must not be empty, otherwise .nbattrs file will be deleted :(
        lfs.getRoot().createFolder("test");
        
        String oldVersion = "foo";
        
        URL url = AutoUpgradeTest.class.getResource("layer" + oldVersion + ".xml");
        XMLFileSystem xmlfs = new XMLFileSystem(url);
        
        MultiFileSystem mfs = new MultiFileSystem(
                new FileSystem[] { lfs, xmlfs }
        );
        
        String fooBar = "/foo/bar";
        
        FileObject fooBarFO = mfs.findResource(fooBar);
        String attrName = "color";
        String attrValue = "black";
        fooBarFO.setAttribute(attrName, attrValue);
        
        System.setProperty("netbeans.user", new File(wrkDir, "new").getAbsolutePath());
        
        AutoUpgrade.doUpgrade(old, oldVersion);
        
        FileSystem dfs = FileUtil.getConfigRoot().getFileSystem();
        
        MultiFileSystem newmfs = new MultiFileSystem(
                new FileSystem[] { dfs, xmlfs }
        );
        
        FileObject newFooBarFO = newmfs.findResource(fooBar);
        assertNotNull(newFooBarFO);
        assertEquals(attrValue, newFooBarFO.getAttribute(attrName));
    }
 }
