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

package org.netbeans.core.output2;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.Arrays;
import junit.framework.TestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author tim
 */
public class StorageTest extends TestCase {

    public StorageTest(String testName) {
        super(testName);
    }

    Storage filemap = null;
    Storage heap = null;
    @Override
    protected void setUp() throws Exception {
        filemap = new FileMapStorage();
        heap = new HeapStorage();
    }

    @Override
    protected void tearDown() throws Exception {
        filemap.dispose();
        heap.dispose();
    }

    // #85050
    public void testMMappedFileCanBeDeleted() throws Exception {
        write(filemap, "Test text");
        BufferResource<ByteBuffer> br = filemap.getReadBuffer(0, 10);
        ByteBuffer b = br.getBuffer();
        String s = b.asCharBuffer().toString();
        br.releaseBuffer();
        File f = ((FileMapStorage) filemap).getOutputFile();
        assertTrue("Memory mapped file should be created", f.exists());
        filemap.dispose();
        tryToDeleteFile(f);
        assertFalse("Memory mapped file should be deleted", f.exists());
    }

    public void testMMappedFileWithMoreBuffersCanBeDeleted() throws Exception {

        for (int i = 0; i < 10; i++) {
            int fwrite = write(filemap, "A string");
            BufferResource<ByteBuffer> br = filemap.getReadBuffer(fwrite, filemap.size() - fwrite);
            ByteBuffer fbuf = br.getBuffer();
            assertNotNull(fbuf);
            br.releaseBuffer();
        }
        File f = ((FileMapStorage) filemap).getOutputFile();
        filemap.dispose();
        tryToDeleteFile(f);
        assertFalse("Memory mapped file should be deleted", f.exists());
    }

    private void tryToDeleteFile(File f) {
        for (int i = 0; i < 500; i++) {
            if (!f.exists()) {
                break;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public void testIsClosed() throws Exception {
        doTestIsClosed(heap);
        doTestIsClosed(filemap);
    }
    
    private void doTestIsClosed (Storage storage) throws Exception {
        System.out.println("testIsClosed - " + storage.getClass());
        assertTrue (storage.isClosed());
        
        String test = "Hello world";
        storage.write(ByteBuffer.wrap(test.getBytes()));
        
        assertFalse (storage.isClosed());
        
        storage.close();
        assertTrue (storage.isClosed());
        
        write (storage, test);
        assertFalse (storage.isClosed());
        
        storage.close();
        assertTrue (storage.isClosed());
        
    }
                        
    private int write (Storage storage, String s) throws Exception {
        ByteBuffer buf = storage.getWriteBuffer(AbstractLines.toByteIndex(s.length()));
        buf.asCharBuffer().put(s);
        buf.position (buf.position() + AbstractLines.toByteIndex(s.length()));
        int result = storage.write(buf);
        storage.flush();
        return result;
    }
    
    
    public void testIdenticalBehaviors() throws Exception {
        String[] s = new String[10];
        String a = "abcd";
        String b = a;
        for (int i=0; i < s.length; i++) {
            s[i] = b;
            b += a;
            int hwrite = write (heap, s[i]);
            int fwrite = write (filemap, s[i]);
            assertEquals (hwrite, fwrite);
            assertEquals(heap.isClosed(), filemap.isClosed());
            assertEquals(heap.size(), filemap.size());
            BufferResource<ByteBuffer> hbufRef = heap.getReadBuffer(hwrite, heap.size() - hwrite);
            BufferResource<ByteBuffer> fbufRef = filemap.getReadBuffer(hwrite, filemap.size() - fwrite);
            ByteBuffer hbuf = hbufRef.getBuffer();
            ByteBuffer fbuf = fbufRef.getBuffer();
            hbufRef.releaseBuffer();
            fbufRef.releaseBuffer();
        }
    }
    
    public void testFileMapStorageCanBeAsLargeAsIntegerMaxValue() {
        System.out.println("testFileMapStorageCanBeAsLargeAsIntegerMaxValue - THIS TEST WILL CREATE A 2 GIGABYTE TEMP FILE!!!!");
        if (true) {
            System.out.println("Wisely skipping this test");
            return;
        }
        char[] c = new char[16384];
        Arrays.fill (c, 'a');
        String s = new String(c);
        try {
            while (filemap.size() < Integer.MAX_VALUE) {
                 write (filemap, s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail ("Could not create a large file - " + e.getMessage());
        }
    }
    
    public void testOutputWriterUsesHeapStorageWithLowMemoryFlagSet() throws Exception {
        System.out.println("testOutputWriterUsesHeapStorageWithLowMemoryFlagSet");
        boolean old = OutWriter.lowDiskSpace;
        OutWriter.lowDiskSpace = true;
        OutWriter ow = new OutWriter ();
        try {
            ow.println("Foo");
            assertTrue (ow.getStorage() instanceof HeapStorage);
        } finally {
            ow.dispose();
            OutWriter.lowDiskSpace = old;
        }
    }
    
}
