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

package org.apache.tools.ant.module.api.support;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

// XXX tests needed: runTarget (perhaps)

/**
 * Test functionality of ActionUtils.
 * @author Jesse Glick
 */
public class ActionUtilsTest extends NbTestCase {
    
    public ActionUtilsTest(String name) {
        super(name);
    }
    
    private FileObject dir, f1, f1form, f2, subdir, f3, fx, subdir2, f3a, f4, subsubdir, f5, f5a;
    private DataObject d1, d2, d3, dx;
    private Node n1, n2, n3, nx;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        clearWorkDir();
        dir = FileUtil.toFileObject(getWorkDir());
        f1 = dir.createData("f1.data");
        f1form = dir.createData("f1.form");
        f2 = dir.createData("f2");
        subdir = dir.createFolder("sub");
        f3 = subdir.createData("f3.data");
        f3a = subdir.createData("f3a.data");
        subdir2 = dir.createFolder("subdir2");
        f4 = subdir2.createData("f3.nondata");
        subsubdir = subdir2.createFolder("sub");
        f5 = subdir2.createData("f1.data");
        f5a = subsubdir.createData("f3.data");
        fx = new XMLFileSystem().getRoot();
        d1 = DataObject.find(f1);
        d2 = DataObject.find(f2);
        d3 = DataObject.find(f3);
        dx = DataObject.find(fx);
        n1 = d1.getNodeDelegate();
        n2 = d2.getNodeDelegate();
        n3 = d3.getNodeDelegate();
        nx = dx.getNodeDelegate();
    }
    
    public void testFindSelectedFiles() throws Exception {
        assertEquals("one selected file", Collections.singletonList(f1), filesFrom(new Node[] {n1}, null, null, true));
        assertEquals("two selected files", Arrays.asList(new FileObject[] {f1, f2}), filesFrom(new Node[] {n1, n2}, null, null, true));
        assertEquals("zero selection", null, filesFrom(new Node[0], null, null, true));
        assertEquals("not a file selection", null, filesFrom(new Node[] {Node.EMPTY}, null, null, true));
        assertEquals("not a disk file", null, filesFrom(new Node[] {nx}, null, null, true));
        assertEquals("order significant", Arrays.asList(new FileObject[] {f2, f1}), filesFrom(new Node[] {n2, n1}, null, null, true));
        assertEquals("one disk file", Collections.singletonList(f1), filesFrom(new Node[] {n1, nx}, null, null, false));
        assertEquals("one non-disk file", null, filesFrom(new Node[] {n1, nx}, null, null, true));
        assertEquals("one *.data", Collections.singletonList(f1), filesFrom(new Node[] {n1, n2}, null, ".data", false));
        assertEquals("one not *.data", null, filesFrom(new Node[] {n1, n2}, null, ".data", true));
        assertEquals("one file in sub/", Collections.singletonList(f3), filesFrom(new Node[] {n1, n3}, subdir, null, false));
        assertEquals("one not in sub/", null, filesFrom(new Node[] {n1, n3}, subdir, null, true));
        assertEquals("one sub/*.data", Collections.singletonList(f3), filesFrom(new Node[] {n3}, subdir, ".data", true));
        assertEquals("duplicates removed (cf. #50644)", Collections.singletonList(f1), filesFrom(new Node[] {n1, n1}, null, null, true));
        assertEquals("duplicates removed #2 (cf. #50644)", Arrays.asList(new FileObject[] {f1, f2}), filesFrom(new Node[] {n1, n2, n1}, null, null, true));
        assertEquals("two selected files", Arrays.asList(new FileObject[] {f1, f2}), files2List(ActionUtils.findSelectedFiles(Lookups.fixed(f1, f2), null, null, true)));
        assertEquals("one form, one selection", Collections.singletonList(f1), files2List(ActionUtils.findSelectedFiles(Lookups.fixed(f1, f1form), null, ".data", true)));
    }
    
    private static Lookup context(Node[] sel) {
        Lookup[] delegates = new Lookup[sel.length + 1];
        for (int i = 0; i < sel.length; i++) {
            delegates[i] = sel[i].getLookup();
        }
        delegates[sel.length] = Lookups.fixed((Object[]) sel);
        return new ProxyLookup(delegates);
    }
    
    private static List<FileObject> filesFrom(Node[] sel, FileObject dir, String suffix, boolean strict) {
        return files2List(ActionUtils.findSelectedFiles(context(sel), dir, suffix, strict));
    }
    
    public void testAntIncludesList() throws Exception {
        assertEquals("2 includes", "f1.data,sub/f3.data", ActionUtils.antIncludesList(new FileObject[] {f1, f3}, dir));
        assertEquals("1 include", "f1.data", ActionUtils.antIncludesList(new FileObject[] {f1}, dir));
        assertEquals("no includes", "", ActionUtils.antIncludesList(new FileObject[0], dir));                
        assertEquals("1 folder include","sub/**",ActionUtils.antIncludesList(new FileObject[]{subdir}, dir, true));
        assertEquals("root folder include","**",ActionUtils.antIncludesList(new FileObject[]{dir}, dir, true));        
        assertEquals("2 folder includes","sub/**,subdir2/sub/**",ActionUtils.antIncludesList(new FileObject[]{subdir, subsubdir}, dir, true));
        assertEquals("mixed files and folder includes","sub/f3.data,subdir2/sub/**",ActionUtils.antIncludesList(new FileObject[]{f3, subsubdir}, dir, true));        
        assertEquals("1 folder include","sub/*",ActionUtils.antIncludesList(new FileObject[]{subdir}, dir, false));
        assertEquals("root folder include","*",ActionUtils.antIncludesList(new FileObject[]{dir}, dir, false));        
        assertEquals("2 folder includes","sub/*,subdir2/sub/*",ActionUtils.antIncludesList(new FileObject[]{subdir, subsubdir}, dir, false));
        assertEquals("mixed files and folder includes","sub/f3.data,subdir2/sub/*",ActionUtils.antIncludesList(new FileObject[]{f3, subsubdir}, dir, false));
        assertEquals("antIncludeList(FileObject[], FileObject) delegates to antIncludeList(FileObject[], FileObject, true)",ActionUtils.antIncludesList(new FileObject[]{subdir}, dir) ,ActionUtils.antIncludesList(new FileObject[]{subdir}, dir, true));
        assertEquals("antIncludeList(FileObject[], FileObject) delegates to antIncludeList(FileObject[], FileObject, true)",ActionUtils.antIncludesList(new FileObject[]{dir}, dir),ActionUtils.antIncludesList(new FileObject[]{dir}, dir, true));        
    }
    
    public void testRegexpMapFiles() throws Exception {
        Pattern fromRx = Pattern.compile("\\.data$");
        String toSubst = ".nondata";
        assertEquals("mapped one file", Collections.singletonList(f4), files2List(
            ActionUtils.regexpMapFiles(new FileObject[] {f3, f3a}, subdir, fromRx, subdir2, toSubst, false)));
        assertEquals("did not map one file", null, files2List(
            ActionUtils.regexpMapFiles(new FileObject[] {f3, f3a}, subdir, fromRx, subdir2, toSubst, true)));
        assertEquals("mapped two file", Arrays.asList(new FileObject[] {f5, f5a}), files2List(
            ActionUtils.regexpMapFiles(new FileObject[] {f1, f3}, dir, null, subdir2, null, true)));
        // XXX test that files which match a regexp, but are substituted to be the same thing, still are OK
    }
    
    private static List<FileObject> files2List(FileObject[] files) {
        return files != null ? Arrays.asList(files) : null;
    }
    
}
