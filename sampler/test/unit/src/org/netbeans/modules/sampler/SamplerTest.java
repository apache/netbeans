/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.sampler;

import java.awt.Dialog;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import org.junit.*;
import static org.junit.Assert.*;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;

/**
 *
 * @author Tomas Hurka
 */
public class SamplerTest {

    @BeforeClass
    public static void setUpClass() throws Exception {
        //register DialogDisplayer which "pushes" Yes option in the document save dialog
        MockServices.setServices(DD.class);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of createSampler method, of class Sampler.
     */
    @Test
    public void testCreateSampler() {
        System.out.println("createSampler");
        String name = "test";
        Sampler result = Sampler.createSampler(name);
        assertNotNull(result);
    }

    /**
     * Test of createManualSampler method, of class Sampler.
     */
    @Test
    public void testCreateManualSampler() {
        System.out.println("createManualSampler");
        String name = "gentest";
        Sampler result = Sampler.createManualSampler(name);
        assertNotNull(result);
    }

    /**
     * Test of cancel method, of class Sampler.
     */
    @Test
    public void testCancel() {
        System.out.println("cancel");
        Sampler instance = Sampler.createManualSampler("cancel");
        instance.start();
        instance.cancel();
    }

    /**
     * Test of stopAndWriteTo method, of class Sampler.
     */
    @Test
    public void testStopAndWriteTo() throws IOException {
        System.out.println("stopAndWriteTo");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        Sampler instance = Sampler.createSampler("cancel");
        instance.start();
        instance.stopAndWriteTo(dos);
        dos.close();
        // there should no data in out, since stopAndWriteTo is 
        // invoked immediately after start
        assertTrue(out.size() == 0);
    }
   /**
     * Test of stopAndWriteTo method, of class Sampler.
     */
    @Test
    public void testStopAndWriteTo1() throws IOException {
        System.out.println("stopAndWriteTo1");
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);
        Sampler instance = Sampler.createSampler("cancel");
        instance.start();
        longRunningMethod();
        instance.stopAndWriteTo(dos);
        dos.close();
        // make sure we have some sampling data
        assertTrue(out.size() > 0);
    }

    private void longRunningMethod() {
        for (int i=0; i<100;i++) {
            try {
                Thread.sleep(30);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    /**
     * Test of stop method, of class Sampler.
     */
    @Test
    public void testStop() {
        System.out.println("stop");
        Sampler instance = Sampler.createManualSampler("stop");
        DD.hasData = false;
        instance.start();
        longRunningMethod();
        instance.stop();
        assert(DD.hasData);
    }

    /** Our own dialog displayer.
     */
    public static final class DD extends DialogDisplayer {
        static boolean hasData;
        
        @Override
        public Dialog createDialog(DialogDescriptor descriptor) {
            throw new IllegalStateException ("Not implemented");
        }
        
        @Override
        public Object notify(NotifyDescriptor descriptor) {
           hasData = true;
           return null;
        }
        
    } // end of DD    
    
}
