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
package org.netbeans.api.queries;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;

/**
 *
 * @author Tomas Zezula
 */
public class FileEncodingQueryTest extends NbTestCase {
    static {
        // confuse the system a bit, if your system runs with UTF-8 default locale...
        //System.setProperty("file.encoding", "cp1252");
    }
    
    private final String expectedEncoding;
    private final File file;
    
    public FileEncodingQueryTest (final String name, final File file, final String expectedEncoding) {
        super (name);
        this.file = file;
        this.expectedEncoding = expectedEncoding;
    }
    
    public FileEncodingQueryTest (final String testMethod) {
        super (testMethod);
        this.file = null;
        this.expectedEncoding = null;
    }
    
    @Override
    public void setUp () throws IOException {
        clearWorkDir();
        MockServices.setServices (ContentBaseEncodingQuery.class, FileTypeBaseEncodingQuery.class);
        FileTypeBaseEncodingQuery.setExtMap(Collections.<String,Charset>emptyMap(), StandardCharsets.UTF_8);
    }
    
    
    public static Test suite () throws Exception {
        URL url = FileEncodingQueryTest.class.getResource("data");        
        File dataFolder = BaseUtilities.toFile(URI.create(url.toExternalForm()));
        assertTrue(dataFolder.isDirectory());
        File dataFile = new File (dataFolder, "data.properties");
        assertTrue (dataFile.exists());
        Properties data = new Properties ();
        InputStream in = new FileInputStream (dataFile);
        try {
            data.load(in);
        } finally {
            in.close();
        }
        NbTestSuite suite = new NbTestSuite ();
        for (Map.Entry<Object,Object> e : data.entrySet()) {
            String name = (String) e.getKey();
            String encoding = (String) e.getValue();
            File c = new File (dataFolder,name);
            if (c.isFile()) {
                suite.addTest(new FileEncodingQueryTest(FileEncodingQueryTest.class.getSimpleName()+" "+c.getName(),c,encoding));
            }
        }
        suite.addTestSuite(FileEncodingQueryTest.class);
        return suite;
    }
    
    @Override
    protected void runTest() throws Throwable {
        if (file == null && expectedEncoding == null) {
            super.runTest();
            return;
        } else {
            final Listener listener = new Listener ();
            Logger.getLogger(FileEncodingQuery.class.getName()).setLevel(Level.FINEST);
            Logger.getLogger(FileEncodingQuery.class.getName()).addHandler(listener);
            try {
                listener.reset();
                performTest(this.file, this.expectedEncoding);
                CharsetDecoder decoder = listener.getDecoder();
                assertNotNull(decoder);
                String usedEncoding = getRealCharsetName(decoder);
                assertEquals(usedEncoding, this.expectedEncoding);
                CharsetEncoder encoder = listener.getEncoder();
                assertNotNull(encoder);
                usedEncoding = getRealCharsetName(encoder);
                assertEquals(usedEncoding, this.expectedEncoding);
            } finally {
                Logger.getLogger(FileEncodingQuery.class.getName()).removeHandler(listener);
            }
        }
    } 
    
    
    public void testPartialRead () throws IOException {
        final String testString = "test-1test-2test-3test-4";
        final File test = new File(getWorkDir(), "testPartialRead.orig");
        PrintWriter _out = new PrintWriter (new FileWriter (test));
        _out.print (testString);
        _out.close();        
        FileObject fo = FileUtil.toFileObject(test);        
        assertNotNull(fo);
        
        
        Charset encoding = FileEncodingQuery.getEncoding(fo);
        InputStream ins = fo.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FileUtil.copy(ins, baos);
        ins.close();
        byte[] arr = baos.toByteArray();
        baos.close();
        baos = null;
        
        final Reader in = new InputStreamReader (new ByteArrayInputStream(arr),encoding);
        final ByteArrayOutputStream outbs = new ByteArrayOutputStream();
        final Writer out = new OutputStreamWriter (outbs,encoding);
        char[] buffer = new char[6];
        int len;
        while ( (len = in.read(buffer)) >0) {
            out.write (buffer);
        }        
        in.close();
        out.close();
        assertEquals(testString, new String(outbs.toByteArray()));
        
    }
    
