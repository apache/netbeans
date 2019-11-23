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
package org.netbeans.modules.db.dataview.util;

import java.io.*;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.*;
import org.openide.util.NbBundle;

/**
 * XML uses inband encoding detection - this class obtains it.
 *
 * Copied from xml.core to no introduce another dependency
 * 
 * @author  Petr Kuzel
 * @version 1.0
 */
public class EncodingHelper extends Object {

    // heuristic constant guessing max prolog length
    private static final int EXPECTED_PROLOG_LENGTH = 1000;
    private static final Logger logger = Logger.getLogger(EncodingHelper.class.getName());
    
    /**
     * Returns the Java encoding name for the specified IANA encoding name.
     * 
     * @param ianaEncoding
     * @return
     */
    public static String getIANA2JavaMapping(String ianaEncoding) {
        String java = encodingIANA2JavaMap.get (ianaEncoding.toUpperCase ());
        return java == null ? ianaEncoding : java;
    }
    
    /**
     * Returns the IANA encoding name for the specified Java encoding name.    
     * 
     * @param ianaEncoding
     * @return
     */
    public static String getJava2IANAMapping(String javaEncoding) {
        String iana = encodingJava2IANAMap.get (javaEncoding);
        return iana == null ? javaEncoding : iana;
    }
        
    /** Detect input stream encoding.
    * The stream stays intact.
    * @return java encoding names ("UTF8", "ASCII", etc.) or null
    * if the stream is not markable or enoding cannot be detected.
    */
    public static String detectEncoding(InputStream in) throws IOException {

        if (! in.markSupported()) {            
            logger.log(Level.WARNING, "EncodingHelper got unmarkable stream: " + in.getClass()); // NOI18N
            return null;
        }

        try {
            in.mark(EXPECTED_PROLOG_LENGTH);

            byte[] bytes = new byte[EXPECTED_PROLOG_LENGTH];
            for (int i = 0; i<bytes.length; i++) {
                try {
                    int datum = in.read();
                    if (datum == -1) break;
                    bytes[i] = (byte) datum;
                } catch (EOFException ex) {
                }
            }

            String enc = autoDetectEncoding(bytes);
            if (enc == null) return null;
            
            enc = detectDeclaredEncoding(bytes, enc);
            if (enc == null) return null;
            
            return getIANA2JavaMapping(enc);
        } finally {
            in.reset();
        }
    }

        
    /**
     * @return Java encoding family identifier or <tt>null</tt> for unrecognized
     */
    public static String autoDetectEncoding(byte[] buf) throws IOException {
        

        if (buf.length >= 4) {
            switch (buf[0]) {
                case 0:  
                    // byte order mark of (1234-big endian) or (2143) USC-4
                    // or '<' encoded as UCS-4 (1234, 2143, 3412) or UTF-16BE 
                    if (buf[1] == (byte)0x3c && buf[2] == (byte)0x00 && buf[3] == (byte)0x3f) {
                        return "UnicodeBigUnmarked";
                    }
                    // else it's probably UCS-4
                    break;

                case 0x3c:
                    switch (buf[1]) {
                        // First character is '<'; could be XML without
                        // an XML directive such as "<hello>", "<!-- ...", // NOI18N
                        // and so on.
                        
                        // 3c 00 3f 00 UTF-16 little endian
                        case 0x00:
                            if (buf [2] == (byte)0x3f && buf [3] == (byte)0x00) {
                                return  "UnicodeLittleUnmarked";
                            }                            
                            break;

                        // 3c 3f 78 6d == ASCII and supersets '<?xm'
                        case '?':
                            if (buf [2] == 'x' && buf [3] == 'm') {
                                return  "UTF8"; // NOI18N
                            }
                            break;
                    }
                    break;

                // 4c 6f a7 94 ... some EBCDIC code page
                case 0x4c:
                    if (buf[1] == (byte)0x6f && buf[2] == (byte)0xa7 && buf[3] == (byte)0x94) {
                        return "Cp037"; // NOI18N
                    }                     
                    break;

                // UTF-16 big-endian marked
                case (byte)0xfe:
                    if (buf[1] == (byte)0xff && (buf[2] != 0 || buf[3] != 0)) {
                        return  "UnicodeBig"; // NOI18N
                    }
                    break;

                // UTF-16 little-endian marked
                case (byte)0xff:
                    if (buf[1] == (byte)0xfe && (buf[2] != 0 || buf[3] != 0)) {                        
                        return "UnicodeLittle"; // NOI18N
                    }
                    break;
                    
                // UTF-8 byte order mark
                case (byte)0xef:
                    if (buf[1] == (byte)0xbb && buf[2] == (byte)0xbf) {
                        return "UTF8";  //NOI18N
                    }
                    break;
                    
            }
        }

        return null;
    }

