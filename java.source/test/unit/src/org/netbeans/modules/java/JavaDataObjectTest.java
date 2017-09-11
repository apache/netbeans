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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */

package org.netbeans.modules.java;

import org.netbeans.junit.NbTestCase;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.test.MockLookup;

/**
 *
 * @author Jan Lahoda
 */
public class JavaDataObjectTest extends NbTestCase {
    
    public JavaDataObjectTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        MockLookup.init();
    }

    public void testJES() throws Exception {
        MockLookup.setInstances(JavaDataLoader.getLoader(JavaDataLoader.class));
        
        FileSystem fs = FileUtil.createMemoryFileSystem();
        FileObject f = fs.getRoot().createData("test.java");
        
        DataObject od = DataObject.find(f);
        
        assertTrue(od instanceof JavaDataObject);
        
        Object c = od.getCookie(EditorCookie.class);
        
//        assertTrue(c instanceof JavaDataObject.JavaEditorSupport);
        assertTrue(c == od.getCookie(OpenCookie.class));
        
        assertTrue(c == od.getLookup().lookup(EditorCookie.class));
        assertTrue(c == od.getLookup().lookup(OpenCookie.class));
        assertTrue(c == od.getLookup().lookup(CloneableEditorSupport.class));
    }
}
