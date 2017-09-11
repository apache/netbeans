/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.dataview.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.util.Exceptions;

/**
 *
 * @author mblaesing
 */
public class FileBackedBlobTest {

    private static byte[] testCase1;
    private static byte[] testCase2;
    private static byte[] testCase3;

    private static void assertStreamEquals(InputStream is1, InputStream is2) {
        try {
            long position = 0;
            while (true) {
                int input1 = is1.read();
                int input2 = is2.read();
                if (input1 != input2) {
                    throw new AssertionError("Streams differ at position: " + Long.toString(position));
                }
                if (input1 == -1 || input2 == -1) {
                    return;
                }
                position++;
            }
        } catch (IOException ex) {
            throw new AssertionError(ex);
        } finally {
            try {
                is1.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                is2.close();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public FileBackedBlobTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
        byte[] testPattern = "TestCase".getBytes();
        int testLength = testPattern.length;
        testCase1 = new byte[10];
        testCase2 = new byte[1024];
        testCase3 = new byte[1024 * 1024];
        for (int i = 0; i < testCase1.length; i++) {
            testCase1[i] = testPattern[ i % testLength];
        }
        for (int i = 0; i < testCase2.length; i++) {
            testCase2[i] = testPattern[ i % testLength];
        }
        for (int i = 0; i < testCase3.length; i++) {
            testCase3[i] = testPattern[ i % testLength];
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        testCase1 = null;
        testCase2 = null;
        testCase3 = null;
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testLength() throws Exception {
        FileBackedBlob b;
        b = new FileBackedBlob(new ByteArrayInputStream(testCase1));
        assertEquals(b.length(), testCase1.length);
        b.free();
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        assertEquals(b.length(), testCase2.length);
        b.free();
        b = new FileBackedBlob(new ByteArrayInputStream(testCase3));
        assertEquals(b.length(), testCase3.length);
        b.free();
    }

    @Test
    public void testGetBytes() throws Exception {
        FileBackedBlob b;
        // Each case is tested with the complete array and then with den range
        // as index: 5.-9.
        byte[] shortReference = new byte[5];
        System.arraycopy(testCase1, 5, shortReference, 0, 5);

        b = new FileBackedBlob(new ByteArrayInputStream(testCase1));
        assertArrayEquals(b.getBytes(1, (int) b.length()), testCase1);
        assertArrayEquals(b.getBytes(6, 5), shortReference);
        b.free();
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        assertArrayEquals(b.getBytes(1, (int) b.length()), testCase2);
        assertArrayEquals(b.getBytes(6, 5), shortReference);
        b.free();
        b = new FileBackedBlob(new ByteArrayInputStream(testCase3));
        assertArrayEquals(b.getBytes(1, (int) b.length()), testCase3);
        assertArrayEquals(b.getBytes(6, 5), shortReference);
        b.free();
    }

    @Test
    public void testGetBinaryStream_0args() throws Exception {
        FileBackedBlob b;
        b = new FileBackedBlob(new ByteArrayInputStream(testCase1));
        assertStreamEquals(b.getBinaryStream(), new ByteArrayInputStream(testCase1));
        b.free();
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        assertStreamEquals(b.getBinaryStream(), new ByteArrayInputStream(testCase2));
        b.free();
        b = new FileBackedBlob(new ByteArrayInputStream(testCase3));
        assertStreamEquals(b.getBinaryStream(), new ByteArrayInputStream(testCase3));
        b.free();
    }

    @Test
    public void testSetBytes_long_byteArr() throws Exception {
        FileBackedBlob b;
        byte[] test1 = "test".getBytes("ASCII");
        byte[] test2 = "test0123456789".getBytes("ASCII");
        byte[] firstPartReference = new byte[testCase2.length - test1.length - 4];
        byte[] secondPartReference = new byte[4];
        System.arraycopy(testCase2, 0, firstPartReference, 0, testCase2.length - test1.length - 4);
        System.arraycopy(testCase2, testCase2.length - 4 - 1, secondPartReference, 0, 4);
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        b.setBytes(testCase2.length - test1.length - 4 + 1, test1);
        assertArrayEquals(firstPartReference, b.getBytes(1, testCase2.length - test1.length - 4));
        assertArrayEquals(secondPartReference, b.getBytes(testCase2.length - 4, 4));
        assertArrayEquals(test1, b.getBytes(testCase2.length - 4 - test1.length + 1, test1.length));
        assertEquals(b.length(), 1024);
        b.setBytes(testCase2.length - test1.length - 4 + 1, test2);
        assertArrayEquals(firstPartReference, b.getBytes(1, testCase2.length - test1.length - 4));
        assertEquals(b.length(), 1024 - test1.length - 4 + test2.length);
        assertArrayEquals(test2, b.getBytes(b.length() - test2.length + 1, test2.length));
        b.free();
    }

    @Test
    public void testSetBytes_4args() throws Exception {
        FileBackedBlob b;
        byte[] test1 = "test".getBytes("ASCII");
        byte[] test2 = "01test23456789".getBytes("ASCII");
        byte[] firstPartReference = new byte[testCase2.length - test1.length - 4];
        byte[] secondPartReference = new byte[4];
        System.arraycopy(testCase2, 0, firstPartReference, 0, testCase2.length - test1.length - 4);
        System.arraycopy(testCase2, testCase2.length - 4 - 1, secondPartReference, 0, 4);
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        b.setBytes(testCase2.length - test1.length - 4 + 1, test2, 2, 4);
        assertArrayEquals(firstPartReference, b.getBytes(1, testCase2.length - test1.length - 4));
        assertArrayEquals(secondPartReference, b.getBytes(testCase2.length - 4, 4));
        assertArrayEquals(test1, b.getBytes(testCase2.length - 4 - test1.length + 1, test1.length));
        assertEquals(b.length(), 1024);
        b.setBytes(testCase2.length - test1.length - 4 + 1, test2);
        assertArrayEquals(firstPartReference, b.getBytes(1, testCase2.length - test1.length - 4));
        assertEquals(b.length(), 1024 - test1.length - 4 + test2.length);
        assertArrayEquals(test2, b.getBytes(b.length() - test2.length + 1, test2.length));
        b.free();
    }

    @Test
    public void testSetBinaryStream() throws Exception {
        FileBackedBlob b;
        byte[] test1 = "test".getBytes("ASCII");
        byte[] test2 = "test0123456789".getBytes("ASCII");
        byte[] firstPartReference = new byte[testCase2.length - test1.length - 4];
        byte[] secondPartReference = new byte[4];
        System.arraycopy(testCase2, 0, firstPartReference, 0, testCase2.length - test1.length - 4);
        System.arraycopy(testCase2, testCase2.length - 4 - 1, secondPartReference, 0, 4);
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        OutputStream os = b.setBinaryStream(testCase2.length - test1.length - 4 + 1);
        os.write(test1);
        os.close();
        assertArrayEquals(firstPartReference, b.getBytes(1, testCase2.length - test1.length - 4));
        assertArrayEquals(secondPartReference, b.getBytes(testCase2.length - 4, 4));
        assertArrayEquals(test1, b.getBytes(testCase2.length - 4 - test1.length + 1, test1.length));
        assertEquals(b.length(), 1024);
        os = b.setBinaryStream(testCase2.length - test1.length - 4 + 1);
        os.write(test2);
        os.close();
        assertArrayEquals(firstPartReference, b.getBytes(1, testCase2.length - test1.length - 4));
        assertEquals(b.length(), 1024 - test1.length - 4 + test2.length);
        assertArrayEquals(test2, b.getBytes(b.length() - test2.length + 1, test2.length));
        b.free();
    }

    @Test
    public void testTruncate() throws Exception {
        FileBackedBlob b;
        byte[] reference;
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        assertEquals(b.length(), testCase2.length);
        b.truncate(1024);
        assertEquals(b.length(), 1024);
        reference = new byte[1024];
        System.arraycopy(testCase2, 0, reference, 0, 1024);
        assertStreamEquals(b.getBinaryStream(), new ByteArrayInputStream(reference));
        b.truncate(10);
        assertEquals(b.length(), 10);
        reference = new byte[10];
        System.arraycopy(testCase2, 0, reference, 0, 10);
        assertStreamEquals(b.getBinaryStream(), new ByteArrayInputStream(reference));
        b.free();
    }

    @Test
    public void testFree() throws Exception {
        FileBackedBlob b;
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        assertTrue(b.getBackingFile().exists());
        b.free();
        assertFalse(b.getBackingFile().exists());
    }

    @Test
    public void testFinalize() throws Exception {
        FileBackedBlob b;
        b = new FileBackedBlob(new ByteArrayInputStream(testCase2));
        assertTrue(b.getBackingFile().exists());
        try {
            b.finalize();
        } catch (Throwable ex) {
            throw new AssertionError(ex);
        }
        assertFalse(b.getBackingFile().exists());
    }
}
