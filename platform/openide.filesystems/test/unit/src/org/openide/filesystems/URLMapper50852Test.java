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

        @Override
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
  
  
  
