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
package org.netbeans.modules.versioning.spi;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;

import java.io.File;
import java.util.*;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.StatusDecorator;

/**
 * Versioning SPI unit tests of VCSAnnotator.
 * 
 * @author Maros Sandor
 */
public class VCSAnnotatorTest extends NbTestCase {
    
    private File dataRootDir;

    public VCSAnnotatorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        dataRootDir = getWorkDir();
    }

    public void testAnnotator() throws FileStateInvalidException {
        FileObject fo = FileUtil.toFileObject(dataRootDir);
        FileSystem fs = fo.getFileSystem();
        StatusDecorator status = fs.getDecorator();
        
        Set<FileObject> sof = new HashSet<FileObject>();
        sof.add(fo);
        String annotatedName = status.annotateName("xxx", sof);
        assertEquals(annotatedName, "xxx");

        annotatedName = status.annotateName("annotate-me", sof);
        assertEquals(annotatedName, "annotate-me");
    }
}