    public void testLongUnicode () throws IOException {
        URL url = FileEncodingQueryTest.class.getResource("data");        
        File dataFolder = BaseUtilities.toFile(URI.create(url.toExternalForm()));
        assertTrue(dataFolder.isDirectory());
        File dataFile = new File (dataFolder, "longUnicode.txt");
        assertTrue (dataFile.exists());
        File test = new File(getWorkDir(), "longUnicode.orig");
        copyToWorkDir(dataFile, test);
        FileObject from = FileUtil.toFileObject(test);
        assertNotNull(from);
        FileObject to = from.getParent().createData("longUnicode","res");
        assertNotNull(to);
        copyFile (from, to);
        assertEquals (from, to);        
    }
    
    public void testLongUnicode2 () throws IOException {
        URL url = FileEncodingQueryTest.class.getResource("data");        
        File dataFolder = BaseUtilities.toFile(URI.create(url.toExternalForm()));
        assertTrue(dataFolder.isDirectory());
        File dataFile = new File (dataFolder, "longUnicode2.txt");
        assertTrue (dataFile.exists());
        File test = new File(getWorkDir(), "longUnicode2.orig");
        copyToWorkDir(dataFile, test);
        FileObject from = FileUtil.toFileObject(test);
        assertNotNull(from);
        FileObject to = from.getParent().createData("longUnicode2","res");
        assertNotNull(to);
        copyFile (from, to);
        assertEquals (from, to);        
    }
    
    public void testDefaultEncodingOnSFSIsUTF8() throws IOException {
        FileObject fo = FileUtil.createData(FileUtil.getConfigRoot(), "some.file");
        Charset enc = FileEncodingQuery.getEncoding(fo);
        assertEquals("UTF-8", enc.toString());
    }
    
    private void performTest(File templ, String expectedEncoding) throws Exception {
        File test = new File(getWorkDir(), templ.getName() +".orig");
        copyToWorkDir(templ, test);
        FileObject from = FileUtil.toFileObject(test);
        assertNotNull(from);
        FileObject to = from.getParent().createData(templ.getName(),"res");
        assertNotNull(to);
        copyFile (from, to);
        assertEquals (from, to);
    }
    
    private void assertEquals (final FileObject from, final FileObject to) throws IOException {
        assertEquals(from.getSize(), to.getSize());
        final InputStream fromIn = from.getInputStream();
        try {
            final InputStream toIn = to.getInputStream();
            try {
                byte[] fromData = new byte[1024];
                byte[] toData = new byte[1024];
                
                int fromLen, toLen;
                
                while ((fromLen=fromIn.read(fromData, 0, fromData.length))>0) {
                    toLen = toIn.read(toData, 0, toData.length);
                    assertEquals(fromLen, toLen);
                    for (int i=0; i<fromLen; i++) {
                        assertEquals(fromData[i], toData[i]);
                    }
                }
                
            } finally {
                toIn.close();
            }
        } finally {
            fromIn.close ();
        }
    }
    
    private String getRealCharsetName (CharsetDecoder decoder) {
        if (decoder instanceof ContentBaseEncodingQuery.SniffingDecoder) {
            decoder = ((ContentBaseEncodingQuery.SniffingDecoder)decoder).decoder;
            assertNotNull(decoder);
        }
        return decoder.charset().name();
    }        
    
    private String getRealCharsetName (CharsetEncoder encoder) {
        if (encoder instanceof ContentBaseEncodingQuery.SniffingEncoder) {
            encoder = ((ContentBaseEncodingQuery.SniffingEncoder)encoder).encoder;
            assertNotNull(encoder);
        }
        return encoder.charset().name();
    }
    
