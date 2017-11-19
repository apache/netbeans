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

package org.netbeans.modules.uihandler;

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.JButton;
import java.util.Locale;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;

/**
 *
 * @author Jaroslav Tulach
 */
public class EucJPReadPageTest extends NbTestCase {
    
    public static Test suite() {
        return GraphicsEnvironment.isHeadless() ? new TestSuite() : new TestSuite(EucJPReadPageTest.class);
    }

    public EucJPReadPageTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.user", getWorkDirPath());
        clearWorkDir();
        MemoryURL.initialize();
        DD.d = null;
        MockServices.setServices(DD.class);
        Locale.setDefault(new Locale("te", "ST"));
        
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);

        // setup the listing
        installer.restored();
        
        Installer.dontWaitForUserInputInTests();
    }

    @Override
    protected void tearDown() throws Exception {
        Installer installer = Installer.findObject(Installer.class, true);
        assertNotNull(installer);
        installer.doClose();
    }
    
    public void testKFranksFile() throws Exception {
        doKFranksFile("index_ja.html");
    }
    @RandomlyFails // NB-Core-Build #6193: DD.d assigned
    public void testKFranksErrorFile() throws Exception {
        doKFranksFile("error_ja.html");
    }
    private void doKFranksFile(String f) throws Exception {
        String jaText = "\u30b3\u30de\u30f3\u30c9";
        
        InputStream is = getClass().getResourceAsStream(f);
        assertNotNull("index_ja found", is);
        
        MemoryURL.registerURL("memory://kun.html", is);
        
        boolean res = Installer.displaySummary("KUN", true, false,true);
        assertFalse("Close options was pressed", res);
        assertNotNull("DD.d assigned", DD.d);
        
        List<Object> data = Arrays.asList(DD.d.getOptions());
        assertEquals("three objects: " + data, 3, DD.d.getOptions().length);
        for (Object o : DD.d.getOptions()) {
            assertEquals("is jbutton", JButton.class, o.getClass());
            JButton b = (JButton)o;
            String t = b.getText();
            
            if (t.indexOf(jaText) == -1) {
                failUTF("Expecting the right text (" + jaText + ": " + t);
            }
        }
        
    }
    
    private static void failUTF(String err) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < err.length(); i++) {
            if (err.charAt(i) < 128) {
                sb.append(err.charAt(i));
            } else {
                sb.append("\\u" + Integer.toString(err.charAt(i), 16));
            }
        }
        fail(sb.toString());
    }
    
    public void testNoEucFile() throws Exception {
        doNoEucInTheFile("index_ja.html");
    }
    public void testNoEucErrorFile() throws Exception {
        doNoEucInTheFile("error_ja.html");
    }
    private void doNoEucInTheFile(String f) throws Exception {
        String jaText = "\u30b3\u30de\u30f3\u30c9";
        
        InputStream is = getClass().getResourceAsStream(f);
        assertNotNull("index_ja found", is);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        Installer.copyWithEncoding(is, os, Collections.<String, String>emptyMap());
        
        assertEquals("No euc:\n" + os, -1, os.toString().toLowerCase().indexOf("euc-jp"));
        if (os.toString().indexOf("UTF-8") == -1 && os.toString().indexOf("utf-8") == -1) {
            fail("utf-8 should be there:\n" + os);
        }
    }
    
    public static final class DD extends DialogDisplayer {
        static NotifyDescriptor d;
        
        public Object notify(NotifyDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            return NotifyDescriptor.CLOSED_OPTION;
        }

        public Dialog createDialog(DialogDescriptor descriptor) {
            assertNull(d);
            d = descriptor;
            
            return new DialogImpl(d, new Frame());
        }

        private static class DialogImpl extends Dialog {
            NotifyDescriptor d;
            
            private DialogImpl(NotifyDescriptor d, Frame owner) {
                super(owner);
                this.d = d;
            }

            @Override
            public synchronized void setVisible(boolean b) {
                assertFalse(isModal());
                if (d != null) {
                    d.setValue(NotifyDescriptor.CLOSED_OPTION);
                    d = null;
                }
            }
        }
        
    }
}
