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
package org.netbeans.modules.java.j2seplatform.libraries;

import java.io.File;
import org.netbeans.modules.java.j2seplatform.AbstractJ2SEAttacherTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;

/**
 *
 * @author sdedic
 */
public class J2SELibrarySourceJavadocAttacherTest extends AbstractJ2SEAttacherTestBase {

    public J2SELibrarySourceJavadocAttacherTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        setupLibraries();
    }

    private FileObject libraryClassRoot;
    
    private void setupLibraries() throws Exception {
        File dir = new File(getBase());
        
        // create library1:
        String libPath = dir.toString() + "/library1";
        File library = LibraryTestUtils.createJar(new File(libPath), "library1.jar", new String[]{"Main.class"});
        File src = new File(libPath+"/src1");
        File javadoc = new File(libPath+"/javadoc1");
        javadoc.mkdir();
        LibraryTestUtils.registerLibrary("library1", library, src, javadoc);
        libraryClassRoot = FileUtil.getArchiveRoot(FileUtil.toFileObject(library));
        classesRootURL = URLMapper.findURL(libraryClassRoot, URLMapper.INTERNAL);
    }
}
