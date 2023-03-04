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

/*
 * EncodingUtilTest.java
 * JUnit based test
 *
 * Created on September 13, 2007, 4:10 PM
 */

package org.netbeans.modules.xml.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;
import junit.framework.TestCase;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Samaresh
 */
public class EncodingUtilTest extends TestCase {
    
    public EncodingUtilTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testIsValidEncoding() {
        //try null
        String encoding = null;
        boolean result = EncodingUtil.isValidEncoding(encoding);
        assert(false == result);
        
        //try some junk
        encoding = "junk";
        result = EncodingUtil.isValidEncoding(encoding);
        assert(false == result);
        
        //try valid encoding string
        encoding = "UTF-8";
        result = EncodingUtil.isValidEncoding(encoding);
        assert(true == result);
    } /* Test of isValidEncoding method, of class EncodingUtil. */

    public void testGetProjectEncoding() throws Exception {
        //TODO
        FileObject file = null;
        assert(true);
    }
    
    static final String[] JAVA_ENCODINGS = new String[] {
       "ASCII",
       "ISO8859_1", 
       "ISO8859_2", 
       "ISO8859_3", 
       "ISO8859_4", 
       "ISO8859_5", 
       "ISO8859_6", 
       "ISO8859_7",
       "ISO8859_8",
       "ISO8859_9",
       "Big5",
       "Cp037",
       "Cp1006",
       "Cp1025",
       "Cp1026",
       "Cp1046",
       "Cp1097",
       "Cp1098",
       "Cp1112",
       "Cp1122",
       "Cp1123",
       "Cp1124",
       "Cp1250",
       "Cp1251",
       "Cp1252",
       "Cp1253",
       "Cp1254",
       "Cp1255",
       "Cp1256",
       "Cp1257",
       "Cp1258",
       "Cp1381",
       "Cp1383",
       "Cp273",
       "Cp277",
       "Cp278",
       "Cp280",
       "Cp284",
       "Cp285",
       "Cp297",
       "Cp33722",
       "Cp420",
       "Cp424",
       "Cp437",
       "Cp500",
       "Cp737",
       "Cp775",
       "Cp838",
       "Cp850",
       "Cp852",
       "Cp855",
       "Cp857",
       "Cp860",
       "Cp861",
       "Cp862",
       "Cp863",
       "Cp864",
       "Cp865",
       "Cp866",
       "Cp868",
       "Cp869",
       "Cp870",
       "Cp871",
       "Cp874",
       "Cp875",
       "Cp918",
       "Cp921",
       "Cp922",
       "Cp930",
       "Cp933",
       "Cp935",
       "Cp937",
       "Cp939",
       "Cp942",
       "Cp948",
       "Cp949",
       "Cp950",
       "Cp964",
       "Cp970",
       "EUC_CN",
       "EUC_JP",
       "EUC_KR",
       "EUC_TW",
       "GBK",
//       "ISO2022CN",  // unsupported on write     see http://developer.java.sun.com/developer/bugParade/bugs/4296969.html
//       "ISO2022CN_CNS",  // unsupported on read
//       "ISO2022CN_GB", // unsupported on read
       "ISO2022JP",
       "ISO2022KR",
       "JIS0201",
//       "JIS0208",  // cannot write '<'
//       "JIS0212",  // cannot write '<'
       "KOI8_R",
       "MS874",
       "MacArabic",
       "MacCentralEurope",
       "MacCroatian",
       "MacCyrillic",
       "MacDingbat",
       "MacGreek",
       "MacHebrew",
       "MacIceland",
       "MacRoman",
       "MacRomania",
       "MacSymbol",
       "MacThai",
       "MacTurkish",
       "MacUkraine",
       "SJIS",
       "UTF8",
       "Unicode",
       "UTF-16",
       "UnicodeLittle",
       "UnicodeLittleUnmarked",
       "UnicodeBig",
       "UnicodeBigUnmarked",
    };
    
