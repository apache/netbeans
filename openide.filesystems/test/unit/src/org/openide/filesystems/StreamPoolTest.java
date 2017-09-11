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



