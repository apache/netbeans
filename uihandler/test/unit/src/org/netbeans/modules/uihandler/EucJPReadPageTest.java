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