    private void copyFile (FileObject from, FileObject to) throws IOException {
        Charset ci = FileEncodingQuery.getEncoding(from);
        Reader in = new BufferedReader (new InputStreamReader (from.getInputStream(),ci));
        try {
            FileLock lck = to.lock();
            try {
                Charset co = FileEncodingQuery.getEncoding(to);
                Writer out = new BufferedWriter (new OutputStreamWriter (to.getOutputStream(lck),co));
                try {
                    char[] data = new char[1024];
                    int len;
                    while ((len=in.read(data, 0, data.length))>0) {
                        out.write(data, 0, len);
                    }
                } finally {
                    out.close();
                }
            } finally {
                lck.releaseLock();
            }
        } finally {
            in.close();
        }
    }
    
    
    private void copyToWorkDir(File resource, File toFile) throws IOException {
        InputStream is = new FileInputStream(resource);
        OutputStream outs = new FileOutputStream(toFile);
        int read;
        while ((read = is.read()) != (-1)) {
            outs.write(read);
        }
        outs.close();
        is.close();
    }
    
    
    public static class ContentBaseEncodingQuery extends FileEncodingQueryImplementation {
        
    
        public Charset getEncoding(FileObject file) {
            return new SniffingCharSet ();
        }
        
        
        private static class SniffingCharSet extends Charset {
            
            public SniffingCharSet () {
                super ("UTF-8", new String[0]);
            }
        
            public boolean contains(Charset c) {
                return false;
            }

            public CharsetDecoder newDecoder() {
                return new SniffingDecoder (this);
            }

            public CharsetEncoder newEncoder() {
                return new SniffingEncoder (this);
            }
        }
        
        private static class SniffingEncoder extends CharsetEncoder {
            
            private CharBuffer buffer = CharBuffer.allocate(4*1024);
            private CharsetEncoder encoder;
            private boolean cont;
            
            public SniffingEncoder (Charset cs) {
                super (cs, 1,2);
            }
                        
            
            protected CoderResult encodeLoop(CharBuffer in, ByteBuffer out) {
                if (buffer == null) {
                    assert encoder != null;
                    return encoder.encode(in, out, false);
                }
                if (cont) {
                    return flushHead (in,out);
                }
                if (buffer.remaining() == 0 || (buffer.position() > 0 && in.limit() == 0)) {
                   return handleHead (in,out);
               }
               else if (buffer.remaining() < in.remaining()) {
                   int limit = in.limit();
                   in.limit(in.position()+buffer.remaining());
                   buffer.put(in);
                   in.limit(limit);
                   return handleHead (in, out);
               }
               else {
                   buffer.put(in);
                   return CoderResult.UNDERFLOW;
               }
            }
            
            private CoderResult handleHead (CharBuffer in, ByteBuffer out) {
                String encoding = getEncoding ();
                if (encoding == null) {          
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                }
                else {
                    Charset c = Charset.forName(encoding);
                    encoder = c.newEncoder();
                    return flushHead(in, out);
                }
            }
                
            private CoderResult flushHead (CharBuffer in , ByteBuffer out) {
                buffer.flip();
                CoderResult r = encoder.encode(buffer,out, in==null);
                if (r.isOverflow()) {
                    cont = true;
                    return r;
                }
                buffer = null;
                if (in == null) {
                    return r;
                }
                return encoder.encode(in, out, false);
            }
            
            private String getEncoding () {
                String sb = buffer.asReadOnlyBuffer().flip().toString();
                int st = sb.indexOf("encoding=\"");
                if (st == -1) {
                    return null;
                }
                int et = sb.indexOf('\"', st+10);
                if (et == -1) {
                    return null;
                }
                return sb.substring(st+10, et);
            }
            
            @Override
            protected CoderResult implFlush(ByteBuffer out) {
                CoderResult res;
                if (buffer != null) {
                    if (cont) {
                        res = flushHead(null, out);
                        return res;
                    }
                    else {
                        res = handleHead(null, out);
                        return res;
                    }
                }
                else {
                    CharBuffer empty = (CharBuffer) CharBuffer.allocate(0).flip();
                    encoder.encode(empty, out, true);
                }
                res = encoder.flush(out);
                return res;
            }
            
            @Override
            protected void implReset() {
                if (encoder != null) {
                    encoder.reset();
                }
            }           
        }
        
        private static class SniffingDecoder extends CharsetDecoder {
            
            private ByteBuffer buffer = ByteBuffer.allocate(4*1024);
            private CharsetDecoder decoder;
            private boolean cont;
            
            public SniffingDecoder (Charset cs) {
                super (cs,1,2);
            }
                                            
