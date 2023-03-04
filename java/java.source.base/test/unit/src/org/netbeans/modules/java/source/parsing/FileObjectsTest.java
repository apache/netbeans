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

package org.netbeans.modules.java.source.parsing;

import com.sun.tools.javac.api.ClientCodeWrapper;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.net.URI;
import java.util.EnumSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import javax.tools.JavaFileObject;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.preprocessorbridge.spi.JavaFileFilterImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Utilities;

/**
 *
 * @author Tomas Zezula
 */
public class FileObjectsTest extends NbTestCase {
    
    private static final String DATA = "public class Test {"+       //NOI18N
        "public static void main (String[] args) {}" +              //NOI18N
         "}";                                                       //NOI18N
    
    private static final String PAD = "/**Filter added filling******************************************************************************************/";     //NOI18N
    
    public FileObjectsTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testRegularGetCharContent () throws Exception {
        final File wd = this.getWorkDir();
        final File testFile = createTestFile (wd);
        JavaFileObject jfo = FileObjects.fileFileObject(testFile, wd, null, null);
        CharSequence content = jfo.getCharContent(true);
        String expectedData = DATA+"\n";
        assertTrue (expectedData.contentEquals(content));
        
        Filter f = new Filter (null);
        jfo = FileObjects.fileFileObject(testFile, wd, f, null);
        content = jfo.getCharContent(true);
        expectedData = DATA+"\n";
        assertTrue (expectedData.contentEquals(content));
        assertEquals(EnumSet.of(Call.READER), f.calls);
        
        f = new Filter (PAD);
        jfo = FileObjects.fileFileObject(testFile, wd, f, null);
        content = jfo.getCharContent(true);
        expectedData = PAD + DATA+"\n";
        assertTrue (expectedData.contentEquals(content));
        assertEquals(EnumSet.of(Call.READER), f.calls);        
    }
    
    public void testZipBaseToUri() {
        final TestZipFileObject test1 = new TestZipFileObject("file:/tmp/00/foo.jar",  //NOI18N
                                                             "a/b/c",              //NOI18N
                                                             "Foo.java",     //NOI18N
                                                             0L);
        final URI uri1 = test1.toUri();
        assertNotNull(uri1);
        assertEquals("jar:file:/tmp/00/foo.jar!/a/b/c/Foo.java", uri1.toString());      //NOI18N
        
        final TestZipFileObject test2 = new TestZipFileObject("file:/tmp/00/foo.jar",  //NOI18N
                                                             "a/b/c",              //NOI18N
                                                             "SIAPI query syntax.html",     //NOI18N
                                                             0L);
        final URI uri2 = test2.toUri();
        assertNotNull(uri2);
        assertEquals("jar:file:/tmp/00/foo.jar!/a/b/c/SIAPI%20query%20syntax.html", uri2.toString());   //NOI18N
    }

    public void testFileObjectNotCreated() throws Exception {
        clearWorkDir();
        
        final File workDir = getWorkDir();
        
        FileUtil.refreshFor(workDir);
        
        final FileObject wd = FileUtil.toFileObject(workDir);
        final FileObject existing = FileUtil.createData(wd, "test/foo/existing.java");
        assertNotNull(existing);
        final javax.tools.FileObject existingFo = FileObjects.sourceFileObject(existing.getURL(), wd);
        assertEquals ("test.foo.existing",((InferableJavaFileObject)existingFo).inferBinaryName());
        try {
            final InputStream in = existingFo.openInputStream();
            in.close();
        } catch (IOException e) {
           assertFalse("InputSream should exist for existing file",true);
        }
        try {
            final OutputStream out = existingFo.openOutputStream();
            out.close();
        } catch (IOException e) {
           assertFalse("OutputStream should exist for existing file",true);
        }
        File nonExistring = new File (new File(new File (workDir,"test"),"foo"),"nonexisting.java");
        final javax.tools.FileObject nonExistingFo = FileObjects.sourceFileObject(Utilities.toURI(nonExistring).toURL(), wd);
        assertEquals ("test.foo.nonexisting",((InferableJavaFileObject)nonExistingFo).inferBinaryName());
        try {
            final InputStream in = nonExistingFo.openInputStream();
            assertFalse("InputSream should not exist for non existing file",true);
        } catch (IOException e) {}
        try {
            final OutputStream out = nonExistingFo.openOutputStream();
            out.close();
        } catch (IOException e) {
           assertFalse("OutputStream should exist for existing file",true);
        }
        try {
            final InputStream in = nonExistingFo.openInputStream();
            in.close();
        } catch (IOException e) {
            assertFalse("InputSream should exist for non existing file after OutputStream taken",true);
        }

    }
    private static enum Call {
        READER,
        WRITER,
        CHARCONTENT
    }    
    
    private static class Filter implements JavaFileFilterImplementation {
        
        final Set<Call> calls = EnumSet.noneOf(Call.class);
        final String prepend;
        
        public Filter (final String prepend) {            
            this.prepend = prepend;
        }

        public Reader filterReader(Reader r) {
            calls.add(Call.READER);
            if (this.prepend == null) {
                return r;
            }
            else {
                return new CompositeReader (new StringReader(this.prepend),r);
            }
        }

        public CharSequence filterCharSequence(CharSequence charSequence) {
            calls.add(Call.CHARCONTENT);
            return charSequence;
        }

        public Writer filterWriter(Writer w) {
            calls.add(Call.WRITER);
            return w;
        }

        public void addChangeListener(ChangeListener listener) {            
        }

        public void removeChangeListener(ChangeListener listener) {           
        }
        
    }
    
    private static class CompositeReader extends Reader {
        
        private final Reader[] rds;
        int index;
        
        public CompositeReader (final Reader...rds) {
            assert rds != null;
            this.rds = rds;
            this.index = 0;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (index==rds.length) {
                return -1;
            }
            int read = rds[index].read(cbuf, off, len);
            if (read == -1) {
                index++;
                if (index < rds.length) {
                    return 0;
                }
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            for (Reader r : rds) {
                r.close();
            }
        }
        
    }
    
    @ClientCodeWrapper.Trusted
    private static class TestZipFileObject extends FileObjects.ZipFileBase {
        
        private final String archiveURI;
        
        public TestZipFileObject (final String archiveUri, final String folderName, final String fileName, final long mtime) {
            super(null, folderName, fileName, mtime);
            this.archiveURI = archiveUri;
        }

        @Override
        protected URI getArchiveURI() {
            return URI.create(this.archiveURI);
        }

        @Override
        protected long getSize() throws IOException {
            return 0;
        }

        public InputStream openInputStream() throws IOException {
            throw new UnsupportedOperationException("Not supported.");      //NOI18N
        }
        
    }
    
    private static File createTestFile (final File wd) throws IOException {
        File f = new File (wd,"Test.java");
        PrintWriter out = new PrintWriter (new FileWriter (f));
        try {
            out.print(DATA);
        } finally {
            out.close();
        }
        return f;
    }

}
