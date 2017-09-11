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


import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.netbeans.junit.*;

/**
 * @author Radek Matous
 */
public class FileObject75826Test extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private LocalFileSystem lfs;
    private FileObject testFo;

    public FileObject75826Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(FileObject75826Test.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = (LocalFileSystem)TestUtilHid.createLocalFileSystem(getName(), new String[]{getName() });
        lfs = new TestFileSystem(lfs, getName());
        testFo = lfs.findResource(getName());
        assertNotNull(testFo);
    }

    public void testOutputStreamFiresIOException() throws IOException {
        OutputStream os = null;
        FileLock lock = null;
        try {
            os = testFo.getOutputStream();
            fail();
        } catch (IOException ex) {}
        try {
            lock = testFo.lock();
            assertNotNull(lock);
            assertTrue(lock.isValid());
        } finally {
            if (lock != null && lock.isValid()) {
                lock.releaseLock();
            }
        }
    }

    public void testCloseStreamFiresIOException() throws IOException {
        FileLock lock = null;
        OutputStream os = testFo.getOutputStream();
        try {
            os.close();
            fail();
        } catch (IOException ex) {}
        try {
            lock = testFo.lock();
            assertNotNull(lock);
            assertTrue(lock.isValid());
        } finally {
            if (lock != null && lock.isValid()) {
                lock.releaseLock();
            }
        }
    }

    private static final class TestFileSystem extends LocalFileSystem {
        TestFileSystem(LocalFileSystem lfs, String testName) throws Exception {
            super();
            if ("testOutputStreamFiresIOException".equals(testName)) {
                this.info = new LocalFileSystem.Impl(this) {
                    public OutputStream outputStream(String name) throws java.io.IOException {
                        throw new IOException();
                    }
                };
            } else if ("testCloseStreamFiresIOException".equals(testName)) {
                this.info = new LocalFileSystem.Impl(this) {
                    public OutputStream outputStream(String name) throws java.io.IOException {
                        return new FilterOutputStream(super.outputStream(name)) {
                            public void close() throws IOException {
                                throw new IOException();
                            }
                        };
                    }
                };
            }
            setRootDirectory(lfs.getRootDirectory());
        }
    }
}