    /**
     * Look for encoding='' anyway stop at <tt>?></tt>
     * @return found encoding or null if none declared
     */
    public static String detectDeclaredEncoding(byte[] data, String baseEncoding) throws IOException {

        StringBuffer buf = new StringBuffer();
        Reader r;
        char delimiter = '"';

        r = new InputStreamReader(new ByteArrayInputStream(data), baseEncoding);
        try {
            for (int c = r.read(); c != -1; c = r.read()) {
                buf.append((char)c);
            }
        } catch (IOException ex) {
            // EOF of data out of boundary
            // dont care try to guess from given data
        }
        
        String s = buf.toString();
        
        int iend = s.indexOf("?>");
        iend = iend == -1 ? s.length() : iend;
        
        int iestart = s.indexOf("encoding");
        if (iestart == -1 || iestart > iend) return null;
        
        char[] chars = s.toCharArray();
        
        int i = iestart;
        
        for (; i<iend; i++) {
            if (chars[i] == '=') break;
        }
        
        for (; i<iend; i++) {
            if (chars[i] == '\'' || chars[i] == '"') {
                delimiter = chars[i];
                break;
            }
                
        }

        i++;
        
        int ivalstart = i;
        for (; i<iend; i++) {
            if (chars[i] == delimiter) {
                return new String(chars, ivalstart, i - ivalstart);
            }
        }
        
        return null;
    }
    
    /**
     * Parse MIME content type for attributes. 
     */
    public static String parseMIMECharSet(String mime) {
        
        final String CHARSET = "charset";
        
        if (mime != null) {
            int i;

            mime = mime.toLowerCase ();
            i = mime.indexOf (';');
            if (i != -1) {
                String	attributes;

                attributes = mime.substring (i + 1);
                mime = mime.substring (0, i);

                // use "charset=..." if it's available // NOI18N
                i = attributes.indexOf (CHARSET); // NOI18N
                if (i != -1) {
                    attributes = attributes.substring (i + CHARSET.length());
                    // strip out subsequent attributes
                    if ((i = attributes.indexOf (';')) != -1)
                        attributes = attributes.substring (0, i);
                    // find start of value
                    if ((i = attributes.indexOf ('=')) != -1) {
                        attributes = attributes.substring (i + 1);
                        // strip out rfc822 comments
                        if ((i = attributes.indexOf ('(')) != -1)
                            attributes = attributes.substring (0, i);
                        // double quotes are optional
                        if ((i = attributes.indexOf ('"')) != -1) {
                            attributes = attributes.substring (i + 1);
                            attributes = attributes.substring (0,
                                                               attributes.indexOf ('"'));
                        }
                        return attributes.trim();
                        // XXX "\;", "\)" etc were mishandled above // NOI18N
                    }
                }
            }
        } 
        
        return null;        
    }

    
    
    /** Document itself is encoded as Unicode, but in
    * the document prolog is an encoding attribute.
    * @return java encoding names ("UTF8", "ASCII", etc.) or null if no guess
    */
    public static String detectEncoding(Document doc) throws IOException {
        if (doc == null) return null;

        try {
            String text = doc.getText(0,
                                      doc.getLength() > EXPECTED_PROLOG_LENGTH ?
                                      EXPECTED_PROLOG_LENGTH : doc.getLength()
                                     );
            InputStream in = new ByteArrayInputStream(text.getBytes());
            return detectEncoding(in);

        } catch (BadLocationException ex) {
            throw new RuntimeException(ex.toString());
        }

    }
    

    /**
     * IANA to Java encoding mappings
     */
    final static Map<String, String> encodingIANA2JavaMap = new TreeMap<String, String>();
    final static Map<String, String> encodingIANADescriptionMap = new TreeMap<String, String>();
    final static Map<String, String> encodingIANAAliasesMap = new TreeMap<String, String>();
    final static Map<String, String> encodingJava2IANAMap = new TreeMap<String, String>();

