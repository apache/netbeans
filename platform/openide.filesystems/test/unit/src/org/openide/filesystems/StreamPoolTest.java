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

package org.openide.filesystems;


import java.io.*;
import java.util.logging.Level;
import org.netbeans.junit.*;

public class StreamPoolTest extends NbTestCase {
    private TestFileSystem lfs;
    private FileObject testFo;

    public StreamPoolTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();

        lfs = new TestFileSystem (getWorkDir());
        
        FileOutputStream os = new FileOutputStream(new File(getWorkDir(), "tst.file.txt"));
        os.write(13);
        os.close();
        lfs.refresh(true);
        testFo = lfs.findResource("tst.file.txt");
        assertNotNull(testFo);
    }


    public void testDontExcludeWhenExceptionFromClose() throws Exception {
        lfs.throwEx = true;
        OutputStream os = testFo.getOutputStream();
        os.write(10);
        try {
            os.close();
            fail("should throw an exception");
        } catch (IOException ex) {
            // OK
        }
        InputStream is = testFo.getInputStream();
        assertNotNull("Still we are able to get input stream", is);
        assertEquals("And read it", 13, is.read());
        assertEquals("Up until the end", -1, is.read());
        is.close();
    }
    public void testDontPrintInterruptedException() throws Exception {
        OutputStream os = testFo.getOutputStream();
        os.write(10);
        CharSequence log = Log.enable("", Level.INFO);
        Thread.currentThread().interrupt();
        InputStream is = testFo.getInputStream();
        assertTrue("Remains interrupted", Thread.interrupted());
        
        if (log.toString().contains("InterruptedException")) {
            fail("No interrupted exceptions printed:\n" + log);
        }
        try {
            is.read();
            fail("Cannot read, file is locked");
        } catch (FileAlreadyLockedException ex) {
            assertNotNull("OK", ex);
        }
        
        is.close();
        os.close();
    }


    private static final class TestFileSystem extends LocalFileSystem {
        boolean throwEx;
        
        TestFileSystem (File dir) throws Exception {
            super ();
            setRootDirectory(dir);
        }

        @Override
        protected OutputStream outputStream(String name) throws IOException {
            return new OutputStream() {
                @Override
                public void write(int b) throws IOException {
                }

                @Override
                public void close() throws IOException {
                    if (throwEx) {
                        throw new IOException("Always thrown.");
                    }
                }
            };
        }
    }
}



