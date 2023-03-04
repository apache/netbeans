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
package org.openide.filesystems;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class AbstractFolderTest extends NbTestCase {

    public AbstractFolderTest(String name) {
        super(name);
    }
    
    public void testNPEInEarlyRefresh() {
        
        F f = new F();
        f.refresh(null, "some", true);
    }
    
    private static class F extends AbstractFolder {
        private static final FileSystem FS = FileUtil.createMemoryFileSystem();
        
        private final String[] names;

        public F(String... names) {
            super(FS, null, "empty");
            this.names = names;
        }
        
        

        @Override
        void setAttribute(String attrName, Object value, boolean fire) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected String[] list() {
            return names;
        }

        @Override
        void handleDelete(FileLock lock) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        protected AbstractFolder createFile(String name) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void rename(FileLock lock, String name, String ext) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isFolder() {
            return true;
        }

        @Override
        public Date lastModified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isData() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Object getAttribute(String attrName) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setAttribute(String attrName, Object value) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Enumeration<String> getAttributes() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public long getSize() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public InputStream getInputStream() throws FileNotFoundException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public OutputStream getOutputStream(FileLock lock) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileLock lock() throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void setImportant(boolean b) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject createFolder(String name) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public FileObject createData(String name, String ext) throws IOException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isReadOnly() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
}
