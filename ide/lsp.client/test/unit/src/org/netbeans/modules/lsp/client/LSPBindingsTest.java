/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.lsp.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import static junit.framework.TestCase.assertNotNull;
import org.eclipse.lsp4j.ServerCapabilities;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.editor.mimelookup.MimeLookupCacheSPI;
import org.netbeans.modules.lsp.client.spi.LanguageServerProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.MIMEResolver;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

public class LSPBindingsTest {

    @Test
    public void testGetBindingsOnNonProjectFolder() throws Exception {
        MockServices.setServices(MockMimeLookupCacheSPI.class, MockMimeResolver.class);

        FileObject folder = FileUtil.createMemoryFileSystem().getRoot().createFolder("myfolder");
        FileObject file = folder.createData("data.mock-txt");
        assertEquals("application/mock-txt", file.getMIMEType());
        assertNull("No project owner", FileOwnerQuery.getOwner(file));

        LSPBindings bindings = LSPBindings.getBindings(file);
        assertNotNull("Bindings for the projectless file found", bindings);
    }

    public static final class MockMimeLookupCacheSPI extends MimeLookupCacheSPI {
        @Override
        public Lookup getLookup(MimePath mp) {
            assertEquals("application/mock-txt", mp.getPath());
            return Lookups.fixed(new MockLSP());
        }
    }

    public static final class MockLSP implements LanguageServerProvider {
        @Override
        public LanguageServerDescription startServer(Lookup lookup) {
            final MockProcess process = new MockProcess();
            return LanguageServerDescription.create(process.in, process.out, process);
        }
    }

    public final static class MockMimeResolver extends MIMEResolver {

        public MockMimeResolver() {
        }

        @Override
        public String findMIMEType(FileObject fo) {
            return fo.hasExt("mock-txt") ? "application/mock-txt" : null;
        }
    }

    static final class MockProcess extends Process {
        final ByteArrayInputStream in;
        final ByteArrayOutputStream out;

        public MockProcess() {
            this.in = new ByteArrayInputStream(new byte[0]);
            this.out = new ByteArrayOutputStream();
        }

        @Override
        public OutputStream getOutputStream() {
            return out;
        }

        @Override
        public InputStream getInputStream() {
            return in;
        }

        @Override
        public InputStream getErrorStream() {
            return in;
        }

        @Override
        public int waitFor() throws InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public boolean isAlive() {
            StackTraceElement[] stack = new Exception().getStackTrace();
            if (stack[1].getMethodName().equals("initServer")) {
                return false;
            }
            return true;
        }

        @Override
        public int exitValue() {
            return 0;
        }

        @Override
        public void destroy() {
        }
    }
}
