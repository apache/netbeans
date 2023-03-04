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
package org.netbeans.modules.search.matcher;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author jhavlin
 */
public class DefaultMatcherTest extends NbTestCase {

    private static final Logger LOG = Logger.getLogger(
            DefaultMatcherTest.class.getName());

    public DefaultMatcherTest(String name) {
        super(name);
    }

    public void testHasTextContent() {
        createAndCheckTextContent("UTF-8");
        createAndCheckTextContent("UTF-16");
        createAndCheckTextContent("x-UTF-32BE-BOM");
        createAndCheckTextContent("x-UTF-32LE-BOM");
    }

    private void createAndCheckTextContent(String encoding) {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        try (OutputStream os = root.createAndOpen("file");
                OutputStreamWriter osw = new OutputStreamWriter(os, encoding)) {
            osw.append("Test Text");
            osw.flush();
        } catch (UnsupportedEncodingException eee) {
            LOG.log(Level.INFO, "Unknown encoding {0}", encoding);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        assertTrue("File with encoding " + encoding
                + " was detected as binary file",
                DefaultMatcher.hasTextContent(root.getFileObject("file")));
    }

    public void testHasTextContentWithBinaryContent() {
        FileObject root = FileUtil.createMemoryFileSystem().getRoot();
        try (OutputStream os = root.createAndOpen("file")) {
            os.write(new byte[]{90, 98, 88, 97, 94, 0, 1, 2, 4, 5, 4, 6});
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        assertFalse("Binary file was detected as textual file",
                DefaultMatcher.hasTextContent(root.getFileObject("file")));
    }
}
