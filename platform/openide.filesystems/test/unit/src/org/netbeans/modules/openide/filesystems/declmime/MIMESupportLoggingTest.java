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

package org.netbeans.modules.openide.filesystems.declmime;

import java.util.logging.LogRecord;
import org.openide.filesystems.*;
import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

public class MIMESupportLoggingTest extends NbTestCase {
    private TestLookup lookup;
    public MIMESupportLoggingTest(String testName) {
        super(testName);
    }

    static {
        System.setProperty("org.openide.util.Lookup", MIMESupportLoggingTest.TestLookup.class.getName());
        assertEquals(MIMESupportLoggingTest.TestLookup.class, Lookup.getDefault().getClass());
        
    }
    
    protected @Override void setUp() throws Exception {
        lookup = (MIMESupportLoggingTest.TestLookup)Lookup.getDefault();
        lookup.init();
    }


    public void testLogging() throws Exception {
        MIMESupportLoggingTest.TestResolver testR = new MIMESupportLoggingTest.TestResolver("a/a");
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).isEmpty());

        FileObject fo = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "mysterious.lenka");
        FileObject fo2 = FileUtil.createData(FileUtil.createMemoryFileSystem().getRoot(), "mysterious.dita");

        assertEquals("content/unknown",fo.getMIMEType());

        lookup.setLookups(testR);
        assertTrue(Lookup.getDefault().lookupAll(MIMEResolver.class).contains(testR));
        Logger log = Logger.getLogger("org.openide.filesystems.MIMESupport");
        log.setLevel(Level.FINE);
        ReadingHandler handler = new ReadingHandler();
        log.addHandler(handler);

        assertEquals(testR.getMime(),fo2.getMIMEType());
        assertFalse("File read " + fo2, handler.wasRead());
        
        testR.read = true;
        assertEquals(testR.getMime(),fo.getMIMEType());
        assertTrue("File read " + fo, handler.wasRead());
    }

    private static final class TestResolver extends MIMEResolver {
        private final String mime;
        private boolean read;
        
        private TestResolver(String mime) {            
            this.mime = mime;
        }
        
        public String findMIMEType(FileObject fo) {
            if (read) {
                try {
                    fo.getInputStream().read(new byte[10]);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
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

    static class ReadingHandler extends Handler {
        
        private boolean read = false;
        
        @Override
        public void publish(LogRecord record) {
            if ("MSG_CACHED_INPUT_STREAM".equals(record.getMessage())) {
                read = true;
            }
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }

        public boolean wasRead() {
            return read;
        }
        
    }
}
