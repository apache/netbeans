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

package org.openide.filesystems.test;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import static org.junit.Assert.assertTrue;

/**
 * Common utility methods for massaging and inspecting files from tests.
 */
public class TestFileUtils {

    private TestFileUtils() {}

    /**
     * Create a new data file with specified initial contents.
     * No file events should be fired until the resulting file is complete (see {@link FileObject#createAndOpen}).
     * @param root a root folder which should already exist
     * @param path a /-separated path to the new file within that root
     * @param body the complete contents of the new file (in UTF-8 encoding)
     */
    public static FileObject writeFile(FileObject root, String path, String body) throws IOException {
        int slash = path.lastIndexOf('/');
        if (slash != -1) {
            root = FileUtil.createFolder(root, path.substring(0, slash));
            path = path.substring(slash + 1);
        }
        FileObject existing = root.getFileObject(path);
        OutputStream os = existing != null ? existing.getOutputStream() : root.createAndOpen(path);
        try {
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            pw.print(body);
            pw.flush();
        } finally {
            os.close();
        }
        return root.getFileObject(path);
    }

    /**
     * Create a new ZIP file.
     * No file events should be fired until the resulting file is complete (see {@link FileObject#createAndOpen}).
     * @param root a root folder which should already exist
     * @param path a /-separated path to the new ZIP file within that root
     * @param entries a list of entries in the form of "filename:UTF8-contents"; parent dirs created automatically
     * @return the newly created ZIP file (use {@link FileUtil#getArchiveRoot} if you want the root entry)
     * @throws IOException for the usual reasons
     */
    public static FileObject writeZipFile(FileObject root, String path, String... entries) throws IOException {
        int slash = path.lastIndexOf('/');
        if (slash != -1) {
            root = FileUtil.createFolder(root, path.substring(0, slash));
            path = path.substring(slash + 1);
        }
        FileObject existing = root.getFileObject(path);
        OutputStream os = existing != null ? existing.getOutputStream() : root.createAndOpen(path);
        try {
            org.openide.util.test.TestFileUtils.writeZipFile(os, entries);
        } finally {
            os.close();
        }
        return root.getFileObject(path);
    }

    /**
     * Make sure the timestamp on a file changes.
     * @param f a file to touch (make newer)
     * @param ref if not null, make f newer than this file; else make f newer than it was before
     */
    public static void touch(FileObject f, FileObject ref) throws IOException, InterruptedException {
        org.openide.util.test.TestFileUtils.touch(FileUtil.toFile(f), ref != null ? FileUtil.toFile(ref) : null);
        f.refresh();
    }

    /**
     * Read the contents of a file as a single string.
     * @param a data file
     * @return its contents (in UTF-8 encoding)
     * @deprecated Use file.asText()
     */
    @Deprecated
    public static String readFile(FileObject file) throws IOException {
        return file.asText("UTF-8");
    }

    /**
     * Assert that the contents of a file (in UTF-8) include the specified text as a substring.
     * @param file an existing data file
     * @param contents a substring expected to be found in it
     */
    public static void assertContains(FileObject file, String contents) throws IOException {
        String text = file.asText("UTF-8");
        assertTrue("Found '" + contents + "' in '" + text + "'", text.contains(contents));
    }

}
