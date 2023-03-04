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
package org.netbeans.modules.web.common.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import static org.junit.Assert.*;
import org.netbeans.api.project.Project;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.project.FileOwnerQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.test.MockLookup;

public class WebServerTest extends NbTestCase {

    private Project testProject1;
    private FileObject siteRoot1;
    private FileObject fooHtml;
    private Project testProject2;
    private Project testProject3;
    private FileObject siteRoot2;
    private FileObject siteRoot3;
    private FileObject barHtml;

    public WebServerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockLookup.init();
        FileObject fo = FileUtil.toFileObject(getWorkDir());
        fo = fo.createFolder(""+System.currentTimeMillis());
        FileObject proj1 = FileUtil.createFolder(fo, "proj1");
        siteRoot1 = FileUtil.createFolder(proj1, "site_a");
        OutputStream os = siteRoot1.createAndOpen("foo.html");
        os.write("I'm foo_l".getBytes());
        os.close();
        fooHtml = siteRoot1.getFileObject("foo.html");
        FileObject proj2 = FileUtil.createFolder(fo, "proj2");
        siteRoot2 = FileUtil.createFolder(proj2, "site_b");
        os = siteRoot2.createAndOpen("bar.html");
        os.write("I'm bar_ley".getBytes());
        os.close();
        barHtml = siteRoot2.getFileObject("bar.html");
        os = siteRoot2.createAndOpen("foo.html");
        os.write("I'm fool number Dva".getBytes());
        os.close();
        testProject1 = new TestProject(proj1);
        testProject2 = new TestProject(proj2);

        FileObject proj3 = FileUtil.createFolder(fo, "proj3");
        siteRoot3 = FileUtil.createFolder(proj2, "site_c");
        os = siteRoot3.createAndOpen("foo.html");
        os.write("I'm fool 3x3".getBytes());
        os.close();
        testProject3 = new TestProject(proj3);

        MockLookup.setInstances(new FileOwnerQueryImpl(testProject1, testProject2));
    }

    public void testWebServer() throws Exception {
        WebServer ws = WebServer.getWebserver();

        assertNull(ws.toServer(fooHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/foo.html")));
        ws.start(testProject1, siteRoot1, "/");
        assertEquals(new URL("http://localhost:8383/foo.html"), ws.toServer(fooHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/none.html")));
        assertEquals(fooHtml, ws.fromServer(new URL("http://localhost:8383/foo.html")));

        assertNull(ws.toServer(barHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/xxx/bar.html")));
        ws.start(testProject2, siteRoot2, "/xxx");
        assertEquals(new URL("http://localhost:8383/xxx/bar.html"), ws.toServer(barHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/xxx/none.html")));
        assertEquals(barHtml, ws.fromServer(new URL("http://localhost:8383/xxx/bar.html")));

        assertURLContent("http://localhost:8383/foo.html", "I'm foo_l");
        assertURLContent("http://localhost:8383/xxx/bar.html", "I'm bar_ley");
        assertURLContent("http://localhost:8383/xxx/foo.html", "I'm fool number Dva");

        assertURLDoesNotExist("http://localhost:8383/xxx/none.html");
        assertURLDoesNotExist("http://localhost:8383/none/a.html");

        ws.stop(testProject1);
        assertNull(ws.toServer(fooHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/foo.html")));
        assertEquals(new URL("http://localhost:8383/xxx/bar.html"), ws.toServer(barHtml));
        assertEquals(barHtml, ws.fromServer(new URL("http://localhost:8383/xxx/bar.html")));

        ws.stop(testProject2);
        assertNull(ws.toServer(fooHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/foo.html")));
        assertNull(ws.toServer(barHtml));
        assertNull(ws.fromServer(new URL("http://localhost:8383/xxx/bar.html")));

        assertURLDoesNotExist("http://localhost:8383/foo.html");
        assertURLDoesNotExist("http://localhost:8383/xxx/bar.html");
    }

    public void testWebServer2() throws Exception {
        WebServer ws = WebServer.getWebserver();

        ws.start(testProject1, siteRoot1, "/app1");
        assertURLContent("http://localhost:8383/app1/foo.html", "I'm foo_l");
        assertMimetype("http://localhost:8383/app1/foo.html", "text/html");

        ws.start(testProject2, siteRoot2, "/app2");
        assertURLContent("http://localhost:8383/app2/foo.html", "I'm fool number Dva");

        ws.start(testProject3, siteRoot3, "/app3");
        assertURLContent("http://localhost:8383/app3/foo.html", "I'm fool 3x3");

        ws.start(testProject1, siteRoot1, "/app");
        assertURLContent("http://localhost:8383/app/foo.html", "I'm foo_l");
        assertURLContent("http://localhost:8383/app3/foo.html", "I'm fool 3x3");
        assertURLDoesNotExist("http://localhost:8383/app1/foo.html");

        ws.start(testProject2, siteRoot2, "/app");
        assertURLContent("http://localhost:8383/app/foo.html", "I'm fool number Dva");
        assertURLContent("http://localhost:8383/app3/foo.html", "I'm fool 3x3");
        assertURLDoesNotExist("http://localhost:8383/app2/foo.html");

        ws.start(testProject1, siteRoot1, "/app");
        assertURLContent("http://localhost:8383/app/foo.html", "I'm foo_l");
        assertURLContent("http://localhost:8383/app3/foo.html", "I'm fool 3x3");

        ws.start(testProject2, siteRoot2, "/app");
        assertURLContent("http://localhost:8383/app/foo.html", "I'm fool number Dva");
        assertURLContent("http://localhost:8383/app3/foo.html", "I'm fool 3x3");
    }

    private void assertURLContent(String url, String content) throws Exception {
        InputStream is = new URL(url).openStream();
        byte[] b = new byte[30];
        is.read(b);
        is.close();
        assertEquals(content, new String(b).trim());
    }
    
    private void assertMimetype(String url, String mimetype) throws Exception {
        URLConnection is = new URL(url).openConnection();
        is.getInputStream().close();
        String resultMimeType = is.getHeaderField("Content-Type");
        assertEquals(mimetype, resultMimeType);
    }

    private void assertURLDoesNotExist(String url) throws Exception {
        boolean ok = false;
        try {
            byte[] b = new byte[30];
            InputStream is = new URL(url).openStream();
            is.read(b);
        } catch (IOException ex) {
            ok = true;
        }
        assertTrue(url, ok);

    }

    static class TestProject implements Project {

        private FileObject fo;

        public TestProject(FileObject fo) {
            this.fo = fo;
        }

        @Override
        public FileObject getProjectDirectory() {
            return fo;
        }

        @Override
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }

    }

    static class FileOwnerQueryImpl implements FileOwnerQueryImplementation {

        private Project testProject1;
        private Project testProject2;

        public FileOwnerQueryImpl(Project testProject1, Project testProject2) {
            this.testProject1 = testProject1;
            this.testProject2 = testProject2;
        }

        @Override
        public Project getOwner(URI file) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public Project getOwner(FileObject file) {
            if (file.getParent().equals(testProject1.getProjectDirectory())) {
                return testProject1;
            }
            if (file.getParent().equals(testProject2.getProjectDirectory())) {
                return testProject2;
            }
            return null;
        }

    }
}