    /** Test of autoDetectEncoding method, of class org.netbeans.modules.xml.core.lib.EncodingHelper. */
    public void testEncodingDetection() throws IOException {

        // typical xml prolog with all allowed IANA encoding names
        String fmt = "<?xml version=\"1.0\" encoding=''{0}'' ?> <?pi abcdefghijklmnopqrtsuvwxyz_1234567890\"ABCDEFGHIJKLMNOPQRTSUVWXYZ-.?>";
        String enc = null;
        
        for (int i = 0; i<JAVA_ENCODINGS.length; i++) {
            char xml[] = MessageFormat.format(fmt, new Object[] {JAVA_ENCODINGS[i]}).toCharArray();
            
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {                
                OutputStreamWriter wr = new OutputStreamWriter(os, JAVA_ENCODINGS[i]);
                wr.write(xml);
                wr.flush();
                wr.close();
            } catch (IOException ex) {
                fail("While writing as " + JAVA_ENCODINGS[i] + ":" + ex);
            }
            
            //InputStream in = new ByteArrayInputStream(os.toByteArray());
            byte[] out = os.toByteArray();
            char[] chars = new char[xml.length];
            
            enc = EncodingUtil.autoDetectEncoding(out);
            
            if (enc != null) {
                
                try {
                    ByteArrayInputStream in = new ByteArrayInputStream(out);
                    InputStreamReader reader = new InputStreamReader(in, JAVA_ENCODINGS[i]);
                    reader.read(chars);

                    // check read characters for identity
                    
                    for(int j = 0; j<chars.length; j++) {
                        if (chars[j] != xml[j]) {
                            fail(JAVA_ENCODINGS[i] + " cannot write '" + xml[j] + "'");
                        }
                    }

                    // try to decode encoding
                    String denc = EncodingUtil.detectDeclaredEncoding(out, enc);
                    if (JAVA_ENCODINGS[i].equals(denc) == false) {
                        fail("detectDeclaredEncoding() failure got " + denc + " instead of " + JAVA_ENCODINGS[i]);
                    }
                    
                } catch (IOException ex) {
                    System.out.println(JAVA_ENCODINGS[i] + " detected as \t" + enc);
                    fail("Cannot read: " + JAVA_ENCODINGS[i] + " due to: " + ex);
                }
                
            } else {
                Set<String> known = new HashSet<String>();
                known.add("Cp930");
                known.add("MacDingbat");
                known.add("MacSymbol");
                
                if (known.contains(JAVA_ENCODINGS[i]) == false) {
                    fail(JAVA_ENCODINGS[i] + " indetermined \t" + out[0] + ", " +  out[1] + ", " + out[2]  + ", " + out[3]);
                }
            }
            
        }
        

        // Byte Order marks recognition test

        byte[] usc4_1234 = new byte[] {(byte)0,(byte)0,(byte)0xfe,(byte)0xff};
        byte[] usc4_4321 = new byte[] {(byte)0xff,(byte)0xfe,(byte)0,(byte)0};
        byte[] usc4_2143 = new byte[] {(byte)0,(byte)0,(byte)0xff,(byte)0xfe};
        byte[] usc4_3412 = new byte[] {(byte)0xfe,(byte)0xff,(byte)0,(byte)0};
        byte[] utf16_be = new byte[] {(byte)0xfe,(byte)0xff,(byte)'<',(byte)'?'};
        byte[] utf16_le = new byte[] {(byte)0xff,(byte)0xfe,(byte)'<',(byte)'?'};
        byte[] utf8 = new byte[] {(byte)0xef,(byte)0xbb,(byte)0xbf,(byte)'<'};

        if (EncodingUtil.autoDetectEncoding(usc4_1234) != null) fail("usc4_1234");
        if (EncodingUtil.autoDetectEncoding(usc4_4321) != null) fail("usc4_4321");
        if (EncodingUtil.autoDetectEncoding(usc4_2143) != null) fail("usc4_2143");
        if (EncodingUtil.autoDetectEncoding(usc4_3412) != null) fail("usc4_3412");
        
        
        // test roundtrip on recognized

        System.out.println("Warning: BOM encoding roundtrip test disabled.");
        
/*  There are probably bugs in JDK that recognizes BOM as '?'
        // parameters
        byte[] out, datab, mark;
        String data;
        char[] buf, outch;
        int mark_le;        
        InputStreamReader r;
        
        //
        
        mark = utf16_be;
        enc = EncodingHelper.autoDetectEncoding(mark);        
        enc = "UnicodeBig";
        mark_le = 2;
        
        data = MessageFormat.format(fmt, new String[] {enc});
        outch = data.toCharArray();
        datab = data.getBytes(enc);
        
        out = new byte[datab.length + mark_le];
        System.arraycopy(mark, 0, out, 0, 4);
        System.arraycopy(datab, 0, out, mark_le, datab.length);
        
        r = new InputStreamReader(new ByteArrayInputStream(out), enc);
        buf = new char[outch.length];
        r.read(buf);

        for(int j = 0; j<outch.length; j++) {
            if (buf[j] != outch[j]) {
                fail(enc + " cannot write '" + outch[j] + "'" + " got'" + buf[j] + buf[j+1] + "'");
            }
        }


        // 
        
        mark = utf16_le;
        enc = EncodingHelper.autoDetectEncoding(mark);        
        mark_le = 2;
        
        data = MessageFormat.format(fmt, new String[] {enc});
        outch = data.toCharArray();
        datab = data.getBytes(enc);
        
        out = new byte[datab.length + mark_le];
        System.arraycopy(mark, 0, out, 0, 4);         
        System.arraycopy(datab, 0, out, mark_le, datab.length);
        
        r = new InputStreamReader(new ByteArrayInputStream(out), enc);
        buf = new char[outch.length];
        r.read(buf);

        for(int j = 0; j<outch.length; j++) {
            if (buf[j] != outch[j]) {
                fail(enc + " cannot write '" + outch[j] + "'"  + " got'" + buf[j] + "'");
            }
        }
                
        //
        
        mark = utf8;
        enc = EncodingHelper.autoDetectEncoding(mark);        
        mark_le = 3;
        
        data = MessageFormat.format(fmt, new String[] {enc});
        outch = data.toCharArray();
        datab = data.getBytes(enc);
        
        out = new byte[datab.length + mark_le];
        System.arraycopy(mark, 0, out, 0, 4);         
        System.arraycopy(datab, 0, out, mark_le, datab.length);
        
        r = new InputStreamReader(new ByteArrayInputStream(mark), enc);
        buf = new char[outch.length];
        r.read(buf);

        for(int j = 0; j<outch.length; j++) {
            if (buf[j] != outch[j]) {
                fail(enc + " cannot write '" + outch[j] + "'"  + " got'" + buf[j] + buf[j+1] + buf[j+2] + "'");
            }
        }
  */      
    }
    
    
}
