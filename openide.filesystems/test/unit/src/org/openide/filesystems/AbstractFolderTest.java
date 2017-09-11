/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
