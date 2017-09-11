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


import org.netbeans.junit.*;

import java.net.URL;
import java.io.File;
import org.openide.util.BaseUtilities;

/**
 * Simulates issue 50852.
 *
 * @author Radek Matous
 */
public class URLMapper50852Test extends NbTestCase {
    private static URL testURL = null;
    private static final MyThread resultsComputingThread = new MyThread();
    private static final MyThread secondThread = new MyThread();
    static MyURLMapper MAPPER_INSTANCE = null;


    public URLMapper50852Test(String name) {
        super(name);
    }

    public static void main(String[] args) throws Exception {
        junit.textui.TestRunner.run(new NbTestSuite(URLMapper50852Test.class));
    }

    /**
     * Setups variables.
     */
    protected void setUp() throws Exception {
        File workdir = getWorkDir();
        testURL = BaseUtilities.toURI(workdir).toURL();
        System.setProperty("org.openide.util.Lookup", "org.openide.filesystems.URLMapper50852Test$Lkp");
        MAPPER_INSTANCE = new MyURLMapper ();
        Lkp lkp = (Lkp)org.openide.util.Lookup.getDefault();
        lkp.getInstanceContent().add(MAPPER_INSTANCE);
    }

    public void testURLMapper50852 () throws Exception {
        resultsComputingThread.start();
        Thread.sleep(1000);        
        secondThread.start();
        Thread.sleep(1000);
        
        for (int i = 0; i < 5; i++) {
            if (!resultsComputingThread.isFinished() && secondThread.isFinished() ) {
                break;
            }
            Thread.sleep(1000);
        }
        assertFalse (resultsComputingThread.isFinished());
        assertTrue ("Even if a thread is blocked in the computation, another one can proceed", secondThread.isFinished());        
        assertTrue ("and successfully call into the mapper", MAPPER_INSTANCE.called);
        synchronized (testURL) {
            testURL.notifyAll();
        }
        
    }


    public static final class Lkp extends org.openide.util.lookup.AbstractLookup {
        private org.openide.util.lookup.InstanceContent ic;
        public Lkp() {
            this(new org.openide.util.lookup.InstanceContent());
        }

        private Lkp(org.openide.util.lookup.InstanceContent ic) {
            super(ic);
            this.ic = ic;
        }

        org.openide.util.lookup.InstanceContent getInstanceContent () {
            return ic;
        }
        
        protected void beforeLookup(Template template) {
            super.beforeLookup(template);

            synchronized (testURL) {
                if (Thread.currentThread() == resultsComputingThread) {
                    try {
                        testURL.wait();
                    } catch (InterruptedException e) {
                        fail ();
                    }
                }
            }
        }

    } // end of Lkp

    public static final class MyURLMapper extends URLMapper  {                
        private boolean called = false;
        
        
        public URL getURL(FileObject fo, int type) {
            called = true;
            return null;
        }

        public FileObject[] getFileObjects(URL url) {
            called = true;
            return new FileObject[0];
        }

        public String findMIMEType(FileObject fo) {
            called = true;
            return null;
        }

        boolean isCalled() {
            return called;
        }
    }

    private static class MyThread extends Thread {
        private boolean finished = false;
        
        public void run() {
            super.run();
            URLMapper.findFileObject(testURL);
            finished = true;
        }

        boolean isFinished() {
            return finished;
        }
    }
}
  
  
  
