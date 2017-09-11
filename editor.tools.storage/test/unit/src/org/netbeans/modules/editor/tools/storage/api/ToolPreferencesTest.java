/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.editor.tools.storage.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author lahvac
 */
public class ToolPreferencesTest extends NbTestCase {
    
    public ToolPreferencesTest(String testName) {
        super(testName);
    }
    
    public void testWriteToPreferences() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File settingsFile = new File(wd, "settings.xml");
        ToolPreferences prefs = ToolPreferences.from(settingsFile.toURI());
        assertNull(prefs.getPreferences("test", "text/x-test").get("test", null));
        prefs.getPreferences("test", "text/x-test").put("test", "testValue");
        assertEquals("testValue", ToolPreferences.from(settingsFile.toURI()).getPreferences("test", "text/x-test").get("test", null));
        prefs.save();
        assertEquals("testValue", ToolPreferences.from(settingsFile.toURI()).getPreferences("test", "text/x-test").get("test", null));
        
        Reference<ToolPreferences> ref = new WeakReference<>(prefs);
        prefs = null;
        assertGC("Must not hold onto the ToolPreferences", ref);
    }
    
    public void testEscaping() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File settingsFile = new File(wd, "settings.xml");
        ToolPreferences prefs = ToolPreferences.from(settingsFile.toURI());
        String key = "\"'<>&";
        String value = "&><'\"";
        assertNull(prefs.getPreferences("test", "text/x-test").get(key, null));
        prefs.getPreferences("test", "text/x-test").put(key, value);
        assertEquals(value, ToolPreferences.from(settingsFile.toURI()).getPreferences("test", "text/x-test").get(key, null));
        prefs.save();
    }
    
    public void testDontSaveEmptyNodes() throws Exception {
        clearWorkDir();
        File wd = getWorkDir();
        File settingsFile = new File(wd, "settings.xml");
        ToolPreferences prefs = ToolPreferences.from(settingsFile.toURI());
        Preferences p = prefs.getPreferences("test", "text/x-test");
        p.node("a/b/e/f");
        p.node("a/b/c/d").put("test", "test");
        prefs.save();
        StringBuilder content = new StringBuilder();
        try (Reader r = new InputStreamReader(new FileInputStream(settingsFile), "UTF-8")) {
            int read;
            
            while ((read = r.read()) != (-1)) {
                content.append((char) read);
            }
        }
        assertEquals("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                     "<!DOCTYPE configuration PUBLIC \"-//NetBeans//DTD Tool Configuration 1.0//EN\" \"http://www.netbeans.org/dtds/ToolConfiguration-1_0.dtd\">\n" +
                     "<configuration>\n" +
                     "    <tool kind=\"test\" type=\"text/x-test\">\n" +
                     "        <node name=\"a\">\n" +
                     "            <node name=\"b\">\n" +
                     "                <node name=\"c\">\n" +
                     "                    <node name=\"d\">\n" +
                     "                        <attribute name=\"test\" value=\"test\"/>\n" +
                     "                    </node>\n" +
                     "                </node>\n" +
                     "            </node>\n" +
                     "        </node>\n" +
                     "    </tool>\n" +
                     "</configuration>\n",
                     content.toString().replace("\r", ""));
    }
    
}
