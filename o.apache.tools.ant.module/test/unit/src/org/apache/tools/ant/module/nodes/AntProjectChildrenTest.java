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

package org.apache.tools.ant.module.nodes;

import java.util.Arrays;
import java.util.List;
import org.apache.tools.ant.module.xml.AntProjectSupport;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 * Test children of an Ant project.
 * @author Jesse Glick
 */
public class AntProjectChildrenTest extends NbTestCase {

    public AntProjectChildrenTest(String name) {
        super(name);
    }
    
    private FileObject testdir;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        testdir = FileUtil.toFileObject(this.getDataDir());
        assertNotNull("testdir unit/data exists", testdir);
    }
    
//lazy children calculation makes this fail. more or less same code is tested by TargetListerTest
    public void testBasicChildren() throws Exception {
//        FileObject simple = testdir.getFileObject("targetlister/simple.xml");
//        assertNotNull("simple.xml found", simple);
//        assertEquals("correct children of simple.xml",
//            Arrays.asList(new String[] {"described", "-internal", "-internal-described", "main", "undescribed"}),
//            displayNamesForChildrenOf(simple));
    }
//    
    public void testImportedChildren() throws Exception {
//        // #44491 caused this to fail.
//        FileObject importing = testdir.getFileObject("targetlister/importing.xml");
//        assertNotNull("importing.xml found", importing);
//        assertEquals("correct children of importing.xml",
//            Arrays.asList(new String[] {"main", "subtarget1", "subtarget2", "subtarget3", "whatever"}),
//            displayNamesForChildrenOf(importing));
    }
//    
//    private static List<String> displayNamesForChildrenOf(FileObject fo) {
//        Children ch = new AntProjectChildren(new AntProjectSupport(fo));
//        Node[] nodes = ch.getNodes(true);
//        return displayNamesFor(nodes);
//    }
    
    private static List<String> displayNamesFor(Node[] nodes) {
        String[] names = new String[nodes.length];
        for (int i = 0; i < nodes.length; i++) {
            names[i] = nodes[i].getDisplayName();
        }
        return Arrays.asList(names);
    }
    
}
