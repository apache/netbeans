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
package org.netbeans.modules.groovy.grailsproject.actions;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;
import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Martin Janicek
 */
public class GotoBaseActionTest extends NbTestCase {

    private static class GotoBaseActionImpl extends GotoBaseAction {

        public GotoBaseActionImpl(String name) {
            super(name);
        }

        @Override
        protected FileObject getTargetFO(String fileName, FileObject sourceFO) {
            return null;
        }

        @Override
        protected String getTargetFilePath(String filename, FileObject sourceFO) {
            return null;
        }
    }

    private static final GotoBaseAction gotoAction = new GotoBaseActionImpl("GotoBaseActionTest");


    public GotoBaseActionTest(String name) {
        super(name);
    }

    @Test
    public void testFindPackagePath1() throws IOException {
        File folder = new File(getWorkDir(), "/whatever/grails-app/domain/packagename");
        File file = new File(folder, "SomeDomainClass.groovy");

        setupFolder(folder);
        setupTestFile(file);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file)); //NOI18N

        assertEquals("packagename", gotoAction.findPackagePath(fo));
    }

    @Test
    public void testFindPackagePath2() throws IOException {
        File folder = new File(getWorkDir(), "/whatever/grails-app/domain/packagename/secondarypkg");
        File file = new File(folder, "AnotherDomainClass.groovy");

        setupFolder(folder);
        setupTestFile(file);
        FileObject fo = FileUtil.toFileObject(FileUtil.normalizeFile(file)); //NOI18N

        assertEquals("packagename" + File.separator + "secondarypkg", gotoAction.findPackagePath(fo));
    }

    private void setupFolder(File folder) {
        if (!folder.exists()) {
            if (!folder.mkdirs()) {
                // If we are not able to create folders, we can't continue
                fail("Test folder couldn't be created for some reason!");
            }
        }
    }

    private void setupTestFile(File testFile) {
        if (!testFile.exists()) {
            try {
                if (!testFile.createNewFile()) {
                    fail("Testfile couldn't be created for some reason!");
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }
}
