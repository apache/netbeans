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
package org.netbeans.modules.web.core.syntax.completion.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import org.netbeans.test.web.core.syntax.TestBase;

/**
 *
 * @author Martin Fousek <marfous@netbeans.org>
 */
public class JspCompletionItemTest extends TestBase {

    public JspCompletionItemTest(String name) {
        super(name);
    }

    public void testGetStreamForUrl() throws Exception {
        /* What is this for?
        URL url = new URL("http://java.sun.com/jsp/jstl/core");
        InputStream inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        */

        URL url = new URL("jar:file:" + getTestFile("testJarLibrary/file.zip").getPath() + "!/file.txt");
        InputStream inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        assertEquals("testFile", readContentFromIS(inputStreamForUrl));

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file_space.zip").getPath() + "!/file space.txt");
        inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        assertEquals("test file with spaces", readContentFromIS(inputStreamForUrl));

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file space.zip").getPath() + "!/file.txt");
        inputStreamForUrl = JspCompletionItem.getInputStreamForUrl(url);
        assertNotNull(inputStreamForUrl);
        assertEquals("test file with spaces", readContentFromIS(inputStreamForUrl));
    }

    public void testGetHelp() throws Exception {
        Method constructHelpMethod = JspCompletionItem.class.getDeclaredMethod("constructHelp", URL.class);
        constructHelpMethod.setAccessible(true);

        URL url = new URL("jar:file:" + getTestFile("testJarLibrary/file.zip").getPath() + "!/file.txt");
        String help = (String) constructHelpMethod.invoke(null, url);
        assertEquals("testFile", help);

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file_space.zip").getPath() + "!/file space.txt");
        help = (String) constructHelpMethod.invoke(null, url);
        assertEquals("test file with spaces", help.trim());

        url = new URL("jar:file:" + getTestFile("testJarLibrary/file space.zip").getPath() + "!/file.txt");
        help = (String) constructHelpMethod.invoke(null, url);
        assertEquals("test file with spaces", help.trim());
    }

    private String readContentFromIS(InputStream is) throws IOException {
        StringBuilder content = new StringBuilder();

        InputStreamReader isr = new InputStreamReader(is);
        try {
            int data = isr.read();
            while (data != -1) {
                content.append((char) data);
                data = isr.read();
            }
        } finally {
            isr.close();
        }
        return content.toString().trim();
    }

}
