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
package org.netbeans.modules.project.ui;

import java.util.List;
import org.netbeans.api.templates.FileBuilder;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Contains script engine-dependent test for api.templates module. The Platform does not
 * contain Freemarker script engine, and JS script engine is not available in modern JDK, so it would
 * need GraalJS from webcommon. Placing freemarker-specific tests here.
 * @author sdedic
 */
public class TemplatesTest extends NbTestCase {

    public TemplatesTest(String name) {
        super(name);
    }
    
    /**
     * Checks that the default template handler can interpolate filename using Freemarker,
     * if the freemarker is specified as an engine on a file/folder.
     * @throws Exception 
     */
    public void testInterpolatedFilename() throws Exception {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        FileObject templateFile = FileUtil.getConfigFile("Templates/Test/FolderTemplate"); // NOI18N
        FileBuilder fb = new FileBuilder(templateFile, root)
                .defaultMode(FileBuilder.Mode.COPY)
                .name("Interpolated"); // NOI18N
        List<FileObject> fos = fb.build();
        
        FileObject instRoot = fos.get(0);
        FileObject instFile = fos.get(1);
        FileObject parent = instFile.getParent();
        assertSame(root, instRoot.getParent());
        assertEquals("Interpolated/foo-bar", parent.getPath()); // NOI18N
        assertEquals("App.java", instFile.getNameExt()); // NOI18N
    }
}
