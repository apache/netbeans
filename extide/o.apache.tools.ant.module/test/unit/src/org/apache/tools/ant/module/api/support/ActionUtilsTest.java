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
