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

package org.netbeans.modules.html;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Document;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.html.palette.HtmlPaletteFactory;
import org.openide.cookies.EditorCookie;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;

public class EncodingTest extends NbTestCase {
    
    public EncodingTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        // invoke the pallete factory in advance to prevent deadlock
        HtmlPaletteFactory.getHtmlPalette();
        Utils.setUp();
        
        // to help the loader to recognize our files
        FileUtil.setMIMEType("html", "text/html");
    }
    
    /** Loads an empty file.
     */
    public void testLoadEmptyFile () throws Exception {
        checkEncoding (null, "empty.html", true);
    }
    
    /** Loades a file that does not specify an encoding.
     */
    public void testLoadOfNoEncoding () throws Exception {
        checkEncoding (null, "sample.html", true);
    }
    
    /** Loades a file that does not specify an encoding.
     */
    public void testLoadOfWrongEncoding () throws Exception {
        checkEncoding (null, "wrongencoding.html", false);
    }
    
    /** Test load of UTF-8 encoding.
     */
    public void testEncodingUTF8 () throws Exception {
        checkEncoding ("UTF-8", "UTF8.html", true);
    }
    /** Test load of UTF-8 encoding specified in ' ' instead of " "
     */
    public void testEncodingApostrof () throws Exception {
        checkEncoding ("UTF-8", "apostrof.html", true);
    }
    
    /** Test load of UTF-8 encoding specified in ' ' instead of " "
     * with a text that is followed with "
     */
    public void testEncodingApostrofWithQuote () throws Exception {
        checkEncoding ("UTF-8", "apostrofwithoutquote.html", true);
    }

    public void testEncodingCaching() throws Exception {
        final Logger log = Logger.getLogger(HtmlDataObject.class.getName());
        class TestHandler extends Handler {
            private final Pattern pattern = Pattern.compile("^HtmlDataObject.getFileEncoding (non)?cached .*$");   //NOI18N
            Boolean cached;
            @Override
            public void publish(LogRecord record) {
                final String message = record.getMessage();
                final Matcher matcher = pattern.matcher(message);
                if (matcher.matches()) {
                    cached = matcher.group(1) == null ? Boolean.TRUE : Boolean.FALSE;
                }
            }
            @Override
            public void flush() {
            }
            @Override
            public void close() throws SecurityException {
            }
        }
        final TestHandler handler = new TestHandler();
        log.addHandler(handler);
        final Level origLevel = log.getLevel();
        log.setLevel(Level.FINEST);
        try {
            FileObject data = FileUtil.createData (FileUtil.toFileObject(getWorkDir()), "UTF8.html"); //NOI18N
            copy("UTF8.html",data); //NOI18N
            handler.cached = null;
            read(data);
            assertFalse("Encoding should be calculated",handler.cached);    //NOI18N
            handler.cached = null;
            read(data);
            assertTrue("Encoding should be cached",handler.cached);        //NOI18N
            //Modify file
            copy("UTF8.html",data); //NOI18N
            handler.cached = null;
            read(data);
            assertFalse("Encoding should be calculated",handler.cached);    //NOI18N
            handler.cached = null;
            read(data);
            assertTrue("Encoding should be cached",handler.cached);        //NOI18N
        } finally {
            log.setLevel(origLevel);
            log.removeHandler(handler);
        }
    }

    private void copy(final String res, final FileObject data) throws Exception {
        final InputStream is = getClass ().getResourceAsStream ("data/"+res);   //NOI18N
        try {
            assertNotNull (res+" should exist", is);    //NOI18N
            FileLock lock = data.lock();
            try {
                OutputStream os = data.getOutputStream (lock);
                try {
                    FileUtil.copy (is, os);
                } finally {
                    os.close ();
                }
            } finally {
                lock.releaseLock ();
            }
        } finally {
            is.close ();
        }
    }

    
    private void read(final FileObject data) throws IOException {
        final Charset cs = FileEncodingQuery.getEncoding(data);
        final BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream(), cs));
        try {
            CharBuffer buffer = CharBuffer.allocate(1024);
            while (in.read(buffer)>0) {
                buffer.clear();
            }
        } finally {
            in.close();
        }
    }

    /** @param enc expected encoding
     *  @param res resource path
     *  @param withCmp should also document content be compared?
     */
    private void checkEncoding (String enc, String res, boolean withCmp) throws Exception {    
        InputStream is = getClass ().getResourceAsStream ("data/"+res);
        assertNotNull (res+" should exist", is);
        
        FileObject data = FileUtil.createData (FileUtil.toFileObject(getWorkDir()), res);
        FileLock lock = data.lock();
        OutputStream os = data.getOutputStream (lock);
        FileUtil.copy (is, os);
        is.close ();
        os.close ();
        lock.releaseLock ();
        
        DataObject obj = DataObject.find (data);
        
        assertEquals ("Must be HtmlDataObject", HtmlDataObject.class, obj.getClass ());
        
        OpenCookie open = (OpenCookie)obj.getCookie (OpenCookie.class);
        assertNotNull("There is an open cookie", open);
        
        open.open ();
        
        EditorCookie ec = (EditorCookie)obj.getCookie (EditorCookie.class);
        assertNotNull ("There is an editor cookie", ec);
        
        Document doc = ec.openDocument();
        assertNotNull ("Need a document", doc);
        
        
        Reader r;
        if (enc == null) {
            r = new InputStreamReader (getClass ().getResourceAsStream ("data/"+res));
        } else {
            r = new InputStreamReader (getClass ().getResourceAsStream ("data/"+res), enc);
        }
           
        if (!withCmp)
            return;
        
        compareDoc (r, doc);
        r.close ();
        
        doc.insertString (0, "X", null);
        doc.remove (0, 1);
        
        SaveCookie sc = (SaveCookie)obj.getCookie(SaveCookie.class);
        assertNotNull ("Document is modified", sc);
        sc.save ();
       
        InputStream i1 = getClass ().getResourceAsStream ("data/"+res);
        InputStream i2 = obj.getPrimaryFile().getInputStream();
        compareStream (i1, i2);
        i2.close ();
        i1.close ();
        
    }
    
    /** Compares content of document and reader
     */
    private static void compareDoc (Reader r, Document doc) throws Exception {
        for (int i = 0; i < doc.getLength(); i++) {
            String ch = doc.getText (i, 1);
            assertEquals ("Really one char", 1, ch.length());
            
            char fromStream = (char)r.read ();
            if (fromStream != ch.charAt (0) && fromStream == (char)13 && ch.charAt (0) == (char)10) {
                // new line in document is always represented by 13, read next character
                fromStream = (char)r.read ();
            }
            
            
            assertEquals ("Stream and doc should be the same on index " + i, (int)fromStream, (int)ch.charAt (0));
        }
    }
    
    /** Compares content of two streams. 
     */
    /*package*/ static void compareStream (InputStream i1, InputStream i2) throws Exception {
        for (int i = 0; true; i++) {
            int c1 = i1.read ();
            int c2 = i2.read ();

            assertEquals (i + "th bytes are different", c1, c2);
            
            if (c1 == -1) return;
        }
    }
    
}
