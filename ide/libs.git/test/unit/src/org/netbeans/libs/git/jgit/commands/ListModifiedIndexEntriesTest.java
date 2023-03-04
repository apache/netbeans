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
package org.netbeans.libs.git.jgit.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import org.netbeans.libs.git.jgit.AbstractGitTestCase;
import org.netbeans.libs.git.progress.ProgressMonitor;

/**
 *
 * @author ondra
 */
public class ListModifiedIndexEntriesTest extends AbstractGitTestCase {

    private File workDir;
    
    public ListModifiedIndexEntriesTest(String testName) throws IOException {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        workDir = getWorkingDirectory();
    }

    public void testSingleModification () throws Exception {
        File f = new File(workDir, "file");
        f.createNewFile();
        add();
        commit();

        write(f, "modification");

        File[] modifications = getClient(workDir).listModifiedIndexEntries(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);
        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);

        add(f);
        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { f }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new File[] { f }, modifications));
        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new File[] { f }, modifications));
    }

    public void testMultipleModification () throws Exception {
        File f1 = new File(workDir, "file");
        f1.createNewFile();
        File f2 = new File(workDir, "file2");
        f2.createNewFile();
        add();
        commit();

        write(f1, "modification");
        write(f2, "modification 2");

        File[] modifications = getClient(workDir).listModifiedIndexEntries(new File[] { f1, f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);
        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(0, modifications.length);

        add(f1, f2);
        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { f1 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new File[] { f1 }, modifications));

        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { f2 }, NULL_PROGRESS_MONITOR);
        assertEquals(1, modifications.length);
        assertTrue(Arrays.equals(new File[] { f2 }, modifications));

        modifications = getClient(workDir).listModifiedIndexEntries(new File[] { workDir }, NULL_PROGRESS_MONITOR);
        assertEquals(2, modifications.length);
        assertEquals(new HashSet<File>(Arrays.asList(new File[] { f1, f2 })), new HashSet<File>(Arrays.asList(modifications)));
    }
}
