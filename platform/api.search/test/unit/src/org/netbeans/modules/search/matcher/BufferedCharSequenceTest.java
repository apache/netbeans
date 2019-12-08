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

package org.netbeans.modules.search.matcher;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author vvg
 */
public class BufferedCharSequenceTest {

    private static final String UTF_8 = "UTF-8"; //NOI18N
    private static final String EUC_JP = "EUC_JP"; //NOI18N

    private  Charset cs_UTF_8 = Charset.forName(UTF_8);



    public BufferedCharSequenceTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {       
        cs_UTF_8 = Charset.forName(UTF_8);
    }

    @After
    public void tearDown() {       
        cs_UTF_8 = null;
    }

    /**
     * Test of close method, of class BufferedCharSequence.
     */
//    @Test
    public void testClose() throws Exception {
        System.out.println("close");
        BufferedCharSequence instance = null;
        BufferedCharSequence expResult = null;
        BufferedCharSequence result = instance.close();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of duplicate method, of class BufferedCharSequence.
     */
//    @Test
    public void testDuplicate() {
        System.out.println("duplicate");
        BufferedCharSequence instance = null;
        BufferedCharSequence expResult = null;
        BufferedCharSequence result = instance.duplicate();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of length method, of class BufferedCharSequence.
     */
    @Test
    public void testLength() {
        System.out.println("length");                
        Charset cs;
        //BufferedCharSequence instance;
        int result;                           
        cs = Charset.forName(UTF_8);
        for(TypeOfStream stype: TypeOfStream.values()) {
            result = getLenght(stype, TypeOfContent.BYTE_10, cs, 10);
            assertEquals(10, result);

            result = getLenght(stype, TypeOfContent.BYTE_0, cs, 0);
            assertEquals(0, result);

            result = getLenght(stype, TypeOfContent.BYTE_1, cs, 1);
            assertEquals(1, result);
        }
    }
    
    private int getLenght(TypeOfStream stype, TypeOfContent ctype, Charset cs, int size){
        InputStream stream = getInputStream(stype, ctype, cs);
        @SuppressWarnings("deprecation")
        BufferedCharSequence instance = new BufferedCharSequence(stream, cs.newDecoder(), size);
        instance.setMaxBufferSize(10);
        return instance.length();
    }

   
    /**
     * Test of charAt method, of class BufferedCharSequence.
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void testCharAt_File() {
        System.out.println("charAt_File");
        int index = 0;        
        Charset cs = Charset.forName(UTF_8);
        InputStream stream = getInputStream(TypeOfStream.FILE, TypeOfContent.BYTE_0, cs);
        @SuppressWarnings("deprecation")
        BufferedCharSequence instance = new BufferedCharSequence(stream, cs.newDecoder(), 0);
        instance.charAt(index);
    }

    /**
     * Test of charAt method, of class BufferedCharSequence.
     */
    @Test(expected=IndexOutOfBoundsException.class)
    public void testCharAt_Byte() {
        System.out.println("charAt_Byte");
        int index = 0;
        Charset cs = Charset.forName(UTF_8);
        InputStream stream = getInputStream(TypeOfStream.BYTE, TypeOfContent.BYTE_0, cs);
        @SuppressWarnings("deprecation")
        BufferedCharSequence instance = new BufferedCharSequence(stream, cs.newDecoder(), 0);
        instance.charAt(index);
    }


    @Test
    public void testCharAt$1_byte() {
        System.out.println("testCharAt$1_byte");
        int index = 0;       
        Charset cs = Charset.forName(UTF_8);
        for(TypeOfStream stype: TypeOfStream.values()) {
            try {
                InputStream stream = getInputStream(stype, TypeOfContent.BYTE_1, cs);
                @SuppressWarnings("deprecation")
                BufferedCharSequence instance = new BufferedCharSequence(stream, cs.newDecoder(), 1);
                char expResult = 'a';
                char result = instance.charAt(index);
                assertEquals(expResult, result);

            } catch (IndexOutOfBoundsException ioobe) {
                ioobe.printStackTrace();
                fail(ioobe.toString());
            } catch (BufferedCharSequence.SourceIOException bcse) {
                bcse.printStackTrace();
                fail(bcse.toString());
            }
        }
    }   


    @Test
    public void testCharAt$10_byte() {
        System.out.println("testCharAt$10_byte");
        File file = getFile("10_bytes");
        Charset cs = Charset.forName(UTF_8);
        for(TypeOfStream stype: TypeOfStream.values()) {
            InputStream stream = getInputStream(stype, TypeOfContent.BYTE_10, cs);
            @SuppressWarnings("deprecation")
            BufferedCharSequence instance = new BufferedCharSequence(stream, cs.newDecoder(), 10);
            instance.setMaxBufferSize(10);
            char result;

            result = instance.charAt(0);
            assertEquals('0', result);

            result = instance.charAt(9);
            assertEquals('9', result);

            result = instance.charAt(5);
            assertEquals('5', result);

            result = instance.charAt(9);
            assertEquals('9', result);
        }
   }
  

    @Test
    public void testCharAt_$10_byte$2() {
        System.out.println("testCharAt$10_byte$2");       
        Charset cs = Charset.forName(UTF_8);
        for(TypeOfStream stype: TypeOfStream.values()) {
            InputStream stream = getInputStream(stype, TypeOfContent.BYTE_10, cs);
            @SuppressWarnings("deprecation")
            BufferedCharSequence instance = new BufferedCharSequence(stream, cs.newDecoder(), 10);
            instance.setMaxBufferSize(5);
            char result;

            result = instance.charAt(0);
            assertEquals('0', result);

            result = instance.charAt(9);
            assertEquals('9', result);

            result = instance.charAt(5);
            assertEquals('5', result);

            result = instance.charAt(9);
            assertEquals('9', result);
        }

   }

    /**
     * Test of subSequence method, of class BufferedCharSequence.
     */
//    @Test
    public void testSubSequence() {
        System.out.println("subSequence");
        int start = 0;
        int end = 0;
        BufferedCharSequence instance = null;
        CharSequence expResult = null;
        CharSequence result = instance.subSequence(start, end);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of toString method, of class BufferedCharSequence.
     */
    @Test
    public void testToString() {
        System.out.println("toString");
        for(TypeOfStream stype: TypeOfStream.values()) {
            InputStream stream = getInputStream(stype, TypeOfContent.BYTE_10, cs_UTF_8);
            @SuppressWarnings("deprecation")
            BufferedCharSequence instance = new BufferedCharSequence(stream, cs_UTF_8.newDecoder(), 10);
            instance.setMaxBufferSize(5);
            String expResult = TypeOfContent.BYTE_10.getContent();
            String result = instance.toString();
            assertEquals(expResult, result);
        }
    }

    /**
     * Test of position method, of class BufferedCharSequence.
     */
//    @Test
    public void testPosition() {
        System.out.println("position");
        BufferedCharSequence instance = null;
        int expResult = 0;
        int result = instance.position();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of nextChar method, of class BufferedCharSequence.
     */
//    @Test
    public void testNextChar() {
        System.out.println("nextChar");
        BufferedCharSequence instance = null;
        char expResult = ' ';
        char result = instance.nextChar();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of rewind method, of class BufferedCharSequence.
     */
//    @Test
    public void testRewind() {
        System.out.println("rewind");
        BufferedCharSequence instance = null;
        BufferedCharSequence expResult = null;
        BufferedCharSequence result = instance.rewind();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getLineText method, of class BufferedCharSequence.
     */
    @Test
    public void testNextLineText() {
        System.out.println("nextLineText");
        System.out.println("nextLineText@no line terminators in the file.");
        for(TypeOfStream stype: TypeOfStream.values()) {
            InputStream stream = getInputStream(stype, TypeOfContent.BYTE_10, cs_UTF_8);
            @SuppressWarnings("deprecation")
            BufferedCharSequence instance = new BufferedCharSequence(stream, cs_UTF_8.newDecoder(), 10);
            assertEquals(0, instance.position());
            String expResult = TypeOfContent.BYTE_10.getContent();
            String result = instance.nextLineText();
            assertEquals(expResult, result);
            assertEquals(11, instance.position());
        }
    }
    
    /** Tests fix of issue 95203. */
    @Test
    public void testUnicodeAt4KB() {
        File f = getFile("more_than_4KB.txt");
        assertNotNull(f);
        FileObject fo = FileUtil.toFileObject(f);
        assertNotNull(fo);
        BufferedCharSequence chars = new BufferedCharSequence(fo, cs_UTF_8.newDecoder(), f.length());
        assertEquals('0', chars.charAt(0));
        assertEquals('1', chars.charAt(1));
        int xPos;
        if (chars.charAt(4094) == '\r' && chars.charAt(4095) == '\n') {
            // windows line-endings, can be caused by hg extension win32text
            xPos = 4098;
        } else {
            // unix or mac line-endings
            xPos = 4097;
        }
        assertEquals('X', chars.charAt(xPos));
        assertEquals('Y', chars.charAt(xPos+1));
    }

    /**
     * Returns {@code FileChennel} for the specified data file located in the
     * {@code org.netbeans.modules.search.data} package.
     * @param fileName - name of the data file.
     * @return {@code FileChennel} of the data file.
     */
    public FileChannel getDataFileChannel(String fileName) {
        File file = getFile(fileName);

        FileInputStream fis = getFileInputStream(file);
        return fis.getChannel();
    }


    public FileInputStream getFileInputStream(File f) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        assertNotNull(fis);
        return fis;
    }

    public File getFile(String fileName) {
        File file = MatcherTestUtils.getFile(fileName);
        assertTrue (file.exists());
        return file;
    }

    public ByteArrayInputStream getByteArrayInputStream(byte[] buf) {
        ByteArrayInputStream bis = new ByteArrayInputStream(buf);
        assertNotNull(bis);
        return bis;
    }
    
    public InputStream getInputStream(TypeOfStream type, TypeOfContent content, Charset cs) {        
        switch (type) {
            case FILE: 
               return getFileInputStream(getFile(content.getFileName()));
            case BYTE:              
               return getByteArrayInputStream(content.getContent().getBytes(cs));
            default: return null;
        }                        
    }
    /**
     * Enum describes a type of InputStream.
     */
    enum TypeOfStream{
        FILE, //FileInputStream
        BYTE  //ByteArrayInputStream
    }
    /**
     * Enum describes a type of the content of the InputStream.
     */
    enum TypeOfContent{
        BYTE_0(0, "0_bytes", ""), //InputStream contains 0 byte or is created from "0_bytes" file
        BYTE_1(1, "1_byte", "a"),
        BYTE_10(10, "10_bytes", "0123456789");

        private final int buf_size;
        private final String file_name;
        private final String content;

        private TypeOfContent(int size, String name, String content) {
            this.buf_size = size;
            this.file_name = name;
            this.content = content;
        }

        public String getFileName() {
            return file_name;
        }

        public int getBufSize() {
            return buf_size;
        }

        public String getContent() {
            return content;
        }

    }

}