            protected CoderResult decodeLoop(ByteBuffer in, CharBuffer out) {
                if (buffer == null) {
                    assert decoder != null;
                    return decoder.decode(in, out, false);
                }
                if (cont) {
                    return flushHead (in,out);
                }
                if (buffer.remaining() == 0) {
                   return handleHead (in,out);
               }
               else if (buffer.remaining() < in.remaining()) {
                   int limit = in.limit();
                   in.limit(in.position()+buffer.remaining());
                   buffer.put(in);
                   in.limit(limit);
                   return handleHead (in, out);
               }
               else {
                   buffer.put(in);
                   return CoderResult.UNDERFLOW;
               }
            }
            
            private CoderResult handleHead (ByteBuffer in, CharBuffer out) {
                String encoding = getEncoding ();
                if (encoding == null) {          
                    buffer = null;
                    throwUnknownEncoding();
                    return null;
                }
                else {
                    Charset c = Charset.forName(encoding);
                    decoder = c.newDecoder();
                    return flushHead(in, out);
                }
            }
                
            private CoderResult flushHead (ByteBuffer in , CharBuffer out) {
                buffer.flip();
                CoderResult r = decoder.decode(buffer,out, in==null);
                if (r.isOverflow()) {
                    cont = true;
                    return r;
                }
                buffer = null;
                if (in == null) {
                    return r;
                }
                return decoder.decode(in, out, false);
            }
            
            private String getEncoding () {
                byte[] arr = buffer.array();
                String sb = new String (arr,0,buffer.position());
                int st = sb.indexOf("encoding=\"");
                if (st == -1) {
                    return null;
                }
                int et = sb.indexOf('\"', st+10);
                if (et == -1) {
                    return null;
                }
                return sb.substring(st+10, et);
            }
            
            @Override
            protected CoderResult implFlush(CharBuffer out) {
                CoderResult res;
                if (buffer != null) {
                    if (cont) {
                        res = flushHead(null, out);
                        return res;
                    }
                    else {
                        res = handleHead(null, out);
                        return res;
                    }
                }
                else {
                    ByteBuffer empty = (ByteBuffer) ByteBuffer.allocate(0).flip();
                    decoder.decode(empty, out, true);
                }
                res = decoder.flush(out);
                return res;
            }
            
            @Override
            protected void implReset() {
                if (decoder != null) {
                    decoder.reset();
                }
            }            
            
        }
    }
    
    public static class FileTypeBaseEncodingQuery extends FileEncodingQueryImplementation {
        
        private static Map<String,Charset> map = Collections.emptyMap();
        private static Charset defaultEncoding;
        
        protected static void setExtMap (Map<String,Charset> _map, Charset _defaultEncoding) {
            if (_map == null) {
                _map = Collections.emptyMap();
            }
            map = _map;
            defaultEncoding = _defaultEncoding;
        }
            
        public Charset getEncoding(FileObject file) {
            String ext = file.getExt();
            Charset result = map.get(ext);
            if (result == null) {
                result = defaultEncoding;
            }
            return result;
        }
    }
    
    static class Listener extends Handler {        
        
        private CharsetEncoder encoder;
        private CharsetDecoder decoder;
        
        public void reset () {
            this.encoder = null;
            this.decoder = null;
        }
        
        public CharsetEncoder getEncoder() {
            return this.encoder;
        }
        
        public CharsetDecoder getDecoder() {
            return this.decoder;
        }           

        public void publish(final LogRecord record) {
            final String message = record.getMessage();
            if (FileEncodingQuery.ENCODER_SELECTED.equals(message)) {
                if (this.encoder != null) {
                    throw new IllegalStateException ();
                }
                Object[] params = record.getParameters();
                assert params.length == 1;
                assert params[0] instanceof CharsetEncoder;
                this.encoder = (CharsetEncoder) params[0];
            }
            else if (FileEncodingQuery.DECODER_SELECTED.equals(message)) {
                if (this.decoder != null) {
                    throw new IllegalStateException ();
                }
                Object[] params = record.getParameters();
                assert params.length == 1;
                assert params[0] instanceof CharsetDecoder;
                this.decoder = (CharsetDecoder) params[0];
            }
        }

        public void flush() {            
        }

        public void close() throws SecurityException {            
        }
    }
        
}
