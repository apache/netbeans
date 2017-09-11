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


import java.io.InputStream;
import java.io.OutputStream;
import org.netbeans.junit.*;
import org.openide.util.Task;

/**
 * Simulate deadlock from issue 50137.
 *
 * There is expected not to call any foreign code from
 * StreamPool.
 *
 * @author Radek Matous
 */
public class StreamPool50137Test extends NbTestCase {
    /**
     * filesystem containing created instances
     */
    private FileSystem lfs;
    private FileObject testFo;

    public StreamPool50137Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(StreamPool50137Test.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {

        TestUtilHid.destroyLocalFileSystem(getName());
        lfs = new TestFileSystem ((LocalFileSystem)TestUtilHid.createLocalFileSystem(getName(), new String[]{"TestDeadlock" }));        
        testFo = lfs.findResource("TestDeadlock");
        assertNotNull(testFo);        
    }


    public void testStreamPoolDeadlock () throws Exception {
        FileLock fLock = testFo.lock();
        OutputStream os = null;
        InputStream is = null;        
        
        try {
            os = testFo.getOutputStream(fLock);
            os.close(); os = null;
            is = testFo.getInputStream();
            is.close(); is = null;            
        } finally {
            if (fLock != null) fLock.releaseLock();
            if (os != null) os.close();
            if (is != null) is.close();            
        }
    }
    
    private static final class TestFileSystem extends LocalFileSystem {
        TestFileSystem (LocalFileSystem lfs) throws Exception {
            super ();             
            this.info = new TestImplInfo (this);
            setRootDirectory(lfs.getRootDirectory());
        }                            
    }

    private static final class TestImplInfo extends LocalFileSystem.Impl  {
        private final TestFileSystem tfs;
        TestImplInfo (TestFileSystem tfs) {
            super (tfs);
            this.tfs = tfs;
            
        }
        
        public OutputStream outputStream(String name) throws java.io.IOException {
            OutputStream retValue = super.outputStream(name);
            deadlockSimulation ();            
            return retValue;
        }

        public InputStream inputStream(String name) throws java.io.FileNotFoundException {
            InputStream  retValue = super.inputStream(name);
            deadlockSimulation ();
            return retValue;
        }                        
        
        
        private void deadlockSimulation() {
            Task task = org.openide.util.RequestProcessor.getDefault().post(new Runnable () {
                public void run() {
                    StreamPool.find(tfs);
                }                
            });
            task.waitFinished();                        
        }

    }

}
  
  
  