    /**
     * Static initialization
     */
    static {
        encodingIANA2JavaMap.put("BIG5", "Big5"); // NOI18N
        encodingIANADescriptionMap.put("BIG5", NbBundle.getMessage(EncodingHelper.class, "NAME_BIG5")); // NOI18N
        encodingIANAAliasesMap.put("BIG5", "BIG5"); // NOI18N

        encodingIANA2JavaMap.put("IBM037", "CP037");  // NOI18N
        encodingIANADescriptionMap.put("IBM037", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM037")); // NOI18N
        encodingIANAAliasesMap.put("IBM037", "IBM037"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-US", "IBM037"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-CA", "IBM037"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-NL", "IBM037"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-WT", "IBM037"); // NOI18N

        encodingIANA2JavaMap.put("IBM277", "CP277");  // NOI18N
        encodingIANADescriptionMap.put("IBM277", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM277")); // NOI18N
        encodingIANAAliasesMap.put("IBM277", "IBM277"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-DK", "IBM277"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-NO", "IBM277"); // NOI18N

        encodingIANA2JavaMap.put("IBM278", "CP278");  // NOI18N
        encodingIANADescriptionMap.put("IBM278", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM277")); // NOI18N
        encodingIANAAliasesMap.put("IBM278", "IBM278"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-FI", "IBM278"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-SE", "IBM278"); // NOI18N

        encodingIANA2JavaMap.put("IBM280", "CP280");  // NOI18N
        encodingIANADescriptionMap.put("IBM280", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM280")); // NOI18N
        encodingIANAAliasesMap.put("IBM280", "IBM280"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-IT", "IBM280"); // NOI18N

        encodingIANA2JavaMap.put("IBM284", "CP284");  // NOI18N
        encodingIANADescriptionMap.put("IBM284", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM284")); // NOI18N
        encodingIANAAliasesMap.put("IBM284", "IBM284"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-ES", "IBM284"); // NOI18N

        encodingIANA2JavaMap.put("IBM285", "CP285");  // NOI18N
        encodingIANADescriptionMap.put("IBM285", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM285")); // NOI18N
        encodingIANAAliasesMap.put("IBM285", "IBM285"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-GB", "IBM285"); // NOI18N

        encodingIANA2JavaMap.put("IBM297", "CP297");  // NOI18N
        encodingIANADescriptionMap.put("IBM297", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM297")); // NOI18N
        encodingIANAAliasesMap.put("IBM297", "IBM297"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-FR", "IBM297"); // NOI18N

        encodingIANA2JavaMap.put("IBM424", "CP424");  // NOI18N
        encodingIANADescriptionMap.put("IBM424", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM424")); // NOI18N
        encodingIANAAliasesMap.put("IBM424", "IBM424"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-HE", "IBM424"); // NOI18N

        encodingIANA2JavaMap.put("IBM500", "CP500");  // NOI18N
        encodingIANADescriptionMap.put("IBM500", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM500")); // NOI18N
        encodingIANAAliasesMap.put("IBM500", "IBM500"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-CH", "IBM500"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-BE", "IBM500"); // NOI18N

        encodingIANA2JavaMap.put("IBM870", "CP870");  // NOI18N
        encodingIANADescriptionMap.put("IBM870", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM870")); // NOI18N
        encodingIANAAliasesMap.put("IBM870", "IBM870"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-ROECE", "IBM870"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-YU", "IBM870"); // NOI18N

        encodingIANA2JavaMap.put("IBM871", "CP871");  // NOI18N
        encodingIANADescriptionMap.put("IBM871", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM871")); // NOI18N
        encodingIANAAliasesMap.put("IBM871", "IBM871"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-IS", "IBM871"); // NOI18N

        encodingIANA2JavaMap.put("IBM918", "CP918");  // NOI18N
        encodingIANADescriptionMap.put("IBM918", NbBundle.getMessage(EncodingHelper.class, "NAME_IBM918")); // NOI18N
        encodingIANAAliasesMap.put("IBM918", "IBM918"); // NOI18N
        encodingIANAAliasesMap.put("EBCDIC-CP-AR2", "IBM918"); // NOI18N

        encodingIANA2JavaMap.put("EUC-JP", "EUCJIS"); // NOI18N
        encodingIANADescriptionMap.put("EUC-JP", NbBundle.getMessage(EncodingHelper.class, "NAME_EUC-JP")); // NOI18N
        encodingIANAAliasesMap.put("EUC-JP", "EUC-JP"); // NOI18N

        encodingIANA2JavaMap.put("EUC-KR", "KSC5601"); // NOI18N
        encodingIANADescriptionMap.put("EUC-KR", NbBundle.getMessage(EncodingHelper.class, "NAME_EUC-KR")); // NOI18N
        encodingIANAAliasesMap.put("EUC-KR", "EUC-KR");  // NOI18N

        encodingIANA2JavaMap.put("GB2312", "GB2312"); // NOI18N
        encodingIANADescriptionMap.put("GB2312", NbBundle.getMessage(EncodingHelper.class, "NAME_GB2312")); // NOI18N
        encodingIANAAliasesMap.put("GB2312", "GB2312"); // NOI18N

        encodingIANA2JavaMap.put("ISO-2022-JP", "JIS");  // NOI18N
        encodingIANADescriptionMap.put("ISO-2022-JP", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-2022-JP")); // NOI18N
        encodingIANAAliasesMap.put("ISO-2022-JP", "ISO-2022-JP"); // NOI18N

        encodingIANA2JavaMap.put("ISO-2022-KR", "ISO2022KR");   // NOI18N
        encodingIANADescriptionMap.put("ISO-2022-KR", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-2022-KR")); // NOI18N
        encodingIANAAliasesMap.put("ISO-2022-KR", "ISO-2022-KR"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-1", "8859_1");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-1", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-1")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-1", "ISO-8859-1"); // NOI18N
        encodingIANAAliasesMap.put("LATIN1", "ISO-8859-1"); // NOI18N
        encodingIANAAliasesMap.put("L1", "ISO-8859-1"); // NOI18N
        encodingIANAAliasesMap.put("IBM819", "ISO-8859-1"); // NOI18N
        encodingIANAAliasesMap.put("CP819", "ISO-8859-1"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-2", "8859_2");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-2", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-2")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-2", "ISO-8859-2"); // NOI18N
        encodingIANAAliasesMap.put("LATIN2", "ISO-8859-2"); // NOI18N
        encodingIANAAliasesMap.put("L2", "ISO-8859-2"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-3", "8859_3");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-3", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-3")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-3", "ISO-8859-3"); // NOI18N
        encodingIANAAliasesMap.put("LATIN3", "ISO-8859-3"); // NOI18N
        encodingIANAAliasesMap.put("L3", "ISO-8859-3"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-4", "8859_4");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-4", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-4")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-4", "ISO-8859-4"); // NOI18N
        encodingIANAAliasesMap.put("LATIN4", "ISO-8859-4"); // NOI18N
        encodingIANAAliasesMap.put("L4", "ISO-8859-4"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-5", "8859_5");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-5", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-5")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-5", "ISO-8859-5"); // NOI18N
        encodingIANAAliasesMap.put("CYRILLIC", "ISO-8859-5"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-6", "8859_6");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-6", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-6")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-6", "ISO-8859-6"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-7", "8859_7");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-7", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-7")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-7", "ISO-8859-7"); // NOI18N
        encodingIANAAliasesMap.put("GREEK", "ISO-8859-7"); // NOI18N
        encodingIANAAliasesMap.put("GREEK8", "ISO-8859-7"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-8", "8859_8");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-8", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-8")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-8", "ISO-8859-8"); // NOI18N
        encodingIANAAliasesMap.put("HEBREW", "ISO-8859-8"); // NOI18N

        encodingIANA2JavaMap.put("ISO-8859-9", "8859_9");     // NOI18N
        encodingIANADescriptionMap.put("ISO-8859-9", NbBundle.getMessage(EncodingHelper.class, "NAME_ISO-8859-9")); // NOI18N
        encodingIANAAliasesMap.put("ISO-8859-9", "ISO-8859-9"); // NOI18N
        encodingIANAAliasesMap.put("LATIN5", "ISO-8859-9"); // NOI18N
        encodingIANAAliasesMap.put("L5", "ISO-8859-9"); // NOI18N

        encodingIANA2JavaMap.put("KOI8-R", "KOI8_R"); // NOI18N
        encodingIANADescriptionMap.put("KOI8-R", NbBundle.getMessage(EncodingHelper.class, "NAME_KOI8-R")); // NOI18N
        encodingIANAAliasesMap.put("KOI8-R", "KOI8-R"); // NOI18N

        encodingIANADescriptionMap.put("US-ASCII", NbBundle.getMessage(EncodingHelper.class, "NAME_ASCII")); // NOI18N
        encodingIANAAliasesMap.put("ASCII", "US-ASCII");  // NOI18N
        encodingIANAAliasesMap.put("US-ASCII", "US-ASCII");  // NOI18N
        encodingIANAAliasesMap.put("ISO646-US", "US-ASCII");  // NOI18N
        encodingIANAAliasesMap.put("IBM367", "US-ASCII");  // NOI18N
        encodingIANAAliasesMap.put("CP367", "US-ASCII");  // NOI18N

        encodingIANA2JavaMap.put("UTF-8", "UTF8");  // NOI18N
        encodingIANADescriptionMap.put("UTF-8", NbBundle.getMessage(EncodingHelper.class, "NAME_UTF-8")); // NOI18N
        encodingIANAAliasesMap.put("UTF-8", "UTF-8"); // NOI18N

        encodingIANA2JavaMap.put("UTF-16", "Unicode"); // NOI18N
        encodingIANADescriptionMap.put("UTF-16", NbBundle.getMessage(EncodingHelper.class, "NAME_UTF-16")); // NOI18N
        encodingIANAAliasesMap.put("UTF-16", "UTF-16");  // NOI18N


        Iterator<String> iter = encodingIANA2JavaMap.keySet().iterator();
        String key;
        while (iter.hasNext()) {
            key = iter.next();
            encodingJava2IANAMap.put(encodingIANA2JavaMap.get(key), key);
        }
        encodingIANA2JavaMap.put("US-ASCII", "8859_1"); // NOI18N    
    }
    
}
