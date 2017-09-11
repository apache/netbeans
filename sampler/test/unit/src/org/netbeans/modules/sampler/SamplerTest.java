/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
