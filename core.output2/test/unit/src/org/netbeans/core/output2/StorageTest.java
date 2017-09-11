/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
