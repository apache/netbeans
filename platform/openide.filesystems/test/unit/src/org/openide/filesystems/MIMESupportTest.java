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

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.Enumeration;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Enumerations;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jaroslav Tulach, Radek Matous
 */
public class MIMESupportTest extends NbTestCase {
    private TestLookup lookup;
    public MIMESupportTest(String testName) {
        super(testName);
    }

    static {
        System.setProperty("org.openide.util.Lookup", MIMESupportTest.TestLookup.class.getName());
        assertEquals(MIMESupportTest.TestLookup.class, Lookup.getDefault().getClass());
        
    }
    
    protected @Override void setUp() throws Exception {
        lookup = (MIMESupportTest.TestLookup)Lookup.getDefault();
        lookup.init();
    }

    public void testFindMIMETypeCanBeGarbageCollected() throws IOException {
        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "Ahoj.bla");

        String expResult = "content/unknown";
        String result = FileUtil.getMIMEType(fo);
        assertEquals("some content found", expResult, result);

        WeakReference<FileObject> r = new WeakReference<FileObject>(fo);
        fo = null;
        assertGC("Can be GCed", r);
    }

    public void testBehaviourWhemLookupResultIsChanging() throws Exception {
        MIMESupportTest.TestResolver testR = new MIMESupportTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());

        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "mysterious.lenka");

        assertEquals("content/unknown",fo.getMIMEType());

        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));
        assertEquals(testR.getMime(),fo.getMIMEType());

        testR = new MIMESupportTest.TestResolver("b/b");
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));
        assertEquals(testR.getMime(),fo.getMIMEType());
    }

    public void testUnreadableFiles() throws Exception {
        MIMESupportTest.TestResolver testR = new MIMESupportTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());
        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));
        AbstractFileSystem afs = new AbstractFileSystem() {
            @Override
            public String getDisplayName() {
                return "";
            }
            @Override
            public boolean isReadOnly() {
                return false;
            }
            @Override
            protected boolean canRead(String name) {
                return !name.equals("f");
            }
        };
        afs.list = new AbstractFileSystem.List() {
            public String[] children(String f) {
                if (f.equals("")) {
                    return new String[] {"f"};
                } else {
                    return null;
                }
            }
        };
        afs.info = new AbstractFileSystem.Info() {
            public Date lastModified(String name) {
                return null;
            }
            public boolean folder(String name) {
                return name.equals("");
            }
            public boolean readOnly(String name) {
                return false;
            }
            public String mimeType(String name) {
                return null;
            }
            public long size(String name) {
                return 0;
            }
            public InputStream inputStream(String name) throws FileNotFoundException {
                throw new FileNotFoundException();
            }
            public OutputStream outputStream(String name) throws IOException {
                throw new IOException();
            }
            public void lock(String name) throws IOException {}
            public void unlock(String name) {}
            public void markUnimportant(String name) {}
        };
        afs.attr = new AbstractFileSystem.Attr() {
            public Object readAttribute(String name, String attrName) {
                return null;
            }
            public void writeAttribute(String name, String attrName, Object value) throws IOException {}
            public Enumeration<String> attributes(String name) {
                return Enumerations.empty();
            }
            public void renameAttributes(String oldName, String newName) {}
            public void deleteAttributes(String name) {}
        };
        FileObject fo = afs.findResource("f");
        assertNotNull(fo);
        assertFalse(fo.canRead());
        assertEquals("unreadable", fo.getMIMEType());
    }

    private static final class TestResolver extends MIMEResolver {
        private final String mime;
        private TestResolver(String mime) {            
            this.mime = mime;
        }
        
        public String findMIMEType(FileObject fo) {
            if (fo.canRead()) {
                return mime;
            } else {
                return "unreadable";
            }
        }        
        
        private String getMime() {
            return mime;
        }
    }

    public void testRetryOnInterruptedIOException() throws Exception {
        final byte[] arr = new byte[]{10, 11, 12, 13};
        final FilterInputStream is = new FilterInputStream(new ByteArrayInputStream(arr)) {
            boolean thrown = false;

            @Override
            public synchronized int read() throws IOException {
                throwIfNotThrown();
                return super.read();
            }

            @Override
            public int read(byte[] b) throws IOException {
                throwIfNotThrown();
                return super.read(b);
            }

            @Override
            public synchronized int read(byte[] b, int off, int len) throws IOException {
                throwIfNotThrown();
                return super.read(b, off, len);
            }

            private void throwIfNotThrown() throws InterruptedIOException {
                if (!thrown) {
                    thrown = true;
                    throw new InterruptedIOException();
                }
            }
        };

        FileObject myFo = new FileObject() {
            @Override
            public String getName() {
                return "fake";
            }

            @Override
            public String getExt() {
                return "ext";
            }

            @Override
            public void rename(FileLock lock, String name, String ext) throws IOException {
                throw new IOException();
            }

            @Override
            public FileSystem getFileSystem() throws FileStateInvalidException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public FileObject getParent() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public boolean isFolder() {
                return false;
            }

            @Override
            public Date lastModified() {
                return new Date(333);
            }

            @Override
            public boolean isRoot() {
                return false;
            }

            @Override
            public boolean isData() {
                return true;
            }

            @Override
            public boolean isValid() {
                return true;
            }

            @Override
            public void delete(FileLock lock) throws IOException {
                throw new IOException();
            }

            @Override
            public Object getAttribute(String attrName) {
                return null;
            }

            @Override
            public void setAttribute(String attrName, Object value) throws IOException {
                throw new IOException();
            }

            @Override
            public Enumeration<String> getAttributes() {
                return Enumerations.empty();
            }

            @Override
            public void addFileChangeListener(FileChangeListener fcl) {
            }

            @Override
            public void removeFileChangeListener(FileChangeListener fcl) {
            }

            @Override
            public long getSize() {
                return arr.length;
            }

            @Override
            public InputStream getInputStream() throws FileNotFoundException {
                return is;
            }

            @Override
            public OutputStream getOutputStream(FileLock lock) throws IOException {
                throw new IOException();
            }

            @Override
            public FileLock lock() throws IOException {
                throw new IOException();
            }

            @Override
            public void setImportant(boolean b) {
            }

            @Override
            public FileObject[] getChildren() {
                return new FileObject[0];
            }

            @Override
            public FileObject getFileObject(String name, String ext) {
                return null;
            }

            @Override
            public FileObject createFolder(String name) throws IOException {
                throw new IOException();
            }

            @Override
            public FileObject createData(String name, String ext) throws IOException {
                throw new IOException();
            }

            @Override
            public boolean isReadOnly() {
                return true;
            }
        };

        class MockMimeResolver extends MIMEResolver {
            @Override
            public String findMIMEType(FileObject fo) {
                try {
                    InputStream is = fo.getInputStream();
                    if (is.read() == 10 && is.read() == 11 && is.read() == 12 && is.read() == 13) {
                        is.close();
                        return "hi/there";
                    }
                    is.close();
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
                return null;
            }
        }

        lookup.setLookups(new MockMimeResolver());

        String type = FileUtil.getMIMEType(myFo);
        assertEquals("The proper type is guessed", "hi/there", type);
    }

    public void testWithinMimeTypes() throws Exception {
        MIMESupportTest.TestExtResolver testResolverA = new MIMESupportTest.TestExtResolver("a", "a/a");
        MIMESupportTest.TestExtResolver testResolverB = new MIMESupportTest.TestExtResolver("b", "b/b");
        MIMESupportTest.TestExtResolver testResolverXML = new MIMESupportTest.TestExtResolver("xml", "xml/xml");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());

        lookup.setLookups(testResolverA, testResolverB, testResolverXML);
        assertEquals(3, Lookup.getDefault().lookupAll(MIMEResolver.class).size());

        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "file.a");
        String mimeType = fo.getMIMEType("b/b");
        assertEquals("content/unknown", mimeType);
        mimeType = FileUtil.getMIMEType(fo, "a/a");
        assertEquals("a/a", mimeType);
        mimeType = FileUtil.getMIMEType(fo);
        assertEquals("a/a", mimeType);
        mimeType = FileUtil.getMIMEType(fo, "b/b");
        assertEquals("a/a", mimeType);

        //#161340 - do not cache text/xml if it is falback value
        FileObject fo1 = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "file.xml");
        mimeType = fo1.getMIMEType("a/a");
        assertEquals("Fallback for xml failed.", "text/xml", mimeType);
        mimeType = FileUtil.getMIMEType(fo1);
        assertEquals("Fallback MIME type for xml should not be cached.", "xml/xml", mimeType);
    }

    public void testExtensionChangeNoticedInAtomicAction() throws Exception {
        MIMESupportTest.TestExtResolver resHTML = new MIMESupportTest.TestExtResolver("html", "text/html");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());

        lookup.setLookups(resHTML);
        assertEquals(1, Lookup.getDefault().lookupAll(MIMEResolver.class).size());

        class R implements Runnable {
            @Override
            public void run() {
                try {
                    FileObject fo1 = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "file.any");

                    assertEquals("Unknown mime type", "content/unknown", fo1.getMIMEType());
                    FileLock lock = fo1.lock();
                    fo1.rename(lock, "file", "html");
                    lock.releaseLock();

                    assertEquals("Now the file is recognized", "text/html", fo1.getMIMEType());
                } catch (IOException ex) {
                    throw new IllegalStateException(ex);
                }
            }
        }

        FileUtil.runAtomicAction(new R());
    }

    private static final class TestExtResolver extends MIMEResolver {
        private final String ext;
        private final String mime;
        private TestExtResolver(String ext, String mime) {
            super(mime);
            this.ext = ext;
            this.mime = mime;
        }

        public String findMIMEType(FileObject fo) {
            if (fo.getExt().equals(ext)) {
                return mime;
            } else {
                return null;
            }
        }
    }
    
    public void testDeclarativeMIMEResolvers() throws Exception {
        FileObject resolver = FileUtil.createData(FileUtil.getConfigRoot(), "Services/MIMEResolver/r.xml");
        resolver.setAttribute("position", 2);
        OutputStream os = resolver.getOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name='foo'/>");
        ps.println("  <resolver mime='text/x-foo'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        FileObject foo = FileUtil.createMemoryFileSystem().getRoot().createData("x.foo");
        assertEquals("text/x-foo", foo.getMIMEType());
        // Test changing a resolver:
        os = resolver.getOutputStream();
        ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name='foo'/>");
        ps.println("  <resolver mime='text/x-foo2'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        foo = FileUtil.createMemoryFileSystem().getRoot().createData("x2.foo");
        assertEquals("text/x-foo2", foo.getMIMEType());
        // Test adding a resolver:
        resolver = FileUtil.createData(FileUtil.getConfigRoot(), "Services/MIMEResolver/r2.xml");
        resolver.setAttribute("position", 1);
        os = resolver.getOutputStream();
        ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <ext name='foo'/>");
        ps.println("  <resolver mime='text/x-foo3'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        foo = FileUtil.createMemoryFileSystem().getRoot().createData("x3.foo");
        assertEquals("text/x-foo3", foo.getMIMEType());
        // Test removing a resolver:
        resolver.delete();
        foo = FileUtil.createMemoryFileSystem().getRoot().createData("x4.foo");
        assertEquals("text/x-foo2", foo.getMIMEType());
    }

    public void testFAttrs() throws Exception {
        FileObject resolver = FileUtil.createData(FileUtil.getConfigRoot(), "Services/MIMEResolver/r.xml");
        resolver.setAttribute("position", 2);
        OutputStream os = resolver.getOutputStream();
        PrintStream ps = new PrintStream(os);
        ps.println("<!DOCTYPE MIME-resolver PUBLIC '-//NetBeans//DTD MIME Resolver 1.0//EN' 'http://www.netbeans.org/dtds/mime-resolver-1_0.dtd'>");
        ps.println("<MIME-resolver>");
        ps.println(" <file>");
        ps.println("  <fattr name='foo' text='yes'/>");
        ps.println("  <resolver mime='text/x-boo'/>");
        ps.println(" </file>");
        ps.println("</MIME-resolver>");
        os.close();
        FileObject foo = FileUtil.createMemoryFileSystem().getRoot().createData("somefile");
        assertEquals("content/unknown", foo.getMIMEType());
        foo.setAttribute("foo", Boolean.FALSE);
        assertEquals("content/unknown", foo.getMIMEType());
        foo.setAttribute("foo", "no");
        assertEquals("content/unknown", foo.getMIMEType());
        foo.setAttribute("foo", "yes");
        assertEquals("text/x-boo", foo.getMIMEType());
    }

    public static class TestLookup extends ProxyLookup {
        public TestLookup() {
            super();
            init();
        }
        
        private void init() {
            setLookups(new Lookup[] {});
        }
        
        private void setLookups(Object... instances) {
            setLookups(new Lookup[] {getInstanceLookup(instances)});
        }
        
        private Lookup getInstanceLookup(final Object... instances) {
            InstanceContent instanceContent = new InstanceContent();
            for(Object i : instances) {
                instanceContent.add(i);
            }
            Lookup instanceLookup = new AbstractLookup(instanceContent);
            return instanceLookup;
        }        
    }    
    
}
