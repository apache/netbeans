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

package org.netbeans.test.editor;

import java.util.Collection;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.EditorKit;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.rtf.RTFEditorKit;
import junit.framework.Test;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.junit.NbTestCase;
import org.openide.modules.ModuleInfo;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Lookup;

/**
 *
 * @author Vita Stejskal
 */
public class EditorSanityTest extends NbTestCase {

    static {
        System.setProperty("java.awt.headless", "true");
    }
    
    /** Creates a new instance of EditorSanityTest */
    public EditorSanityTest(String name) {
        super(name);
    }

    public static Test suite() {
        return
            NbModuleSuite.emptyConfiguration().addTest(Init.class).
                addTest(EditorSanityTest.class).
                clusters(".*").enableModules(".*").gui(false)
        .suite();
    }
    
    public static final class Init extends NbTestCase {
        public Init(String name) {
            super(name);
        }
        
        public void testInitBindings() {
            try {
                org.openide.awt.AcceleratorBinding.setAccelerator(null, null);
                fail("Just initialize the class, otherwise it has to throw NPE");
            } catch (NullPointerException ex) {
                // OK
            }
        }
    }


    public void testHTMLEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "text/html");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/html", kitFromJdk);
        assertTrue("Wrong JDK kit for text/html", kitFromJdk instanceof HTMLEditorKit);

        // Check that org.netbeans.modules.html.editor is available
        boolean htmlPresent = false;
        Collection<? extends ModuleInfo> modules = Lookup.getDefault().lookupAll(ModuleInfo.class);
        for(ModuleInfo info : modules) {
            if (info.getCodeNameBase().equals("org.netbeans.modules.html.editor")) {
                htmlPresent = true;
                break;
            }
        }

        if (htmlPresent) {
            // Test Netbeans kit
            EditorKit kitFromNb = CloneableEditorSupport.getEditorKit("text/html");
            assertNotNull("Can't find Nb kit for text/html", kitFromNb);
            assertEquals("Wrong Nb kit for text/html",
                "org.netbeans.modules.html.editor.api.HtmlKit", kitFromNb.getClass().getName());
        } else {
            log("Module org.netbeans.modules.html.editor not present, skipping HTMLKit test...");
        }
    }

    public void testPlainEditorKits() {
        // VIS: JEditorPane when constructed contains javax.swing.JEditorPane$PlainEditorKit
        // and calling JEP.setContenetType("text/plain") has no effect. IMO this is probably
        // a defect in JDK, becuase JEP should always honour its EditorKit registry.
        JEditorPane pane = new JEditorPane();
        pane.setEditorKit(new DefaultEditorKit() {
            public @Override String getContentType() {
                return "text/whatever";
            }
        });
        setContentTypeInAwt(pane, "text/plain");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/plain", kitFromJdk);
        assertEquals("The kit for text/plain should not be from JDK", 
            "org.netbeans.modules.editor.plain.PlainKit", kitFromJdk.getClass().getName());

        // Test Netbeans kit
        EditorKit kitFromNb = CloneableEditorSupport.getEditorKit("text/plain");
        assertNotNull("Can't find Nb kit for text/plain", kitFromNb);
        assertEquals("Wrong Nb kit for text/plain", 
            "org.netbeans.modules.editor.plain.PlainKit", kitFromNb.getClass().getName());
    }

    public void testTextRtfEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "text/rtf");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for text/rtf", kitFromJdk);
        assertTrue("Wrong JDK kit for application/rtf", kitFromJdk instanceof RTFEditorKit);
    }

    public void testApplicationRtfEditorKits() {
        JEditorPane pane = new JEditorPane();
        setContentTypeInAwt(pane, "application/rtf");
        
        // Test JDK kit
        EditorKit kitFromJdk = pane.getEditorKit();
        assertNotNull("Can't find JDK kit for application/rtf", kitFromJdk);
        assertTrue("Wrong JDK kit for application/rtf", kitFromJdk instanceof RTFEditorKit);
    }
    
    private void setContentTypeInAwt(final JEditorPane pane, final String mimeType) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {
                    pane.setContentType(mimeType);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            fail("Can't set content type in AWT: " + e.getMessage());
        }
    }
}
