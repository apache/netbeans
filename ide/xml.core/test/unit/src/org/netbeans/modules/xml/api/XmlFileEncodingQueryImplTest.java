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

package org.netbeans.modules.xml.api;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import junit.framework.Test;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.NbTestSuite;
import org.netbeans.spi.queries.FileEncodingQueryImplementation;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Tomas Zezula
 */
public final class XmlFileEncodingQueryImplTest extends NbTestCase {
    
    private final URLConnection c;
    private final String expectedEncoding;
    private static final String NULL_ENCODING = "<null>";           //NOI18N
    
    public XmlFileEncodingQueryImplTest(String testName) {
        super(testName);
        this.c = null;
        this.expectedEncoding = null;
    }
    
    public XmlFileEncodingQueryImplTest(final String name, final URLConnection c, final String expectedEncoding) {
        super (name);
        this.c = c;
        this.expectedEncoding = expectedEncoding;
    }
    
    public static Test suite () throws Exception {
        final URL dataFile = XmlFileEncodingQueryImplTest.class.getResource("data/data.properties");        //NOI18N
        final Properties data = new Properties ();
        final InputStream in = dataFile.openStream();
        try {
            data.load(in);
        } finally {
            in.close();
        }
        final NbTestSuite suite = new NbTestSuite ();
        for (final Map.Entry<Object,Object> e : data.entrySet()) {
            final String name = (String) e.getKey();
            final String encoding = (String) e.getValue();
            final URL testFile = XmlFileEncodingQueryImplTest.class.getResource("data/"+name);              //NOI18N            
            try {            
                final URLConnection testConnection = testFile.openConnection();
                testConnection.connect();
                suite.addTest(new XmlFileEncodingQueryImplTest( XmlFileEncodingQueryImplTest.class.getSimpleName()+" "+name,testConnection,encoding));
            } catch (IOException ioe) {
                //Missing data file, skeep it
            }
        }
        suite.addTestSuite(XmlFileEncodingQueryImplTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        this.clearWorkDir();
        MockServices.setServices(MockXMLFileEncodingQuery.class);
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    @Override
    protected void runTest() throws Throwable {
        if (c == null && expectedEncoding == null) {
            super.runTest();
        } else {
            final Listener listener = new Listener ();
            Logger.getLogger(XmlFileEncodingQueryImpl.class.getName()).setLevel(Level.FINEST);
            Logger.getLogger(XmlFileEncodingQueryImpl.class.getName()).addHandler(listener);
            try {
                listener.reset();
                performTest(c);
                CharsetDecoder decoder = listener.getDecoder();
                CharsetEncoder encoder = listener.getEncoder();
                if (NULL_ENCODING.equals(this.expectedEncoding)) {
                    assertNull(decoder);
                    assertNull(encoder);
                }
                else {
                    assertNotNull(decoder);
                    String usedEncoding = decoder.charset().name();
                    assertEquals(usedEncoding, this.expectedEncoding);
                    assertNotNull(encoder);
                    usedEncoding = encoder.charset().name();
                    assertEquals(usedEncoding, this.expectedEncoding);
                }                                
            } finally {
                Logger.getLogger(XmlFileEncodingQueryImpl.class.getName()).removeHandler(listener);
            }
        }
    }
    
    private void performTest (final URLConnection template) throws IOException {
        final File test = new File(getWorkDir(), getName() +".orig");      //NOI18N
        copyToWorkDir(template, test);
        final FileObject from = FileUtil.toFileObject(test);
        assertNotNull(from);
        final FileObject to = from.getParent().createData(getName(),"res");    //NOI18N
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
    
    private void copyToWorkDir(final URLConnection resource, final File toFile) throws IOException {
        final InputStream is = resource.getInputStream();
        try {
            final OutputStream outs = new FileOutputStream(toFile);
            try {
                int read;
                while ((read = is.read()) != (-1)) {
                    outs.write(read);
                }
            }finally {
                outs.close();
            }
        } finally {
            is.close();
        }
    }
    
    private void copyFile (final FileObject from, final FileObject to) throws IOException {        
        final Charset ci = FileEncodingQuery.getEncoding(from);
        final Reader in = new BufferedReader (new InputStreamReader (from.getInputStream(),ci));
        try {
            final FileLock lck = to.lock();
            try {
                final Charset co = FileEncodingQuery.getEncoding(to);
                final Writer out = new BufferedWriter (new OutputStreamWriter (to.getOutputStream(lck),co));
                try {
                    final char[] data = new char[1024];
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

    public void testSingleton() {        
        final XmlFileEncodingQueryImpl q1 = XmlFileEncodingQueryImpl.singleton();
        final XmlFileEncodingQueryImpl q2 = XmlFileEncodingQueryImpl.singleton();
        assertTrue(q1 == q2);
    }
    
    
    private static final class Listener extends Handler {        
        
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
            if (XmlFileEncodingQueryImpl.ENCODER_SELECTED.equals(message)) {
                if (this.encoder != null) {
                    throw new IllegalStateException ();
                }
                Object[] params = record.getParameters();
                assert params.length == 1;
                assert params[0] instanceof CharsetEncoder;
                this.encoder = (CharsetEncoder) params[0];
            }
            else if (XmlFileEncodingQueryImpl.DECODER_SELECTED.equals(message)) {
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

    public static class MockXMLFileEncodingQuery extends FileEncodingQueryImplementation {

        @Override
        public Charset getEncoding(FileObject file) {
            return XmlFileEncodingQueryImpl.singleton().getEncoding(file);
        }
        
    }

}
